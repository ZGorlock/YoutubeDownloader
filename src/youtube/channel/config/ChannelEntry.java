/*
 * File:    ChannelEntry.java
 * Package: youtube.channel.config
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.config;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.io.console.Console;
import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.config.Color;
import youtube.config.SponsorBlocker;
import youtube.config.base.ConfigData;
import youtube.state.KeyStore;
import youtube.util.FileUtils;
import youtube.util.LogUtils;
import youtube.util.PathUtils;
import youtube.util.WebUtils;

/**
 * Defines a Channel Entry configuration of the Youtube Channel Downloader.
 */
public abstract class ChannelEntry extends ConfigData {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelEntry.class);
    
    
    //Constants
    
    /**
     * The list of valid delimiters in a Channel Entry group setting.
     */
    public static final String MULTI_GROUP_DELIMITERS = ".,;:+&";
    
    /**
     * The separator used in a canonical key.
     */
    public static final String CANONICAL_KEY_SEPARATOR = ".";
    
    /**
     * The default value of the flag indicating whether a Channel Entry is enabled or not.
     */
    public static final boolean DEFAULT_ACTIVE = true;
    
    /**
     * The default value of the flag indicating whether to save the content that is downloaded by the Channel Entry as audio files or not.
     */
    public static final boolean DEFAULT_SAVE_AS_AUDIO = false;
    
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
     * The name of the Channel Entry.
     */
    public String name;
    
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
     * A flag indicating whether to save the content that is downloaded by the Channel Entry as audio files or not.
     */
    public Boolean saveAsAudio;
    
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
     * The parent of the Channel Entry.
     */
    public ChannelGroup parent;
    
    
    //Constructors
    
    /**
     * Creates a Channel Entry.
     *
     * @param configData The json data of the Channel Entry.
     * @param parent     The parent of the Channel Entry.
     * @throws RuntimeException When the configuration data does not contain the required fields.
     */
    protected ChannelEntry(Map<String, Object> configData, ChannelGroup parent) {
        super(configData);
        
        if (parent != null) {
            this.parent = parent;
            this.parent.children.add(this);
        }
        
        this.key = parseString("key").map(this::formatIdentifier)
                .orElseThrow(() -> {
                    logger.warn(Color.bad("Configuration missing required field: ") + Color.link("key"));
                    return new RuntimeException();
                });
        
        this.playlistId = parseString("playlistId").map(e -> e.replaceAll("^UC", "UU")).orElse(null);
        this.channelId = Optional.ofNullable(playlistId).filter(e -> e.startsWith("UU")).map(e -> e.replaceAll("^UU", "UC")).orElse(null);
        this.url = parseString("url").orElseGet(() -> determineUrl(playlistId));
        
        this.name = parseString("name").map(this::formatIdentifier).orElseGet(() -> StringUtility.toPascalCase(key));
        this.group = parseData("group");
        
        this.active = parseData("active");
        this.saveAsAudio = parseBoolean("saveAsAudio").orElseGet(() -> parseData("saveAsMp3"));
        this.savePlaylist = parseData("savePlaylist");
        this.reversePlaylist = parseData("reversePlaylist");
        this.keepClean = parseData("keepClean");
        
        this.ignoreGlobalLocations = parseData("ignoreGlobalLocations");
        this.locationPrefix = !isIgnoreGlobalLocations() ? PathUtils.path(true, (isSaveAsAudio() ? FileUtils.Config.musicDir : FileUtils.Config.videoDir)) : null;
        
        this.outputFolderPath = parseString("outputFolder").map(ChannelEntry::cleanFilePath).orElseGet(() -> parseData("outputFolderPath"));
        this.outputFolder = Optional.ofNullable(outputFolderPath).map(e -> parseFilePath(locationPrefix, getOutputFolderPath())).orElse(null);
        
        this.sponsorBlockConfig = parseMap("sponsorBlock").map(SponsorBlocker::loadChannelConfig).orElse(null);
    }
    
    /**
     * Creates a Channel Entry.
     *
     * @param configData The json data of the Channel Entry.
     * @throws RuntimeException When the configuration data does not contain the required fields.
     */
    protected ChannelEntry(Map<String, Object> configData) {
        this(configData, null);
    }
    
    /**
     * Creates an empty Channel Entry.
     */
    protected ChannelEntry() {
        super();
    }
    
    
    //Methods
    
    /**
     * Returns whether the Channel Entry is a Channel Config.
     *
     * @return Whether the Channel Entry is a Channel Config.
     */
    public boolean isChannel() {
        return this instanceof ChannelConfig;
    }
    
    /**
     * Returns whether the Channel Entry is a Channel Group.
     *
     * @return Whether the Channel Entry is a Channel Group.
     */
    public boolean isGroup() {
        return this instanceof ChannelGroup;
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
                        Optional.of(this).filter(ChannelEntry::isGroup).stream().flatMap(e -> Stream.of(key, name)),
                        Optional.ofNullable(group).stream().flatMap(e -> Arrays.stream(e.split("\\s*[" + Pattern.quote(MULTI_GROUP_DELIMITERS) + "]+\\s*"))),
                        Optional.ofNullable(parent).map(ChannelEntry::getAllGroups).stream().flatMap(Collection::stream))
                .flatMap(e -> e)
                .filter(e -> !StringUtility.isNullOrBlank(e))
                .map(String::toUpperCase).distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Returns whether the Channel Entry is a member of a specific group.
     *
     * @param group The key or name of the group.
     * @return Whether the Channel Entry is a member of the specified group.
     */
    public boolean isMemberOfGroup(String group) {
        return Optional.ofNullable(group)
                .map(this::formatSearchIdentifier).filter(e -> !e.isEmpty())
                .map(search -> getAllGroups().stream()
                        .map(this::formatSearchIdentifier).filter(e -> !StringUtility.isNullOrEmpty(e))
                        .anyMatch(target -> StringUtility.equals(target, search)))
                .orElse(true);
    }
    
    /**
     * Returns whether the Channel Entry is filtered or not.
     *
     * @return Whether the Channel Entry is filtered or not.
     */
    public boolean isFiltered() {
        return false;
    }
    
    /**
     * Returns the configuration data of the Channel Entry.
     *
     * @return The configuration data of the Channel Entry.
     */
    public Map<String, Object> getConfig() {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("key", Optional.ofNullable(key).map(String::strip).orElse(null));
        fields.put("active", active);
        fields.put("name", Optional.ofNullable(name).map(String::strip).orElse(null));
        fields.put("group", Optional.ofNullable(group).map(String::strip).orElse(null));
        fields.put("url", Optional.ofNullable(url).map(String::strip).orElse(null));
        fields.put("playlistId", Optional.ofNullable(playlistId).map(String::strip).orElse(null));
        fields.put("outputFolder", Optional.ofNullable(outputFolderPath).orElse(Optional.ofNullable(outputFolder).map(File::getAbsolutePath).orElse(null)));
        fields.put("saveAsAudio", saveAsAudio);
        fields.put("savePlaylist", savePlaylist);
        fields.put("reversePlaylist", reversePlaylist);
        fields.put("ignoreGlobalLocations", ignoreGlobalLocations);
        fields.put("keepClean", keepClean);
        return fields;
    }
    
    /**
     * Returns the effective configuration data of the Channel Entry.
     *
     * @return The effective configuration data of the Channel Entry.
     */
    public Map<String, Object> getEffectiveConfig() {
        final Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("key", getKey());
        fields.put("active", isActive());
        fields.put("name", getName());
        fields.put("group", getGroup());
        fields.put("url", getUrl());
        fields.put("playlistId", getPlaylistId());
        fields.put("outputFolder", getOutputFolder());
        fields.put("saveAsAudio", isSaveAsAudio());
        fields.put("savePlaylist", isSavePlaylist());
        fields.put("reversePlaylist", isReversePlaylist());
        fields.put("ignoreGlobalLocations", isIgnoreGlobalLocations());
        fields.put("keepClean", isKeepClean());
        return fields;
    }
    
    /**
     * Formats an identifier of the Channel Entry.
     *
     * @param identifier The identifier.
     * @return The formatted identifier.
     */
    protected String formatIdentifier(String identifier) {
        return Optional.ofNullable(identifier)
                .map(String::strip)
                .map(e -> e.replaceAll("\\s+", "_"))
                .map(e -> e.replaceAll(Stream.of(
                                MULTI_GROUP_DELIMITERS,
                                CANONICAL_KEY_SEPARATOR,
                                KeyStore.SEPARATOR)
                        .map(Pattern::quote)
                        .collect(Collectors.joining("", "[", "]")), ""))
                .orElse(null);
    }
    
    /**
     * Formats a identifier of the Channel Entry to be used in a search.
     *
     * @param identifier The identifier.
     * @return The formatted group identifier.
     */
    protected String formatSearchIdentifier(String identifier) {
        return Optional.ofNullable(identifier)
                .map(String::strip).map(String::toUpperCase)
                .map(e -> e.replaceAll("(?i)[^A-Z\\d]", ""))
                .orElse(null);
    }
    
    /**
     * Prints the Channel Entry to the screen.
     *
     * @param indent The initial indent.
     */
    protected void print(int indent) {
        Optional.ofNullable(getKey())
                .map(key -> (key + (isGroup() ? ":" : "")))
                .map(e -> e.replace("_", " ")).map(String::toLowerCase).map(StringUtility::toTitleCase)
                .ifPresent(key -> {
                    final Console.ConsoleEffect color = isActive() ?
                                                        (isGroup() ? Color.Config.link : Color.Config.channel) :
                                                        (active ? Color.Config.log : Color.Config.bad);
                    logger.debug(String.join("",
                            (Channels.isFilterActive() && isFiltered()) ? Color.good("* ") : Color.log("  "),
                            StringUtility.repeatString(LogUtils.INDENT_HARD, indent),
                            Color.apply(color, key)));
                });
    }
    
    /**
     * Prints the Channel Entry to the screen.
     */
    public void print() {
        print((getParent() == null) ? -1 : 0);
    }
    
    /**
     * Returns a string representation of the Channel Entry.
     *
     * @return a string representation of the Channel Entry.
     */
    @Override
    public String toString() {
        return getKey();
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
     * Returns the name of the Channel Entry.
     *
     * @return The name of the Channel Entry.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the group of the Channel Entry.
     *
     * @return The group of the Channel Entry.
     */
    public String getGroup() {
        return group;
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
                .replaceAll("^~", Optional.ofNullable(parent).map(ChannelEntry::getOutputFolderPath).map(Matcher::quoteReplacement).orElse(""));
    }
    
    /**
     * Returns whether to save the content that is downloaded by the Channel Entry as audio files or not.
     *
     * @return Whether to save the content that is downloaded by the Channel Entry as audio files or not.
     */
    public boolean isSaveAsAudio() {
        return Optional.ofNullable(saveAsAudio).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::isSaveAsAudio).orElse(DEFAULT_SAVE_AS_AUDIO));
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
     * Returns whether to disregard the globally configured storage drive and video and music directories when determining the paths for the Channel Entry.
     *
     * @return Whether to disregard the globally configured storage drive and video and music directories when determining the paths for the Channel Entry.
     */
    public String getLocationPrefix() {
        return Optional.ofNullable(locationPrefix).orElseGet(() ->
                Optional.ofNullable(parent).map(ChannelEntry::getLocationPrefix).orElse(""));
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
    public ChannelGroup getParent() {
        return parent;
    }
    
    
    //Static Methods
    
    /**
     * Loads and validates a Channel Entry.
     *
     * @param configData The json data of the Channel Entry configuration.
     * @param parent     The parent of the Channel Entry.
     * @return The Channel Entry.
     * @throws RuntimeException When the configuration data does not contain the required fields.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ChannelEntry> T load(Map<String, Object> configData, ChannelGroup parent) {
        final ChannelEntry channelEntry = hasChildren(configData) ?
                                          new ChannelGroup(configData, parent) :
                                          new ChannelConfig(configData, parent);
        validateRequiredFields(channelEntry.getEffectiveConfig());
        return (T) channelEntry;
    }
    
    /**
     * Loads and validates a Channel Entry.
     *
     * @param configData The json data of the Channel Entry configuration.
     * @return The Channel Entry.
     * @throws RuntimeException When the configuration data does not contain the required fields.
     */
    public static <T extends ChannelEntry> T load(Map<String, Object> configData) {
        return load(configData, null);
    }
    
    /**
     * Determines whether the json data of a Channel Entry contains child configurations.
     *
     * @param configData The json data of the Channel Entry configuration.
     * @return Whether the configuration data contains child configurations.
     */
    protected static boolean hasChildren(Map<String, Object> configData) {
        return Optional.ofNullable(configData)
                .map(e -> e.containsKey("channels"))
                .orElse(false);
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
                        .replace("${D}", FileUtils.Config.storageDrive.getAbsolutePath())
                        .replace("${V}", FileUtils.Config.videoDir.getAbsolutePath())
                        .replace("${M}", FileUtils.Config.musicDir.getAbsolutePath()));
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
        return Optional.ofNullable(playlistId)
                .map(ChannelConfig.ChannelType::determineType)
                .map(type -> {
                    switch (type) {
                        case CHANNEL:
                            return WebUtils.getChannelUrl(playlistId);
                        case PLAYLIST:
                        case ALBUM:
                            return WebUtils.getPlaylistUrl(playlistId);
                        default:
                            return null;
                    }
                }).orElse(null);
    }
    
    /**
     * Validates that the json data of a Channel Entry contains all the required fields.
     *
     * @param configData The json data of the Channel Entry.
     * @throws RuntimeException When the configuration data does not contain the required fields.
     */
    protected static void validateRequiredFields(Map<String, Object> configData) {
        Optional.of((hasChildren(configData) ? ChannelGroup.REQUIRED_FIELDS : ChannelConfig.REQUIRED_FIELDS).stream()
                        .filter(e -> (MapUtility.getOrNull(configData, e) == null))
                        .collect(Collectors.toList()))
                .filter(e -> !e.isEmpty())
                .ifPresent(missingFields -> {
                    logger.warn(Color.bad("Channel" + (hasChildren(configData) ? " Group" : "") + ": ") + Color.channelKey((String) MapUtility.getOrNull(configData, "key")) +
                            Color.bad(" configuration missing ") + Color.number(missingFields.size()) + Color.bad(" required field" + ((missingFields.size() != 1) ? "s" : "") + ": ") +
                            missingFields.stream().map(Color::link).collect(Collectors.joining(Color.bad(", "))));
                    throw new RuntimeException();
                });
    }
    
}
