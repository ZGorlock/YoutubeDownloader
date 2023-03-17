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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import commons.lambda.function.checked.CheckedFunction;
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
     * Returns the canonical version of a file.
     *
     * @param file The file.
     * @return The canonical version of the file.
     */
    public static File getCanonicalFile(File file) {
        return Optional.ofNullable(file)
                .map((CheckedFunction<File, File>) File::getCanonicalFile)
                .orElse(file);
    }
    
    /**
     * Returns the file format from a file name.
     *
     * @param fileName The file name.
     * @return The file format from the file name.
     */
    public static String getFileFormat(String fileName) {
        return fileName.replaceAll("^.*?\\.((?:f\\d+\\.)*[^.]+)$", "$1").toLowerCase();
    }
    
    /**
     * Returns the title from a file name.
     *
     * @param fileName The file name.
     * @return The title from the file name.
     */
    public static String getFileTitle(String fileName) {
        return fileName.replaceAll((Pattern.quote('.' + getFileFormat(fileName)) + "$"), "");
    }
    
    /**
     * Returns the reduced title from a file name.
     *
     * @param fileName The file name.
     * @return The reduced title from the file name.
     */
    public static String getFileTitleKey(String fileName) {
        return getFileTitle(fileName).toUpperCase()
                .replaceAll("[^A-Z\\d" + Pattern.quote(Utils.TITLE_NON_ASCII_CHAR) + "]", "");
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
