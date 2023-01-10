/*
 * File:    FilterProcess.java
 * Package: youtube.channel.process.macro
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.process.macro;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.Channel;
import youtube.entity.Video;

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
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param regex      Whether to evaluate the search strings as regex.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    @SuppressWarnings("RegExpUnexpectedAnchor")
    public static void contains(Channel channel, Map<String, Video> videoMap, List<String> search, boolean regex, boolean ignoreCase, boolean negate) {
        BaseProcess.filter(channel, videoMap, video ->
                negate ^ search.stream().anyMatch(e ->
                        regex ? video.getTitle().matches((ignoreCase ? "(?i)" : "") + "^.*" + e + ".*$") :
                        ignoreCase ? video.getTitle().toLowerCase().contains(e.toLowerCase()) :
                        video.getTitle().contains(e)));
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    public static void contains(Channel channel, Map<String, Video> videoMap, List<String> search, boolean ignoreCase, boolean negate) {
        contains(channel, videoMap, search, !BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void contains(Channel channel, Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        contains(channel, videoMap, search, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void contains(Channel channel, Map<String, Video> videoMap, List<String> search) {
        contains(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains a search string.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void contains(Channel channel, Map<String, Video> videoMap, String search, boolean ignoreCase) {
        contains(channel, videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title contains a search string.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void contains(Channel channel, Map<String, Video> videoMap, String search) {
        contains(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of search strings, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void containsIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> search) {
        contains(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains a search string, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void containsIgnoreCase(Channel channel, Map<String, Video> videoMap, String search) {
        contains(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void notContains(Channel channel, Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        contains(channel, videoMap, search, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of search strings.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void notContains(Channel channel, Map<String, Video> videoMap, List<String> search) {
        notContains(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a search string.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void notContains(Channel channel, Map<String, Video> videoMap, String search, boolean ignoreCase) {
        notContains(channel, videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a search string.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void notContains(Channel channel, Map<String, Video> videoMap, String search) {
        notContains(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of search strings, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void notContainsIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> search) {
        notContains(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a search string, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void notContainsIgnoreCase(Channel channel, Map<String, Video> videoMap, String search) {
        notContains(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     * @param negate      Whether the condition should be negated.
     */
    public static void regexContains(Channel channel, Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase, boolean negate) {
        contains(channel, videoMap, regexSearch, BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexContains(Channel channel, Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexContains(channel, videoMap, regexSearch, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexContains(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexContains(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexContains(Channel channel, Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexContains(channel, videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title contains a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexContains(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexContains(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains any of a set of regex search strings, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexContainsIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexContains(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains a regex search string, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexContainsIgnoreCase(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexContains(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexNotContains(Channel channel, Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexContains(channel, videoMap, regexSearch, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotContains(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotContains(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexNotContains(Channel channel, Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexNotContains(channel, videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotContains(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexNotContains(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title contains none of a set of regex search strings, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotContainsIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotContains(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not contain a regex search string, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotContainsIgnoreCase(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexNotContains(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param regex      Whether to evaluate the search strings as regex.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    public static void startsWith(Channel channel, Map<String, Video> videoMap, List<String> search, boolean regex, boolean ignoreCase, boolean negate) {
        regexContains(channel, videoMap, search.stream().map(e -> "^" + (regex ? e : Pattern.quote(e))).collect(Collectors.toList()), ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    public static void startsWith(Channel channel, Map<String, Video> videoMap, List<String> search, boolean ignoreCase, boolean negate) {
        startsWith(channel, videoMap, search, !BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void startsWith(Channel channel, Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        startsWith(channel, videoMap, search, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of search strings.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void startsWith(Channel channel, Map<String, Video> videoMap, List<String> search) {
        startsWith(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with a search string.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void startsWith(Channel channel, Map<String, Video> videoMap, String search, boolean ignoreCase) {
        startsWith(channel, videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title starts with a search string.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void startsWith(Channel channel, Map<String, Video> videoMap, String search) {
        startsWith(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of search strings, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void startsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> search) {
        startsWith(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with a search string, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void startsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, String search) {
        startsWith(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with any of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void notStartsWith(Channel channel, Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        startsWith(channel, videoMap, search, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with any of a set of search strings.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void notStartsWith(Channel channel, Map<String, Video> videoMap, List<String> search) {
        notStartsWith(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a search string.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void notStartsWith(Channel channel, Map<String, Video> videoMap, String search, boolean ignoreCase) {
        notStartsWith(channel, videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a search string.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void notStartsWith(Channel channel, Map<String, Video> videoMap, String search) {
        notStartsWith(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with any of a set of search strings, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void notStartsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> search) {
        notStartsWith(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a search string, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void notStartsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, String search) {
        notStartsWith(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     * @param negate      Whether the condition should be negated.
     */
    public static void regexStartsWith(Channel channel, Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase, boolean negate) {
        startsWith(channel, videoMap, regexSearch, BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexStartsWith(Channel channel, Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexStartsWith(channel, videoMap, regexSearch, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexStartsWith(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexStartsWith(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexStartsWith(Channel channel, Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexStartsWith(channel, videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title starts with a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexStartsWith(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexStartsWith(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with any of a set of regex search strings, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexStartsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexStartsWith(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with a regex search string, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexStartsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexStartsWith(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexNotStartsWith(Channel channel, Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexStartsWith(channel, videoMap, regexSearch, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotStartsWith(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotStartsWith(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexNotStartsWith(Channel channel, Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexNotStartsWith(channel, videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotStartsWith(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexNotStartsWith(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title starts with a regex search string, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotStartsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotStartsWith(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not start with a regex search string, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotStartsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexNotStartsWith(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param regex      Whether to evaluate the search strings as regex.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    public static void endsWith(Channel channel, Map<String, Video> videoMap, List<String> search, boolean regex, boolean ignoreCase, boolean negate) {
        regexContains(channel, videoMap, search.stream().map(e -> (regex ? e : Pattern.quote(e)) + "$").collect(Collectors.toList()), ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     * @param negate     Whether the condition should be negated.
     */
    public static void endsWith(Channel channel, Map<String, Video> videoMap, List<String> search, boolean ignoreCase, boolean negate) {
        endsWith(channel, videoMap, search, !BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void endsWith(Channel channel, Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        endsWith(channel, videoMap, search, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of search strings.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void endsWith(Channel channel, Map<String, Video> videoMap, List<String> search) {
        endsWith(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with a search string.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void endsWith(Channel channel, Map<String, Video> videoMap, String search, boolean ignoreCase) {
        endsWith(channel, videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title ends with a search string.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void endsWith(Channel channel, Map<String, Video> videoMap, String search) {
        endsWith(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of search strings, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void endsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> search) {
        endsWith(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with a search string, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void endsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, String search) {
        endsWith(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with any of a set of search strings.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of search strings.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void notEndsWith(Channel channel, Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        endsWith(channel, videoMap, search, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with any of a set of search strings.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void notEndsWith(Channel channel, Map<String, Video> videoMap, List<String> search) {
        notEndsWith(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a search string.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The search string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void notEndsWith(Channel channel, Map<String, Video> videoMap, String search, boolean ignoreCase) {
        notEndsWith(channel, videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a search string.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void notEndsWith(Channel channel, Map<String, Video> videoMap, String search) {
        notEndsWith(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with any of a set of search strings, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of search strings.
     */
    public static void notEndsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> search) {
        notEndsWith(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a search string, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     */
    public static void notEndsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, String search) {
        notEndsWith(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     * @param negate      Whether the condition should be negated.
     */
    public static void regexEndsWith(Channel channel, Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase, boolean negate) {
        endsWith(channel, videoMap, regexSearch, BaseProcess.REGEX, ignoreCase, negate);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexEndsWith(Channel channel, Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexEndsWith(channel, videoMap, regexSearch, ignoreCase, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexEndsWith(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexEndsWith(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexEndsWith(Channel channel, Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexEndsWith(channel, videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title ends with a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexEndsWith(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexEndsWith(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with any of a set of regex search strings, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexEndsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexEndsWith(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with a regex search string, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexEndsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexEndsWith(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexNotEndsWith(Channel channel, Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        regexEndsWith(channel, videoMap, regexSearch, ignoreCase, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with any of a set of regex search strings.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotEndsWith(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotEndsWith(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexNotEndsWith(Channel channel, Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexNotEndsWith(channel, videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a regex search string.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotEndsWith(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexNotEndsWith(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title ends with a regex search string, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex search strings.
     */
    public static void regexNotEndsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexNotEndsWith(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the title does not end with a regex search string, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     */
    public static void regexNotEndsWithIgnoreCase(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexNotEndsWith(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Filters videos in the video map if the upload date is before a specified date.
     *
     * @param channel       The Channel.
     * @param videoMap      The Video map.
     * @param dateCondition The date condition to filter by.
     * @param negate        Whether the condition should be negated.
     */
    public static void date(Channel channel, Map<String, Video> videoMap, Predicate<LocalDate> dateCondition, boolean negate) {
        BaseProcess.filter(channel, videoMap, video ->
                Optional.ofNullable(video.getInfo().date).map(e -> (negate ^ dateCondition.test(e.toLocalDate()))).orElse(false));
    }
    
    /**
     * Filters videos in the video map if the upload date is before a specified date.
     *
     * @param channel       The Channel.
     * @param videoMap      The Video map.
     * @param dateCondition The date condition to filter by.
     */
    public static void date(Channel channel, Map<String, Video> videoMap, Predicate<LocalDate> dateCondition) {
        date(channel, videoMap, dateCondition, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is before a specified date.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param date     The date.
     * @param negate   Whether the condition should be negated.
     */
    public static void dateBefore(Channel channel, Map<String, Video> videoMap, LocalDate date, boolean negate) {
        date(channel, videoMap, videoDate -> videoDate.isBefore(date), negate);
    }
    
    /**
     * Filters videos in the video map if the upload date is before a specified date.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param date     The date.
     */
    public static void dateBefore(Channel channel, Map<String, Video> videoMap, LocalDate date) {
        dateBefore(channel, videoMap, date, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is not before a specified date.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param date     The date.
     */
    public static void dateNotBefore(Channel channel, Map<String, Video> videoMap, LocalDate date) {
        dateBefore(channel, videoMap, date, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is after a specified date.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param date     The date.
     * @param negate   Whether the condition should be negated.
     */
    public static void dateAfter(Channel channel, Map<String, Video> videoMap, LocalDate date, boolean negate) {
        date(channel, videoMap, videoDate -> videoDate.isAfter(date), negate);
    }
    
    /**
     * Filters videos in the video map if the upload date is after a specified date.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param date     The date.
     */
    public static void dateAfter(Channel channel, Map<String, Video> videoMap, LocalDate date) {
        dateAfter(channel, videoMap, date, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is not after a specified date.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param date     The date.
     */
    public static void dateNotAfter(Channel channel, Map<String, Video> videoMap, LocalDate date) {
        dateAfter(channel, videoMap, date, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is a specified date.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param date     The date.
     * @param negate   Whether the condition should be negated.
     */
    public static void dateEquals(Channel channel, Map<String, Video> videoMap, LocalDate date, boolean negate) {
        date(channel, videoMap, videoDate -> videoDate.isEqual(date), negate);
    }
    
    /**
     * Filters videos in the video map if the upload date is a specified date.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param date     The date.
     */
    public static void dateEquals(Channel channel, Map<String, Video> videoMap, LocalDate date) {
        dateEquals(channel, videoMap, date, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is not a specified date.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param date     The date.
     */
    public static void dateNotEquals(Channel channel, Map<String, Video> videoMap, LocalDate date) {
        dateEquals(channel, videoMap, date, BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is between two specified dates.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param start    The start date.
     * @param end      The end date.
     * @param negate   Whether the condition should be negated.
     */
    public static void dateBetween(Channel channel, Map<String, Video> videoMap, LocalDate start, LocalDate end, boolean negate) {
        date(channel, videoMap, videoDate -> (!videoDate.isBefore(start) && !videoDate.isAfter(end)));
    }
    
    /**
     * Filters videos in the video map if the upload date is between two specified dates.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param start    The start date.
     * @param end      The end date.
     */
    public static void dateBetween(Channel channel, Map<String, Video> videoMap, LocalDate start, LocalDate end) {
        dateBetween(channel, videoMap, start, end, !BaseProcess.NEGATE);
    }
    
    /**
     * Filters videos in the video map if the upload date is not between two specified dates.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param start    The start date.
     * @param end      The end date.
     */
    public static void dateNotBetween(Channel channel, Map<String, Video> videoMap, LocalDate start, LocalDate end) {
        dateBetween(channel, videoMap, start, end, BaseProcess.NEGATE);
    }
    
}
