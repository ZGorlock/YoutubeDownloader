/*
 * File:    VideoInfo.java
 * Package: youtube.entity.info
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.base.EntityInfo;
import youtube.entity.info.detail.ChapterList;
import youtube.entity.info.detail.Location;
import youtube.entity.info.detail.ThumbnailSet;
import youtube.entity.info.detail.base.EntityDetailSet;
import youtube.util.WebUtils;

/**
 * Defines the Video Info of a Youtube Video.
 */
public class VideoInfo extends EntityInfo {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(VideoInfo.class);
    
    
    //Constants
    
    /**
     * A list of titles indicating that a Video is private.
     */
    private static final String[] PRIVATE_TITLES = new String[] {
            "Private video"
    };
    
    /**
     * A list of titles indicating that a Video is deleted.
     */
    private static final String[] DELETED_TITLES = new String[] {
            "Deleted video"
    };
    
    /**
     * A list of broadcast types indicating that a Video is a live stream.
     */
    private static final String[] LIVE_STREAM_BROADCAST_TYPES = new String[] {
            "live",
            "upcoming"
    };
    
    
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
     * The Chapter List of the Video.
     */
    public ChapterList chapters;
    
    /**
     * The quality definition of the Video.
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
    
    
    //Constructors
    
    /**
     * Creates a Video Info.
     *
     * @param videoData The json data of the Video.
     */
    public VideoInfo(Map<String, Object> videoData) {
        super(videoData);
        
        this.videoId = metadata.getEntityId();
        this.url = WebUtils.VIDEO_BASE + videoId;
        
        this.playlistPosition = integerParser.apply(getData("snippet", "position"));
        
        this.durationString = getData("contentDetails", "duration");
        this.duration = durationParser.apply(durationString);
        this.chapters = new ChapterList(description, duration);
        
        this.definition = getData("contentDetails", "definition");
        this.language = getData("snippet", "defaultLanguage");
        this.audioLanguage = getData("snippet", "defaultAudioLanguage");
        
        this.location = new Location(getData("recordingDetails"));
        this.broadcastType = Optional.ofNullable((String) getData("snippet", "liveBroadcastContent"))
                .orElseGet(() -> Optional.ofNullable(thumbnails).map(EntityDetailSet::getAll).stream().flatMap(Collection::stream)
                                         .map(ThumbnailSet.Thumbnail::getUrl).anyMatch(e -> e.contains("_live.")) ? "live" : "none");
    }
    
    /**
     * Creates a Video Info.
     *
     * @param videoId The id of the Video.
     * @param title   The title of the Video.
     * @param date    The date the Video was uploaded.
     */
    @SuppressWarnings("unchecked")
    public VideoInfo(String videoId, String title, String date) {
        this(MapUtility.mapOf(
                new ImmutablePair<>("kind", "youtube#video"),
                new ImmutablePair<>("id", videoId),
                new ImmutablePair<>("snippet", MapUtility.mapOf(
                        new ImmutablePair<>("title", title),
                        new ImmutablePair<>("publishedAt", date)))));
    }
    
    /**
     * Creates an empty Video Info.
     */
    public VideoInfo() {
        super();
    }
    
    
    //Methods
    
    /**
     * Returns whether the Entity is private.
     *
     * @return Whether the Entity is private.
     */
    @Override
    public boolean isPrivate() {
        return super.isPrivate() || StringUtility.containsAnyIgnoreCase(getRawTitle(), PRIVATE_TITLES);
    }
    
    /**
     * Returns whether the Video is deleted.
     *
     * @return Whether the Video is deleted.
     */
    public boolean isDeleted() {
        return StringUtility.containsAnyIgnoreCase(getRawTitle(), DELETED_TITLES);
    }
    
    /**
     * Returns whether the Video is a live stream.
     *
     * @return Whether the Video is a live stream.
     */
    public boolean isLiveStream() {
        return StringUtility.containsAnyIgnoreCase(getBroadcastType(), LIVE_STREAM_BROADCAST_TYPES);
    }
    
    /**
     * Returns whether the Video is valid for processing.
     *
     * @return Whether the Video is valid for processing.
     */
    public boolean isValid() {
        return !isPrivate() && !isDeleted() && !isLiveStream();
    }
    
    
    //Getters
    
    /**
     * Returns the id of the Video.
     *
     * @return The id of the Video.
     */
    public String getVideoId() {
        return videoId;
    }
    
    /**
     * Returns the index position of the Video in its Youtube playlist.
     *
     * @return The index position of the Video in its Youtube playlist.
     */
    public Long getPlaylistPosition() {
        return playlistPosition;
    }
    
    /**
     * Returns the string representing the duration of the Video.
     *
     * @return The string representing the duration of the Video.
     */
    public String getDurationString() {
        return durationString;
    }
    
    /**
     * Returns the duration of the Video, in seconds.
     *
     * @return The duration of the Video, in seconds.
     */
    public Long getDuration() {
        return duration;
    }
    
    /**
     * Returns the Chapter List of the Video.
     *
     * @return The Chapter List of the Video.
     */
    public ChapterList getChapters() {
        return chapters;
    }
    
    /**
     * Returns the quality definition of the Video.
     *
     * @return The quality definition of the Video.
     */
    public String getDefinition() {
        return definition;
    }
    
    /**
     * Returns the language of the Video.
     *
     * @return The language of the Video.
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * Returns the audio language of the Video.
     *
     * @return The audio language of the Video.
     */
    public String getAudioLanguage() {
        return audioLanguage;
    }
    
    /**
     * Returns the Location of the Video.
     *
     * @return The Location of the Video.
     */
    public Location getLocation() {
        return location;
    }
    
    /**
     * Returns the broadcast type of the Video.
     *
     * @return The broadcast type of the Video.
     */
    public String getBroadcastType() {
        return broadcastType;
    }
    
}
