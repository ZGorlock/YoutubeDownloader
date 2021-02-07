/*
 * File:    YoutubeDownloader.java
 * Author:  Zachary Gill
 */

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;

/**
 * Downloads Youtube Videos.
 */
public class YoutubeDownloader {
    
    //Constants
    
    /**
     * The initial queue of Youtube video urls to download.
     */
    private static final File downloadQueue = new File("data/downloadQueue.txt");
    
    /**
     * The list of Youtube video urls to download.
     */
    private static final List<String> download = new ArrayList<>();
    
    /**
     * The output directory for downloaded videos.
     */
    private static final File outputDir = new File(System.getProperty("user.home") + File.separatorChar + "YoutubeDownloader");
    
    
    //Static Fields
    
    /**
     * A flag indicating whether to download the videos as mp3 files or not.
     */
    private static final boolean asMp3 = false;
    
    /**
     * A flag indicating whether to the log the download command or not.
     */
    private static final boolean logCommand = true;
    
    /**
     * A flag indicating whether to log the download work or not.
     */
    private static final boolean logWork = true;
    
    
    //Main Method
    
    /**
     * The main method for the Youtube Downloader.
     *
     * @param args The arguments to the main method.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws Exception {
        if (!YoutubeUtils.doStartupChecks()) {
            return;
        }
        
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.err.println("Unable to create output directory");
            return;
        }
        if (downloadQueue.exists()) {
            download.addAll(Files.readAllLines(downloadQueue.toPath()));
        }
        
        Scanner in = new Scanner(System.in);
        while (true) {
            List<String> work = new ArrayList<>(download);
            for (String video : work) {
                System.out.println("Downloading: " + video);
                Matcher videoUrlMatcher = YoutubeUtils.VIDEO_URL_PATTERN.matcher(video);
                if (videoUrlMatcher.matches()) {
                    String id = videoUrlMatcher.group("id");
                    if (YoutubeUtils.downloadYoutubeVideo(video, new File(outputDir, id + ".mp4"), asMp3, logCommand, logWork)) {
                        System.out.println("Done");
                    } else {
                        System.err.println("Failed");
                    }
                } else {
                    System.err.println("URL is not a Youtube video");
                }
                download.remove(video);
            }
            Files.write(downloadQueue.toPath(), download);
            
            String input = in.nextLine();
            if (!input.isEmpty()) {
                download.add(input);
                Files.write(downloadQueue.toPath(), download);
            }
        }
    }
    
}
