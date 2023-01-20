/*
 * File:    ApiUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
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
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * The maximum number of API playlist pages to save to a single file.
     */
    private static final int MAX_PAGES_PER_FILE = 100;
    
    
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
        
        CHANNEL(Endpoint.CHANNEL, ChannelInfo::new),
        PLAYLIST(Endpoint.PLAYLIST, PlaylistInfo::new),
        VIDEO(Endpoint.VIDEO, VideoInfo::new);
        
        
        //Fields
        
        /**
         * The Endpoint used to fetch the Entity.
         */
        public final Endpoint endpoint;
        
        /**
         * The function that parses the Entity.
         */
        public final Function<Map<String, Object>, EntityInfo> entityParser;
        
        
        //Constructors
        
        /**
         * Constructs an API Entity.
         *
         * @param endpoint     The Endpoint used to fetch the Entity.
         * @param entityParser The function that parses the Entity.
         */
        ApiEntity(Endpoint endpoint, Function<Map<String, Object>, EntityInfo> entityParser) {
            this.endpoint = endpoint;
            this.entityParser = entityParser;
        }
        
        
        //Methods
        
        /**
         * Parses an Entity.
         *
         * @param entityData The json data of the Youtube Entity.
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
         * Returns the function that parses the Entity.
         *
         * @return The function that parses the Entity.
         */
        public Function<Map<String, Object>, EntityInfo> getEntityParser() {
            return entityParser;
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
        return EntityHandler.loadEntity(ApiEntity.CHANNEL, channelId, ApiUtils::fetchChannelData, channelState);
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
     * @param channel The Channel.
     * @return The Channel Info.
     */
    public static ChannelInfo fetchChannel(Channel channel) {
        return fetchChannel(channel.getConfig().getChannelId(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the Channel.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchChannelData(String channelId, ChannelState channelState) throws Exception {
        return ApiHandler.fetchEntityData(ApiEntity.CHANNEL, channelId, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel.
     *
     * @param channelId The id of the Channel.
     * @return The json data of the Channel.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchChannelData(String channelId) throws Exception {
        return fetchChannelData(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel.
     *
     * @param channel The Channel.
     * @return The json data of the Channel.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchChannelData(Channel channel) throws Exception {
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
        return EntityHandler.loadEntity(ApiEntity.PLAYLIST, playlistId, ApiUtils::fetchPlaylistData, channelState);
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
     * @param channel The Channel.
     * @return The Playlist Info.
     */
    public static PlaylistInfo fetchPlaylist(Channel channel) {
        return fetchPlaylist(channel.getConfig().getPlaylistId(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist.
     *
     * @param playlistId   The id of the Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the Playlist.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchPlaylistData(String playlistId, ChannelState channelState) throws Exception {
        return ApiHandler.fetchEntityData(ApiEntity.PLAYLIST, playlistId, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist.
     *
     * @param playlistId The id of the Playlist.
     * @return The json data of the Playlist.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchPlaylistData(String playlistId) throws Exception {
        return fetchPlaylistData(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist.
     *
     * @param channel The Channel.
     * @return The json data of the Playlist.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchPlaylistData(Channel channel) throws Exception {
        return fetchPlaylistData(channel.getConfig().getPlaylistId(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches a Video.
     *
     * @param videoId      The id of the Video.
     * @param channelState The Channel State of the calling Channel.
     * @return The Video Info.
     */
    public static VideoInfo fetchVideo(String videoId, ChannelState channelState) throws Exception {
        return EntityHandler.loadEntity(ApiEntity.VIDEO, videoId, ApiUtils::fetchVideoData, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Video.
     *
     * @param videoId The id of the Video.
     * @return The Video Info.
     */
    public static VideoInfo fetchVideo(String videoId) throws Exception {
        return fetchVideo(videoId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Video.
     *
     * @param videoId      The id of the Video.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the Video.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchVideoData(String videoId, ChannelState channelState) throws Exception {
        return ApiHandler.fetchEntityData(ApiEntity.VIDEO, videoId, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Video.
     *
     * @param videoId The id of the Video.
     * @return The json data of the Video.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchVideoData(String videoId) throws Exception {
        return fetchVideoData(videoId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Playlist.
     *
     * @param playlistId   The id of the Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The list of Video Info.
     * @throws Exception When there is an error.
     */
    public static List<VideoInfo> fetchPlaylistVideos(String playlistId, ChannelState channelState) throws Exception {
        return EntityHandler.loadEntities(ApiEntity.VIDEO, playlistId, ApiUtils::fetchPlaylistVideosData, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Playlist.
     *
     * @param playlistId The id of the Playlist.
     * @return The list of Video Info.
     * @throws Exception When there is an error.
     */
    public static List<VideoInfo> fetchPlaylistVideos(String playlistId) throws Exception {
        return fetchPlaylistVideos(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Videos of a Playlist.
     *
     * @param channel The Channel.
     * @return The list of Video Info.
     * @throws Exception When there is an error.
     */
    public static List<VideoInfo> fetchPlaylistVideos(Channel channel) throws Exception {
        return fetchPlaylistVideos(channel.getConfig().getPlaylistId(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Playlist.
     *
     * @param playlistId   The id of the Playlist.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the Videos.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> fetchPlaylistVideosData(String playlistId, ChannelState channelState) throws Exception {
        return ApiHandler.fetchPagedData(Endpoint.PLAYLIST_ITEMS,
                new HashMap<>(Map.of("playlistId", playlistId.replaceAll("^UC", "UU"))),
                e -> Optional.ofNullable((Map<String, Object>) e.get("contentDetails")).map(e2 -> (String) e2.get("videoId")).orElse(null),
                ids -> ApiHandler.callApi(Endpoint.VIDEO, new HashMap<>(Map.of("id", ids)), channelState),
                channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Playlist.
     *
     * @param playlistId The id of the Playlist.
     * @return The json data of the Videos.
     * @throws Exception When there is an error.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(String playlistId) throws Exception {
        return fetchPlaylistVideosData(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Videos of a Playlist.
     *
     * @param channel The Channel.
     * @return The json data of the Videos.
     * @throws Exception When there is an error.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(Channel channel) throws Exception {
        return fetchPlaylistVideosData(channel.getConfig().getPlaylistId(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The list of Playlist Info.
     * @throws Exception When there is an error.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(String channelId, ChannelState channelState) throws Exception {
        return EntityHandler.loadEntities(ApiEntity.PLAYLIST, channelId, ApiUtils::fetchChannelPlaylistsData, channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelId The id of the Channel.
     * @return The list of Playlist Info.
     * @throws Exception When there is an error.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(String channelId) throws Exception {
        return fetchChannelPlaylists(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channel The Channel.
     * @return The list of Playlist Info.
     * @throws Exception When there is an error.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(Channel channel) throws Exception {
        return fetchChannelPlaylists(channel.getConfig().getChannelId(), channel.getState());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Playlists of a Channel.
     *
     * @param channelId    The id of the Channel.
     * @param channelState The Channel State of the calling Channel.
     * @return The json data of the Playlists.
     * @throws Exception When there is an error.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(String channelId, ChannelState channelState) throws Exception {
        return ApiHandler.fetchPagedData(Endpoint.CHANNEL_PLAYLISTS,
                new HashMap<>(Map.of("channelId", channelId.replaceAll("^UU", "UC"))),
                e -> (String) e.get("id"),
                ids -> ApiHandler.callApi(Endpoint.PLAYLIST, new HashMap<>(Map.of("id", ids)), channelState),
                channelState);
    }
    
    /**
     * Calls the Youtube Data API and fetches the Playlists of a Channel.
     *
     * @param channelId The id of the Channel.
     * @return The json data of the Playlists.
     * @throws Exception When there is an error.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(String channelId) throws Exception {
        return fetchChannelPlaylistsData(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of the Playlists of a Channel.
     *
     * @param channel The Channel.
     * @return The json data of the Playlists.
     * @throws Exception When there is an error.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(Channel channel) throws Exception {
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
         * Calls the Youtube Data API and fetches the data of an Entity.
         *
         * @param entityType   The Type of the Entity.
         * @param id           The id of the Entity.
         * @param parameters   A map of parameters.
         * @param channelState The Channel State of the calling Channel.
         * @return The json data of the Entity.
         * @throws Exception When there is an error.
         */
        private static Map<String, Object> fetchEntityData(ApiEntity entityType, String id, Map<String, String> parameters, ChannelState channelState) throws Exception {
            return Optional.ofNullable(entityType).map(ApiEntity::getEndpoint)
                    .map((UncheckedFunction<Endpoint, String>) e ->
                            callApi(e, parameters, channelState))
                    .map(e -> parseResponse(e, channelState))
                    .map(e -> ListUtility.getOrNull(e, 0))
                    .orElse(Map.of());
        }
        
        /**
         * Calls the Youtube Data API and fetches the data of an Entity.
         *
         * @param entityType   The Type of the Entity.
         * @param id           The id of the Entity.
         * @param channelState The Channel State of the calling Channel.
         * @return The json data of the Entity.
         * @throws Exception When there is an error.
         */
        private static Map<String, Object> fetchEntityData(ApiEntity entityType, String id, ChannelState channelState) throws Exception {
            return fetchEntityData(entityType, id, new HashMap<>(Map.of("id", id)), channelState);
        }
        
        /**
         * Calls the Youtube Data API and fetches paged data.
         *
         * @param endpoint          The API Endpoint.
         * @param parameters        A map of parameters.
         * @param idExtractor       The function that extracts the id from a response data element.
         * @param entityDataFetcher The function that fetches a page of Entity json data from the list of extracted Entity ids.
         * @param channelState      The Channel State of the calling Channel.
         * @return The json data of the pages fetched.
         * @throws Exception When there is an error.
         */
        private static List<Map<String, Object>> fetchPagedData(Endpoint endpoint, Map<String, String> parameters, UncheckedFunction<Map<String, Object>, String> idExtractor, UncheckedFunction<String, String> entityDataFetcher, ChannelState channelState) throws Exception {
            return Optional.ofNullable(loadPagedDataCache(endpoint, channelState))
                    .orElseGet((UncheckedSupplier<List<String>>) () -> {
                        final List<String> pages = new ArrayList<>();
                        do {
                            Optional.ofNullable(callApi(endpoint, parameters, channelState))
                                    .map(e -> parseResponse(e, channelState).stream()
                                            .map(idExtractor)
                                            .filter(e2 -> !StringUtility.isNullOrBlank(e2))
                                            .collect(Collectors.joining(",")))
                                    .map(entityDataFetcher)
                                    .ifPresent(pages::add);
                        } while (parameters.get("pageToken") != null);
                        
                        savePagedDataCache(pages, endpoint, channelState);
                        return pages;
                    })
                    .stream()
                    .flatMap(e -> parseResponse(e, channelState).stream())
                    .collect(Collectors.toList());
        }
        
        /**
         * Loads a paged data cache, if present.
         *
         * @param endpoint     The API Endpoint.
         * @param channelState The Channel State of the calling Channel.
         * @return The list of cached pages, or null if it could not be loaded.
         */
        private static List<String> loadPagedDataCache(Endpoint endpoint, ChannelState channelState) {
            return Optional.ofNullable(channelState).map(e -> e.getDataFile(endpoint.getName()))
                    .filter(File::exists).map((CheckedFunction<File, String>) FileUtils::readFileToString)
                    .filter(e -> !StringUtility.isNullOrBlank(e))
                    .map(e -> e.split("(?:^|\r?\n)[\\[,\\]](?:\r?\n|$)"))
                    .map(e -> Arrays.stream(e)
                            .filter(e2 -> !StringUtility.isNullOrBlank(e2))
                            .collect(Collectors.toList()))
                    .filter(e -> !ListUtility.isNullOrEmpty(e))
                    .orElse(null);
        }
        
        /**
         * Saves a paged data cache for a Channel.
         *
         * @param pages        The list of data pages.
         * @param endpoint     The API Endpoint.
         * @param channelState The Channel State of the calling Channel.
         */
        private static void savePagedDataCache(List<String> pages, Endpoint endpoint, ChannelState channelState) {
            Optional.ofNullable(channelState).map(e -> e.getDataFile(endpoint.getName()))
                    .ifPresent((CheckedConsumer<File>) dataFile -> FileUtils.writeStringToFile(dataFile,
                            pages.stream().collect(Collectors.joining(
                                    (System.lineSeparator() + "," + System.lineSeparator()),
                                    ("[" + System.lineSeparator()), (System.lineSeparator() + "]")))));
        }
        
        /**
         * Calls the Youtube Data API.
         *
         * @param endpoint     The API Endpoint.
         * @param parameters   A map of parameters.
         * @param channelState The Channel State of the calling Channel.
         * @return The Entity data.
         * @throws Exception When there is an error.
         */
        private static String callApi(Endpoint endpoint, Map<String, String> parameters, ChannelState channelState) throws Exception {
            final AtomicReference<String> response = new AtomicReference<>(null);
            final AtomicBoolean error = new AtomicBoolean(false);
            
            for (int retry = 0; retry <= MAX_RETRIES; retry++) {
                final HttpGet request = buildApiRequest(endpoint, new HashMap<>(parameters));
                
                try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
                    response.set(EntityUtils.toString(httpResponse.getEntity()).strip());
                    error.set(httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK);
                    
                    Stats.totalApiCalls.incrementAndGet();
                    Stats.totalApiEntityCalls.addAndGet((endpoint.getCategory() == EndpointCategory.ENTITY) ? 1 : 0);
                    Stats.totalApiDataCalls.addAndGet((endpoint.getCategory() == EndpointCategory.DATA) ? 1 : 0);
                    Stats.totalApiFailures.addAndGet(error.get() ? 1 : 0);
                    if (channelState != null) {
                        FileUtils.writeStringToFile(channelState.getCallLogFile(),
                                (StringUtility.padLeft(String.valueOf(response.get().length()), 8) + " bytes " +
                                        (error.get() ? "=XXX=" : "=====") + ' ' + request.getURI() + System.lineSeparator()), true);
                    }
                    
                    if (!error.get()) {
                        parameters.put("pageToken", (String) ((JSONObject) new JSONParser().parse(response.get())).get("nextPageToken"));
                        return response.get();
                    }
                }
            }
            return handleResponse(response.get(), channelState);
        }
        
        /**
         * Builds an API HTTP GET request.
         *
         * @param endpoint   The API Endpoint.
         * @param parameters A map of parameters.
         * @return The API HTTP GET request.
         * @throws Exception When there is an error building the request.
         */
        private static HttpGet buildApiRequest(Endpoint endpoint, Map<String, String> parameters) throws Exception {
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
         * @throws Exception When there is an error encoding the url.
         */
        private static String buildApiUrl(Endpoint endpoint, Map<String, String> parameters) throws Exception {
            return String.join("/", REQUEST_BASE, endpoint.getName()) +
                    buildApiParameterString(parameters);
        }
        
        /**
         * Builds an API parameter string for an API endpoint.
         *
         * @param parameters A map of parameters.
         * @return The API parameter string.
         * @throws Exception When there is an error encoding the parameters.
         */
        private static String buildApiParameterString(Map<String, String> parameters) throws Exception {
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
         * @throws RuntimeException If the response has an error.
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
                                        ((channelState != null) ? (Color.bad(" for Channel: ") + Color.channel(channelState) + Color.bad("; Skipping this run")) : ""));
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
         */
        @SuppressWarnings("unchecked")
        private static List<Map<String, Object>> parseResponse(String response, ChannelState channelState) {
            return Optional.ofNullable(response)
                    .map((CheckedFunction<String, Map<String, Object>>) e ->
                            (Map<String, Object>) new JSONParser().parse(e))
                    .map(e -> (ArrayList<Map<String, Object>>) e.get("items"))
                    .orElseThrow(() -> {
                        if ((channelState != null) && channelState.getErrorFlag().compareAndSet(false, true)) {
                            System.out.println(Color.bad("Error parsing API data for Channel: ") + Color.channel(channelState) + Color.bad("; Skipping this run"));
                        }
                        throw new RuntimeException("Youtube Data API responded with invalid data");
                    });
        }
        
    }
    
    /**
     * Handles Youtube Entity loading and caching.
     */
    private static class EntityHandler {
        
        //Static Fields
        
        /**
         * A cache of previously fetched Entities.
         */
        private static final Map<ApiEntity, Map<String, EntityInfo>> entityCache = Arrays.stream(ApiEntity.values())
                .collect(MapCollectors.mapEachTo(() -> new HashMap<>()));
        
        
        //Static Methods
        
        /**
         * Caches a Youtube Entity.
         *
         * @param entityType   The Type of the Entity.
         * @param entityId     The id of the Entity.
         * @param entityLoader The function used to load the Entity.
         * @return The Youtube Entity, or null if it could not be loaded.
         */
        @SuppressWarnings("unchecked")
        private static <T extends EntityInfo> T cacheEntity(ApiEntity entityType, String entityId, CheckedFunction<String, T> entityLoader) {
            return Optional.ofNullable(entityId)
                    .map(e -> (T) entityCache.get(entityType).computeIfAbsent(entityId, entityLoader))
                    .orElse(null);
        }
        
        /**
         * Loads a Youtube Entity.
         *
         * @param entityType       The Entity Type of the Entity.
         * @param entityId         The id of the Entity.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @param channelState     The Channel State of the calling Channel.
         * @param <T>              The type of the Entity.
         * @return The Youtube Entity, or null if it could not be loaded.
         */
        private static <T extends EntityInfo> T loadEntity(ApiEntity entityType, String entityId, CheckedBiFunction<String, ChannelState, Map<String, Object>> entityDataLoader, ChannelState channelState) {
            return cacheEntity(entityType, entityId, id -> entityType.parse(entityDataLoader.apply(id, channelState)));
        }
        
        /**
         * Loads a Youtube Entity.
         *
         * @param entityType       The Entity Type of the Entity.
         * @param entityId         The id of the Entity.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @param <T>              The type of the Entity.
         * @return The Youtube Entity, or null if it could not be loaded.
         */
        private static <T extends EntityInfo> T loadEntity(ApiEntity entityType, String entityId, CheckedFunction<String, Map<String, Object>> entityDataLoader) {
            return loadEntity(entityType, entityId, (id, state) -> entityDataLoader.apply(id), null);
        }
        
        /**
         * Loads a Youtube Entity.
         *
         * @param entityType The Entity Type of the Entity.
         * @param entityData The json data of the Entity.
         * @param <T>        The type of the Entity.
         * @return The Youtube Entity.
         */
        private static <T extends EntityInfo> T loadEntity(ApiEntity entityType, Map<String, Object> entityData) {
            return loadEntity(entityType, (String) entityData.get("id"), id -> entityData);
        }
        
        /**
         * Loads a list of Youtube Entities.
         *
         * @param entityType       The Entity Type of the Entities.
         * @param entityId         The id of the parent Entity.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @param channelState     The Channel State of the calling Channel.
         * @param <T>              The type of the Entities.
         * @return The list of Youtube Entities, or null it could not be loaded.
         */
        @SuppressWarnings("unchecked")
        private static <T extends EntityInfo> List<T> loadEntities(ApiEntity entityType, String entityId, CheckedBiFunction<String, ChannelState, List<Map<String, Object>>> entityDataLoader, ChannelState channelState) {
            return Optional.ofNullable(entityId)
                    .map(id -> entityDataLoader.apply(id, channelState))
                    .map(entityDataList -> entityDataList.stream()
                            .map(entityData -> (T) loadEntity(entityType, entityData))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()))
                    .orElse(null);
        }
        
        /**
         * Loads a list of Youtube Entities.
         *
         * @param entityType       The Entity Type of the Entities.
         * @param entityId         The id of the parent Entity.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @param <T>              The type of the Entities.
         * @return The list of Youtube Entities, or null it could not be loaded.
         */
        private static <T extends EntityInfo> List<T> loadEntities(ApiEntity entityType, String entityId, CheckedFunction<String, List<Map<String, Object>>> entityDataLoader) {
            return loadEntities(entityType, entityId, (id, state) -> entityDataLoader.apply(id), null);
        }
        
    }
    
}
