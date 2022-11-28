/*
 * File:    ChannelProcesses_Sample.java
 * Package: youtube.channel.process
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.process;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.ChannelConfig;
import youtube.channel.process.macro.BaseProcess;
import youtube.channel.process.macro.FilterProcess;
import youtube.channel.process.macro.RenameProcess;
import youtube.entity.info.VideoInfo;

/**
 * Holds pre and post processes to operate on Channels before or after generating the download queue.
 */
@SuppressWarnings({"SpellCheckingInspection", "DuplicateBranchesInSwitch", "StatementWithEmptyBody", "RedundantSuppression"})
public class ChannelProcesses_Sample {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelProcesses_Sample.class);
    
    
    //Static Methods
    
    /**
     * Performs special checks specific to a Channel before producing the download queue.<br/>
     * Typically used for renaming videos in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The video map.
     * @throws Exception When there is an error.
     */
    public static void performSpecialPreConditions(ChannelConfig channel, Map<String, VideoInfo> videoMap) throws Exception {
        switch (channel.getKey().replaceAll("_[PS]\\d+$", "")) {
            
            //GENERAL
            
            case "MIND_FIELD":
                RenameProcess.format(videoMap,
                        "^(?<title>.+?)(?:\\s*-\\s*(?:Mind\\sField\\s(?:S\\d+\\s)?)\\(Ep\\.?\\s*(?<episode>\\d+)\\))?$",
                        "Mind Field - S0" + channel.getName().charAt(channel.getName().length() - 1) + "E0$i - $title");
                break;
            
            
            //PHYSICS
            
            case "STEVE_MOULD":
                RenameProcess.format(videoMap, false,
                        "(?i)^.*(?:fewer|more)\\sthan\\stom.*$",
                        "This Video Has...");
                break;
            
            
            //MEDICINE
            
            case "NHAT_BANG_SPA":
                RenameProcess.replace(videoMap,
                        "010", "10");
                break;
            
            
            //CRIME
            
            case "FORENSIC_FILES":
                if (channel.getKey().endsWith("_S01")) {
                    final VideoInfo video = new VideoInfo("OZc6vcGjknI", "Medical Detectives (Forensic Files) - Series Premiere - The Disappearance of Helle Crafts", "2015-01-23 12:15:00", channel);
                    final HashMap<String, VideoInfo> tmp = new LinkedHashMap<>(videoMap);
                    videoMap.clear();
                    videoMap.put(video.videoId, video);
                    videoMap.putAll(tmp);
                }
                RenameProcess.replace(videoMap,
                        "Series Premiere", "S01E01");
                RenameProcess.regexReplace(videoMap, List.of(
                        Map.entry("^(?:Medical\\sDetectives\\s)?\\(?Forensic\\sFiles\\s?\\)?(?:\\s?in\\sHD)?", "Forensic Files"),
                        Map.entry("Season\\s(\\d+)\\s?,\\sEp(?:isode)?\\s(\\d+)\\s*-", "S$1E$2 -"),
                        Map.entry("S(\\d)E", "S0$1E"),
                        Map.entry("E(\\d)\\s", "E0$1 ")));
                break;
            
            case "LOCK_PICKING_LAWYER":
                RenameProcess.regexReplace(videoMap, List.of(
                        Map.entry("\\[(\\d+)]\\s", "000$1 - "),
                        Map.entry("^\\d+(?=\\d{4})", "")));
                break;
            
            
            //DOCUMENTARY
            
            case "DW_DOCUMENTARY":
                RenameProcess.regexRemoveIgnoreCase(videoMap, List.of(
                        "\\[4K]",
                        "\\s*[\\-|]?\\s*[\\[(][^)\\]]*DOCUMENTARY[)\\]]",
                        "\\s*[\\-|]?\\s*(?:(?:FULL|FREE|DW)\\s*)+(?:[^-)\\]]+\\s*)?(?:DOCUMENTARY|DOCUMENTAL|ENGLISH)"));
                break;
            
            case "CNBC":
            case "CNBC_ORIGINALS":
                RenameProcess.regexRemoveIgnoreCase(videoMap, List.of(
                        "\\[4K]",
                        "\\s*[\\-|]\\s*CNBC\\s*(?:DOCUMENTARY|AFTER\\s?HOURS|MARATHON)?$",
                        "^CNBC\\s*(?:DOCUMENTARY|AFTER\\s?HOURS|MARATHON)?\\s*[\\-|]\\s*"));
                break;
            
            case "FRONTLINE_PBS":
                RenameProcess.regexRemoveIgnoreCase(videoMap, List.of(
                        "\\[4K]",
                        "\\s*[\\-|]?\\s*@Associated\\s+Press",
                        "\\s*[\\-|]?\\s*#AskFRONTLINE",
                        "\\s*-\\s*FRONTLINE(?:\\sPBS)?(?:\\s(?:DOCUMENTARY|EXPLAINS))?",
                        "\\s+\\((?:(?:FULL|FREE)\\s+)*DOCUMENTARY\\)",
                        "\\s+\\(.*CAPTIONS\\sAVAILABLE.*\\)"));
                RenameProcess.regexReplaceIgnoreCase(videoMap, List.of(
                        Map.entry("\\s+(?:-\\s*|\\()INTERVIEW(?:\\)|$)", " - Interview"),
                        Map.entry("\\s+(?:-\\s*|\\()TRAILER(?:\\)|$)", " - Trailer"),
                        Map.entry("\\s+(?:-\\s*|\\()PODCAST(?:\\)|$)", " - Podcast")));
                break;
            
            case "SPARK_DOCUMENTARY":
                RenameProcess.replace(videoMap,
                        " l ", " | ");
                RenameProcess.regexRemoveIgnoreCase(videoMap, List.of(
                        "\\[4K]",
                        "\\s*[\\-|]?\\s*[\\[(][^)\\]]*DOCUMENTARY[)\\]]",
                        "\\s*-\\s*SPARK(?:\\s(?:DOCUMENTARY|EXPLAINS))?",
                        "\\s+\\((?:(?:FULL|FREE)\\s+)*DOCUMENTARY\\)"));
                break;
            
            case "ENDEVR_DOCUMENTARY":
                RenameProcess.regexRemoveIgnoreCase(videoMap, List.of(
                        "\\[4K]",
                        "\\s*[\\-|]?\\s*[\\[(][^)\\]]*DOCUMENTARY[)\\]]",
                        "\\s*-\\s*ENDE?VR(?:\\s(?:DOCUMENTARY|EXPLAINS))?",
                        "\\s+\\((?:(?:FULL|FREE)\\s+)*DOCUMENTARY\\)"));
                break;
            
            
            //RUNESCAPE
            
            case "BY_RELEASE":
                RenameProcess.format(videoMap,
                        "^(?<title>.+?)(?:\\s*-\\s*By\\sRelease\\s*-?\\s#?(?<episode>\\d*))?$",
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
                        Map.entry("\\s*$", (" (" + channel.getName().replace("OsrsChallenges", "") + ")"))));
                break;
            
            case "MUDKIP_HCIM":
                RenameProcess.format(videoMap, false,
                        "^(?:\\[OSRS]\\s*)?(?:Maxed HCIM\\s)?(?<title>.+?)\\s(?:Maxed HCIM\\s)?(?:-\\s|\\(|)[#\\-]\\s?(?<episode>\\d*\\.?\\d+)\\)?$",
                        "Maxed HCIM - $episode - $title");
                RenameProcess.format(videoMap, false,
                        "^(?:\\[OSRS]\\s*)?(?:HCIM\\s)?#?(?<episode>\\d+)\\s*-\\s*(?<title>.+?)$",
                        "HCIM - $episode - $title");
                RenameProcess.format(videoMap, false,
                        "^(?:\\[OSRS]\\s*)?(?<title>.+?)\\s(?:-\\s|\\(|)HCIM\\s*(?:[Ee]p(?:isode|\\.)\\s*)?#?(?<episode>\\d*\\.?\\d+)\\)?\\s*(?<level>(?:\\(\\d+-\\d+\\))?)$",
                        "HCIM - $episode - $title $level");
                break;
            
            case "MUDKIP_UIM":
                RenameProcess.format(videoMap, false,
                        "^(?<title>.+?)\\s\\((?:UIM\\s)?[#\\-]\\s*(?<episode>\\d+)\\)$",
                        "UIM - $episode - $title");
                break;
            
            case "SWAMPLETICS":
                RenameProcess.format(videoMap,
                        "^(?<title>.+?)\\s\\((?:Swampletics\\s?)?(?:#|-\\s)(?<episode>\\d+)\\)$",
                        "Swampletics - $i - $title");
                break;
            
            case "TILEMAN":
                RenameProcess.format(videoMap,
                        "^(?<title>.+?)\\s(?:[|\\-]\\s)?(?:tileman\\s?)?[#\\-]\\s*(?<episode>\\d+)$",
                        "Tileman - $i - $title");
                break;
            
            case "LOWER_THE_BETTER":
                RenameProcess.format(videoMap,
                        "^(?<title>.+?)(?:\\s?[:\\-]\\s?Lower\\s[Tt]he\\sBetter\\s?(?:Ep\\.\\s)?(?:[#\\-]\\s?)(?<episode>\\d+))?$",
                        "Lower the Better - $i - $title");
                break;
            
            case "OSRS_WEEKLY_RECAP":
                RenameProcess.format(videoMap,
                        "^(?<title>.+?)!*\\s*(?:[#\\-]\\s*(?:\\d+\\s*\\-\\s*)?)?(?:(?:OSRS\\s)?Weekly\\sRecap[\\s\\d\\-#!]*)?(?:\\[OSRS])?$",
                        "Weekly Recap - $d - $title");
                break;
            
            case "IRON_MAIN":
                RenameProcess.format(videoMap,
                        "^(?<title>.+?)(\\s*[|\\-]\\s*(?:IronMain\\s*)?[(\\[][#\\-]\\s*(?<episode>\\d+)[)\\]])?$",
                        "IronMain - $i - $title");
                break;
            
            case "ONE_KICK_RICK":
                RenameProcess.replace(videoMap,
                        "Series Trailer", "ep.0");
                RenameProcess.format(videoMap,
                        "^(?<title>.+?)\\s*-\\s*(?:Lumbridge\\s*-\\s*Draynor\\s(?:Only\\s)?HCIM\\s-\\s)?(?:One\\sKick\\sRick\\s-\\s)?ep\\.(?<episode>\\d+)$",
                        "One Kick Rick - $episode - $title");
                break;
            
            
            //LOFI
            
            case "MUSIC_LAB":
            case "MUSIC_LAB_HACKER":
            case "MUSIC_LAB_WORK":
            case "MUSIC_LAB_CHILLSTEP":
            case "MUSIC_LAB_CHILLOUT":
            case "MUSIC_LAB_AMBIENT":
            case "MUSIC_LAB_LOFI":
            case "MUSIC_LAB_CONTEMPORARY":
            case "MUSIC_LAB_STUDY":
            case "MUSIC_LAB_CHILLHOP":
            case "MUSIC_LAB_ENERGY":
            case "MUSIC_LAB_FUTURE_GARAGE":
            case "AMBIENT_MUSIC_LAB":
            case "AMBIENT_MUSIC_LAB_STUFY_WORK_AMBIENT":
            case "AMBIENT_MUSIC_LAB_MEDITATION_AMBIENT":
            case "AMBIENT_MUSIC_LAB_DRONE":
            case "AMBIENT_MUSIC_LAB_SPACE_AMBIENT":
            case "AMBIENT_MUSIC_LAB_NATURE_AMBIENT":
            case "AMBIENT_MUSIC_LAB_MOVIES_AMBIENT":
            case "AMBIENT_MUSIC_LAB_DARK_AMBIENT":
            case "AMBIENT_MUSIC_LAB_AMBIENT_MUSIC":
            case "DREAMHOP_MUSIC":
            case "LITTLE_SOUL":
                RenameProcess.appendUploadDate(videoMap);
                break;
            
            
            //NIGHTCORE
            
            case "KURUMI":
            case "NEZUKO":
            case "ASUNA":
            case "ARIA":
            case "ARIA_NIGHTCORE":
                RenameProcess.appendUploadDate(videoMap);
                break;
            
            
            //DARKSYNTH
            
            case "JIMTV_PROGRAMMING":
                RenameProcess.replace(videoMap, List.of(
                        Map.entry("Programming - Coding - Hacking music vol.", "Volume "),
                        Map.entry(" (", " - "),
                        Map.entry(")", "")));
                break;
            
            case "AIM_TO_HEAD":
                RenameProcess.regexRemove(videoMap,
                        "\\[(?:Copyright\\s)?(?:FREE|SOLD)]\\s*", true);
                break;
            
            
            //AUDIOBOOK
            
            case "GREATEST_AUDIOBOOKS":
                RenameProcess.regexReplaceIgnoreCase(videoMap, List.of(
                        Map.entry("\\+", ""),
                        Map.entry("(?:[\\(\\[]\\s*)?(?:GR?EATEST|FULL|CONDENSED|WHISPER)?\\s*([^\\s\\-]+\\sLANGUAGE|)#?\\s*(?:POEM|POETRY|NOVEL|STORY)?\\s*AUDIO\\s*.?\\s*BOOKS?(?:\\.COM?)?(?:\\s*VERSION)?(?:\\s*[\\]\\)])?", " $1 "),
                        Map.entry("-?\\s+BY(?:\\s*THE)?\\s*", " - "),
                        Map.entry("-?\\s*\\(?V\\s*(\\d+)\\s*(?:\\)|\\(|-|$)", " - V$1"),
                        Map.entry("-?\\s*\\(?(BOOK\\s*\\d+)?\\s*(?:CHA)?P(?:ART|\\.)?\\s*(\\d+)\\s*\\(?OF\\s*(\\d+)\\s*(?:\\)|\\(|-|$)", " - ($1 Part $2 of $3) - "),
                        Map.entry("-?\\s*\\(?(BOOK\\s*\\d+)?\\s*(?:CHA)?P(?:ART|\\.)?\\s*(\\d+)\\s*(?:\\)|\\(|-|$)", " - ($1 Part $2) - "),
                        Map.entry("-?\\s*\\(?BOOK\\s*(\\d+)\\s*\\(?OF\\s*(\\d+)\\s*(?:\\)|\\(|-|$)", " - (Book $1 of $2) - "),
                        Map.entry("-?\\s*\\(?BOOK\\s*(\\d+)\\s*(?:\\)|\\(|-|$)", " - (Book $1) - "),
                        Map.entry("(\\s*\\-)+$", ""),
                        Map.entry("\\(\\s+", "("),
                        Map.entry("\\s+\\)", ")")));
                BaseProcess.rename(videoMap, (id, video) ->
                        Arrays.stream(video.title.split("\\s+-\\s+", -1))
                                .map(e -> e.equals(e.toUpperCase()) ? StringUtility.toTitleCase(e.toLowerCase()) : e)
                                .collect(Collectors.joining(" - ")));
                break;
            
            
            //SOUNDBYTE
            
            case "SOUND_LIBRARY":
                RenameProcess.regexRemove(videoMap,
                        "\\s*-\\s*(?:Sound Effects?|Music) for Editing", true);
                break;
        }
    }
    
    /**
     * Performs special checks specific to a Channel after producing the queue.<br/>
     * Typically used for filtering; Do not use this to rename videos in the video map.
     *
     * @param channel  The Channel.
     * @param videoMap The video map.
     * @throws Exception When there is an error.
     */
    public static void performSpecialPostConditions(ChannelConfig channel, Map<String, VideoInfo> videoMap) throws Exception {
        switch (channel.getKey().replaceAll("_[PS]\\d+$", "")) {
            
            //GENERAL
            
            case "ANSWERS_WITH_JOE":
                FilterProcess.containsIgnoreCase(videoMap,
                        "live stream");
                break;
            
            case "THOUGHTY2_NEW_INTRO_ONLY":
                FilterProcess.dateBefore(videoMap,
                        LocalDate.of(2018, Month.JUNE, 8));
                break;
            
            case "VSAUCE":
                FilterProcess.contains(videoMap, List.of(
                        "#",
                        "- shorts",
                        "LUT -",
                        "IMG! -",
                        "DONG",
                        "Mind Field"));
                FilterProcess.dateBefore(videoMap,
                        LocalDate.of(2011, Month.OCTOBER, 15));
                break;
            
            case "DOMAIN_OF_SCIENCE":
                FilterProcess.dateBefore(videoMap,
                        LocalDate.of(2016, Month.NOVEMBER, 27));
                break;
            
            
            //SPACE
            
            case "ISAAC_ARTHUR":
                FilterProcess.containsIgnoreCase(videoMap, List.of(
                        "livestream",
                        "live stream",
                        "collab",
                        "colab",
                        "patreon",
                        " diy ",
                        "hades",
                        "in the beginning"));
                break;
            
            case "PBS_SPACE_TIME_MATT_ONLY":
                FilterProcess.dateBefore(videoMap,
                        LocalDate.of(2015, Month.SEPTEMBER, 1));
                break;
            
            
            //PHYSICS
            
            case "UP_AND_ATOM":
                FilterProcess.containsIgnoreCase(videoMap, List.of(
                        "merchandise",
                        "livestream",
                        "live stream"));
                break;
            
            
            //ENGINEERING
            
            case "ADAM_SAVAGE_ONE_DAY_BUILDS":
                FilterProcess.notContainsIgnoreCase(videoMap, "one day build");
                FilterProcess.containsIgnoreCase(videoMap, "last call");
                break;
            
            
            //COMPUTING
            
            case "NEAT_AI":
                FilterProcess.contains(videoMap, "#short");
                break;
            
            
            //CHEMISTRY
            
            case "NILE_BLUE":
                FilterProcess.containsIgnoreCase(videoMap, "announcement");
                break;
            
            
            //MEDICINE
            
            case "CHUBBYEMU":
                FilterProcess.dateBefore(videoMap,
                        LocalDate.of(2017, Month.AUGUST, 7));
                break;
            
            case "LIKE_YOU":
                FilterProcess.containsIgnoreCase(videoMap, List.of(
                        "photographer", "phone", "friend", "my son", "dogs"));
                break;
            
            
            //DOCUMENTARY
            
            case "ENDEVR_DOCUMENTARY":
                FilterProcess.containsIgnoreCase(videoMap, "DW Documentary");
                break;
            
            case "PHILON":
                FilterProcess.dateBefore(videoMap,
                        LocalDate.of(2022, Month.APRIL, 6));
                break;
            
            
            //RUNESCAPE
            
            case "OSRS_BEATZ":
                FilterProcess.notContainsIgnoreCase(videoMap,
                        "runescape");
                break;
            
            case "OSRS_WEEKLY_RECAP":
                FilterProcess.notContainsIgnoreCase(videoMap,
                        "weekly recap");
                break;
            
            case "OSRS_MARKET_ANALYSIS":
                FilterProcess.notContainsIgnoreCase(videoMap, List.of(
                        "market", "economy"));
                break;
            
            
            //FUNNY
            
            case "KITBOGA_UNCUT":
                FilterProcess.containsIgnoreCase(videoMap,
                        "live stream");
                break;
            
            
            //CUBE
            
            case "BEST_CUBE_COUBOY":
            case "BEST_CUBE_SPARTA":
                FilterProcess.regexNotContainsIgnoreCase(videoMap,
                        "best c(?:ube|oub)");
                break;
            
            case "SEXY_CUBE":
                FilterProcess.regexNotContainsIgnoreCase(videoMap,
                        "sexy c(?:ube|oub)");
                break;
            
            
            //LOFI
            
            case "MUSIC_LAB":
            case "MUSIC_LAB_HACKER":
            case "MUSIC_LAB_WORK":
            case "MUSIC_LAB_CHILLSTEP":
            case "MUSIC_LAB_CHILLOUT":
            case "MUSIC_LAB_AMBIENT":
            case "MUSIC_LAB_LOFI":
            case "MUSIC_LAB_CONTEMPORARY":
            case "MUSIC_LAB_STUDY":
            case "MUSIC_LAB_CHILLHOP":
            case "MUSIC_LAB_ENERGY":
            case "MUSIC_LAB_FUTURE_GARAGE":
            case "AMBIENT_MUSIC_LAB":
            case "AMBIENT_MUSIC_LAB_STUFY_WORK_AMBIENT":
            case "AMBIENT_MUSIC_LAB_MEDITATION_AMBIENT":
            case "AMBIENT_MUSIC_LAB_DRONE":
            case "AMBIENT_MUSIC_LAB_SPACE_AMBIENT":
            case "AMBIENT_MUSIC_LAB_NATURE_AMBIENT":
            case "AMBIENT_MUSIC_LAB_MOVIES_AMBIENT":
            case "AMBIENT_MUSIC_LAB_DARK_AMBIENT":
            case "AMBIENT_MUSIC_LAB_AMBIENT_MUSIC":
                FilterProcess.containsIgnoreCase(videoMap,
                        "live 24-7");
                break;
            
            
            //POP
            
            case "MR_MOM_MUSIC_NEW":
                FilterProcess.dateBefore(videoMap,
                        LocalDate.of(2020, Month.JANUARY, 24));
                break;
            
            
            //PSYTRANCE
            
            case "SPEEDSOUND":
                channel.state.blocked.add("FhOSu5fq5eE");
                break;
            
            
            //AUDIOBOOK
            
            case "GREATEST_AUDIOBOOKS":
                FilterProcess.containsIgnoreCase(videoMap, List.of(
                        "Book Review", "Author Interview", "Preview", "Excerpt"));
                break;
        }
    }
    
}
