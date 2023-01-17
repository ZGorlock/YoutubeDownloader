/*
 * File:    Video.java
 * Package: youtube.entity
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity;

import java.io.File;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.ChannelEntry;
import youtube.config.Configurator;
import youtube.entity.base.Entity;
import youtube.entity.base.EntityType;
import youtube.entity.info.VideoInfo;
import youtube.util.FileUtils;
import youtube.util.PathUtils;
import youtube.util.Utils;

/**
 * Defines a Video.
 */
public class Video extends Entity<VideoInfo> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Video.class);
    
    
    //Fields
    
    /**
     * The title of the Video.
     */
    public String title;
    
    /**
     * The output directory of the Video.
     */
    public File outputDir;
    
    /**
     * The download file of the Video.
     */
    public File download;
    
    /**
     * The output file of the Video.
     */
    public File output;
    
    
    //Constructors
    
    /**
     * Creates a Video.
     *
     * @param videoInfo The Video Info associated with the Video.
     * @param parent    The parent Channel of the Video.
     */
    public Video(VideoInfo videoInfo, Channel parent) {
        super(EntityType.VIDEO, videoInfo, parent);
        
        updateTitle(null);
    }
    
    /**
     * Creates a Video.
     *
     * @param videoInfo The Video Info associated with the Video.
     */
    public Video(VideoInfo videoInfo) {
        this(videoInfo, null);
    }
    
    
    //Methods
    
    /**
     * Initializes the files of the Video.
     */
    private void initFiles() {
        download = new File(getOutputDir(), getTitle());
        output = new File(getOutputDir(), (getTitle() + '.' + getFormat()));
    }
    
    /**
     * Updates the title of the Video.
     *
     * @param videoTitle The title, or null to reset the title to the original.
     */
    public void updateTitle(String videoTitle) {
        title = Utils.cleanVideoTitle(
                Optional.ofNullable(videoTitle)
                        .orElseGet(() -> getInfo().getTitle()));
        initFiles();
    }
    
    /**
     * Resets the title of the Video.
     */
    public void resetTitle() {
        updateTitle(null);
    }
    
    /**
     * Updates the output folder of the Video.
     *
     * @param outputDir The output folder.
     */
    public void updateOutputDir(File outputDir) {
        download = new File(outputDir, getDownload().getName());
        output = new File(outputDir, getOutput().getName());
    }
    
    /**
     * Updates the output file of the Video.
     *
     * @param output The output file.
     */
    public void updateOutput(File output) {
        this.output = output;
        updateOutputDir(output.getParentFile());
        updateTitle(FileUtils.getFileTitle(output.getName()));
    }
    
    /**
     * Returns a string representation of the Video.
     *
     * @return a string representation of the Video.
     */
    @Override
    public String toString() {
        return getTitle();
    }
    
    
    //Getters
    
    /**
     * Returns the title of the Video.
     *
     * @return The title.
     */
    public String getTitle() {
        return Optional.ofNullable(title)
                .orElseGet(() -> getInfo().getTitle());
    }
    
    /**
     * Returns the output directory for the Video.
     *
     * @return The output directory.
     */
    public File getOutputDir() {
        return Optional.ofNullable(getOutput())
                .map(File::getParentFile)
                .orElseGet(() -> Optional.ofNullable(getConfig())
                        .map(ChannelEntry::getOutputFolder)
                        .orElse(PathUtils.TMP_DIR));
    }
    
    /**
     * Returns the output format for the Video.
     *
     * @return The output format.
     */
    public String getFormat() {
        return Optional.ofNullable(getOutput())
                .map(File::getName)
                .map(FileUtils::getFileFormat)
                .orElseGet(() -> Optional.ofNullable(getConfig())
                                         .map(ChannelEntry::isSaveAsMp3)
                                         .orElse(Configurator.Config.asMp3)
                                 ? Utils.DEFAULT_AUDIO_FORMAT : Utils.DEFAULT_VIDEO_FORMAT);
    }
    
    /**
     * Returns the download file of the Video.
     *
     * @return The download file.
     */
    public File getDownload() {
        return download;
    }
    
    /**
     * Returns the output file of the Video.
     *
     * @return The output file.
     */
    public File getOutput() {
        return output;
    }
    
}
