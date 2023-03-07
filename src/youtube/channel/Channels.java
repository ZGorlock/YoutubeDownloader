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
import youtube.util.LogUtils;
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
    
    
    //Static Fields
    
    /**
     * The Channels configuration root.
     */
    private static final ChannelGroup root = new ChannelGroup();
    
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
     * The list of Channel keys to be processed this run.
     */
    private static final List<String> filteredChannels = new ArrayList<>();
    
    /**
     * The list of registered Channel Config keys.
     */
    private static final Set<String> channelKeys = new HashSet<>();
    
    /**
     * The list of registered Channel Config keys.
     */
    private static final Set<String> channelNames = new HashSet<>();
    
    /**
     * The list of registered Channel Group keys.
     */
    private static final Set<String> groupKeys = new HashSet<>();
    
    /**
     * The list of registered Channel Group names.
     */
    private static final Set<String> groupNames = new HashSet<>();
    
    /**
     * The flag indicating whether the Channels configuration has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
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
     * Returns the list of Channel keys to be processed this run.
     *
     * @return The list of Channel keys to be processed this run.
     */
    public static List<String> getFiltered() {
        return new ArrayList<>(filteredChannels);
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
     * Loads the Channels configuration from the channels file.
     *
     * @throws RuntimeException When the Channels configuration could not be loaded.
     */
    @SuppressWarnings("unchecked")
    public static void loadChannels() {
        if (loaded.compareAndSet(false, true)) {
            logger.debug(Color.log("Loading Channels..."));
            
            Config.init();
            
            try {
                final List<Map<String, Object>> channelListData = (List<Map<String, Object>>) new JSONParser().parse(readChannelConfiguration());
                
                loadChannelList(channelListData, root);
                
            } catch (Exception e) {
                logger.error(Color.bad("Could not load channels from: ") + Color.quoteFilePath(CHANNELS_FILE), e);
                throw new RuntimeException(e);
            }
        }
        
        filterChannels();
        print();
    }
    
    /**
     * Determines the list of Channel to be processed this run.
     */
    private static void filterChannels() {
        filteredChannels.clear();
        
        if (Config.channel != null) {
            filteredChannels.add(Config.channel);
            
        } else {
            boolean skip = (Config.startAt != null);
            boolean stop = (Config.stopAt != null);
            
            for (ChannelConfig config : getConfigs()) {
                if (!(skip &= !config.getKey().equals(Config.startAt)) && config.isMemberOfGroup(Config.group)) {
                    filteredChannels.add(config.getKey());
                    if (stop && config.getKey().equals(Config.stopAt)) {
                        break;
                    }
                }
            }
        }
        
        filteredChannels.removeIf(key -> (getChannel(key) == null));
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
            logger.error(Color.bad("Could not load: ") + Color.channelKey((String) channelEntryData.get("key")), e);
        }
    }
    
    /**
     * Registers a Channel Config.
     *
     * @param channelConfig The Channel Config.
     * @throws RuntimeException When a Channel Config with the same <i>key</i> or <i>name</i> has already been registered.
     */
    public static void registerChannelConfig(ChannelConfig channelConfig) {
        if (!channelKeys.add(channelConfig.getKey())) {
            logger.warn(Color.bad("A Channel with the key: ") + Color.channelKey(channelConfig) + Color.bad(" has already been registered"));
            throw new RuntimeException();
        }
        if (!channelNames.add(channelConfig.getName())) {
            logger.warn(Color.bad("A Channel with the name: ") + Color.channelName(channelConfig) + Color.bad(" has already been registered"));
            throw new RuntimeException();
        }
        
        configs.put(channelConfig.getKey(), channelConfig);
        channels.put(channelConfig.getKey(), new Channel(channelConfig));
    }
    
    /**
     * Registers a Channel Group.
     *
     * @param channelGroup The Channel Group.
     * @throws RuntimeException When a Channel Group with the same <i>key</i> or <i>name</i> has already been registered.
     */
    public static void registerChannelGroup(ChannelGroup channelGroup) {
        if (!groupKeys.add(channelGroup.getKey())) {
            logger.warn(Color.bad("A Channel Group with the key: ") + Color.channelKey(channelGroup) + Color.bad(" has already been registered"));
            throw new RuntimeException();
        }
        if (!groupNames.add(channelGroup.getName())) {
            logger.warn(Color.bad("A Channel Group with the name: ") + Color.channelName(channelGroup) + Color.bad(" has already been registered"));
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
        if (!Config.printChannels) {
            return;
        }
        
        logger.trace(LogUtils.NEWLINE);
        logger.debug(Color.number("--- Channels ---"));
        
        root.print();
        
        logger.trace(LogUtils.NEWLINE);
    }
    
    
    //Inner Classes
    
    /**
     * Holds the channels Config.
     */
    public static class Config {
        
        //Constants
        
        /**
         * The default value of the flag indicating whether to print the Channel list at the start of the run or not.
         */
        public static final boolean DEFAULT_PRINT_CHANNELS = false;
        
        
        //Static Fields
        
        /**
         * A flag indicating whether to print the Channel list at the start of the run or not.
         */
        public static boolean printChannels = DEFAULT_PRINT_CHANNELS;
        
        /**
         * The Channel to process, or null if all Channels should be processed.
         */
        public static String channel = null;
        
        /**
         * The group to process, or null if all groups should be processed.
         */
        public static String group = null;
        
        /**
         * The Channel to start processing from, if processing all Channels.
         */
        public static String startAt = null;
        
        /**
         * The Channel to stop processing at, if processing all Channels.
         */
        public static String stopAt = null;
        
        
        //Static Methods
        
        /**
         * Initializes the Config.
         */
        private static void init() {
            printChannels = Configurator.getSetting(List.of(
                            "printChannels",
                            "log.printChannels",
                            "output.printChannels"),
                    DEFAULT_PRINT_CHANNELS);
            
            channel = Configurator.getSetting("filter.channel");
            group = Configurator.getSetting("filter.group");
            startAt = Configurator.getSetting("filter.startAt");
            stopAt = Configurator.getSetting("filter.stopAt");
        }
        
    }
    
}
