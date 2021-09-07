/*
 * File:    Configuration.java
 * Package: youtube.tools
 * Author:  Zachary Gill
 */

package youtube.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    private static final Map<String, Object> settings = new HashMap<>();
    
    /**
     * A flag indicating whether the configuration settings have been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Functions
    
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
     */
    @SuppressWarnings("unchecked")
    public static void loadSettings(String activeProject) {
        if (loaded.compareAndSet(false, true)) {
            Configurator.activeProject = activeProject;
            
            try {
                String jsonString = FileUtils.readFileToString(CONF_FILE, StandardCharsets.UTF_8);
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(jsonString);
                JSONObject conf = (JSONObject) json.get(activeProject);
                for (Object setting : conf.entrySet()) {
                    Map.Entry<String, Object> settingEntry = (Map.Entry<String, Object>) setting;
                    settings.put(settingEntry.getKey(), settingEntry.getValue());
                }
                
            } catch (IOException | ParseException e) {
                System.err.println("Could not load settings from: " + CONF_FILE.getAbsolutePath());
            }
        }
    }
    
}
