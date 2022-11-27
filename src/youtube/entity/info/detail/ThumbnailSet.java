/*
 * File:    ThumbnailSet.java
 * Package: youtube.entity.info.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.detail;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.base.Entity;

/**
 * Defines the Thumbnail Set of an Entity.
 */
public class ThumbnailSet extends LinkedHashMap<ThumbnailSet.Quality, ThumbnailSet.Thumbnail> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ThumbnailSet.class);
    
    
    //Enums
    
    /**
     * An enumeration of Thumbnail Qualities.
     */
    public enum Quality {
        DEFAULT,
        MEDIUM,
        HIGH,
        STANDARD,
        MAXRES
    }
    
    
    //Constructors
    
    /**
     * Creates the Thumbnail Set for an Entity.
     *
     * @param thumbnailData The Thumbnail Set json data of the Entity.
     */
    @SuppressWarnings("unchecked")
    public ThumbnailSet(Map<String, Object> thumbnailData) {
        Optional.ofNullable(thumbnailData)
                .map(Map::entrySet).stream().flatMap(Collection::stream)
                .filter(e -> (e.getValue() != null))
                .map(e -> new Thumbnail(e.getKey(), (Map<String, Object>) e.getValue()))
                .sorted(Comparator.comparingInt(o -> o.quality.ordinal()))
                .forEachOrdered(e -> put(e.quality, e));
    }
    
    
    //Methods
    
    /**
     * Returns the best Thumbnail.
     *
     * @return The best Thumbnail.
     */
    public Thumbnail getBest() {
        return values().stream()
                .max(Comparator.comparingLong(Thumbnail::getSize))
                .orElse(null);
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Thumbnail.
     */
    public static class Thumbnail {
        
        //Fields
        
        /**
         * The quality of the Thumbnail.
         */
        public Quality quality;
        
        /**
         * The url of the Thumbnail.
         */
        public String url;
        
        /**
         * The width of the Thumbnail.
         */
        public Long width;
        
        /**
         * The height of the Thumbnail.
         */
        public Long height;
        
        
        //Constructors
        
        /**
         * Creates a Thumbnail.
         *
         * @param quality       The quality of the Thumbnail.
         * @param thumbnailData The json data of the Thumbnail.
         */
        public Thumbnail(String quality, Map<String, Object> thumbnailData) {
            this.quality = Quality.valueOf(quality.toUpperCase());
            this.url = (String) thumbnailData.get("url");
            this.width = Entity.integerParser.apply(thumbnailData.get("width"));
            this.height = Entity.integerParser.apply(thumbnailData.get("height"));
        }
        
        
        //Methods
        
        /**
         * Returns the size of the Thumbnail.
         *
         * @return The size of the Thumbnail.
         */
        public long getSize() {
            return ((width == null) || (height == null)) ? -1 :
                   (width * height);
        }
        
    }
    
}
