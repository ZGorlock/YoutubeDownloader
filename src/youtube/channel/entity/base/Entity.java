/*
 * File:    Entity.java
 * Package: youtube.channel.entity.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity.base;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import commons.lambda.function.checked.CheckedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
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
     * The date the Entity was uploaded.
     */
    public LocalDateTime date;
    
    /**
     * The Thumbnail Set of the Entity.
     */
    public ThumbnailSet thumbnails;
    
    /**
     * The tags associated with the Entity.
     */
    public List<String> tags;
    
    
    //Constructors
    
    /**
     * Creates an Entity.
     *
     * @param entityData The json data of the Entity,
     * @param channel    The Channel containing the Entity.
     */
    @SuppressWarnings("unchecked")
    protected Entity(Map<String, Object> entityData, Channel channel) {
        this.channel = channel;
        
        this.metadata = new EntityMetadata(entityData);
        
        this.originalTitle = (String) entityData.get("title");
        this.title = Utils.cleanVideoTitle(originalTitle);
        this.description = (String) entityData.get("description");
        
        this.date = Optional.ofNullable((String) entityData.get("publishedAt"))
                .map(dateParser).orElseGet(LocalDateTime::now);
        
        this.thumbnails = new ThumbnailSet((Map<String, Object>) entityData.get("thumbnails"));
        this.tags = Optional.ofNullable((List<String>) entityData.get("tags")).orElse(new ArrayList<>());
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
     * Returns the string representation of the Entity.
     *
     * @return the string representation of the Entity.
     */
    @Override
    public String toString() {
        return title;
    }
    
}
