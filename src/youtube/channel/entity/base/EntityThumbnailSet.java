/*
 * File:    EntityThumbnailSet.java
 * Package: youtube.channel.entity.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import commons.lambda.stream.collector.MapCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a Thumbnail Set of an Entity.
 */
public class EntityThumbnailSet {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EntityThumbnailSet.class);
    
    
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
    
    
    //Fields
    
    /**
     * A map of Thumbnails for an Entity.
     */
    private final Map<Quality, Thumbnail> thumbnails;
    
    
    //Constructors
    
    /**
     * Creates the Thumbnail Set for an Entity.
     *
     * @param thumbnailSetData The json data from the Thumbnail Set.
     */
    @SuppressWarnings("unchecked")
    protected EntityThumbnailSet(Map<String, Object> thumbnailSetData) {
        this.thumbnails = Optional.ofNullable(thumbnailSetData).orElse(new HashMap<>()).entrySet().stream()
                .map(e -> new Thumbnail(e.getKey(), (Map<String, Object>) e.getValue()))
                .collect(MapCollectors.toLinkedHashMap(e -> e.quality, e -> e));
    }
    
    
    //Methods
    
    /**
     * Returns the list of Thumbnails.
     *
     * @return The list of Thumbnails.
     */
    public List<Thumbnail> getAll() {
        return new ArrayList<>(thumbnails.values());
    }
    
    /**
     * Returns a Thumbnail of a specific quality.
     *
     * @param quality The quality of the Thumbnail.
     * @return The Thumbnail of the specified quality, or null if it does not exist.
     */
    public Thumbnail get(Quality quality) {
        return thumbnails.get(quality);
    }
    
    /**
     * Returns the best Thumbnail.
     *
     * @return The best Thumbnail.
     */
    public Thumbnail getBest() {
        return thumbnails.values().stream()
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
