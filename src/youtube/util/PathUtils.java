/*
 * File:    PathUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.OperatingSystem;

/**
 * Provides path utility methods for the Youtube Downloader.
 */
public final class PathUtils {
    
    //Constants
    
    /**
     * The working directory.
     */
    public static final File WORKING_DIR = new File("").getAbsoluteFile();
    
    /**
     * The data directory.
     */
    public static final File DATA_DIR = new File("data");
    
    /**
     * The resources directory.
     */
    public static final File RESOURCES_DIR = new File("resources");
    
    /**
     * The temporary directory.
     */
    public static final File TMP_DIR = new File("tmp");
    
    /**
     * The Unix path separator.
     */
    public static final String SEPARATOR = "/";
    
    /**
     * The Windows path separator.
     */
    public static final String WINDOWS_SEPARATOR = "\\";
    
    /**
     * The local path separator.
     */
    public static final String LOCAL_SEPARATOR = OperatingSystem.isWindows() ? WINDOWS_SEPARATOR : SEPARATOR;
    
    /**
     * A regex pattern that matches a path separator.
     */
    public static final String SEPARATOR_PATTERN = "[" + Pattern.quote(WINDOWS_SEPARATOR) + Pattern.quote(SEPARATOR) + "]";
    
    
    //Static Methods
    
    /**
     * Builds a file path.
     *
     * @param pathSeparator The desired path separator.
     * @param endingSlash   Whether to include a path separator at the end of the path or not.
     * @param parts         The parts of the file path.
     * @return The file path.
     */
    public static String buildPath(String pathSeparator, boolean endingSlash, String... parts) {
        return (String.join(pathSeparator, parts) + (endingSlash ? pathSeparator : ""))
                .replaceAll((SEPARATOR_PATTERN + "+"), Matcher.quoteReplacement(pathSeparator));
    }
    
    /**
     * Builds a file path.
     *
     * @param pathSeparator The desired path separator.
     * @param parts         The parts of the file path.
     * @return The file path.
     * @see #buildPath(String, boolean, String...)
     */
    public static String buildPath(String pathSeparator, String... parts) {
        return buildPath(pathSeparator, false, parts);
    }
    
    /**
     * Builds a file path.
     *
     * @param pathSeparator The desired path separator.
     * @param endingSlash   Whether to include a path separator at the end of the path or not.
     * @param file          The file.
     * @return The file path.
     * @see #buildPath(String, boolean, String...)
     */
    public static String buildPath(String pathSeparator, boolean endingSlash, File file) {
        return buildPath(pathSeparator, endingSlash, file.getAbsolutePath());
    }
    
    /**
     * Builds a file path.
     *
     * @param pathSeparator The desired path separator.
     * @param file          The file.
     * @return The file path.
     * @see #buildPath(String, boolean, File)
     */
    public static String buildPath(String pathSeparator, File file) {
        return buildPath(pathSeparator, false, file);
    }
    
    /**
     * Builds a file path.
     *
     * @param endingSlash Whether to include a path separator at the end of the path or not.
     * @param parts       The parts of the file path.
     * @return The file path.
     * @see #buildPath(String, boolean, String...)
     */
    public static String path(boolean endingSlash, String... parts) {
        return buildPath(SEPARATOR, endingSlash, parts);
    }
    
    /**
     * Builds a file path.
     *
     * @param parts The parts of the file path.
     * @return The file path.
     * @see #path(boolean, String...)
     */
    public static String path(String... parts) {
        return path(false, parts);
    }
    
    /**
     * Builds a file path.
     *
     * @param endingSlash Whether to include a path separator at the end of the path or not.
     * @param file        The file.
     * @return The file path.
     * @see #path(boolean, String...)
     */
    public static String path(boolean endingSlash, File file) {
        return path(endingSlash, file.getAbsolutePath());
    }
    
    /**
     * Builds a file path.
     *
     * @param file The file.
     * @return The file path.
     * @see #path(boolean, File)
     */
    public static String path(File file) {
        return path(false, file);
    }
    
    /**
     * Builds a Windows file path.
     *
     * @param endingSlash Whether to include a path separator at the end of the path or not.
     * @param parts       The parts of the file path.
     * @return The file path.
     * @see #buildPath(String, boolean, String...)
     */
    public static String windowsPath(boolean endingSlash, String... parts) {
        return buildPath(WINDOWS_SEPARATOR, endingSlash, parts);
    }
    
    /**
     * Builds a Windows file path.
     *
     * @param parts The parts of the file path.
     * @return The file path.
     * @see #windowsPath(boolean, String...)
     */
    public static String windowsPath(String... parts) {
        return windowsPath(false, parts);
    }
    
    /**
     * Builds a Windows file path.
     *
     * @param endingSlash Whether to include a path separator at the end of the path or not.
     * @param file        The file.
     * @return The file path.
     * @see #windowsPath(boolean, String...)
     */
    public static String windowsPath(boolean endingSlash, File file) {
        return windowsPath(endingSlash, file.getAbsolutePath());
    }
    
    /**
     * Builds a Windows file path.
     *
     * @param file The file.
     * @return The file path.
     * @see #windowsPath(boolean, File)
     */
    public static String windowsPath(File file) {
        return windowsPath(false, file);
    }
    
    /**
     * Builds a local file path.
     *
     * @param endingSlash Whether to include a path separator at the end of the path or not.
     * @param parts       The parts of the file path.
     * @return The file path.
     * @see #buildPath(String, boolean, String...)
     */
    public static String localPath(boolean endingSlash, String... parts) {
        return buildPath(LOCAL_SEPARATOR, endingSlash, parts);
    }
    
    /**
     * Builds a local file path.
     *
     * @param parts The parts of the file path.
     * @return The file path.
     * @see #localPath(boolean, String...)
     */
    public static String localPath(String... parts) {
        return localPath(false, parts);
    }
    
    /**
     * Builds a local file path.
     *
     * @param endingSlash Whether to include a path separator at the end of the path or not.
     * @param file        The file.
     * @return The file path.
     * @see #localPath(boolean, String...)
     */
    public static String localPath(boolean endingSlash, File file) {
        return localPath(endingSlash, file.getAbsolutePath());
    }
    
    /**
     * Builds a local file path.
     *
     * @param file The file.
     * @return The file path.
     * @see #localPath(boolean, File)
     */
    public static String localPath(File file) {
        return localPath(false, file);
    }
    
    /**
     * Determines whether or not a path contains any path separators.
     *
     * @param path The path.
     * @return Whether or not a path contains any path separators.
     */
    public static boolean containsPathSeparator(String path) {
        return path.matches("^.*" + SEPARATOR_PATTERN + ".*$");
    }
    
    /**
     * Returns the path to the user home directory.
     *
     * @return The path to the user home directory.
     */
    public static String getUserHomePath() {
        return path(org.apache.commons.io.FileUtils.getUserDirectoryPath());
    }
    
    /**
     * Returns the user home directory.
     *
     * @return The user home directory.
     * @see #getUserHomePath()
     */
    public static File getUserHome() {
        return new File(getUserHomePath());
    }
    
    /**
     * Returns the path to the user drive.
     *
     * @return The path to the user drive.
     */
    public static String getUserDrivePath() {
        return getUserHomePath().replaceAll((SEPARATOR_PATTERN + ".+$"), Matcher.quoteReplacement(SEPARATOR));
    }
    
    /**
     * Returns the user drive.
     *
     * @return The user drive.
     * @see #getUserDrivePath()
     */
    public static File getUserDrive() {
        return new File(getUserDrivePath());
    }
    
    /**
     * Returns the path to the temporary directory.
     *
     * @return The path to the temporary directory.
     */
    public static String getTempDirPath() {
        return path(org.apache.commons.io.FileUtils.getTempDirectoryPath());
    }
    
    /**
     * Returns the temporary directory.
     *
     * @return The temporary directory.
     * @see #getTempDirPath()
     */
    public static File getTempDir() {
        return new File(getTempDirPath());
    }
    
}
