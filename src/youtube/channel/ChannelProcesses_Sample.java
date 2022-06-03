/*
 * File:    ChannelProcesses_Sample.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.channel;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import youtube.YoutubeChannelDownloader;
import youtube.channel.process.FilterProcess;
import youtube.channel.process.RenameProcess;

/**
 * Holds pre and post processes to operate on Channels before or after generating the download queue.
 */
public class ChannelProcesses_Sample {
    
    //Functions
    
    /**
     * Performs special checks specific to a Channel before producing the download queue.
     * Typically used for renaming videos in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The video map.
     * @param queue    The list of queued videos.
     * @param save     The list of saved videos.
     * @param blocked  The list of blocked videos.
     * @throws Exception When there is an error.
     */
    public static void performSpecialPreConditions(Channel channel, Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> queue, List<String> save, List<String> blocked) throws Exception {
        switch (channel.key) {
            
            case "JIMTV_PROGRAMMING":
                RenameProcess.replace(videoMap, List.of(
                        Map.entry("Programming - Coding - Hacking music vol.", "Volume "),
                        Map.entry(" (", " - "),
                        Map.entry(")", "")));
                break;
            
            case "LITTLE_SOUL":
            case "KURUMI":
            case "ASUNA":
            case "ARIA":
            case "ARIA_NIGHTCORE":
                RenameProcess.appendUploadDate(videoMap, "yyyy-MM-dd");
                break;
            
            case "SOUND_LIBRARY":
                RenameProcess.regexRemove(videoMap,
                        "(?i)\\s*-\\s*(?:Sound Effects?|Music) for Editing");
                break;
            
            case "BY_RELEASE":
                RenameProcess.pattern(videoMap,
                        "^(?<title>.+?)(?:\\s*-\\s*By\\sRelease\\s*-?-\\s(?<episode>\\d*))?$",
                        "By Release - $i - $title");
                break;
            case "OSRS_CHALLENGES_TANZOO":
            case "OSRS_CHALLENGES_VIRTOSO":
                RenameProcess.regexReplace(videoMap, List.of(
                        Map.entry("Tanzoo\\s*vs?\\.?\\s*Virtoso\\s*-", ""),
                        Map.entry("\\s*-?\\sRunescape\\s2007\\s*-?", ""),
                        Map.entry("-\\s*Challenge\\s*Episodes?", "- Episode"),
                        Map.entry("(?:\\s*-\\s*)?(?:OSRS|Osrs|osrs)\\s*Challenges?\\s*-?", ""),
                        Map.entry("\\s*Special\\s*$", ""),
                        Map.entry("(?:\\s*-\\s*)?\\((?:Tanzoo|Virtoso)\\)", ""),
                        Map.entry("\\s*-*\\s*(?:Episode|EP\\.|Ep\\.)\\s*(?<episode>\\d+)\\s*$", ""),
                        Map.entry("^\\s*", "OSRS Challenges - "),
                        Map.entry("\\s*$", (" (" + channel.name.replace("OsrsChallenges", "") + ")"))));
                break;
            case "MUDKIP_HCIM":
                RenameProcess.pattern(videoMap,
                        "^(?:\\[OSRS]\\s*)?(?:Maxed HCIM\\s)?(?<title>.+?)\\s(?:Maxed HCIM\\s)?(?:-\\s|\\(|)-\\s(?<episode>\\d*\\.?\\d+)\\)?$", false,
                        "Maxed HCIM - $episode - $title");
                RenameProcess.pattern(videoMap,
                        "^(?:\\[OSRS]\\s*)?(?:HCIM\\s)?(?<episode>\\d+)\\s*-\\s*(?<title>.+?)$", false,
                        "HCIM - $episode - $title");
                RenameProcess.pattern(videoMap,
                        "^(?:\\[OSRS]\\s*)?(?<title>.+?)\\s(?:-\\s|\\(|)HCIM\\s*(?:[Ee]p(?:isode|\\.)\\s*)?(?<episode>\\d*\\.?\\d+)\\)?\\s*(?<level>(?:\\(\\d+-\\d+\\))?)$", false,
                        "HCIM - $episode - $title $level");
                break;
            case "MUDKIP_UIM":
                RenameProcess.pattern(videoMap,
                        "^(?<title>.+?)(\\s(?:\\(UIM\\s-\\s|\\(-\\s)(?<episode>\\d+)\\))$", false,
                        "UIM - $episode - $title");
                break;
            case "SWAMPLETICS":
                RenameProcess.pattern(videoMap,
                        "^(?<title>.+?)\\s\\((?:Swampletics\\s?)?(?:#|-\\s)(?<episode>\\d+)\\)$",
                        "Swampletics - $i - $title");
                break;
            case "LOWER_THE_BETTER":
                RenameProcess.pattern(videoMap,
                        "^(?<title>.+?)(?:\\s?[:\\-]\\s?Lower\\s[Tt]he\\sBetter\\s?(?:Ep\\.\\s)?(?:#|-\\s)(?<episode>\\d+))?$",
                        "Lower the Better - $i - $title");
                break;
            case "OSRS_WEEKLY_RECAP":
                RenameProcess.pattern(videoMap,
                        "^(?<title>.+?)!*\\s*(?:[#\\-]\\s*(?:\\d+\\s*\\-\\s*)?)?(?:(?:OSRS\\s)?Weekly\\sRecap[\\s\\d\\-#!]*)?(?:\\[OSRS])?$",
                        "Weekly Recap - $d - $title");
                break;
            case "IRON_MAIN":
                RenameProcess.pattern(videoMap,
                        "^(?<title>.+?)(\\s*[|\\-]\\s*(?:IronMain\\s*)?[(\\[][#\\-]\\s*(?<episode>\\d+)[)\\]])?$",
                        "IronMain - $i - $title");
                break;
            case "ONE_KICK_RICK":
                RenameProcess.replace(videoMap, "Series Trailer", "ep.0");
                RenameProcess.pattern(videoMap,
                        "^(?<title>.+?)\\s*-\\s*(?:Lumbridge-Draynor\\s(?:Only\\s)?HCIM\\s-\\s)?(?:One\\sKick\\sRick\\s-\\s)?ep\\.(?<episode>\\d+)$",
                        "One Kick Rick - $episode - $title");
                break;
            
            case "STEVE_MOULD":
                RenameProcess.pattern(videoMap,
                        "(?i)^.*(?:fewer|more)\\sthan\\stom.*$", false,
                        "This Video Has...");
                break;
            case "MIND_FIELD_S1":
            case "MIND_FIELD_S2":
            case "MIND_FIELD_S3":
                RenameProcess.pattern(videoMap,
                        "^(?<title>.+?)(?:\\s*-\\s*(?:Mind\\sField\\s(?:S\\d+\\s)?)\\(Ep\\.?\\s*(?<episode>\\d+)\\))?$",
                        "Mind Field - S0" + channel.name.charAt(channel.name.length() - 1) + "E0$i - $title");
                break;
            
            case "LOCK_PICKING_LAWYER":
                RenameProcess.regexReplace(videoMap, List.of(
                        Map.entry("\\[(\\d+)]\\s", "000$1 - "),
                        Map.entry("^\\d+(?=\\d{4})", "")));
                break;
            
            case "NHAT_BANG_SPA":
                RenameProcess.replace(videoMap, "010", "10");
                break;
            
            case "FORENSIC_FILES_S01":
                YoutubeChannelDownloader.Video video = new YoutubeChannelDownloader.Video("OZc6vcGjknI", "Medical Detectives (Forensic Files) - Series Premiere - The Disappearance of Helle Crafts", "2015-01-23 12:15:00", channel);
                HashMap<String, YoutubeChannelDownloader.Video> tmp = new LinkedHashMap<>(videoMap);
                videoMap.clear();
                videoMap.put(video.videoId, video);
                videoMap.putAll(tmp);
            case "FORENSIC_FILES_S02":
            case "FORENSIC_FILES_S03":
            case "FORENSIC_FILES_S04":
            case "FORENSIC_FILES_S05":
            case "FORENSIC_FILES_S06":
            case "FORENSIC_FILES_S07":
            case "FORENSIC_FILES_S08":
            case "FORENSIC_FILES_S09":
            case "FORENSIC_FILES_S10":
            case "FORENSIC_FILES_S11":
            case "FORENSIC_FILES_S12":
            case "FORENSIC_FILES_S13":
            case "FORENSIC_FILES_S14":
            case "FORENSIC_FILES":
                RenameProcess.replace(videoMap, "Series Premiere", "S01E01");
                RenameProcess.regexReplace(videoMap, List.of(
                        Map.entry("^(?:Medical\\sDetectives\\s)?\\(?Forensic\\sFiles\\s?\\)?(?:\\s?in\\sHD)?", "Forensic Files"),
                        Map.entry("Season\\s(\\d+)\\s?,\\sEp(?:isode)?\\s(\\d+)\\s*-", "S$1E$2 -"),
                        Map.entry("S(\\d)E", "S0$1E"),
                        Map.entry("E(\\d)\\s", "E0$1 ")));
                break;
        }
    }
    
    /**
     * Performs special checks specific to a Channel after producing the queue.
     * Typically used for filtering; Do not use this to rename videos in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The video map.
     * @param queue    The list of queued videos.
     * @param save     The list of saved videos.
     * @param blocked  The list of blocked videos.
     * @throws Exception When there is an error.
     */
    @SuppressWarnings("DuplicateBranchesInSwitch")
    public static void performSpecialPostConditions(Channel channel, Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> queue, List<String> save, List<String> blocked) throws Exception {
        switch (channel.key) {
            
            case "MUSIC_LAB_HACKER":
            case "MUSIC_LAB_WORK":
            case "MUSIC_LAB_CHILLSTEP":
            case "MUSIC_LAB_CHILLOUT":
            case "MUSIC_LAB_AMBIENT":
            case "MUSIC_LAB_LOFI":
            case "MUSIC_LAB_CONTEMPORARY":
            case "MUSIC_LAB_STUDY":
            case "MUSIC_LAB_CHILLHOP":
                FilterProcess.containsIgnoreCase(videoMap, blocked, "live 24-7");
                break;
            
            case "MR_MOM_MUSIC_NEW":
                FilterProcess.dateBefore(videoMap, blocked,
                        new SimpleDateFormat("yyyy-MM-dd").parse("2020-01-24"));
                break;
            
            case "OSRS_BEATZ":
                FilterProcess.notContainsIgnoreCase(videoMap, blocked, "runescape");
                break;
            case "OSRS_WEEKLY_RECAP":
                FilterProcess.notContainsIgnoreCase(videoMap, blocked, "weekly recap");
                break;
            case "OSRS_MARKET_ANALYSIS":
                FilterProcess.notContainsIgnoreCase(videoMap, blocked, List.of("market", "economy"));
                break;
            
            case "ISAAC_ARTHUR":
            case "ISAAC_ARTHUR_P01":
            case "ISAAC_ARTHUR_P02":
            case "ISAAC_ARTHUR_P03":
            case "ISAAC_ARTHUR_P04":
            case "ISAAC_ARTHUR_P05":
            case "ISAAC_ARTHUR_P06":
            case "ISAAC_ARTHUR_P07":
            case "ISAAC_ARTHUR_P08":
            case "ISAAC_ARTHUR_P09":
            case "ISAAC_ARTHUR_P10":
            case "ISAAC_ARTHUR_P11":
            case "ISAAC_ARTHUR_P12":
            case "ISAAC_ARTHUR_P13":
            case "ISAAC_ARTHUR_P14":
            case "ISAAC_ARTHUR_P15":
            case "ISAAC_ARTHUR_P16":
            case "ISAAC_ARTHUR_P17":
            case "ISAAC_ARTHUR_P18":
            case "ISAAC_ARTHUR_P19":
            case "ISAAC_ARTHUR_P20":
            case "ISAAC_ARTHUR_P21":
            case "ISAAC_ARTHUR_P22":
            case "ISAAC_ARTHUR_P23":
            case "ISAAC_ARTHUR_P24":
            case "ISAAC_ARTHUR_P25":
            case "ISAAC_ARTHUR_P27":
            case "ISAAC_ARTHUR_P28":
                FilterProcess.containsIgnoreCase(videoMap, blocked, List.of(
                        "livestream", "hades", "in the beginning", "collab", "colab"));
                break;
            case "PBS_SPACE_TIME_MATT_ONLY":
                FilterProcess.dateBefore(videoMap, blocked,
                        new SimpleDateFormat("yyyy-MM-dd").parse("2015-09-01"));
                break;
            
            case "ANSWERS_WITH_JOE":
                FilterProcess.containsIgnoreCase(videoMap, blocked, "live stream");
                break;
            case "THOUGHTY2_NEW_INTRO_ONLY":
                FilterProcess.dateBefore(videoMap, blocked,
                        new SimpleDateFormat("yyyy-MM-dd").parse("2018-06-08"));
                break;
            
            case "VSAUCE":
                FilterProcess.contains(videoMap, blocked, List.of("#", "- shorts", "LUT -", "IMG! -", "DONG", "Mind Field"));
                FilterProcess.dateBefore(videoMap, blocked,
                        new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-15"));
                break;
            
            case "DOMAIN_OF_SCIENCE":
                FilterProcess.dateBefore(videoMap, blocked,
                        new SimpleDateFormat("yyyy-MM-dd").parse("2016-11-27"));
                break;
            
            case "ADAM_SAVAGE_ONE_DAY_BUILDS":
                FilterProcess.notContainsIgnoreCase(videoMap, blocked, "one day build");
                FilterProcess.containsIgnoreCase(videoMap, blocked, "last call");
                break;
            
            case "NILE_BLUE":
                FilterProcess.containsIgnoreCase(videoMap, blocked, "announcement");
                break;
            
            case "CHUBBYEMU":
                FilterProcess.dateBefore(videoMap, blocked,
                        new SimpleDateFormat("yyyy-MM-dd").parse("2017-08-07"));
                break;
            case "LIKE_YOU":
                FilterProcess.containsIgnoreCase(videoMap, blocked, List.of(
                        "photographer", "phone", "friend", "my son", "dogs"));
                break;
            
            case "KITBOGA_UNCUT":
                FilterProcess.containsIgnoreCase(videoMap, blocked, "live stream");
                break;
            
            case "BEST_CUBE_COUBOY":
            case "BEST_CUBE_SPARTA":
                FilterProcess.regexNotContainsIgnoreCase(videoMap, blocked, "best c(?:ube|oub)");
                break;
            case "SEXY_CUBE":
                FilterProcess.regexNotContainsIgnoreCase(videoMap, blocked, "sexy c(?:ube|oub)");
                break;
        }
    }
    
}
