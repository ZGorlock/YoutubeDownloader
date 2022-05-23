/*
 * File:    YoutubeDownloader.java
 * Package: youtube
 * Author:  Zachary Gill
 */

package youtube;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import youtube.util.Configurator;
import youtube.util.YoutubeUtils;

/**
 * Downloads Youtube Videos.
 */
public class YoutubeDownloader {
    
    //Constants
    
    /**
     * The initial queue of Youtube video urls to download.
     */
    private static final File DOWNLOAD_QUEUE = new File("data/downloadQueue.txt");
    
    /**
     * The output directory for downloaded videos.
     */
    private static final File OUTPUT_DIR = new File(System.getProperty("user.home") + File.separatorChar + "YoutubeDownloader");
    
    //Loads the configuration settings in Configurator
    static {
        Configurator.loadSettings("YoutubeDownloader");
    }
    
    
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
     * @throws Exception When there is an error.
     */
    public static void main(String[] args) throws Exception {
        if (!YoutubeUtils.doStartupChecks()) {
            return;
        }
        
        if (!OUTPUT_DIR.exists() && !OUTPUT_DIR.mkdirs()) {
            System.err.println("Unable to create output directory");
            return;
        }
        if (DOWNLOAD_QUEUE.exists()) {
            download.addAll(Files.readAllLines(DOWNLOAD_QUEUE.toPath()));
        }
        
        System.out.println();
        Scanner in = new Scanner(System.in);
        while (true) {
            List<String> work = new ArrayList<>(download);
            for (String url : work) {
                System.out.println("Downloading: " + url);
                Matcher videoUrlMatcher = YoutubeUtils.VIDEO_URL_PATTERN.matcher(url);
                if (videoUrlMatcher.matches()) {
                    String id = videoUrlMatcher.group("id");
                    switch (YoutubeUtils.downloadYoutubeVideo(url, new File(OUTPUT_DIR, id))) {
                        case SUCCESS:
                            System.out.println("Done");
                            break;
                        case FAILURE:
                            System.err.println("Failed");
                            break;
                        case ERROR:
                            System.err.println("Error");
                            break;
                    }
                } else {
                    System.err.println("URL is not a Youtube video");
                }
                download.remove(url);
                System.out.println();
            }
            FileUtils.writeLines(DOWNLOAD_QUEUE, download);
            
            String input = in.nextLine();
            if (!input.isEmpty()) {
                download.add(input);
                FileUtils.writeLines(DOWNLOAD_QUEUE, download);
            } else {
                break;
            }
        }
    }
    
}
