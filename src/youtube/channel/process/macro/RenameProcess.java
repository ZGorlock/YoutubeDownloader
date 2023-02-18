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
import youtube.entity.Channel;
import youtube.entity.Video;
import youtube.entity.info.VideoInfo;
import youtube.entity.info.base.EntityInfo;
import youtube.entity.info.base.EntityMetadata;

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
     * Replaces a set of search strings with corresponding replacement strings in the title of each Video in the Video map.
     *
     * @param channel      The Channel.
     * @param videoMap     The Video map.
     * @param replacements The list of search strings and the corresponding replacement strings.
     * @param regex        Whether to evaluate the search strings as regex.
     * @param ignoreCase   Whether to ignore the case of the search strings.
     */
    public static void replace(Channel channel, Map<String, Video> videoMap, List<Map.Entry<String, String>> replacements, boolean regex, boolean ignoreCase) {
        BaseProcess.rename(channel, videoMap, (id, video) ->
                expandTitle(replacements.stream().reduce(Map.entry(video.getTitle(), ""),
                                (s, e) -> Map.entry(s.getKey().replaceAll(
                                                ((ignoreCase ? "(?i)" : "") + (regex ? e.getKey() : Pattern.quote(e.getKey()))),
                                                (regex ? e.getValue() : Matcher.quoteReplacement(e.getValue()))),
                                        "")).getKey(),
                        video.getInfo()));
    }
    
    /**
     * Replaces a set of search strings with corresponding replacement strings in the title of each Video in the Video map.
     *
     * @param channel      The Channel.
     * @param videoMap     The Video map.
     * @param replacements The list of search strings and the corresponding replacement strings.
     * @param ignoreCase   Whether to ignore the case of the search strings.
     */
    public static void replace(Channel channel, Map<String, Video> videoMap, List<Map.Entry<String, String>> replacements, boolean ignoreCase) {
        replace(channel, videoMap, replacements, !BaseProcess.REGEX, ignoreCase);
    }
    
    /**
     * Replaces a set of search strings with corresponding replacement strings in the title of each Video in the Video map.
     *
     * @param channel      The Channel.
     * @param videoMap     The Video map.
     * @param replacements The list of search strings and the corresponding replacement strings.
     */
    public static void replace(Channel channel, Map<String, Video> videoMap, List<Map.Entry<String, String>> replacements) {
        replace(channel, videoMap, replacements, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a search string with a replacement string in the title of each Video in the Video map.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The search string.
     * @param replace    The replacement string.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void replace(Channel channel, Map<String, Video> videoMap, String search, String replace, boolean ignoreCase) {
        replace(channel, videoMap, List.of(Map.entry(search, replace)), ignoreCase);
    }
    
    /**
     * Replaces a search string with a replacement string in the title of each Video in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     * @param replace  The replacement string.
     */
    public static void replace(Channel channel, Map<String, Video> videoMap, String search, String replace) {
        replace(channel, videoMap, search, replace, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a set of search strings with corresponding replacement strings in the title of each Video in the Video map, regardless of case.
     *
     * @param channel      The Channel.
     * @param videoMap     The Video map.
     * @param replacements The list of search strings and the corresponding replacement strings.
     */
    public static void replaceIgnoreCase(Channel channel, Map<String, Video> videoMap, List<Map.Entry<String, String>> replacements) {
        replace(channel, videoMap, replacements, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a set of search strings with corresponding replacement strings in the title of each Video in the Video map, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The search string.
     * @param replace  The replacement string.
     */
    public static void replaceIgnoreCase(Channel channel, Map<String, Video> videoMap, String search, String replace) {
        replace(channel, videoMap, search, replace, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a set of regex search strings with corresponding replacement strings in the title of each Video in the Video map.
     *
     * @param channel           The Channel.
     * @param videoMap          The Video map.
     * @param regexReplacements The list of regex search strings and the corresponding replacement strings.
     * @param ignoreCase        Whether to ignore the case of the regex search strings.
     */
    public static void regexReplace(Channel channel, Map<String, Video> videoMap, List<Map.Entry<String, String>> regexReplacements, boolean ignoreCase) {
        replace(channel, videoMap, regexReplacements, BaseProcess.REGEX, ignoreCase);
    }
    
    /**
     * Replaces a set of regex search strings with corresponding replacement strings in the title of each Video in the Video map.
     *
     * @param channel           The Channel.
     * @param videoMap          The Video map.
     * @param regexReplacements The list of regex search strings and the corresponding replacement strings.
     */
    public static void regexReplace(Channel channel, Map<String, Video> videoMap, List<Map.Entry<String, String>> regexReplacements) {
        regexReplace(channel, videoMap, regexReplacements, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a regex search string with a replacement strings in the title of each Video in the Video map.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     * @param replace     The replacement string.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexReplace(Channel channel, Map<String, Video> videoMap, String regexSearch, String replace, boolean ignoreCase) {
        regexReplace(channel, videoMap, List.of(Map.entry(regexSearch, replace)), ignoreCase);
    }
    
    /**
     * Replaces a regex search string with a replacement strings in the title of each Video in the Video map.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     * @param replace     The replacement string.
     */
    public static void regexReplace(Channel channel, Map<String, Video> videoMap, String regexSearch, String replace) {
        regexReplace(channel, videoMap, regexSearch, replace, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a set of regex search strings with corresponding replacement strings in the title of each Video in the Video map, regardless of case.
     *
     * @param channel           The Channel.
     * @param videoMap          The Video map.
     * @param regexReplacements The list of regex search strings and the corresponding replacement strings.
     */
    public static void regexReplaceIgnoreCase(Channel channel, Map<String, Video> videoMap, List<Map.Entry<String, String>> regexReplacements) {
        regexReplace(channel, videoMap, regexReplacements, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Replaces a set of regex search strings with corresponding replacement strings in the title of each Video in the Video map, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex search string.
     * @param replace     The replacement string.
     */
    public static void regexReplaceIgnoreCase(Channel channel, Map<String, Video> videoMap, String regexSearch, String replace) {
        regexReplace(channel, videoMap, regexSearch, replace, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of search strings from the title of each Video in the Video map.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of strings to remove.
     * @param regex      Whether to evaluate the search strings as regex.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void remove(Channel channel, Map<String, Video> videoMap, List<String> search, boolean regex, boolean ignoreCase) {
        replace(channel, videoMap, search.stream().map(e -> Map.entry(e, "")).collect(Collectors.toList()), regex, ignoreCase);
    }
    
    /**
     * Removes a set of search strings from the title of each Video in the Video map.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The list of strings to remove.
     * @param ignoreCase Whether to ignore the case of the search strings.
     */
    public static void remove(Channel channel, Map<String, Video> videoMap, List<String> search, boolean ignoreCase) {
        remove(channel, videoMap, search, !BaseProcess.REGEX, ignoreCase);
    }
    
    /**
     * Removes a set of search strings from the title of each Video in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of strings to remove.
     */
    public static void remove(Channel channel, Map<String, Video> videoMap, List<String> search) {
        remove(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a search strings from the title of each Video in the Video map.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param search     The string to remove.
     * @param ignoreCase Whether to ignore the case of the search string.
     */
    public static void remove(Channel channel, Map<String, Video> videoMap, String search, boolean ignoreCase) {
        remove(channel, videoMap, List.of(search), ignoreCase);
    }
    
    /**
     * Removes a search strings from the title of each Video in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The string to remove.
     */
    public static void remove(Channel channel, Map<String, Video> videoMap, String search) {
        remove(channel, videoMap, search, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of search strings from the title of each Video in the Video map, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The list of strings to remove.
     */
    public static void removeIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> search) {
        remove(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of search strings from the title of each Video in the Video map, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param search   The string to remove.
     */
    public static void removeIgnoreCase(Channel channel, Map<String, Video> videoMap, String search) {
        remove(channel, videoMap, search, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of regex search strings from the title of each Video in the Video map.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex strings to remove.
     * @param ignoreCase  Whether to ignore the case of the regex search strings.
     */
    public static void regexRemove(Channel channel, Map<String, Video> videoMap, List<String> regexSearch, boolean ignoreCase) {
        remove(channel, videoMap, regexSearch, BaseProcess.REGEX, ignoreCase);
    }
    
    /**
     * Removes a set of regex search strings from the title of each Video in the Video map.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex strings to remove.
     */
    public static void regexRemove(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexRemove(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a regex search string from the title of each Video in the Video map.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex string to remove.
     * @param ignoreCase  Whether to ignore the case of the regex search string.
     */
    public static void regexRemove(Channel channel, Map<String, Video> videoMap, String regexSearch, boolean ignoreCase) {
        regexRemove(channel, videoMap, List.of(regexSearch), ignoreCase);
    }
    
    /**
     * Removes a regex search string from the title of each Video in the Video map.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex string to remove.
     */
    public static void regexRemove(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexRemove(channel, videoMap, regexSearch, !BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of regex search strings from the title of each Video in the Video map, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The list of regex strings to remove.
     */
    public static void regexRemoveIgnoreCase(Channel channel, Map<String, Video> videoMap, List<String> regexSearch) {
        regexRemove(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Removes a set of regex search strings from the title of each Video in the Video map, regardless of case.
     *
     * @param channel     The Channel.
     * @param videoMap    The Video map.
     * @param regexSearch The regex string to remove.
     */
    public static void regexRemoveIgnoreCase(Channel channel, Map<String, Video> videoMap, String regexSearch) {
        regexRemove(channel, videoMap, regexSearch, BaseProcess.IGNORE_CASE);
    }
    
    /**
     * Appends a string to the end of the title of each Video in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param suffix   The suffix.
     */
    public static void append(Channel channel, Map<String, Video> videoMap, String suffix) {
        regexReplace(channel, videoMap, "$", Matcher.quoteReplacement(suffix));
    }
    
    /**
     * Prepends a string to the beginning of the title of each Video in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param prefix   The prefix.
     */
    public static void prepend(Channel channel, Map<String, Video> videoMap, String prefix) {
        regexReplace(channel, videoMap, "^", Matcher.quoteReplacement(prefix));
    }
    
    /**
     * Appends the upload date to the end of the title of each Video in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     */
    public static void appendUploadDate(Channel channel, Map<String, Video> videoMap) {
        append(channel, videoMap, " - $d");
        regexReplace(channel, videoMap, "(\\s-\\s" + DEFAULT_DATE_FORMAT.replaceAll("[^-/_]", "\\\\d") + ")\\1+$", "$1");
    }
    
    /**
     * Prepends the upload date to the beginning of the title of each Video in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     */
    public static void prependUploadDate(Channel channel, Map<String, Video> videoMap) {
        prepend(channel, videoMap, "$d - ");
        regexReplace(channel, videoMap, "^(" + DEFAULT_DATE_FORMAT.replaceAll("[^-/_]", "\\\\d") + "\\s-\\s)\\1+", "$1");
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param strict     Whether to fail if a title does not match the pattern.
     * @param pattern    The regex pattern.
     * @param ignoreCase Whether to ignore the case of the regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The date format.
     * @throws RuntimeException When strict mode is enabled and the title of a Video in the Video map does not match the specified pattern.
     */
    public static void format(Channel channel, Map<String, Video> videoMap, boolean strict, String pattern, boolean ignoreCase, String result, String dateFormat) {
        final AtomicInteger index = new AtomicInteger(0);
        final Pattern matchPattern = Pattern.compile((ignoreCase ? "(?i)" : "") + pattern);
        final List<String> variables = Pattern.compile("(?i)\\(\\?<(?<name>[A-Z\\d]+)>").matcher(pattern).results()
                .map(e -> ("$" + e.group(1)))
                .sorted(Comparator.comparingInt(e -> -e.length()))
                .collect(Collectors.toList());
        
        BaseProcess.rename(channel, videoMap, (id, video) ->
                Optional.of(matchPattern.matcher(video.getTitle()))
                        .filter(e -> (index.incrementAndGet() > 0))
                        .filter(Matcher::matches)
                        .map(matcher -> video.getTitle().replaceAll(matchPattern.pattern(), Matcher.quoteReplacement(
                                expandTitle(variables.stream()
                                                .filter(e -> result.matches("^.*" + Pattern.quote(e) + "\\b.*$"))
                                                .reduce(result, (s, e) -> s.replace(e, matcher.group(e.substring(1)))),
                                        video.getInfo(), index.get(), dateFormat))))
                        .orElseGet(() -> {
                            if (strict) {
                                logger.warn(Color.bad("The video: ") + Color.videoName(video.getTitle()) + Color.bad(" does not match the pattern: ") + Color.quoted(Color.base(pattern)));
                                throw new RuntimeException();
                            }
                            return video.getTitle();
                        }));
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param strict     Whether to fail if a title does not match the pattern.
     * @param pattern    The regex pattern.
     * @param ignoreCase Whether to ignore the case of the regex pattern.
     * @param result     The resulting title pattern.
     */
    public static void format(Channel channel, Map<String, Video> videoMap, boolean strict, String pattern, boolean ignoreCase, String result) {
        format(channel, videoMap, strict, pattern, ignoreCase, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param strict     Whether to fail if a title does not match the pattern.
     * @param pattern    The regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The date format.
     */
    public static void format(Channel channel, Map<String, Video> videoMap, boolean strict, String pattern, String result, String dateFormat) {
        format(channel, videoMap, strict, pattern, !BaseProcess.IGNORE_CASE, result, dateFormat);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param strict   Whether to fail if a title does not match the pattern.
     * @param pattern  The regex pattern.
     * @param result   The resulting title pattern.
     */
    public static void format(Channel channel, Map<String, Video> videoMap, boolean strict, String pattern, String result) {
        format(channel, videoMap, strict, pattern, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param pattern    The regex pattern.
     * @param ignoreCase Whether to ignore the case of the regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The format to use for dates.
     */
    public static void format(Channel channel, Map<String, Video> videoMap, String pattern, boolean ignoreCase, String result, String dateFormat) {
        format(channel, videoMap, BaseProcess.STRICT, pattern, ignoreCase, result, dateFormat);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param pattern    The regex pattern.
     * @param ignoreCase Whether to ignore the case of the regex pattern.
     * @param result     The resulting title pattern.
     */
    public static void format(Channel channel, Map<String, Video> videoMap, String pattern, boolean ignoreCase, String result) {
        format(channel, videoMap, pattern, ignoreCase, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param pattern    The regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The format to use for dates.
     */
    public static void format(Channel channel, Map<String, Video> videoMap, String pattern, String result, String dateFormat) {
        format(channel, videoMap, pattern, !BaseProcess.IGNORE_CASE, result, dateFormat);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param pattern  The regex pattern.
     * @param result   The resulting title pattern.
     */
    public static void format(Channel channel, Map<String, Video> videoMap, String pattern, String result) {
        format(channel, videoMap, pattern, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map, regardless of case.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param strict     Whether to fail if a title does not match the pattern.
     * @param pattern    The regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The date format.
     */
    public static void formatIgnoreCase(Channel channel, Map<String, Video> videoMap, boolean strict, String pattern, String result, String dateFormat) {
        format(channel, videoMap, strict, pattern, BaseProcess.IGNORE_CASE, result, dateFormat);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param strict   Whether to fail if a title does not match the pattern.
     * @param pattern  The regex pattern.
     * @param result   The resulting title pattern.
     */
    public static void formatIgnoreCase(Channel channel, Map<String, Video> videoMap, boolean strict, String pattern, String result) {
        formatIgnoreCase(channel, videoMap, strict, pattern, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map, regardless of case.
     *
     * @param channel    The Channel.
     * @param videoMap   The Video map.
     * @param pattern    The regex pattern.
     * @param result     The resulting title pattern.
     * @param dateFormat The date format.
     */
    public static void formatIgnoreCase(Channel channel, Map<String, Video> videoMap, String pattern, String result, String dateFormat) {
        formatIgnoreCase(channel, videoMap, BaseProcess.STRICT, pattern, result, dateFormat);
    }
    
    /**
     * Matches a regex pattern and constructs the new title of each Video in the Video map, regardless of case.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param pattern  The regex pattern.
     * @param result   The resulting title pattern.
     */
    public static void formatIgnoreCase(Channel channel, Map<String, Video> videoMap, String pattern, String result) {
        formatIgnoreCase(channel, videoMap, pattern, result, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * Expands a title string by replacing default variables with their corresponding values.
     *
     * @param title      The title.
     * @param videoInfo  The Video Info.
     * @param index      The current index.
     * @param dateFormat The date format.
     * @return The expanded title.
     */
    private static String expandTitle(String title, VideoInfo videoInfo, Integer index, String dateFormat) {
        return Stream.of(
                        Map.entry("i", Optional.ofNullable(index)),
                        Map.entry("n", Optional.ofNullable(videoInfo).map(VideoInfo::getPlaylistPosition)),
                        Map.entry("p", Optional.ofNullable(videoInfo).map(EntityInfo::getMetadata).map(EntityMetadata::getPlaylist).map(EntityInfo::getTitle)),
                        Map.entry("c", Optional.ofNullable(videoInfo).map(EntityInfo::getMetadata).map(EntityMetadata::getChannel).map(EntityInfo::getTitle)),
                        Map.entry("v?Id", Optional.ofNullable(videoInfo).map(EntityInfo::getMetadata).map(EntityMetadata::getEntityId)),
                        Map.entry("pId", Optional.ofNullable(videoInfo).map(EntityInfo::getMetadata).map(EntityMetadata::getPlaylistId)),
                        Map.entry("cId", Optional.ofNullable(videoInfo).map(EntityInfo::getMetadata).map(EntityMetadata::getChannelId)),
                        Map.entry("d", Optional.ofNullable(videoInfo).map(EntityInfo::getDate).map(e -> e.format(
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
     * @param videoInfo  The Video Info.
     * @param dateFormat The date format.
     * @return The expanded title.
     */
    private static String expandTitle(String title, VideoInfo videoInfo, String dateFormat) {
        return expandTitle(title, videoInfo, null, dateFormat);
    }
    
    /**
     * Expands a title string by replacing default variables with their corresponding values.
     *
     * @param title     The title.
     * @param videoInfo The Video Info.
     * @return The expanded title.
     */
    private static String expandTitle(String title, VideoInfo videoInfo) {
        return expandTitle(title, videoInfo, null);
    }
    
}
