/*
 * File:    YoutubeUtils.java
 * Package: youtube.tools
 * Author:  Zachary Gill
 */

package youtube.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.CmdLine;
import commons.access.OperatingSystem;
import commons.console.ConsoleProgressBar;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;

/**
 * Provides utility methods for the Youtube Downloader.
 */
public final class YoutubeUtils {
    
    //Enums
    
    /**
     * An enumeration of Youtube Downloader executables.
     */
    public enum Executable {
        
        //Values
        
        YOUTUBE_DL("youtube-dl", "https://www.youtube-dl.org/"),
        YT_DLP("yt-dlp", "https://github.com/yt-dlp/yt-dlp/");
        
        
        //Fields
        
        /**
         * The name of the Executable.
         */
        private final String name;
        
        /**
         * The exe of the Executable.
         */
        private final File exe;
        
        /**
         * The website of the Executable.
         */
        private final String website;
        
        
        //Constructors
        
        /**
         * Constructs an Executable.
         *
         * @param name    The name of the Executable.
         * @param website The website of the Executable.
         */
        Executable(String name, String website) {
            this.name = name;
            this.exe = new File(name + (OperatingSystem.isWindows() ? ".exe" : ""));
            this.website = website;
        }
        
        
        //Methods
        
        /**
         * Returns the name of the Executable.
         *
         * @return The name of the Executable.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Returns the exe of the Executable.
         *
         * @return The exe of the Executable.
         */
        public File getExe() {
            return exe;
        }
        
        /**
         * Returns the website of the Executable.
         *
         * @return The website of the Executable.
         */
        public String getWebsite() {
            return website;
        }
        
    }
    
    
    //Constants
    
    /**
     * The Youtube Downloader executable to use.
     */
    public static final Executable EXECUTABLE = Executable.valueOf(
            ((String) Configurator.getSetting("executable", Executable.YT_DLP.name)).toUpperCase().replace("-", "_"));
    
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
     * @param video              The video url.
     * @param output             The output file to create.
     * @param asMp3              Whether or not to save the video as an mp3.
     * @param logCommand         Whether or not to log the download command.
     * @param logWork            Whether or not to log the download work.
     * @param sponsorBlockConfig The SponsorBlock configuration for the active Channel.
     * @return Whether the video was successfully downloaded or not.
     * @throws Exception When there is an error downloading the video.
     */
    public static boolean downloadYoutubeVideo(String video, File output, boolean asMp3, boolean logCommand, boolean logWork, SponsorBlocker.SponsorBlockConfig sponsorBlockConfig) throws Exception {
        String outputPath = output.getAbsolutePath();
        outputPath = outputPath.substring(0, outputPath.lastIndexOf('.'));
        
        String cmd = EXECUTABLE.getExe().getName() + " " +
                "--output \"" + outputPath + ".%(ext)s\" " +
                "--geo-bypass --rm-cache-dir " +
                (asMp3 ? "--extract-audio --audio-format mp3 " :
                 "--format best ") +
                SponsorBlocker.getCommand(sponsorBlockConfig) +
                video;
        if (logCommand) {
            System.out.println(cmd);
        }
        String result = executeProcess(cmd, logWork);
        String[] resultLines = result.split("\r\n");
        
        return resultLines.length > 4 && !resultLines[resultLines.length - 1].toLowerCase().contains("internal server error. retrying (attempt 10 of 10)");
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
            parameterString.append(URLEncoder.encode(parameterEntry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(parameterEntry.getValue(), StandardCharsets.UTF_8));
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
        title = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "")
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS_SUPPLEMENT}+", "");
        title = title.replace("\\", "-")
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
                .replace("#", "- ")
                .replace("С", "C")
                .replaceAll("[^\\x00-\\x7F]", "")
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .replaceAll("\\s*[.\\-]$", "")
                .replaceAll("\\s+", " ")
                .trim();
        return title;
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
        
        String currentExecutableVersion = EXECUTABLE.getExe().exists() ? executeProcess(EXECUTABLE.getExe().getName() + " --version", false).trim() : "";
        String latestExecutableVersion = YoutubeUtils.getLatestExecutableVersion();
        
        if (EXECUTABLE.getExe().exists() && (currentExecutableVersion.isEmpty() || latestExecutableVersion.isEmpty())) {
            System.err.println("Unable to check for " + EXECUTABLE.getName() + " updates");
            
        } else if (!currentExecutableVersion.equals(latestExecutableVersion)) {
            if (!EXECUTABLE.getExe().exists()) {
                System.err.println("Requires " + EXECUTABLE.getName());
            } else {
                System.err.println("An update is available for " + EXECUTABLE.getName());
                System.err.println("Current Version: " + currentExecutableVersion + " | Latest Version: " + latestExecutableVersion);
            }
            System.err.println("Downloading...");
            
            File executable = YoutubeUtils.downloadLatestExecutable(latestExecutableVersion);
            if ((executable == null) || !EXECUTABLE.getExe().exists() || !executable.getName().equals(EXECUTABLE.getExe().getName())) {
                System.err.println("Unable to update " + EXECUTABLE.getName());
                return false;
            } else {
                System.err.println("Successfully updated " + EXECUTABLE.getName() + " to " + latestExecutableVersion);
                System.out.println();
                return true;
            }
        }
        
        return true;
    }
    
    /**
     * Returns the latest executable version.
     *
     * @return The latest executable version, or an empty string if there was an error.
     */
    public static String getLatestExecutableVersion() {
        String url;
        String versionPatternRegex;
        
        switch (EXECUTABLE) {
            case YOUTUBE_DL:
                url = EXECUTABLE.getWebsite();
                versionPatternRegex = "<a\\shref=\"latest\">Latest</a>\\s\\(v(?<version>[0-9.]+)\\)\\sdownloads:";
                break;
            
            case YT_DLP:
                url = EXECUTABLE.getWebsite() + "releases/";
                versionPatternRegex = "<a\\shref=\"/yt-dlp/yt-dlp/releases/tag/(?<version>[0-9.]+)\"";
                break;
            
            default:
                return "";
        }
        
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
            
            Pattern versionPattern = Pattern.compile(".*" + versionPatternRegex + ".*");
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
     * Downloads the latest executable.
     *
     * @param latestVersion The latest version of the executable.
     * @return The downloaded executable, or null if there was an error.
     */
    public static File downloadLatestExecutable(String latestVersion) {
        switch (EXECUTABLE) {
            case YOUTUBE_DL:
                //https://www.youtube-dl.org/downloads/latest/youtube-dl.exe
                return downloadFile(EXECUTABLE.getWebsite() + "downloads/latest/" + EXECUTABLE.getExe().getName(), EXECUTABLE.getExe());
            
            case YT_DLP:
                //https://github.com/yt-dlp/yt-dlp/releases/download/2021.08.10/yt-dlp.exe
                return downloadFile(EXECUTABLE.getWebsite() + "releases/download/" + latestVersion + '/' + EXECUTABLE.getExe().getName(), EXECUTABLE.getExe());
            
            default:
                return null;
        }
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
     * Executes a command line process.
     *
     * @param cmd The command.
     * @param log Whether or not to log the response from the process.
     * @return The response from the process.
     */
    public static String executeProcess(String cmd, boolean log) {
        try {
            ProcessBuilder builder = CmdLine.buildProcess(cmd);
            
            Process process = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            Pattern progressPattern = log ? null : Pattern.compile("^\\[download]\\s*(?<percentage>\\d+\\.\\d+)%\\s*of\\s*(?<total>\\d+\\.\\d+)(?<units>.iB).*$");
            Pattern resumePattern = log ? null : Pattern.compile("^\\[download]\\s*Resuming\\s*download\\s*at\\s*byte\\s*(?<initialProgress>\\d+).*$");
            ConsoleProgressBar progressBar = null;
            long initialProgress = -1L;
            
            StringBuilder response = new StringBuilder();
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                
                if (log) {
                    System.out.println(line);
                    
                } else {
                    if (initialProgress == -1) {
                        Matcher resumeMatcher = resumePattern.matcher(line);
                        if (resumeMatcher.matches()) {
                            initialProgress = Long.parseLong(resumeMatcher.group("initialProgress")) / 1024;
                        }
                    }
                    
                    Matcher progressMatcher = progressPattern.matcher(line);
                    if (progressMatcher.matches()) {
                        double percentage = Double.parseDouble(progressMatcher.group("percentage")) / 100.0;
                        double total = Double.parseDouble(progressMatcher.group("total"));
                        
                        String units = progressMatcher.group("units").replace("i", "");
                        switch (units) {
                            case "TB":
                                total *= 1024;
                            case "GB":
                                total *= 1024;
                            case "MB":
                                total *= 1024;
                            case "KB":
                            default:
                        }
                        
                        if (progressBar == null) {
                            progressBar = new ConsoleProgressBar("", (long) total, "KB");
                            progressBar.setAutoPrint(true);
                            initialProgress = Math.max(initialProgress, 0);
                            progressBar.setInitialProgress(initialProgress);
                        }
                        
                        long progress = (long) (percentage * total);
                        progressBar.update(progress);
                    }
                }
                
                response.append(line).append(System.lineSeparator());
            }
            
            process.waitFor();
            r.close();
            process.destroy();
            
            if (progressBar != null) {
                progressBar.complete();
            }
            
            return response.toString();
            
        } catch (Exception e) {
            return "Failed";
        }
    }
    
    /**
     * Downloads a file from a url to the specified file and returns the file.<br>
     * This is a blocking operation and should be called from a thread.
     *
     * @param url      The url to the file to download.
     * @param download The file to download to.
     * @return The downloaded file or null if there was an error.
     * @see FileUtils#copyURLToFile(URL, File, int, int)
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
    
}
