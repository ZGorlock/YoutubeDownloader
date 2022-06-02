/*
 * File:    YoutubeUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.CmdLine;
import commons.access.OperatingSystem;
import commons.object.string.StringUtility;
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
    
    /**
     * A list of possible video formats.
     */
    public static final List<String> VIDEO_FORMATS = List.of("3gp", "flv", "mp4", "webm");
    
    /**
     * A list of possible audio formats.
     */
    public static final List<String> AUDIO_FORMATS = List.of("aac", "m4a", "mp3", "ogg", "wav");
    
    /**
     * The newline string.
     */
    public static final String NEWLINE = Color.base("");
    
    /**
     * The indentation string.
     */
    public static final String INDENT = Color.base(StringUtility.spaces(5));
    
    
    //Functions
    
    /**
     * Tries to find a video.
     *
     * @param output The output file for the video.
     * @return The found file or files.
     */
    public static File findVideo(File output) {
        File outputDir = output.getParentFile();
        if (!outputDir.exists()) {
            return null;
        }
        
        File[] existingFiles = outputDir.listFiles();
        if (existingFiles == null) {
            return null;
        }
        
        String name = output.getName().replaceAll("\\.[^.]+$|[^a-zA-Z\\d+]|\\s+", "");
        
        List<File> found = new ArrayList<>();
        for (File existingFile : existingFiles) {
            String existingName = existingFile.getName().replaceAll("\\.[^.]+$|[^a-zA-Z\\d+]|\\s+", "");
            if (existingName.equalsIgnoreCase(name) && (existingFile.length() > 0)) {
                String format = getFormat(output.getName());
                String existingFormat = getFormat(existingFile.getName());
                if (format.equalsIgnoreCase(existingFormat) ||
                        (VIDEO_FORMATS.contains(format) && VIDEO_FORMATS.contains(existingFormat)) ||
                        (AUDIO_FORMATS.contains(format) && AUDIO_FORMATS.contains(existingFormat))) {
                    found.add(existingFile);
                }
            }
        }
        
        if (found.size() == 1) {
            return found.get(0);
        } else {
            return null;
        }
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
                .replace(" ", " ")
                .replace("&amp;", "&")
                .replaceAll("^#(sh[oa]rts?)", "$1 - ")
                .replace("#", "- ")
                .replaceAll("[—–-]", "-")
                .replaceAll("[’‘]", "'")
                .replace("С", "C")
                .replaceAll("[™©®†]", "")
                .replace("¹", "1")
                .replace("²", "2")
                .replace("³", "3")
                .replace("×", "x")
                .replace("÷", "%")
                .replace("⋯", "...")
                .replaceAll("[^\\x00-\\x7F]", "+")
                .replaceAll("\\p{Cntrl}&&[^\r\n\t]", "")
                .replaceAll("\\s*[.!\\-]+$", "")
                .replaceAll("(?:\\+\\s+)+", "+ ")
                .replaceAll("\\++", "+")
                .replaceAll("^\\s*\\+\\s*", "")
                .replaceAll("(?:-\\s+)+", "- ")
                .replaceAll("-+", "-")
                .replaceAll("^\\s*-\\s*", "")
                .replace("+-", "+ -")
                .replaceAll("^\\s+|\\s+$", "")
                .replaceAll("(^-\\s*)+|(\\s*-)+$", "")
                .replaceAll("!(?:\\s*!)+", "!")
                .replaceAll("\\s+", " ")
                .replaceAll("\\$+", Matcher.quoteReplacement("$"))
                .trim();
        return title;
    }
    
    /**
     * Returns the file format of a file name.
     *
     * @param fileName The file name.
     * @return The file format of the file name.
     */
    public static String getFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
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
     * Formats a header that will be printed to the console.
     *
     * @param header The header.
     * @return The formatted header.
     */
    public static String formatHeader(String header) {
        return StringUtility.toTitleCase(header.toLowerCase().replace("_", " "));
    }
    
    /**
     * Performs startup checks.
     *
     * @return Whether all checks were successful or not.
     */
    public static boolean doStartupChecks() {
        if (!isOnline()) {
            System.out.println(NEWLINE);
            System.out.println(Color.bad("Internet access is required"));
            return false;
        }
        
        return checkExe();
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
     * Determines if the exe exists and attempts to update it, or attempts to download it if it does not exist.
     *
     * @return Whether the exe exists.
     */
    private static boolean checkExe() {
        boolean exists = EXECUTABLE.getExe().exists();
        String currentVersion = Configurator.Config.preventExeVersionCheck ? "?" : getCurrentExecutableVersion();
        String latestVersion = Configurator.Config.preventExeVersionCheck ? "?" : getLatestExecutableVersion();
        String printedCurrentVersion = currentVersion.equals("?") ? "" : Color.number(" v" + currentVersion);
        String printedLatestVersion = latestVersion.equals("?") ? "" : Color.number(" v" + latestVersion);
        
        if (exists) {
            if (Configurator.Config.printExeVersion) {
                System.out.println(NEWLINE);
                System.out.println(Color.exe(EXECUTABLE.getExe().getName()) + printedCurrentVersion);
                System.out.println(NEWLINE);
            }
        } else {
            System.out.println(NEWLINE);
            System.out.println(Color.bad("Requires ") + Color.exe(EXECUTABLE.getName()));
            System.out.println(NEWLINE);
        }
        
        if (exists && (currentVersion.isEmpty() || latestVersion.isEmpty())) {
            if (Configurator.Config.printExeVersion) {
                System.out.println(Color.bad("Unable to check for updates for ") + Color.exe(EXECUTABLE.getName()));
                System.out.println(NEWLINE);
            }
            
        } else if (!exists || (!currentVersion.equals(latestVersion))) {
            if (exists) {
                if (Configurator.Config.printExeVersion) {
                    System.out.println(Color.base("Current Version:") + printedCurrentVersion + Color.base(" Latest Version:") + printedLatestVersion);
                } else {
                    System.out.println(NEWLINE);
                }
            }
            
            if (!Configurator.Config.preventExeAutoUpdate) {
                latestVersion = latestVersion.equals("?") ? getLatestExecutableVersion() : latestVersion;
                
                System.out.println(Color.base("Downloading ") + Color.exe(EXECUTABLE.getName()) + printedLatestVersion);
                File executable = downloadLatestExecutable(latestVersion);
                
                if ((executable == null) || !EXECUTABLE.getExe().exists() || !executable.getName().equals(EXECUTABLE.getExe().getName())) {
                    System.out.println(Color.bad("Unable to " + (exists ? "update" : "download") + " ") + Color.exe(EXECUTABLE.getName()));
                } else {
                    System.out.println(Color.base("Successfully " + (exists ? "updated to" : "downloaded") + " ") + Color.exe(EXECUTABLE.getName()) + printedLatestVersion);
                }
            } else {
                System.out.println(Color.bad("Would have " + (exists ? "updated to" : "downloaded") + " ") + Color.exe(EXECUTABLE.getName()) + printedLatestVersion + Color.bad(" but auto updating is disabled"));
            }
            System.out.println(NEWLINE);
        }
        
        return EXECUTABLE.getExe().exists();
    }
    
    /**
     * Returns the current executable version.
     *
     * @return The current executable version, or an empty string if there was an error.
     */
    private static String getCurrentExecutableVersion() {
        return EXECUTABLE.getExe().exists() ? CmdLine.executeCmd(EXECUTABLE.getExe().getName() + " --version")
                .replaceAll("\r?\n", "").replaceAll("\\[\\*].*$", "").trim() : "";
    }
    
    /**
     * Returns the latest executable version.
     *
     * @return The latest executable version, or an empty string if there was an error.
     */
    private static String getLatestExecutableVersion() {
        String url;
        String versionPatternRegex;
        
        switch (EXECUTABLE) {
            case YOUTUBE_DL:
                url = EXECUTABLE.getWebsite();
                versionPatternRegex = "<a\\shref=\"latest\">Latest</a>\\s\\(v(?<version>[\\d.]+)\\)\\sdownloads:";
                break;
            
            case YT_DLP:
                url = EXECUTABLE.getWebsite() + "releases/";
                versionPatternRegex = "<a\\shref=\"/yt-dlp/yt-dlp/releases/tag/(?<version>[\\d.]+)\"";
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
    private static File downloadLatestExecutable(String latestVersion) {
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
     * Downloads a file from a url to the specified file and returns the file.<br>
     * This is a blocking operation and should be called from a thread.
     *
     * @param url      The url to the file to download.
     * @param download The file to download to.
     * @return The downloaded file or null if there was an error.
     * @see FileUtils#copyURLToFile(URL, File, int, int)
     */
    private static File downloadFile(String url, File download) {
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
