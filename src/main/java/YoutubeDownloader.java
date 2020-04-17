/*
 * File:    YoutubeDownloader.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeDownloader {
    
    private static final File downloadQueue = new File("data/downloadQueue.txt");
    
    private static final List<String> download = new ArrayList<>();
    
    private static final File outputDir = new File(System.getProperty("user.home") + File.separatorChar + "YoutubeDownloader");
    
    private static final Pattern videoUrlPattern = Pattern.compile("^.*/watch?.*v=(?<id>[^=?&]+).*$");
    
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws Exception {
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
                Matcher videoUrlMatcher = videoUrlPattern.matcher(video);
                if (videoUrlMatcher.matches()) {
                    String id = videoUrlMatcher.group("id");
                    YoutubeUtils.downloadYoutubeVideo(video, new File(outputDir, id + ".mp4"), false, true);
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
