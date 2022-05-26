/*
 * File:    FilterProcess.java
 * Package: youtube.channel.process
 * Author:  Zachary Gill
 */

package youtube.channel.process;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import youtube.YoutubeChannelDownloader;

/**
 * Provides Channel Process macros to help with filtering.
 */
public class FilterProcess {
    
    //Static Methods
    
    /**
     * Performs a test for each entry in the video map and filters it is necessary.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param check    The filter condition.
     */
    public static void forEach(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, Predicate<YoutubeChannelDownloader.Video> check) {
        videoMap.forEach((id, video) -> {
            if (check.test(video) && !blocked.contains(id)) {
                blocked.add(id);
            }
        });
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param search   The set of search strings.
     */
    public static void contains(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, List<String> search) {
        forEach(videoMap, blocked, (video ->
                search.stream().anyMatch(e -> video.title.contains(e))));
    }
    
    /**
     * Filters videos in the video map if the title contains a search string.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param search   The search string.
     */
    public static void contains(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, String search) {
        contains(videoMap, blocked, List.of(search));
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of search strings.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param search   The set of search strings.
     */
    public static void notContains(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, List<String> search) {
        forEach(videoMap, blocked, (video ->
                search.stream().noneMatch(e -> video.title.contains(e))));
    }
    
    /**
     * Filters videos in the video map if the title does not contain a search string.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param search   The search string.
     */
    public static void notContains(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, String search) {
        notContains(videoMap, blocked, List.of(search));
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings, regardless of case.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param search   The set of search strings.
     */
    public static void containsIgnoreCase(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, List<String> search) {
        forEach(videoMap, blocked, (video ->
                search.stream().anyMatch(e -> video.title.toLowerCase().contains(e.toLowerCase()))));
    }
    
    /**
     * Filters videos in the video map if the title contains a search string, regardless of case.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param search   The search string.
     */
    public static void containsIgnoreCase(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, String search) {
        containsIgnoreCase(videoMap, blocked, List.of(search));
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of search strings, regardless of case.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param search   The set of search strings.
     */
    public static void notContainsIgnoreCase(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, List<String> search) {
        forEach(videoMap, blocked, (video ->
                search.stream().noneMatch(e -> video.title.toLowerCase().contains(e.toLowerCase()))));
    }
    
    /**
     * Filters videos in the video map if the title does not contain a search string, regardless of case.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param search   The search string.
     */
    public static void notContainsIgnoreCase(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, String search) {
        notContainsIgnoreCase(videoMap, blocked, List.of(search));
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param blocked     The list of blocked video ids.
     * @param regexSearch The set of regex search strings.
     */
    public static void regexContains(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, List<String> regexSearch) {
        forEach(videoMap, blocked, (video ->
                regexSearch.stream().anyMatch(e -> video.title.matches("^.*" + e + ".*$"))));
    }
    
    /**
     * Filters videos in the video map if the title contains a regex search string.
     *
     * @param videoMap    The video map.
     * @param blocked     The list of blocked video ids.
     * @param regexSearch The regex search string.
     */
    public static void regexContains(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, String regexSearch) {
        regexContains(videoMap, blocked, List.of(regexSearch));
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param blocked     The list of blocked video ids.
     * @param regexSearch The set of regex search strings.
     */
    public static void regexNotContains(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, List<String> regexSearch) {
        forEach(videoMap, blocked, (video ->
                regexSearch.stream().noneMatch(e -> video.title.matches("^.*" + e + ".*$"))));
    }
    
    /**
     * Filters videos in the video map if the title does not contain a regex search string.
     *
     * @param videoMap    The video map.
     * @param blocked     The list of blocked video ids.
     * @param regexSearch The regex search string.
     */
    public static void regexNotContains(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, String regexSearch) {
        regexNotContains(videoMap, blocked, List.of(regexSearch));
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings, regardless of case.
     *
     * @param videoMap    The video map.
     * @param blocked     The list of blocked video ids.
     * @param regexSearch The set of regex search strings.
     */
    public static void regexContainsIgnoreCase(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, List<String> regexSearch) {
        forEach(videoMap, blocked, (video ->
                regexSearch.stream().anyMatch(e -> video.title.matches("(?i)^.*" + e + ".*$"))));
    }
    
    /**
     * Filters videos in the video map if the title contains a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param blocked     The list of blocked video ids.
     * @param regexSearch The regex search string.
     */
    public static void regexContainsIgnoreCase(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, String regexSearch) {
        regexContainsIgnoreCase(videoMap, blocked, List.of(regexSearch));
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of regex search strings, regardless of case.
     *
     * @param videoMap    The video map.
     * @param blocked     The list of blocked video ids.
     * @param regexSearch The set of regex search strings.
     */
    public static void regexNotContainsIgnoreCase(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, List<String> regexSearch) {
        forEach(videoMap, blocked, (video ->
                regexSearch.stream().noneMatch(e -> video.title.matches("(?i)^.*" + e + ".*$"))));
    }
    
    /**
     * Filters videos in the video map if the title does not contain a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param blocked     The list of blocked video ids.
     * @param regexSearch The regex search string.
     */
    public static void regexNotContainsIgnoreCase(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, String regexSearch) {
        regexNotContainsIgnoreCase(videoMap, blocked, List.of(regexSearch));
    }
    
    /**
     * Filters videos in the video map if the upload date is before a specified date.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param date     The date.
     */
    public static void dateBefore(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, Date date) {
        forEach(videoMap, blocked, (video ->
                video.date.before(date)));
    }
    
    /**
     * Filters videos in the video map if the upload date is not before a specified date.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param date     The date.
     */
    public static void dateNotBefore(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, Date date) {
        forEach(videoMap, blocked, (video ->
                !video.date.before(date)));
    }
    
    /**
     * Filters videos in the video map if the upload date is after a specified date.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param date     The date.
     */
    public static void dateAfter(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, Date date) {
        forEach(videoMap, blocked, (video ->
                video.date.after(date)));
    }
    
    /**
     * Filters videos in the video map if the upload date is not after a specified date.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param date     The date.
     */
    public static void dateNotAfter(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, Date date) {
        forEach(videoMap, blocked, (video ->
                !video.date.after(date)));
    }
    
    /**
     * Filters videos in the video map if the upload date is between two specified dates.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param start    The start date.
     * @param end      The end date.
     */
    public static void dateBetween(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, Date start, Date end) {
        forEach(videoMap, blocked, (video ->
                video.date.after(start) && video.date.before(end)));
    }
    
    /**
     * Filters videos in the video map if the upload date is not between two specified dates.
     *
     * @param videoMap The video map.
     * @param blocked  The list of blocked video ids.
     * @param start    The start date.
     * @param end      The end date.
     */
    public static void dateNotBetween(Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> blocked, Date start, Date end) {
        forEach(videoMap, blocked, (video ->
                video.date.before(start) || video.date.after(end)));
    }
    
}
