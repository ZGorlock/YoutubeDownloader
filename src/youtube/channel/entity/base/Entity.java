/*
 * File:    Entity.java
 * Package: youtube.channel.entity.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity.base;

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
import youtube.channel.Channel;
import youtube.channel.entity.detail.Statistics;
import youtube.channel.entity.detail.TagList;
import youtube.channel.entity.detail.ThumbnailSet;
import youtube.channel.entity.detail.TopicList;
import youtube.util.Utils;

/**
 * Defines the base properties of an Entity.
 */
public abstract class Entity {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Entity.class);
    
    
    //Constants
    
    /**
     * The date format used in Entity data.
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    
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
    
    
    //Fields
    
    /**
     * The Channel containing the Entity.
     */
    public Channel channel;
    
    /**
     * The Metadata of the Entity.
     */
    public EntityMetadata metadata;
    
    /**
     * The url of the Entity.
     */
    public String url;
    
    /**
     * The original title of the Entity.
     */
    public String originalTitle;
    
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
     * The html to embed the Entity player.
     */
    public String embeddedPlayer;
    
    
    //Constructors
    
    /**
     * Creates an Entity.
     *
     * @param entityData The json data of the Entity,
     * @param channel    The Channel containing the Entity.
     */
    protected Entity(Map<String, Object> entityData, Channel channel) {
        this.channel = channel;
        
        this.metadata = new EntityMetadata(entityData);
        
        this.originalTitle = getData("title");
        this.title = Utils.cleanVideoTitle(originalTitle);
        
        this.description = getData("description");
        this.status = getData("status", "privacyStatus");
        
        this.dateString = getData("publishedAt");
        this.date = Optional.ofNullable(dateString).map(dateParser).orElseGet(LocalDateTime::now);
        
        this.tags = new TagList(getData("tags"));
        this.topics = new TopicList(getData("topicDetails", "topicCategories"));
        
        this.stats = new Statistics(getDataPart("statistics"));
        
        this.thumbnails = new ThumbnailSet(getData("thumbnails"));
        this.embeddedPlayer = getData("player", "embedHtml");
    }
    
    /**
     * Creates an Entity.
     *
     * @param entityData The json data of the Entity,
     */
    protected Entity(Map<String, Object> entityData) {
        this(entityData, null);
    }
    
    /**
     * The default no-argument constructor for an Entity.
     */
    protected Entity() {
    }
    
    
    //Methods
    
    /**
     * Initializes the Channel of the Entity.
     *
     * @param channel The Channel containing the Entity.
     */
    public void init(Channel channel) {
        this.channel = channel;
    }
    
    /**
     * Returns a part of the raw data of the Entity.
     *
     * @param part The name of the data part.
     * @return The part of the raw data of the Entity, or null if it does not exist.
     */
    protected Map<String, Object> getDataPart(String part) {
        return metadata.getDataPart(part);
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
        return metadata.getData(part, field);
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
        return Optional.ofNullable(title).map(e -> e.equalsIgnoreCase("Private video")).orElse(false) ||
                Optional.ofNullable(status).map(e -> e.equalsIgnoreCase("private")).orElse(false);
    }
    
    /**
     * Returns the string representation of the Entity.
     *
     * @return the string representation of the Entity.
     */
    @Override
    public String toString() {
        return title;
    }
    
}
