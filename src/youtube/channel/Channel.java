/*
 * File:    Channel.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.channel;

import java.io.File;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

import commons.object.collection.MapUtility;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.conf.Color;
import youtube.util.PathUtils;
import youtube.util.SponsorBlocker;

/**
 * Defines a Channel of the Youtube Channel Downloader.
 */
public class Channel {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Channel.class);
    
    
    //Constants
    
    /**
     * A list of required fields in a Channel configuration.
     */
    public static final List<String> requiredFields = List.of("key", "name", "playlistId", "outputFolder");
    
    /**
     * The default value of the flag indicating whether a Channel is enabled or not.
     */
    public static final boolean DEFAULT_ACTIVE = true;
    
    /**
     * The default value of the flag indicating whether to save the videos from the Channel as a mp3 file or not; mp4 otherwise.
     */
    public static final boolean DEFAULT_SAVE_AS_MP3 = false;
    
    /**
     * The default value of the flag indicating whether to reverse the order of the playlist or not, putting newer videos first.
     */
    public static final boolean DEFAULT_REVERSE_PLAYLIST = false;
    
    /**
     * The default value of the flag indicating whether to disregard the globally configured storage drive and the video directory, if saveAsMp3 is false, or music directory, if saveAsMp3 is true.
     */
    public static final boolean DEFAULT_IGNORE_GLOBAL_LOCATIONS = false;
    
    /**
     * The default value of the flag indicating whether to delete files from the output directory that are not in the playlist anymore.
     */
    public static final boolean DEFAULT_KEEP_CLEAN = false;
    
    
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
     * A flag indicating whether to disregard the globally configured storage drive and the video directory, if saveAsMp3 is false, or music directory, if saveAsMp3 is true; false by default.
     */
    public boolean ignoreGlobalLocations;
    
    /**
     * A flag indicating whether to save the videos from the Channel as a mp3 file or not; mp4 otherwise; false by default.
     */
    public boolean saveAsMp3;
    
    /**
     * The playlist file to add files downloaded from the Channel to; null by default.
     */
    public File playlistFile;
    
    /**
     * A flag indicating whether to reverse the order of the playlist or not, putting newer videos first; false by default.
     */
    public boolean reversePlaylist;
    
    /**
     * A flag indicating whether to delete files from the output directory that are not in the playlist anymore; false by default.
     */
    public boolean keepClean;
    
    /**
     * The SponsorBlock configuration for the Channel; null by default.
     */
    public SponsorBlocker.SponsorBlockConfig sponsorBlockConfig;
    
    /**
     * The state of the Channel.
     */
    public ChannelState state;
    
    /**
     * The Channel Tree entry of the Channel.
     */
    public ChannelTree treeEntry;
    
    /**
     * A flag indicating whether there was an error retrieving the Channel this run or not.
     */
    public boolean error;
    
    
    //Constructors
    
    /**
     * Creates a Channel.
     *
     * @param channelJson The json data containing the Channel configuration.
     * @throws InvalidPropertiesFormatException When the Channel configuration does not contain a required field.
     */
    @SuppressWarnings("unchecked")
    public Channel(JSONObject channelJson) throws Exception {
        for (String requiredField : requiredFields) {
            if ((channelJson == null) || !channelJson.containsKey(requiredField)) {
                System.out.println(Color.bad("Channel: ") + MapUtility.getOrNull(channelJson, "key") +
                        Color.bad(" configuration missing required field: ") + requiredField);
                throw new RuntimeException();
            }
        }
        
        this.key = ((String) channelJson.get("key")).replace(".", "");
        this.active = (boolean) channelJson.getOrDefault("active", DEFAULT_ACTIVE);
        this.name = ((String) channelJson.get("name")).replaceAll("[.|]", "");
        this.group = ((String) channelJson.getOrDefault("group", "")).replaceAll(".+\\.", "");
        
        this.url = (String) channelJson.getOrDefault("url", "");
        this.playlistId = (String) channelJson.get("playlistId");
        
        this.saveAsMp3 = (boolean) channelJson.getOrDefault("saveAsMp3", DEFAULT_SAVE_AS_MP3);
        this.ignoreGlobalLocations = (boolean) channelJson.getOrDefault("ignoreGlobalLocations", DEFAULT_IGNORE_GLOBAL_LOCATIONS);
        this.reversePlaylist = (boolean) channelJson.getOrDefault("reversePlaylist", DEFAULT_REVERSE_PLAYLIST);
        this.keepClean = (boolean) channelJson.getOrDefault("keepClean", DEFAULT_KEEP_CLEAN);
        
        final String directoryPrefix = this.ignoreGlobalLocations ? "" :
                                       PathUtils.path(true, (this.saveAsMp3 ? Channels.musicDir : Channels.videoDir));
        this.outputFolder = parseFilePath(directoryPrefix, (String) channelJson.get("outputFolder"));
        this.playlistFile = (channelJson.get("playlistFile") == null) ? null :
                            parseFilePath(directoryPrefix, (String) channelJson.get("playlistFile"));
        
        this.state = new ChannelState(this);
        this.error = false;
        
        if (channelJson.containsKey("sponsorBlock")) {
            JSONObject sponsorBlockJson = (JSONObject) channelJson.get("sponsorBlock");
            this.sponsorBlockConfig = SponsorBlocker.loadConfig(sponsorBlockJson);
            this.sponsorBlockConfig.type = SponsorBlocker.SponsorBlockConfig.Type.CHANNEL;
        } else {
            this.sponsorBlockConfig = null;
        }
    }
    
    /**
     * The default no-argument constructor for a Channel.
     */
    public Channel() {
    }
    
    
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
        return playlistId.startsWith("U");
    }
    
    /**
     * Returns whether the Channel is active or not.
     *
     * @return Whether the Channel is active or not.
     */
    public boolean isActive() {
        return treeEntry.isActive();
    }
    
    /**
     * Returns whether the Channel is a member of a specific group or not.
     *
     * @param group The group.
     * @return Whether the Channel is a member of the specified group or not.
     */
    public boolean isMemberOfGroup(String group) {
        return treeEntry.isMemberOfGroup(group);
    }
    
    /**
     * Returns the string representation of the Channel.
     * 
     * @return the string representation of the Channel.
     */
    @Override
    public String toString() {
        return name;
    }
    
    
    //Functions
    
    /**
     * Parses a Channel file path.
     *
     * @param directoryPrefix The directory prefix.
     * @param filePath        The file path.
     * @return A file representing the parsed file path.
     */
    private static File parseFilePath(String directoryPrefix, String filePath) {
        return new File(directoryPrefix +
                filePath.replace("${D}", Channels.storageDrive.getAbsolutePath())
                        .replace("${V}", Channels.videoDir.getAbsolutePath())
                        .replace("${M}", Channels.musicDir.getAbsolutePath()));
    }
    
}
