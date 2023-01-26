/*
 * File:    Channels.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.config.ChannelConfig;
import youtube.channel.config.ChannelEntry;
import youtube.channel.config.ChannelGroup;
import youtube.config.Color;
import youtube.config.Configurator;
import youtube.entity.Channel;
import youtube.util.FileUtils;
import youtube.util.PathUtils;
import youtube.util.Utils;

/**
 * Manages Channel Configs and Channel Groups for the Youtube Channel Downloader.
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
    public static final File CHANNELS_FILE = new File(PathUtils.WORKING_DIR, ("channels" + '.' + Utils.CONFIG_FILE_FORMAT));
    
    /**
     * The default drive to use for storage of downloaded files.
     */
    public static final String DEFAULT_STORAGE_DRIVE = PathUtils.getUserDrivePath();
    
    /**
     * The default Music directory in the storage drive.
     */
    public static final String DEFAULT_MUSIC_DIR = "Music/";
    
    /**
     * The default Videos directory in the storage drive.
     */
    public static final String DEFAULT_VIDEOS_DIR = "Videos/";
    
    
    //Static Fields
    
    /**
     * The map of Channels.
     */
    private static final Map<String, Channel> channels = new LinkedHashMap<>();
    
    /**
     * The map of Channel Configs.
     */
    private static final Map<String, ChannelConfig> configs = new LinkedHashMap<>();
    
    /**
     * The map of Channel Groups.
     */
    private static final Map<String, ChannelGroup> groups = new LinkedHashMap<>();
    
    /**
     * The Channels configuration root.
     */
    private static final ChannelGroup root = new ChannelGroup();
    
    /**
     * A flag indicating whether the Channels configuration has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    /**
     * A list of registered Channel Config keys.
     */
    private static final Set<String> channelKeys = new HashSet<>();
    
    /**
     * A list of registered Channel Config keys.
     */
    private static final Set<String> channelNames = new HashSet<>();
    
    /**
     * A list of registered Channel Group keys.
     */
    private static final Set<String> groupKeys = new HashSet<>();
    
    /**
     * A list of registered Channel Group names.
     */
    private static final Set<String> groupNames = new HashSet<>();
    
    /**
     * The drive to use for storage of downloaded files.
     */
    public static final File storageDrive = new File(Configurator.getSetting("location.storageDrive", DEFAULT_STORAGE_DRIVE));
    
    /**
     * The Music directory in the storage drive.
     */
    public static final File musicDir = new File(storageDrive, Configurator.getSetting("location.musicDir", DEFAULT_MUSIC_DIR));
    
    /**
     * The Videos directory in the storage drive.
     */
    public static final File videoDir = new File(storageDrive, Configurator.getSetting("location.videoDir", DEFAULT_VIDEOS_DIR));
    
    
    //Static Methods
    
    /**
     * Returns the list of Channels.
     *
     * @return The list of Channels.
     */
    public static List<Channel> getChannels() {
        return new ArrayList<>(channels.values());
    }
    
    /**
     * Returns the list of Channel Configs.
     *
     * @return The list of Channel Configs.
     */
    public static List<ChannelConfig> getConfigs() {
        return new ArrayList<>(configs.values());
    }
    
    /**
     * Returns the list of Channel Groups.
     *
     * @return The list of Channel Groups.
     */
    public static List<ChannelGroup> getGroups() {
        return new ArrayList<>(groups.values());
    }
    
    /**
     * Returns the Channels configuration root.
     *
     * @return The Channels configuration root.
     */
    public static ChannelGroup getRoot() {
        return root;
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
     * Returns a Channel Config of a specified key.
     *
     * @param key The key of the Channel Config.
     * @return The Channel Config of the specified key, or null if it does not exist.
     */
    public static ChannelConfig getConfig(String key) {
        return configs.get(key);
    }
    
    /**
     * Returns a Channel Group of a specified key.
     *
     * @param key The key of the Channel Group.
     * @return The Channel Group of the specified key, or null if it does not exist.
     */
    public static ChannelGroup getGroup(String key) {
        return groups.get(key);
    }
    
    /**
     * Returns index of a Channel of a specified key.
     *
     * @param key The key of the Channel.
     * @return The index of the Channel of the specified key, or -1 if it does not exist.
     */
    public static int channelIndex(String key) {
        return new ArrayList<>(channels.keySet()).indexOf(key);
    }
    
    /**
     * Returns index of a Channel Config of a specified key.
     *
     * @param key The key of the Channel Config.
     * @return The index of the Channel Config of the specified key, or -1 if it does not exist.
     */
    public static int configIndex(String key) {
        return new ArrayList<>(configs.keySet()).indexOf(key);
    }
    
    /**
     * Returns index of a Channel Group of a specified key.
     *
     * @param key The key of the Channel Group.
     * @return The index of the Channel Group of the specified key, or -1 if it does not exist.
     */
    public static int groupIndex(String key) {
        return new ArrayList<>(groups.keySet()).indexOf(key);
    }
    
    /**
     * Loads the Channels configuration from the channels file.
     */
    @SuppressWarnings("unchecked")
    public static void loadChannels() {
        if (loaded.compareAndSet(false, true)) {
            try {
                final List<Map<String, Object>> channelListData = (List<Map<String, Object>>) new JSONParser().parse(readChannelConfiguration());
                
                loadChannelList(channelListData, root);
                
            } catch (Exception e) {
                System.out.println(Color.bad("Could not load channels from: ") + Color.filePath(CHANNELS_FILE));
                System.out.println(Utils.INDENT + Color.bad(e));
                throw new RuntimeException(e);
            }
        }
        
        print();
    }
    
    /**
     * Loads a list of Channel Entry configurations.
     *
     * @param channelListData The json data of the channel list.
     * @param parent          The parent of the configurations in the channel list.
     */
    private static void loadChannelList(List<Map<String, Object>> channelListData, ChannelGroup parent) {
        for (Map<String, Object> channelEntryData : channelListData) {
            loadChannelEntry(channelEntryData, parent);
        }
    }
    
    /**
     * Loads a Channel Entry configuration.
     *
     * @param channelEntryData The json data of the Channel Entry.
     * @param parent           The parent of the Channel Entry.
     */
    @SuppressWarnings("unchecked")
    private static void loadChannelEntry(Map<String, Object> channelEntryData, ChannelGroup parent) {
        try {
            final ChannelEntry channelEntry = ChannelEntry.load(channelEntryData, parent);
            
            if (channelEntry.isChannel()) {
                registerChannelConfig((ChannelConfig) channelEntry);
            } else {
                registerChannelGroup((ChannelGroup) channelEntry);
                loadChannelList((List<Map<String, Object>>) channelEntryData.get("channels"), (ChannelGroup) channelEntry);
            }
            
        } catch (Exception e) {
            System.out.println(Color.bad("Could not load: ") + Color.channel(channelEntryData.getOrDefault("key", "null")));
            if ((e.getMessage() != null) && !e.getMessage().isEmpty()) {
                System.out.println(Utils.INDENT + Color.bad(e.getMessage()));
            }
        }
    }
    
    /**
     * Registers a Channel Config.
     *
     * @param channelConfig The Channel Config.
     */
    public static void registerChannelConfig(ChannelConfig channelConfig) {
        if (!channelKeys.add(channelConfig.getKey())) {
            System.out.println(Color.bad("A Channel with the key: ") + Color.channel(channelConfig.getKey()) + Color.bad(" has already been registered"));
            throw new RuntimeException();
        }
        if (!channelNames.add(channelConfig.getName())) {
            System.out.println(Color.bad("A Channel with the name: ") + Color.channel(channelConfig.getName()) + Color.bad(" has already been registered"));
            throw new RuntimeException();
        }
        
        configs.put(channelConfig.getKey(), channelConfig);
        channels.put(channelConfig.getKey(), new Channel(channelConfig));
    }
    
    /**
     * Registers a Channel Group.
     *
     * @param channelGroup The Channel Group.
     */
    public static void registerChannelGroup(ChannelGroup channelGroup) {
        if (!groupKeys.add(channelGroup.getKey())) {
            System.out.println(Color.bad("A Channel Group with the key: ") + Color.channel(channelGroup.getKey()) + Color.bad(" has already been registered"));
            throw new RuntimeException();
        }
        if (!groupNames.add(channelGroup.getName())) {
            System.out.println(Color.bad("A Channel Group with the name: ") + Color.channel(channelGroup.getName()) + Color.bad(" has already been registered"));
            throw new RuntimeException();
        }
        
        groups.put(channelGroup.getKey(), channelGroup);
    }
    
    /**
     * Reads the Channels configuration file.
     *
     * @return The content of the Channels configuration file.
     * @throws Exception When there is an issue reading the configuration file.
     */
    private static String readChannelConfiguration() throws Exception {
        return FileUtils.readLines(CHANNELS_FILE).stream()
                .filter(e -> !e.strip().startsWith("//"))
                .collect(Collectors.joining());
    }
    
    /**
     * Prints the Channels map.
     */
    public static void print() {
        if (!Configurator.Config.printChannels) {
            return;
        }
        
        System.out.println(Utils.NEWLINE);
        System.out.println(Color.number("--- Channels ---"));
        
        root.print();
        
        System.out.println(Utils.NEWLINE);
    }
    
}
