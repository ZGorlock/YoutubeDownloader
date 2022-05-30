/*
 * File:    YoutubeChannelDownloader.java
 * Package: youtube
 * Author:  Zachary Gill
 */

package youtube;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
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
import youtube.channel.Channel;
import youtube.channel.ChannelProcesses;
import youtube.channel.Channels;
import youtube.channel.KeyStore;
import youtube.util.Color;
import youtube.util.Configurator;
import youtube.util.YoutubeUtils;

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
            System.out.println(Color.bad("Must supply a Google API key with Youtube Data API enabled in ") + Color.file("./apiKey"));
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
     * The maximum number of API playlist pages to save to a single file.
     */
    private static final int MAX_PAGES_PER_FILE = 100;
    
    //Loads the settings, keystore, and Channel configurations
    static {
        Configurator.loadSettings("YoutubeChannelDownloader");
        Channels.loadChannels();
        KeyStore.load();
    }
    
    
    //Static Fields
    
    /**
     * The current Channel being processed.
     */
    public static Channel channel = null;
    
    /**
     * The video map for the Channel being processed.
     */
    private static final Map<String, Video> videoMap = new LinkedHashMap<>();
    
    /**
     * The HTTP Client used to interact with the Youtube API.
     */
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    
    
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
        
        Channels.print();
        
        run();
        
        Stats.print();
    }
    
    
    //Methods
    
    /**
     * Processes all Channels.
     *
     * @throws Exception When there is an error.
     */
    private static void run() throws Exception {
        if (Configurator.Config.channel == null) {
            boolean skip = (Configurator.Config.startAt != null);
            boolean stop = (Configurator.Config.stopAt != null);
            
            if (!((skip && stop) && (Channels.indexOf(Configurator.Config.stopAt.key) < Channels.indexOf(Configurator.Config.startAt.key)))) {
                for (Channel currentChannel : Channels.getChannels()) {
                    if (!(skip &= (currentChannel != Configurator.Config.startAt)) &&
                            ((Configurator.Config.group == null) || (currentChannel.group.equalsIgnoreCase(Configurator.Config.group)))) {
                        if (currentChannel.active) {
                            setChannel(currentChannel);
                            processChannel();
                        }
                        if (stop && (currentChannel == Configurator.Config.stopAt)) {
                            break;
                        }
                    }
                }
            }
            
        } else if (Configurator.Config.channel.active) {
            setChannel(Configurator.Config.channel);
            processChannel();
        }
        
        KeyStore.save();
        
        System.out.println(YoutubeUtils.NEWLINE);
    }
    
    /**
     * Sets the active Channel.
     *
     * @param newChannel The Channel to be processed.
     * @throws Exception When there is an error.
     */
    private static void setChannel(Channel newChannel) throws Exception {
        channel = newChannel;
        channel.state.cleanup();
        channel.state.load();
    }
    
    /**
     * Processes the active Channel.
     *
     * @return Whether the Channel was successfully processed or not.
     * @throws Exception When there is an error.
     */
    private static boolean processChannel() throws Exception {
        System.out.println(YoutubeUtils.NEWLINE);
        System.out.println(Color.base("Processing Channel: ") + Color.channel(channel.name));
        
        boolean success = YoutubeUtils.isOnline() &&
                fetchChannelData() &&
                processChannelData() &&
                produceQueue() &&
                downloadVideos() &&
                createPlaylist() &&
                cleanChannel();
        
        Stats.totalChannels++;
        
        return success;
    }
    
    /**
     * Retrieves the data for the active Channel from the Youtube API.
     *
     * @return Whether the Channel data was successfully retrieved or not.
     * @throws Exception When there is an error.
     */
    private static boolean fetchChannelData() throws Exception {
        if (Configurator.Config.preventChannelFetch) {
            return true;
        }
        
        Map<String, String> parameters = new HashMap<>();
        parameters.put("part", "snippet");
        parameters.put("maxResults", "50");
        parameters.put("playlistId", channel.playlistId);
        parameters.put("key", API_KEY);
        
        int page = 0;
        List<String> data = new ArrayList<>();
        List<String> calls = new ArrayList<>();
        boolean more = false;
        do {
            HttpGet request = new HttpGet(REQUEST_BASE + YoutubeUtils.buildParameterString(parameters));
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
                    
                    if (((++page % MAX_PAGES_PER_FILE) == 0) || !more) {
                        FileUtils.writeStringToFile(
                                channel.state.getDataFile(page / MAX_PAGES_PER_FILE),
                                data.stream().collect(Collectors.joining(",", ("[" + System.lineSeparator()), "]")),
                                "UTF-8", false);
                        data.clear();
                    }
                }
                
            }
        } while (more);
        
        FileUtils.writeLines(channel.state.callLogFile, calls);
        return true;
    }
    
    /**
     * Processes the data for the active Channel that was retrieved from the Youtube API.
     *
     * @return Whether the Channel data was successfully processed or not.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("RedundantStreamOptionalCall")
    private static boolean processChannelData() throws Exception {
        videoMap.clear();
        
        return channel.state.getDataFiles().stream()
                .sorted(Comparator.comparing(File::getName)).allMatch(chunk -> {
                    try {
                        return processChannelData(chunk);
                    } catch (Exception e) {
                        System.out.println(Color.bad("Error while parsing Channel data"));
                        return false;
                    }
                });
    }
    
    /**
     * Processes a chunk of data for the active Channel that was retrieved from the Youtube API.
     *
     * @param chunk The data chunk file.
     * @return Whether the Channel data chunk was successfully processed or not.
     * @throws Exception When there is an error.
     */
    private static boolean processChannelData(File chunk) throws Exception {
        String data = FileUtils.readFileToString(chunk, "UTF-8");
        
        if (data.contains("\"code\": 404")) {
            System.out.println(Color.bad("The Channel ") + Color.channel(channel.name) + Color.bad(" does not exist"));
            return false;
        }
        if (data.contains("\"code\": 403")) {
            System.out.println(Color.bad("Your API Key is not authorized or has exceeded its quota"));
            return false;
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
                
                Video video = new Video(videoId, title, date, channel);
                
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
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean produceQueue() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println(Color.bad("Must populate video map before producing the queue"));
            return false;
        }
        
        channel.state.queue.clear();
        if (Configurator.Config.retryFailed) {
            channel.state.blocked.clear();
        }
        
        ChannelProcesses.performSpecialPreConditions(channel, videoMap, channel.state.queue, channel.state.saved, channel.state.blocked);
        
        videoMap.values().stream().collect(Collectors.groupingBy(e -> e.title)).entrySet()
                .stream().filter(e -> (e.getValue().size() > 1)).forEach(e ->
                        System.out.println(Color.bad("The title: '") + Color.video(e.getValue().get(0).title) + Color.bad("' appears ") + Color.number(e.getValue().size()) + Color.bad(" times")));
        
        videoMap.forEach((videoId, video) -> {
            channel.state.saved.remove(videoId);
            
            if (video.output.exists()) {
                channel.state.saved.add(videoId);
                channel.state.blocked.remove(videoId);
                channel.state.keyStore.put(videoId, video.output.getAbsolutePath().replace("/", "\\"));
                
            } else if (!channel.state.blocked.contains(videoId)) {
                File oldOutput = (channel.state.keyStore.containsKey(videoId) && new File(channel.state.keyStore.get(videoId)).exists()) ?
                                 new File(channel.state.keyStore.get(videoId)) : YoutubeUtils.findVideo(video.output);
                
                if ((oldOutput == null) || !oldOutput.exists()) {
                    channel.state.queue.add(videoId);
                    
                } else {
                    File newOutput = new File(video.channel.outputFolder, video.output.getName()
                            .replace(("." + YoutubeUtils.getFormat(video.output.getName())), ("." + YoutubeUtils.getFormat(oldOutput.getName()))));
                    
                    if (!oldOutput.getName().equals(newOutput.getName())) {
                        if (!Configurator.Config.preventRenaming) {
                            System.out.println(Color.base("Renaming: '") + Color.video(oldOutput.getName()) + Color.base("' to: '") + Color.video(newOutput.getName()) + Color.base("'"));
                            oldOutput.renameTo(newOutput);
                            
                            video.updateOutput(newOutput);
                            channel.state.saved.add(videoId);
                            channel.state.keyStore.replace(videoId, video.output.getAbsolutePath().replace("/", "\\"));
                            
                            if (channel.saveAsMp3) {
                                Stats.totalAudioRenames++;
                            } else {
                                Stats.totalVideoRenames++;
                            }
                            
                        } else {
                            System.out.println(Color.bad("Would have renamed: '") + Color.video(oldOutput.getName()) + Color.bad("' to: '") + Color.video(newOutput.getName()) + Color.bad("' but renaming is disabled"));
                            channel.state.queue.add(videoId);
                        }
                        
                    } else {
                        video.output = newOutput;
                        channel.state.saved.add(videoId);
                        channel.state.keyStore.replace(videoId, video.output.getAbsolutePath().replace("/", "\\"));
                    }
                }
            }
        });
        
        ChannelProcesses.performSpecialPostConditions(channel, videoMap, channel.state.queue, channel.state.saved, channel.state.blocked);
        
        channel.state.save();
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
            System.out.println(Color.bad("Must populate video map before downloading videos"));
            return false;
        }
        
        if (!channel.state.queue.isEmpty()) {
            System.out.println(Color.number(String.valueOf(channel.state.queue.size())) + Color.base(" in Queue..."));
        }
        
        List<String> working = new ArrayList<>(channel.state.queue);
        for (int i = 0; i < working.size(); i++) {
            String videoId = working.get(i);
            Video video = videoMap.get(videoId);
            
            if (!Configurator.Config.preventDownload) {
                System.out.println(Color.base("Downloading (") + Color.number(i + 1) + Color.base("/") + Color.number(working.size()) + Color.base("): ") + Color.video(video.title));
            } else {
                System.out.println(Color.bad("Would have downloaded: '") + Color.video(video.title) + Color.bad("' but downloading is disabled"));
                continue;
            }
            
            switch (YoutubeUtils.downloadYoutubeVideo(video)) {
                case SUCCESS:
                    channel.state.saved.add(videoId);
                    channel.state.keyStore.put(videoId, video.output.getAbsolutePath().replace("/", "\\"));
                    
                    if (channel.saveAsMp3) {
                        Stats.totalAudioDownloads++;
                        Stats.totalAudioDataDownloaded += video.output.length();
                    } else {
                        Stats.totalVideoDownloads++;
                        Stats.totalVideoDataDownloaded += video.output.length();
                    }
                    System.out.println(YoutubeUtils.INDENT + Color.good("Download Succeeded"));
                    break;
                
                case ERROR:
                    channel.state.blocked.add(videoId);
                
                case FAILURE:
                    if (channel.saveAsMp3) {
                        Stats.totalAudioDownloadFailures++;
                    } else {
                        Stats.totalVideoDownloadFailures++;
                    }
                    System.out.println(YoutubeUtils.INDENT + Color.bad("Download Failed"));
                    break;
            }
            channel.state.queue.remove(videoId);
            
            channel.state.save();
        }
        
        File[] partFiles = channel.outputFolder.listFiles(e -> e.getName().endsWith(".part"));
        if (partFiles != null) {
            for (File partFile : partFiles) {
                if (!Configurator.Config.preventDeletion) {
                    System.out.println(Color.base("Deleting: '") + Color.video(partFile.getName()) + Color.base("'"));
                    FileUtils.forceDelete(partFile);
                } else {
                    System.out.println(Color.bad("Would have deleted: '") + Color.video(partFile.getName()) + Color.bad("' but deletion is disabled"));
                }
            }
        }
        
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
            System.out.println(Color.bad("Must populate video map before creating a playlist"));
            return false;
        }
        
        if (channel.playlistFile == null) {
            return false;
        }
        List<String> existingPlaylist = channel.playlistFile.exists() ? FileUtils.readLines(channel.playlistFile, "UTF-8") : new ArrayList<>();
        String playlistPath = channel.playlistFile.getParentFile().getAbsolutePath() + '\\';
        
        List<String> playlist = new ArrayList<>();
        for (Map.Entry<String, Video> video : videoMap.entrySet()) {
            if (channel.state.saved.contains(video.getKey())) {
                playlist.add(video.getValue().output.getAbsolutePath().replace(playlistPath, ""));
            }
        }
        
        if (channel.isChannel()) {
            Collections.reverse(playlist);
        }
        if (channel.reversePlaylist) {
            Collections.reverse(playlist);
        }
        
        if (!channel.error && !playlist.equals(existingPlaylist)) {
            if (!Configurator.Config.preventPlaylistEdit) {
                System.out.println(Color.base("Updating playlist: '") + Color.file(channel.playlistFile.getName()) + Color.base("'"));
                FileUtils.writeLines(channel.playlistFile, playlist);
            } else {
                System.out.println(Color.bad("Would have updated playlist: '") + Color.file(channel.playlistFile.getName()) + Color.base("' but playlist modification is disabled"));
            }
        }
        return true;
    }
    
    /**
     * Cleans the output directory of the active Channel.
     *
     * @return Whether the output directory was successfully cleaned or not.
     * @throws Exception When there is an error.
     */
    private static boolean cleanChannel() throws Exception {
        if (videoMap.isEmpty()) {
            System.out.println(Color.bad("Must populate video map before cleaning the channel directory"));
            return false;
        }
        
        List<String> saved = new ArrayList<>();
        for (Map.Entry<String, Video> video : videoMap.entrySet()) {
            if (channel.state.saved.contains(video.getKey())) {
                saved.add(video.getValue().output.getAbsolutePath());
            }
        }
        
        if (!channel.error && channel.keepClean) {
            
            File[] videos = channel.outputFolder.listFiles();
            if (videos != null) {
                for (File video : videos) {
                    if (video.isFile() && !saved.contains(video.getAbsolutePath())) {
                        boolean isPartFile = video.getName().endsWith(".part");
                        String printedFile = Color.apply((isPartFile ? Color.FILE : Color.VIDEO), video.getName());
                        
                        if (!Configurator.Config.preventDeletion) {
                            System.out.println(Color.base("Deleting: '") + printedFile + Color.base("'"));
                            FileUtils.forceDelete(video);
                            
                            if (!isPartFile) {
                                if (channel.saveAsMp3) {
                                    Stats.totalAudioDeletions++;
                                } else {
                                    Stats.totalVideoDeletions++;
                                }
                            }
                            
                        } else {
                            System.out.println(Color.bad("Would have deleted: '") + printedFile + Color.bad("' but deletion is disabled"));
                        }
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Keeps track of statistics for the Youtube Channel Downloader.
     */
    private static class Stats {
        
        //Static Fields
        
        /**
         * A counter of the total number of Channels that were processed this run.
         */
        static int totalChannels = 0;
        
        /**
         * A counter of the total number of video files that were downloaded this run.
         */
        static int totalVideoDownloads = 0;
        
        /**
         * A counter of the total number of audio files that were downloaded this run.
         */
        static int totalAudioDownloads = 0;
        
        /**
         * A counter of the total number of video files that were renamed this run.
         */
        static int totalVideoRenames = 0;
        
        /**
         * A counter of the total number of audio files that were renamed this run.
         */
        static int totalAudioRenames = 0;
        
        /**
         * A counter of the total number of video files that were deleted this run.
         */
        static int totalVideoDeletions = 0;
        
        /**
         * A counter of the total number of audio files that were deleted this run.
         */
        static int totalAudioDeletions = 0;
        
        /**
         * A counter of the total number of video files that failed to download this run.
         */
        static int totalVideoDownloadFailures = 0;
        
        /**
         * A counter of the total number of audio files that failed to download this run.
         */
        static int totalAudioDownloadFailures = 0;
        
        /**
         * A counter of the total video data downloaded from Youtube this run, in bytes.
         */
        static long totalVideoDataDownloaded = 0L;
        
        /**
         * A counter of the total audio data downloaded from Youtube this run, in bytes.
         */
        static long totalAudioDataDownloaded = 0L;
        
        /**
         * A counter of the total number of times the Youtube Data API was called this run.
         */
        static int totalApiCalls = 0;
        
        /**
         * A counter of the total number of times calling the Youtube Data API failed this run.
         */
        static int totalApiFailures = 0;
        
        /**
         * A counter of the total number of video files saved from Youtube.
         */
        static int totalVideo = 0;
        
        /**
         * A counter of the total number of audio files saved from Youtube.
         */
        static int totalAudio = 0;
        
        /**
         * A counter of the total video data saved from Youtube, in bytes.
         */
        static long totalVideoData = 0L;
        
        /**
         * A counter of the total audio data saved from Youtube, in bytes.
         */
        static long totalAudioData = 0L;
        
        
        //Functions
        
        /**
         * Calculates the total data saved from Youtube.
         */
        private static void calculateData() {
            for (Channel channel : Channels.getChannels()) {
                if (channel.state.saveFile.exists()) {
                    
                    for (String saved : channel.state.saved) {
                        if (!channel.state.keyStore.containsKey(saved)) {
                            continue;
                        }
                        
                        File file = new File(channel.state.keyStore.get(saved));
                        if (!file.exists()) {
                            continue;
                        }
                        
                        if (YoutubeUtils.VIDEO_FORMATS.contains(YoutubeUtils.getFormat(file.getName()))) {
                            Stats.totalVideo++;
                            Stats.totalVideoData += file.length();
                        } else if (YoutubeUtils.AUDIO_FORMATS.contains(YoutubeUtils.getFormat(file.getName()))) {
                            Stats.totalAudio++;
                            Stats.totalAudioData += file.length();
                        }
                    }
                }
            }
        }
        
        /**
         * Prints statistics about the completed run.
         */
        public static void print() {
            if (!Configurator.Config.printStats) {
                return;
            }
            
            DecimalFormat integerFormat = new DecimalFormat("#,##0");
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
            double bytesInMb = 1048576.0;
            BiConsumer<String, Object> printer = (String title, Object value) -> {
                switch (Optional.ofNullable(value).map(e -> e.getClass().getSimpleName()).orElse("String")) {
                    case "Integer":
                    case "int":
                    case "Long":
                    case "long":
                        System.out.println(YoutubeUtils.INDENT +
                                Color.base(title) + Color.number(integerFormat.format(value)));
                        break;
                    case "Double":
                    case "double":
                        System.out.println(YoutubeUtils.INDENT +
                                Color.base(title) + Color.number(decimalFormat.format((double) value / bytesInMb)) + Color.base("MB"));
                        break;
                    case "String":
                        System.out.println(Color.link(YoutubeUtils.formatHeader(title)));
                        break;
                }
            };
            
            calculateData();
            
            System.out.println(YoutubeUtils.NEWLINE);
            System.out.println(Color.number("--- Stats ---"));
            
            printer.accept("CHANNEL:", null);
            printer.accept("Channels Processed: ... ", totalChannels);
            printer.accept("Total Channels: ....... ", Channels.getChannels().size());
            
            printer.accept("RUN:", null);
            printer.accept("Downloaded: ........... ", (totalVideoDownloads + totalAudioDownloads));
            printer.accept("    Video: ............ ", totalVideoDownloads);
            printer.accept("    Audio: ............ ", totalAudioDownloads);
            printer.accept("Renamed: .............. ", (totalVideoRenames + totalAudioRenames));
            printer.accept("    Video: ............ ", totalVideoRenames);
            printer.accept("    Audio: ............ ", totalAudioRenames);
            printer.accept("Deleted: .............. ", (totalVideoDeletions + totalAudioDeletions));
            printer.accept("    Video: ............ ", totalVideoDeletions);
            printer.accept("    Audio: ............ ", totalAudioDeletions);
            printer.accept("Failed: ............... ", (totalVideoDownloadFailures + totalAudioDownloadFailures));
            printer.accept("    Video: ............ ", totalVideoDownloadFailures);
            printer.accept("    Audio: ............ ", totalAudioDownloadFailures);
            printer.accept("Data Downloaded: ...... ", (double) (totalVideoDataDownloaded + totalAudioDataDownloaded));
            printer.accept("    Video: ............ ", (double) totalVideoDataDownloaded);
            printer.accept("    Audio: ............ ", (double) totalAudioDataDownloaded);
            
            printer.accept("API:", null);
            printer.accept("Api Calls: ............ ", totalApiCalls);
            printer.accept("Api Failures: ......... ", totalApiFailures);
            
            printer.accept("OVERALL:", null);
            printer.accept("Total: ................ ", (totalVideo + totalAudio));
            printer.accept("    Video: ............ ", totalVideo);
            printer.accept("    Audio: ............ ", totalAudio);
            printer.accept("Total Data: ........... ", (double) (totalVideoData + totalAudioData));
            printer.accept("    Video: ............ ", (double) totalVideoData);
            printer.accept("    Audio: ............ ", (double) totalAudioData);
            
            System.out.println(YoutubeUtils.NEWLINE);
        }
        
    }
    
    /**
     * Defines a Video.<br>
     * Kept for backward compatibility.
     */
    public static class Video extends youtube.channel.Video {
        
        //Constructors
        
        /**
         * Creates a Video.
         *
         * @param videoId The ID of the Video.
         * @param title   The title of the Video.
         * @param date    The date the Video was uploaded.
         * @param channel The Channel containing the Video.
         */
        public Video(String videoId, String title, String date, Channel channel) {
            super(videoId, title, date, channel);
        }
        
    }
    
}
