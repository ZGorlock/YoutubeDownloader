/*
 * File:    DownloadUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.CmdLine;
import commons.io.console.Console;
import commons.io.console.ProgressBar;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.config.ChannelEntry;
import youtube.config.Color;
import youtube.config.Configurator;
import youtube.config.SponsorBlocker;
import youtube.entity.Video;

/**
 * Provides download utility methods for the Youtube Downloader.
 */
public final class DownloadUtils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);
    
    
    //Constants
    
    /**
     * A list of error responses that are considered a failure instead of an error, so the video will not be blocked.
     */
    private static final String[] NON_CRITICAL_ERRORS = new String[] {
            "giving up after 10",
            "urlopen error",
            "sign in to",
            "please install or provide the path",
            "check back later",
            "requested format is not available"
    };
    
    /**
     * A list of error responses that will trigger a retry attempt using browser cookies, if configured.
     */
    private static final String[] RETRY_WITH_COOKIES_ERRORS = new String[] {
            "sign in to"
    };
    
    
    //Enums
    
    /**
     * An enumeration of Download Response Statuses.
     */
    public enum DownloadResponseStatus {
        
        //Values
        
        SUCCESS("Succeeded", false, Color.Config.good),
        FAILURE("Failed", true, Color.Config.bad),
        ERROR("Error", true, Color.Config.bad);
        
        
        //Fields
        
        /**
         * The message associated with the Status.
         */
        public final String message;
        
        /**
         * Whether the Status is bad or not.
         */
        public final boolean bad;
        
        /**
         * The color of the Status.
         */
        public final Console.ConsoleEffect color;
        
        
        //Constructors
        
        /**
         * Constructs a Download Response Status.
         *
         * @param message The message associated with the Status.
         * @param bad     Whether the Status is bad or not.
         * @param color   The color of the Status.
         */
        DownloadResponseStatus(String message, boolean bad, Console.ConsoleEffect color) {
            this.message = message;
            this.bad = bad;
            this.color = color;
        }
        
        
        //Getters
        
        /**
         * Returns the message associated with the Status.
         *
         * @return The message associated with the Status.
         */
        public String getMessage() {
            return message;
        }
        
        /**
         * Returns whether the Status is bad or not.
         *
         * @return Whether the Status is bad or not.
         */
        public boolean isBad() {
            return bad;
        }
        
        /**
         * Returns the color of the Status.
         *
         * @return The color of the Status.
         */
        public Console.ConsoleEffect getColor() {
            return color;
        }
        
    }
    
    
    //Static Methods
    
    /**
     * Downloads a Youtube Video.
     *
     * @param video The Video.
     * @return A download response indicating the result of the download attempt.
     */
    public static DownloadResponse downloadYoutubeVideo(Video video) {
        return downloadYoutubeVideo(video, false);
    }
    
    /**
     * Downloads a Youtube Video.
     *
     * @param video   The Video.
     * @param isRetry Whether this download attempt is a retry or not.
     * @return A download response indicating the result of the download attempt.
     */
    private static DownloadResponse downloadYoutubeVideo(Video video, boolean isRetry) {
        final boolean newExe = !ExecutableUtils.Config.executable.isDeprecated();
        final boolean audio = Optional.ofNullable(video.getConfig()).map(ChannelEntry::isSaveAsAudio).orElse(Config.asAudio);
        final SponsorBlocker.SponsorBlockConfig sponsorBlockConfig = Optional.ofNullable(video.getConfig()).map(ChannelEntry::getSponsorBlockConfig).orElse(null);
        
        if (isRetry && (Configurator.Config.neverUseBrowserCookies || StringUtility.isNullOrBlank(Config.browser))) {
            return null;
        }
        
        final String cmd = Color.exe(ExecutableUtils.Config.executable.getCall()) + Color.log(" ") +
                Color.log("--output ") + Color.quoteFilePath((video.getDownload().getAbsolutePath() + ".%(ext)s"), true) + Color.log(" ") +
                Color.log("--geo-bypass --rm-cache-dir ") +
                Color.log(Configurator.Config.neverUseBrowserCookies ? "--no-cookies-from-browser " :
                          (isRetry ? ("--cookies-from-browser " + Config.browser.toLowerCase() + " ") : "")) +
                Color.log((audio ? ("--extract-audio --audio-format " + Config.defaultAudioFormat + " ") :
                           ((newExe && !Config.preMerged) ? "" : "--format best ")) +
                        (newExe ? ("-f b" + (audio ? "a" : "") + " ") : "")) +
                Color.log(SponsorBlocker.getCommand(sponsorBlockConfig) + " ") +
                Color.log(Optional.ofNullable(ExecutableUtils.Config.customFlags).orElse("") + " ") +
                Color.link(video.getInfo().getUrl());
        
        return performDownload(cmd, video, isRetry);
    }
    
    /**
     * Performs a Youtube Video download.
     *
     * @param cmd   The command.
     * @param video The Video.
     * @return A download response indicating the result of the download attempt.
     */
    private static DownloadResponse performDownload(String cmd, Video video, boolean isRetry) {
        LogUtils.logDivider(logger, '-');
        LogUtils.log(logger, (isRetry ? LogUtils.LogLevel.WARN : LogUtils.LogLevel.INFO),
                (LogUtils.Config.showCommand ? (LogUtils.INDENT + cmd) : StringUtility.removeConsoleEscapeCharacters(cmd)));
        
        final DownloadResponse response = new DownloadResponse();
        final DownloadProgressBar progressBar = new DownloadProgressBar(video, response);
        
        try {
            final String cmdResponse = CmdLine.executeCmd(StringUtility.removeConsoleEscapeCharacters(cmd), false, progressBar);
            
            response.processCmdResponse(cmdResponse);
            progressBar.finishDownload();
            
            if (!isRetry && StringUtility.containsAnyIgnoreCase(response.getError(), RETRY_WITH_COOKIES_ERRORS)) {
                final DownloadResponse retryResponse = downloadYoutubeVideo(video, true);
                if (retryResponse != null) {
                    return retryResponse;
                }
            }
            
        } catch (Exception e) {
            response.setError("Unknown Error");
            response.setMessage(response.getError());
            response.setStatus(DownloadResponseStatus.ERROR);
            
            progressBar.finishDownload(e);
        }
        
        LogUtils.log(logger, (response.getStatus().isBad() ? LogUtils.LogLevel.WARN : LogUtils.LogLevel.INFO),
                (LogUtils.INDENT + response.printedResponse()));
        LogUtils.logDivider(logger, '-');
        
        return response;
    }
    
    
    //Inner Classes
    
    /**
     * Holds the download Config.
     */
    public static class Config {
        
        //Constants
        
        /**
         * The default value of the flag indicating whether to download only pre-merged formats or not; only used when using yt-dlp.
         */
        public static final boolean DEFAULT_PRE_MERGED = true;
        
        /**
         * The default value of the flag indicating whether to download as audio files or not.
         */
        public static final boolean DEFAULT_AS_AUDIO = false;
        
        /**
         * The default value of the default format to save video files in.
         */
        public static final String DEFAULT_DEFAULT_VIDEO_FORMAT = FileUtils.DEFAULT_VIDEO_FORMAT;
        
        /**
         * The default value of the default format to save audio files in.
         */
        public static final String DEFAULT_DEFAULT_AUDIO_FORMAT = FileUtils.DEFAULT_AUDIO_FORMAT;
        
        
        //Static Fields
        
        /**
         * A flag indicating whether to download only pre-merged formats or not; only used when using yt-dlp.
         */
        public static boolean preMerged = DEFAULT_PRE_MERGED;
        
        /**
         * A flag indicating whether to download as audio files or not.
         */
        public static boolean asAudio = DEFAULT_AS_AUDIO;
        
        /**
         * The default format to save video files in.
         */
        public static String defaultVideoFormat = DEFAULT_DEFAULT_VIDEO_FORMAT;
        
        /**
         * The default format to save audio files in.
         */
        public static String defaultAudioFormat = DEFAULT_DEFAULT_AUDIO_FORMAT;
        
        /**
         * The browser that cookies will be used from when attempting to retry certain failed downloads.
         */
        public static String browser = null;
        
        
        //Static Methods
        
        /**
         * Initializes the Config.
         */
        private static void init() {
            preMerged = Configurator.getSetting(List.of(
                            "preMerged",
                            "format.preMerged",
                            "process.format.preMerged"),
                    DEFAULT_PRE_MERGED);
            asAudio = Configurator.getSetting(List.of(
                            "asAudio",
                            "format.asAudio",
                            "process.format.asAudio",
                            "asMp3",
                            "format.asMp3",
                            "process.format.asMp3"),
                    DEFAULT_AS_AUDIO);
            
            defaultVideoFormat = Configurator.getSetting(List.of(
                            "defaultVideoFormat",
                            "videoFormat",
                            "format.defaultVideoFormat",
                            "format.videoFormat",
                            "process.format.defaultVideoFormat",
                            "process.format.videoFormat"),
                    DEFAULT_DEFAULT_VIDEO_FORMAT);
            defaultAudioFormat = Configurator.getSetting(List.of(
                            "defaultAudioFormat",
                            "audioFormat",
                            "format.defaultAudioFormat",
                            "format.audioFormat",
                            "process.format.defaultAudioFormat",
                            "process.format.audioFormat"),
                    DEFAULT_DEFAULT_AUDIO_FORMAT);
            
            browser = Configurator.getSetting(List.of(
                    "browser",
                    "location.browser"));
        }
        
    }
    
    /**
     * Defines a Download Response.
     */
    public static class DownloadResponse {
        
        //Fields
        
        /**
         * The download status.
         */
        private DownloadResponseStatus status;
        
        /**
         * The download message.
         */
        private String message;
        
        /**
         * The download error.
         */
        private String error;
        
        /**
         * The download log.
         */
        private String log;
        
        
        //Methods
        
        /**
         * Processes a command response.
         *
         * @param cmdResponse The command response.
         */
        protected void processCmdResponse(String cmdResponse) {
            setLog(Optional.ofNullable(cmdResponse).map(String::strip).orElse(""));
            setError((!getLog().contains("ERROR: ") && !getLog().contains(".exe: error: ")) ? null :
                     getLog().substring(Math.max(getLog().lastIndexOf("ERROR: "), getLog().lastIndexOf(".exe: error: ")))
                             .replaceAll("\r?\n", " - ").replaceAll("^\\.exe:\\s*", ""));
            setMessage((getError() == null) ? getMessage() : getError()
                    .replaceAll("(?i)^ERROR:\\s*(?:-\\s*)?", "")
                    .replaceAll("^\\[[^\\\\]+]\\s*[^:]+:\\s*", "")
                    .replaceAll(":\\s*<[^>]+>\\s*\\(caused\\sby.+\\)+$", "")
                    .trim());
            setStatus((getError() == null) ? DownloadResponseStatus.SUCCESS :
                      StringUtility.containsAnyIgnoreCase(getError(), NON_CRITICAL_ERRORS) ?
                      DownloadResponseStatus.FAILURE : DownloadResponseStatus.ERROR);
        }
        
        /**
         * Returns a simple string representing the Response.
         *
         * @return The simple string representing the Response.
         */
        public String simpleResponse() {
            return "Download " + getStatus().getMessage();
        }
        
        /**
         * Returns a string representing the Response.
         *
         * @return The string representing the Response.
         */
        public String response() {
            return simpleResponse() + Optional.ofNullable(getMessage())
                    .filter(e -> !e.isEmpty()).map(e -> (" - " + e)).orElse("");
        }
        
        /**
         * Returns a printable simple string representing the Response.
         *
         * @return The printable simple string representing the Response.
         */
        public String printedSimpleResponse() {
            return Color.apply(getStatus().getColor(), simpleResponse());
        }
        
        /**
         * Returns a printable string representing the Response.
         *
         * @return The printable string representing the Response.
         */
        public String printedResponse() {
            return Color.apply(getStatus().getColor(), response());
        }
        
        
        //Getters
        
        /**
         * Returns the download status.
         *
         * @return The download status.
         */
        public DownloadResponseStatus getStatus() {
            return status;
        }
        
        /**
         * Returns the download message.
         *
         * @return The download message.
         */
        public String getMessage() {
            return message;
        }
        
        /**
         * Returns the download error.
         *
         * @return The download error.
         */
        public String getError() {
            return error;
        }
        
        /**
         * Returns the download log.
         *
         * @return The download log.
         */
        public String getLog() {
            return log;
        }
        
        
        //Setters
        
        /**
         * Sets the download status.
         *
         * @param status The download status.
         */
        public void setStatus(DownloadResponseStatus status) {
            this.status = status;
        }
        
        /**
         * Sets the download message.
         *
         * @param message The download message.
         */
        public void setMessage(String message) {
            this.message = message;
        }
        
        /**
         * Sets the download error.
         *
         * @param error The download error.
         */
        public void setError(String error) {
            this.error = error;
        }
        
        /**
         * Sets the download log.
         *
         * @param log The download log.
         */
        public void setLog(String log) {
            this.log = log;
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
        private static final Pattern PROGRESS_PATTERN = Pattern.compile("^\\[download]\\s*(?<percentage>\\d+\\.\\d+)%\\s*of\\s*~?\\s*(?<total>\\d+\\.\\d+)(?<units>.iB).*$");
        
        /**
         * A regex pattern matching a 'resuming download' line from the executable output.
         */
        private static final Pattern RESUME_PATTERN = Pattern.compile("^\\[download]\\s*Resuming\\s*download\\s*at\\s*byte\\s*(?<initialProgress>\\d+).*$");
        
        /**
         * A regex pattern matching a 'video already exists' line from the executable output.
         */
        private static final Pattern EXISTS_PATTERN = Pattern.compile("^\\[download]\\s(?<output>.+)\\shas\\salready\\sbeen\\sdownloaded$");
        
        /**
         * A regex pattern matching a 'output destination' line from the executable output.
         */
        private static final Pattern DESTINATION_PATTERN = Pattern.compile("^\\[download]\\s*Destination:\\s*(?<destination>.+)$");
        
        /**
         * A regex pattern matching a 'merging formats' line from the executable output.
         */
        private static final Pattern MERGE_PATTERN = Pattern.compile("^\\[Merger]\\s*Merging\\s*formats\\s*into\\s*\"(?<merge>.+)\"$");
        
        /**
         * A regex pattern matching a 'extracting audio' line from the executable output.
         */
        private static final Pattern EXTRACT_AUDIO_PATTERN = Pattern.compile("^\\[ExtractAudio]\\s*Destination:\\s*(?<audio>.+)$");
        
        
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
        private final AtomicBoolean newPart = new AtomicBoolean(false);
        
        /**
         * A counter storing the saved progress of the download.
         */
        private final AtomicLong saveProgress = new AtomicLong(0L);
        
        
        //Constructors
        
        /**
         * Creates a new Download Progress Bar.
         *
         * @param video    The Video being downloaded.
         * @param response the Download Response.
         */
        public DownloadProgressBar(Video video, DownloadResponse response) {
            super("", 0L, 32, "KB", (LogUtils.Config.showProgressBar && !LogUtils.Config.showWork));
            
            this.video = video;
            this.response = response;
            
            setIndent(LogUtils.INDENT_WIDTH);
            setColors(Color.Config.progressBarBase, Color.Config.progressBarGood, Color.Config.progressBarBad);
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
            logger.trace(LogUtils.Config.showWork ? Color.log(log) : log);
            
            if (!LogUtils.Config.showWork && LogUtils.Config.showProgressBar) {
                
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
                    final double total = Double.parseDouble(progressMatcher.group("total"));
                    final String units = progressMatcher.group("units").replace("i", "").toUpperCase();
                    
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
                    
                    if (getNewPart().compareAndSet(true, false)) {
                        updateTotal((long) (total * scale));
                        getSaveProgress().set(getProgress());
                    }
                    
                    final long progress = ((long) (percentage * total * scale)) + getSaveProgress().get();
                    return update(progress);
                }
            }
            
            final Matcher existsMatcher = EXISTS_PATTERN.matcher(log);
            if (existsMatcher.matches()) {
                getVideo().updateFormat(existsMatcher.group("output"));
                
                final long size = getVideo().getOutput().length() / 1024;
                updateTotal(size);
                defineInitialProgress(size);
                
                getResponse().setMessage("Already downloaded");
                return true;
            }
            
            final Matcher destinationMatcher = DESTINATION_PATTERN.matcher(log);
            if (destinationMatcher.matches()) {
                getVideo().updateFormat(destinationMatcher.group("destination"));
                return getNewPart().compareAndSet(false, true);
            }
            
            final Matcher mergeMatcher = MERGE_PATTERN.matcher(log);
            if (mergeMatcher.matches()) {
                getVideo().updateFormat(mergeMatcher.group("merge"));
                
                if (!isCompleted()) {
                    final String completionMessage = Color.good("Merging Formats" +
                            (Optional.ofNullable(getVideo().getConfig()).map(ChannelEntry::isSaveAsAudio).orElse(Config.asAudio) ? " and Extracting Audio" : "") + "...");
                    logger.info(StringUtility.removeConsoleEscapeCharacters(completionMessage));
                    complete(true, completionMessage);
                }
                
                getResponse().setMessage(null);
                return true;
            }
            
            final Matcher extractAudioMatcher = EXTRACT_AUDIO_PATTERN.matcher(log);
            if (extractAudioMatcher.matches()) {
                getVideo().updateFormat(extractAudioMatcher.group("audio"));
                
                if (!isCompleted()) {
                    final String completionMessage = Color.good("Extracting Audio...");
                    logger.info(StringUtility.removeConsoleEscapeCharacters(completionMessage));
                    complete(true, completionMessage);
                }
                
                getResponse().setMessage(null);
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
            if (LogUtils.Config.showProgressBar && !LogUtils.Config.showWork) {
                if (!isCompleted()) {
                    if (((exception != null) && (getProgress() > 0)) || (getResponse().getError() != null)) {
                        final String errorMessage = Color.bad(getResponse().getMessage());
                        if (!errorMessage.isBlank()) {
                            logger.warn(StringUtility.removeConsoleEscapeCharacters(errorMessage));
                        }
                        fail(true, errorMessage);
                    } else {
                        final String completionMessage = Optional.ofNullable(getResponse().getMessage()).map(Color::good).orElse("");
                        if (!completionMessage.isBlank()) {
                            logger.info(StringUtility.removeConsoleEscapeCharacters(completionMessage));
                        }
                        complete(true, completionMessage);
                    }
                }
                getResponse().setMessage(null);
            }
            if (exception != null) {
                logger.warn(Color.bad(exception.getStackTrace()));
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
        
        
        //Getters
        
        /**
         * Returns the Video being downloaded.
         *
         * @return The Video being downloaded.
         */
        protected Video getVideo() {
            return video;
        }
        
        /**
         * Returns the Download Response.
         *
         * @return The Download Response.
         */
        protected DownloadResponse getResponse() {
            return response;
        }
        
        /**
         * Returns a flag indicating whether a new file part is being downloaded.
         *
         * @return A flag indicating whether a new file part is being downloaded.
         */
        protected AtomicBoolean getNewPart() {
            return newPart;
        }
        
        /**
         * Returns a counter storing the saved progress of the download.
         *
         * @return A counter storing the saved progress of the download.
         */
        protected AtomicLong getSaveProgress() {
            return saveProgress;
        }
        
    }
    
}
