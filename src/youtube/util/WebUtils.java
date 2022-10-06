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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
import youtube.channel.Channels;
import youtube.channel.Video;
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
     * The base url for Youtube videos.
     */
    public static final String YOUTUBE_BASE = "https://www.youtube.com";
    
    /**
     * The base url for Youtube videos.
     */
    public static final String VIDEO_BASE = YOUTUBE_BASE + "/watch?v=";
    
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
    
    
    //Functions
    
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
     * @param url The Youtube video url.
     * @return The fetched Video.
     */
    public static Video fetchVideo(String url) {
        Matcher videoUrlMatcher = VIDEO_URL_PATTERN.matcher(url);
        
        String videoId = videoUrlMatcher.matches() ? videoUrlMatcher.group("video") : "";
        String title = videoId;
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        
        if (!Configurator.Config.preventVideoFetch) {
            Pattern metaPattern = Pattern.compile("^\\s*<meta\\s*itemprop=\"(?<prop>[^\"]+)\"\\s*content=\"(?<value>[^\"]+)\"\\s*>\\s*$");
            
            String html = getHtml(url);
            String[] lines = html.split("\n");
            
            for (String line : lines) {
                Matcher metaMatcher = metaPattern.matcher(line);
                if (metaMatcher.matches()) {
                    switch (metaMatcher.group("prop")) {
                        case "videoId":
                            videoId = metaMatcher.group("value");
                            break;
                        case "name":
                            title = metaMatcher.group("value");
                            break;
                        case "datePublished":
                            date = metaMatcher.group("value");
                            break;
                    }
                }
            }
        }
        
        return new Video(videoId, title, (date + " 00:00:00"), PathUtils.TMP_DIR, Configurator.Config.asMp3);
    }
    
    /**
     * Fetches the Channel playlist id from a Youtube channel url.
     *
     * @param url The Youtube channel url.
     * @return The fetched playlist id, or an empty string if there was an error.
     */
    public static String fetchPlaylistId(String url) {
        Matcher playlistUrlMatcher = PLAYLIST_URL_PATTERN.matcher(url);
        if (playlistUrlMatcher.matches()) {
            String playlistId = playlistUrlMatcher.group("playlist");
            if (playlistId != null) {
                return playlistId;
            }
        }
        
        Matcher channelUrlMatcher = CHANNEL_URL_PATTERN.matcher(url);
        if (channelUrlMatcher.matches()) {
            Pattern externalIdPattern = Pattern.compile("^.*\"externalId\":\"(?<externalId>[^\"]+)\".*$");
            
            String html = getHtml(url);
            String[] lines = html.split("\n");
            
            for (String line : lines) {
                Matcher externalIdMatcher = externalIdPattern.matcher(line);
                if (externalIdMatcher.matches()) {
                    String externalId = externalIdMatcher.group("externalId");
                    if (externalId != null) {
                        return externalId.replaceAll("^UC", "UU");
                    }
                }
            }
        }
        
        return "";
    }
    
    /**
     * Checks and attempts to automatically fetch the playlist id for a Channel if needed.
     *
     * @param channel The Channel.
     */
    public static void checkPlaylistId(Channel channel) {
        if ((channel.playlistId == null) || channel.playlistId.isEmpty()) {
            channel.playlistId = WebUtils.fetchPlaylistId(channel.url);
            
            System.out.println(Color.bad("Channel does not have a playlistId defined, please add this to the Channel configuration"));
            if (channel.playlistId.isEmpty()) {
                throw new RuntimeException();
            }
            System.out.println(Color.bad("I was able to fetch it automatically based on the defined url: ") + Color.EXE.apply(channel.playlistId));
            System.out.println(Color.bad("Automatically fetching it every time is slow though, it is better to add it to ") + Color.filePath(Channels.CHANNELS_FILE));
        }
    }
    
}
