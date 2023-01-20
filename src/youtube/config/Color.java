/*
 * File:    Color.java
 * Package: youtube.config
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.config;

import java.io.File;
import java.util.List;
import java.util.function.BiFunction;

import commons.console.Console;
import commons.console.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.channel.config.ChannelConfig;
import youtube.util.PathUtils;
import youtube.util.Utils;

/**
 * Handles coloring of console output.
 */
public class Color {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Color.class);
    
    
    //Constants
    
    /**
     * A list of available colors.
     */
    public static final List<String> AVAILABLE_COLORS = List.of(
            "DEFAULT", "DEFAULT_COLOR",
            "WHITE", "GREY", "DARK_GREY", "BLACK",
            "DARK_RED", "RED", "ORANGE", "YELLOW",
            "DARK_GREEN", "GREEN", "TEAL", "CYAN",
            "DARK_BLUE", "BLUE", "PURPLE", "MAGENTA");
    
    /**
     * The default value of the flag indicating whether to enable colors or not.
     */
    public static final boolean DEFAULT_ENABLE_COLORS = true;
    
    /**
     * The default color to use for "base" text.
     */
    public static final String DEFAULT_COLOR_BASE = "GREEN";
    
    /**
     * The default color to use for "good" text.
     */
    public static final String DEFAULT_COLOR_GOOD = "CYAN";
    
    /**
     * The default color to use for "bad" text.
     */
    public static final String DEFAULT_COLOR_BAD = "RED";
    
    /**
     * The default color to use for "log" text.
     */
    public static final String DEFAULT_COLOR_LOG = "DARK_GREY";
    
    /**
     * The default color to use for "channel" text.
     */
    public static final String DEFAULT_COLOR_CHANNEL = "YELLOW";
    
    /**
     * The default color to use for "video" text.
     */
    public static final String DEFAULT_COLOR_VIDEO = "PURPLE";
    
    /**
     * The default color to use for "number" text.
     */
    public static final String DEFAULT_COLOR_NUMBER = "WHITE";
    
    /**
     * The default color to use for "file" text.
     */
    public static final String DEFAULT_COLOR_FILE = "GREY";
    
    /**
     * The default color to use for "exe" text.
     */
    public static final String DEFAULT_COLOR_EXE = "ORANGE";
    
    /**
     * The default color to use for "link" text.
     */
    public static final String DEFAULT_COLOR_LINK = "TEAL";
    
    /**
     * The default color to use for Progress Bar "base" text.
     */
    public static final String DEFAULT_COLOR_PROGRESS_BAR_BASE = ProgressBar.DEFAULT_COLOR_BASE.name();
    
    /**
     * The default color to use for Progress Bar "good" text.
     */
    public static final String DEFAULT_COLOR_PROGRESS_BAR_GOOD = ProgressBar.DEFAULT_COLOR_GOOD.name();
    
    /**
     * The default color to use for Progress Bar "bad" text.
     */
    public static final String DEFAULT_COLOR_PROGRESS_BAR_BAD = ProgressBar.DEFAULT_COLOR_BAD.name();
    
    //Ensure config is loaded prior to using colors
    static {
        if (Configurator.activeProject == null) {
            System.out.println(Utils.NEWLINE);
            System.out.println(Color.bad("Attempted to load colors before the config was initialized"));
            throw new RuntimeException();
        }
    }
    
    
    //Static Functions
    
    /**
     * A function that loads a color setting configuration.
     */
    private static final BiFunction<String, String, Console.ConsoleEffect> colorSettingLoader = (String name, String def) -> {
        final String confColor = Configurator.getSetting("color", name, def);
        String color = confColor.matches("(?i)DEFAULT(?:.COLOR)?") ? def :
                       confColor.toUpperCase().replace(" ", "_");
        
        if (!AVAILABLE_COLORS.contains(color)) {
            System.out.println(Color.apply(Console.ConsoleEffect.RED, (confColor + " is not a valid color")));
            color = def;
        }
        return Console.ConsoleEffect.valueOf(color);
    };
    
    
    //Static Fields
    
    /**
     * A flag indicating whether to enable colors or not.
     */
    public static final boolean enableColors = Configurator.getSetting("color", "enableColors", DEFAULT_ENABLE_COLORS);
    
    /**
     * The color to use for "base" text.
     */
    public static final Console.ConsoleEffect BASE = colorSettingLoader.apply("base", DEFAULT_COLOR_BASE);
    
    /**
     * The color to use for "good" text.
     */
    public static final Console.ConsoleEffect GOOD = colorSettingLoader.apply("good", DEFAULT_COLOR_GOOD);
    
    /**
     * The color to use for "bad" text.
     */
    public static final Console.ConsoleEffect BAD = colorSettingLoader.apply("bad", DEFAULT_COLOR_BAD);
    
    /**
     * The color to use for "log" text.
     */
    public static final Console.ConsoleEffect LOG = colorSettingLoader.apply("log", DEFAULT_COLOR_LOG);
    
    /**
     * The color to use for "channel" text.
     */
    public static final Console.ConsoleEffect CHANNEL = colorSettingLoader.apply("channel", DEFAULT_COLOR_CHANNEL);
    
    /**
     * The color to use for "video" text.
     */
    public static final Console.ConsoleEffect VIDEO = colorSettingLoader.apply("video", DEFAULT_COLOR_VIDEO);
    
    /**
     * The color to use for "number" text.
     */
    public static final Console.ConsoleEffect NUMBER = colorSettingLoader.apply("number", DEFAULT_COLOR_NUMBER);
    
    /**
     * The color to use for "file" text.
     */
    public static final Console.ConsoleEffect FILE = colorSettingLoader.apply("file", DEFAULT_COLOR_FILE);
    
    /**
     * The color to use for "exe" text.
     */
    public static final Console.ConsoleEffect EXE = colorSettingLoader.apply("exe", DEFAULT_COLOR_EXE);
    
    /**
     * The color to use for "link" text.
     */
    public static final Console.ConsoleEffect LINK = colorSettingLoader.apply("link", DEFAULT_COLOR_LINK);
    
    /**
     * The color to use for Progress Bar "base" text.
     */
    public static final Console.ConsoleEffect PROGRESS_BAR_BASE = colorSettingLoader.apply("progressBar.base", DEFAULT_COLOR_PROGRESS_BAR_BASE);
    
    /**
     * The color to use for Progress Bar "good" text.
     */
    public static final Console.ConsoleEffect PROGRESS_BAR_GOOD = colorSettingLoader.apply("progressBar.good", DEFAULT_COLOR_PROGRESS_BAR_GOOD);
    
    /**
     * The color to use for Progress Bar "bad" text.
     */
    public static final Console.ConsoleEffect PROGRESS_BAR_BAD = colorSettingLoader.apply("progressBar.bad", DEFAULT_COLOR_PROGRESS_BAR_BAD);
    
    
    //Static Methods
    
    /**
     * Applies a color to output.
     *
     * @param color The color.
     * @param o     The output.
     * @return The colored output.
     */
    public static String apply(Console.ConsoleEffect color, Object o) {
        return !enableColors ? String.valueOf(o) :
               color.apply(String.valueOf(o));
    }
    
    /**
     * Colors "base" output.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String base(Object o) {
        return apply(BASE, o);
    }
    
    /**
     * Colors "good" output.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String good(Object o) {
        return apply(GOOD, o);
    }
    
    /**
     * Colors "bad" output.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String bad(Object o) {
        return apply(BAD, o);
    }
    
    /**
     * Colors "log" output.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String log(Object o) {
        return apply(LOG, o);
    }
    
    /**
     * Colors "channel" output.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String channel(Object o) {
        return apply(CHANNEL, o);
    }
    
    /**
     * Colors "video" output.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String video(Object o) {
        return apply(VIDEO, o);
    }
    
    /**
     * Colors "number" output.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String number(Object o) {
        return apply(NUMBER, o);
    }
    
    /**
     * Colors "file" output.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String file(Object o) {
        return apply(FILE, o);
    }
    
    /**
     * Colors "exe" output.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String exe(Object o) {
        return apply(EXE, o);
    }
    
    /**
     * Colors "link" output.
     *
     * @param o The output.
     * @return The colored output.
     */
    public static String link(Object o) {
        return apply(LINK, o);
    }
    
    /**
     * Adds colored quotes around output.
     *
     * @param output The output.
     * @return The quoted output.
     */
    public static String quoted(String output) {
        final String quote = Color.log("'");
        return quote + output + quote;
    }
    
    /**
     * Colors the output indicating a file path.
     *
     * @param filePath The file path.
     * @param quote    Whether to quote the file path or not.
     * @return The colored output.
     */
    public static String filePath(String filePath, boolean quote) {
        final String filePathOutput = Color.file(PathUtils.path(filePath));
        return quote ? quoted(filePathOutput) : filePathOutput;
    }
    
    /**
     * Colors the output indicating a file path.
     *
     * @param filePath The file path.
     * @return The colored output.
     */
    public static String filePath(String filePath) {
        return filePath(filePath, true);
    }
    
    /**
     * Colors the output indicating a file path.
     *
     * @param file  The file.
     * @param quote Whether to quote the file path or not.
     * @return The colored output.
     */
    public static String filePath(File file, boolean quote) {
        return filePath(file.getAbsolutePath(), quote);
    }
    
    /**
     * Colors the output indicating a file path.
     *
     * @param file The file.
     * @return The colored output.
     */
    public static String filePath(File file) {
        return filePath(file, true);
    }
    
    /**
     * Colors the output indicating a file name.
     *
     * @param fileName The file name.
     * @param quote    Whether to quote the file name or not.
     * @return The colored output.
     */
    public static String fileName(String fileName, boolean quote) {
        final String fileNameOutput = Color.file(fileName);
        return quote ? quoted(fileNameOutput) : fileNameOutput;
    }
    
    /**
     * Colors the output indicating a file name.
     *
     * @param fileName The file name.
     * @return The colored output.
     */
    public static String fileName(String fileName) {
        return fileName(fileName, true);
    }
    
    /**
     * Colors the output indicating a file name.
     *
     * @param file  The file.
     * @param quote Whether to quote the file name or not.
     * @return The colored output.
     */
    public static String fileName(File file, boolean quote) {
        return fileName(file.getName(), quote);
    }
    
    /**
     * Colors the output indicating a file name.
     *
     * @param file The file.
     * @return The colored output.
     */
    public static String fileName(File file) {
        return fileName(file, true);
    }
    
    /**
     * Colors the output indicating a video.
     *
     * @param title The video title.
     * @param quote Whether to quote the title or not.
     * @return The colored output.
     */
    public static String videoName(String title, boolean quote) {
        final String videoNameOutput = Color.video(title);
        return quote ? quoted(videoNameOutput) : videoNameOutput;
    }
    
    /**
     * Colors the output indicating a video.
     *
     * @param title The video title.
     * @return The colored output.
     */
    public static String videoName(String title) {
        return videoName(title, true);
    }
    
    /**
     * Colors the output indicating a video rename.
     *
     * @param originalTitle The video title.
     * @param renamedTitle  The renamed video title.
     * @return The colored output.
     */
    public static String videoRename(String originalTitle, String renamedTitle) {
        return quoted(Color.video(originalTitle)) + Color.log(" to: ") + quoted(Color.video(renamedTitle));
    }
    
    /**
     * Colors the output indicating a Channel range.
     *
     * @param startChannelKey The key of the starting Channel.
     * @param endChannelKey   The key of the ending Channel.
     * @return The colored output.
     */
    public static String channelRange(String startChannelKey, String endChannelKey) {
        return Color.number("[ ") + Color.channel(startChannelKey) + Color.number(", ") + Color.channel(endChannelKey) + Color.number(" ]");
    }
    
    /**
     * Colors the output indicating a Channel range.
     *
     * @param startChannel The starting Channel.
     * @param endChannel   The ending Channel.
     * @return The colored output.
     */
    public static String channelRange(ChannelConfig startChannel, ChannelConfig endChannel) {
        return channelRange(startChannel.getKey(), endChannel.getKey());
    }
    
}
