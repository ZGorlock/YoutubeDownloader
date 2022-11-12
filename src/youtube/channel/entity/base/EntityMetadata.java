/*
 * File:    EntityMetadata.java
 * Package: youtube.channel.entity.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity.base;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.entity.Channel;
import youtube.channel.entity.Playlist;
import youtube.util.ApiUtils;

/**
 * Defines the Metadata of an Entity.
 */
public class EntityMetadata {
    
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
    @SuppressWarnings("unchecked")
    protected EntityMetadata(Map<String, Object> entityData) {
        final Map<String, Object> snippet = (Map<String, Object>) entityData.get("snippet");
        
        this.kind = (String) entityData.get("kind");
        this.eTag = (String) entityData.get("etag");
        this.itemId = (String) entityData.get("id");
        
        this.channelId = (String) snippet.get("channelId");
        this.channelTitle = (String) snippet.get("channelTitle");
        this.channel = Optional.ofNullable(channelId).map(ApiUtils::fetchChannel).orElse(null);
        
        this.playlistId = (String) snippet.get("playlistId");
        this.playlist = Optional.ofNullable(playlistId).map(ApiUtils::fetchPlaylist).orElse(null);
        
        entityData.clear();
        entityData.putAll(snippet);
    }
    
    /**
     * The default no-argument constructor for an EntityMetadata.
     */
    protected EntityMetadata() {
    }
    
}
