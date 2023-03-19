/*
 * File:    FileUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.lambda.function.checked.CheckedFunction;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
import youtube.config.Configurator;

/**
 * Provides file utility methods for the Youtube Downloader.
 */
public final class FileUtils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
    
    
    //Constants
    
    /**
     * The charset to use when reading and writing files.
     */
    public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;
    
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
     * The xml file format.
     */
    public static final String XML_FILE_FORMAT = "xml";
    
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
    
    
    //Static Fields
    
    /**
     * A flag indicating whether the filesystem configuration has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Static Methods
    
    /**
     * Initializes the file system.
     */
    public static void initFilesystem() {
        if (loaded.compareAndSet(false, true)) {
            logger.debug(Color.log("Initializing Filesystem..."));
            
            Config.init();
        }
    }
    
    /**
     * Attempts to find a video file.
     *
     * @param output The output file for the video.
     * @return The uniquely found file; or null if the file could not be found, or if multiple files were found.
     */
    public static File findVideoFile(File output) {
        return Optional.ofNullable(output)
                .filter(File::exists).map(File::getParentFile)
                .filter(File::exists).map(Filesystem::getFiles)
                .map(files -> files.stream().map(FileUtils::getCanonical)
                        .filter(e -> getTitleKey(e.getName()).equals(getTitleKey(output.getName())))
                        .filter(File::exists).filter(e -> !Filesystem.isEmpty(e))
                        .filter(e -> getFormat(e.getName()).equals(getFormat(output.getName())) ||
                                (isVideoFormat(e.getName()) && isVideoFormat(output.getName())) ||
                                (isAudioFormat(e.getName()) && isAudioFormat(output.getName()))
                        ).collect(Collectors.toList()))
                .filter(e -> (e.size() == 1)).map(e -> e.get(0))
                .orElse(null);
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
                
                .replaceAll(Stream.of(VIDEO_FORMATS_OPTIONS, AUDIO_FORMATS_OPTIONS).flatMap(Collection::stream)
                        .collect(Collectors.joining("|", "(?i)\\.(?:", ")$")), "")
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
     * Returns the canonical version of a file.
     *
     * @param file The file.
     * @return The canonical version of the file.
     */
    public static File getCanonical(File file) {
        return Optional.ofNullable(file)
                .map((CheckedFunction<File, File>) File::getCanonicalFile)
                .orElse(file);
    }
    
    /**
     * Determines if the file format from a file name is an audio format.
     *
     * @param fileName The file name.
     * @return Whether the file format from the file name is an audio format.
     */
    public static boolean isAudioFormat(String fileName) {
        return ListUtility.containsIgnoreCase(AUDIO_FORMATS_OPTIONS, getFormat(fileName));
    }
    
    /**
     * Determines if the file format from a file name is an video format.
     *
     * @param fileName The file name.
     * @return Whether the file format from the file name is an video format.
     */
    public static boolean isVideoFormat(String fileName) {
        return ListUtility.containsIgnoreCase(VIDEO_FORMATS_OPTIONS, getFormat(fileName));
    }
    
    /**
     * Determines if the file format from a file name is a specific format.
     *
     * @param fileName   The file name.
     * @param fileFormat The file format.
     * @return Whether the file format from the file name is the specified format.
     */
    public static boolean isFormat(String fileName, String fileFormat) {
        return getFormat(fileName).equalsIgnoreCase(fileFormat.replaceAll("^\\.+", ""));
    }
    
    /**
     * Adds the file format to a file name.
     *
     * @param fileName   The file name.
     * @param fileFormat The file format.
     * @return The formatted file name.
     */
    public static String setFormat(String fileName, String fileFormat) {
        return Optional.ofNullable(fileFormat)
                .map(format -> format.replaceFirst("^\\.*", "."))
                .filter(format -> format.matches("^\\..*[^.]$"))
                .flatMap(format -> Optional.ofNullable(fileName)
                        .filter(name -> !StringUtility.isNullOrBlank(name))
                        .map(name -> name.replaceFirst(("(?:" + Pattern.quote(format) + ")*$"), format)))
                .orElseGet(() -> getTitle(fileName));
    }
    
    /**
     * Returns the file format from a file name.
     *
     * @param fileName   The file name.
     * @param includeDot Whether to include the dot at the beginning of the file format.
     * @return The file format from the file name.
     */
    public static String getFormat(String fileName, boolean includeDot) {
        return Optional.ofNullable(fileName)
                .filter(name -> !StringUtility.isNullOrBlank(name))
                .map(name -> name.replaceAll("(?i)^.*?(\\.(?:f\\d+\\.)*\\w+)$", "$1"))
                .filter(format -> format.matches("^\\..*[^.]$"))
                .map(format -> format.replaceFirst("^\\.*", (includeDot ? "." : "")))
                .map(String::toLowerCase)
                .orElse("");
    }
    
    /**
     * Returns the file format from a file name.
     *
     * @param fileName The file name.
     * @return The file format from the file name.
     */
    public static String getFormat(String fileName) {
        return getFormat(fileName, false);
    }
    
    /**
     * Returns the title from a file name.
     *
     * @param fileName The file name.
     * @return The title from the file name.
     */
    public static String getTitle(String fileName) {
        return Optional.ofNullable(fileName)
                .filter(name -> !StringUtility.isNullOrBlank(name))
                .map(e -> e.replaceFirst(("(?i)" + Pattern.quote(getFormat(e, true)) + "$"), ""))
                .orElse("");
    }
    
    /**
     * Returns the reduced title from a file name.
     *
     * @param fileName The file name.
     * @return The reduced title from the file name.
     */
    public static String getTitleKey(String fileName) {
        return Optional.ofNullable(fileName).map(FileUtils::getTitle)
                .map(title -> title.replaceAll("(?i)[^\\w" + Pattern.quote(TITLE_NON_ASCII_CHAR) + "]+", ""))
                .map(String::toUpperCase)
                .orElse(null);
    }
    
    
    //Inner Classes
    
    /**
     * Holds the filesystem Config.
     */
    public static class Config {
        
        //Constants
        
        /**
         * The default drive to use for storage of downloaded files.
         */
        public static final String DEFAULT_STORAGE_DRIVE = PathUtils.getUserDrivePath();
        
        /**
         * The default Music directory in the storage drive.
         */
        public static final String DEFAULT_MUSIC_DIR = "Music/";
        
        /**
         * The default Videos directory in the storage drive.
         */
        public static final String DEFAULT_VIDEOS_DIR = "Videos/";
        
        /**
         * The default output directory to store downloaded files in.
         */
        public static final String DEFAULT_OUTPUT_DIR = PathUtils.path(PathUtils.getUserHomePath(), "Youtube");
        
        
        //Static Fields
        
        /**
         * The drive to use for storage of downloaded files.
         */
        public static File storageDrive = new File(DEFAULT_STORAGE_DRIVE);
        
        /**
         * The Music directory in the storage drive.
         */
        public static File musicDir = new File(storageDrive, DEFAULT_MUSIC_DIR);
        
        /**
         * The Videos directory in the storage drive.
         */
        public static File videoDir = new File(storageDrive, DEFAULT_VIDEOS_DIR);
        
        /**
         * The output directory to store downloaded files in.
         */
        public static File outputDir = new File(DEFAULT_OUTPUT_DIR);
        
        
        //Static Methods
        
        /**
         * Initializes the Config.
         */
        private static void init() {
            storageDrive = new File(Configurator.getSetting(List.of(
                            "storageDrive",
                            "location.storageDrive"),
                    DEFAULT_STORAGE_DRIVE));
            
            musicDir = new File(storageDrive, Configurator.getSetting(List.of(
                            "musicDir",
                            "location.musicDir"),
                    DEFAULT_MUSIC_DIR));
            videoDir = new File(storageDrive, Configurator.getSetting(List.of(
                            "videoDir",
                            "location.videoDir"),
                    DEFAULT_VIDEOS_DIR));
            
            outputDir = new File(Configurator.getSetting(List.of(
                            "outputDir",
                            "location.outputDir",
                            "location.output"),
                    DEFAULT_OUTPUT_DIR));
        }
        
    }
    
}
