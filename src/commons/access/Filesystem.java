/*
 * File:    Filesystem.java
 * Package: commons.access
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.access;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Level;
import commons.object.string.StringUtility;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides access to the filesystem.
 */
public final class Filesystem {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Filesystem.class);
    
    
    //Constants
    
    /**
     * A regex pattern for a Windows file name that starts with a drive letter.
     */
    public static final Pattern WINDOWS_DRIVE_FILE_NAME_PATTERN = Pattern.compile("^[A-Z]:.*");
    
    
    //Static Fields
    
    /**
     * The list holding the temporary files and folders created during this session.
     */
    private static final List<File> tmpFiles = new ArrayList<>();
    
    //Delete the temporary files and folders created during this session
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                tmpFiles.stream().filter(File::exists).forEach(Filesystem::delete)));
    }
    
    
    //Static Methods
    
    /**
     * Attempts to create a the specified file.
     *
     * @param file The file to create.
     * @return Whether the operation was successful or not.<br>
     * Will return true if the file already exists.
     */
    public static boolean createFile(File file) {
        Log.trace("Creating file: {}", file);
        
        if (file.exists()) {
            Log.trace("File already exists: {}", file);
            return true;
        }
        
        //make missing directories before creating file
        File parent = file.getParentFile();
        if ((parent != null) && !parent.exists() && !createDirectory(parent)) {
            Log.warn("Could not create destination directory: {}", parent);
            return false;
        }
        
        try {
            if (file.createNewFile() || FileUtils.waitFor(file, 1)) {
                Log.info("Created file: {}", file);
                return true;
            }
        } catch (IOException ignored) {
        }
        
        Log.warn("Unable to create file: {}", file);
        return false;
    }
    
    /**
     * Attempts to create the specified directory.
     *
     * @param dir The directory to create.
     * @return Whether the operation was successful or not.<br>
     * Will return true if the directory already exists.
     */
    public static boolean createDirectory(File dir) {
        Log.trace("Creating directory: {}", dir);
        
        if (dir.exists()) {
            Log.trace("Directory already exists: {}", dir);
            return true;
        }
        
        if (dir.mkdirs() || FileUtils.waitFor(dir, 1)) {
            Log.info("Created directory: {}", dir);
            return true;
        }
        
        Log.warn("Unable to create directory: {}", dir);
        return false;
    }
    
    /**
     * Attempts to delete the specified file.
     *
     * @param file The file to delete.
     * @return Whether the operation was successful or not.
     */
    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            return deleteDirectory(file);
        }
        Log.trace("Deleting file: {}", file);
        
        if (!file.exists()) {
            Log.trace("File already deleted: {}", file);
            return true;
        }
        
        if (FileUtils.deleteQuietly(file)) {
            Log.info("Deleted file: {}", file);
            return true;
        }
        
        Log.warn("Unable to delete directory: {}", file);
        return false;
    }
    
    /**
     * Attempts to recursively delete the specified directory.
     *
     * @param dir The directory to delete.
     * @return Whether the operation was successful or not.
     */
    public static boolean deleteDirectory(File dir) {
        if (dir.isFile()) {
            return deleteFile(dir);
        }
        Log.trace("Deleting directory: {}", dir);
        
        if (!dir.exists()) {
            Log.trace("Directory already deleted: {}", dir);
            return true;
        }
        
        try {
            FileUtils.deleteDirectory(dir);
            Log.info("Deleted directory: {}", dir);
            return true;
        } catch (IOException e) {
            Log.error("Unable to delete directory: {}", e, dir);
            return false;
        }
    }
    
    /**
     * Attempts to delete the specified file or directory.
     *
     * @param file The file or directory.
     * @return Whether the operation was successful or not.
     * @see #deleteFile(File)
     * @see #deleteDirectory(File)
     */
    public static boolean delete(File file) {
        return file.isFile() ? deleteFile(file) : deleteDirectory(file);
    }
    
    /**
     * Attempts to rename the specified file.
     *
     * @param fileSrc  The original file.
     * @param fileDest The renamed file.
     * @return Whether the operation was successful or not.
     */
    public static boolean renameFile(File fileSrc, File fileDest) {
        if (fileSrc.isDirectory()) {
            return renameDirectory(fileSrc, fileDest);
        }
        Log.trace("Renaming file: {} to: {}", fileSrc, fileDest);
        
        if (!fileSrc.exists()) {
            Log.warn("Source file does not exist: {}", fileSrc);
            return false;
        }
        if (fileSrc.getAbsolutePath().equals(fileDest.getAbsolutePath())) {
            Log.trace("Source file: {} and destination file: {} are the same", fileSrc, fileDest);
            return true;
        }
        if (fileDest.exists()) {
            Log.warn("Destination file already exists: {}", fileDest);
            return false;
        }
        
        if (fileSrc.renameTo(fileDest)) {
            Log.info("Renamed file: {} to: {}", fileSrc, fileDest);
            return true;
        }
        
        Log.warn("Unable to rename file: {} to: {}", fileSrc, fileDest);
        return false;
    }
    
    /**
     * Attempts to rename the specified directory.
     *
     * @param dirSrc  The original directory.
     * @param dirDest The renamed directory.
     * @return Whether the operation was successful or not.
     */
    public static boolean renameDirectory(File dirSrc, File dirDest) {
        if (dirSrc.isFile()) {
            return renameFile(dirSrc, dirDest);
        }
        Log.trace("Renaming directory: {} to: {}", dirSrc, dirDest);
        
        if (!dirSrc.exists()) {
            Log.warn("Source directory does not exist: {}", dirSrc);
            return false;
        }
        if (dirSrc.getAbsolutePath().equals(dirDest.getAbsolutePath())) {
            Log.trace("Source directory: {} and destination directory: {} are the same", dirSrc, dirDest);
            return true;
        }
        if (dirDest.exists()) {
            Log.warn("Destination directory already exists: {}", dirDest);
            return false;
        }
        
        if (dirSrc.renameTo(dirDest)) {
            Log.info("Renamed directory: {} to: {}", dirSrc, dirDest);
            return true;
        }
        
        Log.warn("Unable to rename directory: {} to: {}", dirSrc, dirDest);
        return false;
    }
    
    /**
     * Attempts to rename the specified file or directory.
     *
     * @param src  The original file or directory.
     * @param dest The renamed file or directory.
     * @return Whether the operation was successful or not.
     * @see #renameFile(File, File)
     * @see #renameDirectory(File, File)
     */
    public static boolean rename(File src, File dest) {
        return (src.isFile() && dest.isFile()) ? renameFile(src, dest) : renameDirectory(src, dest);
    }
    
    /**
     * Attempts to copy the file fileSrc to a file fileDest.<br>
     * If fileDest is a directory, fileSrc will be copied into that directory.<br>
     * If fileDest is a file, fileSrc will be copied to that file path.
     *
     * @param fileSrc   The source file.
     * @param fileDest  The destination file or directory.
     * @param overwrite Whether or not to overwrite the destination file if it exists.
     * @return Whether the operation was successful or not.
     */
    public static boolean copyFile(File fileSrc, File fileDest, boolean overwrite) {
        if (fileSrc.isDirectory()) {
            return copyDirectory(fileSrc, fileDest, overwrite);
        }
        Log.trace("Copying file: {} to: {}", fileSrc, fileDest);
        
        if (!fileSrc.exists()) {
            Log.warn("Source file does not exist: {}", fileSrc);
            return false;
        }
        if (fileSrc.getAbsolutePath().equalsIgnoreCase(fileDest.getAbsolutePath())) {
            Log.trace("Source file: {} and destination file: {} are the same", fileSrc, fileDest);
            return true;
        }
        
        try {
            if (fileDest.exists() && fileDest.isDirectory()) {
                File destFile = new File(fileDest, fileSrc.getName());
                if (destFile.exists()) {
                    if (overwrite) {
                        deleteFile(destFile);
                    } else {
                        Log.warn("Destination file already exists: {}", destFile);
                        return false;
                    }
                }
                FileUtils.copyFileToDirectory(fileSrc, fileDest, true); //copies file into destination directory
                Log.info("Copied file: {} to: {}", fileSrc, destFile);
            } else {
                if (fileDest.exists()) {
                    if (overwrite) {
                        delete(fileDest);
                    } else {
                        Log.warn("Destination file already exists: {}", fileDest);
                        return false;
                    }
                }
                FileUtils.copyFile(fileSrc, fileDest, true); //copies file to destination file path
                Log.info("Copied file: {} to: {}", fileSrc, fileDest);
            }
            return true;
        } catch (IOException e) {
            Log.error("Unable to copy file: {} to: {}", e, fileSrc, fileDest);
            return false;
        }
    }
    
    /**
     * Attempts to copy the file fileSrc to a file fileDest.<br>
     * If fileDest is a directory, fileSrc will be copied into that directory.<br>
     * If fileDest is a file, fileSrc will be copied to that file path.
     *
     * @param fileSrc  The source file.
     * @param fileDest The destination file or directory.
     * @return Whether the operation was successful or not.
     * @see #copyFile(File, File, boolean)
     */
    public static boolean copyFile(File fileSrc, File fileDest) {
        return copyFile(fileSrc, fileDest, false);
    }
    
    /**
     * Attempts to copy directory dirSrc to directory dirDest.
     *
     * @param dirSrc    The source directory.
     * @param dirDest   The destination directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @param insert    If set to true, dirSrc will be copied inside dirDest.<br>
     *                  Otherwise, dirSrc will be copied to the location dirDest.
     * @return Whether the operation was successful or not.
     */
    public static boolean copyDirectory(File dirSrc, File dirDest, boolean overwrite, boolean insert) {
        if (dirSrc.isFile()) {
            return copyFile(dirSrc, dirDest, overwrite);
        }
        Log.trace("Copying directory: {} to: {}", dirSrc, dirDest);
        
        if (!dirSrc.exists()) {
            Log.warn("Source directory does not exist: {}", dirSrc);
            return false;
        }
        if (dirSrc.getAbsolutePath().equalsIgnoreCase(dirDest.getAbsolutePath())) {
            Log.trace("Source directory: {} and destination directory: {} are the same", dirSrc, dirDest);
            return true;
        }
        
        try {
            if (insert) {
                if (dirDest.isFile()) { //and if dirSrc is a directory
                    if (overwrite) {
                        deleteFile(dirDest);
                    } else {
                        Log.warn("Destination directory is a file: {}", dirDest);
                        return false;
                    }
                }
                if (!dirDest.exists()) {
                    if (!createDirectory(dirDest)) { //attempt to create destination directory if it doesn't exist
                        Log.warn("Could not create destination directory: {}", dirDest);
                        return false;
                    }
                }
                File destDir = new File(dirDest, dirSrc.getName());
                if (destDir.exists()) {
                    if (overwrite) {
                        deleteDirectory(destDir);
                    } else {
                        Log.warn("Destination directory already exists: {}", destDir);
                        return false;
                    }
                }
                FileUtils.copyDirectoryToDirectory(dirSrc, dirDest); //copies directory within the destination directory
                Log.info("Copied directory: {} to: {}", dirSrc, destDir);
            } else {
                if (dirDest.exists()) {
                    if (overwrite) {
                        delete(dirDest);
                    } else {
                        Log.warn("Destination directory already exists: {}", dirDest);
                        return false;
                    }
                }
                if (!dirDest.getParentFile().exists()) {
                    if (!createDirectory(dirDest.getParentFile())) { //attempt to create destination directory if it doesn't exist
                        Log.warn("Could not create destination directory: {}", dirDest.getParentFile());
                        return false;
                    }
                }
                FileUtils.copyDirectory(dirSrc, dirDest, true); //copies directory to destination directory path
                Log.info("Copied directory: {} to: {}", dirSrc, dirDest);
            }
            return true;
        } catch (IOException e) {
            Log.error("Unable to copy directory: {} to: {}", e, dirSrc, dirDest);
            return false;
        }
    }
    
    /**
     * Attempts to copy directory dirSrc to directory dirDest.
     *
     * @param dirSrc    The source directory.
     * @param dirDest   The destination directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @return Whether the operation was successful or not.
     * @see #copyDirectory(File, File, boolean, boolean)
     */
    public static boolean copyDirectory(File dirSrc, File dirDest, boolean overwrite) {
        return copyDirectory(dirSrc, dirDest, overwrite, false);
    }
    
    /**
     * Attempts to copy directory dirSrc to directory dirDest.
     *
     * @param dirSrc  The source directory.
     * @param dirDest The destination directory.
     * @return Whether the operation was successful or not.
     * @see #copyDirectory(File, File, boolean)
     */
    public static boolean copyDirectory(File dirSrc, File dirDest) {
        return copyDirectory(dirSrc, dirDest, false);
    }
    
    /**
     * Attempts to copy src to dest.
     *
     * @param src       The path to the source file or directory.
     * @param dest      The path to the destination file or directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @return Whether the operation was successful or not.
     * @see #copyFile(File, File, boolean)
     * @see #copyDirectory(File, File, boolean)
     */
    public static boolean copy(File src, File dest, boolean overwrite) {
        return src.isFile() ? copyFile(src, dest, overwrite) : copyDirectory(src, dest, overwrite);
    }
    
    /**
     * Attempts to copy src to dest.
     *
     * @param src  The path to the source file or directory.
     * @param dest The path to the destination file or directory.
     * @return Whether the operation was successful or not.
     * @see #copy(File, File, boolean)
     */
    public static boolean copy(File src, File dest) {
        return copy(src, dest, false);
    }
    
    /**
     * Attempts to move file fileSrc to file fileDest.<br>
     * If fileDest is a directory, fileSrc will be moved into that directory.<br>
     * If fileDest is a file, fileSrc will be moved to that file path.
     *
     * @param fileSrc   The source file.
     * @param fileDest  The destination file.
     * @param overwrite Whether or not to overwrite the destination file if it exists.
     * @return Whether the operation was successful or not.
     */
    public static boolean moveFile(File fileSrc, File fileDest, boolean overwrite) {
        if (fileSrc.isDirectory()) {
            return moveDirectory(fileSrc, fileDest, overwrite);
        }
        Log.trace("Moving file: {} to: {}", fileSrc, fileDest);
        
        if (!fileSrc.exists()) {
            Log.warn("Source file does not exist: {}", fileSrc);
            return false;
        }
        if (fileSrc.getAbsolutePath().equalsIgnoreCase(fileDest.getAbsolutePath())) {
            Log.trace("Source file: {} and destination file: {} are the same", fileSrc, fileDest);
            return true;
        }
        
        try {
            if (fileDest.exists() && fileDest.isDirectory()) {
                File destFile = new File(fileDest, fileSrc.getName());
                if (destFile.exists()) {
                    if (overwrite) {
                        deleteFile(destFile);
                    } else {
                        Log.warn("Destination file already exists: {}", destFile);
                        return false;
                    }
                }
                FileUtils.moveFileToDirectory(fileSrc, fileDest, true); //moves file into destination directory, creating the directory if it doesn't exist
                Log.info("Moved file: {} to: {}", fileSrc, destFile);
            } else {
                if (fileDest.exists()) {
                    if (overwrite) {
                        delete(fileDest);
                    } else {
                        Log.warn("Destination file already exists: {}", fileDest);
                        return false;
                    }
                }
                FileUtils.moveFile(fileSrc, fileDest); //moves file from the source file path to the destination file path
                Log.info("Moved file: {} to: {}", fileSrc, fileDest);
            }
            return true;
        } catch (IOException e) {
            Log.error("Unable to move file: {} to: {}", e, fileSrc, fileDest);
            return false;
        }
    }
    
    /**
     * Attempts to move file fileSrc to file fileDest.<br>
     * If fileDest is a directory, fileSrc will be moved into that directory.<br>
     * If fileDest is a file, fileSrc will be moved to that file path.
     *
     * @param fileSrc  The source file.
     * @param fileDest The destination file.
     * @return Whether the operation was successful or not.
     * @see #moveFile(File, File, boolean)
     */
    public static boolean moveFile(File fileSrc, File fileDest) {
        return moveFile(fileSrc, fileDest, false);
    }
    
    /**
     * Attempts to move directory dirSrc to directory dirDest.
     *
     * @param dirSrc    The source directory.
     * @param dirDest   The destination directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @param insert    If set to true, dirSrc will be moved inside dirDest.<br>
     *                  Otherwise, dirSrc will be moved to the location dirDest.
     * @return Whether the operation was successful or not.
     */
    public static boolean moveDirectory(File dirSrc, File dirDest, boolean overwrite, boolean insert) {
        if (dirSrc.isFile()) {
            return moveFile(dirSrc, dirDest, overwrite);
        }
        Log.trace("Moving directory: {} to: {}", dirSrc, dirDest);
        
        if (!dirSrc.exists()) {
            Log.warn("Source directory does not exist: {}", dirSrc);
            return false;
        }
        if (dirSrc.getAbsolutePath().equalsIgnoreCase(dirDest.getAbsolutePath())) {
            Log.trace("Source directory: {} and destination directory: {} are the same", dirSrc, dirDest);
            return true;
        }
        
        try {
            if (insert) {
                if (dirDest.isFile()) { //and if dirSrc is a directory
                    if (overwrite) {
                        deleteFile(dirDest);
                    } else {
                        Log.warn("Destination directory is a file: {}", dirDest);
                        return false;
                    }
                }
                File destDir = new File(dirDest, dirSrc.getName());
                if (destDir.exists()) {
                    if (overwrite) {
                        deleteDirectory(destDir);
                    } else {
                        Log.warn("Destination directory already exists: {}", destDir);
                        return false;
                    }
                }
                FileUtils.moveDirectoryToDirectory(dirSrc, dirDest, true); //moves directory within the destination directory
                Log.info("Moved directory: {} to: {}", dirSrc, destDir);
            } else {
                if (dirDest.exists()) {
                    if (overwrite) {
                        delete(dirDest);
                    } else {
                        Log.warn("Destination directory already exists: {}", dirDest);
                        return false;
                    }
                }
                if (!dirDest.getParentFile().exists()) {
                    if (!createDirectory(dirDest.getParentFile())) { //attempt to create destination directory if it doesn't exist
                        Log.warn("Could not create destination directory: {}", dirDest.getParentFile());
                        return false;
                    }
                }
                FileUtils.moveDirectory(dirSrc, dirDest); //moves directory to destination directory path
                Log.info("Moved directory: {} to: {}", dirSrc, dirDest);
            }
            return true;
        } catch (IOException e) {
            Log.error("Unable to move directory: {} to: {}", e, dirSrc, dirDest);
            return false;
        }
    }
    
    /**
     * Attempts to move directory dirSrc to directory dirDest.
     *
     * @param dirSrc    The source directory.
     * @param dirDest   The destination directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @return Whether the operation was successful or not.
     * @see #moveDirectory(File, File, boolean, boolean)
     */
    public static boolean moveDirectory(File dirSrc, File dirDest, boolean overwrite) {
        return moveDirectory(dirSrc, dirDest, overwrite, false);
    }
    
    /**
     * Attempts to move directory dirSrc to directory dirDest.
     *
     * @param dirSrc  The source directory.
     * @param dirDest The destination directory.
     * @return Whether the operation was successful or not.
     * @see #moveDirectory(File, File, boolean)
     */
    public static boolean moveDirectory(File dirSrc, File dirDest) {
        return moveDirectory(dirSrc, dirDest, false);
    }
    
    /**
     * Attempts to move src to dest.
     *
     * @param src       The source file or directory.
     * @param dest      The destination file or directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @return Whether the operation was successful or not.
     * @see #moveFile(File, File, boolean)
     * @see #moveDirectory(File, File, boolean)
     */
    public static boolean move(File src, File dest, boolean overwrite) {
        return src.isFile() ? moveFile(src, dest, overwrite) : moveDirectory(src, dest, overwrite);
    }
    
    /**
     * Attempts to move src to dest.
     *
     * @param src  The source file or directory.
     * @param dest The destination file or directory.
     * @return Whether the operation was successful or not.
     * @see #move(File, File, boolean)
     */
    public static boolean move(File src, File dest) {
        return move(src, dest, false);
    }
    
    /**
     * Attempts to replace file fileDest with file fileSrc.
     *
     * @param fileSrc  The source file.
     * @param fileDest The destination file to replace.
     * @return Whether the operation was successful or not.
     * @see #moveFile(File, File, boolean)
     */
    public static boolean replaceFile(File fileSrc, File fileDest) {
        return moveFile(fileSrc, fileDest, true);
    }
    
    /**
     * Attempts to replace directory dirDest with directory dirSrc.
     *
     * @param dirSrc  The source directory.
     * @param dirDest The destination directory to replace.
     * @return Whether the operation was successful or not.
     * @see #moveDirectory(File, File, boolean)
     */
    public static boolean replaceDirectory(File dirSrc, File dirDest) {
        return moveDirectory(dirSrc, dirDest, true);
    }
    
    /**
     * Attempts to replace dest with src.
     *
     * @param src  The source file or directory.
     * @param dest The destination file or directory to replace.
     * @return Whether the operation was successful or not.
     * @see #replaceFile(File, File)
     * @see #replaceDirectory(File, File)
     */
    public static boolean replace(File src, File dest) {
        return src.isFile() ? replaceFile(src, dest) : replaceDirectory(src, dest);
    }
    
    /**
     * Attempts to delete all the files within a directory.
     *
     * @param dir The directory to clear.
     * @return Whether the operation was successful or not.
     */
    public static boolean clearDirectory(File dir) {
        Log.trace("Clearing directory: {}", dir);
        
        if (!dir.exists()) {
            Log.warn("Target directory does not exist: {}", dir);
            return false;
        }
        if (!dir.isDirectory()) {
            Log.warn("Target directory is not a directory: {}", dir);
            return false;
        }
        
        boolean success = true;
        List<File> entries = getFilesAndDirs(dir);
        
        for (File entry : entries) {
            success &= delete(entry);
        }
        
        if (success) {
            Log.info("Cleared directory: {}", dir);
        } else {
            Log.warn("Unable to clear directory: {}", dir);
        }
        return success;
    }
    
    /**
     * Returns a list of files in the specified directory that pass the specified filter.
     *
     * @param directory The directory to search for files in.
     * @param filter    The filter for files in the directory.
     * @return A list of files that were discovered.
     */
    public static List<File> listFiles(File directory, FileFilter filter) {
        Log.trace("Listing the files in directory: {}", directory);
        
        if (!directory.exists()) {
            Log.warn("Target directory does not exist: {}", directory);
            return new ArrayList<>();
        }
        if (!directory.isDirectory()) {
            Log.warn("Target directory is not a directory: {}", directory);
            return new ArrayList<>();
        }
        
        File[] files = directory.listFiles(filter);
        
        if (files == null) {
            Log.warn("Unable to list the files in directory: {}", directory);
            return new ArrayList<>();
        }
        
        return Arrays.asList(files);
    }
    
    /**
     * Returns a list of files that pass the specified filter in the specified directory and in all subdirectories that pass the filter.
     *
     * @param directory  The directory to search for files in.
     * @param fileFilter The filter for files.
     * @param dirFilter  The filter for directories to enter.
     * @return A list of files that were discovered.
     * @see #getFilesRecursivelyHelper(File, FileFilter, FileFilter)
     */
    public static List<File> getFilesRecursively(File directory, FileFilter fileFilter, FileFilter dirFilter) {
        Log.trace("Listing the files in directory recursively: {}", directory);
        
        if (!directory.exists()) {
            Log.warn("Target directory does not exist: {}", directory);
            return new ArrayList<>();
        }
        if (!directory.isDirectory()) {
            Log.warn("Target directory is not a directory: {}", directory);
            return new ArrayList<>();
        }
        
        return getFilesRecursivelyHelper(directory, fileFilter, dirFilter);
    }
    
    /**
     * Returns a list of files that pass the specified filter in the specified directory and in all subdirectories.
     *
     * @param directory  The directory to search for files in.
     * @param fileFilter The filter for files.
     * @return A list of files that were discovered.
     * @see #getFilesRecursively(File, FileFilter, FileFilter)
     */
    public static List<File> getFilesRecursively(File directory, FileFilter fileFilter) {
        return getFilesRecursively(directory, fileFilter, dir -> true);
    }
    
    /**
     * Returns a list of files in the specified directory and in all subdirectories.
     *
     * @param directory The directory to search for files in.
     * @return A list of files that were discovered.
     * @see #getFilesRecursively(File, FileFilter)
     */
    public static List<File> getFilesRecursively(File directory) {
        return getFilesRecursively(directory, file -> true);
    }
    
    /**
     * Recursive helper for the getFilesRecursively method.
     *
     * @param directory  The directory to search for files in.
     * @param fileFilter The filter for files.
     * @param dirFilter  The filter for directories to enter.
     * @return A list of files that were discovered.
     */
    private static List<File> getFilesRecursivelyHelper(File directory, FileFilter fileFilter, FileFilter dirFilter) {
        List<File> returnList = new ArrayList<>();
        
        File[] list = directory.listFiles(file ->
                ((file.isFile() && fileFilter.accept(file)) || (file.isDirectory() && dirFilter.accept(file))));
        
        if (list == null) {
            Log.warn("Unable to recursively list the files in directory: {}", directory);
            return returnList;
        }
        
        for (File file : list) {
            if (file.isFile()) {
                returnList.add(file); //add file to list
            } else {
                returnList.addAll(getFilesRecursively(file, fileFilter, dirFilter)); //enter directory
            }
        }
        
        return returnList;
    }
    
    /**
     * Returns a list of directories that pass the specified filter in the specified directory and in all subdirectories.
     *
     * @param directory The directory to search for directories in.
     * @param dirFilter The filter for directories.
     * @return A list of directories that were discovered.
     * @see #getDirsRecursivelyHelper(File, FileFilter)
     */
    public static List<File> getDirsRecursively(File directory, FileFilter dirFilter) {
        Log.trace("Listing the directories in directory recursively: {}", directory);
        
        if (!directory.exists()) {
            Log.warn("Target directory does not exist: {}", directory);
            return new ArrayList<>();
        }
        if (!directory.isDirectory()) {
            Log.warn("Target directory is not a directory: {}", directory);
            return new ArrayList<>();
        }
        
        return getDirsRecursivelyHelper(directory, dirFilter);
    }
    
    /**
     * Returns a list of directories in the specified directory and in all subdirectories.
     *
     * @param directory The directory to search for directories in.
     * @return A list of directories that were discovered.
     * @see #getDirsRecursively(File, FileFilter)
     */
    public static List<File> getDirsRecursively(File directory) {
        return getDirsRecursively(directory, dir -> true);
    }
    
    /**
     * Recursive helper for the getDirsRecursively method.
     *
     * @param directory The directory to search for directories in.
     * @param dirFilter The filter for directories.
     * @return A list of directories that were discovered.
     */
    private static List<File> getDirsRecursivelyHelper(File directory, FileFilter dirFilter) {
        List<File> returnList = new ArrayList<>();
        
        File[] list = directory.listFiles(file ->
                (file.isDirectory() && dirFilter.accept(file)));
        
        if (list == null) {
            Log.warn("Unable to recursively list the directories in directory: {}", directory);
            return returnList;
        }
        
        for (File file : list) {
            returnList.add(file); //add directory to list
            returnList.addAll(getDirsRecursively(file, dirFilter)); //enter directory
        }
        
        return returnList;
    }
    
    /**
     * Returns a list of files and directories that pass the specified filters in the specified directory and in all subdirectories.
     *
     * @param directory  The directory to search for files and directories in.
     * @param fileFilter The filter for files.
     * @param dirFilter  The filter for directories.
     * @return A list of files and directories that were discovered.
     * @see #getFilesAndDirsRecursivelyHelper(File, FileFilter, FileFilter)
     */
    public static List<File> getFilesAndDirsRecursively(File directory, FileFilter fileFilter, FileFilter dirFilter) {
        Log.trace("Listing the files and directories in directory recursively: {}", directory);
        
        if (!directory.exists()) {
            Log.warn("Target directory does not exist: {}", directory);
            return new ArrayList<>();
        }
        if (!directory.isDirectory()) {
            Log.warn("Target directory is not a directory: {}", directory);
            return new ArrayList<>();
        }
        
        return getFilesAndDirsRecursivelyHelper(directory, fileFilter, dirFilter);
    }
    
    /**
     * Returns a list of files and directories that pass the specified filter in the specified directory and in all subdirectories.
     *
     * @param directory  The directory to search for files and directories in.
     * @param fileFilter The filter for files.
     * @return A list of files and directories that were discovered.
     * @see #getFilesAndDirsRecursively(File, FileFilter, FileFilter)
     */
    public static List<File> getFilesAndDirsRecursively(File directory, FileFilter fileFilter) {
        return getFilesAndDirsRecursively(directory, fileFilter, dir -> true);
    }
    
    /**
     * Returns a list of files and directories in the specified directory and in all subdirectories.
     *
     * @param directory The directory to search for files and directories in.
     * @return A list of files and directories that were discovered.
     * @see #getFilesAndDirsRecursively(File, FileFilter)
     */
    public static List<File> getFilesAndDirsRecursively(File directory) {
        return getFilesAndDirsRecursively(directory, file -> true);
    }
    
    /**
     * Recursive helper for the getFilesAndDirsRecursively method.
     *
     * @param directory  The directory to search for files and directories in.
     * @param fileFilter The filter for files.
     * @param dirFilter  The filter for directories.
     * @return A list of files and directories that were discovered.
     */
    private static List<File> getFilesAndDirsRecursivelyHelper(File directory, FileFilter fileFilter, FileFilter dirFilter) {
        List<File> returnList = new ArrayList<>();
        
        File[] list = directory.listFiles(file ->
                ((file.isFile() && fileFilter.accept(file)) || (file.isDirectory() && dirFilter.accept(file))));
        
        if (list == null) {
            Log.warn("Unable to recursively list the files and directories in directory: {}", directory);
            return new ArrayList<>();
        }
        
        for (File file : list) {
            returnList.add(file); //add file or directory to list
            
            if (file.isDirectory()) {
                returnList.addAll(getFilesAndDirsRecursively(file, fileFilter, dirFilter)); //enter directory
            }
        }
        
        return returnList;
    }
    
    /**
     * Returns a list of files that pass the specified filter in the specified directory.
     *
     * @param directory  The directory to search for files in.
     * @param fileFilter The filter for files.
     * @return A list of files that were discovered.
     * @see #listFiles(File, FileFilter)
     */
    public static List<File> getFiles(File directory, FileFilter fileFilter) {
        return listFiles(directory, file ->
                (file.isFile() && fileFilter.accept(file)));
    }
    
    /**
     * Returns a list of files in the specified directory.
     *
     * @param directory The directory to search for files in.
     * @return A list of files that were discovered.
     * @see #listFiles(File, FileFilter)
     */
    public static List<File> getFiles(File directory) {
        return listFiles(directory, File::isFile);
    }
    
    /**
     * Returns a list of directories that pass the specified filter in the specified directory.
     *
     * @param directory The directory to search for files in.
     * @param dirFilter The filter for directories.
     * @return A list of directories that were discovered.
     * @see #listFiles(File, FileFilter)
     */
    public static List<File> getDirs(File directory, FileFilter dirFilter) {
        return listFiles(directory, file ->
                (file.isDirectory() && dirFilter.accept(file)));
    }
    
    /**
     * Returns a list of directories in the specified directory.
     *
     * @param directory The directory to search for files in.
     * @return A list of directories that were discovered.
     * @see #listFiles(File, FileFilter)
     */
    public static List<File> getDirs(File directory) {
        return listFiles(directory, File::isDirectory);
    }
    
    /**
     * Returns a list of files and directories that pass the specified filters in the specified directory.
     *
     * @param directory  The directory to search for files and directories in.
     * @param fileFilter The filter for files.
     * @param dirFilter  The filter for directories.
     * @return A list of files and directories that were discovered.
     * @see #listFiles(File, FileFilter)
     */
    public static List<File> getFilesAndDirs(File directory, FileFilter fileFilter, FileFilter dirFilter) {
        return listFiles(directory, file ->
                ((file.isFile() && fileFilter.accept(file)) || (file.isDirectory() && dirFilter.accept(file))));
    }
    
    /**
     * Returns a list of files and directories in the specified directory.
     *
     * @param directory The directory to search for files and directories in.
     * @return A list of files and directories that were discovered.
     * @see #listFiles(File, FileFilter)
     */
    public static List<File> getFilesAndDirs(File directory) {
        return listFiles(directory, file -> true);
    }
    
    /**
     * Determines if a file or directory exists or not.
     *
     * @param file The file or directory.
     * @return Whether the file or directory exists, or not.
     */
    public static boolean exists(File file) {
        return file.exists();
    }
    
    /**
     * Determines if the content of two files is equal.
     *
     * @param a The first file.
     * @param b The second file.
     * @return True if the files are equal, false otherwise.<br>
     * Will return false if either file does not exist.
     */
    public static boolean contentEquals(File a, File b) {
        Log.trace("Comparing content of file: {} to: {}", a, b);
        
        if (!a.exists() || !b.exists()) {
            if (!a.exists()) {
                Log.warn("File does not exist: {}", a);
            }
            if (!b.exists()) {
                Log.warn("File does not exist: {}", b);
            }
            return false;
        }
        
        try {
            return FileUtils.contentEquals(a, b);
        } catch (IOException e) {
            Log.error("Unable to compare files: {} and: {}", e, a, b);
            return false;
        }
    }
    
    /**
     * Calculates the size of a file.
     *
     * @param file The file to test.
     * @return The size of the file in bytes.<br>
     * Will return 0 if the file does not exist.
     */
    public static long sizeOfFile(File file) {
        if (file.isDirectory()) {
            return sizeOfDirectory(file);
        }
        Log.trace("Determining size of file: {}", file);
        
        if (!file.exists()) {
            Log.trace("Target file does not exist: {}", file);
            return 0;
        }
        
        return FileUtils.sizeOf(file);
    }
    
    /**
     * Calculates the size of a directory recursively.
     *
     * @param dir The directory to test.
     * @return The size of the directory in bytes.<br>
     * Will return 0 if the directory does not exist.
     */
    public static long sizeOfDirectory(File dir) {
        if (dir.isFile()) {
            return sizeOfFile(dir);
        }
        Log.trace("Determining size of directory: {}", dir);
        
        if (!dir.exists()) {
            Log.trace("Target directory does not exist: {}", dir);
            return 0;
        }
        
        return FileUtils.sizeOfDirectory(dir);
    }
    
    /**
     * Calculates the size of a file or directory.
     *
     * @param file The file or directory.
     * @return The size of the file or directory in bytes.<br>
     * Will return 0 if the file or directory does not exist.
     * @see #sizeOfFile(File)
     * @see #sizeOfDirectory(File)
     */
    public static long sizeOf(File file) {
        return file.isFile() ? sizeOfFile(file) : sizeOfDirectory(file);
    }
    
    /**
     * Determines if a file is empty or not.
     *
     * @param file The file to test.
     * @return Whether the file is empty or not.<br>
     * Will return true if the file does not exist.
     */
    public static boolean fileIsEmpty(File file) {
        if (file.isDirectory()) {
            return directoryIsEmpty(file);
        }
        Log.trace("Determining if file: {} is empty", file);
        
        if (!file.exists()) {
            Log.trace("Target file does not exist: {}", file);
            return true;
        }
        
        return (sizeOfFile(file) == 0);
    }
    
    /**
     * Determines if a directory is empty or not.
     *
     * @param dir The directory to test.
     * @return Whether the directory is empty or not.<br>
     * Will return true if the directory does not exist.
     */
    public static boolean directoryIsEmpty(File dir) {
        if (dir.isFile()) {
            return fileIsEmpty(dir);
        }
        Log.trace("Determining if directory: {} is empty", dir);
        
        if (!dir.exists()) {
            Log.trace("Target directory does not exist: {}", dir);
            return true;
        }
        
        Path path = dir.toPath();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            Iterator<Path> files = ds.iterator();
            return !files.hasNext();
        } catch (IOException e) {
            Log.error("Unable to determine if directory is empty: {}", e, dir);
            return false;
        }
    }
    
    /**
     * Determines if a file or directory is empty or not.
     *
     * @param file The file or directory to test.
     * @return Whether the file or directory is empty or not.
     * @see #fileIsEmpty(File)
     * @see #directoryIsEmpty(File)
     */
    public static boolean isEmpty(File file) {
        return file.isFile() ? fileIsEmpty(file) : directoryIsEmpty(file);
    }
    
    /**
     * Compares the size of two files.
     *
     * @param a The first file.
     * @param b The second file.
     * @return Less than 0 if the size of file a is less than the size of file b.<br>
     * Greater than 0 if the size of file a is greater than the size of file b.
     * 0 if the size of file a is equal to the size of file b.
     */
    public static int sizeCompare(File a, File b) {
        Log.trace("Comparing size of file: {} to: {}", a, b);
        
        if (!a.exists() || !b.exists()) {
            if (!a.exists()) {
                Log.warn("File does not exist: {}", a);
            }
            if (!b.exists()) {
                Log.warn("File does not exist: {}", b);
            }
            return 0;
        }
        
        return Long.compare(FileUtils.sizeOf(a), FileUtils.sizeOf(b));
    }
    
    /**
     * Compares the date of two files.
     *
     * @param a The first file.
     * @param b The second file.
     * @return Less than 0 if the date of file a is older than the date of file b.<br>
     * Greater than 0 if the date of file a is newer than the date of file b.<br>
     * 0 if the date of file a is equal to the date of file b.
     */
    public static int dateCompare(File a, File b) {
        Log.trace("Comparing date of file: {} to: {}", a, b);
        
        if (!a.exists() || !b.exists()) {
            if (!a.exists()) {
                Log.warn("File does not exist: {}", a);
            }
            if (!b.exists()) {
                Log.warn("File does not exist: {}", b);
            }
            return 0;
        }
        
        if (FileUtils.isFileOlder(a, b)) {
            return -1;
        }
        if (FileUtils.isFileNewer(a, b)) {
            return 1;
        }
        return 0;
    }
    
    /**
     * Reads the file dates of a file.
     *
     * @param file The file.
     * @return The file dates of the file.
     */
    public static Map<String, FileTime> readDates(File file) {
        Log.trace("Reading file dates of file: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
        }
        
        Map<String, FileTime> dates = new HashMap<>();
        List<String> attributes = Arrays.asList("lastModifiedTime", "lastAccessTime", "creationTime");
        for (String attribute : attributes) {
            try {
                dates.put(attribute, (FileTime) Files.getAttribute(file.toPath(), attribute));
            } catch (Exception e) {
                Log.error("Unable to read: " + attribute + " of file: {}", e, file);
            }
        }
        return dates;
    }
    
    /**
     * Returns the last modified time of a file.
     *
     * @param file The file.
     * @return The last modified time of the file, or null if there was an error.
     */
    public static Date getLastModifiedTime(File file) {
        Log.trace("Reading the last modified time of file: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
        }
        
        try {
            return new Date(((FileTime) Files.getAttribute(file.toPath(), "lastModifiedTime")).toMillis());
        } catch (Exception e) {
            Log.error("Unable to read the last modified time of file: {}", e, file);
            return null;
        }
    }
    
    /**
     * Returns the last access time of a file.
     *
     * @param file The file.
     * @return The last access time of the file, or null if there was an error.
     */
    public static Date getLastAccessTime(File file) {
        Log.trace("Reading the last access time of file: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
        }
        
        try {
            return new Date(((FileTime) Files.getAttribute(file.toPath(), "lastAccessTime")).toMillis());
        } catch (Exception e) {
            Log.error("Unable to read the last access time of file: {}", e, file);
            return null;
        }
    }
    
    /**
     * Returns the creation time of a file.
     *
     * @param file The file.
     * @return The creation time of the file, or null if there was an error.
     */
    public static Date getCreationTime(File file) {
        Log.trace("Reading the creation time of file: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
        }
        
        try {
            return new Date(((FileTime) Files.getAttribute(file.toPath(), "creationTime")).toMillis());
        } catch (Exception e) {
            Log.error("Unable to read the creation time of file: {}", e, file);
            return null;
        }
    }
    
    /**
     * Writes the file dates of a file.
     *
     * @param file  The file.
     * @param dates The file dates.
     */
    public static void writeDates(File file, Map<String, FileTime> dates) {
        Log.trace("Writing file dates of file: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
        }
        
        List<String> attributes = Arrays.asList("lastModifiedTime", "lastAccessTime", "creationTime");
        for (String attribute : attributes) {
            FileTime date = dates.get(attribute);
            if (date == null) {
                continue;
            }
            try {
                Files.setAttribute(file.toPath(), attribute, date);
            } catch (Exception e) {
                Log.error("Unable to write: " + attribute + " of file: {}", e, file);
            }
        }
    }
    
    /**
     * Sets the last modified time of a file.
     *
     * @param file The file.
     * @param time The last modified time to set.
     * @return Whether the last modified time of the file was successfully set or not.
     */
    public static boolean setLastModifiedTime(File file, Date time) {
        Log.trace("Writing the last modified time of file: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
        }
        
        try {
            Files.setAttribute(file.toPath(), "lastModifiedTime", FileTime.fromMillis(time.getTime()));
            Log.debug("Wrote the last modified time of file: {}", file);
            return true;
        } catch (Exception e) {
            Log.error("Unable to write the last modified time of file: {}", e, file);
            return false;
        }
    }
    
    /**
     * Sets the last access time of a file.
     *
     * @param file The file.
     * @param time The last access time to set.
     * @return Whether the last access time of the file was successfully set or not.
     */
    public static boolean setLastAccessTime(File file, Date time) {
        Log.trace("Writing the last access time of file: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
        }
        
        try {
            Files.setAttribute(file.toPath(), "lastAccessTime", FileTime.fromMillis(time.getTime()));
            Log.debug("Wrote the last access time of file: {}", file);
            return true;
        } catch (Exception e) {
            Log.error("Unable to write the last access time of file: {}", e, file);
            return false;
        }
    }
    
    /**
     * Sets the creation time of a file.
     *
     * @param file The file.
     * @param time The creation time to set.
     * @return Whether the creation time of the file was successfully set or not.
     */
    public static boolean setCreationTime(File file, Date time) {
        Log.trace("Writing the creation time of file: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
        }
        
        try {
            Files.setAttribute(file.toPath(), "creationTime", FileTime.fromMillis(time.getTime()));
            Log.debug("Wrote the creation time of file: {}", file);
            return true;
        } catch (Exception e) {
            Log.error("Unable to write the creation time of file: {}", e, file);
            return false;
        }
    }
    
    /**
     * Opens an input stream for the file provided
     *
     * @param file The file to open for input.
     * @return The input stream that was opened.<br>
     * Will return null if file does not exist.
     */
    public static FileInputStream openInputStream(File file) {
        Log.trace("Opening input file stream to file: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
            return null;
        }
        if (file.isDirectory()) {
            Log.warn("Unable to open input streams on directories: {}", file);
            return null;
        }
        
        try {
            return FileUtils.openInputStream(file);
        } catch (IOException e) {
            Log.error("Unable to open input file stream to file: {}", e, file);
            return null;
        }
    }
    
    /**
     * Opens an output stream for the file provided.
     *
     * @param file   The file to open for output.
     * @param append The flag indicating whether to append to the file or not.
     * @return The output stream that was opened.
     */
    public static FileOutputStream openOutputStream(File file, boolean append) {
        Log.trace("Opening " + (append ? "appending " : "") + "output file stream to file: {}", file);
        
        if (file.isDirectory()) {
            Log.warn("Unable to open output streams on directories: {}", file);
            return null;
        }
        
        try {
            return FileUtils.openOutputStream(file, append);
        } catch (IOException e) {
            Log.error("Unable to open output file stream to file: {}", e, file);
            return null;
        }
    }
    
    /**
     * Opens an output stream for the file provided.
     *
     * @param file The file to open for output.
     * @return The output stream that was opened.
     * @see #openOutputStream(File, boolean)
     */
    public static FileOutputStream openOutputStream(File file) {
        return openOutputStream(file, false);
    }
    
    /**
     * Reads a file out to a string.
     *
     * @param file The file to read.
     * @return The contents of the file as a string, or null if there it could not be read.
     */
    public static String readFileToString(File file) {
        Log.trace("Reading file to string: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
            return null;
        }
        if (file.isDirectory()) {
            Log.warn("Unable to read directories to strings: {}", file);
            return null;
        }
        
        try {
            return FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            Log.error("Unable to read file to string: {}", e, file);
            return null;
        }
    }
    
    /**
     * Reads a file out to a byte array.
     *
     * @param file The file to read.
     * @return The contents of the file as a byte array, or null if there it could not be read.
     */
    public static byte[] readFileToByteArray(File file) {
        Log.trace("Reading file to byte array: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
            return null;
        }
        if (file.isDirectory()) {
            Log.warn("Unable to read directories to byte arrays: {}", file);
            return null;
        }
        
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            Log.error("Unable to read file to byte array: {}", e, file);
            return null;
        }
    }
    
    /**
     * Reads a file out to a list of lines.
     *
     * @param file The file to read.
     * @return The contents of the file as a list of strings, or null if there it could not be read.
     */
    public static List<String> readLines(File file) {
        Log.trace("Reading lines from file: {}", file);
        
        if (!file.exists()) {
            Log.warn("File does not exist: {}", file);
            return null;
        }
        if (file.isDirectory()) {
            Log.warn("Unable to read lines from directories: {}", file);
            return null;
        }
        
        try {
            return FileUtils.readLines(file, "UTF-8");
        } catch (IOException e) {
            Log.error("Unable to read lines from file: {}", e, file);
            return null;
        }
    }
    
    /**
     * Writes a string to a file.
     *
     * @param file   The file to write to.
     * @param data   The string to write to the file.
     * @param append The flag indicating whether to append to the file or not.
     * @return Whether the write was successful or not.
     */
    public static boolean writeStringToFile(File file, String data, boolean append) {
        Log.trace("Writing string to file: {}", file);
        
        if (!file.exists()) {
            createFile(file);
        }
        if (file.isDirectory()) {
            Log.warn("Unable to write strings to directories: {}", file);
            return false;
        }
        
        try {
            FileUtils.writeStringToFile(file, data, "UTF-8", append);
            Log.info("Wrote string to file: {}", file);
            return true;
        } catch (IOException e) {
            Log.error("Unable to write string to file: {}", e, file);
            return false;
        }
    }
    
    /**
     * Writes a string to a file.
     *
     * @param file The file to write to.
     * @param data The string to write to the file.
     * @return Whether the write was successful or not.
     * @see #writeStringToFile(File, String, boolean)
     */
    public static boolean writeStringToFile(File file, String data) {
        return writeStringToFile(file, data, false);
    }
    
    /**
     * Writes a byte array to a file.
     *
     * @param file   The file to write to.
     * @param data   The byte array to write to the file.
     * @param append The flag indicating whether to append to the file or not.
     * @return Whether the write was successful or not.
     */
    public static boolean writeByteArrayToFile(File file, byte[] data, boolean append) {
        Log.trace("Writing byte array to file: {}", file);
        
        if (!file.exists()) {
            createFile(file);
        }
        if (file.isDirectory()) {
            Log.warn("Unable to write byte arrays to directories: {}", file);
            return false;
        }
        
        try {
            FileUtils.writeByteArrayToFile(file, data, append);
            Log.info("Wrote byte array to file: {}", file);
            return true;
        } catch (IOException e) {
            Log.error("Unable to write byte array to file: {}", e, file);
            return false;
        }
    }
    
    /**
     * Writes a byte array to a file.
     *
     * @param file The file to write to.
     * @param data The byte array to write to the file.
     * @return Whether the write was successful or not.
     * @see #writeByteArrayToFile(File, byte[], boolean)
     */
    public static boolean writeByteArrayToFile(File file, byte[] data) {
        return writeByteArrayToFile(file, data, false);
    }
    
    /**
     * Writes string lines from a collections to a file.
     *
     * @param file   The file to write to.
     * @param lines  The collection of lines to be written.
     * @param append The flag indicating whether to append to the file or not.
     * @return Whether the write was successful or not.
     */
    public static boolean writeLines(File file, Collection<String> lines, boolean append) {
        Log.trace("Writing lines to file: {}", file);
        
        if (!file.exists()) {
            createFile(file);
        }
        if (file.isDirectory()) {
            Log.warn("Unable to write lines to directories: {}", file);
            return false;
        }
        
        try {
            FileUtils.writeLines(file, lines, append);
            Log.debug("Wrote lines to file: {}", file);
            return true;
        } catch (IOException e) {
            Log.error("Unable to write lines to file: {}", e, file);
            return false;
        }
    }
    
    /**
     * Writes string lines from a collections to a file.
     *
     * @param file  The file to write to.
     * @param lines The collection of lines to be written.
     * @return Whether the write was successful or not.
     * @see #writeLines(File, Collection, boolean)
     */
    public static boolean writeLines(File file, Collection<String> lines) {
        return writeLines(file, lines, false);
    }
    
    /**
     * Safely replaces a file with another file.<br>
     * To be used when data preservation is critical and speed is not.
     *
     * @param newFile      The new file.
     * @param originalFile The original file.
     * @return Whether the original file was successfully replaced with the new file or not.
     */
    public static boolean safeReplace(File newFile, File originalFile) {
        Log.trace("Safely replacing: {} with: {}", originalFile, newFile);
        
        if (!newFile.exists()) {
            Log.warn("The file: {} does not exist", newFile);
            return false;
        }
        
        File backup = new File(originalFile.getParentFile(), originalFile.getName() + ".bak");
        if (backup.exists()) {
            Log.error("A backup file: {} already exists, this could be data preserved from a failure; Will not continue", null, backup);
            return false;
        }
        
        if (originalFile.exists()) {
            Log.info("Creating a backup file: {}", backup);
            if (!copyFile(originalFile, backup) || (checksum(backup) != checksum(originalFile)) || !deleteFile(originalFile)) {
                Log.warn("Failed to create backup file: {}", backup);
                if (!originalFile.exists() && backup.exists()) {
                    move(backup, originalFile);
                }
                deleteFile(backup);
                return false;
            }
        }
        
        Log.info("Replacing: {} with: {}", originalFile, newFile);
        if (!copy(newFile, originalFile, true) || (checksum(originalFile) != checksum(newFile)) || !deleteFile(newFile)) {
            Log.warn("Failed to replace: {} with: {}", originalFile, newFile);
            if (backup.exists()) {
                move(backup, originalFile);
            }
            return false;
        }
        
        deleteFile(backup);
        Log.info("Successfully replaced: {} with: {}", originalFile, newFile);
        return true;
    }
    
    /**
     * Safely rewrites a file.<br>
     * To be used when data preservation is critical and speed is not.
     *
     * @param file The file.
     * @param data The data to write.
     * @return Whether the file was successfully rewritten or not.
     */
    public static boolean safeRewrite(File file, String data) {
        Log.trace("Safely rewriting: {}", file);
        
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
        if (tmp.exists()) {
            Log.error("A temporary file: {} already exists, this could be data preserved from a failure; Will not continue", null, tmp);
            return false;
        }
        
        Log.info("Rewriting: {}", file);
        if (!writeStringToFile(tmp, data) || !safeReplace(tmp, file)) {
            Log.warn("Failed to rewrite: {}", file);
            deleteFile(tmp);
            return false;
        }
        
        deleteFile(tmp);
        Log.info("Successfully rewrote: {}", file);
        return true;
    }
    
    /**
     * Safely rewrites a file.<br>
     * To be used when data preservation is critical and speed is not.
     *
     * @param file The file.
     * @param data The lines to write.
     * @return Whether the file was successfully rewritten or not.
     * @see #safeRewrite(File, String)
     */
    public static boolean safeRewrite(File file, List<String> data) {
        return safeRewrite(file, StringUtility.unsplitLines(data));
    }
    
    /**
     * Returns a system temporary directory.
     *
     * @return A system temporary directory.
     */
    public static File getTempDirectory() {
        String path = FileUtils.getTempDirectoryPath();
        if (path != null) {
            return new File(path);
        }
        return null;
    }
    
    /**
     * Returns the current user's directory.
     *
     * @return The current user's directory.
     */
    public static File getUserDirectory() {
        String path = FileUtils.getUserDirectoryPath();
        if (path != null) {
            return new File(path);
        }
        return null;
    }
    
    /**
     * Creates a symbolic link.
     *
     * @param target The target of the symbolic link.
     * @param link   The symbolic link.
     * @return Whether the operation was successful or not.
     */
    public static boolean createSymbolicLink(File target, File link) {
        Log.trace("Creating symbolic link from: {} to: {}", target, link);
        
        if (!target.exists()) {
            Log.warn("Symbolic link target does not exist: {}", target);
            return false;
        }
        if (link.exists()) {
            Log.warn("File already exists: {}", link);
            return false;
        }
        
        try {
            Files.createSymbolicLink(Paths.get(link.getAbsolutePath()), Paths.get(target.getAbsolutePath()));
            Log.info("Created symbolic link from: {} to: {}", target, link);
            return true;
        } catch (Exception e) {
            Log.error("Unable to create symbolic link from: {} to: {}", e, target, link);
            return false;
        }
    }
    
    /**
     * Tests if a file is a symbolic link.
     *
     * @param file The file.
     * @return Whether the file is a symbolic link or not.
     */
    public static boolean isSymbolicLink(File file) {
        return Files.isSymbolicLink(Paths.get(file.getAbsolutePath()));
    }
    
    /**
     * Calculates the CRC32 checksum of a file.
     *
     * @param file The file.
     * @return The checksum of the specified file.
     */
    public static long checksum(File file) {
        try {
            if (file.isFile()) {
                return FileUtils.checksumCRC32(file);
            } else if (file.isDirectory()) {
                long checksum = 0;
                for (File fd : getFilesRecursively(file)) {
                    checksum += FileUtils.checksumCRC32(fd);
                    checksum %= Long.MAX_VALUE;
                }
                return checksum;
            } else {
                return 0;
            }
        } catch (IOException ignored) {
            return 0;
        }
    }
    
    /**
     * Calculates the CRC32 checksums of the files in a directory.
     *
     * @param d The directory.
     * @return A JSON string containing the checksums of the files in the directory.
     */
    @SuppressWarnings("unchecked")
    public static String checksumDirectory(File d) {
        JSONObject json = new JSONObject();
        
        JSONArray checksums = new JSONArray();
        
        if (d.exists()) {
            for (File f : getFilesRecursively(d)) {
                String fName = StringUtility.lShear(StringUtility.fixFileSeparators(f.getAbsolutePath()), (d.getAbsolutePath() + '/').length());
                if (fName.endsWith("sync")) {
                    continue;
                }
                long fChecksum = checksum(f);
                
                JSONObject checksum = new JSONObject();
                checksum.put("file", fName);
                checksum.put("checksum", fChecksum);
                checksums.add(checksum);
            }
        }
        
        json.put("checksums", checksums);
        return json.toString();
    }
    
    /**
     * Performs a comparison between a checksum store and a directory.
     *
     * @param d         The directory.
     * @param checksums A JSON string containing a checksum store.
     * @return A JSON string specifying the modified, added, and deleted files.
     */
    @SuppressWarnings("unchecked")
    public static String compareChecksumDirectory(File d, String checksums) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject initial = (JSONObject) parser.parse(checksums);
            JSONObject target = (JSONObject) parser.parse(checksumDirectory(d));
            JSONObject compare = new JSONObject();
            
            Map<String, Long> initialChecksums = new HashMap<>();
            Map<String, Long> targetChecksums = new HashMap<>();
            for (Object initialChecksum : (JSONArray) initial.get("checksums")) {
                initialChecksums.put((String) ((JSONObject) initialChecksum).get("file"), (Long) ((JSONObject) initialChecksum).get("checksum"));
            }
            for (Object targetChecksum : (JSONArray) target.get("checksums")) {
                targetChecksums.put((String) ((JSONObject) targetChecksum).get("file"), (Long) ((JSONObject) targetChecksum).get("checksum"));
            }
            
            JSONArray additions = new JSONArray();
            JSONArray modifications = new JSONArray();
            JSONArray deletions = new JSONArray();
            
            //additions and modifications
            for (Entry<String, Long> entry : targetChecksums.entrySet()) {
                if (!initialChecksums.containsKey(entry.getKey())) {
                    additions.add(entry.getKey());
                } else if (!entry.getValue().equals(initialChecksums.get(entry.getKey()))) {
                    modifications.add(entry.getKey());
                }
            }
            
            //deletions
            for (Entry<String, Long> entry : initialChecksums.entrySet()) {
                if (!targetChecksums.containsKey(entry.getKey())) {
                    deletions.add(entry.getKey());
                }
            }
            
            compare.put("additions", additions);
            compare.put("modifications", modifications);
            compare.put("deletions", deletions);
            return compare.toString();
            
        } catch (ParseException | NumberFormatException e) {
            return "";
        }
    }
    
    /**
     * Generates a path from a list of directories using the proper file separators.
     *
     * @param endingSlash Whether or not to include an ending slash in the path.
     * @param paths       The list of directories of the path.
     * @return The final path string with the proper file separators.
     */
    public static String generatePath(boolean endingSlash, String... paths) {
        StringBuilder finalPath = new StringBuilder();
        for (String path : paths) {
            if ((finalPath.length() > 0) && !finalPath.toString().endsWith("/")) {
                finalPath.append('/');
            }
            finalPath.append(path);
        }
        if (endingSlash) {
            finalPath.append('/');
        }
        return finalPath.toString();
    }
    
    /**
     * Generates a path from a list of directories using the proper file separators.
     *
     * @param paths The list of directories of the path.
     * @return The final path string with the proper file separators.
     * @see #generatePath(boolean, String...)
     */
    public static String generatePath(String... paths) {
        return generatePath(false, paths);
    }
    
    /**
     * Returns the file type of a file.
     *
     * @param file The file.
     * @return The file type of the file.
     */
    public static String getFileType(File file) {
        return (file.getName().contains(".")) ?
               file.getName().substring(file.getName().lastIndexOf('.') + 1) : "";
    }
    
    /**
     * Returns a temporary file.
     *
     * @param extension The extension of the temporary file.
     * @param name      The requested name of the temporary file.
     * @return The temporary file.
     */
    public static File getTemporaryFile(String extension, String name) {
        File tmpFile;
        int index = 0;
        do {
            tmpFile = new File(Project.TMP_DIR,
                    (StringUtility.isNullOrEmpty(name) ? UUID.randomUUID().toString() : name) +
                            ((index > 0) ? ("_" + index) : "") +
                            ((StringUtility.isNullOrEmpty(extension) || StringUtility.lTrim(extension).startsWith(".")) ? "" : ".") +
                            StringUtility.removeWhiteSpace(extension));
            index++;
        } while (tmpFile.exists());
        
        tmpFiles.add(tmpFile);
        return tmpFile;
    }
    
    /**
     * Returns a temporary file.
     *
     * @param extension The extension of the temporary file.
     * @return The temporary file.
     * @see #getTemporaryFile(String, String)
     */
    public static File getTemporaryFile(String extension) {
        return getTemporaryFile(extension, null);
    }
    
    /**
     * Returns a temporary file.
     *
     * @return The temporary file.
     * @see #getTemporaryFile(String)
     */
    public static File getTemporaryFile() {
        return getTemporaryFile(null);
    }
    
    /**
     * Creates a temporary file and returns the created file.
     *
     * @param extension The extension of the temporary file.
     * @param name      The requested name of the temporary file.
     * @return The created temporary file.
     * @see #getTemporaryFile(String, String)
     */
    public static File createTemporaryFile(String extension, String name) {
        final File tmpFile = getTemporaryFile(extension, name);
        Filesystem.createFile(tmpFile);
        return tmpFile;
    }
    
    /**
     * Creates a temporary file and returns the created file.
     *
     * @param extension The extension of the temporary file.
     * @return The created temporary file.
     * @see #createTemporaryFile(String, String)
     */
    public static File createTemporaryFile(String extension) {
        return createTemporaryFile(extension, null);
    }
    
    /**
     * Creates a temporary file and returns the created file.
     *
     * @return The created temporary file.
     * @see #createTemporaryFile(String)
     */
    public static File createTemporaryFile() {
        return createTemporaryFile(null);
    }
    
    /**
     * Returns a temporary directory.
     *
     * @param name The requested name of the temporary directory.
     * @return The temporary directory.
     */
    public static File getTemporaryDirectory(String name) {
        File tmpDir;
        int index = 0;
        do {
            tmpDir = new File(Project.TMP_DIR,
                    (StringUtility.isNullOrEmpty(name) ? UUID.randomUUID().toString() : name) +
                            ((index > 0) ? ("_" + index) : ""));
            index++;
        } while (tmpDir.exists());
        
        tmpFiles.add(tmpDir);
        return tmpDir;
    }
    
    /**
     * Returns a temporary directory.
     *
     * @return The temporary directory.
     * @see #getTemporaryDirectory(String)
     */
    public static File getTemporaryDirectory() {
        return getTemporaryDirectory(null);
    }
    
    /**
     * Creates a temporary directory and returns the created directory.
     *
     * @param name The requested name of the temporary directory.
     * @return The created temporary directory.
     * @see #getTemporaryDirectory(String)
     */
    public static File createTemporaryDirectory(String name) {
        final File tmpDir = getTemporaryDirectory(name);
        Filesystem.createDirectory(tmpDir);
        return tmpDir;
    }
    
    /**
     * Creates a temporary directory and returns the created directory.
     *
     * @return The created temporary directory.
     * @see #createTemporaryDirectory(String)
     */
    public static File createTemporaryDirectory() {
        return createTemporaryDirectory(null);
    }
    
    /**
     * Returns the path length of a default temporary file with an extension.
     *
     * @param extension The extension of the temporary file.
     * @return The path length of a default temporary file with the specified extension.
     */
    public static int getTemporaryFilePathLength(String extension) {
        return generatePath(Project.TMP_DIR.getName(), UUID.randomUUID() +
                ((extension.isEmpty() || extension.startsWith(".")) ? "" : ".") + extension).length();
    }
    
    /**
     * Returns the path length of a default temporary file.
     *
     * @return The path length of a default temporary file.
     * @see #getTemporaryFilePathLength(String)
     */
    public static int getTemporaryFilePathLength() {
        return getTemporaryFilePathLength("");
    }
    
    /**
     * Returns the path length of a default temporary directory.
     *
     * @return The path length of a default temporary directory.
     */
    public static int getTemporaryDirectoryPathLength() {
        return generatePath(Project.TMP_DIR.getName(), UUID.randomUUID().toString()).length();
    }
    
    
    //Inner Classes
    
    /**
     * Logs Filesystem events.
     */
    private static class Log {
        
        //Static Fields
        
        /**
         * The Filesystem log level.
         */
        private static final Level level = Level.DEBUG;
        
        
        //Static Methods
        
        /**
         * Returns the Filesystem log level.
         *
         * @return The Filesystem log level.
         */
        public static Level logLevel() {
            return level;
        }
        
        /**
         * Logs a Filesystem event.
         *
         * @param log   The log.
         * @param error The error.
         * @param vars  The variables for the log.
         */
        private static void doLog(Level level, String log, Throwable error, File... vars) {
            if (level.isGreaterOrEqual(logLevel())) {
                logger.trace(StringUtility.format(("Filesystem: " + StringUtility.padLeft(level.toString(), 5) + ": " + log),
                        Arrays.stream(vars).map(StringUtility::fileString).map(e -> StringUtility.quote(e, true)).toArray()), error);
            }
        }
        
        /**
         * Logs a Filesystem trace event.
         *
         * @param log  The log.
         * @param vars The variables for the log.
         */
        public static void trace(String log, File... vars) {
            doLog(Level.TRACE, log, null, vars);
        }
        
        /**
         * Logs a Filesystem debug event.
         *
         * @param log  The log.
         * @param vars The variables for the log.
         */
        public static void debug(String log, File... vars) {
            doLog(Level.DEBUG, log, null, vars);
        }
        
        /**
         * Logs a Filesystem info event.
         *
         * @param log  The log.
         * @param vars The variables for the log.
         */
        public static void info(String log, File... vars) {
            doLog(Level.INFO, log, null, vars);
        }
        
        /**
         * Logs a Filesystem warn event.
         *
         * @param log  The log.
         * @param vars The variables for the log.
         */
        public static void warn(String log, File... vars) {
            doLog(Level.WARN, log, null, vars);
        }
        
        /**
         * Logs a Filesystem error event.
         *
         * @param log   The log.
         * @param error The error.
         * @param vars  The variables for the log.
         */
        public static void error(String log, Throwable error, File... vars) {
            doLog(Level.ERROR, log, error, vars);
        }
        
    }
    
}
