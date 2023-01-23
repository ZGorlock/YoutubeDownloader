/*
 * File:    EntityMetadata.java
 * Package: youtube.entity.info.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.base;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.ChannelInfo;
import youtube.entity.info.PlaylistInfo;
import youtube.util.ApiUtils;

/**
 * Defines the Entity Metadata of an Entity.
 */
public class EntityMetadata extends EntityData {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EntityMetadata.class);
    
    
    //Fields
    
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
     * @param entityData The json data of the Entity.
     */
    public EntityMetadata(Map<String, Object> entityData) {
        super(entityData);
        
        this.kind = getData("kind");
        this.eTag = getData("etag");
        this.itemId = getData("id");
        
        this.channelId = getData("snippet", "channelId");
        this.channelTitle = getData("snippet", "channelTitle");
        this.channel = ApiUtils.fetchChannel(channelId);
        
        this.playlistId = getData("snippet", "playlistId");
        this.playlist = ApiUtils.fetchPlaylist(playlistId);
        
        this.entityId = itemId;
    }
    
    /**
     * Creates an empty Entity Metadata.
     */
    public EntityMetadata() {
    }
    
    
    //Methods
    
    /**
     * Returns a string representation of the Entity Metadata.
     *
     * @return a string representation of the Entity Metadata.
     */
    @Override
    public String toString() {
        return Stream.of(getKind(), getEntityId())
                .filter(Objects::nonNull).collect(Collectors.joining(":"));
    }
    
    
    //Getters
    
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
