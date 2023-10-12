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
import java.util.UUID;
import java.util.stream.Collectors;

import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.process.macro.BaseProcess;
import youtube.channel.process.macro.FilterProcess;
import youtube.channel.process.macro.RenameProcess;
import youtube.entity.Channel;
import youtube.entity.Video;
import youtube.entity.info.VideoInfo;

/**
 * Defines custom processes that operate on Channels during execution.
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
     * Typically used for renaming Videos in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     */
    public static void performSpecialPreConditions(Channel channel, Map<String, Video> videoMap) {
        switch (channel.getConfig().getKey().replaceAll("_[PS]\\d+$", "")) {
            
            //GENERAL
            
            case "MIND_FIELD":
                RenameProcess.formatIgnoreCase(channel, videoMap,
                        "^(?<title>.+?)(?:\\s*-\\s*(?:MIND\\sFIELD\\s(?:S\\d+\\s)?)\\(EP(?:ISODE|\\.)?\\s*(?<episode>\\d+)\\))?$",
                        "Mind Field - S0" + channel.getConfig().getName().charAt(channel.getConfig().getName().length() - 1) + "E0$i - $title");
                break;
            
            
            //PHYSICS
            
            case "STEVE_MOULD":
                RenameProcess.formatIgnoreCase(channel, videoMap, false,
                        "^.*(?:FEWER|MORE)\\sTHAN\\sTOM.*$",
                        "This Video Has...");
                break;
            
            
            //MEDICINE
            
            case "NHAT_BANG_SPA":
                RenameProcess.replace(channel, videoMap,
                        "010", "10");
                break;
            
            
            //CRIME
            
            case "FORENSIC_FILES":
                if (channel.getConfig().getKey().endsWith("_S01")) {
                    final VideoInfo videoInfo = new VideoInfo("OZc6vcGjknI", "Medical Detectives (Forensic Files) - Series Premiere - The Disappearance of Helle Crafts", "2015-01-23 12:15:00");
                    final HashMap<String, Video> tmp = new LinkedHashMap<>(videoMap);
                    videoMap.clear();
                    videoMap.put(videoInfo.getVideoId(), new Video(videoInfo));
                    videoMap.putAll(tmp);
                }
                RenameProcess.replaceIgnoreCase(channel, videoMap,
                        "SERIES PREMIERE", "S01E01");
                RenameProcess.regexReplaceIgnoreCase(channel, videoMap, List.of(
                        Map.entry("^(?:MEDICAL\\sDETECTIVES\\s)?\\(?FORENSIC\\sFILES\\s?\\)?(?:\\s?IN\\sHD)?", "Forensic Files"),
                        Map.entry("SEASON\\s(\\d+)\\s?,\\sEP(?:ISODE|\\.)?\\s*(\\d+)\\s*-", "S$1E$2 -"),
                        Map.entry("S(\\d)E", "S0$1E"),
                        Map.entry("E(\\d)\\s", "E0$1 ")));
                break;
            
            case "LOCK_PICKING_LAWYER":
                RenameProcess.regexReplace(channel, videoMap, List.of(
                        Map.entry("\\[(\\d+)]\\s", "000$1 - "),
                        Map.entry("^\\d+(?=\\d{4})", "")));
                break;
            
            
            //NATURE
            
            case "FREE_DOCUMENTARY":
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap,
                        "\\s*[\\-|]\\s*(?:FD|FREE\\sDOCUMENTARY)\\s*(?:NATURE)?$");
                break;
            
            
            //DOCUMENTARY
            
            case "DW_DOCUMENTARY":
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap, List.of(
                        "\\[4K]",
                        "\\s*[\\-|]?\\s*[\\[(][^)\\]]*DOCUMENTARY[)\\]]",
                        "\\s*[\\-|]?\\s*(?:(?:FULL|FREE|DW)\\s*)+(?:[^-)\\]]+\\s*)?(?:DOCUMENTARY|DOCUMENTAL|ENGLISH)"));
                break;
            
            case "CNBC":
            case "CNBC_ORIGINALS":
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap, List.of(
                        "\\[4K]",
                        "\\s*[\\-|]\\s*CNBC\\s*(?:DOCUMENTARY|AFTER\\s?HOURS|MARATHON)?$",
                        "^CNBC\\s*(?:DOCUMENTARY|AFTER\\s?HOURS|MARATHON)?\\s*[\\-|]\\s*"));
                break;
            
            case "FRONTLINE_PBS":
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap, List.of(
                        "\\[4K]",
                        "\\s*[\\-|]?\\s*@ASSOCIATED\\s+PRESS",
                        "\\s*[\\-|]?\\s*#ASKFRONTLINE",
                        "\\s*-\\s*FRONTLINE(?:\\sPBS)?(?:\\s(?:DOCUMENTARY|EXPLAINS))?",
                        "\\s+\\((?:(?:FULL|FREE)\\s+)*DOCUMENTARY\\)",
                        "\\s+\\(.*CAPTIONS\\sAVAILABLE.*\\)"));
                RenameProcess.regexReplaceIgnoreCase(channel, videoMap, List.of(
                        Map.entry("\\s+(?:-\\s*|\\()INTERVIEW(?:\\)|$)", " - Interview"),
                        Map.entry("\\s+(?:-\\s*|\\()TRAILER(?:\\)|$)", " - Trailer"),
                        Map.entry("\\s+(?:-\\s*|\\()PODCAST(?:\\)|$)", " - Podcast")));
                break;
            
            case "SPARK_DOCUMENTARY":
                RenameProcess.replace(channel, videoMap,
                        " l ", " | ");
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap, List.of(
                        "\\[4K]",
                        "\\s*[\\-|]?\\s*[\\[(][^)\\]]*DOCUMENTARY[)\\]]",
                        "\\s*-\\s*SPARK(?:\\s(?:DOCUMENTARY|EXPLAINS))?",
                        "\\s+\\((?:(?:FULL|FREE)\\s+)*DOCUMENTARY\\)"));
                break;
            
            case "ENDEVR_DOCUMENTARY":
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap, List.of(
                        "\\[4K]",
                        "\\s*[\\-|]?\\s*[\\[(][^)\\]]*DOCUMENTARY[)\\]]",
                        "\\s*-\\s*ENDE?VR(?:\\s(?:DOCUMENTARY|EXPLAINS))?",
                        "\\s+\\((?:(?:FULL|FREE)\\s+)*DOCUMENTARY\\)"));
                break;
            
            
            //RUNESCAPE
            
            case "BY_RELEASE":
                RenameProcess.formatIgnoreCase(channel, videoMap,
                        "^(?<title>.+?)(?:\\s*-\\s*BY\\sRELEASE\\s*-?\\s#?(?<episode>\\d*))?$",
                        "By Release - $i - $title");
                break;
            
            case "OSRS_CHALLENGES_TANZOO":
            case "OSRS_CHALLENGES_VIRTOSO":
                RenameProcess.regexReplaceIgnoreCase(channel, videoMap, List.of(
                        Map.entry("TANZOO\\s*VS?\\.?\\s*VIRTOSO\\s*-", ""),
                        Map.entry("\\s*-?\\sRUNESCAPE\\s2007\\s*-?", ""),
                        Map.entry("-\\s*CHALLENGE\\s*EPISODES?", "- Episode"),
                        Map.entry("(?:\\s*-\\s*)?OSRS\\s*CHALLENGES?\\s*-?", ""),
                        Map.entry("\\s*SPECIAL\\s*$", ""),
                        Map.entry("(?:\\s*-\\s*)?\\((?:TANZOO|VIRTOSO)\\)", ""),
                        Map.entry("\\s*-*\\s*EP(?:ISODE|\\.)?\\s*(?<episode>\\d+)\\s*$", ""),
                        Map.entry("^\\s*", "OSRS Challenges - "),
                        Map.entry("\\s*$", (" (" + channel.getConfig().getName().replace("OsrsChallenges", "") + ")"))));
                break;
            
            case "MUDKIP_HCIM":
                RenameProcess.formatIgnoreCase(channel, videoMap, false,
                        "^(?:\\[OSRS]\\s*)?(?:MAXED\\sHCIM\\s)?(?<title>.+?)\\s(?:MAXED\\sHCIM\\s)?(?:-\\s|\\(|)[#\\-]\\s?(?<episode>\\d*\\.?\\d+)\\)?$",
                        "Maxed HCIM - $episode - $title");
                RenameProcess.formatIgnoreCase(channel, videoMap, false,
                        "^(?:\\[OSRS]\\s*)?(?:HCIM\\s)?#?(?<episode>\\d+)\\s*-\\s*(?<title>.+?)$",
                        "HCIM - $episode - $title");
                RenameProcess.formatIgnoreCase(channel, videoMap, false,
                        "^(?:\\[OSRS]\\s*)?(?<title>.+?)\\s(?:-\\s|\\(|)HCIM\\s*(?:EP(?:ISODE|\\.)?\\s*)?#?(?<episode>\\d*\\.?\\d+)\\)?\\s*(?<level>(?:\\(\\d+-\\d+\\))?)$",
                        "HCIM - $episode - $title $level");
                break;
            
            case "MUDKIP_UIM":
                RenameProcess.formatIgnoreCase(channel, videoMap, false,
                        "^(?<title>.+?)\\s\\((?:UIM\\s)?[#\\-]\\s*(?<episode>\\d+)\\)$",
                        "UIM - $episode - $title");
                break;
            
            case "SWAMPLETICS":
                RenameProcess.formatIgnoreCase(channel, videoMap,
                        "^(?<title>.+?)\\s\\((?:SWAMPLETICS\\s?)?(?:#|-\\s)(?<episode>\\d+)\\)$",
                        "Swampletics - $i - $title");
                break;
            
            case "TILEMAN":
                RenameProcess.formatIgnoreCase(channel, videoMap,
                        "^(?<title>.+?)\\s(?:[|\\-]\\s)?(?:TILEMAN\\s?)?[#\\-]\\s*(?<episode>\\d+)$",
                        "Tileman - $i - $title");
                break;
            
            case "LOWER_THE_BETTER":
                RenameProcess.formatIgnoreCase(channel, videoMap,
                        "^(?<title>.+?)(?:\\s?[:\\-]\\s?LOWER\\sTHE\\sBETTER\\s?(?:EP(?:ISODE|\\.)?\\s*)?(?:[#\\-]\\s?)(?<episode>\\d+))?$",
                        "Lower the Better - $i - $title");
                break;
            
            case "OSRS_WEEKLY_RECAP":
                RenameProcess.formatIgnoreCase(channel, videoMap,
                        "^(?<title>.+?)!*\\s*(?:[#\\-]\\s*(?:\\d+\\s*\\-\\s*)?)?(?:(?:OSRS\\s)?WEEKLY\\sRECAP[\\s\\d\\-#!]*)?(?:\\[OSRS])?$",
                        "Weekly Recap - $d - $title");
                break;
            
            case "IRON_MAIN":
                RenameProcess.formatIgnoreCase(channel, videoMap,
                        "^(?<title>.+?)(\\s*[|\\-]\\s*(?:IRONMAIN\\s*)?[(\\[][#\\-]\\s*(?<episode>\\d+)[)\\]])?$",
                        "IronMain - $i - $title");
                break;
            
            case "ONE_KICK_RICK":
                RenameProcess.replaceIgnoreCase(channel, videoMap,
                        "SERIES TRAILER", "ep.0");
                RenameProcess.formatIgnoreCase(channel, videoMap,
                        "^(?<title>.+?)\\s*-\\s*(?:LUMBRIDGE\\s*-\\s*DRAYNOR\\s(?:ONLY\\s)?HCIM\\s-\\s)?(?:ONE\\sKICK\\sRICK\\s-\\s)?EP(?:ISODE|\\.)?\\s*(?<episode>\\d+)$",
                        "One Kick Rick - $episode - $title");
                break;
            
            
            //D&D
            
            case "BARDIFY":
            case "BARDIFY_EVENTS_AND_SITUATIONS":
            case "BARDIFY_CITIES_AND_VILLAGES":
            case "BARDIFY_DUNGEONS_AND_CRYPTS":
            case "BARDIFY_TRAVEL":
            case "BARDIFY_COMBAT":
            case "BARDIFY_TAVERN":
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap,
                        "\\s*[\\-|]\\s*(?:RPG\\s*[\\-|/])?\\s*D[N&]D.*\\sMUSIC\\s*(?:[\\-|]\\s*\\d+\\sHOURS?\\s*)?");
                break;
            
            
            //PAD
            
            case "SGT_502":
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap,
                        "^\\[PAD\\]\\s*(?:[\\-|:]\\s*)?");
                break;
            
            
            //MUSIC
            
            case "THE_COMET_IS_COMING":
            case "THE_COMET_IS_COMING_2015_PROPHECY_EP":
            case "THE_COMET_IS_COMING_2016_CHANNEL_THE_SPIRITS":
            case "THE_COMET_IS_COMING_2017_DEATH_TO_THE_PLANET_EP":
            case "THE_COMET_IS_COMING_2019_THE_AFTERLIFE_EP":
            case "THE_COMET_IS_COMING_2019_TRUST_IN_THE_LIFEFORCE_OF_THE_DEEP_MYSTERY":
            case "THE_COMET_IS_COMING_2022_HYPERDIMENSIONAL_EXPANSION_BEAM":
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap, List.of(
                        "^THE\\s+COMET\\s+IS\\s+COMING\\s*-\\s*",
                        "\\((?:AUDIO|VISUALIZER)\\)"));
                BaseProcess.rename(channel, videoMap, (id, video) ->
                        Arrays.stream(video.getTitle().split("\\s+(?=\\()", -1))
                                .map(e -> (!e.startsWith("(") && e.equals(e.toUpperCase())) ? StringUtility.toTitleCase(e.toLowerCase()) : e)
                                .collect(Collectors.joining(" ")));
                break;
            
            case "NEONI":
            case "NEONI_2021_WARS_IN_A_WONDERLAND":
            case "NEONI_2021_MACHINE":
            case "NEONI_2022_ALL_MY_FAVORITE_MONSTERS":
                RenameProcess.replaceIgnoreCase(channel, videoMap,
                        "Neoni - HOOLIGAN (Official Lyric Video)", ("UUID-" + UUID.randomUUID()));
                RenameProcess.formatIgnoreCase(channel, videoMap, false,
                        "^NEONI\\s+X\\s+(?<feat>.*)\\s+-\\s+(?<title>.*)\\s*$",
                        "$title (feat. $feat)");
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap, List.of(
                        "^NEONI\\s*-\\s*",
                        "\\(feat\\.\\s*\\)",
                        "\\(\\s*(?:OFFICIAL)?\\s*(?:MUSIC|LYRICS?|LIVE)?\\s*VIDEOS?\\s*\\)",
                        "-\\s+ANIMATION.+$"));
                RenameProcess.regexReplaceIgnoreCase(channel, videoMap,
                        "\\(FEAT\\.\\s*(.+?)\\sX\\s(.+?)\\)", "(feat. $1 & $2)");
                BaseProcess.rename(channel, videoMap, (id, video) ->
                        Arrays.stream(video.getTitle().split("\\s+(?=\\()", -1))
                                .map(e -> (!e.startsWith("(") && e.equals(e.toUpperCase())) ? StringUtility.toTitleCase(e.toLowerCase()) : e)
                                .collect(Collectors.joining(" ")));
                break;
            
            case "MR_KITTY":
            case "MR_KITTY_2011_DEATH":
            case "MR_KITTY_2012_ETERNITY":
            case "MR_KITTY_2014_TIME":
            case "MR_KITTY_2015_FRAGMENTS":
            case "MR_KITTY_2017_AI":
            case "MR_KITTY_2019_EPHEMERAL":
            case "MR_KITTY_2020_EP":
                RenameProcess.regexReplaceIgnoreCase(channel, videoMap,
                        "\\(FEAT\\.\\s*\\+\\s*\\)\\s*\\[(.+?)\\]", "(feat. $1)");
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap, List.of(
                        "^MR\\.?\\s?KITTY\\s*-\\s*",
                        "\\(feat\\.\\s*\\+?\\s*\\)",
                        "\\(\\s*(?:OFFICIAL)?\\s*(?:MUSIC|LYRICS?|LIVE)?\\s*VIDEOS?\\s*\\)"));
                BaseProcess.rename(channel, videoMap, (id, video) ->
                        Arrays.stream(video.getTitle().split("\\s+(?=\\()", -1))
                                .map(e -> (!e.startsWith("(") && !e.matches("(?i)XI+") && e.equals(e.toUpperCase())) ?
                                          StringUtility.toTitleCase(e.toLowerCase()) : e)
                                .collect(Collectors.joining(" ")));
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
            case "MUSIC_LAB_WAVE":
            case "AMBIENT_MUSIC_LAB":
            case "AMBIENT_MUSIC_LAB_STUDY_WORK_AMBIENT":
            case "AMBIENT_MUSIC_LAB_MEDITATION_AMBIENT":
            case "AMBIENT_MUSIC_LAB_DRONE":
            case "AMBIENT_MUSIC_LAB_SPACE_AMBIENT":
            case "AMBIENT_MUSIC_LAB_NATURE_AMBIENT":
            case "AMBIENT_MUSIC_LAB_MOVIES_AMBIENT":
            case "AMBIENT_MUSIC_LAB_DARK_AMBIENT":
            case "AMBIENT_MUSIC_LAB_AMBIENT_MUSIC":
            case "DREAMHOP_MUSIC":
            case "LITTLE_SOUL":
                RenameProcess.appendUploadDate(channel, videoMap);
                break;
            
            
            //NIGHTCORE
            
            case "KURUMI":
            case "NEZUKO":
            case "ASUNA":
            case "ARIA":
            case "ARIA_NIGHTCORE":
                RenameProcess.appendUploadDate(channel, videoMap);
                break;
            
            
            //DARKSYNTH
            
            case "JIMTV_PROGRAMMING":
                RenameProcess.replaceIgnoreCase(channel, videoMap, List.of(
                        Map.entry("PROGRAMMING - CODING - HACKING MUSIC VOL.", "Volume "),
                        Map.entry(" (", " - "),
                        Map.entry(")", "")));
                break;
            
            case "AIM_TO_HEAD":
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap,
                        "\\[(?:COPYRIGHT\\s)?(?:FREE|SOLD)]\\s*");
                break;
            
            
            //AUDIOBOOK
            
            case "GREATEST_AUDIOBOOKS":
                RenameProcess.regexReplaceIgnoreCase(channel, videoMap, List.of(
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
                BaseProcess.rename(channel, videoMap, (id, video) ->
                        Arrays.stream(video.getTitle().split("\\s+-\\s+", -1))
                                .map(e -> e.equals(e.toUpperCase()) ? StringUtility.toTitleCase(e.toLowerCase()) : e)
                                .collect(Collectors.joining(" - ")));
                break;
            
            
            //SOUNDBYTE
            
            case "SOUND_LIBRARY":
                RenameProcess.regexRemoveIgnoreCase(channel, videoMap,
                        "\\s*-\\s*(?:SOUND\\sEFFECTS?|MUSIC)\\sFOR\\sEDITING");
                break;
        }
    }
    
    /**
     * Performs special checks specific to a Channel after producing the queue.<br/>
     * Typically used for filtering; Do not use this to rename Videos in the Video map.
     *
     * @param channel  The Channel.
     * @param videoMap The Video map.
     */
    public static void performSpecialPostConditions(Channel channel, Map<String, Video> videoMap) {
        switch (channel.getConfig().getKey().replaceAll("_[PS]\\d+$", "")) {
            
            //GENERAL
            
            case "ANSWERS_WITH_JOE":
                FilterProcess.containsIgnoreCase(channel, videoMap,
                        "LIVE STREAM");
                break;
            
            case "THOUGHTY2_NEW_INTRO_ONLY":
                FilterProcess.dateBefore(channel, videoMap,
                        LocalDate.of(2018, Month.JUNE, 8));
                break;
            
            case "VSAUCE":
                FilterProcess.containsIgnoreCase(channel, videoMap, List.of(
                        "#",
                        "- SHORTS",
                        "LUT -",
                        "IMG! -",
                        "DONG",
                        "MIND FIELD"));
                FilterProcess.dateBefore(channel, videoMap,
                        LocalDate.of(2011, Month.OCTOBER, 15));
                break;
            
            case "DOMAIN_OF_SCIENCE":
                FilterProcess.dateBefore(channel, videoMap,
                        LocalDate.of(2016, Month.NOVEMBER, 27));
                break;
            
            
            //SPACE
            
            case "ISAAC_ARTHUR":
                FilterProcess.regexContainsIgnoreCase(channel, videoMap, List.of(
                        "LIVE\\s?STREAM",
                        "COLL?AB",
                        "PATREON",
                        "\\s*DIY\\s*",
                        "HADES",
                        "IN\\sTHE\\sBEGINNING"));
                break;
            
            case "PBS_SPACE_TIME_MATT_ONLY":
                FilterProcess.dateBefore(channel, videoMap,
                        LocalDate.of(2015, Month.SEPTEMBER, 1));
                break;
            
            
            //PHYSICS
            
            case "UP_AND_ATOM":
                FilterProcess.regexContainsIgnoreCase(channel, videoMap, List.of(
                        "LIVE\\s?STREAM",
                        "MERCHANDISE"));
                break;
            
            
            //ENGINEERING
            
            case "ADAM_SAVAGE_ONE_DAY_BUILDS":
                FilterProcess.notContainsIgnoreCase(channel, videoMap,
                        "ONE DAY BUILD");
                FilterProcess.containsIgnoreCase(channel, videoMap,
                        "LAST CALL");
                break;
            
            
            //COMPUTING
            
            case "NEAT_AI":
                FilterProcess.containsIgnoreCase(channel, videoMap,
                        "#SHORT");
                break;
            
            
            //CHEMISTRY
            
            case "NILE_BLUE":
                FilterProcess.containsIgnoreCase(channel, videoMap,
                        "ANNOUNCEMENT");
                break;
            
            
            //MEDICINE
            
            case "CHUBBYEMU":
                FilterProcess.dateBefore(channel, videoMap,
                        LocalDate.of(2017, Month.AUGUST, 7));
                break;
            
            case "LIKE_YOU":
                FilterProcess.containsIgnoreCase(channel, videoMap, List.of(
                        "PHOTOGRAPHER",
                        "PHONE",
                        "FRIEND",
                        "MY SON",
                        "DOGS"));
                break;
            
            
            //DOCUMENTARY
            
            case "ENDEVR_DOCUMENTARY":
                FilterProcess.containsIgnoreCase(channel, videoMap,
                        "DW DOCUMENTARY");
                break;
            
            case "PHILON":
                FilterProcess.dateBefore(channel, videoMap,
                        LocalDate.of(2022, Month.APRIL, 6));
                break;
            
            
            //RUNESCAPE
            
            case "OSRS_BEATZ":
                FilterProcess.notContainsIgnoreCase(channel, videoMap,
                        "RUNESCAPE");
                break;
            
            case "OSRS_WEEKLY_RECAP":
                FilterProcess.notContainsIgnoreCase(channel, videoMap,
                        "WEEKLY RECAP");
                break;
            
            case "OSRS_MARKET_ANALYSIS":
                FilterProcess.notContainsIgnoreCase(channel, videoMap, List.of(
                        "MARKET",
                        "ECONOMY"));
                break;
            
            
            //PAD
            
            case "SGT_502":
                FilterProcess.regexContainsIgnoreCase(channel, videoMap,
                        "CAT ");
                break;
            
            
            //FUNNY
            
            case "KITBOGA_UNCUT":
                FilterProcess.regexContainsIgnoreCase(channel, videoMap,
                        "LIVE\\s?STREAM");
                break;
            
            case "WIZARDS_WITH_GUNS_SKITS":
                FilterProcess.containsIgnoreCase(channel, videoMap,
                        "WIZARDS WATCH");
                break;
            
            case "WIZARDS_WITH_GUNS_WIZARDS_WATCH":
                FilterProcess.notContainsIgnoreCase(channel, videoMap,
                        "WIZARDS WATCH");
                break;
            
            
            //CUBE
            
            case "BEST_CUBE_COUBOY":
            case "BEST_CUBE_SPARTA":
                FilterProcess.regexNotContainsIgnoreCase(channel, videoMap,
                        "BEST\\sC(?:UBE|OUB)");
                break;
            
            case "SEXY_CUBE":
                FilterProcess.regexNotContainsIgnoreCase(channel, videoMap,
                        "SEXY\\sC(?:UBE|OUB)");
                break;
            
            
            //MUSIC
            
            case "NEONI":
            case "NEONI_2021_WARS_IN_A_WONDERLAND":
            case "NEONI_2021_MACHINE":
            case "NEONI_2022_ALL_MY_FAVORITE_MONSTERS":
                FilterProcess.startsWithIgnoreCase(channel, videoMap,
                        "UUID-");
                FilterProcess.containsIgnoreCase(channel, videoMap, List.of(
                        "#",
                        "PREVIEW",
                        "SNEAK PEEK",
                        "UNRELEASED MUSIC",
                        "CAR REEL",
                        "RECAP"));
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
            case "MUSIC_LAB_WAVE":
            case "AMBIENT_MUSIC_LAB":
            case "AMBIENT_MUSIC_LAB_STUDY_WORK_AMBIENT":
            case "AMBIENT_MUSIC_LAB_MEDITATION_AMBIENT":
            case "AMBIENT_MUSIC_LAB_DRONE":
            case "AMBIENT_MUSIC_LAB_SPACE_AMBIENT":
            case "AMBIENT_MUSIC_LAB_NATURE_AMBIENT":
            case "AMBIENT_MUSIC_LAB_MOVIES_AMBIENT":
            case "AMBIENT_MUSIC_LAB_DARK_AMBIENT":
            case "AMBIENT_MUSIC_LAB_AMBIENT_MUSIC":
                FilterProcess.regexContainsIgnoreCase(channel, videoMap,
                        "LIVE\\s24[\\-|/]7");
                break;
            
            
            //POP
            
            case "MR_MOM_MUSIC_NEW":
                FilterProcess.dateBefore(channel, videoMap,
                        LocalDate.of(2020, Month.JANUARY, 24));
                break;
            
            case "CLOUD_KID_TOP_50":
            case "CLOUD_KID_GAMING_MUSIC":
            case "CLOUD_KID_BADASS_VILLAIN_VIBES":
            case "CLOUD_KID_ROCKTRONIC":
                channel.getState().getBlocked().addAll(List.of(
                        "dL87mBMKLn4",
                        "RpkLAdp5iH8",
                        "WCpo3RFdPUE",
                        "8vUc3fiouMo"));
                break;
            
            
            //PHONK
            
            case "MIX_SNAKE":
                FilterProcess.notContainsIgnoreCase(channel, videoMap,
                        "PHONK");
                break;
            
            
            //PSYTRANCE
            
            case "SPEEDSOUND":
                channel.getState().getBlocked().add("FhOSu5fq5eE");
                break;
            
            
            //AUDIOBOOK
            
            case "GREATEST_AUDIOBOOKS":
                FilterProcess.containsIgnoreCase(channel, videoMap, List.of(
                        "BOOK REVIEW",
                        "AUTHOR INTERVIEW",
                        "PREVIEW",
                        "EXCERPT"));
                break;
        }
    }
    
}
