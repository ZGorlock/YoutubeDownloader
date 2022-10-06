/*
 * File:    Channel.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import commons.lambda.stream.mapper.Mappers;
import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.conf.Color;
import youtube.conf.SponsorBlocker;
import youtube.util.PathUtils;

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
    public static final List<String> REQUIRED_FIELDS = List.of("key", "playlistId", "outputFolder");
    
    /**
     * A list of base fields in a Channel configuration.
     */
    public static final List<String> BASE_FIELDS = List.of("key", "active", "name", "url", "playlistId", "outputFolder", "saveAsMp3", "keepClean");
    
    /**
     * A list of all fields in a Channel configuration.
     */
    public static final List<String> ALL_FIELDS = List.of("key", "active", "name", "group", "url", "playlistId", "outputFolder", "saveAsMp3", "playlistFile", "reversePlaylist", "ignoreGlobalLocations", "keepClean");
    
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
    
    /**
     * The map of default field values in a Channel configuration; the default value of fields that are not included is null.
     */
    private static final Map<String, Object> DEFAULT_FIELD_VALUES = Map.of(
            "active", DEFAULT_ACTIVE,
            "saveAsMp3", DEFAULT_SAVE_AS_MP3,
            "reversePlaylist", DEFAULT_REVERSE_PLAYLIST,
            "ignoreGlobalLocations", DEFAULT_IGNORE_GLOBAL_LOCATIONS,
            "keepClean", DEFAULT_KEEP_CLEAN);
    
    
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
     * The key of the Channel; required.
     */
    public String key;
    
    /**
     * A flag indicating whether a Channel is enabled or not; true by default.
     */
    public boolean active;
    
    /**
     * The name of the Channel.
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
     * The type of the Channel.
     */
    public ChannelType type;
    
    /**
     * The output folder to store the videos that are downloaded from the Channel; required.
     */
    public File outputFolder;
    
    /**
     * The path representing the output folder.
     */
    public String outputFolderPath;
    
    /**
     * A flag indicating whether to save the videos from the Channel as a mp3 file or not; mp4 otherwise; false by default.
     */
    public boolean saveAsMp3;
    
    /**
     * The playlist file to add files downloaded from the Channel to; null by default.
     */
    public File playlistFile;
    
    /**
     * The path representing the playlist file.
     */
    public String playlistFilePath;
    
    /**
     * A flag indicating whether to reverse the order of the playlist or not, putting newer videos first; false by default.
     */
    public boolean reversePlaylist;
    
    /**
     * A flag indicating whether to disregard the globally configured storage drive and the video directory, if saveAsMp3 is false, or music directory, if saveAsMp3 is true; false by default.
     */
    public boolean ignoreGlobalLocations;
    
    /**
     * The location prefix of the Channel.
     */
    public String locationPrefix;
    
    /**
     * A flag indicating whether to delete files from the output directory that are not in the playlist anymore; false by default.
     */
    public boolean keepClean;
    
    /**
     * The json data containing the Channel configuration.
     */
    public JSONObject channelJson;
    
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
     * @param fields The fields from the Channel configuration.
     * @throws Exception When the Channel configuration does not contain all of the required fields.
     */
    public Channel(Map<String, Object> fields) throws Exception {
        validateRequiredFields(fields);
        
        final Function<String, Optional<String>> stringFieldGetter = (String name) ->
                Optional.ofNullable((String) fields.get(name));
        final Function<String, Optional<Boolean>> booleanFieldGetter = (String name) ->
                Optional.ofNullable((Boolean) fields.get(name));
        
        this.channelJson = new JSONObject(fields);
        
        this.key = stringFieldGetter.apply("key").map(e -> e.replaceAll("[.|]", "")).orElseThrow(RuntimeException::new);
        this.name = stringFieldGetter.apply("name").map(e -> e.replaceAll("[.|]", "")).orElseGet(() -> StringUtility.toPascalCase(key));
        this.group = stringFieldGetter.apply("group").map(e -> e.replaceAll(".+\\.", "")).orElse("");
        
        this.playlistId = stringFieldGetter.apply("playlistId").map(e -> e.replaceAll("^UC", "UU")).orElseThrow(RuntimeException::new);
        this.url = stringFieldGetter.apply("url").orElseGet(() -> determineUrl(playlistId));
        this.type = ChannelType.determineType(playlistId);
        
        this.active = booleanFieldGetter.apply("active").orElse(DEFAULT_ACTIVE);
        this.saveAsMp3 = booleanFieldGetter.apply("saveAsMp3").orElse(DEFAULT_SAVE_AS_MP3);
        this.keepClean = booleanFieldGetter.apply("keepClean").orElse(DEFAULT_KEEP_CLEAN);
        this.reversePlaylist = booleanFieldGetter.apply("reversePlaylist").orElse(DEFAULT_REVERSE_PLAYLIST);
        
        this.ignoreGlobalLocations = booleanFieldGetter.apply("ignoreGlobalLocations").orElse(DEFAULT_IGNORE_GLOBAL_LOCATIONS);
        this.locationPrefix = !ignoreGlobalLocations ? PathUtils.path(true, (saveAsMp3 ? Channels.musicDir : Channels.videoDir)) : "";
        
        this.outputFolderPath = stringFieldGetter.apply("outputFolder").map(Channel::cleanFilePath).orElseGet(() -> stringFieldGetter.apply("outputFolderPath").orElse(null));
        this.outputFolder = Optional.ofNullable(outputFolderPath).map(e -> parseFilePath(locationPrefix, outputFolderPath)).orElse(null);
        
        this.playlistFilePath = stringFieldGetter.apply("playlistFile").map(Channel::cleanFilePath).orElseGet(() -> stringFieldGetter.apply("playlistFilePath").orElse(null));
        this.playlistFile = Optional.ofNullable(playlistFilePath).map(e -> parseFilePath(locationPrefix, playlistFilePath)).orElse(null);
        
        this.state = new ChannelState(this);
        this.error = false;
        
        this.sponsorBlockConfig = Optional.ofNullable((JSONObject) fields.get("sponsorBlock"))
                .map(SponsorBlocker::loadConfig)
                .map(Mappers.forEach(e -> e.type = SponsorBlocker.SponsorBlockConfig.Type.CHANNEL))
                .orElse(null);
    }
    
    /**
     * The default no-argument constructor for a Channel.
     */
    public Channel() {
    }
    
    
    //Methods
    
    /**
     * Returns whether the Channel is a Channel or not.
     *
     * @return Whether the Channel is a Channel or not.
     */
    public boolean isChannel() {
        return (type == ChannelType.CHANNEL);
    }
    
    /**
     * Returns whether the Channel is a Playlist or not.
     *
     * @return Whether the Channel is a Playlist or not.
     */
    public boolean isPlaylist() {
        return (type == ChannelType.PLAYLIST);
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
     * Returns the map of the field values of the Channel.
     *
     * @return The map of field values of the Channel.
     */
    public Map<String, Object> getFields() {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("key", Optional.ofNullable(key).filter(e -> !StringUtility.isNullOrBlank(e)).orElse(null));
        fields.put("active", active);
        fields.put("name", Optional.ofNullable(name).filter(e -> !StringUtility.isNullOrBlank(e)).orElse(null));
        fields.put("group", Optional.ofNullable(group).filter(e -> !StringUtility.isNullOrBlank(e)).orElse(null));
        fields.put("url", Optional.ofNullable(url).filter(e -> !StringUtility.isNullOrBlank(e)).orElse(null));
        fields.put("playlistId", Optional.ofNullable(playlistId).filter(e -> !StringUtility.isNullOrBlank(e)).orElse(null));
        fields.put("outputFolder", Optional.ofNullable(outputFolderPath).orElse(Optional.ofNullable(outputFolder).map(File::getAbsolutePath).orElse(null)));
        fields.put("saveAsMp3", saveAsMp3);
        fields.put("playlistFile", Optional.ofNullable(playlistFilePath).orElse(Optional.ofNullable(playlistFile).map(File::getAbsolutePath).orElse(null)));
        fields.put("reversePlaylist", reversePlaylist);
        fields.put("ignoreGlobalLocations", ignoreGlobalLocations);
        fields.put("keepClean", keepClean);
        
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
    
    
    //Functions
    
    /**
     * Parses a Channel file path.
     *
     * @param directoryPrefix The directory prefix.
     * @param filePath        The file path.
     * @return A file representing the parsed file path.
     */
    public static File parseFilePath(String directoryPrefix, String filePath) {
        return new File((directoryPrefix + cleanFilePath(filePath))
                .replace("${D}", Channels.storageDrive.getAbsolutePath())
                .replace("${V}", Channels.videoDir.getAbsolutePath())
                .replace("${M}", Channels.musicDir.getAbsolutePath()));
    }
    
    /**
     * Clean a Channel file path.
     *
     * @param filePath The file path.
     * @return The cleaned Channel file path.
     */
    public static String cleanFilePath(String filePath) {
        return filePath
                .replaceAll("[:*?\"<>|]", " - ")
                .replaceAll("\\s+", " ").strip();
    }
    
    /**
     * Determines the url corresponding to a playlist id.
     *
     * @param playlistId The playlist id.
     * @return The url corresponding to the playlist id.
     */
    public static String determineUrl(String playlistId) {
        final ChannelType type = ChannelType.determineType(playlistId);
        if (type == null) {
            return null;
        }
        
        switch (type) {
            case CHANNEL:
                return "https://www.youtube.com/channel/" + playlistId.replaceAll("^UU", "UC");
            case PLAYLIST:
                return "https://www.youtube.com/playlist?list=" + playlistId;
            default:
                return null;
        }
    }
    
    /**
     * Validates that a map of fields being used to construct a new Channel contains all the required fields.
     *
     * @param fields The map of fields being used to construct a new Channel.
     * @throws Exception When the map of fields does not contain all the required fields.
     */
    private static void validateRequiredFields(Map<String, Object> fields) throws Exception {
        Optional.of(REQUIRED_FIELDS.stream()
                        .filter(e -> !MapUtility.contains(fields, e))
                        .collect(Collectors.toList()))
                .filter(e -> !e.isEmpty())
                .ifPresent(missingFields -> {
                    System.out.println(Color.bad("Channel: ") + Color.channel(MapUtility.getOrNull(fields, "key")) +
                            Color.bad(" configuration missing ") + Color.number(missingFields.size()) + Color.bad(" required field" + ((missingFields.size() != 1) ? "s" : "") + ": ") +
                            missingFields.stream().map(Color::link).collect(Collectors.joining(Color.bad(", "))));
                    throw new RuntimeException();
                });
    }
    
    
    //Inner Classes
    
    /**
     * Handles the formatting of Channel json strings.
     */
    public static class ChannelJsonFormatter {
        
        //Functions
        
        /**
         * A function that formats a Channel json string.
         */
        private static final BiFunction<Map<String, Object>, List<String>, String> formatter = (Map<String, Object> fields, List<String> toInclude) ->
                fields.entrySet().stream()
                        .filter(e -> toInclude.contains(e.getKey()))
                        .map(e -> StringUtility.spaces(2) +
                                StringUtility.quote(e.getKey()) + ": " + ((e.getValue() instanceof String) ? StringUtility.quote(String.valueOf(e.getValue())) : String.valueOf(e.getValue())))
                        .collect(Collectors.joining(("," + System.lineSeparator()), ("{" + System.lineSeparator()), (System.lineSeparator() + "}")));
        
        
        //Static Methods
        
        /**
         * Produces a full json string representing a Channel.
         *
         * @param channel The Channel.
         * @return The full json string representing the Channel.
         */
        public static String toFullJsonString(Channel channel) {
            return formatter.apply(channel.getFields(), ALL_FIELDS);
        }
        
        /**
         * Produces a json string representing a Channel.
         *
         * @param channel The Channel.
         * @return The json string representing the Channel.
         */
        public static String toJsonString(Channel channel) {
            final Map<String, Object> fields = channel.getFields();
            final List<String> toInclude = ALL_FIELDS.stream()
                    .filter(field -> BASE_FIELDS.contains(field) || !Objects.equals(fields.get(field), DEFAULT_FIELD_VALUES.get(field)))
                    .collect(Collectors.toList());
            
            return formatter.apply(fields, toInclude);
        }
        
        /**
         * Produces a minimal json string representing a Channel.
         *
         * @param channel The Channel.
         * @return The minimal json string representing the Channel.
         */
        public static String toMinJsonString(Channel channel) {
            final Map<String, Object> fields = channel.getFields();
            final List<String> toInclude = ALL_FIELDS.stream()
                    .filter(field -> REQUIRED_FIELDS.contains(field) || !Objects.equals(fields.get(field), DEFAULT_FIELD_VALUES.get(field)))
                    .collect(Collectors.toList());
            
            return formatter.apply(fields, toInclude);
        }
        
    }
    
}
