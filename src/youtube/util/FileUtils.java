/*
 * File:    FileUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.access.Desktop;
import commons.access.Internet;
import commons.lambda.function.checked.CheckedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    
    //Static Methods
    
    /**
     * Reads a string from file.
     *
     * @param file The file to read from.
     * @return The string read from the file.
     * @throws IOException When there is an error reading the file.
     */
    public static String readFileToString(File file) throws IOException {
        return ((file == null) || !file.exists()) ? "" :
               org.apache.commons.io.FileUtils.readFileToString(file, FILE_CHARSET);
    }
    
    /**
     * Reads a list of lines from a file.
     *
     * @param file The file to read from.
     * @return The list of lines read from the file.
     * @throws IOException When there is an error reading the file.
     */
    public static List<String> readLines(File file) throws IOException {
        return ((file == null) || !file.exists()) ? new ArrayList<>() :
               org.apache.commons.io.FileUtils.readLines(file, FILE_CHARSET);
    }
    
    /**
     * Writes a string to a file.
     *
     * @param file   The file to write to.
     * @param data   The string to write.
     * @param append Whether to append to the file or not.
     * @throws IOException When there is an error writing the file.
     */
    public static void writeStringToFile(File file, String data, boolean append) throws IOException {
        org.apache.commons.io.FileUtils.writeStringToFile(file, data, FILE_CHARSET, append);
    }
    
    /**
     * Writes a string to a file.
     *
     * @param file The file to write to.
     * @param data The string to write.
     * @throws IOException When there is an error writing the file.
     */
    public static void writeStringToFile(File file, String data) throws IOException {
        org.apache.commons.io.FileUtils.writeStringToFile(file, data, FILE_CHARSET);
    }
    
    /**
     * Writes a list of lines to a file.
     *
     * @param file   The file to write to.
     * @param lines  The list of lines to write.
     * @param append Whether to append to the file or not.
     * @throws IOException When there is an error writing the file.
     */
    public static void writeLines(File file, List<String> lines, boolean append) throws IOException {
        org.apache.commons.io.FileUtils.writeLines(file, FILE_CHARSET.name(), lines, append);
    }
    
    /**
     * Writes a list of lines to a file.
     *
     * @param file  The file to write to.
     * @param lines The list of lines to write.
     * @throws IOException When there is an error writing the file.
     */
    public static void writeLines(File file, List<String> lines) throws IOException {
        org.apache.commons.io.FileUtils.writeLines(file, FILE_CHARSET.name(), lines);
    }
    
    /**
     * Lists the files and directories in a directory.
     *
     * @param dir       The directory.
     * @param recursive Whether to list files recursively or not.
     * @return The list of files and directories in the directory.
     */
    public static List<File> getFiles(File dir, boolean recursive) {
        return ((dir == null) || !dir.exists() || !dir.isDirectory()) ? new ArrayList<>() :
               new ArrayList<>(org.apache.commons.io.FileUtils.listFiles(dir, null, recursive));
    }
    
    /**
     * Lists the files and directories in a directory.
     *
     * @param dir The directory.
     * @return The list of files and directories in the directory.
     */
    public static List<File> getFiles(File dir) {
        return getFiles(dir, false);
    }
    
    /**
     * Lists the canonical files and directories in a directory.
     *
     * @param dir       The directory.
     * @param recursive Whether to list files recursively or not.
     * @return The list of canonical files and directories in the directory.
     */
    public static List<File> getCanonicalFiles(File dir, boolean recursive) {
        return getFiles(dir, recursive).stream()
                .map(FileUtils::getCanonicalFile)
                .collect(Collectors.toList());
    }
    
    /**
     * Lists the canonical files and directories in a directory.
     *
     * @param dir The directory.
     * @return The list of canonical files and directories in the directory.
     */
    public static List<File> getCanonicalFiles(File dir) {
        return getCanonicalFiles(dir, false);
    }
    
    /**
     * Copies a file.
     *
     * @param source The source file.
     * @param dest   The destination file.
     * @throws IOException When there is an error copying the file.
     */
    public static void copyFile(File source, File dest) throws IOException {
        org.apache.commons.io.FileUtils.copyFile(source, dest, true);
    }
    
    /**
     * Moves a file.
     *
     * @param source The source file.
     * @param dest   The destination file.
     * @throws IOException When there is an error moving the file.
     */
    public static void moveFile(File source, File dest) throws IOException {
        org.apache.commons.io.FileUtils.moveFile(source, dest);
    }
    
    /**
     * Deletes a file.
     *
     * @param file The file to delete.
     * @throws IOException When there is an error deleting the file.
     */
    public static void deleteFile(File file) throws IOException {
        org.apache.commons.io.FileUtils.forceDelete(file);
    }
    
    /**
     * Deletes a file when the program ends.
     *
     * @param file The file to delete.
     * @throws IOException When there is an error deleting the file.
     */
    public static void deleteFileOnExit(File file) throws IOException {
        org.apache.commons.io.FileUtils.forceDeleteOnExit(file);
    }
    
    /**
     * Recycles a file.
     *
     * @param file The file to recycle.
     * @param safe A flag indicating whether to fail if recycling is not available, instead of deleting the file instead.
     * @throws IOException When there is an error recycling the file.
     */
    public static void recycleFile(File file, boolean safe) throws IOException {
        if (!Desktop.trash(file)) {
            if (safe) {
                throw new IOException("Unable to recycle file");
            } else {
                deleteFile(file);
            }
        }
    }
    
    /**
     * Recycles a file.
     *
     * @param file The file to recycle.
     * @throws IOException When there is an error recycling the file.
     */
    public static void recycleFile(File file) throws IOException {
        recycleFile(file, true);
    }
    
    /**
     * Downloads a file from a url.
     *
     * @param url  The url to download from.
     * @param file The file to download to.
     * @throws IOException When there is an error downloading the file.
     */
    public static void downloadFile(String url, File file) throws IOException {
        if (Internet.downloadFile(url, file) == null) {
            throw new IOException("Unable to download file");
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
    
}
