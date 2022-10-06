/*
 * File:    RenameProcess.java
 * Package: youtube.channel.process
 * Author:  Zachary Gill
 */

package youtube.process.macro;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Video;
import youtube.conf.Color;

/**
 * Provides Channel Process macros to help with renaming.
 */
public class RenameProcess {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RenameProcess.class);
    
    
    //Static Methods
    
    /**
     * Performs an action for each entry in the video map.
     *
     * @param videoMap The video map.
     * @param action   The action.
     */
    public static void forEach(Map<String, Video> videoMap, BiConsumer<String, Video> action) {
        videoMap.forEach(action);
    }
    
    /**
     * Replaces a set of search strings with corresponding replacement strings in the title of each video in the video map.
     *
     * @param videoMap     The video map.
     * @param replacements The list of search strings and the corresponding replacement strings.
     */
    public static void replace(Map<String, Video> videoMap, List<Map.Entry<String, String>> replacements) {
        forEach(videoMap, (id, video) ->
                video.updateTitle(replacements.stream().reduce(Map.entry(video.title, ""), (s, e) ->
                        Map.entry(s.getKey().replace(e.getKey(), e.getValue()), "")).getKey()));
    }
    
    /**
     * Replaces a search string with a replacement string in the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     * @param replace  The replacement string.
     */
    public static void replace(Map<String, Video> videoMap, String search, String replace) {
        replace(videoMap, List.of(Map.entry(search, replace)));
    }
    
    /**
     * Replaces a set of regex search strings with corresponding replacement strings in the title of each video in the video map.
     *
     * @param videoMap          The video map.
     * @param regexReplacements The list of regex search strings and the corresponding replacement strings.
     */
    public static void regexReplace(Map<String, Video> videoMap, List<Map.Entry<String, String>> regexReplacements) {
        forEach(videoMap, (id, video) ->
                video.updateTitle(regexReplacements.stream().reduce(Map.entry(video.title, ""), (s, e) ->
                        Map.entry(s.getKey().replaceAll(e.getKey(), e.getValue()), "")).getKey()));
    }
    
    /**
     * Replaces a regex search string with a replacement strings in the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param regex    The regex search string.
     * @param replace  The replacement string.
     */
    public static void regexReplace(Map<String, Video> videoMap, String regex, String replace) {
        regexReplace(videoMap, List.of(Map.entry(regex, replace)));
    }
    
    /**
     * Removes a set of search strings from the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param removals The list of strings to remove.
     */
    public static void remove(Map<String, Video> videoMap, List<String> removals) {
        replace(videoMap, removals.stream().map(e -> Map.entry(e, "")).collect(Collectors.toList()));
    }
    
    /**
     * Removes a search strings from the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param search   The string to remove.
     */
    public static void remove(Map<String, Video> videoMap, String search) {
        replace(videoMap, search, "");
    }
    
    /**
     * Removes a set of regex search strings from the title of each video in the video map.
     *
     * @param videoMap      The video map.
     * @param regexRemovals The list of regex strings to remove.
     */
    public static void regexRemove(Map<String, Video> videoMap, List<String> regexRemovals) {
        regexReplace(videoMap, regexRemovals.stream().map(e -> Map.entry(e, "")).collect(Collectors.toList()));
    }
    
    /**
     * Removes a regex search string from the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param regex    The regex string to remove.
     */
    public static void regexRemove(Map<String, Video> videoMap, String regex) {
        regexReplace(videoMap, regex, "");
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param pattern  The regex pattern.
     * @param strict   Whether to fail if a title does not match the pattern or not.
     * @param result   The resulting title pattern.
     */
    public static void pattern(Map<String, Video> videoMap, String pattern, boolean strict, String result) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final AtomicInteger index = new AtomicInteger(0);
        final Pattern groupPattern = Pattern.compile("\\(\\?<(?<name>[A-Za-z\\d]+)>");
        forEach(videoMap, (id, video) -> {
            index.incrementAndGet();
            Matcher matcher = Pattern.compile(pattern).matcher(video.title);
            if (matcher.matches()) {
                String finalResult = result;
                for (String group : groupPattern.matcher(pattern).results().map(e -> ("$" + e.group(1))).collect(Collectors.toList())) {
                    finalResult = finalResult.replace(group, (finalResult.contains(group) ? Matcher.quoteReplacement(matcher.group(group.substring(1))) : ""));
                }
                video.updateTitle(video.title.replaceAll(pattern, finalResult
                        .replace("$i", String.valueOf(index.get()))
                        .replace("$d", dateFormat.format(video.date))));
            } else if (strict) {
                System.out.println(Color.bad("The video: ") + Color.videoName(video.title) + Color.bad(" does not match the pattern: ") + Color.quoted(Color.base(pattern)));
                throw new RuntimeException();
            }
        });
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param pattern  The regex pattern.
     * @param result   The resulting title pattern.
     */
    public static void pattern(Map<String, Video> videoMap, String pattern, String result) {
        pattern(videoMap, pattern, true, result);
    }
    
    /**
     * Appends a string to the end of the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param suffix   The suffix.
     */
    public static void append(Map<String, Video> videoMap, String suffix) {
        forEach(videoMap, (id, video) ->
                video.updateTitle(video.title + suffix));
    }
    
    /**
     * Prepends a string to the beginning of the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param prefix   The prefix.
     */
    public static void prepend(Map<String, Video> videoMap, String prefix) {
        forEach(videoMap, (id, video) ->
                video.updateTitle(prefix + video.title));
    }
    
    /**
     * Appends the upload date to the end of the title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param dateFormat The format of the date.
     */
    public static void appendUploadDate(Map<String, Video> videoMap, String dateFormat) {
        forEach(videoMap, (id, video) ->
                video.updateTitle(video.title + " - " + new SimpleDateFormat(dateFormat).format(video.date)));
    }
    
}
