/*
 * File:    Color.java
 * Package: youtube.config
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.config;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import commons.io.console.Console;
import commons.io.console.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.config.ChannelConfig;
import youtube.channel.config.ChannelEntry;
import youtube.channel.state.ChannelState;
import youtube.entity.Channel;
import youtube.entity.Video;
import youtube.entity.info.ChannelInfo;
import youtube.entity.info.VideoInfo;
import youtube.util.ExecutableUtils;
import youtube.util.FileUtils;
import youtube.util.PathUtils;

/**
 * Handles coloring of console output.
 */
public class Color {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Color.class);
    
    
    //Constants
    
    /**
     * A list of available colors.
     */
    public static final List<String> AVAILABLE_COLORS = List.of(
            "DEFAULT", "DEFAULT_COLOR",
            "WHITE", "GREY", "DARK_GREY", "BLACK",
            "DARK_RED", "RED", "ORANGE", "YELLOW",
            "DARK_GREEN", "GREEN", "TEAL", "CYAN",
            "DARK_BLUE", "BLUE", "PURPLE", "MAGENTA");
    
    
    //Static Fields
    
    /**
     * A flag indicating whether the color configuration settings have been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Static Methods
    
    /**
     * Initializes the color configuration.
     */
    public static void initColors() {
        if (loaded.compareAndSet(false, true)) {
            logger.debug(log("Initializing Colors..."));
            
            Config.init();
        }
    }
    
    /**
     * Applies a color to output.
     *
     * @param color The color.
     * @param o     The output.
     * @return The colored output.
     */
    public static String apply(Console.ConsoleEffect color, Object o) {
        return !Config.enableColors ? String.valueOf(o) :
               color.apply(String.valueOf(o));
    }
    
    /**
     * Colors "base" output to be displayed on the console.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String base(Object o) {
        return apply(Config.base, o);
    }
    
    /**
     * Colors "good" output to be displayed on the console.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String good(Object o) {
        return apply(Config.good, o);
    }
    
    /**
     * Colors "bad" output to be displayed on the console.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String bad(Object o) {
        return apply(Config.bad, o);
    }
    
    /**
     * Colors "log" output to be displayed on the console.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String log(Object o) {
        return apply(Config.log, o);
    }
    
    /**
     * Colors "channel" output to be displayed on the console.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String channel(Object o) {
        return apply(Config.channel, o);
    }
    
    /**
     * Colors "video" output to be displayed on the console.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String video(Object o) {
        return apply(Config.video, o);
    }
    
    /**
     * Colors "number" output to be displayed on the console.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String number(Object o) {
        return apply(Config.number, o);
    }
    
    /**
     * Colors "file" output to be displayed on the console.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String file(Object o) {
        return apply(Config.file, o);
    }
    
    /**
     * Colors "exe" output to be displayed on the console.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String exe(Object o) {
        return apply(Config.exe, o);
    }
    
    /**
     * Colors "link" output to be displayed on the console.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String link(Object o) {
        return apply(Config.link, o);
    }
    
    /**
     * Adds colored quotes around output to be displayed on the console.
     *
     * @param output      The output.
     * @param doubleQuote Whether to use double quotes.
     * @return The quoted output.
     */
    public static String quoted(String output, boolean doubleQuote) {
        final String quote = log(doubleQuote ? '\"' : '\'');
        return quote + output + quote;
    }
    
    /**
     * Adds colored quotes around output to be displayed on the console.
     *
     * @param output The output.
     * @return The quoted output.
     */
    public static String quoted(String output) {
        return quoted(output, false);
    }
    
    /**
     * Colors and formats a file path to be displayed on the console.
     *
     * @param filePath The file path.
     * @return The prepared console output.
     */
    public static String filePath(String filePath) {
        return file(PathUtils.path(filePath));
    }
    
    /**
     * Colors and formats a file path to be displayed on the console.
     *
     * @param file The file.
     * @return The prepared console output.
     */
    public static String filePath(File file) {
        return filePath(file.getAbsolutePath());
    }
    
    /**
     * Colors and formats a quoted file path to be displayed on the console.
     *
     * @param filePath    The file path.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteFilePath(String filePath, boolean doubleQuote) {
        return quoted(filePath(filePath), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted file path to be displayed on the console.
     *
     * @param filePath The file path.
     * @return The prepared console output.
     */
    public static String quoteFilePath(String filePath) {
        return quoteFilePath(filePath, false);
    }
    
    /**
     * Colors and formats a quoted file path to be displayed on the console.
     *
     * @param file        The file.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteFilePath(File file, boolean doubleQuote) {
        return quoteFilePath(file.getAbsolutePath(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted file path to be displayed on the console.
     *
     * @param file The file.
     * @return The prepared console output.
     */
    public static String quoteFilePath(File file) {
        return quoteFilePath(file, false);
    }
    
    /**
     * Colors and formats a file name to be displayed on the console.
     *
     * @param fileName The file name.
     * @return The prepared console output.
     */
    public static String fileName(String fileName) {
        return file(fileName);
    }
    
    /**
     * Colors and formats a file name to be displayed on the console.
     *
     * @param file The file.
     * @return The prepared console output.
     */
    public static String fileName(File file) {
        return fileName(file.getName());
    }
    
    /**
     * Colors and formats a quoted file name to be displayed on the console.
     *
     * @param fileName    The file name.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteFileName(String fileName, boolean doubleQuote) {
        return quoted(fileName(fileName), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted file name to be displayed on the console.
     *
     * @param fileName The file name.
     * @return The prepared console output.
     */
    public static String quoteFileName(String fileName) {
        return quoteFileName(fileName, false);
    }
    
    /**
     * Colors and formats a quoted file name to be displayed on the console.
     *
     * @param file        The file.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteFileName(File file, boolean doubleQuote) {
        return quoteFileName(file.getName(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted file name to be displayed on the console.
     *
     * @param file The file.
     * @return The prepared console output.
     */
    public static String quoteFileName(File file) {
        return quoteFileName(file, false);
    }
    
    /**
     * Colors and formats a Video title to be displayed on the console.
     *
     * @param videoTitle The Video title.
     * @return The prepared console output.
     */
    public static String videoTitle(String videoTitle) {
        return video(videoTitle);
    }
    
    /**
     * Colors and formats a Video title to be displayed on the console.
     *
     * @param videoInfo The Video Info.
     * @return The prepared console output.
     */
    public static String videoTitle(VideoInfo videoInfo) {
        return videoTitle(videoInfo.getTitle());
    }
    
    /**
     * Colors and formats a Video title to be displayed on the console.
     *
     * @param video The Video.
     * @return The prepared console output.
     */
    public static String videoTitle(Video video) {
        return videoTitle(video.getTitle());
    }
    
    /**
     * Colors and formats a quoted Video title to be displayed on the console.
     *
     * @param videoTitle  The Video title.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteVideoTitle(String videoTitle, boolean doubleQuote) {
        return quoted(videoTitle(videoTitle), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Video title to be displayed on the console.
     *
     * @param videoTitle The Video title.
     * @return The prepared console output.
     */
    public static String quoteVideoTitle(String videoTitle) {
        return quoteVideoTitle(videoTitle, false);
    }
    
    /**
     * Colors and formats a quoted Video title to be displayed on the console.
     *
     * @param videoInfo   The Video Info.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteVideoTitle(VideoInfo videoInfo, boolean doubleQuote) {
        return quoteVideoTitle(videoInfo.getTitle(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Video title to be displayed on the console.
     *
     * @param videoInfo The Video Info.
     * @return The prepared console output.
     */
    public static String quoteVideoTitle(VideoInfo videoInfo) {
        return quoteVideoTitle(videoInfo, false);
    }
    
    /**
     * Colors and formats a quoted Video title to be displayed on the console.
     *
     * @param video       The Video.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteVideoTitle(Video video, boolean doubleQuote) {
        return quoteVideoTitle(video.getTitle(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Video title to be displayed on the console.
     *
     * @param video The Video.
     * @return The prepared console output.
     */
    public static String quoteVideoTitle(Video video) {
        return quoteVideoTitle(video, false);
    }
    
    /**
     * Colors and formats a Video file path to be displayed on the console.
     *
     * @param videoFilePath The Video file path.
     * @return The prepared console output.
     */
    public static String videoFilePath(String videoFilePath) {
        return FileUtils.isFormat(videoFilePath, FileUtils.DOWNLOAD_FILE_FORMAT) ? file(videoFilePath) : video(videoFilePath);
    }
    
    /**
     * Colors and formats a Video file path to be displayed on the console.
     *
     * @param videoFile The Video file.
     * @return The prepared console output.
     */
    public static String videoFilePath(File videoFile) {
        return videoFilePath(videoFile.getAbsolutePath());
    }
    
    /**
     * Colors and formats a Video file path to be displayed on the console.
     *
     * @param video The Video.
     * @return The prepared console output.
     */
    public static String videoFilePath(Video video) {
        return videoFilePath(video.getOutput());
    }
    
    /**
     * Colors and formats a quoted Video file path to be displayed on the console.
     *
     * @param videoFilePath The Video file path.
     * @param doubleQuote   Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteVideoFilePath(String videoFilePath, boolean doubleQuote) {
        return quoted(videoFilePath(videoFilePath), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Video file path to be displayed on the console.
     *
     * @param videoFilePath The Video file path.
     * @return The prepared console output.
     */
    public static String quoteVideoFilePath(String videoFilePath) {
        return quoteVideoFilePath(videoFilePath, false);
    }
    
    /**
     * Colors and formats a quoted Video file path to be displayed on the console.
     *
     * @param videoFile   The Video file.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteVideoFilePath(File videoFile, boolean doubleQuote) {
        return quoteVideoFilePath(videoFile.getAbsolutePath(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Video file path to be displayed on the console.
     *
     * @param videoFile The Video file.
     * @return The prepared console output.
     */
    public static String quoteVideoFilePath(File videoFile) {
        return quoteVideoFilePath(videoFile, false);
    }
    
    /**
     * Colors and formats a quoted Video file path to be displayed on the console.
     *
     * @param video       The Video.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteVideoFilePath(Video video, boolean doubleQuote) {
        return quoteVideoFilePath(video.getOutput(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Video file path to be displayed on the console.
     *
     * @param video The Video.
     * @return The prepared console output.
     */
    public static String quoteVideoFilePath(Video video) {
        return quoteVideoFilePath(video, false);
    }
    
    /**
     * Colors and formats a Video file name to be displayed on the console.
     *
     * @param videoFileName The Video file name.
     * @return The prepared console output.
     */
    public static String videoFileName(String videoFileName) {
        return FileUtils.isFormat(videoFileName, FileUtils.DOWNLOAD_FILE_FORMAT) ? file(videoFileName) : video(videoFileName);
    }
    
    /**
     * Colors and formats a Video file name to be displayed on the console.
     *
     * @param videoFile The Video file.
     * @return The prepared console output.
     */
    public static String videoFileName(File videoFile) {
        return videoFileName(videoFile.getName());
    }
    
    /**
     * Colors and formats a Video file name to be displayed on the console.
     *
     * @param video The Video.
     * @return The prepared console output.
     */
    public static String videoFileName(Video video) {
        return videoFileName(video.getOutput());
    }
    
    /**
     * Colors and formats a quoted Video file name to be displayed on the console.
     *
     * @param videoFileName The Video file name.
     * @param doubleQuote   Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteVideoFileName(String videoFileName, boolean doubleQuote) {
        return quoted(videoFileName(videoFileName), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Video file name to be displayed on the console.
     *
     * @param videoFileName The Video file name.
     * @return The prepared console output.
     */
    public static String quoteVideoFileName(String videoFileName) {
        return quoteVideoFileName(videoFileName, false);
    }
    
    /**
     * Colors and formats a quoted Video file name to be displayed on the console.
     *
     * @param videoFile   The Video file.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteVideoFileName(File videoFile, boolean doubleQuote) {
        return quoteVideoFileName(videoFile.getName(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Video file name to be displayed on the console.
     *
     * @param videoFile The Video file.
     * @return The prepared console output.
     */
    public static String quoteVideoFileName(File videoFile) {
        return quoteVideoFileName(videoFile, false);
    }
    
    /**
     * Colors and formats a quoted Video file name to be displayed on the console.
     *
     * @param video       The Video.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteVideoFileName(Video video, boolean doubleQuote) {
        return quoteVideoFileName(video.getOutput(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Video file name to be displayed on the console.
     *
     * @param video The Video.
     * @return The prepared console output.
     */
    public static String quoteVideoFileName(Video video) {
        return quoteVideoFileName(video, false);
    }
    
    /**
     * Colors and formats a Channel name to be displayed on the console.
     *
     * @param channelName The Channel name.
     * @return The prepared console output.
     */
    public static String channelName(String channelName) {
        return channel(channelName);
    }
    
    /**
     * Colors and formats a Channel name to be displayed on the console.
     *
     * @param channelState The Channel State.
     * @return The prepared console output.
     */
    public static String channelName(ChannelState channelState) {
        return channelName(channelState.getChannelName());
    }
    
    /**
     * Colors and formats a Channel name to be displayed on the console.
     *
     * @param channelEntry The Channel Entry.
     * @return The prepared console output.
     */
    public static String channelName(ChannelEntry channelEntry) {
        return channelName(channelEntry.getName());
    }
    
    /**
     * Colors and formats a Channel name to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String channelName(Channel channel) {
        return channelName(channel.getConfig());
    }
    
    /**
     * Colors and formats a quoted Channel name to be displayed on the console.
     *
     * @param channelName The Channel name.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelName(String channelName, boolean doubleQuote) {
        return quoted(channelName(channelName), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel name to be displayed on the console.
     *
     * @param channelName The Channel name.
     * @return The prepared console output.
     */
    public static String quoteChannelName(String channelName) {
        return quoteChannelName(channelName, false);
    }
    
    /**
     * Colors and formats a quoted Channel name to be displayed on the console.
     *
     * @param channelState The Channel State.
     * @param doubleQuote  Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelName(ChannelState channelState, boolean doubleQuote) {
        return quoteChannelName(channelState.getChannelName(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel name to be displayed on the console.
     *
     * @param channelState The Channel State.
     * @return The prepared console output.
     */
    public static String quoteChannelName(ChannelState channelState) {
        return quoteChannelName(channelState, false);
    }
    
    /**
     * Colors and formats a quoted Channel name to be displayed on the console.
     *
     * @param channelEntry The Channel Entry.
     * @param doubleQuote  Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelName(ChannelEntry channelEntry, boolean doubleQuote) {
        return quoteChannelName(channelEntry.getName(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel name to be displayed on the console.
     *
     * @param channelEntry The Channel Entry.
     * @return The prepared console output.
     */
    public static String quoteChannelName(ChannelEntry channelEntry) {
        return quoteChannelName(channelEntry, false);
    }
    
    /**
     * Colors and formats a quoted Channel name to be displayed on the console.
     *
     * @param channel     The Channel.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelName(Channel channel, boolean doubleQuote) {
        return quoteChannelName(channel.getConfig(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel name to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String quoteChannelName(Channel channel) {
        return quoteChannelName(channel, false);
    }
    
    /**
     * Colors and formats a Channel display name to be displayed on the console.
     *
     * @param channelDisplayName The Channel display name.
     * @return The prepared console output.
     */
    public static String channelDisplayName(String channelDisplayName) {
        return channel(channelDisplayName);
    }
    
    /**
     * Colors and formats a Channel display name to be displayed on the console.
     *
     * @param channelConfig The Channel Config.
     * @return The prepared console output.
     */
    public static String channelDisplayName(ChannelConfig channelConfig) {
        return channelDisplayName(channelConfig.getDisplayName());
    }
    
    /**
     * Colors and formats a Channel display name to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String channelDisplayName(Channel channel) {
        return channelDisplayName(channel.getConfig());
    }
    
    /**
     * Colors and formats a quoted Channel display name to be displayed on the console.
     *
     * @param channelDisplayName The Channel display name.
     * @param doubleQuote        Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelDisplayName(String channelDisplayName, boolean doubleQuote) {
        return quoted(channelDisplayName(channelDisplayName), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel display name to be displayed on the console.
     *
     * @param channelDisplayName The Channel display name.
     * @return The prepared console output.
     */
    public static String quoteChannelDisplayName(String channelDisplayName) {
        return quoteChannelDisplayName(channelDisplayName, false);
    }
    
    /**
     * Colors and formats a quoted Channel display name to be displayed on the console.
     *
     * @param channelConfig The Channel Config.
     * @param doubleQuote   Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelDisplayName(ChannelConfig channelConfig, boolean doubleQuote) {
        return quoteChannelDisplayName(channelConfig.getDisplayName(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel display name to be displayed on the console.
     *
     * @param channelConfig The Channel Config.
     * @return The prepared console output.
     */
    public static String quoteChannelDisplayName(ChannelConfig channelConfig) {
        return quoteChannelDisplayName(channelConfig, false);
    }
    
    /**
     * Colors and formats a quoted Channel display name to be displayed on the console.
     *
     * @param channel     The Channel.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelDisplayName(Channel channel, boolean doubleQuote) {
        return quoteChannelDisplayName(channel.getConfig(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel display name to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String quoteChannelDisplayName(Channel channel) {
        return quoteChannelDisplayName(channel, false);
    }
    
    /**
     * Colors and formats a Channel key to be displayed on the console.
     *
     * @param channelKey The Channel key.
     * @return The prepared console output.
     */
    public static String channelKey(String channelKey) {
        return channel(channelKey);
    }
    
    /**
     * Colors and formats a Channel key to be displayed on the console.
     *
     * @param channelEntry The Channel Entry.
     * @return The prepared console output.
     */
    public static String channelKey(ChannelEntry channelEntry) {
        return channelKey(channelEntry.getKey());
    }
    
    /**
     * Colors and formats a Channel key to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String channelKey(Channel channel) {
        return channelKey(channel.getConfig());
    }
    
    /**
     * Colors and formats a quoted Channel key to be displayed on the console.
     *
     * @param channelKey  The Channel key.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelKey(String channelKey, boolean doubleQuote) {
        return quoted(channelKey(channelKey), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel key to be displayed on the console.
     *
     * @param channelKey The Channel key.
     * @return The prepared console output.
     */
    public static String quoteChannelKey(String channelKey) {
        return quoteChannelKey(channelKey, false);
    }
    
    /**
     * Colors and formats a quoted Channel key to be displayed on the console.
     *
     * @param channelEntry The Channel Entry.
     * @param doubleQuote  Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelKey(ChannelEntry channelEntry, boolean doubleQuote) {
        return quoteChannelKey(channelEntry.getKey(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel key to be displayed on the console.
     *
     * @param channelEntry The Channel Entry.
     * @return The prepared console output.
     */
    public static String quoteChannelKey(ChannelEntry channelEntry) {
        return quoteChannelKey(channelEntry, false);
    }
    
    /**
     * Colors and formats a quoted Channel key to be displayed on the console.
     *
     * @param channel     The Channel.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelKey(Channel channel, boolean doubleQuote) {
        return quoteChannelKey(channel.getConfig().getKey(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel key to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String quoteChannelKey(Channel channel) {
        return quoteChannelKey(channel, false);
    }
    
    /**
     * Colors and formats a Channel title to be displayed on the console.
     *
     * @param channelTitle The Channel title.
     * @return The prepared console output.
     */
    public static String channelTitle(String channelTitle) {
        return channel(channelTitle);
    }
    
    /**
     * Colors and formats a Channel title to be displayed on the console.
     *
     * @param channelInfo The Channel Info.
     * @return The prepared console output.
     */
    public static String channelTitle(ChannelInfo channelInfo) {
        return channelTitle(channelInfo.getTitle());
    }
    
    /**
     * Colors and formats a Channel title to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String channelTitle(Channel channel) {
        return channelTitle(channel.getInfo());
    }
    
    /**
     * Colors and formats a quoted Channel title to be displayed on the console.
     *
     * @param channelTitle The Channel title.
     * @param doubleQuote  Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelTitle(String channelTitle, boolean doubleQuote) {
        return quoted(channelTitle(channelTitle), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel title to be displayed on the console.
     *
     * @param channelTitle The Channel title.
     * @return The prepared console output.
     */
    public static String quoteChannelTitle(String channelTitle) {
        return quoteChannelTitle(channelTitle, false);
    }
    
    /**
     * Colors and formats a quoted Channel title to be displayed on the console.
     *
     * @param channelInfo The Channel Info.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelTitle(ChannelInfo channelInfo, boolean doubleQuote) {
        return quoteChannelTitle(channelInfo.getTitle(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel title to be displayed on the console.
     *
     * @param channelInfo The Channel Info.
     * @return The prepared console output.
     */
    public static String quoteChannelTitle(ChannelInfo channelInfo) {
        return quoteChannelTitle(channelInfo, false);
    }
    
    /**
     * Colors and formats a quoted Channel title to be displayed on the console.
     *
     * @param channel     The Channel.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelTitle(Channel channel, boolean doubleQuote) {
        return quoteChannelTitle(channel.getInfo(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel title to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String quoteChannelTitle(Channel channel) {
        return quoteChannelTitle(channel, false);
    }
    
    /**
     * Colors and formats a Channel file path to be displayed on the console.
     *
     * @param channelFilePath The Channel file path.
     * @return The prepared console output.
     */
    public static String channelFilePath(String channelFilePath) {
        return FileUtils.isFormat(channelFilePath, FileUtils.DOWNLOAD_FILE_FORMAT) ? file(channelFilePath) : channel(channelFilePath);
    }
    
    /**
     * Colors and formats a Channel file path to be displayed on the console.
     *
     * @param channelFile The Channel file.
     * @return The prepared console output.
     */
    public static String channelFilePath(File channelFile) {
        return channelFilePath(channelFile.getAbsolutePath());
    }
    
    /**
     * Colors and formats a Channel file path to be displayed on the console.
     *
     * @param channelConfig The Channel Config.
     * @return The prepared console output.
     */
    public static String channelFilePath(ChannelConfig channelConfig) {
        return channelFilePath(channelConfig.getOutputFolder());
    }
    
    /**
     * Colors and formats a Channel file path to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String channelFilePath(Channel channel) {
        return channelFilePath(channel.getConfig());
    }
    
    /**
     * Colors and formats a quoted Channel file path to be displayed on the console.
     *
     * @param channelFilePath The Channel file path.
     * @param doubleQuote     Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelFilePath(String channelFilePath, boolean doubleQuote) {
        return quoted(channelFilePath(channelFilePath), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel file path to be displayed on the console.
     *
     * @param channelFilePath The Channel file path.
     * @return The prepared console output.
     */
    public static String quoteChannelFilePath(String channelFilePath) {
        return quoteChannelFilePath(channelFilePath, false);
    }
    
    /**
     * Colors and formats a quoted Channel file path to be displayed on the console.
     *
     * @param channelFile The Channel file.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelFilePath(File channelFile, boolean doubleQuote) {
        return quoteChannelFilePath(channelFile.getAbsolutePath(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel file path to be displayed on the console.
     *
     * @param channelFile The Channel file.
     * @return The prepared console output.
     */
    public static String quoteChannelFilePath(File channelFile) {
        return quoteChannelFilePath(channelFile, false);
    }
    
    /**
     * Colors and formats a quoted Channel file path to be displayed on the console.
     *
     * @param channelConfig The Channel Config.
     * @param doubleQuote   Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelFilePath(ChannelConfig channelConfig, boolean doubleQuote) {
        return quoteChannelFilePath(channelConfig.getOutputFolder(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel file path to be displayed on the console.
     *
     * @param channelConfig The Channel Config.
     * @return The prepared console output.
     */
    public static String quoteChannelFilePath(ChannelConfig channelConfig) {
        return quoteChannelFilePath(channelConfig, false);
    }
    
    /**
     * Colors and formats a quoted Channel file path to be displayed on the console.
     *
     * @param channel     The Channel.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelFilePath(Channel channel, boolean doubleQuote) {
        return quoteChannelFilePath(channel.getConfig(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel file path to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String quoteChannelFilePath(Channel channel) {
        return quoteChannelFilePath(channel, false);
    }
    
    /**
     * Colors and formats a Channel file name to be displayed on the console.
     *
     * @param channelFileName The Channel file name.
     * @return The prepared console output.
     */
    public static String channelFileName(String channelFileName) {
        return FileUtils.isFormat(channelFileName, FileUtils.DOWNLOAD_FILE_FORMAT) ? file(channelFileName) : channel(channelFileName);
    }
    
    /**
     * Colors and formats a Channel file name to be displayed on the console.
     *
     * @param channelFile The Channel file.
     * @return The prepared console output.
     */
    public static String channelFileName(File channelFile) {
        return channelFileName(channelFile.getName());
    }
    
    /**
     * Colors and formats a Channel file name to be displayed on the console.
     *
     * @param channelConfig The Channel Config.
     * @return The prepared console output.
     */
    public static String channelFileName(ChannelConfig channelConfig) {
        return channelFileName(channelConfig.getOutputFolder());
    }
    
    /**
     * Colors and formats a Channel file name to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String channelFileName(Channel channel) {
        return channelFileName(channel.getConfig());
    }
    
    /**
     * Colors and formats a quoted Channel file name to be displayed on the console.
     *
     * @param channelFileName The Channel file name.
     * @param doubleQuote     Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelFileName(String channelFileName, boolean doubleQuote) {
        return quoted(channelFileName(channelFileName), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel file name to be displayed on the console.
     *
     * @param channelFileName The Channel file name.
     * @return The prepared console output.
     */
    public static String quoteChannelFileName(String channelFileName) {
        return quoteChannelFileName(channelFileName, false);
    }
    
    /**
     * Colors and formats a quoted Channel file name to be displayed on the console.
     *
     * @param channelFile The Channel file.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelFileName(File channelFile, boolean doubleQuote) {
        return quoteChannelFileName(channelFile.getName(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel file name to be displayed on the console.
     *
     * @param channelFile The Channel file.
     * @return The prepared console output.
     */
    public static String quoteChannelFileName(File channelFile) {
        return quoteChannelFileName(channelFile, false);
    }
    
    /**
     * Colors and formats a quoted Channel file name to be displayed on the console.
     *
     * @param channelConfig The Channel Config.
     * @param doubleQuote   Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelFileName(ChannelConfig channelConfig, boolean doubleQuote) {
        return quoteChannelFileName(channelConfig.getOutputFolder(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel file name to be displayed on the console.
     *
     * @param channelConfig The Channel Config.
     * @return The prepared console output.
     */
    public static String quoteChannelFileName(ChannelConfig channelConfig) {
        return quoteChannelFileName(channelConfig, false);
    }
    
    /**
     * Colors and formats a quoted Channel file name to be displayed on the console.
     *
     * @param channel     The Channel.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteChannelFileName(Channel channel, boolean doubleQuote) {
        return quoteChannelFileName(channel.getConfig(), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted Channel file name to be displayed on the console.
     *
     * @param channel The Channel.
     * @return The prepared console output.
     */
    public static String quoteChannelFileName(Channel channel) {
        return quoteChannelFileName(channel, false);
    }
    
    /**
     * Colors and formats an executable name to be displayed on the console.
     *
     * @param executableName The executable name.
     * @return The prepared console output.
     */
    public static String exeName(String executableName) {
        return exe(executableName);
    }
    
    /**
     * Colors and formats an executable name to be displayed on the console.
     *
     * @param executable The executable.
     * @return The prepared console output.
     */
    public static String exeName(ExecutableUtils.Executable executable) {
        return exeName(executable.getName());
    }
    
    /**
     * Colors and formats a quoted executable name to be displayed on the console.
     *
     * @param executableName The executable name.
     * @param doubleQuote    Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteExeName(String executableName, boolean doubleQuote) {
        return quoted(exeName(executableName), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted executable name to be displayed on the console.
     *
     * @param executableName The executable name.
     * @return The prepared console output.
     */
    public static String quoteExeName(String executableName) {
        return quoteExeName(executableName, false);
    }
    
    /**
     * Colors and formats a quoted executable name to be displayed on the console.
     *
     * @param executable  The executable.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteExeName(ExecutableUtils.Executable executable, boolean doubleQuote) {
        return quoted(exeName(executable), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted executable name to be displayed on the console.
     *
     * @param executable The executable.
     * @return The prepared console output.
     */
    public static String quoteExeName(ExecutableUtils.Executable executable) {
        return quoteExeName(executable, false);
    }
    
    /**
     * Colors and formats an executable file path to be displayed on the console.
     *
     * @param executableFilePath The executable file path.
     * @return The prepared console output.
     */
    public static String exeFilePath(String executableFilePath) {
        return exe(executableFilePath);
    }
    
    /**
     * Colors and formats an executable file path to be displayed on the console.
     *
     * @param executable The executable.
     * @return The prepared console output.
     */
    public static String exeFilePath(ExecutableUtils.Executable executable) {
        return exeFilePath(executable.getExe().getAbsolutePath());
    }
    
    /**
     * Colors and formats a quoted executable file path to be displayed on the console.
     *
     * @param executableFilePath The executable file path.
     * @param doubleQuote        Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteExeFilePath(String executableFilePath, boolean doubleQuote) {
        return quoted(exeFilePath(executableFilePath), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted executable file path to be displayed on the console.
     *
     * @param executableFilePath The executable file path.
     * @return The prepared console output.
     */
    public static String quoteExeFilePath(String executableFilePath) {
        return quoteExeFilePath(executableFilePath, false);
    }
    
    /**
     * Colors and formats a quoted executable file path to be displayed on the console.
     *
     * @param executable  The executable.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteExeFilePath(ExecutableUtils.Executable executable, boolean doubleQuote) {
        return quoted(exeFilePath(executable), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted executable file path to be displayed on the console.
     *
     * @param executable The executable.
     * @return The prepared console output.
     */
    public static String quoteExeFilePath(ExecutableUtils.Executable executable) {
        return quoteExeFilePath(executable, false);
    }
    
    /**
     * Colors and formats an executable file name to be displayed on the console.
     *
     * @param executableFileName The executable file name.
     * @return The prepared console output.
     */
    public static String exeFileName(String executableFileName) {
        return exe(executableFileName);
    }
    
    /**
     * Colors and formats an executable file name to be displayed on the console.
     *
     * @param executable The executable.
     * @return The prepared console output.
     */
    public static String exeFileName(ExecutableUtils.Executable executable) {
        return exeFileName(executable.getExe().getName());
    }
    
    /**
     * Colors and formats a quoted executable file name to be displayed on the console.
     *
     * @param executableFileName The executable file name.
     * @param doubleQuote        Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteExeFileName(String executableFileName, boolean doubleQuote) {
        return quoted(exeFileName(executableFileName), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted executable file name to be displayed on the console.
     *
     * @param executableFileName The executable file name.
     * @return The prepared console output.
     */
    public static String quoteExeFileName(String executableFileName) {
        return quoteExeFileName(executableFileName, false);
    }
    
    /**
     * Colors and formats a quoted executable file name to be displayed on the console.
     *
     * @param executable  The executable.
     * @param doubleQuote Whether to use double quotes.
     * @return The prepared console output.
     */
    public static String quoteExeFileName(ExecutableUtils.Executable executable, boolean doubleQuote) {
        return quoted(exeFileName(executable), doubleQuote);
    }
    
    /**
     * Colors and formats a quoted executable file name to be displayed on the console.
     *
     * @param executable The executable.
     * @return The prepared console output.
     */
    public static String quoteExeFileName(ExecutableUtils.Executable executable) {
        return quoteExeFileName(executable, false);
    }
    
    /**
     * Colors and formats a variable based on its data type to be displayed on the console.
     *
     * @param variable The variable.
     * @return The prepared console output.
     */
    public static String formatVariable(Object variable) {
        final String value = String.valueOf(variable);
        
        if (variable == null) {
            return log(value);
            
        } else if (variable instanceof Number) {
            return number(value);
            
        } else if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return good(value);
        } else if (variable instanceof Boolean) {
            return good(Boolean.toString((Boolean) variable));
            
        } else if (value.contains("://")) {
            return quoted(link(value));
        } else if (variable instanceof URL) {
            return quoted(link(((URL) variable).toExternalForm()));
        } else if (variable instanceof URI) {
            return quoted(link(((URI) variable).toASCIIString()));
            
        } else if (value.contains(":/") || value.startsWith("/") || value.endsWith("/") ||
                value.contains(":\\") || value.startsWith("\\") || value.endsWith("\\")) {
            return quoted(file(value));
        } else if (variable instanceof File) {
            return quoted(file(((File) variable).getAbsolutePath()));
        } else if (variable instanceof Path) {
            return quoted(file(((Path) variable).toAbsolutePath().toString()));
            
        } else if (variable instanceof String) {
            return quoted(base(value));
        } else if (variable instanceof StringBuilder) {
            return quoted(base(((StringBuilder) variable).toString()));
        } else if (variable instanceof StringBuffer) {
            return quoted(base(((StringBuffer) variable).toString()));
        } else if (variable instanceof CharSequence) {
            return quoted(base(((CharSequence) variable).toString()));
            
        } else {
            return bad(value);
        }
    }
    
    
    //Inner Classes
    
    /**
     * Holds the color Config.
     */
    public static class Config {
        
        //Constants
        
        /**
         * The default value of the flag indicating whether to enable colors or not.
         */
        public static final boolean DEFAULT_ENABLE_COLORS = true;
        
        /**
         * The default color to use for "base" text.
         */
        public static final String DEFAULT_COLOR_BASE = "GREEN";
        
        /**
         * The default color to use for "good" text.
         */
        public static final String DEFAULT_COLOR_GOOD = "CYAN";
        
        /**
         * The default color to use for "bad" text.
         */
        public static final String DEFAULT_COLOR_BAD = "RED";
        
        /**
         * The default color to use for "log" text.
         */
        public static final String DEFAULT_COLOR_LOG = "DARK_GREY";
        
        /**
         * The default color to use for "channel" text.
         */
        public static final String DEFAULT_COLOR_CHANNEL = "YELLOW";
        
        /**
         * The default color to use for "video" text.
         */
        public static final String DEFAULT_COLOR_VIDEO = "PURPLE";
        
        /**
         * The default color to use for "number" text.
         */
        public static final String DEFAULT_COLOR_NUMBER = "WHITE";
        
        /**
         * The default color to use for "file" text.
         */
        public static final String DEFAULT_COLOR_FILE = "GREY";
        
        /**
         * The default color to use for "exe" text.
         */
        public static final String DEFAULT_COLOR_EXE = "ORANGE";
        
        /**
         * The default color to use for "link" text.
         */
        public static final String DEFAULT_COLOR_LINK = "TEAL";
        
        /**
         * The default color to use for Progress Bar "base" text.
         */
        public static final String DEFAULT_COLOR_PROGRESS_BAR_BASE = ProgressBar.DEFAULT_COLOR_BASE.name();
        
        /**
         * The default color to use for Progress Bar "good" text.
         */
        public static final String DEFAULT_COLOR_PROGRESS_BAR_GOOD = ProgressBar.DEFAULT_COLOR_GOOD.name();
        
        /**
         * The default color to use for Progress Bar "bad" text.
         */
        public static final String DEFAULT_COLOR_PROGRESS_BAR_BAD = ProgressBar.DEFAULT_COLOR_BAD.name();
        
        
        //Static Fields
        
        /**
         * A flag indicating whether to enable colors or not.
         */
        public static boolean enableColors = DEFAULT_ENABLE_COLORS;
        
        /**
         * The color to use for "base" text.
         */
        public static Console.ConsoleEffect base = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_BASE);
        
        /**
         * The color to use for "good" text.
         */
        public static Console.ConsoleEffect good = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_GOOD);
        
        /**
         * The color to use for "bad" text.
         */
        public static Console.ConsoleEffect bad = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_BAD);
        
        /**
         * The color to use for "log" text.
         */
        public static Console.ConsoleEffect log = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_LOG);
        
        /**
         * The color to use for "channel" text.
         */
        public static Console.ConsoleEffect channel = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_CHANNEL);
        
        /**
         * The color to use for "video" text.
         */
        public static Console.ConsoleEffect video = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_VIDEO);
        
        /**
         * The color to use for "number" text.
         */
        public static Console.ConsoleEffect number = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_NUMBER);
        
        /**
         * The color to use for "file" text.
         */
        public static Console.ConsoleEffect file = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_FILE);
        
        /**
         * The color to use for "exe" text.
         */
        public static Console.ConsoleEffect exe = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_EXE);
        
        /**
         * The color to use for "link" text.
         */
        public static Console.ConsoleEffect link = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_LINK);
        
        /**
         * The color to use for Progress Bar "base" text.
         */
        public static Console.ConsoleEffect progressBarBase = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_PROGRESS_BAR_BASE);
        
        /**
         * The color to use for Progress Bar "good" text.
         */
        public static Console.ConsoleEffect progressBarGood = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_PROGRESS_BAR_GOOD);
        
        /**
         * The color to use for Progress Bar "bad" text.
         */
        public static Console.ConsoleEffect progressBarBad = Console.ConsoleEffect.valueOf(DEFAULT_COLOR_PROGRESS_BAR_BAD);
        
        
        //Static Functions
        
        /**
         * A function that loads a color setting configuration.
         */
        private static final BiFunction<String, String, Console.ConsoleEffect> colorSettingLoader = (String subKey, String def) -> {
            final String confColor = !loaded.get() ? def :
                                     Configurator.getSetting(Configurator.ConfigSection.COLOR.getSettingKey(subKey), def);
            String color = confColor.matches("(?i)DEFAULT(?:.COLOR)?") ? def :
                           confColor.toUpperCase().replace(" ", "_");
            
            if (!AVAILABLE_COLORS.contains(color)) {
                logger.warn(apply(Console.ConsoleEffect.RED, (confColor + " is not a valid color")));
                color = def;
            }
            return Console.ConsoleEffect.valueOf(color);
        };
        
        
        //Static Methods
        
        /**
         * Initializes the Config.
         */
        private static void init() {
            enableColors = Configurator.getSetting(List.of(
                            "enableColors",
                            "color.enableColors",
                            "color.enable"),
                    DEFAULT_ENABLE_COLORS);
            
            base = colorSettingLoader.apply("base", DEFAULT_COLOR_BASE);
            good = colorSettingLoader.apply("good", DEFAULT_COLOR_GOOD);
            bad = colorSettingLoader.apply("bad", DEFAULT_COLOR_BAD);
            log = colorSettingLoader.apply("log", DEFAULT_COLOR_LOG);
            channel = colorSettingLoader.apply("channel", DEFAULT_COLOR_CHANNEL);
            video = colorSettingLoader.apply("video", DEFAULT_COLOR_VIDEO);
            number = colorSettingLoader.apply("number", DEFAULT_COLOR_NUMBER);
            file = colorSettingLoader.apply("file", DEFAULT_COLOR_FILE);
            exe = colorSettingLoader.apply("exe", DEFAULT_COLOR_EXE);
            link = colorSettingLoader.apply("link", DEFAULT_COLOR_LINK);
            
            progressBarBase = colorSettingLoader.apply("progressBar.base", DEFAULT_COLOR_PROGRESS_BAR_BASE);
            progressBarGood = colorSettingLoader.apply("progressBar.good", DEFAULT_COLOR_PROGRESS_BAR_GOOD);
            progressBarBad = colorSettingLoader.apply("progressBar.bad", DEFAULT_COLOR_PROGRESS_BAR_BAD);
        }
        
    }
    
}
