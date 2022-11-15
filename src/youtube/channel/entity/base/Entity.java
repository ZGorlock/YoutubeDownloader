/*
 * File:    Entity.java
 * Package: youtube.channel.entity.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity.base;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import commons.lambda.function.checked.CheckedFunction;
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
     * Parses a datestamp from Entity data into a date; or null if there was an error.
     */
    public static final CheckedFunction<String, LocalDateTime> dateParser = (String datestamp) ->
            LocalDateTime.parse(datestamp.replaceAll("[TZ]", " ").strip(), DateTimeFormatter.ofPattern(DATE_FORMAT));
    
    
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
     * The datestamp the Entity was uploaded.
     */
    public String datestamp;
    
    /**
     * The date the Entity was uploaded.
     */
    public LocalDateTime date;
    
    /**
     * The html to embed the Entity player.
     */
    public String embeddedPlayer;
    
    /**
     * The Tag List of the Entity.
     */
    public TagList tags;
    
    /**
     * The Topic List of the Entity.
     */
    public TopicList topics;
    
    /**
     * The Thumbnail Set of the Entity.
     */
    public ThumbnailSet thumbnails;
    
    /**
     * The Statistics of the Entity.
     */
    public Statistics stats;
    
    
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
        
        this.datestamp = getData("publishedAt");
        this.date = Optional.ofNullable(datestamp).map(dateParser).orElseGet(LocalDateTime::now);
        
        this.embeddedPlayer = getData("player", "embedHtml");
        
        this.tags = new TagList(getData("tags"));
        this.topics = new TopicList(getData("topicDetails", "topicCategories"));
        
        this.thumbnails = new ThumbnailSet(getData("thumbnails"));
        this.stats = new Statistics(getDataPart("statistics"));
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
