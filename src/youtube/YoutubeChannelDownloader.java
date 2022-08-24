/*
 * File:    YoutubeChannelDownloader.java
 * Package: youtube
 * Author:  Zachary Gill
 */

package youtube;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
import youtube.channel.ChannelProcesses;
import youtube.channel.Channels;
import youtube.channel.KeyStore;
import youtube.channel.Video;
import youtube.util.ApiUtils;
import youtube.util.Color;
import youtube.util.Configurator;
import youtube.util.DownloadUtils;
import youtube.util.Stats;
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
            
            if (!((skip && stop) && (Channels.indexOf(Configurator.Config.stopAt) < Channels.indexOf(Configurator.Config.startAt)))) {
                for (Channel currentChannel : Channels.getChannels()) {
                    if (!(skip &= !currentChannel.key.equals(Configurator.Config.startAt)) && currentChannel.isMemberOfGroup(Configurator.Config.group)) {
                        setChannel(currentChannel);
                        processChannel();
                        if (stop && currentChannel.key.equals(Configurator.Config.stopAt)) {
                            break;
                        }
                    }
                }
            }
            
        } else {
            setChannel(Configurator.Config.channel);
            processChannel();
        }
        
        System.out.println(Utils.NEWLINE);
        KeyStore.save();
        Stats.print();
    }
    
    /**
     * Sets the active Channel.
     *
     * @param newChannel The Channel to be processed.
     * @throws Exception When there is an error.
     */
    private static void setChannel(Channel newChannel) throws Exception {
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
        System.out.println(Color.base("Processing Channel: ") + Color.channel(channel.name));
        
        boolean success = WebUtils.isOnline() &&
                fetchChannelData() &&
                processChannelData() &&
                produceQueue() &&
                downloadVideos() &&
                createPlaylist() &&
                cleanChannel();
        
        Stats.totalChannelsProcessed++;
        return success;
    }
    
    /**
     * Retrieves the data for the active Channel.
     *
     * @return Whether the Channel data was successfully retrieved or not.
     * @throws Exception When there is an error.
     */
    private static boolean fetchChannelData() throws Exception {
        if (Configurator.Config.preventChannelFetch) {
            return !channel.state.getDataFiles().isEmpty();
        }
        channel.state.cleanupData();
        
        try {
            ApiUtils.fetchApiChannelData(channel);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * Processes the data for the active Channel that was retrieved from the Youtube API.
     *
     * @return Whether the Channel data was successfully processed or not.
     * @throws Exception When there is an error.
     */
    private static boolean processChannelData() throws Exception {
        videoMap.clear();
        
        try {
            final Set<String> videoTitles = new HashSet<>();
            ApiUtils.parseApiChannelData(channel).stream()
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
        
        channel.state.queue.clear();
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
                channel.state.keyStore.put(videoId, video.output.getAbsolutePath().replace("/", "\\"));
                
            } else if (!channel.state.blocked.contains(videoId)) {
                File oldOutput = (channel.state.keyStore.containsKey(videoId) && new File(channel.state.keyStore.get(videoId)).exists()) ?
                                 new File(channel.state.keyStore.get(videoId)) : Utils.findVideoFile(video.output);
                
                if ((oldOutput == null) || !oldOutput.exists()) {
                    channel.state.queue.add(videoId);
                    
                } else {
                    File newOutput = new File(video.channel.outputFolder, video.output.getName()
                            .replace(("." + Utils.getFileFormat(video.output.getName())), ("." + Utils.getFileFormat(oldOutput.getName()))));
                    
                    if (!oldOutput.getName().equals(newOutput.getName())) {
                        if (!Configurator.Config.preventRenaming) {
                            System.out.println(Color.base("Renaming: ") + Color.videoRename(oldOutput.getName(), newOutput.getName()));
                            oldOutput.renameTo(newOutput);
                            
                            video.updateOutput(newOutput);
                            channel.state.saved.add(videoId);
                            channel.state.keyStore.replace(videoId, video.output.getAbsolutePath().replace("/", "\\"));
                            
                            if (channel.saveAsMp3) {
                                Stats.totalAudioRenames++;
                            } else {
                                Stats.totalVideoRenames++;
                            }
                            
                        } else {
                            System.out.println(Color.bad("Would have renamed: ") + Color.videoRename(oldOutput.getName(), newOutput.getName()) + Color.bad(" but renaming is disabled"));
                            channel.state.queue.add(videoId);
                        }
                        
                    } else {
                        video.output = newOutput;
                        channel.state.saved.add(videoId);
                        channel.state.keyStore.replace(videoId, video.output.getAbsolutePath().replace("/", "\\"));
                    }
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
        
        if (!channel.state.queue.isEmpty()) {
            System.out.println(Color.number(String.valueOf(channel.state.queue.size())) + Color.base(" in Queue..."));
        }
        
        List<String> working = new ArrayList<>(channel.state.queue);
        for (int i = 0; i < working.size(); i++) {
            String videoId = working.get(i);
            Video video = videoMap.get(videoId);
            
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
                    channel.state.keyStore.put(videoId, video.output.getAbsolutePath().replace("/", "\\"));
                    
                    if (channel.saveAsMp3) {
                        Stats.totalAudioDownloads++;
                        Stats.totalAudioDataDownloaded += video.output.length();
                    } else {
                        Stats.totalVideoDownloads++;
                        Stats.totalVideoDataDownloaded += video.output.length();
                    }
                    break;
                
                case ERROR:
                    channel.state.blocked.add(videoId);
                case FAILURE:
                    if (channel.saveAsMp3) {
                        Stats.totalAudioDownloadFailures++;
                    } else {
                        Stats.totalVideoDownloadFailures++;
                    }
                    break;
            }
            System.out.println(Utils.INDENT + response.printedResponse());
            
            channel.state.queue.remove(videoId);
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
        
        if (channel.playlistFile == null) {
            return false;
        }
        List<String> existingPlaylist = channel.playlistFile.exists() ? FileUtils.readLines(channel.playlistFile, "UTF-8") : new ArrayList<>();
        String playlistPath = channel.playlistFile.getParentFile().getAbsolutePath() + '\\';
        
        List<String> playlist = new ArrayList<>();
        for (Map.Entry<String, Video> video : videoMap.entrySet()) {
            if (channel.state.saved.contains(video.getKey())) {
                playlist.add(video.getValue().output.getAbsolutePath().replace(playlistPath, ""));
            }
        }
        
        if (channel.isChannel()) {
            Collections.reverse(playlist);
        }
        if (channel.reversePlaylist) {
            Collections.reverse(playlist);
        }
        
        if (!channel.error && !playlist.equals(existingPlaylist)) {
            if (!Configurator.Config.preventPlaylistEdit) {
                System.out.println(Color.base("Updating playlist: ") + Color.fileName(channel.playlistFile.getName()));
                FileUtils.writeLines(channel.playlistFile, playlist);
            } else {
                System.out.println(Color.bad("Would have updated playlist: ") + Color.fileName(channel.playlistFile.getName()) + Color.base(" but playlist modification is disabled"));
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
                .filter(e -> e.key.matches(channel.key + "(?:_P\\d+)?"))
                .flatMap(e -> e.state.saved.stream().map(save -> e.state.keyStore.get(save)))
                .distinct().collect(Collectors.toList());
        
        if (!channel.error && channel.keepClean) {
            
            File[] videos = channel.outputFolder.listFiles();
            if (videos != null) {
                for (File video : videos) {
                    if (video.isFile() && !saved.contains(video.getAbsolutePath())) {
                        boolean isPartFile = video.getName().endsWith(".part");
                        String printedFile = Color.apply((isPartFile ? Color.FILE : Color.VIDEO), video.getName());
                        
                        if (!Configurator.Config.preventDeletion) {
                            System.out.println(Color.base("Deleting: ") + Color.quoted(printedFile));
                            FileUtils.forceDelete(video);
                            
                            if (!isPartFile) {
                                if (channel.saveAsMp3) {
                                    Stats.totalAudioDeletions++;
                                } else {
                                    Stats.totalVideoDeletions++;
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
