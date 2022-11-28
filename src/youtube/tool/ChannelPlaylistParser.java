/*
 * File:    ChannelPlaylistParser.java
 * Package: youtube.tool
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.tool;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import commons.lambda.function.unchecked.UncheckedFunction;
import commons.lambda.stream.mapper.Mappers;
import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.ChannelConfig;
import youtube.channel.ChannelGroup;
import youtube.channel.Channels;
import youtube.channel.util.ChannelJsonFormatter;
import youtube.config.Configurator;
import youtube.entity.info.PlaylistInfo;
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
    
    
    //Constants
    
    /**
     * The list of fields to forcefully include in playlist configurations.
     */
    private static final List<String> FORCE_INCLUDE_FIELDS = List.of("active");
    
    /**
     * The list of fields to forcefully exclude from playlist configurations.
     */
    private static final List<String> FORCE_EXCLUDE_FIELDS = List.of("name", "url");
    
    /**
     * The number of leading indentations in the final output.
     */
    private static final int INDENTATION_COUNT = 0;
    
    /**
     * A flag indicating whether the final output should be formatted as an array or not.
     */
    private static final boolean FORMAT_AS_ARRAY = false;
    
    /**
     * A flag indicating whether each playlist should get its own output folder, otherwise the same output folder as the base Channel will be used.
     */
    private static boolean SEPARATE_FOLDERS = true;
    
    
    //Static Fields
    
    /**
     * The key of the Channel containing the playlists.
     */
    private static final String baseChannelKey = "GREATEST_AUDIOBOOKS";
    
    /**
     * A list of playlist names to skip while parsing.
     */
    private static final List<String> skipPlaylists = List.of(
    );
    
    
    //Main Method
    
    /**
     * Runs the Channel Playlist Parser.
     *
     * @param args Arguments to the main method.
     * @throws Exception When there is an error.
     */
    public static void main(String[] args) throws Exception {
        Configurator.loadSettings(Utils.Project.YOUTUBE_CHANNEL_DOWNLOADER);
        Channels.loadChannels();
        
        final ChannelConfig baseChannel = Channels.getChannel(baseChannelKey);
        if ((baseChannel == null) || !baseChannel.isYoutubeChannel()) {
            return;
        }
        baseChannel.state.cleanupData();
        
        final List<PlaylistInfo> playlistData = parsePlaylistData(baseChannel);
        final List<ChannelConfig> playlistChannels = makePlaylistChannels(baseChannel, playlistData);
        
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
    private static List<PlaylistInfo> parsePlaylistData(ChannelConfig channel) throws Exception {
        return ApiUtils.fetchChannelPlaylists(channel);
    }
    
    /**
     * Creates playlist Channels from the parsed playlist data.
     *
     * @param baseChannel The base Channel.
     * @param playlists   The list of parsed Playlists.
     * @return The list of playlist Channels.
     * @throws Exception When there is an error.
     */
    private static List<ChannelConfig> makePlaylistChannels(ChannelConfig baseChannel, List<PlaylistInfo> playlists) throws Exception {
        return playlists.stream()
                .filter(Objects::nonNull)
                .filter(playlist -> !skipPlaylists.contains(playlist.title))
                .map((UncheckedFunction<PlaylistInfo, ChannelConfig>) playlist ->
                        new ChannelConfig(MapUtility.mapOf(List.of(
                                new ImmutablePair<>("key", (baseChannel.key + "_P")),
                                new ImmutablePair<>("playlistId", playlist.playlistId),
                                new ImmutablePair<>("active", true),
                                new ImmutablePair<>("saveAsMp3", baseChannel.isSaveAsMp3()),
                                new ImmutablePair<>("savePlaylist", (SEPARATE_FOLDERS || baseChannel.isSavePlaylist())),
                                new ImmutablePair<>("keepClean", (SEPARATE_FOLDERS && baseChannel.isKeepClean())),
                                new ImmutablePair<>("outputFolderPath", SEPARATE_FOLDERS ? ("~/" + playlist.title) : null),
                                new ImmutablePair<>("playlistFilePath", !SEPARATE_FOLDERS ? ("~ - " + playlist.title + '.' + Utils.PLAYLIST_FORMAT) : null)
                        )), (ChannelGroup) baseChannel.getParent()))
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        playlistChannels -> playlistChannels.stream()
                                .map(Mappers.forEach(playlistChannel ->
                                        playlistChannel.key += StringUtility.padZero((playlistChannels.indexOf(playlistChannel) + 1), 2)))
                                .collect(Collectors.toList())));
    }
    
    /**
     * Prints the playlist Channels.
     *
     * @param playlistChannels The list of playlist Channels.
     * @return The formatted playlist Channels json string.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    private static String formatPlaylistChannels(List<ChannelConfig> playlistChannels) throws Exception {
        return playlistChannels.stream().sequential()
                .map(e -> StringUtility.splitLines(
                                ChannelJsonFormatter.toMinJsonString(e, FORCE_INCLUDE_FIELDS, FORCE_EXCLUDE_FIELDS)).stream()
                        .map(e2 -> (StringUtility.spaces(INDENTATION_COUNT + (FORMAT_AS_ARRAY ? 1 : 0) * ChannelJsonFormatter.INDENT_WIDTH) + e2))
                        .collect(Collectors.joining(System.lineSeparator())))
                .collect(Collectors.joining(("," + System.lineSeparator()),
                        FORMAT_AS_ARRAY ? ("[" + System.lineSeparator()) : "",
                        FORMAT_AS_ARRAY ? (System.lineSeparator() + "]") : ""));
    }
    
}
