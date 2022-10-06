/*
 * File:    KeyStoreDirectoryUpdater.java
 * Package: youtube.tool
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.tool;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.state.KeyStore;
import youtube.util.FileUtils;
import youtube.util.PathUtils;

/**
 * Updates the key store after moving a Channel to a new directory.
 */
public class KeyStoreDirectoryUpdater {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(KeyStoreDirectoryUpdater.class);
    
    
    //Static Fields
    
    /**
     * The name of the Channel directory.
     */
    private static final String name = "Minute Physics";
    
    /**
     * The string to search for which uniquely identifies the old location of the Channel.
     */
    private static final String oldLocation = "General";
    
    /**
     * The string to replace with which uniquely identifies the new location of the Channel and corresponds with <b>oldDirectory</b>.
     */
    private static final String newLocation = "Physics";
    
    
    //Main Method
    
    /**
     * Runs the Key Store Directory Updater.
     *
     * @param args Arguments to the main method
     * @throws Exception When there is an error.
     */
    public static void main(String[] args) throws Exception {
        final String search = PathUtils.localPath(true, "", oldLocation, name);
        final String replace = PathUtils.localPath(true, "", newLocation, name);
        
        final List<String> keyStoreLines = FileUtils.readLines(KeyStore.KEY_STORE_FILE);
        
        FileUtils.writeLines(KeyStore.KEY_STORE_FILE, keyStoreLines.stream()
                .map(e -> e.replace(search, replace))
                .collect(Collectors.toList()));
    }
    
}
