/*
 * File:    EntityFetcher.java
 * Package: youtube.tool
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.ChannelInfo;
import youtube.entity.info.PlaylistInfo;
import youtube.entity.info.VideoInfo;
import youtube.entity.info.base.EntityInfo;
import youtube.util.ApiUtils;

/**
 * Fetches an Entity through the Youtube Data API.
 */
public class EntityFetcher {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EntityFetcher.class);
    
    
    //Main Method
    
    /**
     * Runs the Entity Fetcher.
     *
     * @param args Arguments to the main method.
     */
    public static void main(String[] args) {
        final Map<String, List<EntityInfo>> results = new LinkedHashMap<>();
        List<EntityInfo> resultEntities;
        
        resultEntities = results.computeIfAbsent("Entities", key -> new ArrayList<>());
        fetchVideo("J_hJB0ZpVjk", resultEntities);
        fetchPlaylist("PLovlAKbQVz6D3nqwNV7XmIAJBlZ_6OmYw", resultEntities);
        fetchChannel("UCMV3aTOwUtG5vwfH9_rzb2w", resultEntities);
        
        resultEntities = results.computeIfAbsent("Channel Playlists", key -> new ArrayList<>());
        fetchChannelPlaylists("UCMV3aTOwUtG5vwfH9_rzb2w", resultEntities);
        
        resultEntities = results.computeIfAbsent("Playlist Videos", key -> new ArrayList<>());
        fetchPlaylistVideos("PLovlAKbQVz6D3nqwNV7XmIAJBlZ_6OmYw", resultEntities);
        
        resultEntities = results.computeIfAbsent("Chapter Test", key -> new ArrayList<>());
        fetchVideo("Bd1yJ1B-stA", resultEntities);
        fetchVideo("xUhiNZk0niU", resultEntities);
        fetchVideo("hUumqcL7f2o", resultEntities);
        fetchVideo("p602LDNvKq4", resultEntities);
        
        results.clear();
    }
    
    
    //Static Methods
    
    /**
     * Fetches a Video from the Youtube Data API.
     *
     * @param videoId The id of the Video.
     * @param result  The list to store the Video in.
     * @return The json data of the Video and the Video Info.
     */
    private static ImmutablePair<EntityInfo, Map<String, Object>> fetchVideo(String videoId, List<EntityInfo> result) {
        final Map<String, Object> videoData = ApiUtils.fetchVideoData(videoId);
        final VideoInfo video = ApiUtils.fetchVideo(videoId);
        
        result.add(video);
        return new ImmutablePair<>(video, videoData);
    }
    
    /**
     * Fetches a Playlist from the Youtube Data API.
     *
     * @param playlistId The id of the Playlist.
     * @param result     The list to store the Playlist in.
     * @return The json data of the Playlist and the Playlist Info.
     */
    private static ImmutablePair<EntityInfo, Map<String, Object>> fetchPlaylist(String playlistId, List<EntityInfo> result) {
        final Map<String, Object> playlistData = ApiUtils.fetchPlaylistData(playlistId);
        final PlaylistInfo playlist = ApiUtils.fetchPlaylist(playlistId);
        
        result.add(playlist);
        return new ImmutablePair<>(playlist, playlistData);
    }
    
    /**
     * Fetches a Channel from the Youtube Data API.
     *
     * @param channelId The id of the channel.
     * @param result    The list to store the Channel in.
     * @return The json data of the Channel and the Channel Info.
     */
    private static ImmutablePair<EntityInfo, Map<String, Object>> fetchChannel(String channelId, List<EntityInfo> result) {
        final Map<String, Object> channelData = ApiUtils.fetchChannelData(channelId);
        final ChannelInfo channel = ApiUtils.fetchChannel(channelId);
        
        result.add(channel);
        return new ImmutablePair<>(channel, channelData);
    }
    
    /**
     * Fetches the Playlists of a Channel from the Youtube Data API.
     *
     * @param channelId The id of the Channel.
     * @param result    The list to store the Playlists in.
     * @return The json data of the Playlists and the list of Playlist Info.
     */
    private static List<ImmutablePair<EntityInfo, Map<String, Object>>> fetchChannelPlaylists(String channelId, List<EntityInfo> result) {
        final List<Map<String, Object>> channelPlaylistsData = ApiUtils.fetchChannelPlaylistsData(channelId);
        final List<PlaylistInfo> channelPlaylists = ApiUtils.fetchChannelPlaylists(channelId);
        
        result.addAll(channelPlaylists);
        return IntStream.range(0, channelPlaylists.size())
                .mapToObj(i -> new ImmutablePair<>((EntityInfo) channelPlaylists.get(i), channelPlaylistsData.get(i)))
                .collect(Collectors.toList());
    }
    
    /**
     * Fetches the Videos of a Playlist from the Youtube Data API.
     *
     * @param playlistId The id of the Playlist.
     * @param result     The list to store the Videos in.
     * @return The json data of the Videos and the list of Video Info.
     */
    private static List<ImmutablePair<EntityInfo, Map<String, Object>>> fetchPlaylistVideos(String playlistId, List<EntityInfo> result) {
        final List<Map<String, Object>> playlistVideosData = ApiUtils.fetchPlaylistVideosData(playlistId);
        final List<VideoInfo> playlistVideos = ApiUtils.fetchPlaylistVideos(playlistId);
        
        result.addAll(playlistVideos);
        return IntStream.range(0, playlistVideos.size())
                .mapToObj(i -> new ImmutablePair<>((EntityInfo) playlistVideos.get(i), playlistVideosData.get(i)))
                .collect(Collectors.toList());
    }
    
}
