/*
 * File:    Stats.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.state;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.conf.Configurator;
import youtube.util.Color;
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
    
    
    //Functions
    
    /**
     * Calculates the total data saved from Youtube.
     */
    private static void calculateData() {
        Channels.getChannels().stream()
                .flatMap(channel -> channel.state.saved.stream().map(saved -> channel.state.keyStore.get(saved)))
                .filter(Objects::nonNull).distinct()
                .map(File::new).filter(File::exists)
                .forEach(file -> {
                    if (Utils.VIDEO_FORMATS.contains(Utils.getFileFormat(file.getName()))) {
                        Stats.totalVideo++;
                        Stats.totalVideoData += file.length();
                    } else if (Utils.AUDIO_FORMATS.contains(Utils.getFileFormat(file.getName()))) {
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
        
        DecimalFormat integerFormat = new DecimalFormat("#,##0");
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        double bytesInMb = 1048576.0;
        BiConsumer<String, Object> printer = (String title, Object value) -> {
            switch (Optional.ofNullable(value).map(e -> e.getClass().getSimpleName()).orElse("String")) {
                case "Integer":
                case "int":
                case "Long":
                case "long":
                    System.out.println(Utils.INDENT +
                            Color.base(title) + Color.number(integerFormat.format(value)));
                    break;
                case "Double":
                case "double":
                    System.out.println(Utils.INDENT +
                            Color.base(title) + Color.number(decimalFormat.format((double) value / bytesInMb)) + Color.base("MB"));
                    break;
                case "String":
                    System.out.println(Color.link(Utils.formatUnderscoredString(title)));
                    break;
            }
        };
        
        calculateData();
        
        System.out.println(Utils.NEWLINE);
        System.out.println(Utils.NEWLINE);
        System.out.println(Color.number("--- Stats ---"));
        
        printer.accept("CHANNEL:", null);
        printer.accept("Channels Processed: ... ", totalChannelsProcessed);
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
        
        System.out.println(Utils.NEWLINE);
    }
    
}
