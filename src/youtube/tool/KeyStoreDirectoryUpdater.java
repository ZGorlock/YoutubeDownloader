/*
 * File:    KeyStoreDirectoryUpdater.java
 * Package: youtube.tool
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.tool;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
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
     * @param args Arguments to the main method.
     * @throws Exception When there is an error.
     */
    public static void main(String[] args) throws Exception {
        final String search = PathUtils.localPath(true, "", oldLocation, name);
        final String replace = PathUtils.localPath(true, "", newLocation, name);
        
        final List<String> originalLines = FileUtils.readLines(KeyStore.KEY_STORE_FILE);
        final List<String> updatedLines = Optional.ofNullable(originalLines)
                .map(lines -> lines.stream()
                        .map(line -> line.replace(search, replace))
                        .collect(Collectors.toList()))
                .filter(newLines -> {
                    try {
                        FileUtils.writeLines(KeyStore.KEY_STORE_FILE, newLines);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .orElse(null);
        
        if ((originalLines != null) && (updatedLines != null)) {
            final long updateCount = IntStream.range(0, updatedLines.size()).filter(i -> !updatedLines.get(i).equals(originalLines.get(i))).count();
            if (updateCount > 0) {
                logger.info(Color.good("Successfully updated ") + Color.number(updateCount) + Color.good(" lines in the key store file: ") + Color.quoteFilePath(KeyStore.KEY_STORE_FILE));
            } else {
                logger.info(Color.base("No updates were made in the key store file: ") + Color.quoteFilePath(KeyStore.KEY_STORE_FILE));
            }
        } else {
            logger.error(Color.bad("Failed to update the key store file: ") + Color.quoteFilePath(KeyStore.KEY_STORE_FILE));
        }
    }
    
}
