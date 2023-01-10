/*
 * File:    Playlist.java
 * Package: youtube.entity
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.base.Entity;
import youtube.entity.base.EntityType;
import youtube.entity.info.PlaylistInfo;

/**
 * Defines a Playlist.
 */
public class Playlist extends Entity<PlaylistInfo> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Playlist.class);
    
    
    //Constructors
    
    /**
     * Creates a Playlist.
     *
     * @param playlistInfo The Playlist Info associated with the Playlist.
     * @param parent       The parent Channel of the Playlist.
     */
    public Playlist(PlaylistInfo playlistInfo, Channel parent) {
        super(EntityType.PLAYLIST, playlistInfo, parent);
    }
    
    /**
     * Creates a Playlist.
     *
     * @param playlistInfo The Playlist Info associated with the Playlist.
     */
    public Playlist(PlaylistInfo playlistInfo) {
        this(playlistInfo, null);
    }
    
}
