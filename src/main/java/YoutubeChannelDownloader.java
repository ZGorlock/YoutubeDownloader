import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
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
    
    private static final boolean outputCommand = true;
    
    //To get the playlistId for a Youtube Channel:
    //1. Go to the Youtube Channel
    //2. View the Page Source
    //3. Search for "externalId" and copy that value
    //4. Replace the second character from a 'C' to a 'U'
    private enum Channel {
        TRAP_CITY           (true, "TrapCity", "UU65afEgL62PGFWXY7n6CUbA", new File("E:/Music/Trap/Trap City"), true, new File("E:/Music/Trap/Trap.m3u")), 
        SKY_BASS            (true, "SkyBass", "UUpXbwekw4ySNHGt26aAKvHQ", new File("E:/Music/Trap/Sky Bass"), true, new File("E:/Music/Trap/Trap.m3u")),
        TRAP_NATION         (true, "TrapNation", "UUa10nxShhzNrCE1o2ZOPztg", new File("E:/Music/Trap/Trap Nation"), true, new File("E:/Music/Trap/Trap.m3u")),
        BASS_NATION         (true, "BassNation", "UUCvVpbYRgYjMN7mG7qQN0Pg", new File("E:/Music/Trap/Bass Nation"), true, new File("E:/Music/Trap/Trap.m3u")),
        
        BRAVE_WILDERNESS    (false, "BraveWilderness", "UU6E2mP01ZLH_kbAyeazCNdg", new File("E:/Downloads/Brave Wilderness"), false);
        
        private boolean active; 
        private String name;
        private String playlistId;
        private File outputFolder;
        private boolean saveAsMp3;
        private File playlistFile;
        
        Channel(boolean active, String name, String playlistId, File outputFolder, boolean saveAsMp3, File playlistFile) {
            this.active = active;
            this.name = name;
            this.playlistId = playlistId;
            this.outputFolder = outputFolder;
            this.saveAsMp3 = saveAsMp3;
            this.playlistFile = playlistFile;
        }
        Channel(boolean active, String name, String playlistId, File outputFolder, boolean saveAsMp3) {
            this.active = active;
            this.name = name;
            this.playlistId = playlistId;
            this.outputFolder = outputFolder;
            this.saveAsMp3 = saveAsMp3;
        }
    }
    
    private static final boolean doAllChannels = true;
    private static Channel channel = null;
    
    private static Runtime runtime = Runtime.getRuntime();
    private static CloseableHttpClient httpClient = HttpClients.createDefault();
    
    private static Map<String, Video> videoMap = new HashMap<>();
    
    private static String playlistId;
    private static File outputFolder;
    private static File playlistM3u;
    
    private static File dataFile;
    private static File saveFile;
    private static File queueFile;
    private static File blockedFile;
    
    
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
    
    private static void setChannel(Channel thisChannel) {
        channel = thisChannel;
    
        playlistId = channel.playlistId;
        outputFolder = channel.outputFolder;
        playlistM3u = channel.playlistFile;
    
        dataFile = new File("data/" + channel.name + "-data.txt");
        saveFile = new File("data/" + channel.name + "-save.txt");
        queueFile = new File("data/" + channel.name + "-queue.txt");
        blockedFile = new File("data/" + channel.name + "-blocked.txt");
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
    
        FileUtils.writeStringToFile(dataFile, data.toString(), "UTF-8", false);
    }
    
    private static void processChannelData() throws Exception {
        String data = FileUtils.readFileToString(dataFile, "UTF-8");
        videoMap.clear();
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
            if ((!save.contains(key) || !value.output.exists()) && !blocked.contains(key)) {
                queue.add(key);
                save.remove(key);
            }
            if (!save.contains(key) && value.output.exists()) {
                save.add(key);
            }
        });
        FileUtils.writeLines(queueFile, queue);
        FileUtils.writeLines(saveFile, save);
        FileUtils.writeLines(blockedFile, blocked);
    }
    
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
            if (downloadYoutubeVideo(videoId, video.output, channel.saveAsMp3)) {
                if (channel.saveAsMp3 && (channel.playlistFile != null)) {
                    FileUtils.write(playlistM3u, video.output.getAbsolutePath() + System.lineSeparator(), "UTF-8", true);
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
    
    
    private static boolean downloadYoutubeVideo(String videoId, File output, boolean asMp3) throws Exception {
        String outputPath = output.getAbsolutePath();
        outputPath = outputPath.substring(0, outputPath.lastIndexOf('.'));
        
        String cmd = "youtube-dl.exe " +
                     "--output \"" + outputPath + ".%(ext)s\" " +
                     "--geo-bypass " +
                     (asMp3 ? "--extract-audio --audio-format mp3 " :
                      "--format best ") +
                     VIDEO_BASE + videoId;
        if (outputCommand) {
            System.out.println(cmd);
        }
        String result = executeProcess(cmd);
        
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
                    .replace("|", "-")
                    .replace("‒", "-")
                    .replaceAll("[^\\x00-\\x7F]", "")
                    .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
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
    
    
    private static class Video {
        public String videoId;
        public String title;
        public String url;
        public File output;
    }
    
}
