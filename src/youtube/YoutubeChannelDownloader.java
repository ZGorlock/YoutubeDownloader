/*
 * File:    YoutubeChannelDownloader.java
 * Package: youtube
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import commons.access.Desktop;
import commons.access.Filesystem;
import commons.access.Internet;
import commons.time.DateTimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.channel.process.ChannelProcesses;
import youtube.config.Color;
import youtube.config.Configurator;
import youtube.entity.Channel;
import youtube.entity.Video;
import youtube.entity.info.VideoInfo;
import youtube.state.KeyStore;
import youtube.state.Stats;
import youtube.util.ApiUtils;
import youtube.util.DownloadUtils;
import youtube.util.FileUtils;
import youtube.util.LogUtils;
import youtube.util.PathUtils;
import youtube.util.Utils;
import youtube.util.WebUtils;

/**
 * Downloads Youtube Channels and Playlists.
 */
public class YoutubeChannelDownloader {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(YoutubeChannelDownloader.class);
    
    
    //Static Fields
    
    /**
     * The current Channel being processed.
     */
    public static Channel channel = null;
    
    /**
     * The video map for the Channel being processed.
     */
    private static final Map<String, Video> videoMap = new LinkedHashMap<>();
    
    /**
     * The program start time.
     */
    private static final long startTime = System.currentTimeMillis();
    
    
    //Main Method
    
    /**
     * The main method for the Youtube Channel Downloader.
     *
     * @param args The arguments to the main method.
     */
    public static void main(String[] args) {
        logger.info(Color.number("--------------------------"));
        logger.info(Color.number("Youtube Channel Downloader"));
        logger.info(Color.number("--------------------------"));
        
        if (!Utils.startup(Configurator.Program.YOUTUBE_CHANNEL_DOWNLOADER)) {
            return;
        }
        
        if (!Configurator.Config.preventRun) {
            run();
        }
    }
    
    
    //Methods
    
    /**
     * Processes Channels.
     */
    private static void run() {
        logger.trace(LogUtils.NEWLINE);
        logger.debug(Color.log("Starting..."));
        
        Channels.initChannels();
        KeyStore.initKeystore();
        
        logger.trace(LogUtils.NEWLINE);
        Channels.getFiltered().forEach(YoutubeChannelDownloader::processChannel);
        logger.trace(LogUtils.NEWLINE);
        
        KeyStore.saveKeyStore();
        Stats.print();
        
        if (LogUtils.Config.printExecutionTime) {
            logger.trace(LogUtils.NEWLINE);
            logger.debug(Color.log("Completed in ") + Color.number(DateTimeUtility.durationToDurationString(
                    (System.currentTimeMillis() - startTime), false, false, true)));
        }
    }
    
    /**
     * Processes a Channel.
     *
     * @param channelKey The key of the Channel.
     * @return Whether the Channel was successfully processed or not.
     */
    private static boolean processChannel(String channelKey) {
        channel = Channels.getChannel(channelKey);
        return processChannel();
    }
    
    /**
     * Processes the active Channel.
     *
     * @return Whether the Channel was successfully processed or not.
     */
    private static boolean processChannel() {
        if ((channel == null) || !channel.getConfig().isActive()) {
            return false;
        }
        
        logger.trace(LogUtils.NEWLINE);
        if (!Configurator.Config.preventProcess) {
            logger.info(Color.base("Processing Channel: ") + Color.channelDisplayName(channel));
        } else {
            logger.info(Color.bad("Would have processed Channel: ") + Color.channelDisplayName(channel) + Color.bad(" but processing is disabled"));
            logger.trace(LogUtils.NEWLINE);
            return false;
        }
        
        boolean success = Internet.isOnline() &&
                initChannel() &&
                loadChannelData() &&
                produceQueue() &&
                downloadVideos() &&
                createPlaylist() &&
                cleanChannel();
        
        Stats.totalChannelsProcessed.incrementAndGet();
        
        logger.trace(LogUtils.NEWLINE);
        return success;
    }
    
    /**
     * Initializes the active Channel.
     *
     * @return Whether the Channel was successfully initialized or not.
     */
    private static boolean initChannel() {
        try {
            videoMap.clear();
            
            if (!Configurator.Config.preventChannelFetch) {
                channel.getState().cleanupData();
            }
            
            WebUtils.checkPlaylistId(channel.getConfig());
            channel.getInfo();
            
        } catch (Exception e) {
            logger.error(Color.bad("Failed to initialize Channel: ") + Color.channelName(channel) + Color.bad(" for processing"), e);
            return false;
        }
        return true;
    }
    
    /**
     * Loads the data for the active Channel.
     *
     * @return Whether the Channel data was successfully loaded or not.
     */
    private static boolean loadChannelData() {
        try {
            final Set<String> videoTitles = new HashSet<>();
            ApiUtils.fetchChannelVideos(channel).stream()
                    .filter(Objects::nonNull).filter(VideoInfo::isValid)
                    .map(videoInfo -> new Video(videoInfo, channel))
                    .filter(video -> videoTitles.add(video.getTitle()))
                    .forEach(video -> videoMap.put(video.getInfo().getVideoId(), video));
        } catch (Exception e) {
            logger.error(Color.bad("Failed to load the data of Channel: ") + Color.channelName(channel), e);
            return false;
        }
        return true;
    }
    
    /**
     * Produces the queue of videos to download from the active Channel.
     *
     * @return Whether the queue was successfully produced or not.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean produceQueue() {
        if (videoMap.isEmpty()) {
            logger.warn(Color.bad("Must populate video map before producing the queue"));
            return false;
        }
        
        channel.getState().getQueued().clear();
        if (Configurator.Config.retryPreviousFailures) {
            channel.getState().getBlocked().clear();
        }
        
        ChannelProcesses.performSpecialPreConditions(channel, videoMap);
        
        videoMap.values().stream().collect(Collectors.groupingBy(Video::getTitle)).entrySet()
                .stream().filter(e -> (e.getValue().size() > 1)).forEach(e ->
                        logger.warn(Color.bad("The title: ") + Color.quoteVideoTitle(e.getValue().get(0)) + Color.bad(" appears ") + Color.number(e.getValue().size()) + Color.bad(" times")));
        
        videoMap.forEach((videoId, video) -> {
            channel.getState().getSaved().remove(videoId);
            
            if (video.getOutput().exists() && FileUtils.getCanonical(video.getOutput()).getAbsolutePath().equals(video.getOutput().getAbsolutePath())) {
                channel.getState().getSaved().add(videoId);
                channel.getState().getBlocked().remove(videoId);
                channel.getState().getKeyStore().put(video);
                
            } else if (!channel.getState().getBlocked().contains(videoId)) {
                File oldOutput = Optional.ofNullable(channel.getState().getKeyStore().get(videoId))
                        .map(KeyStore.KeyStoreEntry::getLocalPath)
                        .map(File::new).filter(File::exists)
                        .map(FileUtils::getCanonical).filter(File::exists)
                        .orElseGet(() -> FileUtils.findVideoFile(video.getOutput()));
                
                if ((oldOutput == null) || !oldOutput.exists()) {
                    channel.getState().getQueued().add(videoId);
                    
                } else {
                    File newOutput = Optional.ofNullable(video.getOutput()).map(File::getName)
                            .map(e -> FileUtils.setFormat(e, FileUtils.getFormat(oldOutput.getName())))
                            .map(e -> new File(video.getConfig().getOutputFolder(), e))
                            .orElse(oldOutput);
                    
                    if (oldOutput.getName().equals(newOutput.getName())) {
                        video.updateOutput(newOutput);
                        
                    } else if (!Configurator.Config.preventRenaming) {
                        logger.info(Color.base("Renaming: ") + Color.quoteVideoFileName(oldOutput) + Color.log(" to: ") + Color.quoteVideoFileName(newOutput));
                        
                        oldOutput.renameTo(newOutput);
                        video.updateOutput(newOutput);
                        
                        if (channel.getConfig().isSaveAsMp3()) {
                            Stats.totalAudioRenames.incrementAndGet();
                        } else {
                            Stats.totalVideoRenames.incrementAndGet();
                        }
                        
                    } else {
                        logger.info(Color.bad("Would have renamed: ") + Color.quoteVideoFileName(oldOutput) + Color.log(" to: ") + Color.quoteVideoFileName(newOutput) + Color.bad(" but renaming is disabled"));
                        video.updateOutput(oldOutput);
                    }
                    
                    channel.getState().getSaved().add(videoId);
                    channel.getState().getKeyStore().put(video);
                }
            }
        });
        
        ChannelProcesses.performSpecialPostConditions(channel, videoMap);
        
        channel.getState().save();
        return true;
    }
    
    /**
     * Downloads the queued videos from the active Channel.
     *
     * @return Whether the queued videos were successfully downloaded or not.
     */
    private static boolean downloadVideos() {
        if (videoMap.isEmpty()) {
            logger.warn(Color.bad("Must populate video map before downloading videos"));
            return false;
        }
        
        if (!channel.getState().getQueued().isEmpty()) {
            logger.info(Color.number(String.valueOf(channel.getState().getQueued().size())) + Color.base(" in Queue..."));
        }
        
        List<String> working = new ArrayList<>(channel.getState().getQueued());
        for (int i = 0; i < working.size(); i++) {
            String videoId = working.get(i);
            Video video = videoMap.get(videoId);
            
            if (!Configurator.Config.preventDownload) {
                logger.info(Color.base("Downloading (") + Color.number(i + 1) + Color.base("/") + Color.number(working.size()) + Color.base("): ") + Color.videoTitle(video));
            } else {
                logger.info(Color.bad("Would have downloaded: ") + Color.quoteVideoTitle(video) + Color.bad(" but downloading is disabled"));
                continue;
            }
            
            DownloadUtils.DownloadResponse response = DownloadUtils.downloadYoutubeVideo(video);
            switch (response.getStatus()) {
                case SUCCESS:
                    channel.getState().getSaved().add(videoId);
                    channel.getState().getKeyStore().put(video);
                    
                    if (channel.getConfig().isSaveAsMp3()) {
                        Stats.totalAudioDownloads.incrementAndGet();
                        Stats.totalAudioDataDownloaded.addAndGet(video.getOutput().length());
                    } else {
                        Stats.totalVideoDownloads.incrementAndGet();
                        Stats.totalVideoDataDownloaded.addAndGet(video.getOutput().length());
                    }
                    break;
                
                case ERROR:
                    channel.getState().getBlocked().add(videoId);
                case FAILURE:
                    if (channel.getConfig().isSaveAsMp3()) {
                        Stats.totalAudioDownloadFailures.incrementAndGet();
                    } else {
                        Stats.totalVideoDownloadFailures.incrementAndGet();
                    }
                    break;
            }
            
            channel.getState().getQueued().remove(videoId);
            channel.getState().save();
        }
        return true;
    }
    
    /**
     * Creates a playlist of the videos from the active Channel.
     *
     * @return Whether the playlist was successfully created or not.
     */
    private static boolean createPlaylist() {
        if (videoMap.isEmpty()) {
            logger.warn(Color.bad("Must populate video map before creating a playlist"));
            return false;
        }
        
        if (channel.getConfig().getPlaylistFile() == null) {
            return true;
        }
        
        final List<String> existingPlaylist = new ArrayList<>();
        if (channel.getConfig().getPlaylistFile().exists()) {
            try {
                Optional.of(channel.getConfig().getPlaylistFile())
                        .map(Filesystem::readLines)
                        .map(existingPlaylist::addAll)
                        .orElseThrow(() -> new IOException("Error reading: " + PathUtils.path(channel.getConfig().getPlaylistFile())));
            } catch (Exception e) {
                logger.error(Color.bad("Failed to load existing playlist: ") + Color.quoteFilePath(channel.getConfig().getPlaylistFile()), e);
                return false;
            }
        }
        
        String playlistPath = PathUtils.localPath(true, channel.getConfig().getPlaylistFile().getParentFile());
        
        List<String> playlist = new ArrayList<>();
        for (Map.Entry<String, Video> video : videoMap.entrySet()) {
            if (channel.getState().getSaved().contains(video.getKey())) {
                playlist.add(PathUtils.localPath(video.getValue().getOutput()).replace(playlistPath, ""));
            }
        }
        
        if (channel.getConfig().isYoutubeChannel() ^ channel.getConfig().isReversePlaylist()) {
            Collections.reverse(playlist);
        }
        
        if (!channel.getState().getErrorFlag().get() && !playlist.equals(existingPlaylist)) {
            if (!Configurator.Config.preventPlaylistEdit) {
                logger.info(Color.base("Updating playlist: ") + Color.quoteFilePath(channel.getConfig().getPlaylistFile()));
                try {
                    Optional.of(channel.getConfig().getPlaylistFile())
                            .filter(file -> Filesystem.writeLines(file, playlist))
                            .orElseThrow(() -> new IOException("Error writing: " + PathUtils.path(channel.getConfig().getPlaylistFile())));
                } catch (IOException e) {
                    logger.error(Color.bad("Failed to update playlist: ") + Color.quoteFilePath(channel.getConfig().getPlaylistFile()), e);
                    return false;
                }
            } else {
                logger.info(Color.bad("Would have updated playlist: ") + Color.quoteFilePath(channel.getConfig().getPlaylistFile()) + Color.bad(" but playlist modification is disabled"));
            }
        }
        return true;
    }
    
    /**
     * Cleans the output directory of the active Channel.
     *
     * @return Whether the output directory was successfully cleaned or not.
     */
    private static boolean cleanChannel() {
        if (videoMap.isEmpty()) {
            logger.warn(Color.bad("Must populate video map before cleaning the channel directory"));
            return false;
        }
        
        List<String> saved = Channels.getChannels().stream()
                .filter(e -> e.getConfig().getKey().matches(channel.getConfig().getKey() + "(?:_P\\d+)?"))
                .flatMap(e -> e.getState().getSaved().stream()
                        .map(save -> e.getState().getKeyStore().get(save)))
                .filter(Objects::nonNull)
                .map(KeyStore.KeyStoreEntry::getLocalPath)
                .filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        
        if (!channel.getState().getErrorFlag().get() && channel.getConfig().isKeepClean()) {
            
            List<File> channelFiles = Filesystem.getFiles(channel.getConfig().getOutputFolder());
            for (File channelFile : channelFiles) {
                if (channelFile.isFile() && !saved.contains(PathUtils.localPath(channelFile))) {
                    
                    if (!Configurator.Config.preventDeletion) {
                        logger.info(Color.base("Deleting: ") + Color.quoteVideoFileName(channelFile));
                        try {
                            Optional.of(channelFile)
                                    .filter(file -> Configurator.Config.deleteToRecyclingBin ?
                                                    Desktop.trash(file) :
                                                    Filesystem.deleteFile(file))
                                    .orElseThrow(() -> new IOException("Error deleting: " + PathUtils.path(channelFile)));
                        } catch (Exception e) {
                            logger.error(Color.bad("Failed to delete: ") + Color.quoteVideoFileName(channelFile), e);
                        }
                        
                        if (!FileUtils.isFormat(channelFile.getName(), FileUtils.DOWNLOAD_FILE_FORMAT)) {
                            if (channel.getConfig().isSaveAsMp3()) {
                                Stats.totalAudioDeletions.incrementAndGet();
                            } else {
                                Stats.totalVideoDeletions.incrementAndGet();
                            }
                        }
                        
                    } else {
                        logger.info(Color.bad("Would have deleted: ") + Color.quoteVideoFileName(channelFile) + Color.bad(" but deletion is disabled"));
                    }
                }
            }
        }
        return true;
    }
    
}
