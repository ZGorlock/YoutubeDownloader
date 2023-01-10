/*
 * File:    EntityInterface.java
 * Package: youtube.entity.base
 * Author:  Zachary Gill
 */

package youtube.entity.base;

import youtube.channel.ChannelConfig;
import youtube.channel.state.ChannelState;
import youtube.entity.Channel;
import youtube.entity.info.base.EntityInfo;

/**
 * Defines the public contract of an Entity.
 *
 * @param <T> The EntityInfo type of the Entity.
 */
public interface EntityInterface<T extends EntityInfo> {
    
    //Getters
    
    /**
     * Returns the Entity Type of the Entity.
     *
     * @return The Entity Type.
     */
    EntityType getType();
    
    /**
     * Returns the Entity Info associated with the Entity.
     *
     * @return The Entity Info.
     */
    T getInfo();
    
    /**
     * Returns the parent Channel of the Entity.
     *
     * @return The parent Channel.
     */
    Channel getParent();
    
    /**
     * Returns the Channel Config associated with the Entity.
     *
     * @return The Channel Config.
     */
    ChannelConfig getConfig();
    
    /**
     * Returns the Channel State of the Entity.
     *
     * @return The Channel State.
     */
    ChannelState getState();
    
}
