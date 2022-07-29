/*
 * File:    WebUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import youtube.channel.Video;

/**
 * Provides web utility methods for the Youtube Downloader.
 */
public final class WebUtils {
    
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
            
            FileUtils.copyURLToFile(new URL(url), download, 200, Integer.MAX_VALUE);
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
        
        return new Video(videoId, title, (date + " 00:00:00"), Utils.TMP_DIR, Configurator.Config.asMp3);
    }
    
}
