/*
 * File:    Configurator.java
 * Package: youtube.config
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
        
        YOUTUBE_CHANNEL_DOWNLOADER,
        YOUTUBE_DOWNLOADER;
        
        
        //Methods
        
        /**
         * Returns the title of the Program.
         *
         * @return The title of the Program.
         */
        public String getTitle() {
            return Utils.formatUnderscoredString(name()).replace(" ", "");
        }
        
    }
    
    
    //Static Fields
    
    /**
     * The active program.
     */
    public static Program activeProgram = null;
    
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
     * Fetches the configuration settings of a config section.
     *
     * @param section The config section.
     * @return The configuration settings.
     */
    public static Map<String, Object> getSettings(String section) {
        return Optional.ofNullable(section)
                .map(settings::get).map(MapUtility::clone)
                .orElseGet(MapUtility::emptyMap);
    }
    
    /**
     * Fetches the configuration settings of the active program.
     *
     * @return The configuration settings.
     */
    public static Map<String, Object> getSettings() {
        return Optional.ofNullable(activeProgram)
                .map(Program::getTitle)
                .map(Configurator::getSettings)
                .orElseGet(MapUtility::emptyMap);
    }
    
    /**
     * Fetches a configuration setting of a config section.
     *
     * @param section The config section.
     * @param name    The name of the configuration setting.
     * @param def     The default value to return if the configuration setting does not exist.
     * @param <T>     The type of the setting.
     * @return The value of the configuration setting, or the default value if it does not exist.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSetting(String section, String name, T def) {
        return (T) Optional.of(section)
                .map(Configurator::getSettings)
                .map(e -> e.getOrDefault(name, def))
                .orElse(null);
    }
    
    /**
     * Fetches a configuration setting of the active program.
     *
     * @param name The name of the configuration setting.
     * @param def  The default value to return if the configuration setting does not exist.
     * @param <T>  The type of the setting.
     * @return The value of the configuration setting, or the default value if it does not exist.
     */
    public static <T> T getSetting(String name, T def) {
        return Optional.ofNullable(activeProgram)
                .map(Program::getTitle)
                .map(e -> getSetting(e, name, def))
                .orElse(null);
    }
    
    /**
     * Fetches a configuration setting of the active program.
     *
     * @param name The name of the configuration setting.
     * @param <T>  The type of the setting.
     * @return The value of the configuration setting, or null if it does not exist.
     */
    public static <T> T getSetting(String name) {
        return getSetting(name, null);
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
                
                loadSettingSection(settingsData, activeProgram.getTitle());
                loadSettingSection(settingsData, "sponsorBlock");
                loadSettingSection(settingsData, "color");
                loadSettingSection(settingsData, "log");
                
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
            settings.putIfAbsent(section, new HashMap<>());
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
         * The default value of the flag indicating whether to print statistics at the end of the run or not.
         */
        public static final boolean DEFAULT_PRINT_STATS = true;
        
        /**
         * The default value of the flag indicating whether to print the Channel list at the beginning of the run or not.
         */
        public static final boolean DEFAULT_PRINT_CHANNELS = false;
        
        /**
         * The default value of the flag indicating whether to run in safe mode or not.
         */
        public static final boolean DEFAULT_SAFE_MODE = false;
        
        /**
         * The default value of the flag indicating whether to move files to the recycling bin instead of deleting them.
         */
        public static final boolean DEFAULT_DELETE_TO_RECYCLING_BIN = false;
        
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
         * The default value of the flag indicating whether to prohibit the use of browser cookies in an attempt to download restricted videos or not.
         */
        public static final boolean DEFAULT_NEVER_USE_BROWSER_COOKIES = true;
        
        /**
         * The default value of the flag indicating whether to retry previously failed videos or not.
         */
        public static final boolean DEFAULT_RETRY_PREVIOUS_FAILURES = false;
        
        /**
         * The default value of the flag indicating whether to print the executable version at the beginning of the run or not.
         */
        public static final boolean DEFAULT_PRINT_EXE_VERSION = true;
        
        /**
         * The default value of the flag indicating whether to log the download command or not.
         */
        public static final boolean DEFAULT_LOG_COMMAND = true;
        
        /**
         * The default value of the flag indicating whether to log the download work or not.
         */
        public static final boolean DEFAULT_LOG_WORK = false;
        
        /**
         * The default value of the flag indicating whether to print a progress bar for downloads or not.
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
        public static final boolean preMerged = Configurator.getSetting("format.preMerged", DEFAULT_PRE_MERGED);
        
        /**
         * A flag indicating whether to download the videos as mp3 files or not.
         */
        public static final boolean asMp3 = Configurator.getSetting("format.asMp3", DEFAULT_AS_MP3) ||
                Configurator.getSetting("asMp3", DEFAULT_AS_MP3);
        
        /**
         * A flag indicating whether to print statistics at the end of the run or not.
         */
        public static final boolean printStats = Configurator.getSetting("log.printStats",
                Configurator.getSetting("output.printStats", DEFAULT_PRINT_STATS));
        
        /**
         * A flag indicating whether to print the Channel list at the beginning of the run or not.
         */
        public static final boolean printChannels = Configurator.getSetting("log.printChannels",
                Configurator.getSetting("output.printChannels", DEFAULT_PRINT_CHANNELS));
        
        /**
         * A flag indicating whether to run in safe mode or not.
         */
        public static final boolean safeMode = Configurator.getSetting("flag.safeMode", DEFAULT_SAFE_MODE);
        
        /**
         * A flag indicating whether to disable downloading content or not.
         */
        public static final boolean preventDownload = safeMode || Configurator.getSetting("flag.preventDownload", DEFAULT_PREVENT_DOWNLOAD);
        
        /**
         * A flag indicating whether to globally prevent any media deletion or not.
         */
        public static final boolean preventDeletion = safeMode || Configurator.getSetting("flag.preventDeletion", DEFAULT_PREVENT_DELETION);
        
        /**
         * A flag indicating whether to globally prevent any media renaming or not.
         */
        public static final boolean preventRenaming = safeMode || Configurator.getSetting("flag.preventRenaming", DEFAULT_PREVENT_RENAMING);
        
        /**
         * A flag indicating whether to disable playlist modification or not.
         */
        public static final boolean preventPlaylistEdit = safeMode || Configurator.getSetting("flag.preventPlaylistEdit", DEFAULT_PREVENT_PLAYLIST_EDIT);
        
        /**
         * A flag indicating whether to disable fetching the latest data for Channels or not.
         */
        public static final boolean preventChannelFetch = safeMode || Configurator.getSetting("flag.preventChannelFetch", DEFAULT_PREVENT_CHANNEL_FETCH);
        
        /**
         * A flag indicating whether to disable fetching the info for Videos or not.
         */
        public static final boolean preventVideoFetch = safeMode || Configurator.getSetting("flag.preventVideoFetch", DEFAULT_PREVENT_VIDEO_FETCH);
        
        /**
         * A flag indicating whether to disable automatic updating of the yt-dlp or youtube-dl executables or not.
         */
        public static final boolean preventExeAutoUpdate = safeMode || Configurator.getSetting("flag.preventExeAutoUpdate", DEFAULT_PREVENT_EXE_AUTO_UPDATE);
        
        /**
         * A flag indicating whether to disable checking the latest version of the yt-dlp or youtube-dl executables or not.
         */
        public static final boolean preventExeVersionCheck = safeMode || Configurator.getSetting("flag.preventExeVersionCheck", DEFAULT_PREVENT_EXE_VERSION_CHECK);
        
        /**
         * A flag indicating whether to move files to the recycling bin instead of deleting them.
         */
        public static final boolean deleteToRecyclingBin = Configurator.getSetting("flag.deleteToRecyclingBin", DEFAULT_DELETE_TO_RECYCLING_BIN);
        
        /**
         * A flag indicating whether to prohibit the use of browser cookies in an attempt to download restricted videos or not.
         */
        public static final boolean neverUseBrowserCookies = Configurator.getSetting("flag.neverUseBrowserCookies", DEFAULT_NEVER_USE_BROWSER_COOKIES);
        
        /**
         * A flag indicating whether to retry previously failed videos or not.
         */
        public static final boolean retryPreviousFailures = Configurator.getSetting("flag.retryPreviousFailures",
                Configurator.getSetting("flag.retryFailed", DEFAULT_RETRY_PREVIOUS_FAILURES));
        
        /**
         * A flag indicating whether to print the executable version at the beginning of the run or not.
         */
        public static final boolean printExeVersion = Configurator.getSetting("output.printExeVersion",
                Configurator.getSetting("log", "printExeVersion", DEFAULT_PRINT_EXE_VERSION));
        
        /**
         * A flag indicating whether to log the download command or not.
         */
        public static final boolean logCommand = Configurator.getSetting("flag.logCommand",
                Configurator.getSetting("log", "logCommand", DEFAULT_LOG_COMMAND));
        
        /**
         * A flag indicating whether to log the download work or not.
         */
        public static final boolean logWork = Configurator.getSetting("flag.logWork",
                Configurator.getSetting("log", "logWork", DEFAULT_LOG_WORK));
        
        /**
         * A flag indicating whether to print a progress bar for downloads or not.
         */
        public static final boolean showProgressBar = Configurator.getSetting("flag.showProgressBar",
                Configurator.getSetting("log", "showProgressBar", DEFAULT_SHOW_PROGRESS_BAR));
        
        /**
         * The number of days to retain log files before deleting them, or -1 to retain logs indefinitely.
         */
        public static final Long daysToKeepLogs = Configurator.getSetting("log", "daysToKeepLogs", DEFAULT_DAYS_TO_KEEP_LOGS);
        
        /**
         * The browser that cookies will be used from when attempting to retry certain failed downloads.
         */
        public static final String browser = Configurator.getSetting("location.browser");
        
        /**
         * The Channel to process, or null if all Channels should be processed.
         */
        public static final String channel = Configurator.getSetting("filter.channel");
        
        /**
         * The group to process, or null if all groups should be processed.
         */
        public static final String group = Configurator.getSetting("filter.group");
        
        /**
         * The Channel to start processing from, if processing all Channels.
         */
        public static final String startAt = Configurator.getSetting("filter.startAt");
        
        /**
         * The Channel to stop processing at, if processing all Channels.
         */
        public static final String stopAt = Configurator.getSetting("filter.stopAt");
        
    }
    
}
