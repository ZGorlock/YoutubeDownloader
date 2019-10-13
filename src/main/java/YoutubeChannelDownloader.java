import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    
    private enum Channel {
        TRAP_CITY ("TrapCity", "UU65afEgL62PGFWXY7n6CUbA", new File("E:/Music/Trap City/Songs"), new File("E:/Music/Trap City/Trap City.m3u"));
        
        private String name;
        private String playlistId;
        private File outputFolder;
        private File playlistFile;
        
        Channel(String name, String playlistId, File outputFolder, File playlistFile) {
            this.name = name;
            this.playlistId = playlistId;
            this.outputFolder = outputFolder;
            this.playlistFile = playlistFile;
        }
        Channel(String name, String playlistId, File outputFolder) {
            this.name = name;
            this.playlistId = playlistId;
            this.outputFolder = outputFolder;
        }
    }
    
    private static final Channel channel = Channel.TRAP_CITY;
    private static final boolean saveAsMp3 = true;
    private static final boolean addToPlaylist = true;
    
    private static final String PLAYLIST_ID = channel.playlistId;
    private static final File OUTPUT_FOLDER = channel.outputFolder;
    private static final File PLAYLIST_M3U = channel.playlistFile;
    
    private static final File DATA_FILE = new File(channel.name + "-data.txt");
    private static final File SAVE_FILE = new File(channel.name + "-save.txt");
    private static final File QUEUE_FILE = new File(channel.name + "-queue.txt");
    private static final File BLOCKED_FILE = new File(channel.name + "-blocked.txt");
    
    private static Runtime runtime = Runtime.getRuntime();
    private static CloseableHttpClient httpClient = HttpClients.createDefault();
    
    private static Map<String, Video> videoMap = new HashMap<>();
    
    
    public static void main(String[] args) throws Exception {
        getChannelData();
        processChannelData();
        produceQueue();
        downloadVideos();
    }
    
    
    private static void getChannelData() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("part", "snippet");
        parameters.put("maxResults", "50");
        parameters.put("playlistId", PLAYLIST_ID);
        parameters.put("key", API_KEY);
    
        StringBuilder data = new StringBuilder("[").append(System.lineSeparator());
        boolean more;
        boolean first = true;
        do {
            HttpGet request = new HttpGet(REQUEST_BASE + buildParameterString(parameters));
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
    
        FileUtils.writeStringToFile(DATA_FILE, data.toString(), "UTF-8", false);
    }
    
    private static void processChannelData() throws Exception {
        String data = FileUtils.readFileToString(DATA_FILE, "UTF-8");
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
                
                Video video = new Video();
                video.videoId = videoId;
                video.title = cleanTitle(title);
                video.url = VIDEO_BASE + videoId;
                video.output = new File(OUTPUT_FOLDER, video.title + (saveAsMp3 ? ".mp3" : ".mp4"));
                
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
        
        List<String> save = SAVE_FILE.exists() ? FileUtils.readLines(SAVE_FILE, "UTF-8") : new ArrayList<>();
        List<String> blocked = BLOCKED_FILE.exists() ? FileUtils.readLines(BLOCKED_FILE, "UTF-8") : new ArrayList<>();
        List<String> queue = new ArrayList<>();
        videoMap.forEach((key, value) -> {
            if ((!save.contains(key) || !value.output.exists()) && !blocked.contains(key)) {
                queue.add(key);
                save.remove(key);
            }
            if (!save.contains(key) && value.output.exists()) {
                save.add(key);
            }
        });
        FileUtils.writeLines(QUEUE_FILE, queue);
        FileUtils.writeLines(SAVE_FILE, save);
        FileUtils.writeLines(BLOCKED_FILE, blocked);
    }
    
    private static void downloadVideos() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println("Must populate video map before downloading videos");
            return;
        }
        
        List<String> queue = QUEUE_FILE.exists() ? FileUtils.readLines(QUEUE_FILE, "UTF-8") : new ArrayList<>();
        List<String> save = SAVE_FILE.exists() ? FileUtils.readLines(SAVE_FILE, "UTF-8") : new ArrayList<>();
        List<String> blocked = BLOCKED_FILE.exists() ? FileUtils.readLines(BLOCKED_FILE, "UTF-8") : new ArrayList<>();
        
        List<String> working = new ArrayList<>(queue);
        for (String videoId : working) {
            Video video = videoMap.get(videoId);
            
            System.out.println("Downloading: " + video.title);
            if (downloadYoutubeVideo(videoId, video.output, saveAsMp3)) {
                if (saveAsMp3 && addToPlaylist) {
                    FileUtils.write(PLAYLIST_M3U, video.output.getAbsolutePath() + System.lineSeparator(), "UTF-8", true);
                }
                
                queue.remove(videoId);
                save.add(videoId);
            } else {
                queue.remove(videoId);
                blocked.add(videoId);
            }
            
            FileUtils.writeLines(QUEUE_FILE, queue);
            FileUtils.writeLines(SAVE_FILE, save);
            FileUtils.writeLines(BLOCKED_FILE, blocked);
        }
    }
    
    
    private static boolean downloadYoutubeVideo(String videoId, File output, boolean asMp3) throws Exception {
        String outputPath = output.getAbsolutePath();
        outputPath = outputPath.substring(0, outputPath.lastIndexOf('.'));
        
        String result = executeProcess("youtube-dl.exe " +
                       "--output \"" + outputPath + ".%(ext)s\" " +
                       (asMp3 ? "--extract-audio --audio-format mp3 " : "") +
                       VIDEO_BASE + videoId);
        
        return result.split("\r\n").length > 2;
    }
    
    private static String buildParameterString(Map<String, String> parameters) throws Exception {
        StringBuilder parameterString = new StringBuilder("?");
        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            if (parameterString.length() > 1) {
                parameterString.append("&");
            }
            parameterString.append(URLEncoder.encode(parameterEntry.getKey(), "UTF-8"))
                           .append("=")
                           .append(URLEncoder.encode(parameterEntry.getValue(), "UTF-8"));
        }
        return parameterString.toString();
    }
    
    private static String cleanTitle(String title) {
        return title.replace("\\", "-")
                    .replace("/", "-")
                    .replace(":", "-")
                    .replace("*", "-")
                    .replace("?", "")
                    .replace("\"", "'")
                    .replace("<", "-")
                    .replace(">", "-")
                    .replace("|", "-");
    }
    
    private static String executeProcess(String cmd) {
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", cmd);
        
            Process process = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            StringBuilder response = new StringBuilder();
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                response.append(line).append(System.lineSeparator());
            }
            
            process.waitFor();
            r.close();
            process.destroy();
        
            return response.toString();
        
        } catch (Exception e) {
            return "Failed";
        }
    }
    
    private static void rewritePlaylist() throws Exception {
        List<String> list = new ArrayList<>();
        Files.list(OUTPUT_FOLDER.toPath()).forEach(var -> list.add(Paths.get(var.toString()).toFile().getAbsolutePath()));
        Collections.shuffle(list);
        FileUtils.writeLines(PLAYLIST_M3U, list);
    }
    
    
    private static class Video {
        public String videoId;
        public String title;
        public String url;
        public File output;
    }
    
}
