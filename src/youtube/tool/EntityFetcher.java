/*
 * File:    EntityFetcher.java
 * Package: youtube.tool
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.entity.Channel;
import youtube.channel.entity.Playlist;
import youtube.channel.entity.Video;
import youtube.channel.entity.base.Entity;
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
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public static void main(String[] args) throws Exception {
        final List<Map.Entry<Entity, Map<String, Object>>> results = new ArrayList<>();
        
        results.add(fetchVideo("0YiNACjWW-4"));
        results.add(fetchPlaylist("PLovlAKbQVz6D3nqwNV7XmIAJBlZ_6OmYw"));
        results.add(fetchChannel("UCMV3aTOwUtG5vwfH9_rzb2w"));
        
        results.clear();
    }
    
    
    //Static Methods
    
    /**
     * Fetches a Video Entity from the Youtube Data API.
     *
     * @param videoId The id of the Video Entity.
     * @return The Video Entity data and the Video Entity.
     * @throws Exception When there is an error.
     */
    private static Map.Entry<Entity, Map<String, Object>> fetchVideo(String videoId) throws Exception {
        final Map<String, Object> videoData = ApiUtils.fetchVideoData(videoId);
        final Video video = new Video(videoData);
        return Map.entry(video, videoData);
    }
    
    /**
     * Fetches a Playlist Entity from the Youtube Data API.
     *
     * @param playlistId The id of the Playlist Entity.
     * @return The Playlist Entity data and the Playlist Entity.
     * @throws Exception When there is an error.
     */
    private static Map.Entry<Entity, Map<String, Object>> fetchPlaylist(String playlistId) throws Exception {
        final Map<String, Object> playlistData = ApiUtils.fetchPlaylistData(playlistId);
        final Playlist playlist = new Playlist(playlistData);
        return Map.entry(playlist, playlistData);
    }
    
    /**
     * Fetches a Channel Entity from the Youtube Data API.
     *
     * @param channelId The id of the Channel Entity.
     * @return The Channel Entity data and the Channel Entity.
     * @throws Exception When there is an error.
     */
    private static Map.Entry<Entity, Map<String, Object>> fetchChannel(String channelId) throws Exception {
        final Map<String, Object> channelData = ApiUtils.fetchChannelData(channelId);
        final Channel channel = new Channel(channelData);
        return Map.entry(channel, channelData);
    }
    
}
