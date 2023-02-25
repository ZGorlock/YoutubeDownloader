/*
 * File:    ExecutableUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.CmdLine;
import commons.access.Internet;
import commons.access.OperatingSystem;
import commons.object.string.StringUtility;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
import youtube.config.Configurator;

/**
 * Provides executable utility methods for the Youtube Downloader.
 */
public final class ExecutableUtils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ExecutableUtils.class);
    
    
    //Constants
    
    /**
     * The executable directory.
     */
    public static final File EXECUTABLE_DIR = PathUtils.WORKING_DIR;
    
    /**
     * The Youtube Downloader executable to use.
     */
    public static final Executable EXECUTABLE = Executable.valueOf(
            Configurator.getSetting("executable", Executable.YT_DLP.getName()).toUpperCase().replace("-", "_"));
    
    
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
         * The call to the Executable.
         */
        private final String call;
        
        /**
         * The website of the Executable.
         */
        private final String website;
        
        
        //Constructors
        
        /**
         * Constructs an Executable.
         *
         * @param executableName The name of the Executable.
         * @param website        The website of the Executable.
         */
        Executable(String executableName, String website) {
            this.name = executableName;
            this.exe = new File(EXECUTABLE_DIR, (name + (OperatingSystem.isWindows() ? ('.' + Utils.EXECUTABLE_FILE_FORMAT) : "")));
            this.call = !EXECUTABLE_DIR.equals(PathUtils.WORKING_DIR) ? StringUtility.quote(exe.getAbsolutePath()) :
                        ((OperatingSystem.isWindows() ? "" : ('.' + PathUtils.SEPARATOR)) + exe.getName());
            this.website = website;
        }
        
        
        //Getters
        
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
         * Returns the call to the Executable.
         *
         * @return The call to the Executable.
         */
        public String getCall() {
            return call;
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
    
    
    //Static Methods
    
    /**
     * Determines if the exe exists and attempts to update it, or attempts to download it if it does not exist.
     *
     * @return Whether the exe exists.
     */
    public static boolean checkExe() {
        boolean exists = EXECUTABLE.getExe().exists();
        String currentVersion = Configurator.Config.preventExeVersionCheck ? "?" : getCurrentExecutableVersion();
        String latestVersion = Configurator.Config.preventExeVersionCheck ? "?" : getLatestExecutableVersion();
        String printedCurrentVersion = currentVersion.equals("?") ? "" : Color.number(" v" + currentVersion);
        String printedLatestVersion = latestVersion.equals("?") ? "" : Color.number(" v" + latestVersion);
        
        if (exists) {
            if (Configurator.Config.printExeVersion) {
                logger.trace(LogUtils.NEWLINE);
                logger.info(Color.exeFileName(EXECUTABLE) + printedCurrentVersion);
                logger.trace(LogUtils.NEWLINE);
            }
        } else {
            logger.trace(LogUtils.NEWLINE);
            logger.warn(Color.bad("Requires ") + Color.exeName(EXECUTABLE));
            logger.trace(LogUtils.NEWLINE);
        }
        
        if (exists && (currentVersion.isEmpty() || latestVersion.isEmpty())) {
            if (Configurator.Config.printExeVersion) {
                logger.warn(Color.bad("Unable to check for updates for ") + Color.exeName(EXECUTABLE));
                logger.trace(LogUtils.NEWLINE);
            }
            
        } else if (!exists || !currentVersion.equals(latestVersion)) {
            if (exists) {
                if (Configurator.Config.printExeVersion) {
                    logger.info(Color.base("Current Version:") + printedCurrentVersion + Color.base(" Latest Version:") + printedLatestVersion);
                } else {
                    logger.trace(LogUtils.NEWLINE);
                }
            }
            
            if (!Configurator.Config.preventExeAutoUpdate) {
                latestVersion = latestVersion.equals("?") ? getLatestExecutableVersion() : latestVersion;
                
                logger.info(Color.base("Downloading ") + Color.exeName(EXECUTABLE) + printedLatestVersion);
                File executable = downloadLatestExecutable(latestVersion);
                
                if ((executable == null) || !EXECUTABLE.getExe().exists() || !executable.getName().equals(EXECUTABLE.getExe().getName())) {
                    logger.warn(Color.bad("Unable to " + (exists ? "update" : "download") + " ") + Color.exeName(EXECUTABLE));
                } else {
                    logger.info(Color.base("Successfully " + (exists ? "updated to" : "downloaded") + " ") + Color.exeName(EXECUTABLE) + printedLatestVersion);
                }
            } else {
                logger.info(Color.bad("Would have " + (exists ? "updated to" : "downloaded") + " ") + Color.exeName(EXECUTABLE) + printedLatestVersion + Color.bad(" but auto updating is disabled"));
            }
            logger.trace(LogUtils.NEWLINE);
        }
        
        return EXECUTABLE.getExe().exists();
    }
    
    /**
     * Returns the current executable version.
     *
     * @return The current executable version, or an empty string if there was an error.
     */
    private static String getCurrentExecutableVersion() {
        return EXECUTABLE.getExe().exists() ? CmdLine.executeCmd(EXECUTABLE.getCall() + " --version")
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
                return Internet.downloadFile(EXECUTABLE.getWebsite() + "downloads/latest/" + EXECUTABLE.getExe().getName(), EXECUTABLE.getExe());
            
            case YT_DLP:
                //https://github.com/yt-dlp/yt-dlp/releases/download/2021.08.10/yt-dlp.exe
                return Internet.downloadFile(EXECUTABLE.getWebsite() + "releases/download/" + latestVersion + '/' + EXECUTABLE.getExe().getName(), EXECUTABLE.getExe());
            
            default:
                return null;
        }
    }
    
}
