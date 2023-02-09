/*
 * File:    StringMathUtility.java
 * Package: commons.math.string
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.math.string;

import java.math.BigDecimal;

import commons.math.big.BigMathUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides string math functionality.
 */
public class StringMathUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(StringMathUtility.class);
    
    
    //Static Methods
    
    /**
     * Adds two numbers represented by strings.
     *
     * @param n1        The first number.
     * @param n2        The second number.
     * @param precision The number of significant figures to return.
     * @return The sum of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#add(BigDecimal, BigDecimal, int)
     */
    public static String add(String n1, String n2, int precision) throws NumberFormatException {
        return BigMathUtility.add(new BigDecimal(n1), new BigDecimal(n2), precision).toPlainString();
    }
    
    /**
     * Adds two numbers represented by strings.
     *
     * @param n1            The first number.
     * @param n2            The second number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The sum of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#add(BigDecimal, BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String add(String n1, String n2, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.add(new BigDecimal(n1), new BigDecimal(n2), precisionMode).toPlainString();
    }
    
    /**
     * Adds two numbers represented by strings.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return The sum of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#add(BigDecimal, BigDecimal)
     */
    public static String add(String n1, String n2) throws NumberFormatException {
        return BigMathUtility.add(new BigDecimal(n1), new BigDecimal(n2)).toPlainString();
    }
    
    /**
     * Subtracts two numbers represented by strings.
     *
     * @param n1        The first number.
     * @param n2        The second number.
     * @param precision The number of significant figures to return.
     * @return The difference of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#subtract(BigDecimal, BigDecimal, int)
     */
    public static String subtract(String n1, String n2, int precision) throws NumberFormatException {
        return BigMathUtility.subtract(new BigDecimal(n1), new BigDecimal(n2), precision).toPlainString();
    }
    
    /**
     * Subtracts two numbers represented by strings.
     *
     * @param n1            The first number.
     * @param n2            The second number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The difference of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#subtract(BigDecimal, BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String subtract(String n1, String n2, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.subtract(new BigDecimal(n1), new BigDecimal(n2), precisionMode).toPlainString();
    }
    
    /**
     * Subtracts two numbers represented by strings.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return The difference of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#subtract(BigDecimal, BigDecimal)
     */
    public static String subtract(String n1, String n2) throws NumberFormatException {
        return BigMathUtility.subtract(new BigDecimal(n1), new BigDecimal(n2)).toPlainString();
    }
    
    /**
     * Multiplies two numbers represented by strings.
     *
     * @param n1        The first number.
     * @param n2        The second number.
     * @param precision The number of significant figures to return.
     * @return The product of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#multiply(BigDecimal, BigDecimal, int)
     */
    public static String multiply(String n1, String n2, int precision) throws NumberFormatException {
        return BigMathUtility.multiply(new BigDecimal(n1), new BigDecimal(n2), precision).toPlainString();
    }
    
    /**
     * Multiplies two numbers represented by strings.
     *
     * @param n1            The first number.
     * @param n2            The second number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The product of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#multiply(BigDecimal, BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String multiply(String n1, String n2, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.multiply(new BigDecimal(n1), new BigDecimal(n2), precisionMode).toPlainString();
    }
    
    /**
     * Multiplies two numbers represented by strings.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return The product of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#multiply(BigDecimal, BigDecimal)
     */
    public static String multiply(String n1, String n2) throws NumberFormatException {
        return BigMathUtility.multiply(new BigDecimal(n1), new BigDecimal(n2)).toPlainString();
    }
    
    /**
     * Divides two numbers represented by strings.
     *
     * @param n1        The dividend.
     * @param n2        The divisor.
     * @param precision The number of significant figures to return.
     * @return The quotient of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the divisor is zero.
     * @see BigMathUtility#divide(BigDecimal, BigDecimal, int)
     */
    public static String divide(String n1, String n2, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.divide(new BigDecimal(n1), new BigDecimal(n2), precision).toPlainString();
    }
    
    /**
     * Divides two numbers represented by strings.
     *
     * @param n1            The dividend.
     * @param n2            The divisor.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The quotient of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the divisor is zero.
     * @see BigMathUtility#divide(BigDecimal, BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String divide(String n1, String n2, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.divide(new BigDecimal(n1), new BigDecimal(n2), precisionMode).toPlainString();
    }
    
    /**
     * Divides two numbers represented by strings.
     *
     * @param n1 The dividend.
     * @param n2 The divisor.
     * @return The quotient of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the divisor is zero.
     * @see BigMathUtility#divide(BigDecimal, BigDecimal)
     */
    public static String divide(String n1, String n2) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.divide(new BigDecimal(n1), new BigDecimal(n2)).toPlainString();
    }
    
    /**
     * Performs the modulus operation on two numbers represented by strings.
     *
     * @param n1        The dividend.
     * @param n2        The divisor.
     * @param precision The number of significant figures to return.
     * @return The modulus of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the divisor is zero.
     * @see BigMathUtility#mod(BigDecimal, BigDecimal, int)
     */
    public static String mod(String n1, String n2, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.mod(new BigDecimal(n1), new BigDecimal(n2), precision).toPlainString();
    }
    
    /**
     * Performs the modulus operation on two numbers represented by strings.
     *
     * @param n1            The dividend.
     * @param n2            The divisor.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The modulus of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the divisor is zero.
     * @see BigMathUtility#mod(BigDecimal, BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String mod(String n1, String n2, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.mod(new BigDecimal(n1), new BigDecimal(n2), precisionMode).toPlainString();
    }
    
    /**
     * Performs the modulus operation on two numbers represented by strings.
     *
     * @param n1 The dividend.
     * @param n2 The divisor.
     * @return The modulus of the two numbers, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the divisor is zero.
     * @see BigMathUtility#mod(BigDecimal, BigDecimal)
     */
    public static String mod(String n1, String n2) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.mod(new BigDecimal(n1), new BigDecimal(n2)).toPlainString();
    }
    
    /**
     * Performs the power operation on two numbers represented by strings.
     *
     * @param n1        The number.
     * @param n2        The power.
     * @param precision The number of significant figures to return.
     * @return The first number raised to the power of the second number, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#power(BigDecimal, BigDecimal, int)
     */
    public static String power(String n1, String n2, int precision) throws NumberFormatException {
        return BigMathUtility.power(new BigDecimal(n1), new BigDecimal(n2), precision).toPlainString();
    }
    
    /**
     * Performs the power operation on two numbers represented by strings.
     *
     * @param n1            The number.
     * @param n2            The power.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The first number raised to the power of the second number, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#power(BigDecimal, BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String power(String n1, String n2, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.power(new BigDecimal(n1), new BigDecimal(n2), precisionMode).toPlainString();
    }
    
    /**
     * Performs the power operation on two numbers represented by strings.
     *
     * @param n1 The number.
     * @param n2 The power.
     * @return The first number raised to the power of the second number, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#power(BigDecimal, BigDecimal)
     */
    public static String power(String n1, String n2) throws NumberFormatException {
        return BigMathUtility.power(new BigDecimal(n1), new BigDecimal(n2)).toPlainString();
    }
    
    /**
     * Performs the root operation on two numbers represented by strings.
     *
     * @param n1        The number.
     * @param n2        The root.
     * @param precision The number of significant figures to return.
     * @return The second number root of the first number, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the first number is less than zero.
     * @see BigMathUtility#root(BigDecimal, BigDecimal, int)
     */
    public static String root(String n1, String n2, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.root(new BigDecimal(n1), new BigDecimal(n2), precision).toPlainString();
    }
    
    /**
     * Performs the root operation on two numbers represented by strings.
     *
     * @param n1            The number.
     * @param n2            The root.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The second number root of the first number, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the first number is less than zero.
     * @see BigMathUtility#root(BigDecimal, BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String root(String n1, String n2, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.root(new BigDecimal(n1), new BigDecimal(n2), precisionMode).toPlainString();
    }
    
    /**
     * Performs the root operation on two numbers represented by strings.
     *
     * @param n1 The number.
     * @param n2 The root.
     * @return The second number root of the first number, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the first number is less than zero.
     * @see BigMathUtility#root(BigDecimal, BigDecimal)
     */
    public static String root(String n1, String n2) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.root(new BigDecimal(n1), new BigDecimal(n2)).toPlainString();
    }
    
    /**
     * Performs the square root operation on a number represented by strings.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The square root of the number, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the number is less than zero.
     * @see BigMathUtility#sqrt(BigDecimal, int)
     */
    public static String sqrt(String n, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.sqrt(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Performs the square root operation on a number represented by strings.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The square root of the number, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the number is less than zero.
     * @see BigMathUtility#sqrt(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String sqrt(String n, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.sqrt(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Performs the square root operation on a number represented by strings.
     *
     * @param n The number.
     * @return The square root of the number, represented as a string.
     * @throws NumberFormatException When a number string does not represent a number.
     * @throws ArithmeticException   When the number is less than zero.
     * @see BigMathUtility#sqrt(BigDecimal)
     */
    public static String sqrt(String n) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.sqrt(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Computes the log of a number represented by a string to a specified base.
     *
     * @param n         The number.
     * @param base      The log base.
     * @param precision The number of significant figures to return.
     * @return The log of the number to the specified base, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero or the base of the log is invalid.
     * @see BigMathUtility#log(BigDecimal, int, int)
     */
    public static String log(String n, int base, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.log(new BigDecimal(n), base, precision).toPlainString();
    }
    
    /**
     * Computes the log of a number represented by a string to a specified base.
     *
     * @param n             The number.
     * @param base          The log base.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The log of the number to the specified base, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero or the base of the log is invalid.
     * @see BigMathUtility#log(BigDecimal, int, BigMathUtility.PrecisionMode)
     */
    public static String log(String n, int base, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.log(new BigDecimal(n), base, precisionMode).toPlainString();
    }
    
    /**
     * Computes the log of a number represented by a string to a specified base.
     *
     * @param n    The number.
     * @param base The log base.
     * @return The log of the number to the specified base, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero or the base of the log is invalid.
     * @see BigMathUtility#log(BigDecimal, int)
     */
    public static String log(String n, int base) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.log(new BigDecimal(n), base).toPlainString();
    }
    
    /**
     * Computes the log base 10 of a number represented by a string.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The log base 10 of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero.
     * @see BigMathUtility#log10(BigDecimal, int)
     */
    public static String log10(String n, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.log10(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Computes the log base 10 of a number represented by a string.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The log base 10 of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero.
     * @see BigMathUtility#log10(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String log10(String n, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.log10(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Computes the log base 10 of a number represented by a string.
     *
     * @param n The number.
     * @return The log base 10 of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero.
     * @see BigMathUtility#log10(BigDecimal, int)
     */
    public static String log10(String n) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.log10(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Computes the log base 2 of a number represented by a string.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The log base 2 of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero.
     * @see BigMathUtility#log2(BigDecimal, int)
     */
    public static String log2(String n, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.log2(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Computes the log base 2 of a number represented by a string.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The log base 2 of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero.
     * @see BigMathUtility#log2(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String log2(String n, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.log2(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Computes the log base 2 of a number represented by a string.
     *
     * @param n The number.
     * @return The log base 2 of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero.
     * @see BigMathUtility#log2(BigDecimal, int)
     */
    public static String log2(String n) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.log2(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Computes the natural log of a number represented by a string.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The natural log of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero.
     * @see BigMathUtility#ln(BigDecimal, int)
     */
    public static String ln(String n, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.ln(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Computes the natural log of a number represented by a string.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The natural log of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero.
     * @see BigMathUtility#ln(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String ln(String n, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.ln(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Computes the natural log of a number represented by a string.
     *
     * @param n The number.
     * @return The natural log of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than or equal to zero.
     * @see BigMathUtility#ln(BigDecimal)
     */
    public static String ln(String n) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.ln(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Computes the exponential function at a number represented by a string.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The value of the exponential function at the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#exp(BigDecimal, int)
     */
    public static String exp(String n, int precision) throws NumberFormatException {
        return BigMathUtility.exp(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Computes the exponential function at a number represented by a string.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The value of the exponential function at the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#exp(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String exp(String n, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.exp(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Computes the exponential function at a number represented by a string.
     *
     * @param n The number.
     * @return The value of the exponential function at the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#exp(BigDecimal)
     */
    public static String exp(String n) throws NumberFormatException {
        return BigMathUtility.exp(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Computes the factorial of a number represented by a string.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The factorial of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than 0.
     * @see BigMathUtility#factorial(BigDecimal, int)
     */
    public static String factorial(String n, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.factorial(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Computes the factorial of a number represented by a string.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The factorial of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than 0.
     * @see BigMathUtility#factorial(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String factorial(String n, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.factorial(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Computes the factorial of a number represented by a string.
     *
     * @param n The number.
     * @return The factorial of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than 0.
     * @see BigMathUtility#factorial(BigDecimal)
     */
    public static String factorial(String n) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.factorial(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Computes the gamma function on a number represented by a string.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The gamma function on the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#gamma(BigDecimal, int)
     */
    public static String gamma(String n, int precision) throws NumberFormatException {
        return BigMathUtility.gamma(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Computes the gamma function on a number represented by a string.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The gamma function on the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#gamma(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String gamma(String n, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.gamma(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Computes the gamma function on a number represented by a string.
     *
     * @param n The number.
     * @return The gamma function on the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#gamma(BigDecimal)
     */
    public static String gamma(String n) throws NumberFormatException {
        return BigMathUtility.gamma(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Computes the reciprocal of a number represented by a string.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The reciprocal of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is zero.
     * @see BigMathUtility#reciprocal(BigDecimal, int)
     */
    public static String reciprocal(String n, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.reciprocal(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Computes the reciprocal of a number represented by a string.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The reciprocal of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is zero.
     * @see BigMathUtility#reciprocal(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String reciprocal(String n, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.reciprocal(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Computes the reciprocal of a number represented by a string.
     *
     * @param n The number.
     * @return The reciprocal of the number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is zero.
     * @see BigMathUtility#reciprocal(BigDecimal)
     */
    public static String reciprocal(String n) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.reciprocal(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Computes the sine of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The sine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#sin(BigDecimal, int)
     */
    public static String sin(String x, int precision) throws NumberFormatException {
        return BigMathUtility.sin(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the sine of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The sine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#sin(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String sin(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.sin(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the sine of an angle.
     *
     * @param x The angle in radians.
     * @return The sine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#sin(BigDecimal)
     */
    public static String sin(String x) throws NumberFormatException {
        return BigMathUtility.sin(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the arc sine of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The arc sine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is not in the domain [-1,1].
     * @see BigMathUtility#asin(BigDecimal, int)
     */
    public static String asin(String x, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.asin(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the arc sine of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The arc sine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is not in the domain [-1,1].
     * @see BigMathUtility#asin(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String asin(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.asin(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the arc sine of a number.
     *
     * @param x The number.
     * @return The arc sine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is not in the domain [-1,1].
     * @see BigMathUtility#asin(BigDecimal)
     */
    public static String asin(String x) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.asin(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the hyperbolic sine of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic sine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#sinh(BigDecimal, int)
     */
    public static String sinh(String x, int precision) throws NumberFormatException {
        return BigMathUtility.sinh(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the hyperbolic sine of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic sine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#sinh(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String sinh(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.sinh(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the hyperbolic sine of an angle.
     *
     * @param x The angle in radians.
     * @return The hyperbolic sine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#sinh(BigDecimal)
     */
    public static String sinh(String x) throws NumberFormatException {
        return BigMathUtility.sinh(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc sine of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic arc sine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#asinh(BigDecimal, int)
     */
    public static String asinh(String x, int precision) throws NumberFormatException {
        return BigMathUtility.asinh(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc sine of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic arc sine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#asinh(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String asinh(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.asinh(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc sine of a number.
     *
     * @param x The number.
     * @return The hyperbolic arc sine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#asinh(BigDecimal)
     */
    public static String asinh(String x) throws NumberFormatException {
        return BigMathUtility.asinh(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the cosine of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The cosine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#cos(BigDecimal, int)
     */
    public static String cos(String x, int precision) throws NumberFormatException {
        return BigMathUtility.cos(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the cosine of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The cosine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#cos(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String cos(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.cos(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the cosine of an angle.
     *
     * @param x The angle in radians.
     * @return The cosine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#cos(BigDecimal)
     */
    public static String cos(String x) throws NumberFormatException {
        return BigMathUtility.cos(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the arc cosine of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The arc cosine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is not in the domain [-1,1].
     * @see BigMathUtility#acos(BigDecimal, int)
     */
    public static String acos(String x, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.acos(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the arc cosine of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The arc cosine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is not in the domain [-1,1].
     * @see BigMathUtility#acos(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String acos(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.acos(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the arc cosine of a number.
     *
     * @param x The number.
     * @return The arc cosine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is not in the domain [-1,1].
     * @see BigMathUtility#acos(BigDecimal)
     */
    public static String acos(String x) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.acos(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the hyperbolic cosine of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic cosine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#cosh(BigDecimal, int)
     */
    public static String cosh(String x, int precision) throws NumberFormatException {
        return BigMathUtility.cosh(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the hyperbolic cosine of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic cosine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#cosh(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String cosh(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.cosh(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the hyperbolic cosine of an angle.
     *
     * @param x The angle in radians.
     * @return The hyperbolic cosine of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#cosh(BigDecimal)
     */
    public static String cosh(String x) throws NumberFormatException {
        return BigMathUtility.cosh(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc cosine of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic arc cosine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than 1.
     * @see BigMathUtility#acosh(BigDecimal, int)
     */
    public static String acosh(String x, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.acosh(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc cosine of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic arc cosine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than 1.
     * @see BigMathUtility#acosh(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String acosh(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.acosh(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc cosine of a number.
     *
     * @param x The number.
     * @return The hyperbolic arc cosine of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is less than 1.
     * @see BigMathUtility#acosh(BigDecimal)
     */
    public static String acosh(String x) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.acosh(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the tangent of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The tangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#tan(BigDecimal, int)
     */
    public static String tan(String x, int precision) throws NumberFormatException {
        return BigMathUtility.tan(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the tangent of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The tangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#tan(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String tan(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.tan(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the tangent of an angle.
     *
     * @param x The angle in radians.
     * @return The tangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#tan(BigDecimal)
     */
    public static String tan(String x) throws NumberFormatException {
        return BigMathUtility.tan(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the arc tangent of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The arc tangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#atan(BigDecimal, int)
     */
    public static String atan(String x, int precision) throws NumberFormatException {
        return BigMathUtility.atan(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the arc tangent of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The arc tangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#atan(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String atan(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.atan(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the arc tangent of a number.
     *
     * @param x The number.
     * @return The arc tangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#atan(BigDecimal)
     */
    public static String atan(String x) throws NumberFormatException {
        return BigMathUtility.atan(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the arc tangent of a coordinate while preserving quadrant information.
     *
     * @param y         The y coordinate
     * @param x         The x coordinate.
     * @param precision The number of significant figures to return.
     * @return The arc tangent of the coordinate, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When x is 0 and y is 0.
     * @see BigMathUtility#atan2(BigDecimal, BigDecimal, int)
     */
    public static String atan2(String y, String x, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.atan2(new BigDecimal(y), new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the arc tangent of a coordinate while preserving quadrant information.
     *
     * @param y             The y coordinate
     * @param x             The x coordinate.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The arc tangent of the coordinate, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When x is 0 and y is 0.
     * @see BigMathUtility#atan2(BigDecimal, BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String atan2(String y, String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.atan2(new BigDecimal(y), new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the arc tangent of a coordinate while preserving quadrant information.
     *
     * @param y The y coordinate
     * @param x The x coordinate.
     * @return The arc tangent of the coordinate, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When x is 0 and y is 0.
     * @see BigMathUtility#atan2(BigDecimal, BigDecimal)
     */
    public static String atan2(String y, String x) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.atan2(new BigDecimal(y), new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the hyperbolic tangent of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic tangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#tanh(BigDecimal, int)
     */
    public static String tanh(String x, int precision) throws NumberFormatException {
        return BigMathUtility.tanh(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the hyperbolic tangent of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic tangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#tanh(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String tanh(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.tanh(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the hyperbolic tangent of an angle.
     *
     * @param x The angle in radians.
     * @return The hyperbolic tangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#tanh(BigDecimal)
     */
    public static String tanh(String x) throws NumberFormatException {
        return BigMathUtility.tanh(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc tangent of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic arc tangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is not in the domain (-1,1).
     * @see BigMathUtility#atanh(BigDecimal, int)
     */
    public static String atanh(String x, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.atanh(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc tangent of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic arc tangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is not in the domain (-1,1).
     * @see BigMathUtility#atanh(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String atanh(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.atanh(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc tangent of a number.
     *
     * @param x The number.
     * @return The hyperbolic arc tangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the number is not in the domain (-1,1).
     * @see BigMathUtility#atanh(BigDecimal)
     */
    public static String atanh(String x) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.atanh(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the cotangent of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The cotangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the angle is 0.
     * @see BigMathUtility#cot(BigDecimal, int)
     */
    public static String cot(String x, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.cot(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the cotangent of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The cotangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the angle is 0.
     * @see BigMathUtility#cot(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String cot(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.cot(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the cotangent of an angle.
     *
     * @param x The angle in radians.
     * @return The cotangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the angle is 0.
     * @see BigMathUtility#cot(BigDecimal)
     */
    public static String cot(String x) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.cot(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the arc cotangent of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The arc cotangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#acot(BigDecimal, int)
     */
    public static String acot(String x, int precision) throws NumberFormatException {
        return BigMathUtility.acot(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the arc cotangent of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The arc cotangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#acot(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String acot(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.acot(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the arc cotangent of a number.
     *
     * @param x The number.
     * @return The arc cotangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#acot(BigDecimal)
     */
    public static String acot(String x) throws NumberFormatException {
        return BigMathUtility.acot(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the hyperbolic cotangent of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic cotangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the angle is 0.
     * @see BigMathUtility#coth(BigDecimal, int)
     */
    public static String coth(String x, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.coth(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the hyperbolic cotangent of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic cotangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the angle is 0.
     * @see BigMathUtility#coth(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String coth(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.coth(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the hyperbolic cotangent of an angle.
     *
     * @param x The angle in radians.
     * @return The hyperbolic cotangent of the angle.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the angle is 0.
     * @see BigMathUtility#coth(BigDecimal)
     */
    public static String coth(String x) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.coth(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc cotangent of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic arc cotangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the angle is in the range [-1,1].
     * @see BigMathUtility#acoth(BigDecimal, int)
     */
    public static String acoth(String x, int precision) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.acoth(new BigDecimal(x), precision).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc cotangent of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic arc cotangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the angle is in the range [-1,1].
     * @see BigMathUtility#acoth(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String acoth(String x, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.acoth(new BigDecimal(x), precisionMode).toPlainString();
    }
    
    /**
     * Computes the hyperbolic arc cotangent of a number.
     *
     * @param x The number.
     * @return The hyperbolic arc cotangent of the number, in radians.
     * @throws NumberFormatException When the number string does not represent a number.
     * @throws ArithmeticException   When the angle is in the range [-1,1].
     * @see BigMathUtility#acoth(BigDecimal)
     */
    public static String acoth(String x) throws NumberFormatException, ArithmeticException {
        return BigMathUtility.acoth(new BigDecimal(x)).toPlainString();
    }
    
    /**
     * Computes pi.
     *
     * @param precision The number of significant figures to return.
     * @return Pi, represented as a string.
     * @see BigMathUtility#pi(int)
     */
    public static String pi(int precision) {
        return BigMathUtility.pi(precision).toPlainString();
    }
    
    /**
     * Computes pi.
     *
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return Pi, represented as a string.
     * @see BigMathUtility#pi(BigMathUtility.PrecisionMode)
     */
    public static String pi(BigMathUtility.PrecisionMode precisionMode) {
        return BigMathUtility.pi(precisionMode).toPlainString();
    }
    
    /**
     * Computes pi.
     *
     * @return Pi, represented as a string.
     * @see BigMathUtility#pi()
     */
    public static String pi() {
        return BigMathUtility.pi().toPlainString();
    }
    
    /**
     * Computes e.
     *
     * @param precision The number of significant figures to return.
     * @return E, represented as a string.
     * @see BigMathUtility#e(int)
     */
    public static String e(int precision) {
        return BigMathUtility.e(precision).toPlainString();
    }
    
    /**
     * Computes e.
     *
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return E, represented as a string.
     * @see BigMathUtility#e(BigMathUtility.PrecisionMode)
     */
    public static String e(BigMathUtility.PrecisionMode precisionMode) {
        return BigMathUtility.e(precisionMode).toPlainString();
    }
    
    /**
     * Computes e.
     *
     * @return E, represented as a string.
     * @see BigMathUtility#e()
     */
    public static String e() {
        return BigMathUtility.e().toPlainString();
    }
    
    /**
     * Computes the nth bernoulli number.
     *
     * @param n         The index of the bernoulli number.
     * @param precision The number of significant figures to return.
     * @return The nth bernoulli number, represented as a string.
     * @throws ArithmeticException When the index is less than zero.
     * @see BigMathUtility#bernoulli(int, int)
     */
    public static String bernoulli(int n, int precision) throws ArithmeticException {
        return BigMathUtility.bernoulli(n, precision).toPlainString();
    }
    
    /**
     * Computes the nth bernoulli number.
     *
     * @param n             The index of the bernoulli number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The nth bernoulli number, represented as a string.
     * @throws ArithmeticException When the index is less than zero.
     * @see BigMathUtility#bernoulli(int, BigMathUtility.PrecisionMode)
     */
    public static String bernoulli(int n, BigMathUtility.PrecisionMode precisionMode) throws ArithmeticException {
        return BigMathUtility.bernoulli(n, precisionMode).toPlainString();
    }
    
    /**
     * Computes the nth bernoulli number.
     *
     * @param n The index of the bernoulli number.
     * @return The nth bernoulli number, represented as a string.
     * @throws ArithmeticException When the index is less than zero.
     * @see BigMathUtility#bernoulli(int)
     */
    public static String bernoulli(int n) throws ArithmeticException {
        return BigMathUtility.bernoulli(n).toPlainString();
    }
    
    /**
     * Returns the integral part of a number represented by a string.
     *
     * @param n The number.
     * @return The integral part of the number, represented as a string.
     * @see BigMathUtility#integralPart(BigDecimal)
     */
    public static String integralPart(String n) {
        return BigMathUtility.integralPart(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Returns the fractional part of a number represented by a string.
     *
     * @param n The number.
     * @return The fractional part of the number, represented as a string.
     * @see BigMathUtility#fractionalPart(BigDecimal)
     */
    public static String fractionalPart(String n) {
        return BigMathUtility.fractionalPart(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Returns the significant digits of a number represented by a string.
     *
     * @param n The number.
     * @return The significant digits of the number.
     * @see BigMathUtility#significantDigits(BigDecimal)
     */
    public static int significantDigits(String n) {
        return BigMathUtility.significantDigits(new BigDecimal(n));
    }
    
    /**
     * Returns the mantissa of a number represented by a string.
     *
     * @param n The number.
     * @return The mantissa of the number, represented as a string.
     * @see BigMathUtility#mantissa(BigDecimal)
     */
    public static String mantissa(String n) {
        return BigMathUtility.mantissa(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Returns the exponent of a number represented by a string.
     *
     * @param n The number.
     * @return The exponent of the number.
     * @see BigMathUtility#exponent(BigDecimal)
     */
    public static int exponent(String n) {
        return BigMathUtility.exponent(new BigDecimal(n));
    }
    
    /**
     * Rounds a number represented by a string.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The rounded number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#round(BigDecimal, int)
     */
    public static String round(String n, int precision) throws NumberFormatException {
        return BigMathUtility.round(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Rounds a number represented by a string.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The rounded number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#round(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String round(String n, BigMathUtility.PrecisionMode precisionMode) throws NumberFormatException {
        return BigMathUtility.round(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Rounds a number represented by a string.
     *
     * @param n The number.
     * @return The rounded number, represented as a string.
     * @throws NumberFormatException When the number string does not represent a number.
     * @see BigMathUtility#round(BigDecimal)
     */
    public static String round(String n) throws NumberFormatException {
        return BigMathUtility.round(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Returns the absolute value of a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The absolute value of the number.
     * @see BigMathUtility#abs(BigDecimal, int)
     */
    public static String abs(String n, int precision) {
        return BigMathUtility.abs(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Returns the absolute value of a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The absolute value of the number.
     * @see BigMathUtility#abs(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String abs(String n, BigMathUtility.PrecisionMode precisionMode) {
        return BigMathUtility.abs(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Returns the absolute value of a number.
     *
     * @param n The number.
     * @return The absolute value of the number.
     * @see BigMathUtility#abs(BigDecimal)
     */
    public static String abs(String n) {
        return BigMathUtility.abs(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Returns the negated value of a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The negated value of the number.
     * @see BigMathUtility#negate(BigDecimal, int)
     */
    public static String negate(String n, int precision) {
        return BigMathUtility.negate(new BigDecimal(n), precision).toPlainString();
    }
    
    /**
     * Returns the negated value of a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The negated value of the number.
     * @see BigMathUtility#negate(BigDecimal, BigMathUtility.PrecisionMode)
     */
    public static String negate(String n, BigMathUtility.PrecisionMode precisionMode) {
        return BigMathUtility.negate(new BigDecimal(n), precisionMode).toPlainString();
    }
    
    /**
     * Returns the negated value of a number.
     *
     * @param n The number.
     * @return The negated value of the number.
     * @see BigMathUtility#negate(BigDecimal)
     */
    public static String negate(String n) {
        return BigMathUtility.negate(new BigDecimal(n)).toPlainString();
    }
    
    /**
     * Determines if a number represented by a string is greater than another number represented by a string.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return Whether the first number is greater than the second number or not.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#greaterThan(BigDecimal, BigDecimal)
     */
    public static boolean greaterThan(String n1, String n2) throws NumberFormatException {
        return BigMathUtility.greaterThan(new BigDecimal(n1), new BigDecimal(n2));
    }
    
    /**
     * Determines if a number represented by a string is less than another number represented by a string.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return Whether the first number is less than the second number or not.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#lessThan(BigDecimal, BigDecimal)
     */
    public static boolean lessThan(String n1, String n2) throws NumberFormatException {
        return BigMathUtility.lessThan(new BigDecimal(n1), new BigDecimal(n2));
    }
    
    /**
     * Determines if a number represented by a string is equal to another number represented by a string.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return Whether the first number is equal to the second number or not.
     * @throws NumberFormatException When a number string does not represent a number.
     * @see BigMathUtility#equalTo(BigDecimal, BigDecimal)
     */
    public static boolean equalTo(String n1, String n2) throws NumberFormatException {
        return BigMathUtility.equalTo(new BigDecimal(n1), new BigDecimal(n2));
    }
    
}
