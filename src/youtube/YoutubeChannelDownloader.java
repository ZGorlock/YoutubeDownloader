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
import youtube.channel.ChannelConfig;
import youtube.channel.Channels;
import youtube.channel.process.ChannelProcesses;
import youtube.config.Color;
import youtube.config.Configurator;
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
    public static ChannelConfig channel = null;
    
    /**
     * The video map for the Channel being processed.
     */
    private static final Map<String, VideoInfo> videoMap = new LinkedHashMap<>();
    
    
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
     * Processes all Channels.
     *
     * @throws Exception When there is an error.
     */
    private static void run() throws Exception {
        Channels.loadChannels();
        KeyStore.load();
        
        if (Configurator.Config.channel == null) {
            boolean skip = (Configurator.Config.startAt != null);
            boolean stop = (Configurator.Config.stopAt != null);
            
            if (!((skip && stop) && (Channels.channelIndex(Configurator.Config.stopAt) < Channels.channelIndex(Configurator.Config.startAt)))) {
                for (ChannelConfig currentChannel : Channels.getChannels()) {
                    if (!(skip &= !currentChannel.getKey().equals(Configurator.Config.startAt)) && currentChannel.isMemberOfGroup(Configurator.Config.group)) {
                        setChannel(currentChannel);
                        processChannel();
                        if (stop && currentChannel.getKey().equals(Configurator.Config.stopAt)) {
                            break;
                        }
                    }
                }
            }
            
        } else {
            setChannel(Configurator.Config.channel);
            processChannel();
        }
        
        KeyStore.save();
        Stats.print();
    }
    
    /**
     * Sets the active Channel.
     *
     * @param newChannel The Channel to be processed.
     * @throws Exception When there is an error.
     */
    private static void setChannel(ChannelConfig newChannel) throws Exception {
        channel = newChannel;
        if (channel != null) {
            channel.state.load();
        }
    }
    
    /**
     * Sets the active Channel.
     *
     * @param newChannelKey The key of the Channel to be processed.
     * @throws Exception When there is an error.
     */
    private static void setChannel(String newChannelKey) throws Exception {
        setChannel(Channels.getChannel(newChannelKey));
    }
    
    /**
     * Processes the active Channel.
     *
     * @return Whether the Channel was successfully processed or not.
     * @throws Exception When there is an error.
     */
    private static boolean processChannel() throws Exception {
        if ((channel == null) || !channel.isActive()) {
            return false;
        }
        
        System.out.println(Utils.NEWLINE);
        System.out.println(Color.base("Processing Channel: ") + Color.channel(channel.getDisplayName()));
        
        boolean success = WebUtils.isOnline() &&
                loadChannelData() &&
                produceQueue() &&
                downloadVideos() &&
                createPlaylist() &&
                cleanChannel();
        
        Stats.totalChannelsProcessed.incrementAndGet();
        return success;
    }
    
    /**
     * Loads the data for the active Channel.
     *
     * @return Whether the Channel data was successfully loaded or not.
     * @throws Exception When there is an error.
     */
    private static boolean loadChannelData() throws Exception {
        videoMap.clear();
        if (!Configurator.Config.preventChannelFetch) {
            channel.state.cleanupData();
        }
        ApiUtils.clearCache();
        
        try {
            WebUtils.checkPlaylistId(channel);
            
            final Set<String> videoTitles = new HashSet<>();
            ApiUtils.fetchPlaylistVideos(channel).stream()
                    .filter(Objects::nonNull).filter(VideoInfo::isValid)
                    .filter(video -> videoTitles.add(video.title))
                    .forEach(video -> videoMap.put(video.videoId, video));
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
        
        channel.state.queued.clear();
        if (Configurator.Config.retryPreviousFailures) {
            channel.state.blocked.clear();
        }
        
        ChannelProcesses.performSpecialPreConditions(channel, videoMap);
        
        videoMap.values().stream().collect(Collectors.groupingBy(e -> e.title)).entrySet()
                .stream().filter(e -> (e.getValue().size() > 1)).forEach(e ->
                        System.out.println(Color.bad("The title: ") + Color.videoName(e.getValue().get(0).title) + Color.bad(" appears ") + Color.number(e.getValue().size()) + Color.bad(" times")));
        
        videoMap.forEach((videoId, video) -> {
            channel.state.saved.remove(videoId);
            
            if (video.output.exists()) {
                channel.state.saved.add(videoId);
                channel.state.blocked.remove(videoId);
                channel.state.keyStore.put(videoId, PathUtils.localPath(video.output));
                
            } else if (!channel.state.blocked.contains(videoId)) {
                File oldOutput = Optional.ofNullable(channel.state.keyStore.get(videoId))
                        .map(File::new)
                        .filter(File::exists)
                        .orElseGet(() -> Utils.findVideoFile(video.output));
                
                if ((oldOutput == null) || !oldOutput.exists()) {
                    channel.state.queued.add(videoId);
                    
                } else {
                    File newOutput = new File(video.channel.getOutputFolder(), video.output.getName()
                            .replace(("." + Utils.getFileFormat(video.output.getName())), ("." + Utils.getFileFormat(oldOutput.getName()))));
                    
                    if (oldOutput.getName().equals(newOutput.getName())) {
                        video.output = newOutput;
                        
                    } else if (!Configurator.Config.preventRenaming) {
                        System.out.println(Color.base("Renaming: ") + Color.videoRename(oldOutput.getName(), newOutput.getName()));
                        
                        oldOutput.renameTo(newOutput);
                        video.updateOutput(newOutput);
                        
                        if (channel.isSaveAsMp3()) {
                            Stats.totalAudioRenames.incrementAndGet();
                        } else {
                            Stats.totalVideoRenames.incrementAndGet();
                        }
                        
                    } else {
                        System.out.println(Color.bad("Would have renamed: ") + Color.videoRename(oldOutput.getName(), newOutput.getName()) + Color.bad(" but renaming is disabled"));
                        video.updateOutput(oldOutput);
                    }
                    
                    channel.state.saved.add(videoId);
                    channel.state.keyStore.replace(videoId, PathUtils.localPath(video.output));
                }
            }
        });
        
        ChannelProcesses.performSpecialPostConditions(channel, videoMap);
        
        channel.state.save();
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
        
        if (!channel.state.queued.isEmpty()) {
            System.out.println(Color.number(String.valueOf(channel.state.queued.size())) + Color.base(" in Queue..."));
        }
        
        List<String> working = new ArrayList<>(channel.state.queued);
        for (int i = 0; i < working.size(); i++) {
            String videoId = working.get(i);
            VideoInfo video = videoMap.get(videoId);
            
            if (!Configurator.Config.preventDownload) {
                System.out.println(Color.base("Downloading (") + Color.number(i + 1) + Color.base("/") + Color.number(working.size()) + Color.base("): ") + Color.videoName(video.title, false));
            } else {
                System.out.println(Color.bad("Would have downloaded: ") + Color.videoName(video.title) + Color.bad(" but downloading is disabled"));
                continue;
            }
            
            DownloadUtils.DownloadResponse response = DownloadUtils.downloadYoutubeVideo(video);
            switch (response.status) {
                case SUCCESS:
                    channel.state.saved.add(videoId);
                    channel.state.keyStore.put(videoId, PathUtils.localPath(video.output));
                    
                    if (channel.isSaveAsMp3()) {
                        Stats.totalAudioDownloads.incrementAndGet();
                        Stats.totalAudioDataDownloaded.addAndGet(video.output.length());
                    } else {
                        Stats.totalVideoDownloads.incrementAndGet();
                        Stats.totalVideoDataDownloaded.addAndGet(video.output.length());
                    }
                    break;
                
                case ERROR:
                    channel.state.blocked.add(videoId);
                case FAILURE:
                    if (channel.isSaveAsMp3()) {
                        Stats.totalAudioDownloadFailures.incrementAndGet();
                    } else {
                        Stats.totalVideoDownloadFailures.incrementAndGet();
                    }
                    break;
            }
            System.out.println(Utils.INDENT + response.printedResponse());
            
            channel.state.queued.remove(videoId);
            channel.state.save();
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
        
        if (channel.getPlaylistFile() == null) {
            return false;
        }
        List<String> existingPlaylist = FileUtils.readLines(channel.getPlaylistFile());
        String playlistPath = PathUtils.localPath(true, channel.getPlaylistFile().getParentFile());
        
        List<String> playlist = new ArrayList<>();
        for (Map.Entry<String, VideoInfo> video : videoMap.entrySet()) {
            if (channel.state.saved.contains(video.getKey())) {
                playlist.add(PathUtils.localPath(video.getValue().output).replace(playlistPath, ""));
            }
        }
        
        if (channel.isYoutubeChannel() ^ channel.isReversePlaylist()) {
            Collections.reverse(playlist);
        }
        
        if (!channel.error.get() && !playlist.equals(existingPlaylist)) {
            if (!Configurator.Config.preventPlaylistEdit) {
                System.out.println(Color.base("Updating playlist: ") + Color.filePath(channel.getPlaylistFile()));
                FileUtils.writeLines(channel.getPlaylistFile(), playlist);
            } else {
                System.out.println(Color.bad("Would have updated playlist: ") + Color.filePath(channel.getPlaylistFile()) + Color.bad(" but playlist modification is disabled"));
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
                .filter(e -> e.getKey().matches(channel.getKey() + "(?:_P\\d+)?"))
                .flatMap(e -> e.state.saved.stream().map(save -> e.state.keyStore.get(save)))
                .map(PathUtils::localPath)
                .distinct().collect(Collectors.toList());
        
        if (!channel.error.get() && channel.isKeepClean()) {
            
            File[] videos = channel.getOutputFolder().listFiles();
            if (videos != null) {
                for (File video : videos) {
                    if (video.isFile() && !saved.contains(PathUtils.localPath(video))) {
                        boolean isPartFile = video.getName().endsWith(".part");
                        String printedFile = Color.apply((isPartFile ? Color.FILE : Color.VIDEO), video.getName());
                        
                        if (!Configurator.Config.preventDeletion) {
                            System.out.println(Color.base("Deleting: ") + Color.quoted(printedFile));
                            FileUtils.deleteFile(video);
                            
                            if (!isPartFile) {
                                if (channel.isSaveAsMp3()) {
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
        }
        return true;
    }
    
}
