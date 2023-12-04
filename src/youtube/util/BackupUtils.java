/*
 * File:    BackupUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.access.Project;
import commons.io.file.ArchiveUtility;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.config.Color;
import youtube.config.Configurator;

/**
 * Provides backup utility methods for the Youtube Downloader.
 */
public final class BackupUtils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(BackupUtils.class);
    
    
    //Constants
    
    /**
     * The root backup key used in backup names.
     */
    public static final String ROOT_BACKUP_KEY = "Backup";
    
    
    //Static Fields
    
    /**
     * A flag indicating whether the backup system has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Static Methods
    
    /**
     * Initializes the backup system.
     *
     * @return Whether the backup system was successfully initialized.
     */
    public static boolean initBackup() {
        if (loaded.compareAndSet(false, true)) {
            logger.trace(LogUtils.NEWLINE);
            logger.debug(Color.log("Initializing Backup System..."));
            
            Config.init();
            
            createBackup();
            Runtime.getRuntime().addShutdownHook(new Thread(BackupUtils::shutdownBackup));
            
            return true;
        }
        return false;
    }
    
    /**
     * Shuts down the backup configuration.
     */
    private static void shutdownBackup() {
        if (loaded.get()) {
            cleanupOldBackups();
        }
    }
    
    /**
     * Creates a backup.
     *
     * @param force Whether to skip backup frequency checks and force a backup to be created.
     * @return The created backup file, or null if a backup was not created.
     */
    public static File createBackup(boolean force) {
        if (!Config.enableBackups) {
            return null;
        }
        
        logger.debug(Color.log("Starting backup..."));
        
        if (!force && Optional.ofNullable(Config.daysBetweenBackups).orElse(-1L) > 0) {
            final List<File> recentBackups = fetchAllBackupsNewerThan(Config.daysBetweenBackups.intValue());
            if (!recentBackups.isEmpty()) {
                logger.trace(Color.log("Skipping backup; a recent backup already exists: ") +
                        Color.quoteFileName(recentBackups.get(recentBackups.size() - 1)));
                return null;
            }
        }
        
        final File backup = new File(Config.backupDir, Stream.of(
                        Utils.PROJECT_TITLE, DateUtils.datestamp(), ROOT_BACKUP_KEY,
                        Optional.of(DateUtils.datestamp()).map(BackupUtils::fetchAllBackups)
                                .filter(dayBackups -> !dayBackups.isEmpty()).map(dayBackups -> (dayBackups.size() + 1))
                                .map(String::valueOf).orElse(""))
                .filter(e -> !StringUtility.isNullOrBlank(e))
                .collect(Collectors.joining("-")));
        
        logger.debug(Color.log("Creating backup: ") + Color.quoteFileName(backup));
        
        final boolean success = Filesystem.createDirectory(backup) &&
                !ListUtility.isNullOrEmpty(Stream.of(
                                Config.includeConfigs ? Configurator.CONF_FILE : null,
                                Config.includeConfigs ? Channels.CHANNELS_FILE : null,
                                Config.includeData ? Project.DATA_DIR : null,
                                Config.includeLogs ? LogUtils.LOG_DIR : null,
                                Config.includeSourceCode ? Project.SOURCE_DIR : null,
                                Config.includeCompiledSource ? Project.OUTPUT_DIR : null,
                                Config.includeApiKey ? ApiUtils.API_KEY_FILE : null,
                                Config.includeExecutable ? ExecutableUtils.Config.executable.getExe() : null)
                        .filter(Objects::nonNull)
                        .filter(include -> Optional.of(include)
                                .map(fileSource -> new File(backup, fileSource.getName()))
                                .map(fileBackup -> Filesystem.copy(include, fileBackup))
                                .filter(e -> e).orElseGet(() -> {
                                    logger.warn(Color.bad("Failed to backup: ") + Color.quoteFileName(include));
                                    return false;
                                }))
                        .collect(Collectors.toList()));
        
        if (!success) {
            logger.warn(Color.bad("Failed to create backup: ") + Color.quoteFileName(backup));
            return null;
        }
        
        if (Config.compressBackups) {
            final File backupArchive = new File(Config.backupDir,
                    FileUtils.setFormat(backup.getName(), FileUtils.ARCHIVE_FILE_FORMAT));
            
            logger.debug(Color.log("Compressing backup: ") + Color.quoteFileName(backupArchive));
            
            if (ArchiveUtility.compile(backupArchive, ArchiveUtility.CompressionMethod.COMPRESS, backup)) {
                Filesystem.deleteDirectory(backup);
                return backupArchive;
            } else {
                logger.warn(Color.bad("Failed to compress backup: ") + Color.quoteFileName(backupArchive));
                Filesystem.deleteFile(backupArchive);
            }
        }
        
        return backup;
    }
    
    /**
     * Creates a backup.
     *
     * @return The created backup file, or null if a backup was not created.
     */
    public static File createBackup() {
        return createBackup(false);
    }
    
    /**
     * Returns the backup files present in the backup directory.
     *
     * @return The backup files present in the backup directory.
     */
    public static List<File> fetchAllBackups() {
        return Filesystem.getFilesAndDirs(Config.backupDir);
    }
    
    /**
     * Returns the backup files with a certain backup key.
     *
     * @param backupKey The backup key.
     * @return The backup files with the specified backup key.
     */
    public static List<File> fetchAllBackups(String backupKey) {
        return fetchAllBackups().stream()
                .filter(backup -> (StringUtility.isNullOrBlank(backupKey) || backup.getName().contains("-" + backupKey)))
                .collect(Collectors.toList());
    }
    
    /**
     * Returns the backup files from a certain date.
     *
     * @param date The date.
     * @return The backup files from the specified date.
     */
    public static List<File> fetchAllBackups(Date date) {
        return Optional.ofNullable(date).map(DateUtils::datestamp)
                .map(BackupUtils::fetchAllBackups)
                .orElse(Collections.emptyList());
    }
    
    /**
     * Returns the backup files grouped by date.
     *
     * @return The backup files grouped by date.
     */
    public static Map<Date, List<File>> fetchAllBackupsByDate() {
        return fetchAllBackups().stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(BackupUtils::getBackupDate),
                        backupDateMap -> backupDateMap.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .collect(MapCollectors.toLinkedHashMap())));
    }
    
    /**
     * Returns the backup files filtered by date.
     *
     * @param filter The date filter.
     * @return The backup files that passed the date filter.
     */
    private static List<File> filterBackupsByDate(Predicate<Date> filter) {
        return fetchAllBackupsByDate().entrySet().stream()
                .filter(e -> filter.test(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    
    /**
     * Returns the backup files from before a date.
     *
     * @param date The date.
     * @return The backup files from before the specified date.
     */
    public static List<File> fetchAllBackupsBefore(Date date) {
        return filterBackupsByDate(d -> d.before(date));
    }
    
    /**
     * Returns the backup files from after a date.
     *
     * @param date The date.
     * @return The backup files from after the specified date.
     */
    public static List<File> fetchAllBackupsAfter(Date date) {
        return filterBackupsByDate(d -> d.after(date));
    }
    
    /**
     * Returns the backup files from on or before a date.
     *
     * @param date The date.
     * @return The backup files from on or before the specified date.
     */
    public static List<File> fetchAllBackupsOnOrBefore(Date date) {
        return filterBackupsByDate(d -> !d.after(date));
    }
    
    /**
     * Returns the backup files from on or after a date.
     *
     * @param date The date.
     * @return The backup files from on or after the specified date.
     */
    public static List<File> fetchAllBackupsOnOrAfter(Date date) {
        return filterBackupsByDate(d -> !d.before(date));
    }
    
    /**
     * Returns the backup files from between two dates.
     *
     * @param startDate The start date.
     * @param endDate   The end date.
     * @return The backup files from between the specified start and end date.
     */
    public static List<File> fetchAllBackupsBetween(Date startDate, Date endDate) {
        return filterBackupsByDate(d -> (!d.before(startDate) && !d.after(endDate)));
    }
    
    /**
     * Returns the backup files newer than a certain number of days.
     *
     * @param days The number of days.
     * @return The backup files newer than the specified number of days.
     */
    public static List<File> fetchAllBackupsNewerThan(int days) {
        return fetchAllBackupsOnOrAfter(new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(days)));
    }
    
    /**
     * Returns the backup files older than a certain number of days.
     *
     * @param days The number of days.
     * @return The backup files older than the specified number of days.
     */
    public static List<File> fetchAllBackupsOlderThan(int days) {
        return fetchAllBackupsOnOrBefore(new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(days)));
    }
    
    /**
     * Returns the associated date of a backup file.
     *
     * @param backupFile The backup file.
     * @return The associated date or the backup file, or a dummy date if the file name does not include a datestamp.
     */
    public static Date getBackupDate(File backupFile) {
        return Optional.ofNullable(backupFile)
                .map(FileUtils::getStampedFileDate)
                .orElseGet(() -> new Date(0));
    }
    
    /**
     * Purges old backup files.
     */
    private static void cleanupOldBackups() {
        Optional.ofNullable(Config.daysToKeepBackups).map(Long::intValue)
                .filter(daysToKeepBackups -> (daysToKeepBackups >= 0))
                .map(BackupUtils::fetchAllBackupsOlderThan)
                .stream().flatMap(Collection::stream)
                .forEach(FileUtils::delete);
        
        Filesystem.getDirs(Config.backupDir).stream()
                .filter(Filesystem::directoryIsEmpty)
                .forEach(FileUtils::delete);
    }
    
    
    //Inner Classes
    
    /**
     * Holds the backup Config.
     */
    public static class Config {
        
        //Constants
        
        /**
         * The default value of the flag indicating whether to enable backups or not.
         */
        public static final boolean DEFAULT_ENABLE_BACKUPS = false;
        
        /**
         * The default backup directory to store project state backups.
         */
        public static final String DEFAULT_BACKUP_DIR = "backup/";
        
        /**
         * The default value of the flag indicating whether to compress backups or not.
         */
        public static final boolean DEFAULT_COMPRESS_BACKUPS = true;
        
        /**
         * The default value of the setting indicating the number of days between project state backups.
         */
        public static final long DEFAULT_DAYS_BETWEEN_BACKUPS = 7;
        
        /**
         * The default value of the setting indicating the number of days to retain project state backups before deleting them.
         */
        public static final long DEFAULT_DAYS_TO_KEEP_BACKUPS = 30;
        
        /**
         * The default value of the flag indicating whether to include configuration files in a backup.
         */
        public static boolean DEFAULT_INCLUDE_CONFIGS = true;
        
        /**
         * The default value of the flag indicating whether to include data files in a backup.
         */
        public static boolean DEFAULT_INCLUDE_DATA = true;
        
        /**
         * The default value of the flag indicating whether to include logs in a backup.
         */
        public static boolean DEFAULT_INCLUDE_LOGS = true;
        
        /**
         * The default value of the flag indicating whether to include source code in a backup.
         */
        public static boolean DEFAULT_INCLUDE_SOURCE_CODE = false;
        
        /**
         * The default value of the flag indicating whether to include compiled source code in a backup.
         */
        public static boolean DEFAULT_INCLUDE_COMPILED_SOURCE = false;
        
        /**
         * The default value of the flag indicating whether to include the api key in a backup.
         */
        public static boolean DEFAULT_INCLUDE_API_KEY = false;
        
        /**
         * The default value of the flag indicating whether to include the executable files in a backup.
         */
        public static boolean DEFAULT_INCLUDE_EXECUTABLE = false;
        
        
        //Static Fields
        
        /**
         * A flag indicating whether to enable backups or not.
         */
        public static boolean enableBackups = DEFAULT_ENABLE_BACKUPS;
        
        /**
         * The backup directory to store project state backups.
         */
        public static File backupDir = new File(DEFAULT_BACKUP_DIR);
        
        /**
         * A flag indicating whether to compress backups or not.
         */
        public static boolean compressBackups = DEFAULT_COMPRESS_BACKUPS;
        
        /**
         * The number of days days between project state backups, or -1 to backup before every run.
         */
        public static Long daysBetweenBackups = DEFAULT_DAYS_BETWEEN_BACKUPS;
        
        /**
         * The number of days to retain project state backups before deleting them, or -1 to retain backups indefinitely.
         */
        public static Long daysToKeepBackups = DEFAULT_DAYS_TO_KEEP_BACKUPS;
        
        /**
         * The default value of the flag indicating whether to include configuration files in a backup.
         */
        public static boolean includeConfigs = DEFAULT_INCLUDE_CONFIGS;
        
        /**
         * The default value of the flag indicating whether to include data files in a backup.
         */
        public static boolean includeData = DEFAULT_INCLUDE_DATA;
        
        /**
         * The default value of the flag indicating whether to include logs in a backup.
         */
        public static boolean includeLogs = DEFAULT_INCLUDE_LOGS;
        
        /**
         * The default value of the flag indicating whether to include source code in a backup.
         */
        public static boolean includeSourceCode = DEFAULT_INCLUDE_SOURCE_CODE;
        
        /**
         * The default value of the flag indicating whether to include compiled source code in a backup.
         */
        public static boolean includeCompiledSource = DEFAULT_INCLUDE_COMPILED_SOURCE;
        
        /**
         * The default value of the flag indicating whether to include the api key in a backup.
         */
        public static boolean includeApiKey = DEFAULT_INCLUDE_API_KEY;
        
        /**
         * The default value of the flag indicating whether to include the executable files in a backup.
         */
        public static boolean includeExecutable = DEFAULT_INCLUDE_EXECUTABLE;
        
        
        //Static Methods
        
        /**
         * Initializes the Config.
         */
        private static void init() {
            enableBackups = Configurator.getSetting(List.of(
                            "enableBackups",
                            "flag.enableBackups",
                            "backup.enableBackups",
                            "backup.enable"),
                    DEFAULT_ENABLE_BACKUPS);
            
            backupDir = new File(Configurator.getSetting(List.of(
                            "backupDir",
                            "backup.backupDir",
                            "location.backupDir"),
                    DEFAULT_BACKUP_DIR));
            
            compressBackups = Configurator.getSetting(List.of(
                            "compressBackups",
                            "flag.compressBackups",
                            "backup.compressBackups",
                            "backup.compress"),
                    DEFAULT_COMPRESS_BACKUPS);
            
            daysBetweenBackups = Configurator.getSetting(List.of(
                            "daysBetweenBackups",
                            "flag.daysBetweenBackups",
                            "backup.daysBetweenBackups"),
                    DEFAULT_DAYS_BETWEEN_BACKUPS);
            daysToKeepBackups = Configurator.getSetting(List.of(
                            "daysToKeepBackups",
                            "flag.daysToKeepBackups",
                            "backup.daysToKeepBackups"),
                    DEFAULT_DAYS_TO_KEEP_BACKUPS);
            
            includeConfigs = Configurator.getSetting(List.of(
                            "includeConfigsInBackup",
                            "flag.includeConfigsInBackup",
                            "backup.includeConfigs",
                            "backup.files.includeConfigs",
                            "backup.files.configs"),
                    DEFAULT_INCLUDE_CONFIGS);
            includeData = Configurator.getSetting(List.of(
                            "includeDataInBackup",
                            "flag.includeDataInBackup",
                            "backup.includeData",
                            "backup.files.includeData",
                            "backup.files.data"),
                    DEFAULT_INCLUDE_DATA);
            includeLogs = Configurator.getSetting(List.of(
                            "includeLogsInBackup",
                            "flag.includeLogsInBackup",
                            "backup.includeLogs",
                            "backup.files.includeLogs",
                            "backup.files.logs"),
                    DEFAULT_INCLUDE_LOGS);
            includeSourceCode = Configurator.getSetting(List.of(
                            "includeSourceCodeInBackup",
                            "flag.includeSourceCodeInBackup",
                            "backup.includeSourceCode",
                            "backup.files.includeSourceCode",
                            "backup.files.sourceCode"),
                    DEFAULT_INCLUDE_SOURCE_CODE);
            includeCompiledSource = Configurator.getSetting(List.of(
                            "includeCompiledSourceInBackup",
                            "flag.includeCompiledSourceInBackup",
                            "backup.includeCompiledSource",
                            "backup.files.includeCompiledSource",
                            "backup.files.compiledSource"),
                    DEFAULT_INCLUDE_COMPILED_SOURCE);
            includeApiKey = Configurator.getSetting(List.of(
                            "includeApiKeyInBackup",
                            "flag.includeApiKeyInBackup",
                            "backup.includeApiKey",
                            "backup.files.includeApiKey",
                            "backup.files.apiKey"),
                    DEFAULT_INCLUDE_API_KEY);
            includeExecutable = Configurator.getSetting(List.of(
                            "includeExecutableInBackup",
                            "flag.includeExecutableInBackup",
                            "backup.includeExecutable",
                            "backup.files.includeExecutable",
                            "backup.files.executable"),
                    DEFAULT_INCLUDE_EXECUTABLE);
        }
        
    }
    
}
