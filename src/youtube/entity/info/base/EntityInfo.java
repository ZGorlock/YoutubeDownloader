/*
 * File:    EntityInfo.java
 * Package: youtube.entity.info.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.base;

import java.time.LocalDateTime;
import java.util.Map;

import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.detail.Statistics;
import youtube.entity.info.detail.TagList;
import youtube.entity.info.detail.ThumbnailSet;
import youtube.entity.info.detail.TopicList;
import youtube.util.Utils;

/**
 * Defines the Entity Info of an Entity.
 */
public abstract class EntityInfo extends EntityData {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EntityInfo.class);
    
    
    //Constants
    
    /**
     * A list of statuses indicating that an Entity is private.
     */
    private static final String[] PRIVATE_STATUSES = new String[] {
            "private"
    };
    
    
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
        super(entityData);
        this.metadata = new EntityMetadata(entityData);
        
        this.rawTitle = parseData("snippet", "title");
        this.title = Utils.cleanVideoTitle(rawTitle);
        
        this.description = parseData("snippet", "description");
        this.status = parseData("status", "privacyStatus");
        
        this.dateString = parseData("snippet", "publishedAt");
        this.date = parseDate(dateString);
        
        this.tags = new TagList(parseData("snippet", "tags"));
        this.topics = new TopicList(parseData("topicDetails", "topicCategories"));
        
        this.stats = new Statistics(parseData("statistics"));
        
        this.thumbnails = new ThumbnailSet(parseData("snippet", "thumbnails"));
        this.embeddedPlayer = parseData("player", "embedHtml");
    }
    
    /**
     * Creates an empty Entity Info.
     */
    protected EntityInfo() {
        super();
    }
    
    
    //Methods
    
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
