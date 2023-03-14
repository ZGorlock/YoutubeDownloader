/*
 * File:    ChannelInspector.java
 * Package: youtube.tool
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.tool;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import commons.object.collection.ArrayUtility;
import commons.object.string.StringUtility;
import commons.time.DateTimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
import youtube.entity.info.ChannelInfo;
import youtube.entity.info.PlaylistInfo;
import youtube.entity.info.VideoInfo;
import youtube.entity.info.detail.base.EntityDetail;
import youtube.state.Stats;
import youtube.util.ApiUtils;
import youtube.util.LogUtils;
import youtube.util.WebUtils;

/**
 * Inspects a Youtube channel.
 */
public class ChannelInspector {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelInspector.class);
    
    
    //Constants
    
    /**
     * A flag indicating whether to inspect the channel info.
     */
    private static final boolean INSPECT_INFO = true;
    
    /**
     * A flag indicating whether to inspect the channel videos.
     */
    private static final boolean INSPECT_VIDEOS = true;
    
    /**
     * A flag indicating whether to inspect the channel playlists.
     */
    private static final boolean INSPECT_PLAYLISTS = true;
    
    
    //Static Fields
    
    /**
     * The Youtube channel custom url key.
     */
    private static final String channelCustomUrlKey = "@bbcearth";
    
    /**
     * The Youtube channel url.
     */
    private static final String channelUrl = WebUtils.getChannelUrl(channelCustomUrlKey);
    
    /**
     * The Youtube channel id.
     */
    private static final String channelId = WebUtils.getChannelId(channelUrl);
    
    /**
     * The Youtube entity id to inspect.
     */
    private static String toInspect = null;
    
    
    //Static Functions
    
    /**
     * Prints data to the console about the inspected entity.
     */
    private static final BiConsumer<String, Object> printData = (String key, Object value) ->
            logger.debug(LogUtils.INDENT_HARD +
                    Color.base(key + " " + ".".repeat(24 - key.length()) + " ") +
                    Color.number(Optional.ofNullable(value).map(String::valueOf)
                            .map(e -> e.replaceAll("\r?\n\\s*", ("\n" + " ".repeat(24 + LogUtils.INDENT_WIDTH + 2))))
                            .orElse(null)));
    
    
    //Main Method
    
    /**
     * Runs the Channel Inspector.
     *
     * @param args Arguments to the main method.
     */
    public static void main(String[] args) {
        toInspect = init(ArrayUtility.getOrNull(args, 0));
        
        logger.debug(Color.log("Inspecting: ") + Color.number(toInspect));
        logger.debug(LogUtils.NEWLINE);
        
        final ChannelInfo channel = inspectChannelInfo();
        final List<VideoInfo> videos = inspectChannelVideos();
        final List<PlaylistInfo> playlists = inspectChannelPlaylists();
        
        logger.debug(Color.log("API Calls: ") + Color.number(Stats.totalApiCalls));
    }
    
    
    //Static Methods
    
    /**
     * Determines the channel id to inspect.
     *
     * @param arg The argument defining the channel id.
     * @return The channel id to inspect.
     */
    @SuppressWarnings("ConstantValue")
    private static String init(String arg) {
        if (!StringUtility.isNullOrBlank(arg)) {
            return (WebUtils.isYoutubeUrl(arg)) ? WebUtils.getChannelId(arg) :
                   (arg.matches("^U[UC].+$")) ? arg.replaceAll("^UU", "UC") :
                   init(WebUtils.getChannelUrl(arg));
        }
        return (channelId != null) ? init(channelId) :
               (channelUrl != null) ? init(channelUrl) :
               (channelCustomUrlKey != null) ? init(channelCustomUrlKey) :
               null;
    }
    
    /**
     * Inspect the Youtube channel's info.
     *
     * @return The Channel Info.
     */
    private static ChannelInfo inspectChannelInfo() {
        if (!INSPECT_INFO) {
            return null;
        }
        
        final ChannelInfo channel = ApiUtils.fetchChannel(toInspect);
        
        logger.debug(Color.base("Info:"));
        printData.accept("Title", channel.getTitle());
        printData.accept("Custom Url Key", channel.getCustomUrlKey());
        printData.accept("Url", channel.getUrl());
        printData.accept("Channel ID", channel.getChannelId());
        printData.accept("Status", channel.getStatus());
        printData.accept("Description", channel.getDescription());
        printData.accept("Topics", channel.getTopics().getAll().stream().map(EntityDetail::toString).collect(Collectors.joining(System.lineSeparator())));
        logger.debug(LogUtils.NEWLINE);
        
        return channel;
    }
    
    /**
     * Analyzes the Youtube channel's videos.
     *
     * @return The list of Video Info.
     */
    private static List<VideoInfo> inspectChannelVideos() {
        if (!INSPECT_VIDEOS) {
            return null;
        }
        
        final List<VideoInfo> videos = ApiUtils.fetchChannelVideos(toInspect);
        final long totalDuration = videos.stream().mapToLong(VideoInfo::getDuration).sum();
        
        logger.debug(Color.base("Videos:"));
        printData.accept("Video Count", videos.size());
        printData.accept("Total Length", DateTimeUtility.durationToDurationString(totalDuration, true, false, true));
        printData.accept("Average Length", DateTimeUtility.durationToDurationString((totalDuration / videos.size()), true, false, true));
        printData.accept("Longest Video", DateTimeUtility.durationToDurationString(videos.stream().mapToLong(VideoInfo::getDuration).max().orElse(0), true, false, true));
        printData.accept("Shortest Video", DateTimeUtility.durationToDurationString(videos.stream().mapToLong(VideoInfo::getDuration).min().orElse(0), true, false, true));
        printData.accept("Newest Video", videos.stream().map(VideoInfo::getDate).sorted(Comparator.reverseOrder()).limit(1).findFirst().orElse(null));
        printData.accept("Oldest Video", videos.stream().map(VideoInfo::getDate).sorted().limit(1).findFirst().orElse(null));
        logger.debug(LogUtils.NEWLINE);
        
        return videos;
    }
    
    /**
     * Analyzes the Youtube channel's playlists.
     *
     * @return The list of Playlist Info.
     */
    private static List<PlaylistInfo> inspectChannelPlaylists() {
        if (!INSPECT_PLAYLISTS) {
            return null;
        }
        
        final List<PlaylistInfo> playlists = ApiUtils.fetchChannelPlaylists(toInspect);
        
        logger.debug(Color.base("Playlists:"));
        printData.accept("Playlist Count", playlists.size());
        logger.debug(LogUtils.NEWLINE);
        
        return playlists;
    }
    
}
