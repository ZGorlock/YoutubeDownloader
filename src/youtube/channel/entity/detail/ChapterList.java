/*
 * File:    ChapterList.java
 * Package: youtube.channel.entity.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity.detail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import commons.time.DateTimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.util.Utils;

/**
 * Defines the Chapter List of a Video Entity.
 */
public class ChapterList extends ArrayList<ChapterList.Chapter> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChapterList.class);
    
    
    //Constants
    
    /**
     * The regex pattern for a timestamp string.
     */
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("(?<timestamp>(?:\\d{1,2}:)?\\d{1,2}:\\d{2})");
    
    /**
     * The regex pattern for a Chapter definition.
     */
    private static final Pattern CHAPTER_DEFINITION_PATTERN = Pattern.compile("^\\s*(\\d+\\s*[\\-=.)]\\s*)?(?<titlePart1>.*?)\\s*[\\-=]?\\s*[(\\[]?" + TIMESTAMP_PATTERN.pattern() + "[])]?\\s*[\\-=]?\\s*(?<titlePart2>.*?)\\s*$");
    
    
    //Constructors
    
    /**
     * Creates the Chapter List for a Video Entity.
     *
     * @param description The description of the Video Entity.
     * @param duration    The duration of the Video Entity.
     */
    public ChapterList(String description, Long duration) {
        Optional.ofNullable(description).map(StringUtility::splitLines)
                .stream().flatMap(Collection::stream)
                .filter(e -> !e.isEmpty()).filter(e -> e.contains(":"))
                .filter(e -> e.matches("^.*" + TIMESTAMP_PATTERN.pattern() + ".*$"))
                .map(Chapter::new)
                .sorted(Comparator.comparing(o -> o.startTime))
                .forEachOrdered(this::add);
        
        if (!isEmpty()) {
            if (get(0).startTime != 0) {
                clear();
            } else {
                get(size() - 1).endTime = duration;
                IntStream.range(0, size()).forEach(i ->
                        get(i).link(ListUtility.getOrNull(this, (i - 1)), ListUtility.getOrNull(this, (i + 1))));
            }
        }
    }
    
    /**
     * Creates the Chapter List for a Video Entity.
     *
     * @param description The description of the Video Entity.
     */
    public ChapterList(String description) {
        this(description, null);
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Chapter.
     */
    public static class Chapter {
        
        //Fields
        
        /**
         * The title of the Chapter.
         */
        public String title;
        
        /**
         * The timestamp of the Chapter.
         */
        public String timestamp;
        
        /**
         * The start time of the Chapter, in seconds.
         */
        public Long startTime;
        
        /**
         * The end time of the Chapter, in seconds.
         */
        public Long endTime;
        
        /**
         * The previous Chapter.
         */
        public Chapter previous;
        
        /**
         * The next Chapter.
         */
        public Chapter next;
        
        
        //Constructors
        
        /**
         * Creates a Chapter.
         *
         * @param line The description line defining the Chapter.
         */
        public Chapter(String line) {
            Optional.ofNullable(line)
                    .map(CHAPTER_DEFINITION_PATTERN::matcher).filter(Matcher::matches)
                    .ifPresent(chapterDefinitionMatcher -> {
                        this.title = cleanTitle(chapterDefinitionMatcher.group("titlePart1"), chapterDefinitionMatcher.group("titlePart2"));
                        this.timestamp = chapterDefinitionMatcher.group("timestamp");
                        this.startTime = DateTimeUtility.durationStampToDuration(chapterDefinitionMatcher.group("timestamp")) / 1000L;
                    });
        }
        
        
        //Methods
        
        /**
         * Links a Chapter to the surrounding chapters.
         *
         * @param previous The previous Chapter, or null.
         * @param next     The next Chapter, or null.
         */
        private void link(Chapter previous, Chapter next) {
            this.previous = previous;
            this.next = next;
            this.endTime = Optional.ofNullable(next).map(e -> e.startTime).orElse(endTime);
        }
        
        /**
         * Cleans the title of the Chapter.
         *
         * @param titlePart1 The first part of the title.
         * @param titlePart2 The second part of the title.
         * @return The cleaned title.
         */
        private String cleanTitle(String titlePart1, String titlePart2) {
            return Utils.cleanVideoTitle((titlePart1 + titlePart2)
                    .replace(":", "-"));
        }
        
        /**
         * Returns the duration of the Chapter.
         *
         * @return The duration of the Chapter.
         */
        public long getDuration() {
            return endTime - startTime;
        }
        
        /**
         * Returns the url parameter to add to jump to the Chapter.
         *
         * @return The url parameter.
         */
        public String getUrlParameter() {
            return "&t=" + startTime;
        }
        
        /**
         * Returns a string representation of the Chapter.
         *
         * @return A string representation of the Chapter.
         */
        @Override
        public String toString() {
            return timestamp + " -> " + title;
        }
        
    }
    
}
