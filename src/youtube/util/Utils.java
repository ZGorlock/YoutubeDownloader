/*
 * File:    Utils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import commons.object.string.StringUtility;

/**
 * Provides utility methods for the Youtube Downloader.
 */
public final class Utils {
    
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
     * The temporary directory.
     */
    public static final File TMP_DIR = new File("tmp");
    
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
     * Performs startup operations.
     *
     * @param project The current active project.
     * @return Whether startup was successful or not.
     */
    public static boolean startup(Project project) {
        if (!WebUtils.isOnline()) {
            System.out.println(NEWLINE);
            System.out.println(Color.bad("Internet access is required"));
            return false;
        }
        
        Configurator.loadSettings(project);
        
        return ExecutableUtils.checkExe();
    }
    
    /**
     * Tries to find a video.
     *
     * @param output The output file for the video.
     * @return The found file or files.
     */
    public static File findVideoFile(File output) {
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
                String format = getFileFormat(output.getName());
                String existingFormat = getFileFormat(existingFile.getName());
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
    public static String cleanVideoTitle(String title) {
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
    public static String getFileFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
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
    
}
