/*
 * File:    ChannelInfo.java
 * Package: youtube.entity.info
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.base.EntityInfo;
import youtube.entity.info.detail.Statistics;
import youtube.util.WebUtils;

/**
 * Defines the Channel Info of a Youtube Channel.
 */
public class ChannelInfo extends EntityInfo {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelInfo.class);
    
    
    //Fields
    
    /**
     * The id of the Channel.
     */
    public String channelId;
    
    /**
     * The custom url key of the Channel.
     */
    public String customUrlKey;
    
    /**
     * The custom url of the Channel.
     */
    public String customUrl;
    
    /**
     * The number of videos in the Channel.
     */
    public Long videoCount;
    
    
    //Constructors
    
    /**
     * Creates a Channel Info.
     *
     * @param channelData The json data of the Channel.
     */
    public ChannelInfo(Map<String, Object> channelData) {
        super(channelData);
        
        this.channelId = metadata.getEntityId();
        this.url = WebUtils.CHANNEL_BASE + channelId;
        
        this.customUrlKey = parseData("snippet", "customUrl");
        this.customUrl = Optional.ofNullable(customUrlKey).map(e -> e.replaceAll("^@*", WebUtils.CHANNEL_BASE)).orElse(url);
        
        this.videoCount = Optional.ofNullable(getStats()).map(e -> e.get("videoCount")).map(Statistics.Stat::getCount).orElse(null);
    }
    
    /**
     * Creates an empty Channel Info.
     */
    public ChannelInfo() {
        super();
    }
    
    
    //Getters
    
    /**
     * Returns the id of the Channel.
     *
     * @return The id of the Channel.
     */
    public String getChannelId() {
        return channelId;
    }
    
    /**
     * Returns the custom url key of the Channel.
     *
     * @return The custom url key of the Channel.
     */
    public String getCustomUrlKey() {
        return customUrlKey;
    }
    
    /**
     * Returns the custom url of the Channel.
     *
     * @return The custom url of the Channel.
     */
    public String getCustomUrl() {
        return customUrl;
    }
    
    /**
     * Returns the number of videos in the Channel.
     *
     * @return The number of videos in the Channel.
     */
    public Long getVideoCount() {
        return videoCount;
    }
    
}
