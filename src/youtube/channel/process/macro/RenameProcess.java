/*
 * File:    RenameProcess.java
 * Package: youtube.channel.process.macro
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.process.macro;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
import youtube.entity.info.VideoInfo;

/**
 * Provides Channel Process macros to help with renaming.
 */
public class RenameProcess {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RenameProcess.class);
    
    
    //Constants
    
    /**
     * The default date format to use when renaming.
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    
    
    //Static Methods
    
    /**
     * Replaces a set of search strings with corresponding replacement strings in the title of each video in the video map.
     *
     * @param videoMap     The video map.
     * @param replacements The list of search strings and the corresponding replacement strings.
     * @param regex        Whether to evaluate the search strings as regex.
     * @param ignoreCase   Whether to ignore the case of the search strings.
     */
    public static void replace(Map<String, VideoInfo> videoMap, List<Map.Entry<String, String>> replacements, boolean regex, boolean ignoreCase) {
        BaseProcess.rename(videoMap, (id, video) ->
                expandTitle(replacements.stream().reduce(Map.entry(video.title, ""),
                                (s, e) -> Map.entry(s.getKey().replaceAll(
                                                ((ignoreCase ? "(?i)" : "") + (regex ? e.getKey() : Pattern.quote(e.getKey()))),
                                                (regex ? e.getValue() : Matcher.quoteReplacement(e.getValue()))),
                                        "")).getKey(),
                        video));
    }
    
    /**
     * Replaces a set of search strings with corresponding replacement strings in the title of each video in the video map.
     *
     * @param videoMap     The video map.
     * @param replacements The list of search strings and the corresponding replacement strings.
     * @param ignoreCase   Whether to ignore the case of the search strings.
     */
    public static void replace(Map<String, VideoInfo> videoMap, List<Map.Entry<String, String>> replacements, boolean ignoreCase) {
        replace(videoMap, replacements, !BaseProcess.REGEX, ignoreCase);
    }
    
    /**
     * Replaces a set of search strings with corresponding replacement strings in the title of each video in the video map.
     *
     * @param videoMap     The video map.
     * @param replacements The list of search strings and the corresponding replacement strings.
     */
    public static void replace(Map<String, VideoInfo> videoMap, List<Map.Entry<String, String>> replacements) {
        replace(videoMap, replacements, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a search string with a replacement string in the title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param search     The search string.
     * @param replace    The replacement string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void replace(Map<String, VideoInfo> videoMap, String search, String replace, boolean ignoreCase) {
        replace(videoMap, List.of(Map.entry(search, replace)), ignoreCase);
    }
    
    /**
     * Replaces a search string with a replacement string in the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     * @param replace  The replacement string.
     */
    public static void replace(Map<String, VideoInfo> videoMap, String search, String replace) {
        replace(videoMap, search, replace, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a set of search strings with corresponding replacement strings in the title of each video in the video map, regardless of case.
     *
     * @param videoMap     The video map.
     * @param replacements The list of search strings and the corresponding replacement strings.
     */
    public static void replaceIgnoreCase(Map<String, VideoInfo> videoMap, List<Map.Entry<String, String>> replacements) {
        replace(videoMap, replacements, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a set of search strings with corresponding replacement strings in the title of each video in the video map, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The search string.
     * @param replace  The replacement string.
     */
    public static void replaceIgnoreCase(Map<String, VideoInfo> videoMap, String search, String replace) {
        replace(videoMap, search, replace, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a set of regex search strings with corresponding replacement strings in the title of each video in the video map.
     *
     * @param videoMap          The video map.
     * @param regexReplacements The list of regex search strings and the corresponding replacement strings.
     * @param ignoreCase        Whether to ignore the case of the regex search strings.
     */
    public static void regexReplace(Map<String, VideoInfo> videoMap, List<Map.Entry<String, String>> regexReplacements, boolean ignoreCase) {
        replace(videoMap, regexReplacements, BaseProcess.REGEX, ignoreCase);
    }
    
    /**
     * Replaces a set of regex search strings with corresponding replacement strings in the title of each video in the video map.
     *
     * @param videoMap          The video map.
     * @param regexReplacements The list of regex search strings and the corresponding replacement strings.
     */
    public static void regexReplace(Map<String, VideoInfo> videoMap, List<Map.Entry<String, String>> regexReplacements) {
        regexReplace(videoMap, regexReplacements, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a regex search string with a replacement strings in the title of each video in the video map.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     * @param replace     The replacement string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexReplace(Map<String, VideoInfo> videoMap, String regexSearch, String replace, boolean ignoreCase) {
        regexReplace(videoMap, List.of(Map.entry(regexSearch, replace)), ignoreCase);
    }
    
    /**
     * Replaces a regex search string with a replacement strings in the title of each video in the video map.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     * @param replace     The replacement string.
     */
    public static void regexReplace(Map<String, VideoInfo> videoMap, String regexSearch, String replace) {
        regexReplace(videoMap, regexSearch, replace, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a set of regex search strings with corresponding replacement strings in the title of each video in the video map, regardless of case.
     *
     * @param videoMap          The video map.
     * @param regexReplacements The list of regex search strings and the corresponding replacement strings.
     */
    public static void regexReplaceIgnoreCase(Map<String, VideoInfo> videoMap, List<Map.Entry<String, String>> regexReplacements) {
        regexReplace(videoMap, regexReplacements, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a set of regex search strings with corresponding replacement strings in the title of each video in the video map, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex search string.
     * @param replace     The replacement string.
     */
    public static void regexReplaceIgnoreCase(Map<String, VideoInfo> videoMap, String regexSearch, String replace) {
        regexReplace(videoMap, regexSearch, replace, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of search strings from the title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param search     The list of strings to remove.
     * @param regex      Whether to evaluate the search strings as regex.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void remove(Map<String, VideoInfo> videoMap, List<String> search, boolean regex, boolean ignoreCase) {
        replace(videoMap, search.stream().map(e -> Map.entry(e, "")).collect(Collectors.toList()), regex, ignoreCase);
    }
    
    /**
     * Removes a set of search strings from the title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param search     The list of strings to remove.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void remove(Map<String, VideoInfo> videoMap, List<String> search, boolean ignoreCase) {
        remove(videoMap, search, !BaseProcess.REGEX, ignoreCase);
    }
    
    /**
     * Removes a set of search strings from the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param search   The list of strings to remove.
     */
    public static void remove(Map<String, VideoInfo> videoMap, List<String> search) {
        remove(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a search strings from the title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param search     The string to remove.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void remove(Map<String, VideoInfo> videoMap, String search, boolean ignoreCase) {
        remove(videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Removes a search strings from the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param search   The string to remove.
     */
    public static void remove(Map<String, VideoInfo> videoMap, String search) {
        remove(videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of search strings from the title of each video in the video map, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The list of strings to remove.
     */
    public static void removeIgnoreCase(Map<String, VideoInfo> videoMap, List<String> search) {
        remove(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of search strings from the title of each video in the video map, regardless of case.
     *
     * @param videoMap The video map.
     * @param search   The string to remove.
     */
    public static void removeIgnoreCase(Map<String, VideoInfo> videoMap, String search) {
        remove(videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of regex search strings from the title of each video in the video map.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex strings to remove.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexRemove(Map<String, VideoInfo> videoMap, List<String> regexSearch, boolean ignoreCase) {
        remove(videoMap, regexSearch, BaseProcess.REGEX, ignoreCase);
    }
    
    /**
     * Removes a set of regex search strings from the title of each video in the video map.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex strings to remove.
     */
    public static void regexRemove(Map<String, VideoInfo> videoMap, List<String> regexSearch) {
        regexRemove(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a regex search string from the title of each video in the video map.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex string to remove.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexRemove(Map<String, VideoInfo> videoMap, String regexSearch, boolean ignoreCase) {
        regexRemove(videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Removes a regex search string from the title of each video in the video map.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex string to remove.
     */
    public static void regexRemove(Map<String, VideoInfo> videoMap, String regexSearch) {
        regexRemove(videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of regex search strings from the title of each video in the video map, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The list of regex strings to remove.
     */
    public static void regexRemoveIgnoreCase(Map<String, VideoInfo> videoMap, List<String> regexSearch) {
        regexRemove(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of regex search strings from the title of each video in the video map, regardless of case.
     *
     * @param videoMap    The video map.
     * @param regexSearch The regex string to remove.
     */
    public static void regexRemoveIgnoreCase(Map<String, VideoInfo> videoMap, String regexSearch) {
        regexRemove(videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Appends a string to the end of the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param suffix   The suffix.
     */
    public static void append(Map<String, VideoInfo> videoMap, String suffix) {
        regexReplace(videoMap, "$", Matcher.quoteReplacement(suffix));
    }
    
    /**
     * Prepends a string to the beginning of the title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param prefix   The prefix.
     */
    public static void prepend(Map<String, VideoInfo> videoMap, String prefix) {
        regexReplace(videoMap, "^", Matcher.quoteReplacement(prefix));
    }
    
    /**
     * Appends the upload date to the end of the title of each video in the video map.
     *
     * @param videoMap The video map.
     */
    public static void appendUploadDate(Map<String, VideoInfo> videoMap) {
        append(videoMap, " - $d");
        regexReplace(videoMap, "(\\s-\\s" + DEFAULT_DATE_FORMAT.replaceAll("[^-/_]", "\\\\d") + ")\\1+$", "$1");
    }
    
    /**
     * Prepends the upload date to the beginning of the title of each video in the video map.
     *
     * @param videoMap The video map.
     */
    public static void prependUploadDate(Map<String, VideoInfo> videoMap) {
        prepend(videoMap, "$d - ");
        regexReplace(videoMap, "^(" + DEFAULT_DATE_FORMAT.replaceAll("[^-/_]", "\\\\d") + "\\s-\\s)\\1+", "$1");
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param strict     Whether to fail if a title does not match the pattern.
     * @param pattern    The regex pattern.
     * @param ignoreCase Whether to ignore the case of the regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The date format.
     */
    public static void format(Map<String, VideoInfo> videoMap, boolean strict, String pattern, boolean ignoreCase, String result, String dateFormat) {
        final AtomicInteger index = new AtomicInteger(0);
        final List<String> variables = Pattern.compile("\\(\\?<(?<name>[A-Za-z\\d]+)>").matcher(pattern).results()
                .map(e -> ("$" + e.group(1)))
                .sorted(Comparator.comparingInt(e -> -e.length()))
                .collect(Collectors.toList());
        
        BaseProcess.rename(videoMap, (id, video) ->
                Optional.of(Pattern.compile(pattern).matcher(video.title))
                        .filter(e -> (index.incrementAndGet() > 0))
                        .filter(Matcher::matches)
                        .map(matcher -> video.title.replaceAll(((ignoreCase ? "(?i)" : "") + pattern),
                                expandTitle(variables.stream()
                                                .filter(e -> result.matches("^.*" + Pattern.quote(e) + "\\b.*$"))
                                                .reduce(result, (s, e) -> s.replace(e, matcher.group(e.substring(1)))),
                                        video, index.get(), dateFormat)))
                        .orElseGet(() -> {
                            if (strict) {
                                System.out.println(Color.bad("The video: ") + Color.videoName(video.title) + Color.bad(" does not match the pattern: ") + Color.quoted(Color.base(pattern)));
                                throw new RuntimeException();
                            }
                            return video.title;
                        }));
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param strict     Whether to fail if a title does not match the pattern.
     * @param pattern    The regex pattern.
     * @param ignoreCase Whether to ignore the case of the regex pattern.
     * @param result     The resulting title pattern.
     */
    public static void format(Map<String, VideoInfo> videoMap, boolean strict, String pattern, boolean ignoreCase, String result) {
        format(videoMap, strict, pattern, ignoreCase, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param strict     Whether to fail if a title does not match the pattern.
     * @param pattern    The regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The date format.
     */
    public static void format(Map<String, VideoInfo> videoMap, boolean strict, String pattern, String result, String dateFormat) {
        format(videoMap, strict, pattern, !BaseProcess.IGNORE_CASE, result, dateFormat);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param strict   Whether to fail if a title does not match the pattern.
     * @param pattern  The regex pattern.
     * @param result   The resulting title pattern.
     */
    public static void format(Map<String, VideoInfo> videoMap, boolean strict, String pattern, String result) {
        format(videoMap, strict, pattern, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param pattern    The regex pattern.
     * @param ignoreCase Whether to ignore the case of the regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The format to use for dates.
     */
    public static void format(Map<String, VideoInfo> videoMap, String pattern, boolean ignoreCase, String result, String dateFormat) {
        format(videoMap, BaseProcess.STRICT, pattern, ignoreCase, result, dateFormat);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param pattern    The regex pattern.
     * @param ignoreCase Whether to ignore the case of the regex pattern.
     * @param result     The resulting title pattern.
     */
    public static void format(Map<String, VideoInfo> videoMap, String pattern, boolean ignoreCase, String result) {
        format(videoMap, pattern, ignoreCase, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map.
     *
     * @param videoMap   The video map.
     * @param pattern    The regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The format to use for dates.
     */
    public static void format(Map<String, VideoInfo> videoMap, String pattern, String result, String dateFormat) {
        format(videoMap, pattern, !BaseProcess.IGNORE_CASE, result, dateFormat);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map.
     *
     * @param videoMap The video map.
     * @param pattern  The regex pattern.
     * @param result   The resulting title pattern.
     */
    public static void format(Map<String, VideoInfo> videoMap, String pattern, String result) {
        format(videoMap, pattern, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map, regardless of case.
     *
     * @param videoMap   The video map.
     * @param strict     Whether to fail if a title does not match the pattern.
     * @param pattern    The regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The date format.
     */
    public static void formatIgnoreCase(Map<String, VideoInfo> videoMap, boolean strict, String pattern, String result, String dateFormat) {
        format(videoMap, strict, pattern, BaseProcess.IGNORE_CASE, result, dateFormat);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map, regardless of case.
     *
     * @param videoMap The video map.
     * @param strict   Whether to fail if a title does not match the pattern.
     * @param pattern  The regex pattern.
     * @param result   The resulting title pattern.
     */
    public static void formatIgnoreCase(Map<String, VideoInfo> videoMap, boolean strict, String pattern, String result) {
        formatIgnoreCase(videoMap, strict, pattern, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map, regardless of case.
     *
     * @param videoMap   The video map.
     * @param pattern    The regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The date format.
     */
    public static void formatIgnoreCase(Map<String, VideoInfo> videoMap, String pattern, String result, String dateFormat) {
        formatIgnoreCase(videoMap, BaseProcess.STRICT, pattern, result, dateFormat);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each video in the video map, regardless of case.
     *
     * @param videoMap The video map.
     * @param pattern  The regex pattern.
     * @param result   The resulting title pattern.
     */
    public static void formatIgnoreCase(Map<String, VideoInfo> videoMap, String pattern, String result) {
        formatIgnoreCase(videoMap, pattern, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Expands a title string by replacing default variables with their corresponding values.
     *
     * @param title      The title.
     * @param video      The Video Entity.
     * @param index      The current index.
     * @param dateFormat The date format.
     * @return The expanded title.
     */
    private static String expandTitle(String title, VideoInfo video, Integer index, String dateFormat) {
        return Stream.of(
                        Map.entry("i", Optional.ofNullable(index)),
                        Map.entry("n", Optional.ofNullable(video.playlistPosition)),
                        Map.entry("p", Optional.ofNullable(video.metadata).map(e -> e.playlist).map(e -> e.title)),
                        Map.entry("c", Optional.ofNullable(video.metadata).map(e -> e.channel).map(e -> e.title)),
                        Map.entry("v?Id", Optional.ofNullable(video.metadata).map(e -> e.entityId)),
                        Map.entry("pId", Optional.ofNullable(video.metadata).map(e -> e.playlistId)),
                        Map.entry("cId", Optional.ofNullable(video.metadata).map(e -> e.channelId)),
                        Map.entry("d", Optional.ofNullable(video.date).map(e -> e.format(
                                DateTimeFormatter.ofPattern(Optional.ofNullable(dateFormat).orElse(DEFAULT_DATE_FORMAT))))))
                .reduce(Map.entry(title, Optional.empty()),
                        (s, e) -> Map.entry(
                                s.getKey().replaceAll(
                                        ("(?i)\\$" + e.getKey() + "\\b"),
                                        Matcher.quoteReplacement(String.valueOf(e.getValue().orElse(null)))),
                                Optional.empty()
                        )).getKey();
    }
    
    /**
     * Expands a title string by replacing default variables with their corresponding values.
     *
     * @param title      The title.
     * @param video      The Video Entity.
     * @param dateFormat The date format.
     * @return The expanded title.
     */
    private static String expandTitle(String title, VideoInfo video, String dateFormat) {
        return expandTitle(title, video, null, dateFormat);
    }
    
    /**
     * Expands a title string by replacing default variables with their corresponding values.
     *
     * @param title The title.
     * @param video The Video Entity.
     * @return The expanded title.
     */
    private static String expandTitle(String title, VideoInfo video) {
        return expandTitle(title, video, null);
    }
    
}
