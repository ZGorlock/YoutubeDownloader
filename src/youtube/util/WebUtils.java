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
    public static final String CHANNEL_CUSTOM_BASE = YOUTUBE_BASE + "/@";
    
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
    public static final Pattern CHANNEL_URL_PATTERN = Pattern.compile("^.*/(?:c(?:hannel)?|u(?:ser)?)/(?<channel>\\w+).*$");
    
    
    //Static Methods
    
    /**
     * Fetches the Video from a Youtube video url.
     *
     * @param url    The video url.
     * @param useApi Whether to use the Youtube API to fetch the Video.
     * @return The Video.
     */
    @SuppressWarnings("unchecked")
    public static VideoInfo fetchVideo(String url, boolean useApi) {
        final Matcher videoUrlMatcher = VIDEO_URL_PATTERN.matcher(url);
        
        VideoInfo video = null;
        final Map<String, String> videoDetails = MapUtility.mapOf(
                new ImmutablePair<>("videoId", videoUrlMatcher.matches() ? videoUrlMatcher.group("video") : ""),
                new ImmutablePair<>("name", ""),
                new ImmutablePair<>("datePublished", new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
        
        if (!Configurator.Config.preventVideoFetch) {
            
            if (useApi) {
                try {
                    video = ApiUtils.fetchVideo(videoDetails.get("videoId"));
                } catch (Exception ignored) {
                }
                
            } else {
                final Pattern metaPattern = Pattern.compile("^\\s*<meta\\s*itemprop=\"(?<prop>[^\"]+)\"\\s*content=\"(?<value>[^\"]+)\"\\s*>\\s*$");
                Optional.ofNullable(url).map(Internet::getHtml)
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
     * Fetches the Channel playlist id from a Youtube channel url.
     *
     * @param url The Youtube channel url.
     * @return The playlist id, or an empty string if there was an error.
     */
    public static String fetchPlaylistId(String url) {
        return Optional.ofNullable(url)
                .map(PLAYLIST_URL_PATTERN::matcher).filter(Matcher::matches)
                .map(playlistUrlMatcher -> playlistUrlMatcher.group("playlist"))
                .orElseGet(() -> Optional.ofNullable(url)
                        .map(CHANNEL_URL_PATTERN::matcher).filter(Matcher::matches)
                        .map(channelUrlMatcher -> Pattern.compile("^.*\"externalId\":\"(?<externalId>[^\"]+)\".*$"))
                        .map(externalIdPattern -> Optional.ofNullable(url).map(Internet::getHtml)
                                .map(Node::toString).map(StringUtility::splitLines)
                                .stream().flatMap(Collection::stream)
                                .map(externalIdPattern::matcher).filter(Matcher::matches)
                                .map(externalIdMatcher -> externalIdMatcher.group("externalId")).filter(Objects::nonNull)
                                .map(externalId -> externalId.replaceAll("^UC", "UU"))
                                .findFirst().orElse(""))
                        .orElse(""));
    }
    
    /**
     * Checks and attempts to automatically fetch the playlist id for a Channel if needed.
     *
     * @param channel The Channel Config.
     * @throws RuntimeException When the Channel Config does not have a <i>playlistId</i> defined.
     */
    public static void checkPlaylistId(ChannelConfig channel) {
        if (StringUtility.isNullOrBlank(channel.getPlaylistId())) {
            channel.playlistId = WebUtils.fetchPlaylistId(channel.getUrl());
            
            System.out.println(Color.bad("Channel does not have a playlistId defined, please add this to the Channel configuration"));
            if (channel.getPlaylistId().isEmpty()) {
                throw new RuntimeException();
            }
            System.out.println(Color.bad("I was able to fetch it automatically based on the defined url: ") + Color.EXE.apply(channel.getPlaylistId()));
            System.out.println(Color.bad("Automatically fetching it every time is slow though, it is better to add it to ") + Color.filePath(Channels.CHANNELS_FILE));
        }
    }
    
}
