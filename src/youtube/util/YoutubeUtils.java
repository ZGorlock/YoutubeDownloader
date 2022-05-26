/*
 * File:    YoutubeUtils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.CmdLine;
import commons.access.OperatingSystem;
import commons.console.ProgressBar;
import commons.object.string.StringUtility;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import youtube.channel.Video;

/**
 * Provides utility methods for the Youtube Downloader.
 */
public final class YoutubeUtils {
    
    //Enums
    
    /**
     * An enumeration of Youtube Downloader executables.
     */
    public enum Executable {
        
        //Values
        
        YOUTUBE_DL("youtube-dl", "https://www.youtube-dl.org/"),
        YT_DLP("yt-dlp", "https://github.com/yt-dlp/yt-dlp/");
        
        
        //Fields
        
        /**
         * The name of the Executable.
         */
        private final String name;
        
        /**
         * The exe of the Executable.
         */
        private final File exe;
        
        /**
         * The website of the Executable.
         */
        private final String website;
        
        
        //Constructors
        
        /**
         * Constructs an Executable.
         *
         * @param name    The name of the Executable.
         * @param website The website of the Executable.
         */
        Executable(String name, String website) {
            this.name = name;
            this.exe = new File(name + (OperatingSystem.isWindows() ? ".exe" : ""));
            this.website = website;
        }
        
        
        //Methods
        
        /**
         * Returns the name of the Executable.
         *
         * @return The name of the Executable.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Returns the exe of the Executable.
         *
         * @return The exe of the Executable.
         */
        public File getExe() {
            return exe;
        }
        
        /**
         * Returns the website of the Executable.
         *
         * @return The website of the Executable.
         */
        public String getWebsite() {
            return website;
        }
        
    }
    
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
     * The Youtube Downloader executable to use.
     */
    public static final Executable EXECUTABLE = Executable.valueOf(
            ((String) Configurator.getSetting("executable", Executable.YT_DLP.name)).toUpperCase().replace("-", "_"));
    
    /**
     * The base url for Youtube videos.
     */
    public static final String VIDEO_BASE = "https://www.youtube.com/watch?v=";
    
    /**
     * The regex pattern for a Youtube url.
     */
    public static final Pattern VIDEO_URL_PATTERN = Pattern.compile("^.*/watch?.*v=(?<id>[^=?&]+).*$");
    
    /**
     * A list of possible video formats.
     */
    public static final List<String> VIDEO_FORMATS = List.of("3gp", "flv", "mp4", "webm");
    
    /**
     * A list of possible audio formats.
     */
    public static final List<String> AUDIO_FORMATS = List.of("aac", "m4a", "mp3", "ogg", "wav");
    
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
     * The newline string.
     */
    public static final String NEWLINE = Color.base("");
    
    /**
     * The indentation string.
     */
    public static final String INDENT = Color.base("     ");
    
    
    //Functions
    
    /**
     * Downloads a Youtube video.
     *
     * @param video The Video data object.
     * @return A download response indicated the result of the download attempt.
     * @throws Exception When there is an error downloading the video.
     */
    public static DownloadResponse downloadYoutubeVideo(Video video) throws Exception {
        boolean ytDlp = EXECUTABLE.equals(Executable.YT_DLP);
        boolean asMp3 = Optional.ofNullable(video.channel).map(e -> e.saveAsMp3).orElse(Configurator.Config.asMp3);
        SponsorBlocker.SponsorBlockConfig sponsorBlockConfig = Optional.ofNullable(video.channel).map(e -> e.sponsorBlockConfig).orElse(null);
        
        String cmd = Color.exe(EXECUTABLE.getExe().getName()) + Color.log(" ") +
                Color.log("--output \"") + Color.file(video.download.getAbsolutePath().replace("\\", "/") + ".%(ext)s") + Color.log("\" ") +
                Color.log("--geo-bypass --rm-cache-dir ") +
                Color.log(asMp3 ? "--extract-audio --audio-format mp3 " :
                          ((ytDlp && !Configurator.Config.preMerged) ? "" : ("--format best " + (ytDlp ? "-f b " : "")))) +
                Color.log(SponsorBlocker.getCommand(sponsorBlockConfig)) +
                Color.link(video.url);
        
        if (Configurator.Config.logCommand) {
            System.out.println(INDENT + Color.base(cmd));
        }
        
        return performDownload(StringUtility.removeConsoleEscapeCharacters(cmd), video, Configurator.Config.logWork);
    }
    
    /**
     * Executes a command line process.
     *
     * @param cmd   The command.
     * @param video The Video data object.
     * @param log   Whether or not to log the response from the process.
     * @return A download response indicated the result of the download attempt.
     */
    private static DownloadResponse performDownload(String cmd, Video video, boolean log) {
        ProgressBar progressBar = null;
        
        try {
            ProcessBuilder builder = CmdLine.buildProcess(cmd);
            builder.redirectErrorStream(true);
            
            Process process = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            Pattern progressPattern = log ? null : Pattern.compile("^\\[download]\\s*(?<percentage>\\d+\\.\\d+)%\\s*of\\s*(?<total>\\d+\\.\\d+)(?<units>.iB).*$");
            Pattern resumePattern = log ? null : Pattern.compile("^\\[download]\\s*Resuming\\s*download\\s*at\\s*byte\\s*(?<initialProgress>\\d+).*$");
            Pattern existsPattern = Pattern.compile("^\\[download]\\s(?<output>.+)\\shas\\salready\\sbeen\\sdownloaded$");
            Pattern destinationPattern = Pattern.compile("^\\[[^]]+]\\s*Destination:\\s*(?<destination>.+)$");
            Pattern mergePattern = Pattern.compile("^\\[Merger]\\s*Merging\\s*formats\\s*into\\s*\"(?<merge>.+)\"$");
            
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            long initialProgress = -1L;
            long saveProgress = 0L;
            boolean newPart = true;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                
                if (log) {
                    System.out.println(Color.log(line));
                    
                } else {
                    if (initialProgress == -1) {
                        Matcher resumeMatcher = resumePattern.matcher(line);
                        if (resumeMatcher.matches()) {
                            initialProgress = Long.parseLong(resumeMatcher.group("initialProgress")) / 1024;
                        }
                    }
                    
                    Matcher progressMatcher = progressPattern.matcher(line);
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
                                progressBar.setIndent(StringUtility.removeConsoleEscapeCharacters(INDENT).length());
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
                    }
                }
                
                Matcher existsMatcher = existsPattern.matcher(line);
                if (existsMatcher.matches()) {
                    File output = new File(existsMatcher.group("output"));
                    if (video != null) {
                        video.output = output;
                    }
                    long size = output.length() / 1024;
                    if (progressBar == null) {
                        progressBar = new ProgressBar("", size, 32, "KB", true);
                        progressBar.setIndent(StringUtility.removeConsoleEscapeCharacters(INDENT).length());
                        progressBar.defineInitialProgress(size);
                    }
                    progressBar.complete(false, Color.good("Already downloaded"));
                }
                
                Matcher destinationMatcher = destinationPattern.matcher(line);
                if (destinationMatcher.matches()) {
                    File destination = new File(destinationMatcher.group("destination"));
                    if (video != null) {
                        video.output = destination;
                    }
                    newPart = true;
                }
                
                Matcher mergeMatcher = mergePattern.matcher(line);
                if (mergeMatcher.matches()) {
                    File merge = new File(mergeMatcher.group("merge"));
                    if (video != null) {
                        video.output = merge;
                    }
                    newPart = true;
                }
                
                responseBuilder.append(line).append(System.lineSeparator());
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
                progressBar.setIndent(StringUtility.removeConsoleEscapeCharacters(INDENT).length());
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
            
            Thread.sleep(200);
            return (error == null) ? DownloadResponse.SUCCESS :
                   NON_CRITICAL_ERRORS.stream().anyMatch(e -> error.toLowerCase().contains(e.toLowerCase())) ? DownloadResponse.FAILURE :
                   DownloadResponse.ERROR;
            
        } catch (Exception e) {
            if (progressBar != null) {
                progressBar.fail(true, Color.bad("Unknown Error"));
            }
            System.out.println(Color.bad(e.getStackTrace()));
            return DownloadResponse.ERROR;
        }
    }
    
    /**
     * Tries to find a video.
     *
     * @param output The output file for the video.
     * @return The found file or files.
     */
    public static File findVideo(File output) {
        File outputDir = output.getParentFile();
        if (!outputDir.exists()) {
            return null;
        }
        
        File[] existingFiles = outputDir.listFiles();
        if (existingFiles == null) {
            return null;
        }
        
        String name = output.getName().replaceAll("\\.[^.]+$|[^a-zA-Z\\d+]|\\s+", "");
        
        List<File> found = new ArrayList<>();
        for (File existingFile : existingFiles) {
            String existingName = existingFile.getName().replaceAll("\\.[^.]+$|[^a-zA-Z\\d+]|\\s+", "");
            if (existingName.equalsIgnoreCase(name) && (existingFile.length() > 0)) {
                String format = getFormat(output.getName());
                String existingFormat = getFormat(existingFile.getName());
                if (format.equalsIgnoreCase(existingFormat) ||
                        (YoutubeUtils.VIDEO_FORMATS.contains(format) && YoutubeUtils.VIDEO_FORMATS.contains(existingFormat)) ||
                        (YoutubeUtils.AUDIO_FORMATS.contains(format) && YoutubeUtils.AUDIO_FORMATS.contains(existingFormat))) {
                    found.add(existingFile);
                }
            }
        }
        
        if (found.size() == 1) {
            return found.get(0);
        } else {
            return null;
        }
    }
    
    /**
     * Cleans the title of a Youtube video.
     *
     * @param title The title.
     * @return The cleaned title.
     */
    public static String cleanTitle(String title) {
        title = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "")
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS_SUPPLEMENT}+", "");
        title = title.replace("\\", "-")
                .replace("/", "-")
                .replace(":", "-")
                .replace("*", "-")
                .replace("?", "")
                .replace("\"", "'")
                .replace("<", "-")
                .replace(">", "-")
                .replace("|", "-")
                .replace("‒", "-")
                .replace(" ", " ")
                .replace("&amp;", "&")
                .replaceAll("^#(sh[oa]rts?)", "$1 - ")
                .replace("#", "- ")
                .replaceAll("[—–-]", "-")
                .replaceAll("[’‘]", "'")
                .replace("С", "C")
                .replaceAll("[™©®†]", "")
                .replace("¹", "1")
                .replace("²", "2")
                .replace("³", "3")
                .replace("×", "x")
                .replace("÷", "%")
                .replace("⋯", "...")
                .replaceAll("[^\\x00-\\x7F]", "+")
                .replaceAll("\\p{Cntrl}&&[^\r\n\t]", "")
                .replaceAll("\\s*[.!\\-]+$", "")
                .replaceAll("(?:\\+\\s+)+", "+ ")
                .replaceAll("\\++", "+")
                .replaceAll("^\\s*\\+\\s*", "")
                .replaceAll("(?:-\\s+)+", "- ")
                .replaceAll("-+", "-")
                .replaceAll("^\\s*-\\s*", "")
                .replace("+-", "+ -")
                .replaceAll("^\\s+|\\s+$", "")
                .replaceAll("(^-\\s*)+|(\\s*-)+$", "")
                .replaceAll("!(?:\\s*!)+", "!")
                .replaceAll("\\s+", " ")
                .replaceAll("\\$+", Matcher.quoteReplacement("$"))
                .trim();
        return title;
    }
    
    /**
     * Returns the file format of a file name.
     *
     * @param fileName The file name.
     * @return The file format of the file name.
     */
    public static String getFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
    
    /**
     * Builds a parameter string to be appended to a url.
     *
     * @param parameters A map of parameters.
     * @return The parameter string.
     * @throws Exception When there is an error encoding the string.
     */
    public static String buildParameterString(Map<String, String> parameters) throws Exception {
        StringBuilder parameterString = new StringBuilder("?");
        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            if (parameterString.length() > 1) {
                parameterString.append("&");
            }
            parameterString.append(URLEncoder.encode(parameterEntry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(parameterEntry.getValue(), StandardCharsets.UTF_8));
        }
        return parameterString.toString();
    }
    
    /**
     * Performs startup checks.
     *
     * @return Whether all checks were successful or not.
     */
    public static boolean doStartupChecks() {
        if (!YoutubeUtils.isOnline()) {
            System.out.println(Color.bad("Internet access is required"));
            return false;
        }
        
        return checkExe();
    }
    
    /**
     * Determines if the system has access to the internet.
     *
     * @return Whether the system has access to the internet or not.
     */
    public static boolean isOnline() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("youtube.com", 80), 200);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Determines if the exe exists and attempts to update it, or attempts to download it if it does not exist.
     *
     * @return Whether the exe exists.
     */
    private static boolean checkExe() {
        String currentExecutableVersion = EXECUTABLE.getExe().exists() ? CmdLine.executeCmd(EXECUTABLE.getExe().getName() + " --version")
                .replaceAll("\r?\n", "").replaceAll("\\[\\*].*$", "").trim() : "";
        String latestExecutableVersion = YoutubeUtils.getLatestExecutableVersion();
        boolean exists = EXECUTABLE.getExe().exists();
        boolean update = exists && !currentExecutableVersion.equals(latestExecutableVersion);
        
        if (exists) {
            System.out.println(Color.exe(EXECUTABLE.getExe().getName()) + Color.number(" v" + currentExecutableVersion));
        } else {
            System.out.println(Color.bad("Requires ") + Color.exe(EXECUTABLE.getName()));
        }
        System.out.println(YoutubeUtils.NEWLINE);
        
        if (exists && (currentExecutableVersion.isEmpty() || latestExecutableVersion.isEmpty())) {
            System.out.println(Color.bad("Unable to check for updates for ") + Color.exe(EXECUTABLE.getName()));
            System.out.println(YoutubeUtils.NEWLINE);
            
        } else if (!exists || update) {
            if (exists) {
                System.out.println(Color.base("Current Version: ") + Color.number(currentExecutableVersion) + Color.base(" Latest Version: ") + Color.number(latestExecutableVersion));
            }
            
            if (!Configurator.Config.preventExeAutoUpdate) {
                System.out.println(Color.base("Downloading ") + Color.exe(EXECUTABLE.getName()) + Color.number(" v" + latestExecutableVersion));
                File executable = YoutubeUtils.downloadLatestExecutable(latestExecutableVersion);
                
                if ((executable == null) || !EXECUTABLE.getExe().exists() || !executable.getName().equals(EXECUTABLE.getExe().getName())) {
                    System.out.println(Color.bad("Unable to " + (exists ? "update" : "download") + " ") + Color.exe(EXECUTABLE.getName()));
                } else {
                    System.out.println(Color.base("Successfully " + (exists ? "updated to" : "downloaded") + " ") + Color.exe(EXECUTABLE.getName()) + Color.number(" v" + latestExecutableVersion));
                }
            } else {
                System.out.println(Color.bad("Would have " + (exists ? "updated to" : "downloaded") + " ") + Color.exe(EXECUTABLE.getName()) + Color.number(" v" + latestExecutableVersion) + Color.bad(" but auto updating is disabled"));
            }
            System.out.println(YoutubeUtils.NEWLINE);
        }
        
        System.out.println(YoutubeUtils.NEWLINE);
        return EXECUTABLE.getExe().exists();
    }
    
    /**
     * Returns the latest executable version.
     *
     * @return The latest executable version, or an empty string if there was an error.
     */
    private static String getLatestExecutableVersion() {
        String url;
        String versionPatternRegex;
        
        switch (EXECUTABLE) {
            case YOUTUBE_DL:
                url = EXECUTABLE.getWebsite();
                versionPatternRegex = "<a\\shref=\"latest\">Latest</a>\\s\\(v(?<version>[\\d.]+)\\)\\sdownloads:";
                break;
            
            case YT_DLP:
                url = EXECUTABLE.getWebsite() + "releases/";
                versionPatternRegex = "<a\\shref=\"/yt-dlp/yt-dlp/releases/tag/(?<version>[\\d.]+)\"";
                break;
            
            default:
                return "";
        }
        
        try {
            String html = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36")
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .followRedirects(true)
                    .execute()
                    .parse()
                    .toString();
            
            Pattern versionPattern = Pattern.compile(".*" + versionPatternRegex + ".*");
            String[] lines = html.split("\n");
            for (String line : lines) {
                Matcher versionMatcher = versionPattern.matcher(line);
                if (versionMatcher.matches()) {
                    return versionMatcher.group("version");
                }
            }
            
        } catch (IOException ignored) {
        }
        return "";
    }
    
    /**
     * Downloads the latest executable.
     *
     * @param latestVersion The latest version of the executable.
     * @return The downloaded executable, or null if there was an error.
     */
    private static File downloadLatestExecutable(String latestVersion) {
        switch (EXECUTABLE) {
            case YOUTUBE_DL:
                //https://www.youtube-dl.org/downloads/latest/youtube-dl.exe
                return downloadFile(EXECUTABLE.getWebsite() + "downloads/latest/" + EXECUTABLE.getExe().getName(), EXECUTABLE.getExe());
            
            case YT_DLP:
                //https://github.com/yt-dlp/yt-dlp/releases/download/2021.08.10/yt-dlp.exe
                return downloadFile(EXECUTABLE.getWebsite() + "releases/download/" + latestVersion + '/' + EXECUTABLE.getExe().getName(), EXECUTABLE.getExe());
            
            default:
                return null;
        }
    }
    
    /**
     * Downloads a file from a url to the specified file and returns the file.<br>
     * This is a blocking operation and should be called from a thread.
     *
     * @param url      The url to the file to download.
     * @param download The file to download to.
     * @return The downloaded file or null if there was an error.
     * @see FileUtils#copyURLToFile(URL, File, int, int)
     */
    private static File downloadFile(String url, File download) {
        try {
            if (!isOnline()) {
                throw new IOException();
            }
            
            FileUtils.copyURLToFile(new URL(url), download, 200, Integer.MAX_VALUE);
            return download;
            
        } catch (IOException ignored) {
            return null;
        }
    }
    
}
