/*
 * File:    TopicList.java
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
 * Defines the Topic List of an Entity.
 */
public class TopicList extends EntityDetailSet<TopicList.Topic> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(TopicList.class);
    
    
    //Constructors
    
    /**
     * Creates the Topic List for an Entity.
     *
     * @param entityTopics The topic list of the Entity.
     */
    public TopicList(List<Object> entityTopics) {
        super();
        
        Optional.ofNullable(entityTopics)
                .stream().flatMap(Collection::stream)
                .filter(Objects::nonNull).map(String::valueOf)
                .filter(e -> !e.isEmpty())
                .distinct().map(Topic::new)
                .forEachOrdered(this::add);
    }
    
    /**
     * Creates an empty Topic List.
     */
    public TopicList() {
        super();
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Topic.
     */
    public static class Topic extends EntityDetail {
        
        //Fields
        
        /**
         * The url of the Topic.
         */
        public String url;
        
        
        //Constructors
        
        /**
         * Creates a Topic.
         *
         * @param url The url of the Topic.
         */
        public Topic(String url) {
            super();
            
            this.url = url;
        }
        
        /**
         * Creates an empty Topic.
         */
        public Topic() {
            super();
        }
        
        
        //Methods
        
        /**
         * Returns the key of the Topic.
         *
         * @return The key of the Topic.
         */
        @Override
        protected String getKey() {
            return getUrl();
        }
        
        
        //Getters
        
        /**
         * Returns the url of the Topic.
         *
         * @return The url of the Topic.
         */
        public String getUrl() {
            return url;
        }
        
    }
    
}
