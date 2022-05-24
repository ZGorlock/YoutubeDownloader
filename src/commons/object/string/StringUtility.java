/*
 * File:    StringUtility.java
 * Package: commons.object.string
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object.string;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.math.BoundUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional string functionality.
 */
public final class StringUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(StringUtility.class);
    
    
    //Constants
    
    /**
     * A string of vowel characters.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static final String VOWEL_CHARS = "AEIOU";
    
    /**
     * A string of consonant characters.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static final String CONSONANT_CHARS = "BCDFGHJKLMNPQRSTVWXYZ";
    
    /**
     * A list of operator tokens.
     */
    public static final List<String> OPERATOR_TOKENS = Arrays.asList("+", "-", "*", "/", "\\", "%", ">", "<", "!", "==", "!=", "<>", ">=", "<=");
    
    /**
     * A pattern for extracting the starting indent of a string.
     */
    public static final Pattern INDENT_SPACE_PATTERN = Pattern.compile("^(?<indent>\\s*(?:(?:\\d+\\.\\s*)|(?:\\*\\s*))?).*");
    
    
    //Enums
    
    /**
     * An enumeration of boxText Box Types.
     */
    public enum BoxType {
        
        //Values
        
        NO_BOX,
        BOX,
        DOUBLE_BOX
        
    }
    
    
    //Static Methods
    
    /**
     * Determines if a string is null or empty.
     *
     * @param string The string.
     * @return Whether the string is null or empty.
     */
    public static boolean isNullOrEmpty(String string) {
        return (string == null) || string.isEmpty();
    }
    
    /**
     * Determines if a string is null or blank.
     *
     * @param string The string.
     * @return Whether the string is null or blank.
     */
    public static boolean isNullOrBlank(String string) {
        return (string == null) || string.isBlank();
    }
    
    /**
     * Determines if a string equals another string.
     *
     * @param str1 The first string.
     * @param str2 The second string.
     * @return Whether the strings are equal.
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }
    
    /**
     * Determines if a string equals another string, regardless of case.
     *
     * @param str1 The first string.
     * @param str2 The second string.
     * @return Whether the strings are equal, regardless of case.
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return (str1 == null) ? (str2 == null) :
               str1.equalsIgnoreCase(str2);
    }
    
    /**
     * Determines if a string contains a substring.
     *
     * @param string The string.
     * @param search The substring to search for.
     * @return Whether or not the string contains the substring.
     */
    public static boolean contains(String string, String search) {
        return (string != null) && (search != null) &&
                string.contains(search);
    }
    
    /**
     * Determines if a string contains a substring, regardless of case.
     *
     * @param string The string.
     * @param search The substring to search for.
     * @return Whether or not the string contains the substring, regardless of case.
     */
    public static boolean containsIgnoreCase(String string, String search) {
        return (string != null) && (search != null) &&
                string.toUpperCase().contains(search.toUpperCase());
    }
    
    /**
     * Determines if a string contains any of a set of substrings.
     *
     * @param string The string.
     * @param search The set of substrings to search for.
     * @return Whether or not the string contains any of the set of substrings.
     */
    public static boolean containsAny(String string, String[] search) {
        return (string != null) &&
                Arrays.stream(search).filter(Objects::nonNull).anyMatch(string::contains);
    }
    
    /**
     * Determines if a string contains any of a set of substrings, regardless of case.
     *
     * @param string The string.
     * @param search The set of substrings to search for.
     * @return Whether or not the string contains any of the set of substrings, regardless of case.
     * @see #containsAny(String, String[])
     */
    public static boolean containsAnyIgnoreCase(String string, String[] search) {
        return (string != null) &&
                containsAny(string.toUpperCase(), Arrays.stream(search).filter(Objects::nonNull).map(String::toUpperCase).toArray(String[]::new));
    }
    
    /**
     * Determines if a string contains all of a set of substrings.
     *
     * @param string The string.
     * @param search The set of substrings to search for.
     * @return Whether or not the string contains all of the set of substrings.
     */
    public static boolean containsAll(String string, String[] search) {
        return (string != null) &&
                Arrays.stream(search).filter(Objects::nonNull).allMatch(string::contains);
    }
    
    /**
     * Determines if a string contains all of a set of substrings, regardless of case.
     *
     * @param string The string.
     * @param search The set of substrings to search for.
     * @return Whether or not the string contains all of the set of substrings, regardless of case.
     * @see #containsAll(String, String[])
     */
    public static boolean containsAllIgnoreCase(String string, String[] search) {
        return (string != null) &&
                containsAll(string.toUpperCase(), Arrays.stream(search).filter(Objects::nonNull).map(String::toUpperCase).toArray(String[]::new));
    }
    
    /**
     * Determines if a string contains a character.
     *
     * @param string The string.
     * @param search The character to search for.
     * @return Whether or not the string contains the character.
     */
    public static boolean containsChar(String string, char search) {
        return (string != null) &&
                (string.indexOf(search) != -1);
    }
    
    /**
     * Determines if a string contains a character, regardless of case.
     *
     * @param string The string.
     * @param search The character to search for.
     * @return Whether or not the string contains the character, regardless of case.
     * @see #containsChar(String, char)
     */
    public static boolean containsCharIgnoreCase(String string, char search) {
        return (string != null) &&
                containsChar(string.toUpperCase(), Character.toUpperCase(search));
    }
    
    /**
     * Determines if a string contains any of a set of characters.
     *
     * @param string The string.
     * @param search The set of characters to search for.
     * @return Whether or not the string contains any of the set of characters.
     */
    public static boolean containsAnyChar(String string, Character[] search) {
        return (string != null) &&
                Arrays.stream(search).filter(Objects::nonNull).anyMatch(e -> (string.indexOf(e) != -1));
    }
    
    /**
     * Determines if a string contains any of a set of characters, regardless of case.
     *
     * @param string The string.
     * @param search The set of characters to search for.
     * @return Whether or not the string contains any of the set of characters, regardless of case.
     * @see #containsAnyChar(String, Character[])
     */
    public static boolean containsAnyCharIgnoreCase(String string, Character[] search) {
        return (string != null) &&
                containsAnyChar(string.toUpperCase(), Arrays.stream(search).filter(Objects::nonNull).map(Character::toUpperCase).toArray(Character[]::new));
    }
    
    /**
     * Reverses a string.
     *
     * @param string The string.
     * @return The reversed string.
     */
    public static String reverse(String string) {
        return new StringBuilder(string).reverse().toString();
    }
    
    /**
     * Formats a string with arguments.
     *
     * @param string    The format string.
     * @param arguments The arguments.
     * @return The formatted string.
     */
    public static String format(String string, Object... arguments) {
        final String argumentPlaceholder = Pattern.quote("{}");
        return Arrays.stream(arguments).map(Objects::toString)
                .map(e -> e.replaceAll(argumentPlaceholder, "")).map(Matcher::quoteReplacement)
                .reduce(string, (s, e) -> s.replaceFirst(argumentPlaceholder, e));
    }
    
    /**
     * Converts a code point to a character.
     *
     * @param codePoint The code point.
     * @return The corresponding character.
     */
    public static char toChar(int codePoint) {
        return (char) codePoint;
    }
    
    /**
     * Converts a string to a stream of characters.
     *
     * @param string The string.
     * @return The stream of characters of the string.
     * @see #toChar(int)
     */
    public static Stream<Character> charStream(String string) {
        return string.chars().mapToObj(StringUtility::toChar);
    }
    
    /**
     * Converts a string to a stream of string characters.
     *
     * @param string The string.
     * @return The stream of string characters of the string.
     * @see #charStream(String)
     */
    public static Stream<String> stringStream(String string) {
        return charStream(string).map(String::valueOf);
    }
    
    /**
     * Trims the whitespace off of the front and back ends of a string.
     *
     * @param string The string to trim.
     * @return The trimmed string.
     * @see #lTrim(String)
     * @see #rTrim(String)
     */
    public static String trim(String string) {
        return lTrim(rTrim(string));
    }
    
    /**
     * Trims the whitespace off the left end of a string.
     *
     * @param string The string to trim.
     * @return The trimmed string.
     */
    public static String lTrim(String string) {
        return string.replaceAll("^[\\s\0]+", "");
    }
    
    /**
     * Trims the whitespace off the right end of a string.
     *
     * @param string The string to trim.
     * @return The trimmed string.
     */
    public static String rTrim(String string) {
        return string.replaceAll("[\\s\0]+$", "");
    }
    
    /**
     * Removes the first n characters from the beginning of a string and the last n characters from the end of a string.
     *
     * @param string The string to skin.
     * @param skin   The number of characters to skin.
     * @return The string with the first n characters and last n characters removed.
     * @see #lShear(String, int)
     * @see #rShear(String, int)
     */
    public static String skin(String string, int skin) {
        return lShear(rShear(string, skin), skin);
    }
    
    /**
     * Removes the first n characters from the beginning of a string.
     *
     * @param string The string to shear.
     * @param shear  The number of characters to shear.
     * @return The string with the first n characters removed.
     */
    public static String lShear(String string, int shear) {
        if (shear <= 0) {
            return string;
        }
        if (shear >= string.length()) {
            return "";
        }
        return string.substring(shear);
    }
    
    /**
     * Removes the last n characters from the end of a string.
     *
     * @param string The string to shear.
     * @param shear  The number of characters to shear.
     * @return The string with the last n characters removed.
     */
    public static String rShear(String string, int shear) {
        if (shear <= 0) {
            return string;
        }
        if (shear >= string.length()) {
            return "";
        }
        return string.substring(0, (string.length() - shear));
    }
    
    /**
     * Returns the first n characters from the beginning of a string and the last n characters from the end of a string.
     *
     * @param string The string to gut.
     * @param skin   The number of characters to not gut.
     * @return The first n characters and the last n characters of the string.
     * @see #lSnip(String, int)
     * @see #rSnip(String, int)
     */
    public static String gut(String string, int skin) {
        if (skin >= (string.length() / 2)) {
            return string;
        }
        return lSnip(string, skin) + rSnip(string, skin);
    }
    
    /**
     * Returns the first n characters from the beginning of a string.
     *
     * @param string The string to snip.
     * @param snip   The number of characters to return.
     * @return The first n characters of the string.
     */
    public static String lSnip(String string, int snip) {
        if (snip <= 0) {
            return "";
        }
        if (snip >= string.length()) {
            return string;
        }
        return string.substring(0, snip);
    }
    
    /**
     * Returns the last n characters from the end of a string.
     *
     * @param string The string to snip.
     * @param snip   The number of characters to return.
     * @return The last n characters of the string.
     */
    public static String rSnip(String string, int snip) {
        if (snip <= 0) {
            return "";
        }
        if (snip >= string.length()) {
            return string;
        }
        return string.substring(string.length() - snip);
    }
    
    /**
     * Pads a string on both sides to a specified length.
     *
     * @param string  The string to pad.
     * @param size    The target size of the string.
     * @param padding The character to pad with.
     * @return The padded string.
     * @see #padAbsolute(String, int, char)
     */
    public static String pad(String string, int size, char padding) {
        return (string.length() >= size) ? string :
               padAbsolute(string, ((size - string.length()) / 2), padding);
    }
    
    /**
     * Pads a string on both sides to a specified length.
     *
     * @param string The string to pad.
     * @param size   The target size of the string.
     * @return The padded string.
     * @see #pad(String, int, char)
     */
    public static String pad(String string, int size) {
        return pad(string, size, ' ');
    }
    
    /**
     * Pads a string on both sides with a specified amount of padding.
     *
     * @param string  The string to pad.
     * @param size    The amount of padding.
     * @param padding The character to pad with.
     * @return The padded string.
     */
    public static String padAbsolute(String string, int size, char padding) {
        final String pad = fillStringOfLength(padding, size);
        return pad + string + pad;
    }
    
    /**
     * Pads a string on both sides with a specified amount of padding.
     *
     * @param string The string to pad.
     * @param size   The amount of padding.
     * @return The padded string.
     * @see #padAbsolute(String, int, char)
     */
    public static String padAbsolute(String string, int size) {
        return padAbsolute(string, size, ' ');
    }
    
    /**
     * Pads a string on the left to a specified length.
     *
     * @param string  The string to pad.
     * @param size    The target size of the string.
     * @param padding The character to pad with.
     * @return The padded string.
     * @see #padLeftAbsolute(String, int, char)
     */
    public static String padLeft(String string, int size, char padding) {
        return (string.length() >= size) ? string :
               padLeftAbsolute(string, (size - string.length()), padding);
    }
    
    /**
     * Pads a string on the left to a specified length.
     *
     * @param string The string to pad.
     * @param size   The target size of the string.
     * @return The padded string.
     * @see #padLeft(String, int, char)
     */
    public static String padLeft(String string, int size) {
        return padLeft(string, size, ' ');
    }
    
    /**
     * Pads a string on the left with a specified amount of padding.
     *
     * @param string  The string to pad.
     * @param size    The amount of padding.
     * @param padding The character to pad with.
     * @return The padded string.
     */
    public static String padLeftAbsolute(String string, int size, char padding) {
        return fillStringOfLength(padding, size) + string;
    }
    
    /**
     * Pads a string on the left with a specified amount of padding.
     *
     * @param string The string to pad.
     * @param size   The amount of padding.
     * @return The padded string.
     * @see #padLeftAbsolute(String, int, char)
     */
    public static String padLeftAbsolute(String string, int size) {
        return padLeftAbsolute(string, size, ' ');
    }
    
    /**
     * Pads a string on the right to a specified length.
     *
     * @param string  The string to pad.
     * @param size    The target size of the string.
     * @param padding The character to pad with.
     * @return The padded string.
     * @see #padRightAbsolute(String, int, char)
     */
    public static String padRight(String string, int size, char padding) {
        return (string.length() >= size) ? string :
               padRightAbsolute(string, (size - string.length()), padding);
    }
    
    /**
     * Pads a string on the right to a specified length.
     *
     * @param string The string to pad.
     * @param size   The target size of the string.
     * @return The padded string.
     * @see #padRight(String, int, char)
     */
    public static String padRight(String string, int size) {
        return padRight(string, size, ' ');
    }
    
    /**
     * Pads a string on the right with a specified amount of padding.
     *
     * @param string  The string to pad.
     * @param size    The amount of padding.
     * @param padding The character to pad with.
     * @return The padded string.
     */
    public static String padRightAbsolute(String string, int size, char padding) {
        return string + fillStringOfLength(padding, size);
    }
    
    /**
     * Pads a string on the left with a specified amount of padding.
     *
     * @param string The string to pad.
     * @param size   The amount of padding.
     * @return The padded string.
     * @see #padRightAbsolute(String, int, char)
     */
    public static String padRightAbsolute(String string, int size) {
        return padRightAbsolute(string, size, ' ');
    }
    
    /**
     * Pads a number string with leading zeros to fit a particular size.
     *
     * @param string The number string to pad.
     * @param size   The specified size of the final string.
     * @return The padded number string.
     * @see #padLeft(String, int, char)
     */
    public static String padZero(String string, int size) {
        return padLeft(string, size, '0');
    }
    
    /**
     * Pads a number string with leading zeros to fit a particular size.
     *
     * @param num  The number to pad.
     * @param size The specified size of the final string.
     * @return The padded number string.
     * @see #padZero(String, int)
     */
    public static String padZero(int num, int size) {
        return padZero(Integer.toString(num), size);
    }
    
    /**
     * Fixes double spaces in a string.
     *
     * @param string The string to operate on.
     * @return The string with double spaces replaced with single spaces.
     */
    public static String fixSpaces(String string) {
        return StringUtility.trim(string.replaceAll("\\s+", " "));
    }
    
    /**
     * Fixes file separators in a string.
     *
     * @param string The string to operate on.
     * @return The string with file separators standardized.
     */
    public static String fixFileSeparators(String string) {
        return string.replace("\\", "/").replaceAll("/+", "/");
    }
    
    /**
     * Quotes a string.
     *
     * @param string       The string to quote.
     * @param singleQuotes Whether to use single quotes, otherwise double quotes will be used.
     * @return The quoted string.
     * @see #padAbsolute(String, int, char)
     */
    public static String quote(String string, boolean singleQuotes) {
        return padAbsolute(string, 1, (singleQuotes ? '\'' : '"'));
    }
    
    /**
     * Quotes a string.
     *
     * @param string The string to quote.
     * @return The quoted string.
     * @see #quote(String, boolean)
     */
    public static String quote(String string) {
        return quote(string, false);
    }
    
    /**
     * Creates a string of the length specified filled with spaces.
     *
     * @param num The length to make the string.
     * @return A new string filled with spaces to the length specified.
     * @see #fillStringOfLength(char, int)
     */
    public static String spaces(int num) {
        return fillStringOfLength(' ', num);
    }
    
    /**
     * Creates a string of the length specified filled with the character specified.
     *
     * @param fill The character to fill the string with.
     * @param size The length to make the string.
     * @return A new string filled with the specified character to the length specified.
     */
    public static String fillStringOfLength(char fill, int size) {
        final char[] chars = new char[Math.max(size, 0)];
        Arrays.fill(chars, fill);
        return new String(chars);
    }
    
    /**
     * Repeats a string a certain number of times.
     *
     * @param string The string to repeat.
     * @param num    The number of times to repeat the string.
     * @return A string containing the base string repeated a number of times.
     * @see String#repeat(int)
     */
    public static String repeatString(String string, int num) {
        return (num <= 0) ? "" :
               String.valueOf(string).repeat(num);
    }
    
    /**
     * Removes the whitespace from a string.
     *
     * @param string The string to operate on.
     * @return The string with all whitespace characters removed.
     */
    public static String removeWhiteSpace(String string) {
        return isNullOrBlank(string) ? "" :
               string.replaceAll("[\\s\0]+", "");
    }
    
    /**
     * Removes the punctuation from a string.
     *
     * @param string The string to operate on.
     * @return The string with punctuation removed.
     */
    public static String removePunctuation(String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char current = string.charAt(i);
            if (isAlphanumeric(current) || isWhitespace(current)) {
                sb.append(string.charAt(i));
            }
        }
        return sb.toString();
    }
    
    /**
     * Gently removes the punctuation from a string.
     *
     * @param string The string to operate on.
     * @param save   A list of punctuation characters to ignore.
     * @return The string with punctuation gently removed.
     */
    public static String removePunctuationSoft(String string, List<Character> save) {
        return charStream(string)
                .filter(c -> !isSymbol(c) || save.contains(c))
                .map(String::valueOf).collect(Collectors.joining());
    }
    
    /**
     * Removes the diacritical marks from a string.
     *
     * @param string The string to operate on.
     * @return The string with all diacritical marks removed.
     */
    public static String removeDiacritics(String string) {
        return isNullOrBlank(string) ? "" :
               Normalizer.normalize(string, Normalizer.Form.NFD)
                       .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "")
                       .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS_SUPPLEMENT}+", "");
    }
    
    /**
     * Removes the console escape codes from a string.
     *
     * @param string The string to operate on.
     * @return The string with all console escape codes removed.
     */
    public static String removeConsoleEscapeCharacters(String string) {
        return string.replaceAll("\u001B[^m]*m", "");
    }
    
    /**
     * Determines if a character is alphanumeric or not.
     *
     * @param c The character.
     * @return Whether the character is alphanumeric or not.
     * @see Character#isLetterOrDigit(int)
     */
    public static boolean isAlphanumeric(char c) {
        return Character.isLetterOrDigit(c);
    }
    
    /**
     * Determines if a string is alphanumeric or not.
     *
     * @param string The string.
     * @return Whether the string is alphanumeric or not.
     * @see #isAlphanumeric(char)
     */
    public static boolean isAlphanumeric(String string) {
        return !isNullOrEmpty(string) && charStream(string).allMatch(StringUtility::isAlphanumeric);
    }
    
    /**
     * Determines if a character is alphabetic or not.
     *
     * @param c The character.
     * @return Whether the character is alphabetic or not.
     * @see Character#isAlphabetic(int)
     */
    public static boolean isAlphabetic(char c) {
        return Character.isAlphabetic(c);
    }
    
    /**
     * Determines if a string is alphabetic or not.
     *
     * @param string The string.
     * @return Whether the string is alphabetic or not.
     * @see #isAlphabetic(char)
     */
    public static boolean isAlphabetic(String string) {
        return !isNullOrEmpty(string) && charStream(string).allMatch(StringUtility::isAlphabetic);
    }
    
    /**
     * Determines if a character is a vowel or not.
     *
     * @param c The character.
     * @return Whether the character is a vowel or not.
     * @see #VOWEL_CHARS
     */
    public static boolean isVowel(char c) {
        return containsCharIgnoreCase(VOWEL_CHARS, c);
    }
    
    /**
     * Determines if a character is a consonant or not.
     *
     * @param c The character.
     * @return Whether the character is a consonant or not.
     * @see #CONSONANT_CHARS
     */
    public static boolean isConsonant(char c) {
        return containsCharIgnoreCase(CONSONANT_CHARS, c);
    }
    
    /**
     * Determines if a character is numeric or not.
     *
     * @param c The character.
     * @return Whether the character is numeric or not.
     * @see Character#isDigit(char)
     */
    public static boolean isNumeric(char c) {
        return Character.isDigit(c);
    }
    
    /**
     * Determines if a string is numeric or not.
     *
     * @param string The string.
     * @return Whether the string is numeric or not.
     * @see #isNumeric(char)
     */
    public static boolean isNumeric(String string) {
        return !isNullOrEmpty(string) && charStream(string).allMatch(StringUtility::isNumeric);
    }
    
    /**
     * Determines if a character is a symbol or not.
     *
     * @param c The character.
     * @return Whether the character is a symbol or not.
     * @see #isAlphanumeric(char)
     * @see #isWhitespace(char)
     */
    public static boolean isSymbol(char c) {
        return !(isAlphanumeric(c) || isWhitespace(c));
    }
    
    /**
     * Determines if a string is punctuation or not.
     *
     * @param string The string.
     * @return Whether the string is punctuation or not.
     * @see #isSymbol(char)
     */
    public static boolean isSymbol(String string) {
        return !isNullOrEmpty(string) && charStream(string).allMatch(StringUtility::isSymbol);
    }
    
    /**
     * Determines if a character is whitespace or not.
     *
     * @param c The character.
     * @return Whether the character is whitespace or not.
     * @see Character#isWhitespace(char)
     */
    public static boolean isWhitespace(char c) {
        return Character.isWhitespace(c) || (c == '\0');
    }
    
    /**
     * Determines if a string is whitespace or not.
     *
     * @param string The string.
     * @return Whether the string is whitespace or not.
     * @see #isWhitespace(char)
     */
    public static boolean isWhitespace(String string) {
        return !isNullOrEmpty(string) && charStream(string).allMatch(StringUtility::isWhitespace);
    }
    
    /**
     * Determines if a string token represents a number of not.
     *
     * @param token The token to examine.
     * @return Whether the token represents a number of not.
     */
    public static boolean tokenIsNum(String token) {
        try {
            Double.parseDouble(token);
        } catch (NumberFormatException ignored) {
            return false;
        }
        return true;
    }
    
    /**
     * Determines if a string token represents an operator or not.
     *
     * @param token The token to examine.
     * @return Whether the token represents an operator or not.
     * @see #OPERATOR_TOKENS
     */
    public static boolean tokenIsOperator(String token) {
        return OPERATOR_TOKENS.contains(token);
    }
    
    /**
     * Replaces a character in a string with another character.
     *
     * @param string  The string to operate on.
     * @param index   The index in the string to replace at.
     * @param replace The character to replace with at the specified index.
     * @return The string with the replacement performed.
     */
    public static String replaceCharAt(String string, int index, char replace) {
        if ((index < 0) || (index > (string.length() - 1))) {
            return string;
        }
        return lSnip(string, index) + replace + lShear(string, index + 1);
    }
    
    /**
     * Inserts a character in a string at.
     *
     * @param string The string to operate on.
     * @param index  The index in the string to insert at.
     * @param insert The character to insert at the specified index.
     * @return The string with the insertion performed.
     */
    public static String insertCharAt(String string, int index, char insert) {
        if ((index < 0) || (index > (string.length()))) {
            return string;
        }
        if (index == string.length()) {
            return string + insert;
        }
        return lSnip(string, index) + insert + lShear(string, index);
    }
    
    /**
     * Deletes a character from a string.
     *
     * @param string The string to operate on.
     * @param index  The index in the string to delete from.
     * @return The string with the deletion performed.
     */
    public static String deleteCharAt(String string, int index) {
        if ((index < 0) || (index > (string.length() - 1))) {
            return string;
        }
        return lSnip(string, index) + lShear(string, index + 1);
    }
    
    /**
     * Replaces a substring in a string with another substring.
     *
     * @param string     The string to operate on.
     * @param startIndex The starting index of the substring in the string.
     * @param endIndex   The ending index of the substring in the string.
     * @param replace    The substring to replace with.
     * @return The string with the replacement performed.
     */
    public static String replaceSubstringAt(String string, int startIndex, int endIndex, String replace) {
        if ((startIndex < 0) || (startIndex > string.length()) ||
                (endIndex < 0) || (endIndex > string.length()) ||
                (startIndex > endIndex)) {
            return string;
        }
        if (startIndex == string.length()) {
            return string + replace;
        }
        if (endIndex == string.length()) {
            return lSnip(string, startIndex) + replace;
        }
        return lSnip(string, startIndex) + replace + lShear(string, endIndex);
    }
    
    /**
     * Inserts a substring in a string.
     *
     * @param string The string to operate on.
     * @param index  The index in the string to insert at.
     * @param insert The substring to insert.
     * @return The string with the insertion performed.
     */
    public static String insertSubstringAt(String string, int index, String insert) {
        if ((index < 0) || (index > (string.length()))) {
            return string;
        }
        if (index == string.length()) {
            return string + insert;
        }
        return lSnip(string, index) + insert + lShear(string, index);
    }
    
    /**
     * Deletes a substring from a string.
     *
     * @param string     The string to operate on.
     * @param startIndex The starting index of the substring in the string.
     * @param endIndex   The ending index of the substring in the string.
     * @return The string with the deletion performed.
     */
    public static String deleteSubstringAt(String string, int startIndex, int endIndex) {
        if ((startIndex < 0) || (startIndex > (string.length() - 1)) ||
                (endIndex < 0) || (endIndex > string.length()) ||
                (startIndex > endIndex)) {
            return string;
        }
        if (endIndex == string.length()) {
            return lSnip(string, startIndex);
        }
        return lSnip(string, startIndex) + lShear(string, endIndex);
    }
    
    /**
     * Determines the number of occurrences of a pattern in a string.
     *
     * @param string  The string to search in.
     * @param pattern The pattern to find the number of occurrences of.
     * @return The number of occurrences of the pattern in the string.
     */
    public static int numberOfOccurrences(String string, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        
        int n = 0;
        while (m.find()) {
            n++;
        }
        return n;
    }
    
    /**
     * Determines the number of occurrences of a pattern in a string.
     *
     * @param string  The string to search in.
     * @param pattern The pattern to find the number of occurrences of.
     * @param start   The index to start looking from.
     * @param end     The index to stop looking at.
     * @return The number of occurrences of the pattern in the string.
     * @see #numberOfOccurrences(String, String)
     */
    public static int numberOfOccurrences(String string, String pattern, int start, int end) {
        if ((start == 0) && (end == string.length() - 1)) {
            return numberOfOccurrences(string, pattern);
        }
        return numberOfOccurrences(string.substring(start, end), pattern);
    }
    
    /**
     * Converts a string to camel case.<br>
     * Usage: "The Variable_Name" = "theVariableName"
     *
     * @param string The string to convert.
     * @return The string converted to camel case.
     */
    public static String toCamelCase(String string) {
        string = string.replaceAll("[\\-_:~.]", " ");
        string = trim(toUpperTitleCase(string));
        string = string.replaceAll("\\s+", "~");
        string = Character.toLowerCase(string.charAt(0)) + lShear(string, 1);
        
        boolean lower = true;
        StringBuilder camelCase = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            char c1 = (string.length() > (i + 1)) ? string.charAt(i + 1) : '\0';
            if ((c == '~') && (c1 != '\0')) {
                camelCase.append(Character.toUpperCase(c1));
                i++;
            } else {
                if (Character.isUpperCase(c) && !((c1 != '\0') && Character.isLowerCase(c1))) {
                    c = Character.toLowerCase(c);
                }
                camelCase.append(c);
            }
        }
        
        return camelCase.toString();
    }
    
    /**
     * Converts a string to pascal case.<br>
     * Usage: "the Variable_Name" = "TheVariableName"
     *
     * @param string The string to convert.
     * @return The string converted to pascal case.
     */
    public static String toPascalCase(String string) {
        string = toCamelCase(string);
        return Character.toUpperCase(string.charAt(0)) + lShear(string, 1);
    }
    
    /**
     * Converts a string to constant case.<br>
     * Usage: "a Constant.name" = "A_CONSTANT_NAME"
     *
     * @param string The string to convert.
     * @return The string converted to constant case.
     */
    public static String toConstantCase(String string) {
        string = toCamelCase(string);
        
        StringBuilder constantCase = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            char c1 = (string.length() > (i + 1)) ? string.charAt(i + 1) : '\0';
            if (Character.isUpperCase(c) && (c1 != '\0') && Character.isLowerCase(c1)) {
                constantCase.append('_');
            }
            constantCase.append(c);
        }
        
        return constantCase.toString().toUpperCase();
    }
    
    /**
     * Converts a string to title case.<br>
     * Usage: "the title of the book" = "The Title of the Book" (with filter)
     * Usage: "the title of the book" = "The Title Of The Book" (without filter)
     *
     * @param string The string to convert.
     * @param filter Whether or not to filter insignificant words.
     * @return The string converted to title case.
     */
    private static String toTitleCase(String string, boolean filter) {
        if (isNullOrEmpty(string)) {
            return "";
        }
        final List<String> lowercase = Arrays.asList("a", "an", "the", "and", "but", "for", "of", "at", "by", "from", "is");
        
        String[] words = string.split("\\s+");
        StringBuilder titleCase = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) {
                continue;
            }
            if (titleCase.length() > 0) {
                titleCase.append(' ');
            }
            
            if (filter && !first && (i < (words.length - 1)) && lowercase.contains(word.toLowerCase())) {
                titleCase.append(word.toLowerCase());
            } else {
                titleCase.append(Character.toUpperCase(word.charAt(0))).append(lShear(word, 1));
            }
            first = false;
        }
        return titleCase.toString();
    }
    
    /**
     * Converts a string to title case.<br>
     * Usage: "the TITLE of the book" = "The TITLE of the Book"
     *
     * @param string The string to convert.
     * @return The string converted to title case.
     * @see #toTitleCase(String, boolean)
     */
    public static String toTitleCase(String string) {
        return toTitleCase(string, true);
    }
    
    /**
     * Converts a string to upper title case.<br>
     * Usage: "the TITLE of the book" = "The TITLE Of The Book"
     *
     * @param string The string to convert.
     * @return The string converted to upper title case.
     * @see #toTitleCase(String, boolean)
     */
    public static String toUpperTitleCase(String string) {
        return toTitleCase(string, false);
    }
    
    /**
     * Converts a string to sentence case.<br>
     * Usage: "The Title of the Book" = "The title of the book"
     *
     * @param string The string to convert.
     * @return The string converted to sentence case.
     */
    public static String toSentenceCase(String string) {
        return isNullOrEmpty(string) ? "" :
               Character.toUpperCase(string.charAt(0)) + lShear(string, 1).toLowerCase();
    }
    
    /**
     * Converts a string to an alternative unicode string.<br>
     * Usage: "This is unicode" = "𝚃𝚑𝚒𝚜 𝚒𝚜 𝚞𝚗𝚒𝚌𝚘𝚍𝚎"
     *
     * @param string The string to convert.
     * @return The alternative unicode string.
     */
    public static String toUnicodeAlternative(String string) {
        return string.chars().map(i ->
                        (BoundUtility.inBounds(i, (int) 'A', (int) 'Z')) ? (0xDE70 + (i - 'A')) :
                        (BoundUtility.inBounds(i, (int) 'a', (int) 'z')) ? (0xDE8A + (i - 'a')) :
                        (BoundUtility.inBounds(i, (int) '0', (int) '9')) ? (0xDFF6 + (i - '0')) : -i)
                .mapToObj(i -> (i > 0) ? String.valueOf(new char[] {(char) 0xD835, (char) i}) : String.valueOf((char) -i))
                .collect(Collectors.joining());
    }
    
    /**
     * Returns '[aA] {str}' or '[aA]n {str}' depending on the string.
     *
     * @param string    The string to justify.
     * @param uppercase Whether or not to capitalize the first letter.
     * @return '[aA] {str}' or '[aA]n {str}'.
     */
    public static String justifyAOrAn(String string, boolean uppercase) {
        return isNullOrBlank(string) ? "" :
               (uppercase ? 'A' : 'a') + (isVowel(string.charAt(0)) ? "n" : "") + ' ' + string;
    }
    
    /**
     * Returns 'a {str}' or 'an {str}' depending on the string.
     *
     * @param string The string to justify.
     * @return 'a {str}' or 'an {str}'.
     * @see #justifyAOrAn(String, boolean)
     */
    public static String justifyAOrAn(String string) {
        return justifyAOrAn(string, false);
    }
    
    /**
     * Returns '{quantity} {unit}' or '{quantity} {unit}s' depending on the quantity.
     *
     * @param quantity The quantity.
     * @param unit     The name of the unit.
     * @return '{quantity} {unit}' or '{quantity} {unit}s' depending on the quantity
     */
    public static String justifyQuantity(int quantity, String unit) {
        return isNullOrBlank(unit) ? String.valueOf(quantity) :
               String.valueOf(quantity) + ' ' + unit + ((quantity != 1) ? "s" : "");
    }
    
    /**
     * Returns a string representing a file.
     *
     * @param file     The file.
     * @param absolute Whether or not to use the absolute path.
     * @return The string representing the file.
     */
    public static String fileString(File file, boolean absolute) {
        return StringUtility.fixFileSeparators((absolute ? file.getAbsolutePath() : file.getPath()));
    }
    
    /**
     * Returns a string representing a file.
     *
     * @param file The file.
     * @return The string representing the file.
     * @see #fileString(File, boolean)
     */
    public static String fileString(File file) {
        return fileString(file, true);
    }
    
    /**
     * Tokenizes a passed string into its tokens and returns a list of those tokens.
     *
     * @param string The string to tokenize.
     * @param delim  The regex delimiter to separate tokens by.
     * @param hard   Whether or not to include empty tokens.
     * @return The list of all the tokens from the passed string.
     */
    public static List<String> tokenize(String string, String delim, boolean hard) {
        return new ArrayList<>(Arrays.asList(string.split(delim, (hard ? -1 : 0))));
    }
    
    /**
     * Tokenizes a passed string into its tokens and returns a list of those tokens.
     *
     * @param string The string to tokenize.
     * @param delim  The regex delimiter to separate tokens by.
     * @return The list of all the tokens from the passed string.
     * @see #tokenize(String, String, boolean)
     */
    public static List<String> tokenize(String string, String delim) {
        return tokenize(string, delim, false);
    }
    
    /**
     * Tokenizes a passed string into its tokens and returns a list of those tokens.
     *
     * @param string The string to tokenize.
     * @return The list of all the tokens from the passed string.
     * @see #tokenize(String, String)
     */
    public static List<String> tokenize(String string) {
        return tokenize(string, "\\s+");
    }
    
    /**
     * Tokenizes a string into a list of tokens of a certain length.
     *
     * @param string The string to tokenize.
     * @param length The length of the tokens.
     * @return The list of all the tokens from the passed string.
     */
    public static List<String> tokenize(String string, int length) {
        return new ArrayList<>(Arrays.asList(string.split("(?<=\\G.{" + length + "})")));
    }
    
    /**
     * Tokenizes a string into a list of tokens based on a list of valid tokens.
     *
     * @param string      The string to tokenize.
     * @param validTokens The list of valid tokens.
     * @param sortList    Whether or not to sort the valid tokens list for best performance.
     * @return The list of all the tokens from the passed string, or null when the string cannot be tokenized.
     */
    public static List<String> tokenize(String string, List<String> validTokens, boolean sortList) {
        final List<String> validTokenList = new ArrayList<>(validTokens);
        if (sortList) {
            validTokenList.sort((o1, o2) -> Integer.compare(o2.length(), o1.length()));
        }
        
        final Map<Character, String> placeholders = new HashMap<>();
        int originalMaxCharacter = (string + String.join("", validTokenList)).codePoints().max().orElse(0);
        int placeholderChar = originalMaxCharacter + 1;
        for (String validToken : validTokenList) {
            if (!string.contains(validToken)) {
                continue;
            }
            if (placeholderChar > 65535) {
                return null;
            }
            char placeholder = (char) placeholderChar++;
            placeholders.put(placeholder, validToken);
            string = string.replace(validToken, String.valueOf(placeholder));
        }
        
        int newMinCharacter = string.codePoints().min().orElse(0);
        if (newMinCharacter <= originalMaxCharacter) {
            return null;
        }
        
        return string.codePoints().mapToObj(StringUtility::toChar).map(placeholders::get).collect(Collectors.toList());
    }
    
    /**
     * Tokenizes a string into a list of tokens based on a list of valid tokens.
     *
     * @param string      The string to tokenize.
     * @param validTokens The list of valid tokens.
     * @return The list of all the tokens from the passed string, or null when the string cannot be tokenized.
     * @see #tokenize(String, List)
     */
    public static List<String> tokenize(String string, List<String> validTokens) {
        return tokenize(string, validTokens, true);
    }
    
    /**
     * Tokenizes a passed string into its a list of arguments delimited either by spaces or quotes.
     *
     * @param string The string to tokenize.
     * @return The list of all the args from the passed string.
     */
    public static List<String> tokenizeArgs(String string) {
        List<String> args = new ArrayList<>();
        
        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == ' ') {
                if ((argBuilder.length() > 0)) {
                    args.add(argBuilder.toString());
                    argBuilder = new StringBuilder();
                }
            } else if (c == '"') {
                for (i = i + 1; i < string.length(); i++) {
                    char k = string.charAt(i);
                    if (k == '"') {
                        args.add(argBuilder.toString());
                        argBuilder = new StringBuilder();
                        break;
                    } else {
                        argBuilder.append(k);
                    }
                }
            } else {
                argBuilder.append(c);
                if (i == (string.length() - 1)) {
                    args.add(argBuilder.toString());
                }
            }
        }
        
        return args;
    }
    
    /**
     * Detokenizes a passed list of tokens back into a string.
     *
     * @param tokens The list of tokens to detokenize.
     * @param delim  The delimiter to insert between tokens.
     * @return The string composed of the tokens in the passed list.
     */
    public static String detokenize(List<String> tokens, String delim) {
        return String.join(delim, tokens);
    }
    
    /**
     * Detokenizes a passed list of tokens back into a string.
     *
     * @param tokens The list of tokens to detokenize.
     * @return The string composed of the tokens in the passed list.
     * @see #detokenize(List, String)
     */
    public static String detokenize(List<String> tokens) {
        return detokenize(tokens, " ");
    }
    
    /**
     * Splits a passed string by line separators and returns a list of lines.
     *
     * @param string The string to split.
     * @return The list of the lines in the passed string.
     * @see #tokenize(String, String, boolean)
     */
    public static List<String> splitLines(String string) {
        return tokenize(string, "\\r?\\n", true);
    }
    
    /**
     * Unsplits a passed list of lines with line separators and returns a string.
     *
     * @param lines The list of lines to unsplit.
     * @return The string containing the lines in the passed list.
     * @see #detokenize(List, String)
     */
    public static String unsplitLines(List<String> lines) {
        return detokenize(lines, System.lineSeparator());
    }
    
    /**
     * Centers a text within a certain width.
     *
     * @param text  The text to center.
     * @param width The width to center the text within.
     * @return The centered text.
     * @see #pad(String, int)
     */
    public static String centerText(String text, int width) {
        return pad(text, width);
    }
    
    /**
     * Wraps text to fit a certain width.
     *
     * @param text        The text for format.
     * @param width       The width to limit the box of text to.
     * @param clean       Whether or not to honor words and preserve line indents.
     * @param breakIndent The number of additional spaces to add before a line that was wrapped.
     * @return The text formatted into a box.
     */
    public static List<String> wrapText(String text, int width, boolean clean, int breakIndent) {
        List<String> wrapped = new ArrayList<>();
        
        int spaces = 0;
        Matcher indentSpaceMatcher = INDENT_SPACE_PATTERN.matcher(text);
        if (indentSpaceMatcher.matches()) {
            String indentString = indentSpaceMatcher.group("indent");
            spaces = indentString.length();
            if ((breakIndent > 0) && indentString.endsWith(". ")) {
                breakIndent -= 2;
                breakIndent = Math.max(breakIndent, 0);
            }
        }
        String indent = spaces(spaces);
        String subIndent = spaces(breakIndent);
        
        if (text.isEmpty()) {
            wrapped.add(padRight(text, width));
            return wrapped;
        }
        
        boolean first = true;
        while (!text.isEmpty()) {
            if (!first) {
                text = (clean ? indent : "") + subIndent + text;
            }
            String work = lSnip(text, width);
            
            boolean addDash = false;
            int trueWidth = Math.min(width, text.length());
            int finalWidth = trueWidth;
            if (clean && !text.equals(work) && !isWhitespace(text.charAt(finalWidth))) {
                for (int i = work.length() - 1; i >= 0; i--) {
                    if (isWhitespace(work.charAt(i))) {
                        break;
                    }
                    finalWidth--;
                }
                if (finalWidth < ((trueWidth - (spaces + (first ? 0 : breakIndent))) / (width / 10.0))) {
                    finalWidth = width - 1;
                    addDash = true;
                }
            }
            
            work = lSnip(work, finalWidth);
            if (addDash) {
                work = work + '-';
            }
            work = padRight(work, width);
            wrapped.add(work);
            
            text = lShear(text, finalWidth);
            if (clean) {
                int internalSpaces = 0;
                for (int i = 0; i < text.length(); i++) {
                    if (!isWhitespace(text.charAt(i))) {
                        break;
                    }
                    internalSpaces++;
                }
                text = lShear(text, internalSpaces);
            }
            
            first = false;
        }
        
        return wrapped;
    }
    
    /**
     * Wraps text to fit a certain width.
     *
     * @param text  The text for format.
     * @param width The width to limit the box of text to.
     * @param clean Whether or not to honor words and preserve line indents.
     * @return The text formatted into a box.
     * @see #wrapText(String, int, boolean, int)
     */
    public static List<String> wrapText(String text, int width, boolean clean) {
        return wrapText(text, width, clean, 0);
    }
    
    /**
     * Wraps text to fit a certain width.
     *
     * @param text  The text for format.
     * @param width The width to limit the box of text to.
     * @return The text formatted into a box.
     * @see #wrapText(String, int, boolean)
     */
    public static List<String> wrapText(String text, int width) {
        return wrapText(text, width, false);
    }
    
    /**
     * Formats text to fit a certain width.
     *
     * @param text        The text to format.
     * @param width       The width to limit the box of text to.
     * @param clean       Whether or not to honor words and preserve line indents.
     * @param breakIndent The number of additional spaces to add before a line that was wrapped.
     * @param border      The number of spaces to border the right side of the box with.
     * @param box         The box type to add to the formatted text.
     * @return The text formatted into a box.
     */
    public static List<String> boxText(List<String> text, int width, boolean clean, int breakIndent, int border, BoxType box) {
        List<String> boxed = new ArrayList<>();
        
        for (String work : text) {
            boxed.addAll(wrapText(work, width - (border * 2), clean, breakIndent));
        }
        
        if (border > 0) {
            String borderIndent = spaces(border);
            for (int i = 0; i < boxed.size(); i++) {
                boxed.set(i, borderIndent + boxed.get(i) + borderIndent);
            }
        }
        
        if (box != BoxType.NO_BOX) {
            char horizontal;
            char vertical;
            char nwCorner;
            char neCorner;
            char seCorner;
            char swCorner;
            switch (box) {
                case BOX:
                    horizontal = '─';
                    vertical = '│';
                    nwCorner = '┌';
                    neCorner = '┐';
                    seCorner = '┘';
                    swCorner = '└';
                    break;
                case DOUBLE_BOX:
                    horizontal = '═';
                    vertical = '║';
                    nwCorner = '╔';
                    neCorner = '╗';
                    seCorner = '╝';
                    swCorner = '╚';
                    break;
                default:
                    return boxed;
            }
            
            for (int i = 0; i < boxed.size(); i++) {
                boxed.set(i, vertical + boxed.get(i) + vertical);
            }
            boxed.add(0, nwCorner + fillStringOfLength(horizontal, width) + neCorner);
            boxed.add(swCorner + fillStringOfLength(horizontal, width) + seCorner);
        }
        
        return boxed;
    }
    
    /**
     * Formats text to fit a certain width.
     *
     * @param text        The text to format.
     * @param width       The width to limit the box of text to.
     * @param clean       Whether or not to honor words and preserve line indents.
     * @param breakIndent The number of additional spaces to add before a line that was wrapped.
     * @param border      The number of spaces to border the right side of the box with.
     * @return The text formatted into a box.
     * @see #boxText(List, int, boolean, int, int, BoxType)
     */
    public static List<String> boxText(List<String> text, int width, boolean clean, int breakIndent, int border) {
        return boxText(text, width, clean, breakIndent, border, BoxType.NO_BOX);
    }
    
    /**
     * Formats text to fit a certain width.
     *
     * @param text        The text to format.
     * @param width       The width to limit the box of text to.
     * @param clean       Whether or not to honor words and preserve line indents.
     * @param breakIndent The number of additional spaces to add before a line that was wrapped.
     * @return The text formatted into a box.
     * @see #boxText(List, int, boolean, int, int)
     */
    public static List<String> boxText(List<String> text, int width, boolean clean, int breakIndent) {
        return boxText(text, width, clean, breakIndent, 0);
    }
    
    /**
     * Formats text to fit a certain width.
     *
     * @param text  The text to format.
     * @param width The width to limit the box of text to.
     * @param clean Whether or not to honor words and preserve line indents.
     * @return The text formatted into a box.
     * @see #boxText(List, int, boolean, int)
     */
    public static List<String> boxText(List<String> text, int width, boolean clean) {
        return boxText(text, width, clean, 0);
    }
    
    /**
     * Formats text to fit a certain width.
     *
     * @param text  The text to format.
     * @param width The width to limit the box of text to.
     * @return The text formatted into a box.
     * @see #boxText(List, int, boolean)
     */
    public static List<String> boxText(List<String> text, int width) {
        return boxText(text, width, true);
    }
    
}
