import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

public class YoutubeChannelDownloader {
    
    //Constants
    
    private static String API_KEY = "";
    
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
    
    private static final String REQUEST_BASE = "https://www.googleapis.com/youtube/v3/playlistItems";
    
    private static final String VIDEO_BASE = "https://www.youtube.com/watch?v=";
    
    
    //Static Fields
    
    private static final boolean logCommand = true;
    
    private static final boolean logWork = false;
    
    private static final boolean doAllChannels = true;
    
    private static Channel channel = null;
    
    private static Runtime runtime = Runtime.getRuntime();
    
    private static CloseableHttpClient httpClient = HttpClients.createDefault();
    
    private static Map<String, Video> videoMap = new LinkedHashMap<>();
    
    private static String playlistId;
    
    private static File outputFolder;
    
    private static File playlistM3u;
    
    private static File dataFile;
    
    private static File saveFile;
    
    private static File queueFile;
    
    private static File blockedFile;
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
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
    
    private static void processChannel() throws Exception {
        System.out.println("Processing Channel: " + channel.name);
        System.out.println();
        
        getChannelData();
        processChannelData();
        produceQueue();
        downloadVideos();
        
        System.out.println();
    }
    
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
    
    private static void processChannelData() throws Exception {
        String data = FileUtils.readFileToString(dataFile, "UTF-8");
        videoMap.clear();
        if (data.contains("\"code\": 404")) {
            System.err.println("The Playlist for " + channel.name + " does not exist");
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
                
                String videoId = (String) resourceId.get("videoId");
                String title = (String) snippet.get("title");
                String date = (String) snippet.get("publishedAt");
                
                Video video = new Video();
                video.videoId = videoId;
                video.title = YoutubeUtils.cleanTitle(title);
                video.url = VIDEO_BASE + videoId;
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
    
    private static void produceQueue() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println("Must populate video map before producing the queue");
            return;
        }
        
        List<String> save = saveFile.exists() ? FileUtils.readLines(saveFile, "UTF-8") : new ArrayList<>();
        List<String> blocked = blockedFile.exists() ? FileUtils.readLines(blockedFile, "UTF-8") : new ArrayList<>();
        List<String> queue = new ArrayList<>();
        videoMap.forEach((key, value) -> {
            if (!save.contains(key) && videoExists(value.output)) {
                save.add(key);
            }
            if ((!save.contains(key) || !videoExists(value.output)) && !blocked.contains(key)) {
                queue.add(key);
                save.remove(key);
            }
        });
        Channel.performSpecialConditions(channel, videoMap, queue, save, blocked);
        
        FileUtils.writeLines(queueFile, queue);
        FileUtils.writeLines(saveFile, save);
        FileUtils.writeLines(blockedFile, blocked);
    }
    
    private static boolean videoExists(File output) {
        File outputDir = output.getParentFile();
        if (!outputDir.exists()) {
            return false;
        }
        File[] existingFiles = outputDir.listFiles();
        if (existingFiles == null) {
            return false;
        }
        
        String outputName = output.getName().replaceAll("[^a-zA-Z0-9]", "").replaceAll("\\s+", " ");
        for (File existingFile : existingFiles) {
            String existingName = existingFile.getName().replaceAll("[^a-zA-Z0-9]", "").replaceAll("\\s+", " ");
            if (existingName.equalsIgnoreCase(outputName)) {
                return true;
            }
        }
        return false;
    }
    
    private static void downloadVideos() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println("Must populate video map before downloading videos");
            return;
        }
        
        List<String> queue = queueFile.exists() ? FileUtils.readLines(queueFile, "UTF-8") : new ArrayList<>();
        List<String> save = saveFile.exists() ? FileUtils.readLines(saveFile, "UTF-8") : new ArrayList<>();
        List<String> blocked = blockedFile.exists() ? FileUtils.readLines(blockedFile, "UTF-8") : new ArrayList<>();
        
        if (channel.saveAsMp3 && (channel.playlistFile != null)) {
            List<String> current = playlistM3u.exists() ? FileUtils.readLines(playlistM3u, "UTF-8") : new ArrayList<>();
            for (String saved : save) {
                if (!videoMap.containsKey(saved)) {
                    continue;
                }
                String playlistEntry = videoMap.get(saved).output.getAbsolutePath();
                if (!current.contains(playlistEntry)) {
                    FileUtils.write(playlistM3u, playlistEntry + System.lineSeparator(), "UTF-8", true);
                }
            }
        }
        
        List<String> working = new ArrayList<>(queue);
        for (String videoId : working) {
            Video video = videoMap.get(videoId);
            
            System.out.println("Downloading: " + video.title);
            if (YoutubeUtils.downloadYoutubeVideo(VIDEO_BASE + videoId, video.output, channel.saveAsMp3, logCommand, logWork)) {
                if (channel.saveAsMp3 && (channel.playlistFile != null)) {
                    List<String> current = playlistM3u.exists() ? FileUtils.readLines(playlistM3u, "UTF-8") : new ArrayList<>();
                    if (!current.contains(video.output.getAbsolutePath())) {
                        FileUtils.write(playlistM3u, video.output.getAbsolutePath() + System.lineSeparator(), "UTF-8", true);
                    }
                }
                
                queue.remove(videoId);
                save.add(videoId);
            } else {
                queue.remove(videoId);
                blocked.add(videoId);
            }
            
            FileUtils.writeLines(queueFile, queue);
            FileUtils.writeLines(saveFile, save);
            FileUtils.writeLines(blockedFile, blocked);
        }
    }
    
    
    //Inner Classes
    
    public static class Video {
        
        public String videoId;
        
        public String title;
        
        public String url;
        
        public Date date;
        
        public File output;
        
    }
    
}
