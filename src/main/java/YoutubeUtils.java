/*
 * File:    YoutubeUtils.java
 * Author:  Zachary Gill
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

/**
 * Provides utility methods for the Youtube Downloader.
 */
public final class YoutubeUtils {
    
    //Constants
    
    /**
     * The base url for Youtube videos.
     */
    public static final String VIDEO_BASE = "https://www.youtube.com/watch?v=";
    
    /**
     * The regex pattern for a Youtube url.
     */
    public static final Pattern VIDEO_URL_PATTERN = Pattern.compile("^.*/watch?.*v=(?<id>[^=?&]+).*$");
    
    
    //Functions
    
    /**
     * Downloads a Youtube video.
     *
     * @param video      The video url.
     * @param output     The output file to create.
     * @param asMp3      Whether or not to save the video as an mp3.
     * @param logCommand Whether or not to log the youtube-dl command.
     * @param logWork    Whether or not to log the download work.
     * @return Whether the video was successfully downloaded or not.
     * @throws Exception When there is an error downloading the video.
     */
    public static boolean downloadYoutubeVideo(String video, File output, boolean asMp3, boolean logCommand, boolean logWork) throws Exception {
        String outputPath = output.getAbsolutePath();
        outputPath = outputPath.substring(0, outputPath.lastIndexOf('.'));
        
        String cmd = "youtube-dl.exe " +
                "--output \"" + outputPath + ".%(ext)s\" " +
                "--geo-bypass --rm-cache-dir " +
                (asMp3 ? "--extract-audio --audio-format mp3 " :
                 "--format best ") +
                video;
        if (logCommand) {
            System.out.println(cmd);
        }
        String result = executeProcess(cmd, logWork);
        
        return result.split("\r\n").length > 2;
    }
    
    /**
     * Builds a parameter string to be appended to a url.
     *
     * @param parameters A map of parameters.
     * @return The parameter string.
     * @throws Exception When there is an error encoding the string.
     */
    public static String buildParameterString(Map<String, String> parameters) throws Exception {
        StringBuilder parameterString = new StringBuilder("?");
        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            if (parameterString.length() > 1) {
                parameterString.append("&");
            }
            parameterString.append(URLEncoder.encode(parameterEntry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(parameterEntry.getValue(), "UTF-8"));
        }
        return parameterString.toString();
    }
    
    /**
     * Cleans the title of a Youtube video.
     *
     * @param title The title.
     * @return The cleaned title.
     */
    public static String cleanTitle(String title) {
        return title.replace("\\", "-")
                .replace("/", "-")
                .replace(":", "-")
                .replace("*", "-")
                .replace("?", "")
                .replace("\"", "'")
                .replace("<", "-")
                .replace(">", "-")
                .replace("|", "-")
                .replace("‒", "-")
                .replace("—", "-")
                .replace("С", "C")
                .replaceAll("[^\\x00-\\x7F]", "")
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .replaceAll("\\s+", " ");
    }
    
    /**
     * Determines if a video has already been downloaded or not.
     *
     * @param output The output file for the video.
     * @return Whether the video has already been downloaded or not.
     */
    public static boolean videoExists(File output) {
        File outputDir = output.getParentFile();
        if (!outputDir.exists()) {
            return false;
        }
        File[] existingFiles = outputDir.listFiles();
        if (existingFiles == null) {
            return false;
        }
        
        String outputName = output.getName().replaceAll("[^a-zA-Z0-9]", "").replaceAll("\\s+", " ");
        for (File existingFile : existingFiles) {
            String existingName = existingFile.getName().replaceAll("[^a-zA-Z0-9]", "").replaceAll("\\s+", " ");
            if (existingName.equalsIgnoreCase(outputName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Performs startup checks.
     *
     * @return Whether all checks were successful or not.
     */
    public static boolean doStartupChecks() {
        if (!YoutubeUtils.isOnline()) {
            System.err.println("Internet access is required");
            return false;
        }
        
        String currentYoutubeDlVersion = executeProcess("youtube-dl.exe --version", false).trim();
        String latestYoutubeDlVersion = YoutubeUtils.getCurrentYoutubeDlVersion();
        if (currentYoutubeDlVersion.isEmpty()) {
            System.err.println("Unable to check for youtube-dl updates");
        } else if (!currentYoutubeDlVersion.equals(latestYoutubeDlVersion)) {
            System.err.println("An update is available for youtube-dl");
            System.err.println("Current Version: " + currentYoutubeDlVersion + " | Latest Version: " + latestYoutubeDlVersion);
            System.err.println("Download: https://www.youtube-dl.org/downloads/latest/youtube-dl.exe");
            return false;
        }
        
        return true;
    }
    
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
     * Returns the current youtube-dl version.
     *
     * @return The current youtube-dl version.
     */
    public static String getCurrentYoutubeDlVersion() {
        try {
            String html = Jsoup.connect("https://youtube-dl.org/")
                    .ignoreContentType(true)
                    .maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36")
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .followRedirects(true)
                    .execute()
                    .parse()
                    .toString();
            
            Pattern versionPattern = Pattern.compile(".*<a\\shref=\"latest\">Latest</a>\\s\\(v(?<version>[0-9.]+)\\)\\sdownloads:.*");
            String[] lines = html.split("\n");
            for (String line : lines) {
                Matcher versionMatcher = versionPattern.matcher(line);
                if (versionMatcher.matches()) {
                    return versionMatcher.group("version");
                }
            }
            
        } catch (IOException ignored) {
        }
        return "";
    }
    
    /**
     * Executes a command line process.
     *
     * @param cmd The command.
     * @param log Whether or not to log the response from the process.
     * @return The response from the process.
     */
    public static String executeProcess(String cmd, boolean log) {
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", cmd);
            
            Process process = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            StringBuilder response = new StringBuilder();
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                if (log) {
                    System.out.println(line);
                }
                response.append(line).append(System.lineSeparator());
            }
            
            process.waitFor();
            r.close();
            process.destroy();
            
            return response.toString();
            
        } catch (Exception e) {
            return "Failed";
        }
    }
    
}
