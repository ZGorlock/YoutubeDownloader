/*
 * File:    TagList.java
 * Package: youtube.entity.info.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.detail;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.detail.base.EntityDetail;
import youtube.entity.info.detail.base.EntityDetailSet;

/**
 * Defines the Tag List of an Entity.
 */
public class TagList extends EntityDetailSet<EntityDetail> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(TagList.class);
    
    
    //Constructors
    
    /**
     * Creates the Tag List for an Entity.
     *
     * @param entityTags The tag list of the Entity.
     */
    public TagList(List<Object> entityTags) {
        super();
        
        Optional.ofNullable(entityTags)
                .stream().flatMap(Collection::stream)
                .filter(Objects::nonNull).map(String::valueOf)
                .filter(e -> !e.isEmpty())
                .distinct().map(Tag::new)
                .forEachOrdered(this::add);
    }
    
    /**
     * Creates an empty Tag List.
     */
    public TagList() {
        super();
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Tag.
     */
    public static class Tag extends EntityDetail {
        
        //Fields
        
        /**
         * The name of the Tag.
         */
        public String name;
        
        
        //Constructors
        
        /**
         * Creates a Tag.
         *
         * @param name The name of the Tag.
         */
        public Tag(String name) {
            super();
            
            this.name = name;
        }
        
        /**
         * Creates an empty Tag.
         */
        public Tag() {
            super();
        }
        
        
        //Methods
        
        /**
         * Returns the key of the Tag.
         *
         * @return The key of the Tag.
         */
        @Override
        protected String getKey() {
            return getName();
        }
        
        
        //Getters
        
        /**
         * Returns the name of the Tag.
         *
         * @return The name of the Tag.
         */
        public String getName() {
            return name;
        }
        
    }
    
}
