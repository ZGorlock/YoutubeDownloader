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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.object.collection.MapUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
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
    
    
    //Static Fields
    
    /**
     * The HTTP Client used to interact with the Youtube API.
     */
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    
    
    //Functions
    
    /**
     * Calls the Youtube Data API and fetches videos for a Channel.
     *
     * @param channel The Channel.
     * @return The number of Channel data chunks read.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("unchecked")
    public static int fetchChannelVideoData(Channel channel) throws Exception {
        return callApi(channel, "playlistItems", null, MapUtility.mapOf(
                new ImmutablePair<>("playlistId", channel.getPlaylistId())));
    }
    
    /**
     * Parses the Youtube Data API video response data for a Channel.
     *
     * @param channel The Channel.
     * @return The list of Videos parsed from the Channel data.
     * @throws Exception When there is an error.
     */
    public static List<Video> parseChannelVideoData(Channel channel) throws Exception {
        return parseData(channel, null, dataItem -> {
            final JSONObject snippet = (JSONObject) dataItem.get("snippet");
            final JSONObject resourceId = (JSONObject) snippet.get("resourceId");
            final JSONObject thumbnails = (JSONObject) snippet.get("thumbnails");
            
            final String videoId = (String) resourceId.get("videoId");
            final String title = (String) snippet.get("title");
            final String date = (String) snippet.get("publishedAt");
            
            //filter private videos
            if (title.equals("Private video")) {
                return null;
            }
            
            //filter live videos
            if (Optional.ofNullable((JSONObject) thumbnails.get("default"))
                    .map(defaultThumbnail -> Optional.ofNullable((String) defaultThumbnail.get("url"))
                            .map(url -> url.substring(url.length() - 9, url.length() - 4).equalsIgnoreCase("_live")).orElse(true))
                    .orElse(true)) {
                return null;
            }
            
            return new Video(videoId, title, date, channel);
        });
    }
    
    /**
     * Calls the Youtube Data API and fetches playlists for a Channel.
     *
     * @param channel The Channel.
     * @return The API response.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("unchecked")
    public static int fetchChannelPlaylistData(Channel channel) throws Exception {
        return (!channel.isYoutubeChannel()) ? -1 :
               callApi(channel, "playlists", "playlist", MapUtility.mapOf(
                       new ImmutablePair<>("channelId", channel.getPlaylistId().replaceAll("^UU", "UC"))));
    }
    
    /**
     * Parses the Youtube Data API playlist response data for a Channel.
     *
     * @param channel The Channel.
     * @return The map of playlist names and ids parsed from the Channel data.
     * @throws Exception When there is an error.
     */
    public static List<Playlist> parseChannelPlaylistData(Channel channel) throws Exception {
        return parseData(channel, "playlist", dataItem -> {
            final JSONObject snippet = (JSONObject) dataItem.get("snippet");
            
            final String playlistId = (String) dataItem.get("id");
            final String title = (String) snippet.get("title");
            final String date = (String) snippet.get("publishedAt");
            
            return new Playlist(playlistId, title, date, channel);
        });
    }
    
    /**
     * Calls the Youtube Data API.
     *
     * @param channel    The Channel.
     * @param endpoint   The API endpoint name.
     * @param type       The data type.
     * @param parameters A map of parameters.
     * @return The number of data chunks read.
     * @throws Exception When there is an error.
     */
    private static int callApi(Channel channel, String endpoint, String type, Map<String, String> parameters) throws Exception {
        WebUtils.checkPlaylistId(channel);
        
        final AtomicInteger chunk = new AtomicInteger(0);
        final List<String> data = new ArrayList<>();
        final List<String> calls = new ArrayList<>();
        final AtomicBoolean more = new AtomicBoolean(false);
        
        do {
            for (int retry = 0; retry < MAX_RETRIES; retry++) {
                final HttpGet request = buildApiRequest(endpoint, parameters);
                
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    final HttpEntity entity = response.getEntity();
                    final Header headers = entity.getContentType();
                    final String result = EntityUtils.toString(entity);
                    
                    Stats.totalApiCalls++;
                    calls.add(request.getURI() + " ===== " + result.replaceAll("[\\s\\r\\n]+", " "));
                    
                    if (result.contains("\"error\": {")) {
                        Stats.totalApiFailures++;
                        if (retry < (MAX_RETRIES - 1)) {
                            continue;
                        }
                        channel.error.set(true);
                    } else {
                        retry = MAX_RETRIES;
                    }
                    data.add(result);
                    
                    parameters.put("pageToken", (String) ((JSONObject) new JSONParser().parse(result)).get("nextPageToken"));
                    more.set(parameters.get("pageToken") != null);
                    
                    if (((chunk.incrementAndGet() % MAX_PAGES_PER_FILE) == 0) || !more.get()) {
                        FileUtils.writeStringToFile(
                                channel.state.getDataFile((chunk.get() / MAX_PAGES_PER_FILE), type),
                                data.stream().collect(Collectors.joining(",", ("[" + System.lineSeparator()), "]")));
                        data.clear();
                    }
                }
            }
        } while (more.get());
        
        FileUtils.writeLines(channel.state.callLogFile, calls);
        return chunk.get();
    }
    
    /**
     * Builds an API HTTP GET request.
     *
     * @param endpoint             The API endpoint name.
     * @param additionalParameters A map of additional parameters.
     * @return The API HTTP GET request.
     * @throws Exception When there is an error building the request.
     */
    private static HttpGet buildApiRequest(String endpoint, Map<String, String> additionalParameters) throws Exception {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("part", "snippet");
        parameters.put("maxResults", String.valueOf(MAX_RESULTS_PER_PAGE));
        parameters.put("key", API_KEY);
        parameters.putAll(additionalParameters);
        
        final HttpGet request = new HttpGet(buildApiUrl(endpoint, parameters));
        request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");
        return request;
    }
    
    /**
     * Builds an API url string.
     *
     * @param endpoint   The API endpoint name.
     * @param parameters A map of parameters.
     * @return The API url string.
     * @throws Exception When there is an error encoding the url.
     */
    private static String buildApiUrl(String endpoint, Map<String, String> parameters) throws Exception {
        return String.join("/", REQUEST_BASE, endpoint) +
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
     * Parses the Youtube Data API response data.
     *
     * @param channel The Channel.
     * @param type    The data type.
     * @param parser  The function to parse the response data items.
     * @param <T>     The type of the response data items.
     * @return The parsed response data items.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> parseData(Channel channel, String type, Function<JSONObject, T> parser) throws Exception {
        return channel.state.getDataFiles(type).stream()
                .sorted(Comparator.comparing(File::getName))
                .map(chunkFile -> {
                    try {
                        return ((Stream<JSONObject>) ((JSONArray) new JSONParser().parse(readChunkFile(channel, chunkFile))).stream())
                                .flatMap((Function<JSONObject, Stream<JSONObject>>) dataChunk -> ((JSONArray) dataChunk.get("items")).stream())
                                .filter(dataItem -> {
                                    if ((dataItem == null) && channel.error.compareAndSet(false, true)) {
                                        System.out.println(Color.bad("Error reading the data for Channel: ") + Color.channel(channel.getName()) + Color.bad("; Skipping this run"));
                                    }
                                    return !channel.error.get();
                                })
                                .map(parser)
                                .collect(Collectors.toList());
                    } catch (Exception e) {
                        System.out.println(Color.bad("Error while parsing Channel data"));
                        throw new RuntimeException(e);
                    }
                }).flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Reads a Channel data chunk file.
     *
     * @param channel   The Channel.
     * @param chunkFile The Channel data chunk file.
     * @return The content of the data chunk file.
     * @throws Exception When there is an error reading the file, or the data indicated a failure.
     */
    private static String readChunkFile(Channel channel, File chunkFile) throws Exception {
        final String data = FileUtils.readFileToString(chunkFile);
        
        if (data.contains("\"code\": 404")) {
            System.out.println(Color.bad("The Channel: ") + Color.channel(channel.getName()) + Color.bad(" does not exist"));
            throw new RuntimeException();
        }
        if (data.contains("\"code\": 403")) {
            System.out.println(Color.bad("Your API Key is not authorized or has exceeded its quota"));
            throw new RuntimeException();
        }
        
        return data;
    }
    
}
