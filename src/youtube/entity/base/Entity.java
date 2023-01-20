/*
 * File:    Entity.java
 * Package: youtube.entity.base
 * Author:  Zachary Gill
 */

package youtube.entity.base;

import java.util.Optional;

import commons.object.string.EntityStringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.config.ChannelConfig;
import youtube.channel.state.ChannelState;
import youtube.entity.Channel;
import youtube.entity.info.base.EntityInfo;

/**
 * Defines an Entity.
 *
 * @param <T> The EntityInfo type of the Entity.
 */
public abstract class Entity<T extends EntityInfo> implements EntityInterface<T> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Entity.class);
    
    
    //Fields
    
    /**
     * The Entity Type of the Entity.
     */
    protected final EntityType type;
    
    /**
     * The Entity Info associated with the Entity.
     */
    protected T info;
    
    /**
     * The parent Channel of the Entity.
     */
    protected final Channel parent;
    
    
    //Constructors
    
    /**
     * Creates an Entity.
     *
     * @param type   The Entity Type of the Entity.
     * @param info   The Entity Info associated with the Entity.
     * @param parent The parent Channel of the Entity.
     */
    protected Entity(EntityType type, T info, Channel parent) {
        this.type = type;
        this.info = info;
        this.parent = parent;
    }
    
    /**
     * Creates an Entity.
     *
     * @param type The Entity Type of the Entity.
     * @param info The Entity Info associated with the Entity.
     */
    protected Entity(EntityType type, T info) {
        this(type, info, null);
    }
    
    
    //Methods
    
    /**
     * Returns a string representation of the Entity.
     *
     * @return a string representation of the Entity.
     */
    @Override
    public String toString() {
        return Optional.ofNullable(getInfo(true))
                .map(EntityInfo::toString)
                .orElseGet(() -> Optional.ofNullable(getType())
                        .map(EntityType::getName)
                        .orElseGet(() -> EntityStringUtility.simpleClassString(this)));
    }
    
    
    //Getters
    
    /**
     * Returns the Entity Type of the Entity.
     *
     * @return The Entity Type.
     */
    @Override
    public EntityType getType() {
        return type;
    }
    
    /**
     * Returns the Entity Info associated with the Entity.
     *
     * @return The Entity Info.
     */
    @Override
    public T getInfo() {
        return Optional.ofNullable(info)
                .orElseThrow(NullPointerException::new);
    }
    
    /**
     * Returns the Entity Info associated with the Entity.
     *
     * @param immediate Whether to return the Entity Info without additional processing or validation.
     * @return The Entity Info.
     */
    protected final T getInfo(boolean immediate) {
        return immediate ? info : getInfo();
    }
    
    /**
     * Returns the parent Channel of the Entity.
     *
     * @return The parent Channel, or null if it does not exist.
     */
    @Override
    public Channel getParent() {
        return parent;
    }
    
    /**
     * Returns the Channel Config associated with the Entity.
     *
     * @return The Channel Config, or null if it does not exist.
     */
    @Override
    public ChannelConfig getConfig() {
        return Optional.ofNullable(getParent())
                .map(Entity::getConfig)
                .orElse(null);
    }
    
    /**
     * Returns the Channel State of the Entity.
     *
     * @return The Channel State, or null if it does not exist.
     */
    @Override
    public ChannelState getState() {
        return Optional.ofNullable(getParent())
                .map(Entity::getState)
                .orElse(null);
    }
    
}
