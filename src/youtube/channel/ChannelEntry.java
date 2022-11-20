/*
 * File:    ChannelEntry.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.console.Console;
import commons.lambda.stream.mapper.Mappers;
import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.conf.Color;
import youtube.conf.SponsorBlocker;
import youtube.util.PathUtils;
import youtube.util.Utils;

/**
 * Defines a Channel Entry configuration of the Youtube Channel Downloader.
 */
public class ChannelEntry {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelEntry.class);
    
    
    //Constants
    
    /**
     * The default value of the flag indicating whether a Channel Entry is enabled or not.
     */
    public static final boolean DEFAULT_ACTIVE = true;
    
    /**
     * The default value of the flag indicating whether to save the content that is downloaded by the Channel Entry as mp3 files or not; mp4 otherwise.
     */
    public static final boolean DEFAULT_SAVE_AS_MP3 = false;
    
    /**
     * The default value of the flag indicating whether to save the content that is downloaded by the Chanel Entry to a playlist or not.
     */
    public static final boolean DEFAULT_SAVE_PLAYLIST = false;
    
    /**
     * The default value of the flag indicating whether to reverse the order of the playlist or not, putting newer videos first.
     */
    public static final boolean DEFAULT_REVERSE_PLAYLIST = false;
    
    /**
     * The default value of the flag indicating whether to disregard the globally configured storage drive and video and music directories when determining the paths for the Channel Entry.
     */
    public static final boolean DEFAULT_IGNORE_GLOBAL_LOCATIONS = false;
    
    /**
     * The default value of the flag indicating whether to delete files from the output directory that were removed from the Youtube playlist.
     */
    public static final boolean DEFAULT_KEEP_CLEAN = false;
    
    
    //Fields
    
    /**
     * The key of the Channel Entry.
     */
    public String key;
    
    /**
     * A flag indicating whether the Channel Entry is enabled or not.
     */
    public Boolean active;
    
    /**
     * The group of the Channel Entry.
     */
    public String group;
    
    /**
     * The url of the Channel Entry.
     */
    public String url;
    
    /**
     * The playlist id referenced by the Channel Entry.
     */
    public String playlistId;
    
    /**
     * The channel id of the Channel Entry.
     */
    public String channelId;
    
    /**
     * The output folder to store the content that is downloaded by the Channel Entry.
     */
    public File outputFolder;
    
    /**
     * The path representing the output folder.
     */
    public String outputFolderPath;
    
    /**
     * A flag indicating whether to save the content that is downloaded by the Channel Entry as mp3 files or not; mp4 otherwise.
     */
    public Boolean saveAsMp3;
    
    /**
     * A flag indicating whether to save the content that is downloaded by the Chanel Entry to a playlist or not.
     */
    public Boolean savePlaylist;
    
    /**
     * A flag indicating whether to reverse the order of the playlist or not, putting newer videos first.
     */
    public Boolean reversePlaylist;
    
    /**
     * A flag indicating whether to disregard the globally configured storage drive and video and music directories when determining the paths for the Channel Entry.
     */
    public Boolean ignoreGlobalLocations;
    
    /**
     * The location prefix of the Channel Entry.
     */
    public String locationPrefix;
    
    /**
     * A flag indicating whether to delete files from the output directory that were removed from the referenced Youtube source.
     */
    public Boolean keepClean;
    
    /**
     * The SponsorBlock configuration for the Channel Entry.
     */
    public SponsorBlocker.SponsorBlockConfig sponsorBlockConfig;
    
    /**
     * The json data of the Channel Entry configuration.
     */
    protected JSONObject channelEntryJson;
    
    /**
     * The parent of the Channel Entry.
     */
    protected ChannelEntry parent = null;
    
    
    //Functions
    
    /**
     * Reads a field from the Channel Entry json configuration.
     */
    protected final Function<String, Optional<Object>> fieldGetter = (String name) ->
            Optional.ofNullable(channelEntryJson.get(name));
    
    /**
     * Reads a string field from the Channel Entry json configuration.
     */
    protected final Function<String, Optional<String>> stringFieldGetter = (String name) ->
            Optional.ofNullable((String) fieldGetter.apply(name).orElse(null));
    
    /**
     * Reads a boolean field from the Channel Entry json configuration.
     */
    protected final Function<String, Optional<Boolean>> booleanFieldGetter = (String name) ->
            Optional.ofNullable((Boolean) fieldGetter.apply(name).orElse(null));
    
    
    //Constructors
    
    /**
     * Creates a Channel Entry.
     *
     * @param fields The fields from the Channel Entry configuration.
     * @param parent The parent of the Channel Entry configuration.
     * @throws RuntimeException When the Channel Entry configuration does not contain all of the required fields.
     */
    public ChannelEntry(Map<String, Object> fields, ChannelGroup parent) {
        Optional.ofNullable(parent).filter(e -> (e.getKey() != null)).ifPresent(e -> {
            this.parent = e;
            e.children.add(this);
        });
        
        this.channelEntryJson = new JSONObject(fields);
        
        this.key = stringFieldGetter.apply("key").map(e -> e.replaceAll("[.|]", ""))
                .orElseThrow(() -> {
                    System.out.println(Color.bad("Configuration missing required field: ") + Color.link("key"));
                    return new RuntimeException();
                });
        
        this.playlistId = stringFieldGetter.apply("playlistId").map(e -> e.replaceAll("^UC", "UU")).orElse(null);
        this.channelId = Optional.ofNullable(playlistId).filter(e -> e.startsWith("UU")).map(e -> e.replaceAll("^UU", "UC")).orElse(null);
        this.url = stringFieldGetter.apply("url").orElseGet(() -> determineUrl(playlistId));
        
        this.group = stringFieldGetter.apply("group").map(e -> e.replaceAll(".+\\.", "")).orElse(null);
        
        this.active = booleanFieldGetter.apply("active").orElse(null);
        this.saveAsMp3 = booleanFieldGetter.apply("saveAsMp3").orElse(null);
        this.savePlaylist = booleanFieldGetter.apply("savePlaylist").orElse(null);
        this.reversePlaylist = booleanFieldGetter.apply("reversePlaylist").orElse(null);
        this.keepClean = booleanFieldGetter.apply("keepClean").orElse(null);
        
        this.ignoreGlobalLocations = booleanFieldGetter.apply("ignoreGlobalLocations").orElse(null);
        this.locationPrefix = !isIgnoreGlobalLocations() ? PathUtils.path(true, (isSaveAsMp3() ? Channels.musicDir : Channels.videoDir)) : null;
        
        this.outputFolderPath = stringFieldGetter.apply("outputFolder").map(ChannelEntry::cleanFilePath).orElseGet(() -> stringFieldGetter.apply("outputFolderPath").orElse(null));
        this.outputFolder = Optional.ofNullable(outputFolderPath).map(e -> parseFilePath(locationPrefix, getOutputFolderPath())).orElse(null);
        
        this.sponsorBlockConfig = Optional.ofNullable((JSONObject) fields.get("sponsorBlock"))
                .map(SponsorBlocker::loadConfig)
                .map(Mappers.forEach(e -> e.type = SponsorBlocker.SponsorBlockConfig.Type.CHANNEL))
                .orElse(null);
    }
    
    /**
     * Creates a Channel Entry.
     *
     * @param fields The fields from the Channel Entry configuration.
     * @throws RuntimeException When the Channel Entry configuration does not contain all of the required fields.
     */
    public ChannelEntry(Map<String, Object> fields) {
        this(fields, null);
    }
    
    /**
     * The default no-argument constructor for a Channel Entry.
     */
    public ChannelEntry() {
    }
    
    
    //Methods
    
    /**
     * Returns whether the Channel Entry is a Channel or not.
     *
     * @return Whether the Channel Entry is a Channel or not.
     */
    public boolean isChannel() {
        return !isGroup();
    }
    
    /**
     * Returns whether the Channel Entry is a Channel Group or not.
     *
     * @return Whether the Channel Entry is a Channel Group or not.
     */
    @SuppressWarnings("unchecked")
    public boolean isGroup() {
        return isGroupConfiguration(channelEntryJson);
    }
    
    /**
     * Returns the configuration depth of the Channel Entry.
     *
     * @return The configuration depth of the Channel Entry.
     */
    public int getDepth() {
        return 1 + Optional.ofNullable(parent).map(ChannelEntry::getDepth).orElse(0);
    }
    
    /**
     * Returns the canonical key of the Channel Entry.
     *
     * @return The canonical key of the Channel Entry.
     */
    public String getCanonicalKey() {
        return String.join(".", getCanonicalGroupKey(), key)
                .replaceAll("^\\.|\\.$", "");
    }
    
    /**
     * Returns the canonical group key of the Channel Entry.
     *
     * @return The canonical group key of the Channel Entry.
     */
    public String getCanonicalGroupKey() {
        return Optional.ofNullable(parent).map(ChannelEntry::getCanonicalKey).orElse("");
    }
    
    /**
     * Returns a list of all ancestors of the Channel Entry.
     *
     * @return A list of all ancestors of the Channel Entry.
     */
    public List<ChannelEntry> getAllAncestors() {
        return Optional.ofNullable(parent)
                .map(parent -> Stream.concat(
                                Stream.of(parent),
                                parent.getAllAncestors().stream())
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
    
    /**
     * Returns a list of all group keys of the Channel Entry.
     *
     * @return A list of all group keys of the Channel Entry.
     */
    public List<String> getAllGroups() {
        return Stream.of(
                        Stream.of(isGroup() ? key : null),
                        Optional.ofNullable(group).stream().flatMap(e -> Arrays.stream(e.split("\\s*[,|]\\s*"))),
                        Optional.ofNullable(parent).map(ChannelEntry::getAllGroups).stream().flatMap(Collection::stream))
                .flatMap(e -> e)
                .filter(e -> !StringUtility.isNullOrBlank(e))
                .collect(Collectors.toList());
    }
    
    /**
     * Returns whether the Channel Entry is a member of a specific group or not.
     *
     * @param groupKeyOrName The key or name of the group.
     * @return Whether the Channel Entry is a member of the specified group or not.
     */
    public boolean isMemberOfGroup(String groupKeyOrName) {
        final BiPredicate<String, String> groupEquals = (String test, String target) ->
                (Stream.of(test, target)
                        .map(String::toLowerCase)
                        .map(e -> e.replaceAll("[^a-z\\d]", ""))
                        .map(e -> e.replaceAll("s$", ""))
                        .distinct().count() == 1);
        
        return StringUtility.isNullOrBlank(groupKeyOrName) ||
                Optional.ofNullable(key).map(key -> groupEquals.test(key, groupKeyOrName)).orElse(false) ||
                Optional.ofNullable(group).map(group -> groupEquals.test(group, groupKeyOrName)).orElse(false) ||
                Optional.ofNullable(parent).map(parent -> parent.isMemberOfGroup(groupKeyOrName)).orElse(false);
    }
    
    /**
     * Returns the map of the field values of the Channel Entry.
     *
     * @return The map of the field values of the Channel Entry.
     */
    public Map<String, Object> getFields() {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("key", Optional.ofNullable(key).map(String::strip).orElse(null));
        fields.put("active", active);
        fields.put("group", Optional.ofNullable(group).map(String::strip).orElse(null));
        fields.put("url", Optional.ofNullable(url).map(String::strip).orElse(null));
        fields.put("playlistId", Optional.ofNullable(playlistId).map(String::strip).orElse(null));
        fields.put("outputFolder", Optional.ofNullable(outputFolderPath).orElse(Optional.ofNullable(outputFolder).map(File::getAbsolutePath).orElse(null)));
        fields.put("saveAsMp3", saveAsMp3);
        fields.put("savePlaylist", savePlaylist);
        fields.put("reversePlaylist", reversePlaylist);
        fields.put("ignoreGlobalLocations", ignoreGlobalLocations);
        fields.put("keepClean", keepClean);
        return fields;
    }
    
    /**
     * Returns the map of the effective field values of the Channel Entry.
     *
     * @return The map of the effective field values of the Channel Entry.
     */
    public Map<String, Object> getEffectiveFields() {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("key", getKey());
        fields.put("active", isActive());
        fields.put("group", getGroup());
        fields.put("url", getUrl());
        fields.put("playlistId", getPlaylistId());
        fields.put("outputFolder", getOutputFolder());
        fields.put("saveAsMp3", isSaveAsMp3());
        fields.put("savePlaylist", isSavePlaylist());
        fields.put("reversePlaylist", isReversePlaylist());
        fields.put("ignoreGlobalLocations", isIgnoreGlobalLocations());
        fields.put("keepClean", isKeepClean());
        return fields;
    }
    
    /**
     * Prints the Channel Entry to the screen.
     *
     * @param indent The initial indent.
     */
    protected void print(int indent) {
        Optional.ofNullable(key)
                .map(key -> (key + (isGroup() ? ":" : ""))).map(Utils::formatUnderscoredString)
                .ifPresent(key -> {
                    final Console.ConsoleEffect color = isActive() ? (isGroup() ? Color.LINK : Color.CHANNEL) : (active ? Color.LOG : Color.BAD);
                    System.out.println(StringUtility.repeatString(Utils.INDENT, indent) +
                            Color.apply(color, key));
                });
    }
    
    /**
     * Prints the Channel Entry to the screen.
     */
    public void print() {
        print((parent == null) ? -1 : 0);
    }
    
    /**
     * Returns the string representation of the Channel Entry.
     *
     * @return the string representation of the Channel Entry.
     */
    @Override
    public String toString() {
        return key;
    }
    
    
    //Getters
    
    /**
     * Returns the key of the Channel Entry.
     *
     * @return The key of the Channel Entry.
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Returns whether the Channel Entry is active or not.
     *
     * @return Whether the Channel Entry is active or not.
     */
    public boolean isActive() {
        return Optional.ofNullable(active).orElse(DEFAULT_ACTIVE) &&
                Optional.ofNullable(parent).map(ChannelEntry::isActive).orElse(true);
    }
    
    /**
     * Returns the group of the Channel Entry.
     *
     * @return The group of the Channel Entry.
     */
    public String getGroup() {
        return Optional.ofNullable(group).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::getGroup).orElse(null));
    }
    
    /**
     * Returns the url of the Channel Entry.
     *
     * @return The url of the Channel Entry.
     */
    public String getUrl() {
        return Optional.ofNullable(url).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::getUrl).orElse(null));
    }
    
    /**
     * Returns the playlist id referenced by the Channel Entry.
     *
     * @return The playlist id referenced by the Channel Entry.
     */
    public String getPlaylistId() {
        return Optional.ofNullable(playlistId).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::getPlaylistId).orElse(null));
    }
    
    /**
     * Returns the channel id of the Channel Entry.
     *
     * @return The channel id of the Channel Entry.
     */
    public String getChannelId() {
        return Optional.ofNullable(channelId).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::getChannelId).orElse(null));
    }
    
    /**
     * Returns the output folder to store the content that is downloaded by the Channel Entry.
     *
     * @return The output folder to store the content that is downloaded by the Channel Entry.
     */
    public File getOutputFolder() {
        return Optional.ofNullable(outputFolder).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::getOutputFolder).orElse(null));
    }
    
    /**
     * Returns the path representing the output folder.
     *
     * @return The path representing the output folder.
     */
    public String getOutputFolderPath() {
        return Optional.ofNullable(outputFolderPath).orElse("~")
                .replaceAll("^~", Optional.ofNullable(parent).map(ChannelEntry::getOutputFolderPath).orElse(""));
    }
    
    /**
     * Returns whether to save the content that is downloaded by the Channel Entry as mp3 files or not; mp4 otherwise.
     *
     * @return Whether to save the content that is downloaded by the Channel Entry as mp3 files or not; mp4 otherwise.
     */
    public boolean isSaveAsMp3() {
        return Optional.ofNullable(saveAsMp3).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::isSaveAsMp3).orElse(DEFAULT_SAVE_AS_MP3));
    }
    
    /**
     * Returns whether to save the content that is downloaded by the Chanel Entry to a playlist or not.
     *
     * @return Whether to save the content that is downloaded by the Chanel Entry to a playlist or not.
     */
    public boolean isSavePlaylist() {
        return Optional.ofNullable(savePlaylist).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::isSavePlaylist).orElse(DEFAULT_SAVE_PLAYLIST));
    }
    
    /**
     * Returns whether to reverse the order of the playlist or not, putting newer videos first.
     *
     * @return Whether to reverse the order of the playlist or not, putting newer videos first.
     */
    public boolean isReversePlaylist() {
        return Optional.ofNullable(reversePlaylist).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::isReversePlaylist).orElse(DEFAULT_REVERSE_PLAYLIST));
    }
    
    /**
     * Returns whether to disregard the globally configured storage drive and video and music directories when determining the paths for the Channel Entry.
     *
     * @return Whether to disregard the globally configured storage drive and video and music directories when determining the paths for the Channel Entry.
     */
    public boolean isIgnoreGlobalLocations() {
        return Optional.ofNullable(ignoreGlobalLocations).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::isIgnoreGlobalLocations).orElse(DEFAULT_IGNORE_GLOBAL_LOCATIONS));
    }
    
    /**
     * Returns whether to delete files from the output directory that were removed from the referenced Youtube source.
     *
     * @return Whether to delete files from the output directory that were removed from the referenced Youtube source.
     */
    public boolean isKeepClean() {
        return Optional.ofNullable(keepClean).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::isKeepClean).orElse(DEFAULT_KEEP_CLEAN));
    }
    
    /**
     * Returns the SponsorBlock configuration for the Channel Entry.
     *
     * @return The SponsorBlock configuration for the Channel Entry.
     */
    public SponsorBlocker.SponsorBlockConfig getSponsorBlockConfig() {
        return Optional.ofNullable(sponsorBlockConfig).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::getSponsorBlockConfig).orElse(null));
    }
    
    /**
     * Returns the parent of the Channel Entry.
     *
     * @return The parent of the Channel Entry.
     */
    public ChannelEntry getParent() {
        return parent;
    }
    
    
    //Static Methods
    
    /**
     * Loads and validates a Channel Entry configuration.
     *
     * @param fields The map of fields of the Channel Entry.
     * @param parent The parent of the Channel configuration.
     * @return The Channel Entry.
     */
    @SuppressWarnings("unchecked")
    protected static <T extends ChannelEntry> T load(Map<String, Object> fields, ChannelGroup parent) throws Exception {
        T channelEntry = isGroupConfiguration(fields) ? (T) new ChannelGroup(fields, parent) : (T) new Channel(fields, parent);
        validateRequiredFields(channelEntry.getEffectiveFields());
        return channelEntry;
    }
    
    /**
     * Loads and validates a Channel Entry configuration.
     *
     * @param fields The map of fields of the Channel Entry.
     * @return The Channel Entry.
     */
    protected static <T extends ChannelEntry> T load(Map<String, Object> fields) throws Exception {
        return load(fields, null);
    }
    
    /**
     * Returns whether a Channel Entry configuration represents a Channel Group.
     *
     * @param fields The map of fields of the Channel Entry.
     * @return Whether a Channel Entry configuration represents a Channel Group.
     */
    protected static boolean isGroupConfiguration(Map<String, Object> fields) {
        return fields.containsKey(ChannelGroup.CHILD_CONFIGURATION_KEY);
    }
    
    /**
     * Parses a Channel Entry file path.
     *
     * @param directoryPrefix The directory prefix.
     * @param filePath        The file path.
     * @return A file representing the parsed file path.
     */
    protected static File parseFilePath(String directoryPrefix, String filePath) {
        return new File(Optional.ofNullable(directoryPrefix).orElse("") +
                cleanFilePath(filePath)
                        .replace("${D}", Channels.storageDrive.getAbsolutePath())
                        .replace("${V}", Channels.videoDir.getAbsolutePath())
                        .replace("${M}", Channels.musicDir.getAbsolutePath()));
    }
    
    /**
     * Clean a Channel Entry file path.
     *
     * @param filePath The file path.
     * @return The cleaned Channel file path.
     */
    protected static String cleanFilePath(String filePath) {
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
    protected static String determineUrl(String playlistId) {
        return Optional.ofNullable(Channel.ChannelType.determineType(playlistId))
                .map(type -> {
                    switch (type) {
                        case CHANNEL:
                            return "https://www.youtube.com/channel/" + playlistId.replaceAll("^UU", "UC");
                        case PLAYLIST:
                            return "https://www.youtube.com/playlist?list=" + playlistId;
                        default:
                            return null;
                    }
                }).orElse(null);
    }
    
    /**
     * Validates that a map of fields being used to construct a new Channel Entry contains all the required fields.
     *
     * @param fields The map of fields being used to construct a new Channel Entry.
     * @throws RuntimeException When the map of fields does not contain all the required fields.
     */
    protected static void validateRequiredFields(Map<String, Object> fields) {
        Optional.of((isGroupConfiguration(fields) ? ChannelGroup.REQUIRED_FIELDS : Channel.REQUIRED_FIELDS).stream()
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
    
}
