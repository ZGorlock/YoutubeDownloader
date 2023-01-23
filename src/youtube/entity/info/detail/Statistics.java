/*
 * File:    Statistics.java
 * Package: youtube.entity.info.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.detail;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.detail.base.EntityDetail;
import youtube.entity.info.detail.base.EntityDetailSet;

/**
 * Defines the Statistics of an Entity.
 */
public class Statistics extends EntityDetailSet<Statistics.Stat> {
    
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
        super(statisticsData);
        
        Optional.ofNullable(statisticsData)
                .map(Map::entrySet).stream().flatMap(Collection::stream)
                .filter(e -> (e.getKey() != null) && (e.getValue() != null))
                .map(Stat::new)
                .forEachOrdered(this::add);
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
    public static class Stat extends EntityDetail {
        
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
            super(Map.ofEntries(statData));
            
            this.name = statData.getKey();
            this.count = parseLong(statData.getKey()).orElse(null);
        }
        
        /**
         * Creates a Stat.
         *
         * @param name  The name of the Stat.
         * @param count The count of the Stat.
         */
        public Stat(String name, Long count) {
            super();
            
            this.name = name;
            this.count = count;
        }
        
        /**
         * Creates an empty Stat.
         */
        public Stat() {
            super();
        }
        
        
        //Methods
        
        /**
         * Returns the key of the Stat.
         *
         * @return The key of the Stat.
         */
        @Override
        protected String getKey() {
            return getName();
        }
        
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
