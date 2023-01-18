/*
 * File:    EntityInfo.java
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
import youtube.entity.info.detail.Statistics;
import youtube.entity.info.detail.TagList;
import youtube.entity.info.detail.ThumbnailSet;
import youtube.entity.info.detail.TopicList;
import youtube.util.Utils;

/**
 * Defines the Entity Info of a Youtube Entity.
 */
public abstract class EntityInfo {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EntityInfo.class);
    
    
    //Constants
    
    /**
     * The date format used in Entity data.
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * A list of statuses indicating that an Entity is private.
     */
    private static final String[] PRIVATE_STATUSES = new String[] {
            "private"
    };
    
    
    //Static Functions
    
    /**
     * Parses a date string from Entity data into a date; or null if there was an error.
     */
    public static final CheckedFunction<String, LocalDateTime> dateParser = (String dateString) ->
            Optional.ofNullable(dateString)
                    .map(e -> e.replaceAll("(?i)[TZ]", " ")).map(String::strip)
                    .map(e -> LocalDateTime.parse(e, DateTimeFormatter.ofPattern(DATE_FORMAT)))
                    .orElse(null);
    
    /**
     * Parses a duration string from Entity data into a duration, in seconds; or null if there was an error.
     */
    public static final CheckedFunction<String, Long> durationParser = (String durationString) ->
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
     * Parses an integer from Entity data into a long; or null if it could not be parsed.
     */
    public static final CheckedFunction<Object, Long> integerParser = (Object integerObject) ->
            Optional.ofNullable(integerObject)
                    .map(String::valueOf)
                    .map((CheckedFunction<String, Long>) Long::parseLong)
                    .orElse(null);
    
    /**
     * Parses a number from Entity data into a double; or null if it could not be parsed.
     */
    public static final CheckedFunction<Object, Double> numberParser = (Object numberObject) ->
            Optional.ofNullable(numberObject)
                    .map(String::valueOf)
                    .map((CheckedFunction<String, Double>) Double::parseDouble)
                    .orElse(null);
    
    
    //Fields
    
    /**
     * The Metadata of the Entity.
     */
    public EntityMetadata metadata;
    
    /**
     * The url of the Entity.
     */
    public String url;
    
    /**
     * The raw title of the Entity.
     */
    public String rawTitle;
    
    /**
     * The title of the Entity.
     */
    public String title;
    
    /**
     * The description of the Entity.
     */
    public String description;
    
    /**
     * The privacy status of the Entity.
     */
    public String status;
    
    /**
     * The string representing the date the Entity was uploaded.
     */
    public String dateString;
    
    /**
     * The date the Entity was uploaded.
     */
    public LocalDateTime date;
    
    /**
     * The Tag List of the Entity.
     */
    public TagList tags;
    
    /**
     * The Topic List of the Entity.
     */
    public TopicList topics;
    
    /**
     * The Statistics of the Entity.
     */
    public Statistics stats;
    
    /**
     * The Thumbnail Set of the Entity.
     */
    public ThumbnailSet thumbnails;
    
    /**
     * The html used to embed the player of the Entity.
     */
    public String embeddedPlayer;
    
    
    //Constructors
    
    /**
     * Creates an Entity Info.
     *
     * @param entityData The json data of the Entity.
     */
    protected EntityInfo(Map<String, Object> entityData) {
        this.metadata = new EntityMetadata(entityData);
        
        this.rawTitle = getData("title");
        this.title = Utils.cleanVideoTitle(rawTitle);
        
        this.description = getData("description");
        this.status = getData("status", "privacyStatus");
        
        this.dateString = getData("publishedAt");
        this.date = dateParser.apply(dateString);
        
        this.tags = new TagList(getData("tags"));
        this.topics = new TopicList(getData("topicDetails", "topicCategories"));
        
        this.stats = new Statistics(getDataPart("statistics"));
        
        this.thumbnails = new ThumbnailSet(getData("thumbnails"));
        this.embeddedPlayer = getData("player", "embedHtml");
    }
    
    /**
     * Creates an empty Entity Info.
     */
    public EntityInfo() {
    }
    
    
    //Methods
    
    /**
     * Returns a part of the raw data of the Entity.
     *
     * @param part The name of the data part.
     * @return The part of the raw data of the Entity, or null if it does not exist.
     */
    protected Map<String, Object> getDataPart(String part) {
        return getMetadata().getDataPart(part);
    }
    
    /**
     * Returns an element from a specific part of the raw data of the Entity.
     *
     * @param part  The name of the data part.
     * @param field The name of the data element.
     * @param <T>   The type of the element.
     * @return The element from a specific part of the raw data of the Entity, or null if it does not exist.
     */
    protected <T> T getData(String part, String field) {
        return getMetadata().getData(part, field);
    }
    
    /**
     * Returns an element from the default part of the raw data of the Entity.
     *
     * @param field The name of the data element.
     * @param <T>   The type of the element.
     * @return The element from a default part of the raw data of the Entity, or null if it does not exist.
     */
    protected <T> T getData(String field) {
        return getData("snippet", field);
    }
    
    /**
     * Returns whether the Entity is private.
     *
     * @return Whether the Entity is private.
     */
    public boolean isPrivate() {
        return StringUtility.containsAnyIgnoreCase(getStatus(), PRIVATE_STATUSES);
    }
    
    /**
     * Returns a string representation of the Entity.
     *
     * @return a string representation of the Entity.
     */
    @Override
    public String toString() {
        return getTitle();
    }
    
    
    //Getters
    
    /**
     * Returns the Entity Metadata of the Entity.
     *
     * @return The Entity Metadata of the Entity.
     */
    public EntityMetadata getMetadata() {
        return metadata;
    }
    
    /**
     * Returns the url of the Entity.
     *
     * @return The url of the Entity.
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Returns the raw title of the Entity.
     *
     * @return The raw title of the Entity.
     */
    public String getRawTitle() {
        return rawTitle;
    }
    
    /**
     * Returns the title of the Entity.
     *
     * @return The title of the Entity.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Returns the description of the Entity.
     *
     * @return The description of the Entity.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns the privacy status of the Entity.
     *
     * @return The privacy status of the Entity.
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Returns the string representing the date the Entity was uploaded.
     *
     * @return The string representing the date the Entity was uploaded.
     */
    public String getDateString() {
        return dateString;
    }
    
    /**
     * Returns the date the Entity was uploaded.
     *
     * @return The date the Entity was uploaded.
     */
    public LocalDateTime getDate() {
        return date;
    }
    
    /**
     * Returns the Tag List of the Entity.
     *
     * @return The Tag List of the Entity.
     */
    public TagList getTags() {
        return tags;
    }
    
    /**
     * Returns the Topic List of the Entity.
     *
     * @return The Topic List of the Entity.
     */
    public TopicList getTopics() {
        return topics;
    }
    
    /**
     * Returns the Statistics of the Entity.
     *
     * @return The Statistics of the Entity.
     */
    public Statistics getStats() {
        return stats;
    }
    
    /**
     * Returns the Thumbnail Set of the Entity.
     *
     * @return The Thumbnail Set of the Entity.
     */
    public ThumbnailSet getThumbnails() {
        return thumbnails;
    }
    
    /**
     * Returns the html used to embed the player of the Entity.
     *
     * @return The html used to embed the player of the Entity.
     */
    public String getEmbeddedPlayer() {
        return embeddedPlayer;
    }
    
}
