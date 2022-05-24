/*
 * File:    ChannelState.java
 * Package: youtube.channel
 * Author:  Zachary Gill
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

import org.apache.commons.io.FileUtils;
import youtube.util.Configurator;

/**
 * Manages the state of a Channel.
 */
public class ChannelState {
    
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
        
        this.stateLocation = new File("data/channel/" + channel.name);
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
            queue = queueFile.exists() ? FileUtils.readLines(queueFile, "UTF-8") : new ArrayList<>();
            saved = saveFile.exists() ? FileUtils.readLines(saveFile, "UTF-8") : new ArrayList<>();
            blocked = blockedFile.exists() ? FileUtils.readLines(blockedFile, "UTF-8") : new ArrayList<>();
        } catch (IOException e) {
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
            throw new RuntimeException(e);
        }
        
        KeyStore.save();
    }
    
    /**
     * Returns the list of data files.
     *
     * @return The list of data files.
     */
    @SuppressWarnings("ConstantConditions")
    public List<File> getDataFiles() {
        return Arrays.asList(stateLocation.listFiles(e -> e.getName().startsWith(dataFile.getName())));
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
     * Cleans up the state directory.
     *
     * @throws Exception When there is an error cleaning the state directory.
     */
    public void cleanup() throws Exception {
        if (!Configurator.Config.preventChannelFetch) {
            for (File dataFile : getDataFiles()) {
                FileUtils.deleteQuietly(dataFile);
            }
        }
        cleanupLegacyState();
    }
    
    /**
     * Cleans up any legacy state files.
     *
     * @throws Exception When there is an error cleaning up legacy state files.
     */
    private void cleanupLegacyState() throws Exception {
        for (File cleanupFile : Arrays.asList(dataFile, saveFile, queueFile, blockedFile)) {
            File oldFile = new File(cleanupFile.getAbsolutePath().replace("\\", "/").replace("/channel/", "/"));
            if (oldFile.exists()) {
                if (cleanupFile.exists()) {
                    FileUtils.deleteQuietly(oldFile);
                } else {
                    FileUtils.moveFile(oldFile, cleanupFile);
                }
            }
        }
    }
    
}