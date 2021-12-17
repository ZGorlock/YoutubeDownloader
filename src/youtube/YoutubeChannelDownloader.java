/*
 * File:    YoutubeChannelDownloader.java
 * Package: youtube
 * Author:  Zachary Gill
 */

package youtube;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.Charsets;
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
import youtube.channel.Channel;
import youtube.channel.ChannelProcesses;
import youtube.channel.Channels;
import youtube.tools.Configurator;
import youtube.tools.YoutubeUtils;

/**
 * Downloads Youtube Channels and Playlists.
 */
@SuppressWarnings("FieldMayBeFinal")
public class YoutubeChannelDownloader {
    
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
            System.err.println("Must supply a Google API key with Youtube Data API enabled in /apiKey");
            System.exit(0);
        }
    }
    
    /**
     * The base url for querying Youtube Playlists.
     */
    private static final String REQUEST_BASE = "https://www.googleapis.com/youtube/v3/playlistItems";
    
    /**
     * The maximum number of times to retry an API call before failing.
     */
    private static final int MAX_RETRIES = 10;
    
    /**
     * The store of video keys and their current saved file name.
     */
    private static final File KEY_STORE_FILE = new File("data/keyStore.txt");
    
    //Loads the settings and Channel configurations
    static {
        Configurator.loadSettings("YoutubeChannelDownloader");
        Channels.loadChannels();
    }
    
    
    //Static Fields
    
    /**
     * A flag indicating whether to the log the download command or not.
     */
    private static final boolean logCommand = (boolean) Configurator.getSetting("flag.logCommand", true);
    
    /**
     * A flag indicating whether to log the download work or not.
     */
    private static final boolean logWork = (boolean) Configurator.getSetting("flag.logWork", false);
    
    /**
     * A flag indicating whether to retry previously failed videos or not.
     */
    private static final boolean retryFailed = (boolean) Configurator.getSetting("flag.retryFailed", false);
    
    /**
     * The HTTP Client used to interact with the Youtube API.
     */
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    
    /**
     * The video map for the Channel being processed.
     */
    private static final Map<String, Video> videoMap = new LinkedHashMap<>();
    
    /**
     * A map of video keys and their current saved file name for each Channel.
     */
    private static final Map<String, Map<String, String>> keyStore = new LinkedHashMap<>();
    
    /**
     * A counter of the total number of Channels that were processed this run.
     */
    private static int totalChannels = 0;
    
    /**
     * A counter of the total number of videos that were downloaded this run.
     */
    private static int totalDownloads = 0;
    
    /**
     * A counter of the total number of videos that failed to download this run.
     */
    private static int totalDownloadFailures = 0;
    
    /**
     * A counter of the total number of times the Youtube Data API was called this run.
     */
    private static int totalApiCalls = 0;
    
    /**
     * A counter of the total number of times calling the Youtube Data API failed this run.
     */
    private static int totalApiFailures = 0;
    
    /**
     * A counter of the total number of videos saved from Youtube.
     */
    private static int totalVideos = 0;
    
    /**
     * A counter of the total number of songs saved from Youtube.
     */
    private static int totalSongs = 0;
    
    /**
     * A counter of the total data downloaded from Youtube this run.
     */
    private static double totalDataDownloaded = 0.0;
    
    /**
     * A counter of the total data saved from Youtube, in MB.
     */
    private static double totalData = 0.0;
    
    
    //Fields
    
    /**
     * The Channel to process, or null if all Channels should be processed.
     */
    private static Channel channel = null;
    
    /**
     * The group to process, or null if all groups should be processed.
     */
    private static String group = null;
    
    /**
     * The Channel to start processing from, if processing all Channels.
     */
    private static Channel startAt = null;
    
    /**
     * The Channel to stop processing at, if processing all Channels.
     */
    private static Channel stopAt = null;
    
    //Loads the Channel filters
    static {
        try {
            channel = Channels.getChannel((String) Configurator.getSetting("filter.channel"));
        } catch (Exception ignored) {
        }
        try {
            group = (String) Configurator.getSetting("filter.group");
        } catch (Exception ignored) {
        }
        try {
            startAt = Channels.getChannel((String) Configurator.getSetting("filter.startAt"));
        } catch (Exception ignored) {
        }
        try {
            stopAt = Channels.getChannel((String) Configurator.getSetting("filter.stopAt"));
        } catch (Exception ignored) {
        }
    }
    
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
     * The internal call log file for the Channel being processed.
     */
    private static File callLogFile;
    
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
        loadKeyStore();
        
        if (channel == null) {
            boolean skip = (startAt != null);
            boolean stop = (stopAt != null);
            
            if (!((skip && stop) && (Channels.indexOf(stopAt.key) < Channels.indexOf(startAt.key)))) {
                for (Channel currentChannel : Channels.getChannels()) {
                    skip = skip && (!currentChannel.key.equals(startAt.key));
                    if (!skip && currentChannel.active &&
                            ((group == null) || (currentChannel.group.equalsIgnoreCase(group)))) {
                        setChannel(currentChannel);
                        processChannel();
                        if (stop && (currentChannel == stopAt)) {
                            break;
                        }
                    }
                }
            }
            
            channel = null;
            
        } else if (channel.active) {
            setChannel(channel);
            processChannel();
        }
        
        saveKeyStore();
        calculateData();
        printStats();
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
        callLogFile = new File("data/channel/" + channel.name + "/" + channel.name + "-callLog.txt");
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
     * @return Whether the Channel was successfully processed or not.
     * @throws Exception When there is an error.
     */
    private static boolean processChannel() throws Exception {
        System.out.println("Processing Channel: " + channel.name);
        System.out.println();
        
        boolean success = YoutubeUtils.isOnline() &&
                getChannelData() &&
                processChannelData() &&
                produceQueue() &&
                downloadVideos() &&
                createPlaylist();
        
        totalChannels++;
        System.out.println();
        return success;
    }
    
    /**
     * Retrieves the data for the active Channel from the Youtube API.
     *
     * @return Whether the Channel data was successfully retrieved or not.
     * @throws Exception When there is an error.
     */
    private static boolean getChannelData() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("part", "snippet");
        parameters.put("maxResults", "50");
        parameters.put("playlistId", playlistId);
        parameters.put("key", API_KEY);
        
        StringBuilder data = new StringBuilder("[").append(System.lineSeparator());
        List<String> calls = new ArrayList<>();
        boolean more = false;
        boolean first = true;
        do {
            HttpGet request = new HttpGet(REQUEST_BASE + YoutubeUtils.buildParameterString(parameters));
            request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");
            
            for (int retry = 0; retry < MAX_RETRIES; retry++) {
                
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    HttpEntity entity = response.getEntity();
                    Header headers = entity.getContentType();
                    String result = EntityUtils.toString(entity);
                    
                    totalApiCalls++;
                    calls.add(request.getURI() + " ===== " + result.replaceAll("[\\s\\r\\n]+", " "));
                    
                    if (result.contains("\"error\": {")) {
                        totalApiFailures++;
                        if (retry < (MAX_RETRIES - 1)) {
                            continue;
                        }
                        channel.error = true;
                    } else {
                        retry = MAX_RETRIES;
                    }
                    
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
                
            }
        } while (more);
        data.append("]");
        
        FileUtils.writeStringToFile(dataFile, data.toString(), "UTF-8", false);
        FileUtils.writeLines(callLogFile, calls);
        return true;
    }
    
    /**
     * Processes the data for the active Channel that was retrieved from the Youtube API.
     *
     * @return Whether the Channel data was successfully processed or not.
     * @throws Exception When there is an error.
     */
    private static boolean processChannelData() throws Exception {
        String data = FileUtils.readFileToString(dataFile, "UTF-8");
        videoMap.clear();
        if (data.contains("\"code\": 404")) {
            System.err.println("The Channel " + channel.name + " does not exist");
            return false;
        }
        if (data.contains("\"code\": 403")) {
            System.err.println("Your API Key is not authorized or has exceeded its quota");
            return false;
        }
        
        JSONParser parser = new JSONParser();
        JSONArray dataJson = (JSONArray) parser.parse(data);
        for (Object dataChunk : dataJson) {
            JSONObject dataChunkJson = (JSONObject) dataChunk;
            JSONArray dataItems = (JSONArray) dataChunkJson.get("items");
            if (dataItems == null) {
                System.err.println("Error reading the Channel " + channel.name + "; Skipping this run");
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
                
                Video video = new Video();
                video.channelKey = channel.key;
                video.videoId = videoId;
                video.originalTitle = title;
                video.title = YoutubeUtils.cleanTitle(title);
                video.url = YoutubeUtils.VIDEO_BASE + videoId;
                video.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date.replace("T", " ").replace("Z", ""));
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
        return true;
    }
    
    /**
     * Produces the queue of videos to download from the active Channel.
     *
     * @return Whether the queue was successfully produced or not.
     * @throws Exception When there is an error.
     */
    private static boolean produceQueue() throws Exception {
        if (videoMap.isEmpty()) {
            System.err.println("Must populate video map before producing the queue");
            return false;
        }
        
        List<String> save = saveFile.exists() ? FileUtils.readLines(saveFile, "UTF-8") : new ArrayList<>();
        List<String> blocked = blockedFile.exists() ? FileUtils.readLines(blockedFile, "UTF-8") : new ArrayList<>();
        List<String> queue = new ArrayList<>();
        Map<String, String> channelKeyStore = YoutubeChannelDownloader.keyStore.get(channel.name);
        
        if (retryFailed) {
            blocked.clear();
        }
        
        ChannelProcesses.performSpecialPreConditions(channel, videoMap, queue, save, blocked);
        videoMap.forEach((key, value) -> {
            save.remove(key);
            
            if (YoutubeUtils.videoExists(value.output)) {
                save.add(key);
                blocked.remove(key);
                channelKeyStore.put(key, value.output.getAbsolutePath().replace("/", "\\"));
                
            } else if (!blocked.contains(key)) {
                if (channelKeyStore.containsKey(key) && new File(channelKeyStore.get(key)).exists() &&
                        new File(channelKeyStore.get(key)).renameTo(value.output)) {
                    save.add(key);
                    channelKeyStore.replace(key, value.output.getAbsolutePath().replace("/", "\\"));
                } else {
                    queue.add(key);
                }
            }
        });
        ChannelProcesses.performSpecialPostConditions(channel, videoMap, queue, save, blocked);
        
        queue.removeAll(blocked);
        save.removeAll(blocked);
        
        FileUtils.writeLines(queueFile, queue);
        FileUtils.writeLines(saveFile, save);
        FileUtils.writeLines(blockedFile, blocked);
        saveKeyStore();
        return true;
    }
    
    /**
     * Downloads the queued videos from the active Channel.
     *
     * @return Whether the queued videos were successfully downloaded or not.
     * @throws Exception When there is an error.
     */
    private static boolean downloadVideos() throws Exception {
        if (videoMap.isEmpty()) {
            System.err.println("Must populate video map before downloading videos");
            return false;
        }
        
        List<String> queue = queueFile.exists() ? FileUtils.readLines(queueFile, "UTF-8") : new ArrayList<>();
        List<String> save = saveFile.exists() ? FileUtils.readLines(saveFile, "UTF-8") : new ArrayList<>();
        List<String> blocked = blockedFile.exists() ? FileUtils.readLines(blockedFile, "UTF-8") : new ArrayList<>();
        Map<String, String> channelKeyStore = YoutubeChannelDownloader.keyStore.get(channel.name);
        
        if (!queue.isEmpty()) {
            System.out.println("Downloading " + queue.size() + " in Queue...");
        }
        
        List<String> working = new ArrayList<>(queue);
        for (int i = 0; i < working.size(); i++) {
            String videoId = working.get(i);
            Video video = videoMap.get(videoId);
            
            System.out.println("Downloading (" + (i + 1) + '/' + working.size() + "): " + video.title);
            System.out.print("    ");
            if (YoutubeUtils.downloadYoutubeVideo(YoutubeUtils.VIDEO_BASE + videoId, video.output, channel.saveAsMp3, logCommand, logWork, channel.sponsorBlockConfig)) {
                queue.remove(videoId);
                save.add(videoId);
                channelKeyStore.put(videoId, video.output.getAbsolutePath().replace("/", "\\"));
                totalDownloads++;
                totalDataDownloaded += (video.output.length() / 1048576.0);
                System.out.println("    Download Succeeded");
            } else {
                queue.remove(videoId);
                blocked.add(videoId);
                totalDownloadFailures++;
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
        
        saveKeyStore();
        return true;
    }
    
    /**
     * Creates a playlist of the videos from the active Channel.
     *
     * @return Whether the playlist was successfully created or not.
     * @throws Exception When there is an error.
     */
    private static boolean createPlaylist() throws Exception {
        if (videoMap.isEmpty()) {
            System.err.println("Must populate video map before creating a playlist");
            return false;
        }
        
        if (channel.playlistFile == null) {
            return false;
        }
        List<String> existingPlaylist = channel.playlistFile.exists() ? FileUtils.readLines(channel.playlistFile, "UTF-8") : new ArrayList<>();
        String playlistPath = channel.playlistFile.getParentFile().getAbsolutePath() + '\\';
        
        List<String> save = saveFile.exists() ? FileUtils.readLines(saveFile, "UTF-8") : new ArrayList<>();
        List<String> playlist = new ArrayList<>();
        for (Map.Entry<String, Video> video : videoMap.entrySet()) {
            if (save.contains(video.getKey())) {
                playlist.add(video.getValue().output.getAbsolutePath().replace(playlistPath, ""));
            }
        }
        
        if (channel.isChannel()) {
            Collections.reverse(playlist);
        }
        if (channel.reversePlaylist) {
            Collections.reverse(playlist);
        }
        
        if (!channel.error) {
            if (!playlist.equals(existingPlaylist)) {
                FileUtils.writeLines(channel.playlistFile, playlist);
                
                if (channel.keepClean) {
                    File[] videos = channel.outputFolder.listFiles();
                    if (videos != null) {
                        for (File video : videos) {
                            if (video.isFile() && !playlist.contains(video.getAbsolutePath().replace(playlistPath, ""))) {
                                FileUtils.forceDeleteOnExit(video);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Loads the map of video keys and their current saved file names for each Channel.
     *
     * @throws Exception When there is an error loading the file.
     */
    private static void loadKeyStore() throws Exception {
        keyStore.clear();
        for (Channel channel : Channels.getChannels()) {
            keyStore.putIfAbsent(channel.name, new LinkedHashMap<>());
        }
        
        if (!KEY_STORE_FILE.exists()) {
            return;
        }
        
        List<String> lines = FileUtils.readLines(KEY_STORE_FILE, Charsets.UTF_8);
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            String[] lineParts = line.split("\\|\\|\\|");
            if (lineParts.length == 3) {
                keyStore.putIfAbsent(lineParts[0], new LinkedHashMap<>());
                keyStore.get(lineParts[0]).put(lineParts[1], lineParts[2]);
            }
        }
    }
    
    /**
     * Saves the map of video keys and their current saved file names for each Channel.
     *
     * @throws Exception When there is an error saving the file.
     */
    private static void saveKeyStore() throws Exception {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> keyStoreEntry : keyStore.entrySet()) {
            for (Map.Entry<String, String> keyStoreChannelEntry : keyStoreEntry.getValue().entrySet()) {
                lines.add(keyStoreEntry.getKey() + "|||" + keyStoreChannelEntry.getKey() + "|||" + keyStoreChannelEntry.getValue());
            }
        }
        FileUtils.writeLines(KEY_STORE_FILE, lines);
    }
    
    /**
     * Calculates the total data saved from Youtube.
     */
    private static void calculateData() throws Exception {
        File channelData = new File("data/channel");
        
        for (Channel channel : Channels.getChannels()) {
            File channelSaveFile = new File(channelData, channel.name + '/' + channel.name + "-save.txt");
            if (channelSaveFile.exists()) {
                List<String> save = FileUtils.readLines(channelSaveFile, StandardCharsets.UTF_8);
                Map<String, String> channelKeyStore = YoutubeChannelDownloader.keyStore.get(channel.name);
                
                for (String saved : save) {
                    if (!channelKeyStore.containsKey(saved)) {
                        continue;
                    }
                    
                    File file = new File(channelKeyStore.get(saved));
                    if (!file.exists()) {
                        continue;
                    }
                    
                    totalData += (file.length() / 1048576.0);
                    if (file.getName().toLowerCase().endsWith(".mp4")) {
                        totalVideos++;
                    } else if (file.getName().toLowerCase().endsWith(".mp3")) {
                        totalSongs++;
                    }
                }
            }
        }
    }
    
    /**
     * Prints statistics about the completed run.
     */
    private static void printStats() {
        System.out.println("Channels Processed: " + totalChannels);
        System.out.println("Videos Downloaded:  " + totalDownloads);
        System.out.println("Videos Failed:      " + totalDownloadFailures);
        System.out.println("API Calls:          " + totalApiCalls);
        System.out.println("API Failures:       " + totalApiFailures);
        System.out.println("Data Downloaded:    " + new DecimalFormat("#.##MB").format(totalDataDownloaded));
        
        if (channel == null) {
            System.out.println("Total Videos:       " + totalVideos);
            System.out.println("Total Songs:        " + totalSongs);
            System.out.println("Total Data:         " + new DecimalFormat("#.##MB").format(totalData));
        }
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Video.
     */
    public static class Video {
        
        //Fields
        
        /**
         * The key of the Channel containing the Video.
         */
        public String channelKey;
        
        /**
         * The ID of the Video.
         */
        public String videoId;
        
        /**
         * The original title of the Video.
         */
        public String originalTitle;
        
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
