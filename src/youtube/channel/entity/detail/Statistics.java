/*
 * File:    Statistics.java
 * Package: youtube.channel.entity.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity.detail;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.entity.base.Entity;

/**
 * Defines the Statistics of an Entity.
 */
public class Statistics extends LinkedHashMap<String, Statistics.Stat> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Statistics.class);
    
    
    //Constructors
    
    /**
     * Creates the Statistics for an Entity.
     *
     * @param statisticsData The Statistics json data of the Entity.
     */
    public Statistics(Map<String, Object> statisticsData) {
        Optional.ofNullable(statisticsData)
                .map(Map::entrySet).stream().flatMap(Collection::stream)
                .map(e -> new Stat(e.getKey(), e.getValue()))
                .forEachOrdered(stat -> put(stat.name, stat));
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Stat.
     */
    public static class Stat {
        
        //Fields
        
        /**
         * The name of the Stat.
         */
        public String name;
        
        /**
         * The count of the Stat.
         */
        public Long count;
        
        
        //Constructors
        
        /**
         * Creates a Stat.
         *
         * @param name  The name of the Stat.
         * @param count The count of the Stat.
         */
        public Stat(String name, Object count) {
            this.name = name;
            this.count = Entity.integerParser.apply(count);
        }
        
        
        //Methods
        
        /**
         * Returns a string representation of the Stat.
         *
         * @return A string representation of the Stat.
         */
        @Override
        public String toString() {
            return String.valueOf(count);
        }
        
    }
    
}
