/*
 * File:    NumberStringUtility.java
 * Package: commons.math
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.math;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import commons.object.collection.ArrayUtility;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional number string functionality.
 */
public final class NumberStringUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(NumberStringUtility.class);
    
    
    //Constants
    
    /**
     * The tokens used to create number names.
     */
    private static final String[][] NUMBER_NAMES = new String[][] {
            {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"}, //digits
            {"", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"}, //tens
            {"", "hundred"}, //hundreds
            {"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"}, //teens
            {"", "thousand", "mi", "bi", "tri", "quadri", "quinti", "sexti", "septi", "octi", "noni"}, //latin special
            {"", "un", "duo", "tre", "quattuor", "quin", "sex", "septen", "octo", "novem"}, //latin 1's prefix
            {"", "dec", "vigin", "trigin", "quadragin", "quinquagin", "sexagin", "septuagin", "octogin", "nonagin"}, //latin 10's prefix
            {"", "cen", "duocen", "trecen", "quadringen", "quingen", "sescen", "septingen", "octingen", "nongen"}, //latin 100's prefix
            {"", "millia"}, //latin 1000's separator,
            {"llion", "illion", "tillion"}, //suffixes
            {"th", "st", "nd", "rd"}, //fractional
            {"zero", "fir", "seco", "thi", "four", "fif", "six", "seven", "eigh", "nin"}, //reciprocal
            {"negative", "point", "and", "oh", "o", "times X to the", "s"} //modifiers
    };
    
    /**
     * A list of valid tokens that can appear in a number phrase.
     */
    private static final List<String> VALID_TOKENS = Arrays.stream(NUMBER_NAMES)
            .flatMap(Arrays::stream).filter(e -> !e.isEmpty())
            .map(e -> e.replace("X", NUMBER_NAMES[NumberNameSet.TENS.ordinal()][1]))
            .distinct().collect(Collectors.toList());
    
    /**
     * A list of valid tokens that can appear in a latin power name.
     */
    private static final List<String> VALID_LATIN_POWER_NAME_TOKENS = Stream.of(
                    IntStream.rangeClosed(NumberNameSet.LATIN_SPECIAL.ordinal(), NumberNameSet.LATIN_THOUSANDS_SEPARATORS.ordinal()).boxed()
                            .map(i -> NUMBER_NAMES[i]).flatMap(Arrays::stream).filter(e -> !e.isEmpty())
                            .sorted((o1, o2) -> Integer.compare(o2.length(), o1.length())).toArray(),
                    Arrays.stream(NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()])
                            .sorted((o1, o2) -> Integer.compare(o2.length(), o1.length())).toArray())
            .flatMap(Arrays::stream).map(String.class::cast).collect(Collectors.toList());
    
    /**
     * A regex pattern for numbers.
     */
    public static final Pattern NUMBER_PATTERN = Pattern.compile("[+\\-]?(?:(?:\\d+(?:\\.\\d+)?)|(?:\\.\\d+))");
    
    /**
     * A regex pattern for numbers in exponential notation.
     */
    public static final Pattern EXPONENTIAL_NOTATION_PATTERN = Pattern.compile("(?<integral>[+\\-]?\\d*)(?:\\.(?<fractional>\\d+))?[Ee](?<exponent>[+\\-]?\\d+)");
    
    /**
     * A regex pattern for integers.
     */
    public static final Pattern INTEGER_PATTERN = Pattern.compile("[+\\-]?\\d+");
    
    /**
     * A regex pattern for a string that is only integers.
     */
    public static final Pattern INTEGER_STRING_PATTERN = Pattern.compile("^\\d+$");
    
    /**
     * A regex pattern for numbers that are zero.
     */
    public static final Pattern ZERO_PATTERN = Pattern.compile("[+\\-]?(?:(?:0+(?:\\.0+)?)|(?:\\.0+))");
    
    /**
     * A regex pattern for a string that is only zeros.
     */
    public static final Pattern ZERO_STRING_PATTERN = Pattern.compile("^0+$");
    
    /**
     * A regex pattern for a string ending in zeros.
     */
    public static final Pattern ENDING_ZEROS_PATTERN = Pattern.compile(".*?(?<zeros>0+)$");
    
    /**
     * A regex pattern for a string ending in a fractional suffix.
     */
    public static final Pattern ENDING_FRACTIONAL_SUFFIX_PATTERN = Pattern.compile("(?:" +
            String.join("|", NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()]) + ")?$");
    
    /**
     * A regex pattern for a latin power name.
     */
    public static final Pattern LATIN_POWER_NAME_PATTERN = Pattern.compile("^" +
            IntStream.rangeClosed(NumberNameSet.LATIN_SPECIAL.ordinal(), NumberNameSet.SUFFIXES.ordinal()).boxed()
                    .map(i -> NUMBER_NAMES[i]).flatMap(Arrays::stream).filter(e -> !e.isEmpty())
                    .collect(Collectors.joining("|", "(?:", ")")) + "+$"
    );
    
    /**
     * A regex pattern for validating number phrases.
     */
    public static final Pattern NUMBER_PHRASE_PATTERN = Pattern.compile("^(?:" + Stream.of(
                    VALID_TOKENS,
                    Collections.singletonList("(?:" + NUMBER_PATTERN.pattern() + ")"),
                    Collections.singletonList("(?:" + EXPONENTIAL_NOTATION_PATTERN.pattern().replaceAll("\\?<[^>]+>", "?:") + ")"))
            .flatMap(Collection::stream)
            .collect(Collectors.joining("|", "(?:", ")")) + "[\\s\\-]?)+$");
    
    
    //Enums
    
    /**
     * An enumeration of the token table Number Name Sets.
     */
    private enum NumberNameSet {
        
        //Values
        
        DIGITS,
        TENS,
        HUNDREDS,
        TEENS,
        LATIN_SPECIAL,
        LATIN_ONES_PREFIXES,
        LATIN_TENS_PREFIXES,
        LATIN_HUNDREDS_PREFIXES,
        LATIN_THOUSANDS_SEPARATORS,
        SUFFIXES,
        FRACTIONAL,
        RECIPROCAL,
        MODIFIERS
        
    }
    
    /**
     * An enumeration of the token table Number Name Suffixes.
     */
    private enum NumberNameSuffix {
        
        //Values
        
        SPECIAL,
        SMALL,
        STANDARD
        
    }
    
    /**
     * An enumeration of the token table Number Name Modifiers.
     */
    private enum NumberNameModifier {
        
        //Values
        
        NEGATIVE,
        POINT,
        AND,
        OH_HUNDRED,
        O_HUNDRED,
        EXPONENTIATED,
        PLURAL
        
    }
    
    /**
     * An enumeration of supported Fraction Modes.
     */
    public enum FractionMode {
        
        //Values
        
        DEFAULT,
        SIMPLE,
        FANCY
        
    }
    
    
    //Static Methods
    
    /**
     * Returns the string value of a number.
     *
     * @param number The number.
     * @param format Whether or not to format the number.
     * @return The string value of the number.
     */
    public static String stringValueOf(Number number, boolean format) {
        final BigDecimal n = (number instanceof BigDecimal) ? (BigDecimal) number : new BigDecimal(number.toString());
        String numberString = n.stripTrailingZeros().toString().toUpperCase();
        
        if (format) {
            String value = (numberString.contains("E") ? StringUtility.lSnip(numberString, numberString.indexOf('E')) : numberString)
                    .replaceAll("^[+\\-]", "").replaceAll("0?\\.", "");
            long exponent = (numberString.contains("E") ? Long.parseLong(StringUtility.lShear(numberString, (numberString.indexOf('E') + 1))) : 0L);
            final boolean isNegative = numberString.startsWith("-");
            
            int decimalZeros = 0;
            while (StringUtility.lSnip(value, 1).equals("0")) {
                value = StringUtility.lShear(value, 1);
                exponent--;
                decimalZeros++;
            }
            
            if ((value.length() + decimalZeros) > BigMathUtility.PrecisionMode.MATH_PRECISION.getPrecision()) {
                final int decimal = (numberString.contains(".") ? (numberString.indexOf('.') - (numberString.matches("^[+\\-]?0\\..*") ? 1 : 0)) :
                                     (numberString.contains("E") ? numberString.indexOf('E') :
                                      numberString.length())) - (isNegative ? 1 : 0);
                
                value = StringUtility.lSnip(value, (BigMathUtility.PrecisionMode.MATH_PRECISION.getPrecision() - decimalZeros));
                if ((decimal == 0) && (exponent == 0)) {
                    value = "0." + value;
                } else if ((decimal > 0) && (decimal < BigMathUtility.PrecisionMode.MATH_PRECISION.getPrecision())) {
                    value = StringUtility.lSnip(value, decimal) + '.' + StringUtility.lShear(value, decimal);
                } else if (decimal == BigMathUtility.PrecisionMode.MATH_PRECISION.getPrecision()) {
                    value = StringUtility.lSnip(value, decimal);
                } else {
                    value = StringUtility.lSnip(value, 1) + '.' + StringUtility.lShear(value, 1);
                    exponent += (decimal - 1);
                }
                value = value.replaceAll("\\.?0+$", "");
                numberString = (isNegative ? '-' : "") + value + ((exponent == 0) ? "" : ('E' + ((exponent < 0) ? "" : "+") + exponent));
            }
            
            if (numberString.contains("E")) {
                value = StringUtility.lSnip(numberString, numberString.indexOf('E'))
                        .replaceAll("^-", "").replace(".", "");
                exponent = Long.parseLong(StringUtility.lShear(numberString, (numberString.indexOf('E') + 1)));
                
                int powerOffset;
                try {
                    powerOffset = Integer.parseInt(String.valueOf(exponent));
                } catch (Exception ignored) {
                    return numberString;
                }
                final long zeroCount = Math.max(((powerOffset >= 0) ? (powerOffset - value.length() + 1) : (-powerOffset - 1)), 0);
                final long numberSize = (powerOffset >= 0) ? powerOffset : (value.length() + zeroCount);
                
                if ((powerOffset != 0) && (zeroCount != 0) &&
                        ((Math.abs(numberSize) >= BigMathUtility.PrecisionMode.HIGH_PRECISION.getPrecision()) || (
                                (Math.abs(numberSize) >= BigMathUtility.PrecisionMode.MID_PRECISION.getPrecision()) && (zeroCount >= value.length())))) {
                    numberString = (isNegative ? '-' : "") + StringUtility.lSnip(value, 1) + ((value.length() > 1) ? '.' : "") + StringUtility.lShear(value, 1) +
                            'E' + ((powerOffset > 0) ? '+' : "") + powerOffset;
                } else if (powerOffset > 0) {
                    if (powerOffset < (value.length() - 1)) {
                        numberString = (isNegative ? '-' : "") + StringUtility.lSnip(value, (powerOffset + 1)) + '.' + StringUtility.lShear(value, (powerOffset - 1));
                    } else {
                        numberString = (isNegative ? '-' : "") + value + StringUtility.fillStringOfLength('0', (int) zeroCount);
                    }
                } else if (powerOffset < 0) {
                    numberString = (isNegative ? '-' : "") + "0." + StringUtility.fillStringOfLength('0', (int) zeroCount) + value;
                }
            }
        }
        
        return numberString;
    }
    
    /**
     * Returns the string value of a number.
     *
     * @param number The number.
     * @return The string value of the number.
     * @see #stringValueOf(Number, boolean)
     */
    public static String stringValueOf(Number number) {
        return stringValueOf(number, true);
    }
    
    /**
     * Returns the number value of a number string.
     *
     * @param numberString The number string.
     * @param format       Whether or not to format the number.
     * @return The number value of the number string.
     * @throws NumberFormatException When the string does not represent a number.
     */
    public static Number numberValueOf(String numberString, boolean format) throws NumberFormatException {
        return new BigDecimal(stringValueOf(
                new BigDecimal(cleanNumberString(numberString)).stripTrailingZeros(), format));
    }
    
    /**
     * Returns the number value of a number string.
     *
     * @param numberString The number string.
     * @return The number value of the number string.
     * @throws NumberFormatException When the string does not represent a number.
     * @see #numberValueOf(String, boolean)
     */
    public static Number numberValueOf(String numberString) throws NumberFormatException {
        return numberValueOf(numberString, true);
    }
    
    /**
     * Converts a number string to a phrase that represents the number.
     *
     * @param numberString The number, represented by a string.
     * @param fractionMode The fraction mode to use.
     * @return The phrase equivalent of the number string.
     * @throws NumberFormatException When the string does not represent a number.
     */
    @SuppressWarnings({"DuplicateExpressions", "RedundantSuppression"})
    public static String numberStringToNumberPhrase(String numberString, FractionMode fractionMode) throws NumberFormatException {
        final String originalNumberString = numberString;
        numberString = cleanNumberString(numberString);
        if (!numberString.matches(NUMBER_PATTERN.pattern()) && !numberString.matches(EXPONENTIAL_NOTATION_PATTERN.pattern())) {
            throw new NumberFormatException("The string: " + StringUtility.quote(originalNumberString, true) + " does not represent a number");
        }
        final String cleanedNumberString = numberString;
        
        boolean isNegative = numberString.startsWith("-");
        numberString = numberString.replaceAll("^-", "");
        
        String integral;
        String fractional;
        long powerOffset = 0;
        boolean integralNumber = false;
        boolean fractionalNumber = false;
        
        if (numberString.contains("E")) {
            try {
                powerOffset = Long.parseLong(StringUtility.lShear(numberString, (numberString.indexOf("E") + 1)));
                numberString = StringUtility.rShear(numberString, (NumberUtility.length(powerOffset) + ((numberString.contains("E-") || numberString.contains("E+")) ? 2 : 1)));
                if ((powerOffset != 0) && (powerOffset == -powerOffset)) {
                    throw new NumberFormatException("Exponential notation exponent overflow");
                }
                if (numberString.contains(".")) {
                    if ((powerOffset > 0) && ((numberString.length() - numberString.indexOf('.') - 1) > powerOffset)) {
                        numberString = numberString.substring(0, (int) (powerOffset + 2)).replace(".", "") +
                                '.' + numberString.substring((int) (powerOffset + 2));
                        powerOffset = 0;
                        integralNumber = true;
                    } else if ((powerOffset < 0) && (numberString.indexOf('.') > -powerOffset)) {
                        numberString = numberString.substring(0, (int) (numberString.indexOf('.') + powerOffset)) +
                                '.' + numberString.substring((int) (numberString.indexOf('.') + powerOffset)).replace(".", "");
                        powerOffset = 0;
                        fractionalNumber = true;
                    } else {
                        float powerSig = Math.signum(powerOffset);
                        powerOffset -= (powerOffset > 0) ? (numberString.length() - numberString.indexOf('.') - 1) : -numberString.indexOf('.');
                        if ((Math.signum(powerOffset) != powerSig) && (Math.abs(powerOffset) > (Long.MAX_VALUE * 0.9))) {
                            throw new NumberFormatException("Exponential notation exponent overflow");
                        }
                        if (powerOffset == 0) {
                            integralNumber = powerSig > 0;
                            fractionalNumber = powerSig < 0;
                        }
                        numberString = numberString.replace(".", "");
                    }
                } else if (powerOffset < 0) {
                    powerOffset++;
                }
                if (numberString.contains(".")) {
                    integral = StringUtility.lSnip(numberString, numberString.indexOf('.'));
                    fractional = StringUtility.lShear(numberString, (numberString.indexOf('.') + 1));
                } else {
                    integral = ((powerOffset > 0) || integralNumber) ? numberString : "0";
                    fractional = ((powerOffset < 0) || fractionalNumber) ? numberString : "0";
                }
                
            } catch (NumberFormatException e) {
                return numberStringToExponentialNotationPhrase(cleanedNumberString);
            }
            
        } else {
            integral = (numberString.contains(".")) ? StringUtility.lSnip(numberString, numberString.indexOf('.')) : numberString;
            fractional = (numberString.contains(".")) ? StringUtility.lShear(numberString, (numberString.indexOf('.') + 1)) : "0";
        }
        
        List<String> phraseParts = new ArrayList<>();
        
        while (!integral.isEmpty()) {
            final long integralLength = integral.length() + ((powerOffset > 0) ? powerOffset : 0);
            if (integralLength < 0) {
                return numberStringToExponentialNotationPhrase(cleanedNumberString);
            }
            int chunkSize = (int) MathUtility.xmod(integralLength, 3);
            if (chunkSize > integral.length()) {
                integral += StringUtility.repeatString("0", (chunkSize - integral.length()));
            }
            
            int numberChunk = Integer.parseInt(StringUtility.lSnip(integral, chunkSize));
            if (numberChunk > 0) {
                long latinPower = (integralLength / 3) - (MathUtility.isDivisibleBy(integralLength, 3) ? 1 : 0);
                phraseParts.add(smallNumberName(numberChunk));
                phraseParts.add(latinPowerName(latinPower));
            }
            integral = StringUtility.lShear(integral, chunkSize);
        }
        
        if (!fractional.equals("0")) {
            final long originalLength = fractional.length();
            fractional = fractional.replaceAll("^0+", "");
            final long zeroCount = (originalLength - fractional.length()) +
                    ((powerOffset < 0) ? (-powerOffset) : 0);
            
            if (zeroCount < 0) {
                return numberStringToExponentialNotationPhrase(cleanedNumberString);
            }
            powerOffset = -zeroCount;
            
            if ((fractionMode != FractionMode.SIMPLE) && ((fractionMode == FractionMode.FANCY) || ((fractional.length() < 4) && ((powerOffset + zeroCount) == 0)) ||
                    (zeroCount >= Math.min(fractional.length(), (BigMathUtility.PrecisionMode.HIGH_PRECISION.getPrecision() / 2)) ||
                            fractional.matches(".*0{" + (BigMathUtility.PrecisionMode.HIGH_PRECISION.getPrecision() / 2) + "}.*")))) {
                
                if (!phraseParts.isEmpty()) {
                    phraseParts.add(NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.AND.ordinal()]);
                }
                
                final long originalPowerOffset = powerOffset;
                if (!MathUtility.isDivisibleBy(originalPowerOffset, 3)) {
                    fractional = StringUtility.repeatString("0", (int) (Math.abs(originalPowerOffset) % 3)) + fractional;
                    powerOffset -= (originalPowerOffset % 3);
                }
                
                while (!fractional.isEmpty()) {
                    final long fractionalLength = fractional.length() + ((powerOffset < 0) ? Math.abs(powerOffset) : 0);
                    int chunkSize = (int) MathUtility.xmod(fractionalLength, 3);
                    if ((fractionalLength < 0) || ((powerOffset < 0) && (Math.signum(powerOffset) != Math.signum(powerOffset - 3))) || (chunkSize < 0)) {
                        return numberStringToExponentialNotationPhrase(cleanedNumberString);
                    }
                    
                    if (chunkSize != 3) {
                        fractional += StringUtility.repeatString("0", (3 - chunkSize));
                        chunkSize = 3;
                    }
                    
                    int numberChunk = Integer.parseInt(StringUtility.lSnip(fractional, chunkSize));
                    if (numberChunk > 0) {
                        
                        long latinPower = ((powerOffset - 3) / 3);
                        String power;
                        if ((latinPower == -1) && MathUtility.isDivisibleBy(numberChunk, 10)) {
                            if (MathUtility.isDivisibleBy(numberChunk, 100)) {
                                numberChunk /= 100;
                                power = powerOfTenName(-1);
                            } else {
                                numberChunk /= 10;
                                power = powerOfTenName(-2);
                            }
                        } else {
                            power = latinPowerName(latinPower);
                        }
                        
                        phraseParts.add(smallNumberName(numberChunk));
                        phraseParts.add(power + ((numberChunk > 1) ? NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.PLURAL.ordinal()] : ""));
                    }
                    
                    fractional = StringUtility.lShear(fractional, chunkSize);
                    powerOffset -= chunkSize;
                }
                
            } else {
                phraseParts.add(NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.POINT.ordinal()]);
                
                for (long i = powerOffset; i < 0; i++) {
                    phraseParts.add(NUMBER_NAMES[NumberNameSet.DIGITS.ordinal()][0]);
                }
                
                while (!fractional.isEmpty()) {
                    int decimalDigit = Integer.parseInt(StringUtility.lSnip(fractional, 1));
                    phraseParts.add(NUMBER_NAMES[NumberNameSet.DIGITS.ordinal()][decimalDigit]);
                    fractional = StringUtility.lShear(fractional, 1);
                }
            }
        }
        
        if (phraseParts.isEmpty()) {
            return NUMBER_NAMES[NumberNameSet.DIGITS.ordinal()][0];
        }
        if (isNegative) {
            phraseParts.add(0, NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.NEGATIVE.ordinal()]);
        }
        
        return phraseParts.stream().filter(e -> !e.isEmpty())
                .collect(Collectors.joining(" "));
    }
    
    /**
     * Converts a number string to a phrase that represents the number.
     *
     * @param numberString The number, represented by a string.
     * @return The phrase equivalent of the number string.
     * @throws NumberFormatException When the string does not represent a number.
     * @see #numberStringToNumberPhrase(String, FractionMode)
     */
    public static String numberStringToNumberPhrase(String numberString) throws NumberFormatException {
        return numberStringToNumberPhrase(numberString, FractionMode.DEFAULT);
    }
    
    /**
     * Converts a number to a phrase that represents the number.
     *
     * @param number The number.
     * @return The phrase equivalent of the number.
     * @see #numberStringToNumberPhrase(String)
     */
    public static String numberToNumberPhrase(Number number, FractionMode fractionMode) {
        return numberStringToNumberPhrase(stringValueOf(number, false), fractionMode);
    }
    
    /**
     * Converts a number to a phrase that represents the number.
     *
     * @param number The number.
     * @return The phrase equivalent of the number.
     * @see #numberToNumberPhrase(Number, FractionMode)
     */
    public static String numberToNumberPhrase(Number number) {
        return numberToNumberPhrase(number, FractionMode.DEFAULT);
    }
    
    /**
     * Converts a number string to a phrase that represents the number in exponential notation.
     *
     * @param numberString The number, represented by a string.
     * @return The phrase equivalent of the number string in exponential notation.
     * @throws NumberFormatException When the string does not represent a number in exponential notation.
     * @see #numberStringToNumberPhrase(String)
     */
    public static String numberStringToExponentialNotationPhrase(String numberString) throws NumberFormatException {
        numberString = cleanNumberString(numberString);
        if (!numberString.matches(EXPONENTIAL_NOTATION_PATTERN.pattern())) {
            throw new NumberFormatException("The string: " + StringUtility.quote(numberString, true) + " does not represent a number in exponential notation");
        }
        
        String value = StringUtility.lSnip(numberString, numberString.indexOf('E'));
        String exponent = StringUtility.lShear(numberString, (numberString.indexOf('E') + 1)).replace("+", "");
        
        return numberStringToNumberPhrase(value, FractionMode.SIMPLE) + ' ' +
                NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.EXPONENTIATED.ordinal()]
                        .replace("X", NUMBER_NAMES[NumberNameSet.TENS.ordinal()][1]) + ' ' +
                numberStringToNumberPhrase(exponent, FractionMode.SIMPLE);
    }
    
    /**
     * Converts a number to a phrase that represents the number in exponential notation.
     *
     * @param number The number.
     * @return The phrase equivalent of the number in exponential notation.
     * @throws NumberFormatException When the string does not represent a number in exponential notation.
     * @see #numberStringToExponentialNotationPhrase(String)
     */
    public static String numberToExponentialNotationPhrase(Number number) throws NumberFormatException {
        return numberStringToExponentialNotationPhrase(stringValueOf(number, false));
    }
    
    /**
     * Converts a phrase that represents a number into its number string equivalent.
     *
     * @param numberPhrase A phrase that represents a number.
     * @return The number string equivalent of the string.
     * @throws NumberFormatException When the string does not represent a number.
     */
    public static String numberPhraseToNumberString(String numberPhrase) throws NumberFormatException {
        final String originalNumberPhrase = numberPhrase;
        numberPhrase = StringUtility.trim(numberPhrase.toLowerCase());
        final String cleanedNumberPhrase = numberPhrase;
        
        if (numberPhrase.isEmpty()) {
            return "0";
        }
        
        try {
            final StringBuilder result = new StringBuilder();
            
            final String exponentialString = NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.EXPONENTIATED.ordinal()]
                    .replace("X", NUMBER_NAMES[NumberNameSet.TENS.ordinal()][1]);
            final String negativeString = NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.NEGATIVE.ordinal()];
            final String pointString = NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.POINT.ordinal()];
            final String andString = NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.AND.ordinal()];
            final String ohHundredString = NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.OH_HUNDRED.ordinal()];
            final String oHundredString = NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.O_HUNDRED.ordinal()];
            
            numberPhrase = numberPhrase.replaceAll("^-\\s+(?=\\d)", "-").replaceAll("^+\\s+(?=\\d)", "+").replaceAll("^-\\s+", negativeString + ' ');
            final Matcher numberPhraseMatcher = NUMBER_PHRASE_PATTERN.matcher("");
            for (String numberPhraseToken : StringUtility.tokenize(numberPhrase.replace(StringUtility.padAbsolute(exponentialString, 1), " ~ "))) {
                if (!VALID_TOKENS.contains(numberPhraseToken) && !numberPhraseMatcher.reset(numberPhraseToken).matches() && !numberPhraseToken.equals("~")) {
                    throw new NumberFormatException();
                }
            }
            
            if (!numberPhrase.contains(" ")) {
                final Matcher numberMatcher = NUMBER_PATTERN.matcher(numberPhrase);
                final Matcher exponentialNotationMatcher = EXPONENTIAL_NOTATION_PATTERN.matcher(numberPhrase);
                if (numberMatcher.matches() || exponentialNotationMatcher.matches()) {
                    return cleanNumberString(numberPhrase);
                }
            }
            
            final int negativeStringCount = StringUtility.numberOfOccurrences(StringUtility.padAbsolute(numberPhrase, 1).replace(" ", "  "), StringUtility.padAbsolute(negativeString, 1));
            final int exponentialStringCount = StringUtility.numberOfOccurrences(StringUtility.padAbsolute(numberPhrase, 1).replace(" ", "  "), StringUtility.padAbsolute(exponentialString.replace(" ", "  "), 1));
            final int pointStringCount = StringUtility.numberOfOccurrences(StringUtility.padAbsolute(numberPhrase, 1).replace(" ", "  "), StringUtility.padAbsolute(pointString, 1));
            final int andStringCount = StringUtility.numberOfOccurrences(StringUtility.padAbsolute(numberPhrase, 1).replace(" ", "  "), StringUtility.padAbsolute(andString, 1));
            if ((negativeStringCount > 2) || (exponentialStringCount > 1) || ((exponentialStringCount == 0) && (negativeStringCount > 1)) || (pointStringCount > 1)) {
                throw new NumberFormatException();
            }
            if (andStringCount > 1) {
                numberPhrase = StringUtility.fixSpaces(StringUtility.padAbsolute(numberPhrase, 1).replace(StringUtility.padAbsolute(andString, 1), "  "));
            }
            numberPhrase = StringUtility.fixSpaces(StringUtility.padAbsolute(numberPhrase, 1).replace(StringUtility.padAbsolute(ohHundredString, 1), StringUtility.padAbsolute(NUMBER_NAMES[NumberNameSet.HUNDREDS.ordinal()][1], 1)));
            numberPhrase = StringUtility.fixSpaces(StringUtility.padAbsolute(numberPhrase, 1).replace(StringUtility.padAbsolute(oHundredString, 1), StringUtility.padAbsolute(NUMBER_NAMES[NumberNameSet.HUNDREDS.ordinal()][1], 1)));
            numberPhrase = StringUtility.trim(numberPhrase);
            if (numberPhrase.isEmpty()) {
                throw new NumberFormatException();
            }
            
            String number;
            String exponent;
            if (numberPhrase.contains(StringUtility.padAbsolute(exponentialString, 1))) {
                List<String> numberPhraseParts = StringUtility.tokenize(numberPhrase, StringUtility.padAbsolute(exponentialString, 1));
                number = numberPhraseParts.get(0);
                exponent = numberPhraseParts.get(1);
                if (exponent.isEmpty()) {
                    throw new NumberFormatException();
                }
            } else {
                number = numberPhrase;
                exponent = "";
            }
            
            if (!number.isEmpty()) {
                final boolean isNegative = number.startsWith(StringUtility.padRightAbsolute(negativeString, 1)) || number.startsWith("-");
                number = StringUtility.padAbsolute(number, 1).replace(" ", "  ").replace(StringUtility.padRightAbsolute(negativeString, 1), " ");
                number = number.replace("-", "");
                number = StringUtility.fixSpaces(number);
                if (number.isEmpty()) {
                    throw new NumberFormatException();
                }
                
                String magnitudeNumber;
                String simpleFractional;
                if (number.contains(StringUtility.padRightAbsolute(pointString, 1))) {
                    List<String> numberParts = StringUtility.tokenize(number, StringUtility.padRightAbsolute(pointString, 1));
                    magnitudeNumber = StringUtility.trim(numberParts.get(0));
                    simpleFractional = StringUtility.trim(numberParts.get(1));
                    if (simpleFractional.isEmpty()) {
                        throw new NumberFormatException();
                    }
                } else {
                    magnitudeNumber = number;
                    simpleFractional = "";
                }
                
                final Map<Long, BigDecimal> magnitudeComponents = new LinkedHashMap<>();
                
                if (!magnitudeNumber.isEmpty()) {
                    List<String> magnitudeNumberTokens = StringUtility.tokenize(magnitudeNumber, " ");
                    
                    BigDecimal chunk = BigMathUtility.ZERO;
                    boolean[] chunkParts = new boolean[] {false, false, false, false};
                    boolean inProgress = false;
                    int index;
                    for (int i = 0; i < magnitudeNumberTokens.size(); i++) {
                        String magnitudeNumberToken = magnitudeNumberTokens.get(i);
                        if (magnitudeNumberToken.equals(andString)) {
                            if (inProgress) {
                                magnitudeComponents.put(0L, magnitudeComponents.getOrDefault(0L, BigMathUtility.ZERO).add(chunk));
                                chunk = BigMathUtility.ZERO;
                                chunkParts = new boolean[] {false, false, false, false};
                                inProgress = false;
                            }
                            continue;
                        }
                        
                        index = ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.DIGITS.ordinal()], magnitudeNumberToken);
                        if (index >= 0) {
                            if (chunkParts[2]) {
                                magnitudeComponents.put(0L, magnitudeComponents.getOrDefault(0L, BigMathUtility.ZERO).add(chunk));
                                chunk = BigMathUtility.ZERO;
                                chunkParts = new boolean[] {false, false, false, false};
                            }
                            chunk = chunk.add(BigDecimal.valueOf(index));
                            chunkParts[2] = true;
                            inProgress = true;
                            continue;
                        }
                        index = ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.TENS.ordinal()], magnitudeNumberToken);
                        if (index > 0) {
                            if (chunkParts[2] || chunkParts[1]) {
                                if (chunkParts[3]) {
                                    magnitudeComponents.put(0L, magnitudeComponents.getOrDefault(0L, BigMathUtility.ZERO).add(chunk));
                                    chunk = BigMathUtility.ZERO;
                                    chunkParts = new boolean[] {false, false, false, false};
                                } else {
                                    chunk = chunk.multiply(BigMathUtility.ONE_HUNDRED);
                                    chunkParts[2] = false;
                                    chunkParts[3] = true;
                                }
                            }
                            chunk = chunk.add(BigDecimal.valueOf(index).movePointRight(1));
                            chunkParts[1] = true;
                            inProgress = true;
                            continue;
                        }
                        if (magnitudeNumberToken.equals(NUMBER_NAMES[NumberNameSet.HUNDREDS.ordinal()][1])) {
                            if (chunkParts[0]) {
                                magnitudeComponents.put(0L, magnitudeComponents.getOrDefault(0L, BigMathUtility.ZERO).add(chunk));
                                chunk = BigMathUtility.ZERO;
                                chunkParts = new boolean[] {false, false, false, false};
                            }
                            chunk = chunk.multiply(BigMathUtility.ONE_HUNDRED);
                            chunkParts[0] = true;
                            chunkParts[1] = false;
                            chunkParts[2] = false;
                            chunkParts[3] = true;
                            inProgress = true;
                            continue;
                        }
                        index = ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.TEENS.ordinal()], magnitudeNumberToken);
                        if (index > 0) {
                            if (chunkParts[2] || chunkParts[1]) {
                                if (chunkParts[3]) {
                                    magnitudeComponents.put(0L, magnitudeComponents.getOrDefault(0L, BigMathUtility.ZERO).add(chunk));
                                    chunk = BigMathUtility.ZERO;
                                    chunkParts = new boolean[] {false, false, false, false};
                                } else {
                                    chunk = chunk.multiply(BigMathUtility.ONE_HUNDRED);
                                    chunkParts[3] = true;
                                }
                            }
                            chunk = chunk.add(BigDecimal.valueOf(index).add(BigMathUtility.TEN));
                            chunkParts[1] = true;
                            chunkParts[2] = true;
                            inProgress = true;
                            continue;
                        }
                        index = ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.RECIPROCAL.ordinal()], magnitudeNumberToken) +
                                ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.RECIPROCAL.ordinal()], StringUtility.rShear(magnitudeNumberToken, 2)) + 1;
                        if (index >= 0) {
                            if (chunkParts[2]) {
                                magnitudeComponents.put(0L, magnitudeComponents.getOrDefault(0L, BigMathUtility.ZERO).add(chunk));
                                chunk = BigMathUtility.ZERO;
                                chunkParts = new boolean[] {false, false, false, false};
                            }
                            chunk = chunk.add(BigDecimal.valueOf(index));
                            chunkParts[2] = true;
                            inProgress = true;
                            continue;
                        }
                        
                        if (magnitudeNumberToken.matches("^[\\d.eE+\\-]+" + ENDING_FRACTIONAL_SUFFIX_PATTERN.pattern())) {
                            try {
                                BigDecimal literalChunk = new BigDecimal(magnitudeNumberToken.replaceAll(ENDING_FRACTIONAL_SUFFIX_PATTERN.pattern(), ""));
                                if (BigMathUtility.lessThan(literalChunk, BigMathUtility.ONE_THOUSAND) &&
                                        BigMathUtility.equalTo(BigMathUtility.fractionalPart(literalChunk), BigMathUtility.ZERO)) {
                                    magnitudeNumberTokens.remove(i);
                                    magnitudeNumberTokens.addAll(i--, StringUtility.tokenize(numberStringToNumberPhrase(literalChunk.toString()), " "));
                                } else {
                                    if (inProgress) {
                                        magnitudeComponents.put(0L, magnitudeComponents.getOrDefault(0L, BigMathUtility.ZERO).add(chunk));
                                        chunk = literalChunk;
                                        chunkParts = new boolean[] {false, false, false, false};
                                    } else {
                                        chunk = chunk.add(literalChunk);
                                    }
                                    inProgress = true;
                                }
                                continue;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        
                        long latinPower;
                        magnitudeNumberToken = magnitudeNumberToken.replaceAll("s$", "");
                        if (magnitudeNumberToken.equals(NUMBER_NAMES[NumberNameSet.TENS.ordinal()][1] + NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][0])) {
                            chunk = chunk.movePointRight(2);
                            latinPower = -1L;
                        } else if (magnitudeNumberToken.equals(NUMBER_NAMES[NumberNameSet.HUNDREDS.ordinal()][1] + NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][0])) {
                            chunk = chunk.movePointRight(1);
                            latinPower = -1L;
                        } else {
                            latinPower = magnitudeNumberToken.equals(andString) ? 0L :
                                         latinPowerNameToLatinPower(magnitudeNumberToken);
                        }
                        
                        if (!BigMathUtility.equalTo(chunk, BigMathUtility.ZERO) || !magnitudeComponents.isEmpty()) {
                            magnitudeComponents.put(latinPower, magnitudeComponents.getOrDefault(latinPower, BigMathUtility.ZERO).add(chunk));
                        }
                        chunk = BigDecimal.ZERO;
                        chunkParts = new boolean[] {false, false, false, false};
                        inProgress = false;
                    }
                    
                    if (inProgress) {
                        magnitudeComponents.put(0L, magnitudeComponents.getOrDefault(0L, BigMathUtility.ZERO).add(chunk));
                    }
                }
                
                boolean componentsCleaned = false;
                while (!componentsCleaned) {
                    componentsCleaned = true;
                    for (Map.Entry<Long, BigDecimal> component : magnitudeComponents.entrySet()) {
                        final boolean overflow = BigMathUtility.greaterThanOrEqualTo(component.getValue(), BigMathUtility.ONE_THOUSAND);
                        final boolean hasFractional = BigMathUtility.notEqualTo(BigMathUtility.fractionalPart(component.getValue()), BigMathUtility.ZERO);
                        
                        if (overflow || hasFractional) {
                            final BigDecimal integral = BigMathUtility.integralPart(component.getValue());
                            final BigDecimal fractional = BigMathUtility.fractionalPart(component.getValue());
                            
                            if (overflow) {
                                BigDecimal tmpIntegral = integral;
                                long chunkPower = component.getKey();
                                while (BigMathUtility.greaterThan(tmpIntegral, BigMathUtility.ZERO)) {
                                    magnitudeComponents.put(chunkPower, ((chunkPower == component.getKey()) ? BigMathUtility.ZERO : magnitudeComponents.getOrDefault(chunkPower, BigMathUtility.ZERO))
                                            .add(BigMathUtility.mod(tmpIntegral, BigMathUtility.ONE_THOUSAND)));
                                    chunkPower++;
                                    tmpIntegral = tmpIntegral.divideToIntegralValue(BigMathUtility.ONE_THOUSAND);
                                }
                            }
                            
                            if (hasFractional) {
                                BigDecimal tmpFractional = fractional;
                                long chunkPower = component.getKey();
                                while (BigMathUtility.greaterThan(tmpFractional, BigMathUtility.ZERO)) {
                                    tmpFractional = tmpFractional.movePointRight(3);
                                    chunkPower--;
                                    magnitudeComponents.put(chunkPower, magnitudeComponents.getOrDefault(chunkPower, BigMathUtility.ZERO)
                                            .add(BigMathUtility.integralPart(tmpFractional)));
                                    tmpFractional = BigMathUtility.fractionalPart(tmpFractional);
                                }
                            }
                            
                            magnitudeComponents.replace(component.getKey(), BigMathUtility.mod(integral, BigMathUtility.ONE_THOUSAND));
                            componentsCleaned = false;
                            break;
                        }
                    }
                }
                
                final Map<Long, BigDecimal> components = new LinkedHashMap<>();
                magnitudeComponents.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                        .forEachOrdered(e -> components.put(e.getKey(), e.getValue()));
                magnitudeComponents.clear();
                
                if (!simpleFractional.isEmpty()) {
                    if (!components.isEmpty() && (components.keySet().toArray(Long[]::new)[components.size() - 1] < 0)) {
                        throw new NumberFormatException();
                    }
                    
                    List<String> fractionalTokens = StringUtility.tokenize(simpleFractional);
                    
                    final Matcher integerStringMatcher = INTEGER_STRING_PATTERN.matcher(StringUtility.removeWhiteSpace(simpleFractional));
                    if (integerStringMatcher.matches()) {
                        fractionalTokens = StringUtility.stringStream(StringUtility.removeWhiteSpace(simpleFractional)).collect(Collectors.toList());
                    }
                    
                    int chunk = 0;
                    int count = 0;
                    long magnitude = 0;
                    for (String fractionalToken : fractionalTokens) {
                        int digit = ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.DIGITS.ordinal()], fractionalToken);
                        if (digit == -1) {
                            if (!fractionalToken.endsWith(StringUtility.rSnip(NUMBER_NAMES[NumberNameSet.TENS.ordinal()][2], 2))) {
                                digit = ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.RECIPROCAL.ordinal()], fractionalToken) +
                                        ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.RECIPROCAL.ordinal()], StringUtility.rShear(fractionalToken, 2)) + 1;
                            }
                            if (digit == -1) {
                                try {
                                    digit = Integer.parseInt(fractionalToken.replaceAll(ENDING_FRACTIONAL_SUFFIX_PATTERN.pattern(), ""));
                                } catch (NumberFormatException e) {
                                    throw new NumberFormatException(e.getMessage());
                                }
                            }
                        }
                        
                        chunk *= 10;
                        chunk += digit;
                        count++;
                        if (count == 3) {
                            magnitude--;
                            BigDecimal existingChunk = magnitudeComponents.getOrDefault(magnitude, BigMathUtility.ZERO);
                            components.put(magnitude, existingChunk.add(BigDecimal.valueOf(chunk)));
                            chunk = 0;
                            count = 0;
                        }
                    }
                    
                    if (chunk > 0) {
                        chunk *= Math.pow(10, (3 - count));
                        magnitude--;
                        BigDecimal existingChunk = magnitudeComponents.getOrDefault(magnitude, BigMathUtility.ZERO);
                        components.put(magnitude, existingChunk.add(BigDecimal.valueOf(chunk)));
                    }
                }
                
                if (!components.isEmpty()) {
                    final long maximumLatinPower = (long) components.keySet().toArray()[0];
                    final long minimumLatinPower = maximumLatinPower - (BigMathUtility.PrecisionMode.MATH_PRECISION.getPrecision() / 3);
                    final int firstComponentLength = components.get(maximumLatinPower).toPlainString().equals("0") ?
                                                     ((maximumLatinPower < 0) ? 3 : 1) : components.get(maximumLatinPower).toPlainString().length();
                    
                    List<Long> latinPowers = new ArrayList<>(components.keySet());
                    for (Long latinPower : latinPowers) {
                        if ((latinPower < minimumLatinPower) || BigMathUtility.equalTo(components.get(latinPower), BigMathUtility.ZERO)) {
                            components.remove(latinPower);
                        } else if (latinPower == minimumLatinPower) {
                            components.put(latinPower, components.get(latinPower).subtract(
                                    BigMathUtility.mod(components.get(latinPower), BigMathUtility.ONE.movePointRight(firstComponentLength - 1))));
                        }
                    }
                    
                    if (!components.isEmpty()) {
                        latinPowers = new ArrayList<>(components.keySet());
                        
                        final long powerOffset = (latinPowers.get(0) * 3) + components.get(latinPowers.get(0)).toPlainString().length() - 1;
                        final String baseNumber = LongStream.rangeClosed(((latinPowers.size() > 1) ? latinPowers.get(latinPowers.size() - 1) : latinPowers.get(0)), latinPowers.get(0))
                                .boxed().sorted(Collections.reverseOrder())
                                .map(l -> StringUtility.padZero((components.containsKey(l) ? components.get(l).toPlainString() : ""), 3))
                                .collect(Collectors.joining()).replaceAll("(?:^0+)|(?:0+$)", "");
                        final long zeroCount = Math.max(((powerOffset >= 0) ? (powerOffset - baseNumber.length() + 1) : (-powerOffset - 1)), 0);
                        final long numberSize = (powerOffset >= 0) ? powerOffset : (baseNumber.length() + zeroCount);
                        
                        result.append(baseNumber.isEmpty() ? "0" : baseNumber);
                        if ((powerOffset != 0) && (zeroCount != 0) && exponent.isEmpty() &&
                                ((Math.abs(numberSize) >= BigMathUtility.PrecisionMode.HIGH_PRECISION.getPrecision()) || (
                                        (Math.abs(numberSize) >= BigMathUtility.PrecisionMode.MID_PRECISION.getPrecision()) && (zeroCount >= baseNumber.length())))) {
                            result.insert(1, ((baseNumber.length() > 1) ? '.' : "")).append('E').append((powerOffset > 0) ? '+' : "").append(powerOffset);
                        } else if ((zeroCount == 0) && (latinPowers.size() > 1) && (latinPowers.get(0) >= 0) && (latinPowers.get(latinPowers.size() - 1) < 0)) {
                            result.insert((int) (powerOffset + 1), '.');
                        } else if (powerOffset > 0) {
                            result.append(StringUtility.fillStringOfLength('0', (int) zeroCount));
                        } else if (powerOffset < 0) {
                            result.insert(0, StringUtility.fillStringOfLength('0', (int) zeroCount)).insert(0, "0.");
                        }
                    }
                }
                
                result.append((result.length() == 0) ? "0" : "");
                result.insert(0, isNegative ? '-' : "");
                
            } else {
                throw new NumberFormatException();
            }
            
            if (!exponent.isEmpty()) {
                return result.toString() + 'E' + numberPhraseToNumberString(exponent);
            }
            
            return result.toString();
            
        } catch (NumberFormatException ignored) {
            throw new NumberFormatException("The string: " + StringUtility.quote(originalNumberPhrase, true) + " does not represent a valid number phrase");
        }
    }
    
    /**
     * Converts a phrase that represents a number into its numeric equivalent.
     *
     * @param numberPhrase A phrase that represents a number.
     * @return The numeric equivalent of the string.
     * @throws NumberFormatException When the string does not represent a number.
     * @see #numberPhraseToNumberString(String)
     */
    public static Number numberPhraseToNumber(String numberPhrase) throws NumberFormatException {
        return numberValueOf(numberPhraseToNumberString(numberPhrase));
    }
    
    /**
     * Cleans a number string produced by a math operation.
     *
     * @param numberString The number string.
     * @return The cleaned number string.
     */
    @SuppressWarnings("ConditionCoveredByFurtherCondition")
    public static String cleanNumberString(String numberString) {
        numberString = StringUtility.removeWhiteSpace(numberString.toUpperCase());
        
        final boolean negative = numberString.startsWith("-");
        numberString = numberString.replaceAll("^[+\\-]?0*", "");
        
        if (!numberString.isEmpty() && numberString.contains(".")) {
            numberString = ((numberString.charAt(0) == '.') ? '0' : "") + numberString;
            numberString = numberString.replaceAll("\\.$", "");
            if (numberString.contains(".") && !numberString.contains("E")) {
                numberString = numberString.replaceAll("\\.?0*$", "");
            }
        }
        
        if (numberString.contains("E")) {
            String number = cleanNumberString(numberString.substring(0, numberString.indexOf('E')));
            String exponent = cleanNumberString(numberString.substring(numberString.indexOf('E') + 1));
            if (number.isEmpty()) {
                return "0";
            }
            
            final Matcher zeroNumberMatcher = ZERO_PATTERN.matcher(number);
            final Matcher zeroExponentMatcher = ZERO_PATTERN.matcher(exponent);
            if (zeroNumberMatcher.matches()) {
                return "0";
            }
            if (zeroExponentMatcher.matches()) {
                exponent = "0";
            }
            
            if ((number.length() > 1) && !number.contains(".")) {
                number = StringUtility.lSnip(number, 1) + '.' +
                        StringUtility.lShear(number.replace(".", ""), 1);
                exponent = StringMathUtility.add(exponent, String.valueOf(number.length() - 2));
                number = cleanNumberString(number);
            }
            
            if (number.indexOf('.') > 1) {
                number = number.replaceAll("\\.?0+$", "");
                if (number.indexOf('.') > 1) {
                    exponent = StringMathUtility.add(exponent, String.valueOf(number.indexOf('.') - 1));
                    number = StringUtility.lSnip(number, 1) + '.' +
                            StringUtility.lShear(number.replace(".", ""), 1);
                }
            }
            
            if (number.startsWith("0.")) {
                number = number.replaceAll("^0\\.", "");
                int zeros;
                for (zeros = 0; zeros < number.length(); zeros++) {
                    if (number.charAt(zeros) != '0') {
                        break;
                    }
                }
                number = StringUtility.lShear(number, zeros);
                number = StringUtility.lSnip(number, 1) + '.' + StringUtility.lShear(number, 1);
                exponent = StringMathUtility.subtract(exponent, String.valueOf(zeros + 1));
            }
            
            numberString = number + ((exponent.equals("0")) ? "" : ("E" + exponent));
            if (!numberString.contains("E-") && !numberString.contains("E+")) {
                numberString = numberString.replace("E", "E+");
            }
        }
        
        if (numberString.isEmpty() || numberString.equals("0")) {
            return "0";
        }
        
        numberString = (negative ? '-' : "") + numberString;
        
        return numberString;
    }
    
    /**
     * Determines the reciprocal appendix for a particular number.
     *
     * @param number The number.
     * @return The index appendix for the number.
     */
    public static String reciprocalAppendix(Number number) {
        BigDecimal n = (number instanceof BigDecimal) ? (BigDecimal) number : new BigDecimal(number.toString());
        String numberString = n.stripTrailingZeros().toPlainString();
        String last = StringUtility.rSnip(numberString, 1);
        
        if (numberString.contains(".")) {
            return NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][0];
        }
        
        if (numberString.length() > 1) {
            String lastTwo = StringUtility.rSnip(numberString, 2);
            if (lastTwo.equals("11") || lastTwo.equals("12") || lastTwo.equals("13")) {
                return NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][0];
            }
        }
        
        switch (last) {
            case "1":
            case "2":
            case "3":
                return NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][Integer.parseInt(last)];
            default:
                return NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][0];
        }
    }
    
    /**
     * Determines the power of ten order of magnitude of a number.
     *
     * @param number The number.
     * @return The power of ten order of magnitude of the number.
     */
    public static long powerOfTen(Number number) {
        BigDecimal n = (number instanceof BigDecimal) ? (BigDecimal) number : new BigDecimal(number.toString());
        String numberString = n.stripTrailingZeros().toString().toUpperCase().replaceAll("^-", "");
        
        if (numberString.contains("E")) {
            return Integer.parseInt(StringUtility.lShear(numberString, (numberString.indexOf("E") + 1)));
        }
        
        if (n.abs().compareTo(BigDecimal.ONE) > 0) {
            return (numberString.contains(".") ? numberString.indexOf(".") : numberString.length()) - 1;
        } else if (n.abs().compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        } else {
            numberString = numberString.replaceAll("^\\d*\\.", "");
            if (!numberString.isEmpty() && (numberString.charAt(0) == '0')) {
                return -numberString.replaceAll("[^0]+0*$", "").length() - 1;
            } else {
                return numberString.isEmpty() ? 0 : -1;
            }
        }
    }
    
    /**
     * Determines the power of ten order of magnitude of a number to the previous thousands place.
     *
     * @param number The number.
     * @return The power of ten order of magnitude of the number to the previous thousands place.
     * @see #powerOfTen(Number)
     */
    public static long powerOfTenTruncated(Number number) {
        long powerOfTen = powerOfTen(number);
        return powerOfTen - (powerOfTen % 3);
    }
    
    /**
     * Determines the power of one thousand order of magnitude of a number.
     *
     * @param number The number.
     * @return The power of one thousand order of magnitude of the number.
     * @see #powerOfTenTruncated(Number)
     */
    public static long latinPower(Number number) {
        return powerOfTenTruncated(number) / 3;
    }
    
    /**
     * Determines the name of a number less than one thousand.
     *
     * @param number The number.
     * @return The name of the number.
     */
    public static String smallNumberName(int number) {
        if (!BoundUtility.inBounds(number, 0, 999)) {
            return "";
        }
        List<String> nameParts = new ArrayList<>();
        
        int hundreds = number / 100;
        int tens = (number % 100) / 10;
        int ones = number % 10;
        
        if (hundreds > 0) {
            nameParts.add(NUMBER_NAMES[NumberNameSet.DIGITS.ordinal()][hundreds]);
            nameParts.add(NUMBER_NAMES[NumberNameSet.HUNDREDS.ordinal()][1]);
        }
        
        if (tens > 0) {
            if (tens < 2) {
                nameParts.add(NUMBER_NAMES[NumberNameSet.TEENS.ordinal()][ones]);
                ones = 0;
            } else {
                nameParts.add(NUMBER_NAMES[NumberNameSet.TENS.ordinal()][tens]);
            }
        }
        
        if ((ones > 0) || nameParts.isEmpty()) {
            nameParts.add(NUMBER_NAMES[NumberNameSet.DIGITS.ordinal()][ones]);
        }
        
        return String.join(" ", nameParts);
    }
    
    /**
     * Determines the name of the order of magnitude of a power of ten.
     *
     * @param powerOfTen The power of ten.
     * @param dashes     Whether or not to include dashes in the name.
     * @return The name of the order of magnitude of the power of ten.
     * @see #latinPowerName(long, boolean)
     */
    public static String powerOfTenName(long powerOfTen, boolean dashes) {
        boolean fraction = (powerOfTen < 0);
        powerOfTen = Math.abs(powerOfTen);
        long latinPower = (powerOfTen - (powerOfTen % 3)) / 3;
        int powerOverLatin = (int) (powerOfTen % 3);
        
        String name = NUMBER_NAMES[powerOverLatin][1] + ((latinPower > 0) ? ' ' : "") +
                latinPowerName(latinPower, dashes);
        
        return name + (fraction ? NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][0] : "");
    }
    
    /**
     * Determines the name of the order of magnitude of a power of ten.
     *
     * @param powerOfTen The power of ten.
     * @return The name of the order of magnitude of the power of ten.
     * @see #powerOfTenName(long, boolean)
     */
    public static String powerOfTenName(long powerOfTen) {
        return powerOfTenName(powerOfTen, false);
    }
    
    /**
     * Determines the name of the order of magnitude of a latin power.
     *
     * @param latinPower The latin power.
     * @param dashes     Whether or not to include dashes in the name.
     * @return The name of the order of magnitude of the latin power.
     */
    public static String latinPowerName(long latinPower, boolean dashes) {
        boolean fraction = (latinPower < 0);
        latinPower = Math.abs(latinPower);
        
        if (latinPower == 0) {
            return "";
        }
        List<String> nameParts = new ArrayList<>();
        
        if (BoundUtility.inBounds(latinPower, 1, 10)) {
            nameParts.add(NUMBER_NAMES[NumberNameSet.LATIN_SPECIAL.ordinal()][(int) latinPower]);
            nameParts.add((latinPower > 1) ? NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()][NumberNameSuffix.SPECIAL.ordinal()] : "");
            
        } else {
            int milliaCount = 0;
            latinPower--;
            while (latinPower > 0) {
                
                int hundreds = (int) ((latinPower % 1000) / 100);
                int tens = (int) ((latinPower % 100) / 10);
                int ones = (int) (latinPower % 10);
                
                if (milliaCount == 0) {
                    nameParts.add(0, NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()][((tens == 1) ? NumberNameSuffix.SMALL.ordinal() : NumberNameSuffix.STANDARD.ordinal())]);
                } else {
                    if ((latinPower % 1000) >= 1) {
                        for (int i = 0; i < milliaCount; i++) {
                            nameParts.add(0, NUMBER_NAMES[NumberNameSet.LATIN_THOUSANDS_SEPARATORS.ordinal()][1]);
                        }
                    }
                    if (latinPower == 1) {
                        break;
                    }
                }
                milliaCount++;
                
                nameParts.add(0, NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()][tens]);
                nameParts.add(0, NUMBER_NAMES[NumberNameSet.LATIN_ONES_PREFIXES.ordinal()][ones]);
                nameParts.add(0, NUMBER_NAMES[NumberNameSet.LATIN_HUNDREDS_PREFIXES.ordinal()][hundreds]);
                
                latinPower /= 1000;
            }
        }
        
        if (fraction) {
            nameParts.add(NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][0]);
        }
        
        return nameParts.stream().filter(e -> !e.isEmpty())
                .collect(Collectors.joining(dashes ? "-" : ""));
    }
    
    /**
     * Determines the name of the order of magnitude of a latin power.
     *
     * @param latinPower The latin power.
     * @return The name of the order of magnitude of the latin power.
     * @see #latinPowerName(long, boolean)
     */
    public static String latinPowerName(long latinPower) {
        return latinPowerName(latinPower, false);
    }
    
    /**
     * Converts a latin power name into its latin power equivalent.
     *
     * @param latinPowerName The latin power name.
     * @return The latin power.
     * @throws NumberFormatException When the latin power name is not valid.
     */
    public static long latinPowerNameToLatinPower(String latinPowerName) throws NumberFormatException {
        final String originalLatinPowerName = latinPowerName;
        latinPowerName = StringUtility.removeWhiteSpace(latinPowerName.toLowerCase().replace("-", ""));
        final String cleanedLatinPowerName = latinPowerName;
        
        try {
            boolean isNegative = false;
            if (latinPowerName.endsWith(NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][0]) ||
                    latinPowerName.endsWith(NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][0] +
                            NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.PLURAL.ordinal()])) {
                latinPowerName = latinPowerName.replaceAll((NUMBER_NAMES[NumberNameSet.FRACTIONAL.ordinal()][0] +
                        NUMBER_NAMES[NumberNameSet.MODIFIERS.ordinal()][NumberNameModifier.PLURAL.ordinal()] + "?$"), "");
                isNegative = true;
            }
            
            if (latinPowerName.isEmpty()) {
                return 0L;
            }
            
            final Matcher latinPowerNameMatcher = LATIN_POWER_NAME_PATTERN.matcher(latinPowerName);
            if (!latinPowerNameMatcher.matches()) {
                throw new NumberFormatException();
            }
            List<String> nameParts = StringUtility.tokenize(latinPowerName, VALID_LATIN_POWER_NAME_TOKENS, false);
            if (nameParts == null) {
                throw new NumberFormatException();
            } else if (nameParts.isEmpty()) {
                return 0L;
            }
            nameParts = ListUtility.reverse(nameParts);
            
            if (ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()], nameParts.get(0)) && (nameParts.size() >= 2)) {
                if ((nameParts.size() > 2) && nameParts.get(0).equals(NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()][NumberNameSuffix.SPECIAL.ordinal()])) {
                    if (nameParts.get(1).endsWith(StringUtility.lSnip(NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()][NumberNameSuffix.SMALL.ordinal()], 1))) {
                        nameParts.set(1, StringUtility.rShear(nameParts.get(1), 1));
                        nameParts.set(0, NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()][NumberNameSuffix.SMALL.ordinal()]);
                    } else {
                        throw new NumberFormatException();
                    }
                }
                if (nameParts.get(0).equals(NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()][NumberNameSuffix.SMALL.ordinal()]) &&
                        !nameParts.get(1).equals(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()][1])) {
                    if (nameParts.get(1).endsWith(StringUtility.lSnip(NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()][NumberNameSuffix.STANDARD.ordinal()], 1))) {
                        nameParts.set(1, StringUtility.rShear(nameParts.get(1), 1));
                        nameParts.set(0, NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()][NumberNameSuffix.STANDARD.ordinal()]);
                    } else {
                        throw new NumberFormatException();
                    }
                }
                
                boolean validSuffix = false;
                switch (NumberNameSuffix.values()[ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.SUFFIXES.ordinal()], nameParts.get(0))]) {
                    case SPECIAL:
                        validSuffix = ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_SPECIAL.ordinal()], nameParts.get(1));
                        break;
                    case SMALL:
                        validSuffix = nameParts.get(1).equals(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()][1]);
                        break;
                    case STANDARD:
                        validSuffix = !ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_SPECIAL.ordinal()], nameParts.get(1)) &&
                                !nameParts.get(1).equals(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()][1]);
                        break;
                }
                if (!validSuffix) {
                    throw new NumberFormatException();
                }
                nameParts.remove(0);
                
            } else if (!nameParts.get(0).equals(NUMBER_NAMES[NumberNameSet.LATIN_SPECIAL.ordinal()][1])) {
                throw new NumberFormatException();
            }
            
            long latinPower = 0;
            int lastMilliaCount = -1;
            while (!nameParts.isEmpty()) {
                
                int milliaCount = 0;
                while (!nameParts.isEmpty()) {
                    if (nameParts.get(0).equals(NUMBER_NAMES[NumberNameSet.LATIN_THOUSANDS_SEPARATORS.ordinal()][1])) {
                        milliaCount++;
                        nameParts.remove(0);
                    } else {
                        break;
                    }
                }
                if (milliaCount <= lastMilliaCount) {
                    throw new NumberFormatException();
                }
                lastMilliaCount = milliaCount;
                
                long tmpLatinPower;
                int namePartsUsed = 1;
                if ((nameParts.size() >= 3) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_HUNDREDS_PREFIXES.ordinal()], nameParts.get(2)) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_ONES_PREFIXES.ordinal()], nameParts.get(1)) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()], nameParts.get(0))) {
                    tmpLatinPower = (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_HUNDREDS_PREFIXES.ordinal()], nameParts.get(2)) * 100L) +
                            (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_ONES_PREFIXES.ordinal()], nameParts.get(1))) +
                            (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()], nameParts.get(0)) * 10L);
                    namePartsUsed = 3;
                } else if ((nameParts.size() >= 2) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_HUNDREDS_PREFIXES.ordinal()], nameParts.get(1)) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()], nameParts.get(0))) {
                    tmpLatinPower = (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_HUNDREDS_PREFIXES.ordinal()], nameParts.get(1)) * 100L) +
                            (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()], nameParts.get(0)) * 10L);
                    namePartsUsed = 2;
                } else if ((nameParts.size() >= 2) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_HUNDREDS_PREFIXES.ordinal()], nameParts.get(1)) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_ONES_PREFIXES.ordinal()], nameParts.get(0))) {
                    tmpLatinPower = (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_HUNDREDS_PREFIXES.ordinal()], nameParts.get(1)) * 100L) +
                            (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_ONES_PREFIXES.ordinal()], nameParts.get(0)));
                    namePartsUsed = 2;
                } else if ((nameParts.size() >= 2) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_ONES_PREFIXES.ordinal()], nameParts.get(1)) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()], nameParts.get(0))) {
                    tmpLatinPower = (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_ONES_PREFIXES.ordinal()], nameParts.get(1))) +
                            (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()], nameParts.get(0)) * 10L);
                    namePartsUsed = 2;
                } else if ((nameParts.size() >= 1) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_HUNDREDS_PREFIXES.ordinal()], nameParts.get(0))) {
                    tmpLatinPower = (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_HUNDREDS_PREFIXES.ordinal()], nameParts.get(0)) * 100L);
                } else if ((nameParts.size() >= 1) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()], nameParts.get(0))) {
                    tmpLatinPower = (ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_TENS_PREFIXES.ordinal()], nameParts.get(0)) * 10L);
                } else if (((nameParts.size() > 1) || ((nameParts.size() == 1) && (milliaCount > 0))) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_ONES_PREFIXES.ordinal()], nameParts.get(0))) {
                    tmpLatinPower = ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_ONES_PREFIXES.ordinal()], nameParts.get(0));
                } else if ((nameParts.size() >= 1) &&
                        ArrayUtility.contains(NUMBER_NAMES[NumberNameSet.LATIN_SPECIAL.ordinal()], nameParts.get(0))) {
                    tmpLatinPower = ArrayUtility.indexOf(NUMBER_NAMES[NumberNameSet.LATIN_SPECIAL.ordinal()], nameParts.get(0)) - 1L;
                } else if (milliaCount > 0) {
                    tmpLatinPower = 1L;
                    namePartsUsed = 0;
                } else {
                    throw new NumberFormatException();
                }
                nameParts.subList(0, namePartsUsed).clear();
                
                latinPower += (tmpLatinPower * (long) (Math.pow(1000, milliaCount)));
            }
            
            return (latinPower + 1L) * (isNegative ? -1 : 1);
            
        } catch (NumberFormatException ignored) {
            throw new NumberFormatException("The string: " + StringUtility.quote(originalLatinPowerName, true) + " does not represent a valid latin power name");
        }
    }
    
}
