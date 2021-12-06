/*
 * File:    NumberUtility.java
 * Package: commons.math
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional number functionality.
 */
public class NumberUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(NumberUtility.class);
    
    
    //Constants
    
    /**
     * A regex pattern for a hexadecimal number.
     */
    public static final Pattern HEX_NUMBER_PATTERN = Pattern.compile("-?[0-9A-Fa-f]*(?:\\.[0-9A-Fa-f]+)?");
    
    
    //Functions
    
    /**
     * Determines if a number is even.
     *
     * @param num The number.
     * @return Whether the number is even or not.
     */
    public static boolean isEven(long num) {
        return (num % 2) == 0;
    }
    
    /**
     * Determines if a number is odd.
     *
     * @param num The number.
     * @return Whether the number is odd or not.
     */
    public static boolean isOdd(long num) {
        return !isEven(num);
    }
    
    /**
     * Determines if a character is a number related character or not.
     *
     * @param c The character in question.
     * @return Whether the character is a number related character or not.
     */
    public static boolean isNumberChar(char c) {
        return ((c >= '0') && (c <= '9')) || (c == '.') || (c == '-');
    }
    
    /**
     * Returns the number of digits in a number.
     *
     * @param num The number.
     * @return The number of digits in the number.
     */
    public static int length(long num) {
        return String.valueOf(num).replace("-", "").length();
    }
    
    /**
     * Extracts the number characters out of a string.
     *
     * @param str The string.
     * @return The string of extracted number characters.
     */
    public static String extractNumberChars(String str) {
        return str.chars().boxed()
                .map(e -> (char) e.intValue())
                .filter(NumberUtility::isNumberChar)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
    
    /**
     * Converts a hex number string to a decimal number string.
     *
     * @param hex      The hex number string.
     * @param accuracy The number of decimal places to return in the decimal number string.
     * @return The decimal number string.
     */
    public static String hexToDecimal(String hex, int accuracy) {
        if (!HEX_NUMBER_PATTERN.matcher(hex).matches() || (accuracy < 0)) {
            return "";
        }
        
        boolean negative = hex.startsWith("-");
        hex = hex.replaceAll("^-", "");
        String integral = hex.contains(".") ? hex.substring(0, hex.indexOf(".")) : hex;
        String fraction = hex.contains(".") ? hex.substring(hex.indexOf(".") + 1) : "";
        if (integral.contains("-") || fraction.contains(".") || fraction.contains("-")) {
            return "";
        }
        
        StringBuilder decimal = new StringBuilder();
        decimal.append(negative ? "-" : "");
        decimal.append(integral.isEmpty() ? "0" : new BigDecimal(new BigInteger(integral, 16)).toPlainString());
        if (fraction.isEmpty() || (accuracy == 0)) {
            return decimal.toString();
        }
        decimal.append(".");
        
        int numberDigits = accuracy;
        int length = Math.min(fraction.length(), numberDigits);
        int[] hexDigits = new int[numberDigits];
        Arrays.fill(hexDigits, 0);
        IntStream.range(0, length).boxed().parallel().forEach(i -> hexDigits[i] = Integer.parseInt(String.valueOf(fraction.charAt(i)), 16));
        
        while ((numberDigits != 0)) {
            int carry = 0;
            for (int i = length - 1; i >= 0; i--) {
                int value = hexDigits[i] * 10 + carry;
                carry = value / 16;
                hexDigits[i] = value % 16;
            }
            decimal.append(carry);
            numberDigits--;
        }
        return decimal.toString();
    }
    
    /**
     * Converts a hex number string to a decimal number string.
     *
     * @param hex The hex number string.
     * @return The decimal number string.
     * @see #hexToDecimal(String, int)
     */
    public static String hexToDecimal(String hex) {
        String fraction = hex.contains(".") ? hex.substring(hex.indexOf(".") + 1) : "";
        return hexToDecimal(hex, fraction.length());
    }
    
}
