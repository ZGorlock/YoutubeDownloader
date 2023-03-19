/*
 * File:    YoutubeDownloader.java
 * Package: youtube
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.access.Project;
import commons.lambda.stream.mapper.Mappers;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
import youtube.config.Configurator;
import youtube.entity.Video;
import youtube.util.DownloadUtils;
import youtube.util.FileUtils;
import youtube.util.LogUtils;
import youtube.util.PathUtils;
import youtube.util.Utils;
import youtube.util.WebUtils;

/**
 * Downloads Youtube Videos.
 */
public class YoutubeDownloader {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(YoutubeDownloader.class);
    
    
    //Constants
    
    /**
     * The initial queue of Youtube video urls to download.
     */
    public static final File DOWNLOAD_QUEUE = new File(Project.DATA_DIR, FileUtils.setFormat("downloadQueue", FileUtils.LIST_FILE_FORMAT));
    
    
    //Static Fields
    
    /**
     * The list of Youtube video urls to download.
     */
    private static final List<String> download = new ArrayList<>();
    
    
    //Main Method
    
    /**
     * The main method for the Youtube Downloader.
     *
     * @param args The arguments to the main method.
     */
    public static void main(String[] args) {
        logger.info(Color.number("------------------"));
        logger.info(Color.number("Youtube Downloader"));
        logger.info(Color.number("------------------"));
        logger.trace(LogUtils.NEWLINE);
        
        if (!Utils.startup(Configurator.Program.YOUTUBE_DOWNLOADER)) {
            return;
        }
        
        if (!FileUtils.Config.outputDir.exists() && !FileUtils.Config.outputDir.mkdirs()) {
            logger.error(Color.bad("Unable to create output directory: ") + Color.quoteFilePath(FileUtils.Config.outputDir));
            return;
        }
        
        if (!Configurator.Config.preventRun) {
            run();
        }
    }
    
    /**
     * Runs the Youtube Downloader.
     */
    private static void run() {
        logger.debug(Color.log("Starting..."));
        logger.trace(LogUtils.NEWLINE);
        
        loadDownloadQueue();
        
        try (Scanner in = new Scanner(System.in)) {
            do {
                download.forEach(url -> {
                    download(url);
                    logger.trace(LogUtils.NEWLINE);
                });
                download.clear();
                
                saveDownloadQueue();
                getInput(in);
                
            } while (!download.isEmpty());
        }
    }
    
    /**
     * Downloads a video.
     *
     * @param url The url of the video.
     * @return The download response.
     */
    private static DownloadUtils.DownloadResponse download(String url) {
        return Optional.ofNullable(url)
                .map(YoutubeDownloader::getVideoDetails)
                .filter(YoutubeDownloader::allowDownload)
                .map(DownloadUtils::downloadYoutubeVideo)
                .orElse(null);
    }
    
    /**
     * Returns whether or not downloading is allowed.
     *
     * @param video The video.
     * @return Whether or not downloading is allowed.
     */
    private static boolean allowDownload(Video video) {
        return Optional.of(Configurator.Config.preventDownload).map(e -> !e)
                .filter(e -> e).map(Mappers.forEach(e ->
                        logger.info(Color.base("Downloading: ") + Color.videoTitle(video))))
                .orElseGet(() -> {
                    logger.info(Color.bad("Would have downloaded: ") + Color.quoteVideoTitle(video) + Color.bad(" but downloading is disabled"));
                    return false;
                });
    }
    
    /**
     * Fetches information about a video.
     *
     * @param url The url of the video.
     * @return The Video, or null if it could the details could not be fetched.
     */
    private static Video getVideoDetails(String url) {
        return Optional.ofNullable(url)
                .map(YoutubeDownloader::parseUrl)
                .map(WebUtils::fetchVideo)
                .map(Mappers.forEach(e -> e.updateOutputDir(FileUtils.Config.outputDir)))
                .orElseGet(() -> {
                    logger.warn(Color.bad("Failed to fetch the video details from: ") + Color.link(url));
                    return null;
                });
    }
    
    /**
     * Parses a video url.
     *
     * @param url The url of the video.
     * @return The parsed url, or null if it is not valid.
     */
    private static String parseUrl(String url) {
        return Optional.ofNullable(url)
                .filter(WebUtils::isVideoUrl)
                .orElseGet(() -> {
                    logger.warn(Color.bad("The URL: ") + Color.link(url) + Color.bad(" is not a Youtube video"));
                    return null;
                });
    }
    
    /**
     * Waits for an input to the console.
     *
     * @param in The input scanner.
     */
    private static void getInput(Scanner in) {
        Optional.ofNullable(in)
                .map(Mappers.forEach(e -> System.out.print(Color.log(": "))))
                .map(Scanner::nextLine).map(String::strip)
                .filter(e -> !e.isBlank())
                .ifPresent(input -> {
                    download.add(input);
                    saveDownloadQueue();
                });
    }
    
    /**
     * Loads the download queue from file.
     *
     * @throws RuntimeException When the download queue could not be loaded.
     */
    private static void loadDownloadQueue() {
        Optional.of(DOWNLOAD_QUEUE)
                .filter(file -> file.exists() || Filesystem.createFile(file))
                .map(Filesystem::readLines)
                .map(lines -> lines.stream()
                        .filter(line -> !StringUtility.isNullOrBlank(line))
                        .collect(Collectors.toList()))
                .map(download::addAll)
                .orElseThrow(() -> new RuntimeException(new IOException("Error reading: " + PathUtils.path(DOWNLOAD_QUEUE))));
    }
    
    /**
     * Saves the download queue to file.
     *
     * @throws RuntimeException When the download queue could not be saved.
     */
    private static void saveDownloadQueue() {
        Optional.of(download)
                .map(downloadQueue -> downloadQueue.stream()
                        .filter(line -> !StringUtility.isNullOrBlank(line))
                        .collect(Collectors.toList()))
                .filter(downloadQueue -> Filesystem.writeLines(DOWNLOAD_QUEUE, downloadQueue))
                .orElseThrow(() -> new RuntimeException(new IOException("Error writing: " + PathUtils.path(DOWNLOAD_QUEUE))));
    }
    
}
