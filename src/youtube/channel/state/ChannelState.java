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
import youtube.channel.ChannelConfig;
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
    public String channelName;
    
    /**
     * The ids of the videos queued for download for the Channel.
     */
    public List<String> queued;
    
    /**
     * The ids of the saved videos of the Channel.
     */
    public List<String> saved;
    
    /**
     * The ids of the blocked videos of the Channel.
     */
    public List<String> blocked;
    
    /**
     * The key store of the Channel.
     */
    public Map<String, String> keyStore;
    
    /**
     * The internal directory to store the Channel State.
     */
    public File stateLocation;
    
    /**
     * The internal data file for the Channel.
     */
    public File dataFile;
    
    /**
     * The internal call log file for the Channel.
     */
    public File callLogFile;
    
    /**
     * The internal file holding the saved videos for the Channel.
     */
    public File saveFile;
    
    /**
     * The internal file holding the videos queued for download for the Channel.
     */
    public File queueFile;
    
    /**
     * The internal file holding the blocked videos for the Channel.
     */
    public File blockFile;
    
    /**
     * A flag indicating whether there was an error processing the Channel this run or not.
     */
    public AtomicBoolean error;
    
    
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
        this.dataFile = new File(this.stateLocation, (channelName + "-data.json"));
        this.callLogFile = new File(this.stateLocation, (channelName + "-callLog.log"));
        this.saveFile = new File(this.stateLocation, (channelName + "-save.txt"));
        this.queueFile = new File(this.stateLocation, (channelName + "-queue.txt"));
        this.blockFile = new File(this.stateLocation, (channelName + "-blocked.txt"));
        
        this.error = new AtomicBoolean(false);
        
        load();
    }
    
    
    //Methods
    
    /**
     * Loads the queued, saved, and blocked lists.
     *
     * @throws RuntimeException When there is an error loading the state.
     */
    private void load() {
        try {
            cleanupLegacyState();
            
            queued = FileUtils.readLines(queueFile);
            saved = FileUtils.readLines(saveFile);
            blocked = FileUtils.readLines(blockFile);
            
        } catch (IOException e) {
            System.out.println(Color.bad("Failed to load the state of Channel: ") + Color.channel(channelName));
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Saves the queued, saved, and blocked lists.
     *
     * @throws RuntimeException When there is an error saving the state.
     */
    public void save() {
        Stream.of(queued, saved, blocked).forEach(list -> {
            list.removeIf(StringUtility::isNullOrBlank);
            ListUtility.removeDuplicates(list);
        });
        
        queued.removeAll(blocked);
        queued.removeAll(saved);
        saved.removeAll(blocked);
        blocked.removeAll(saved);
        
        try {
            FileUtils.writeLines(queueFile, queued);
            FileUtils.writeLines(saveFile, saved);
            FileUtils.writeLines(blockFile, blocked);
            
        } catch (IOException e) {
            System.out.println(Color.bad("Failed to save the state of Channel: ") + Color.channel(channelName));
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the list of data files.
     *
     * @return The list of data files.
     */
    public List<File> getDataFiles() {
        return Optional.ofNullable(stateLocation)
                .map((UncheckedFunction<File, List<File>>) FileUtils::getFiles)
                .map(e -> e.stream()
                        .filter(e2 -> e2.getName().startsWith(dataFile.getName().replace(".json", "")))
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
        return new File(stateLocation, (dataFile.getName()
                .replaceFirst("(?=\\.)", getDataFileTypeSuffix(type))));
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
            Stream.of(getDataFiles(), List.of(callLogFile)).flatMap(Collection::stream)
                    .forEach((CheckedConsumer<File>) FileUtils::deleteFile);
        }
    }
    
    /**
     * Cleans up any legacy state files.
     *
     * @throws IOException When there is an error cleaning up legacy state files.
     */
    private void cleanupLegacyState() throws IOException {
        Stream.of(dataFile, saveFile, queueFile, blockFile)
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
        Stream.of(dataFile, callLogFile)
                .map(e -> e.getName().replaceAll("\\..+$", ""))
                .map((UncheckedFunction<String, List<File>>) e -> FileUtils.getFiles(stateLocation).stream()
                        .filter(e2 -> e2.getName().startsWith(e) && e2.getName().endsWith(".txt"))
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
        return channelName;
    }
    
}
