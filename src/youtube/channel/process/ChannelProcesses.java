/*
 * File:    ChannelProcesses.java
 * Package: youtube.channel.process
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.process;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.Channel;
import youtube.entity.Video;

/**
 * Defines custom processes that operate on Channels during execution.
 */
@SuppressWarnings({"SpellCheckingInspection", "DuplicateBranchesInSwitch", "StatementWithEmptyBody", "RedundantSuppression"})
public class ChannelProcesses {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelProcesses.class);
    
    
    //Static Methods
    
    /**
     * Performs special checks specific to a Channel before producing the download queue.<br/>
     * Typically used for renaming videos in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     */
    public static void performSpecialPreConditions(Channel channel, Map<String, Video> videoMap) {
        switch (channel.getConfig().getKey()) {
            
        }
    }
    
    /**
     * Performs special checks specific to a Channel after producing the queue.<br/>
     * Typically used for filtering; Do not use this to rename videos in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     */
    public static void performSpecialPostConditions(Channel channel, Map<String, Video> videoMap) {
        switch (channel.getConfig().getKey()) {
            
        }
    }
    
}
