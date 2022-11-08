/*
 * File:    Video.java
 * Package: youtube.channel.entity
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.entity;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Channel;
import youtube.util.Utils;
import youtube.util.WebUtils;

/**
 * Defines a Video.
 */
public class Video {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Video.class);
    
    
    //Fields
    
    /**
     * The Channel containing the Video.
     */
    public Channel channel;
    
    /**
     * The ID of the Video.
     */
    public String videoId;
    
    /**
     * The original title of the Video.
     */
    public String originalTitle;
    
    /**
     * The title of the Video.
     */
    public String title;
    
    /**
     * The url of the Video.
     */
    public String url;
    
    /**
     * The date the Video was uploaded.
     */
    public Date date;
    
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
     * @param videoId   The ID of the Video.
     * @param title     The title of the Video.
     * @param date      The date the Video was uploaded.
     * @param outputDir The output directory for the Video.
     * @param saveAsMp3 Whether the Video is an mp3 or not.
     */
    public Video(String videoId, String title, String date, File outputDir, boolean saveAsMp3) {
        this.channel = null;
        this.videoId = videoId;
        this.originalTitle = title;
        this.title = Utils.cleanVideoTitle(title);
        this.url = WebUtils.VIDEO_BASE + videoId;
        try {
            this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date
                    .replace("T", " ").replace("Z", ""));
        } catch (ParseException ignored) {
            this.date = new Date();
        }
        this.download = new File(outputDir, this.title);
        this.output = new File(outputDir, (this.title + '.' + (saveAsMp3 ? Utils.AUDIO_FORMAT : Utils.VIDEO_FORMAT)));
    }
    
    /**
     * Creates a Video.
     *
     * @param videoId The ID of the Video.
     * @param title   The title of the Video.
     * @param date    The date the Video was uploaded.
     * @param channel The Channel containing the Video.
     */
    public Video(String videoId, String title, String date, Channel channel) {
        this(videoId, title, date, channel.getOutputFolder(), channel.isSaveAsMp3());
        this.channel = channel;
    }
    
    /**
     * The default no-argument constructor for a Video.
     */
    public Video() {
    }
    
    
    //Methods
    
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
    
    /**
     * Returns the string representation of the Video.
     *
     * @return the string representation of the Video.
     */
    @Override
    public String toString() {
        return title;
    }
    
}
