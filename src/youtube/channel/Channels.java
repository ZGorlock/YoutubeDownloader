/*
 * File:    Channels.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.conf.Color;
import youtube.conf.Configurator;
import youtube.util.FileUtils;
import youtube.util.PathUtils;
import youtube.util.Utils;

/**
 * Holds Channels and Playlists for the Youtube Channel Downloader.
 */
public class Channels {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Channels.class);
    
    
    //Constants
    
    /**
     * The file containing the Channel configuration for the Youtube Downloader.
     */
    public static final File CHANNELS_FILE = new File(PathUtils.WORKING_DIR, "channels.json");
    
    /**
     * The default drive to use for storage of downloaded files.
     */
    public static final File DEFAULT_STORAGE_DRIVE = PathUtils.getUserDrive();
    
    /**
     * The default Music directory in the storage drive.
     */
    public static final File DEFAULT_MUSIC_DIR = new File(PathUtils.getUserHome(), "Music");
    
    /**
     * The default Videos directory in the storage drive.
     */
    public static final File DEFAULT_VIDEOS_DIR = new File(PathUtils.getUserHome(), "Videos");
    
    
    //Static Fields
    
    /**
     * The list of Channels.
     */
    private static final Map<String, Channel> channels = new LinkedHashMap<>();
    
    /**
     * The Channel Tree root entry.
     */
    private static final ChannelTree channelTree = ChannelTree.getChannelTreeRoot();
    
    /**
     * A flag indicating whether the Channel configuration has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    /**
     * The drive to use for storage of downloaded files.
     */
    public static final File storageDrive = Optional.ofNullable((String) Configurator.getSetting("location.storageDrive"))
            .map(File::new)
            .orElse(DEFAULT_STORAGE_DRIVE);
    
    /**
     * The Music directory in the storage drive.
     */
    public static final File musicDir = Optional.ofNullable((String) Configurator.getSetting("location.musicDir"))
            .map(e -> new File(storageDrive, e))
            .orElse(DEFAULT_MUSIC_DIR);
    
    /**
     * The Videos directory in the storage drive.
     */
    public static final File videoDir = Optional.ofNullable((String) Configurator.getSetting("location.videoDir"))
            .map(e -> new File(storageDrive, e))
            .orElse(DEFAULT_VIDEOS_DIR);
    
    
    //Functions
    
    /**
     * Returns the list of Channels.
     *
     * @return The list of Channels.
     */
    public static List<Channel> getChannels() {
        return new ArrayList<>(channels.values());
    }
    
    /**
     * Returns the list of Channel groups.
     *
     * @return The list of Channel groups.
     */
    public static List<String> getGroups() {
        return channelTree.getAllChildGroups().stream().map(group -> group.key).collect(Collectors.toList());
    }
    
    /**
     * Returns the Channel map.
     *
     * @return The Channel map.
     */
    public static Map<String, List<Channel>> getChannelMap() {
        Map<String, List<Channel>> channelMap = new LinkedHashMap<>();
        channels.values().forEach(channel -> {
            channelMap.putIfAbsent(channel.group, new ArrayList<>());
            channelMap.get(channel.group).add(channel);
        });
        return channelMap;
    }
    
    /**
     * Returns a Channel of a specified key.
     *
     * @param key The key of the Channel.
     * @return The Channel of the specified key, or null if it does not exist.
     */
    public static Channel getChannel(String key) {
        return channels.get(key);
    }
    
    /**
     * Returns index of a Channel of a specified key.
     *
     * @param key The key of the Channel.
     * @return The index of the Channel of the specified key, or -1 if it does not exist.
     */
    public static int indexOf(String key) {
        return new ArrayList<>(channels.keySet()).indexOf(key);
    }
    
    /**
     * Loads the Channel configuration from the channels file.
     */
    public static void loadChannels() {
        if (loaded.compareAndSet(false, true)) {
            try {
                String jsonString = FileUtils.readFileToString(CHANNELS_FILE);
                JSONParser parser = new JSONParser();
                JSONArray channelList = (JSONArray) parser.parse(jsonString);
                
                loadChannelList(channelList, channelTree);
                
            } catch (IOException | ParseException e) {
                System.out.println(Color.bad("Could not load channels from: ") + Color.filePath(CHANNELS_FILE));
                System.out.println(Utils.INDENT + Color.bad(e));
                throw new RuntimeException(e);
            }
        }
        
        Channels.print();
    }
    
    /**
     * Loads the Channel configuration from a json channel list.
     *
     * @param channelList  The json channel list.
     * @param currentGroup The current group being loaded.
     */
    @SuppressWarnings("unchecked")
    private static void loadChannelList(JSONArray channelList, ChannelTree currentGroup) {
        for (Object channelEntry : channelList) {
            JSONObject channelJson = (JSONObject) channelEntry;
            
            ChannelTree currentChannel = new ChannelTree();
            currentChannel.key = ((String) channelJson.getOrDefault("key", "")).replace(".", "");
            currentChannel.active = (boolean) channelJson.getOrDefault("active", Channel.DEFAULT_ACTIVE);
            currentChannel.parent = currentGroup;
            currentGroup.children.add(currentChannel);
            
            try {
                if (channelJson.containsKey("channels")) {
                    loadChannelList((JSONArray) channelJson.get("channels"), currentChannel);
                    
                } else {
                    Channel channel = new Channel(channelJson);
                    channel.treeEntry = currentChannel;
                    channel.state.load();
                    
                    channels.put(channel.key, channel);
                    currentChannel.channel = channel;
                }
                
            } catch (Exception e) {
                System.out.println(Color.bad("Could not load channel: ") + Color.channel(channelJson.getOrDefault("key", "null")));
                if ((e.getMessage() != null) && !e.getMessage().isEmpty()) {
                    System.out.println(Utils.INDENT + Color.bad(e.getMessage()));
                }
            }
        }
    }
    
    /**
     * Prints the Channel map.
     */
    public static void print() {
        if (!Configurator.Config.printChannels) {
            return;
        }
        
        System.out.println(Utils.NEWLINE);
        System.out.println(Color.number("--- Channels ---"));
        
        channelTree.print();
        
        System.out.println(Utils.NEWLINE);
    }
    
}
