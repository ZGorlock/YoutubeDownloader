/*
 * File:    Video.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.channel;

import java.io.File;
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
     * @param videoId The ID of the Video.
     * @param title   The title of the Video.
     * @param date    The date the Video was uploaded.
     * @param channel The Channel containing the Video.
     * @throws Exception When there is an error parsing the upload date.
     */
    public Video(String videoId, String title, String date, Channel channel) throws Exception {
        this.channel = channel;
        this.videoId = videoId;
        this.originalTitle = title;
        this.title = YoutubeUtils.cleanTitle(title);
        this.url = YoutubeUtils.VIDEO_BASE + videoId;
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date.replace("T", " ").replace("Z", ""));
        this.download = new File(this.channel.outputFolder, this.title);
        this.output = new File(this.channel.outputFolder, (this.title + '.' + (channel.saveAsMp3 ? "mp3" : "mp4")));
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
        this.title = title;
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
