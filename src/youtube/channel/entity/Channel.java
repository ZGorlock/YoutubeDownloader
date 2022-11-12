/*
 * File:    Channel.java
 * Package: youtube.channel.entity
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.entity.base.Entity;
import youtube.util.WebUtils;

/**
 * Defines a Youtube Channel.
 */
public class Channel extends Entity {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Channel.class);
    
    
    //Fields
    
    /**
     * The id of the Channel.
     */
    public String channelId;
    
    /**
     * The custom url of the Channel.
     */
    public String customUrl;
    
    
    //Constructors
    
    /**
     * Creates a Channel.
     *
     * @param channelData The json data of the Channel.
     * @param channel     The Channel containing the Channel Entity.
     */
    public Channel(Map<String, Object> channelData, youtube.channel.Channel channel) {
        super(channelData, channel);
        
        this.channelId = metadata.itemId;
        this.metadata.entityId = channelId;
        
        this.customUrl = (String) channelData.get("customUrl");
        this.url = WebUtils.CHANNEL_BASE + Optional.ofNullable(customUrl).map(e -> e.replaceAll("^@", "")).orElse(channelId);
    }
    
    /**
     * Creates a Channel.
     *
     * @param channelData The json data of the Channel.
     */
    public Channel(Map<String, Object> channelData) {
        this(channelData, null);
    }
    
    /**
     * The default no-argument constructor for a Channel.
     */
    public Channel() {
        super();
    }
    
}
