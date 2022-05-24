/*
 * File:    Color.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.util.List;
import java.util.function.BiFunction;

import commons.console.Console;

/**
 * Handles coloring of console output.
 */
public class Color {
    
    //Constants
    
    /**
     * A list of available colors.
     */
    public static final List<String> AVAILABLE_COLORS = List.of("DEFAULT_COLOR",
            "WHITE", "BLACK", "DARK_GREY", "GREY",
            "DARK_RED", "RED", "ORANGE",
            "DARK_GREEN", "GREEN", "CYAN", "YELLOW",
            "DARK_BLUE", "BLUE", "LIGHT_BLUE", "PURPLE", "MAGENTA");
    
    
    //Functions
    
    /**
     * A function that loads a color setting configuration.
     */
    private static final BiFunction<String, String, Console.ConsoleEffect> colorSettingLoader = (String conf, String def) -> {
        String confColor = (String) Configurator.getSetting("color", conf, def);
        String color = confColor.toUpperCase().replace(" ", "_");
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
    public static final boolean enableColors = (boolean) Configurator.getSetting("color", "enableColors", true);
    
    /**
     * The color to use for "base" text".
     */
    public static final Console.ConsoleEffect BASE = colorSettingLoader.apply("base", "GREEN");
    
    /**
     * The color to use for "good" text".
     */
    public static final Console.ConsoleEffect GOOD = colorSettingLoader.apply("good", "CYAN");
    
    /**
     * The color to use for "bad" text".
     */
    public static final Console.ConsoleEffect BAD = colorSettingLoader.apply("bad", "RED");
    
    /**
     * The color to use for "log" text".
     */
    public static final Console.ConsoleEffect LOG = colorSettingLoader.apply("log", "DARK_GREY");
    
    /**
     * The color to use for "channel" text".
     */
    public static final Console.ConsoleEffect CHANNEL = colorSettingLoader.apply("channel", "YELLOW");
    
    /**
     * The color to use for "video" text".
     */
    public static final Console.ConsoleEffect VIDEO = colorSettingLoader.apply("video", "PURPLE");
    
    /**
     * The color to use for "number" text".
     */
    public static final Console.ConsoleEffect NUMBER = colorSettingLoader.apply("number", "WHITE");
    
    /**
     * The color to use for "file" text".
     */
    public static final Console.ConsoleEffect FILE = colorSettingLoader.apply("file", "GREY");
    
    /**
     * The color to use for "exe" text".
     */
    public static final Console.ConsoleEffect EXE = colorSettingLoader.apply("exe", "ORANGE");
    
    /**
     * The color to use for "link" text".
     */
    public static final Console.ConsoleEffect LINK = colorSettingLoader.apply("link", "LIGHT_BLUE");
    
    
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
    
}