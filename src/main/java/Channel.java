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
    
    MUSIC_LAB_HACKER(true, "MusicLabHacker", "PLdE7uo_7KBkfAWkk7-Clm18krBuziKQfr", "Beats/Music Lab", true, "Beats/Hacker Music.m3u"),
    MUSIC_LAB_WORK(true, "MusicLabWork", "PLdE7uo_7KBkc6L7Bgqzz_Q7q2JN2AqHV3", "Beats/Music Lab", true, "Beats/Work Music.m3u"),
    MUSIC_LAB_CHILLSTEP(true, "MusicLabChillstep", "PLdE7uo_7KBkeH0adsnxZupMARfGxY6qik", "Beats/Music Lab", true, "Beats/Chillstep Music.m3u"),
    MUSIC_LAB_CHILLOUT(true, "MusicLabChillout", "PLdE7uo_7KBkeSTmryNClNxUkioFpq3Btx", "Beats/Music Lab", true, "Beats/Chillout Music.m3u"),
    MUSIC_LAB_AMBIENT(true, "MusicLabAmbient", "PLdE7uo_7KBketYRDDb8lheUOD4q7U27DR", "Beats/Music Lab", true, "Beats/Ambient Music.m3u"),
    MUSIC_LAB_LOFI(false, "MusicLabLoFi", "PLdE7uo_7KBkcywzGlsZ9c6aerJr3tK058", "Beats/Music Lab", true, "Beats/Lo-Fi Music.m3u"),
    MUSIC_LAB_CONTEMPORARY(false, "MusicLabContemporary", "PLdE7uo_7KBkdg39l-YqG9eXanlNBiSgzf", "Beats/Music Lab", true, "Beats/Contemporary Music.m3u"),
    MUSIC_LAB_STUDY(true, "MusicLabStudy", "PLdE7uo_7KBkcx8_AcwRjTPiYSauQTQeNi", "Beats/Music Lab", true, "Beats/Study Music.m3u"),
    MUSIC_LAB_CHILLHOP(false, "MusicLabChillhop", "PLdE7uo_7KBkdmK1rCN4D-GO9g79QXqdVd", "Beats/Music Lab", true, "Beats/Chillhop Music.m3u"),
    
    TRAP_CITY(true, "TrapCity", "UU65afEgL62PGFWXY7n6CUbA", "Trap/Trap City", true, "Trap/Trap.m3u"),
    SKY_BASS(true, "SkyBass", "UUpXbwekw4ySNHGt26aAKvHQ", "Trap/Sky Bass", true, "Trap/Trap.m3u"),
    TRAP_NATION(true, "TrapNation", "UUa10nxShhzNrCE1o2ZOPztg", "Trap/Trap Nation", true, "Trap/Trap.m3u"),
    BASS_NATION(true, "BassNation", "UUCvVpbYRgYjMN7mG7qQN0Pg", "Trap/Bass Nation", true, "Trap/Trap.m3u"),
    
    THE_COMET_IS_COMING(false, "TheCometIsComing", "PLqffNt5cY34WycBZsqhVoXgRnehbbxyTB", "Music/The Comet Is Coming", true, "Music/The Comet Is Coming.m3u"),
    
    OSRS_BEATZ(true, "OsrsBeatz", "UUs1rnF_c_VSg74M5CQ-HKWg", "Runescape/OSRS Beatz", true, "Runescape/OSRS Beatz/OSRS Beatz.m3u"),
    
    BRAVE_WILDERNESS(false, "BraveWilderness", "UU6E2mP01ZLH_kbAyeazCNdg", "Brave Wilderness", false),
    
    VSAUCE(true, "Vsauce", "UU6nSFpj9HTCZ5t-N3Rm3-HA", "Vsauce", false),
    MIND_FIELD_S1(true, "MindFieldS1", "PLZRRxQcaEjA4qyEuYfAMCazlL0vQDkIj2", "Vsauce/Mind Field/Season 1", false),
    MIND_FIELD_S2(true, "MindFieldS2", "PLZRRxQcaEjA7wmh3Z6EQuOK9fm1CqnJCI", "Vsauce/Mind Field/Season 2", false),
    MIND_FIELD_S3(true, "MindFieldS3", "PLZRRxQcaEjA7LX19uAySGlc9hmprBxfEP", "Vsauce/Mind Field/Season 3", false);
    
    
    //Constants
    
    public final File storageDrive = new File("E:/");
    
    public final File musicDir = new File(storageDrive, "Music");
    
    public final File videoDir = new File(storageDrive, "Videos");
    
    
    //Fields
    
    public boolean active;
    
    public String name;
    
    public String playlistId;
    
    public File outputFolder;
    
    public boolean saveAsMp3;
    
    public File playlistFile;
    
    
    //Constructors
    
    Channel(boolean active, String name, String playlistId, String outputFolder, boolean saveAsMp3, String playlistFile) {
        this.active = active;
        this.name = name;
        this.playlistId = playlistId;
        this.outputFolder = new File(saveAsMp3 ? musicDir : videoDir, outputFolder);
        this.saveAsMp3 = saveAsMp3;
        this.playlistFile = (playlistFile != null) ? new File(saveAsMp3 ? musicDir : videoDir, playlistFile) : null;
    }
    
    Channel(boolean active, String name, String playlistId, String outputFolder, boolean saveAsMp3) {
        this(active, name, playlistId, outputFolder, saveAsMp3, null);
    }
    
    
    //Functions
    
    public static void performSpecialConditions(Channel channel, Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> queue, List<String> save, List<String> blocked) throws Exception {
        switch (channel) {
            case MUSIC_LAB_HACKER:
            case MUSIC_LAB_WORK:
            case MUSIC_LAB_CHILLSTEP:
            case MUSIC_LAB_CHILLOUT:
            case MUSIC_LAB_AMBIENT:
            case MUSIC_LAB_LOFI:
            case MUSIC_LAB_CONTEMPORARY:
            case MUSIC_LAB_STUDY:
            case MUSIC_LAB_CHILLHOP:
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("live 24-7")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
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