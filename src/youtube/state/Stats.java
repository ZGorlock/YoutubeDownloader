/*
 * File:    Stats.java
 * Package: youtube.state
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.state;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.config.Color;
import youtube.config.Configurator;
import youtube.util.FileUtils;
import youtube.util.Utils;

/**
 * Keeps track of statistics for the Youtube Channel Downloader.
 */
public final class Stats {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Stats.class);
    
    
    //Static Fields
    
    /**
     * A counter of the total number of Channels that were processed this run.
     */
    public static final AtomicInteger totalChannelsProcessed = new AtomicInteger(0);
    
    /**
     * A counter of the total number of video files that were downloaded this run.
     */
    public static final AtomicInteger totalVideoDownloads = new AtomicInteger(0);
    
    /**
     * A counter of the total number of audio files that were downloaded this run.
     */
    public static final AtomicInteger totalAudioDownloads = new AtomicInteger(0);
    
    /**
     * A counter of the total number of video files that were renamed this run.
     */
    public static final AtomicInteger totalVideoRenames = new AtomicInteger(0);
    
    /**
     * A counter of the total number of audio files that were renamed this run.
     */
    public static final AtomicInteger totalAudioRenames = new AtomicInteger(0);
    
    /**
     * A counter of the total number of video files that were deleted this run.
     */
    public static final AtomicInteger totalVideoDeletions = new AtomicInteger(0);
    
    /**
     * A counter of the total number of audio files that were deleted this run.
     */
    public static final AtomicInteger totalAudioDeletions = new AtomicInteger(0);
    
    /**
     * A counter of the total number of video files that failed to download this run.
     */
    public static final AtomicInteger totalVideoDownloadFailures = new AtomicInteger(0);
    
    /**
     * A counter of the total number of audio files that failed to download this run.
     */
    public static final AtomicInteger totalAudioDownloadFailures = new AtomicInteger(0);
    
    /**
     * A counter of the total video data downloaded from Youtube this run, in bytes.
     */
    public static final AtomicLong totalVideoDataDownloaded = new AtomicLong(0L);
    
    /**
     * A counter of the total audio data downloaded from Youtube this run, in bytes.
     */
    public static final AtomicLong totalAudioDataDownloaded = new AtomicLong(0L);
    
    /**
     * A counter of the total number of times the Youtube Data API was called this run.
     */
    public static final AtomicInteger totalApiCalls = new AtomicInteger(0);
    
    /**
     * A counter of the total number of times the Youtube Data API was called to fetch an Entity this run.
     */
    public static final AtomicInteger totalApiEntityCalls = new AtomicInteger(0);
    
    /**
     * A counter of the total number of times the Youtube Data API was called to fetch data this run.
     */
    public static final AtomicInteger totalApiDataCalls = new AtomicInteger(0);
    
    /**
     * A counter of the total number of times calling the Youtube Data API failed this run.
     */
    public static final AtomicInteger totalApiFailures = new AtomicInteger(0);
    
    /**
     * A counter of the total number of video files saved from Youtube.
     */
    public static final AtomicInteger totalVideo = new AtomicInteger(0);
    
    /**
     * A counter of the total number of audio files saved from Youtube.
     */
    public static final AtomicInteger totalAudio = new AtomicInteger(0);
    
    /**
     * A counter of the total video data saved from Youtube, in bytes.
     */
    public static final AtomicLong totalVideoData = new AtomicLong(0L);
    
    /**
     * A counter of the total audio data saved from Youtube, in bytes.
     */
    public static final AtomicLong totalAudioData = new AtomicLong(0L);
    
    
    //Static Methods
    
    /**
     * Calculates the total data saved from Youtube.
     */
    private static void calculateData() {
        Channels.getChannels().stream()
                .flatMap(channel -> channel.getState().getSaved().stream()
                        .map(saved -> channel.getState().getKeyStore().get(saved)))
                .filter(Objects::nonNull).distinct()
                .map(File::new).filter(File::exists)
                .forEach(file -> {
                    if (Utils.VIDEO_FORMATS_OPTIONS.contains(FileUtils.getFileFormat(file.getName()).toLowerCase())) {
                        Stats.totalVideo.incrementAndGet();
                        Stats.totalVideoData.addAndGet(file.length());
                    } else if (Utils.AUDIO_FORMATS_OPTIONS.contains(FileUtils.getFileFormat(file.getName()).toLowerCase())) {
                        Stats.totalAudio.incrementAndGet();
                        Stats.totalAudioData.addAndGet(file.length());
                    }
                });
    }
    
    /**
     * Prints statistics about the completed run.
     */
    public static void print() {
        if (!Configurator.Config.printStats) {
            return;
        }
        
        final DecimalFormat integerFormat = new DecimalFormat("#,##0");
        final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        final AtomicInteger maxDataLength = new AtomicInteger(0);
        
        final BiConsumer<String, Object> statPrinter = (String title, Object value) ->
                System.out.println(Optional.ofNullable(value)
                        .map(e -> Optional.of(String.valueOf(e))
                                .filter(e2 -> e2.matches("^[\\d.E]+$"))
                                .map(e2 -> e2.contains(".") ? decimalFormat.format((double) e / 1048576) : integerFormat.format(e))
                                .orElse(String.valueOf(e)))
                        .map(e -> StringUtility.padLeft(e,
                                maxDataLength.accumulateAndGet(e.length(), (x, y) -> e.contains(".") ? Math.max(x, y) : 0)))
                        .map(e -> String.join("",
                                Utils.INDENT, Color.base(title), Color.number(e), (e.contains(".") ? Color.base(" MB") : "")))
                        .orElseGet(() -> Color.link(Utils.formatUnderscoredString(title))));
        
        calculateData();
        
        System.out.println(Utils.NEWLINE);
        System.out.println(Utils.NEWLINE);
        System.out.println(Color.number("--- Stats ---"));
        
        statPrinter.accept("CHANNEL:", null);
        statPrinter.accept("Processed: ............ ", totalChannelsProcessed.get());
        statPrinter.accept("Total: ................ ", Channels.getChannels().size());
        
        statPrinter.accept("RUN:", null);
        statPrinter.accept("Downloaded: ........... ", (totalVideoDownloads.get() + totalAudioDownloads.get()));
        statPrinter.accept("    Video: ............ ", totalVideoDownloads.get());
        statPrinter.accept("    Audio: ............ ", totalAudioDownloads.get());
        statPrinter.accept("Failed: ............... ", (totalVideoDownloadFailures.get() + totalAudioDownloadFailures.get()));
        statPrinter.accept("    Video: ............ ", totalVideoDownloadFailures.get());
        statPrinter.accept("    Audio: ............ ", totalAudioDownloadFailures.get());
        statPrinter.accept("Renamed: .............. ", (totalVideoRenames.get() + totalAudioRenames.get()));
        statPrinter.accept("    Video: ............ ", totalVideoRenames.get());
        statPrinter.accept("    Audio: ............ ", totalAudioRenames.get());
        statPrinter.accept("Deleted: .............. ", (totalVideoDeletions.get() + totalAudioDeletions.get()));
        statPrinter.accept("    Video: ............ ", totalVideoDeletions.get());
        statPrinter.accept("    Audio: ............ ", totalAudioDeletions.get());
        statPrinter.accept("Data: ................. ", (double) (totalVideoDataDownloaded.get() + totalAudioDataDownloaded.get()));
        statPrinter.accept("    Video: ............ ", (double) totalVideoDataDownloaded.get());
        statPrinter.accept("    Audio: ............ ", (double) totalAudioDataDownloaded.get());
        
        statPrinter.accept("API:", null);
        statPrinter.accept("Calls: ................ ", totalApiCalls.get());
        statPrinter.accept("    Entity: ........... ", totalApiEntityCalls.get());
        statPrinter.accept("    Data: ............. ", totalApiDataCalls.get());
        statPrinter.accept("Failures: ............. ", totalApiFailures.get());
        
        statPrinter.accept("TOTAL:", null);
        statPrinter.accept("Downloads: ............ ", (totalVideo.get() + totalAudio.get()));
        statPrinter.accept("    Video: ............ ", totalVideo.get());
        statPrinter.accept("    Audio: ............ ", totalAudio.get());
        statPrinter.accept("Data: ................. ", (double) (totalVideoData.get() + totalAudioData.get()));
        statPrinter.accept("    Video: ............ ", (double) totalVideoData.get());
        statPrinter.accept("    Audio: ............ ", (double) totalAudioData.get());
        
        System.out.println(Utils.NEWLINE);
    }
    
}
