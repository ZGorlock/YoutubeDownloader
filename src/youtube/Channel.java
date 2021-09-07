/*
 * File:    Channel.java
 * Package: youtube
 * Author:  Zachary Gill
 */

package youtube;

import java.io.File;

/**
 * Defines a Channel of the Youtube Channel Downloader.
 */
public class Channel {
    
    //Fields
    
    /**
     * The key of the Channel.
     */
    public String key;
    
    /**
     * A flag indicating whether or not a Channel is enabled or not.
     */
    public boolean active;
    
    /**
     * The name of the Channel.
     */
    public String name;
    
    /**
     * The group of the Channel.
     */
    public String group;
    
    /**
     * The url of the Channel.
     */
    public String url;
    
    /**
     * The Playlist ID of the Channel.
     */
    public String playlistId;
    
    /**
     * The output folder to store the videos that are downloaded from the Channel.
     */
    public File outputFolder;
    
    /**
     * A flag indicating whether or not to save the videos from the Channel as an mp3 file or not; mp4 otherwise.
     */
    public boolean saveAsMp3;
    
    /**
     * The playlist file to add mp3 files downloaded from the Channel to if saving as mp3s; or null.
     */
    public File playlistFile;
    
    /**
     * A flag indicating whether or not to delete files from the output directory that are not in the playlist anymore.
     */
    public boolean keepClean;
    
    /**
     * A flag indicating whether there was an error retrieving the Channel this run or not.
     */
    public boolean error;
    
    
    //Methods
    
    /**
     * Returns whether the Channel is a Playlist or not.
     *
     * @return Whether the Channel is a Playlist or not.
     */
    public boolean isPlaylist() {
        return playlistId.startsWith("PL");
    }
    
    /**
     * Returns whether the Channel is a Channel or not.
     *
     * @return Whether the Channel is a Channel or not.
     */
    public boolean isChannel() {
        return playlistId.startsWith("UU");
    }
    
}
