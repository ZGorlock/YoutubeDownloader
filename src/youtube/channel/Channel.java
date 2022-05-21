/*
 * File:    Channel.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.channel;

import java.io.File;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

import org.json.simple.JSONObject;
import youtube.util.SponsorBlocker;

/**
 * Defines a Channel of the Youtube Channel Downloader.
 */
public class Channel {
    
    //Constants
    
    /**
     * A list of required fields in a Channel configuration.
     */
    public static final List<String> requiredFields = List.of("key", "name", "playlistId", "outputFolder");
    
    
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
                throw new InvalidPropertiesFormatException("Channel configuration missing required field: " + requiredField +
                        (((channelJson != null) && channelJson.containsKey("key")) ? (" (" + channelJson.get("key") + ')') : ""));
            }
        }
        
        this.key = (String) channelJson.get("key");
        this.active = (boolean) channelJson.getOrDefault("active", true);
        this.name = ((String) channelJson.get("name")).replace("|", "");
        this.group = (String) channelJson.getOrDefault("group", "");
        this.url = (String) channelJson.getOrDefault("url", "");
        this.playlistId = (String) channelJson.get("playlistId");
        this.saveAsMp3 = (boolean) channelJson.getOrDefault("saveAsMp3", false);
        this.ignoreGlobalLocations = (boolean) channelJson.getOrDefault("ignoreGlobalLocations", false);
        this.reversePlaylist = (boolean) channelJson.getOrDefault("reversePlaylist", false);
        this.keepClean = (boolean) channelJson.getOrDefault("keepClean", false);
        
        final String directoryPrefix = (this.ignoreGlobalLocations ? "" : (this.saveAsMp3 ? Channels.musicDir : Channels.videoDir));
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
                filePath.replace("${D}", Channels.storageDrive)
                        .replace("${V}", Channels.videoDir)
                        .replace("${M}", Channels.musicDir));
    }
    
}
