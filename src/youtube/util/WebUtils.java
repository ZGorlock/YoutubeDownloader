/*
 * File:    WebUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.Internet;
import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.channel.config.ChannelConfig;
import youtube.config.Color;
import youtube.config.Configurator;
import youtube.entity.Video;
import youtube.entity.info.VideoInfo;

/**
 * Provides web utility methods for the Youtube Downloader.
 */
public final class WebUtils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(WebUtils.class);
    
    
    //Constants
    
    /**
     * The base url for Youtube.
     */
    public static final String YOUTUBE_BASE = "https://www.youtube.com";
    
    /**
     * The base url for Youtube videos.
     */
    public static final String VIDEO_BASE = YOUTUBE_BASE + "/watch?v=";
    
    /**
     * The base url for Youtube playlists.
     */
    public static final String PLAYLIST_BASE = YOUTUBE_BASE + "/playlist?list=";
    
    /**
     * The base url for Youtube channels.
     */
    public static final String CHANNEL_BASE = YOUTUBE_BASE + "/channel/";
    
    /**
     * The base custom url for Youtube channels.
     */
    public static final String CHANNEL_CUSTOM_BASE = YOUTUBE_BASE + "/c/";
    
    /**
     * The regex pattern for a Youtube video url.
     */
    public static final Pattern VIDEO_URL_PATTERN = Pattern.compile("^.*[?&]v=(?<video>[^=?&]+).*$");
    
    /**
     * The regex pattern for a Youtube playlist url.
     */
    public static final Pattern PLAYLIST_URL_PATTERN = Pattern.compile("^.*[?&]list=(?<playlist>[^=?&]+).*$");
    
    /**
     * The regex pattern for a Youtube channel url.
     */
    public static final Pattern CHANNEL_URL_PATTERN = Pattern.compile("^.*/(?:@|(?:c(?:hannel|/@)?|u(?:ser)?)/)(?<channel>\\w+).*$");
    
    
    //Static Fields
    
    /**
     * A flag indicating whether the web system has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Static Methods
    
    /**
     * Initializes the web system.
     *
     * @return Whether the web system was successfully initialized.
     */
    public static boolean initWeb() {
        if (loaded.compareAndSet(false, true)) {
            logger.trace(LogUtils.NEWLINE);
            logger.debug(Color.log("Initializing Web..."));
            
            return checkInternet();
        }
        return false;
    }
    
    /**
     * Determines if internet access is available.
     *
     * @return Whether internet access is available.
     */
    public static boolean checkInternet() {
        logger.debug(Color.log("Checking Internet..."));
        
        if (!Internet.isOnline()) {
            logger.trace(LogUtils.NEWLINE);
            logger.warn(Color.bad("Internet access is required"));
            return false;
        }
        return true;
    }
    
    /**
     * Determines if a url is a Youtube channel url.
     *
     * @param url The url.
     * @return Whether the specified url is a Youtube channel url.
     */
    public static boolean isYoutubeUrl(String url) {
        return Optional.ofNullable(url)
                .map(e -> e.startsWith(YOUTUBE_BASE))
                .orElse(false);
    }
    
    /**
     * Determines if a url is a Youtube channel url.
     *
     * @param url        The url.
     * @param urlPattern The pattern the url should match.
     * @return Whether the specified url is a Youtube channel url which matches the specified pattern.
     */
    public static boolean isYoutubeUrl(String url, Pattern urlPattern) {
        return Optional.ofNullable(url)
                .filter(WebUtils::isYoutubeUrl)
                .map(urlPattern::matcher).map(Matcher::matches)
                .orElse(false);
    }
    
    /**
     * Determines if a url is a Youtube channel url.
     *
     * @param url The url.
     * @return Whether the specified url is a Youtube channel url.
     */
    public static boolean isChannelUrl(String url) {
        return isYoutubeUrl(url, CHANNEL_URL_PATTERN);
    }
    
    /**
     * Determines if a url is a Youtube playlist url.
     *
     * @param url The url.
     * @return Whether the specified url is a Youtube playlist url.
     */
    public static boolean isPlaylistUrl(String url) {
        return isYoutubeUrl(url, PLAYLIST_URL_PATTERN);
    }
    
    /**
     * Determines if a url is a Youtube video url.
     *
     * @param url The url.
     * @return Whether the specified url is a Youtube video url.
     */
    public static boolean isVideoUrl(String url) {
        return isYoutubeUrl(url, VIDEO_URL_PATTERN);
    }
    
    /**
     * Returns the url associated with the Youtube channel.
     *
     * @param channelId The Youtube channel id or custom url key.
     * @return The Youtube channel url, or null if the channel id is invalid.
     */
    public static String getChannelUrl(String channelId) {
        return Optional.ofNullable(channelId)
                .filter(id -> !id.isBlank())
                .map(id -> id.replaceAll("^UU", "UC")).map(id -> id.replaceAll("^@", "/@"))
                .map(id -> ((id.startsWith("/") ? YOUTUBE_BASE :
                             id.startsWith("UC") ? CHANNEL_BASE : CHANNEL_CUSTOM_BASE) + id))
                .orElse(null);
    }
    
    /**
     * Returns the url associated with the Youtube playlist.
     *
     * @param playlistId The Youtube playlist id.
     * @return The Youtube playlist url, or null if the playlist id is invalid.
     */
    public static String getPlaylistUrl(String playlistId) {
        return Optional.ofNullable(playlistId)
                .filter(id -> !id.isBlank())
                .map(id -> (PLAYLIST_BASE + id))
                .orElse(null);
    }
    
    /**
     * Returns the url associated with the Youtube video.
     *
     * @param videoId The Youtube video id.
     * @return The Youtube video url, or null if the video id is invalid.
     */
    public static String getVideoUrl(String videoId) {
        return Optional.ofNullable(videoId)
                .filter(id -> !id.isBlank())
                .map(id -> (VIDEO_BASE + id))
                .orElse(null);
    }
    
    /**
     * Fetches the Video from a Youtube video url.
     *
     * @param url    The video url.
     * @param useApi Whether to use the Youtube API to fetch the Video.
     * @return The Video.
     */
    @SuppressWarnings("unchecked")
    public static VideoInfo fetchVideo(String url, boolean useApi) {
        VideoInfo video = null;
        final Map<String, String> videoDetails = MapUtility.mapOf(
                new ImmutablePair<>("videoId", getVideoId(url)),
                new ImmutablePair<>("name", ""),
                new ImmutablePair<>("datePublished", new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
        
        if (!Configurator.Config.preventVideoFetch) {
            if (useApi) {
                video = ApiUtils.fetchVideo(videoDetails.get("videoId"));
                
            } else {
                final Pattern metaPattern = Pattern.compile("^\\s*<meta\\s*itemprop=\"(?<prop>[^\"]+)\"\\s*content=\"(?<value>[^\"]+)\"\\s*>\\s*$");
                Optional.of(url).map(Internet::getHtml)
                        .map(Node::toString).map(StringUtility::splitLines)
                        .stream().flatMap(Collection::stream)
                        .map(metaPattern::matcher).filter(Matcher::matches)
                        .forEach(metaMatcher -> videoDetails.computeIfPresent(
                                metaMatcher.group("prop"), (k, v) -> metaMatcher.group("value")));
            }
        }
        
        return Optional.ofNullable(video).orElseGet(() ->
                new VideoInfo(videoDetails.get("videoId"),
                        Optional.of(videoDetails.get("name")).filter(e -> !StringUtility.isNullOrBlank(e)).orElse(videoDetails.get("videoId")),
                        (videoDetails.get("datePublished") + " 00:00:00")));
    }
    
    /**
     * Fetches the Video from a Youtube video url.
     *
     * @param url The video url.
     * @return The Video.
     */
    public static Video fetchVideo(String url) {
        return new Video(fetchVideo(url, true));
    }
    
    /**
     * Extracts the video id from a Youtube video url.
     *
     * @param url The Youtube video url.
     * @return The video id, or an empty string if there was an error.
     */
    public static String getVideoId(String url) {
        return Optional.ofNullable(url)
                .filter(WebUtils::isVideoUrl)
                .map(VIDEO_URL_PATTERN::matcher).filter(Matcher::matches)
                .map(videoUrlMatcher -> videoUrlMatcher.group("video"))
                .orElse("");
    }
    
    /**
     * Extracts the playlist id from a Youtube playlist url.
     *
     * @param url The Youtube playlist url.
     * @return The playlist id, or an empty string if there was an error.
     */
    public static String getPlaylistId(String url) {
        return Optional.ofNullable(url)
                .filter(WebUtils::isPlaylistUrl)
                .map(PLAYLIST_URL_PATTERN::matcher).filter(Matcher::matches)
                .map(playlistUrlMatcher -> playlistUrlMatcher.group("playlist"))
                .orElseGet(() -> getChannelPlaylistId(url));
    }
    
    /**
     * Extracts the channel playlist id from a Youtube channel url.
     *
     * @param url The Youtube channel url.
     * @return The channel playlist id, or an empty string if there was an error.
     */
    public static String getChannelPlaylistId(String url) {
        return Optional.ofNullable(url)
                .map(WebUtils::getChannelId)
                .map(id -> id.replaceAll("^UC", "UU"))
                .orElse("");
    }
    
    /**
     * Extracts the channel id from a Youtube channel url.
     *
     * @param url The Youtube channel url.
     * @return The channel id, or an empty string if there was an error.
     */
    public static String getChannelId(String url) {
        return Optional.ofNullable(url)
                .filter(WebUtils::isChannelUrl)
                .map(CHANNEL_URL_PATTERN::matcher).filter(Matcher::matches)
                .map(channelUrlMatcher -> channelUrlMatcher.group("channel"))
                .filter(id -> id.startsWith("UC"))
                .orElseGet(() -> fetchExternalId(url));
    }
    
    /**
     * Fetches the external id from a Youtube channel url.
     *
     * @param url The Youtube channel url.
     * @return The external id, or an empty string if there was an error.
     */
    public static String fetchExternalId(String url) {
        final Pattern externalIdPattern = Pattern.compile("^.*\"externalId\":\"(?<externalId>[^\"]+)\".*$");
        return Optional.ofNullable(url)
                .filter(WebUtils::isChannelUrl)
                .map(Internet::getHtml)
                .map(Node::toString).map(StringUtility::splitLines)
                .stream().flatMap(Collection::stream)
                .map(externalIdPattern::matcher).filter(Matcher::matches)
                .map(externalIdMatcher -> externalIdMatcher.group("externalId")).filter(Objects::nonNull)
                .findFirst().orElse("");
    }
    
    /**
     * Checks and attempts to automatically fetch the playlist id for a Channel if needed.
     *
     * @param channel The Channel Config.
     * @throws RuntimeException When the Channel Config does not have a <i>playlistId</i> defined.
     */
    public static void checkPlaylistId(ChannelConfig channel) {
        if (StringUtility.isNullOrBlank(channel.getPlaylistId())) {
            channel.playlistId = getPlaylistId(channel.getUrl());
            
            logger.warn(Color.bad("Channel does not have a ") + Color.link("playlistId") + Color.bad(" defined, please add this to the Channel configuration"));
            if (channel.getPlaylistId().isEmpty()) {
                throw new RuntimeException();
            }
            logger.debug(Color.bad("I was able to fetch it automatically based on the defined url: ") + Color.channelKey(channel.getPlaylistId()));
            logger.debug(Color.bad("Automatically fetching it every time is slow though, it is better to add it to ") + Color.quoteFilePath(Channels.CHANNELS_FILE));
        }
    }
    
}
