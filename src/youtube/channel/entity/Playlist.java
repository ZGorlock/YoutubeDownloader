/*
 * File:    Playlist.java
 * Package: youtube.channel.entity
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
import youtube.util.Utils;
import youtube.util.WebUtils;

/**
 * Defines a Playlist.
 */
public class Playlist {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Playlist.class);
    
    
    //Fields
    
    /**
     * The Channel containing the Playlist.
     */
    public Channel channel;
    
    /**
     * The ID of the Playlist.
     */
    public String playlistId;
    
    /**
     * The original title of the Playlist.
     */
    public String originalTitle;
    
    /**
     * The title of the Playlist.
     */
    public String title;
    
    /**
     * The url of the Playlist.
     */
    public String url;
    
    /**
     * The date the Playlist was uploaded.
     */
    public Date date;
    
    
    //Constructors
    
    /**
     * Creates a Playlist.
     *
     * @param playlistId The ID of the Playlist.
     * @param title      The title of the Playlist.
     * @param date       The date the Playlist was uploaded.
     */
    public Playlist(String playlistId, String title, String date, Channel channel) {
        this.channel = channel;
        this.playlistId = playlistId;
        this.originalTitle = title;
        this.title = Utils.cleanVideoTitle(title);
        this.url = WebUtils.PLAYLIST_BASE + playlistId;
        try {
            this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date
                    .replace("T", " ").replace("Z", ""));
        } catch (ParseException ignored) {
            this.date = new Date();
        }
    }
    
    /**
     * The default no-argument constructor for a Playlist.
     */
    public Playlist() {
    }
    
    
    //Methods
    
    /**
     * Returns the string representation of the Playlist.
     *
     * @return the string representation of the Playlist.
     */
    @Override
    public String toString() {
        return title;
    }
    
}
