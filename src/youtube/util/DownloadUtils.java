/*
 * File:    DownloadUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.CmdLine;
import commons.console.Console;
import commons.console.ProgressBar;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.Video;
import youtube.conf.Color;
import youtube.conf.Configurator;
import youtube.conf.SponsorBlocker;

/**
 * Provides download utility methods for the Youtube Downloader.
 */
public final class DownloadUtils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);
    
    
    //Enums
    
    /**
     * An enumeration of Download Response Statuses.
     */
    public enum DownloadResponseStatus {
        
        //Values
        
        SUCCESS("Succeeded", Color.GOOD),
        FAILURE("Failed", Color.BAD),
        ERROR("Error", Color.BAD);
        
        
        //Fields
        
        /**
         * The message associated with the Status.
         */
        public final String message;
        
        /**
         * The color of the Status.
         */
        public final Console.ConsoleEffect color;
        
        
        //Constructors
        
        /**
         * Constructs a Download Response Status.
         *
         * @param message The message associated with the Status.
         */
        DownloadResponseStatus(String message, Console.ConsoleEffect color) {
            this.message = message;
            this.color = color;
        }
        
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
    public static final Pattern DESTINATION_PATTERN = Pattern.compile("^\\[download]\\s*Destination:\\s*(?<destination>.+)$");
    
    /**
     * A regex pattern matching a 'extracting audio' line from the executable output.
     */
    public static final Pattern EXTRACT_AUDIO_PATTERN = Pattern.compile("^\\[ExtractAudio]\\s*Destination:\\s*(?<audio>.+)$");
    
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
    
    /**
     * A list of error responses that will trigger a retry attempt using browser cookies, if configured.
     */
    public static final List<String> RETRY_WITH_COOKIES_ERRORS = List.of(
            "sign in to"
    );
    
    
    //Functions
    
    /**
     * Downloads a Youtube video.
     *
     * @param video The Video data object.
     * @return A download response indicating the result of the download attempt.
     * @throws Exception When there is an error downloading the video.
     */
    public static DownloadResponse downloadYoutubeVideo(Video video) throws Exception {
        return downloadYoutubeVideo(video, false);
    }
    
    /**
     * Downloads a Youtube video.
     *
     * @param video   The Video data object.
     * @param isRetry Whether this download attempt is a retry or not.
     * @return A download response indicating the result of the download attempt.
     * @throws Exception When there is an error downloading the video.
     */
    private static DownloadResponse downloadYoutubeVideo(Video video, boolean isRetry) throws Exception {
        boolean ytDlp = ExecutableUtils.EXECUTABLE.equals(ExecutableUtils.Executable.YT_DLP);
        boolean asMp3 = Optional.ofNullable(video.channel).map(e -> e.saveAsMp3).orElse(Configurator.Config.asMp3);
        SponsorBlocker.SponsorBlockConfig sponsorBlockConfig = Optional.ofNullable(video.channel).map(e -> e.sponsorBlockConfig).orElse(null);
        
        if (isRetry && (Configurator.Config.neverUseBrowserCookies ||
                (Configurator.Config.browser == null) || Configurator.Config.browser.isEmpty())) {
            return null;
        }
        
        String cmd = Color.exe(ExecutableUtils.EXECUTABLE.getCall()) + Color.log(" ") +
                Color.log("--output \"") + Color.filePath((video.download.getAbsolutePath() + ".%(ext)s"), false) + Color.log("\" ") +
                Color.log("--geo-bypass --rm-cache-dir " +
                        (isRetry ? ("--cookies-from-browser " + Configurator.Config.browser.toLowerCase() + " ") : "")) +
                Color.log(asMp3 ? "--extract-audio --audio-format mp3 " :
                          ((ytDlp && !Configurator.Config.preMerged) ? "" : ("--format best " + (ytDlp ? "-f b " : "")))) +
                Color.log(SponsorBlocker.getCommand(sponsorBlockConfig) + " ") +
                Color.link(video.url);
        
        if (Configurator.Config.logCommand) {
            System.out.println(Utils.INDENT + Color.base(cmd));
        }
        
        return performDownload(StringUtility.removeConsoleEscapeCharacters(cmd), video, isRetry);
    }
    
    /**
     * Executes a command line process.
     *
     * @param cmd   The command.
     * @param video The Video data object.
     * @return A download response indicating the result of the download attempt.
     */
    private static DownloadResponse performDownload(String cmd, Video video, boolean isRetry) {
        DownloadResponse response = new DownloadResponse();
        ProgressBar progressBar = null;
        
        final Function<Long, ProgressBar> downloadProgressBarInitializer = (Long total) -> {
            ProgressBar downloadProgressBar = new ProgressBar("", total, 32, "KB", true);
            downloadProgressBar.setIndent(StringUtility.removeConsoleEscapeCharacters(Utils.INDENT).length());
            downloadProgressBar.setColors(Color.PROGRESS_BAR_BASE, Color.PROGRESS_BAR_GOOD, Color.PROGRESS_BAR_BAD);
            return downloadProgressBar;
        };
        
        logger.debug("");
        logger.debug(StringUtility.repeatString("-", 200));
        logger.debug("");
        logger.info(cmd);
        logger.debug("");
        
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
                logger.trace(line);
                responseBuilder.append(line).append(System.lineSeparator());
                
                if (Configurator.Config.logWork) {
                    System.out.println(Color.log(line));
                    
                } else if (Configurator.Config.showProgressBar) {
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
                                progressBar = downloadProgressBarInitializer.apply(total);
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
                    
                    response.message = "Already downloaded";
                    if (Configurator.Config.showProgressBar && !Configurator.Config.logWork) {
                        if (progressBar == null) {
                            progressBar = downloadProgressBarInitializer.apply(size);
                            progressBar.defineInitialProgress(size);
                        }
                    }
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
                
                Matcher extractAudioMatcher = EXTRACT_AUDIO_PATTERN.matcher(line);
                if (extractAudioMatcher.matches()) {
                    File audio = new File(extractAudioMatcher.group("audio"));
                    if (video != null) {
                        video.output = audio;
                    }
                    if (Configurator.Config.showProgressBar && !Configurator.Config.logWork) {
                        if (progressBar == null) {
                            progressBar = downloadProgressBarInitializer.apply(1L);
                        }
                        progressBar.complete(true, Color.good("Extracting Audio..."));
                    }
                    response.message = null;
                    continue;
                }
                
                Matcher mergeMatcher = MERGE_PATTERN.matcher(line);
                if (mergeMatcher.matches()) {
                    File merge = new File(mergeMatcher.group("merge"));
                    if (video != null) {
                        video.output = merge;
                    }
                    if (Configurator.Config.showProgressBar && !Configurator.Config.logWork) {
                        if (progressBar == null) {
                            progressBar = downloadProgressBarInitializer.apply(1L);
                        }
                        progressBar.complete(true, Color.good("Merging Formats..."));
                    }
                    response.message = null;
                    continue;
                }
                
                unusedLines++;
            }
            
            process.waitFor();
            r.close();
            process.destroy();
            
            response.log = responseBuilder.toString().trim();
            response.error = (!response.log.contains("ERROR: ") && !response.log.contains(".exe: error: ")) ? null :
                             response.log.substring(Math.max(response.log.lastIndexOf("ERROR: "), response.log.lastIndexOf(".exe: error: ")))
                                     .replaceAll("\r?\n", " - ").replaceAll("^\\.exe:\\s*", "");
            response.message = (response.error == null) ? response.message :
                               response.error
                                       .replaceAll("(?i)^ERROR:\\s*(?:-\\s*)?", "")
                                       .replaceAll("^\\[[^\\\\]+]\\s*[^:]+:\\s*", "")
                                       .replaceAll(":\\s*<[^>]+>\\s*\\(caused\\sby.+\\)+$", "")
                                       .trim();
            response.status = (response.error == null) ? DownloadResponseStatus.SUCCESS :
                              NON_CRITICAL_ERRORS.stream().anyMatch(e -> response.error.toLowerCase().contains(e.toLowerCase())) ?
                              DownloadResponseStatus.FAILURE : DownloadResponseStatus.ERROR;
            
            if (Configurator.Config.showProgressBar && !Configurator.Config.logWork) {
                if (progressBar == null) {
                    progressBar = downloadProgressBarInitializer.apply(1L);
                    progressBar.update(-1);
                }
                if (!progressBar.isCompleted()) {
                    if (response.error == null) {
                        progressBar.complete(true, Optional.ofNullable(response.message).map(Color::good).orElse(""));
                    } else {
                        progressBar.fail(true, Color.bad(response.message));
                    }
                }
                response.message = null;
            }
            
            if ((response.error != null) && !isRetry && (video != null) &&
                    RETRY_WITH_COOKIES_ERRORS.stream().anyMatch(e -> response.error.toLowerCase().contains(e.toLowerCase()))) {
                DownloadResponse retryResponse = downloadYoutubeVideo(video, true);
                if (retryResponse != null) {
                    return retryResponse;
                }
            }
            
        } catch (Exception e) {
            response.error = "Unknown Error";
            response.message = "Unknown Error";
            response.status = DownloadResponseStatus.ERROR;
            
            if (Configurator.Config.showProgressBar && !Configurator.Config.logWork && (progressBar != null)) {
                progressBar.fail(true, Color.bad(response.message));
                response.message = null;
            }
            System.out.println(Color.bad(e.getStackTrace()));
        }
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
        return response;
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Download Response.
     */
    public static class DownloadResponse {
        
        //Fields
        
        /**
         * The download status.
         */
        public DownloadResponseStatus status;
        
        /**
         * The download message.
         */
        public String message;
        
        /**
         * The download error.
         */
        public String error;
        
        /**
         * The download log.
         */
        public String log;
        
        
        //Methods
        
        /**
         * Returns a simple string representing the Response.
         *
         * @return The simple string representing the Response.
         */
        public String simpleResponse() {
            return "Download " + status.message;
        }
        
        /**
         * Returns a string representing the Response.
         *
         * @return The string representing the Response.
         */
        public String response() {
            return simpleResponse() + (((message == null) || message.isEmpty()) ? "" : (" - " + message));
        }
        
        /**
         * Returns a printable simple string representing the Response.
         *
         * @return The printable simple string representing the Response.
         */
        public String printedSimpleResponse() {
            return Color.apply(status.color, simpleResponse());
        }
        
        /**
         * Returns a printable string representing the Response.
         *
         * @return The printable string representing the Response.
         */
        public String printedResponse() {
            return Color.apply(status.color, response());
        }
        
    }
    
}
