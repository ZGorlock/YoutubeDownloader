/*
 * File:    ChannelTree.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.console.Console;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.util.Color;
import youtube.util.Utils;

/**
 * Defines a Channel Tree entry of the Youtube Channel Downloader.
 */
public class ChannelTree {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelTree.class);
    
    
    //Static Fields
    
    /**
     * The Channel Tree root entry.
     */
    private static final ChannelTree root = new ChannelTree();
    
    
    //Fields
    
    /**
     * The key of the Channel Tree entry.
     */
    public String key = null;
    
    /**
     * A flag indicating whether the Channel Tree entry is enabled or not; true by default.
     */
    public boolean active = true;
    
    /**
     * The corresponding Channel of the Channel Tree entry.
     */
    public Channel channel = null;
    
    /**
     * The list of children of the Channel Tree entry.
     */
    public List<ChannelTree> children = new ArrayList<>();
    
    /**
     * The parent of the Channel Tree entry.
     */
    public ChannelTree parent = null;
    
    
    //Methods
    
    /**
     * Returns the canonical key of the Channel Tree entry.
     *
     * @return The canonical key of the Channel Tree entry.
     */
    public String getCanonicalKey() {
        String groupName = getCanonicalGroupKey();
        return String.join(".", groupName, key).replaceAll("^\\.|\\.$", "");
    }
    
    /**
     * Returns the canonical group key of the Channel Tree entry.
     *
     * @return The canonical group key of the Channel Tree entry.
     */
    public String getCanonicalGroupKey() {
        return (parent == null) ? "" : parent.getCanonicalKey();
    }
    
    /**
     * Returns the depth of the Channel Tree entry.
     *
     * @return The depth of the Channel Tree entry.
     */
    public int getDepth() {
        return 1 + ((parent == null) ? 0 : parent.getDepth());
    }
    
    /**
     * Returns whether the Channel Entry is a Channel or not.
     *
     * @return Whether the Channel Entry is a Channel or not.
     */
    public boolean isChannel() {
        return (channel != null);
    }
    
    /**
     * Returns whether the Channel Entry is a group or not.
     *
     * @return Whether the Channel Entry is a group or not.
     */
    public boolean isGroup() {
        return !isChannel();
    }
    
    /**
     * Returns whether the Channel Entry is active or not.
     *
     * @return Whether the Channel Entry is active or not.
     */
    public boolean isActive() {
        return active &&
                (!isChannel() || channel.active) &&
                ((parent == null) || parent.isActive());
    }
    
    /**
     * Returns whether the Channel Entry is a member of a specific group or not.
     *
     * @param group The group.
     * @return Whether the Channel Entry is a member of the specified group or not.
     */
    public boolean isMemberOfGroup(String group) {
        final BiPredicate<String, String> groupEquals = (String target, String test) ->
                (Stream.of(test, target)
                        .map(String::toLowerCase)
                        .map(e -> e.replaceAll("[^a-z\\d]", ""))
                        .map(e -> e.replaceAll("s$", ""))
                        .distinct().count() == 1);
        
        return ((group == null) || group.isEmpty() ||
                ((key != null) && groupEquals.test(key, group)) ||
                (isChannel() && groupEquals.test(channel.group, group)) ||
                ((parent != null) && parent.isMemberOfGroup(group)));
    }
    
    /**
     * Returns a list of all child Channel Tree entries.
     *
     * @return A list of all child Channel Tree entries.
     */
    public List<ChannelTree> getAllChildren() {
        if (children.isEmpty()) {
            return new ArrayList<>();
        }
        
        final List<ChannelTree> allChildren = new ArrayList<>();
        for (ChannelTree child : children) {
            allChildren.add(child);
            allChildren.addAll(child.getAllChildren());
        }
        return allChildren;
    }
    
    /**
     * Returns a list of all child Channel Tree entries which are groups.
     *
     * @return A list of all child Channel Tree entries which are groups.
     */
    public List<ChannelTree> getAllChildGroups() {
        return getAllChildren().stream().filter(ChannelTree::isGroup).collect(Collectors.toList());
    }
    
    /**
     * Returns a list of all child Channel Tree entries which are Channels.
     *
     * @return A list of all child Channel Tree entries which are Channels.
     */
    public List<ChannelTree> getAllChildChannels() {
        return getAllChildren().stream().filter(ChannelTree::isChannel).collect(Collectors.toList());
    }
    
    /**
     * Returns a list of all parent Channel Tree entries.
     *
     * @return A list of all parent Channel Tree entries.
     */
    public List<ChannelTree> getAllParents() {
        if (parent == null) {
            return new ArrayList<>();
        }
        
        final List<ChannelTree> allParents = new ArrayList<>();
        allParents.add(parent);
        allParents.addAll(parent.getAllParents());
        return allParents;
    }
    
    /**
     * Returns the child Channel Tree entry with a specific key.
     *
     * @param key The key.
     * @return The child Channel Tree entry with the specified key, or null if none exist.
     */
    public ChannelTree getChildByKey(String key) {
        if (this.key.equals(key)) {
            return this;
        }
        
        return children.stream()
                .map(child -> child.getChildByKey(key))
                .filter(Objects::nonNull)
                .findAny().orElse(null);
    }
    
    /**
     * Returns the child Channel Tree entry with a specific Channel.
     *
     * @param channel The Channel.
     * @return The child Channel Tree entry with the specified Channel, or null if none exist.
     */
    public ChannelTree getByChannel(Channel channel) {
        if (this.channel.equals(channel)) {
            return this;
        }
        
        return children.stream()
                .map(child -> child.getByChannel(channel))
                .filter(Objects::nonNull)
                .findAny().orElse(null);
    }
    
    /**
     * Prints the Channel Tree entry to the screen.
     *
     * @param indent The initial indent.
     */
    private void print(int indent) {
        if (key != null) {
            String title = isChannel() ? channel.key : (key + ':');
            Console.ConsoleEffect color = isGroup() ? (isActive() ? Color.LINK : (active ? Color.LOG : Color.BAD)) :
                                          (isActive() ? Color.CHANNEL : (channel.active ? Color.LOG : Color.BAD));
            
            System.out.println(StringUtility.repeatString(Utils.INDENT, indent) +
                    Color.apply(color, Utils.formatUnderscoredString(title)));
        }
        
        for (ChannelTree child : children) {
            child.print(indent + 1);
        }
    }
    
    /**
     * Prints the Channel Tree entry to the screen.
     */
    public void print() {
        print((parent == null) ? -1 : 0);
    }
    
    
    //Static Methods
    
    /**
     * Returns the Channel Tree root entry.
     *
     * @return The Channel Tree root entry.
     */
    public static ChannelTree getChannelTreeRoot() {
        return root;
    }
    
}
