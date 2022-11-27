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
import java.util.function.BiFunction;
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
import youtube.channel.Channel;
import youtube.conf.Color;
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
        
    }
    
    /**
     * An enumeration of Youtube Entity Types.
     */
    public enum EntityType {
        
        //Values
        
        CHANNEL(Endpoint.CHANNEL, (data, channel) -> new ChannelInfo(data, channel)),
        PLAYLIST(Endpoint.PLAYLIST, (data, channel) -> new PlaylistInfo(data, channel)),
        VIDEO(Endpoint.VIDEO, (data, channel) -> new VideoInfo(data, channel));
        
        
        //Fields
        
        /**
         * The Endpoint used to fetch an Entity of the Entity Type.
         */
        public final Endpoint endpoint;
        
        /**
         * The function that parses an Entity of the Entity Type.
         */
        public final BiFunction<Map<String, Object>, Channel, EntityInfo> entityParser;
        
        
        //Constructors
        
        /**
         * Constructs an EntityType.
         *
         * @param endpoint     The Endpoint used to fetch an Entity of the Entity Type.
         * @param entityParser The function that parses an Entity of the Entity Type.
         */
        EntityType(Endpoint endpoint, BiFunction<Map<String, Object>, Channel, EntityInfo> entityParser) {
            this.endpoint = endpoint;
            this.entityParser = entityParser;
        }
        
        
        //Methods
        
        /**
         * Parses an Entity of the Entity Type.
         *
         * @param entityData The json data of the Youtube Entity.
         * @param channel    The Channel.
         * @param <T>        The type of the Entity.
         * @return The parsed Entity, or null if it could not be parsed.
         */
        @SuppressWarnings("unchecked")
        public <T extends EntityInfo> T parse(Map<String, Object> entityData, Channel channel) {
            return (T) entityParser.apply(entityData, channel);
        }
        
        /**
         * Parses an Entity of the Entity Type.
         *
         * @param entityData The json data of the Youtube Entity.
         * @param <T>        The type of the Entity.
         * @return The parsed Entity, or null if it could not be parsed.
         */
        public <T extends EntityInfo> T parse(Map<String, Object> entityData) {
            return parse(entityData, null);
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
        
    }
    
    
    //Static Methods
    
    /**
     * Calls the Youtube Data API and fetches a Channel Entity.
     *
     * @param channelId The Youtube id of the Channel Entity.
     * @param channel   The parent Channel.
     * @return The Channel Entity, or null if the Channel Entity cannot be fetched.
     */
    public static ChannelInfo fetchChannel(String channelId, Channel channel) {
        return EntityHandler.loadEntity(EntityType.CHANNEL, channelId, ApiUtils::fetchChannelData, channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Channel Entity.
     *
     * @param channelId The Youtube id of the Channel Entity.
     * @return The Channel Entity, or null if the Channel Entity cannot be fetched.
     */
    public static ChannelInfo fetchChannel(String channelId) {
        return fetchChannel(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Channel Entity.
     *
     * @param channel The parent Channel.
     * @return The Channel Entity, or null if the Channel Entity cannot be fetched.
     */
    public static ChannelInfo fetchChannel(Channel channel) {
        return fetchChannel(channel.getChannelId(), channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel Entity.
     *
     * @param channelId The Youtube id of the Channel Entity.
     * @param channel   The parent Channel.
     * @return The json data of the Channel Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchChannelData(String channelId, Channel channel) throws Exception {
        return ApiHandler.fetchEntityData(EntityType.CHANNEL, channelId, channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel Entity.
     *
     * @param channelId The Youtube id of the Channel Entity.
     * @return The json data of the Channel Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchChannelData(String channelId) throws Exception {
        return fetchChannelData(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel Entity.
     *
     * @param channel The parent Channel.
     * @return The json data of the Channel Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchChannelData(Channel channel) throws Exception {
        return fetchChannelData(channel.getChannelId(), channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Playlist Entity.
     *
     * @param playlistId The Youtube id of the Playlist Entity.
     * @param channel    The parent Channel.
     * @return The Playlist Entity, or null if the Playlist Entity cannot be fetched.
     */
    public static PlaylistInfo fetchPlaylist(String playlistId, Channel channel) {
        return EntityHandler.loadEntity(EntityType.PLAYLIST, playlistId, ApiUtils::fetchPlaylistData, channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Playlist Entity.
     *
     * @param playlistId The Youtube id of the Playlist Entity.
     * @return The Playlist Entity, or null if the Playlist Entity cannot be fetched.
     */
    public static PlaylistInfo fetchPlaylist(String playlistId) {
        return fetchPlaylist(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Playlist Entity.
     *
     * @param channel The parent Channel.
     * @return The Playlist Entity, or null if the Playlist Entity cannot be fetched.
     */
    public static PlaylistInfo fetchPlaylist(Channel channel) {
        return fetchPlaylist(channel.getPlaylistId(), channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist Entity.
     *
     * @param playlistId The Youtube id of the Playlist Entity.
     * @param channel    The parent Channel.
     * @return The json data of the Playlist Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchPlaylistData(String playlistId, Channel channel) throws Exception {
        return ApiHandler.fetchEntityData(EntityType.PLAYLIST, playlistId, channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist Entity.
     *
     * @param playlistId The Youtube id of the Playlist Entity.
     * @return The json data of the Playlist Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchPlaylistData(String playlistId) throws Exception {
        return fetchPlaylistData(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist Entity.
     *
     * @param channel The parent Channel.
     * @return The json data of the Playlist Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchPlaylistData(Channel channel) throws Exception {
        return fetchPlaylistData(channel.getPlaylistId(), channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Video Entity.
     *
     * @param videoId The Youtube id of the Video Entity.
     * @param channel The parent Channel.
     * @return The Video Entity, or null if the Video Entity cannot be fetched.
     */
    public static VideoInfo fetchVideo(String videoId, Channel channel) {
        return EntityHandler.loadEntity(EntityType.VIDEO, videoId, ApiUtils::fetchVideoData, channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches a Video Entity.
     *
     * @param videoId The Youtube id of the Video Entity.
     * @return The Video Entity, or null if the Video Entity cannot be fetched.
     */
    public static VideoInfo fetchVideo(String videoId) {
        return fetchVideo(videoId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Video Entity.
     *
     * @param videoId The Youtube id of the Video Entity.
     * @param channel The parent Channel.
     * @return The json data of the Video Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchVideoData(String videoId, Channel channel) throws Exception {
        return ApiHandler.fetchEntityData(EntityType.VIDEO, videoId, channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Video Entity.
     *
     * @param videoId The Youtube id of the Video Entity.
     * @return The json data of the Video Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchVideoData(String videoId) throws Exception {
        return fetchVideoData(videoId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the list of Video Entities for a Playlist Entity.
     *
     * @param playlistId The Youtube id of the Playlist Entity.
     * @param channel    The parent Channel.
     * @return The list of Video Entities.
     * @throws Exception When there is an error.
     */
    public static List<VideoInfo> fetchPlaylistVideos(String playlistId, Channel channel) throws Exception {
        return EntityHandler.loadEntities(EntityType.VIDEO, playlistId, ApiUtils::fetchPlaylistVideosData, channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the list of Video Entities for a Playlist Entity.
     *
     * @param playlistId The Youtube id of the Playlist Entity.
     * @return The list of Video Entities.
     * @throws Exception When there is an error.
     */
    public static List<VideoInfo> fetchPlaylistVideos(String playlistId) throws Exception {
        return fetchPlaylistVideos(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the list of Video Entities for a Playlist Entity.
     *
     * @param channel The parent Channel.
     * @return The list of Video Entities.
     * @throws Exception When there is an error.
     */
    public static List<VideoInfo> fetchPlaylistVideos(Channel channel) throws Exception {
        return fetchPlaylistVideos(channel.getPlaylistId(), channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a list of Video Entities for a Playlist Entity.
     *
     * @param playlistId The Youtube id of the Playlist Entity.
     * @param channel    The parent Channel.
     * @return The json data of the list of Video Entries.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> fetchPlaylistVideosData(String playlistId, Channel channel) throws Exception {
        return ApiHandler.fetchPagedData(Endpoint.PLAYLIST_ITEMS,
                new HashMap<>(Map.of("playlistId", playlistId.replaceAll("^UC", "UU"))),
                e -> Optional.ofNullable((Map<String, Object>) e.get("contentDetails")).map(e2 -> (String) e2.get("videoId")).orElse(null),
                ids -> ApiHandler.callApi(Endpoint.VIDEO, new HashMap<>(Map.of("id", ids)), channel),
                channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a list of Video Entities for a Playlist Entity.
     *
     * @param playlistId The Youtube id of the Playlist Entity.
     * @return The json data of the list of Video Entries.
     * @throws Exception When there is an error.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(String playlistId) throws Exception {
        return fetchPlaylistVideosData(playlistId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a list of Video Entities for a Playlist Entity.
     *
     * @param channel The parent Channel.
     * @return The json data of the list of Video Entries.
     * @throws Exception When there is an error.
     */
    public static List<Map<String, Object>> fetchPlaylistVideosData(Channel channel) throws Exception {
        return fetchPlaylistVideosData(channel.getPlaylistId(), channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the list of Playlist Entities for a Channel Entity.
     *
     * @param channelId The Youtube id of the Channel Entity.
     * @param channel   The parent Channel.
     * @return The list of Playlist Entities.
     * @throws Exception When there is an error.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(String channelId, Channel channel) throws Exception {
        return EntityHandler.loadEntities(EntityType.PLAYLIST, channelId, ApiUtils::fetchChannelPlaylistsData, channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the list of Playlist Entities for a Channel Entity.
     *
     * @param channelId The Youtube id of the Channel Entity.
     * @return The list of Playlist Entities.
     * @throws Exception When there is an error.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(String channelId) throws Exception {
        return fetchChannelPlaylists(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the list of Playlist Entities for a Channel Entity.
     *
     * @param channel The parent Channel.
     * @return The list of Playlist Entities.
     * @throws Exception When there is an error.
     */
    public static List<PlaylistInfo> fetchChannelPlaylists(Channel channel) throws Exception {
        return fetchChannelPlaylists(channel.getChannelId(), channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a list of Playlist Entities for a Channel Entity.
     *
     * @param channelId The Youtube id of the Channel Entity.
     * @param channel   The parent Channel.
     * @return The json data of the list of Playlist Entries.
     * @throws Exception When there is an error.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(String channelId, Channel channel) throws Exception {
        return ApiHandler.fetchPagedData(Endpoint.CHANNEL_PLAYLISTS,
                new HashMap<>(Map.of("channelId", channelId.replaceAll("^UU", "UC"))),
                e -> (String) e.get("id"),
                ids -> ApiHandler.callApi(Endpoint.PLAYLIST, new HashMap<>(Map.of("id", ids)), channel),
                channel);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a list of Playlist Entities for a Channel Entity.
     *
     * @param channelId The Youtube id of the Channel Entity.
     * @return The json data of the list of Playlist Entries.
     * @throws Exception When there is an error.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(String channelId) throws Exception {
        return fetchChannelPlaylistsData(channelId, null);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a list of Playlist Entities for a Channel Entity.
     *
     * @param channel The parent Channel.
     * @return The json data of the list of Playlist Entries.
     * @throws Exception When there is an error.
     */
    public static List<Map<String, Object>> fetchChannelPlaylistsData(Channel channel) throws Exception {
        return fetchChannelPlaylistsData(channel.getChannelId(), channel);
    }
    
    /**
     * Clears the fetched Entity cache.
     */
    public static void clearCache() {
        Arrays.stream(EntityType.values())
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
         * @param entityType The Type of the Entity.
         * @param id         The id of the Entity.
         * @param parameters A map of parameters.
         * @param channel    The parent Channel.
         * @return The json data of the Entity.
         * @throws Exception When there is an error.
         */
        private static Map<String, Object> fetchEntityData(EntityType entityType, String id, Map<String, String> parameters, Channel channel) throws Exception {
            return Optional.ofNullable(callApi(entityType.endpoint, parameters, channel))
                    .map(e -> parseResponse(e, channel))
                    .map(e -> ListUtility.getOrNull(e, 0))
                    .orElse(Map.of());
        }
        
        /**
         * Calls the Youtube Data API and fetches the data of an Entity.
         *
         * @param entityType The Type of the Entity.
         * @param id         The id of the Entity.
         * @param channel    The parent Channel.
         * @return The json data of the Entity.
         * @throws Exception When there is an error.
         */
        private static Map<String, Object> fetchEntityData(EntityType entityType, String id, Channel channel) throws Exception {
            return fetchEntityData(entityType, id, new HashMap<>(Map.of("id", id)), channel);
        }
        
        /**
         * Calls the Youtube Data API and fetches paged data.
         *
         * @param endpoint          The API Endpoint.
         * @param parameters        A map of parameters.
         * @param idExtractor       The function that extracts an Entity id from a response data element.
         * @param entityDataFetcher The function that fetches a page of Entity json data from the list of extracted Entity ids.
         * @param channel           The parent Channel.
         * @return The json data of the pages fetched.
         * @throws Exception When there is an error.
         */
        private static List<Map<String, Object>> fetchPagedData(Endpoint endpoint, Map<String, String> parameters, UncheckedFunction<Map<String, Object>, String> idExtractor, UncheckedFunction<String, String> entityDataFetcher, Channel channel) throws Exception {
            return Optional.ofNullable(loadPagedDataCache(endpoint, channel))
                    .orElseGet((UncheckedSupplier<List<String>>) () -> {
                        final List<String> pages = new ArrayList<>();
                        do {
                            Optional.ofNullable(callApi(endpoint, parameters, channel))
                                    .map(e -> parseResponse(e, channel).stream()
                                            .map(idExtractor)
                                            .filter(e2 -> !StringUtility.isNullOrBlank(e2))
                                            .collect(Collectors.joining(",")))
                                    .map(entityDataFetcher)
                                    .ifPresent(pages::add);
                        } while (parameters.get("pageToken") != null);
                        
                        savePagedDataCache(pages, endpoint, channel);
                        return pages;
                    })
                    .stream()
                    .flatMap(e -> parseResponse(e, channel).stream())
                    .collect(Collectors.toList());
        }
        
        /**
         * Loads a paged data cache, if present.
         *
         * @param endpoint The API Endpoint.
         * @param channel  The parent Channel.
         * @return The list of cached pages, or null if not loaded.
         */
        private static List<String> loadPagedDataCache(Endpoint endpoint, Channel channel) {
            return Optional.ofNullable(channel).map(e -> e.state.getDataFile(endpoint.name))
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
         * @param pages    The list of data pages.
         * @param endpoint The API Endpoint.
         * @param channel  The parent Channel.
         */
        private static void savePagedDataCache(List<String> pages, Endpoint endpoint, Channel channel) {
            Optional.ofNullable(channel).map(e -> e.state.getDataFile(endpoint.name))
                    .ifPresent((CheckedConsumer<File>) dataFile -> FileUtils.writeStringToFile(dataFile,
                            pages.stream().collect(Collectors.joining(
                                    (System.lineSeparator() + "," + System.lineSeparator()),
                                    ("[" + System.lineSeparator()), (System.lineSeparator() + "]")))));
        }
        
        /**
         * Calls the Youtube Data API.
         *
         * @param endpoint   The API Endpoint.
         * @param parameters A map of parameters.
         * @param channel    The parent Channel.
         * @return The Entity data.
         * @throws Exception When there is an error.
         */
        private static String callApi(Endpoint endpoint, Map<String, String> parameters, Channel channel) throws Exception {
            final AtomicReference<String> response = new AtomicReference<>(null);
            final AtomicBoolean error = new AtomicBoolean(false);
            
            for (int retry = 0; retry <= MAX_RETRIES; retry++) {
                final HttpGet request = buildApiRequest(endpoint, new HashMap<>(parameters));
                
                try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
                    response.set(EntityUtils.toString(httpResponse.getEntity()).strip());
                    error.set(httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK);
                    
                    Stats.totalApiCalls.incrementAndGet();
                    Stats.totalApiEntityCalls.addAndGet((endpoint.category == EndpointCategory.ENTITY) ? 1 : 0);
                    Stats.totalApiDataCalls.addAndGet((endpoint.category == EndpointCategory.DATA) ? 1 : 0);
                    Stats.totalApiFailures.addAndGet(error.get() ? 1 : 0);
                    if (channel != null) {
                        FileUtils.writeStringToFile(channel.state.callLogFile,
                                (StringUtility.padLeft(String.valueOf(response.get().length()), 8) + " bytes " +
                                        (error.get() ? "=XXX=" : "=====") + ' ' + request.getURI() + System.lineSeparator()), true);
                    }
                    
                    if (!error.get()) {
                        parameters.put("pageToken", (String) ((JSONObject) new JSONParser().parse(response.get())).get("nextPageToken"));
                        return response.get();
                    }
                }
            }
            return handleResponse(response.get(), channel);
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
            parameters.putIfAbsent("part", endpoint.responseParts.stream().map(e -> e.name).collect(Collectors.joining(",")));
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
            return String.join("/", REQUEST_BASE, endpoint.name) +
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
         * @param response The response.
         * @param channel  The parent Channel.
         * @return The response.
         * @throws RuntimeException If the response has an error.
         */
        private static String handleResponse(String response, Channel channel) {
            return Optional.ofNullable(response)
                    .filter(e -> e.contains("\"error\": {"))
                    .filter(e -> e.contains("\"code\":"))
                    .map(e -> e.replaceAll("(?s)^.*\"code\":\\s*(\\d+).*$", "$1"))
                    .filter(e -> !StringUtility.isNullOrBlank(e))
                    .filter(errorCode -> {
                        switch (errorCode) {
                            case "404":
                                System.out.println(Color.bad("The Youtube source") +
                                        ((channel != null) ? (Color.bad(" referenced by Channel: ") + Color.channel(channel.getName())) : "") +
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
                                        ((channel != null) ? (Color.bad(" for Channel: ") + Color.channel(channel.getName()) + Color.bad("; Skipping this run")) : ""));
                                break;
                        }
                        if (channel != null) {
                            channel.error.set(true);
                        }
                        throw new RuntimeException("Youtube Data API responded with error code: " + errorCode +
                                (response.contains("\"reason\":") ? (" (" + response.replaceAll("(?s)^.*\"reason\": \"([^\"]+)\",.*$", "$1") + ")") : ""));
                    })
                    .orElse(response);
        }
        
        /**
         * Parses the data of a response from the Youtube Data API.
         *
         * @param response The response.
         * @param channel  The parent Channel.
         * @return The parsed data from the response.
         */
        @SuppressWarnings("unchecked")
        private static List<Map<String, Object>> parseResponse(String response, Channel channel) {
            return Optional.ofNullable(response)
                    .map((CheckedFunction<String, Map<String, Object>>) e ->
                            (Map<String, Object>) new JSONParser().parse(e))
                    .map(e -> (ArrayList<Map<String, Object>>) e.get("items"))
                    .orElseThrow(() -> {
                        if ((channel != null) && channel.error.compareAndSet(false, true)) {
                            System.out.println(Color.bad("Error parsing data for Channel: ") + Color.channel(channel.getName()) + Color.bad("; Skipping this run"));
                        }
                        throw new RuntimeException("Youtube Data API responded with invalid data");
                    });
        }
        
    }
    
    /**
     * Handles Youtube Entity loading and caching.
     */
    public static class EntityHandler {
        
        //Static Fields
        
        /**
         * A cache of previously fetched Entities.
         */
        public static final Map<EntityType, Map<String, EntityInfo>> entityCache = Arrays.stream(EntityType.values())
                .collect(MapCollectors.mapEachTo(() -> new HashMap<>()));
        
        
        //Static Methods
        
        /**
         * Caches a Youtube Entity.
         *
         * @param entityType   The Type of the Entity.
         * @param entityId     The Youtube id of the Entity.
         * @param entityLoader The function used to load the Entity.
         * @return The Youtube Entity.
         */
        @SuppressWarnings("unchecked")
        private static <T extends EntityInfo> T cacheEntity(EntityType entityType, String entityId, CheckedFunction<String, T> entityLoader) {
            return Optional.ofNullable(entityId)
                    .map(e -> (T) entityCache.get(entityType).computeIfAbsent(entityId, entityLoader))
                    .orElse(null);
        }
        
        /**
         * Loads a Youtube Entity.
         *
         * @param entityType       The Type of the Entity.
         * @param entityId         The Youtube id of the Entity.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @param channel          The parent Channel.
         * @param <T>              The type of the Entity.
         * @return The Youtube Entity.
         */
        private static <T extends EntityInfo> T loadEntity(EntityType entityType, String entityId, CheckedBiFunction<String, Channel, Map<String, Object>> entityDataLoader, Channel channel) {
            return cacheEntity(entityType, entityId, id -> entityType.parse(entityDataLoader.apply(id, channel), channel));
        }
        
        /**
         * Loads a Youtube Entity.
         *
         * @param entityType The Type of the Entity.
         * @param entityData The json data of the Entity.
         * @param channel    The parent Channel.
         * @param <T>        The type of the Entity.
         * @return The Youtube Entity.
         */
        private static <T extends EntityInfo> T loadEntity(EntityType entityType, Map<String, Object> entityData, Channel channel) {
            return loadEntity(entityType, (String) entityData.get("id"), (e, e2) -> entityData, channel);
        }
        
        /**
         * Loads a list of Youtube Entities.
         *
         * @param entityType       The Type of the Entities.
         * @param entityId         The Youtube id of the parent Entity.
         * @param entityDataLoader The function used to load the json data of the Entity.
         * @param channel          The parent Channel.
         * @param <T>              The type of the Entities.
         * @return The list of Youtube Entities.
         */
        @SuppressWarnings("unchecked")
        private static <T extends EntityInfo> List<T> loadEntities(EntityType entityType, String entityId, CheckedBiFunction<String, Channel, List<Map<String, Object>>> entityDataLoader, Channel channel) {
            return entityDataLoader.apply(entityId, channel).stream()
                    .map(e -> (T) loadEntity(entityType, e, channel))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        
    }
    
}
