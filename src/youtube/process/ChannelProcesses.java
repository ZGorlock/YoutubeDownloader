/*
 * File:    ChannelProcesses.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.process;

import java.util.List;
import java.util.Map;

import youtube.YoutubeChannelDownloader;
import youtube.channel.Channel;

/**
 * Holds pre and post processes to operate on Channels before or after generating the download queue.
 */
public class ChannelProcesses {
    
    //Functions
    
    /**
     * Performs special checks specific to a Channel before producing the download queue.
     * Typically used for renaming videos in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The video map.
     * @param queue    The list of queued videos.
     * @param save     The list of saved videos.
     * @param blocked  The list of blocked videos.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static void performSpecialPreConditions(Channel channel, Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> queue, List<String> save, List<String> blocked) throws Exception {
        switch (channel.key) {
            
        }
    }
    
    /**
     * Performs special checks specific to a Channel after producing the queue.
     * Typically used for filtering; Do not use this to rename videos in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The video map.
     * @param queue    The list of queued videos.
     * @param save     The list of saved videos.
     * @param blocked  The list of blocked videos.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static void performSpecialPostConditions(Channel channel, Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> queue, List<String> save, List<String> blocked) throws Exception {
        switch (channel.key) {
            
        }
    }
    
}
