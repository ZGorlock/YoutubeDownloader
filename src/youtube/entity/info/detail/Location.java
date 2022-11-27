/*
 * File:    Location.java
 * Package: youtube.entity.info.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.detail;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.base.EntityInfo;

/**
 * Defines the Location of a Video Entity.
 */
public class Location {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Location.class);
    
    
    //Fields
    
    /**
     * The description of the Location.
     */
    public String description;
    
    /**
     * The latitude of the Location.
     */
    public Double latitude;
    
    /**
     * The longitude of the Location.
     */
    public Double longitude;
    
    /**
     * The altitude of the Location.
     */
    public Double altitude;
    
    
    //Constructors
    
    /**
     * Creates the Location for a Video Entity.
     *
     * @param locationData The Location json data of the Video Entity.
     */
    @SuppressWarnings("unchecked")
    public Location(Map<String, Object> locationData) {
        Optional.ofNullable(locationData)
                .map(location -> {
                    this.description = (String) location.get("locationDescription");
                    return (Map<String, Object>) location.get("location");
                }).ifPresent(coordinates -> {
                    this.latitude = EntityInfo.numberParser.apply(coordinates.get("latitude"));
                    this.longitude = EntityInfo.numberParser.apply(coordinates.get("longitude"));
                    this.altitude = EntityInfo.numberParser.apply(coordinates.get("altitude"));
                });
    }
    
}
