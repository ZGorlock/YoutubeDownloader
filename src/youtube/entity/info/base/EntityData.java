/*
 * File:    EntityData.java
 * Package: youtube.entity.info.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.base;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import commons.lambda.stream.collector.MapCollectors;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.base.ConfigData;

/**
 * Defines the Entity Data of an Entity.
 */
public class EntityData extends ConfigData {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EntityData.class);
    
    
    //Constants
    
    /**
     * The date format used in Entity Data.
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    
    //Constructors
    
    /**
     * Creates an Entity Data.
     *
     * @param entityData The json data of the Entity.
     */
    public EntityData(Map<String, Object> entityData) {
        super(entityData);
    }
    
    /**
     * Creates an empty Entity Data.
     */
    public EntityData() {
        super();
    }
    
    
    //Methods
    
    /**
     * Parses a date string from the Entity Data into a date.
     *
     * @param dateString The date string.
     * @return The date, or null if it could not be parsed.
     */
    protected LocalDateTime parseDate(String dateString) {
        return Optional.ofNullable(dateString)
                .map(e -> e.replaceAll("(?i)[TZ]", " ")).map(String::strip)
                .map(e -> LocalDateTime.parse(e, DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .orElse(null);
    }
    
    /**
     * Parses a duration string from the Entity Data into a duration.
     *
     * @param durationString The duration string.
     * @return The duration, in seconds, or null if it could not be parsed.
     */
    @SuppressWarnings("DataFlowIssue")
    protected long parseDuration(String durationString) {
        return Optional.ofNullable(durationString)
                .map(e -> e.replaceAll("(?i)[PT\\s]", ""))
                .map(e -> Arrays.stream(e.split("(?<=\\D)"))
                        .map(e2 -> Map.entry(
                                StringUtility.rSnip(e2, 1).toUpperCase(),
                                Long.parseLong(StringUtility.rShear(e2, 1))))
                        .collect(MapCollectors.toHashMap()))
                .map(e -> (((((
                        e.getOrDefault("D", 0L) * 24) +
                        e.getOrDefault("H", 0L)) * 60) +
                        e.getOrDefault("M", 0L)) * 60) +
                        e.getOrDefault("S", 0L))
                .orElse(null);
    }
    
}
