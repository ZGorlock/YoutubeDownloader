/*
 * File:    WebUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
import youtube.channel.Channels;
import youtube.channel.entity.Video;
import youtube.conf.Color;
import youtube.conf.Configurator;

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
    public static final String CHANNEL_BASE = YOUTUBE_BASE + "/c/";
    
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
     * Determines if the system has access to the internet.
     *
     * @return Whether the system has access to the internet or not.
     */
    public static boolean isOnline() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("youtube.com", 80), 200);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Returns the html of a web page.
     *
     * @param url The url.
     * @return The html of the web page, or an empty string if there was an error.
     */
    public static String getHtml(String url) {
        try {
            return Jsoup.connect(url)
                    .ignoreContentType(true)
                    .maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36")
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .followRedirects(true)
                    .execute()
                    .parse()
                    .toString();
        } catch (IOException ignored) {
        }
        return "";
    }
    
    /**
     * Downloads a file from a url to the specified file and returns the file.<br>
     * This is a blocking operation and should be called from a thread.
     *
     * @param url      The url to the file to download.
     * @param download The file to download to.
     * @return The downloaded file or null if there was an error.
     */
    public static File downloadFile(String url, File download) {
        try {
            if (!isOnline()) {
                throw new IOException();
            }
            
            FileUtils.downloadFile(url, download);
            return download;
            
        } catch (IOException ignored) {
            return null;
        }
    }
    
    /**
     * Fetches the Video information from a Youtube video url.
     *
     * @param url    The Youtube video url.
     * @param useApi Whether to use the Youtube API to get the Video information.
     * @return The fetched Video.
     */
    @SuppressWarnings("unchecked")
    public static Video fetchVideo(String url, boolean useApi) {
        final Matcher videoUrlMatcher = VIDEO_URL_PATTERN.matcher(url);
        
        Video video = null;
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
                StringUtility.splitLines(getHtml(url)).stream()
                        .map(metaPattern::matcher).filter(Matcher::matches)
                        .forEach(metaMatcher -> videoDetails.computeIfPresent(
                                metaMatcher.group("prop"), (k, v) -> metaMatcher.group("value")));
            }
        }
        
        return Optional.ofNullable(video).orElseGet(() ->
                new Video(videoDetails.get("videoId"),
                        Optional.of(videoDetails.get("name")).filter(e -> !StringUtility.isNullOrBlank(e)).orElse(videoDetails.get("videoId")),
                        (videoDetails.get("datePublished") + " 00:00:00")));
    }
    
    /**
     * Fetches the Video information from a Youtube video url.
     *
     * @param url The Youtube video url.
     * @return The fetched Video.
     */
    public static Video fetchVideo(String url) {
        return fetchVideo(url, true);
    }
    
    /**
     * Fetches the Channel playlist id from a Youtube channel url.
     *
     * @param url The Youtube channel url.
     * @return The fetched playlist id, or an empty string if there was an error.
     */
    public static String fetchPlaylistId(String url) {
        return Optional.ofNullable(url)
                .map(PLAYLIST_URL_PATTERN::matcher).filter(Matcher::matches)
                .map(playlistUrlMatcher -> playlistUrlMatcher.group("playlist"))
                .orElseGet(() -> Optional.ofNullable(url)
                        .map(CHANNEL_URL_PATTERN::matcher).filter(Matcher::matches)
                        .map(channelUrlMatcher -> Pattern.compile("^.*\"externalId\":\"(?<externalId>[^\"]+)\".*$"))
                        .map(externalIdPattern -> StringUtility.splitLines(getHtml(url)).stream()
                                .map(externalIdPattern::matcher).filter(Matcher::matches)
                                .map(externalIdMatcher -> externalIdMatcher.group("externalId")).filter(Objects::nonNull)
                                .map(externalId -> externalId.replaceAll("^UC", "UU"))
                                .findFirst().orElse(""))
                        .orElse(""));
    }
    
    /**
     * Checks and attempts to automatically fetch the playlist id for a Channel if needed.
     *
     * @param channel The Channel.
     */
    public static void checkPlaylistId(Channel channel) {
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
