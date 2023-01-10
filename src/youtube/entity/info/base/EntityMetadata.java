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
import youtube.entity.info.ChannelInfo;
import youtube.entity.info.PlaylistInfo;
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
    public Map<String, Object> data;
    
    /**
     * The kind of the Entity.
     */
    public String kind;
    
    /**
     * The eTag of the Entity.
     */
    public String eTag;
    
    /**
     * The item id of the Entity.
     */
    public String itemId;
    
    /**
     * The id of the Channel containing the Entity.
     */
    public String channelId;
    
    /**
     * The title of the Channel containing the Entity.
     */
    public String channelTitle;
    
    /**
     * The Channel containing the Entity.
     */
    public ChannelInfo channel;
    
    /**
     * The id of the Playlist containing the Entity.
     */
    public String playlistId;
    
    /**
     * The Playlist containing the Entity.
     */
    public PlaylistInfo playlist;
    
    /**
     * The id of the Entity.
     */
    public String entityId;
    
    
    //Constructors
    
    /**
     * Creates the Metadata for an Entity.
     *
     * @param entityData The json data of the Entity,
     */
    protected EntityMetadata(Map<String, Object> entityData) {
        this.data = entityData;
        
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
     * @return The part of the raw data of the Entity, or an empty map if it does not exist.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDataPart(String part) {
        return Optional.ofNullable(part)
                .map(e -> (Map<String, Object>) getData().getOrDefault(e, Map.of()))
                .orElseGet(this::getData);
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
        return (T) Optional.ofNullable(getDataPart(part))
                .map(e -> e.get(field))
                .orElse(null);
    }
    
    /**
     * Returns an element from the default part of the raw data of the Entity.
     *
     * @param field The name of the data element.
     * @param <T>   The type of the element.
     * @return The element from a default part of the raw data of the Entity, or null if it does not exist.
     */
    public <T> T getData(String field) {
        return getData(null, field);
    }
    
    /**
     * Returns a string representation of the Entity Metadata.
     *
     * @return a string representation of the Entity Metadata.
     */
    @Override
    public String toString() {
        return getKind() + Optional.ofNullable(getEntityId()).map(e -> (":" + e)).orElse("");
    }
    
    
    //Getters
    
    /**
     * Returns the raw json data of the Entity.
     *
     * @return the raw json data of the Entity.
     */
    public Map<String, Object> getData() {
        return data;
    }
    
    /**
     * Returns the kind of the Entity.
     *
     * @return The kind of the Entity.
     */
    public String getKind() {
        return kind;
    }
    
    /**
     * Returns the eTag of the Entity.
     *
     * @return The eTag of the Entity.
     */
    public String getETag() {
        return eTag;
    }
    
    /**
     * Returns the item id of the Entity.
     *
     * @return The item id of the Entity.
     */
    public String getItemId() {
        return itemId;
    }
    
    /**
     * Returns id of the Channel containing the Entity.
     *
     * @return The id of the Channel containing the Entity.
     */
    public String getChannelId() {
        return channelId;
    }
    
    /**
     * Returns title of the Channel containing the Entity.
     *
     * @return The title of the Channel containing the Entity.
     */
    public String getChannelTitle() {
        return channelTitle;
    }
    
    /**
     * Returns the Channel containing the Entity.
     *
     * @return The Channel containing the Entity.
     */
    public ChannelInfo getChannel() {
        return channel;
    }
    
    /**
     * Returns the id of the Playlist containing the Entity.
     *
     * @return The id of the Playlist containing the Entity.
     */
    public String getPlaylistId() {
        return playlistId;
    }
    
    /**
     * Returns the Playlist containing the Entity.
     *
     * @return The Playlist containing the Entity.
     */
    public PlaylistInfo getPlaylist() {
        return playlist;
    }
    
    /**
     * Returns the id of the Entity.
     *
     * @return The id of the Entity.
     */
    public String getEntityId() {
        return entityId;
    }
    
}
