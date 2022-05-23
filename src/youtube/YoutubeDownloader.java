/*
 * File:    YoutubeDownloader.java
 * Package: youtube
 * Author:  Zachary Gill
 */

package youtube;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import youtube.channel.Video;
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
    
    //Loads the configuration settings in Configurator
    static {
        Configurator.loadSettings("YoutubeDownloader");
    }
    
    
    //Static Fields
    
    /**
     * The output directory for downloaded content.
     */
    private static final File outputDir = new File((String) Configurator.getSetting("location.output",
            (System.getProperty("user.home") + File.separatorChar + "YoutubeDownloader")));
    
    
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
        
        if (!outputDir.exists() && !outputDir.mkdirs()) {
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
                
                if (YoutubeUtils.VIDEO_URL_PATTERN.matcher(url).matches()) {
                    Video video = fetchVideo(url);
                    
                    System.out.println("Downloading: " + video.title);
                    switch (YoutubeUtils.downloadYoutubeVideo(video)) {
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
    
    private static Video fetchVideo(String url) {
        Matcher urlMatcher = YoutubeUtils.VIDEO_URL_PATTERN.matcher(url);
        
        String videoId = urlMatcher.matches() ? urlMatcher.group("id") : "";
        String title = videoId;
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        
        try {
            String html = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36")
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .followRedirects(true)
                    .execute()
                    .parse()
                    .toString();
            
            Pattern metaPattern = Pattern.compile("^\\s*<meta\\s*itemprop=\"(?<prop>[^\"]+)\"\\s*content=\"(?<value>[^\"]+)\"\\s*>\\s*$");
            
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
            
        } catch (IOException ignored) {
        }
        
        return new Video(videoId, title, (date + " 00:00:00"), outputDir, Configurator.Config.asMp3);
    }
    
}
