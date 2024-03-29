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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.access.Project;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
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
    public static final File CHANNELS_FILE = new File(PathUtils.WORKING_DIR, FileUtils.setFormat("channels", FileUtils.CONFIG_FILE_FORMAT));
    
    /**
     * The Channel data directory.
     */
    public static final File CHANNELS_DATA_DIR = new File(Project.DATA_DIR, "channel");
    
    
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
     * Returns whether a Channel is filtered.
     *
     * @param key The key of the Channel.
     * @return Whether a Channel is filtered.
     */
    public static boolean isFiltered(String key) {
        return filteredChannels.contains(key);
    }
    
    /**
     * Initializes the Channels configuration.
     *
     * @return Whether the Channels configuration was successfully initialized.
     */
    public static boolean initChannels() {
        if (loaded.compareAndSet(false, true)) {
            logger.trace(LogUtils.NEWLINE);
            logger.debug(Color.log("Initializing Channels..."));
            
            Config.init();
            
            loadChannels();
            filterChannels();
            
            print();
            
            return true;
        }
        return false;
    }
    
    /**
     * Loads the Channels configuration from the channels file.
     *
     * @throws RuntimeException When the Channels configuration could not be loaded.
     */
    @SuppressWarnings("unchecked")
    private static void loadChannels() {
        logger.debug(Color.log("Loading Channels..."));
        
        try {
            final List<Map<String, Object>> channelListData = (List<Map<String, Object>>) new JSONParser().parse(readChannelConfiguration());
            
            loadChannelList(channelListData, root);
            
        } catch (Exception e) {
            logger.error(Color.bad("Could not load channels from: ") + Color.quoteFilePath(CHANNELS_FILE), e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Determines the list of Channel to be processed this run.
     */
    private static void filterChannels() {
        filteredChannels.clear();
        
        if (!Config.enableFiltering) {
            getConfigs().stream().map(ChannelEntry::getKey)
                    .forEachOrdered(filteredChannels::add);
            
        } else if (!ListUtility.isNullOrEmpty(Config.channelList)) {
            filteredChannels.addAll(Config.channelList);
        } else if (Config.channel != null) {
            filteredChannels.add(Config.channel);
            
        } else {
            boolean skip = (Config.startAt != null);
            boolean stop = (Config.stopAt != null);
            
            final Predicate<ChannelConfig> groupFilter = (ChannelConfig config) ->
                    Optional.ofNullable(Config.groupList).filter(e -> !e.isEmpty())
                            .map(groups -> groups.stream().anyMatch(config::isMemberOfGroup))
                            .orElseGet(() -> config.isMemberOfGroup(Config.group));
            
            for (ChannelConfig config : getConfigs()) {
                if (!(skip &= !config.getKey().equals(Config.startAt)) && groupFilter.test(config)) {
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
     * @throws RuntimeException When there is an issue reading the configuration file.
     */
    private static String readChannelConfiguration() {
        return Optional.of(CHANNELS_FILE).map(Filesystem::readLines)
                .map(lines -> lines.stream()
                        .filter(line -> !StringUtility.isNullOrBlank(line))
                        .filter(line -> !line.strip().startsWith("//"))
                        .collect(Collectors.joining()))
                .orElseThrow(() -> new RuntimeException(new IOException("Error reading: " + PathUtils.path(CHANNELS_FILE))));
    }
    
    /**
     * Returns the Channel caches present in the Channels data directory.
     *
     * @return The Channel caches present in the Channels data directory.
     */
    public static List<File> fetchAllChannelCaches() {
        return Filesystem.getDirs(CHANNELS_DATA_DIR);
    }
    
    /**
     * Returns the Channel cache with a particular Channel name.
     *
     * @param channelName The name of the Channel.
     * @return The Channel cache with the specified Channel name.
     */
    public static File fetchChannelCache(String channelName) {
        return Optional.ofNullable(channelName)
                .map(name -> new File(CHANNELS_DATA_DIR, name))
                .orElse(null);
    }
    
    /**
     * Returns the Channel cache for a particular Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @return The Channel cache for the specified Channel, or null if it does not exist.
     */
    public static File fetchChannelCache(ChannelConfig channelConfig) {
        return fetchChannelCache(channelConfig.getName());
    }
    
    /**
     * Returns the Channel cache for a particular Channel.
     *
     * @param channel The Channel.
     * @return The Channel cache for the specified Channel, or null if it does not exist.
     */
    public static File fetchChannelCache(Channel channel) {
        return fetchChannelCache(channel.getConfig());
    }
    
    /**
     * Returns whether Channel filtering is enabled and active.
     *
     * @return Whether Channel filtering is enabled and active.
     */
    public static boolean isFilterActive() {
        return Config.enableFiltering && (filteredChannels.size() != channels.size());
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
        
        /**
         * The default value of the flag indicating whether to enable filtering or not.
         */
        public static final boolean DEFAULT_ENABLE_FILTERING = true;
        
        /**
         * The default value of the flag indicating whether to enable custom rename processes or not.
         */
        public static final boolean DEFAULT_ENABLE_CUSTOM_RENAME_PROCESSES = true;
        
        /**
         * The default value of the flag indicating whether to enable custom filter processes or not.
         */
        public static final boolean DEFAULT_ENABLE_CUSTOM_FILTER_PROCESSES = true;
        
        
        //Static Fields
        
        /**
         * A flag indicating whether to print the Channel list at the start of the run or not.
         */
        public static boolean printChannels = DEFAULT_PRINT_CHANNELS;
        
        /**
         * A flag indicating whether to enable filtering or not.
         */
        public static boolean enableFiltering = DEFAULT_ENABLE_FILTERING;
        
        /**
         * The Channel to process, or null if all Channels should be processed.
         */
        public static String channel = null;
        
        /**
         * The list Channel to process, or empty or null if all Channels should be processed.
         */
        public static List<String> channelList = null;
        
        /**
         * The group to process, or null if all groups should be processed.
         */
        public static String group = null;
        
        /**
         * The list groups to process, or empty or null if all groups should be processed.
         */
        public static List<String> groupList = null;
        
        /**
         * The Channel to start processing from, if processing all Channels.
         */
        public static String startAt = null;
        
        /**
         * The Channel to stop processing at, if processing all Channels.
         */
        public static String stopAt = null;
        
        /**
         * A flag indicating whether to enable custom rename processes or not.
         */
        public static boolean enableCustomRenameProcesses = DEFAULT_ENABLE_CUSTOM_RENAME_PROCESSES;
        
        /**
         * A flag indicating whether to enable custom filter processes or not.
         */
        public static boolean enableCustomFilterProcesses = DEFAULT_ENABLE_CUSTOM_FILTER_PROCESSES;
        
        
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
            
            enableFiltering = Configurator.getSetting(List.of(
                            "enableFiltering",
                            "filter.enableFiltering",
                            "filter.enable"),
                    DEFAULT_ENABLE_FILTERING);
            
            channel = Configurator.getSetting("filter.channel");
            channelList = Configurator.getSetting("filter.channelList");
            
            group = Configurator.getSetting("filter.group");
            groupList = Configurator.getSetting("filter.groupList");
            
            startAt = Configurator.getSetting("filter.startAt");
            stopAt = Configurator.getSetting("filter.stopAt");
            
            enableCustomRenameProcesses = Configurator.getSetting(List.of(
                            "enableCustomRenameProcesses",
                            "process.enableCustomRenameProcesses",
                            "process.name.enableCustomRenameProcesses",
                            "name.enableCustomRenameProcesses"),
                    DEFAULT_ENABLE_CUSTOM_RENAME_PROCESSES);
            enableCustomFilterProcesses = Configurator.getSetting(List.of(
                            "enableCustomFilterProcesses",
                            "process.enableCustomFilterProcesses",
                            "process.filter.enableCustomFilterProcesses",
                            "filter.enableCustomFilterProcesses"),
                    DEFAULT_ENABLE_CUSTOM_FILTER_PROCESSES);
        }
        
    }
    
}
