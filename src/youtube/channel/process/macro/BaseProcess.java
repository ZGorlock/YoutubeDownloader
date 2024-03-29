/*
 * File:    BaseProcess.java
 * Package: youtube.channel.process.macro
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.process.macro;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.Channel;
import youtube.entity.Video;

/**
 * Provides base Channel Process macros.
 */
public class BaseProcess {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(BaseProcess.class);
    
    
    //Constants
    
    /**
     * The flag indicating that the case of search strings should be ignored when processing.
     */
    public static final boolean IGNORE_CASE = true;
    
    /**
     * The flag indicating that search strings should be evaluated as regex when processing.
     */
    public static final boolean REGEX = true;
    
    /**
     * The flag indicating that pattern matching should be strict when processing.
     */
    public static final boolean STRICT = true;
    
    /**
     * The flag indicating that the condition should be negated when processing.
     */
    public static final boolean NEGATE = true;
    
    
    //Static Methods
    
    /**
     * Performs an action for each entry in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param action   The action to perform.
     */
    public static void forEach(Channel channel, Map<String, Video> videoMap, BiConsumer<String, Video> action) {
        videoMap.forEach(action);
    }
    
    /**
     * Renames entries in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     * @param function The function to rename with.
     */
    public static void rename(Channel channel, Map<String, Video> videoMap, BiFunction<String, Video, String> function) {
        forEach(channel, videoMap, (id, video) ->
                video.updateTitle(function.apply(id, video)));
    }
    
    /**
     * Filters entries in the video map.
     *
     * @param channel   The Channel.
     * @param videoMap  The Video map.
     * @param condition The condition to filter by.
     */
    public static void filter(Channel channel, Map<String, Video> videoMap, Predicate<Video> condition) {
        forEach(channel, videoMap, (id, video) ->
                Optional.ofNullable(channel.getState().getBlocked())
                        .filter(e -> condition.test(video))
                        .ifPresent(e -> e.add(id)));
    }
    
}
