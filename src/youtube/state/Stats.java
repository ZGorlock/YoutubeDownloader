/*
 * File:    Stats.java
 * Package: youtube.state
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.state;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.lambda.stream.collector.MapCollectors;
import commons.lambda.stream.mapper.Mappers;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channels;
import youtube.config.Color;
import youtube.util.BackupUtils;
import youtube.util.FileUtils;
import youtube.util.LogUtils;

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
     * A counter of the total number of Channels.
     */
    public static final AtomicLong totalChannels = new AtomicLong(0L);
    
    /**
     * A counter of the total number of Channels that were filtered this run.
     */
    public static final AtomicLong totalFilteredChannels = new AtomicLong(0L);
    
    /**
     * A counter of the total number of Channels that were processed this run.
     */
    public static final AtomicLong totalChannelsProcessed = new AtomicLong(0L);
    
    /**
     * A counter of the total number of video files that were downloaded this run.
     */
    public static final AtomicLong totalVideoDownloads = new AtomicLong(0L);
    
    /**
     * A counter of the total number of audio files that were downloaded this run.
     */
    public static final AtomicLong totalAudioDownloads = new AtomicLong(0L);
    
    /**
     * A counter of the total number of video files that were renamed this run.
     */
    public static final AtomicLong totalVideoRenames = new AtomicLong(0L);
    
    /**
     * A counter of the total number of audio files that were renamed this run.
     */
    public static final AtomicLong totalAudioRenames = new AtomicLong(0L);
    
    /**
     * A counter of the total number of video files that were deleted this run.
     */
    public static final AtomicLong totalVideoDeletions = new AtomicLong(0L);
    
    /**
     * A counter of the total number of audio files that were deleted this run.
     */
    public static final AtomicLong totalAudioDeletions = new AtomicLong(0L);
    
    /**
     * A counter of the total number of video files that failed to download this run.
     */
    public static final AtomicLong totalVideoDownloadFailures = new AtomicLong(0L);
    
    /**
     * A counter of the total number of audio files that failed to download this run.
     */
    public static final AtomicLong totalAudioDownloadFailures = new AtomicLong(0L);
    
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
    public static final AtomicLong totalApiCalls = new AtomicLong(0L);
    
    /**
     * A counter of the total number of times the Youtube Data API was called to fetch an Entity this run.
     */
    public static final AtomicLong totalApiEntityCalls = new AtomicLong(0L);
    
    /**
     * A counter of the total number of times the Youtube Data API was called to fetch data this run.
     */
    public static final AtomicLong totalApiDataCalls = new AtomicLong(0L);
    
    /**
     * A counter of the total number of times calling the Youtube Data API failed this run.
     */
    public static final AtomicLong totalApiFailures = new AtomicLong(0L);
    
    /**
     * A counter of the total number of video files saved from Youtube.
     */
    public static final AtomicLong totalVideo = new AtomicLong(0L);
    
    /**
     * A counter of the total number of audio files saved from Youtube.
     */
    public static final AtomicLong totalAudio = new AtomicLong(0L);
    
    /**
     * A counter of the total video data saved from Youtube, in bytes.
     */
    public static final AtomicLong totalVideoData = new AtomicLong(0L);
    
    /**
     * A counter of the total audio data saved from Youtube, in bytes.
     */
    public static final AtomicLong totalAudioData = new AtomicLong(0L);
    
    /**
     * A counter of the total number of video files saved from Youtube, considering only the Channels that were processed this run.
     */
    public static final AtomicLong totalFilteredVideo = new AtomicLong(0L);
    
    /**
     * A counter of the total number of audio files saved from Youtube, considering only the Channels that were processed this run.
     */
    public static final AtomicLong totalFilteredAudio = new AtomicLong(0L);
    
    /**
     * A counter of the total video data saved from Youtube, in bytes, considering only the Channels that were processed this run.
     */
    public static final AtomicLong totalFilteredVideoData = new AtomicLong(0L);
    
    /**
     * A counter of the total audio data saved from Youtube, in bytes, considering only the Channels that were processed this run.
     */
    public static final AtomicLong totalFilteredAudioData = new AtomicLong(0L);
    
    /**
     * A counter of the total number of Channel caches saved from the Youtube Data API.
     */
    public static final AtomicLong totalChannelCaches = new AtomicLong(0L);
    
    /**
     * A counter of the total data size of Channel caches saved from the Youtube Data API.
     */
    public static final AtomicLong totalChannelCacheData = new AtomicLong(0L);
    
    /**
     * A counter of the total number of logs present in the log directory.
     */
    public static final AtomicLong totalLogs = new AtomicLong(0L);
    
    /**
     * A counter of the total log data present in the log directory.
     */
    public static final AtomicLong totalLogData = new AtomicLong(0L);
    
    /**
     * A counter of the total number of backups present in the backup directory.
     */
    public static final AtomicLong totalBackups = new AtomicLong(0L);
    
    /**
     * A counter of the total backup data present in the backup directory.
     */
    public static final AtomicLong totalBackupData = new AtomicLong(0L);
    
    
    //Static Methods
    
    /**
     * Calculates the total data saved from Youtube.
     */
    private static void calculateData() {
        logger.debug(Color.log("Calculating Stats..."));
        
        final Map<String, Long> fileData = KeyStore.getAllEntries().stream()
                .filter(Objects::nonNull).filter(KeyStore.KeyStoreEntry::isValid)
                .collect(MapCollectors.toHashMap(
                        KeyStore.KeyStoreEntry::getLocalPath,
                        entry -> Optional.of(entry)
                                .map(KeyStore.KeyStoreEntry::getLocalFile).filter(File::exists)
                                .map(File::length).orElse(0L)));
        
        List.of(totalChannels, totalFilteredChannels,
                totalVideo, totalFilteredVideo, totalVideoData, totalFilteredVideoData,
                totalAudio, totalFilteredAudio, totalAudioData, totalFilteredAudioData,
                totalChannelCaches, totalChannelCacheData,
                totalLogs, totalLogData,
                totalBackups, totalBackupData
        ).forEach(stat -> stat.set(0L));
        
        Stream.of(false, true).forEach(filtered ->
                Channels.getChannels().stream()
                        .filter(channel -> (!filtered || Channels.isFiltered(channel.getConfig().getKey())))
                        .map(Mappers.forEach(channel ->
                                (filtered ? totalFilteredChannels : totalChannels).incrementAndGet()))
                        .flatMap(channel -> channel.getState().getSaved().stream()
                                .map(saved -> channel.getState().getKeyStore().get(saved))
                                .filter(Objects::nonNull))
                        .map(KeyStore.KeyStoreEntry::getLocalPath).distinct()
                        .filter(fileData::containsKey)
                        .forEach(filePath -> {
                            if (FileUtils.isVideoFormat(filePath)) {
                                (filtered ? totalFilteredVideo : totalVideo).incrementAndGet();
                                (filtered ? totalFilteredVideoData : totalVideoData).addAndGet(fileData.get(filePath));
                            } else if (FileUtils.isAudioFormat(filePath)) {
                                (filtered ? totalFilteredAudio : totalAudio).incrementAndGet();
                                (filtered ? totalFilteredAudioData : totalAudioData).addAndGet(fileData.get(filePath));
                            }
                        }));
        
        Channels.fetchAllChannelCaches()
                .forEach(cache -> {
                    totalChannelCaches.incrementAndGet();
                    totalChannelCacheData.addAndGet(Filesystem.sizeOf(cache));
                });
        
        LogUtils.fetchAllLogs()
                .forEach(log -> {
                    totalLogs.incrementAndGet();
                    totalLogData.addAndGet(log.length());
                });
        
        BackupUtils.fetchAllBackups()
                .forEach(file -> {
                    totalBackups.incrementAndGet();
                    totalBackupData.addAndGet(Filesystem.sizeOf(file));
                });
    }
    
    /**
     * Prints statistics about the completed run.
     */
    public static void print() {
        if (!LogUtils.Config.printStats) {
            return;
        }
        
        final DecimalFormat integerFormat = new DecimalFormat("#,##0");
        final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        final AtomicInteger maxKeyLength = new AtomicInteger(24);
        final AtomicInteger maxDataLength = new AtomicInteger(0);
        
        final BiConsumer<String, Object> statPrinter = (String title, Object value) ->
                logger.debug(Optional.ofNullable(value)
                        .map(String::valueOf)
                        .map(stringValue -> !stringValue.matches("^[\\d.E]+$") ? stringValue :
                                            !stringValue.contains(".") ? integerFormat.format(value) :
                                            decimalFormat.format((double) value / 1048576))
                        .map(stringValue -> StringUtility.padLeft(stringValue,
                                maxDataLength.accumulateAndGet(stringValue.length(), (x, y) -> stringValue.contains(".") ? Math.max(x, y) : 0)))
                        .map(stringValue -> String.join("",
                                LogUtils.INDENT_HARD.repeat(StringUtility.numberOfOccurrences(title, "\t")),
                                Color.base(title.replace("\t", "")), Color.log(": "),
                                Color.log(".".repeat(maxKeyLength.get() - title.replace("\t", " ".repeat(LogUtils.INDENT_WIDTH)).length() + 2) + ' '),
                                Color.number(stringValue), (stringValue.contains(".") ? Color.base(" MB") : "")))
                        .orElseGet(() -> String.join("",
                                LogUtils.INDENT_HARD.repeat(StringUtility.numberOfOccurrences(title, "\t")),
                                Color.link(title.replace("\t", "")) + Color.log(": "))));
        
        calculateData();
        
        logger.trace(LogUtils.NEWLINE);
        logger.debug(Color.number("--- Stats ---"));
        
        statPrinter.accept("Channel", null);
        statPrinter.accept("\tProcessed", totalChannelsProcessed.get());
        if (Channels.isFilterActive()) {
            statPrinter.accept("\tFiltered", totalFilteredChannels.get());
        }
        statPrinter.accept("\tTotal", totalChannels.get());
        
        statPrinter.accept("Run", null);
        statPrinter.accept("\tDownloaded", (totalVideoDownloads.get() + totalAudioDownloads.get()));
        statPrinter.accept("\t\tVideo", totalVideoDownloads.get());
        statPrinter.accept("\t\tAudio", totalAudioDownloads.get());
        statPrinter.accept("\tFailed", (totalVideoDownloadFailures.get() + totalAudioDownloadFailures.get()));
        statPrinter.accept("\t\tVideo", totalVideoDownloadFailures.get());
        statPrinter.accept("\t\tAudio", totalAudioDownloadFailures.get());
        statPrinter.accept("\tRenamed", (totalVideoRenames.get() + totalAudioRenames.get()));
        statPrinter.accept("\t\tVideo", totalVideoRenames.get());
        statPrinter.accept("\t\tAudio", totalAudioRenames.get());
        statPrinter.accept("\tDeleted", (totalVideoDeletions.get() + totalAudioDeletions.get()));
        statPrinter.accept("\t\tVideo", totalVideoDeletions.get());
        statPrinter.accept("\t\tAudio", totalAudioDeletions.get());
        statPrinter.accept("\tData", (double) (totalVideoDataDownloaded.get() + totalAudioDataDownloaded.get()));
        statPrinter.accept("\t\tVideo", (double) totalVideoDataDownloaded.get());
        statPrinter.accept("\t\tAudio", (double) totalAudioDataDownloaded.get());
        
        statPrinter.accept("Api", null);
        statPrinter.accept("\tCalls", totalApiCalls.get());
        statPrinter.accept("\t\tEntity", totalApiEntityCalls.get());
        statPrinter.accept("\t\tData", totalApiDataCalls.get());
        statPrinter.accept("\tFailures", totalApiFailures.get());
        
        statPrinter.accept("Cache", null);
        statPrinter.accept("\tChannels", totalChannelCaches.get());
        statPrinter.accept("\t\tData", (double) totalChannelCacheData.get());
        if (LogUtils.Config.allowFileLogging) {
            statPrinter.accept("\tLogs", totalLogs.get());
            statPrinter.accept("\t\tData", (double) totalLogData.get());
        }
        if (BackupUtils.Config.enableBackups) {
            statPrinter.accept("\tBackups", totalBackups.get());
            statPrinter.accept("\t\tData", (double) totalBackupData.get());
        }
        
        if (Channels.isFilterActive()) {
            statPrinter.accept("Filter", null);
            statPrinter.accept("\tDownloads", (totalFilteredVideo.get() + totalFilteredAudio.get()));
            statPrinter.accept("\t\tVideo", totalFilteredVideo.get());
            statPrinter.accept("\t\tAudio", totalFilteredAudio.get());
            statPrinter.accept("\tData", (double) (totalFilteredVideoData.get() + totalFilteredAudioData.get()));
            statPrinter.accept("\t\tVideo", (double) totalFilteredVideoData.get());
            statPrinter.accept("\t\tAudio", (double) totalFilteredAudioData.get());
        }
        
        statPrinter.accept("Total", null);
        statPrinter.accept("\tDownloads", (totalVideo.get() + totalAudio.get()));
        statPrinter.accept("\t\tVideo", totalVideo.get());
        statPrinter.accept("\t\tAudio", totalAudio.get());
        statPrinter.accept("\tData", (double) (totalVideoData.get() + totalAudioData.get()));
        statPrinter.accept("\t\tVideo", (double) totalVideoData.get());
        statPrinter.accept("\t\tAudio", (double) totalAudioData.get());
    }
    
}
