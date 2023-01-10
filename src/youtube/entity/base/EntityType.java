/*
 * File:    EntityType.java
 * Package: youtube.entity.base
 * Author:  Zachary Gill
 */

package youtube.entity.base;

import commons.object.string.StringUtility;
import youtube.entity.Channel;
import youtube.entity.Playlist;
import youtube.entity.Video;
import youtube.entity.info.ChannelInfo;
import youtube.entity.info.PlaylistInfo;
import youtube.entity.info.VideoInfo;
import youtube.entity.info.base.EntityInfo;

/**
 * An enumeration of Entity Types.
 */
public enum EntityType {
    
    //Values
    
    CHANNEL(Channel.class, ChannelInfo.class),
    PLAYLIST(Playlist.class, PlaylistInfo.class),
    VIDEO(Video.class, VideoInfo.class);
    
    
    //Fields
    
    /**
     * The name of the Entity Type.
     */
    public final String name;
    
    /**
     * The Entity class of the Entity Type.
     */
    public final Class<? extends EntityInterface<?>> entityClass;
    
    /**
     * The Entity Info class of the Entity Type.
     */
    public final Class<? extends EntityInfo> entityInfoClass;
    
    
    //Constructors
    
    /**
     * Constructs an EntityType.
     *
     * @param entityClass     The Entity class of the Entity Type.
     * @param entityInfoClass The Entity Info class of the Entity Type.
     */
    EntityType(Class<? extends EntityInterface<?>> entityClass, Class<? extends EntityInfo> entityInfoClass) {
        this.name = StringUtility.toTitleCase(name());
        this.entityClass = entityClass;
        this.entityInfoClass = entityInfoClass;
    }
    
    
    //Getters
    
    /**
     * Returns the name of the Entity Type.
     *
     * @return The name of the Entity Type.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the Entity class of the Entity Type.
     *
     * @return The Entity class of the Entity Type.
     */
    public Class<? extends EntityInterface<?>> getEntityClass() {
        return entityClass;
    }
    
    /**
     * Returns the Entity Info class of the Entity Type.
     *
     * @return The Entity Info class of the Entity Type.
     */
    public Class<? extends EntityInfo> getEntityInfoClass() {
        return entityInfoClass;
    }
    
}
