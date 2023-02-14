/*
 * File:    YoutubeDownloader.java
 * Package: youtube
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import commons.access.Project;
import commons.lambda.function.unchecked.UncheckedConsumer;
import commons.lambda.stream.mapper.Mappers;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
import youtube.config.Configurator;
import youtube.entity.Video;
import youtube.util.DownloadUtils;
import youtube.util.FileUtils;
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
    public static final File DOWNLOAD_QUEUE = new File(Project.DATA_DIR, ("downloadQueue" + '.' + Utils.LIST_FILE_FORMAT));
    
    /**
     * The default output location.
     */
    public static final String DEFAULT_OUTPUT_LOCATION = PathUtils.path(PathUtils.getUserHomePath(), "Youtube");
    
    
    //Static Fields
    
    /**
     * The list of Youtube video urls to download.
     */
    private static final List<String> download = new ArrayList<>();
    
    /**
     * The output directory.
     */
    private static File outputDir;
    
    
    //Main Method
    
    /**
     * The main method for the Youtube Downloader.
     *
     * @param args The arguments to the main method.
     */
    public static void main(String[] args) {
        if (!Utils.startup(Utils.Program.YOUTUBE_DOWNLOADER)) {
            return;
        }
        
        outputDir = getOutputDir();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.out.println(Color.bad("Unable to create output directory: ") + Color.filePath(outputDir));
            return;
        }
        System.out.println(Utils.NEWLINE);
        
        loadDownloadQueue();
        try (Scanner in = new Scanner(System.in)) {
            
            do {
                download.forEach(url -> {
                    download(url);
                    System.out.println(Utils.NEWLINE);
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
                .map(Mappers.forEach(e -> System.out.println(Utils.INDENT + e.printedResponse())))
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
                        System.out.println(Color.base("Downloading: ") + Color.video(video.getTitle()))))
                .orElseGet(() -> {
                    System.out.println(Color.bad("Would have downloaded: ") + Color.videoName(video.getTitle()) + Color.bad(" but downloading is disabled"));
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
                .map(Mappers.forEach(e -> e.updateOutputDir(outputDir)))
                .orElseGet(() -> {
                    System.out.println(Color.bad("Failed to fetch the video details from: ") + Color.link(url));
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
                .map(WebUtils.VIDEO_URL_PATTERN::matcher).filter(Matcher::matches).map(Matcher::group)
                .orElseGet(() -> {
                    System.out.println(Color.bad("The URL: ") + Color.link(url) + Color.bad(" is not a Youtube video"));
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
        Optional.of(download).ifPresent((UncheckedConsumer<List<String>>) download ->
                download.addAll(FileUtils.readLines(DOWNLOAD_QUEUE).stream()
                        .filter(e -> !StringUtility.isNullOrBlank(e))
                        .collect(Collectors.toList())));
    }
    
    /**
     * Saves the download queue to file.
     *
     * @throws RuntimeException When the download queue could not be saved.
     */
    private static void saveDownloadQueue() {
        Optional.of(download).ifPresent((UncheckedConsumer<List<String>>) download ->
                FileUtils.writeLines(DOWNLOAD_QUEUE, download.stream()
                        .filter(e -> !StringUtility.isNullOrBlank(e))
                        .collect(Collectors.toList())));
    }
    
    /**
     * Returns the output directory.
     *
     * @return The output directory.
     */
    private static File getOutputDir() {
        return new File(Configurator.getSetting("location.output", DEFAULT_OUTPUT_LOCATION));
    }
    
}
