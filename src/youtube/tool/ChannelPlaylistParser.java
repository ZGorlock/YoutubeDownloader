/*
 * File:    ChannelPlaylistParser.java
 * Package: youtube.tool
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
import youtube.channel.ChannelJsonFormatter;
import youtube.channel.Channels;
import youtube.channel.entity.Playlist;
import youtube.conf.Configurator;
import youtube.util.ApiUtils;
import youtube.util.Utils;

/**
 * Parses Youtube Data API v3 <i>playlists</i> response.
 */
public class ChannelPlaylistParser {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelPlaylistParser.class);
    
    
    //Static Fields
    
    /**
     * The key of the Channel containing the playlists.
     */
    private static final String baseChannelKey = "SOUND_LIBRARY";
    
    /**
     * A flag indicating whether each each playlist should get its own output folder, otherwise the same output folder as the Channel will be used.
     */
    private static boolean separateFolders = true;
    
    
    //Main Method
    
    /**
     * Runs the Channel Playlist Parser.
     *
     * @param args Arguments to the main method
     * @throws Exception When there is an error.
     */
    public static void main(String[] args) throws Exception {
        Configurator.loadSettings(Utils.Project.YOUTUBE_CHANNEL_DOWNLOADER);
        Channels.loadChannels();
        
        final Channel baseChannel = Channels.getChannel(baseChannelKey);
        if ((baseChannel == null) || !baseChannel.isYoutubeChannel()) {
            return;
        }
        
        final List<Playlist> playlistData = parsePlaylistData(baseChannel);
        final List<Channel> playlistChannels = makePlaylistChannels(baseChannel, playlistData);
        
        final String playlistJson = formatPlaylistChannels(playlistChannels);
        System.out.println(playlistJson);
    }
    
    
    //Static Methods
    
    /**
     * Parses the playlist data from the API json response.
     *
     * @param channel The base Channel.
     * @return The list of parsed Playlists.
     * @throws Exception When there is an error.
     */
    private static List<Playlist> parsePlaylistData(Channel channel) throws Exception {
        ApiUtils.fetchChannelPlaylistData(channel);
        return ApiUtils.parseChannelPlaylistData(channel);
    }
    
    /**
     * Creates playlist Channels from the parsed playlist data.
     *
     * @param baseChannel The base Channel.
     * @param playlists   The list of parsed Playlists.
     * @return The list of playlist Channels.
     * @throws Exception When there is an error.
     */
    private static List<Channel> makePlaylistChannels(Channel baseChannel, List<Playlist> playlists) throws Exception {
        return playlists.stream().map(playlist -> {
            final Map<String, Object> playlistFields = new LinkedHashMap<>();
            playlistFields.put("key", (baseChannel.key + "_P" + StringUtility.padZero((playlists.indexOf(playlist) + 1), 2)));
            playlistFields.put("playlistId", playlist.playlistId);
            playlistFields.put("active", baseChannel.active);
            playlistFields.put("saveAsMp3", baseChannel.saveAsMp3);
            playlistFields.put("keepClean", (separateFolders && baseChannel.keepClean));
            playlistFields.put("outputFolder", (baseChannel.outputFolderPath + (separateFolders ? (" - " + playlist.title) : "")));
            playlistFields.put("playlistFile", (((baseChannel.playlistFilePath == null) && separateFolders) ? null :
                                                Optional.ofNullable(baseChannel.playlistFilePath)
                                                        .orElse(baseChannel.outputFolderPath + "." + Utils.PLAYLIST_FORMAT)
                                                        .replace(("." + Utils.PLAYLIST_FORMAT), (" - " + playlist.title + "." + Utils.PLAYLIST_FORMAT))));
            try {
                return new Channel(playlistFields);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
    
    /**
     * Prints the playlist Channels.
     *
     * @param playlistChannels The list of playlist Channels.
     * @return The formatted playlist Channels json string.
     * @throws Exception When there is an error.
     */
    private static String formatPlaylistChannels(List<Channel> playlistChannels) throws Exception {
        return playlistChannels.stream().sequential()
                .map(e -> StringUtility.splitLines(ChannelJsonFormatter.toBaseJsonString(e)).stream()
                        .map(e2 -> (StringUtility.spaces(2) + e2)).collect(Collectors.joining(System.lineSeparator())))
                .collect(Collectors.joining(("," + System.lineSeparator()), ("[" + System.lineSeparator()), (System.lineSeparator() + "]")));
    }
    
}
