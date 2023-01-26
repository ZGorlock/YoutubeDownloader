/*
 * File:    YoutubeChannelDownloader.java
 * Package: youtube
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube;

import java.io.File;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.channel.config.ChannelConfig;
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
    
    
    //Main Method
    
    /**
     * The main method for the Youtube Channel Downloader.
     *
     * @param args The arguments to the main method.
     * @throws Exception When there is an error.
     */
    public static void main(String[] args) throws Exception {
        if (!Utils.startup(Utils.Project.YOUTUBE_CHANNEL_DOWNLOADER)) {
            return;
        }
        
        run();
    }
    
    
    //Methods
    
    /**
     * Processes Channels.
     *
     * @throws Exception When there is an error.
     */
    private static void run() throws Exception {
        Channels.loadChannels();
        KeyStore.load();
        
        if (Configurator.Config.channel == null) {
            boolean skip = (Configurator.Config.startAt != null);
            boolean stop = (Configurator.Config.stopAt != null);
            
            if (!((skip && stop) && (Channels.configIndex(Configurator.Config.stopAt) < Channels.configIndex(Configurator.Config.startAt)))) {
                for (ChannelConfig config : Channels.getConfigs()) {
                    if (!(skip &= !config.getKey().equals(Configurator.Config.startAt)) && config.isMemberOfGroup(Configurator.Config.group)) {
                        processChannel(config.getKey());
                        if (stop && config.getKey().equals(Configurator.Config.stopAt)) {
                            break;
                        }
                    }
                }
            }
            
        } else {
            processChannel(Configurator.Config.channel);
        }
        
        KeyStore.save();
        Stats.print();
    }
    
    /**
     * Processes a Channel.
     *
     * @param channelKey The key of the Channel.
     * @return Whether the Channel was successfully processed or not.
     * @throws Exception When there is an error.
     */
    private static boolean processChannel(String channelKey) throws Exception {
        channel = Channels.getChannel(channelKey);
        return processChannel();
    }
    
    /**
     * Processes the active Channel.
     *
     * @return Whether the Channel was successfully processed or not.
     * @throws Exception When there is an error.
     */
    private static boolean processChannel() throws Exception {
        if ((channel == null) || !channel.getConfig().isActive()) {
            return false;
        }
        
        System.out.println(Utils.NEWLINE);
        System.out.println(Color.base("Processing Channel: ") + Color.channel(channel.getConfig().getDisplayName()));
        
        boolean success = WebUtils.isOnline() &&
                initChannel() &&
                loadChannelData() &&
                produceQueue() &&
                downloadVideos() &&
                createPlaylist() &&
                cleanChannel();
        
        Stats.totalChannelsProcessed.incrementAndGet();
        return success;
    }
    
    /**
     * Initializes the active Channel.
     *
     * @return Whether the Channel was successfully initialized or not.
     * @throws Exception When there is an error.
     */
    private static boolean initChannel() throws Exception {
        try {
            videoMap.clear();
            
            if (!Configurator.Config.preventChannelFetch) {
                channel.getState().cleanupData();
            }
            channel.getInfo();
            
            ApiUtils.clearCache();
            WebUtils.checkPlaylistId(channel.getConfig());
            
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * Loads the data for the active Channel.
     *
     * @return Whether the Channel data was successfully loaded or not.
     */
    private static boolean loadChannelData() throws Exception {
        try {
            final Set<String> videoTitles = new HashSet<>();
            ApiUtils.fetchPlaylistVideos(channel).stream()
                    .filter(Objects::nonNull).filter(VideoInfo::isValid)
                    .map(videoInfo -> new Video(videoInfo, channel))
                    .filter(video -> videoTitles.add(video.getTitle()))
                    .forEach(video -> videoMap.put(video.getInfo().getVideoId(), video));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * Produces the queue of videos to download from the active Channel.
     *
     * @return Whether the queue was successfully produced or not.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean produceQueue() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println(Color.bad("Must populate video map before producing the queue"));
            return false;
        }
        
        channel.getState().getQueued().clear();
        if (Configurator.Config.retryPreviousFailures) {
            channel.getState().getBlocked().clear();
        }
        
        ChannelProcesses.performSpecialPreConditions(channel, videoMap);
        
        videoMap.values().stream().collect(Collectors.groupingBy(Video::getTitle)).entrySet()
                .stream().filter(e -> (e.getValue().size() > 1)).forEach(e ->
                        System.out.println(Color.bad("The title: ") + Color.videoName(e.getValue().get(0).getTitle()) + Color.bad(" appears ") + Color.number(e.getValue().size()) + Color.bad(" times")));
        
        videoMap.forEach((videoId, video) -> {
            channel.getState().getSaved().remove(videoId);
            
            if (video.getOutput().exists() && FileUtils.getCanonicalFile(video.getOutput()).getAbsolutePath().equals(video.getOutput().getAbsolutePath())) {
                channel.getState().getSaved().add(videoId);
                channel.getState().getBlocked().remove(videoId);
                channel.getState().getKeyStore().put(videoId, PathUtils.localPath(video.getOutput()));
                
            } else if (!channel.getState().getBlocked().contains(videoId)) {
                File oldOutput = Optional.ofNullable(channel.getState().getKeyStore().get(videoId))
                        .map(File::new).filter(File::exists)
                        .map(FileUtils::getCanonicalFile).filter(File::exists)
                        .orElseGet(() -> Utils.findVideoFile(video.getOutput()));
                
                if ((oldOutput == null) || !oldOutput.exists()) {
                    channel.getState().getQueued().add(videoId);
                    
                } else {
                    File newOutput = new File(video.getConfig().getOutputFolder(), video.getOutput().getName()
                            .replace(('.' + FileUtils.getFileFormat(video.getOutput().getName())), ('.' + FileUtils.getFileFormat(oldOutput.getName()))));
                    
                    if (oldOutput.getName().equals(newOutput.getName())) {
                        video.updateOutput(newOutput);
                        
                    } else if (!Configurator.Config.preventRenaming) {
                        System.out.println(Color.base("Renaming: ") + Color.videoRename(oldOutput.getName(), newOutput.getName()));
                        
                        oldOutput.renameTo(newOutput);
                        video.updateOutput(newOutput);
                        
                        if (channel.getConfig().isSaveAsMp3()) {
                            Stats.totalAudioRenames.incrementAndGet();
                        } else {
                            Stats.totalVideoRenames.incrementAndGet();
                        }
                        
                    } else {
                        System.out.println(Color.bad("Would have renamed: ") + Color.videoRename(oldOutput.getName(), newOutput.getName()) + Color.bad(" but renaming is disabled"));
                        video.updateOutput(oldOutput);
                    }
                    
                    channel.getState().getSaved().add(videoId);
                    channel.getState().getKeyStore().replace(videoId, PathUtils.localPath(video.getOutput()));
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
     * @throws Exception When there is an error.
     */
    private static boolean downloadVideos() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println(Color.bad("Must populate video map before downloading videos"));
            return false;
        }
        
        if (!channel.getState().getQueued().isEmpty()) {
            System.out.println(Color.number(String.valueOf(channel.getState().getQueued().size())) + Color.base(" in Queue..."));
        }
        
        List<String> working = new ArrayList<>(channel.getState().getQueued());
        for (int i = 0; i < working.size(); i++) {
            String videoId = working.get(i);
            Video video = videoMap.get(videoId);
            
            if (!Configurator.Config.preventDownload) {
                System.out.println(Color.base("Downloading (") + Color.number(i + 1) + Color.base("/") + Color.number(working.size()) + Color.base("): ") + Color.videoName(video.getTitle(), false));
            } else {
                System.out.println(Color.bad("Would have downloaded: ") + Color.videoName(video.getTitle()) + Color.bad(" but downloading is disabled"));
                continue;
            }
            
            DownloadUtils.DownloadResponse response = DownloadUtils.downloadYoutubeVideo(video);
            switch (response.status) {
                case SUCCESS:
                    channel.getState().getSaved().add(videoId);
                    channel.getState().getKeyStore().put(videoId, PathUtils.localPath(video.getOutput()));
                    
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
            System.out.println(Utils.INDENT + response.printedResponse());
            
            channel.getState().getQueued().remove(videoId);
            channel.getState().save();
        }
        return true;
    }
    
    /**
     * Creates a playlist of the videos from the active Channel.
     *
     * @return Whether the playlist was successfully created or not.
     * @throws Exception When there is an error.
     */
    private static boolean createPlaylist() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println(Color.bad("Must populate video map before creating a playlist"));
            return false;
        }
        
        if (channel.getConfig().getPlaylistFile() == null) {
            return false;
        }
        List<String> existingPlaylist = FileUtils.readLines(channel.getConfig().getPlaylistFile());
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
                System.out.println(Color.base("Updating playlist: ") + Color.filePath(channel.getConfig().getPlaylistFile()));
                FileUtils.writeLines(channel.getConfig().getPlaylistFile(), playlist);
            } else {
                System.out.println(Color.bad("Would have updated playlist: ") + Color.filePath(channel.getConfig().getPlaylistFile()) + Color.bad(" but playlist modification is disabled"));
            }
        }
        return true;
    }
    
    /**
     * Cleans the output directory of the active Channel.
     *
     * @return Whether the output directory was successfully cleaned or not.
     * @throws Exception When there is an error.
     */
    private static boolean cleanChannel() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println(Color.bad("Must populate video map before cleaning the channel directory"));
            return false;
        }
        
        List<String> saved = Channels.getChannels().stream()
                .filter(e -> e.getConfig().getKey().matches(channel.getConfig().getKey() + "(?:_P\\d+)?"))
                .flatMap(e -> e.getState().getSaved().stream().map(save -> e.getState().getKeyStore().get(save)))
                .map(PathUtils::localPath)
                .distinct().collect(Collectors.toList());
        
        if (!channel.getState().getErrorFlag().get() && channel.getConfig().isKeepClean()) {
            
            List<File> videos = FileUtils.getFiles(channel.getConfig().getOutputFolder());
            for (File video : videos) {
                if (video.isFile() && !saved.contains(PathUtils.localPath(video))) {
                    boolean isPartFile = video.getName().endsWith('.' + Utils.DOWNLOAD_FILE_FORMAT);
                    String printedFile = Color.apply((isPartFile ? Color.FILE : Color.VIDEO), video.getName());
                    
                    if (!Configurator.Config.preventDeletion) {
                        System.out.println(Color.base("Deleting: ") + Color.quoted(printedFile));
                        FileUtils.deleteFile(video);
                        
                        if (!isPartFile) {
                            if (channel.getConfig().isSaveAsMp3()) {
                                Stats.totalAudioDeletions.incrementAndGet();
                            } else {
                                Stats.totalVideoDeletions.incrementAndGet();
                            }
                        }
                        
                    } else {
                        System.out.println(Color.bad("Would have deleted: ") + Color.quoted(printedFile) + Color.bad(" but deletion is disabled"));
                    }
                }
            }
        }
        return true;
    }
    
}
