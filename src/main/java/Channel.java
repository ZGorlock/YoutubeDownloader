/*
 * File:    Channel.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

//To get the playlistId for a Youtube Playlist:
//1, Go to the Youtube Playlist
//2. Simply copy it from the url:
//  https://www.youtube.com/watch?v=3qiLI1ILMlU&list=PLdE7uo_7KBkfAWkk7-Clm18krBuziKQfr
//                                                  <PLdE7uo_7KBkfAWkk7-Clm18krBuziKQfr>
//
//To get the playlistId for a Youtube Channel:
//1. Go to the Youtube Channel
//2. View the Page Source
//3. Search for "externalId" and copy that value
//4. Replace the second character from a 'C' to a 'U'
public enum Channel {
    
    //Values
    
    TRAP_CITY(true, "TrapCity", "UU65afEgL62PGFWXY7n6CUbA", new File("E:/Music/Beats/Trap City"), true, new File("E:/Music/Beats/Trap.m3u")),
    SKY_BASS(true, "SkyBass", "UUpXbwekw4ySNHGt26aAKvHQ", new File("E:/Music/Beats/Sky Bass"), true, new File("E:/Music/Beats/Trap.m3u")),
    TRAP_NATION(true, "TrapNation", "UUa10nxShhzNrCE1o2ZOPztg", new File("E:/Music/Beats/Trap Nation"), true, new File("E:/Music/Beats/Trap.m3u")),
    BASS_NATION(true, "BassNation", "UUCvVpbYRgYjMN7mG7qQN0Pg", new File("E:/Music/Beats/Bass Nation"), true, new File("E:/Music/Beats/Trap.m3u")),
    
    THE_COMET_IS_COMING(false, "TheCometIsComing", "PLqffNt5cY34WycBZsqhVoXgRnehbbxyTB", new File("E:/Music/Music/The Comet Is Coming"), true, new File("E:/Music/Music/The Comet Is Coming.m3u")),
    
    OSRS_BEATZ(true, "OsrsBeatz", "UUs1rnF_c_VSg74M5CQ-HKWg", new File("E:/Music/Runescape/OSRS Beatz"), true, new File("E:/Music/Runescape/OSRS Beatz/OSRS Beatz.m3u")),
    
    BRAVE_WILDERNESS(false, "BraveWilderness", "UU6E2mP01ZLH_kbAyeazCNdg", new File("E:/Downloads/Brave Wilderness"), false),
    
    VSAUCE(true, "Vsauce", "UU6nSFpj9HTCZ5t-N3Rm3-HA", new File("E:/Videos/Vsauce"), false),
    MIND_FIELD_S1(true, "MindFieldS1", "PLZRRxQcaEjA4qyEuYfAMCazlL0vQDkIj2", new File("E:/Videos/Vsauce/Mind Field/Season 1"), false),
    MIND_FIELD_S2(true, "MindFieldS2", "PLZRRxQcaEjA7wmh3Z6EQuOK9fm1CqnJCI", new File("E:/Videos/Vsauce/Mind Field/Season 2"), false),
    MIND_FIELD_S3(true, "MindFieldS3", "PLZRRxQcaEjA7LX19uAySGlc9hmprBxfEP", new File("E:/Videos/Vsauce/Mind Field/Season 3"), false);
    
    
    //Fields
    
    public boolean active;
    
    public String name;
    
    public String playlistId;
    
    public File outputFolder;
    
    public boolean saveAsMp3;
    
    public File playlistFile;
    
    
    //Constructors
    
    Channel(boolean active, String name, String playlistId, File outputFolder, boolean saveAsMp3, File playlistFile) {
        this.active = active;
        this.name = name;
        this.playlistId = playlistId;
        this.outputFolder = outputFolder;
        this.saveAsMp3 = saveAsMp3;
        this.playlistFile = playlistFile;
    }
    
    Channel(boolean active, String name, String playlistId, File outputFolder, boolean saveAsMp3) {
        this.active = active;
        this.name = name;
        this.playlistId = playlistId;
        this.outputFolder = outputFolder;
        this.saveAsMp3 = saveAsMp3;
    }
    
    
    //Functions
    
    public static void performSpecialConditions(Channel channel, Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> queue, List<String> save, List<String> blocked) throws Exception {
        switch (channel) {
            case OSRS_BEATZ:
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("runescape")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            case VSAUCE:
                Date oldest = new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-15");
                videoMap.forEach((key, value) -> {
                    if (value.title.contains("#") || value.title.contains("DONG") || value.title.contains("Mind Field") || value.date.before(oldest)) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
        }
    }
    
}