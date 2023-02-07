/*
 * File:    ApiUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.lambda.function.checked.CheckedBiFunction;
import commons.lambda.function.checked.CheckedConsumer;
import commons.lambda.function.checked.CheckedFunction;
import commons.lambda.function.unchecked.UncheckedFunction;
import commons.lambda.function.unchecked.UncheckedSupplier;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.config.ChannelConfig;
import youtube.channel.state.ChannelState;
import youtube.config.Color;
import youtube.entity.Channel;
import youtube.entity.info.ChannelInfo;
import youtube.entity.info.PlaylistInfo;
import youtube.entity.info.VideoInfo;
import youtube.entity.info.base.EntityInfo;
import youtube.state.Stats;

/**
 * Provides API utility methods for the Youtube Downloader.
 */
public final class ApiUtils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ApiUtils.class);
    
    
    //Constants
    
    /**
     * The file containing the Youtube API key.
     */
    public static final File API_KEY_FILE = new File(PathUtils.WORKING_DIR, "apiKey");
    
    /**
     * The Youtube API key.
     */
    private static final String API_KEY;
    
    //Populates API_KEY
    static {
        try {
            API_KEY = FileUtils.readFileToString(API_KEY_FILE).strip();
            if (API_KEY.isEmpty()) {
                throw new KeyException();
            }
        } catch (Exception e) {
            System.out.println(Color.bad("Must supply a Google API key with Youtube Data API enabled in ") + Color.filePath(API_KEY_FILE));
            System.out.println(Color.bad("See: ") + Color.link("https://github.com/ZGorlock/YoutubeDownloader#getting-an-api-key"));
            throw new RuntimeException(e);
        }
    }
    
    /**
     * The base url for querying Youtube Playlists.
     */
    private static final String REQUEST_BASE = "https://www.googleapis.com/youtube/v3";
    
    /**
     * The maximum number of times to retry an API call before failing.
     */
    private static final int MAX_RETRIES = 10;
    
    /**
     * The maximum number of results to request per page.
     */
    private static final int MAX_RESULTS_PER_PAGE = 50;
    
    /**
     * The file containing the API call log history.
     */
    public static final File CALL_LOG_FILE = new File(PathUtils.DATA_DIR, ("callLog" + '.' + Utils.LOG_FILE_FORMAT));
    
    
    //Enums
    
    /**
     * An enumeration of Youtube Data API Endpoint Categories.
     */
    public enum EndpointCategory {
        DATA,
        ENTITY
    }
    
    /**
     * An enumeration of Youtube Data API Endpoints.
     */
    public enum Endpoint {
        
        //Values
        
        CHANNEL("channels", EndpointCategory.ENTITY, List.of(ResponsePart.SNIPPET, ResponsePart.STATUS, ResponsePart.STATISTICS, ResponsePart.TOPIC_DETAILS)),
        PLAYLIST("playlists", EndpointCategory.ENTITY, List.of(ResponsePart.SNIPPET, ResponsePart.CONTENT_DETAILS, ResponsePart.STATUS, ResponsePart.PLAYER)),
        VIDEO("videos", EndpointCategory.ENTITY, List.of(ResponsePart.SNIPPET, ResponsePart.CONTENT_DETAILS, ResponsePart.STATUS, ResponsePart.STATISTICS, ResponsePart.TOPIC_DETAILS, ResponsePart.RECORDING_DETAILS, ResponsePart.PLAYER)),
        
        PLAYLIST_ITEMS("playlistItems", EndpointCategory.DATA, List.of(ResponsePart.CONTENT_DETAILS)),
        CHANNEL_PLAYLISTS("playlists", EndpointCategory.DATA, List.of(ResponsePart.ID));
        
        
        //Fields
        
        /**
         * The name of the Endpoint.
         */
        public final String name;
        
        /**
         * The Category of the Endpoint.
         */
        public final EndpointCategory category;
        
        /**
         * The response parts to retrieve from the Endpoint.
         */
        public final List<ResponsePart> responseParts;
        
        
        //Constructors
        
        /**
         * Constructs an Endpoint.
         *
         * @param name          The name of the Endpoint.
         * @param category      The Category of the Endpoint.
         * @param responseParts The response parts to retrieve from the Endpoint.
         */
        Endpoint(String name, EndpointCategory category, List<ResponsePart> responseParts) {
            this.name = name;
            this.category = category;
            this.responseParts = responseParts.stream().distinct().collect(Collectors.toList());
        }
        
        
        //Getters
        
        /**
         * Returns the name of the Endpoint.
         *
         * @return The name of the Endpoint.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Returns the Category of the Endpoint.
         *
         * @return The Category of the Endpoint.
         */
        public EndpointCategory getCategory() {
            return category;
        }
        
        /**
         * Returns the response parts to retrieve from the Endpoint.
         *
         * @return The response parts to retrieve from the Endpoint.
         */
        public List<ResponsePart> getResponseParts() {
            return responseParts;
        }
        
    }
    
    /**
     * An enumeration of Youtube API Entities.
     */
    public enum ApiEntity {
        
        //Values
        
        CHANNEL(Endpoint.CHANNEL, ApiUtils::fetchChannel, ApiUtils::fetchChannel, ApiUtils::fetchChannelData, ApiUtils::fetchChannelData, ChannelInfo::new),
        PLAYLIST(Endpoint.PLAYLIST, ApiUtils::fetchPlaylist, ApiUtils::fetchPlaylist, ApiUtils::fetchPlaylistData, ApiUtils::fetchPlaylistData, PlaylistInfo::new),
        VIDEO(Endpoint.VIDEO, ApiUtils::fetchVideo, ApiUtils::fetchVideo, ApiUtils::fetchVideoData, ApiUtils::fetchVideoData, VideoInfo::new);
        
        
        //Fields
        
        /**
         * The Endpoint used to fetch the Entity.
         */
        public final Endpoint endpoint;
        
        /**
         * The function that loads the Entity.
         */
        public final CheckedFunction<String, ? extends EntityInfo> entityLoader;
        
        /**
         * The function that loads the Entity for a Channel.
         */
        public final CheckedBiFunction<String, ChannelState, ? extends EntityInfo> checkedEntityLoader;
        
        /**
         * The function that loads the json data of the Entity.
         */
        public final CheckedFunction<String, Map<String, Object>> entityDataLoader;
        
        /**
         * The function that loads the json data of the Entity for a Channel.
         */
        public final CheckedBiFunction<String, ChannelState, Map<String, Object>> checkedEntityDataLoader;
        
        /**
         * The function that parses the Entity.
         */
        public final CheckedFunction<Map<String, Object>, ? extends EntityInfo> entityParser;
        
        
        //Constructors
        
        /**
         * Constructs an API Entity.
         *
         * @param endpoint                The Endpoint used to fetch the Entity.
         * @param entityLoader            The function that loads the Entity.
         * @param checkedEntityLoader     The function that loads the Entity for a Channel.
         * @param entityDataLoader        The function that loads the json data of the Entity.
         * @param checkedEntityDataLoader The function that loads the json data of the Entity for a Channel.
         * @param entityParser            The function that parses the Entity.
         */
        ApiEntity(Endpoint endpoint,
                CheckedFunction<String, EntityInfo> entityLoader,
                CheckedBiFunction<String, ChannelState, EntityInfo> checkedEntityLoader,
                CheckedFunction<String, Map<String, Object>> entityDataLoader,
                CheckedBiFunction<String, ChannelState, Map<String, Object>> checkedEntityDataLoader,
                CheckedFunction<Map<String, Object>, EntityInfo> entityParser) {
            this.endpoint = endpoint;
            this.entityLoader = entityLoader;
            this.checkedEntityLoader = checkedEntityLoader;
            this.entityDataLoader = entityDataLoader;
            this.checkedEntityDataLoader = checkedEntityDataLoader;
            this.entityParser = entityParser;
        }
        
        
        //Methods
        
        /**
         * Loads an Entity.
         *
         * @param entityId The id of the Entity.
         * @param <T>      The type of the Entity.
         * @return The Entity.
         */
        @SuppressWarnings("unchecked")
        public <T extends EntityInfo> T load(String entityId) {
            return (T) Optional.ofNullable(entityId).map(getEntityLoader()).orElse(null);
        }
        
        /**
         * Loads an Entity.
         *
         * @param entityId     The id of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @param <T>          The type of the Entity.
         * @return The Entity.
         */
        @SuppressWarnings("unchecked")
        public <T extends EntityInfo> T load(String entityId, ChannelState channelState) {
            return (T) Optional.ofNullable(entityId).map(id -> getCheckedEntityLoader().apply(id, channelState)).orElse(null);
        }
        
        /**
         * Loads the json data of an Entity.
         *
         * @param entityId The id of the Entity.
         * @return The json data of the Entity.
         */
        public Map<String, Object> loadData(String entityId) {
            return Optional.ofNullable(entityId).map(getEntityDataLoader()).orElse(null);
        }
        
        /**
         * Loads the json data of an Entity.
         *
         * @param entityId     The id of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @return The json data of the Entity.
         */
        public Map<String, Object> loadData(String entityId, ChannelState channelState) {
            return Optional.ofNullable(entityId).map(id -> getCheckedEntityDataLoader().apply(id, channelState)).orElse(null);
        }
        
        /**
         * Parses an Entity.
         *
         * @param entityData The json data of the Entity.
         * @param <T>        The type of the Entity.
         * @return The Entity.
         */
        @SuppressWarnings("unchecked")
        public <T extends EntityInfo> T parse(Map<String, Object> entityData) {
            return (T) Optional.ofNullable(entityData).map(getEntityParser()).orElse(null);
        }
        
        
        //Getters
        
        /**
         * Returns the Endpoint used to fetch the Entity.
         *
         * @return The Endpoint used to fetch the Entity.
         */
        public Endpoint getEndpoint() {
            return endpoint;
        }
        
        /**
         * Returns the function that loads the Entity.
         *
         * @param <T> The type of the Entity.
         * @return The function that loads the Entity.
         */
        @SuppressWarnings("unchecked")
        public <T extends EntityInfo> Function<String, T> getEntityLoader() {
            return (Function<String, T>) entityLoader;
        }
        
        /**
         * Returns the function that loads the Entity for a Channel.
         *
         * @param <T> The type of the Entity.
         * @return The function that loads the Entity for a Channel.
         */
        @SuppressWarnings("unchecked")
        public <T extends EntityInfo> BiFunction<String, ChannelState, T> getCheckedEntityLoader() {
            return (BiFunction<String, ChannelState, T>) checkedEntityLoader;
        }
        
        /**
         * Returns the function that loads the json data of the Entity.
         *
         * @return The function that loads the json data of the Entity.
         */
        public Function<String, Map<String, Object>> getEntityDataLoader() {
            return entityDataLoader;
        }
        
        /**
         * Returns the function that loads the json data of the Entity for a Channel.
         *
         * @return The function that loads the json data of the Entity for a Channel.
         */
        public BiFunction<String, ChannelState, Map<String, Object>> getCheckedEntityDataLoader() {
            return checkedEntityDataLoader;
        }
        
        /**
         * Returns the function that parses the Entity.
         *
         * @param <T> The type of the Entity.
         * @return The function that parses the Entity.
         */
        @SuppressWarnings("unchecked")
        public <T extends EntityInfo> Function<Map<String, Object>, T> getEntityParser() {
            return (Function<Map<String, Object>, T>) entityParser;
        }
        
    }
    
    /**
     * An enumeration of Youtube Data API Response Parts.
     */
    public enum ResponsePart {
        
        //Values
        
        SNIPPET,
        ID,
        CONTENT_DETAILS,
        STATUS,
        STATISTICS,
        TOPIC_DETAILS,
        RECORDING_DETAILS,
        LIVE_STREAMING_DETAILS,
        CONTENT_OWNER_DETAILS,
        BRANDING_SETTINGS,
        LOCALIZATIONS,
        PLAYER;
        
        
        //Fields
        
        /**
         * The name of the Response Part.
         */
        public final String name;
        
        
        //Constructors
        
        /**
         * Constructs an ResponsePart.
         */
        ResponsePart() {
            this.name = StringUtility.toCamelCase(name());
        }
        
        
        //Getters
        
        /**
         * Returns the name of the Response Part.
         *
         * @return The name of the Response Part.
         */
        public String getName() {
            return name;
        }
        
    }
    
    
    //Static Methods
    
    /**
     * Calls the Youtube Data API and fetches a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The Channel Info.
     */
    public static ChannelInfo fetchChannel(String channelId, ChannelState channelState) {
        return EntityHandler.loadEntity(ApiEntity.CHANNEL, channelId, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Channel.
     *
     * @param channelId The id of the Channel.
     * @return The Channel Info.
     */
    public static ChannelInfo fetchChannel(String channelId) {
        return fetchChannel(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The Channel Info.
     */
    public static ChannelInfo fetchChannel(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchChannel(channelConfig.getChannelId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Channel.
     *
     * @param channel The Channel.
     * @return The Channel Info.
     */
    public static ChannelInfo fetchChannel(Channel channel) {
        return fetchChannel(channel.getConfig(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the Channel.
     */
    public static Map<String, Object> fetchChannelData(String channelId, ChannelState channelState) {
        return ApiHandler.fetchEntityData(ApiEntity.CHANNEL, channelId, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel.
     *
     * @param channelId The id of the Channel.
     * @return The json data of the Channel.
     */
    public static Map<String, Object> fetchChannelData(String channelId) {
        return fetchChannelData(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The json data of the Channel.
     */
    public static Map<String, Object> fetchChannelData(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchChannelData(channelConfig.getChannelId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel.
     *
     * @param channel The Channel.
     * @return The json data of the Channel.
     */
    public static Map<String, Object> fetchChannelData(Channel channel) {
        return fetchChannelData(channel.getConfig().getChannelId(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches a Playlist.
     *
     * @param playlistId   The id of the Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The Playlist Info.
     */
    public static PlaylistInfo fetchPlaylist(String playlistId, ChannelState channelState) {
        return EntityHandler.loadEntity(ApiEntity.PLAYLIST, playlistId, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Playlist.
     *
     * @param playlistId The id of the Playlist.
     * @return The Playlist Info.
     */
    public static PlaylistInfo fetchPlaylist(String playlistId) {
        return fetchPlaylist(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Playlist.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The Playlist Info.
     */
    public static PlaylistInfo fetchPlaylist(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchPlaylist(channelConfig.getPlaylistId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Playlist.
     *
     * @param channel The Channel.
     * @return The Playlist Info.
     */
    public static PlaylistInfo fetchPlaylist(Channel channel) {
        return fetchPlaylist(channel.getConfig(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist.
     *
     * @param playlistId   The id of the Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the Playlist.
     */
    public static Map<String, Object> fetchPlaylistData(String playlistId, ChannelState channelState) {
        return ApiHandler.fetchEntityData(ApiEntity.PLAYLIST, playlistId, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist.
     *
     * @param playlistId The id of the Playlist.
     * @return The json data of the Playlist.
     */
    public static Map<String, Object> fetchPlaylistData(String playlistId) {
        return fetchPlaylistData(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The json data of the Playlist.
     */
    public static Map<String, Object> fetchPlaylistData(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchPlaylistData(channelConfig.getPlaylistId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist.
     *
     * @param channel The Channel.
     * @return The json data of the Playlist.
     */
    public static Map<String, Object> fetchPlaylistData(Channel channel) {
        return fetchPlaylistData(channel.getConfig().getPlaylistId(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches a Video.
     *
     * @param videoId      The id of the Video.
     * @param channelState The Channel State of the calling Channel.
     * @return The Video Info.
     */
    public static VideoInfo fetchVideo(String videoId, ChannelState channelState) {
        return EntityHandler.loadEntity(ApiEntity.VIDEO, videoId, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Video.
     *
     * @param videoId The id of the Video.
     * @return The Video Info.
     */
    public static VideoInfo fetchVideo(String videoId) {
        return fetchVideo(videoId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Video.
     *
     * @param videoId      The id of the Video.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the Video.
     */
    public static Map<String, Object> fetchVideoData(String videoId, ChannelState channelState) {
        return ApiHandler.fetchEntityData(ApiEntity.VIDEO, videoId, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Video.
     *
     * @param videoId The id of the Video.
     * @return The json data of the Video.
     */
    public static Map<String, Object> fetchVideoData(String videoId) {
        return fetchVideoData(videoId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Playlist.
     *
     * @param playlistId   The id of the Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchPlaylistVideos(String playlistId, ChannelState channelState) {
        return EntityHandler.loadEntityList(ApiEntity.VIDEO, playlistId, channelState, ApiUtils::fetchPlaylistVideosData);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Playlist.
     *
     * @param playlistId The id of the Playlist.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchPlaylistVideos(String playlistId) {
        return fetchPlaylistVideos(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Playlist.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchPlaylistVideos(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchPlaylistVideos(channelConfig.getPlaylistId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Playlist.
     *
     * @param channel The Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchPlaylistVideos(Channel channel) {
        return fetchPlaylistVideos(channel.getConfig(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Playlist.
     *
     * @param playlistId   The id of the Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the list of Videos.
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> fetchPlaylistVideosData(String playlistId, ChannelState channelState) {
        return ApiHandler.fetchEntityListData(Endpoint.PLAYLIST_ITEMS, playlistId, channelState,
                new HashMap<>(Map.of("playlistId", playlistId.replaceAll("^UC", "UU"))),
                e -> Optional.ofNullable((Map<String, Object>) e.get("contentDetails")).map(e2 -> (String) e2.get("videoId")).orElse(null),
                ids -> ApiHandler.callApi(Endpoint.VIDEO, new HashMap<>(Map.of("id", ids)), channelState));
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Playlist.
     *
     * @param playlistId The id of the Playlist.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(String playlistId) {
        return fetchPlaylistVideosData(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Playlist.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchPlaylistVideosData(channelConfig.getPlaylistId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Playlist.
     *
     * @param channel The Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(Channel channel) {
        return fetchPlaylistVideosData(channel.getConfig(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The list of Playlists.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(String channelId, ChannelState channelState) {
        return EntityHandler.loadEntityList(ApiEntity.PLAYLIST, channelId, channelState, ApiUtils::fetchChannelPlaylistsData);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelId The id of the Channel.
     * @return The list of Playlists.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(String channelId) {
        return fetchChannelPlaylists(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The list of Playlists.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchChannelPlaylists(channelConfig.getPlaylistId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channel The Channel.
     * @return The list of Playlists.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(Channel channel) {
        return fetchChannelPlaylists(channel.getConfig().getChannelId(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Playlists of a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the list of Playlists.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(String channelId, ChannelState channelState) {
        return ApiHandler.fetchEntityListData(Endpoint.CHANNEL_PLAYLISTS, channelId, channelState,
                new HashMap<>(Map.of("channelId", channelId.replaceAll("^UU", "UC"))),
                e -> (String) e.get("id"),
                ids -> ApiHandler.callApi(Endpoint.PLAYLIST, new HashMap<>(Map.of("id", ids)), channelState));
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelId The id of the Channel.
     * @return The json data of the list of Playlists.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(String channelId) {
        return fetchChannelPlaylistsData(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Playlists of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The json data of the list of Playlists.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchChannelPlaylistsData(channelConfig.getPlaylistId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Playlists of a Channel.
     *
     * @param channel The Channel.
     * @return The json data of the list of Playlists.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(Channel channel) {
        return fetchChannelPlaylistsData(channel.getConfig().getChannelId(), channel.getState());
    }
    
    /**
     * Clears the fetched Entity cache.
     */
    public static void clearCache() {
        Arrays.stream(ApiEntity.values())
                .map(EntityHandler.entityCache::get)
                .forEach(Map::clear);
    }
    
    
    //Inner Classes
    
    /**
     * Interacts with the Youtube Data API.
     */
    private static class ApiHandler {
        
        //Static Fields
        
        /**
         * The HTTP Client used to interact with the Youtube API.
         */
        private static final CloseableHttpClient httpClient = HttpClients.createDefault();
        
        
        //Static Methods
        
        /**
         * Calls the Youtube Data API and fetches the json data of an Entity.
         *
         * @param entityType   The Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param parameters   A map of parameters.
         * @param channelState The Channel State of the calling Channel.
         * @return The json data of the Entity.
         * @throws RuntimeException When there is an error fetching or parsing the Entity.
         */
        private static Map<String, Object> fetchEntityData(ApiEntity entityType, String entityId, Map<String, String> parameters, ChannelState channelState) {
            return EntityHandler.loadEntityData(entityType, entityId, channelState,
                    (id, state) -> Optional.ofNullable(entityType).map(ApiEntity::getEndpoint)
                            .map((UncheckedFunction<Endpoint, String>) apiEndpoint ->
                                    callApi(apiEndpoint, parameters, state))
                            .map(response -> parseResponse(response, state))
                            .map(dataList -> ListUtility.getOrNull(dataList, 0))
                            .orElse(Map.of()));
        }
        
        /**
         * Calls the Youtube Data API and fetches the json data of an Entity.
         *
         * @param entityType   The Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @return The json data of the Entity.
         * @throws RuntimeException When there is an error fetching or parsing the Entity.
         */
        private static Map<String, Object> fetchEntityData(ApiEntity entityType, String entityId, ChannelState channelState) {
            return fetchEntityData(entityType, entityId, new HashMap<>(Map.of("id", entityId)), channelState);
        }
        
        /**
         * Calls the Youtube Data API and fetches the json data of a list of Entities.
         *
         * @param endpoint          The API Endpoint.
         * @param entityId          The id of the parent Entity.
         * @param channelState      The Channel State of the calling Channel.
         * @param parameters        A map of parameters.
         * @param idExtractor       The function that extracts the id from a response data element.
         * @param entityDataFetcher The function that fetches a page of Entity json data from the list of extracted Entity ids.
         * @return The json data of the list of Entities.
         * @throws RuntimeException When there is an error fetching or parsing the list of Entities.
         */
        public static List<Map<String, Object>> fetchEntityListData(Endpoint endpoint, String entityId, ChannelState channelState, Map<String, String> parameters,
                UncheckedFunction<Map<String, Object>, String> idExtractor,
                UncheckedFunction<String, String> entityDataFetcher) {
            return Optional.ofNullable(loadDataCache(endpoint, channelState))
                    .orElseGet((UncheckedSupplier<List<String>>) () -> {
                        final List<String> pages = new ArrayList<>();
                        do {
                            Optional.ofNullable(endpoint)
                                    .map((UncheckedFunction<Endpoint, String>) apiEndpoint ->
                                            callApi(apiEndpoint, parameters, channelState))
                                    .map(response -> parseResponse(response, channelState).stream()
                                            .map(idExtractor)
                                            .filter(id -> !StringUtility.isNullOrBlank(id))
                                            .collect(Collectors.joining(",")))
                                    .map(entityDataFetcher)
                                    .ifPresent(pages::add);
                        } while (parameters.get("pageToken") != null);
                        
                        saveDataCache(pages, endpoint, channelState);
                        return pages;
                    })
                    .stream()
                    .flatMap(data -> parseResponse(data, channelState).stream())
                    .collect(Collectors.toList());
        }
        
        /**
         * Calls the Youtube Data API and fetches the json data of a list of Entities.
         *
         * @param endpoint          The API Endpoint.
         * @param entityId          The id of the parent Entity.
         * @param channelState      The Channel State of the calling Channel.
         * @param idExtractor       The function that extracts the id from a response data element.
         * @param entityDataFetcher The function that fetches a page of Entity json data from the list of extracted Entity ids.
         * @return The json data of the list of Entities.
         * @throws RuntimeException When there is an error fetching or parsing the list of Entities.
         */
        public static List<Map<String, Object>> fetchEntityListData(Endpoint endpoint, String entityId, ChannelState channelState,
                UncheckedFunction<Map<String, Object>, String> idExtractor,
                UncheckedFunction<String, String> entityDataFetcher) {
            return fetchEntityListData(endpoint, entityId, channelState, new HashMap<>(Map.of("id", entityId)), idExtractor, entityDataFetcher);
        }
        
        /**
         * Loads an API data cache, if present.
         *
         * @param endpoint     The API Endpoint.
         * @param channelState The Channel State of the calling Channel.
         * @return The list of API data pages, or null if it could not be loaded.
         */
        private static List<String> loadDataCache(Endpoint endpoint, ChannelState channelState) {
            return Optional.ofNullable(channelState).map(state -> state.getDataFile(endpoint.getName()))
                    .filter(File::exists).map((CheckedFunction<File, String>) FileUtils::readFileToString)
                    .filter(data -> !StringUtility.isNullOrBlank(data))
                    .map(data -> data.split("(?:^|\r?\n)[\\[,\\]](?:\r?\n|$)"))
                    .map(dataPages -> Arrays.stream(dataPages)
                            .filter(dataPage -> !StringUtility.isNullOrBlank(dataPage))
                            .collect(Collectors.toList()))
                    .filter(dataPages -> !ListUtility.isNullOrEmpty(dataPages))
                    .orElse(null);
        }
        
        /**
         * Saves an API data cache.
         *
         * @param dataPages    The list of API data pages.
         * @param endpoint     The API Endpoint.
         * @param channelState The Channel State of the calling Channel.
         */
        private static void saveDataCache(List<String> dataPages, Endpoint endpoint, ChannelState channelState) {
            Optional.ofNullable(channelState).map(state -> state.getDataFile(endpoint.getName()))
                    .ifPresent((CheckedConsumer<File>) dataFile -> FileUtils.writeStringToFile(dataFile,
                            dataPages.stream().collect(Collectors.joining(
                                    (System.lineSeparator() + "," + System.lineSeparator()),
                                    ("[" + System.lineSeparator()), (System.lineSeparator() + "]")))));
        }
        
        /**
         * Calls the Youtube Data API.
         *
         * @param endpoint     The API Endpoint.
         * @param parameters   A map of parameters.
         * @param channelState The Channel State of the calling Channel.
         * @return The response from the API call.
         * @throws Exception When there is an error calling the API.
         */
        @SuppressWarnings("unchecked")
        public static String callApi(Endpoint endpoint, Map<String, String> parameters, ChannelState channelState) throws Exception {
            final AtomicReference<String> response = new AtomicReference<>(null);
            final AtomicBoolean error = new AtomicBoolean(false);
            
            for (int retry = 0; retry <= MAX_RETRIES; retry++) {
                final HttpGet request = buildApiRequest(endpoint, new HashMap<>(parameters));
                
                try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
                    response.set(EntityUtils.toString(httpResponse.getEntity()).strip());
                    error.set(httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK);
                    
                    logApiCall(endpoint, request.getURI(), response.get(), error.get(), channelState);
                    
                    if (!error.get()) {
                        parameters.put("pageToken", (String) ((Map<String, Object>) new JSONParser().parse(response.get())).get("nextPageToken"));
                        return response.get();
                    }
                }
            }
            return handleResponse(response.get(), channelState);
        }
        
        /**
         * Logs a call to the Youtube Data API.
         *
         * @param endpoint     The API Endpoint.
         * @param request      The uri of the API request.
         * @param response     The API response.
         * @param error        Whether the API response was an error.
         * @param channelState The Channel State of the calling Channel.
         */
        private static void logApiCall(Endpoint endpoint, URI request, String response, boolean error, ChannelState channelState) {
            Stats.totalApiCalls.incrementAndGet();
            Stats.totalApiEntityCalls.addAndGet((endpoint.getCategory() == EndpointCategory.ENTITY) ? 1 : 0);
            Stats.totalApiDataCalls.addAndGet((endpoint.getCategory() == EndpointCategory.DATA) ? 1 : 0);
            Stats.totalApiFailures.addAndGet(error ? 1 : 0);
            
            final String callLog = String.format("%-19s  %15s  %8d bytes  %s  %s", Utils.currentTimestamp(), endpoint.getName(),
                    response.length(), (error ? "=XXX=" : "====="), request);
            Stream.of(CALL_LOG_FILE, (Optional.ofNullable(channelState).map(ChannelState::getCallLogFile).orElse(null)))
                    .filter(Objects::nonNull).forEach((CheckedConsumer<File>) logFile ->
                            FileUtils.writeStringToFile(logFile, (callLog + System.lineSeparator()), true));
        }
        
        /**
         * Builds an API HTTP GET request.
         *
         * @param endpoint   The API Endpoint.
         * @param parameters A map of parameters.
         * @return The API HTTP GET request.
         */
        private static HttpGet buildApiRequest(Endpoint endpoint, Map<String, String> parameters) {
            parameters.putIfAbsent("part", endpoint.getResponseParts().stream().map(ResponsePart::getName).collect(Collectors.joining(",")));
            parameters.putIfAbsent("maxResults", String.valueOf(MAX_RESULTS_PER_PAGE));
            parameters.putIfAbsent("key", API_KEY);
            
            final HttpGet request = new HttpGet(buildApiUrl(endpoint, parameters));
            request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");
            return request;
        }
        
        /**
         * Builds an API url string.
         *
         * @param endpoint   The API Endpoint.
         * @param parameters A map of parameters.
         * @return The API url string.
         */
        private static String buildApiUrl(Endpoint endpoint, Map<String, String> parameters) {
            return String.join("/", REQUEST_BASE, endpoint.getName()) +
                    buildApiParameterString(parameters);
        }
        
        /**
         * Builds an API parameter string for an API endpoint.
         *
         * @param parameters A map of parameters.
         * @return The API parameter string.
         */
        private static String buildApiParameterString(Map<String, String> parameters) {
            return parameters.entrySet().stream()
                    .map(parameter -> String.join("=",
                            URLEncoder.encode(parameter.getKey(), StandardCharsets.UTF_8),
                            URLEncoder.encode(parameter.getValue(), StandardCharsets.UTF_8)))
                    .collect(Collectors.joining("&", "?", ""));
        }
        
        /**
         * Handles a response from the Youtube Data API.
         *
         * @param response     The response.
         * @param channelState The Channel State of the calling Channel.
         * @return The response.
         * @throws RuntimeException If the response is an error.
         */
        private static String handleResponse(String response, ChannelState channelState) {
            return Optional.ofNullable(response)
                    .filter(e -> e.contains("\"error\": {"))
                    .filter(e -> e.contains("\"code\":"))
                    .map(e -> e.replaceAll("(?s)^.*\"code\":\\s*(\\d+).*$", "$1"))
                    .filter(e -> !StringUtility.isNullOrBlank(e))
                    .filter(errorCode -> {
                        switch (errorCode) {
                            case "404":
                                System.out.println(Color.bad("The Youtube source") +
                                        ((channelState != null) ? (Color.bad(" referenced by Channel: ") + Color.channel(channelState)) : "") +
                                        Color.bad(" does not exist"));
                                break;
                            case "403":
                                System.out.println(Color.bad("Your API Key is not authorized or has exceeded its quota"));
                                break;
                            case "400":
                                System.out.println(Color.bad("The API call that was made does not Your API Key is not authorized or has exceeded its quota"));
                                break;
                            default:
                                System.out.println(Color.bad("Error: ") + Color.number(errorCode) + Color.bad(" while calling API") +
                                        ((channelState != null) ? (Color.bad(" for Channel: ") + Color.channel(channelState)) : ""));
                                break;
                        }
                        if (channelState != null) {
                            channelState.getErrorFlag().set(true);
                        }
                        throw new RuntimeException("Youtube Data API responded with error code: " + errorCode +
                                (response.contains("\"reason\":") ? (" (" + response.replaceAll("(?s)^.*\"reason\": \"([^\"]+)\",.*$", "$1") + ")") : ""));
                    })
                    .orElse(response);
        }
        
        /**
         * Parses the data of a response from the Youtube Data API.
         *
         * @param response     The response.
         * @param channelState The Channel State of the calling Channel.
         * @return The parsed data from the response.
         * @throws RuntimeException When the response could not be parsed.
         */
        @SuppressWarnings("unchecked")
        private static List<Map<String, Object>> parseResponse(String response, ChannelState channelState) {
            return Optional.ofNullable(response)
                    .map((CheckedFunction<String, Map<String, Object>>) e ->
                            (Map<String, Object>) new JSONParser().parse(e))
                    .map(e -> (ArrayList<Map<String, Object>>) e.get("items"))
                    .orElseThrow(() -> {
                        if ((channelState != null) && channelState.getErrorFlag().compareAndSet(false, true)) {
                            System.out.println(Color.bad("Error parsing API data for Channel: ") + Color.channel(channelState));
                        }
                        throw new RuntimeException("Youtube Data API responded with invalid data");
                    });
        }
        
    }
    
    /**
     * Handles Entity loading and caching.
     */
    private static class EntityHandler {
        
        //Static Fields
        
        /**
         * A cache of previously fetched Entities.
         */
        private static final Map<ApiEntity, Map<String, EntityInfo>> entityCache = Arrays.stream(ApiEntity.values())
                .collect(MapCollectors.mapEachTo(() -> new HashMap<>()));
        
        /**
         * A cache of the json data of previously fetched Entities.
         */
        private static final Map<ApiEntity, Map<String, Map<String, Object>>> entityDataCache = Arrays.stream(ApiEntity.values())
                .collect(MapCollectors.mapEachTo(() -> new HashMap<>()));
        
        
        //Static Methods
        
        /**
         * Caches the json data of an Entity.
         *
         * @param entityType       The Type of the Entity.
         * @param entityId         The id of the Entity.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @return The json data of the Entity, or null if it could not be loaded.
         */
        private static Map<String, Object> cacheEntityData(ApiEntity entityType, String entityId,
                Function<String, Map<String, Object>> entityDataLoader) {
            return Optional.ofNullable(entityType).map(entityDataCache::get)
                    .flatMap(cache -> Optional.ofNullable(entityId)
                            .map(id -> cache.computeIfAbsent(id, entityDataLoader)))
                    .orElse(null);
        }
        
        /**
         * Caches the json data of an Entity.
         *
         * @param entityType The Type of the Entity.
         * @param entityId   The id of the Entity.
         * @return The json data of the Entity, or null if it could not be loaded.
         */
        private static Map<String, Object> cacheEntityData(ApiEntity entityType, String entityId) {
            return cacheEntityData(entityType, entityId, entityType.getEntityDataLoader());
        }
        
        /**
         * Caches an Entity.
         *
         * @param entityType   The Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param entityLoader The function used to load the Entity.
         * @param <T>          The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        @SuppressWarnings("unchecked")
        private static <T extends EntityInfo> T cacheEntity(ApiEntity entityType, String entityId,
                Function<String, T> entityLoader) {
            return Optional.ofNullable(entityType).map(entityCache::get)
                    .flatMap(cache -> Optional.ofNullable(entityId)
                            .map(id -> (T) cache.computeIfAbsent(id, entityLoader)))
                    .orElse(null);
        }
        
        /**
         * Caches an Entity.
         *
         * @param entityType The Type of the Entity.
         * @param entityId   The id of the Entity.
         * @param <T>        The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        private static <T extends EntityInfo> T cacheEntity(ApiEntity entityType, String entityId) {
            return cacheEntity(entityType, entityId, entityType.getEntityLoader());
        }
        
        /**
         * Loads the json data of an Entity.
         *
         * @param entityType       The Entity Type of the Entity.
         * @param entityId         The id of the Entity.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @return The json data of the Entity, or null if it could not be loaded.
         */
        public static Map<String, Object> loadEntityData(ApiEntity entityType, String entityId,
                Function<String, Map<String, Object>> entityDataLoader) {
            return cacheEntityData(entityType, entityId, entityDataLoader);
        }
        
        /**
         * Loads the json data of an Entity.
         *
         * @param entityType The Entity Type of the Entity.
         * @param entityId   The id of the Entity.
         * @return The json data of the Entity, or null if it could not be loaded.
         */
        public static Map<String, Object> loadEntityData(ApiEntity entityType, String entityId) {
            return loadEntityData(entityType, entityId, entityType.getEntityDataLoader());
        }
        
        /**
         * Loads the json data of an Entity.
         *
         * @param entityType       The Entity Type of the Entity.
         * @param entityId         The id of the Entity.
         * @param channelState     The Channel State of the calling Channel.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @return The json data of the Entity, or null if it could not be loaded.
         */
        public static Map<String, Object> loadEntityData(ApiEntity entityType, String entityId, ChannelState channelState,
                BiFunction<String, ChannelState, Map<String, Object>> entityDataLoader) {
            return loadEntityData(entityType, entityId,
                    id -> entityDataLoader.apply(id, channelState));
        }
        
        /**
         * Loads the json data of an Entity.
         *
         * @param entityType   The Entity Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @return The json data of the Entity, or null if it could not be loaded.
         */
        public static Map<String, Object> loadEntityData(ApiEntity entityType, String entityId, ChannelState channelState) {
            return loadEntityData(entityType, entityId, channelState, entityType.getCheckedEntityDataLoader());
        }
        
        /**
         * Loads the json data of an Entity.
         *
         * @param entityType The Entity Type of the Entity.
         * @param entityData The json data of the Entity.
         * @return The json data of the Entity, or null if it could not be loaded.
         */
        public static Map<String, Object> loadEntityData(ApiEntity entityType, Map<String, Object> entityData) {
            return loadEntityData(entityType, (String) entityData.get("id"),
                    id -> entityData);
        }
        
        /**
         * Loads an Entity.
         *
         * @param entityType   The Entity Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param entityLoader The function used to load the Entity.
         * @param <T>          The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        private static <T extends EntityInfo> T loadEntity(ApiEntity entityType, String entityId,
                Function<String, T> entityLoader) {
            return cacheEntity(entityType, entityId, entityLoader);
        }
        
        /**
         * Loads an Entity.
         *
         * @param entityType       The Entity Type of the Entity.
         * @param entityId         The id of the Entity.
         * @param channelState     The Channel State of the calling Channel.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @param <T>              The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        private static <T extends EntityInfo> T loadEntity(ApiEntity entityType, String entityId, ChannelState channelState,
                BiFunction<String, ChannelState, T> entityDataLoader) {
            return loadEntity(entityType, entityId,
                    id -> entityDataLoader.apply(id, channelState));
        }
        
        /**
         * Loads an Entity.
         *
         * @param entityType       The Entity Type of the Entity.
         * @param entityId         The id of the Entity.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @param entityParser     The function used to parse the Entity.
         * @param <T>              The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        public static <T extends EntityInfo> T loadEntity(ApiEntity entityType, String entityId,
                Function<String, Map<String, Object>> entityDataLoader,
                Function<Map<String, Object>, T> entityParser) {
            return loadEntity(entityType, entityId,
                    id -> Optional.ofNullable(id)
                            .map(entityDataLoader)
                            .map(entityParser)
                            .orElse(null));
        }
        
        /**
         * Loads an Entity.
         *
         * @param entityType The Entity Type of the Entity.
         * @param entityId   The id of the Entity.
         * @param <T>        The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        public static <T extends EntityInfo> T loadEntity(ApiEntity entityType, String entityId) {
            return loadEntity(entityType, entityId, entityType.getEntityDataLoader(), entityType.getEntityParser());
        }
        
        /**
         * Loads an Entity.
         *
         * @param entityType       The Entity Type of the Entity.
         * @param entityId         The id of the Entity.
         * @param channelState     The Channel State of the calling Channel.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @param entityParser     The function used to parse the Entity.
         * @param <T>              The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        public static <T extends EntityInfo> T loadEntity(ApiEntity entityType, String entityId, ChannelState channelState,
                BiFunction<String, ChannelState, Map<String, Object>> entityDataLoader,
                Function<Map<String, Object>, T> entityParser) {
            return loadEntity(entityType, entityId,
                    id -> entityDataLoader.apply(id, channelState),
                    entityParser);
        }
        
        /**
         * Loads an Entity.
         *
         * @param entityType   The Entity Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @param <T>          The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        public static <T extends EntityInfo> T loadEntity(ApiEntity entityType, String entityId, ChannelState channelState) {
            return loadEntity(entityType, entityId, channelState, entityType.getCheckedEntityDataLoader(), entityType.getEntityParser());
        }
        
        /**
         * Loads an Entity.
         *
         * @param entityType The Entity Type of the Entity.
         * @param entityData The json data of the Entity.
         * @param <T>        The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        public static <T extends EntityInfo> T loadEntity(ApiEntity entityType, Map<String, Object> entityData) {
            return loadEntity(entityType, (String) entityData.get("id"),
                    id -> loadEntityData(entityType, entityData),
                    entityType.getEntityParser());
        }
        
        /**
         * Loads the json data of a list of Entities.
         *
         * @param entityType         The Entity Type of the Entities.
         * @param entityId           The id of the parent Entity.
         * @param entitiesDataLoader The function used to load the json data of the Entities.
         * @return The json data of the list of Entities, or null if it could not be loaded.
         */
        public static List<Map<String, Object>> loadEntityListData(ApiEntity entityType, String entityId,
                Function<String, List<Map<String, Object>>> entitiesDataLoader) {
            return Optional.ofNullable(entityId)
                    .map(entitiesDataLoader)
                    .map(entityDataList -> entityDataList.stream()
                            .map(entityData -> loadEntityData(entityType, entityData))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()))
                    .orElse(null);
        }
        
        /**
         * Loads the json data of a list of Entities.
         *
         * @param entityType         The Entity Type of the Entities.
         * @param entityId           The id of the parent Entity.
         * @param channelState       The Channel State of the calling Channel.
         * @param entitiesDataLoader The function used to load the json data of the Entities.
         * @return The json data of the list of Entities, or null if it could not be loaded.
         */
        public static List<Map<String, Object>> loadEntityListData(ApiEntity entityType, String entityId, ChannelState channelState,
                BiFunction<String, ChannelState, List<Map<String, Object>>> entitiesDataLoader) {
            return loadEntityListData(entityType, entityId,
                    id -> entitiesDataLoader.apply(id, channelState));
        }
        
        /**
         * Loads a list of Entities.
         *
         * @param entityType         The Entity Type of the Entities.
         * @param entityId           The id of the parent Entity.
         * @param entitiesDataLoader The function used to load the json data of the Entities.
         * @param <T>                The type of the Entities.
         * @return The list of Entities, or null if it could not be loaded.
         */
        @SuppressWarnings("unchecked")
        public static <T extends EntityInfo> List<T> loadEntityList(ApiEntity entityType, String entityId,
                Function<String, List<Map<String, Object>>> entitiesDataLoader) {
            return Optional.ofNullable(loadEntityListData(entityType, entityId, entitiesDataLoader))
                    .map(entityDataList -> entityDataList.stream()
                            .map(entityData -> (T) loadEntity(entityType, entityData))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()))
                    .orElse(null);
        }
        
        /**
         * Loads a list of Entities.
         *
         * @param entityType         The Entity Type of the Entities.
         * @param entityId           The id of the parent Entity.
         * @param channelState       The Channel State of the calling Channel.
         * @param entitiesDataLoader The function used to load the json data of the Entities.
         * @param <T>                The type of the Entities.
         * @return The list of Entities, or null if it could not be loaded.
         */
        public static <T extends EntityInfo> List<T> loadEntityList(ApiEntity entityType, String entityId, ChannelState channelState,
                BiFunction<String, ChannelState, List<Map<String, Object>>> entitiesDataLoader) {
            return loadEntityList(entityType, entityId,
                    id -> entitiesDataLoader.apply(id, channelState));
        }
        
    }
    
}
