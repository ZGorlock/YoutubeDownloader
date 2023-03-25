/*
 * File:    ArchiveUtility.java
 * Package: commons.io.file
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.io.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import commons.access.Filesystem;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles archive operations.
 */
public final class ArchiveUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ArchiveUtility.class);
    
    
    //Constants
    
    /**
     * The path separator character for an archive file.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public static final String ARCHIVE_PATH_SEPARATOR = "/";
    
    
    //Enums
    
    /**
     * An enumeration of supported Archive Types.
     */
    public enum ArchiveType {
        
        //Values
        
        ZIP("zip"),
        JAR("jar");
        
        
        //Fields
        
        /**
         * The name of the Archive Type.
         */
        private final String name;
        
        
        //Constructors
        
        /**
         * Constructs an Archive Type.
         *
         * @param name The name of the Archive Type.
         */
        ArchiveType(String name) {
            this.name = name;
        }
        
        
        //Methods
        
        /**
         * Returns a string representation of the Archive Type.
         *
         * @return A String representation of the Archive Type.
         */
        @Override
        public String toString() {
            return name;
        }
        
    }
    
    /**
     * An enumeration of archive Compression Methods.
     */
    public enum CompressionMethod {
        
        //Values
        
        STORE(ZipEntry.STORED),
        COMPRESS(ZipEntry.DEFLATED);
        
        
        //Fields
        
        /**
         * The level for the Compression Method.
         */
        private final int level;
        
        
        //Constructors
        
        /**
         * Constructs a Compression Method.
         *
         * @param level The level for the Compression Method.
         */
        CompressionMethod(int level) {
            this.level = level;
        }
        
        
        //Getters
        
        /**
         * Returns the level for the Compression Method.
         *
         * @return The level for the Compression Method.
         */
        public int getLevel() {
            return level;
        }
        
    }
    
    
    //Static Methods
    
    /**
     * Extracts a resource from an archive to an external directory.
     *
     * @param archive         The archive file to extract from.
     * @param resource        The name of the resource to extract.
     * @param outputDirectory The directory to extract the resource to.
     * @return Whether the extraction was successful or not.
     * @see ZipArchive#extractResource(File, String, File)
     * @see JarArchive#extractResource(File, String, File)
     */
    public static boolean extractResource(File archive, String resource, File outputDirectory) {
        resource = resource.replaceAll("[\\\\/]", Matcher.quoteReplacement(ARCHIVE_PATH_SEPARATOR));
        String resourceName = resource.contains(ARCHIVE_PATH_SEPARATOR) ? resource.substring(resource.lastIndexOf(ARCHIVE_PATH_SEPARATOR)) : resource;
        
        if ((archive == null) || !archive.exists()) {
            logger.trace("Unable to extract resource: {} from archive: {} archive does not exist", resource, ((archive == null) ? "null" : StringUtility.fixFileSeparators(archive.getAbsolutePath())));
            return false;
        }
        
        ArchiveType type = getArchiveType(archive);
        if (type == null) {
            logger.trace("Unable to extract resource: {} from archive: {} archive type is not supported", resource, StringUtility.fileString(archive));
            return false;
        }
        
        logger.trace("Extracting resource: {} from {}: {} to: {}", resource, type, StringUtility.fileString(archive), StringUtility.fileString(outputDirectory));
        
        if (!archive.exists()) {
            logger.trace("Unable to extract resource: {} from {}: {} {} does not exist", resource, type, StringUtility.fileString(archive), type);
            return false;
        }
        
        if (!outputDirectory.exists() && !Filesystem.createDirectory(outputDirectory)) {
            logger.trace("Unable to extract resource: {} from {}: {} could not create output directory: {}", resource, type, StringUtility.fileString(archive), StringUtility.fileString(outputDirectory));
            return false;
        }
        
        File output = new File(outputDirectory, resourceName);
        if (!Filesystem.createDirectory(output.getParentFile())) {
            logger.trace("Unable to extract resource: {} from {}: {} could not create output directory: {}", resource, type, StringUtility.fileString(archive), StringUtility.fileString(output.getParentFile()));
            return false;
        }
        
        try {
            switch (type) {
                case ZIP:
                    return ZipArchive.extractResource(archive, resource, output);
                case JAR:
                    return JarArchive.extractResource(archive, resource, output);
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.trace("Unable to extract resource: {} from {}: {}", resource, type, StringUtility.fileString(archive));
            return false;
        }
    }
    
    /**
     * Extracts a directory from an archive to an external directory.
     *
     * @param archive         The archive file to extract from.
     * @param directory       The name of the directory to extract.
     * @param outputDirectory The directory to extract the directory to.
     * @return Whether the extraction was successful or not.
     * @see ZipArchive#extractDirectory(File, String, File)
     * @see JarArchive#extractDirectory(File, String, File)
     */
    public static boolean extractDirectory(File archive, String directory, File outputDirectory) {
        directory = directory.replaceAll("[\\\\/]", Matcher.quoteReplacement(ARCHIVE_PATH_SEPARATOR));
        directory = directory.replaceAll(ARCHIVE_PATH_SEPARATOR + '$', "");
        
        if ((archive == null) || !archive.exists()) {
            logger.trace("Unable to extract directory: {} from archive: {} archive does not exist", directory, ((archive == null) ? "null" : StringUtility.fixFileSeparators(archive.getAbsolutePath())));
            return false;
        }
        
        ArchiveType type = getArchiveType(archive);
        if (type == null) {
            logger.trace("Unable to extract directory: {} from archive: {} archive type is not supported", directory, StringUtility.fileString(archive));
            return false;
        }
        
        logger.trace("Extracting directory: {} from {}: {} to: {}", directory, type, StringUtility.fileString(archive), StringUtility.fileString(outputDirectory));
        
        if (!archive.exists()) {
            logger.trace("Unable to extract directory: {} from {}: {} {} does not exist", directory, type, StringUtility.fileString(archive), type);
            return false;
        }
        
        if (!outputDirectory.exists() && !Filesystem.createDirectory(outputDirectory)) {
            logger.trace("Unable to extract directory: {} from {}: {} could not create output directory: {}", directory, type, StringUtility.fileString(archive), StringUtility.fileString(outputDirectory));
            return false;
        }
        
        try {
            switch (type) {
                case ZIP:
                    return ZipArchive.extractDirectory(archive, directory, outputDirectory);
                case JAR:
                    return JarArchive.extractDirectory(archive, directory, outputDirectory);
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.trace("Unable to extract directory: {} from {}: {}", directory, type, StringUtility.fileString(archive));
            return false;
        }
    }
    
    /**
     * Extracts an archive to a directory.
     *
     * @param archive         The archive file to extract from.
     * @param outputDirectory The directory to extract the archive to.
     * @return Whether the extraction was successful or not.
     * @see ZipArchive#extract(File, File)
     * @see JarArchive#extract(File, File)
     */
    public static boolean extract(File archive, File outputDirectory) {
        if ((archive == null) || !archive.exists()) {
            logger.trace("Unable to extract archive: {} archive does not exist", ((archive == null) ? "null" : StringUtility.fixFileSeparators(archive.getAbsolutePath())));
            return false;
        }
        
        ArchiveType type = getArchiveType(archive);
        if (type == null) {
            logger.trace("Unable to extract archive: {} archive type is not supported", StringUtility.fileString(archive));
            return false;
        }
        
        logger.trace("Extracting {}: {} to: {}", type, StringUtility.fileString(archive), StringUtility.fileString(outputDirectory));
        
        if (!archive.exists()) {
            logger.trace("Unable to extract {}: {} {} does not exist", type, StringUtility.fileString(archive), type);
            return false;
        }
        
        if (!outputDirectory.exists() && !Filesystem.createDirectory(outputDirectory)) {
            logger.trace("Unable to extract {}: {} could not create output directory: {}", type, StringUtility.fileString(archive), StringUtility.fileString(outputDirectory));
            return false;
        }
        
        try {
            switch (type) {
                case ZIP:
                    return ZipArchive.extract(archive, outputDirectory);
                case JAR:
                    return JarArchive.extract(archive, outputDirectory);
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.trace("Unable to extract {}: {}", type, StringUtility.fileString(archive));
            return false;
        }
    }
    
    /**
     * Compiles an archive of a file.
     *
     * @param archive The archive file to produce.
     * @param method  The compression method to use when compiling the archive.
     * @param file    The file to compile.
     * @return Whether the compilation was successful or not.
     * @see ZipArchive#compileFile(File, CompressionMethod, File)
     * @see JarArchive#compileFile(File, CompressionMethod, File)
     */
    public static boolean compileFile(File archive, CompressionMethod method, File file) {
        if (!file.exists()) {
            logger.trace("Unable to compile archive: {} file: {} does not exist", StringUtility.fixFileSeparators(archive.getAbsolutePath()), StringUtility.fixFileSeparators(file.getAbsolutePath()));
            return false;
        }
        
        ArchiveType type = getArchiveType(archive);
        if (type == null) {
            logger.trace("Unable to compile archive: {} from: {} archive type is not supported", StringUtility.fileString(archive), StringUtility.fileString(file));
            return false;
        }
        
        logger.trace("Compiling {}: {} from: {}", type, StringUtility.fileString(archive), StringUtility.fileString(file));
        
        if (!file.exists()) {
            logger.trace("Unable to compile {}: {} file: {} does not exist", type, StringUtility.fileString(archive), StringUtility.fileString(file));
            return false;
        }
        
        try {
            switch (type) {
                case ZIP:
                    return ZipArchive.compileFile(archive, method, file);
                case JAR:
                    return JarArchive.compileFile(archive, method, file);
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.trace("Unable to compile {}: {}", type, StringUtility.fileString(archive));
            return false;
        }
    }
    
    /**
     * Compiles an archive of a file.
     *
     * @param archive The archive file to produce.
     * @param file    The file to compile.
     * @return Whether the compilation was successful or not.
     * @see #compileFile(File, CompressionMethod, File)
     */
    public static boolean compileFile(File archive, File file) {
        return compileFile(archive, CompressionMethod.STORE, file);
    }
    
    /**
     * Compiles an archive of a set of files.
     *
     * @param archive The archive file to produce.
     * @param method  The compression method to use when compiling the archive.
     * @param files   The set of files to compile.
     * @return Whether the compilation was successful or not.
     * @see ZipArchive#compileFiles(File, CompressionMethod, File...)
     * @see JarArchive#compileFiles(File, CompressionMethod, File...)
     */
    public static boolean compileFiles(File archive, CompressionMethod method, File... files) {
        for (File file : files) {
            if (!file.exists()) {
                logger.trace("Unable to compile archive: {} file: {} does not exist", StringUtility.fixFileSeparators(archive.getAbsolutePath()), StringUtility.fixFileSeparators(file.getAbsolutePath()));
                return false;
            }
        }
        
        ArchiveType type = getArchiveType(archive);
        if (type == null) {
            logger.trace("Unable to compile archive: {} from: {} archive type is not supported", StringUtility.fileString(archive), Arrays.stream(files).map(File::getAbsolutePath).collect(Collectors.joining(", ", "[", "]")));
            return false;
        }
        
        logger.trace("Compiling {}: {} from: {}", type, StringUtility.fileString(archive), Arrays.stream(files).map(File::getAbsolutePath).collect(Collectors.joining(", ", "[", "]")));
        
        for (File file : files) {
            if (!file.exists()) {
                logger.trace("Unable to compile {}: {} file: {} does not exist", type, StringUtility.fileString(archive), StringUtility.fileString(file));
                return false;
            }
        }
        
        try {
            switch (type) {
                case ZIP:
                    return ZipArchive.compileFiles(archive, method, files);
                case JAR:
                    return JarArchive.compileFiles(archive, method, files);
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.trace("Unable to compile {}: {}", type, StringUtility.fileString(archive));
            return false;
        }
    }
    
    /**
     * Compiles an archive of a set of files.
     *
     * @param archive The archive file to produce.
     * @param files   The set of files to compile.
     * @return Whether the compilation was successful or not.
     * @see #compileFiles(File, CompressionMethod, File...)
     */
    public static boolean compileFiles(File archive, File... files) {
        return compileFiles(archive, CompressionMethod.STORE, files);
    }
    
    /**
     * Compiles an archive from a directory.
     *
     * @param archive         The archive file to produce.
     * @param method          The compression method to use when compiling the archive.
     * @param sourceDirectory The directory to compile.
     * @param includeDir      If true it stores sourceDirectory in the archive, if false it stores the contents of sourceDirectory in the archive.
     * @return Whether the compilation was successful or not.
     * @see ZipArchive#compile(File, CompressionMethod, File)
     * @see JarArchive#compile(File, CompressionMethod, File)
     */
    public static boolean compile(File archive, CompressionMethod method, File sourceDirectory, boolean includeDir) {
        if (!sourceDirectory.exists()) {
            logger.trace("Unable to compile archive: {} source directory: {} does not exist", StringUtility.fixFileSeparators(archive.getAbsolutePath()), StringUtility.fixFileSeparators(sourceDirectory.getAbsolutePath()));
            return false;
        }
        
        ArchiveType type = getArchiveType(archive);
        if (type == null) {
            logger.trace("Unable to compile archive: {} from: {} archive type is not supported", StringUtility.fileString(archive), StringUtility.fileString(sourceDirectory));
            return false;
        }
        
        logger.trace("Compiling {}: {} from: {}", type, StringUtility.fileString(archive), StringUtility.fileString(sourceDirectory));
        
        if (!sourceDirectory.exists()) {
            logger.trace("Unable to compile {}: {} source directory: {} does not exist", type, StringUtility.fileString(archive), StringUtility.fileString(sourceDirectory));
            return false;
        }
        
        if (includeDir) {
            File tmpDir = Filesystem.createTemporaryDirectory();
            if (!Filesystem.copyDirectory(sourceDirectory, tmpDir, true, true)) {
                logger.trace("Unable to compile {}: {} could not copy source directory: {} to temporary directory", type, StringUtility.fileString(archive), StringUtility.fileString(sourceDirectory));
                return false;
            }
            sourceDirectory = tmpDir;
        }
        
        try {
            switch (type) {
                case ZIP:
                    return ZipArchive.compile(archive, method, sourceDirectory);
                case JAR:
                    return JarArchive.compile(archive, method, sourceDirectory);
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.trace("Unable to compile {}: {}", type, StringUtility.fileString(archive));
            return false;
            
        } finally {
            if (includeDir) {
                Filesystem.deleteDirectory(sourceDirectory);
            }
        }
    }
    
    /**
     * Compiles an archive from a directory.
     *
     * @param archive         The archive file to produce.
     * @param method          The compression method to use when compiling the archive.
     * @param sourceDirectory The directory to compile.
     * @return Whether the compilation was successful or not.
     * @see #compile(File, CompressionMethod, File, boolean)
     */
    public static boolean compile(File archive, CompressionMethod method, File sourceDirectory) {
        return compile(archive, method, sourceDirectory, false);
    }
    
    /**
     * Compiles an archive from a directory.
     *
     * @param archive         The archive file to produce.
     * @param sourceDirectory The directory to compile.
     * @param includeDir      If true it stores sourceDirectory in the archive, if false it stores the contents of sourceDirectory in the archive.
     * @return Whether the compilation was successful or not.
     * @see #compile(File, CompressionMethod, File, boolean)
     */
    public static boolean compile(File archive, File sourceDirectory, boolean includeDir) {
        return compile(archive, CompressionMethod.STORE, sourceDirectory, includeDir);
    }
    
    /**
     * Compiles an archive from a directory.
     *
     * @param archive         The archive file to produce.
     * @param sourceDirectory The directory to compile.
     * @return Whether the compilation was successful or not.
     * @see #compile(File, File, boolean)
     */
    public static boolean compile(File archive, File sourceDirectory) {
        return compile(archive, sourceDirectory, false);
    }
    
    /**
     * Compresses an archive.
     *
     * @param archive The archive.
     * @return Whether the compression was successful or not.
     * @see ZipArchive#compress(File)
     * @see JarArchive#compress(File)
     */
    public static boolean compress(File archive) {
        if (!archive.exists()) {
            logger.trace("Unable to compress archive: {} archive does not exist", StringUtility.fixFileSeparators(archive.getAbsolutePath()));
            return false;
        }
        
        ArchiveType type = getArchiveType(archive);
        if (type == null) {
            logger.trace("Unable to compress archive: {} archive type is not supported", StringUtility.fileString(archive));
            return false;
        }
        
        logger.trace("Compressing {}: {}", type, StringUtility.fileString(archive));
        
        if (!archive.exists()) {
            logger.trace("Unable to compress {}: {} {} does not exist", type, StringUtility.fileString(archive), type);
            return false;
        }
        
        Boolean compressed = isCompressed(archive);
        if (compressed != null && compressed) {
            logger.trace("{}: {} is already compressed", type, StringUtility.fileString(archive));
            return true;
        }
        
        File tmpDir = Filesystem.createTemporaryDirectory();
        if ((tmpDir.exists() && !Filesystem.clearDirectory(tmpDir)) || (!tmpDir.exists() && !Filesystem.createDirectory(tmpDir))) {
            logger.trace("Unable to compress {}: {} could not create temporary directory", type, StringUtility.fileString(archive));
            return false;
        }
        
        logger.trace("Extracting {}", type);
        if (!extract(archive, tmpDir)) {
            logger.trace("Unable to compress {}: {} could not extract {}", type, StringUtility.fileString(archive), type);
            return false;
        }
        
        File archiveBackup = new File(archive.getAbsolutePath().replace("." + type, "-bak." + type));
        if (!Filesystem.renameFile(archive, archiveBackup)) {
            logger.trace("Unable to compress {}: {} could not create backup of {}", type, StringUtility.fileString(archive), type);
            return false;
        }
        
        logger.trace("Compressing {}", type);
        if (compile(archive, CompressionMethod.COMPRESS, tmpDir)) {
            Filesystem.deleteFile(archiveBackup);
        } else {
            Filesystem.renameFile(archiveBackup, archive);
        }
        Filesystem.deleteDirectory(tmpDir);
        return true;
    }
    
    /**
     * Decompresses an archive.
     *
     * @param archive The archive.
     * @return Whether the decompression was successful or not.
     * @see ZipArchive#decompress(File)
     * @see JarArchive#decompress(File)
     */
    public static boolean decompress(File archive) {
        if (!archive.exists()) {
            logger.trace("Unable to decompress archive: {} archive does not exist", StringUtility.fixFileSeparators(archive.getAbsolutePath()));
            return false;
        }
        
        ArchiveType type = getArchiveType(archive);
        if (type == null) {
            logger.trace("Unable to decompress archive: {} archive type is not supported", StringUtility.fileString(archive));
            return false;
        }
        
        logger.trace("Decompressing {}: {}", type, StringUtility.fileString(archive));
        
        if (!archive.exists()) {
            logger.trace("Unable to decompress {}: {} {} does not exist", type, StringUtility.fileString(archive), type);
            return false;
        }
        
        Boolean decompressed = isDecompressed(archive);
        if (decompressed == null || decompressed) {
            logger.trace("{}: {} is already decompressed", type, StringUtility.fileString(archive));
            return true;
        }
        
        File tmpDir = Filesystem.createTemporaryDirectory();
        if ((tmpDir.exists() && !Filesystem.clearDirectory(tmpDir)) || (!tmpDir.exists() && !Filesystem.createDirectory(tmpDir))) {
            logger.trace("Unable to decompress {}: {} could not create temporary directory", type, StringUtility.fileString(archive));
            return false;
        }
        
        logger.trace("Extracting {}", type);
        if (!extract(archive, tmpDir)) {
            logger.trace("Unable to decompress {}: {} could not extract {}", type, StringUtility.fileString(archive), type);
            return false;
        }
        
        File archiveBackup = new File(archive.getAbsolutePath().replace("." + type, "-bak." + type));
        if (!Filesystem.renameFile(archive, archiveBackup)) {
            logger.trace("Unable to decompress {}: {} could not create backup of {}", type, StringUtility.fileString(archive), type);
            return false;
        }
        
        logger.trace("Decompressing {}", type);
        if (compile(archive, CompressionMethod.STORE, tmpDir)) {
            Filesystem.deleteFile(archiveBackup);
        } else {
            Filesystem.renameFile(archiveBackup, archive);
        }
        Filesystem.deleteDirectory(tmpDir);
        return true;
    }
    
    /**
     * Determines if a archive file was compiled using the Compress Method or not.
     *
     * @param archive The archive file.
     * @return Whether the archive file was compiled using the Compress Method or not, or null if there was an error.
     * @see #getCompressionMethod(File)
     */
    public static Boolean isCompressed(File archive) {
        CompressionMethod method = getCompressionMethod(archive);
        if (method == null) {
            return null;
        }
        return method == CompressionMethod.COMPRESS;
    }
    
    /**
     * Determines if an archive file was compiled using the Store Method or not.
     *
     * @param archive The archive file.
     * @return Whether the archive file was compiled using the Store Method or not, or null if there was an error.
     * @see #getCompressionMethod(File)
     */
    public static Boolean isDecompressed(File archive) {
        CompressionMethod method = getCompressionMethod(archive);
        if (method == null) {
            return null;
        }
        return method == CompressionMethod.STORE;
    }
    
    /**
     * Determines the Compression Method used to compile an archive file.
     *
     * @param archive The archive file.
     * @return The Compression Method used to compile the archive file, or null if there was an error.
     * @see ZipArchive#getCompressionMethod(File)
     * @see JarArchive#getCompressionMethod(File)
     */
    public static CompressionMethod getCompressionMethod(File archive) {
        ArchiveType type = getArchiveType(archive);
        if (type == null) {
            return null;
        }
        
        try {
            switch (type) {
                case ZIP:
                    return ZipArchive.getCompressionMethod(archive);
                case JAR:
                    return JarArchive.getCompressionMethod(archive);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Returns the Archive Type of an archive.
     *
     * @param archive The archive.
     * @return The Archive type of the archive, or null if it is not supported.
     */
    public static ArchiveType getArchiveType(File archive) {
        switch (StringUtility.rSnip(archive.getName().toLowerCase(), 4)) {
            case ".zip":
                return ArchiveType.ZIP;
            case ".jar":
                return ArchiveType.JAR;
            default:
                return null;
        }
    }
    
    /**
     * Creates a diff archive between two existing archive files.<br>
     * Files that exist in the target archive but not in the source archive are included in the diff archive.<br>
     * Files that exist in the target archive and the source archive but are modified in the target archive are included in the diff archive.<br>
     * Files that exist in the source archive but do not exist in the target archive are represented in the diff archive as an empty file or directory with a name that ends with a '~'.
     *
     * @param source The source archive.
     * @param target The target archive.
     * @param diff   The archive file to produce containing the differences.
     * @param method The compression method to use when compiling the diff archive.
     * @return Whether the diff archive was successfully calculated and compiled or not.
     * @see #extract(File, File)
     * @see #compile(File, CompressionMethod, File)
     */
    public static boolean createDiffArchive(File source, File target, File diff, CompressionMethod method) {
        if (!source.exists()) {
            logger.trace("Unable to create diff archive: {} source archive: {} does not exist", StringUtility.fileString(diff), StringUtility.fileString(source));
            return false;
        }
        
        if (!target.exists()) {
            logger.trace("Unable to create diff archive: {} target archive: {} does not exist", StringUtility.fileString(diff), StringUtility.fileString(target));
            return false;
        }
        
        ArchiveType sourceType = getArchiveType(source);
        ArchiveType targetType = getArchiveType(target);
        ArchiveType diffType = getArchiveType(diff);
        
        if (sourceType == null) {
            logger.trace("Unable to create diff archive: {} between: {} and: {} source archive type is not supported", StringUtility.fileString(diff), StringUtility.fileString(source), StringUtility.fileString(target));
            return false;
        }
        if (targetType == null) {
            logger.trace("Unable to create diff archive: {} between: {} and: {} target archive type is not supported", StringUtility.fileString(diff), StringUtility.fileString(source), StringUtility.fileString(target));
            return false;
        }
        if (diffType == null) {
            logger.trace("Unable to create diff archive: {} between: {} and: {} diff archive type is not supported", StringUtility.fileString(diff), StringUtility.fileString(source), StringUtility.fileString(target));
            return false;
        }
        if (!sourceType.equals(diffType) || !targetType.equals(diffType)) {
            logger.trace("Unable to create diff archive: {} between: {} and: {} archive types do not match", StringUtility.fileString(diff), StringUtility.fileString(source), StringUtility.fileString(target));
            return false;
        }
        
        logger.trace("Creating diff {}: {} between: {} and: {}", diffType, StringUtility.fileString(diff), StringUtility.fileString(source), StringUtility.fileString(target));
        
        File tmpDir = Filesystem.createTemporaryDirectory();
        if ((tmpDir.exists() && !Filesystem.clearDirectory(tmpDir)) || (!tmpDir.exists() && !Filesystem.createDirectory(tmpDir))) {
            logger.trace("Unable to create diff {}: {} could not create temporary directory", diffType, StringUtility.fileString(diff));
            return false;
        }
        
        File sourceTmpDir = new File(tmpDir, "source");
        if (!Filesystem.createDirectory(sourceTmpDir)) {
            logger.trace("Unable to create diff {}: {} could not create temporary directory for source {}", diffType, StringUtility.fileString(diff), diffType);
            return false;
        }
        
        File targetTmpDir = new File(tmpDir, "target");
        if (!Filesystem.createDirectory(targetTmpDir)) {
            logger.trace("Unable to create diff {}: {} could not create temporary directory for target {}", diffType, StringUtility.fileString(diff), diffType);
            return false;
        }
        
        File diffTmpDir = new File(tmpDir, "diff");
        if ((diffTmpDir.exists() && !Filesystem.clearDirectory(diffTmpDir)) || (!diffTmpDir.exists() && !Filesystem.createDirectory(diffTmpDir))) {
            logger.trace("Unable to create diff {}: {} could not create temporary directory for the {} diff", diffType, StringUtility.fileString(diff), diffType);
            return false;
        }
        
        logger.trace("Extracting source {}", sourceType);
        if (!extract(source, sourceTmpDir)) {
            logger.trace("Unable to create diff {}: {} could not extract the source {}: {}", diffType, StringUtility.fileString(diff), sourceType, StringUtility.fileString(source));
            return false;
        }
        
        logger.trace("Extracting target {}", targetType);
        if (!extract(target, targetTmpDir)) {
            logger.trace("Unable to create diff {}: {} could not extract the target {}: {}", diffType, StringUtility.fileString(diff), sourceType, StringUtility.fileString(source));
            return false;
        }
        
        logger.trace("Calculating diff {}", diffType);
        List<File> contentsSource = Filesystem.getFilesAndDirsRecursively(sourceTmpDir);
        List<File> contentsTarget = Filesystem.getFilesAndDirsRecursively(targetTmpDir);
        
        // insertions/modifications
        for (File contentTarget : contentsTarget) {
            String targetPath = contentTarget.getAbsolutePath().replace(targetTmpDir.getAbsolutePath(), "");
            File original = contentsSource.stream().filter(e -> e.getAbsolutePath().replace(sourceTmpDir.getAbsolutePath(), "").equals(targetPath)).findFirst().orElse(null);
            
            long checksumA = ((original == null) || original.isDirectory()) ? 0 : Filesystem.checksum(original);
            long checksumB = (contentTarget.isDirectory()) ? 0 : Filesystem.checksum(contentTarget);
            
            //if the file is new or it has been modified since the initial version, add it to the diff
            if ((original == null) || (checksumB != checksumA)) {
                String destPath = contentTarget.getAbsolutePath().replace(targetTmpDir.getAbsolutePath(), diffTmpDir.getAbsolutePath());
                File dest = new File(destPath);
                if (dest.isDirectory()) {
                    Filesystem.createDirectory(dest);
                } else {
                    Filesystem.copyFile(contentTarget, dest, true);
                }
            }
        }
        
        //deletions
        for (File contentSource : contentsSource) {
            String sourcePath = contentSource.getAbsolutePath().replace(sourceTmpDir.getAbsolutePath(), "");
            boolean exists = contentsTarget.stream().anyMatch(e -> e.getAbsolutePath().replace(targetTmpDir.getAbsolutePath(), "").equals(sourcePath));
            
            if (!exists) {
                String destPath = contentSource.getAbsolutePath().replace(sourceTmpDir.getAbsolutePath(), diffTmpDir.getAbsolutePath()) + '~';
                File dest = new File(destPath);
                if (dest.isDirectory()) {
                    //if files within the destination have already been registered as deleted, delete the old folder and just mark the directory as deleted
                    String truePath = StringUtility.rShear(destPath, 1);
                    File trueDest = new File(truePath);
                    if (trueDest.exists()) {
                        Filesystem.deleteDirectory(trueDest);
                    }
                    Filesystem.createDirectory(dest);
                    
                } else {
                    //if the files destination dir has already been marked as deleted then we don't need to specifically make the file as deleted
                    File trueDest = dest.getParentFile();
                    File deletedDest = new File(trueDest.getPath() + '~');
                    if (!deletedDest.exists()) {
                        Filesystem.createFile(dest);
                    }
                }
            }
        }
        
        boolean success = compile(diff, method, diffTmpDir);
        Filesystem.deleteDirectory(tmpDir);
        return success;
    }
    
    /**
     * Creates a diff archive between two existing archive files.<br>
     * Files that exist in the target archive but not in the source archive are included in the diff archive.<br>
     * Files that exist in the target archive and the source archive but are modified in the target archive are included in the diff archive.<br>
     * Files that exist in the source archive but do not exist in the target archive are represented in the diff archive as an empty file or directory with a name that ends with a '~'.
     *
     * @param source The source archive.
     * @param target The target archive.
     * @param diff   The archive file to produce containing the differences.
     * @return Whether the diff archive was successfully calculated and compiled or not.
     * @see #createDiffArchive(File, File, File, CompressionMethod)
     */
    public static boolean createDiffArchive(File source, File target, File diff) {
        return createDiffArchive(source, target, diff, CompressionMethod.STORE);
    }
    
    
    //Inner Classes
    
    /**
     * Provides base methods to classes that provide access to archives.
     */
    private static abstract class BaseArchive {
        
        //Static Methods
        
        /**
         * Writes an archive stream to a file.
         *
         * @param in     The archive stream.
         * @param output The file.
         * @throws Exception When there is an error.
         */
        protected static void fileStream(InputStream in, File output) throws Exception {
            try (FileOutputStream out = new FileOutputStream(output)) {
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        }
        
        /**
         * Writes a file to an archive stream.
         *
         * @param out   The archive stream.
         * @param input The file.
         * @throws Exception When there is an error.
         */
        protected static void archiveStream(OutputStream out, File input) throws Exception {
            try (FileInputStream in = new FileInputStream(input)) {
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                
            } finally {
                if (out instanceof ZipOutputStream) {
                    ((ZipOutputStream) out).closeEntry();
                }
            }
        }
        
        /**
         * Sets the archive entry properties for an entry to be inserted into an archive.
         *
         * @param entry  The archive entry.
         * @param file   The file.
         * @param method The compression method to be used.
         * @throws Exception When there is an error.
         */
        @SuppressWarnings("MagicConstant")
        protected static void setEntryProperties(ZipEntry entry, File file, CompressionMethod method) throws Exception {
            entry.setMethod(method.getLevel());
            entry.setSize(file.isDirectory() ? 0 : file.length());
            if (method == CompressionMethod.STORE) {
                if (file.isDirectory()) {
                    entry.setCompressedSize(0);
                    entry.setCrc(0);
                } else {
                    entry.setCompressedSize(file.length());
                    entry.setCrc(Filesystem.checksum(file));
                }
            }
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            entry.setCreationTime(attrs.creationTime());
            entry.setLastAccessTime(attrs.lastAccessTime());
            entry.setLastModifiedTime(attrs.lastModifiedTime());
        }
        
        /**
         * Sets the file properties for a file extracted from an archive.
         *
         * @param file  The file.
         * @param entry The archive entry for the file.
         * @throws Exception When there is an error.
         */
        protected static void setFileProperties(File file, ZipEntry entry) throws Exception {
            Files.setAttribute(file.toPath(), "basic:creationTime", entry.getCreationTime(), LinkOption.NOFOLLOW_LINKS);
            Files.setAttribute(file.toPath(), "basic:lastAccessTime", entry.getLastAccessTime(), LinkOption.NOFOLLOW_LINKS);
            Files.setAttribute(file.toPath(), "basic:lastModifiedTime", entry.getLastModifiedTime(), LinkOption.NOFOLLOW_LINKS);
        }
        
    }
    
    /**
     * Provides access to zip archives.
     */
    private static final class ZipArchive extends BaseArchive {
        
        //Static Methods
        
        /**
         * Extracts a resource from a zip to an external file.
         *
         * @param zip      The zip file to extract from.
         * @param resource The name of the resource to extract.
         * @param output   The file to extract the resource to.
         * @return Whether the extraction was successful or not.
         * @throws Exception When there is an error.
         */
        private static boolean extractResource(File zip, String resource, File output) throws Exception {
            try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
                
                ZipEntry zipEntry = in.getNextEntry();
                while (zipEntry != null) {
                    if (zipEntry.getName().equals(resource)) {
                        
                        fileStream(in, output);
                        setFileProperties(output, zipEntry);
                        
                        in.closeEntry();
                        return true;
                        
                    }
                    zipEntry = in.getNextEntry();
                }
                in.closeEntry();
            }
            
            logger.trace("Unable to extract resource: {} from zip: {} could not find resource: {}", resource, StringUtility.fileString(zip), resource);
            return false;
        }
        
        /**
         * Extracts a directory from a zip to an external directory.
         *
         * @param zip             The zip file to extract from.
         * @param directory       The name of the directory to extract.
         * @param outputDirectory The directory to extract the directory to.
         * @return Whether the extraction was successful or not.
         * @throws Exception When there is an error.
         */
        public static boolean extractDirectory(File zip, String directory, File outputDirectory) throws Exception {
            boolean hit = false;
            try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
                
                ZipEntry zipEntry = in.getNextEntry();
                while (zipEntry != null) {
                    if (zipEntry.getName().startsWith(directory + ARCHIVE_PATH_SEPARATOR)) {
                        File output = new File(outputDirectory, zipEntry.getName().replace(directory.substring(0, directory.lastIndexOf(ARCHIVE_PATH_SEPARATOR) + 1), ""));
                        
                        if (zipEntry.isDirectory()) {
                            if (!Filesystem.createDirectory(output)) {
                                logger.trace("Unable to extract zip: {} could not create output directory: {}", StringUtility.fileString(zip), StringUtility.fileString(output));
                                return false;
                            }
                        } else {
                            fileStream(in, output);
                        }
                        setFileProperties(output, zipEntry);
                        
                        in.closeEntry();
                        hit = true;
                    }
                    zipEntry = in.getNextEntry();
                }
                in.closeEntry();
            }
            
            if (!hit) {
                logger.trace("Unable to extract directory: {} from zip: {} could not find directory: {}", directory, StringUtility.fileString(zip), directory);
            }
            return hit;
        }
        
        /**
         * Extracts a zip to a directory.
         *
         * @param zip             The zip file to extract from.
         * @param outputDirectory The directory to extract the zip to.
         * @return Whether the extraction was successful or not.
         * @throws Exception When there is an error.
         */
        public static boolean extract(File zip, File outputDirectory) throws Exception {
            if (!outputDirectory.exists() && !Filesystem.createDirectory(outputDirectory)) {
                logger.trace("Unable to extract zip: {} output directory: {} does not exist and could not be created", StringUtility.fileString(zip), StringUtility.fileString(outputDirectory.getParentFile()));
                return false;
            }
            
            try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
                
                Map<File, ZipEntry> directoryEntries = new LinkedHashMap<>();
                ZipEntry zipEntry = in.getNextEntry();
                while (zipEntry != null) {
                    File output = new File(outputDirectory, zipEntry.getName());
                    if (!output.getParentFile().exists() && !Filesystem.createDirectory(output.getParentFile())) {
                        logger.trace("Unable to extract zip: {} output directory: {} does not exist and could not be created", StringUtility.fileString(zip), StringUtility.fileString(output.getParentFile()));
                        return false;
                    }
                    
                    if (zipEntry.isDirectory()) {
                        directoryEntries.put(output, zipEntry);
                        if (!output.exists() && !Filesystem.createDirectory(output)) {
                            logger.trace("Unable to extract zip: {} could not create output directory: {}", StringUtility.fileString(zip), StringUtility.fileString(output));
                            return false;
                        }
                    } else {
                        fileStream(in, output);
                        setFileProperties(output, zipEntry);
                    }
                    
                    zipEntry = in.getNextEntry();
                }
                in.closeEntry();
                
                List<File> directories = new ArrayList<>(directoryEntries.keySet());
                Collections.reverse(directories);
                for (File directory : directories) {
                    setFileProperties(directory, directoryEntries.get(directory));
                }
            }
            
            return true;
        }
        
        /**
         * Compiles a zip of a file.
         *
         * @param zip    The zip file to produce.
         * @param method The compression method to use when compiling the zip.
         * @param file   The file to compile.
         * @return Whether the compilation was successful or not.
         * @throws Exception When there is an error.
         */
        public static boolean compileFile(File zip, CompressionMethod method, File file) throws Exception {
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
                
                if (file.isHidden()) {
                    return true;
                }
                if (file.isDirectory()) {
                    logger.trace("Unable to compile zip: {} file: {} is a directory", StringUtility.fileString(zip), StringUtility.fileString(file));
                    return false;
                }
                
                ZipEntry zipEntry = createEntry(file, method);
                out.putNextEntry(zipEntry);
                
                archiveStream(out, file);
                return true;
            }
        }
        
        /**
         * Compiles a zip of a set of files.
         *
         * @param zip    The zip file to produce.
         * @param method The compression method to use when compiling the zip.
         * @param files  The set of files to compile.
         * @return Whether the compilation was successful or not.
         * @throws Exception When there is an error.
         */
        public static boolean compileFiles(File zip, CompressionMethod method, File... files) throws Exception {
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
                
                for (File file : files) {
                    if (file.isHidden()) {
                        continue;
                    }
                    if (file.isDirectory()) {
                        logger.trace("Unable to compile zip: {} file: {} is a directory", StringUtility.fileString(zip), StringUtility.fileString(file));
                        return false;
                    }
                    
                    ZipEntry zipEntry = createEntry(file, method);
                    out.putNextEntry(zipEntry);
                    
                    archiveStream(out, file);
                }
                return true;
            }
        }
        
        /**
         * Compiles a zip from a directory.
         *
         * @param zip             The zip file to produce.
         * @param method          The compression method to use when compiling the zip.
         * @param sourceDirectory The directory to compile.
         * @return Whether the compilation was successful or not.
         * @throws Exception When there is an error.
         * @see #compileContents(ZipOutputStream, CompressionMethod, String, String)
         */
        public static boolean compile(File zip, CompressionMethod method, File sourceDirectory) throws Exception {
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
                
                compileContents(out, method, sourceDirectory.getAbsolutePath(), null);
                return true;
            }
        }
        
        /**
         * Adds the contents to the zip during compilation.
         *
         * @param out              The output stream to the zip file being produced.
         * @param method           The compression method to use when compiling the zip.
         * @param sourceDirectory  The directory to compile.
         * @param currentDirectory The current directory being compiled.
         * @throws Exception When there is an error.
         */
        private static void compileContents(ZipOutputStream out, CompressionMethod method, String sourceDirectory, String currentDirectory) throws Exception {
            File directory = new File(sourceDirectory);
            if (currentDirectory == null) {
                currentDirectory = directory.getAbsolutePath();
            }
            
            for (File file : Filesystem.getFilesAndDirs(directory)) {
                String resourcePath = file.getPath().substring(currentDirectory.length() + 1);
                String resource = resourcePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(ARCHIVE_PATH_SEPARATOR));
                
                if (file.isHidden()) {
                    continue;
                }
                
                if (file.isFile()) {
                    ZipEntry zipEntry = createEntry(resource, file, method);
                    out.putNextEntry(zipEntry);
                    
                    archiveStream(out, file);
                    
                } else if (file.isDirectory()) {
                    if (!resource.isEmpty()) {
                        ZipEntry zipEntry = createEntry(resource + (resource.endsWith(ARCHIVE_PATH_SEPARATOR) ? "" : ARCHIVE_PATH_SEPARATOR), file, method);
                        out.putNextEntry(zipEntry);
                        out.closeEntry();
                    }
                    
                    compileContents(out, method, file.getAbsolutePath(), currentDirectory);
                }
            }
        }
        
        /**
         * Determines the Compression Method used to compile a zip file.
         *
         * @param zip The zip file.
         * @return The Compression Method used to compile the zip file, or null if there was an error.
         * @throws Exception When there is an error.
         */
        public static CompressionMethod getCompressionMethod(File zip) throws Exception {
            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zip))) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                while (zipEntry != null) {
                    if (!zipEntry.isDirectory()) {
                        
                        zipInputStream.closeEntry();
                        switch (zipEntry.getMethod()) {
                            case ZipEntry.STORED:
                                return CompressionMethod.STORE;
                            case ZipEntry.DEFLATED:
                                return CompressionMethod.COMPRESS;
                            default:
                                return null;
                        }
                        
                    }
                    zipEntry = zipInputStream.getNextEntry();
                }
                zipInputStream.closeEntry();
            }
            return null;
        }
        
        /**
         * Creates a zip entry for a file.
         *
         * @param resource The resource.
         * @param file     The file.
         * @param method   The compression method to be used.
         * @return The zip entry.
         * @throws Exception When there is an error.
         * @see #setEntryProperties(ZipEntry, File, CompressionMethod)
         */
        private static ZipEntry createEntry(String resource, File file, CompressionMethod method) throws Exception {
            ZipEntry entry = new ZipEntry(resource);
            setEntryProperties(entry, file, method);
            return entry;
        }
        
        /**
         * Creates a zip entry for a file.
         *
         * @param file   The file.
         * @param method The compression method to be used.
         * @return The zip entry.
         * @throws Exception When there is an error.
         * @see #createEntry(String, File, CompressionMethod)
         */
        private static ZipEntry createEntry(File file, CompressionMethod method) throws Exception {
            return createEntry(file.getName(), file, method);
        }
        
    }
    
    /**
     * Provides access to jar archives.
     */
    private static final class JarArchive extends BaseArchive {
        
        //Static Methods
        
        /**
         * Extracts a resource from a jar to an external file.
         *
         * @param jar      The jar file to extract from.
         * @param resource The name of the resource to extract.
         * @param output   The file to extract the resource to.
         * @return Whether the extraction was successful or not.
         * @throws Exception When there is an error.
         */
        private static boolean extractResource(File jar, String resource, File output) throws Exception {
            try (JarFile jarFile = new JarFile(jar)) {
                
                JarEntry jarEntry = jarFile.getJarEntry(resource);
                if (jarEntry == null) {
                    logger.trace("Unable to extract resource: {} from jar: {} could not find resource: {}", resource, StringUtility.fileString(jar), resource);
                    return false;
                }
                
                fileStreamJar(jarFile, jarEntry, output);
                setFileProperties(output, jarEntry);
            }
            
            return true;
        }
        
        /**
         * Extracts a directory from a jar to an external directory.
         *
         * @param jar             The jar file to extract from.
         * @param directory       The name of the directory to extract.
         * @param outputDirectory The directory to extract the directory to.
         * @return Whether the extraction was successful or not.
         * @throws Exception When there is an error.
         */
        public static boolean extractDirectory(File jar, String directory, File outputDirectory) throws Exception {
            try (JarFile jarFile = new JarFile(jar)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                
                boolean hit = false;
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (jarEntry.getName().startsWith(directory + ARCHIVE_PATH_SEPARATOR)) {
                        File output = new File(outputDirectory, jarEntry.getName().replace(directory.substring(0, directory.lastIndexOf(ARCHIVE_PATH_SEPARATOR) + 1), ""));
                        
                        if (jarEntry.isDirectory()) {
                            if (!Filesystem.createDirectory(output)) {
                                logger.trace("Unable to extract directory: {} from jar: {} could not create output directory: {}", directory, StringUtility.fileString(jar), StringUtility.fileString(output));
                                return false;
                            }
                        } else {
                            fileStreamJar(jarFile, jarEntry, output);
                        }
                        setFileProperties(output, jarEntry);
                        
                        hit = true;
                    }
                }
                
                if (!hit) {
                    logger.trace("Unable to extract directory: {} from jar: {} could not find directory: {}", directory, StringUtility.fileString(jar), directory);
                }
                return hit;
            }
        }
        
        /**
         * Extracts a jar to a directory.
         *
         * @param jar             The jar file to extract from.
         * @param outputDirectory The directory to extract the jar to.
         * @return Whether the extraction was successful or not.
         * @throws Exception When there is an error.
         */
        public static boolean extract(File jar, File outputDirectory) throws Exception {
            if (!outputDirectory.exists() && !Filesystem.createDirectory(outputDirectory)) {
                logger.trace("Unable to extract jar: {} output directory: {} does not exist and could not be created", StringUtility.fileString(jar), StringUtility.fileString(outputDirectory.getParentFile()));
                return false;
            }
            
            try (JarFile jarFile = new JarFile(jar)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                
                Filesystem.createDirectory(new File(outputDirectory, "META-INF"));
                Map<File, JarEntry> directoryEntries = new LinkedHashMap<>();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    File output = new File(outputDirectory, jarEntry.getName());
                    if (!output.getParentFile().exists() && !Filesystem.createDirectory(output.getParentFile())) {
                        logger.trace("Unable to extract jar: {} output directory: {} does not exist and could not be created", StringUtility.fileString(jar), StringUtility.fileString(output.getParentFile()));
                        return false;
                    }
                    
                    if (jarEntry.isDirectory()) {
                        directoryEntries.put(output, jarEntry);
                        if (!output.exists() && !Filesystem.createDirectory(output)) {
                            logger.trace("Unable to extract jar: {} could not create output directory: {}", StringUtility.fileString(jar), StringUtility.fileString(output));
                            return false;
                        }
                    } else {
                        fileStreamJar(jarFile, jarEntry, output);
                        setFileProperties(output, jarEntry);
                    }
                }
                
                List<File> directories = new ArrayList<>(directoryEntries.keySet());
                Collections.reverse(directories);
                for (File directory : directories) {
                    setFileProperties(directory, directoryEntries.get(directory));
                }
                
                return true;
            }
        }
        
        /**
         * Compiles a jar from a file.
         *
         * @param jar    The jar file to produce.
         * @param method The compression method to use when compiling the jar.
         * @param file   The file to compile.
         * @return Whether the compilation was successful or not.
         * @throws Exception When there is an error.
         */
        public static boolean compileFile(File jar, CompressionMethod method, File file) throws Exception {
            try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))) {
                
                if (file.isHidden()) {
                    return true;
                }
                if (file.isDirectory()) {
                    logger.trace("Unable to compile jar: {} file: {} is a directory", StringUtility.fileString(jar), StringUtility.fileString(file));
                    return false;
                }
                
                JarEntry jarEntry = createEntry(file, method);
                out.putNextEntry(jarEntry);
                
                archiveStream(out, file);
                return true;
            }
        }
        
        /**
         * Compiles a jar from a set of files.
         *
         * @param jar    The jar file to produce.
         * @param method The compression method to use when compiling the jar.
         * @param files  The set of files to compile.
         * @return Whether the compilation was successful or not.
         * @throws Exception When there is an error.
         */
        public static boolean compileFiles(File jar, CompressionMethod method, File... files) throws Exception {
            try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))) {
                
                for (File file : files) {
                    if (file.isHidden()) {
                        continue;
                    }
                    if (file.isDirectory()) {
                        logger.trace("Unable to compile jar: {} file: {} is a directory", StringUtility.fileString(jar), StringUtility.fileString(file));
                        return false;
                    }
                    
                    JarEntry jarEntry = createEntry(file, method);
                    out.putNextEntry(jarEntry);
                    
                    archiveStream(out, file);
                }
                return true;
            }
        }
        
        /**
         * Compiles a jar from a directory.
         *
         * @param jar             The jar file to produce.
         * @param method          The compression method to use when compiling the jar.
         * @param sourceDirectory The directory to compile.
         * @return Whether the compilation was successful or not.
         * @throws Exception When there is an error.
         * @see #compileContents(JarOutputStream, CompressionMethod, String, String)
         */
        public static boolean compile(File jar, CompressionMethod method, File sourceDirectory) throws Exception {
            try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))) {
                
                compileContents(out, method, sourceDirectory.getAbsolutePath(), null);
                return true;
            }
        }
        
        /**
         * Adds the contents to a jar during compilation.
         *
         * @param out              The output stream to the jar file being produced.
         * @param method           The compression method to use when compiling the jar.
         * @param sourceDirectory  The directory to compile.
         * @param currentDirectory The current directory being compiled.
         * @throws Exception When there is an error.
         */
        private static void compileContents(JarOutputStream out, CompressionMethod method, String sourceDirectory, String currentDirectory) throws Exception {
            File directory = new File(sourceDirectory);
            if (currentDirectory == null) {
                currentDirectory = directory.getAbsolutePath();
            }
            
            for (File file : Filesystem.getFilesAndDirs(directory)) {
                String resourcePath = file.getPath().substring(currentDirectory.length() + 1);
                String resource = resourcePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(ARCHIVE_PATH_SEPARATOR));
                
                if (file.isHidden()) {
                    continue;
                }
                
                if (file.isFile()) {
                    JarEntry jarEntry = createEntry(resource, file, method);
                    out.putNextEntry(jarEntry);
                    
                    archiveStream(out, file);
                    
                } else if (file.isDirectory()) {
                    if (!resource.isEmpty()) {
                        JarEntry jarEntry = createEntry(resource + (resource.endsWith(ARCHIVE_PATH_SEPARATOR) ? "" : ARCHIVE_PATH_SEPARATOR), file, method);
                        out.putNextEntry(jarEntry);
                        out.closeEntry();
                    }
                    
                    compileContents(out, method, file.getAbsolutePath(), currentDirectory);
                }
            }
        }
        
        /**
         * Determines the Compression Method used to compile a jar file.
         *
         * @param jar The jar file.
         * @return The Compression Method used to compile the jar file, or null if there was an error.
         * @throws Exception When there is an error.
         */
        public static CompressionMethod getCompressionMethod(File jar) throws Exception {
            try (JarFile jarFile = new JarFile(jar)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (!jarEntry.isDirectory()) {
                        
                        switch (jarEntry.getMethod()) {
                            case ZipEntry.STORED:
                                return CompressionMethod.STORE;
                            case ZipEntry.DEFLATED:
                                return CompressionMethod.COMPRESS;
                            default:
                                return null;
                        }
                        
                    }
                }
                return null;
            }
        }
        
        /**
         * Writes a jar stream to a file.
         *
         * @param jarFile  The jar file.
         * @param jarEntry The jar entry to read from.
         * @param output   The file to write to.
         * @throws Exception When there is an error.
         * @see #fileStream(InputStream, File)
         */
        private static void fileStreamJar(JarFile jarFile, JarEntry jarEntry, File output) throws Exception {
            try (InputStream in = jarFile.getInputStream(jarEntry)) {
                fileStream(in, output);
            }
        }
        
        /**
         * Creates a jar entry for a file.
         *
         * @param resource The resource.
         * @param file     The file.
         * @param method   The compression method to be used.
         * @return The jar entry.
         * @throws Exception When there is an error.
         * @see #setEntryProperties(ZipEntry, File, CompressionMethod)
         */
        private static JarEntry createEntry(String resource, File file, CompressionMethod method) throws Exception {
            JarEntry entry = new JarEntry(resource);
            setEntryProperties(entry, file, method);
            return entry;
        }
        
        /**
         * Creates a jar entry for a file.
         *
         * @param file   The file.
         * @param method The compression method to be used.
         * @return The jar entry.
         * @throws Exception When there is an error.
         * @see #createEntry(String, File, CompressionMethod)
         */
        private static JarEntry createEntry(File file, CompressionMethod method) throws Exception {
            return createEntry(file.getName(), file, method);
        }
        
    }
    
}
