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

import commons.lambda.function.checked.CheckedFunction;
import commons.object.string.StringUtility;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
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
    
    
    //Static Methods
    
    /**
     * Calls the Youtube Data API and fetches the info of a channel.
     *
     * @param channelId The Youtube id of the channel.
     * @return The channel info.
     * @throws Exception When there is an error.
     */
    public static String fetchChannelInfo(String channelId) throws Exception {
        return fetchInfo(channelId, "channels");
    }
    
    /**
     * Calls the Youtube Data API and fetches the info of a playlist.
     *
     * @param playlistId The Youtube id of the playlist.
     * @return The playlist info.
     * @throws Exception When there is an error.
     */
    public static String fetchPlaylistInfo(String playlistId) throws Exception {
        return fetchInfo(playlistId, "playlists");
    }
    
    /**
     * Calls the Youtube Data API and fetches the info of a video.
     *
     * @param videoId The Youtube id of the video.
     * @return The video info.
     * @throws Exception When there is an error.
     */
    public static String fetchVideoInfo(String videoId) throws Exception {
        return fetchInfo(videoId, "videos");
    }
    
    /**
     * Calls the Youtube Data API and fetches Entity info.
     *
     * @param id         The id of the Entity.
     * @param endpoint   The API endpoint name.
     * @param parameters A map of parameters.
     * @return The Entity info.
     * @throws Exception When there is an error.
     */
    private static String fetchInfo(String id, String endpoint, Map<String, String> parameters) throws Exception {
        return Optional.ofNullable(callApi(endpoint, parameters))
                .map((CheckedFunction<String, JSONObject>) e ->
                        (JSONObject) new JSONParser().parse(e))
                .map(e -> (JSONArray) e.get("items"))
                .map(e -> (JSONObject) e.get(0))
                .map(JSONAware::toJSONString).orElse(null);
    }
    
    /**
     * Calls the Youtube Data API and fetches Entity info.
     *
     * @param id       The id of the Entity.
     * @param endpoint The API endpoint name.
     * @return The Entity info.
     * @throws Exception When there is an error.
     */
    private static String fetchInfo(String id, String endpoint) throws Exception {
        return fetchInfo(id, endpoint, new HashMap<>(Map.ofEntries(
                Map.entry("id", id))));
    }
    
    /**
     * Calls the Youtube Data API and fetches videos for a Channel.
     *
     * @param channel The Channel.
     * @return The number of Channel data chunks read.
     * @throws Exception When there is an error.
     */
    public static int fetchChannelVideoData(Channel channel) throws Exception {
        return fetchData(channel, "playlistItems", new HashMap<>(Map.ofEntries(
                Map.entry("playlistId", channel.getPlaylistId()))));
    }
    
    /**
     * Calls the Youtube Data API and fetches playlists for a Channel.
     *
     * @param channel The Channel.
     * @return The API response.
     * @throws Exception When there is an error.
     */
    public static int fetchChannelPlaylistData(Channel channel) throws Exception {
        return !channel.isYoutubeChannel() ? -1 :
               fetchData(channel, "playlists", new HashMap<>(Map.ofEntries(
                       Map.entry("channelId", channel.getPlaylistId().replaceAll("^UU", "UC")))));
    }
    
    /**
     * Calls the Youtube Data API and fetches Channel data.
     *
     * @param channel    The Channel.
     * @param endpoint   The API endpoint name.
     * @param parameters A map of parameters.
     * @return The number of data pages read.
     * @throws Exception When there is an error.
     */
    private static int fetchData(Channel channel, String endpoint, Map<String, String> parameters) throws Exception {
        WebUtils.checkPlaylistId(channel);
        
        final AtomicInteger page = new AtomicInteger(0);
        final List<String> data = new ArrayList<>();
        final AtomicBoolean more = new AtomicBoolean(false);
        
        do {
            data.add(callApi(channel, endpoint, parameters));
            more.set(parameters.get("pageToken") != null);
            
            if (((page.incrementAndGet() % MAX_PAGES_PER_FILE) == 0) || !more.get()) {
                FileUtils.writeStringToFile(
                        channel.state.getDataFile((page.get() / MAX_PAGES_PER_FILE), endpoint),
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
     * @param endpoint   The API endpoint name.
     * @param parameters A map of parameters.
     * @return The Entity data.
     * @throws Exception When there is an error.
     */
    private static String callApi(Channel channel, String endpoint, Map<String, String> parameters) throws Exception {
        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            final HttpGet request = buildApiRequest(endpoint, parameters);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                final String result = EntityUtils.toString(response.getEntity());
                final boolean error = result.contains("\"error\": {");
                
                Stats.totalApiCalls++;
                Stats.totalApiFailures += (error ? 1 : 0);
                if (channel != null) {
                    FileUtils.writeStringToFile(channel.state.callLogFile,
                            (StringUtility.padLeft(String.valueOf(result.length()), 8) + " bytes " +
                                    (error ? "=XXX=" : "=====") + ' ' + request.getURI() + System.lineSeparator()), true);
                }
                
                if (!error) {
                    parameters.put("pageToken", (String) ((JSONObject) new JSONParser().parse(result)).get("nextPageToken"));
                    return result;
                }
            }
        }
        
        if (channel != null) {
            channel.error.set(true);
            System.out.println(Color.bad("Error calling API for Channel: ") + Color.channel(channel.getName()) + Color.bad("; Skipping this run"));
        }
        return null;
    }
    
    /**
     * Calls the Youtube Data API.
     *
     * @param endpoint   The API endpoint name.
     * @param parameters A map of parameters.
     * @return The Entity data.
     * @throws Exception When there is an error.
     */
    private static String callApi(String endpoint, Map<String, String> parameters) throws Exception {
        return callApi(null, endpoint, parameters);
    }
    
    /**
     * Builds an API HTTP GET request.
     *
     * @param endpoint   The API endpoint name.
     * @param parameters A map of parameters.
     * @return The API HTTP GET request.
     * @throws Exception When there is an error building the request.
     */
    private static HttpGet buildApiRequest(String endpoint, Map<String, String> parameters) throws Exception {
        parameters.putIfAbsent("part", "snippet");
        parameters.putIfAbsent("maxResults", String.valueOf(MAX_RESULTS_PER_PAGE));
        parameters.putIfAbsent("key", API_KEY);
        
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
     * Parses the Youtube Data API video response data for a Channel.
     *
     * @param channel The Channel.
     * @return The list of Videos parsed from the Channel data.
     * @throws Exception When there is an error.
     */
    public static List<Video> parseChannelVideoData(Channel channel) throws Exception {
        return parseData(channel, "playlistItems", dataItem -> {
//            new Video(dataItem, channel)
            
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
     * Parses the Youtube Data API playlist response data for a Channel.
     *
     * @param channel The Channel.
     * @return The map of playlist names and ids parsed from the Channel data.
     * @throws Exception When there is an error.
     */
    public static List<Playlist> parseChannelPlaylistData(Channel channel) throws Exception {
        return parseData(channel, "playlists", dataItem -> {
//            new Playlist(dataItem, channel)
            
            final JSONObject snippet = (JSONObject) dataItem.get("snippet");
            
            final String playlistId = (String) dataItem.get("id");
            final String title = (String) snippet.get("title");
            final String date = (String) snippet.get("publishedAt");
            
            return new Playlist(playlistId, title, date, channel);
        });
    }
    
    /**
     * Parses the Youtube Data API response data.
     *
     * @param channel  The Channel.
     * @param endpoint The API endpoint name.
     * @param parser   The function to parse the response data items.
     * @param <T>      The type of the response data items.
     * @return The parsed response data items.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> parseData(Channel channel, String endpoint, Function<Map<String, Object>, T> parser) throws Exception {
        return channel.state.getDataFiles(endpoint).stream()
                .sorted(Comparator.comparing(File::getName))
                .map(chunkFile -> {
                    try {
                        return (((List<Map<String, Object>>) new JSONParser().parse(readChunkFile(channel, chunkFile))).stream())
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
