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

import commons.access.Filesystem;
import commons.lambda.function.checked.CheckedBiFunction;
import commons.lambda.function.checked.CheckedFunction;
import commons.lambda.function.unchecked.UncheckedFunction;
import commons.lambda.function.unchecked.UncheckedSupplier;
import commons.lambda.stream.collector.MapCollectors;
import commons.lambda.stream.mapper.Mappers;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.channel.config.ChannelConfig;
import youtube.channel.config.ChannelEntry;
import youtube.channel.state.ChannelState;
import youtube.config.Color;
import youtube.entity.Channel;
import youtube.entity.Playlist;
import youtube.entity.info.ChannelInfo;
import youtube.entity.info.PlaylistInfo;
import youtube.entity.info.VideoInfo;
import youtube.entity.info.base.EntityInfo;
import youtube.state.ApiQuota;
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
    private static final String API_KEY = Optional.of(API_KEY_FILE)
            .map(Filesystem::readFileToString).map(String::strip)
            .filter(key -> !StringUtility.isNullOrEmpty(key))
            .orElseThrow(() -> {
                logger.warn(Color.bad("Must supply a Google API key with Youtube Data API enabled in ") + Color.quoteFilePath(API_KEY_FILE));
                logger.warn(Color.bad("See: ") + Color.link("https://github.com/ZGorlock/YoutubeDownloader#getting-an-api-key"));
                return new RuntimeException(new KeyException());
            });
    
    /**
     * The hash of the Youtube API key.
     */
    private static final String API_KEY_HASH = DigestUtils.sha1Hex(API_KEY);
    
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
     * The pattern for an api log.
     */
    private static final String API_LOG_PATTERN = StringUtility.format("%{}s  ::  %-{}s  %s",
            Channels.getFiltered().stream().map(Channels::getChannel).map(Channel::getConfig)
                    .map(ChannelEntry::getName).mapToInt(String::length).max().orElse(1),
            Arrays.stream(Endpoint.values())
                    .map(Endpoint::getName).mapToInt(String::length).max().orElse(1));
    
    
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
        
        CHANNEL("channels", EndpointCategory.ENTITY, ApiQuota.QuotaCost.CHANNELS_LIST, List.of(ResponsePart.SNIPPET, ResponsePart.STATUS, ResponsePart.STATISTICS, ResponsePart.TOPIC_DETAILS)),
        PLAYLIST("playlists", EndpointCategory.ENTITY, ApiQuota.QuotaCost.PLAYLISTS_LIST, List.of(ResponsePart.SNIPPET, ResponsePart.CONTENT_DETAILS, ResponsePart.STATUS, ResponsePart.PLAYER)),
        VIDEO("videos", EndpointCategory.ENTITY, ApiQuota.QuotaCost.VIDEOS_LIST, List.of(ResponsePart.SNIPPET, ResponsePart.CONTENT_DETAILS, ResponsePart.STATUS, ResponsePart.STATISTICS, ResponsePart.TOPIC_DETAILS, ResponsePart.RECORDING_DETAILS, ResponsePart.PLAYER)),
        
        PLAYLIST_ITEMS("playlistItems", EndpointCategory.DATA, ApiQuota.QuotaCost.PLAYLIST_ITEMS_LIST, List.of(ResponsePart.CONTENT_DETAILS)),
        CHANNEL_PLAYLISTS("playlists", EndpointCategory.DATA, ApiQuota.QuotaCost.PLAYLISTS_LIST, List.of(ResponsePart.ID));
        
        
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
         * The Quota Cost of the Endpoint.
         */
        public final ApiQuota.QuotaCost quotaCost;
        
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
         * @param quotaCost     The Quota Cost of the Endpoint.
         * @param responseParts The response parts to retrieve from the Endpoint.
         */
        Endpoint(String name, EndpointCategory category, ApiQuota.QuotaCost quotaCost, List<ResponsePart> responseParts) {
            this.name = name;
            this.category = category;
            this.quotaCost = quotaCost;
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
         * Returns the Quota Cost of the Endpoint.
         *
         * @return The Quota Cost of the Endpoint.
         */
        public ApiQuota.QuotaCost getQuotaCost() {
            return quotaCost;
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
        
        CHANNEL("channel", Endpoint.CHANNEL, ApiUtils::fetchChannel, ApiUtils::fetchChannel, ApiUtils::fetchChannelData, ApiUtils::fetchChannelData, ChannelInfo::new),
        PLAYLIST("playlist", Endpoint.PLAYLIST, ApiUtils::fetchPlaylist, ApiUtils::fetchPlaylist, ApiUtils::fetchPlaylistData, ApiUtils::fetchPlaylistData, PlaylistInfo::new),
        VIDEO("video", Endpoint.VIDEO, ApiUtils::fetchVideo, ApiUtils::fetchVideo, ApiUtils::fetchVideoData, ApiUtils::fetchVideoData, VideoInfo::new);
        
        
        //Fields
        
        /**
         * The name of the Entity.
         */
        public final String name;
        
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
         * @param name                    The name of the Entity.
         * @param endpoint                The Endpoint used to fetch the Entity.
         * @param entityLoader            The function that loads the Entity.
         * @param checkedEntityLoader     The function that loads the Entity for a Channel.
         * @param entityDataLoader        The function that loads the json data of the Entity.
         * @param checkedEntityDataLoader The function that loads the json data of the Entity for a Channel.
         * @param entityParser            The function that parses the Entity.
         */
        ApiEntity(String name, Endpoint endpoint,
                CheckedFunction<String, EntityInfo> entityLoader,
                CheckedBiFunction<String, ChannelState, EntityInfo> checkedEntityLoader,
                CheckedFunction<String, Map<String, Object>> entityDataLoader,
                CheckedBiFunction<String, ChannelState, Map<String, Object>> checkedEntityDataLoader,
                CheckedFunction<Map<String, Object>, EntityInfo> entityParser) {
            this.name = name;
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
         * Returns the name of the Entity.
         *
         * @return The name of the Entity.
         */
        public String getName() {
            return name;
        }
        
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
    
    
    //Static Fields
    
    /**
     * A flag indicating whether the API has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Static Methods
    
    /**
     * Initializes the API.
     *
     * @return Whether the API was successfully initialized.
     */
    public static boolean initApi() {
        if (loaded.compareAndSet(false, true)) {
            logger.trace(LogUtils.NEWLINE);
            logger.debug(Color.log("Initializing API..."));
            
            return checkKey() &&
                    checkQuota();
        }
        return false;
    }
    
    /**
     * Determines if the API key is available.
     *
     * @return Whether the API key is available.
     */
    public static boolean checkKey() {
        logger.debug(Color.log("Checking API Key..."));
        
        if (StringUtility.isNullOrBlank(API_KEY)) {
            logger.trace(LogUtils.NEWLINE);
            logger.warn(Color.bad("API Key is required"));
            return false;
        }
        return true;
    }
    
    /**
     * Determines if API key is available.
     *
     * @return Whether the API key is available.
     */
    public static boolean checkQuota() {
        logger.debug(Color.log("Checking API Quota..."));
        
        ApiQuota.initQuota(API_KEY_HASH);
        
        return true;
    }
    
    /**
     * Calls the Youtube Data API and fetches a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The Channel Info.
     */
    public static ChannelInfo fetchChannel(String channelId, ChannelState channelState) {
        return ApiHandler.fetchEntity(ApiEntity.CHANNEL, channelId, channelState, ApiUtils::fetchChannelData);
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
        return ApiHandler.fetchEntity(ApiEntity.PLAYLIST, playlistId, channelState, ApiUtils::fetchPlaylistData);
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
        return ApiHandler.fetchEntity(ApiEntity.VIDEO, videoId, channelState, ApiUtils::fetchVideoData);
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
        return ApiHandler.fetchEntityList(ApiEntity.VIDEO, playlistId, channelState, ApiUtils::fetchPlaylistVideosData);
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
     * @param playlistInfo The Playlist Info of the Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchPlaylistVideos(PlaylistInfo playlistInfo, ChannelState channelState) {
        return fetchPlaylistVideos(playlistInfo.getPlaylistId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Playlist.
     *
     * @param playlistInfo The Playlist Info of the Playlist.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchPlaylistVideos(PlaylistInfo playlistInfo) {
        return fetchPlaylistVideos(playlistInfo, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Playlist.
     *
     * @param playlist     The Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchPlaylistVideos(Playlist playlist, ChannelState channelState) {
        return fetchPlaylistVideos(playlist.getInfo(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Playlist.
     *
     * @param playlist The Playlist.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchPlaylistVideos(Playlist playlist) {
        return fetchPlaylistVideos(playlist, null);
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
        return ApiHandler.fetchEntityListData(Endpoint.PLAYLIST_ITEMS, ApiEntity.VIDEO, playlistId, channelState,
                new HashMap<>(Map.of("playlistId", playlistId)),
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
     * @param playlistInfo The Playlist Info of the Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(PlaylistInfo playlistInfo, ChannelState channelState) {
        return fetchPlaylistVideosData(playlistInfo.getPlaylistId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Playlist.
     *
     * @param playlistInfo The Playlist Info of the Playlist.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(PlaylistInfo playlistInfo) {
        return fetchPlaylistVideosData(playlistInfo, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Playlist.
     *
     * @param playlist     The Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(Playlist playlist, ChannelState channelState) {
        return fetchPlaylistVideosData(playlist.getInfo(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Playlist.
     *
     * @param playlist The Playlist.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(Playlist playlist) {
        return fetchPlaylistVideosData(playlist, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchChannelVideos(String channelId, ChannelState channelState) {
        return ApiHandler.fetchEntityList(ApiEntity.VIDEO, channelId, channelState, ApiUtils::fetchChannelVideosData);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Channel.
     *
     * @param playlistId The id of the Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchChannelVideos(String playlistId) {
        return fetchChannelVideos(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Channel.
     *
     * @param channelInfo  The Channel Info of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchChannelVideos(ChannelInfo channelInfo, ChannelState channelState) {
        return fetchChannelVideos(channelInfo.getChannelId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Channel.
     *
     * @param channelInfo The Channel Info of the Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchChannelVideos(ChannelInfo channelInfo) {
        return fetchChannelVideos(channelInfo, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchChannelVideos(ChannelConfig channelConfig, ChannelState channelState) {
        return channelConfig.isYoutubeChannel() ?
               fetchChannelVideos(channelConfig.getChannelId(), channelState) :
               fetchPlaylistVideos(channelConfig.getPlaylistId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchChannelVideos(ChannelConfig channelConfig) {
        return fetchChannelVideos(channelConfig, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Channel.
     *
     * @param channel The Channel.
     * @return The list of Videos.
     */
    public static List<VideoInfo> fetchChannelVideos(Channel channel) {
        return fetchChannelVideos(channel.getConfig(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchChannelVideosData(String channelId, ChannelState channelState) {
        return fetchPlaylistVideosData(channelId.replaceAll("^UC", "UU"), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Channel.
     *
     * @param channelId The id of the Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchChannelVideosData(String channelId) {
        return fetchChannelVideosData(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Channel.
     *
     * @param channelInfo  The Channel Info of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchChannelVideosData(ChannelInfo channelInfo, ChannelState channelState) {
        return fetchChannelVideosData(channelInfo.getChannelId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Channel.
     *
     * @param channelInfo The Channel Info of the Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchChannelVideosData(ChannelInfo channelInfo) {
        return fetchChannelVideosData(channelInfo, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchChannelVideosData(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchChannelVideosData(channelConfig.getChannelId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchChannelVideosData(ChannelConfig channelConfig) {
        return fetchChannelVideosData(channelConfig, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Channel.
     *
     * @param channel The Channel.
     * @return The json data of the list of Videos.
     */
    public static List<Map<String, Object>> fetchChannelVideosData(Channel channel) {
        return fetchChannelVideosData(channel.getConfig(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The list of Playlists.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(String channelId, ChannelState channelState) {
        return ApiHandler.fetchEntityList(ApiEntity.PLAYLIST, channelId, channelState, ApiUtils::fetchChannelPlaylistsData);
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
     * @param channelInfo  The Channel Info of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The list of Playlists.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(ChannelInfo channelInfo, ChannelState channelState) {
        return fetchChannelPlaylists(channelInfo.getChannelId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelInfo The Channel Info of the Channel.
     * @return The list of Playlists.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(ChannelInfo channelInfo) {
        return fetchChannelPlaylists(channelInfo, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The list of Playlists.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchChannelPlaylists(channelConfig.getChannelId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @return The list of Playlists.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(ChannelConfig channelConfig) {
        return fetchChannelPlaylists(channelConfig, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channel The Channel.
     * @return The list of Playlists.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(Channel channel) {
        return fetchChannelPlaylists(channel.getConfig(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Playlists of a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the list of Playlists.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(String channelId, ChannelState channelState) {
        return ApiHandler.fetchEntityListData(Endpoint.CHANNEL_PLAYLISTS, ApiEntity.PLAYLIST, channelId, channelState,
                new HashMap<>(Map.of("channelId", channelId)),
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
     * @param channelInfo  The Channel Info of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the list of Playlists.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(ChannelInfo channelInfo, ChannelState channelState) {
        return fetchChannelPlaylistsData(channelInfo.getChannelId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Playlists of a Channel.
     *
     * @param channelInfo The Channel Info of the Channel.
     * @return The json data of the list of Playlists.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(ChannelInfo channelInfo) {
        return fetchChannelPlaylistsData(channelInfo, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Playlists of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @param channelState  The Channel State of the calling Channel.
     * @return The json data of the list of Playlists.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(ChannelConfig channelConfig, ChannelState channelState) {
        return fetchChannelPlaylistsData(channelConfig.getChannelId(), channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Playlists of a Channel.
     *
     * @param channelConfig The Channel Config of the Channel.
     * @return The json data of the list of Playlists.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(ChannelConfig channelConfig) {
        return fetchChannelPlaylistsData(channelConfig, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Playlists of a Channel.
     *
     * @param channel The Channel.
     * @return The json data of the list of Playlists.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(Channel channel) {
        return fetchChannelPlaylistsData(channel.getConfig(), channel.getState());
    }
    
    /**
     * Logs an API message.
     *
     * @param channelState The Channel State.
     * @param category     The category.
     * @param message      The log message.
     */
    private static void logApi(ChannelState channelState, String category, String message) {
        logger.trace(formatLog(channelState, category, message));
    }
    
    /**
     * Logs an API message.
     *
     * @param category The category.
     * @param message  The log message.
     */
    private static void logApi(String category, String message) {
        logApi(null, category, message);
    }
    
    /**
     * Logs an API message.
     *
     * @param message The log message.
     */
    private static void logApi(String message) {
        logApi(null, message);
    }
    
    /**
     * Formats an API log message.
     *
     * @param channelState The Channel State.
     * @param category     The category.
     * @param message      The log message.
     */
    private static String formatLog(ChannelState channelState, String category, String message) {
        return String.format(API_LOG_PATTERN,
                Optional.ofNullable(channelState).map(ChannelState::getChannelName).orElse("~"),
                Optional.ofNullable(category).map(String::trim).orElse(""),
                message);
    }
    
    /**
     * Formats an API log message.
     *
     * @param category The category.
     * @param message  The log message.
     */
    private static String formatLog(String category, String message) {
        return formatLog(null, category, message);
    }
    
    /**
     * Formats an API log message.
     *
     * @param message The log message.
     */
    private static String formatLog(String message) {
        return formatLog(null, message);
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
         * @param channelState The Channel State of the calling Channel.
         * @param parameters   A map of parameters.
         * @return The json data of the Entity.
         * @throws RuntimeException When there is an error fetching or parsing the Entity.
         */
        private static Map<String, Object> fetchEntityData(ApiEntity entityType, String entityId, ChannelState channelState, Map<String, String> parameters) {
            return EntityHandler.loadEntityData(entityType, entityId, channelState,
                    (id, state) -> Optional.of(entityType.getEndpoint())
                            .map(Mappers.forEach(e -> logApi(state, entityType.getEndpoint().getName(), ("Fetching " + entityType.getName() + " data for: [" + entityId + "]"))))
                            .map((UncheckedFunction<Endpoint, String>) apiEndpoint ->
                                    callApi(apiEndpoint, parameters, state))
                            .map(Mappers.forEach(e -> logApi(state, entityType.getEndpoint().getName(), ("Parsing " + entityType.getName() + " data for: [" + entityId + "]"))))
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
            return fetchEntityData(entityType, entityId, channelState,
                    new HashMap<>(Map.of("id", entityId)));
        }
        
        /**
         * Calls the Youtube Data API and fetches an Entity.
         *
         * @param entityType   The Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @param <T>          The type of the Entity.
         * @return The Entity.
         * @throws RuntimeException When there is an error fetching or parsing the Entity.
         */
        private static <T extends EntityInfo> T fetchEntity(ApiEntity entityType, String entityId, ChannelState channelState,
                BiFunction<String, ChannelState, Map<String, Object>> entityDataFetcher) {
            return EntityHandler.loadEntity(entityType, entityId, channelState, entityDataFetcher, entityType.getEntityParser());
        }
        
        /**
         * Calls the Youtube Data API and fetches the json data of a list of Entities.
         *
         * @param endpoint              The API Endpoint.
         * @param entityListType        The Type of the listed Entities.
         * @param entityId              The id of the parent Entity.
         * @param channelState          The Channel State of the calling Channel.
         * @param parameters            A map of parameters.
         * @param idExtractor           The function that extracts the id from a response data element.
         * @param entityPageDataFetcher The function that fetches a page of Entity json data from the list of extracted Entity ids.
         * @return The json data of the list of Entities.
         * @throws RuntimeException When there is an error fetching or parsing the list of Entities.
         */
        public static List<Map<String, Object>> fetchEntityListData(Endpoint endpoint, ApiEntity entityListType, String entityId, ChannelState channelState, Map<String, String> parameters,
                UncheckedFunction<Map<String, Object>, String> idExtractor,
                UncheckedFunction<String, String> entityPageDataFetcher) {
            return Optional.of(Optional.ofNullable(
                            loadDataCache(endpoint, channelState))
                    .orElseGet((UncheckedSupplier<List<String>>) () -> {
                        final List<String> pages = new ArrayList<>();
                        do {
                            Optional.of(endpoint)
                                    .map(Mappers.forEach(e -> logApi(channelState, endpoint.getName(), ("Fetching " + endpoint.getName() + " list for: [" + entityId + "] (Page " + (pages.size() + 1) + ")"))))
                                    .map((UncheckedFunction<Endpoint, String>) apiEndpoint ->
                                            callApi(apiEndpoint, parameters, channelState))
                                    .map(response -> parseResponse(response, channelState).stream()
                                            .map(idExtractor)
                                            .filter(id -> !StringUtility.isNullOrBlank(id))
                                            .collect(Collectors.joining(",")))
                                    .map(Mappers.forEach(e -> logApi(channelState, endpoint.getName(), ("Fetching " + endpoint.getName() + " entities for: [" + entityId + "] (Page " + (pages.size() + 1) + ")"))))
                                    .map(entityPageDataFetcher)
                                    .ifPresentOrElse(pages::add, () -> pages.add(null));
                        } while (parameters.get("pageToken") != null);
                        
                        saveDataCache(pages, endpoint, channelState);
                        return pages;
                    })
            ).map(pages -> pages.stream()
                    .filter(Objects::nonNull)
                    .map(Mappers.forEach(e -> logApi(channelState, endpoint.getName(), ("Parsing " + endpoint.getName() + " entities for: [" + entityId + "] (Page " + (pages.indexOf(e) + 1) + ")"))))
                    .flatMap(data -> parseResponse(data, channelState).stream())
                    .map(entityData -> EntityHandler.loadEntityData(entityListType, entityData, channelState))
                    .collect(Collectors.toList())
            ).orElse(List.of());
        }
        
        /**
         * Calls the Youtube Data API and fetches the json data of a list of Entities.
         *
         * @param endpoint              The API Endpoint.
         * @param entityListType        The Type of the listed Entities.
         * @param entityId              The id of the parent Entity.
         * @param channelState          The Channel State of the calling Channel.
         * @param idExtractor           The function that extracts the id from a response data element.
         * @param entityPageDataFetcher The function that fetches a page of Entity json data from the list of extracted Entity ids.
         * @return The json data of the list of Entities.
         * @throws RuntimeException When there is an error fetching or parsing the list of Entities.
         */
        public static List<Map<String, Object>> fetchEntityListData(Endpoint endpoint, ApiEntity entityListType, String entityId, ChannelState channelState,
                UncheckedFunction<Map<String, Object>, String> idExtractor,
                UncheckedFunction<String, String> entityPageDataFetcher) {
            return fetchEntityListData(endpoint, entityListType, entityId, channelState,
                    new HashMap<>(Map.of("id", entityId)),
                    idExtractor, entityPageDataFetcher);
        }
        
        /**
         * Calls the Youtube Data API and fetches a list of Entities.
         *
         * @param entityType          The Type of the Entities.
         * @param entityId            The id of the parent Entity.
         * @param channelState        The Channel State of the calling Channel.
         * @param entitiesDataFetcher The function that fetches a page of Entity json data from the list of extracted Entity ids.
         * @param <T>                 The type of the Entities.
         * @return The list of Entities.
         * @throws RuntimeException When there is an error fetching or parsing the list of Entities.
         */
        private static <T extends EntityInfo> List<T> fetchEntityList(ApiEntity entityType, String entityId, ChannelState channelState,
                BiFunction<String, ChannelState, List<Map<String, Object>>> entitiesDataFetcher) {
            return EntityHandler.loadEntityList(entityType, entityId, channelState, entitiesDataFetcher);
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
                    .filter(File::exists).filter(file -> !Filesystem.isEmpty(file))
                    .map(Mappers.forEach(e -> logApi(channelState, endpoint.getName(), ("Loading local data cache: " + e.getAbsolutePath() + "'"))))
                    .map(Filesystem::readFileToString)
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
                    .map(Mappers.forEach(e -> logApi(channelState, endpoint.getName(), ("Saving local data cache: '" + e.getAbsolutePath() + "'"))))
                    .ifPresent(dataFile -> Filesystem.writeStringToFile(dataFile,
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
                logApi(channelState, endpoint.getName(), ("Calling " + endpoint.getName() + " API... " + ((retry > 0) ? (" (Retry #" + retry + ")") : "")));
                
                final HttpGet request = buildApiRequest(endpoint, new HashMap<>(parameters));
                
                try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
                    response.set(EntityUtils.toString(httpResponse.getEntity()).strip());
                    error.set(httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK);
                } finally {
                    logApiCall(endpoint, request.getURI(), response.get(), error.get(), channelState);
                }
                
                if (!error.get()) {
                    parameters.put("pageToken", Optional.ofNullable(response.get())
                            .map((CheckedFunction<String, Map<String, Object>>) e -> (Map<String, Object>) new JSONParser().parse(response.get()))
                            .map(e -> (String) e.get("nextPageToken")).orElse(null));
                    return response.get();
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
            final String log = formatLog(channelState, endpoint.getName(),
                    (response.length() + " B  " + (error ? "-X" : "->") + "  " + request));
            
            LogUtils.log(logger, (error ? LogUtils.LogLevel.WARN : LogUtils.LogLevel.DEBUG), log);
            Optional.ofNullable(channelState).map(ChannelState::getCallLogFile)
                    .ifPresent(callLog -> Filesystem.writeStringToFile(callLog,
                            (log.replaceAll("^.+:: ", (DateUtils.timestamp() + " - ")) + System.lineSeparator()), true));
            
            Stats.totalApiCalls.incrementAndGet();
            Stats.totalApiEntityCalls.addAndGet((endpoint.getCategory() == EndpointCategory.ENTITY) ? 1 : 0);
            Stats.totalApiDataCalls.addAndGet((endpoint.getCategory() == EndpointCategory.DATA) ? 1 : 0);
            Stats.totalApiFailures.addAndGet(error ? 1 : 0);
            
            ApiQuota.registerApiCall(API_KEY_HASH, endpoint.getQuotaCost());
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
        @SuppressWarnings("unchecked")
        private static String handleResponse(String response, ChannelState channelState) {
            return Optional.ofNullable(response)
                    .filter(e -> e.contains("\"error\": {"))
                    .filter(errorResponse -> {
                        final Map<String, Object> error = Optional.of(errorResponse)
                                .map((CheckedFunction<String, Map<String, Object>>) e -> (Map<String, Object>) new JSONParser().parse(e))
                                .map(e -> (Map<String, Object>) e.get("error")).orElse(null);
                        final Integer code = Optional.ofNullable(error).map(e -> (Long) e.get("code")).map(Long::intValue).orElse(null);
                        final String message = Optional.ofNullable(error).map(e -> (String) e.get("message")).orElse(null);
                        final String reason = Optional.ofNullable(error).map(e -> (List<Map<String, Object>>) e.get("errors"))
                                .map(e -> ListUtility.getOrNull(e, 0)).map(e -> (String) e.get("reason")).orElse(null);
                        
                        switch (Optional.ofNullable(code).orElse(-1)) {
                            case 404:
                                logger.warn(Color.bad("The Youtube source") +
                                        Optional.ofNullable(channelState).map(Color::channelName).map(e -> (Color.bad(" referenced by Channel: ") + e)).orElse("") +
                                        Color.bad(" does not exist"));
                                break;
                            case 403:
                                logger.warn(Color.bad("Your API Key is not authorized or has exceeded its quota"));
                                break;
                            case 400:
                                logger.warn(Color.bad("The API call that was made is invalid"));
                                break;
                            default:
                                logger.warn(Color.bad("Error: ") + Color.number(code) + Color.bad(" while calling API") +
                                        Optional.ofNullable(channelState).map(Color::channelName).map(e -> (Color.bad(" for Channel: ") + e)).orElse(""));
                                break;
                        }
                        logger.error(Color.bad("Youtube Data API responded with error code: ") + Color.number(code) +
                                Optional.ofNullable(reason).map(e -> (" (" + e + ")")).map(Color::bad).orElse("") +
                                System.lineSeparator() + LogUtils.INDENT_HARD + Color.bad(message));
                        
                        Optional.ofNullable(channelState).map(ChannelState::getErrorFlag).ifPresent(errorFlag -> errorFlag.set(true));
                        throw new RuntimeException(new HttpResponseException(Optional.ofNullable(code).orElse(-1), reason));
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
                    .map((CheckedFunction<String, Map<String, Object>>) e -> (Map<String, Object>) new JSONParser().parse(e))
                    .map(e -> (ArrayList<Map<String, Object>>) e.getOrDefault("items", new ArrayList<>()))
                    .orElseThrow(() -> {
                        if (Optional.ofNullable(channelState).map(ChannelState::getErrorFlag).map(errorFlag -> errorFlag.compareAndSet(false, true)).orElse(false)) {
                            logger.warn(Color.bad("Error parsing API data for Channel: ") + Color.channelName(channelState));
                        }
                        logger.error(Color.bad("Youtube Data API responded with invalid data"));
                        return new RuntimeException(new ParseException());
                    });
        }
        
    }
    
    /**
     * Handles Entity loading and caching.
     */
    private static class EntityHandler {
        
        //Constants
        
        /**
         * A flag indicating whether or not to log cache interactions by the Entity Handler.
         */
        private static final boolean LOG_CACHE_INTERACTIONS = false;
        
        
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
         * @param channelState     The Channel State of the calling Channel.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @return The json data of the Entity, or null if it could not be loaded.
         */
        private static Map<String, Object> cacheEntityData(ApiEntity entityType, String entityId, ChannelState channelState,
                Function<String, Map<String, Object>> entityDataLoader) {
            return Optional.ofNullable(entityType).map(entityDataCache::get)
                    .flatMap(cache -> Optional.ofNullable(entityId)
                            .map(id -> {
                                logApiCache(entityType, entityId, channelState, true, cache.containsKey(id));
                                return cache.computeIfAbsent(id, entityDataLoader);
                            }))
                    .orElse(null);
        }
        
        /**
         * Caches an Entity.
         *
         * @param entityType   The Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @param entityLoader The function used to load the Entity.
         * @param <T>          The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        @SuppressWarnings("unchecked")
        private static <T extends EntityInfo> T cacheEntity(ApiEntity entityType, String entityId, ChannelState channelState,
                Function<String, T> entityLoader) {
            return Optional.ofNullable(entityType).map(entityCache::get)
                    .flatMap(cache -> Optional.ofNullable(entityId)
                            .map(id -> {
                                logApiCache(entityType, entityId, channelState, false, cache.containsKey(id));
                                return (T) cache.computeIfAbsent(id, entityLoader);
                            }))
                    .orElse(null);
        }
        
        /**
         * Logs a cache interaction by the Entity Handler.
         *
         * @param entityType   The Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @param data         Whether the interaction was with the Entity data cache.
         * @param cached       Whether the Entity is currently cached.
         */
        private static void logApiCache(ApiEntity entityType, String entityId, ChannelState channelState, boolean data, boolean cached) {
            if (LOG_CACHE_INTERACTIONS) {
                logApi(channelState, "cache", String.join(" ",
                        (cached ? "Retrieving" : "Loading"),
                        (entityType.getName() + (data ? " data" : "")),
                        "[" + entityId + "]"));
            }
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
                Function<String, Map<String, Object>> entityDataLoader) {
            return cacheEntityData(entityType, entityId, channelState, entityDataLoader);
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
            return loadEntityData(entityType, entityId, channelState,
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
         * @param entityType   The Entity Type of the Entity.
         * @param entityData   The json data of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @return The json data of the Entity, or null if it could not be loaded.
         */
        public static Map<String, Object> loadEntityData(ApiEntity entityType, Map<String, Object> entityData, ChannelState channelState) {
            return loadEntityData(entityType, (String) entityData.get("id"), channelState,
                    id -> entityData);
        }
        
        /**
         * Loads an Entity.
         *
         * @param entityType   The Entity Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @param entityLoader The function used to load the Entity.
         * @param <T>          The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        private static <T extends EntityInfo> T loadEntity(ApiEntity entityType, String entityId, ChannelState channelState,
                Function<String, T> entityLoader) {
            return cacheEntity(entityType, entityId, channelState, entityLoader);
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
            return loadEntity(entityType, entityId, channelState,
                    id -> entityDataLoader.apply(id, channelState));
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
                Function<String, Map<String, Object>> entityDataLoader,
                Function<Map<String, Object>, T> entityParser) {
            return loadEntity(entityType, entityId, channelState,
                    id -> Optional.ofNullable(id)
                            .map(entityDataLoader)
                            .map(entityParser)
                            .orElse(null));
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
            return loadEntity(entityType, entityId, channelState,
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
         * @param entityType   The Entity Type of the Entity.
         * @param entityData   The json data of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @param <T>          The type of the Entity.
         * @return The Entity, or null if it could not be loaded.
         */
        public static <T extends EntityInfo> T loadEntity(ApiEntity entityType, Map<String, Object> entityData, ChannelState channelState) {
            return loadEntity(entityType, (String) entityData.get("id"), channelState,
                    id -> loadEntityData(entityType, entityData, channelState),
                    entityType.getEntityParser());
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
                Function<String, List<Map<String, Object>>> entitiesDataLoader) {
            return Optional.ofNullable(entityId)
                    .map(entitiesDataLoader)
                    .map(entityDataList -> entityDataList.stream()
                            .map(entityData -> loadEntityData(entityType, entityData, channelState))
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
            return loadEntityListData(entityType, entityId, channelState,
                    id -> entitiesDataLoader.apply(id, channelState));
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
        @SuppressWarnings("unchecked")
        public static <T extends EntityInfo> List<T> loadEntityList(ApiEntity entityType, String entityId, ChannelState channelState,
                Function<String, List<Map<String, Object>>> entitiesDataLoader) {
            return Optional.ofNullable(loadEntityListData(entityType, entityId, channelState, entitiesDataLoader))
                    .map(entityDataList -> entityDataList.stream()
                            .map(entityData -> (T) loadEntity(entityType, entityData, channelState))
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
            return loadEntityList(entityType, entityId, channelState,
                    id -> entitiesDataLoader.apply(id, channelState));
        }
        
    }
    
}
