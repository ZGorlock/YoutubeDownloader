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
import youtube.channel.Channel;
import youtube.entity.info.VideoInfo;

/**
 * Holds pre and post processes to operate on Channels before or after generating the download queue.
 */
@SuppressWarnings({"SpellCheckingInspection", "DuplicateBranchesInSwitch", "StatementWithEmptyBody", "RedundantSuppression"})
public class ChannelProcesses {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelProcesses.class);
    
    
    //Functions
    
    /**
     * Performs special checks specific to a Channel before producing the download queue.<br/>
     * Typically used for renaming videos in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The video map.
     * @throws Exception When there is an error.
     */
    public static void performSpecialPreConditions(Channel channel, Map<String, VideoInfo> videoMap) throws Exception {
        switch (channel.getKey()) {
            
        }
    }
    
    /**
     * Performs special checks specific to a Channel after producing the queue.<br/>
     * Typically used for filtering; Do not use this to rename videos in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The video map.
     * @throws Exception When there is an error.
     */
    public static void performSpecialPostConditions(Channel channel, Map<String, VideoInfo> videoMap) throws Exception {
        switch (channel.getKey()) {
            
        }
    }
    
}
