/*
 * File:    LogUtils.java
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.access.Project;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
import youtube.config.Configurator;

/**
 * Provides log utility methods for the Youtube Downloader.
 */
public final class LogUtils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(LogUtils.class);
    
    
    //Constants
    
    /**
     * The logback configuration file.
     */
    public static final File LOGBACK_CONFIG_FILE = new File(Project.RESOURCES_DIR, FileUtils.setFormat("logback", FileUtils.XML_FILE_FORMAT));
    
    /**
     * The log directory.
     */
    public static final File LOG_DIR = Project.LOG_DIR;
    
    /**
     * The newline string.
     */
    public static final String NEWLINE = Color.base("");
    
    /**
     * The width of an indentation.
     */
    public static final int INDENT_WIDTH = 5;
    
    /**
     * The indentation string.
     */
    public static final String INDENT = Color.base(" ".repeat(INDENT_WIDTH));
    
    /**
     * The hard indentation string.
     */
    public static final String INDENT_HARD = Color.log(" ".repeat(INDENT_WIDTH));
    
    /**
     * The default log level of logs.
     */
    public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.TRACE;
    
    /**
     * The default value of the flag indicating whether a log should be shown in the console.
     */
    public static final boolean DEFAULT_LOG_VISIBLE = false;
    
    /**
     * The default log level of log dividers.
     */
    public static final LogLevel DEFAULT_DIVIDER_LOG_LEVEL = DEFAULT_LOG_LEVEL;
    
    /**
     * The width of a divider.
     */
    public static final int DIVIDER_WIDTH = 200;
    
    /**
     * The default character to use when printing a log divider.
     */
    public static final char DEFAULT_DIVIDER_CHAR = '-';
    
    /**
     * The default number of blanks lines to surround a log divider with.
     */
    public static final int DEFAULT_DIVIDER_BLANK_LINES = 0;
    
    /**
     * The default value of the flag indicating whether a log divider should be shown in the console.
     */
    public static final boolean DEFAULT_DIVIDER_VISIBLE = false;
    
    
    //Enums
    
    /**
     * An enumeration of Log Levels.
     */
    public enum LogLevel {
        OFF,
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }
    
    /**
     * An enumeration of Log Types.
     */
    public enum LogType {
        
        //Values
        
        DOWNLOAD("download"),
        API_CALL("api");
        
        
        //Fields
        
        /**
         * The log key of the Log Type.
         */
        public final String logKey;
        
        
        //Constructors
        
        /**
         * Constructs a Log Type.
         *
         * @param logKey The log key of the Log Type.
         */
        LogType(String logKey) {
            this.logKey = logKey;
        }
        
        
        //Getters
        
        /**
         * Returns the log key of the Log Type.
         *
         * @return The log key of the Log Type.
         */
        public String getLogKey() {
            return logKey;
        }
        
    }
    
    
    //Static Fields
    
    /**
     * A flag indicating whether the logging configuration has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Static Methods
    
    /**
     * Initializes the logging configuration.
     *
     * @return Whether the logging configuration was successfully initialized.
     */
    public static boolean initLogging() {
        if (loaded.compareAndSet(false, true)) {
            logger.trace(LogUtils.NEWLINE);
            logger.debug(Color.log("Initializing Logging..."));
            
            Config.init();
            
            System.setProperty("logback.configurationFile", LOGBACK_CONFIG_FILE.getAbsolutePath());
            Runtime.getRuntime().addShutdownHook(new Thread(LogUtils::shutdownLogging));
            
            return true;
        }
        return false;
    }
    
    /**
     * Shuts down the logging configuration.
     */
    private static void shutdownLogging() {
        if (loaded.get()) {
            cleanupOldLogs();
            logDivider('=', 3);
        }
    }
    
    /**
     * Logs a message.
     *
     * @param logger  The logger to send the log to.
     * @param level   The log level of the log.
     * @param log     The log message.
     * @param visible Whether the log should also be shown in the console.
     */
    public static void log(Logger logger, LogLevel level, String log, boolean visible) {
        logger = Optional.ofNullable(logger).orElse(LogUtils.logger);
        level = Optional.ofNullable(level).orElse(LogLevel.OFF);
        log = (visible ? NEWLINE : "") + Optional.ofNullable(log).orElse("");
        
        switch (level) {
            case ERROR:
                logger.error(log);
                break;
            case WARN:
                logger.warn(log);
                break;
            case INFO:
                logger.info(log);
                break;
            case DEBUG:
                logger.debug(log);
                break;
            case TRACE:
                logger.trace(log);
                break;
            case OFF:
            default:
                break;
        }
    }
    
    /**
     * Logs a message.
     *
     * @param logger The logger to send the log to.
     * @param level  The log level of the log.
     * @param log    The log message.
     */
    public static void log(Logger logger, LogLevel level, String log) {
        log(logger, level, log, DEFAULT_LOG_VISIBLE);
    }
    
    /**
     * Logs a message.
     *
     * @param logger  The logger to send the log to.
     * @param log     The log message.
     * @param visible Whether the log should also be shown in the console.
     */
    public static void log(Logger logger, String log, boolean visible) {
        log(logger, DEFAULT_LOG_LEVEL, log, visible);
    }
    
    /**
     * Logs a message.
     *
     * @param logger The logger to send the log to.
     * @param log    The log message.
     */
    public static void log(Logger logger, String log) {
        log(logger, log, DEFAULT_LOG_VISIBLE);
    }
    
    /**
     * Logs a message.
     *
     * @param level   The log level of the log.
     * @param log     The log message.
     * @param visible Whether the log should also be shown in the console.
     */
    public static void log(LogLevel level, String log, boolean visible) {
        log(null, DEFAULT_LOG_LEVEL, log, visible);
    }
    
    /**
     * Logs a message.
     *
     * @param level The log level of the log.
     * @param log   The log message.
     */
    public static void log(LogLevel level, String log) {
        log(level, log, DEFAULT_LOG_VISIBLE);
    }
    
    /**
     * Logs a divider.
     *
     * @param logger      The logger to send the divider to.
     * @param dividerChar The character that makes up the divider.
     * @param blankLines  The number of blank lines to surround the divider with.
     * @param visible     Whether the divider should also be shown in the console.
     */
    public static void logDivider(Logger logger, char dividerChar, int blankLines, boolean visible) {
        final String margin = StringUtility.repeatString("\n", blankLines);
        final String log = (margin + StringUtility.repeatString(String.valueOf(dividerChar), DIVIDER_WIDTH) + margin);
        
        log(logger, DEFAULT_DIVIDER_LOG_LEVEL, log, visible);
    }
    
    /**
     * Logs a divider.
     *
     * @param logger      The logger to send the divider to.
     * @param dividerChar The character that makes up the divider.
     * @param blankLines  The number of blank lines to surround the divider with.
     */
    public static void logDivider(Logger logger, char dividerChar, int blankLines) {
        logDivider(logger, dividerChar, blankLines, DEFAULT_DIVIDER_VISIBLE);
    }
    
    /**
     * Logs a divider.
     *
     * @param logger      The logger to send the divider to.
     * @param dividerChar The character that makes up the divider.
     * @param visible     Whether the divider should also be shown in the console.
     */
    public static void logDivider(Logger logger, char dividerChar, boolean visible) {
        logDivider(logger, dividerChar, DEFAULT_DIVIDER_BLANK_LINES, visible);
    }
    
    /**
     * Logs a divider.
     *
     * @param logger     The logger to send the divider to.
     * @param blankLines The number of blank lines to surround the divider with.
     * @param visible    Whether the divider should also be shown in the console.
     */
    public static void logDivider(Logger logger, int blankLines, boolean visible) {
        logDivider(logger, DEFAULT_DIVIDER_CHAR, blankLines, visible);
    }
    
    /**
     * Logs a divider.
     *
     * @param logger      The logger to send the divider to.
     * @param dividerChar The character that makes up the divider.
     */
    public static void logDivider(Logger logger, char dividerChar) {
        logDivider(logger, dividerChar, DEFAULT_DIVIDER_VISIBLE);
    }
    
    /**
     * Logs a divider.
     *
     * @param logger     The logger to send the divider to.
     * @param blankLines The number of blank lines to surround the divider with.
     */
    public static void logDivider(Logger logger, int blankLines) {
        logDivider(logger, blankLines, DEFAULT_DIVIDER_VISIBLE);
    }
    
    /**
     * Logs a divider.
     *
     * @param logger  The logger to send the divider to.
     * @param visible Whether the divider should also be shown in the console.
     */
    public static void logDivider(Logger logger, boolean visible) {
        logDivider(logger, DEFAULT_DIVIDER_CHAR, visible);
    }
    
    /**
     * Logs a divider.
     *
     * @param dividerChar The character that makes up the divider.
     * @param blankLines  The number of blank lines to surround the divider with.
     * @param visible     Whether the divider should also be shown in the console.
     */
    public static void logDivider(char dividerChar, int blankLines, boolean visible) {
        logDivider(null, dividerChar, blankLines, visible);
    }
    
    /**
     * Logs a divider.
     *
     * @param dividerChar The character that makes up the divider.
     * @param blankLines  The number of blank lines to surround the divider with.
     */
    public static void logDivider(char dividerChar, int blankLines) {
        logDivider(dividerChar, blankLines, DEFAULT_DIVIDER_VISIBLE);
    }
    
    /**
     * Logs a divider.
     *
     * @param dividerChar The character that makes up the divider.
     * @param visible     Whether the divider should also be shown in the console.
     */
    public static void logDivider(char dividerChar, boolean visible) {
        logDivider(dividerChar, DEFAULT_DIVIDER_BLANK_LINES, visible);
    }
    
    /**
     * Logs a divider.
     *
     * @param blankLines The number of blank lines to surround the divider with.
     * @param visible    Whether the divider should also be shown in the console.
     */
    public static void logDivider(int blankLines, boolean visible) {
        logDivider(DEFAULT_DIVIDER_CHAR, blankLines, visible);
    }
    
    /**
     * Logs a divider.
     *
     * @param dividerChar The character that makes up the divider.
     */
    public static void logDivider(char dividerChar) {
        logDivider(dividerChar, DEFAULT_DIVIDER_VISIBLE);
    }
    
    /**
     * Logs a divider.
     *
     * @param blankLines The number of blank lines to surround the divider with.
     */
    public static void logDivider(int blankLines) {
        logDivider(blankLines, DEFAULT_DIVIDER_VISIBLE);
    }
    
    /**
     * Logs a divider.
     *
     * @param visible Whether the divider should also be shown in the console.
     */
    public static void logDivider(boolean visible) {
        logDivider(DEFAULT_DIVIDER_CHAR, visible);
    }
    
    /**
     * Logs a divider.
     */
    public static void logDivider() {
        logDivider(DEFAULT_DIVIDER_VISIBLE);
    }
    
    /**
     * Returns the daily log file with a specific log key.
     *
     * @param logKey The log key.
     * @return The daily log file with the specified log key.
     */
    public static File getDailyLog(String logKey) {
        return new File(LOG_DIR, FileUtils.setFormat(
                Stream.of(Utils.PROJECT_TITLE, DateUtils.datestamp(), logKey)
                        .filter(e -> !StringUtility.isNullOrBlank(e))
                        .collect(Collectors.joining("-")),
                FileUtils.LOG_FILE_FORMAT));
    }
    
    /**
     * Returns the daily log file of a specific Log Type.
     *
     * @param type The Log Type.
     * @return The daily log file of the specified Log Type.
     */
    public static File getDailyLog(LogType type) {
        return getDailyLog(Optional.ofNullable(type).map(LogType::getLogKey).orElse(""));
    }
    
    /**
     * Returns the default daily log file.
     *
     * @return The default daily log file.
     */
    public static File getDailyLog() {
        return getDailyLog("");
    }
    
    /**
     * Returns the associated date of a log file.
     *
     * @param logFile The log file.
     * @return The associated date or the log file, or a dummy date if the file name does not include a datestamp.
     */
    public static Date getLogDate(File logFile) {
        return Optional.ofNullable(logFile)
                .map(FileUtils::getStampedFileDate)
                .orElseGet(() -> new Date(0));
    }
    
    /**
     * Returns the log files present in the log directory.
     *
     * @return The log files present in the log directory.
     */
    public static List<File> fetchAllLogs() {
        return Filesystem.getFilesRecursively(LOG_DIR);
    }
    
    /**
     * Returns the log files with a certain log key.
     *
     * @param logKey The log key.
     * @return The log files with the specified log key.
     */
    public static List<File> fetchAllLogs(String logKey) {
        return fetchAllLogs().stream()
                .filter(logFile -> (StringUtility.isNullOrBlank(logKey) || logFile.getName().contains("-" + logKey)))
                .collect(Collectors.toList());
    }
    
    /**
     * Returns the log files of a certain Log Type.
     *
     * @param type The Log Type.
     * @return The log files of the specified Log Type.
     */
    public static List<File> fetchAllLogs(LogType type) {
        return fetchAllLogs(Optional.ofNullable(type).map(LogType::getLogKey).orElse(""));
    }
    
    /**
     * Returns the log files from a certain date.
     *
     * @param date The date.
     * @return The log files from the specified date.
     */
    public static List<File> fetchAllLogs(Date date) {
        return Optional.ofNullable(date).map(DateUtils::datestamp)
                .map(LogUtils::fetchAllLogs)
                .orElse(Collections.emptyList());
    }
    
    /**
     * Returns the log files grouped by date.
     *
     * @return The log files grouped by date.
     */
    public static Map<Date, List<File>> fetchAllLogsByDate() {
        return fetchAllLogs().stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(LogUtils::getLogDate),
                        logDateMap -> logDateMap.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .collect(MapCollectors.toLinkedHashMap())));
    }
    
    /**
     * Returns the log files filtered by date.
     *
     * @param filter The date filter.
     * @return The log files that passed the date filter.
     */
    private static List<File> filterLogsByDate(Predicate<Date> filter) {
        return fetchAllLogsByDate().entrySet().stream()
                .filter(e -> filter.test(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    
    /**
     * Returns the log files from before a date.
     *
     * @param date The date.
     * @return The log files from before the specified date.
     */
    public static List<File> fetchAllLogsBefore(Date date) {
        return filterLogsByDate(d -> d.before(date));
    }
    
    /**
     * Returns the log files from after a date.
     *
     * @param date The date.
     * @return The log files from after the specified date.
     */
    public static List<File> fetchAllLogsAfter(Date date) {
        return filterLogsByDate(d -> d.after(date));
    }
    
    /**
     * Returns the log files from on or before a date.
     *
     * @param date The date.
     * @return The log files from on or before the specified date.
     */
    public static List<File> fetchAllLogsOnOrBefore(Date date) {
        return filterLogsByDate(d -> !d.after(date));
    }
    
    /**
     * Returns the log files from on or after a date.
     *
     * @param date The date.
     * @return The log files from on or after the specified date.
     */
    public static List<File> fetchAllLogsOnOrAfter(Date date) {
        return filterLogsByDate(d -> !d.before(date));
    }
    
    /**
     * Returns the log files from between two dates.
     *
     * @param startDate The start date.
     * @param endDate   The end date.
     * @return The log files from between the specified start and end date.
     */
    public static List<File> fetchAllLogsBetween(Date startDate, Date endDate) {
        return filterLogsByDate(d -> (!d.before(startDate) && !d.after(endDate)));
    }
    
    /**
     * Returns the log files newer than a certain number of days.
     *
     * @param days The number of days.
     * @return The log files newer than the specified number of days.
     */
    public static List<File> fetchAllLogsNewerThan(int days) {
        return fetchAllLogsOnOrAfter(new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(days)));
    }
    
    /**
     * Returns the log files older than a certain number of days.
     *
     * @param days The number of days.
     * @return The log files older than the specified number of days.
     */
    public static List<File> fetchAllLogsOlderThan(int days) {
        return fetchAllLogsOnOrBefore(new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(days)));
    }
    
    /**
     * Purges old log files.
     */
    private static void cleanupOldLogs() {
        Optional.ofNullable(Config.daysToKeepLogs).map(Long::intValue)
                .filter(daysToKeepLogs -> (daysToKeepLogs >= 0))
                .map(LogUtils::fetchAllLogsOlderThan)
                .stream().flatMap(Collection::stream)
                .forEach(FileUtils::delete);
        
        Filesystem.getDirs(LOG_DIR).stream()
                .filter(Filesystem::directoryIsEmpty)
                .forEach(FileUtils::delete);
    }
    
    
    //Inner Classes
    
    /**
     * Holds the log Config.
     */
    public static class Config {
        
        //Constants
        
        /**
         * The default value of the flag indicating whether to print statistics at the end of the run or not.
         */
        public static final boolean DEFAULT_PRINT_STATS = true;
        
        /**
         * The default value of the flag indicating whether to print a download report at the end of the run or not.
         */
        public static final boolean DEFAULT_PRINT_REPORT = false;
        
        /**
         * The default value of the flag indicating whether to print the executable version at the start of the run or not.
         */
        public static final boolean DEFAULT_PRINT_EXE_VERSION = true;
        
        /**
         * The default value of the flag indicating whether to print the execution time at the end of the run or not.
         */
        public static final boolean DEFAULT_PRINT_EXECUTION_TIME = true;
        
        /**
         * The default value of the flag indicating whether to print the command sent to the executable during a download or not.
         */
        public static final boolean DEFAULT_SHOW_COMMAND = true;
        
        /**
         * The default value of the flag indicating whether to print the output produced by the executable during a download or not.
         */
        public static final boolean DEFAULT_SHOW_WORK = false;
        
        /**
         * The default value of the flag indicating whether to display a progress bar in the terminal during a download or not.
         */
        public static final boolean DEFAULT_SHOW_PROGRESS_BAR = true;
        
        /**
         * The default value of the setting indicating whether to permit log files in the log directory to be written or not.
         */
        public static final boolean DEFAULT_ALLOW_FILE_LOGGING = true;
        
        /**
         * The default value of the setting indicating the number of days to retain log files before deleting them.
         */
        public static final long DEFAULT_DAYS_TO_KEEP_LOGS = 30;
        
        /**
         * The default value of the setting indicating whether to permit the main log file to be written or not.
         */
        public static final boolean DEFAULT_WRITE_MAIN_LOG = true;
        
        /**
         * The default value of the setting indicating whether to permit the download log file to be written or not.
         */
        public static final boolean DEFAULT_WRITE_DOWNLOAD_LOG = true;
        
        /**
         * The default value of the setting indicating whether to permit the api log file to be written or not.
         */
        public static final boolean DEFAULT_WRITE_API_LOG = true;
        
        
        //Static Fields
        
        /**
         * A flag indicating whether to print statistics at the end of the run or not.
         */
        public static boolean printStats = DEFAULT_PRINT_STATS;
        
        /**
         * A flag indicating whether to print a download report at the end of the run or not.
         */
        public static boolean printReport = DEFAULT_PRINT_REPORT;
        
        /**
         * A flag indicating whether to print the executable version at the start of the run or not.
         */
        public static boolean printExeVersion = DEFAULT_PRINT_EXE_VERSION;
        
        /**
         * A flag indicating whether to print the execution time at the end of the run or not.
         */
        public static boolean printExecutionTime = DEFAULT_PRINT_EXECUTION_TIME;
        
        /**
         * A flag indicating whether to print the command sent to the executable during a download or not.
         */
        public static boolean showCommand = DEFAULT_SHOW_COMMAND;
        
        /**
         * A flag indicating whether to print the output produced by the executable during a download or not.
         */
        public static boolean showWork = DEFAULT_SHOW_WORK;
        
        /**
         * A flag indicating whether to display a progress bar in the terminal during a download or not.
         */
        public static boolean showProgressBar = DEFAULT_SHOW_PROGRESS_BAR;
        
        /**
         * A flag indicating whether to permit log files in the log directory to be written or not.
         */
        public static boolean allowFileLogging = DEFAULT_ALLOW_FILE_LOGGING;
        
        /**
         * A flag indicating whether to permit the main log file to be written or not.
         */
        public static boolean writeMainLog = DEFAULT_WRITE_MAIN_LOG;
        
        /**
         * A flag indicating whether to permit the download log file to be written or not.
         */
        public static boolean writeDownloadLog = DEFAULT_WRITE_DOWNLOAD_LOG;
        
        /**
         * A flag indicating whether to permit the api log file to be written or not.
         */
        public static boolean writeApiLog = DEFAULT_WRITE_API_LOG;
        
        /**
         * The number of days to retain log files before deleting them, or -1 to retain logs indefinitely.
         */
        public static Long daysToKeepLogs = DEFAULT_DAYS_TO_KEEP_LOGS;
        
        
        //Static Methods
        
        /**
         * Initializes the Config.
         */
        private static void init() {
            printStats = Configurator.getSetting(List.of(
                            "printStats",
                            "log.printStats",
                            "output.printStats"),
                    DEFAULT_PRINT_STATS);
            printReport = Configurator.getSetting(List.of(
                            "printReport",
                            "log.printReport",
                            "output.printReport"),
                    DEFAULT_PRINT_REPORT);
            printExeVersion = Configurator.getSetting(List.of(
                            "printExeVersion",
                            "log.printExeVersion",
                            "output.printExeVersion"),
                    DEFAULT_PRINT_EXE_VERSION);
            printExecutionTime = Configurator.getSetting(List.of(
                            "printExecutionTime",
                            "log.printExecutionTime",
                            "output.printExecutionTime"),
                    DEFAULT_PRINT_EXECUTION_TIME);
            
            showCommand = Configurator.getSetting(List.of(
                            "showCommand",
                            "logCommand",
                            "flag.showCommand",
                            "flag.logCommand",
                            "log.download.showCommand",
                            "log.download.logCommand",
                            "log.showCommand",
                            "log.logCommand"),
                    DEFAULT_SHOW_COMMAND);
            showWork = Configurator.getSetting(List.of(
                            "showWork",
                            "logWork",
                            "flag.showWork",
                            "flag.logWork",
                            "log.download.showWork",
                            "log.download.logWork",
                            "log.showWork",
                            "log.logWork"),
                    DEFAULT_SHOW_WORK);
            showProgressBar = Configurator.getSetting(List.of(
                            "showProgressBar",
                            "logProgressBar",
                            "flag.showProgressBar",
                            "flag.logProgressBar",
                            "log.download.showProgressBar",
                            "log.download.logProgressBar",
                            "log.showProgressBar",
                            "log.logProgressBar"),
                    DEFAULT_SHOW_PROGRESS_BAR);
            
            allowFileLogging = Configurator.getSetting(List.of(
                            "allowFileLogging",
                            "flag.allowFileLogging",
                            "log.file.allowFileLogging",
                            "log.file.allow"),
                    DEFAULT_ALLOW_FILE_LOGGING);
            writeMainLog = Configurator.getSetting(List.of(
                            "writeMainLog",
                            "flag.writeMainLog",
                            "log.file.writeMainLog"),
                    DEFAULT_WRITE_MAIN_LOG);
            writeDownloadLog = Configurator.getSetting(List.of(
                            "writeDownloadLog",
                            "flag.writeDownloadLog",
                            "log.file.writeDownloadLog"),
                    DEFAULT_WRITE_DOWNLOAD_LOG);
            writeApiLog = Configurator.getSetting(List.of(
                            "writeApiLog",
                            "flag.writeApiLog",
                            "log.file.writeApiLog"),
                    DEFAULT_WRITE_API_LOG);
            daysToKeepLogs = Configurator.getSetting(List.of(
                            "daysToKeepLogs",
                            "flag.daysToKeepLogs",
                            "log.file.daysToKeepLogs",
                            "log.daysToKeepLogs"),
                    DEFAULT_DAYS_TO_KEEP_LOGS);
        }
        
    }
    
}
