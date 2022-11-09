/*
 * File:    EntityMetadata.java
 * Package: youtube.channel.entity.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity.base;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public final String kind;
    
    /**
     * The eTag of the Entity.
     */
    public final String eTag;
    
    /**
     * The Youtube id of the Entity.
     */
    public final String itemId;
    
    /**
     * The Youtube channel id of the Entity.
     */
    public final String channelId;
    
    /**
     * The Youtube channel title of the Entity.
     */
    public final String channelTitle;
    
    /**
     * The Youtube playlist id of the Entity.
     */
    public final String playlistId;
    
    
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
        
        this.playlistId = (String) snippet.get("playlistId");
        
        entityData.clear();
        entityData.putAll(snippet);
    }
    
}
