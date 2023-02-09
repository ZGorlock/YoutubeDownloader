/*
 * File:    Utils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import commons.access.Internet;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
import youtube.config.Configurator;

/**
 * Provides utility methods for the Youtube Downloader.
 */
public final class Utils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    
    //Set logback configuration file
    static {
        System.setProperty("logback.configurationFile", new File(commons.access.Project.RESOURCES_DIR, "logback.xml").getAbsolutePath());
    }
    
    
    //Enums
    
    /**
     * An enumeration of Youtube Downloader projects.
     */
    public enum Project {
        
        //Values
        
        YOUTUBE_CHANNEL_DOWNLOADER,
        YOUTUBE_DOWNLOADER;
        
        
        //Methods
        
        /**
         * Returns the title of the Project.
         *
         * @return The title of the Project.
         */
        public String getTitle() {
            return formatUnderscoredString(name()).replace(" ", "");
        }
        
    }
    
    
    //Constants
    
    /**
     * A list of possible video formats.
     */
    public static final List<String> VIDEO_FORMATS_OPTIONS = List.of("3gp", "flv", "mp4", "webm");
    
    /**
     * A list of possible audio formats.
     */
    public static final List<String> AUDIO_FORMATS_OPTIONS = List.of("aac", "m4a", "mp3", "ogg", "wav");
    
    /**
     * The default video file format.
     */
    public static final String DEFAULT_VIDEO_FORMAT = "mp4";
    
    /**
     * The default audio file format.
     */
    public static final String DEFAULT_AUDIO_FORMAT = "mp3";
    
    /**
     * The default playlist file format.
     */
    public static final String DEFAULT_PLAYLIST_FORMAT = "m3u";
    
    /**
     * The configuration file format.
     */
    public static final String CONFIG_FILE_FORMAT = "json";
    
    /**
     * The configuration file format.
     */
    public static final String DATA_FILE_FORMAT = "json";
    
    /**
     * The list file format.
     */
    public static final String LIST_FILE_FORMAT = "txt";
    
    /**
     * The log file format.
     */
    public static final String LOG_FILE_FORMAT = "log";
    
    /**
     * The executable file format.
     */
    public static final String EXECUTABLE_FILE_FORMAT = "exe";
    
    /**
     * The download file format.
     */
    public static final String DOWNLOAD_FILE_FORMAT = "part";
    
    /**
     * The backup file format.
     */
    public static final String BACKUP_FILE_FORMAT = "bak";
    
    /**
     * The character used in a video title in place of non-ascii characters.
     */
    public static final String TITLE_NON_ASCII_CHAR = "+";
    
    /**
     * The date format used for timestamps.
     */
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * The newline string.
     */
    public static final String NEWLINE = "";
    
    /**
     * The indentation string.
     */
    public static final String INDENT = StringUtility.spaces(5);
    
    
    //Static Methods
    
    /**
     * Performs startup operations.
     *
     * @param project The current active project.
     * @return Whether startup was successful or not.
     */
    public static boolean startup(Project project) {
        Configurator.loadSettings(project);
        
        if (!Internet.isOnline()) {
            System.out.println(NEWLINE);
            System.out.println(Color.bad("Internet access is required"));
            return false;
        }
        
        return ExecutableUtils.checkExe();
    }
    
    /**
     * Attempts to find a video file.
     *
     * @param output The output file for the video.
     * @return The uniquely found file; or null if the file could not be found, or if multiple files were found.
     */
    public static File findVideoFile(File output) {
        return Optional.ofNullable(output)
                .map(File::getParentFile).filter(File::exists)
                .map(FileUtils::getCanonicalFiles)
                .map(files -> files.stream()
                        .filter(e -> FileUtils.getFileTitleKey(e.getName()).equals(FileUtils.getFileTitleKey(output.getName())))
                        .filter(File::exists).filter(e -> (e.length() > 0))
                        .filter(e -> FileUtils.getFileFormat(e.getName()).equals(FileUtils.getFileFormat(output.getName())) ||
                                (isVideoFormat(e.getName()) && isVideoFormat(output.getName())) ||
                                (isAudioFormat(e.getName()) && isAudioFormat(output.getName()))
                        ).collect(Collectors.toList()))
                .filter(e -> (e.size() == 1)).map(e -> e.get(0))
                .orElse(null);
    }
    
    /**
     * Determines if the file format from a file name is an audio format.
     *
     * @param fileName The file name.
     * @return Whether the file format from the file name is an audio format.
     */
    public static boolean isAudioFormat(String fileName) {
        return ListUtility.containsIgnoreCase(AUDIO_FORMATS_OPTIONS, FileUtils.getFileFormat(fileName));
    }
    
    /**
     * Determines if the file format from a file name is an video format.
     *
     * @param fileName The file name.
     * @return Whether the file format from the file name is an video format.
     */
    public static boolean isVideoFormat(String fileName) {
        return ListUtility.containsIgnoreCase(VIDEO_FORMATS_OPTIONS, FileUtils.getFileFormat(fileName));
    }
    
    /**
     * Cleans the title of a Youtube video.
     *
     * @param title The title.
     * @return The cleaned title.
     */
    public static String cleanVideoTitle(String title) {
        return Normalizer.normalize(Optional.ofNullable(title).orElse(""), Normalizer.Form.NFC)
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "")
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS_SUPPLEMENT}+", "")
                .strip()
                
                .replaceAll("(?i)&amp;", "&")
                .replaceAll("(?i)&quot;", "\"")
                .replaceAll("(?i)&(?:nbsp|#(?:32|160));", " ")
                .strip()
                
                .replace("×", "x")
                .replace("÷", "%")
                .replace("‰", "%")
                .replaceAll("[⋯…]", "...")
                .replace("ˆ", "^")
                .replaceAll("[›»]", ">")
                .replaceAll("[‹«]", "<")
                .replaceAll("[•·]", "*")
                .strip()
                
                .replaceAll("[‚„¸]", ",")
                .replaceAll("[`´‘’]", "'")
                .replaceAll("[“”]", "\"")
                .replaceAll("[¦︱︲]", "|")
                .replaceAll("[᐀゠⸗]", "=")
                .replaceAll("[¬¨－﹣﹘⸻⸺¯−₋⁻―—–‒‑‐᠆־]", "-")
                .replaceAll("[⁓֊〜〰]", "~")
                .replaceAll("[™©®†‡§¶]", "")
                .strip()
                
                .replaceAll("[\r\n\t ]+", " ")
                .replaceAll("\\p{Cntrl}&&[^\r\n\t]", "")
                .replaceAll("[^\\x00-\\xFF]", TITLE_NON_ASCII_CHAR)
                .strip()
                
                .replaceAll("(\\d{1,2}):(\\d{2}):(\\d{2})", "$1-$2-$3")
                .replaceAll("(\\d{1,2}):(\\d{2})", "$1-$2")
                .replaceAll("(\\d{1,2})/(\\d{1,2})/(\\d{2}\\d{2}?)", "$1-$2-$3")
                .replaceAll("(\\d{1,2}\\d{2}?)/(\\d{1,2})/(\\d{1,2})", "$2-$3-$1")
                .replaceAll("(\\d{1,2})/(\\d{1,2})", "$1-$2")
                .strip()
                
                .replaceAll("[:;|/\\\\]", " - ")
                .replaceAll("[?<>*]", "")
                .replace("\"", "'")
                .strip()
                
                .replaceAll("\\++", "+")
                .replaceAll("-+", "-")
                .replace("+-", "+ -")
                .replaceAll("(?:-\\s+)+", "- ")
                .replaceAll("(?:\\+\\s+)+", "+ ")
                .replaceAll("(?:\\s+-\\s+)+", " - ")
                .replaceAll("^\\s*(?:[\\-+\"]+\\s+)+|(?:\\s+[\\-+\"]+)+\\s*$", "")
                .strip()
                
                .replaceAll("!(?:\\s*!)+|(?:\\s*!)+$", "!")
                .replaceAll("\\?(?:\\s*\\?)+|(?:\\s*\\?)+$", "?")
                .replaceAll("\\$(?:\\s*\\$)+", Matcher.quoteReplacement("$"))
                .replaceAll("\\s+", " ")
                .strip();
    }
    
    /**
     * Formats an underscored string.
     *
     * @param string The underscored string.
     * @return The formatted string.
     */
    public static String formatUnderscoredString(String string) {
        return StringUtility.toTitleCase(string.toLowerCase().replace("_", " "));
    }
    
    /**
     * Returns a string representing the current timestamp.
     *
     * @return A string representing the current timestamp.
     */
    public static String currentTimestamp() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    }
    
}
