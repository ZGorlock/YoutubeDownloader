/*
 * File:    DownloadUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.CmdLine;
import commons.console.Console;
import commons.console.ProgressBar;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.ChannelEntry;
import youtube.channel.entity.Video;
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
     * A flag indicating whether to display a progress bar or not.
     */
    public static final boolean DISPLAY_PROGRESS_BAR = Configurator.Config.showProgressBar && !Configurator.Config.logWork;
    
    /**
     * A list of error responses that are considered a failure instead of an error, so the video will not be blocked.
     */
    public static final String[] NON_CRITICAL_ERRORS = new String[] {
            "giving up after 10",
            "urlopen error",
            "sign in to",
            "please install or provide the path"
    };
    
    /**
     * A list of error responses that will trigger a retry attempt using browser cookies, if configured.
     */
    public static final String[] RETRY_WITH_COOKIES_ERRORS = new String[] {
            "sign in to"
    };
    
    
    //Static Methods
    
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
        final boolean ytDlp = (ExecutableUtils.EXECUTABLE == ExecutableUtils.Executable.YT_DLP);
        final boolean asMp3 = Optional.ofNullable(video.channel).map(ChannelEntry::isSaveAsMp3).orElse(Configurator.Config.asMp3);
        final SponsorBlocker.SponsorBlockConfig sponsorBlockConfig = Optional.ofNullable(video.channel).map(ChannelEntry::getSponsorBlockConfig).orElse(null);
        
        if (isRetry && (Configurator.Config.neverUseBrowserCookies || StringUtility.isNullOrBlank(Configurator.Config.browser))) {
            return null;
        }
        
        final String cmd = Color.exe(ExecutableUtils.EXECUTABLE.getCall()) + Color.log(" ") +
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
        logger.debug(System.lineSeparator() + StringUtility.repeatString("-", 200) + System.lineSeparator());
        logger.info(cmd + System.lineSeparator());
        
        final DownloadResponse response = new DownloadResponse();
        final DownloadProgressBar progressBar = new DownloadProgressBar(video, response);
        
        try {
            final String cmdResponse = CmdLine.executeCmd(cmd, false, progressBar);
            
            response.processCmdResponse(cmdResponse);
            progressBar.finishDownload();
            
            if (!isRetry && StringUtility.containsAnyIgnoreCase(response.error, RETRY_WITH_COOKIES_ERRORS)) {
                final DownloadResponse retryResponse = downloadYoutubeVideo(video, true);
                if (retryResponse != null) {
                    return retryResponse;
                }
            }
            
        } catch (Exception e) {
            response.error = "Unknown Error";
            response.message = response.error;
            response.status = DownloadResponseStatus.ERROR;
            
            progressBar.finishDownload(e);
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
         * Processes a command response.
         *
         * @param cmdResponse The command response.
         */
        protected void processCmdResponse(String cmdResponse) {
            log = Optional.ofNullable(cmdResponse).map(String::strip).orElse("");
            error = (!log.contains("ERROR: ") && !log.contains(".exe: error: ")) ? null :
                    log.substring(Math.max(log.lastIndexOf("ERROR: "), log.lastIndexOf(".exe: error: ")))
                            .replaceAll("\r?\n", " - ").replaceAll("^\\.exe:\\s*", "");
            message = (error == null) ? message : error
                    .replaceAll("(?i)^ERROR:\\s*(?:-\\s*)?", "")
                    .replaceAll("^\\[[^\\\\]+]\\s*[^:]+:\\s*", "")
                    .replaceAll(":\\s*<[^>]+>\\s*\\(caused\\sby.+\\)+$", "")
                    .trim();
            status = (error == null) ? DownloadResponseStatus.SUCCESS :
                     StringUtility.containsAnyIgnoreCase(error, NON_CRITICAL_ERRORS) ?
                     DownloadResponseStatus.FAILURE : DownloadResponseStatus.ERROR;
        }
        
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
    
    /**
     * A progress bar for Youtube download operations.
     */
    private static class DownloadProgressBar extends ProgressBar {
        
        //Constants
        
        /**
         * A regex pattern matching a 'download progress' line from the executable output.
         */
        public static final Pattern PROGRESS_PATTERN = Pattern.compile("^\\[download]\\s*(?<percentage>\\d+\\.\\d+)%\\s*of\\s*~?\\s*(?<total>\\d+\\.\\d+)(?<units>.iB).*$");
        
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
         * A regex pattern matching a 'merging formats' line from the executable output.
         */
        public static final Pattern MERGE_PATTERN = Pattern.compile("^\\[Merger]\\s*Merging\\s*formats\\s*into\\s*\"(?<merge>.+)\"$");
        
        /**
         * A regex pattern matching a 'extracting audio' line from the executable output.
         */
        public static final Pattern EXTRACT_AUDIO_PATTERN = Pattern.compile("^\\[ExtractAudio]\\s*Destination:\\s*(?<audio>.+)$");
        
        
        //Fields
        
        /**
         * The Video being downloaded.
         */
        private final Video video;
        
        /**
         * The Download Response.
         */
        private final DownloadResponse response;
        
        /**
         * A flag indicating whether a new file part is being downloaded.
         */
        private AtomicBoolean newPart = new AtomicBoolean(false);
        
        /**
         * A counter storing the saved progress of the download.
         */
        private AtomicLong saveProgress = new AtomicLong(0L);
        
        
        //Constructors
        
        /**
         * Creates a new DownloadProgressBar object.
         *
         * @param video    The Video being downloaded.
         * @param response the Download Response.
         */
        public DownloadProgressBar(Video video, DownloadResponse response) {
            super("", 0L, 32, "KB", DISPLAY_PROGRESS_BAR);
            this.video = video;
            this.response = response;
            
            setIndent(StringUtility.removeConsoleEscapeCharacters(Utils.INDENT).length());
            setColors(Color.PROGRESS_BAR_BASE, Color.PROGRESS_BAR_GOOD, Color.PROGRESS_BAR_BAD);
        }
        
        
        //Methods
        
        /**
         * Processes download log data and updates the progress bar accordingly.
         *
         * @param log     The download log data.
         * @param isError Whether the passed log is an error log or not.
         * @return Whether the progress bar was updated or not.
         */
        @Override
        public synchronized boolean processLog(String log, boolean isError) {
            logger.trace(log);
            
            if (Configurator.Config.logWork) {
                System.out.println(Color.log(log));
                
            } else if (Configurator.Config.showProgressBar) {
                
                if (getInitialProgress() == 0) {
                    final Matcher resumeMatcher = RESUME_PATTERN.matcher(log);
                    if (resumeMatcher.matches()) {
                        final long initialProgress = Long.parseLong(resumeMatcher.group("initialProgress")) / 1024;
                        return defineInitialProgress(initialProgress);
                    }
                }
                
                final Matcher progressMatcher = PROGRESS_PATTERN.matcher(log);
                if (progressMatcher.matches()) {
                    final double percentage = Double.parseDouble(progressMatcher.group("percentage")) / 100.0;
                    final long total = (long) Double.parseDouble(progressMatcher.group("total"));
                    final String units = progressMatcher.group("units").replace("i", "");
                    
                    long scale = 1L;
                    switch (units) {
                        case "TB":
                            scale *= 1024;
                        case "GB":
                            scale *= 1024;
                        case "MB":
                            scale *= 1024;
                        case "KB":
                        default:
                    }
                    
                    if (newPart.compareAndSet(true, false)) {
                        updateTotal(total * scale);
                        saveProgress.set(getProgress());
                    }
                    
                    final long progress = ((long) (percentage * total * scale)) + saveProgress.get();
                    return update(progress);
                }
            }
            
            final Matcher existsMatcher = EXISTS_PATTERN.matcher(log);
            if (existsMatcher.matches()) {
                video.output = new File(existsMatcher.group("output"));
                
                final long size = video.output.length() / 1024;
                updateTotal(size);
                defineInitialProgress(size);
                
                response.message = "Already downloaded";
                return true;
            }
            
            final Matcher destinationMatcher = DESTINATION_PATTERN.matcher(log);
            if (destinationMatcher.matches()) {
                video.output = new File(destinationMatcher.group("destination"));
                return newPart.compareAndSet(false, true);
            }
            
            final Matcher mergeMatcher = MERGE_PATTERN.matcher(log);
            if (mergeMatcher.matches()) {
                video.output = new File(mergeMatcher.group("merge"));
                
                if (!isCompleted()) {
                    complete(true, Color.good("Merging Formats" +
                            (Optional.ofNullable(video.channel).map(ChannelEntry::isSaveAsMp3).orElse(Configurator.Config.asMp3) ? " and Extracting Audio" : "") + "..."));
                }
                
                response.message = null;
                return true;
            }
            
            final Matcher extractAudioMatcher = EXTRACT_AUDIO_PATTERN.matcher(log);
            if (extractAudioMatcher.matches()) {
                video.output = new File(extractAudioMatcher.group("audio"));
                
                if (!isCompleted()) {
                    complete(true, Color.good("Extracting Audio..."));
                }
                
                response.message = null;
                return true;
            }
            
            return false;
        }
        
        /**
         * Finishes the download progress bar.
         *
         * @param exception The exception that ended the download, or null if the download ended naturally.
         */
        protected synchronized void finishDownload(Exception exception) {
            if (DISPLAY_PROGRESS_BAR) {
                if (!isCompleted()) {
                    if (((exception != null) && (getProgress() > 0)) || (response.error != null)) {
                        fail(true, Color.bad(response.message));
                    } else {
                        complete(true, Optional.ofNullable(response.message).map(Color::good).orElse(""));
                    }
                }
                response.message = null;
            }
            if (exception != null) {
                System.out.println(Color.bad(exception.getStackTrace()));
            }
        }
        
        /**
         * Finishes the download progress bar.
         */
        protected synchronized void finishDownload() {
            finishDownload(null);
        }
        
        /**
         * Completes the progress bar.
         */
        @Override
        @SuppressWarnings("EmptyMethod")
        public synchronized void complete() {
        }
        
    }
    
}
