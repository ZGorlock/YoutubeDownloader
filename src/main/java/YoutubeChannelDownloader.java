/*
 * File:    YoutubeChannelDownloader.java
 * Author:  Zachary Gill
 */

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

/**
 * Downloads Youtube Channels and Playlists.
 */
public class YoutubeChannelDownloader {
    
    //Constants
    
    /**
     * The Youtube API key.
     */
    private static String API_KEY = "";
    
    //Populates API_KEY
    static {
        try {
            API_KEY = FileUtils.readFileToString(new File("apiKey"), "UTF-8");
            if (API_KEY.isEmpty()) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println("Must supply a Google API key with Youtube Data API enabled in /apiKey");
            System.exit(0);
        }
    }
    
    /**
     * The base url for querying Youtube Playlists.
     */
    private static final String REQUEST_BASE = "https://www.googleapis.com/youtube/v3/playlistItems";
    
    
    //Static Fields
    
    /**
     * A flag indicating whether to the log the download command or not.
     */
    private static final boolean logCommand = true;
    
    /**
     * A flag indicating whether to log the download work or not.
     */
    private static final boolean logWork = false;
    
    /**
     * A flag indicating whether to process all Channels or not.
     */
    private static final boolean doAllChannels = true;
    
    /**
     * A flag indicating whether to retry previously failed videos or not.
     */
    private static final boolean retryFailed = false;
    
    /**
     * The HTTP Client used to interact with the Youtube API.
     */
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    
    /**
     * The video map for the Channel being processed.
     */
    private static final Map<String, Video> videoMap = new LinkedHashMap<>();
    
    
    //Fields
    
    /**
     * The Channel to process.
     */
    private static Channel channel = null;
    
    /**
     * The Playlist ID for the Channel being processed.
     */
    private static String playlistId;
    
    /**
     * The output folder for the Channel being processed.
     */
    private static File outputFolder;
    
    /**
     * The playlist file for the Channel being processed.
     */
    private static File playlistM3u;
    
    /**
     * The internal data file for the Channel being processed.
     */
    private static File dataFile;
    
    /**
     * The internal file holding the saved videos for the Channel being processed.
     */
    private static File saveFile;
    
    /**
     * The internal file holding the videos queued for download for the Channel being processed.
     */
    private static File queueFile;
    
    /**
     * The internal file holding the blocked videos for the Channel being processed.
     */
    private static File blockedFile;
    
    
    //Main Method
    
    /**
     * The main method for the Youtube Channel Downloader.
     *
     * @param args The arguments to the main method.
     * @throws Exception When there is an error.
     */
    public static void main(String[] args) throws Exception {
        if (!YoutubeUtils.doStartupChecks()) {
            return;
        }
        
        if (doAllChannels) {
            for (Channel currentChannel : Channel.values()) {
                if (currentChannel.active) {
                    setChannel(currentChannel);
                    processChannel();
                }
            }
        } else if (channel != null && channel.active) {
            setChannel(channel);
            processChannel();
        }
    }
    
    
    //Methods
    
    /**
     * Sets the active Channel.
     *
     * @param thisChannel The Channel to be processed.
     * @throws Exception When there is an error.
     */
    private static void setChannel(Channel thisChannel) throws Exception {
        channel = thisChannel;
        
        playlistId = channel.playlistId;
        outputFolder = channel.outputFolder;
        playlistM3u = channel.playlistFile;
        
        dataFile = new File("data/channel/" + channel.name + "/" + channel.name + "-data.txt");
        saveFile = new File("data/channel/" + channel.name + "/" + channel.name + "-save.txt");
        queueFile = new File("data/channel/" + channel.name + "/" + channel.name + "-queue.txt");
        blockedFile = new File("data/channel/" + channel.name + "/" + channel.name + "-blocked.txt");
        
        for (File cleanupFile : Arrays.asList(dataFile, saveFile, queueFile, blockedFile)) {
            File oldFile = new File(cleanupFile.getAbsolutePath().replace("\\", "/").replace("/channel/", "/"));
            if (oldFile.exists()) {
                if (cleanupFile.exists()) {
                    FileUtils.deleteQuietly(oldFile);
                } else {
                    FileUtils.moveFile(oldFile, cleanupFile);
                }
            }
        }
    }
    
    /**
     * Processes the active Channel.
     *
     * @throws Exception When there is an error.
     */
    private static void processChannel() throws Exception {
        System.out.println("Processing Channel: " + channel.name);
        System.out.println();
        
        getChannelData();
        processChannelData();
        produceQueue();
        downloadVideos();
        createPlaylist();
        
        System.out.println();
    }
    
    /**
     * Retrieves the data for the active Channel from the Youtube API.
     *
     * @throws Exception When there is an error.
     */
    private static void getChannelData() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("part", "snippet");
        parameters.put("maxResults", "50");
        parameters.put("playlistId", playlistId);
        parameters.put("key", API_KEY);
        
        StringBuilder data = new StringBuilder("[").append(System.lineSeparator());
        boolean more;
        boolean first = true;
        do {
            HttpGet request = new HttpGet(REQUEST_BASE + YoutubeUtils.buildParameterString(parameters));
            request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                Header headers = entity.getContentType();
                String result = EntityUtils.toString(entity);
                
                more = false;
                JSONParser parser = new JSONParser();
                JSONObject resultJson = (JSONObject) parser.parse(result);
                if (resultJson.containsKey("nextPageToken")) {
                    parameters.put("pageToken", (String) resultJson.get("nextPageToken"));
                    more = true;
                }
                
                if (!first) {
                    data.append(",");
                }
                data.append(result);
                first = false;
            }
        } while (more);
        data.append("]");
        
        FileUtils.writeStringToFile(dataFile, data.toString(), "UTF-8", false);
    }
    
    /**
     * Processes the data for the active Channel that was retrieved from the Youtube API.
     *
     * @throws Exception When there is an error.
     */
    private static void processChannelData() throws Exception {
        String data = FileUtils.readFileToString(dataFile, "UTF-8");
        videoMap.clear();
        if (data.contains("\"code\": 404")) {
            System.err.println("The Playlist for " + channel.name + " does not exist");
            return;
        }
        if (data.contains("\"code\": 403")) {
            System.err.println("Your API Key is not authorized or has exceeded its quota");
            return;
        }
        
        JSONParser parser = new JSONParser();
        JSONArray dataJson = (JSONArray) parser.parse(data);
        for (Object dataChunk : dataJson) {
            JSONObject dataChunkJson = (JSONObject) dataChunk;
            JSONArray dataItems = (JSONArray) dataChunkJson.get("items");
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
                    if ((defaultThumbnailUrl == null) || defaultThumbnailUrl.substring(defaultThumbnailUrl.length() - 9, defaultThumbnailUrl.length() - 4).toLowerCase().equals("_live")) {
                        continue;
                    }
                } else {
                    continue;
                }
                
                Video video = new Video();
                video.videoId = videoId;
                video.title = YoutubeUtils.cleanTitle(title);
                video.url = YoutubeUtils.VIDEO_BASE + videoId;
                video.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS").parse(date.replace("T", " ").replace("Z", ""));
                video.output = new File(outputFolder, video.title + (channel.saveAsMp3 ? ".mp3" : ".mp4"));
                
                boolean exists = false;
                for (Video v : videoMap.values()) {
                    if (v.title.equals(video.title)) {
                        exists = true;
                        break;
                    }
                }
                
                if (!exists) {
                    videoMap.put(videoId, video);
                }
            }
        }
    }
    
    /**
     * Produces the queue of videos to download from the active Channel.
     *
     * @throws Exception When there is an error.
     */
    private static void produceQueue() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println("Must populate video map before producing the queue");
            return;
        }
        
        List<String> save = saveFile.exists() ? FileUtils.readLines(saveFile, "UTF-8") : new ArrayList<>();
        List<String> blocked = blockedFile.exists() ? FileUtils.readLines(blockedFile, "UTF-8") : new ArrayList<>();
        List<String> queue = new ArrayList<>();
        
        if (retryFailed) {
            blocked.clear();
        }
        
        Channel.performSpecialPreConditions(channel, videoMap, queue, save, blocked);
        videoMap.forEach((key, value) -> {
            if (!save.contains(key) && YoutubeUtils.videoExists(value.output)) {
                save.add(key);
            }
            if ((!save.contains(key) || !YoutubeUtils.videoExists(value.output)) && !blocked.contains(key)) {
                queue.add(key);
                save.remove(key);
            }
        });
        Channel.performSpecialPostConditions(channel, videoMap, queue, save, blocked);
        
        queue.removeAll(blocked);
        save.removeAll(blocked);
        
        FileUtils.writeLines(queueFile, queue);
        FileUtils.writeLines(saveFile, save);
        FileUtils.writeLines(blockedFile, blocked);
    }
    
    /**
     * Downloads the queued videos from the active Channel.
     *
     * @throws Exception When there is an error.
     */
    private static void downloadVideos() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println("Must populate video map before downloading videos");
            return;
        }
        
        List<String> queue = queueFile.exists() ? FileUtils.readLines(queueFile, "UTF-8") : new ArrayList<>();
        List<String> save = saveFile.exists() ? FileUtils.readLines(saveFile, "UTF-8") : new ArrayList<>();
        List<String> blocked = blockedFile.exists() ? FileUtils.readLines(blockedFile, "UTF-8") : new ArrayList<>();
        
        List<String> working = new ArrayList<>(queue);
        for (String videoId : working) {
            Video video = videoMap.get(videoId);
            
            System.out.println("Downloading: " + video.title);
            System.out.print("    ");
            if (YoutubeUtils.downloadYoutubeVideo(YoutubeUtils.VIDEO_BASE + videoId, video.output, channel.saveAsMp3, logCommand, logWork)) {
                queue.remove(videoId);
                save.add(videoId);
                System.out.println("    Download Succeeded");
            } else {
                queue.remove(videoId);
                blocked.add(videoId);
                System.err.println("    Download Failed");
            }
            
            File partFile = new File(video.output.getParentFile(), video.output.getName() + ".part");
            if (partFile.exists()) {
                FileUtils.forceDeleteOnExit(partFile);
                save.remove(videoId);
                blocked.add(videoId);
            }
            
            FileUtils.writeLines(queueFile, queue);
            FileUtils.writeLines(saveFile, save);
            FileUtils.writeLines(blockedFile, blocked);
        }
    }
    
    /**
     * Creates a playlist of the videos from the active Channel.
     *
     * @throws Exception When there is an error.
     */
    private static void createPlaylist() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println("Must populate video map before creating a playlist");
            return;
        }
        
        if (channel.playlistFile == null) {
            return;
        }
        
        List<String> save = saveFile.exists() ? FileUtils.readLines(saveFile, "UTF-8") : new ArrayList<>();
        List<String> playlist = new ArrayList<>();
        for (Map.Entry<String, Video> video : videoMap.entrySet()) {
            if (save.contains(video.getKey())) {
                playlist.add(video.getValue().output.getAbsolutePath());
            }
        }
        
        if (channel.isChannel()) {
            Collections.reverse(playlist);
        }
        
        FileUtils.writeLines(channel.playlistFile, playlist);
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Video.
     */
    public static class Video {
        
        //Fields
        
        /**
         * The ID of the Video.
         */
        public String videoId;
        
        /**
         * The title of the Video.
         */
        public String title;
        
        /**
         * The url of the Video.
         */
        public String url;
        
        /**
         * The date the Video was uploaded.
         */
        public Date date;
        
        /**
         * The output file for the Video.
         */
        public File output;
        
    }
    
}
