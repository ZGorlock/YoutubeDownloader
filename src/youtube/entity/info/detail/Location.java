/*
 * File:    Location.java
 * Package: youtube.entity.info.detail
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.detail;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.detail.base.EntityDetail;

/**
 * Defines the Location of a Video.
 */
public class Location extends EntityDetail {
    
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
    public Location(Map<String, Object> locationData) {
        super(locationData);
        
        this.description = getData("locationDescription");
        this.latitude = numberParser.apply(getData("location", "latitude"));
        this.longitude = numberParser.apply(getData("location", "longitude"));
        this.altitude = numberParser.apply(getData("location", "altitude"));
    }
    
    /**
     * Creates the Location for a Video.
     *
     * @param description The description of the Location.
     * @param latitude    The latitude of the Location.
     * @param longitude   The longitude of the Location.
     * @param altitude    The altitude of the Location.
     */
    public Location(String description, Double latitude, Double longitude, Double altitude) {
        super();
        
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }
    
    /**
     * Creates an empty Location.
     */
    public Location() {
        super();
    }
    
    
    //Methods
    
    /**
     * Returns a string representation of the coordinates of the Location.
     *
     * @return A string representation of the coordinates of the Location.
     */
    public String coordinateString() {
        return Stream.of(getLatitude(), getLongitude(), getAltitude())
                .filter(Objects::nonNull).map(String::valueOf)
                .collect(Collectors.joining(", "));
    }
    
    /**
     * Returns the key of the Location.
     *
     * @return The key of the Location.
     */
    @Override
    protected String getKey() {
        return getDescription();
    }
    
    /**
     * Returns a string representation of the Location.
     *
     * @return A string representation of the Location.
     */
    @Override
    public String toString() {
        return getDescription() + " [" + coordinateString() + "]";
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
