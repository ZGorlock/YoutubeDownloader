/*
 * File:    KeyStore.java
 * Package: youtube.state
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.state;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.access.Project;
import commons.lambda.function.checked.CheckedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.config.ChannelConfig;
import youtube.channel.state.ChannelState;
import youtube.config.Color;
import youtube.entity.Channel;
import youtube.entity.Video;
import youtube.util.FileUtils;
import youtube.util.LogUtils;
import youtube.util.PathUtils;

/**
 * Manages the Key Store.
 */
public class KeyStore {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(KeyStore.class);
    
    
    //Constants
    
    /**
     * The file containing the Key Store data.
     */
    public static final File KEY_STORE_FILE = new File(Project.DATA_DIR, FileUtils.setFormat("keyStore", FileUtils.LIST_FILE_FORMAT));
    
    /**
     * The backup file of the Key Store data.
     */
    public static final File KEY_STORE_BACKUP = new File(KEY_STORE_FILE.getParentFile(), (KEY_STORE_FILE.getName().replace(".", "-bak.")));
    
    /**
     * The separator used in a Key Store Entry.
     */
    public static final String SEPARATOR = "|";
    
    /**
     * A flag indicating whether the program should exit if the Key Store file can not be read.
     */
    private static final boolean EXIT_ON_FAIL_TO_READ_KEYSTORE = true;
    
    /**
     * A flag indicating whether the program should exit if the Key Store file can not be written.
     */
    private static final boolean EXIT_ON_FAIL_TO_WRITE_KEYSTORE = true;
    
    /**
     * A flag indicating whether the program should exit if the Key Store file can not be restored from the backup file.
     */
    private static final boolean EXIT_ON_FAIL_TO_RESTORE_FROM_BACKUP = true;
    
    /**
     * A flag indicating whether the program should exit if the Key Store file can not be persisted to the backup file.
     */
    private static final boolean EXIT_ON_FAIL_TO_PERSIST_TO_BACKUP = false;
    
    
    //Static Fields
    
    /**
     * The Key Store which contains a map of video ids and the associated local file path for each Channel.
     */
    private static final ProjectKeyStore keyStore = new ProjectKeyStore();
    
    /**
     * A flag indicating whether the Key Store has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Static Methods
    
    /**
     * Initializes the Key Store.
     *
     * @return Whether the Key Store was successfully initialized.
     */
    public static boolean initKeystore() {
        if (loaded.compareAndSet(false, true)) {
            logger.trace(LogUtils.NEWLINE);
            logger.debug(Color.log("Initializing Key Store..."));
            
            loadKeyStore();
            
            return true;
        }
        return false;
    }
    
    /**
     * Loads the Key Store.
     *
     * @throws RuntimeException When the Key Store could not be loaded.
     */
    private static void loadKeyStore() {
        if (!loaded.get()) {
            logger.warn(Color.bad("The Key Store has not been initialized"));
            return;
        }
        
        logger.debug(Color.log("Loading Key Store..."));
        
        Optional.of(KEY_STORE_FILE)
                .filter(file -> (file.exists() || Filesystem.createFile(file)))
                .filter(file -> (!Filesystem.isEmpty(file) || restoreFromBackup()))
                .filter(KeyStore::readFromFile)
                .orElseThrow(() -> {
                    logger.error(Color.bad("Could not load or create Key Store file: ") + Color.quoteFilePath(KEY_STORE_FILE));
                    return new RuntimeException(new IOException("Error reading: " + PathUtils.path(KEY_STORE_FILE)));
                });
    }
    
    /**
     * Reads the Key Store from a file.
     *
     * @param file The file.
     * @return Whether the Key Store was successfully read from the file.
     */
    private static boolean readFromFile(File file) {
        return Optional.ofNullable(file)
                .filter(File::exists).map(Filesystem::readLines)
                .map(keyStore::parse)
                .map(success -> {
                    if (!success) {
                        logger.error(Color.bad("Failed to read Key Store file: ") + Color.quoteFilePath(file));
                        return !EXIT_ON_FAIL_TO_READ_KEYSTORE;
                    }
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * Attempts to restore the Key Store file from the backup file.
     *
     * @return Whether the operation was successful or not required.
     */
    private static boolean restoreFromBackup() {
        return Optional.of(KEY_STORE_BACKUP)
                .filter(File::exists).filter(backup -> !Filesystem.isEmpty(backup))
                .map(backup -> Filesystem.copyFile(KEY_STORE_BACKUP, KEY_STORE_FILE, true))
                .map(success -> {
                    if (!success) {
                        logger.warn(Color.bad("Failed to restore Key Store file: ") + Color.quoteFilePath(KEY_STORE_FILE) +
                                Color.bad(" from backup file: ") + Color.quoteFilePath(KEY_STORE_BACKUP) +
                                Color.bad("; Please perform this action manually"));
                        return !EXIT_ON_FAIL_TO_RESTORE_FROM_BACKUP;
                    }
                    return true;
                })
                .orElse(true);
    }
    
    /**
     * Saves the Key Store.
     *
     * @throws RuntimeException When the Key Store could not be saved.
     */
    public static void saveKeyStore() {
        if (!loaded.get()) {
            logger.warn(Color.bad("The Key Store has not been initialized"));
            return;
        }
        
        logger.debug(Color.log("Saving Key Store..."));
        
        Optional.of(KEY_STORE_FILE)
                .filter(file -> (file.exists() || Filesystem.createFile(file)))
                .filter(file -> (Filesystem.isEmpty(file) || persistToBackup()))
                .filter(KeyStore::writeToFile)
                .orElseThrow(() -> {
                    logger.error(Color.bad("Could not save or create Key Store file: ") + Color.quoteFilePath(KEY_STORE_FILE));
                    return new RuntimeException(new IOException("Error writing: " + PathUtils.path(KEY_STORE_FILE)));
                });
    }
    
    /**
     * Writes the Key Store to a file.
     *
     * @param file The file.
     * @return Whether the Key Store was successfully written to the file.
     */
    private static boolean writeToFile(File file) {
        return Optional.of(keyStore)
                .map(ProjectKeyStore::format)
                .map(keyStoreLines -> Filesystem.safeRewrite(file, keyStoreLines))
                .map(success -> {
                    if (!success) {
                        logger.warn(Color.bad("Failed to write Key Store file: ") + Color.quoteFilePath(file));
                        return !EXIT_ON_FAIL_TO_WRITE_KEYSTORE;
                    }
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * Attempts to persist the Key Store file to the backup file.
     *
     * @return Whether the operation was successful or not required.
     */
    private static boolean persistToBackup() {
        return Optional.of(KEY_STORE_BACKUP)
                .map(backup -> Filesystem.copyFile(KEY_STORE_FILE, KEY_STORE_BACKUP, true))
                .map(success -> {
                    if (!success) {
                        logger.warn(Color.bad("Failed to backup Key Store file: ") + Color.quoteFilePath(KEY_STORE_FILE) +
                                Color.bad(" to backup file: ") + Color.quoteFilePath(KEY_STORE_BACKUP));
                        return !EXIT_ON_FAIL_TO_PERSIST_TO_BACKUP;
                    }
                    return true;
                })
                .orElse(true);
    }
    
    /**
     * Returns Project Key Store.
     *
     * @return The Project Key Store.
     */
    public static ProjectKeyStore getKeyStore() {
        return keyStore;
    }
    
    /**
     * Returns the Key Store for a Channel.
     *
     * @param channelName The name of the Channel.
     * @return The Channel Key Store.
     */
    public static ChannelKeyStore get(String channelName) {
        return getKeyStore().get(channelName);
    }
    
    /**
     * Returns the Key Store for a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @return The Channel Key Store.
     */
    public static ChannelKeyStore get(ChannelConfig channelConfig) {
        return get(channelConfig.getName());
    }
    
    /**
     * Returns the Key Store for a Channel.
     *
     * @param channel The Channel.
     * @return The Channel Key Store.
     */
    public static ChannelKeyStore get(Channel channel) {
        return get(channel.getConfig());
    }
    
    /**
     * Returns the Key Store for a Channel.
     *
     * @param channelState The Channel State of the Channel.
     * @return The Channel Key Store.
     */
    public static ChannelKeyStore get(ChannelState channelState) {
        return get(channelState.getChannelName());
    }
    
    /**
     * Returns all Channel Key Stores contained in the Key Store.
     *
     * @return The list of Channel Key Stores contained in the Key Store.
     */
    public static List<ChannelKeyStore> getAll() {
        return getKeyStore().getAllChannelEntries();
    }
    
    /**
     * Returns all Key Store Entries contained in the Key Store.
     *
     * @return The list of distinct Key Store Entries contained in the Key Store.
     */
    public static List<KeyStoreEntry> getAllEntries() {
        return getKeyStore().getAllEntries();
    }
    
    /**
     * Returns all Channel names contained in the Key Store.
     *
     * @return The list of Channel names contained in the Key Store.
     */
    public List<String> getAllChannelNames() {
        return getKeyStore().getAllChannelNames();
    }
    
    /**
     * Returns all video ids contained in the Key Store.
     *
     * @return The distinct list of video ids contained in the Key Store.
     */
    public static List<String> getAllVideoIds() {
        return getKeyStore().getAllVideoIds();
    }
    
    /**
     * Returns all local file paths contained in the Key Store.
     *
     * @return The distinct list of local file paths contained in the Key Store.
     */
    public static List<String> getAllFilePaths() {
        return getKeyStore().getAllFilePaths();
    }
    
    /**
     * Returns all local files contained in the Key Store.
     *
     * @return The distinct list of local files contained in the Key Store.
     */
    public static List<File> getAllFiles() {
        return getKeyStore().getAllFiles();
    }
    
    /**
     * Determines if the Key Store contains a video id.
     *
     * @param videoId The id of the Video.
     * @return Whether the Key Store contains the video id.
     */
    public static boolean containsVideoId(String videoId) {
        return getKeyStore().containsVideoId(videoId);
    }
    
    /**
     * Determines if the Key Store contains a file.
     *
     * @param filePath The file path.
     * @return Whether the Key Store contains the file.
     */
    public static boolean containsFilePath(String filePath) {
        return getKeyStore().containsFilePath(filePath);
    }
    
    /**
     * Determines if the Key Store contains a file.
     *
     * @param file The file.
     * @return Whether the Key Store contains the file.
     */
    public static boolean containsFile(File file) {
        return getKeyStore().containsFile(file);
    }
    
    
    //Inner Classes
    
    /**
     * Defines the Key Store for the Project.
     */
    public static class ProjectKeyStore extends KeyStoreMap<ChannelKeyStore> {
        
        //Constructors
        
        /**
         * Creates a Project Key Store.
         */
        private ProjectKeyStore() {
            super();
        }
        
        
        //Methods
        
        /**
         * Returns a Channel Key Store.
         *
         * @param channelName The name of the Channel.
         * @return The Channel Key Store.
         */
        public ChannelKeyStore get(String channelName) {
            return computeIfAbsent(channelName, ChannelKeyStore::new);
        }
        
        /**
         * Returns a Channel Key Store.
         *
         * @param channelConfig The Channel Config of the Channel.
         * @return The Channel Key Store.
         */
        public ChannelKeyStore get(ChannelConfig channelConfig) {
            return get(channelConfig.getName());
        }
        
        /**
         * Returns a Channel Key Store.
         *
         * @param channel The Channel.
         * @return The Channel KeyStore.
         */
        public ChannelKeyStore get(Channel channel) {
            return get(channel.getConfig());
        }
        
        /**
         * Returns a Channel Key Store.
         *
         * @param channelState The Channel State of the Channel.
         * @return The Channel KeyStore.
         */
        public ChannelKeyStore get(ChannelState channelState) {
            return get(channelState.getChannelName());
        }
        
        /**
         * Adds a Key Store Entry to the Project Key Store.
         *
         * @param entry The Key Store Entry.
         * @return Whether the Key Store Entry was successfully added.
         */
        public boolean put(KeyStoreEntry entry) {
            return Optional.ofNullable(entry)
                    .filter(KeyStoreEntry::isValid)
                    .map(KeyStoreEntry::getChannelName).map(this::get)
                    .map(channelKeyStore -> channelKeyStore.put(entry))
                    .orElse(false);
        }
        
        /**
         * Adds a Video to the Project Key Store.
         *
         * @param video The Video.
         * @return Whether the Video was successfully added.
         */
        public boolean put(Video video) {
            return Optional.ofNullable(video)
                    .map(KeyStoreEntry::new)
                    .map(this::put)
                    .orElse(false);
        }
        
        /**
         * Returns all Channel Key Stores contained in the Project Key Store.
         *
         * @return The list of Channel Key Stores contained in the Project KeyStore.
         */
        public List<ChannelKeyStore> getAllChannelEntries() {
            return new ArrayList<>(values());
        }
        
        /**
         * Returns all Key Store Entries contained in the Project Key Store.
         *
         * @return The list of distinct Key Store Entries contained in the Project KeyStore.
         */
        @Override
        public List<KeyStoreEntry> getAllEntries() {
            return getAllChannelEntries().stream()
                    .map(ChannelKeyStore::getAllEntries).flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        }
        
        /**
         * Returns all Channel names contained in the Project Key Store.
         *
         * @return The list of Channel names contained in the Project KeyStore.
         */
        public List<String> getAllChannelNames() {
            return new ArrayList<>(keySet());
        }
        
        /**
         * Returns all video ids contained in the Project Key Store.
         *
         * @return The list of distinct video ids contained in the Project KeyStore.
         */
        @Override
        public List<String> getAllVideoIds() {
            return getAllChannelEntries().stream()
                    .map(ChannelKeyStore::getAllVideoIds).flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        }
        
        /**
         * Parses the lines from a Key Store file.
         *
         * @param lines The lines from the Key Store file.
         * @return Whether the lines from the Key Store file were successfully parsed.
         */
        private boolean parse(List<String> lines) {
            return Optional.ofNullable(lines)
                    .map(keyStoreLines -> keyStoreLines.stream()
                            .map((CheckedFunction<String, KeyStoreEntry>) KeyStoreEntry::parse)
                            .map(this::put)
                            .reduce(true, Boolean::logicalAnd))
                    .isPresent();
        }
        
        /**
         * Formats the Project Key Store.
         *
         * @return The lines of the Project KeyStore data.
         */
        private List<String> format() {
            return values().stream()
                    .map(ChannelKeyStore::format).flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        
    }
    
    /**
     * Defines the KeyStore for a Channel.
     */
    public static class ChannelKeyStore extends KeyStoreMap<KeyStoreEntry> {
        
        //Fields
        
        /**
         * The name of the Channel.
         */
        public String channelName;
        
        
        //Constructors
        
        /**
         * Creates a Channel Key Store.
         *
         * @param channelName The name of the Channel.
         */
        private ChannelKeyStore(String channelName) {
            super();
            
            this.channelName = channelName;
        }
        
        
        //Methods
        
        /**
         * Adds a Key Store Entry to the Channel Key Store.
         *
         * @param entry The Key Store Entry.
         * @return Whether the Key Store Entry was successfully added.
         */
        public boolean put(KeyStoreEntry entry) {
            return Optional.ofNullable(entry)
                    .filter(KeyStoreEntry::isValid)
                    .map(keyStoreEntry -> (super.put(keyStoreEntry.getVideoId(), keyStoreEntry) == null))
                    .orElse(false);
        }
        
        /**
         * Adds a Video to the Channel Key Store.
         *
         * @param video The Video.
         * @return Whether the Video was successfully added.
         */
        public boolean put(Video video) {
            return Optional.ofNullable(video)
                    .map(KeyStoreEntry::new)
                    .map(this::put)
                    .orElse(false);
        }
        
        /**
         * Returns all video ids contained in the Channel Key Store.
         *
         * @return The list of video ids contained in the Channel KeyStore.
         */
        @Override
        public List<String> getAllVideoIds() {
            return new ArrayList<>(keySet());
        }
        
        /**
         * Returns all Key Store Entries contained in the Channel Key Store.
         *
         * @return The list of Key Store Entries contained in the Channel KeyStore.
         */
        @Override
        public List<KeyStoreEntry> getAllEntries() {
            return new ArrayList<>(values());
        }
        
        /**
         * Formats the Channel Key Store.
         *
         * @return The lines of the Channel KeyStore data.
         */
        private List<String> format() {
            return values().stream()
                    .filter(KeyStoreEntry::isValid)
                    .map(KeyStoreEntry::format)
                    .collect(Collectors.toList());
        }
        
        
        //Getters
        
        /**
         * Returns the name of the Channel.
         *
         * @return The name of the Channel.
         */
        public String getChannelName() {
            return channelName;
        }
        
    }
    
    /**
     * Defines a base Key Store Map.
     *
     * @param <T> The type of the Map.
     */
    private static abstract class KeyStoreMap<T> extends LinkedHashMap<String, T> {
        
        //Methods
        
        /**
         * Returns all Key Store Entries contained in the Key Store.
         *
         * @return The list of distinct Key Store Entries contained in the KeyStore.
         */
        public abstract List<KeyStoreEntry> getAllEntries();
        
        /**
         * Returns all video ids contained in the Key Store.
         *
         * @return The list of distinct video ids contained in the KeyStore.
         */
        public abstract List<String> getAllVideoIds();
        
        /**
         * Returns all local file paths contained in the Key Store.
         *
         * @return The distinct list of local file paths contained in the KeyStore.
         */
        public List<String> getAllFilePaths() {
            return getAllEntries().stream()
                    .map(KeyStoreEntry::getLocalPath)
                    .distinct()
                    .collect(Collectors.toList());
        }
        
        /**
         * Returns all local files contained in the Key Store.
         *
         * @return The distinct list of local files contained in the KeyStore.
         */
        public List<File> getAllFiles() {
            return getAllEntries().stream()
                    .map(KeyStoreEntry::getLocalFile)
                    .distinct()
                    .collect(Collectors.toList());
        }
        
        /**
         * Determines if the Key Store contains a Video id.
         *
         * @param videoId The id of the Video.
         * @return Whether the KeyStore contains the Video id.
         */
        public boolean containsVideoId(String videoId) {
            return Optional.ofNullable(videoId)
                    .map(search -> getAllVideoIds().contains(search))
                    .orElse(false);
        }
        
        /**
         * Determines if the Key Store contains a file path.
         *
         * @param filePath The file path.
         * @return Whether the KeyStore contains the file path.
         */
        public boolean containsFilePath(String filePath) {
            return Optional.ofNullable(filePath)
                    .map(PathUtils::localPath)
                    .map(search -> getAllFilePaths().contains(search))
                    .orElse(false);
        }
        
        /**
         * Determines if the Key Store contains a file.
         *
         * @param file The file.
         * @return Whether the KeyStore contains the file.
         */
        public boolean containsFile(File file) {
            return Optional.ofNullable(file)
                    .map(PathUtils::localPath)
                    .map(this::containsFilePath)
                    .orElse(false);
        }
        
    }
    
    /**
     * Defines an Entry in a KeyStore.
     */
    public static class KeyStoreEntry {
        
        //Enums
        
        /**
         * An enumeration of the Parts of a KeyStore Entry.
         */
        private enum Part {
            CHANNEL_NAME,
            VIDEO_ID,
            LOCAL_PATH
        }
        
        
        //Fields
        
        /**
         * The name of the Channel.
         */
        public String channelName;
        
        /**
         * The id of the Video.
         */
        public String videoId;
        
        /**
         * The local file path.
         */
        public String localPath;
        
        /**
         * The local file.
         */
        public File localFile;
        
        
        //Constructors
        
        /**
         * Creates a Key Store Entry.
         *
         * @param channelName The name of the Channel.
         * @param videoId     The id of the Video.
         * @param path        The local file path.
         */
        private KeyStoreEntry(String channelName, String videoId, String path) {
            this.channelName = channelName;
            this.videoId = videoId;
            this.localPath = Optional.ofNullable(path).map(PathUtils::localPath).orElse(null);
            this.localFile = Optional.ofNullable(localPath).map(File::new).map(FileUtils::getCanonical).orElse(null);
        }
        
        /**
         * Creates a Key Store Entry.
         *
         * @param channelName The name of the Channel.
         * @param videoId     The id of the Video.
         * @param file        The local file.
         */
        private KeyStoreEntry(String channelName, String videoId, File file) {
            this(channelName, videoId, PathUtils.localPath(file));
        }
        
        /**
         * Creates a Key Store Entry.
         *
         * @param video The Video.
         */
        private KeyStoreEntry(Video video) {
            this(video.getConfig().getName(), video.getInfo().getVideoId(), video.getOutput());
        }
        
        
        //Methods
        
        /**
         * Determines whether the Key Store Entry is valid.
         *
         * @return Whether the KeyStore Entry is valid.
         */
        public boolean isValid() {
            return (getChannelName() != null) && (getVideoId() != null) &&
                    (getLocalPath() != null) && (getLocalFile() != null);
        }
        
        /**
         * Formats the Key Store Entry.
         *
         * @return A line of the Key Store file.
         */
        private String format() {
            return String.join(SEPARATOR,
                    getChannelName(), getVideoId(), getLocalPath());
        }
        
        
        //Getters
        
        /**
         * Returns the name of the Channel.
         *
         * @return The name of the Channel.
         */
        public String getChannelName() {
            return channelName;
        }
        
        /**
         * Returns the id of theVideo.
         *
         * @return The id of the Video.
         */
        public String getVideoId() {
            return videoId;
        }
        
        /**
         * Returns the local file path.
         *
         * @return The local file path.
         */
        public String getLocalPath() {
            return localPath;
        }
        
        /**
         * Returns the local file.
         *
         * @return The local file.
         */
        public File getLocalFile() {
            return localFile;
        }
        
        
        //Static Methods
        
        /**
         * Parses a line from a Key Store file.
         *
         * @param keyStoreLine The line from the Key Store file.
         * @return The Key Store Entry.
         * @throws ParseException When the Key Store line is not valid.
         */
        private static KeyStoreEntry parse(String keyStoreLine) throws ParseException {
            return Optional.ofNullable(keyStoreLine)
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.split(Pattern.quote(SEPARATOR) + "+"))
                    .filter(lineParts -> (lineParts.length == Part.values().length))
                    .map(lineParts -> new KeyStoreEntry(lineParts[0], lineParts[1], lineParts[2]))
                    .orElseThrow(() -> {
                        logger.warn(Color.bad("Unable to parse Key Store line: ") + Color.quoted(Color.base(keyStoreLine)));
                        return new ParseException(keyStoreLine, 0);
                    });
        }
        
    }
    
}
