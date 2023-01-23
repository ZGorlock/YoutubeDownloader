/*
 * File:    EntityDetail.java
 * Package: youtube.entity.info.detail.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.detail.base;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.base.EntityData;

/**
 * Defines an Entity Detail of an Entity.
 */
public abstract class EntityDetail extends EntityData {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EntityDetail.class);
    
    
    //Constructors
    
    /**
     * Creates a Detail for an Entity.
     *
     * @param entityData The json data of the Entity Detail.
     */
    protected EntityDetail(Map<String, Object> entityData) {
        super(entityData);
    }
    
    /**
     * Creates an empty Entity Detail.
     */
    protected EntityDetail() {
        super();
    }
    
    
    //Methods
    
    /**
     * Returns a string representation of the Entity Detail.
     *
     * @return A string representation of the Entity Detail.
     */
    @Override
    public String toString() {
        return getKey();
    }
    
    
    //Getters
    
    /**
     * Returns the key of the Entity Detail.
     *
     * @return The key of the Entity Detail.
     */
    protected abstract String getKey();
    
}
