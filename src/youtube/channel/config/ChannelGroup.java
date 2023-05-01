/*
 * File:    ChannelGroup.java
 * Package: youtube.channel.config
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a Channel Group of the Youtube Channel Downloader.
 */
public class ChannelGroup extends ChannelEntry {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelGroup.class);
    
    
    //Constants
    
    /**
     * A list of required fields in the configuration of a Channel Group.
     */
    public static final List<String> REQUIRED_FIELDS = List.of("key", "channels");
    
    /**
     * A list of base fields in the configuration of a Channel Group.
     */
    public static final List<String> BASE_FIELDS = List.of("key", "active", "channels");
    
    /**
     * A list of all fields in the configuration of a Channel Group.
     */
    public static final List<String> ALL_FIELDS = List.of("key", "active", "name", "group", "url", "playlistId", "outputFolder", "saveAsAudio", "savePlaylist", "reversePlaylist", "ignoreGlobalLocations", "keepClean", "channels");
    
    
    //Fields
    
    /**
     * The children of the Channel Group.
     */
    public List<ChannelEntry> children = new ArrayList<>();
    
    
    //Constructors
    
    /**
     * Creates a Channel Group.
     *
     * @param configData The json data of the Channel Group.
     * @param parent     The parent of the Channel Group.
     * @throws RuntimeException When the configuration data does not contain the required fields.
     */
    public ChannelGroup(Map<String, Object> configData, ChannelGroup parent) {
        super(configData, parent);
    }
    
    /**
     * Creates a Channel Group.
     *
     * @param configData The json data of the Channel Group.
     * @throws RuntimeException When the configuration data does not contain the required fields.
     */
    public ChannelGroup(Map<String, Object> configData) {
        this(configData, null);
    }
    
    /**
     * Creates an empty Channel Group.
     */
    public ChannelGroup() {
        super();
    }
    
    
    //Methods
    
    /**
     * Returns a list of all children of the Channel Group.
     *
     * @return A list of all children of the Channel Group.
     */
    public List<ChannelEntry> getAllChildren() {
        return Optional.ofNullable(children).filter(e -> !e.isEmpty())
                .map(children -> children.stream()
                        .flatMap(child -> Stream.concat(
                                Stream.of(child),
                                (child.isGroup() ? ((ChannelGroup) child).getAllChildren().stream() : Stream.empty())))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
    
    /**
     * Returns the configuration data of the Channel Group.
     *
     * @return The configuration data of the Channel Group.
     * @throws UnsupportedOperationException When the produced configuration data does not contain all fields.
     */
    @Override
    public Map<String, Object> getConfig() {
        final Map<String, Object> fields = super.getConfig();
        fields.put("channels", children);
        
        if (!ALL_FIELDS.stream().allMatch(fields::containsKey)) {
            throw new UnsupportedOperationException();
        }
        return fields;
    }
    
    /**
     * Returns the effective configuration data of the Channel Group.
     *
     * @return The effective configuration data of the Channel Group.
     * @throws UnsupportedOperationException When the produced effective configuration data does not contain all fields.
     */
    @Override
    public Map<String, Object> getEffectiveConfig() {
        final Map<String, Object> fields = super.getEffectiveConfig();
        fields.put("channels", getChildren());
        
        if (!ALL_FIELDS.stream().allMatch(fields::containsKey)) {
            throw new UnsupportedOperationException();
        }
        return fields;
    }
    
    /**
     * Prints the Channel Group to the screen.
     *
     * @param indent The initial indent.
     */
    @Override
    protected void print(int indent) {
        super.print(indent);
        children.forEach(child -> child.print(indent + 1));
    }
    
    
    //Getters
    
    /**
     * Returns the children of the Channel Group.
     *
     * @return The children of the Channel Group.
     */
    public List<ChannelEntry> getChildren() {
        return children;
    }
    
}
