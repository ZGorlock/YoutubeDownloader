/*
 * File:    FilterProcess.java
 * Package: youtube.process.macro
 * Author:  Zachary Gill
 */

package youtube.process.macro;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Video;

/**
 * Provides Channel Process macros to help with filtering.
 */
public class FilterProcess {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(FilterProcess.class);
    
    
    //Static Methods
    
    /**
     * Performs a test for each entry in the video map and filters it is necessary.
     *
     * @param videoMap The video map.
     * @param check    The filter condition.
     */
    public static void forEach(Map<String, Video> videoMap, Predicate<Video> check) {
        videoMap.forEach((id, video) -> {
            if (check.test(video) && !video.channel.state.blocked.contains(id)) {
                video.channel.state.blocked.add(id);
            }
        });
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings.
     *
     * @param videoMap The video map.
     * @param search   The set of search strings.
     */
    public static void contains(Map<String, Video> videoMap, List<String> search) {
        forEach(videoMap, (video ->
                search.stream().anyMatch(e -> video.title.contains(e))));
    }
    
    /**
     * Filters videos in the video map if the title contains a search string.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void contains(Map<String, Video> videoMap, String search) {
        contains(videoMap, List.of(search));
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of search strings.
     *
     * @param videoMap The video map.
     * @param search   The set of search strings.
     */
    public static void notContains(Map<String, Video> videoMap, List<String> search) {
        forEach(videoMap, (video ->
                search.stream().noneMatch(e -> video.title.contains(e))));
    }
    
    /**
     * Filters videos in the video map if the title does not contain a search string.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void notContains(Map<String, Video> videoMap, String search) {
        notContains(videoMap, List.of(search));
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The set of search strings.
     */
    public static void containsIgnoreCase(Map<String, Video> videoMap, List<String> search) {
        forEach(videoMap, (video ->
                search.stream().anyMatch(e -> video.title.toLowerCase().contains(e.toLowerCase()))));
    }
    
    /**
     * Filters videos in the video map if the title contains a search string, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void containsIgnoreCase(Map<String, Video> videoMap, String search) {
        containsIgnoreCase(videoMap, List.of(search));
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of search strings, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The set of search strings.
     */
    public static void notContainsIgnoreCase(Map<String, Video> videoMap, List<String> search) {
        forEach(videoMap, (video ->
                search.stream().noneMatch(e -> video.title.toLowerCase().contains(e.toLowerCase()))));
    }
    
    /**
     * Filters videos in the video map if the title does not contain a search string, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void notContainsIgnoreCase(Map<String, Video> videoMap, String search) {
        notContainsIgnoreCase(videoMap, List.of(search));
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The set of regex search strings.
     */
    public static void regexContains(Map<String, Video> videoMap, List<String> regexSearch) {
        forEach(videoMap, (video ->
                regexSearch.stream().anyMatch(e -> video.title.matches("^.*" + e + ".*$"))));
    }
    
    /**
     * Filters videos in the video map if the title contains a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexContains(Map<String, Video> videoMap, String regexSearch) {
        regexContains(videoMap, List.of(regexSearch));
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The set of regex search strings.
     */
    public static void regexNotContains(Map<String, Video> videoMap, List<String> regexSearch) {
        forEach(videoMap, (video ->
                regexSearch.stream().noneMatch(e -> video.title.matches("^.*" + e + ".*$"))));
    }
    
    /**
     * Filters videos in the video map if the title does not contain a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotContains(Map<String, Video> videoMap, String regexSearch) {
        regexNotContains(videoMap, List.of(regexSearch));
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The set of regex search strings.
     */
    public static void regexContainsIgnoreCase(Map<String, Video> videoMap, List<String> regexSearch) {
        forEach(videoMap, (video ->
                regexSearch.stream().anyMatch(e -> video.title.matches("(?i)^.*" + e + ".*$"))));
    }
    
    /**
     * Filters videos in the video map if the title contains a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexContainsIgnoreCase(Map<String, Video> videoMap, String regexSearch) {
        regexContainsIgnoreCase(videoMap, List.of(regexSearch));
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of regex search strings, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The set of regex search strings.
     */
    public static void regexNotContainsIgnoreCase(Map<String, Video> videoMap, List<String> regexSearch) {
        forEach(videoMap, (video ->
                regexSearch.stream().noneMatch(e -> video.title.matches("(?i)^.*" + e + ".*$"))));
    }
    
    /**
     * Filters videos in the video map if the title does not contain a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotContainsIgnoreCase(Map<String, Video> videoMap, String regexSearch) {
        regexNotContainsIgnoreCase(videoMap, List.of(regexSearch));
    }
    
    /**
     * Filters videos in the video map if the upload date is before a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     */
    public static void dateBefore(Map<String, Video> videoMap, Date date) {
        forEach(videoMap, (video ->
                video.date.before(date)));
    }
    
    /**
     * Filters videos in the video map if the upload date is not before a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     */
    public static void dateNotBefore(Map<String, Video> videoMap, Date date) {
        forEach(videoMap, (video ->
                !video.date.before(date)));
    }
    
    /**
     * Filters videos in the video map if the upload date is after a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     */
    public static void dateAfter(Map<String, Video> videoMap, Date date) {
        forEach(videoMap, (video ->
                video.date.after(date)));
    }
    
    /**
     * Filters videos in the video map if the upload date is not after a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     */
    public static void dateNotAfter(Map<String, Video> videoMap, Date date) {
        forEach(videoMap, (video ->
                !video.date.after(date)));
    }
    
    /**
     * Filters videos in the video map if the upload date is between two specified dates.
     *
     * @param videoMap The video map.
     * @param start    The start date.
     * @param end      The end date.
     */
    public static void dateBetween(Map<String, Video> videoMap, Date start, Date end) {
        forEach(videoMap, (video ->
                video.date.after(start) && video.date.before(end)));
    }
    
    /**
     * Filters videos in the video map if the upload date is not between two specified dates.
     *
     * @param videoMap The video map.
     * @param start    The start date.
     * @param end      The end date.
     */
    public static void dateNotBetween(Map<String, Video> videoMap, Date start, Date end) {
        forEach(videoMap, (video ->
                video.date.before(start) || video.date.after(end)));
    }
    
}
