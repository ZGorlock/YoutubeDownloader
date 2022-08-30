/*
 * File:    KeyStore.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.channel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.util.FileUtils;
import youtube.util.PathUtils;

/**
 * Manages the key store.
 */
public class KeyStore {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(KeyStore.class);
    
    
    //Constants
    
    /**
     * The store of video keys and their current saved file name.
     */
    public static final File KEY_STORE_FILE = new File(PathUtils.DATA_DIR, "keyStore.txt");
    
    /**
     * The backup file of the keystore.
     */
    public static final File KEY_STORE_BACKUP = new File(KEY_STORE_FILE.getAbsolutePath() + ".bak");
    
    
    //Static Fields
    
    /**
     * A map of video keys and their current saved file name for each Channel.
     */
    private static final Map<String, Map<String, String>> keyStore = new LinkedHashMap<>();
    
    
    //Functions
    
    /**
     * Returns the key store for a Channel.
     *
     * @param channel The Channel.
     * @return The key store for the Channel.
     */
    public static Map<String, String> get(Channel channel) {
        return keyStore.get(channel.name);
    }
    
    /**
     * Loads the map of video keys and their current saved file names for each Channel.
     */
    public static void load() {
        if ((!KEY_STORE_FILE.exists() || (KEY_STORE_FILE.length() == 0)) && KEY_STORE_BACKUP.exists()) {
            try {
                FileUtils.copyFile(KEY_STORE_BACKUP, KEY_STORE_FILE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        List<String> lines;
        try {
            lines = FileUtils.readLines(KEY_STORE_FILE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        keyStore.clear();
        for (Channel channel : Channels.getChannels()) {
            keyStore.putIfAbsent(channel.name, new LinkedHashMap<>());
            channel.state.keyStore = keyStore.get(channel.name);
        }
        
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            String[] lineParts = line.split("\\|+");
            if (lineParts.length == 3) {
                keyStore.putIfAbsent(lineParts[0], new LinkedHashMap<>());
                keyStore.get(lineParts[0]).put(lineParts[1], PathUtils.localPath(lineParts[2]));
            }
        }
    }
    
    /**
     * Saves the map of video keys and their current saved file names for each Channel.
     */
    public static void save() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> keyStoreEntry : keyStore.entrySet()) {
            for (Map.Entry<String, String> keyStoreChannelEntry : keyStoreEntry.getValue().entrySet()) {
                lines.add(keyStoreEntry.getKey() + "|" + keyStoreChannelEntry.getKey() + "|" + PathUtils.localPath(keyStoreChannelEntry.getValue()));
            }
        }
        
        try {
            FileUtils.copyFile(KEY_STORE_FILE, KEY_STORE_BACKUP);
            FileUtils.writeLines(KEY_STORE_FILE, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
