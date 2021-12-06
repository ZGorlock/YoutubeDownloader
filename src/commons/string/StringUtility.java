/*
 * File:    StringUtility.java
 * Package: commons.string
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.string;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
     * A regex pattern for an alphanumeric string.
     */
    public static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9]*");
    
    /**
     * A regex pattern for an alphabetic string.
     */
    public static final Pattern ALPHABETIC_PATTERN = Pattern.compile("[a-zA-Z]*");
    
    /**
     * A regex pattern for a numeric string.
     */
    public static final Pattern NUMERIC_PATTERN = Pattern.compile("-?(?:[0-9]*\\.)?[0-9]+");
    
    /**
     * A regex pattern for a symbol string.
     */
    public static final Pattern SYMBOL_PATTERN = Pattern.compile("[^a-zA-Z0-9]*");
    
    /**
     * A regex pattern for a whitespace string.
     */
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s\0]*");
    
    /**
     * A pattern for extracting the starting indent of a string.
     */
    public static final Pattern INDENT_SPACE_PATTERN = Pattern.compile("^(?<indent>\\s*(?:(?:\\d+\\.\\s*)|(?:\\*\\s*))?).*");
    
    
    //Enums
    
    /**
     * An enumeration of box types for the boxText method.
     */
    public enum BoxType {
        
        //Values
        
        NO_BOX,
        BOX,
        DOUBLE_BOX
        
    }
    
    
    //Functions
    
    /**
     * Tokenizes a passed string into its tokens and returns a list of those tokens.
     *
     * @param str   The string to tokenize.
     * @param delim The regex delimiter to separate tokens by.
     * @param hard  Whether or not to include empty tokens.
     * @return The list of all the tokens from the passed string.
     */
    public static List<String> tokenize(String str, String delim, boolean hard) {
        String[] lines = str.split(delim, (hard ? -1 : 0));
        return new ArrayList<>(Arrays.asList(lines));
    }
    
    /**
     * Tokenizes a passed string into its tokens and returns a list of those tokens.
     *
     * @param str   The string to tokenize.
     * @param delim The regex delimiter to separate tokens by.
     * @return The list of all the tokens from the passed string.
     * @see #tokenize(String, String, boolean)
     */
    public static List<String> tokenize(String str, String delim) {
        return tokenize(str, delim, false);
    }
    
    /**
     * Tokenizes a passed string into its tokens and returns a list of those tokens.
     *
     * @param str The string to tokenize.
     * @return The list of all the tokens from the passed string.
     * @see #tokenize(String, String)
     */
    public static List<String> tokenize(String str) {
        return tokenize(str, "\\s+");
    }
    
    /**
     * Tokenizes a string into a list of tokens of a certain length.
     *
     * @param str    The string to tokenize.
     * @param length The length of the tokens.
     * @return The list of all the tokens from the passed string.
     */
    public static List<String> tokenize(String str, int length) {
        return Arrays.asList(str.split("(?<=\\G.{" + length + "})"));
    }
    
    /**
     * Tokenizes a string into a list of tokens based on a list of valid tokens.
     *
     * @param str         The string to tokenize.
     * @param validTokens The list of valid tokens.
     * @param sortList    Whether or not to sort the valid tokens list for best performance.
     * @return The list of all the tokens from the passed string, or null when the string cannot be tokenized.
     */
    public static List<String> tokenize(String str, List<String> validTokens, boolean sortList) {
        final List<String> validTokenList = new ArrayList<>(validTokens);
        if (sortList) {
            validTokenList.sort((o1, o2) -> Integer.compare(o2.length(), o1.length()));
        }
        
        final Map<Character, String> placeholders = new HashMap<>();
        int originalMaxCharacter = (str + String.join("", validTokenList)).codePoints().max().orElse(0);
        int placeholderChar = originalMaxCharacter + 1;
        for (String validToken : validTokenList) {
            if (!str.contains(validToken)) {
                continue;
            }
            if (placeholderChar > 65535) {
                return null;
            }
            char placeholder = (char) placeholderChar++;
            placeholders.put(placeholder, validToken);
            str = str.replace(validToken, String.valueOf(placeholder));
        }
        
        int newMinCharacter = str.codePoints().min().orElse(0);
        if (newMinCharacter <= originalMaxCharacter) {
            return null;
        }
        
        return str.codePoints().mapToObj(e -> (char) e).map(placeholders::get).collect(Collectors.toList());
    }
    
    /**
     * Tokenizes a string into a list of tokens based on a list of valid tokens.
     *
     * @param str         The string to tokenize.
     * @param validTokens The list of valid tokens.
     * @return The list of all the tokens from the passed string, or null when the string cannot be tokenized.
     * @see #tokenize(String, List)
     */
    public static List<String> tokenize(String str, List<String> validTokens) {
        return tokenize(str, validTokens, true);
    }
    
    /**
     * Detokenizes a passed list of tokens back into a string.
     *
     * @param tokens The list of tokens to detokenize.
     * @param delim  The delimiter to insert between tokens.
     * @return The string composed of the tokens in the passed list.
     */
    public static String detokenize(List<String> tokens, String delim) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            str.append(tokens.get(i));
            if (i != (tokens.size() - 1)) {
                str.append(delim);
            }
        }
        return str.toString();
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
     * @param str The string to split.
     * @return The list of the lines in the passed string.
     * @see #tokenize(String, String, boolean)
     */
    public static List<String> splitLines(String str) {
        return tokenize(str, "\\r?\\n", true);
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
     * Tokenizes a passed string into its a list of arguments delimited either by spaces or quotes.
     *
     * @param str The string to tokenize.
     * @return The list of all the args from the passed string.
     */
    public static List<String> tokenizeArgs(String str) {
        List<String> args = new ArrayList<>();
        
        StringBuilder argBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ' ') {
                if ((argBuilder.length() > 0)) {
                    args.add(argBuilder.toString());
                    argBuilder = new StringBuilder();
                }
            } else if (c == '"') {
                for (i = i + 1; i < str.length(); i++) {
                    char k = str.charAt(i);
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
                if (i == (str.length() - 1)) {
                    args.add(argBuilder.toString());
                }
            }
        }
        
        return args;
    }
    
    /**
     * Reverses a string.
     *
     * @param str The string.
     * @return The reversed string.
     */
    public static String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }
    
    /**
     * Determines if a string contains any of a set of substrings.
     *
     * @param str    The string.
     * @param search The set of substrings to search for.
     * @return Whether or not the string contains any of the set of substrings.
     */
    public static boolean containsAny(String str, String[] search) {
        return Arrays.stream(search).anyMatch(str::contains);
    }
    
    /**
     * Determines if a string contains any of a set of characters.
     *
     * @param str    The string.
     * @param search The set of characters to search for.
     * @return Whether or not the string contains any of the set of characters.
     */
    public static boolean containsAny(String str, Character[] search) {
        return Arrays.stream(search).anyMatch(e -> (str.indexOf(e) >= 0));
    }
    
    /**
     * Determines if a character is alphanumeric or not.
     *
     * @param c The character.
     * @return Whether the character is alphanumeric or not.
     * @see Character#isAlphabetic(int)
     * @see Character#isDigit(char)
     */
    public static boolean isAlphanumeric(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c);
    }
    
    /**
     * Determines if a string is alphanumeric or not.
     *
     * @param str The string.
     * @return Whether the string is alphanumeric or not.
     */
    public static boolean isAlphanumeric(String str) {
        return ALPHANUMERIC_PATTERN.matcher(str).matches();
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
     * @param str The string.
     * @return Whether the string is alphabetic or not.
     */
    public static boolean isAlphabetic(String str) {
        return ALPHABETIC_PATTERN.matcher(str).matches();
    }
    
    /**
     * Determines if a character is a vowel or not.
     *
     * @param c The character.
     * @return Whether the character is a vowel or not.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static boolean isVowel(char c) {
        return ("AEIOUaeiou".indexOf(c) != -1);
    }
    
    /**
     * Determines if a character is a consonant or not.
     *
     * @param c The character.
     * @return Whether the character is a consonant or not.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static boolean isConsonant(char c) {
        return ("BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz".indexOf(c) != -1);
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
     * @param str The string.
     * @return Whether the string is numeric or not.
     */
    public static boolean isNumeric(String str) {
        return NUMERIC_PATTERN.matcher(str).matches();
    }
    
    /**
     * Determines if a character is a symbol or not.
     *
     * @param c The character.
     * @return Whether the character is a symbol or not.
     * @see Character#isLetterOrDigit(char)
     * @see #isWhitespace(char)
     */
    public static boolean isSymbol(char c) {
        return !(Character.isLetterOrDigit(c) || isWhitespace(c));
    }
    
    /**
     * Determines if a string is punctuation or not.
     *
     * @param str The string.
     * @return Whether the string is punctuation or not.
     */
    public static boolean isSymbol(String str) {
        return SYMBOL_PATTERN.matcher(str).matches();
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
     * @param str The string.
     * @return Whether the string is whitespace or not.
     */
    public static boolean isWhitespace(String str) {
        return WHITESPACE_PATTERN.matcher(str).matches();
    }
    
    /**
     * Removes the whitespace from a string.
     *
     * @param string The string to operate on.
     * @return The string with all whitespace characters removed.
     */
    public static String removeWhiteSpace(String string) {
        return string.replaceAll("[\\s\0]+", "");
    }
    
    /**
     * Removes the diacritical marks from a string.
     *
     * @param string The string to operate on.
     * @return The string with all diacritical marks removed.
     */
    public static String removeDiacritics(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "")
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS_SUPPLEMENT}+", "");
    }
    
    /**
     * Replaces a character in a string with another character.
     *
     * @param str     The string to operate on.
     * @param index   The index in the string to replace at.
     * @param replace The character to replace with at the specified index.
     * @return The string with the replacement performed.
     */
    public static String replaceCharAt(String str, int index, char replace) {
        if ((index < 0) || (index > (str.length() - 1))) {
            return str;
        }
        return lSnip(str, index) + replace + lShear(str, index + 1);
    }
    
    /**
     * Inserts a character in a string at.
     *
     * @param str    The string to operate on.
     * @param index  The index in the string to insert at.
     * @param insert The character to insert at the specified index.
     * @return The string with the insertion performed.
     */
    public static String insertCharAt(String str, int index, char insert) {
        if ((index < 0) || (index > (str.length()))) {
            return str;
        }
        if (index == str.length()) {
            return str + insert;
        }
        return lSnip(str, index) + insert + lShear(str, index);
    }
    
    /**
     * Deletes a character from a string.
     *
     * @param str   The string to operate on.
     * @param index The index in the string to delete from.
     * @return The string with the deletion performed.
     */
    public static String deleteCharAt(String str, int index) {
        if ((index < 0) || (index > (str.length() - 1))) {
            return str;
        }
        return lSnip(str, index) + lShear(str, index + 1);
    }
    
    /**
     * Replaces a substring in a string with another substring.
     *
     * @param str        The string to operate on.
     * @param startIndex The starting index of the substring in the string.
     * @param endIndex   The ending index of the substring in the string.
     * @param replace    The substring to replace with.
     * @return The string with the replacement performed.
     */
    public static String replaceSubstringAt(String str, int startIndex, int endIndex, String replace) {
        if ((startIndex < 0) || (startIndex > str.length()) ||
                (endIndex < 0) || (endIndex > str.length()) ||
                (startIndex > endIndex)) {
            return str;
        }
        if (startIndex == str.length()) {
            return str + replace;
        }
        if (endIndex == str.length()) {
            return lSnip(str, startIndex) + replace;
        }
        return lSnip(str, startIndex) + replace + lShear(str, endIndex);
    }
    
    /**
     * Inserts a substring in a string.
     *
     * @param str    The string to operate on.
     * @param index  The index in the string to insert at.
     * @param insert The substring to insert.
     * @return The string with the insertion performed.
     */
    public static String insertSubstringAt(String str, int index, String insert) {
        if ((index < 0) || (index > (str.length()))) {
            return str;
        }
        if (index == str.length()) {
            return str + insert;
        }
        return lSnip(str, index) + insert + lShear(str, index);
    }
    
    /**
     * Deletes a substring from a string.
     *
     * @param str        The string to operate on.
     * @param startIndex The starting index of the substring in the string.
     * @param endIndex   The ending index of the substring in the string.
     * @return The string with the deletion performed.
     */
    public static String deleteSubstringAt(String str, int startIndex, int endIndex) {
        if ((startIndex < 0) || (startIndex > (str.length() - 1)) ||
                (endIndex < 0) || (endIndex > str.length()) ||
                (startIndex > endIndex)) {
            return str;
        }
        if (endIndex == str.length()) {
            return lSnip(str, startIndex);
        }
        return lSnip(str, startIndex) + lShear(str, endIndex);
    }
    
    /**
     * Trims the whitespace off of the front and back ends of a string.
     *
     * @param str The string to trim.
     * @return The trimmed string.
     * @see #lTrim(String)
     * @see #rTrim(String)
     */
    public static String trim(String str) {
        return lTrim(rTrim(str));
    }
    
    /**
     * Trims the whitespace off the left end of a string.
     *
     * @param str The string to trim.
     * @return The trimmed string.
     */
    public static String lTrim(String str) {
        return str.replaceAll("^[\\s\0]+", "");
    }
    
    /**
     * Trims the whitespace off the right end of a string.
     *
     * @param str The string to trim.
     * @return The trimmed string.
     */
    public static String rTrim(String str) {
        return str.replaceAll("[\\s\0]+$", "");
    }
    
    /**
     * Removes the first n characters from the beginning of a string and the last n characters from the end of a string.
     *
     * @param str  The string to skin.
     * @param skin The number of characters to skin.
     * @return The string with the first n characters and last n characters removed.
     * @see #lShear(String, int)
     * @see #rShear(String, int)
     */
    public static String skin(String str, int skin) {
        return lShear(rShear(str, skin), skin);
    }
    
    /**
     * Removes the first n characters from the beginning of a string.
     *
     * @param str   The string to shear.
     * @param shear The number of characters to shear.
     * @return The string with the first n characters removed.
     */
    public static String lShear(String str, int shear) {
        if (shear <= 0) {
            return str;
        }
        if (shear >= str.length()) {
            return "";
        }
        return str.substring(shear);
    }
    
    /**
     * Removes the last n characters from the end of a string.
     *
     * @param str   The string to shear.
     * @param shear The number of characters to shear.
     * @return The string with the last n characters removed.
     */
    public static String rShear(String str, int shear) {
        if (shear <= 0) {
            return str;
        }
        if (shear >= str.length()) {
            return "";
        }
        return str.substring(0, (str.length() - shear));
    }
    
    /**
     * Returns the first n characters from the beginning of a string and the last n characters from the end of a string.
     *
     * @param str  The string to gut.
     * @param skin The number of characters to not gut.
     * @return The first n characters and the last n characters of the string.
     * @see #lSnip(String, int)
     * @see #rSnip(String, int)
     */
    public static String gut(String str, int skin) {
        if (skin >= (str.length() / 2)) {
            return str;
        }
        return lSnip(str, skin) + rSnip(str, skin);
    }
    
    /**
     * Returns the first n characters from the beginning of a string.
     *
     * @param str  The string to snip.
     * @param snip The number of characters to return.
     * @return The first n characters of the string.
     */
    public static String lSnip(String str, int snip) {
        if (snip <= 0) {
            return "";
        }
        if (snip >= str.length()) {
            return str;
        }
        return str.substring(0, snip);
    }
    
    /**
     * Returns the last n characters from the end of a string.
     *
     * @param str  The string to snip.
     * @param snip The number of characters to return.
     * @return The last n characters of the string.
     */
    public static String rSnip(String str, int snip) {
        if (snip <= 0) {
            return "";
        }
        if (snip >= str.length()) {
            return str;
        }
        return str.substring(str.length() - snip);
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
        if (StringUtils.isEmpty(string)) {
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
        if (StringUtils.isEmpty(string)) {
            return "";
        }
        
        return Character.toUpperCase(string.charAt(0)) + ((string.length() > 1) ? lShear(string, 1).toLowerCase() : "");
    }
    
    /**
     * Determines the number of occurrences of a pattern in a string.
     *
     * @param string  The string to search in.
     * @param pattern The pattern to find the number of occurrences of.
     * @return The number of occurrences of the pattern in the string.
     */
    public static int numberOfOccurrences(String string, String pattern) {
        int n = 0;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
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
     * Fixes double spaces in a string.
     *
     * @param string The string to operate on.
     * @return The string with double spaces replaced with single spaces.
     */
    public static String fixSpaces(String string) {
        return StringUtility.trim(string.replaceAll("\\s+", " "));
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
            if (Character.isLetterOrDigit(current) || Character.isWhitespace(current)) {
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
        StringBuilder depuncted = new StringBuilder();
        
        for (char c : string.toCharArray()) {
            if (!isSymbol(c) || save.contains(c)) {
                depuncted.append(c);
            }
        }
        
        return depuncted.toString();
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
     * Determines if a string token represents a number of not.
     *
     * @param token The token to examine.
     * @return Whether the token represents a number of not.
     */
    public static boolean tokenIsNum(String token) {
        try {
            double d = Double.parseDouble(token);
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
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public static boolean tokenIsOperator(String token) {
        String[] operators = new String[] {"+", "-", "*", "/", "\\", "%", ">", "<", "!", "==", "!=", "<>", ">=", "<="};
        for (String operator : operators) {
            if (token.equals(operator)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Pads a string on the left to a specified length.
     *
     * @param str     The string to pad.
     * @param size    The target size of the string.
     * @param padding The character to pad with.
     * @return The padded string.
     */
    public static String padLeft(String str, int size, char padding) {
        if (str.length() >= size) {
            return str;
        }
        
        int numPad = size - str.length();
        char[] chars = new char[numPad];
        Arrays.fill(chars, padding);
        String pad = new String(chars);
        return pad + str;
    }
    
    /**
     * Pads a string on the left to a specified length.
     *
     * @param str  The string to pad.
     * @param size The target size of the string.
     * @return The padded string.
     * @see #padLeft(String, int, char)
     */
    public static String padLeft(String str, int size) {
        return padLeft(str, size, ' ');
    }
    
    /**
     * Pads a string on the left with a specified amount of padding.
     *
     * @param str     The string to pad.
     * @param size    The amount of padding.
     * @param padding The character to pad with.
     * @return The padded string.
     */
    public static String padLeftAbsolute(String str, int size, char padding) {
        return fillStringOfLength(padding, size) + str;
    }
    
    /**
     * Pads a string on the left with a specified amount of padding.
     *
     * @param str  The string to pad.
     * @param size The amount of padding.
     * @return The padded string.
     * @see #padLeftAbsolute(String, int, char)
     */
    public static String padLeftAbsolute(String str, int size) {
        return padLeftAbsolute(str, size, ' ');
    }
    
    /**
     * Pads a string on the right to a specified length.
     *
     * @param str     The string to pad.
     * @param size    The target size of the string.
     * @param padding The character to pad with.
     * @return The padded string.
     */
    public static String padRight(String str, int size, char padding) {
        if (str.length() >= size) {
            return str;
        }
        
        int numPad = size - str.length();
        char[] chars = new char[numPad];
        Arrays.fill(chars, padding);
        String pad = new String(chars);
        return str + pad;
    }
    
    /**
     * Pads a string on the right to a specified length.
     *
     * @param str  The string to pad.
     * @param size The target size of the string.
     * @return The padded string.
     * @see #padRight(String, int, char)
     */
    public static String padRight(String str, int size) {
        return padRight(str, size, ' ');
    }
    
    /**
     * Pads a string on the right with a specified amount of padding.
     *
     * @param str     The string to pad.
     * @param size    The amount of padding.
     * @param padding The character to pad with.
     * @return The padded string.
     */
    public static String padRightAbsolute(String str, int size, char padding) {
        return str + fillStringOfLength(padding, size);
    }
    
    /**
     * Pads a string on the left with a specified amount of padding.
     *
     * @param str  The string to pad.
     * @param size The amount of padding.
     * @return The padded string.
     * @see #padRightAbsolute(String, int, char)
     */
    public static String padRightAbsolute(String str, int size) {
        return padRightAbsolute(str, size, ' ');
    }
    
    /**
     * Pads a string on both sides to a specified length.
     *
     * @param str     The string to pad.
     * @param size    The target size of the string.
     * @param padding The character to pad with.
     * @return The padded string.
     */
    public static String pad(String str, int size, char padding) {
        if (str.length() >= size) {
            return str;
        }
        String pad = fillStringOfLength(padding, ((size - str.length()) / 2));
        return pad + str + pad;
    }
    
    /**
     * Pads a string on both sides to a specified length.
     *
     * @param str  The string to pad.
     * @param size The target size of the string.
     * @return The padded string.
     * @see #pad(String, int, char)
     */
    public static String pad(String str, int size) {
        return pad(str, size, ' ');
    }
    
    /**
     * Pads a string on both sides with a specified amount of padding.
     *
     * @param str     The string to pad.
     * @param size    The amount of padding.
     * @param padding The character to pad with.
     * @return The padded string.
     */
    public static String padAbsolute(String str, int size, char padding) {
        String pad = fillStringOfLength(padding, size);
        return pad + str + pad;
    }
    
    /**
     * Pads a string on both sides with a specified amount of padding.
     *
     * @param str  The string to pad.
     * @param size The amount of padding.
     * @return The padded string.
     * @see #padAbsolute(String, int, char)
     */
    public static String padAbsolute(String str, int size) {
        return padAbsolute(str, size, ' ');
    }
    
    /**
     * Pads a number string with leading zeros to fit a particular size.
     *
     * @param str  The number string to pad.
     * @param size The specified size of the final string.
     * @return The padded number string.
     */
    public static String padZero(String str, int size) {
        if (str.length() >= size) {
            return str;
        }
        
        return padLeft(str, size, '0');
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
        return padRight("", size, fill);
    }
    
    /**
     * Repeats a string a certain number of times.
     *
     * @param str The string to repeat.
     * @param num The number of times to repeat the string.
     * @return A string containing the base string repeated a number of times.
     */
    public static String repeatString(String str, int num) {
        if (num <= 0) {
            return "";
        }
        
        return String.valueOf(str).repeat(num);
    }
    
    /**
     * Returns '[aA] {str}' or '[aA]n {str}' depending on the string.
     *
     * @param str       The string to justify.
     * @param uppercase Whether or not to capitalize the first letter.
     * @return '[aA] {str}' or '[aA]n {str}'.
     */
    public static String justifyAOrAn(String str, boolean uppercase) {
        if (str.isBlank()) {
            return "";
        }
        
        return (uppercase ? 'A' : 'a') + (isVowel(str.charAt(0)) ? "n" : "") + ' ' + str;
    }
    
    /**
     * Returns 'a {str}' or 'an {str}' depending on the string.
     *
     * @param str The string to justify.
     * @return 'a {str}' or 'an {str}'.
     * @see #justifyAOrAn(String, boolean)
     */
    public static String justifyAOrAn(String str) {
        return justifyAOrAn(str, false);
    }
    
    /**
     * Returns '{quantity} {unit}' or '{quantity} {unit}s' depending on the quantity.
     *
     * @param quantity The quantity.
     * @param unit     The name of the unit.
     * @return '{quantity} {unit}' or '{quantity} {unit}s' depending on the quantity
     */
    public static String justifyQuantity(int quantity, String unit) {
        if (unit.isBlank()) {
            return String.valueOf(quantity);
        }
        
        return String.valueOf(quantity) + ' ' + unit + ((quantity != 1) ? "s" : "");
    }
    
    /**
     * Returns a string representing a file.
     *
     * @param file     The file.
     * @param absolute Whether or not to use the absolute path.
     * @return The string representing the file.
     */
    public static String fileString(File file, boolean absolute) {
        String fileString = absolute ? file.getAbsolutePath() : file.getPath();
        return fileString.replace("\\", "/");
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
     * Returns a string representing a method.
     *
     * @param clazz           The class that has the method.
     * @param methodName      The name of the method.
     * @param argumentClasses The classes of the arguments of the method.
     * @return The method string.
     */
    public static String methodString(Class<?> clazz, String methodName, Class<?>... argumentClasses) {
        return clazz.getSimpleName() + "::" + methodName +
                '(' + Arrays.stream(argumentClasses).sequential().map(Class::getSimpleName).collect(Collectors.joining(", ")) + ')';
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
            
            String a = "═";
            String test = new String(a.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            
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
