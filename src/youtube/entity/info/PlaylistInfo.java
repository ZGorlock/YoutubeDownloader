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
import youtube.entity.info.base.EntityInfo;
import youtube.util.WebUtils;

/**
 * Defines the Playlist Info of a Youtube Playlist.
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
     * Creates a Playlist Info.
     *
     * @param playlistData The json data of the Playlist.
     */
    public PlaylistInfo(Map<String, Object> playlistData) {
        super(playlistData);
        
        this.playlistId = metadata.getEntityId();
        this.url = WebUtils.PLAYLIST_BASE + playlistId;
        
        this.videoCount = getData("contentDetails", "itemCount");
    }
    
    /**
     * Creates an empty Playlist Info.
     */
    public PlaylistInfo() {
        super();
    }
    
    
    //Getters
    
    /**
     * Returns the id of the Playlist.
     *
     * @return The id of the Playlist.
     */
    public String getPlaylistId() {
        return playlistId;
    }
    
    /**
     * Returns the number of videos in the Playlist.
     *
     * @return The number of videos in the Playlist.
     */
    public Long getVideoCount() {
        return videoCount;
    }
    
}
