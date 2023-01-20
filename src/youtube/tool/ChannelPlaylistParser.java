/*
 * File:    ChannelPlaylistParser.java
 * Package: youtube.tool
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.tool;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import commons.lambda.function.unchecked.UncheckedFunction;
import commons.lambda.stream.mapper.Mappers;
import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.channel.config.ChannelConfig;
import youtube.channel.util.ChannelJsonFormatter;
import youtube.config.Configurator;
import youtube.entity.Channel;
import youtube.entity.Playlist;
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
        
        final Channel baseChannel = Channels.getChannel(baseChannelKey);
        if ((baseChannel == null) || !baseChannel.getConfig().isYoutubeChannel()) {
            return;
        }
        baseChannel.getState().cleanupData();
        
        final List<Playlist> playlists = fetchPlaylists(baseChannel);
        final List<ChannelConfig> playlistChannels = makePlaylistChannels(playlists);
        
        final String playlistChannelsJson = formatPlaylistChannels(playlistChannels);
        System.out.println(playlistChannelsJson);
    }
    
    
    //Static Methods
    
    /**
     * Fetches the Playlists of a Channel from the API.
     *
     * @param channel The Channel.
     * @return The list of Playlists.
     * @throws Exception When there is an error.
     */
    private static List<Playlist> fetchPlaylists(Channel channel) throws Exception {
        return ApiUtils.fetchChannelPlaylists(channel).stream()
                .map(playlistInfo -> new Playlist(playlistInfo, channel))
                .collect(Collectors.toList());
    }
    
    /**
     * Creates playlist Channel Configs from the list of Playlists.
     *
     * @param playlists The list of Playlists.
     * @return The list of playlist Channel Configs.
     * @throws Exception When there is an error.
     */
    private static List<ChannelConfig> makePlaylistChannels(List<Playlist> playlists) throws Exception {
        return playlists.stream()
                .filter(Objects::nonNull)
                .filter(playlist -> Optional.ofNullable(playlist.getInfo()).map(PlaylistInfo::getTitle)
                        .map(e -> skipPlaylists.stream().noneMatch(e::equalsIgnoreCase)).orElse(false))
                .map((UncheckedFunction<Playlist, ChannelConfig>) playlist ->
                        new ChannelConfig(MapUtility.mapOf(List.of(
                                new ImmutablePair<>("key", (playlist.getConfig().getKey() + "_P")),
                                new ImmutablePair<>("playlistId", playlist.getInfo().getPlaylistId()),
                                new ImmutablePair<>("active", true),
                                new ImmutablePair<>("saveAsMp3", playlist.getConfig().isSaveAsMp3()),
                                new ImmutablePair<>("savePlaylist", (SEPARATE_FOLDERS || playlist.getConfig().isSavePlaylist())),
                                new ImmutablePair<>("keepClean", (SEPARATE_FOLDERS && playlist.getConfig().isKeepClean())),
                                new ImmutablePair<>("outputFolderPath", SEPARATE_FOLDERS ? ("~/" + playlist.getInfo().getTitle()) : null),
                                new ImmutablePair<>("playlistFilePath", !SEPARATE_FOLDERS ? ("~ - " + playlist.getInfo().getTitle() + '.' + Utils.DEFAULT_PLAYLIST_FORMAT) : null)
                        )), playlist.getConfig().getParent()))
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        playlistChannels -> playlistChannels.stream()
                                .map(Mappers.forEach(playlistChannel ->
                                        playlistChannel.key += StringUtility.padZero((playlistChannels.indexOf(playlistChannel) + 1), 2)))
                                .collect(Collectors.toList())));
    }
    
    /**
     * Formats the json configuration of a list of Playlist Channel Configs.
     *
     * @param playlistChannelConfigs The list of Playlist Channel Configs.
     * @return The json configuration.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    private static String formatPlaylistChannels(List<ChannelConfig> playlistChannelConfigs) throws Exception {
        return playlistChannelConfigs.stream()
                .map(playlistChannelConfig -> StringUtility.splitLines(
                                ChannelJsonFormatter.toMinJsonString(playlistChannelConfig, FORCE_INCLUDE_FIELDS, FORCE_EXCLUDE_FIELDS)).stream()
                        .map(jsonConfigLine -> (StringUtility.spaces(INDENTATION_COUNT + (FORMAT_AS_ARRAY ? 1 : 0) * ChannelJsonFormatter.INDENT_WIDTH) + jsonConfigLine))
                        .collect(Collectors.joining(System.lineSeparator())))
                .collect(Collectors.joining(("," + System.lineSeparator()),
                        FORMAT_AS_ARRAY ? ("[" + System.lineSeparator()) : "",
                        FORMAT_AS_ARRAY ? (System.lineSeparator() + "]") : ""));
    }
    
}
