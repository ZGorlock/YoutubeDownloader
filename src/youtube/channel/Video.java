/*
 * File:    Video.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.channel;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import youtube.util.YoutubeUtils;

/**
 * Defines a Video.
 */
public class Video {
    
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
        this.title = YoutubeUtils.cleanTitle(title);
        this.url = YoutubeUtils.VIDEO_BASE + videoId;
        try {
            this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date
                    .replace("T", " ").replace("Z", ""));
        } catch (ParseException ignored) {
            this.date = new Date();
        }
        this.download = new File(outputDir, this.title);
        this.output = new File(outputDir, (this.title + '.' + (saveAsMp3 ? "mp3" : "mp4")));
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
        this(videoId, title, date, channel.outputFolder, channel.saveAsMp3);
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
        this.title = YoutubeUtils.cleanTitle(title);
        this.download = new File(this.channel.outputFolder, this.title);
        this.output = new File(this.channel.outputFolder, (this.title + '.' + YoutubeUtils.getFormat(output.getName())));
    }
    
    /**
     * Updates the output of the Video.
     *
     * @param output The output.
     */
    public void updateOutput(File output) {
        updateTitle(output.getName().replaceAll("\\.[^.]+$", ""));
    }
    
}
