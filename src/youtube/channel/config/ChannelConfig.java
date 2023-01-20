/*
 * File:    ChannelConfig.java
 * Package: youtube.channel.config
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.config;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.util.FileUtils;
import youtube.util.Utils;

/**
 * Defines a Channel Config of the Youtube Channel Downloader.
 */
public class ChannelConfig extends ChannelEntry {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelConfig.class);
    
    
    //Constants
    
    /**
     * A list of required fields in the configuration of a Channel Config.
     */
    public static final List<String> REQUIRED_FIELDS = List.of("key", "playlistId", "outputFolder");
    
    /**
     * A list of base fields in the configuration of a Channel Config.
     */
    public static final List<String> BASE_FIELDS = List.of("key", "active", "name", "url", "playlistId", "outputFolder");
    
    /**
     * A list of all fields in the configuration of a Channel Config.
     */
    public static final List<String> ALL_FIELDS = List.of("key", "active", "name", "group", "url", "playlistId", "outputFolder", "playlistFile", "saveAsMp3", "savePlaylist", "reversePlaylist", "ignoreGlobalLocations", "keepClean");
    
    
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
    
    
    //Constructors
    
    /**
     * Creates a Channel Config.
     *
     * @param config The configuration data.
     * @param parent The parent of the Channel Config.
     * @throws Exception When the configuration data does not contain all of the required fields.
     */
    public ChannelConfig(Map<String, Object> config, ChannelGroup parent) throws Exception {
        super(config, parent);
        
        this.name = stringFieldGetter.apply("name").map(identifierFormatter).orElseGet(() -> StringUtility.toPascalCase(key));
        
        this.playlistFilePath = stringFieldGetter.apply("playlistFile").map(ChannelConfig::cleanFilePath).orElseGet(() -> stringFieldGetter.apply("playlistFilePath").orElse(null));
        this.playlistFile = Optional.ofNullable(playlistFilePath).map(e -> parseFilePath(locationPrefix, getPlaylistFilePath())).orElse(null);
        
        this.type = ChannelType.determineType(playlistId);
    }
    
    /**
     * Creates a Channel Config.
     *
     * @param config The configuration data.
     * @throws Exception When the configuration data does not contain all of the required fields.
     */
    public ChannelConfig(Map<String, Object> config) throws Exception {
        this(config, null);
    }
    
    /**
     * Creates an empty Channel Config.
     */
    public ChannelConfig() {
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
     * Returns the configuration data of the Channel Config.
     *
     * @return The configuration data of the Channel Config.
     */
    @Override
    public Map<String, Object> getConfig() {
        final Map<String, Object> fields = super.getConfig();
        fields.put("name", Optional.ofNullable(name).map(String::strip).orElse(null));
        fields.put("playlistFile", Optional.ofNullable(playlistFilePath).orElse(Optional.ofNullable(playlistFile).map(File::getAbsolutePath).orElse(null)));
        
        if (!ALL_FIELDS.stream().allMatch(fields::containsKey)) {
            throw new UnsupportedOperationException();
        }
        return fields;
    }
    
    /**
     * Returns the effective configuration data of the Channel Config.
     *
     * @return The effective configuration data of the Channel Config.
     */
    @Override
    public Map<String, Object> getEffectiveConfig() {
        final Map<String, Object> fields = super.getEffectiveConfig();
        fields.put("name", getName());
        fields.put("playlistFile", getPlaylistFile());
        
        if (!ALL_FIELDS.stream().allMatch(fields::containsKey)) {
            throw new UnsupportedOperationException();
        }
        return fields;
    }
    
    /**
     * Returns a string representation of the Channel Config.
     *
     * @return A string representation of the Channel Config.
     */
    @Override
    public String toString() {
        return getName();
    }
    
    
    //Getters
    
    /**
     * Returns the name of the Channel Config.
     *
     * @return The name of the Channel Config.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the display name of the Channel Config.
     *
     * @return The display name of the Channel Config.
     */
    public String getDisplayName() {
        return getName() + Optional.ofNullable(name)
                .filter(e -> e.matches(".*P\\d+$"))
                .map(e -> Optional.ofNullable(outputFolderPath).orElse(playlistFilePath))
                .map(FileUtils::getFileTitle).map(e -> e.replaceAll("^~\\s*[-/]", ""))
                .map(String::strip).map(e -> (" - (" + e + ")"))
                .orElse("");
    }
    
    /**
     * Returns the playlist file to add files downloaded by the Channel to.
     *
     * @return The playlist file to add files downloaded by the Channel to.
     */
    public File getPlaylistFile() {
        return Optional.ofNullable(playlistFile).orElseGet(() ->
                (isSavePlaylist() ? new File(getOutputFolder().getAbsolutePath() + '.' + Utils.DEFAULT_PLAYLIST_FORMAT) : null));
    }
    
    /**
     * Returns the path representing the playlist file.
     *
     * @return The path representing the playlist file.
     */
    public String getPlaylistFilePath() {
        return Optional.ofNullable(playlistFilePath).orElse("~")
                .replaceAll("^~", getOutputFolderPath())
                .replaceAll("(?<!^)(?:" + Pattern.quote('.' + Utils.DEFAULT_PLAYLIST_FORMAT) + ")+?$", ('.' + Utils.DEFAULT_PLAYLIST_FORMAT));
    }
    
    /**
     * Returns the type of the Channel.
     *
     * @return The type of the Channel.
     */
    public ChannelType getType() {
        return type;
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
