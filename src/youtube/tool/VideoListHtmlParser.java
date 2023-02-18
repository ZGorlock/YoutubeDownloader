/*
 * File:    VideoListHtmlParser.java
 * Package: youtube.tool
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.tool;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import commons.access.Project;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.YoutubeDownloader;
import youtube.config.Color;
import youtube.util.FileUtils;
import youtube.util.WebUtils;

/**
 * Parses a html video list and extracts a list of video ids.
 */
public class VideoListHtmlParser {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(VideoListHtmlParser.class);
    
    
    //Constants
    
    /**
     * The file containing the html video list data.<br/>
     * 1.   Open your "Liked Videos", "Watch Later", etc. page on Youtube.<br/>
     * 2.   Hold Page Down to get to the bottom of the list.<br/>
     * 3.   Push F12 and go to Elements.<br/>
     * 4.   Find the div with id="contents" and class="style-scope ytd-playlist-video-list-renderer"<br/>
     * 5.   Right click that line and and select Copy / Copy Element<br/>
     * 6.   Paste the copied html to 'data/videoList.html'
     */
    public static final File VIDEO_LIST_HTML_FILE = new File(Project.DATA_DIR, "videoList.html");
    
    
    //Static Fields
    
    /**
     * Whether to write the video list to the download queue file.
     */
    private static final boolean OUTPUT_TO_DOWNLOAD_QUEUE = false;
    
    
    //Main Method
    
    /**
     * Runs the Video List Html Parser.
     *
     * @param args Arguments to the main method.
     * @throws Exception When there is an error.
     */
    public static void main(String[] args) throws Exception {
        final String videoListHtml = FileUtils.readFileToString(VIDEO_LIST_HTML_FILE);
        
        final List<String> videoIdList = Optional.ofNullable(videoListHtml).map(Jsoup::parse)
                .map(e -> e.getElementsByClass("ytd-playlist-video-list-renderer").first())
                .map(e -> e.getElementsByClass("yt-simple-endpoint style-scope ytd-playlist-video-renderer"))
                .stream().flatMap(Collection::stream)
                .map(e -> e.attr("href")).filter(e -> !e.isEmpty())
                .map(WebUtils.VIDEO_URL_PATTERN::matcher).filter(Matcher::matches)
                .map(e -> e.group("video"))
                .collect(Collectors.toList());
        
        videoIdList.stream().map(Color::log).forEach(logger::debug);
        if (OUTPUT_TO_DOWNLOAD_QUEUE) {
            FileUtils.writeLines(YoutubeDownloader.DOWNLOAD_QUEUE, videoIdList, true);
        }
    }
    
}
