/*
 * File:    Console.java
 * Package: commons.io.console
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.io.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines console effects.<br>
 * Support for these effects can not be guaranteed on all consoles.
 */
public final class Console {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Console.class);
    
    
    //Enums
    
    /**
     * An enumeration of Console Effects.
     */
    public enum ConsoleEffect {
        
        //Values
        
        RESET(0),
        
        BOLD(1),
        FAINT(2),
        ITALIC(3),
        UNDERLINE(4),
        UNDERLINE_OFF(24),
        BLINK_SLOW(5),
        BLINK_FAST(6),
        REVERSE(7),
        REVERSE_OFF(27),
        CONCEAL(8),
        STRIKE_THROUGH(9),
        STRIKE_THROUGH_OFF(29),
        
        DEFAULT_COLOR(39),
        
        BLACK(30),
        DARK_RED(31),
        DARK_GREEN(32),
        ORANGE(33),
        DARK_BLUE(34),
        PURPLE(35),
        TEAL(36),
        GREY(37),
        DARK_GREY(90),
        RED(91),
        GREEN(92),
        YELLOW(93),
        BLUE(94),
        MAGENTA(95),
        CYAN(96),
        WHITE(97),
        
        BLACK_BG(40),
        DARK_RED_BG(41),
        DARK_GREEN_BG(42),
        ORANGE_BG(43),
        DARK_BLUE_BG(44),
        PURPLE_BG(45),
        TEAL_BG(46),
        GREY_BG(47),
        DARK_GREY_BG(100),
        RED_BG(101),
        GREEN_BG(102),
        YELLOW_BG(103),
        BLUE_BG(104),
        MAGENTA_BG(105),
        CYAN_BG(106),
        WHITE_BG(107);
        
        
        //Fields
        
        /**
         * The code of the Console Effect.
         */
        private final int code;
        
        /**
         * The key of the Console Effect.
         */
        private final String key;
        
        
        //Constructors
        
        /**
         * Constructs a Console Effect.
         *
         * @param code The code of the Console Effect.
         */
        ConsoleEffect(int code) {
            this.code = code;
            this.key = "\u001B[" + code + "m";
        }
        
        
        //Getters
        
        /**
         * Returns the code of the Console Effect.
         *
         * @return The code of the Console Effect.
         */
        public int getCode() {
            return code;
        }
        
        /**
         * Returns the key of the Console Effect.
         *
         * @return The key of the Console Effect.
         */
        public String getKey() {
            return key;
        }
        
        
        //Methods
        
        /**
         * Applies the Console Effect to a string.
         *
         * @param string The string to apply the effect to.
         * @return The string with the Console Effect applied.
         * @see #stringEffect(String, ConsoleEffect)
         */
        public String apply(String string) {
            return stringEffect(string, this);
        }
        
    }
    
    
    //Static Methods
    
    /**
     * Returns the code for an effect.
     *
     * @param effect The effect.
     * @return The code for the effect.
     */
    private static String effectCode(ConsoleEffect effect) {
        return String.valueOf(effect.getCode());
    }
    
    /**
     * Returns the code for a set of effects.
     *
     * @param effects The effects to add.
     * @return The code for the set of effects.
     */
    private static String effectSetCode(ConsoleEffect... effects) {
        StringBuilder effectSetCode = new StringBuilder();
        for (ConsoleEffect effect : effects) {
            if (effect == ConsoleEffect.RESET) {
                continue;
            }
            if (effectSetCode.length() > 0) {
                effectSetCode.append(';');
            }
            effectSetCode.append(effect.getCode());
        }
        return effectSetCode.toString();
    }
    
    /**
     * Returns the key for a code or set of codes.
     *
     * @param code The code or set of codes.
     * @return The key for the code or set of codes.
     */
    private static String codeKey(String... code) {
        StringBuilder codeBuilder = new StringBuilder();
        for (String codeEntry : code) {
            if (!codeEntry.isEmpty() && (codeBuilder.length() > 0)) {
                codeBuilder.append(';');
            }
            codeBuilder.append(codeEntry);
        }
        return (codeBuilder.length() > 0) ? ("\u001B[" + codeBuilder + 'm') : "";
    }
    
    /**
     * Returns a string with an effect.
     *
     * @param key    The key of the effect.
     * @param string The string.
     * @return The string with the effect.
     */
    private static String effect(String key, String string) {
        return key + string + ConsoleEffect.RESET.getKey();
    }
    
    /**
     * Adds an effect to the string for output to the console.
     *
     * @param string The string to add an effect to.
     * @param effect The effect.
     * @return The string with the added effect for output to the console.
     */
    public static String stringEffect(String string, ConsoleEffect effect) {
        return effect(codeKey(effectCode(effect)), string);
    }
    
    /**
     * Adds effects to the string for output to the console.
     *
     * @param string  The string to add effects to.
     * @param effects The effects to add.
     * @return The string with the added effects for output to the console.
     */
    public static String stringEffects(String string, ConsoleEffect... effects) {
        return effect(codeKey(effectSetCode(effects)), string);
    }
    
    /**
     * Adds color to the string for output to the console.
     *
     * @param string The string to add color to.
     * @param color  The color.
     * @return The string with the added color for output to the console.
     * @see #stringEffects(String, ConsoleEffect...)
     */
    public static String color(String string, ConsoleEffect color) {
        return stringEffects(string, color);
    }
    
    /**
     * Adds color and background to the string for output to the console.
     *
     * @param string     The string to add color to.
     * @param color      The color.
     * @param background The background color.
     * @return The string with the added color for output to the console.
     * @see #stringEffects(String, ConsoleEffect...)
     */
    public static String colorAndBackground(String string, ConsoleEffect color, ConsoleEffect background) {
        return stringEffects(string, color, background);
    }
    
    /**
     * Adds 8-bit color to the string for output to the console.
     *
     * @param string The string to add color to.
     * @param color  The color.
     * @return The string with the added color for output to the console.
     */
    public static String color8Bit(String string, int color) {
        if ((color < 0) || (color > 255)) {
            return string;
        }
        
        return effect(codeKey(color8BitCode(color)), string);
    }
    
    /**
     * Adds 8-bit color and background to the string for output to the console.
     *
     * @param string     The string to add color to.
     * @param color      The color.
     * @param background The background color.
     * @return The string with the added color for output to the console.
     */
    public static String colorAndBackground8Bit(String string, int color, int background) {
        if ((color < 0) || (color > 255) || (background < 0) || (background > 255)) {
            return string;
        }
        
        return effect(codeKey(color8BitCode(color), background8BitCode(background)), string);
    }
    
    /**
     * Returns the code for an 8bit color.
     *
     * @param color The color.
     * @return The code for the 8bit color.
     */
    private static String color8BitCode(int color) {
        return "38;5;" + color;
    }
    
    /**
     * Returns the code for an 8bit background.
     *
     * @param color The color.
     * @return The code for the 8bit background.
     */
    private static String background8BitCode(int color) {
        return "48;5;" + color;
    }
    
    /**
     * Adds 24-bit color to the string for output to the console.
     *
     * @param string The string to add color to.
     * @param red    The red element of the color.
     * @param green  The green element of the color.
     * @param blue   The blue element of the color.
     * @return The string with the added color for output to the console.
     */
    public static String color24Bit(String string, int red, int green, int blue) {
        if ((red < 0) || (red > 255) || (green < 0) || (green > 255) || (blue < 0) || (blue > 255)) {
            return string;
        }
        
        return effect(codeKey(color24BitCode(red, green, blue)), string);
    }
    
    /**
     * Adds 24-bit color and background to the string for output to the console.
     *
     * @param string  The string to add color to.
     * @param red     The red element of the color.
     * @param green   The green element of the color.
     * @param blue    The blue element of the color.
     * @param redBg   The red element of the background color.
     * @param greenBg The green element of the background color.
     * @param blueBg  The blue element of the background color.
     * @return The string with the added effects and color for output to the console.
     */
    public static String colorAndBackground24Bit(String string, int red, int green, int blue, int redBg, int greenBg, int blueBg) {
        if ((red < 0) || (red > 255) || (green < 0) || (green > 255) || (blue < 0) || (blue > 255) ||
                (redBg < 0) || (redBg > 255) || (greenBg < 0) || (greenBg > 255) || (blueBg < 0) || (blueBg > 255)) {
            return string;
        }
        
        return effect(codeKey(color24BitCode(red, green, blue), background24BitCode(redBg, greenBg, blueBg)), string);
    }
    
    /**
     * Returns the code for an 24bit color.
     *
     * @param red   The red element of the color.
     * @param green The green element of the color.
     * @param blue  The blue element of the color.
     * @return The code for the 24bit color.
     */
    private static String color24BitCode(int red, int green, int blue) {
        return "38;2;" + red + ';' + green + ';' + blue;
    }
    
    /**
     * Returns the code for an 24bit background.
     *
     * @param red   The red element of the background.
     * @param green The green element of the background.
     * @param blue  The blue element of the background.
     * @return The code for the 24bit background.
     */
    private static String background24BitCode(int red, int green, int blue) {
        return "48;2;" + red + ';' + green + ';' + blue;
    }
    
    /**
     * Adds effects and color to the string for output to the console.
     *
     * @param string  The string to add effects and color to.
     * @param color   The color.
     * @param effects The effects to add.
     * @return The string with the added effects and color for output to the console.
     * @see #stringEffects(String, ConsoleEffect...)
     */
    public static String stringEffectsWithColor(String string, ConsoleEffect color, ConsoleEffect... effects) {
        List<ConsoleEffect> effectList = new ArrayList<>(Arrays.asList(effects));
        effectList.add(color);
        
        return stringEffects(string, effectList.toArray(new ConsoleEffect[] {}));
    }
    
    /**
     * Adds effects and color and background to the string for output to the console.
     *
     * @param string     The string to add effects and color to.
     * @param color      The color.
     * @param background The background color.
     * @param effects    The effects to add.
     * @return The string with the added effects and color for output to the console.
     * @see #stringEffects(String, ConsoleEffect...)
     */
    public static String stringEffectsWithColorAndBackground(String string, ConsoleEffect color, ConsoleEffect background, ConsoleEffect... effects) {
        List<ConsoleEffect> effectList = new ArrayList<>(Arrays.asList(effects));
        effectList.add(color);
        effectList.add(background);
        
        return stringEffects(string, effectList.toArray(new ConsoleEffect[] {}));
    }
    
    /**
     * Adds effects and 8-bit color to the string for output to the console.
     *
     * @param string  The string to add effects and color to.
     * @param color   The color.
     * @param effects The effects to add.
     * @return The string with the added effects and color for output to the console.
     */
    public static String stringEffectsWithColor8Bit(String string, int color, ConsoleEffect... effects) {
        if ((color < 0) || (color > 255)) {
            return string;
        }
        
        return effect(codeKey(effectSetCode(effects), color8BitCode(color)), string);
    }
    
    /**
     * Adds effects and 8-bit color and background to the string for output to the console.
     *
     * @param string     The string to add effects and color to.
     * @param color      The color.
     * @param background The background color.
     * @param effects    The effects to add.
     * @return The string with the added effects and color for output to the console.
     */
    public static String stringEffectsWithColorAndBackground8Bit(String string, int color, int background, ConsoleEffect... effects) {
        if ((color < 0) || (color > 255) || (background < 0) || (background > 255)) {
            return string;
        }
        
        return effect(codeKey(effectSetCode(effects), color8BitCode(color), background8BitCode(background)), string);
    }
    
    /**
     * Adds effects and 24-bit color to the string for output to the console.
     *
     * @param string  The string to add effects and color to.
     * @param red     The red element of the color.
     * @param green   The green element of the color.
     * @param blue    The blue element of the color.
     * @param effects The effects to add.
     * @return The string with the added effects and color for output to the console.
     */
    public static String stringEffectsWithColor24Bit(String string, int red, int green, int blue, ConsoleEffect... effects) {
        if ((red < 0) || (red > 255) || (green < 0) || (green > 255) || (blue < 0) || (blue > 255)) {
            return string;
        }
        
        return effect(codeKey(effectSetCode(effects), color24BitCode(red, green, blue)), string);
    }
    
    /**
     * Adds effects and 24-bit color and background to the string for output to the console.
     *
     * @param string  The string to add effects and color to.
     * @param red     The red element of the color.
     * @param green   The green element of the color.
     * @param blue    The blue element of the color.
     * @param redBg   The red element of the background color.
     * @param greenBg The green element of the background color.
     * @param blueBg  The blue element of the background color.
     * @param effects The effects to add.
     * @return The string with the added effects and color for output to the console.
     */
    public static String stringEffectsWithColorAndBackground24Bit(String string, int red, int green, int blue, int redBg, int greenBg, int blueBg, ConsoleEffect... effects) {
        if ((red < 0) || (red > 255) || (green < 0) || (green > 255) || (blue < 0) || (blue > 255) ||
                (redBg < 0) || (redBg > 255) || (greenBg < 0) || (greenBg > 255) || (blueBg < 0) || (blueBg > 255)) {
            return string;
        }
        
        return effect(codeKey(effectSetCode(effects), color24BitCode(red, green, blue), background24BitCode(redBg, greenBg, blueBg)), string);
    }
    
}
