/*
 * File:    ThumbnailSet.java
 * Package: youtube.channel.entity.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity.detail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .map(e -> new Thumbnail(e.getKey(), (Map<String, Object>) e.getValue()))
                .sorted(Comparator.comparingInt(o -> o.quality.ordinal()))
                .forEachOrdered(e -> put(e.quality, e));
    }
    
    
    //Methods
    
    /**
     * Returns the list of Thumbnails.
     *
     * @return The list of Thumbnails.
     */
    public List<Thumbnail> getAll() {
        return new ArrayList<>(values());
    }
    
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
        public final Quality quality;
        
        /**
         * The url of the Thumbnail.
         */
        public final String url;
        
        /**
         * The width of the Thumbnail.
         */
        public final Long width;
        
        /**
         * The height of the Thumbnail.
         */
        public final Long height;
        
        
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
            this.width = (Long) thumbnailData.get("width");
            this.height = (Long) thumbnailData.get("height");
        }
        
        
        //Methods
        
        /**
         * Returns the size of the Thumbnail.
         *
         * @return The size of the Thumbnail.
         */
        public long getSize() {
            return width * height;
        }
        
    }
    
}
