/*
 * File:    ApiUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
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
import youtube.channel.Video;

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
     * The Youtube API key.
     */
    private static String API_KEY = "";
    
    // ----------------------------------------------------------------
    //
    // To run this project you need a Google API Key:
    //   1. Go to: https://console.cloud.google.com/projectselector2/apis/dashboard
    //   2. Click 'Create new Project' and name it 'Youtube Downloader'
    //   3. Click on 'Enable APIs and Services'
    //   4. Search 'Youtube' and select 'YouTube Data API v3'
    //   5. Click 'Enable'
    //   6. Click 'Create Credentials'
    //   7. Select 'YouTube Data API v3', click 'Public Data', then click 'Next'
    //   8. Copy your API key to the file ./apiKey in the project
    //
    // ----------------------------------------------------------------
    
    //Populates API_KEY
    static {
        try {
            API_KEY = FileUtils.readFileToString(new File("apiKey"), "UTF-8");
            if (API_KEY.isEmpty()) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println(Color.bad("Must supply a Google API key with Youtube Data API enabled in ") + Color.file("./apiKey"));
            System.exit(0);
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
     * Calls the Youtube Data API and fetches data for a Channel.
     *
     * @param channel The Channel.
     * @return The number of Channel data chunks read.
     * @throws Exception When there is an error.
     */
    public static int fetchApiChannelData(Channel channel) throws Exception {
        WebUtils.checkPlaylistId(channel);
        
        Map<String, String> parameters = new HashMap<>();
        parameters.put("part", "snippet");
        parameters.put("maxResults", "50");
        parameters.put("playlistId", channel.playlistId);
        parameters.put("key", API_KEY);
        
        int chunk = 0;
        List<String> data = new ArrayList<>();
        List<String> calls = new ArrayList<>();
        boolean more = false;
        do {
            HttpGet request = new HttpGet(REQUEST_BASE + "/playlistItems" + buildParameterString(parameters));
            request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");
            
            for (int retry = 0; retry < MAX_RETRIES; retry++) {
                
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    HttpEntity entity = response.getEntity();
                    Header headers = entity.getContentType();
                    String result = EntityUtils.toString(entity);
                    
                    Stats.totalApiCalls++;
                    calls.add(request.getURI() + " ===== " + result.replaceAll("[\\s\\r\\n]+", " "));
                    
                    if (result.contains("\"error\": {")) {
                        Stats.totalApiFailures++;
                        if (retry < (MAX_RETRIES - 1)) {
                            continue;
                        }
                        channel.error = true;
                    } else {
                        retry = MAX_RETRIES;
                    }
                    data.add(result);
                    
                    more = false;
                    JSONParser parser = new JSONParser();
                    JSONObject resultJson = (JSONObject) parser.parse(result);
                    if (resultJson.containsKey("nextPageToken")) {
                        parameters.put("pageToken", (String) resultJson.get("nextPageToken"));
                        more = true;
                    }
                    
                    if (((++chunk % MAX_PAGES_PER_FILE) == 0) || !more) {
                        FileUtils.writeStringToFile(
                                channel.state.getDataFile(chunk / MAX_PAGES_PER_FILE),
                                data.stream().collect(Collectors.joining(",", ("[" + System.lineSeparator()), "]")),
                                "UTF-8", false);
                        data.clear();
                    }
                }
            }
        } while (more);
        
        FileUtils.writeLines(channel.state.callLogFile, calls);
        return chunk;
    }
    
    /**
     * Parses the Youtube Data API response data for a Channel.
     *
     * @param channel The Channel.
     * @return The list of Videos parsed from the Channel data.
     * @throws Exception When there is an error.
     */
    public static List<Video> parseApiChannelData(Channel channel) throws Exception {
        List<Video> videos = new ArrayList<>();
        
        List<File> dataChunks = channel.state.getDataFiles();
        dataChunks.sort(Comparator.comparing(File::getName));
        
        for (File dataChunk : dataChunks) {
            try {
                videos.addAll(parseApiChannelData(channel, dataChunk));
            } catch (Exception e) {
                System.out.println(Color.bad("Error while parsing Channel data"));
                throw new RuntimeException(e);
            }
        }
        return videos;
    }
    
    /**
     * Parses a file chunk of the Youtube Data API response data for a Channel.
     *
     * @param channel The Channel.
     * @param chunk   The Channel data chunk.
     * @return The list of Videos parsed from the Channel data chunk.
     * @throws Exception When there is an error.
     */
    private static List<Video> parseApiChannelData(Channel channel, File chunk) throws Exception {
        List<Video> videos = new ArrayList<>();
        
        String data = FileUtils.readFileToString(chunk, "UTF-8");
        
        if (data.contains("\"code\": 404")) {
            System.out.println(Color.bad("The Channel ") + Color.channel(channel.name) + Color.bad(" does not exist"));
            throw new RuntimeException();
        }
        if (data.contains("\"code\": 403")) {
            System.out.println(Color.bad("Your API Key is not authorized or has exceeded its quota"));
            throw new RuntimeException();
        }
        
        JSONParser parser = new JSONParser();
        JSONArray dataJson = (JSONArray) parser.parse(data);
        for (Object dataChunk : dataJson) {
            JSONObject dataChunkJson = (JSONObject) dataChunk;
            JSONArray dataItems = (JSONArray) dataChunkJson.get("items");
            if (dataItems == null) {
                System.out.println(Color.bad("Error reading the Channel ") + Color.channel(channel.name) + Color.bad("; Skipping this run"));
                continue;
            }
            for (Object dataItem : dataItems) {
                JSONObject dataItemJson = (JSONObject) dataItem;
                JSONObject snippet = (JSONObject) dataItemJson.get("snippet");
                JSONObject resourceId = (JSONObject) snippet.get("resourceId");
                JSONObject thumbnails = (JSONObject) snippet.get("thumbnails");
                
                String videoId = (String) resourceId.get("videoId");
                String title = (String) snippet.get("title");
                String date = (String) snippet.get("publishedAt");
                
                //filter private videos
                if (title.equals("Private video")) {
                    continue;
                }
                
                //filter live videos
                JSONObject defaultThumbnail = (JSONObject) thumbnails.get("default");
                if (defaultThumbnail != null) {
                    String defaultThumbnailUrl = (String) defaultThumbnail.get("url");
                    if ((defaultThumbnailUrl == null) || defaultThumbnailUrl.substring(defaultThumbnailUrl.length() - 9, defaultThumbnailUrl.length() - 4).equalsIgnoreCase("_live")) {
                        continue;
                    }
                } else {
                    continue;
                }
                
                videos.add(new Video(videoId, title, date, channel));
            }
        }
        return videos;
    }
    
    /**
     * Builds a parameter string to be appended to a url.
     *
     * @param parameters A map of parameters.
     * @return The parameter string.
     * @throws Exception When there is an error encoding the string.
     */
    private static String buildParameterString(Map<String, String> parameters) throws Exception {
        StringBuilder parameterString = new StringBuilder("?");
        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            if (parameterString.length() > 1) {
                parameterString.append("&");
            }
            parameterString.append(URLEncoder.encode(parameterEntry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(parameterEntry.getValue(), StandardCharsets.UTF_8));
        }
        return parameterString.toString();
    }
    
}
