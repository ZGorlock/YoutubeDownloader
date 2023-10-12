/*
 * File:    Report.java
 * Package: youtube.state
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.state;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import commons.lambda.stream.mapper.Mappers;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.config.ChannelConfig;
import youtube.config.Color;
import youtube.entity.Channel;
import youtube.entity.Video;
import youtube.entity.base.Entity;
import youtube.entity.info.base.EntityInfo;
import youtube.util.LogUtils;

/**
 * Keeps track of recent downloads for the Youtube Channel Downloader.
 */
public final class Report {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Report.class);
    
    
    //Static Fields
    
    /**
     * A list of the Videos that were downloaded this run.
     */
    public static final Set<Video> downloads = new LinkedHashSet<>();
    
    
    //Static Methods
    
    /**
     * Includes a downloaded Video in the download report.
     *
     * @param video The downloaded Video.
     * @return Whether the Video was included in the download report.
     */
    public static boolean include(Video video) {
        return downloads.add(video);
    }
    
    /**
     * Returns a list of the Videos in the download report.
     *
     * @return The list of Videos in the download report.
     */
    public static List<Video> list() {
        return ListUtility.toList(downloads);
    }
    
    /**
     * Returns a list of the Videos from a specific Channel in the download report.
     *
     * @param channel The Channel.
     * @return The list of Videos from the specified Channel in the download report.
     */
    public static List<Video> list(Channel channel) {
        return list().stream()
                .filter(download -> download.getParent().getConfig().getChannelId().equals(channel.getConfig().getChannelId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Returns a list of the Videos from a specific location in the download report.
     *
     * @param location The location.
     * @return The list of Videos from the specified location in the download report.
     */
    public static List<Video> list(File location) {
        return list().stream()
                .filter(download -> StringUtility.fixFileSeparators(download.getOutput().getAbsolutePath()).contains(
                        StringUtility.fixFileSeparators(location.getAbsolutePath() + "/")))
                .collect(Collectors.toList());
    }
    
    /**
     * Prints a download report about the completed run.
     */
    public static void print() {
        if (!LogUtils.Config.printReport) {
            return;
        }
        
        final DecimalFormat integerFormat = new DecimalFormat("#,##0");
        final AtomicInteger maxSizeLength = new AtomicInteger(0);
        final AtomicInteger maxChannelLength = new AtomicInteger(0);
        final AtomicInteger maxPathLength = new AtomicInteger(0);
        final AtomicInteger maxUrlLength = new AtomicInteger(0);
        
        final Consumer<Video> downloadPrinter = (Video video) ->
                logger.debug(Optional.of(video)
                        .map(download -> String.join(Color.log("  "),
                                Optional.of(download).map(Entity::getParent).map(Channel::getConfig).map(ChannelConfig::getDisplayName)
                                        .map(e -> String.join("",
                                                Color.channel(e),
                                                Color.log(StringUtility.spaces(maxChannelLength.get() - e.length()))))
                                        .orElseGet(() -> Color.log(StringUtility.spaces(maxChannelLength.get()))),
                                Optional.of(download).map(Video::getOutput)
                                        .map(File::length).map(e -> (e / 1024L)).map(integerFormat::format)
                                        .map(e -> String.join("",
                                                Color.log(StringUtility.spaces(maxSizeLength.get() - e.length())),
                                                Color.number(e),
                                                Color.base(" KB")))
                                        .orElseGet(() -> Color.log(StringUtility.spaces(maxSizeLength.get() + " KB".length()))),
                                Optional.of(download).map(Video::getOutput).map(File::getAbsolutePath)
                                        .map(e -> String.join("",
                                                Color.file(e),
                                                Color.log(StringUtility.spaces(maxPathLength.get() - e.length()))))
                                        .orElseGet(() -> Color.log(StringUtility.spaces(maxPathLength.get()))),
                                Optional.of(download).map(Entity::getInfo).map(EntityInfo::getUrl)
                                        .map(e -> String.join("",
                                                Color.link(e),
                                                Color.log(StringUtility.spaces(maxUrlLength.get() - e.length()))))
                                        .orElseGet(() -> Color.log(StringUtility.spaces(maxUrlLength.get())))
                        )).orElse(""));
        
        logger.trace(LogUtils.NEWLINE);
        logger.debug(Color.number("--- Report ---"));
        
        list().stream()
                .filter(Objects::nonNull).filter(download -> download.getOutput().exists())
                .sorted(Comparator.comparing(download -> download.getOutput().getAbsolutePath()))
                .filter(download -> Optional.of(download)
                        .map(Mappers.tryForEach(e -> maxSizeLength.accumulateAndGet(integerFormat.format(download.getOutput().length() / 1024L).length(), Math::max)))
                        .map(Mappers.tryForEach(e -> maxChannelLength.accumulateAndGet(download.getParent().getConfig().getDisplayName().length(), Math::max)))
                        .map(Mappers.tryForEach(e -> maxPathLength.accumulateAndGet(download.getOutput().getAbsolutePath().length(), Math::max)))
                        .map(Mappers.tryForEach(e -> maxUrlLength.accumulateAndGet(download.getInfo().getUrl().length(), Math::max)))
                        .isPresent())
                .collect(Collectors.toList())
                .forEach(downloadPrinter);
    }
    
}
