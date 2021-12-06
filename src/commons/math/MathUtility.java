/*
 * File:    MathUtility.java
 * Package: commons.math
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional math functionality.
 */
public class MathUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(MathUtility.class);
    
    
    //Functions
    
    /**
     * Returns a random number between two values.
     *
     * @param min The minimum possible value.
     * @param max The maximum possible value.
     * @return A random number between the minimum and maximum values.
     */
    public static long random(long min, long max) {
        return (long) (Math.random() * (max - min + 1)) + min;
    }
    
    /**
     * Returns a random number between two values.
     *
     * @param min The minimum possible value.
     * @param max The maximum possible value.
     * @return A random number between the minimum and maximum values.
     * @see #random(long, long)
     */
    public static int random(int min, int max) {
        return (int) random(min, (long) max);
    }
    
    /**
     * Returns a random number between 0 and a value.
     *
     * @param max The maximum possible value.
     * @return A random number between 0 and the maximum values.
     * @see #random(long, long)
     */
    public static long random(long max) {
        return random(0, max);
    }
    
    /**
     * Returns a random number between 0 and a value.
     *
     * @param max The maximum possible value.
     * @return A random number between 0 and the maximum values.
     * @see #random(int, int)
     */
    public static int random(int max) {
        return random(0, max);
    }
    
    /**
     * Returns the result of a dice roll.
     *
     * @param sides The number of sides on the dice.
     * @param rolls The number of rolls to perform.
     * @return The result of the dice roll.
     * @see #random(long, long)
     */
    public static long dice(long sides, long rolls) {
        if ((sides <= 0) || (rolls <= 0)) {
            return 0;
        }
        
        long roll = 0;
        for (int i = 0; i < rolls; i++) {
            roll += random(1, sides);
        }
        return roll;
    }
    
    /**
     * Returns the result of a dice roll.
     *
     * @param sides The number of sides on the dice.
     * @param rolls The number of rolls to perform.
     * @return The result of the dice roll.
     * @see #dice(long, long)
     */
    public static int dice(int sides, int rolls) {
        return (int) dice(sides, (long) rolls);
    }
    
    /**
     * Returns the result of a dice roll.
     *
     * @param sides The number of sides on the dice.
     * @return The result of the dice roll.
     * @see #dice(long, long)
     */
    public static long dice(long sides) {
        return dice(sides, 1);
    }
    
    /**
     * Returns the result of a dice roll.
     *
     * @param sides The number of sides on the dice.
     * @return The result of the dice roll.
     * @see #dice(int, int)
     */
    public static int dice(int sides) {
        return dice(sides, 1);
    }
    
    /**
     * Returns the result of a coin flip.
     *
     * @return The result of the coin flip; 50% true / 50% false.
     */
    public static boolean coinFlip() {
        return (Math.random() < 0.5);
    }
    
    /**
     * Determines if a number is a perfect square or not.
     *
     * @param value The number.
     * @return Whether the number is a perfect square or not.
     */
    public static boolean isSquare(long value) {
        if (value < 0) {
            return false;
        }
        
        double sqrt = Math.sqrt(value);
        return sqrt - Math.floor(sqrt) == 0;
    }
    
    /**
     * Determines if a number is prime or not.
     *
     * @param value The number.
     * @return Whether the number is prime or not.
     */
    public static boolean isPrime(long value) {
        if (value < 2) {
            return false;
        }
        
        for (long i = 2; i <= value / 2; i++) {
            if (value % i == 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Determines if a number is evenly divisible by another number.
     *
     * @param num     The number.
     * @param divisor The divisor
     * @return Whether the number is divisible by the divisor or not.
     */
    public static boolean isDivisibleBy(long num, long divisor) {
        return (divisor != 0) && ((num % divisor) == 0);
    }
    
    /**
     * Calculates the modulus of a number but returns a value in the range [1,mod] rather than [0,mod-1].
     *
     * @param num The number.
     * @param mod The modulus.
     * @return The adjusted modulus of the number.
     */
    public static long xmod(long num, long mod) {
        return (isDivisibleBy(num, mod)) ?
               (Math.abs(mod) * ((num >= 0) ? 1 : -1)) :
               (num % mod);
    }
    
    /**
     * Rounds a decimal number with a certain precision.
     *
     * @param value         The number.
     * @param decimalPlaces The maximum number of decimal places of the result.
     * @return The rounded number.
     */
    public static double roundWithPrecision(double value, int decimalPlaces) {
        double decimalInverse = Math.pow(10.0, decimalPlaces);
        return (double) Math.round(value * decimalInverse) / decimalInverse;
    }
    
    /**
     * Rounds a Big Decimal number with a certain precision.
     *
     * @param value         The number.
     * @param decimalPlaces The maximum number of decimal places of the result.
     * @param roundingMode  The rounding mode to use when rounding the result.
     * @return The rounded number.
     */
    public static BigDecimal roundWithPrecision(BigDecimal value, int decimalPlaces, RoundingMode roundingMode) {
        return new BigDecimal(value.setScale(decimalPlaces, roundingMode).stripTrailingZeros().toPlainString());
    }
    
    /**
     * Rounds a Big Decimal number with a certain precision.
     *
     * @param value         The number.
     * @param decimalPlaces The maximum number of decimal places of the result.
     * @return The rounded number.
     * @see #roundWithPrecision(BigDecimal, int, RoundingMode)
     */
    public static BigDecimal roundWithPrecision(BigDecimal value, int decimalPlaces) {
        return roundWithPrecision(value, decimalPlaces, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculates the digit sum of a number.
     *
     * @param num The number.
     * @return The digit sum of the number.
     */
    public static int digitSum(long num) {
        int digitSum = 0;
        while (num != 0) {
            digitSum += (num % 10);
            num /= 10;
        }
        return Math.abs(digitSum);
    }
    
    /**
     * Calculates the alternating digit sum of a number.
     *
     * @param num The number.
     * @return The alternating digit sum of the number.
     */
    public static int digitSumAlternating(long num) {
        int digitSum = 0;
        int sign = NumberUtility.isEven(NumberUtility.length(num)) ? -1 : 1;
        while (num != 0) {
            digitSum += ((num % 10) * sign);
            num /= 10;
            sign *= -1;
        }
        return Math.abs(digitSum);
    }
    
    /**
     * Calculates the weighted digit sum of a number.
     *
     * @param num The number.
     * @return The weighted digit sum of the number.
     */
    public static int digitSumWeighted(long num) {
        int digitSum = 0;
        while (num != 0) {
            digitSum += ((num % 10) * NumberUtility.length(num));
            num /= 10;
        }
        return Math.abs(digitSum);
    }
    
    /**
     * Calculates the k digit sum of a number.
     *
     * @param num The number.
     * @return The k digit sum of the number.
     * @see #digitSum(long)
     */
    public static int digitSumK(long num, int k) {
        return digitSum(num) + k;
    }
    
    /**
     * Maps a value from one range to another.
     *
     * @param value       The value.
     * @param inputStart  The start of the input range.
     * @param inputEnd    The end of the input range.
     * @param outputStart The start of the output range.
     * @param outputEnd   The end of the output range.
     * @return The value after it has been mapped from the input range to the output range.
     */
    public static double mapValue(double value, double inputStart, double inputEnd, double outputStart, double outputEnd) {
        if (value < inputStart) {
            return outputStart;
        }
        if (value > inputEnd) {
            return outputEnd;
        }
        return (value - inputStart) / (inputEnd - inputStart) * (outputEnd - outputStart) + outputStart;
    }
    
}
