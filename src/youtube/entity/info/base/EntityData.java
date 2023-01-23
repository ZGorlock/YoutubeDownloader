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

import commons.lambda.function.checked.CheckedFunction;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the Entity Data of an Entity.
 */
public class EntityData {
    
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
    
    
    //Static Functions
    
    /**
     * Parses a date string from the Entity Data into a date; or null if it could not be parsed.
     */
    protected static final CheckedFunction<String, LocalDateTime> dateParser = (String dateString) ->
            Optional.ofNullable(dateString)
                    .map(e -> e.replaceAll("(?i)[TZ]", " ")).map(String::strip)
                    .map(e -> LocalDateTime.parse(e, DateTimeFormatter.ofPattern(DATE_FORMAT)))
                    .orElse(null);
    
    /**
     * Parses a duration string from the Entity Data into a duration, in seconds; or null if it could not be parsed.
     */
    protected static final CheckedFunction<String, Long> durationParser = (String durationString) ->
            Optional.ofNullable(durationString)
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
    
    /**
     * Parses an integer from the Entity Data into a long; or null if it could not be parsed.
     */
    protected static final CheckedFunction<Object, Long> integerParser = (Object integerObject) ->
            Optional.ofNullable(integerObject)
                    .map(String::valueOf)
                    .map((CheckedFunction<String, Long>) Long::parseLong)
                    .orElse(null);
    
    /**
     * Parses a number from the Entity Data into a double; or null if it could not be parsed.
     */
    protected static final CheckedFunction<Object, Double> numberParser = (Object numberObject) ->
            Optional.ofNullable(numberObject)
                    .map(String::valueOf)
                    .map((CheckedFunction<String, Double>) Double::parseDouble)
                    .orElse(null);
    
    
    //Fields
    
    /**
     * The raw json Entity Data.
     */
    public Map<String, Object> entityData;
    
    
    //Constructors
    
    /**
     * Creates an Entity Data.
     *
     * @param entityData The json data of the Entity.
     */
    public EntityData(Map<String, Object> entityData) {
        this.entityData = entityData;
    }
    
    /**
     * Creates an empty Entity Data.
     */
    public EntityData() {
    }
    
    
    //Methods
    
    /**
     * Fetches a field from the Entity Data.
     *
     * @param path The path to the requested field.
     * @param <R>  The type of the requested field.
     * @return The value of the field; or null if it does not exist.
     */
    public <R> R getData(String... path) {
        return getData(getEntityData(), path);
    }
    
    
    //Getters
    
    /**
     * Returns the raw json data of the Entity.
     *
     * @return The raw json data of the Entity.
     */
    public Map<String, Object> getEntityData() {
        return entityData;
    }
    
    
    //Static Methods
    
    /**
     * Fetches a field from a json data map.
     *
     * @param data The json data map.
     * @param path The path to the requested field.
     * @param <K>  The type of the keys of the map.
     * @param <V>  The type of the values of the map.
     * @param <R>  The type of the requested field.
     * @return The value of the field; or null if it does not exist.
     */
    @SuppressWarnings("unchecked")
    private static <K, V, R extends V> R getData(Map<K, V> data, K... path) {
        return (data == null) ? null :
               (path.length == 0) ? (R) data :
               (path.length == 1) ? (R) data.get(path[0]) :
               getData(Arrays.stream(path, 0, path.length - 1).sequential()
                               .reduce(data, EntityData::getData, (p, q) -> null),
                       path[path.length - 1]);
    }
    
}
