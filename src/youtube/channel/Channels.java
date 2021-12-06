/*
 * File:    Channels.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.channel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import youtube.tools.Configurator;

/**
 * Holds Channels and Playlists for the Youtube Channel Downloader.
 */
public class Channels {
    
    //Constants
    
    /**
     * The file containing the Channel configuration for the Youtube Downloader.
     */
    public static final File CHANNELS_FILE = new File("channels.json");
    
    
    //Static Fields
    
    /**
     * The list of Channels.
     */
    private static final Map<String, Channel> channels = new LinkedHashMap<>();
    
    /**
     * A flag indicating whether the Channel configuration has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    /**
     * The drive to use for storage of downloaded files.
     */
    public static final String storageDrive = (String) Configurator.getSetting("location.storageDrive");
    
    /**
     * The Music directory in the storage drive.
     */
    public static final String musicDir = storageDrive + Configurator.getSetting("location.musicDir");
    
    /**
     * The Videos directory in the storage drive.
     */
    public static final String videoDir = storageDrive + Configurator.getSetting("location.videoDir");
    
    
    //Functions
    
    /**
     * Returns the list of Channels.
     *
     * @return The list of Channels.
     */
    public static List<Channel> getChannel() {
        return new ArrayList<>(channels.values());
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
    @SuppressWarnings("unchecked")
    public static void loadChannels() {
        if (loaded.compareAndSet(false, true)) {
            try {
                String jsonString = FileUtils.readFileToString(CHANNELS_FILE, StandardCharsets.UTF_8);
                JSONParser parser = new JSONParser();
                JSONArray channelList = (JSONArray) parser.parse(jsonString);
                
                for (Object channelEntry : channelList) {
                    JSONObject channelJson = (JSONObject) channelEntry;
                    
                    try {
                        Channel channel = new Channel(channelJson);
                        channels.put(channel.key, channel);
                        
                    } catch (Exception e) {
                        System.err.println("Could not load channel: " + channelJson.getOrDefault("key", "null").toString());
                        System.err.println("    " + e.getMessage());
                    }
                }
                
            } catch (IOException | ParseException e) {
                System.err.println("Could not load channels from: " + CHANNELS_FILE.getAbsolutePath());
                System.err.println("    " + e.getMessage());
            }
        }
    }
    
}
