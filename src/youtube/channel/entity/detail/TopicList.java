/*
 * File:    TopicList.java
 * Package: youtube.channel.entity.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity.detail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the Topic List of an Entity.
 */
public class TopicList extends ArrayList<TopicList.Topic> {
    
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
    public TopicList(List<String> entityTopics) {
        Optional.ofNullable(entityTopics)
                .stream().flatMap(Collection::stream)
                .distinct().map(Topic::new)
                .forEachOrdered(this::add);
    }
    
    
    //Methods
    
    /**
     * Returns the list of Topics.
     *
     * @return The list of Topics.
     */
    public List<Topic> getAll() {
        return new ArrayList<>(this);
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Topic.
     */
    public static class Topic {
        
        //Fields
        
        /**
         * The url of the Topic.
         */
        public final String url;
        
        
        //Constructors
        
        /**
         * Creates a Topic.
         *
         * @param url The url of the Topic.
         */
        public Topic(String url) {
            this.url = url;
        }
        
        
        //Methods
        
        /**
         * Returns a string representation of the Topic.
         *
         * @return A string representation of the Topic.
         */
        @Override
        public String toString() {
            return url;
        }
        
    }
    
}
