/*
 * File:    Statistics.java
 * Package: youtube.entity.info.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.detail;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.base.EntityInfo;

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
                .map(Stat::new)
                .forEachOrdered(stat -> put(stat.getName(), stat));
    }
    
    /**
     * Creates an empty Statistics.
     */
    public Statistics() {
        super();
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
         * @param statData The json data of the Stat.
         */
        public Stat(Map.Entry<String, Object> statData) {
            Optional.ofNullable(statData)
                    .ifPresent(statDetails -> {
                        this.name = statDetails.getKey();
                        this.count = EntityInfo.integerParser.apply(statDetails.getValue());
                    });
        }
        
        /**
         * Creates a Stat.
         *
         * @param name  The name of the Stat.
         * @param count The count of the Stat.
         */
        public Stat(String name, Object count) {
            this.name = name;
            this.count = EntityInfo.integerParser.apply(count);
        }
        
        /**
         * Creates an empty Stat.
         */
        public Stat() {
        }
        
        
        //Methods
        
        /**
         * Returns a string representation of the Stat.
         *
         * @return A string representation of the Stat.
         */
        @Override
        public String toString() {
            return getName() + ": " + getCount();
        }
        
        
        //Getters
        
        /**
         * Returns the name of the Stat.
         *
         * @return The name of the Stat.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Returns the count of the Stat.
         *
         * @return The count of the Stat.
         */
        public Long getCount() {
            return count;
        }
        
    }
    
}
