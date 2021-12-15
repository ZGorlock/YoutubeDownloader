/*
 * File:    ChannelProcesses_Sample.java
 * Package: youtube.channel
 * Author:  Zachary Gill
 */

package youtube.channel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.string.StringUtility;
import org.apache.commons.io.FileUtils;
import youtube.YoutubeChannelDownloader;
import youtube.tools.YoutubeUtils;

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
    @SuppressWarnings({"StatementWithEmptyBody", "DuplicateBranchesInSwitch"})
    public static void performSpecialPreConditions(Channel channel, Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> queue, List<String> save, List<String> blocked) throws Exception {
        switch (channel.key) {
            
            case "JIMTV_PROGRAMMING":
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle
                            .replace("Programming - Coding - Hacking music vol.", "Volume ")
                            .replace(" (", " - ")
                            .replace(")", "");
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            
            case "BY_RELEASE":
                Pattern byReleaseNamePattern = Pattern.compile("^(?<title>.+)\\s*-\\s*By\\sRelease\\s*-?-\\s(?<episode>\\d+)$");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    Matcher byReleaseNameMatcher = byReleaseNamePattern.matcher(oldTitle);
                    if (byReleaseNameMatcher.matches()) {
                        String newTitle = "By Release - " + byReleaseNameMatcher.group("episode") + " - " + byReleaseNameMatcher.group("title");
                        newTitle = YoutubeUtils.cleanTitle(newTitle);
                        File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                        if (value.output.exists() && !oldTitle.equals(newTitle)) {
                            try {
                                FileUtils.moveFile(value.output, newOutput);
                            } catch (IOException ignored) {
                            }
                        }
                        value.output = newOutput;
                        value.title = newTitle;
                    }
                });
                break;
            case "OSRS_CHALLENGES_TANZOO":
                Pattern tanzooPattern = Pattern.compile("^(?<title>.+)\\s*-*\\s*Episode\\s*(?<episode>\\d+)$");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle
                            .replaceAll("Tanzoo\\s*vs?\\.?\\s*Virtoso\\s*-", "")
                            .replaceAll("-\\s*Challenge\\s*Episodes?", "- Episode")
                            .replaceAll("(?:OSRS|Osrs|osrs)\\s*Challenges?\\s*-?", "")
                            .replaceAll("\\s+", " ")
                            .replaceAll("(^\\s*-\\s*)|(\\s*-\\s*$)", "")
                            .replaceAll("\\s*Special\\s*$", "")
                            .replaceAll("(^\\s*-\\s*)|(\\s*-\\s*$)", "")
                            .replaceAll("\\s+", " ")
                            .trim();
                    Matcher tanzooMatcher = tanzooPattern.matcher(newTitle);
                    if (tanzooMatcher.matches()) {
                        newTitle = tanzooMatcher.group("title").replaceAll("\\s*-\\s*$", "").trim();
                    }
                    newTitle = "OSRS Challenges - " + newTitle + " (Tanzoo)";
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case "OSRS_CHALLENGES_VIRTOSO":
                Pattern virtosoPattern = Pattern.compile("^(?<title>.+)\\s*-*\\s*(Episode|EP\\.|Ep\\.)\\s*(?<episode>\\d+)$");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle
                            .replaceAll("Tanzoo\\s*vs?\\.?\\s*Virtoso\\s*-", "")
                            .replaceAll("\\s*-?\\sRunescape\\s2007\\s*-?", "")
                            .replaceAll("-\\s*Challenge\\s*Episodes?", "- Episode")
                            .replaceAll("(?:OSRS|Osrs|osrs)\\s*Challenges?\\s*-?", "")
                            .replaceAll("^\\s*-\\s*", "")
                            .replaceAll("\\s+", " ")
                            .replaceAll("(^\\s*-\\s*)|(\\s*-\\s*$)", "")
                            .replaceAll("\\s*Special\\s*$", "")
                            .replaceAll("(^\\s*-\\s*)|(\\s*-\\s*$)", "")
                            .replaceAll("\\s+", " ")
                            .trim();
                    Matcher virtosoMatcher = virtosoPattern.matcher(newTitle);
                    if (virtosoMatcher.matches()) {
                        newTitle = virtosoMatcher.group("title").replaceAll("\\s*-\\s*$", "").trim();
                    }
                    newTitle = "OSRS Challenges - " + newTitle + " (Virtoso)";
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case "MUDKIP_HCIM":
                Pattern hcimPattern1 = Pattern.compile("^(HCIM\\s(?<episode>\\d+)-).*");
                Pattern hcimPattern2 = Pattern.compile("^(?<title>.+)\\s(?:-\\s|\\(|)HCIM\\s*(?:Episode|ep\\.|Ep\\.|)\\s*(?<episode>\\d*\\.?\\d+)\\)?\\s*(?<level>\\(\\d+-\\d+\\))?");
                Pattern hcimPattern3 = Pattern.compile("^(?<title>.+)\\s(?:-\\s|\\(|)-\\s(?<episode>\\d*\\.?\\d+)\\)?");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle.replace("[OSRS] ", "");
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    oldTitle = newTitle;
                    if (newTitle.contains("Maxed HCIM")) {
                        newTitle = newTitle.replace("Maxed HCIM ", "");
                        Matcher hcimMatcher3 = hcimPattern3.matcher(newTitle);
                        if (hcimMatcher3.matches()) {
                            newTitle = "Maxed HCIM - " + hcimMatcher3.group("episode") + " - " + hcimMatcher3.group("title");
                        } else {
                            newTitle = newTitle.replaceAll("^Maxed HCIM ", "Maxed HCIM - ");
                        }
                    } else {
                        Matcher hcimMatcher1 = hcimPattern1.matcher(newTitle);
                        if (hcimMatcher1.matches()) {
                            newTitle = newTitle.replace(hcimMatcher1.group(1), hcimMatcher1.group(1).replace("-", " -"));
                        } else {
                            Matcher hcimMatcher2 = hcimPattern2.matcher(newTitle);
                            if (hcimMatcher2.matches()) {
                                newTitle = "HCIM - " + hcimMatcher2.group("episode") + " - " + hcimMatcher2.group("title").trim() +
                                        (hcimMatcher2.group("level") == null ? "" : (" " + hcimMatcher2.group("level")));
                            }
                        }
                        newTitle = newTitle.replaceAll("^HCIM ", "HCIM - ");
                    }
                    newTitle = newTitle
                            .replace("##", "#")
                            .replace("#", "- ")
                            .replace("()", "")
                            .replace(" - (", " (")
                            .replace(" - - ", " - ");
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case "MUDKIP_UIM":
                Pattern uimPattern = Pattern.compile(".*?(\\s(?:\\(UIM\\s-\\s|\\(-\\s)(?<episode>\\d+)\\)).*");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    Matcher uimMatcher = uimPattern.matcher(newTitle);
                    if (uimMatcher.matches()) {
                        newTitle = "UIM - " + uimMatcher.group("episode") + " - " + newTitle.replace(uimMatcher.group(1), "");
                    } else {
                        newTitle = newTitle.replaceAll("^UIM ", "UIM - ");
                    }
                    newTitle = newTitle
                            .replace("##", "#")
                            .replace("#", "- ")
                            .replace(" - - ", " - ");
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case "SWAMPLETICS":
                Pattern swampleticsPattern = Pattern.compile(".*?(\\s(?:\\(Swampletics\\s-\\s|\\(-\\s)(?<episode>\\d+)\\)).*");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    Matcher swampleticsMatcher = swampleticsPattern.matcher(newTitle);
                    if (swampleticsMatcher.matches()) {
                        newTitle = "Swampletics - " + swampleticsMatcher.group("episode") + " - " + newTitle.replace(swampleticsMatcher.group(1), "");
                    }
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case "LOWER_THE_BETTER":
                Pattern lowerTheBetterPattern = Pattern.compile(".*(\\s*-\\s*Lower\\s[Tt]he\\sBetter\\s(?:Ep\\.\\s)?-\\s\\s?(?<episode>\\d+)).*");
                AtomicInteger lowerTheBetterCount = new AtomicInteger(0);
                videoMap.forEach((key, value) -> {
                    lowerTheBetterCount.incrementAndGet();
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    if (!newTitle.toLowerCase().contains("lower the better")) {
                        newTitle += " - Lower the Better - " + lowerTheBetterCount.get();
                    }
                    Matcher lowerTheBetterMatcher = lowerTheBetterPattern.matcher(newTitle);
                    if (lowerTheBetterMatcher.matches()) {
                        newTitle = "Lower the Better - " + lowerTheBetterMatcher.group("episode") + " - " + newTitle.replace(lowerTheBetterMatcher.group(1), "");
                    }
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case "OSRS_WEEKLY_RECAP":
                Pattern osrsWeeklyRecapPattern = Pattern.compile(".*?(\\s*-*\\s*(-\\s\\d+\\s*-*\\s*)?(OSRS\\s)?Weekly\\sRecap[\\s\\d\\-!]*)");
                SimpleDateFormat osrsWeeklyRecapDate = new SimpleDateFormat("yyyy-MM-dd");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle.replaceAll("\\s*\\[OSRS]", "");
                    Matcher osrsWeeklyRecapMatcher = osrsWeeklyRecapPattern.matcher(newTitle);
                    if (osrsWeeklyRecapMatcher.matches()) {
                        newTitle = "Weekly Recap - " + osrsWeeklyRecapDate.format(value.date) + " - " + newTitle.replace(osrsWeeklyRecapMatcher.group(1), "");
                    }
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case "IRON_MAIN":
                Pattern ironMainPattern = Pattern.compile(".*?(\\s*-\\s*(?:IronMain\\s)?\\[-\\s\\s*(?<episode>\\d+)]).*");
                AtomicInteger ironMainCount = new AtomicInteger(0);
                videoMap.forEach((key, value) -> {
                    ironMainCount.incrementAndGet();
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    Matcher ironMainMatcher = ironMainPattern.matcher(newTitle);
                    if (ironMainMatcher.matches()) {
                        newTitle = newTitle.replace(ironMainMatcher.group(1), "");
                    }
                    newTitle = "IronMain - " + ironMainCount.get() + " - " + newTitle;
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case "ONE_KICK_RICK":
                Pattern oneKickRickPattern = Pattern.compile(".*(\\s*-\\s*Lumbridge-Draynor\\s(?:Only\\s)?HCIM\\s-\\s(?:One\\sKick\\sRick\\s-\\s)?(?:Episode|Ep\\.|ep\\.)\\s*(?:-\\s)?(?<episode>\\d+)).*");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    if (newTitle.contains("Series Trailer")) {
                        newTitle = newTitle.replace("Series Trailer", "One Kick Rick - ep.0");
                    }
                    Matcher oneKickRickMatcher = oneKickRickPattern.matcher(newTitle);
                    if (oneKickRickMatcher.matches()) {
                        newTitle = "One Kick Rick - " + oneKickRickMatcher.group("episode") + " - " + newTitle.replace(oneKickRickMatcher.group(1), "");
                    }
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            
            case "STEVE_MOULD":
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("fewer than tom") ||
                            value.title.toLowerCase().contains("more than tom")) {
                        String oldTitle = value.title;
                        String newTitle = "This Video Has...";
                        newTitle = YoutubeUtils.cleanTitle(newTitle);
                        File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                        if (value.output.exists() && !oldTitle.equals(newTitle)) {
                            try {
                                FileUtils.moveFile(value.output, newOutput);
                            } catch (IOException ignored) {
                            }
                        }
                        value.output = newOutput;
                        value.title = newTitle;
                    }
                });
                break;
            case "MIND_FIELD_S1":
                Pattern mindFieldS1Pattern = Pattern.compile(".*?(\\s*-\\s*(?:Mind\\sField\\s)\\(Ep\\.?\\s*(?<episode>\\d+)\\)).*");
                AtomicInteger mindFieldS1Count = new AtomicInteger(0);
                videoMap.forEach((key, value) -> {
                    mindFieldS1Count.incrementAndGet();
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    Matcher mindFieldS1Matcher = mindFieldS1Pattern.matcher(newTitle);
                    if (mindFieldS1Matcher.matches()) {
                        newTitle = newTitle.replace(mindFieldS1Matcher.group(1), "");
                    }
                    newTitle = "Mind Field - S01E0" + mindFieldS1Count.get() + " - " + newTitle;
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    value.title = newTitle;
                });
                break;
            case "MIND_FIELD_S2":
                Pattern mindFieldS2Pattern = Pattern.compile(".*?(\\s*-\\s*(?:Mind\\sField\\sS2\\s)\\(Ep\\.?\\s*(?<episode>\\d+)\\)).*");
                AtomicInteger mindFieldS2Count = new AtomicInteger(0);
                videoMap.forEach((key, value) -> {
                    mindFieldS2Count.incrementAndGet();
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    Matcher mindFieldS2Matcher = mindFieldS2Pattern.matcher(newTitle);
                    if (mindFieldS2Matcher.matches()) {
                        newTitle = newTitle.replace(mindFieldS2Matcher.group(1), "");
                    }
                    newTitle = "Mind Field - S02E0" + mindFieldS2Count.get() + " - " + newTitle;
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    value.title = newTitle;
                });
                break;
            case "MIND_FIELD_S3":
                AtomicInteger mindFieldS3Count = new AtomicInteger(0);
                videoMap.forEach((key, value) -> {
                    mindFieldS3Count.incrementAndGet();
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    newTitle = "Mind Field - S03E0" + mindFieldS3Count.get() + " - " + newTitle;
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    value.title = newTitle;
                });
                break;
            
            case "LOCK_PICKING_LAWYER":
                Pattern lockPickingLawyerPattern = Pattern.compile("^(?<episode>\\d+)\\s-\\s(?<title>.+)$");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle.replace("[", "").replace("]", " -");
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    Matcher lockPickingLawyerMatcher = lockPickingLawyerPattern.matcher(newTitle);
                    if (lockPickingLawyerMatcher.matches()) {
                        newTitle = StringUtility.padZero(lockPickingLawyerMatcher.group("episode"), 4) + " - " + lockPickingLawyerMatcher.group("title");
                    }
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    value.title = newTitle;
                });
                break;
            
            case "NHAT_BANG_SPA":
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle.replace("010", "10");
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    value.title = newTitle;
                });
                break;
            
            case "FORENSIC_FILES":
            case "FORENSIC_FILES_S01":
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
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle
                            .replace("( ", "(")
                            .replace(" )", ")")
                            .replace("Medical Detectives (Forensic Files)", "Forensic Files")
                            .replace(" in HD ", " ")
                            .replace("- ", " - ")
                            .replace(" , ", ", ")
                            .replace("Season ", "S")
                            .replace(", Ep ", "E")
                            .replaceAll("\\sS(\\d)E", " S0$1E")
                            .replaceAll("E(\\d)\\s*-", "E0$1 - ")
                            .replaceAll("\\s+", " ");
                    if (newTitle.equals("Forensic Files - Series Premiere - The Disappearance of Helle Crafts")) {
                        newTitle = "Forensic Files - S01E01 - The Disappearance of Helle Crafts";
                    }
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                if (channel.key.equals("FORENSIC_FILES_S01")) {
                    YoutubeChannelDownloader.Video video = new YoutubeChannelDownloader.Video();
                    video.videoId = "OZc6vcGjknI";
                    video.title = YoutubeUtils.cleanTitle("Forensic Files - S01E01 - The Disappearance of Helle Crafts");
                    video.url = YoutubeUtils.VIDEO_BASE + video.videoId;
                    video.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-01-23 12:15:00");
                    video.output = new File(channel.outputFolder, video.title + (channel.saveAsMp3 ? ".mp3" : ".mp4"));
                    HashMap<String, YoutubeChannelDownloader.Video> tmp = new LinkedHashMap<>(videoMap);
                    videoMap.clear();
                    videoMap.put(video.videoId, video);
                    videoMap.putAll(tmp);
                }
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
    @SuppressWarnings({"StatementWithEmptyBody", "DuplicateBranchesInSwitch"})
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
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("live 24-7")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case "MR_MOM_MUSIC_VIDEOS_NEW":
            case "MR_MOM_MUSIC_NEW":
                final Date mrMomMusicOldest = new SimpleDateFormat("yyyy-MM-dd").parse("2020-01-24");
                videoMap.forEach((key, value) -> {
                    if (value.date.before(mrMomMusicOldest)) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case "OSRS_BEATZ":
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("runescape")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            case "OSRS_WEEKLY_RECAP":
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("weekly recap")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            case "OSRS_MARKET_ANALYSIS":
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("market") && !value.title.toLowerCase().contains("economy")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
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
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("livestream") ||
                            value.title.toLowerCase().contains("hades") ||
                            value.title.equalsIgnoreCase("in the beginning") ||
                            value.title.toLowerCase().contains("collab") || value.title.toLowerCase().contains("colab")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            case "PBS_SPACE_TIME_MATT_ONLY":
                final Date mattOnlyOldest = new SimpleDateFormat("yyyy-MM-dd").parse("2015-09-01");
                videoMap.forEach((key, value) -> {
                    if (value.date.before(mattOnlyOldest)) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case "ANSWERS_WITH_JOE":
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("live stream")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case "VSAUCE":
                final Date vSauceOldest = new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-15");
                videoMap.forEach((key, value) -> {
                    if (value.originalTitle.contains("#") || value.title.contains("DONG") || value.title.contains("Mind Field") || value.date.before(vSauceOldest)) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case "ADAM_SAVAGE_ONE_DAY_BUILDS":
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("one day build") ||
                            value.title.toLowerCase().contains("last call")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case "CHUBBYEMU":
                final Date chubbyEmuOldest = new SimpleDateFormat("yyyy-MM-dd").parse("2017-08-07");
                videoMap.forEach((key, value) -> {
                    if (value.date.before(chubbyEmuOldest)) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            case "LIKE_YOU":
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("photographer") ||
                            value.title.toLowerCase().contains("phone") ||
                            value.title.toLowerCase().contains("friend") ||
                            value.title.toLowerCase().contains("my son") ||
                            value.title.toLowerCase().contains("dogs")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case "KITBOGA_UNCUT":
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("live stream")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case "BEST_CUBE_COUBOY":
            case "BEST_CUBE_SPARTA":
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("best cube") ||
                            value.title.toLowerCase().contains("best coub")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            case "SEXY_CUBE":
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("sexy cube") ||
                            value.title.toLowerCase().contains("sexy coub")) {
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
