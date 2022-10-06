/*
 * File:    ChannelState.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.conf.Color;
import youtube.conf.Configurator;
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
    
    
    //Fields
    
    /**
     * The Channel.
     */
    public Channel channel;
    
    /**
     * The ids of the videos queued for download for the Channel.
     */
    public List<String> queue;
    
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
    public File blockedFile;
    
    
    //Constructors
    
    /**
     * Creates a Channel State for a Channel.
     *
     * @param channel The Channel.
     */
    public ChannelState(Channel channel) {
        this.channel = channel;
        
        this.queue = new ArrayList<>();
        this.saved = new ArrayList<>();
        this.blocked = new ArrayList<>();
        
        this.keyStore = new LinkedHashMap<>();
        
        this.stateLocation = new File(CHANNEL_DATA_DIR, channel.name);
        this.dataFile = new File(this.stateLocation, (channel.name + "-data.txt"));
        this.callLogFile = new File(this.stateLocation, (channel.name + "-callLog.txt"));
        this.saveFile = new File(this.stateLocation, (channel.name + "-save.txt"));
        this.queueFile = new File(this.stateLocation, (channel.name + "-queue.txt"));
        this.blockedFile = new File(this.stateLocation, (channel.name + "-blocked.txt"));
    }
    
    
    //Methods
    
    /**
     * Loads the queue, save, and blocked lists.
     *
     * @throws RuntimeException When there is an error loading the state.
     */
    public void load() {
        try {
            cleanupLegacyState();
            
            queue = FileUtils.readLines(queueFile);
            saved = FileUtils.readLines(saveFile);
            blocked = FileUtils.readLines(blockedFile);
            
        } catch (IOException e) {
            System.out.println(Color.bad("Failed to load the state of channel: ") + Color.channel(channel));
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Saves the queue, save, and blocked lists.
     *
     * @throws RuntimeException When there is an error saving the state.
     */
    public void save() {
        queue.removeAll(blocked);
        queue.removeAll(saved);
        saved.removeAll(blocked);
        blocked.removeAll(saved);
        
        queue = queue.stream().distinct().collect(Collectors.toList());
        saved = saved.stream().distinct().collect(Collectors.toList());
        blocked = blocked.stream().distinct().collect(Collectors.toList());
        
        try {
            FileUtils.writeLines(queueFile, queue);
            FileUtils.writeLines(saveFile, saved);
            FileUtils.writeLines(blockedFile, blocked);
            
        } catch (IOException e) {
            System.out.println(Color.bad("Failed to save the state of channel: ") + Color.channel(channel));
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the list of data files.
     *
     * @return The list of data files.
     */
    public List<File> getDataFiles() {
        File[] dataFiles = stateLocation.listFiles(e -> e.getName().startsWith(dataFile.getName()));
        return (dataFiles == null) ? new ArrayList<>() :
               Arrays.asList(dataFiles);
    }
    
    /**
     * Returns a data file of a specific chunk index.
     *
     * @param chunk The chunk index.
     * @return The data file of the specified chunk index.
     */
    public File getDataFile(int chunk) {
        return new File(stateLocation, (dataFile.getName() + '.' + chunk));
    }
    
    /**
     * Clears the saved data files.
     *
     * @throws Exception When there is an error clearing the saved data files.
     */
    public void cleanupData() throws Exception {
        if (!Configurator.Config.preventChannelFetch) {
            for (File dataFile : getDataFiles()) {
                FileUtils.deleteFile(dataFile);
            }
        }
    }
    
    /**
     * Cleans up any legacy state files.
     *
     * @throws IOException When there is an error cleaning up legacy state files.
     */
    private void cleanupLegacyState() throws IOException {
        for (File cleanupFile : Arrays.asList(dataFile, saveFile, queueFile, blockedFile)) {
            File oldFile = new File(new File(CHANNEL_DATA_DIR.getParentFile(), cleanupFile.getParentFile().getName()), cleanupFile.getName());
            if (oldFile.exists()) {
                if (cleanupFile.exists()) {
                    FileUtils.deleteFile(oldFile);
                } else {
                    FileUtils.moveFile(oldFile, cleanupFile);
                }
            }
        }
    }
    
}
