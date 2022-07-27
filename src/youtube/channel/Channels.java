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
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import youtube.util.Color;
import youtube.util.Configurator;
import youtube.util.YoutubeUtils;

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
    public static List<Channel> getChannels() {
        return new ArrayList<>(channels.values());
    }
    
    /**
     * Returns the list of Channel groups.
     *
     * @return The list of Channel groups.
     */
    public static List<String> getGroups() {
        return channels.values().stream().map(channel -> channel.group).distinct().collect(Collectors.toList());
    }
    
    /**
     * Returns the Channel map.
     *
     * @return The Channel map.
     */
    public static Map<String, List<Channel>> getChannelMap() {
        Map<String, List<Channel>> channelMap = new LinkedHashMap<>();
        channels.values().forEach(channel -> {
            channelMap.putIfAbsent(channel.group, new ArrayList<>());
            channelMap.get(channel.group).add(channel);
        });
        return channelMap;
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
    public static void loadChannels() {
        if (loaded.compareAndSet(false, true)) {
            try {
                String jsonString = FileUtils.readFileToString(CHANNELS_FILE, StandardCharsets.UTF_8);
                JSONParser parser = new JSONParser();
                JSONArray channelList = (JSONArray) parser.parse(jsonString);
                
                loadChannelList(channelList);
                
            } catch (IOException | ParseException e) {
                System.out.println(Color.bad("Could not load channels from: ") + Color.file("./" + CHANNELS_FILE.getName()));
                System.out.println(YoutubeUtils.INDENT + Color.bad(e));
                System.exit(0);
            }
        }
    }
    
    /**
     * Loads the Channel configuration from a channel json list.
     *
     * @param channelList The json channel list.
     */
    @SuppressWarnings("unchecked")
    private static void loadChannelList(JSONArray channelList) {
        for (Object channelEntry : channelList) {
            JSONObject channelJson = (JSONObject) channelEntry;
            
            try {
                if (channelJson.containsKey("channels")) {
                    loadChannelList((JSONArray) channelJson.get("channels"));
                    
                } else {
                    Channel channel = new Channel(channelJson);
                    channels.put(channel.key, channel);
                    channel.state.load();
                }
                
            } catch (Exception e) {
                System.out.println(Color.bad("Could not load channel: ") + Color.channel(channelJson.getOrDefault("key", "null")));
                if ((e.getMessage() != null) && !e.getMessage().isEmpty()) {
                    System.out.println(YoutubeUtils.INDENT + Color.bad(e.getMessage()));
                }
            }
        }
    }
    
    /**
     * Prints the Channel map.
     */
    public static void print() {
        if (!Configurator.Config.printChannels) {
            return;
        }
        
        System.out.println(YoutubeUtils.NEWLINE);
        System.out.println(Color.number("--- Channels ---"));
        
        getChannelMap().forEach((group, channels) -> {
            System.out.println(Color.link(YoutubeUtils.formatHeader(group) + ":"));
            channels.forEach(channel -> System.out.println(YoutubeUtils.INDENT +
                    Color.apply((channel.active ? Color.CHANNEL : Color.BAD), YoutubeUtils.formatHeader(channel.key))));
        });
        
        System.out.println(YoutubeUtils.NEWLINE);
    }
    
}
