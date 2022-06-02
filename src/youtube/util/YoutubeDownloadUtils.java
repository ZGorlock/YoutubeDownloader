/*
 * File:    YoutubeDownloadUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.CmdLine;
import commons.console.ProgressBar;
import commons.object.string.StringUtility;
import youtube.channel.Video;

/**
 * Provides download utility methods for the Youtube Downloader.
 */
public final class YoutubeDownloadUtils {
    
    //Enums
    
    /**
     * An enumeration of Download Responses.
     */
    public enum DownloadResponse {
        
        //Values
        
        SUCCESS,
        FAILURE,
        ERROR
        
    }
    
    
    //Constants
    
    /**
     * A regex pattern matching a 'download progress' line from the executable output.
     */
    public static final Pattern PROGRESS_PATTERN = Pattern.compile("^\\[download]\\s*(?<percentage>\\d+\\.\\d+)%\\s*of\\s*~?(?<total>\\d+\\.\\d+)(?<units>.iB).*$");
    
    /**
     * A regex pattern matching a 'resuming download' line from the executable output.
     */
    public static final Pattern RESUME_PATTERN = Pattern.compile("^\\[download]\\s*Resuming\\s*download\\s*at\\s*byte\\s*(?<initialProgress>\\d+).*$");
    
    /**
     * A regex pattern matching a 'video already exists' line from the executable output.
     */
    public static final Pattern EXISTS_PATTERN = Pattern.compile("^\\[download]\\s(?<output>.+)\\shas\\salready\\sbeen\\sdownloaded$");
    
    /**
     * A regex pattern matching a 'output destination' line from the executable output.
     */
    public static final Pattern DESTINATION_PATTERN = Pattern.compile("^\\[[^]]+]\\s*Destination:\\s*(?<destination>.+)$");
    
    /**
     * A regex pattern matching a 'merging formats' line from the executable output.
     */
    public static final Pattern MERGE_PATTERN = Pattern.compile("^\\[Merger]\\s*Merging\\s*formats\\s*into\\s*\"(?<merge>.+)\"$");
    
    /**
     * A list of error responses that are considered a failure instead of an error, so the video will not be blocked.
     */
    public static final List<String> NON_CRITICAL_ERRORS = List.of(
            "giving up after 10",
            "urlopen error",
            "sign in to",
            "please install or provide the path"
    );
    
    
    //Functions
    
    /**
     * Downloads a Youtube video.
     *
     * @param video The Video data object.
     * @return A download response indicated the result of the download attempt.
     * @throws Exception When there is an error downloading the video.
     */
    public static DownloadResponse downloadYoutubeVideo(Video video) throws Exception {
        boolean ytDlp = YoutubeUtils.EXECUTABLE.equals(YoutubeUtils.Executable.YT_DLP);
        boolean asMp3 = Optional.ofNullable(video.channel).map(e -> e.saveAsMp3).orElse(Configurator.Config.asMp3);
        SponsorBlocker.SponsorBlockConfig sponsorBlockConfig = Optional.ofNullable(video.channel).map(e -> e.sponsorBlockConfig).orElse(null);
        
        String cmd = Color.exe(YoutubeUtils.EXECUTABLE.getExe().getName()) + Color.log(" ") +
                Color.log("--output \"") + Color.file(video.download.getAbsolutePath().replace("\\", "/") + ".%(ext)s") + Color.log("\" ") +
                Color.log("--geo-bypass --rm-cache-dir ") +
                Color.log(asMp3 ? "--extract-audio --audio-format mp3 " :
                          ((ytDlp && !Configurator.Config.preMerged) ? "" : ("--format best " + (ytDlp ? "-f b " : "")))) +
                Color.log(SponsorBlocker.getCommand(sponsorBlockConfig)) +
                Color.link(video.url);
        
        if (Configurator.Config.logCommand) {
            System.out.println(YoutubeUtils.INDENT + Color.base(cmd));
        }
        
        return performDownload(StringUtility.removeConsoleEscapeCharacters(cmd), video, Configurator.Config.logWork);
    }
    
    /**
     * Executes a command line process.
     *
     * @param cmd   The command.
     * @param video The Video data object.
     * @param log   Whether to log the response from the process or not.
     * @return A download response indicated the result of the download attempt.
     */
    private static DownloadResponse performDownload(String cmd, Video video, boolean log) {
        ProgressBar progressBar = null;
        
        try {
            ProcessBuilder builder = CmdLine.buildProcess(cmd);
            builder.redirectErrorStream(true);
            
            Process process = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            long initialProgress = -1L;
            long saveProgress = 0L;
            boolean newPart = true;
            int unusedLines = 0;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                responseBuilder.append(line).append(System.lineSeparator());
                
                if (log) {
                    System.out.println(Color.log(line));
                    
                } else {
                    if (initialProgress == -1) {
                        Matcher resumeMatcher = RESUME_PATTERN.matcher(line);
                        if (resumeMatcher.matches()) {
                            initialProgress = Long.parseLong(resumeMatcher.group("initialProgress")) / 1024;
                            continue;
                        }
                    }
                    
                    Matcher progressMatcher = PROGRESS_PATTERN.matcher(line);
                    if (progressMatcher.matches()) {
                        double percentage = Double.parseDouble(progressMatcher.group("percentage")) / 100.0;
                        long total = (long) Double.parseDouble(progressMatcher.group("total"));
                        
                        String units = progressMatcher.group("units").replace("i", "");
                        switch (units) {
                            case "TB":
                                total *= 1024;
                            case "GB":
                                total *= 1024;
                            case "MB":
                                total *= 1024;
                            case "KB":
                            default:
                        }
                        
                        if (newPart) {
                            if (progressBar == null) {
                                progressBar = new ProgressBar("", total, 32, "KB", true);
                                progressBar.setIndent(StringUtility.removeConsoleEscapeCharacters(YoutubeUtils.INDENT).length());
                                initialProgress = Math.max(initialProgress, 0);
                                progressBar.defineInitialProgress(initialProgress);
                            } else {
                                progressBar.updateTotal(progressBar.getTotal() + total);
                                saveProgress = progressBar.getProgress();
                            }
                            newPart = false;
                        }
                        
                        long progress = ((long) (percentage * total)) + saveProgress;
                        progressBar.update(progress);
                        continue;
                    }
                }
                
                Matcher existsMatcher = EXISTS_PATTERN.matcher(line);
                if (existsMatcher.matches()) {
                    File output = new File(existsMatcher.group("output"));
                    if (video != null) {
                        video.output = output;
                    }
                    long size = output.length() / 1024;
                    if (progressBar == null) {
                        progressBar = new ProgressBar("", size, 32, "KB", true);
                        progressBar.setIndent(StringUtility.removeConsoleEscapeCharacters(YoutubeUtils.INDENT).length());
                        progressBar.defineInitialProgress(size);
                    }
                    progressBar.complete(false, Color.good("Already downloaded"));
                    continue;
                }
                
                Matcher destinationMatcher = DESTINATION_PATTERN.matcher(line);
                if (destinationMatcher.matches()) {
                    File destination = new File(destinationMatcher.group("destination"));
                    if (video != null) {
                        video.output = destination;
                    }
                    newPart = true;
                    continue;
                }
                
                Matcher mergeMatcher = MERGE_PATTERN.matcher(line);
                if (mergeMatcher.matches()) {
                    File merge = new File(mergeMatcher.group("merge"));
                    if (video != null) {
                        video.output = merge;
                    }
                    newPart = true;
                    continue;
                }
                
                unusedLines++;
            }
            
            process.waitFor();
            r.close();
            process.destroy();
            
            String response = responseBuilder.toString().trim();
            String error = !response.contains("ERROR: ") ?
                           ((progressBar == null) ? "ERROR: Unknown Error" : null) :
                           response.substring(response.lastIndexOf("ERROR: ")).replaceAll("\r?\n", " - ");
            
            if (progressBar == null) {
                progressBar = new ProgressBar("", 1, 32, "KB", true);
                progressBar.setIndent(StringUtility.removeConsoleEscapeCharacters(YoutubeUtils.INDENT).length());
                progressBar.update(-1);
            }
            if (!progressBar.isComplete()) {
                if (error == null) {
                    progressBar.complete();
                } else {
                    progressBar.fail(true, Color.bad(error
                            .replaceAll("^ERROR:\\s*", "")
                            .replaceAll("^\\[[^\\\\]+]\\s*[^:]+:\\s*", "")
                            .replaceAll(":\\s*<[^>]+>\\s*\\(caused\\sby.+\\)+$", "")
                            .trim()));
                }
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            return (error == null) ? DownloadResponse.SUCCESS :
                   NON_CRITICAL_ERRORS.stream().anyMatch(e -> error.toLowerCase().contains(e.toLowerCase())) ? DownloadResponse.FAILURE :
                   DownloadResponse.ERROR;
            
        } catch (
                
                Exception e) {
            if (progressBar != null) {
                progressBar.fail(true, Color.bad("Unknown Error"));
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            System.out.println(Color.bad(e.getStackTrace()));
            return DownloadResponse.ERROR;
        }
    }
    
}
