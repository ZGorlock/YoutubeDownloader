/*
 * File:    PlaylistInfo.java
 * Package: youtube.entity.info
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
import youtube.entity.info.base.EntityInfo;
import youtube.util.WebUtils;

/**
 * Defines the Info of a Youtube Playlist Entity.
 */
public class PlaylistInfo extends EntityInfo {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(PlaylistInfo.class);
    
    
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
     * Creates a Playlist Entity Info.
     *
     * @param playlistData The json data of the Playlist.
     * @param channel      The Channel containing the Playlist Entity.
     */
    public PlaylistInfo(Map<String, Object> playlistData, Channel channel) {
        super(playlistData, channel);
        
        this.playlistId = metadata.itemId;
        this.metadata.entityId = playlistId;
        
        this.url = WebUtils.PLAYLIST_BASE + playlistId;
        
        this.videoCount = getData("contentDetails", "itemCount");
    }
    
    /**
     * Creates a Playlist Entity Info.
     *
     * @param playlistData The json data of the Playlist.
     */
    public PlaylistInfo(Map<String, Object> playlistData) {
        this(playlistData, null);
    }
    
    /**
     * The default no-argument constructor for a Playlist Entity Info.
     */
    public PlaylistInfo() {
        super();
    }
    
}
