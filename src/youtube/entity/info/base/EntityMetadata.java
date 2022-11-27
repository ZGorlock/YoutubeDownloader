/*
 * File:    EntityMetadata.java
 * Package: youtube.entity.info.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.base;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.Channel;
import youtube.entity.info.Playlist;
import youtube.util.ApiUtils;

/**
 * Defines the Metadata of a Youtube Entity.
 */
public class EntityMetadata {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EntityMetadata.class);
    
    
    //Fields
    
    /**
     * The raw json data of the Entity.
     */
    public Map<String, Object> rawData;
    
    /**
     * The kind of the Entity.
     */
    public String kind;
    
    /**
     * The eTag of the Entity.
     */
    public String eTag;
    
    /**
     * The Youtube item id of the Entity.
     */
    public String itemId;
    
    /**
     * The Youtube channel id of the Entity.
     */
    public String channelId;
    
    /**
     * The Youtube channel title of the Entity.
     */
    public String channelTitle;
    
    /**
     * The Youtube channel entity of the Entity.
     */
    public Channel channel;
    
    /**
     * The Youtube playlist id of the Entity.
     */
    public String playlistId;
    
    /**
     * The Youtube playlist entity of the Entity.
     */
    public Playlist playlist;
    
    /**
     * The Youtube id of the Entity.
     */
    public String entityId;
    
    
    //Constructors
    
    /**
     * Creates the Metadata for an Entity.
     *
     * @param entityData The json data of the Entity,
     */
    protected EntityMetadata(Map<String, Object> entityData) {
        this.rawData = entityData;
        
        this.kind = getData("kind");
        this.eTag = getData("etag");
        this.itemId = getData("id");
        
        this.channelId = getData("snippet", "channelId");
        this.channelTitle = getData("snippet", "channelTitle");
        this.channel = ApiUtils.fetchChannel(channelId);
        
        this.playlistId = getData("snippet", "playlistId");
        this.playlist = ApiUtils.fetchPlaylist(playlistId);
    }
    
    /**
     * The default no-argument constructor for an EntityMetadata.
     */
    protected EntityMetadata() {
    }
    
    
    //Methods
    
    /**
     * Returns a part of the raw data of the Entity.
     *
     * @param part The name of the data part, or null for the root part.
     * @return The part of the raw data of the Entity, or null if it does not exist.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDataPart(String part) {
        return Optional.ofNullable(part).map(e -> (Map<String, Object>) rawData.get(e)).orElse(null);
    }
    
    /**
     * Returns an element from a specific part of the raw data of the Entity.
     *
     * @param part  The name of the data part.
     * @param field The name of the data element.
     * @param <T>   The type of the element.
     * @return The element from a specific part of the raw data of the Entity, or null if it does not exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String part, String field) {
        return (T) Optional.ofNullable(getDataPart(part)).map(e -> e.get(field)).orElse(null);
    }
    
    /**
     * Returns an element from the default part of the raw data of the Entity.
     *
     * @param field The name of the data element.
     * @param <T>   The type of the element.
     * @return The element from a default part of the raw data of the Entity, or null if it does not exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String field) {
        return (T) rawData.get(field);
    }
    
}
