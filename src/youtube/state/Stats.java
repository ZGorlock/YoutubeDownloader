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
import java.util.function.BiConsumer;

import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.conf.Color;
import youtube.conf.Configurator;
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
    public static int totalChannelsProcessed = 0;
    
    /**
     * A counter of the total number of video files that were downloaded this run.
     */
    public static int totalVideoDownloads = 0;
    
    /**
     * A counter of the total number of audio files that were downloaded this run.
     */
    public static int totalAudioDownloads = 0;
    
    /**
     * A counter of the total number of video files that were renamed this run.
     */
    public static int totalVideoRenames = 0;
    
    /**
     * A counter of the total number of audio files that were renamed this run.
     */
    public static int totalAudioRenames = 0;
    
    /**
     * A counter of the total number of video files that were deleted this run.
     */
    public static int totalVideoDeletions = 0;
    
    /**
     * A counter of the total number of audio files that were deleted this run.
     */
    public static int totalAudioDeletions = 0;
    
    /**
     * A counter of the total number of video files that failed to download this run.
     */
    public static int totalVideoDownloadFailures = 0;
    
    /**
     * A counter of the total number of audio files that failed to download this run.
     */
    public static int totalAudioDownloadFailures = 0;
    
    /**
     * A counter of the total video data downloaded from Youtube this run, in bytes.
     */
    public static long totalVideoDataDownloaded = 0L;
    
    /**
     * A counter of the total audio data downloaded from Youtube this run, in bytes.
     */
    public static long totalAudioDataDownloaded = 0L;
    
    /**
     * A counter of the total number of times the Youtube Data API was called this run.
     */
    public static int totalApiCalls = 0;
    
    /**
     * A counter of the total number of times the Youtube Data API was called to fetch an Entity this run.
     */
    public static int totalApiEntityCalls = 0;
    
    /**
     * A counter of the total number of times the Youtube Data API was called to fetch data this run.
     */
    public static int totalApiDataCalls = 0;
    
    /**
     * A counter of the total number of times calling the Youtube Data API failed this run.
     */
    public static int totalApiFailures = 0;
    
    /**
     * A counter of the total number of video files saved from Youtube.
     */
    public static int totalVideo = 0;
    
    /**
     * A counter of the total number of audio files saved from Youtube.
     */
    public static int totalAudio = 0;
    
    /**
     * A counter of the total video data saved from Youtube, in bytes.
     */
    public static long totalVideoData = 0L;
    
    /**
     * A counter of the total audio data saved from Youtube, in bytes.
     */
    public static long totalAudioData = 0L;
    
    
    //Static Methods
    
    /**
     * Calculates the total data saved from Youtube.
     */
    private static void calculateData() {
        Channels.getChannels().stream()
                .flatMap(channel -> channel.state.saved.stream().map(saved -> channel.state.keyStore.get(saved)))
                .filter(Objects::nonNull).distinct()
                .map(File::new).filter(File::exists)
                .forEach(file -> {
                    if (Utils.VIDEO_FORMATS_OPTIONS.contains(Utils.getFileFormat(file.getName()))) {
                        Stats.totalVideo++;
                        Stats.totalVideoData += file.length();
                    } else if (Utils.AUDIO_FORMATS_OPTIONS.contains(Utils.getFileFormat(file.getName()))) {
                        Stats.totalAudio++;
                        Stats.totalAudioData += file.length();
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
        statPrinter.accept("Processed: ............ ", totalChannelsProcessed);
        statPrinter.accept("Total: ................ ", Channels.getChannels().size());
        
        statPrinter.accept("RUN:", null);
        statPrinter.accept("Downloaded: ........... ", (totalVideoDownloads + totalAudioDownloads));
        statPrinter.accept("    Video: ............ ", totalVideoDownloads);
        statPrinter.accept("    Audio: ............ ", totalAudioDownloads);
        statPrinter.accept("Failed: ............... ", (totalVideoDownloadFailures + totalAudioDownloadFailures));
        statPrinter.accept("    Video: ............ ", totalVideoDownloadFailures);
        statPrinter.accept("    Audio: ............ ", totalAudioDownloadFailures);
        statPrinter.accept("Renamed: .............. ", (totalVideoRenames + totalAudioRenames));
        statPrinter.accept("    Video: ............ ", totalVideoRenames);
        statPrinter.accept("    Audio: ............ ", totalAudioRenames);
        statPrinter.accept("Deleted: .............. ", (totalVideoDeletions + totalAudioDeletions));
        statPrinter.accept("    Video: ............ ", totalVideoDeletions);
        statPrinter.accept("    Audio: ............ ", totalAudioDeletions);
        statPrinter.accept("Data: ................. ", (double) (totalVideoDataDownloaded + totalAudioDataDownloaded));
        statPrinter.accept("    Video: ............ ", (double) totalVideoDataDownloaded);
        statPrinter.accept("    Audio: ............ ", (double) totalAudioDataDownloaded);
        
        statPrinter.accept("API:", null);
        statPrinter.accept("Calls: ................ ", totalApiCalls);
        statPrinter.accept("    Entity: ........... ", totalApiEntityCalls);
        statPrinter.accept("    Data: ............. ", totalApiDataCalls);
        statPrinter.accept("Failures: ............. ", totalApiFailures);
        
        statPrinter.accept("TOTAL:", null);
        statPrinter.accept("Downloads: ............ ", (totalVideo + totalAudio));
        statPrinter.accept("    Video: ............ ", totalVideo);
        statPrinter.accept("    Audio: ............ ", totalAudio);
        statPrinter.accept("Data: ................. ", (double) (totalVideoData + totalAudioData));
        statPrinter.accept("    Video: ............ ", (double) totalVideoData);
        statPrinter.accept("    Audio: ............ ", (double) totalAudioData);
        
        System.out.println(Utils.NEWLINE);
    }
    
}
