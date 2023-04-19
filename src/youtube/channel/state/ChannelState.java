/*
 * File:    ChannelState.java
 * Package: youtube.channel.state
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.state;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.channel.config.ChannelConfig;
import youtube.config.Color;
import youtube.state.KeyStore;
import youtube.util.FileUtils;
import youtube.util.PathUtils;

/**
 * Manages the state of a Channel.
 */
public class ChannelState {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelState.class);
    
    
    //Constants
    
    /**
     * The default Channel data file type.
     */
    public static final String DEFAULT_DATA_TYPE = "playlistItems";
    
    /**
     * The base file name of the data file in a Channel cache.
     */
    private static final String DATA_FILE_NAME = "data";
    
    /**
     * The base file name of the call log file in a Channel cache.
     */
    private static final String CALL_LOG_FILE_NAME = "callLog";
    
    
    //Fields
    
    /**
     * The name of the Channel that the Channel State is associated with.
     */
    private final String channelName;
    
    /**
     * The internal directory used to cache the Channel State.
     */
    private final File cache;
    
    /**
     * The map of State Lists of the Channel.
     */
    private final Map<StateList.Type, StateList> stateLists;
    
    /**
     * The Key Store of the Channel.
     */
    private final KeyStore.ChannelKeyStore keyStore;
    
    /**
     * A flag indicating whether there was an error processing the Channel this run or not.
     */
    private final AtomicBoolean errorFlag;
    
    
    //Constructors
    
    /**
     * Creates a Channel State for a Channel.
     *
     * @param channelConfig The Channel Config associated with the Channel.
     */
    public ChannelState(ChannelConfig channelConfig) {
        this.channelName = channelConfig.getName();
        
        this.cache = Channels.fetchChannelCache(channelConfig);
        this.stateLists = StateList.initializeStateLists(this);
        this.keyStore = KeyStore.get(this);
        
        this.errorFlag = new AtomicBoolean(false);
        
        load();
    }
    
    
    //Methods
    
    /**
     * Loads the Channel State.
     *
     * @throws RuntimeException When there is an error loading the Channel State.
     */
    private void load() {
        cleanupLegacyCache();
        
        try {
            stateLists.values().forEach(StateList::load);
        } catch (Exception e) {
            logger.error(Color.bad("Failed to load the state of Channel: ") + Color.channelName(this), e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Saves the Channel State.
     *
     * @throws RuntimeException When there is an error saving the Channel State.
     */
    public void save() {
        cleanupStateLists();
        
        try {
            stateLists.values().forEach(StateList::save);
        } catch (Exception e) {
            logger.error(Color.bad("Failed to save the state of Channel: ") + Color.channelName(this), e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the list of files in the Channel State cache.
     *
     * @return The list of cache files.
     */
    public List<File> getCacheFiles() {
        return Optional.of(getCache())
                .map(Filesystem::getFiles)
                .orElseGet(Collections::emptyList);
    }
    
    /**
     * Returns a file in the Channel State cache.
     *
     * @param fileName The name of the cache file.
     * @return The cache file.
     */
    public File getCacheFile(String fileName) {
        return new File(getCache(), (getChannelName() + '-' + fileName));
    }
    
    /**
     * Returns a file in the Channel State cache.
     *
     * @param fileName   The name of the cache file.
     * @param fileFormat The format of the cache file.
     * @return The cache file.
     */
    public File getCacheFile(String fileName, String fileFormat) {
        return getCacheFile(FileUtils.setFormat(fileName, fileFormat));
    }
    
    /**
     * Returns the list of data files in the Channel State cache.
     *
     * @return The list of data files.
     */
    public List<File> getDataFiles() {
        return getCacheFiles().stream()
                .filter(cacheFile -> cacheFile.getName().startsWith(getChannelName() + '-' + DATA_FILE_NAME))
                .collect(Collectors.toList());
    }
    
    /**
     * Returns a data file in the Channel State cache.
     *
     * @param type The type of the data file.
     * @return The data file.
     */
    public File getDataFile(String type) {
        return getCacheFile((DATA_FILE_NAME + getDataFileTypeSuffix(type)), FileUtils.DATA_FILE_FORMAT);
    }
    
    /**
     * Returns the default data file in the Channel State cache.
     *
     * @return The data file.
     */
    public File getDataFile() {
        return getDataFile(null);
    }
    
    /**
     * Returns the call log file in the Channel State cache.
     *
     * @return The call log.
     */
    public File getCallLogFile() {
        return getCacheFile(CALL_LOG_FILE_NAME, FileUtils.LOG_FILE_FORMAT);
    }
    
    /**
     * Returns the suffix for a data file type.
     *
     * @param type The type of the data file.
     * @return The suffix for the data file type.
     */
    private String getDataFileTypeSuffix(String type) {
        return Optional.ofNullable(type)
                .map(dataType -> dataType.replace(DEFAULT_DATA_TYPE, ""))
                .filter(dataType -> !dataType.isBlank())
                .map(dataType -> ('-' + dataType)).orElse("");
    }
    
    /**
     * Returns a State List of the Channel State.
     *
     * @param type The type of the State List.
     * @return The State List.
     */
    public StateList getStateList(StateList.Type type) {
        return getStateLists().get(type);
    }
    
    /**
     * Returns the State List holding the ids of the videos queued for download for the Channel.
     *
     * @return The queue State List.
     */
    public StateList getQueued() {
        return getStateList(StateList.Type.QUEUE);
    }
    
    /**
     * Returns the State List holding the ids of the saved videos of the Channel.
     *
     * @return The save State List.
     */
    public StateList getSaved() {
        return getStateList(StateList.Type.SAVE);
    }
    
    /**
     * Returns the State List holding the ids of the blocked videos of the Channel.
     *
     * @return The block State List.
     */
    public StateList getBlocked() {
        return getStateList(StateList.Type.BLOCK);
    }
    
    /**
     * Cleans up the State Lists of the Channel State.
     */
    public void cleanupStateLists() {
        getStateLists().values().forEach(StateList::clean);
        
        getQueued().removeAll(getBlocked());
        getQueued().removeAll(getSaved());
        getSaved().removeAll(getBlocked());
        getBlocked().removeAll(getSaved());
    }
    
    /**
     * Clears the data and log files in the Channel State cache.
     */
    public void cleanupCache() {
        Stream.concat(getDataFiles().stream(), Stream.of(getCallLogFile()))
                .forEach(Filesystem::deleteFile);
    }
    
    /**
     * Cleans up any legacy cache files.
     */
    private void cleanupLegacyCache() {
        getStateLists().values().stream().map(StateList::getFile)
                .forEach(cacheFile -> Stream.of(cacheFile)
                        .flatMap(file -> Stream.of(
                                new File(new File(Channels.CHANNELS_DATA_DIR.getParentFile(), file.getParentFile().getName()), file.getName()),
                                new File(file.getParentFile(), file.getName().replaceAll("e?d?\\.(.+)$", "ed.$1"))))
                        .filter(File::exists)
                        .forEachOrdered(e -> Optional.of(e)
                                .map(legacyFile -> cacheFile.exists() ?
                                                   Filesystem.deleteFile(legacyFile) :
                                                   Filesystem.moveFile(legacyFile, cacheFile))));
        
        Stream.of(getDataFile(), getCallLogFile())
                .map(cacheFile -> cacheFile.getName().replaceAll("\\..+$", ""))
                .flatMap(legacyName -> getCacheFiles().stream()
                        .filter(cacheFile -> cacheFile.getName().startsWith(legacyName))
                        .filter(cacheFile -> FileUtils.isFormat(cacheFile.getName(), FileUtils.LIST_FILE_FORMAT)))
                .forEach(Filesystem::deleteFile);
    }
    
    /**
     * Returns a string representation of the Channel State.
     *
     * @return A string representation of the Channel State.
     */
    @Override
    public String toString() {
        return getChannelName();
    }
    
    
    //Getters
    
    /**
     * Returns the name of the Channel that the Channel State is associated with.
     *
     * @return The name of the Channel that the Channel State is associated with.
     */
    public String getChannelName() {
        return channelName;
    }
    
    /**
     * Returns the internal directory used to cache the Channel State.
     *
     * @return The internal directory used to cache the Channel State.
     */
    public File getCache() {
        return cache;
    }
    
    /**
     * Returns the map of State Lists of the Channel.
     *
     * @return The map of State Lists of the Channel.
     */
    public Map<StateList.Type, StateList> getStateLists() {
        return stateLists;
    }
    
    /**
     * Returns the Key Store of the Channel.
     *
     * @return The Key Store of the Channel.
     */
    public KeyStore.ChannelKeyStore getKeyStore() {
        return keyStore;
    }
    
    /**
     * Returns a flag indicating whether there was an error processing the Channel this run or not.
     *
     * @return A flag indicating whether there was an error processing the Channel this run or not.
     */
    public AtomicBoolean getErrorFlag() {
        return errorFlag;
    }
    
    
    //Inner Classes
    
    /**
     * Defines a State List for a Channel State.
     */
    public static class StateList extends ArrayList<String> {
        
        //Enums
        
        /**
         * An enumeration of the Types of State Lists.
         */
        public enum Type {
            
            //Values
            
            QUEUE,
            SAVE,
            BLOCK;
            
            
            //Fields
            
            /**
             * The name of the Channel State List Type.
             */
            public final String name;
            
            /**
             * The file name of the Channel State List Type.
             */
            public final String fileName;
            
            
            //Constructors
            
            /**
             * Constructs a Channel State List Type.
             */
            Type() {
                this.name = name().toLowerCase();
                this.fileName = this.name + '.' + FileUtils.LIST_FILE_FORMAT;
            }
            
            
            //Getters
            
            /**
             * Returns the name of the Channel State List Type.
             *
             * @return The name of the Channel State List Type.
             */
            public String getName() {
                return name;
            }
            
            /**
             * Returns the file name of the Channel State List Type.
             *
             * @return The file name of the Channel State List Type.
             */
            public String getFileName() {
                return fileName;
            }
            
        }
        
        
        //Fields
        
        /**
         * The Type of the State List.
         */
        public final Type type;
        
        /**
         * The file containing the data of the State List.
         */
        public final File file;
        
        
        //Constructors
        
        /**
         * Creates a State List for a Channel State.
         *
         * @param channelState The Channel State.
         * @param type         The Type of the State List.
         */
        private StateList(ChannelState channelState, Type type) {
            super();
            
            this.type = type;
            this.file = channelState.getCacheFile(type.getFileName());
        }
        
        
        //Methods
        
        /**
         * Loads the data of the State List.
         *
         * @throws RuntimeException When there is an error loading the State List.
         */
        private void load() {
            this.clear();
            
            Optional.of(getFile())
                    .filter(file -> file.exists() || Filesystem.createFile(file))
                    .map(Filesystem::readLines)
                    .map(this::addAll)
                    .orElseThrow(() -> new RuntimeException(new IOException("Error reading: " + PathUtils.path(getFile()))));
        }
        
        /**
         * Saves the data of the State List.
         *
         * @throws RuntimeException When there is an error saving the State List.
         */
        private void save() {
            this.clean();
            
            Optional.of(getFile())
                    .filter(file -> Filesystem.writeLines(file, this))
                    .orElseThrow(() -> new RuntimeException(new IOException("Error writing: " + PathUtils.path(getFile()))));
        }
        
        /**
         * Cleans the data of the State List.
         */
        private void clean() {
            this.removeIf(StringUtility::isNullOrBlank);
            ListUtility.removeDuplicates(this);
        }
        
        
        //Getters
        
        /**
         * Returns the Type of the State List.
         *
         * @return The Type of the State List.
         */
        public Type getType() {
            return type;
        }
        
        /**
         * Returns the file containing the data of the State List.
         *
         * @return The file containing the data of the State List.
         */
        public File getFile() {
            return file;
        }
        
        
        //Static Methods
        
        /**
         * Initializes the State Lists for a Channel State.
         *
         * @param channelState The Channel State.
         * @return The map of State Lists.
         */
        private static Map<Type, StateList> initializeStateLists(ChannelState channelState) {
            return Arrays.stream(Type.values())
                    .map(type -> new StateList(channelState, type))
                    .collect(MapCollectors.toLinkedHashMap(StateList::getType, Function.identity()));
        }
        
    }
    
}
