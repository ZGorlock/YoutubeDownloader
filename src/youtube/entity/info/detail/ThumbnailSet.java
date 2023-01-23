/*
 * File:    ThumbnailSet.java
 * Package: youtube.entity.info.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.detail;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.detail.base.EntityDetail;
import youtube.entity.info.detail.base.EntityDetailSet;

/**
 * Defines the Thumbnail Set of an Entity.
 */
public class ThumbnailSet extends EntityDetailSet<ThumbnailSet.Thumbnail> {
    
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
    public ThumbnailSet(Map<String, Object> thumbnailData) {
        super(thumbnailData);
        
        Optional.ofNullable(thumbnailData)
                .map(Map::entrySet).stream().flatMap(Collection::stream)
                .filter(e -> (e.getKey() != null) && (e.getValue() != null))
                .map(Thumbnail::new)
                .sorted(Comparator.comparingInt(o -> o.getQuality().ordinal()))
                .forEachOrdered(this::add);
    }
    
    /**
     * Creates an empty Thumbnail Set.
     */
    public ThumbnailSet() {
        super();
    }
    
    
    //Methods
    
    /**
     * Returns the best Thumbnail.
     *
     * @return The best Thumbnail.
     */
    public Thumbnail getBest() {
        return getAll().stream()
                .max(Comparator.comparingLong(Thumbnail::getSize))
                .orElse(null);
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Thumbnail.
     */
    public static class Thumbnail extends EntityDetail {
        
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
         * @param thumbnailData The json data of the Thumbnail.
         */
        public Thumbnail(Map.Entry<String, Object> thumbnailData) {
            super(Map.ofEntries(thumbnailData));
            
            this.quality = Quality.valueOf(thumbnailData.getKey().toUpperCase());
            this.url = getData(thumbnailData.getKey(), "url");
            this.width = integerParser.apply(getData(thumbnailData.getKey(), "width"));
            this.height = integerParser.apply(getData(thumbnailData.getKey(), "height"));
        }
        
        /**
         * Creates a Thumbnail.
         *
         * @param quality The quality of the Thumbnail.
         * @param url     The url of the Thumbnail.
         * @param width   The width of the Thumbnail.
         * @param height  The height of the Thumbnail.
         */
        public Thumbnail(Quality quality, String url, Long width, Long height) {
            super();
            
            this.quality = quality;
            this.url = url;
            this.width = width;
            this.height = height;
        }
        
        /**
         * Creates an empty Thumbnail.
         */
        public Thumbnail() {
            super();
        }
        
        
        //Methods
        
        /**
         * Returns the size of the Thumbnail.
         *
         * @return The size of the Thumbnail.
         */
        public long getSize() {
            return ((getWidth() == null) || (getHeight() == null)) ? -1 :
                   (getWidth() * getHeight());
        }
        
        /**
         * Returns the key of the Thumbnail.
         *
         * @return The key of the Thumbnail.
         */
        @Override
        protected String getKey() {
            return getQuality().name().toLowerCase();
        }
        
        
        //Getters
        
        /**
         * Returns the quality of the Thumbnail.
         *
         * @return The quality of the Thumbnail.
         */
        public Quality getQuality() {
            return quality;
        }
        
        /**
         * Returns the url of the Thumbnail.
         *
         * @return The url of the Thumbnail.
         */
        public String getUrl() {
            return url;
        }
        
        /**
         * Returns the width of the Thumbnail.
         *
         * @return The width of the Thumbnail.
         */
        public Long getWidth() {
            return width;
        }
        
        /**
         * Returns the height of the Thumbnail.
         *
         * @return The height of the Thumbnail.
         */
        public Long getHeight() {
            return height;
        }
        
    }
    
}
