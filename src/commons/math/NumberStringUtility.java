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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import commons.string.StringUtility;
import org.apache.commons.lang3.StringUtils;
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
     * The names of digit values for power of ten naming.<br>
     * Array index 0 for ones, 1 for tens, 2 for hundreds.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String[][] POWER_OF_TEN_DIGIT_NAMES = new String[][] {
            {"", "un", "duo", "tre", "quattuor", "quinqua", "se", "septe", "octo", "nove"},
            {"", "deci", "viginti", "triginta", "quadraginta", "quinquaginta", "sexaginta", "septuaginta", "octoginta", "nonaginta"},
            {"", "centi", "ducenti", "trecenti", "quadringenti", "quingenti", "sescenti", "septingenti", "octingenti", "nongenti"}
    };
    
    /**
     * The prefixes of digit values for power of ten naming.<br>
     * Array index 0 for ones, 1 for tens, 2 for hundreds.
     */
    private static final String[][] POWER_OF_TEN_DIGIT_PREFIXES = new String[][] {
            {"", "", "", "", "", "", "", "", "", ""},
            {"", "n", "ms", "ns", "ns", "ns", "n", "n", "mx", ""},
            {"", "nx", "n", "ns", "ns", "ns", "n", "n", "mx", ""}
    };
    
    /**
     * The suffixes of digit values for power of ten naming.<br>
     * Array index 0 for ones, 1 for tens, 2 for hundreds.
     */
    private static final String[][] POWER_OF_TEN_DIGIT_SUFFIXES = new String[][] {
            {"", "", "", "s", "", "", "sx", "mn", "", "mn"},
            {"", "", "", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", "", "", ""}
    };
    
    /**
     * The list of allowed strings for string to number conversion.
     */
    private static final List<String> ALLOWED_STRINGS = Arrays.asList(
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty",
            "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety", "hundred",
            "point", "first", "second", "third", "fifth", "eighth", "ninth"
    );
    
    /**
     * The maximum power of ten recognized by this class.
     */
    public static final int MAX_POWER = 3003;
    
    /**
     * A map of magnitudes to their names.
     */
    public static final Map<Integer, String> MAGNITUDE_MAP = new LinkedHashMap<>();
    
    /**
     * A map of magnitude names to their magnitudes.
     */
    public static final Map<String, Integer> MAGNITUDE_NAME_MAP = new LinkedHashMap<>();
    
    //Populate MAGNITUDE_NAME_MAP
    static {
        for (int i = 3; i <= MAX_POWER; i += 3) {
            String name = powerOfTenName(i);
            MAGNITUDE_MAP.put(i, name);
            MAGNITUDE_NAME_MAP.put(name, i);
        }
    }
    
    /**
     * A regex pattern for numbers in exponential notation.
     */
    public static final Pattern EXPONENTIAL_NOTATION_PATTERN = Pattern.compile("-?(?<first>\\d)\\.(?<decimal>\\d+)E(?<mantissa>-?\\d+)");
    
    /**
     * A regex pattern for a string that is only zeros.
     */
    public static final Pattern ZERO_STRING_PATTERN = Pattern.compile("^0+$");
    
    
    //Functions
    
    /**
     * Returns the string value of a number.
     *
     * @param number The number.
     * @return The string value of the number.
     * @see BigDecimal#toPlainString()
     * @see #cleanNumberString(String)
     */
    public static String stringValueOf(Number number) {
        BigDecimal n = (number instanceof BigDecimal) ? (BigDecimal) number : new BigDecimal(number.toString());
        
        return cleanNumberString(n.toPlainString());
    }
    
    /**
     * Returns the number value of a number string.
     *
     * @param number The number string.
     * @return The number value of the number string.
     * @throws NumberFormatException When the string does not represent a number.
     */
    public static Number numberValueOf(String number) throws NumberFormatException {
        return new BigDecimal(number);
    }
    
    /**
     * Converts a number to a phrase that represents the number.
     *
     * @param number The number.
     * @return The phrase equivalent of the number.
     */
    public static String numberToNumberPhrase(Number number) {
        BigDecimal n = (number instanceof BigDecimal) ? (BigDecimal) number : new BigDecimal(number.toString());
        
        List<String> numberStrElements = new ArrayList<>();
        String stringValue = n.toPlainString();
        String whole = stringValue.contains(".") ? StringUtility.lSnip(stringValue, stringValue.indexOf('.')) : stringValue;
        String decimal = stringValue.contains(".") ? StringUtility.lShear(stringValue, (stringValue.indexOf('.') + 1)) : "0";
        
        if (whole.startsWith("-")) {
            numberStrElements.add("negative");
            whole = StringUtility.lShear(whole, 1);
        }
        
        if (whole.length() < 4) {
            int wholeNumber = Integer.parseInt(whole);
            if (wholeNumber < 20) {
                switch (wholeNumber) {
                    case 0:
                        numberStrElements.add("zero");
                        break;
                    case 1:
                        numberStrElements.add("one");
                        break;
                    case 2:
                        numberStrElements.add("two");
                        break;
                    case 3:
                        numberStrElements.add("three");
                        break;
                    case 4:
                        numberStrElements.add("four");
                        break;
                    case 5:
                        numberStrElements.add("five");
                        break;
                    case 6:
                        numberStrElements.add("six");
                        break;
                    case 7:
                        numberStrElements.add("seven");
                        break;
                    case 8:
                        numberStrElements.add("eight");
                        break;
                    case 9:
                        numberStrElements.add("nine");
                        break;
                    case 10:
                        numberStrElements.add("ten");
                        break;
                    case 11:
                        numberStrElements.add("eleven");
                        break;
                    case 12:
                        numberStrElements.add("twelve");
                        break;
                    case 13:
                        numberStrElements.add("thirteen");
                        break;
                    case 14:
                        numberStrElements.add("fourteen");
                        break;
                    case 15:
                        numberStrElements.add("fifteen");
                        break;
                    case 16:
                        numberStrElements.add("sixteen");
                        break;
                    case 17:
                        numberStrElements.add("seventeen");
                        break;
                    case 18:
                        numberStrElements.add("eighteen");
                        break;
                    case 19:
                        numberStrElements.add("nineteen");
                        break;
                }
                
            } else {
                if (wholeNumber >= 100) {
                    int magnitude = wholeNumber / 100;
                    numberStrElements.add(numberToNumberPhrase(magnitude));
                    numberStrElements.add("hundred");
                    wholeNumber -= (magnitude * 100);
                    if (wholeNumber > 0) {
                        numberStrElements.add("and");
                    }
                }
                
                if ((wholeNumber > 0) && (wholeNumber < 20)) {
                    numberStrElements.add(numberToNumberPhrase(wholeNumber));
                } else {
                    if (wholeNumber >= 10) {
                        int magnitude = wholeNumber / 10;
                        switch (magnitude) {
                            case 2:
                                numberStrElements.add("twenty");
                                break;
                            case 3:
                                numberStrElements.add("thirty");
                                break;
                            case 4:
                                numberStrElements.add("forty");
                                break;
                            case 5:
                                numberStrElements.add("fifty");
                                break;
                            case 6:
                                numberStrElements.add("sixty");
                                break;
                            case 7:
                                numberStrElements.add("seventy");
                                break;
                            case 8:
                                numberStrElements.add("eighty");
                                break;
                            case 9:
                                numberStrElements.add("ninety");
                                break;
                        }
                        wholeNumber -= (magnitude * 10);
                    }
                    
                    if (wholeNumber > 0) {
                        numberStrElements.add(numberToNumberPhrase(wholeNumber));
                    }
                }
            }
            
        } else {
            int powerOfTen = whole.length() - 1;
            int powerOfTenTruncated = (powerOfTen / 3) * 3;
            if (powerOfTen > MAX_POWER) {
                String magnitude = StringUtility.rShear(whole, MAX_POWER);
                numberStrElements.add(numberToNumberPhrase(new BigDecimal(magnitude)));
                numberStrElements.add(MAGNITUDE_MAP.get(MAX_POWER));
                whole = StringUtility.rSnip(whole, (MAX_POWER - 1));
                powerOfTen = whole.length() - 1;
                powerOfTenTruncated = (powerOfTen / 3) * 3;
            }
            while (MAGNITUDE_MAP.containsKey(powerOfTenTruncated) && !ZERO_STRING_PATTERN.matcher(whole).matches()) {
                int quantity = Integer.parseInt(StringUtility.lSnip(whole, (powerOfTen - powerOfTenTruncated + 1)));
                if (quantity > 0) {
                    numberStrElements.add(numberToNumberPhrase(quantity));
                    numberStrElements.add(MAGNITUDE_MAP.get(powerOfTenTruncated));
                }
                whole = StringUtility.lShear(whole, (powerOfTen - powerOfTenTruncated + 1));
                powerOfTen = whole.length() - 1;
                powerOfTenTruncated = (powerOfTen / 3) * 3;
            }
            
            BigDecimal wholeNumber = new BigDecimal(whole);
            if (wholeNumber.compareTo(BigDecimal.ZERO) > 0) {
                if (wholeNumber.compareTo(BigDecimal.valueOf(100)) < 0) {
                    numberStrElements.add("and");
                }
                numberStrElements.add(numberToNumberPhrase(wholeNumber));
            }
        }
        
        if (!decimal.equals("0")) {
            numberStrElements.add("point");
            
            while (decimal.length() > 0) {
                int decimalDigit = Integer.parseInt(StringUtility.lSnip(decimal, 1));
                numberStrElements.add(numberToNumberPhrase(decimalDigit));
                decimal = StringUtility.lShear(decimal, 1);
            }
        }
        
        StringBuilder numberStr = new StringBuilder();
        for (String numberStrElement : numberStrElements) {
            if (numberStr.length() > 0) {
                numberStr.append(' ');
            }
            numberStr.append(numberStrElement);
        }
        
        return numberStr.toString();
    }
    
    /**
     * Converts a number string to a phrase that represents the number.
     *
     * @param number The number, represented by a string.
     * @return The phrase equivalent of the number string.
     * @throws NumberFormatException When the string does not represent a number.
     * @see #numberToNumberPhrase(Number)
     */
    public static String numberStringToNumberPhrase(String number) throws NumberFormatException {
        return numberToNumberPhrase(new BigDecimal(number).stripTrailingZeros());
    }
    
    /**
     * Converts a phrase that represents a number into its numeric equivalent.
     *
     * @param number A phrase that represents a number.
     * @return The numeric equivalent of the string.
     * @throws NumberFormatException When the string does not represent a number.
     */
    public static Number numberPhraseToNumber(String number) throws NumberFormatException {
        boolean isValidInput = true;
        boolean isNegative = false;
        boolean point = false;
        
        BigDecimal result = BigDecimal.ZERO;
        StringBuilder wholeResult = new StringBuilder();
        StringBuilder partialResult = new StringBuilder();
        StringBuilder decimalResult = new StringBuilder();
        
        if (StringUtils.isEmpty(number)) {
            throw new NumberFormatException("Not a number: " + number);
            
        } else {
            if (number.startsWith("-") || number.startsWith("negative") || number.startsWith("neg")) {
                isNegative = true;
            }
            number = number.replaceAll("(^-)|(-\\s)|(negative\\s)|(neg\\s)|(\\sand)", " ");
            
            List<String> splitParts = StringUtility.tokenize(number.trim());
            Boolean[] isNumber = new Boolean[splitParts.size()];
            for (int n = 0; n < splitParts.size(); n++) {
                String str = splitParts.get(n);
                isNumber[n] = false;
                try {
                    BigDecimal i = new BigDecimal(str);
                    isNumber[n] = true;
                    
                } catch (NumberFormatException ignored) {
                    try {
                        BigDecimal i = new BigDecimal(StringUtility.rShear(str, 2));
                        isNumber[n] = true;
                        str = StringUtility.rShear(str, 2);
                        splitParts.set(n, str);
                        
                    } catch (NumberFormatException ignored2) {
                        if (!ALLOWED_STRINGS.contains(str.toLowerCase()) && !MAGNITUDE_NAME_MAP.containsKey(str.toLowerCase())) {
                            if (str.endsWith("st") || str.endsWith("nd") || str.endsWith("rd") || str.endsWith("th")) {
                                str = StringUtility.rShear(str, 2);
                                splitParts.set(n, str);
                                if (ALLOWED_STRINGS.contains(str.toLowerCase()) || MAGNITUDE_NAME_MAP.containsKey(str.toLowerCase())) {
                                    continue;
                                }
                            }
                            isValidInput = false;
                            break;
                        }
                    }
                }
            }
            
            if (isValidInput) {
                for (int n = 0; n < splitParts.size(); n++) {
                    String str = splitParts.get(n);
                    
                    boolean addResult = true;
                    boolean addPoint = true;
                    int memory = 0;
                    BigDecimal bigMemory = null;
                    String intermediateResult = "";
                    
                    if (isNumber[n]) {
                        bigMemory = new BigDecimal(str);
                        
                    } else {
                        switch (str.toLowerCase()) {
                            case "zero":
                                memory = 0;
                                break;
                            case "one":
                            case "first":
                                memory = 1;
                                break;
                            case "two":
                            case "second":
                                memory = 2;
                                break;
                            case "three":
                            case "third":
                                memory = 3;
                                break;
                            case "four":
                                memory = 4;
                                break;
                            case "five":
                            case "fifth":
                                memory = 5;
                                break;
                            case "six":
                                memory = 6;
                                break;
                            case "seven":
                                memory = 7;
                                break;
                            case "eight":
                            case "eighth":
                                memory = 8;
                                break;
                            case "nine":
                            case "ninth":
                                memory = 9;
                                break;
                            case "ten":
                                memory = 10;
                                break;
                            case "eleven":
                                memory = 11;
                                break;
                            case "twelve":
                                memory = 12;
                                break;
                            case "thirteen":
                                memory = 13;
                                break;
                            case "fourteen":
                                memory = 14;
                                break;
                            case "fifteen":
                                memory = 15;
                                break;
                            case "sixteen":
                                memory = 16;
                                break;
                            case "seventeen":
                                memory = 17;
                                break;
                            case "eighteen":
                                memory = 18;
                                break;
                            case "nineteen":
                                memory = 19;
                                break;
                            case "twenty":
                                memory = 20;
                                break;
                            case "thirty":
                                memory = 30;
                                break;
                            case "forty":
                                memory = 40;
                                break;
                            case "fifty":
                                memory = 50;
                                break;
                            case "sixty":
                                memory = 60;
                                break;
                            case "seventy":
                                memory = 70;
                                break;
                            case "eighty":
                                memory = 80;
                                break;
                            case "ninety":
                                memory = 90;
                                break;
                            
                            case "hundred":
                                addResult = false;
                                addPoint = false;
                                result = result.multiply(BigDecimal.valueOf(100));
                                break;
                            
                            case "point":
                                addResult = false;
                                addPoint = false;
                                
                                if (result.compareTo(BigDecimal.ZERO) > 0) {
                                    String resultPart = String.valueOf(result);
                                    if (partialResult.length() == 0) {
                                        partialResult.append(resultPart);
                                    } else {
                                        resultPart = StringUtility.padLeft(resultPart, 3, '0');
                                        for (int i = 0; i < resultPart.length(); i++) {
                                            partialResult.setCharAt(partialResult.length() - 3 + i, resultPart.charAt(i));
                                        }
                                    }
                                    if (wholeResult.length() == 0) {
                                        wholeResult.append(partialResult);
                                    } else {
                                        for (int i = 0; i < partialResult.length(); i++) {
                                            wholeResult.setCharAt(wholeResult.length() - partialResult.length() + i, partialResult.charAt(i));
                                        }
                                    }
                                    partialResult = new StringBuilder();
                                    result = BigDecimal.ZERO;
                                }
                                point = true;
                                break;
                            
                            default:
                                addResult = false;
                                addPoint = false;
                                
                                int magnitude = MAGNITUDE_NAME_MAP.get(str.toLowerCase());
                                
                                String resultPart = String.valueOf(result);
                                if (partialResult.length() == 0) {
                                    partialResult.append(StringUtility.fillStringOfLength('0', magnitude + resultPart.length()));
                                } else {
                                    resultPart = StringUtility.padLeft(resultPart, 3, '0');
                                }
                                for (int i = 0; i < resultPart.length(); i++) {
                                    partialResult.setCharAt(partialResult.length() - (magnitude + resultPart.length()) + i, resultPart.charAt(i));
                                }
                                
                                boolean doubleMagnitude = false;
                                magnitude = 0;
                                while ((n < (splitParts.size() - 1)) && MAGNITUDE_NAME_MAP.containsKey(splitParts.get(n + 1).toLowerCase())) {
                                    magnitude += MAGNITUDE_NAME_MAP.get(splitParts.get(n + 1).toLowerCase());
                                    doubleMagnitude = true;
                                    n++;
                                }
                                
                                if (doubleMagnitude && (partialResult.length() > 0)) {
                                    partialResult.append(StringUtility.fillStringOfLength('0', magnitude));
                                    
                                    if (wholeResult.length() == 0) {
                                        wholeResult.append(partialResult);
                                    } else {
                                        for (int i = 0; i < partialResult.length(); i++) {
                                            wholeResult.setCharAt(wholeResult.length() - partialResult.length() + i, partialResult.charAt(i));
                                        }
                                    }
                                    partialResult = new StringBuilder();
                                }
                                
                                result = BigDecimal.ZERO;
                        }
                    }
                    
                    if (addPoint && point) {
                        decimalResult.append((bigMemory == null) ? memory : bigMemory.toPlainString());
                    } else if (addResult) {
                        result = result.add((bigMemory == null) ? BigDecimal.valueOf(memory) : bigMemory);
                    }
                }
                
                if (point) {
                    if (decimalResult.length() > 0) {
                        wholeResult.append((wholeResult.length() == 0) ? '0' : "").append('.').append(decimalResult);
                    }
                    
                } else {
                    if (result.compareTo(BigDecimal.ZERO) > 0) {
                        String resultPart = String.valueOf(result);
                        if (partialResult.length() == 0) {
                            partialResult.append(resultPart);
                        } else {
                            resultPart = StringUtility.padLeft(resultPart, 3, '0');
                            for (int i = 0; i < resultPart.length(); i++) {
                                partialResult.setCharAt(partialResult.length() - 3 + i, resultPart.charAt(i));
                            }
                        }
                    }
                    
                    if (wholeResult.length() == 0) {
                        wholeResult.append(partialResult);
                    } else {
                        for (int i = 0; i < partialResult.length(); i++) {
                            wholeResult.setCharAt(wholeResult.length() - partialResult.length() + i, partialResult.charAt(i));
                        }
                    }
                    
                    if (wholeResult.length() == 0) {
                        wholeResult.append('0');
                    }
                }
                
                if (isNegative) {
                    wholeResult.insert(0, '-');
                }
                
                return new BigDecimal(wholeResult.toString());
                
            } else {
                throw new NumberFormatException("Not a number: " + number);
            }
        }
    }
    
    /**
     * Converts a phrase that represents a number into its number string equivalent.
     *
     * @param number A phrase that represents a number.
     * @return The number string equivalent of the string.
     * @throws NumberFormatException When the string does not represent a number.
     * @see #numberPhraseToNumber(String)
     * @see #stringValueOf(Number)
     */
    public static String numberPhraseToNumberString(String number) throws NumberFormatException {
        return stringValueOf(numberPhraseToNumber(number));
    }
    
    /**
     * Cleans a number string produces by a math operation.
     *
     * @param number The number string.
     * @return The cleaned number string.
     */
    public static String cleanNumberString(String number) {
        boolean negative = number.startsWith("-");
        if (negative) {
            number = StringUtility.lShear(number, 1);
        }
        
        while (!number.isEmpty() && (number.charAt(0) == '0')) {
            number = StringUtility.lShear(number, 1);
        }
        
        if (!number.isEmpty() && number.contains(".")) {
            while (number.charAt(number.length() - 1) == '0') {
                if (number.length() == 1) {
                    break;
                }
                number = StringUtility.rShear(number, 1);
            }
        }
        
        if (!number.isEmpty() && (number.charAt(number.length() - 1) == '.')) {
            number = StringUtility.rShear(number, 1);
        }
        
        if (number.isEmpty()) {
            return "0";
        }
        
        if (number.charAt(0) == '.') {
            number = '0' + number;
        }
        
        if (negative) {
            number = '-' + number;
        }
        
        return number;
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
        boolean decimal = numberString.contains(".");
        
        if (!decimal && (numberString.length() > 1)) {
            String lastTwo = StringUtility.rSnip(numberString, 2);
            if (lastTwo.equals("11") || lastTwo.equals("12") || lastTwo.equals("13")) {
                return "th";
            }
        }
        
        switch (last) {
            case "1":
                return "st";
            case "2":
                return "nd";
            case "3":
                return "rd";
            default:
                return "th";
        }
    }
    
    /**
     * Determines the power of ten order of magnitude of a number.
     *
     * @param number The number.
     * @return The power of ten order of magnitude of the number.
     */
    public static int powerOfTen(Number number) {
        BigDecimal n = (number instanceof BigDecimal) ? (BigDecimal) number : new BigDecimal(number.toString());
        
        String numberString = n.toPlainString();
        if (numberString.contains("-")) {
            numberString = StringUtility.lShear(numberString, 1);
        }
        
        if (n.abs().compareTo(BigDecimal.valueOf(1)) > 0) {
            if (numberString.contains(".")) {
                return StringUtility.lSnip(numberString, numberString.indexOf(".")).length() - 1;
            } else {
                return numberString.length() - 1;
            }
        } else {
            numberString = StringUtility.lShear(numberString, (numberString.indexOf(".") + 1));
            for (int i = 0; i < numberString.length(); i++) {
                if (numberString.charAt(i) != '0') {
                    return (i + 1) * -1;
                }
            }
        }
        return 0;
    }
    
    /**
     * Determines the power of ten order of magnitude of a number to the previous thousands place.
     *
     * @param number The number.
     * @return The power of ten order of magnitude of the number to the previous thousands place.
     * @see #powerOfTen(Number)
     */
    public static int powerOfTenTruncated(Number number) {
        int powerOfTen = powerOfTen(number);
        return (powerOfTen / 3) * 3;
    }
    
    /**
     * Determines the name of the order of magnitude of a power of ten.
     *
     * @param powerOfTen The power of ten.
     * @return The name of the order of magnitude of the power of ten.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static String powerOfTenName(int powerOfTen) {
        boolean fraction = (powerOfTen < 0);
        powerOfTen = Math.abs(powerOfTen);
        if ((powerOfTen <= 0) || (powerOfTen > MAX_POWER) || (((powerOfTen % 3) != 0) && (powerOfTen != 2))) {
            return "";
        }
        
        String name = "";
        switch (powerOfTen) {
            case 2:
                name = "hundred";
                break;
            case 3:
                name = "thousand";
                break;
            case 6:
                name = "million";
                break;
            case 9:
                name = "billion";
                break;
            case 12:
                name = "trillion";
                break;
            case 15:
                name = "quadrillion";
                break;
            case 18:
                name = "quintillion";
                break;
            case 21:
                name = "sextillion";
                break;
            case 24:
                name = "septillion";
                break;
            case 27:
                name = "octillion";
                break;
            case 30:
                name = "nonillion";
                break;
            case 3003:
                name = "millinillion";
                break;
            
            default:
                int fixedPowerOfTen = ((powerOfTen - 3) / 3);
                
                int hundreds = fixedPowerOfTen / 100;
                int tens = (fixedPowerOfTen % 100) / 10;
                int ones = (fixedPowerOfTen % 10);
                
                name += POWER_OF_TEN_DIGIT_NAMES[0][ones];
                for (char c : POWER_OF_TEN_DIGIT_SUFFIXES[0][ones].toCharArray()) {
                    if (POWER_OF_TEN_DIGIT_PREFIXES[1][tens].contains(String.valueOf(c))) {
                        name += c;
                        break;
                    }
                }
                name += POWER_OF_TEN_DIGIT_NAMES[1][tens];
                for (char c : POWER_OF_TEN_DIGIT_SUFFIXES[1][tens].toCharArray()) {
                    if (POWER_OF_TEN_DIGIT_PREFIXES[2][hundreds].contains(String.valueOf(c))) {
                        name += c;
                        break;
                    }
                }
                name += POWER_OF_TEN_DIGIT_NAMES[2][hundreds];
                
                if (name.endsWith("a")) {
                    name = StringUtility.rShear(name, 1) + 'i';
                }
                
                name += "llion";
                break;
        }
        
        return name + (fraction ? "th" : "");
    }
    
}
