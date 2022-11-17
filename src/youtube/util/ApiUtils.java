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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import commons.lambda.function.checked.CheckedFunction;
import commons.lambda.function.unchecked.UncheckedFunction;
import commons.object.string.StringUtility;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
import youtube.channel.entity.Playlist;
import youtube.channel.entity.Video;
import youtube.channel.entity.base.Entity;
import youtube.conf.Color;
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
    
    /**
     * An enumeration of Youtube Data API Endpoints.
     */
    public enum Endpoint {
        
        //Values
        
        CHANNEL("channels", EndpointCategory.ENTITY, List.of(ResponsePart.SNIPPET, ResponsePart.STATUS, ResponsePart.STATISTICS, ResponsePart.TOPIC_DETAILS)),
        PLAYLIST("playlists", EndpointCategory.ENTITY, List.of(ResponsePart.SNIPPET, ResponsePart.CONTENT_DETAILS, ResponsePart.STATUS, ResponsePart.PLAYER)),
        VIDEO("videos", EndpointCategory.ENTITY, List.of(ResponsePart.SNIPPET, ResponsePart.CONTENT_DETAILS, ResponsePart.STATUS, ResponsePart.STATISTICS, ResponsePart.TOPIC_DETAILS, ResponsePart.RECORDING_DETAILS, ResponsePart.PLAYER)),
        
        PLAYLIST_ITEMS("playlistItems", EndpointCategory.DATA, List.of(ResponsePart.SNIPPET, ResponsePart.CONTENT_DETAILS)),
        CHANNEL_PLAYLISTS("playlists", EndpointCategory.DATA, List.of(ResponsePart.SNIPPET, ResponsePart.CONTENT_DETAILS));
        
        
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
    
    
    //Static Fields
    
    /**
     * The HTTP Client used to interact with the Youtube API.
     */
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    
    /**
     * A cache of previously fetched Entities.
     */
    private static final Map<String, Entity> entityCache = new ConcurrentHashMap<>();
    
    
    //Static Methods
    
    /**
     * Calls the Youtube Data API and fetches a Channel Entity.
     *
     * @param channelId The Youtube id of the Channel Entity.
     * @return The Channel Entity, or null if the Channel Entity cannot be fetched.
     */
    public static youtube.channel.entity.Channel fetchChannel(String channelId) {
        return (youtube.channel.entity.Channel) entityCache.computeIfAbsent(channelId,
                (CheckedFunction<String, Entity>) id -> new youtube.channel.entity.Channel(fetchChannelData(id)));
    }
    
    /**
     * Calls the Youtube Data API and fetches a Playlist Entity.
     *
     * @param playlistId The Youtube id of the Playlist Entity.
     * @return The Playlist Entity, or null if the Playlist Entity cannot be fetched.
     */
    public static Playlist fetchPlaylist(String playlistId) {
        return (Playlist) entityCache.computeIfAbsent(playlistId,
                (CheckedFunction<String, Entity>) id -> new Playlist(fetchPlaylistData(id)));
    }
    
    /**
     * Calls the Youtube Data API and fetches a Video Entity.
     *
     * @param videoId The Youtube id of the Video Entity.
     * @return The Video Entity, or null if the Video Entity cannot be fetched.
     */
    public static Video fetchVideo(String videoId) {
        return (Video) entityCache.computeIfAbsent(videoId,
                (CheckedFunction<String, Entity>) id -> new Video(fetchVideoData(id)));
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Channel Entity.
     *
     * @param channelId The Youtube id of the Channel Entity.
     * @return The json data of the Channel Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchChannelData(String channelId) throws Exception {
        return fetchEntityData(channelId, Endpoint.CHANNEL);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Playlist Entity.
     *
     * @param playlistId The Youtube id of the Playlist Entity.
     * @return The json data of the Playlist Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchPlaylistData(String playlistId) throws Exception {
        return fetchEntityData(playlistId, Endpoint.PLAYLIST);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of a Video Entity.
     *
     * @param videoId The Youtube id of the Video Entity.
     * @return The json data of the Video Entity.
     * @throws Exception When there is an error.
     */
    public static Map<String, Object> fetchVideoData(String videoId) throws Exception {
        return fetchEntityData(videoId, Endpoint.VIDEO);
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of an Entity.
     *
     * @param id         The id of the Entity.
     * @param endpoint   The API Endpoint.
     * @param parameters A map of parameters.
     * @return The json data of the Entity.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> fetchEntityData(String id, Endpoint endpoint, Map<String, String> parameters) throws Exception {
        return Optional.ofNullable(callApi(endpoint, parameters))
                .map((CheckedFunction<String, JSONObject>) e ->
                        (JSONObject) new JSONParser().parse(e))
                .map(e -> (JSONArray) e.get("items"))
                .map(e -> (JSONObject) e.get(0))
                .map(e -> (Map<String, Object>) e).orElse(new HashMap<>());
    }
    
    /**
     * Calls the Youtube Data API and fetches the data of an Entity.
     *
     * @param id       The id of the Entity.
     * @param endpoint The API Endpoint.
     * @return The json data of the Entity.
     * @throws Exception When there is an error.
     */
    private static Map<String, Object> fetchEntityData(String id, Endpoint endpoint) throws Exception {
        return fetchEntityData(id, endpoint, new HashMap<>(Map.ofEntries(
                Map.entry("id", id))));
    }
    
    /**
     * Calls the Youtube Data API and fetches videos for a Channel.
     *
     * @param channel The Channel.
     * @return The number of data pages fetched.
     * @throws Exception When there is an error.
     */
    public static int fetchChannelVideoData(Channel channel) throws Exception {
        return fetchChunkedData(channel, Endpoint.PLAYLIST_ITEMS, new HashMap<>(Map.ofEntries(
                Map.entry("playlistId", channel.getPlaylistId()))));
    }
    
    /**
     * Calls the Youtube Data API and fetches playlists for a Channel.
     *
     * @param channel The Channel.
     * @return The number of data pages fetched.
     * @throws Exception When there is an error.
     */
    public static int fetchChannelPlaylistData(Channel channel) throws Exception {
        return !channel.isYoutubeChannel() ? -1 :
               fetchChunkedData(channel, Endpoint.CHANNEL_PLAYLISTS, new HashMap<>(Map.ofEntries(
                       Map.entry("channelId", channel.getPlaylistId().replaceAll("^UU", "UC")))));
    }
    
    /**
     * Calls the Youtube Data API and fetches chunked Channel data.
     *
     * @param channel    The Channel.
     * @param endpoint   The API Endpoint.
     * @param parameters A map of parameters.
     * @return The number of data pages fetched.
     * @throws Exception When there is an error.
     */
    private static int fetchChunkedData(Channel channel, Endpoint endpoint, Map<String, String> parameters) throws Exception {
        WebUtils.checkPlaylistId(channel);
        
        final AtomicInteger page = new AtomicInteger(0);
        final List<String> data = new ArrayList<>();
        final AtomicBoolean more = new AtomicBoolean(false);
        
        do {
            data.add(callApi(channel, endpoint, parameters));
            more.set(parameters.get("pageToken") != null);
            
            if (((page.incrementAndGet() % MAX_PAGES_PER_FILE) == 0) || !more.get()) {
                FileUtils.writeStringToFile(
                        channel.state.getDataFile((page.get() / MAX_PAGES_PER_FILE), endpoint.name),
                        data.stream().collect(Collectors.joining(",", ("[" + System.lineSeparator()), "]")));
                data.clear();
            }
        } while (more.get());
        
        return page.get();
    }
    
    /**
     * Calls the Youtube Data API.
     *
     * @param channel    The Channel.
     * @param endpoint   The API Endpoint.
     * @param parameters A map of parameters.
     * @return The Entity data.
     * @throws Exception When there is an error.
     */
    private static String callApi(Channel channel, Endpoint endpoint, Map<String, String> parameters) throws Exception {
        final AtomicReference<String> response = new AtomicReference<>(null);
        final AtomicBoolean error = new AtomicBoolean(false);
        
        for (int retry = 0; retry <= MAX_RETRIES; retry++) {
            final HttpGet request = buildApiRequest(endpoint, parameters);
            
            try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
                response.set(EntityUtils.toString(httpResponse.getEntity()));
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
        return handleResponse(channel, response.get());
    }
    
    /**
     * Calls the Youtube Data API.
     *
     * @param endpoint   The API Endpoint.
     * @param parameters A map of parameters.
     * @return The Entity data.
     * @throws Exception When there is an error.
     */
    private static String callApi(Endpoint endpoint, Map<String, String> parameters) throws Exception {
        return callApi(null, endpoint, parameters);
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
     * Parses a Youtube Entity.
     *
     * @param entity The Entity.
     * @param <T>    The type of the Entity.
     * @return The Youtube Entity.
     */
    private static <T extends Entity> T parseEntity(T entity) {
        entityCache.putIfAbsent(entity.metadata.entityId, entity);
        return entity;
    }
    
    /**
     * Parses the Youtube Data API video response data for a Channel.
     *
     * @param channel The Channel.
     * @return The list of Video Entities parsed from the Channel data.
     * @throws Exception When there is an error.
     */
    public static List<Video> parseChannelVideoData(Channel channel) throws Exception {
        return parseData(channel, Endpoint.PLAYLIST_ITEMS, dataItem ->
                parseEntity(new Video(dataItem, channel)));
    }
    
    /**
     * Parses the Youtube Data API playlist response data for a Channel.
     *
     * @param channel The Channel.
     * @return The list of Playlist Entities parsed from the Channel data.
     * @throws Exception When there is an error.
     */
    public static List<Playlist> parseChannelPlaylistData(Channel channel) throws Exception {
        return parseData(channel, Endpoint.CHANNEL_PLAYLISTS, dataItem ->
                parseEntity(new Playlist(dataItem, channel)));
    }
    
    /**
     * Parses the Youtube Data API response data.
     *
     * @param channel  The Channel.
     * @param endpoint The API Endpoint.
     * @param parser   The function to parse the response data items.
     * @param <T>      The type of the response data items.
     * @return The parsed response data items.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> parseData(Channel channel, Endpoint endpoint, Function<Map<String, Object>, T> parser) throws Exception {
        return channel.state.getDataFiles(endpoint.name).stream()
                .sorted(Comparator.comparing(File::getName))
                .map(chunkFile -> {
                    try {
                        return Optional.ofNullable(FileUtils.readFileToString(chunkFile))
                                .map(response -> handleResponse(channel, response))
                                .map((UncheckedFunction<String, List<Map<String, Object>>>) response ->
                                        (List<Map<String, Object>>) new JSONParser().parse(response))
                                .stream().flatMap(Collection::stream)
                                .flatMap(dataChunk -> ((List<Map<String, Object>>) dataChunk.get("items")).stream())
                                .filter(dataItem -> {
                                    if ((dataItem == null) && channel.error.compareAndSet(false, true)) {
                                        System.out.println(Color.bad("Error reading data for Channel: ") + Color.channel(channel.getName()) + Color.bad("; Skipping this run"));
                                    }
                                    return !channel.error.get();
                                })
                                .map(parser)
                                .collect(Collectors.toList());
                    } catch (Exception e) {
                        System.out.println(Color.bad("Error while parsing Channel data"));
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Handles a response from the Youtube Data API.
     *
     * @param channel  The Channel.
     * @param response The response.
     * @return The response.
     * @throws RuntimeException If the response is an error.
     */
    private static String handleResponse(Channel channel, String response) {
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
    
}
