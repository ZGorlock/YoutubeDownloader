/*
 * File:    KeyStore.java
 * Package: youtube.state
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.state;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.lambda.stream.collector.MapCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.util.FileUtils;
import youtube.util.PathUtils;
import youtube.util.Utils;

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
    public static final File KEY_STORE_FILE = new File(PathUtils.DATA_DIR, ("keyStore" + '.' + Utils.LIST_FILE_FORMAT));
    
    /**
     * The backup file of the keystore.
     */
    public static final File KEY_STORE_BACKUP = new File(KEY_STORE_FILE.getParentFile(), (KEY_STORE_FILE.getName() + '.' + Utils.BACKUP_FILE_FORMAT));
    
    /**
     * The separator used in the keystore file.
     */
    public static final String KEYSTORE_SEPARATOR = "|";
    
    
    //Static Fields
    
    /**
     * A map of video keys and their current saved file name for each Channel.
     */
    private static final Map<String, Map<String, String>> keyStore = new LinkedHashMap<>();
    
    
    //Static Methods
    
    /**
     * Loads the map of video keys and their current saved file names.
     */
    public static void load() {
        try {
            if ((!KEY_STORE_FILE.exists() || (KEY_STORE_FILE.length() == 0)) && KEY_STORE_BACKUP.exists()) {
                FileUtils.copyFile(KEY_STORE_BACKUP, KEY_STORE_FILE);
            }
            FileUtils.readLines(KEY_STORE_FILE).stream()
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.split(Pattern.quote(KEYSTORE_SEPARATOR) + "+"))
                    .filter(lineParts -> (lineParts.length == 3))
                    .forEachOrdered(lineParts -> {
                        keyStore.putIfAbsent(lineParts[0], new LinkedHashMap<>());
                        keyStore.get(lineParts[0]).put(lineParts[1], PathUtils.localPath(lineParts[2]));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Saves the map of video keys and their current saved file names.
     */
    public static void save() {
        try {
            FileUtils.copyFile(KEY_STORE_FILE, KEY_STORE_BACKUP);
            FileUtils.writeLines(KEY_STORE_FILE, keyStore.entrySet().stream()
                    .flatMap(store -> store.getValue().entrySet().stream()
                            .map(entry -> String.join(KEYSTORE_SEPARATOR,
                                    store.getKey(), entry.getKey(), PathUtils.localPath(entry.getValue())))
                    ).collect(Collectors.toList()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the key store for a Channel.
     *
     * @param channelName The name of the Channel.
     * @return The key store for the Channel.
     */
    public static Map<String, String> get(String channelName) {
        return keyStore.computeIfAbsent(channelName, k -> new LinkedHashMap<>());
    }
    
    /**
     * Returns the entire key store.
     *
     * @return The key store.
     */
    public static Map<String, Map<String, String>> getAll() {
        return keyStore.entrySet().stream()
                .map(store -> Map.entry(store.getKey(), Map.copyOf(store.getValue())))
                .collect(MapCollectors.toLinkedHashMap());
    }
    
    /**
     * Returns the entire compacted key store.
     *
     * @return The compacted key store.
     */
    public static Map<String, String> getCompact() {
        return keyStore.entrySet().stream()
                .flatMap(store -> store.getValue().entrySet().stream()
                        .map(entry -> Map.entry(
                                (store.getKey() + KEYSTORE_SEPARATOR + entry.getKey()),
                                entry.getValue())))
                .collect(MapCollectors.toLinkedHashMap());
    }
    
    /**
     * Returns all video keys contained in the key store.
     *
     * @return The distinct list of video keys contained in the key store.
     */
    public static List<String> getAllKeys() {
        return keyStore.values().stream()
                .flatMap(store -> store.keySet().stream())
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Returns all files contained in the key store.
     *
     * @return The distinct list of files contained in the key store.
     */
    public static List<File> getAllFiles() {
        return keyStore.values().stream()
                .flatMap(store -> store.values().stream())
                .distinct().map(File::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Determines if the key store contains a file.
     *
     * @param file The file.
     * @return Whether the key store contains the file.
     */
    public static boolean containsFile(File file) {
        return Optional.ofNullable(file)
                .map(PathUtils::localPath)
                .map(path -> keyStore.values().stream()
                        .flatMap(store -> store.values().stream())
                        .anyMatch(value -> value.equals(path)))
                .orElse(false);
    }
    
}
