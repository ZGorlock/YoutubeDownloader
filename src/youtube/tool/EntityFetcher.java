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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.Channel;
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
     * @throws Exception When there is an error.
     */
    public static void main(String[] args) throws Exception {
        final Map<String, List<EntityInfo>> results = new LinkedHashMap<>();
        List<EntityInfo> resultEntities;
        
        resultEntities = results.computeIfAbsent("Entities", key -> new ArrayList<>());
        fetchVideo("0YiNACjWW-4", resultEntities);
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
     * Fetches a Video Entity from the Youtube Data API.
     *
     * @param videoId The id of the Video Entity.
     * @param result  The list to store the Video Entity in.
     * @return The Video Entity data and the Video Entity.
     * @throws Exception When there is an error.
     */
    private static Map.Entry<EntityInfo, Map<String, Object>> fetchVideo(String videoId, List<EntityInfo> result) throws Exception {
        final Map<String, Object> videoData = ApiUtils.fetchVideoData(videoId);
        final VideoInfo video = ApiUtils.fetchVideo(videoId);
        
        result.add(video);
        return Map.entry(video, videoData);
    }
    
    /**
     * Fetches a Playlist Entity from the Youtube Data API.
     *
     * @param playlistId The id of the Playlist Entity.
     * @param result     The list to store the Playlist Entity in.
     * @return The Playlist Entity data and the Playlist Entity.
     * @throws Exception When there is an error.
     */
    private static Map.Entry<EntityInfo, Map<String, Object>> fetchPlaylist(String playlistId, List<EntityInfo> result) throws Exception {
        final Map<String, Object> playlistData = ApiUtils.fetchPlaylistData(playlistId);
        final PlaylistInfo playlist = ApiUtils.fetchPlaylist(playlistId);
        
        result.add(playlist);
        return Map.entry(playlist, playlistData);
    }
    
    /**
     * Fetches a Channel Entity from the Youtube Data API.
     *
     * @param channelId The id of the Channel Entity.
     * @param result    The list to store the Channel Entity in.
     * @return The Channel Entity data and the Channel Entity.
     * @throws Exception When there is an error.
     */
    private static Map.Entry<EntityInfo, Map<String, Object>> fetchChannel(String channelId, List<EntityInfo> result) throws Exception {
        final Map<String, Object> channelData = ApiUtils.fetchChannelData(channelId);
        final Channel channel = ApiUtils.fetchChannel(channelId);
        
        result.add(channel);
        return Map.entry(channel, channelData);
    }
    
    /**
     * Fetches a list of Playlist Entities of a Channel Entity from the Youtube Data API.
     *
     * @param channelId The id of the Channel Entity.
     * @param result    The list to store the Playlist Entities in.
     * @return The Playlist Entities data and the Playlist Entities.
     * @throws Exception When there is an error.
     */
    private static List<Map.Entry<EntityInfo, Map<String, Object>>> fetchChannelPlaylists(String channelId, List<EntityInfo> result) throws Exception {
        final List<Map<String, Object>> channelPlaylistsData = ApiUtils.fetchChannelPlaylistsData(channelId);
        final List<PlaylistInfo> channelPlaylists = ApiUtils.fetchChannelPlaylists(channelId);
        
        result.addAll(channelPlaylists);
        return IntStream.range(0, channelPlaylists.size())
                .mapToObj(i -> Map.entry((EntityInfo) channelPlaylists.get(i), channelPlaylistsData.get(i)))
                .collect(Collectors.toList());
    }
    
    /**
     * Fetches a list of Video Entities of a Playlist Entity from the Youtube Data API.
     *
     * @param playlistId The id of the Playlist Entity.
     * @param result     The list to store the Video Entities in.
     * @return The Video Entities data and the Video Entities.
     * @throws Exception When there is an error.
     */
    private static List<Map.Entry<EntityInfo, Map<String, Object>>> fetchPlaylistVideos(String playlistId, List<EntityInfo> result) throws Exception {
        final List<Map<String, Object>> playlistVideosData = ApiUtils.fetchPlaylistVideosData(playlistId);
        final List<VideoInfo> playlistVideos = ApiUtils.fetchPlaylistVideos(playlistId);
        
        result.addAll(playlistVideos);
        return IntStream.range(0, playlistVideos.size())
                .mapToObj(i -> Map.entry((EntityInfo) playlistVideos.get(i), playlistVideosData.get(i)))
                .collect(Collectors.toList());
    }
    
}
