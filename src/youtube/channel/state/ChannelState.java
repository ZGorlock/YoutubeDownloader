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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.channel.config.ChannelConfig;
import youtube.config.Color;
import youtube.config.Configurator;
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
    private final KeyStore.ChannelKeyStore keyStore;
    
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
        
        this.stateLocation = Channels.fetchChannelCache(channelConfig);
        this.dataFile = getStateFile("data", FileUtils.DATA_FILE_FORMAT);
        this.callLogFile = getStateFile("callLog", FileUtils.LOG_FILE_FORMAT);
        this.saveFile = getStateFile("save", FileUtils.LIST_FILE_FORMAT);
        this.queueFile = getStateFile("queue", FileUtils.LIST_FILE_FORMAT);
        this.blockFile = getStateFile("blocked", FileUtils.LIST_FILE_FORMAT);
        
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
            Optional.of(getQueueFile())
                    .filter(file -> file.exists() || Filesystem.createFile(file))
                    .map(Filesystem::readLines)
                    .map(loaded -> getQueued().addAll(loaded))
                    .orElseThrow(() -> new IOException("Error reading: " + PathUtils.path(getQueueFile())));
            
            Optional.of(getSaveFile())
                    .filter(file -> file.exists() || Filesystem.createFile(file))
                    .map(Filesystem::readLines)
                    .map(loaded -> getSaved().addAll(loaded))
                    .orElseThrow(() -> new IOException("Error reading: " + PathUtils.path(getSaveFile())));
            
            Optional.of(getBlockFile())
                    .filter(file -> file.exists() || Filesystem.createFile(file))
                    .map(Filesystem::readLines)
                    .map(loaded -> getBlocked().addAll(loaded))
                    .orElseThrow(() -> new IOException("Error reading: " + PathUtils.path(getBlockFile())));
            
        } catch (Exception e) {
            logger.error(Color.bad("Failed to load the state of Channel: ") + Color.channelName(this), e);
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
            Optional.of(getQueueFile())
                    .filter(file -> Filesystem.writeLines(file, getQueued()))
                    .orElseThrow(() -> new IOException("Error writing: " + PathUtils.path(getQueueFile())));
            
            Optional.of(getSaveFile())
                    .filter(file -> Filesystem.writeLines(file, getSaved()))
                    .orElseThrow(() -> new IOException("Error writing: " + PathUtils.path(getSaveFile())));
            
            Optional.of(getBlockFile())
                    .filter(file -> Filesystem.writeLines(file, getBlocked()))
                    .orElseThrow(() -> new IOException("Error writing: " + PathUtils.path(getBlockFile())));
            
        } catch (Exception e) {
            logger.error(Color.bad("Failed to save the state of Channel: ") + Color.channelName(this), e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the list of state files.
     *
     * @return The list of state files.
     */
    public List<File> getStateFiles() {
        return Optional.of(getStateLocation())
                .map(Filesystem::getFiles)
                .orElseGet(Collections::emptyList);
    }
    
    /**
     * Returns a state file with a specific name.
     *
     * @param fileName The name of the state file.
     * @return The state file.
     */
    public File getStateFile(String fileName) {
        return new File(getStateLocation(), (getChannelName() + '-' + fileName));
    }
    
    /**
     * Returns a state file with a specific name and format.
     *
     * @param fileName   The name of the state file.
     * @param fileFormat The format of the state file.
     * @return The state file.
     */
    public File getStateFile(String fileName, String fileFormat) {
        return getStateFile(FileUtils.setFormat(fileName, fileFormat));
    }
    
    /**
     * Returns the list of data files.
     *
     * @return The list of data files.
     */
    public List<File> getDataFiles() {
        return getStateFiles().stream()
                .filter(stateFile -> stateFile.getName().startsWith(getDataFile().getName().replaceAll("\\..+$", "")))
                .collect(Collectors.toList());
    }
    
    /**
     * Returns a data file of a specific type.
     *
     * @param type The type of the data file.
     * @return The data file.
     */
    public File getDataFile(String type) {
        return getStateFile(getDataFile().getName()
                .replaceAll((getChannelName() + '-'), "")
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
                .map(dataType -> dataType.replace(DEFAULT_DATA_FILE_TYPE, ""))
                .filter(dataType -> !dataType.isBlank())
                .map(dataType -> ('-' + dataType)).orElse("");
    }
    
    /**
     * Clears the saved data files.
     */
    public void cleanupData() {
        if (!Configurator.Config.preventChannelFetch) {
            Stream.of(getDataFiles(), List.of(getCallLogFile())).flatMap(Collection::stream)
                    .forEach(Filesystem::deleteFile);
        }
    }
    
    /**
     * Cleans up any legacy state files.
     */
    private void cleanupLegacyState() {
        Stream.of(getDataFile(), getSaveFile(), getQueueFile(), getBlockFile())
                .forEach(stateFile -> Optional.of(stateFile)
                        .map(file -> new File(new File(Channels.CHANNELS_DATA_DIR.getParentFile(), file.getParentFile().getName()), file.getName()))
                        .filter(File::exists)
                        .map(oldFile -> stateFile.exists() ?
                                        Filesystem.deleteFile(oldFile) :
                                        Filesystem.moveFile(oldFile, stateFile)));
        
        Stream.of(getDataFile(), getCallLogFile())
                .map(dataFile -> dataFile.getName().replaceAll("\\..+$", ""))
                .flatMap(oldName -> getStateFiles().stream()
                        .filter(file -> file.getName().startsWith(oldName))
                        .filter(file -> FileUtils.isFormat(file.getName(), FileUtils.LIST_FILE_FORMAT)))
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
    public KeyStore.ChannelKeyStore getKeyStore() {
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
