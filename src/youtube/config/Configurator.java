/*
 * File:    Configurator.java
 * Package: youtube.config
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.config;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import commons.object.collection.MapUtility;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.util.FileUtils;
import youtube.util.PathUtils;
import youtube.util.Utils;

/**
 * Handles configuration of the Youtube Downloader.
 */
public class Configurator {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Configurator.class);
    
    
    //Constants
    
    /**
     * The file containing the configuration for the Youtube Downloader.
     */
    public static final File CONF_FILE = new File(PathUtils.WORKING_DIR, ("conf" + '.' + Utils.CONFIG_FILE_FORMAT));
    
    
    //Enums
    
    /**
     * An enumeration of Programs in the Youtube Downloader project.
     */
    public enum Program {
        
        //Values
        
        YOUTUBE_CHANNEL_DOWNLOADER(ConfigSection.YOUTUBE_CHANNEL_DOWNLOADER),
        YOUTUBE_DOWNLOADER(ConfigSection.YOUTUBE_DOWNLOADER);
        
        
        //Fields
        
        /**
         * The root Config Section for the Program.
         */
        private final ConfigSection configRoot;
        
        
        //Constructors
        
        /**
         * Constructs a Program.
         *
         * @param configRoot The root Config Section for the Program.
         */
        Program(ConfigSection configRoot) {
            this.configRoot = configRoot;
        }
        
        
        //Getters
        
        /**
         * Returns the root Config Section for the Program.
         *
         * @return The root Config Section for the Program.
         */
        private ConfigSection getConfigRoot() {
            return configRoot;
        }
        
    }
    
    /**
     * An enumeration of defined Config Sections in the configuration file.
     */
    public enum ConfigSection {
        
        //Values
        
        YOUTUBE_CHANNEL_DOWNLOADER("YoutubeChannelDownloader", true),
        YOUTUBE_DOWNLOADER("YoutubeDownloader", true),
        SPONSOR_BLOCK("sponsorBlock", false),
        COLOR("color", false),
        LOG("log", false);
        
        
        //Fields
        
        /**
         * The key of the Config Section.
         */
        public final String key;
        
        /**
         * A flag indicating whether the Config Section is a root Section.
         */
        public final boolean root;
        
        
        //Constructors
        
        /**
         * Constructs a Config Section.
         *
         * @param key  The key of the Config Section.
         * @param root Whether the Config Section is a root Section.
         */
        ConfigSection(String key, boolean root) {
            this.key = key;
            this.root = root;
        }
        
        
        //Methods
        
        /**
         * Returns the key of a configuration setting that is a member of the Config Section.
         *
         * @param subKey The sub key of the configuration setting.
         * @return The key of the configuration setting.
         */
        public String getSettingKey(String subKey) {
            return getKey() + '.' + subKey;
        }
        
        
        //Getters
        
        /**
         * Returns the key of the Config Section.
         *
         * @return The key of the Config Section.
         */
        public String getKey() {
            return key;
        }
        
        /**
         * Returns whether the Config Section is a root Section.
         *
         * @return Whether the Config Section is a root Section.
         */
        public boolean isRoot() {
            return root;
        }
        
        
        //Static Methods
        
        /**
         * Determines whether the key of a Config Section represents a root Section or not.
         *
         * @param key The key of the Config Section.
         * @return Whether the key represents a root Section.
         */
        public static boolean isRootSection(String key) {
            return Arrays.stream(values())
                    .filter(section -> section.getKey().equals(key))
                    .findFirst().map(ConfigSection::isRoot)
                    .orElse(false);
        }
        
    }
    
    
    //Static Fields
    
    /**
     * The active program.
     */
    public static Program activeProgram = null;
    
    /**
     * The active settings.
     */
    public static Map<String, Object> activeSettings = new TreeMap<>(String::compareTo);
    
    /**
     * A cache of configuration settings from the configuration file.
     */
    private static final Map<String, Map<String, Object>> settings = new HashMap<>();
    
    /**
     * A flag indicating whether the configuration settings have been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Static Methods
    
    /**
     * Fetches the active configuration settings.
     *
     * @return The active configuration settings.
     */
    public static Map<String, Object> getSettings() {
        return Optional.ofNullable(activeSettings)
                .orElseGet(MapUtility::emptyMap);
    }
    
    /**
     * Fetches an active configuration setting.
     *
     * @param key The key of the configuration setting.
     * @param def The default value to return if the configuration setting does not exist.
     * @param <T> The type of the setting.
     * @return The value of the configuration setting, or the default value if it does not exist.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSetting(String key, T def) {
        return (T) Optional.ofNullable(getSettings())
                .map(e -> e.getOrDefault(key, def))
                .orElse(null);
    }
    
    /**
     * Fetches an active configuration setting.
     *
     * @param key The key of the configuration setting.
     * @param <T> The type of the setting.
     * @return The value of the configuration setting, or null if it does not exist.
     */
    public static <T> T getSetting(String key) {
        return getSetting(key, null);
    }
    
    /**
     * Fetches an active configuration setting.
     *
     * @param keyOptions The list of options for the key of the configuration setting, in order of precedence.
     * @param def        The default value to return if the configuration setting does not exist.
     * @param <T>        The type of the setting.
     * @return The value of the configuration setting, or the default value if it does not exist.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSetting(List<String> keyOptions, T def) {
        return (T) Optional.ofNullable(keyOptions)
                .stream().flatMap(Collection::stream)
                .map(Configurator::getSetting)
                .filter(Objects::nonNull)
                .findFirst().orElse(def);
    }
    
    /**
     * Fetches an active configuration setting.
     *
     * @param keyOptions The list of options for the key of the configuration setting, in order of precedence.
     * @param <T>        The type of the setting.
     * @return The value of the configuration setting, or null if it does not exist.
     */
    public static <T> T getSetting(List<String> keyOptions) {
        return getSetting(keyOptions, null);
    }
    
    /**
     * Returns the configuration settings defined in a section of the configuration file.
     *
     * @param section The key of the Config Section.
     * @return The configuration settings defined in the Config Section.
     */
    public static Map<String, Object> getDefinedSettings(String section) {
        return Optional.ofNullable(section)
                .map(settings::get).map(MapUtility::clone)
                .orElseGet(MapUtility::emptyMap);
    }
    
    /**
     * Returns the configuration settings defined in a section of the configuration file.
     *
     * @param section The Config Section.
     * @return The configuration settings defined in the Config Section.
     */
    public static Map<String, Object> getDefinedSettings(ConfigSection section) {
        return Optional.ofNullable(section)
                .map(ConfigSection::getKey)
                .map(Configurator::getDefinedSettings)
                .orElseGet(MapUtility::emptyMap);
    }
    
    /**
     * Returns a configuration setting defined in the configuration file.
     *
     * @param section The key of the Config Section.
     * @param subKey  The sub key of the configuration setting.
     * @param def     The default value to return if the configuration setting does not exist.
     * @param <T>     The type of the setting.
     * @return The value of the configuration setting defined in the configuration file, or the default value if it does not exist.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDefinedSetting(String section, String subKey, T def) {
        return (T) Optional.of(section)
                .map(Configurator::getDefinedSettings)
                .map(e -> e.getOrDefault(subKey, def))
                .orElse(null);
    }
    
    /**
     * Returns a configuration setting defined in the configuration file.
     *
     * @param section The key of the Config Section.
     * @param subKey  The sub key of the configuration setting.
     * @param <T>     The type of the setting.
     * @return The value of the configuration setting defined in the configuration file, or the default value if it does not exist.
     */
    public static <T> T getDefinedSetting(String section, String subKey) {
        return getDefinedSetting(section, subKey, null);
    }
    
    /**
     * Returns a configuration setting defined in the configuration file.
     *
     * @param section The Config Section.
     * @param subKey  The sub key of the configuration setting.
     * @param def     The default value to return if the configuration setting does not exist.
     * @param <T>     The type of the setting.
     * @return The value of the configuration setting defined in the configuration file, or the default value if it does not exist.
     */
    public static <T> T getDefinedSetting(ConfigSection section, String subKey, T def) {
        return Optional.of(section)
                .map(ConfigSection::getKey)
                .map(sectionKey -> getDefinedSetting(sectionKey, subKey, def))
                .orElse(null);
    }
    
    /**
     * Returns a configuration setting defined in the configuration file.
     *
     * @param section The Config Section.
     * @param subKey  The sub key of the configuration setting.
     * @param <T>     The type of the setting.
     * @return The value of the configuration setting defined in the configuration file, or the default value if it does not exist.
     */
    public static <T> T getDefinedSetting(ConfigSection section, String subKey) {
        return getDefinedSetting(section, subKey, null);
    }
    
    /**
     * Loads the settings configuration from the configuration file.
     *
     * @param program The program.
     * @throws RuntimeException When the settings configuration could not be loaded.
     */
    @SuppressWarnings("unchecked")
    public static void loadSettings(Program program) {
        if (loaded.compareAndSet(false, true)) {
            activeProgram = program;
            
            try {
                final Map<String, Object> settingsData = (Map<String, Object>) new JSONParser().parse(readConfiguration());
                settingsData.keySet().forEach(section ->
                        loadSettingSection(settingsData, section));
                
                activeSettings.putAll(getDefinedSettings(activeProgram.getConfigRoot()));
                settings.entrySet().stream()
                        .filter(section -> !ConfigSection.isRootSection(section.getKey()))
                        .forEach(section -> section.getValue().forEach((settingKey, settingValue) ->
                                activeSettings.putIfAbsent((section.getKey() + '.' + settingKey), settingValue)));
                
            } catch (Exception e) {
                logger.error(Color.bad("Could not load settings from: ") + Color.quoteFilePath(CONF_FILE), e);
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Loads a section of the setting configurations.
     *
     * @param settingsData The json data of the settings.
     * @param section      The config section.
     */
    @SuppressWarnings("unchecked")
    private static void loadSettingSection(Map<String, Object> settingsData, String section) {
        final Map<String, Object> settingSectionData = (Map<String, Object>) settingsData.get(section);
        
        if (settingSectionData != null) {
            if (section.equals("sponsorBlock") && (SponsorBlocker.globalConfig == null)) {
                SponsorBlocker.loadGlobalConfig(settingSectionData);
            }
            loadSettingSection(settingSectionData, section, "");
        }
    }
    
    /**
     * Loads a section of the setting configurations.
     *
     * @param settingSectionData The json data of the settings section.
     * @param section            The config section.
     * @param prefix             The name prefix of the setting configurations in the section.
     */
    private static void loadSettingSection(Map<String, Object> settingSectionData, String section, String prefix) {
        for (Map.Entry<String, Object> settingEntryData : settingSectionData.entrySet()) {
            loadSettingEntry(settingEntryData, section, prefix);
        }
    }
    
    /**
     * Loads a setting configuration.
     *
     * @param settingEntryData The json data of the setting.
     * @param section          The config section of the setting.
     * @param prefix           The name prefix of the setting.
     */
    @SuppressWarnings("unchecked")
    private static void loadSettingEntry(Map.Entry<String, Object> settingEntryData, String section, String prefix) {
        if ((settingEntryData.getValue() != null) && (settingEntryData.getValue() instanceof Map)) {
            if (settingEntryData.getKey().equals("sponsorBlock")) {
                SponsorBlocker.loadGlobalConfig(((Map<String, Object>) settingEntryData.getValue()));
            }
            loadSettingSection(((Map<String, Object>) settingEntryData.getValue()), section, (prefix + settingEntryData.getKey() + '.'));
        } else {
            settings.putIfAbsent(section, new TreeMap<>(String::compareTo));
            settings.get(section).put((prefix + settingEntryData.getKey()), settingEntryData.getValue());
        }
        
    }
    
    /**
     * Reads the configuration file.
     *
     * @return The content of the configuration file.
     * @throws Exception When there is an issue reading the configuration file.
     */
    private static String readConfiguration() throws Exception {
        return FileUtils.readLines(CONF_FILE).stream()
                .filter(e -> !e.strip().startsWith("//"))
                .collect(Collectors.joining());
    }
    
    /**
     * Holds a Config.
     */
    public static class Config {
        
        //Constants
        
        /**
         * The default value of the flag indicating whether to download only pre-merged formats or not; only used when using yt-dlp.
         */
        public static final boolean DEFAULT_PRE_MERGED = true;
        
        /**
         * The default value of the flag indicating whether to download the videos as mp3 files or not.
         */
        public static final boolean DEFAULT_AS_MP3 = false;
        
        /**
         * The default value of the flag indicating whether to move files to the recycling bin instead of deleting them.
         */
        public static final boolean DEFAULT_DELETE_TO_RECYCLING_BIN = false;
        
        /**
         * The default value of the flag indicating whether to prohibit the use of browser cookies in an attempt to download restricted videos or not.
         */
        public static final boolean DEFAULT_NEVER_USE_BROWSER_COOKIES = true;
        
        /**
         * The default value of the flag indicating whether to retry previously failed videos or not.
         */
        public static final boolean DEFAULT_RETRY_PREVIOUS_FAILURES = false;
        
        /**
         * The default value of the flag indicating whether to run in safe mode or not.
         */
        public static final boolean DEFAULT_SAFE_MODE = false;
        
        /**
         * The default value of the flag indicating whether to disable running the main code or not.
         */
        public static final boolean DEFAULT_PREVENT_RUN = false;
        
        /**
         * The default value of the flag indicating whether to disable processing Channels or not.
         */
        public static final boolean DEFAULT_PREVENT_PROCESS = false;
        
        /**
         * The default value of the flag indicating whether to disable downloading content or not.
         */
        public static final boolean DEFAULT_PREVENT_DOWNLOAD = false;
        
        /**
         * The default value of the flag indicating whether to globally prevent any media deletion or not.
         */
        public static final boolean DEFAULT_PREVENT_DELETION = false;
        
        /**
         * The default value of the flag indicating whether to globally prevent any media renaming or not.
         */
        public static final boolean DEFAULT_PREVENT_RENAMING = false;
        
        /**
         * The default value of the flag indicating whether to disable playlist modification or not.
         */
        public static final boolean DEFAULT_PREVENT_PLAYLIST_EDIT = false;
        
        /**
         * The default value of the flag indicating whether to disable fetching the latest data for Channels or not.
         */
        public static final boolean DEFAULT_PREVENT_CHANNEL_FETCH = false;
        
        /**
         * The default value of the flag indicating whether to disable fetching the info for Videos or not.
         */
        public static final boolean DEFAULT_PREVENT_VIDEO_FETCH = false;
        
        /**
         * The default value of the flag indicating whether to disable automatic updating of the yt-dlp or youtube-dl executables or not.
         */
        public static final boolean DEFAULT_PREVENT_EXE_AUTO_UPDATE = false;
        
        /**
         * The default value of the flag indicating whether to disable checking the latest version of the yt-dlp or youtube-dl executables or not.
         */
        public static final boolean DEFAULT_PREVENT_EXE_VERSION_CHECK = false;
        
        /**
         * The default value of the flag indicating whether to print statistics at the end of the run or not.
         */
        public static final boolean DEFAULT_PRINT_STATS = true;
        
        /**
         * The default value of the flag indicating whether to print the Channel list at the beginning of the run or not.
         */
        public static final boolean DEFAULT_PRINT_CHANNELS = false;
        
        /**
         * The default value of the flag indicating whether to print the executable version at the beginning of the run or not.
         */
        public static final boolean DEFAULT_PRINT_EXE_VERSION = true;
        
        /**
         * The default value of the flag indicating whether to print the command sent to the executable during a download or not.
         */
        public static final boolean DEFAULT_SHOW_COMMAND = true;
        
        /**
         * The default value of the flag indicating whether to print the output produced by the executable during a download or not.
         */
        public static final boolean DEFAULT_SHOW_WORK = false;
        
        /**
         * The default value of the flag indicating whether to display a progress bar in the terminal during a download or not.
         */
        public static final boolean DEFAULT_SHOW_PROGRESS_BAR = true;
        
        /**
         * The default value of the setting indicating the number of days to retain log files before deleting them.
         */
        public static final long DEFAULT_DAYS_TO_KEEP_LOGS = 30;
        
        
        //Static Fields
        
        /**
         * A flag indicating whether to download only pre-merged formats or not; only used when using yt-dlp.
         */
        public static final boolean preMerged = getSetting(List.of(
                        "preMerged",
                        "format.preMerged"),
                DEFAULT_PRE_MERGED);
        
        /**
         * A flag indicating whether to download the videos as mp3 files or not.
         */
        public static final boolean asMp3 = getSetting(List.of(
                        "asMp3",
                        "format.asMp3"),
                DEFAULT_AS_MP3);
        
        /**
         * A flag indicating whether to move files to the recycling bin instead of deleting them.
         */
        public static final boolean deleteToRecyclingBin = getSetting(List.of(
                        "deleteToRecyclingBin",
                        "flag.deleteToRecyclingBin"),
                DEFAULT_DELETE_TO_RECYCLING_BIN);
        
        /**
         * A flag indicating whether to prohibit the use of browser cookies in an attempt to download restricted videos or not.
         */
        public static final boolean neverUseBrowserCookies = getSetting(List.of(
                        "neverUseBrowserCookies",
                        "flag.neverUseBrowserCookies"),
                DEFAULT_NEVER_USE_BROWSER_COOKIES);
        
        /**
         * A flag indicating whether to retry previously failed videos or not.
         */
        public static final boolean retryPreviousFailures = getSetting(List.of(
                        "retryPreviousFailures",
                        "flag.retryPreviousFailures",
                        "flag.retryFailed"),
                DEFAULT_RETRY_PREVIOUS_FAILURES);
        
        /**
         * A flag indicating whether to run in safe mode or not.
         */
        public static final boolean safeMode = getSetting(List.of(
                        "safeMode",
                        "test.safeMode",
                        "flag.test.safeMode",
                        "flag.safeMode"),
                DEFAULT_SAFE_MODE);
        
        /**
         * A flag indicating whether to disable running the main code or not.
         */
        public static final boolean preventRun = getSetting(List.of(
                        "preventRun",
                        "test.preventRun",
                        "flag.test.preventRun",
                        "flag.preventRun"),
                DEFAULT_PREVENT_RUN);
        
        /**
         * A flag indicating whether to disable processing Channels or not.
         */
        public static final boolean preventProcess = getSetting(List.of(
                        "preventProcess",
                        "test.preventProcess",
                        "flag.test.preventProcess",
                        "flag.preventProcess"),
                DEFAULT_PREVENT_PROCESS);
        
        /**
         * A flag indicating whether to disable downloading content or not.
         */
        public static final boolean preventDownload = safeMode || getSetting(List.of(
                        "preventDownload",
                        "test.preventDownload",
                        "flag.test.preventDownload",
                        "flag.preventDownload"),
                DEFAULT_PREVENT_DOWNLOAD);
        
        /**
         * A flag indicating whether to globally prevent any media deletion or not.
         */
        public static final boolean preventDeletion = safeMode || getSetting(List.of(
                        "preventDeletion",
                        "test.preventDeletion",
                        "flag.test.preventDeletion",
                        "flag.preventDeletion"),
                DEFAULT_PREVENT_DELETION);
        
        /**
         * A flag indicating whether to globally prevent any media renaming or not.
         */
        public static final boolean preventRenaming = safeMode || getSetting(List.of(
                        "preventRenaming",
                        "test.preventRenaming",
                        "flag.test.preventRenaming",
                        "flag.preventRenaming"),
                DEFAULT_PREVENT_RENAMING);
        
        /**
         * A flag indicating whether to disable playlist modification or not.
         */
        public static final boolean preventPlaylistEdit = safeMode || getSetting(List.of(
                        "preventPlaylistEdit",
                        "test.preventPlaylistEdit",
                        "flag.test.preventPlaylistEdit",
                        "flag.preventPlaylistEdit"),
                DEFAULT_PREVENT_PLAYLIST_EDIT);
        
        /**
         * A flag indicating whether to disable fetching the latest data for Channels or not.
         */
        public static final boolean preventChannelFetch = safeMode || getSetting(List.of(
                        "preventChannelFetch",
                        "test.preventChannelFetch",
                        "flag.test.preventChannelFetch",
                        "flag.preventChannelFetch"),
                DEFAULT_PREVENT_CHANNEL_FETCH);
        
        /**
         * A flag indicating whether to disable fetching the info for Videos or not.
         */
        public static final boolean preventVideoFetch = safeMode || getSetting(List.of(
                        "preventVideoFetch",
                        "test.preventVideoFetch",
                        "flag.test.preventVideoFetch",
                        "flag.preventVideoFetch"),
                DEFAULT_PREVENT_VIDEO_FETCH);
        
        /**
         * A flag indicating whether to disable automatic updating of the yt-dlp or youtube-dl executables or not.
         */
        public static final boolean preventExeAutoUpdate = safeMode || getSetting(List.of(
                        "preventExeAutoUpdate",
                        "test.preventExeAutoUpdate",
                        "flag.test.preventExeAutoUpdate",
                        "flag.preventExeAutoUpdate"),
                DEFAULT_PREVENT_EXE_AUTO_UPDATE);
        
        /**
         * A flag indicating whether to disable checking the latest version of the yt-dlp or youtube-dl executables or not.
         */
        public static final boolean preventExeVersionCheck = safeMode || getSetting(List.of(
                        "preventExeVersionCheck",
                        "test.preventExeVersionCheck",
                        "flag.test.preventExeVersionCheck",
                        "flag.preventExeVersionCheck"),
                DEFAULT_PREVENT_EXE_VERSION_CHECK);
        
        /**
         * A flag indicating whether to print statistics at the end of the run or not.
         */
        public static final boolean printStats = getSetting(List.of(
                        "printStats",
                        "log.printStats",
                        "output.printStats"),
                DEFAULT_PRINT_STATS);
        
        /**
         * A flag indicating whether to print the Channel list at the beginning of the run or not.
         */
        public static final boolean printChannels = getSetting(List.of(
                        "log.printChannels",
                        "output.printChannels"),
                DEFAULT_PRINT_CHANNELS);
        
        /**
         * A flag indicating whether to print the executable version at the beginning of the run or not.
         */
        public static final boolean printExeVersion = getSetting(List.of(
                        "printExeVersion",
                        "log.printExeVersion",
                        "output.printExeVersion"),
                DEFAULT_PRINT_EXE_VERSION);
        
        /**
         * A flag indicating whether to print the command sent to the executable during a download or not.
         */
        public static final boolean showCommand = getSetting(List.of(
                        "showCommand",
                        "logCommand",
                        "flag.showCommand",
                        "flag.logCommand",
                        "log.download.showCommand",
                        "log.download.logCommand",
                        "log.showCommand",
                        "log.logCommand"),
                DEFAULT_SHOW_COMMAND);
        
        /**
         * A flag indicating whether to print the output produced by the executable during a download or not.
         */
        public static final boolean showWork = getSetting(List.of(
                        "showWork",
                        "logWork",
                        "flag.showWork",
                        "flag.logWork",
                        "log.download.showWork",
                        "log.download.logWork",
                        "log.showWork",
                        "log.logWork"),
                DEFAULT_SHOW_WORK);
        
        /**
         * A flag indicating whether to display a progress bar in the terminal during a download or not.
         */
        public static final boolean showProgressBar = getSetting(List.of(
                        "showProgressBar",
                        "logProgressBar",
                        "flag.showProgressBar",
                        "flag.logProgressBar",
                        "log.download.showProgressBar",
                        "log.download.logProgressBar",
                        "log.showProgressBar",
                        "log.logProgressBar"),
                DEFAULT_SHOW_PROGRESS_BAR);
        
        /**
         * The number of days to retain log files before deleting them, or -1 to retain logs indefinitely.
         */
        public static final Long daysToKeepLogs = getSetting(List.of(
                        "daysToKeepLogs",
                        "flag.daysToKeepLogs",
                        "log.file.daysToKeepLogs",
                        "log.daysToKeepLogs"),
                DEFAULT_DAYS_TO_KEEP_LOGS);
        
        /**
         * The browser that cookies will be used from when attempting to retry certain failed downloads.
         */
        public static final String browser = getSetting(List.of(
                "browser",
                "location.browser"));
        
        /**
         * The Channel to process, or null if all Channels should be processed.
         */
        public static final String channel = getSetting("filter.channel");
        
        /**
         * The group to process, or null if all groups should be processed.
         */
        public static final String group = getSetting("filter.group");
        
        /**
         * The Channel to start processing from, if processing all Channels.
         */
        public static final String startAt = getSetting("filter.startAt");
        
        /**
         * The Channel to stop processing at, if processing all Channels.
         */
        public static final String stopAt = getSetting("filter.stopAt");
        
    }
    
}
