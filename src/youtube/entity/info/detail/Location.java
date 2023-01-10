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
 * Defines the Location of a Youtube Video.
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
     * Creates the Location for a Video.
     *
     * @param locationData The Location json data of the Video.
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
    
    
    //Getters
    
    /**
     * Returns the description of the Location.
     *
     * @return The description of the Location.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns the latitude of the Location.
     *
     * @return The latitude of the Location.
     */
    public Double getLatitude() {
        return latitude;
    }
    
    /**
     * Returns the longitude of the Location.
     *
     * @return The longitude of the Location.
     */
    public Double getLongitude() {
        return longitude;
    }
    
    /**
     * Returns the altitude of the Location.
     *
     * @return The altitude of the Location.
     */
    public Double getAltitude() {
        return altitude;
    }
    
}
