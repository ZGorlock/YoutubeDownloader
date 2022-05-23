/*
 * File:    Configurator.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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
    private static final Map<String, Object> settings = new LinkedHashMap<>();
    
    /**
     * A flag indicating whether the configuration settings have been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Functions
    
    /**
     * Returns a list of configuration settings.
     *
     * @return The list of configuration settings.
     */
    public static Map<String, Object> getSettings() {
        return new LinkedHashMap<>(settings);
    }
    
    /**
     * Returns a configuration setting by name.
     *
     * @param name The name of the configuration setting.
     * @param def  The default value to return if the configuration setting does not exist.
     * @return The value of the configuration setting, or the default value if it does not exist.
     */
    public static Object getSetting(String name, Object def) {
        return settings.getOrDefault(name, def);
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
                JSONObject conf = (JSONObject) json.get(activeProject);
                
                loadSettings(conf, "");
                
            } catch (IOException | ParseException e) {
                System.err.println("Could not load settings from: " + CONF_FILE.getAbsolutePath());
            }
        }
    }
    
    /**
     * Loads the configuration settings from a configuration JSON object.
     *
     * @param conf   The configuration JSON object.
     * @param prefix The name prefix of the settings in the current configuration JSON object.
     */
    @SuppressWarnings("unchecked")
    private static void loadSettings(JSONObject conf, String prefix) {
        for (Object setting : conf.entrySet()) {
            Map.Entry<String, Object> settingEntry = (Map.Entry<String, Object>) setting;
            if ((settingEntry.getValue() != null) && (settingEntry.getValue() instanceof JSONObject)) {
                if (settingEntry.getKey().equals("sponsorBlock")) {
                    SponsorBlocker.loadGlobalConfig(((JSONObject) settingEntry.getValue()));
                } else {
                    loadSettings(((JSONObject) settingEntry.getValue()), (prefix + settingEntry.getKey() + '.'));
                }
            } else {
                settings.put((prefix + settingEntry.getKey()), settingEntry.getValue());
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
         * A flag indicating whether to globally prevent any media deletion or not.
         */
        public static final boolean preventDeletion = (boolean) Configurator.getSetting("flag.preventDeletion", false);
        
        /**
         * A flag indicating whether to retry previously failed videos or not.
         */
        public static final boolean retryFailed = (boolean) Configurator.getSetting("flag.retryFailed", false);
        
        /**
         * A flag indicating whether to the log the download command or not.
         */
        public static final boolean logCommand = (boolean) Configurator.getSetting("flag.logCommand", true);
        
        /**
         * A flag indicating whether to log the download work or not.
         */
        public static final boolean logWork = (boolean) Configurator.getSetting("flag.logWork", false);
        
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
