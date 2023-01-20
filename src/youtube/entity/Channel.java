/*
 * File:    Channel.java
 * Package: youtube.entity
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.config.ChannelConfig;
import youtube.channel.state.ChannelState;
import youtube.entity.base.Entity;
import youtube.entity.base.EntityType;
import youtube.entity.info.ChannelInfo;
import youtube.util.ApiUtils;

/**
 * Defines a Channel.
 */
public class Channel extends Entity<ChannelInfo> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Channel.class);
    
    
    //Fields
    
    /**
     * The Channel Config associated with the Channel.
     */
    protected final ChannelConfig config;
    
    /**
     * The Channel State of the Channel.
     */
    protected final ChannelState state;
    
    
    //Constructors
    
    /**
     * Creates a Channel.
     *
     * @param channelConfig The Channel Config associated with the Channel.
     */
    public Channel(ChannelConfig channelConfig) {
        super(EntityType.CHANNEL, null);
        
        this.config = channelConfig;
        this.state = new ChannelState(config);
    }
    
    /**
     * Creates a Channel.
     *
     * @param channelInfo The Channel Info associated with the Channel.
     */
    public Channel(ChannelInfo channelInfo) {
        super(EntityType.CHANNEL, channelInfo);
        
        this.config = null;
        this.state = null;
    }
    
    
    //Methods
    
    /**
     * Returns a string representation of the Channel.
     *
     * @return a string representation of the Channel.
     */
    @Override
    public String toString() {
        return Optional.ofNullable(getConfig())
                .map(ChannelConfig::toString)
                .orElseGet(super::toString);
    }
    
    
    //Getters
    
    /**
     * Returns the Channel Info associated with the Channel.
     *
     * @return The Channel Info.
     */
    @Override
    public ChannelInfo getInfo() {
        return Optional.ofNullable(info)
                .orElseGet(() -> (info = ApiUtils.fetchChannel(this)));
    }
    
    /**
     * Returns the Channel Config associated with the Channel.
     *
     * @return The Channel Config.
     */
    @Override
    public ChannelConfig getConfig() {
        return config;
    }
    
    /**
     * Returns the Channel State of the Channel.
     *
     * @return The Channel State.
     */
    @Override
    public ChannelState getState() {
        return state;
    }
    
}
