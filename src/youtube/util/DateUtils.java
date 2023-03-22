/*
 * File:    DateUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import commons.lambda.function.checked.CheckedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides date utility methods for the Youtube Downloader.
 */
public final class DateUtils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);
    
    
    //Constants
    
    /**
     * The stamp format used for timestamps.
     */
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * The stamp format used for datestamps.
     */
    public static final String DATESTAMP_FORMAT = "yyyy-MM-dd";
    
    
    //Static Methods
    
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
    
}
