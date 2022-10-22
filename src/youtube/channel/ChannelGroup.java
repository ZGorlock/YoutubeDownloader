/*
 * File:    ChannelGroup.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines a Channel of the Youtube Channel Downloader.
 */
public class ChannelGroup extends ChannelEntry {
    
    //Constants
    
    /**
     * A list of required fields in a Channel Group configuration.
     */
    public static final List<String> REQUIRED_FIELDS = List.of("key", "channels");
    
    /**
     * A list of base fields in a Channel Group configuration.
     */
    public static final List<String> BASE_FIELDS = List.of("key", "active", "channels");
    
    /**
     * A list of all fields in a Channel Group configuration.
     */
    public static final List<String> ALL_FIELDS = List.of("key", "active", "group", "url", "playlistId", "outputFolder", "saveAsMp3", "reversePlaylist", "ignoreGlobalLocations", "keepClean", "channels");
    
    /**
     * The key of the field containing the children of a Channel Group.
     */
    public static final String CHILD_CONFIGURATION_KEY = "channels";
    
    
    //Fields
    
    /**
     * The children of the Channel Group.
     */
    protected List<ChannelEntry> children = new ArrayList<>();
    
    
    //Constructors
    
    /**
     * Creates a Channel Group.
     *
     * @param fields The fields from the Channel Group configuration.
     * @param parent The parent of the Channel Group configuration.
     * @throws Exception When the Channel Group configuration does not contain all of the required fields.
     */
    public ChannelGroup(Map<String, Object> fields, ChannelGroup parent) throws Exception {
        super(fields, parent);
    }
    
    /**
     * Creates a Channel Group.
     *
     * @param fields The fields from the Channel Group configuration.
     * @throws Exception When the Channel Group configuration does not contain all of the required fields.
     */
    public ChannelGroup(Map<String, Object> fields) throws Exception {
        this(fields, null);
    }
    
    /**
     * The default no-argument constructor for a Channel Group.
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
     * Returns the map of the field values of the Channel Group.
     *
     * @return The map of the field values of the Channel Group.
     */
    @Override
    public Map<String, Object> getFields() {
        final Map<String, Object> fields = super.getFields();
        fields.put(CHILD_CONFIGURATION_KEY, children);
        
        if (!ALL_FIELDS.stream().allMatch(fields::containsKey)) {
            throw new UnsupportedOperationException();
        }
        return fields;
    }
    
    /**
     * Returns the map of the effective field values of the Channel Group.
     *
     * @return The map of the effective field values of the Channel Group.
     */
    @Override
    public Map<String, Object> getEffectiveFields() {
        final Map<String, Object> fields = super.getEffectiveFields();
        fields.put(CHILD_CONFIGURATION_KEY, getChildren());
        
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
        return new ArrayList<>(children);
    }
    
}
