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

import org.apache.commons.io.FileUtils;
import youtube.channel.Video;
import youtube.util.Color;
import youtube.util.Configurator;
import youtube.util.DownloadUtils;
import youtube.util.Utils;
import youtube.util.WebUtils;

/**
 * Downloads Youtube Videos.
 */
public class YoutubeDownloader {
    
    //Constants
    
    /**
     * The initial queue of Youtube video urls to download.
     */
    private static final File DOWNLOAD_QUEUE = new File("data/downloadQueue.txt");
    
    
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
        if (!Utils.startup(Utils.Project.YOUTUBE_DOWNLOADER)) {
            return;
        }
        
        File outputDir = getOutputDir();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.out.println(Color.bad("Unable to create output directory: ") + Color.file(outputDir.getAbsolutePath()));
            return;
        }
        if (DOWNLOAD_QUEUE.exists()) {
            download.addAll(Files.readAllLines(DOWNLOAD_QUEUE.toPath()));
        }
        
        System.out.println(Utils.NEWLINE);
        Scanner in = new Scanner(System.in);
        while (true) {
            List<String> work = new ArrayList<>(download);
            for (String url : work) {
                
                if (WebUtils.VIDEO_URL_PATTERN.matcher(url).matches()) {
                    Video video = WebUtils.fetchVideo(url);
                    video.updateOutputDir(outputDir);
                    
                    if (!Configurator.Config.preventDownload) {
                        System.out.println(Color.base("Downloading: ") + Color.video(video.title));
                        
                        DownloadUtils.DownloadResponse response = DownloadUtils.downloadYoutubeVideo(video);
                        System.out.println(Utils.INDENT + response.printedResponse());
                        
                    } else {
                        System.out.println(Color.bad("Would have downloaded: '") + Color.video(video.title) + Color.bad("' but downloading is disabled"));
                    }
                } else {
                    System.out.println(Color.bad("URL is not a Youtube video"));
                }
                download.remove(url);
                System.out.println(Utils.NEWLINE);
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
    
    /**
     * Returns the output directory.
     *
     * @return The output directory.
     */
    private static File getOutputDir() {
        return new File((String) Configurator.getSetting("location.output",
                (System.getProperty("user.home") + File.separatorChar + "Youtube")));
    }
    
}
