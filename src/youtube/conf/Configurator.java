/*
 * File:    Configurator.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.conf;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.util.Color;
import youtube.util.FileUtils;
import youtube.util.PathUtils;
import youtube.util.SponsorBlocker;
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
    public static final File CONF_FILE = new File(PathUtils.WORKING_DIR, "conf.json");
    
    
    //Static Fields
    
    /**
     * The current active project.
     */
    public static Utils.Project activeProject = null;
    
    /**
     * A cache of configuration settings from the configuration file.
     */
    private static final Map<String, Map<String, Object>> settings = new HashMap<>();
    
    /**
     * A flag indicating whether the configuration settings have been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Functions
    
    /**
     * Returns a list of configuration settings.
     *
     * @param section The section of the configuration.
     * @return The list of configuration settings.
     */
    public static Map<String, Object> getSettings(Utils.Project section) {
        return (section == null) ? new HashMap<>() :
               new HashMap<>(settings.get(section));
    }
    
    /**
     * Returns a list of configuration settings.
     *
     * @return The list of configuration settings.
     * @see #getSettings(Utils.Project)
     */
    public static Map<String, Object> getSettings() {
        return (activeProject == null) ? new HashMap<>() :
               getSettings(activeProject);
    }
    
    /**
     * Returns a configuration setting by name.
     *
     * @param section The section of the configuration setting.
     * @param name    The name of the configuration setting.
     * @param def     The default value to return if the configuration setting does not exist.
     * @return The value of the configuration setting, or the default value if it does not exist.
     */
    public static Object getSetting(String section, String name, Object def) {
        return !settings.containsKey(section) ? def :
               settings.get(section).getOrDefault(name, def);
    }
    
    /**
     * Returns a configuration setting by name.
     *
     * @param name The name of the configuration setting.
     * @param def  The default value to return if the configuration setting does not exist.
     * @return The value of the configuration setting, or the default value if it does not exist.
     * @see #getSetting(String, String, Object)
     */
    public static Object getSetting(String name, Object def) {
        return (activeProject == null) ? def :
               getSetting(activeProject.getTitle(), name, def);
    }
    
    /**
     * Returns a configuration setting by name.
     *
     * @param name The name of the configuration setting.
     * @return The value of the configuration setting, or null if it does not exist.
     * @see #getSetting(String, Object)
     */
    public static Object getSetting(String name) {
        return getSetting(name, null);
    }
    
    /**
     * Loads the configuration settings from the configuration file.
     *
     * @param project The current active project.
     * @see #loadSettings(JSONObject, String)
     */
    public static void loadSettings(Utils.Project project) {
        if (loaded.compareAndSet(false, true)) {
            activeProject = project;
            
            try {
                String jsonString = FileUtils.readFileToString(CONF_FILE);
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(jsonString);
                
                loadSettings(json, activeProject.getTitle());
                loadSettings(json, "sponsorBlock");
                loadSettings(json, "color");
                loadSettings(json, "log");
                
            } catch (IOException | ParseException e) {
                System.out.println(Color.bad("Could not load settings from: ") + Color.filePath(CONF_FILE));
                System.out.println(Utils.INDENT + Color.bad(e));
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Loads the configuration settings from a configuration section.
     *
     * @param json    The root configuration json object.
     * @param section The section of the configuration.
     * @see #loadSettings(JSONObject, String, String)
     */
    private static void loadSettings(JSONObject json, String section) {
        JSONObject conf = (JSONObject) json.get(section);
        if (conf != null) {
            if (section.equals("sponsorBlock") && (SponsorBlocker.globalConfig == null)) {
                SponsorBlocker.loadGlobalConfig(conf);
            }
            loadSettings(conf, section, "");
        }
    }
    
    /**
     * Loads the configuration settings from a configuration JSON object.
     *
     * @param conf    The configuration JSON object.
     * @param section The section of the configuration.
     * @param prefix  The name prefix of the settings in the current configuration JSON object.
     */
    @SuppressWarnings("unchecked")
    private static void loadSettings(JSONObject conf, String section, String prefix) {
        for (Object setting : conf.entrySet()) {
            Map.Entry<String, Object> settingEntry = (Map.Entry<String, Object>) setting;
            if ((settingEntry.getValue() != null) && (settingEntry.getValue() instanceof JSONObject)) {
                if (settingEntry.getKey().equals("sponsorBlock")) {
                    SponsorBlocker.loadGlobalConfig(((JSONObject) settingEntry.getValue()));
                }
                loadSettings(((JSONObject) settingEntry.getValue()), section, (prefix + settingEntry.getKey() + '.'));
            } else {
                settings.putIfAbsent(section, new HashMap<>());
                settings.get(section).put((prefix + settingEntry.getKey()), settingEntry.getValue());
            }
        }
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
         * The default value of the flag indicating whether to retry previously failed videos or not.
         */
        public static final boolean DEFAULT_RETRY_PREVIOUS_FAILURES = false;
        
        /**
         * The default value of the flag indicating whether to prohibit the use of browser cookies in an attempt to download restricted videos or not.
         */
        public static final boolean DEFAULT_NEVER_USE_BROWSER_COOKIES = true;
        
        /**
         * The default value of the flag indicating whether to run in safe mode or not.
         */
        public static final boolean DEFAULT_SAFE_MODE = false;
        
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
        
        
        //Static Fields
        
        /**
         * A flag indicating whether to download only pre-merged formats or not; only used when using yt-dlp.
         */
        public static final boolean preMerged = (boolean) Configurator.getSetting("format.preMerged", DEFAULT_PRE_MERGED);
        
        /**
         * A flag indicating whether to download the videos as mp3 files or not.
         */
        public static final boolean asMp3 = (boolean) Configurator.getSetting("format.asMp3", DEFAULT_AS_MP3) ||
                (boolean) Configurator.getSetting("asMp3", DEFAULT_AS_MP3);
        
        /**
         * A flag indicating whether to print statistics at the end of the run or not.
         */
        public static final boolean printStats = (boolean) Configurator.getSetting("log.printStats",
                Configurator.getSetting("output.printStats", DEFAULT_PRINT_STATS));
        
        /**
         * A flag indicating whether to print the Channel list at the beginning of the run or not.
         */
        public static final boolean printChannels = (boolean) Configurator.getSetting("log.printChannels",
                Configurator.getSetting("output.printChannels", DEFAULT_PRINT_CHANNELS));
        
        /**
         * A flag indicating whether to retry previously failed videos or not.
         */
        public static final boolean retryPreviousFailures = (boolean) Configurator.getSetting("flag.retryPreviousFailures",
                Configurator.getSetting("flag.retryFailed", DEFAULT_RETRY_PREVIOUS_FAILURES));
        
        /**
         * A flag indicating whether to prohibit the use of browser cookies in an attempt to download restricted videos or not.
         */
        public static final boolean neverUseBrowserCookies = (boolean) Configurator.getSetting("flag.neverUseBrowserCookies", DEFAULT_NEVER_USE_BROWSER_COOKIES);
        
        /**
         * A flag indicating whether to run in safe mode or not.
         */
        public static final boolean safeMode = (boolean) Configurator.getSetting("flag.safeMode", DEFAULT_SAFE_MODE);
        
        /**
         * A flag indicating whether to disable downloading content or not.
         */
        public static final boolean preventDownload = safeMode || (boolean) Configurator.getSetting("flag.preventDownload", DEFAULT_PREVENT_DOWNLOAD);
        
        /**
         * A flag indicating whether to globally prevent any media deletion or not.
         */
        public static final boolean preventDeletion = safeMode || (boolean) Configurator.getSetting("flag.preventDeletion", DEFAULT_PREVENT_DELETION);
        
        /**
         * A flag indicating whether to globally prevent any media renaming or not.
         */
        public static final boolean preventRenaming = safeMode || (boolean) Configurator.getSetting("flag.preventRenaming", DEFAULT_PREVENT_RENAMING);
        
        /**
         * A flag indicating whether to disable playlist modification or not.
         */
        public static final boolean preventPlaylistEdit = safeMode || (boolean) Configurator.getSetting("flag.preventPlaylistEdit", DEFAULT_PREVENT_PLAYLIST_EDIT);
        
        /**
         * A flag indicating whether to disable fetching the latest data for Channels or not.
         */
        public static final boolean preventChannelFetch = safeMode || (boolean) Configurator.getSetting("flag.preventChannelFetch", DEFAULT_PREVENT_CHANNEL_FETCH);
        
        /**
         * A flag indicating whether to disable fetching the info for Videos or not.
         */
        public static final boolean preventVideoFetch = safeMode || (boolean) Configurator.getSetting("flag.preventVideoFetch", DEFAULT_PREVENT_VIDEO_FETCH);
        
        /**
         * A flag indicating whether to disable automatic updating of the yt-dlp or youtube-dl executables or not.
         */
        public static final boolean preventExeAutoUpdate = safeMode || (boolean) Configurator.getSetting("flag.preventExeAutoUpdate", DEFAULT_PREVENT_EXE_AUTO_UPDATE);
        
        /**
         * A flag indicating whether to disable checking the latest version of the yt-dlp or youtube-dl executables or not.
         */
        public static final boolean preventExeVersionCheck = safeMode || (boolean) Configurator.getSetting("flag.preventExeVersionCheck", DEFAULT_PREVENT_EXE_VERSION_CHECK);
        
        /**
         * A flag indicating whether to print the executable version at the beginning of the run or not.
         */
        public static final boolean printExeVersion = (boolean) Configurator.getSetting("output.printExeVersion",
                Configurator.getSetting("log", "printExeVersion", DEFAULT_PRINT_EXE_VERSION));
        
        /**
         * A flag indicating whether to log the download command or not.
         */
        public static final boolean logCommand = (boolean) Configurator.getSetting("flag.logCommand",
                Configurator.getSetting("log", "logCommand", DEFAULT_LOG_COMMAND));
        
        /**
         * A flag indicating whether to log the download work or not.
         */
        public static final boolean logWork = (boolean) Configurator.getSetting("flag.logWork",
                Configurator.getSetting("log", "logWork", DEFAULT_LOG_WORK));
        
        /**
         * A flag indicating whether to print a progress bar for downloads or not.
         */
        public static final boolean showProgressBar = (boolean) Configurator.getSetting("flag.showProgressBar",
                Configurator.getSetting("log", "showProgressBar", DEFAULT_SHOW_PROGRESS_BAR));
        
        /**
         * The browser that cookies will be used from when attempting to retry certain failed downloads.
         */
        public static final String browser = (String) Configurator.getSetting("location.browser");
        
        /**
         * The Channel to process, or null if all Channels should be processed.
         */
        public static final String channel = (String) Configurator.getSetting("filter.channel");
        
        /**
         * The group to process, or null if all groups should be processed.
         */
        public static final String group = (String) Configurator.getSetting("filter.group");
        
        /**
         * The Channel to start processing from, if processing all Channels.
         */
        public static final String startAt = (String) Configurator.getSetting("filter.startAt");
        
        /**
         * The Channel to stop processing at, if processing all Channels.
         */
        public static final String stopAt = (String) Configurator.getSetting("filter.stopAt");
        
    }
    
}
