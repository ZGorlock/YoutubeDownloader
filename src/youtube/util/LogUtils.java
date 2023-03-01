/*
 * File:    LogUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
import java.text.SimpleDateFormat;
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
import commons.lambda.function.checked.CheckedFunction;
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
    public static final File LOGBACK_CONFIG_FILE = new File(Project.RESOURCES_DIR, ("logback" + '.' + Utils.XML_FILE_FORMAT));
    
    /**
     * The log directory.
     */
    public static final File LOG_DIR = Project.LOG_DIR;
    
    /**
     * The stamp format used for timestamps.
     */
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * The stamp format used for datestamps.
     */
    public static final String DATESTAMP_FORMAT = "yyyy-MM-dd";
    
    /**
     * The newline string.
     */
    public static final String NEWLINE = Color.base("");
    
    /**
     * The width of a divider.
     */
    public static final int DIVIDER_WIDTH = 200;
    
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
     */
    public static void initLogging() {
        if (loaded.compareAndSet(false, true)) {
            System.setProperty("logback.configurationFile", LogUtils.LOGBACK_CONFIG_FILE.getAbsolutePath());
            
            Runtime.getRuntime().addShutdownHook(new Thread(LogUtils::shutdownLogging));
        }
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
        final String msg = (visible ? NEWLINE : "") + log;
        switch (level) {
            case ERROR:
                logger.error(msg);
                break;
            case WARN:
                logger.warn(msg);
                break;
            case INFO:
                logger.info(msg);
                break;
            case DEBUG:
                logger.debug(msg);
                break;
            case TRACE:
                logger.trace(msg);
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
        log(logger, level, log, false);
    }
    
    /**
     * Logs a message.
     *
     * @param level   The log level of the log.
     * @param log     The log message.
     * @param visible Whether the log should also be shown in the console.
     */
    public static void log(LogLevel level, String log, boolean visible) {
        log(logger, level, log, visible);
    }
    
    /**
     * Logs a message.
     *
     * @param level The log level of the log.
     * @param log   The log message.
     */
    public static void log(LogLevel level, String log) {
        log(level, log, false);
    }
    
    /**
     * Logs a divider.
     *
     * @param logger      The logger to send the divider to.
     * @param dividerChar The character that makes up the divider.
     * @param blankLines  The number of blank lines to surround the divider by.
     */
    public static void logDivider(Logger logger, char dividerChar, int blankLines) {
        final String margin = StringUtility.repeatString("\n", blankLines);
        log(logger, LogLevel.TRACE,
                (margin + StringUtility.repeatString(String.valueOf(dividerChar), DIVIDER_WIDTH) + margin));
    }
    
    /**
     * Logs a divider.
     *
     * @param dividerChar The character that makes up the divider.
     * @param blankLines  The number of blank lines to surround the divider by.
     */
    public static void logDivider(char dividerChar, int blankLines) {
        logDivider(logger, dividerChar, blankLines);
    }
    
    /**
     * Logs a divider.
     *
     * @param logger      The logger to send the divider to.
     * @param dividerChar The character that makes up the divider.
     */
    public static void logDivider(Logger logger, char dividerChar) {
        logDivider(logger, dividerChar, 0);
    }
    
    /**
     * Logs a divider.
     *
     * @param dividerChar The character that makes up the divider.
     */
    public static void logDivider(char dividerChar) {
        logDivider(logger, dividerChar);
    }
    
    /**
     * Returns the daily log file with a specific log key.
     *
     * @param logKey The log key.
     * @return The daily log file with the specified log key.
     */
    public static File getDailyLog(String logKey) {
        return new File(LOG_DIR, (Stream.of(
                        Utils.PROJECT_TITLE, datestamp(), logKey)
                .filter(e -> !StringUtility.isNullOrBlank(e))
                .collect(Collectors.joining("-")) + '.' + Utils.LOG_FILE_FORMAT));
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
        return Optional.ofNullable(date).map(LogUtils::datestamp)
                .map(LogUtils::fetchAllLogs)
                .orElse(Collections.emptyList());
    }
    
    /**
     * Returns the log files grouped by date.
     *
     * @return The log files grouped by date
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
        if (Optional.ofNullable(Configurator.Config.daysToKeepLogs).orElse(-1L) < 0) {
            return;
        }
        fetchAllLogsOlderThan(Configurator.Config.daysToKeepLogs.intValue())
                .forEach(Filesystem::deleteFile);
        
        Filesystem.getDirs(LOG_DIR).stream()
                .filter(Filesystem::directoryIsEmpty)
                .forEach(Filesystem::deleteDirectory);
    }
    
    /**
     * Formats a timestamp from a date.
     *
     * @param date   The date.
     * @param format The timestamp format to use.
     * @return A timestamp representation of the specified date, or null if it could not be formatted.
     */
    private static String formatDate(Date date, String format) {
        return Optional.ofNullable(format).map(SimpleDateFormat::new)
                .map(e -> e.format(date))
                .orElse(null);
    }
    
    /**
     * Parses a date from a timestamp.
     *
     * @param timestamp The timestamp.
     * @param format    The format of the timestamp.
     * @return A date representation of the specified timestamp, or null if it could not be parsed.
     */
    private static Date parseStamp(String timestamp, String format) {
        return Optional.ofNullable(format).map(SimpleDateFormat::new)
                .map((CheckedFunction<SimpleDateFormat, Date>) e -> e.parse(timestamp))
                .orElse(null);
    }
    
    /**
     * Returns a timestamp representing a date.
     *
     * @param date The date.
     * @return A timestamp representing the specified date.
     */
    public static String timestamp(Date date) {
        return formatDate(date, TIMESTAMP_FORMAT);
    }
    
    /**
     * Returns the current timestamp.
     *
     * @return The current timestamp.
     */
    public static String timestamp() {
        return timestamp(new Date());
    }
    
    /**
     * Returns the date represented by a timestamp.
     *
     * @param timestamp The timestamp.
     * @return The date represented by the timestamp.
     */
    public static Date parseTimestamp(String timestamp) {
        return parseStamp(timestamp, TIMESTAMP_FORMAT);
    }
    
    /**
     * Returns a datestamp representing a date.
     *
     * @param date The date.
     * @return A datestamp representing the specified date.
     */
    public static String datestamp(Date date) {
        return formatDate(date, DATESTAMP_FORMAT);
    }
    
    /**
     * Returns the current datestamp.
     *
     * @return The current datestamp.
     */
    public static String datestamp() {
        return datestamp(new Date());
    }
    
    /**
     * Returns the date represented by a datestamp.
     *
     * @param datestamp The datestamp.
     * @return The date represented by the datestamp.
     */
    public static Date parseDatestamp(String datestamp) {
        return parseStamp(datestamp, DATESTAMP_FORMAT);
    }
    
    /**
     * Returns the associated date of a log file.
     *
     * @param logFile The log file.
     * @return The associated date or the log file.
     */
    public static Date getLogDate(File logFile) {
        return Optional.ofNullable(logFile)
                .map(File::getName)
                .map(logName -> logName.replaceAll("^.+?-([\\d\\-]+)[.\\-]\\D.+$", "$1"))
                .map(LogUtils::parseDatestamp)
                .orElse(new Date(0));
    }
    
}
