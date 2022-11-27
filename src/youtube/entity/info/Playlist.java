/*
 * File:    Playlist.java
 * Package: youtube.entity.info
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
import youtube.entity.info.base.Entity;
import youtube.util.WebUtils;

/**
 * Defines a Youtube Playlist.
 */
public class Playlist extends Entity {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Playlist.class);
    
    
    //Fields
    
    /**
     * The id of the Playlist.
     */
    public String playlistId;
    
    /**
     * The number of videos in the Playlist.
     */
    public Long videoCount;
    
    
    //Constructors
    
    /**
     * Creates a Playlist.
     *
     * @param playlistData The json data of the Playlist.
     * @param channel      The Channel containing the Playlist Entity.
     */
    public Playlist(Map<String, Object> playlistData, Channel channel) {
        super(playlistData, channel);
        
        this.playlistId = metadata.itemId;
        this.metadata.entityId = playlistId;
        
        this.url = WebUtils.PLAYLIST_BASE + playlistId;
        
        this.videoCount = getData("contentDetails", "itemCount");
    }
    
    /**
     * Creates a Playlist.
     *
     * @param playlistData The json data of the Playlist.
     */
    public Playlist(Map<String, Object> playlistData) {
        this(playlistData, null);
    }
    
    /**
     * The default no-argument constructor for a Playlist.
     */
    public Playlist() {
        super();
    }
    
}
