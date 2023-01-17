/*
 * File:    FileUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.lambda.function.unchecked.UncheckedFunction;

/**
 * Provides file utility methods for the Youtube Downloader.
 */
public final class FileUtils {
    
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
     * @see org.apache.commons.io.FileUtils#readFileToString(File, Charset)
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
     * @see org.apache.commons.io.FileUtils#readLines(File, Charset)
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
     * @see org.apache.commons.io.FileUtils#writeStringToFile(File, String, String, boolean)
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
     * @see org.apache.commons.io.FileUtils#writeStringToFile(File, String, String)
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
     * @see org.apache.commons.io.FileUtils#writeLines(File, String, Collection, boolean)
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
     * @see org.apache.commons.io.FileUtils#writeLines(File, String, Collection)
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
     * @throws IOException When there is an error listing the directory.
     */
    public static List<File> getFiles(File dir, boolean recursive) throws IOException {
        return new ArrayList<>(org.apache.commons.io.FileUtils.listFiles(dir, null, recursive));
    }
    
    /**
     * Lists the files and directories in a directory.
     *
     * @param dir The directory.
     * @return The list of files and directories in the directory.
     * @throws IOException When there is an error listing the directory.
     */
    public static List<File> getFiles(File dir) throws IOException {
        return getFiles(dir, false);
    }
    
    /**
     * Lists the canonical files and directories in a directory.
     *
     * @param dir       The directory.
     * @param recursive Whether to list files recursively or not.
     * @return The list of canonical files and directories in the directory.
     * @throws IOException When there is an error listing the directory.
     */
    public static List<File> getCanonicalFiles(File dir, boolean recursive) throws IOException {
        return getFiles(dir, recursive).stream()
                .map((UncheckedFunction<File, File>) File::getCanonicalFile)
                .collect(Collectors.toList());
    }
    
    /**
     * Lists the canonical files and directories in a directory.
     *
     * @param dir The directory.
     * @return The list of canonical files and directories in the directory.
     * @throws IOException When there is an error listing the directory.
     */
    public static List<File> getCanonicalFiles(File dir) throws IOException {
        return getCanonicalFiles(dir, false);
    }
    
    /**
     * Copies a file.
     *
     * @param source The source file.
     * @param dest   The destination file.
     * @throws IOException When there is an error copying the file.
     * @see org.apache.commons.io.FileUtils#copyFile(File, File)
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
     * @see org.apache.commons.io.FileUtils#moveFile(File, File)
     */
    public static void moveFile(File source, File dest) throws IOException {
        org.apache.commons.io.FileUtils.moveFile(source, dest);
    }
    
    /**
     * Deletes a file.
     *
     * @param file The file to delete.
     * @throws IOException When there is an error deleting the file.
     * @see org.apache.commons.io.FileUtils#forceDelete(File)
     */
    public static void deleteFile(File file) throws IOException {
        org.apache.commons.io.FileUtils.forceDelete(file);
    }
    
    /**
     * Deletes a file when the program ends.
     *
     * @param file The file to delete.
     * @throws IOException When there is an error deleting the file.
     * @see org.apache.commons.io.FileUtils#forceDeleteOnExit(File)
     */
    public static void deleteFileOnExit(File file) throws IOException {
        org.apache.commons.io.FileUtils.forceDeleteOnExit(file);
    }
    
    /**
     * Downloads a file from a url.
     *
     * @param url  The url to download from.
     * @param file The file to download to.
     * @throws IOException When there is an error downloading the file.
     * @see org.apache.commons.io.FileUtils#copyURLToFile(URL, File, int, int)
     */
    public static void downloadFile(String url, File file) throws IOException {
        org.apache.commons.io.FileUtils.copyURLToFile(new URL(url), file, 200, Integer.MAX_VALUE);
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
        return fileName.replaceAll(("\\." + Pattern.quote(getFileFormat(fileName)) + "$"), "");
    }
    
}
