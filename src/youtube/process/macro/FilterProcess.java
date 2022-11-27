/*
 * File:    FilterProcess.java
 * Package: youtube.process.macro
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.process.macro;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.Video;

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
     * Filters videos in the video map if the title contains any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param regex      Whether to evaluate the search strings as regex.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    @SuppressWarnings("RegExpUnexpectedAnchor")
    public static void contains(Map<String, Video> videoMap, List<String> search, boolean regex, boolean ignoreCase, boolean negate) {
        BaseProcess.filter(videoMap, video ->
                negate ^ search.stream().anyMatch(e ->
                        regex ? video.title.matches((ignoreCase ? "(?i)" : "") + "^.*" + e + ".*$") :
                        ignoreCase ? video.title.toLowerCase().contains(e.toLowerCase()) :
                        video.title.contains(e)));
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    public static void contains(Map<String, Video> videoMap, List<String> search, boolean ignoreCase, boolean negate) {
        contains(videoMap, search, !BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void contains(Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        contains(videoMap, search, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void contains(Map<String, Video> videoMap, List<String> search) {
        contains(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains a search string.
     *
     * @param videoMap   The video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void contains(Map<String, Video> videoMap, String search, boolean ignoreCase) {
        contains(videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title contains a search string.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void contains(Map<String, Video> videoMap, String search) {
        contains(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void containsIgnoreCase(Map<String, Video> videoMap, List<String> search) {
        contains(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains a search string, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void containsIgnoreCase(Map<String, Video> videoMap, String search) {
        contains(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void notContains(Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        contains(videoMap, search, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of search strings.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void notContains(Map<String, Video> videoMap, List<String> search) {
        notContains(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a search string.
     *
     * @param videoMap   The video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void notContains(Map<String, Video> videoMap, String search, boolean ignoreCase) {
        notContains(videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a search string.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void notContains(Map<String, Video> videoMap, String search) {
        notContains(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of search strings, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void notContainsIgnoreCase(Map<String, Video> videoMap, List<String> search) {
        notContains(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a search string, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void notContainsIgnoreCase(Map<String, Video> videoMap, String search) {
        notContains(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     * @param negate      Whether the condition should be negated.
     */
    public static void regexContains(Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase, boolean negate) {
        contains(videoMap, regexSearch, BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexContains(Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexContains(videoMap, regexSearch, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexContains(Map<String, Video> videoMap, List<String> regexSearch) {
        regexContains(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexContains(Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexContains(videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title contains a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexContains(Map<String, Video> videoMap, String regexSearch) {
        regexContains(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexContainsIgnoreCase(Map<String, Video> videoMap, List<String> regexSearch) {
        regexContains(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexContainsIgnoreCase(Map<String, Video> videoMap, String regexSearch) {
        regexContains(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexNotContains(Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexContains(videoMap, regexSearch, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotContains(Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotContains(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexNotContains(Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexNotContains(videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotContains(Map<String, Video> videoMap, String regexSearch) {
        regexNotContains(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of regex search strings, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotContainsIgnoreCase(Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotContains(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotContainsIgnoreCase(Map<String, Video> videoMap, String regexSearch) {
        regexNotContains(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param regex      Whether to evaluate the search strings as regex.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    public static void startsWith(Map<String, Video> videoMap, List<String> search, boolean regex, boolean ignoreCase, boolean negate) {
        regexContains(videoMap, search.stream().map(e -> "^" + (regex ? e : Pattern.quote(e))).collect(Collectors.toList()), ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    public static void startsWith(Map<String, Video> videoMap, List<String> search, boolean ignoreCase, boolean negate) {
        startsWith(videoMap, search, !BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void startsWith(Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        startsWith(videoMap, search, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of search strings.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void startsWith(Map<String, Video> videoMap, List<String> search) {
        startsWith(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with a search string.
     *
     * @param videoMap   The video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void startsWith(Map<String, Video> videoMap, String search, boolean ignoreCase) {
        startsWith(videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title starts with a search string.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void startsWith(Map<String, Video> videoMap, String search) {
        startsWith(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of search strings, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void startsWithIgnoreCase(Map<String, Video> videoMap, List<String> search) {
        startsWith(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with a search string, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void startsWithIgnoreCase(Map<String, Video> videoMap, String search) {
        startsWith(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void notStartsWith(Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        startsWith(videoMap, search, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with any of a set of search strings.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void notStartsWith(Map<String, Video> videoMap, List<String> search) {
        notStartsWith(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a search string.
     *
     * @param videoMap   The video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void notStartsWith(Map<String, Video> videoMap, String search, boolean ignoreCase) {
        notStartsWith(videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a search string.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void notStartsWith(Map<String, Video> videoMap, String search) {
        notStartsWith(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with any of a set of search strings, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void notStartsWithIgnoreCase(Map<String, Video> videoMap, List<String> search) {
        notStartsWith(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a search string, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void notStartsWithIgnoreCase(Map<String, Video> videoMap, String search) {
        notStartsWith(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     * @param negate      Whether the condition should be negated.
     */
    public static void regexStartsWith(Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase, boolean negate) {
        startsWith(videoMap, regexSearch, BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexStartsWith(Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexStartsWith(videoMap, regexSearch, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexStartsWith(Map<String, Video> videoMap, List<String> regexSearch) {
        regexStartsWith(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexStartsWith(Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexStartsWith(videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title starts with a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexStartsWith(Map<String, Video> videoMap, String regexSearch) {
        regexStartsWith(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of regex search strings, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexStartsWithIgnoreCase(Map<String, Video> videoMap, List<String> regexSearch) {
        regexStartsWith(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexStartsWithIgnoreCase(Map<String, Video> videoMap, String regexSearch) {
        regexStartsWith(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexNotStartsWith(Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexStartsWith(videoMap, regexSearch, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotStartsWith(Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotStartsWith(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexNotStartsWith(Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexNotStartsWith(videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotStartsWith(Map<String, Video> videoMap, String regexSearch) {
        regexNotStartsWith(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotStartsWithIgnoreCase(Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotStartsWith(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotStartsWithIgnoreCase(Map<String, Video> videoMap, String regexSearch) {
        regexNotStartsWith(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param regex      Whether to evaluate the search strings as regex.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    public static void endsWith(Map<String, Video> videoMap, List<String> search, boolean regex, boolean ignoreCase, boolean negate) {
        regexContains(videoMap, search.stream().map(e -> (regex ? e : Pattern.quote(e)) + "$").collect(Collectors.toList()), ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    public static void endsWith(Map<String, Video> videoMap, List<String> search, boolean ignoreCase, boolean negate) {
        endsWith(videoMap, search, !BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void endsWith(Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        endsWith(videoMap, search, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of search strings.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void endsWith(Map<String, Video> videoMap, List<String> search) {
        endsWith(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with a search string.
     *
     * @param videoMap   The video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void endsWith(Map<String, Video> videoMap, String search, boolean ignoreCase) {
        endsWith(videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title ends with a search string.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void endsWith(Map<String, Video> videoMap, String search) {
        endsWith(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of search strings, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void endsWithIgnoreCase(Map<String, Video> videoMap, List<String> search) {
        endsWith(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with a search string, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void endsWithIgnoreCase(Map<String, Video> videoMap, String search) {
        endsWith(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with any of a set of search strings.
     *
     * @param videoMap   The video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void notEndsWith(Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        endsWith(videoMap, search, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with any of a set of search strings.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void notEndsWith(Map<String, Video> videoMap, List<String> search) {
        notEndsWith(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a search string.
     *
     * @param videoMap   The video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void notEndsWith(Map<String, Video> videoMap, String search, boolean ignoreCase) {
        notEndsWith(videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a search string.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void notEndsWith(Map<String, Video> videoMap, String search) {
        notEndsWith(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with any of a set of search strings, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The list of search strings.
     */
    public static void notEndsWithIgnoreCase(Map<String, Video> videoMap, List<String> search) {
        notEndsWith(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a search string, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     */
    public static void notEndsWithIgnoreCase(Map<String, Video> videoMap, String search) {
        notEndsWith(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     * @param negate      Whether the condition should be negated.
     */
    public static void regexEndsWith(Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase, boolean negate) {
        endsWith(videoMap, regexSearch, BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexEndsWith(Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexEndsWith(videoMap, regexSearch, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexEndsWith(Map<String, Video> videoMap, List<String> regexSearch) {
        regexEndsWith(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexEndsWith(Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexEndsWith(videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title ends with a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexEndsWith(Map<String, Video> videoMap, String regexSearch) {
        regexEndsWith(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of regex search strings, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexEndsWithIgnoreCase(Map<String, Video> videoMap, List<String> regexSearch) {
        regexEndsWith(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexEndsWithIgnoreCase(Map<String, Video> videoMap, String regexSearch) {
        regexEndsWith(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexNotEndsWith(Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexEndsWith(videoMap, regexSearch, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with any of a set of regex search strings.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotEndsWith(Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotEndsWith(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexNotEndsWith(Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexNotEndsWith(videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a regex search string.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotEndsWith(Map<String, Video> videoMap, String regexSearch) {
        regexNotEndsWith(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotEndsWithIgnoreCase(Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotEndsWith(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a regex search string, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotEndsWithIgnoreCase(Map<String, Video> videoMap, String regexSearch) {
        regexNotEndsWith(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the upload date is before a specified date.
     *
     * @param videoMap      The video map.
     * @param dateCondition The date condition to filter by.
     * @param negate        Whether the condition should be negated.
     */
    public static void date(Map<String, Video> videoMap, Predicate<LocalDate> dateCondition, boolean negate) {
        BaseProcess.filter(videoMap, video ->
                Optional.ofNullable(video.date).map(e -> (negate ^ dateCondition.test(e.toLocalDate()))).orElse(false));
    }
    
    /**
     * Filters videos in the video map if the upload date is before a specified date.
     *
     * @param videoMap      The video map.
     * @param dateCondition The date condition to filter by.
     */
    public static void date(Map<String, Video> videoMap, Predicate<LocalDate> dateCondition) {
        date(videoMap, dateCondition, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is before a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     * @param negate   Whether the condition should be negated.
     */
    public static void dateBefore(Map<String, Video> videoMap, LocalDate date, boolean negate) {
        date(videoMap, videoDate -> videoDate.isBefore(date), negate);
    }
    
    /**
     * Filters videos in the video map if the upload date is before a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     */
    public static void dateBefore(Map<String, Video> videoMap, LocalDate date) {
        dateBefore(videoMap, date, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is not before a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     */
    public static void dateNotBefore(Map<String, Video> videoMap, LocalDate date) {
        dateBefore(videoMap, date, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is after a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     * @param negate   Whether the condition should be negated.
     */
    public static void dateAfter(Map<String, Video> videoMap, LocalDate date, boolean negate) {
        date(videoMap, videoDate -> videoDate.isAfter(date), negate);
    }
    
    /**
     * Filters videos in the video map if the upload date is after a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     */
    public static void dateAfter(Map<String, Video> videoMap, LocalDate date) {
        dateAfter(videoMap, date, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is not after a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     */
    public static void dateNotAfter(Map<String, Video> videoMap, LocalDate date) {
        dateAfter(videoMap, date, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     * @param negate   Whether the condition should be negated.
     */
    public static void dateEquals(Map<String, Video> videoMap, LocalDate date, boolean negate) {
        date(videoMap, videoDate -> videoDate.isEqual(date), negate);
    }
    
    /**
     * Filters videos in the video map if the upload date is a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     */
    public static void dateEquals(Map<String, Video> videoMap, LocalDate date) {
        dateEquals(videoMap, date, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is not a specified date.
     *
     * @param videoMap The video map.
     * @param date     The date.
     */
    public static void dateNotEquals(Map<String, Video> videoMap, LocalDate date) {
        dateEquals(videoMap, date, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is between two specified dates.
     *
     * @param videoMap The video map.
     * @param start    The start date.
     * @param end      The end date.
     * @param negate   Whether the condition should be negated.
     */
    public static void dateBetween(Map<String, Video> videoMap, LocalDate start, LocalDate end, boolean negate) {
        date(videoMap, videoDate -> (!videoDate.isBefore(start) && !videoDate.isAfter(end)));
    }
    
    /**
     * Filters videos in the video map if the upload date is between two specified dates.
     *
     * @param videoMap The video map.
     * @param start    The start date.
     * @param end      The end date.
     */
    public static void dateBetween(Map<String, Video> videoMap, LocalDate start, LocalDate end) {
        dateBetween(videoMap, start, end, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is not between two specified dates.
     *
     * @param videoMap The video map.
     * @param start    The start date.
     * @param end      The end date.
     */
    public static void dateNotBetween(Map<String, Video> videoMap, LocalDate start, LocalDate end) {
        dateBetween(videoMap, start, end, BaseProcess.NEGATE);
    }
    
}
