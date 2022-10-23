/*
 * File:    Channel.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a Channel of the Youtube Channel Downloader.
 */
public class Channel extends ChannelEntry {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Channel.class);
    
    
    //Constants
    
    /**
     * A list of required fields in a Channel configuration.
     */
    public static final List<String> REQUIRED_FIELDS = List.of("key", "playlistId", "outputFolder");
    
    /**
     * A list of base fields in a Channel configuration.
     */
    public static final List<String> BASE_FIELDS = List.of("key", "active", "name", "url", "playlistId", "outputFolder");
    
    /**
     * A list of all fields in a Channel configuration.
     */
    public static final List<String> ALL_FIELDS = List.of("key", "active", "name", "group", "url", "playlistId", "outputFolder", "playlistFile", "saveAsMp3", "reversePlaylist", "ignoreGlobalLocations", "keepClean");
    
    
    //Enums
    
    /**
     * An enumeration of Channel Types.
     */
    public enum ChannelType {
        
        //Values
        
        CHANNEL,
        PLAYLIST;
        
        
        //Static Methods
        
        /**
         * Determines the Channel Type based on a corresponding playlist id.
         *
         * @param playlistId The playlist id.
         * @return The Channel Type.
         */
        public static ChannelType determineType(String playlistId) {
            return StringUtility.isNullOrBlank(playlistId) ? null :
                   playlistId.startsWith("U") ? CHANNEL :
                   playlistId.startsWith("PL") ? PLAYLIST : null;
        }
        
    }
    
    
    //Fields
    
    /**
     * The name of the Channel.
     */
    public String name;
    
    /**
     * The playlist file to add files downloaded by the Channel to.
     */
    public File playlistFile;
    
    /**
     * The path representing the playlist file.
     */
    public String playlistFilePath;
    
    /**
     * The type of the Channel.
     */
    public ChannelType type;
    
    /**
     * The state of the Channel.
     */
    public ChannelState state;
    
    /**
     * A flag indicating whether there was an error processing the Channel this run or not.
     */
    public AtomicBoolean error;
    
    
    //Constructors
    
    /**
     * Creates a Channel.
     *
     * @param fields The fields from the Channel configuration.
     * @param parent The parent of the Channel configuration.
     * @throws Exception When the Channel configuration does not contain all of the required fields.
     */
    public Channel(Map<String, Object> fields, ChannelGroup parent) throws Exception {
        super(fields, parent);
        
        this.name = stringFieldGetter.apply("name").map(e -> e.replaceAll("[.|]", "")).orElseGet(() -> StringUtility.toPascalCase(key));
        
        this.playlistFilePath = stringFieldGetter.apply("playlistFile").map(Channel::cleanFilePath).orElseGet(() -> stringFieldGetter.apply("playlistFilePath").orElse(null));
        this.playlistFile = Optional.ofNullable(playlistFilePath).map(e -> parseFilePath(locationPrefix, playlistFilePath)).orElse(null);
        
        this.type = ChannelType.determineType(playlistId);
        
        this.state = new ChannelState(this);
        this.error = new AtomicBoolean(false);
    }
    
    /**
     * Creates a Channel.
     *
     * @param fields The fields from the Channel configuration.
     * @throws Exception When the Channel configuration does not contain all of the required fields.
     */
    public Channel(Map<String, Object> fields) throws Exception {
        this(fields, null);
    }
    
    /**
     * The default no-argument constructor for a Channel.
     */
    public Channel() {
        super();
    }
    
    
    //Methods
    
    /**
     * Returns whether the Channel references a Youtube channel or not.
     *
     * @return Whether the Channel references a Youtube channel or not.
     */
    public boolean isYoutubeChannel() {
        return (type == ChannelType.CHANNEL);
    }
    
    /**
     * Returns whether the Channel references a Youtube playlist or not.
     *
     * @return Whether the Channel references a Youtube playlist or not.
     */
    public boolean isYoutubePlaylist() {
        return (type == ChannelType.PLAYLIST);
    }
    
    /**
     * Returns the map of the field values of the Channel.
     *
     * @return The map of the field values of the Channel.
     */
    @Override
    public Map<String, Object> getFields() {
        final Map<String, Object> fields = super.getFields();
        fields.put("name", Optional.ofNullable(name).map(String::strip).orElse(null));
        fields.put("playlistFile", Optional.ofNullable(playlistFilePath).orElse(Optional.ofNullable(playlistFile).map(File::getAbsolutePath).orElse(null)));
        
        if (!ALL_FIELDS.stream().allMatch(fields::containsKey)) {
            throw new UnsupportedOperationException();
        }
        return fields;
    }
    
    /**
     * Returns the map of the effective field values of the Channel.
     *
     * @return The map of the effective field values of the Channel.
     */
    @Override
    public Map<String, Object> getEffectiveFields() {
        final Map<String, Object> fields = super.getEffectiveFields();
        fields.put("name", getName());
        fields.put("playlistFile", getPlaylistFile());
        
        if (!ALL_FIELDS.stream().allMatch(fields::containsKey)) {
            throw new UnsupportedOperationException();
        }
        return fields;
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
    
    
    //Getters
    
    /**
     * Returns the name of the Channel.
     *
     * @return The name of the Channel.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the playlist file to add files downloaded by the Channel to.
     *
     * @return The playlist file to add files downloaded by the Channel to.
     */
    public File getPlaylistFile() {
        return Optional.ofNullable(playlistFile).orElseGet(() ->
                (isSavePlaylist() ? new File(getOutputFolder().getAbsolutePath() + ".m3u") : null));
    }
    
    /**
     * Returns whether to save the content that is downloaded by the Chanel to a playlist or not.
     *
     * @return Whether to save the content that is downloaded by the Chanel to a playlist or not.
     */
    @Override
    public boolean isSavePlaylist() {
        return Optional.ofNullable(savePlaylist).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::isSavePlaylist).orElseGet(() -> (playlistFile != null)));
    }
    
}
