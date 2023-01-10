/*
 * File:    TagList.java
 * Package: youtube.entity.info.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.detail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the Tag List of an Entity.
 */
public class TagList extends ArrayList<TagList.Tag> {
    
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
    public TagList(List<String> entityTags) {
        Optional.ofNullable(entityTags)
                .stream().flatMap(Collection::stream)
                .distinct().map(Tag::new)
                .forEachOrdered(this::add);
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Tag.
     */
    public static class Tag {
        
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
            this.name = name;
        }
        
        
        //Methods
        
        /**
         * Returns a string representation of the Tag.
         *
         * @return A string representation of the Tag.
         */
        @Override
        public String toString() {
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
