/*
 * File:    Channel.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.channel;

import java.io.File;

import youtube.tools.SponsorBlocker;

/**
 * Defines a Channel of the Youtube Channel Downloader.
 */
public class Channel {
    
    //Fields
    
    /**
     * The key of the Channel; required.
     */
    public String key;
    
    /**
     * A flag indicating whether a Channel is enabled or not; true by default.
     */
    public boolean active;
    
    /**
     * The name of the Channel; required.
     */
    public String name;
    
    /**
     * The group of the Channel; empty by default.
     */
    public String group;
    
    /**
     * The url of the Channel; empty by default.
     */
    public String url;
    
    /**
     * The Playlist ID of the Channel; required.
     */
    public String playlistId;
    
    /**
     * The output folder to store the videos that are downloaded from the Channel; required.
     */
    public File outputFolder;
    
    /**
     * A flag indicating whether to save the videos from the Channel as a mp3 file or not; mp4 otherwise; false by default.
     */
    public boolean saveAsMp3;
    
    /**
     * The playlist file to add mp3 files downloaded from the Channel to if saving as mp3s; null by default.
     */
    public File playlistFile;
    
    /**
     * A flag indicating whether to delete files from the output directory that are not in the playlist anymore; false by default.
     */
    public boolean keepClean;
    
    /**
     * The SponsorBlock configuration for the Channel; null by default.
     */
    public SponsorBlocker.SponsorBlockConfig sponsorBlockConfig;
    
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
