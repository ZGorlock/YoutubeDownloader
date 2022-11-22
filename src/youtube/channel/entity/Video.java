/*
 * File:    Video.java
 * Package: youtube.channel.entity
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import commons.object.collection.MapUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
import youtube.channel.ChannelEntry;
import youtube.channel.entity.base.Entity;
import youtube.channel.entity.detail.ChapterList;
import youtube.channel.entity.detail.Location;
import youtube.conf.Configurator;
import youtube.util.PathUtils;
import youtube.util.Utils;
import youtube.util.WebUtils;

/**
 * Defines a Youtube Video.
 */
public class Video extends Entity {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Video.class);
    
    
    //Fields
    
    /**
     * The id of the Video.
     */
    public String videoId;
    
    /**
     * The index position of the Video in its Youtube playlist.
     */
    public Long playlistPosition;
    
    /**
     * The string representing the duration of the Video.
     */
    public String durationString;
    
    /**
     * The duration of the Video, in seconds.
     */
    public Long duration;
    
    /**
     * The Chapter List of the Entity.
     */
    public ChapterList chapterList;
    
    /**
     * The definition of the Video.
     */
    public String definition;
    
    /**
     * The language of the Video.
     */
    public String language;
    
    /**
     * The audio language of the Video.
     */
    public String audioLanguage;
    
    /**
     * The Location of the Video.
     */
    public Location location;
    
    /**
     * The broadcast type of the Video.
     */
    public String broadcastType;
    
    /**
     * The download file for the Video.
     */
    public File download;
    
    /**
     * The output file for the Video.
     */
    public File output;
    
    
    //Constructors
    
    /**
     * Creates a Video.
     *
     * @param videoData The json data of the Video.
     * @param channel   The Channel containing the Video Entity.
     */
    @SuppressWarnings("unchecked")
    public Video(Map<String, Object> videoData, Channel channel) {
        super(videoData, channel);
        
        this.videoId = Optional.ofNullable((Map<String, Object>) getData("resourceId"))
                .map(e -> (String) e.get("videoId")).orElse(metadata.itemId);
        this.metadata.entityId = videoId;
        
        this.url = WebUtils.VIDEO_BASE + videoId;
        
        this.playlistPosition = integerParser.apply(getData("position"));
        
        this.durationString = getData("contentDetails", "duration");
        this.duration = durationParser.apply(durationString);
        this.chapterList = new ChapterList(description, duration);
        
        this.definition = getData("contentDetails", "definition");
        this.language = getData("defaultLanguage");
        this.audioLanguage = getData("defaultAudioLanguage");
        
        this.location = new Location(getDataPart("recordingDetails"));
        this.broadcastType = Optional.ofNullable((String) getData("liveBroadcastContent"))
                .orElseGet(() -> thumbnails.values().stream().anyMatch(e -> e.url.contains("_live.")) ? "live" : "none");
        
        init(channel);
    }
    
    /**
     * Creates a Video.
     *
     * @param videoData The json data of the Video.
     */
    public Video(Map<String, Object> videoData) {
        this(videoData, null);
    }
    
    /**
     * Creates a Video.
     *
     * @param videoId The id of the Video.
     * @param title   The title of the Video.
     * @param date    The date the Video was uploaded.
     * @param channel The Channel containing the Video.
     */
    @SuppressWarnings("unchecked")
    public Video(String videoId, String title, String date, Channel channel) {
        this(MapUtility.mapOf(
                        new ImmutablePair<>("snippet", MapUtility.mapOf(
                                new ImmutablePair<>("title", title),
                                new ImmutablePair<>("publishedAt", date),
                                new ImmutablePair<>("resourceId", MapUtility.mapOf(
                                        new ImmutablePair<>("videoId", videoId)))))),
                channel);
    }
    
    /**
     * Creates a Video.
     *
     * @param videoId The id of the Video.
     * @param title   The title of the Video.
     * @param date    The date the Video was uploaded.
     */
    public Video(String videoId, String title, String date) {
        this(videoId, title, date, null);
    }
    
    /**
     * The default no-argument constructor for a Video.
     */
    public Video() {
        super();
    }
    
    
    //Methods
    
    /**
     * Initializes the Channel of the Video.
     *
     * @param channel The Channel containing the Video.
     */
    @Override
    public void init(Channel channel) {
        super.init(channel);
        
        initFiles(Optional.ofNullable(channel).map(ChannelEntry::getOutputFolder).orElse(PathUtils.TMP_DIR),
                Optional.ofNullable(channel).map(ChannelEntry::isSaveAsMp3).orElse(Configurator.Config.asMp3));
    }
    
    /**
     * Initializes the file locations of the Video.
     *
     * @param outputDir The output directory of the Video.
     * @param saveAsMp3 Whether to save the Video as an mp3 or not.
     */
    public void initFiles(File outputDir, boolean saveAsMp3) {
        this.download = new File(outputDir, this.title);
        this.output = new File(outputDir, (this.title + '.' + (saveAsMp3 ? Utils.AUDIO_FORMAT : Utils.VIDEO_FORMAT)));
    }
    
    /**
     * Returns whether the Video is a live stream.
     *
     * @return Whether the Video is a live stream.
     */
    public boolean isLiveStream() {
        return Optional.ofNullable(broadcastType).map(e -> List.of("live", "upcoming").contains(e)).orElse(false);
    }
    
    /**
     * Returns whether the Video is deleted.
     *
     * @return Whether the Video is deleted.
     */
    public boolean isDeleted() {
        return Optional.ofNullable(title).map(e -> e.equalsIgnoreCase("Deleted video")).orElse(false);
    }
    
    /**
     * Returns whether the Video is valid for processing.
     *
     * @return Whether the Video is valid for processing.
     */
    public boolean isValid() {
        return !isPrivate() && !isDeleted() && !isLiveStream();
    }
    
    /**
     * Updates the title of the Video.
     *
     * @param title The title.
     */
    public void updateTitle(String title) {
        this.title = Utils.cleanVideoTitle(title);
        this.download = new File(this.download.getParentFile(), this.title);
        this.output = new File(this.output.getParentFile(), (this.title + '.' + Utils.getFileFormat(this.output.getName())));
    }
    
    /**
     * Updates the output folder of the Video.
     *
     * @param outputDir The output folder.
     */
    public void updateOutputDir(File outputDir) {
        this.download = new File(outputDir, this.download.getName());
        this.output = new File(outputDir, this.output.getName());
    }
    
    /**
     * Updates the output file of the Video.
     *
     * @param output The output file.
     */
    public void updateOutput(File output) {
        updateTitle(output.getName().replaceAll("\\.[^.]+$", ""));
        updateOutputDir(output.getParentFile());
    }
    
}
