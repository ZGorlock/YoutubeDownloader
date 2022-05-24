/*
 * File:    Configurator.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import youtube.channel.Channel;
import youtube.channel.Channels;

/**
 * Handles configuration of the Youtube Downloader.
 */
public class Configurator {
    
    //Constants
    
    /**
     * The file containing the configuration for the Youtube Downloader.
     */
    public static final File CONF_FILE = new File("conf.json");
    
    
    //Static Fields
    
    /**
     * The current active project.
     */
    public static String activeProject = null;
    
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
    public static Map<String, Object> getSettings(String section) {
        return new HashMap<>(settings.get(section));
    }
    
    /**
     * Returns a list of configuration settings.
     *
     * @return The list of configuration settings.
     * @see #getSettings(String)
     */
    public static Map<String, Object> getSettings() {
        return getSettings(activeProject);
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
        return getSetting(activeProject, name, def);
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
     * @param activeProject The current active project.
     * @see #loadSettings(JSONObject, String)
     */
    public static void loadSettings(String activeProject) {
        if (loaded.compareAndSet(false, true)) {
            Configurator.activeProject = activeProject;
            
            try {
                String jsonString = FileUtils.readFileToString(CONF_FILE, StandardCharsets.UTF_8);
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(jsonString);
                
                loadSettings(json, activeProject);
                loadSettings(json, "sponsorBlock");
                loadSettings(json, "log");
                
            } catch (IOException | ParseException e) {
                System.err.println("Could not load settings from: " + CONF_FILE.getAbsolutePath());
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
        
        //Static Fields
        
        /**
         * A flag indicating whether to download only pre-merged formats or not; only used when using yt-dlp.
         */
        public static final boolean preMerged = (boolean) Configurator.getSetting("format.preMerged", true);
        
        /**
         * A flag indicating whether to download the videos as mp3 files or not.
         */
        public static final boolean asMp3 = (boolean) Configurator.getSetting("format.asMp3", false) ||
                (boolean) Configurator.getSetting("asMp3", false);
        
        /**
         * A flag indicating whether to retry previously failed videos or not.
         */
        public static final boolean retryFailed = (boolean) Configurator.getSetting("flag.retryFailed", false);
        
        /**
         * A flag indicating whether to globally prevent any media deletion or not.
         */
        public static final boolean preventDeletion = (boolean) Configurator.getSetting("flag.preventDeletion", false);
        
        /**
         * A flag indicating whether to globally prevent any media renaming or not.
         */
        public static final boolean preventRenaming = (boolean) Configurator.getSetting("flag.preventRenaming", false);
        
        /**
         * A flag indicating whether to disable downloading content or not.
         */
        public static final boolean preventDownload = (boolean) Configurator.getSetting("flag.preventDownload", false);
        
        /**
         * A flag indicating whether to disable fetching the latest data for Channels or not.
         */
        public static final boolean preventChannelFetch = (boolean) Configurator.getSetting("flag.preventChannelFetch", false);
        
        /**
         * A flag indicating whether to disable fetching the info for Videos or not.
         */
        public static final boolean preventVideoFetch = (boolean) Configurator.getSetting("flag.preventVideoFetch", false);
        
        /**
         * A flag indicating whether to disable automatic updating of the yt-dlp or youtube-dl executables or not.
         */
        public static final boolean preventExeAutoUpdate = (boolean) Configurator.getSetting("flag.preventExeAutoUpdate", false);
        
        /**
         * A flag indicating whether to the log the download command or not.
         */
        public static final boolean logCommand = (boolean) Configurator.getSetting("log", "logCommand",
                Configurator.getSetting("flag.logCommand", true));
        
        /**
         * A flag indicating whether to log the download work or not.
         */
        public static final boolean logWork = (boolean) Configurator.getSetting("log", "logWork",
                Configurator.getSetting("flag.logWork", false));
        
        /**
         * The Channel to process, or null if all Channels should be processed.
         */
        public static final Channel channel = Optional.ofNullable((String) Configurator.getSetting("filter.channel")).map(Channels::getChannel).orElse(null);
        
        /**
         * The group to process, or null if all groups should be processed.
         */
        public static final String group = (String) Configurator.getSetting("filter.group");
        
        /**
         * The Channel to start processing from, if processing all Channels.
         */
        public static final Channel startAt = Optional.ofNullable((String) Configurator.getSetting("filter.startAt")).map(Channels::getChannel).orElse(null);
        
        /**
         * The Channel to stop processing at, if processing all Channels.
         */
        public static final Channel stopAt = Optional.ofNullable((String) Configurator.getSetting("filter.stopAt")).map(Channels::getChannel).orElse(null);
        
    }
    
}
