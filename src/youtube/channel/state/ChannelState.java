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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.lambda.function.checked.CheckedConsumer;
import commons.lambda.function.unchecked.UncheckedFunction;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.config.ChannelConfig;
import youtube.config.Color;
import youtube.config.Configurator;
import youtube.state.KeyStore;
import youtube.util.FileUtils;
import youtube.util.PathUtils;
import youtube.util.Utils;

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
     * The Channel data directory.
     */
    public static final File CHANNEL_DATA_DIR = new File(PathUtils.DATA_DIR, "channel");
    
    /**
     * The default Channel data file type.
     */
    public static final String DEFAULT_DATA_FILE_TYPE = "playlistItems";
    
    
    //Fields
    
    /**
     * The name of the Channel.
     */
    private final String channelName;
    
    /**
     * The ids of the videos queued for download for the Channel.
     */
    private final List<String> queued;
    
    /**
     * The ids of the saved videos of the Channel.
     */
    private final List<String> saved;
    
    /**
     * The ids of the blocked videos of the Channel.
     */
    private final List<String> blocked;
    
    /**
     * The key store of the Channel.
     */
    private final Map<String, String> keyStore;
    
    /**
     * The internal directory to store the Channel State.
     */
    private final File stateLocation;
    
    /**
     * The internal data file for the Channel.
     */
    private final File dataFile;
    
    /**
     * The internal call log file for the Channel.
     */
    private final File callLogFile;
    
    /**
     * The internal file holding the saved videos for the Channel.
     */
    private final File saveFile;
    
    /**
     * The internal file holding the videos queued for download for the Channel.
     */
    private final File queueFile;
    
    /**
     * The internal file holding the blocked videos for the Channel.
     */
    private final File blockFile;
    
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
        
        this.queued = new ArrayList<>();
        this.saved = new ArrayList<>();
        this.blocked = new ArrayList<>();
        
        this.keyStore = KeyStore.get(channelName);
        
        this.stateLocation = new File(CHANNEL_DATA_DIR, channelName);
        this.dataFile = new File(this.stateLocation, (channelName + "-data" + '.' + Utils.DATA_FILE_FORMAT));
        this.callLogFile = new File(this.stateLocation, (channelName + "-callLog" + '.' + Utils.LOG_FILE_FORMAT));
        this.saveFile = new File(this.stateLocation, (channelName + "-save" + '.' + Utils.LIST_FILE_FORMAT));
        this.queueFile = new File(this.stateLocation, (channelName + "-queue" + '.' + Utils.LIST_FILE_FORMAT));
        this.blockFile = new File(this.stateLocation, (channelName + "-blocked" + '.' + Utils.LIST_FILE_FORMAT));
        
        this.errorFlag = new AtomicBoolean(false);
        
        load();
    }
    
    
    //Methods
    
    /**
     * Loads the queued, saved, and blocked lists.
     *
     * @throws RuntimeException When there is an error loading the state.
     */
    private void load() {
        Stream.of(getQueued(), getSaved(), getBlocked()).forEach(List::clear);
        cleanupLegacyState();
        
        try {
            getQueued().addAll(FileUtils.readLines(getQueueFile()));
            getSaved().addAll(FileUtils.readLines(getSaveFile()));
            getBlocked().addAll(FileUtils.readLines(getBlockFile()));
            
        } catch (IOException e) {
            System.out.println(Color.bad("Failed to load the state of Channel: ") + Color.channel(getChannelName()));
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Saves the queued, saved, and blocked lists.
     *
     * @throws RuntimeException When there is an error saving the state.
     */
    public void save() {
        Stream.of(getQueued(), getSaved(), getBlocked()).forEach(list -> {
            list.removeIf(StringUtility::isNullOrBlank);
            ListUtility.removeDuplicates(list);
        });
        
        getQueued().removeAll(getBlocked());
        getQueued().removeAll(getSaved());
        getSaved().removeAll(getBlocked());
        getBlocked().removeAll(getSaved());
        
        try {
            FileUtils.writeLines(getQueueFile(), getQueued());
            FileUtils.writeLines(getSaveFile(), getSaved());
            FileUtils.writeLines(getBlockFile(), getBlocked());
            
        } catch (IOException e) {
            System.out.println(Color.bad("Failed to save the state of Channel: ") + Color.channel(getChannelName()));
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the list of data files.
     *
     * @return The list of data files.
     */
    public List<File> getDataFiles() {
        return Optional.ofNullable(getStateLocation())
                .map((UncheckedFunction<File, List<File>>) FileUtils::getFiles)
                .map(e -> e.stream()
                        .filter(e2 -> e2.getName().startsWith(getDataFile().getName().replace(('.' + Utils.DATA_FILE_FORMAT), "")))
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }
    
    /**
     * Returns a data file of a specific type.
     *
     * @param type The type of the data file.
     * @return The data file.
     */
    public File getDataFile(String type) {
        return new File(getStateLocation(), getDataFile().getName()
                .replaceFirst("(?=\\.)", getDataFileTypeSuffix(type)));
    }
    
    /**
     * Returns the suffix for a data file type.
     *
     * @param type The type of the data file.
     * @return The suffix for the data file type.
     */
    private String getDataFileTypeSuffix(String type) {
        return Optional.ofNullable(type)
                .map(e -> e.replace(DEFAULT_DATA_FILE_TYPE, ""))
                .filter(e -> !e.isBlank())
                .map(e -> '-' + e).orElse("");
    }
    
    /**
     * Clears the saved data files.
     *
     * @throws Exception When there is an error clearing the saved data files.
     */
    public void cleanupData() throws Exception {
        if (!Configurator.Config.preventChannelFetch) {
            Stream.of(getDataFiles(), List.of(getCallLogFile())).flatMap(Collection::stream)
                    .forEach((CheckedConsumer<File>) FileUtils::deleteFile);
        }
    }
    
    /**
     * Cleans up any legacy state files.
     */
    private void cleanupLegacyState() {
        Stream.of(getDataFile(), getSaveFile(), getQueueFile(), getBlockFile())
                .forEach((CheckedConsumer<File>) stateFile -> {
                    final File oldFile = new File(new File(CHANNEL_DATA_DIR.getParentFile(), stateFile.getParentFile().getName()), stateFile.getName());
                    if (oldFile.exists()) {
                        if (stateFile.exists()) {
                            FileUtils.deleteFile(oldFile);
                        } else {
                            FileUtils.moveFile(oldFile, stateFile);
                        }
                    }
                });
        Stream.of(getDataFile(), getCallLogFile())
                .map(e -> e.getName().replaceAll("\\..+$", ""))
                .map((UncheckedFunction<String, List<File>>) e -> FileUtils.getFiles(getStateLocation()).stream()
                        .filter(e2 -> e2.getName().startsWith(e) && e2.getName().endsWith('.' + Utils.LIST_FILE_FORMAT))
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .forEach((CheckedConsumer<File>) FileUtils::deleteFile);
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
     * Returns the name of the Channel.
     *
     * @return The name of the Channel.
     */
    public String getChannelName() {
        return channelName;
    }
    
    /**
     * Returns the ids of the videos queued for download for the Channel.
     *
     * @return The ids of the videos queued for download for the Channel.
     */
    public List<String> getQueued() {
        return queued;
    }
    
    /**
     * Returns the ids of the saved videos of the Channel.
     *
     * @return The ids of the saved videos of the Channel.
     */
    public List<String> getSaved() {
        return saved;
    }
    
    /**
     * Returns the ids of the blocked videos of the Channel.
     *
     * @return The ids of the blocked videos of the Channel.
     */
    public List<String> getBlocked() {
        return blocked;
    }
    
    /**
     * Returns the key store of the Channel.
     *
     * @return The key store of the Channel.
     */
    public Map<String, String> getKeyStore() {
        return keyStore;
    }
    
    /**
     * Returns the internal directory to store the Channel State.
     *
     * @return The internal directory to store the Channel State.
     */
    public File getStateLocation() {
        return stateLocation;
    }
    
    /**
     * Returns the internal data file for the Channel.
     *
     * @return The internal data file for the Channel.
     */
    public File getDataFile() {
        return dataFile;
    }
    
    /**
     * Returns the internal call log file for the Channel.
     *
     * @return The internal call log file for the Channel.
     */
    public File getCallLogFile() {
        return callLogFile;
    }
    
    /**
     * Returns the internal file holding the saved videos for the Channel.
     *
     * @return The internal file holding the saved videos for the Channel.
     */
    public File getSaveFile() {
        return saveFile;
    }
    
    /**
     * Returns the internal file holding the videos queued for download for the Channel.
     *
     * @return The internal file holding the videos queued for download for the Channel.
     */
    public File getQueueFile() {
        return queueFile;
    }
    
    /**
     * Returns the internal file holding the blocked videos for the Channel.
     *
     * @return The internal file holding the blocked videos for the Channel.
     */
    public File getBlockFile() {
        return blockFile;
    }
    
    /**
     * Returns a flag indicating whether there was an error processing the Channel this run or not.
     *
     * @return A flag indicating whether there was an error processing the Channel this run or not.
     */
    public AtomicBoolean getErrorFlag() {
        return errorFlag;
    }
    
}
