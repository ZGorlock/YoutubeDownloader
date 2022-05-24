/*
 * File:    BigMathUtility.java
 * Package: commons.math
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import ch.obermuhlner.math.big.BigDecimalMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional big math functionality.
 */
public final class BigMathUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(BigMathUtility.class);
    
    
    //Constants
    
    /**
     * The default precision to use for math operations.
     */
    public static final int DEFAULT_MATH_PRECISION = PrecisionMode.HIGH_PRECISION.getPrecision();
    
    /**
     * The default rounding mode to use for math operations.
     */
    public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * A Big Decimal 1.
     */
    public static final BigDecimal ONE = BigDecimal.ONE;
    
    /**
     * A Big Decimal 0.
     */
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    
    /**
     * A Big Decimal -1.
     */
    public static final BigDecimal NEGATIVE_ONE = BigDecimal.ONE.negate();
    
    /**
     * A Big Decimal 10.
     */
    public static final BigDecimal TEN = BigDecimal.valueOf(10);
    
    /**
     * A Big Decimal 100.
     */
    public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    
    /**
     * A Big Decimal 1000.
     */
    public static final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000);
    
    
    //Enums
    
    /**
     * An enumeration of math operation Precision Modes.
     */
    public enum PrecisionMode {
        
        //Values
        
        DEFAULT_PRECISION(-1),
        NO_PRECISION(0),
        LOW_PRECISION(8),
        MID_PRECISION(16),
        HIGH_PRECISION(64),
        MAX_PRECISION(512),
        MATH_PRECISION(1024);
        
        
        //Fields
        
        /**
         * The number of decimal places for the Precision Mode.
         */
        private final int precision;
        
        
        //Constructors
        
        /**
         * Constructs a Precision Mode.
         *
         * @param precision The number of decimal places for the Precision Mode.
         */
        PrecisionMode(int precision) {
            this.precision = precision;
        }
        
        
        //Getters
        
        /**
         * Returns the number of decimal places for the Precision Mode.
         *
         * @return The number of decimal places for the Precision Mode.
         */
        public int getPrecision() {
            return precision;
        }
        
    }
    
    
    //Static Methods
    
    /**
     * Adds two numbers.
     *
     * @param n1        The first number.
     * @param n2        The second number.
     * @param precision The number of significant figures to return.
     * @return The sum of the two numbers.
     */
    public static BigDecimal add(BigDecimal n1, BigDecimal n2, int precision) {
        BigDecimal result = n1.add(n2);
        if (precision != PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            result = result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
        return result;
    }
    
    /**
     * Adds two numbers.
     *
     * @param n1            The first number.
     * @param n2            The second number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The sum of the two numbers.
     * @see #add(BigDecimal, BigDecimal, int)
     */
    public static BigDecimal add(BigDecimal n1, BigDecimal n2, PrecisionMode precisionMode) {
        return add(n1, n2, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Adds two numbers.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return The sum of the two numbers.
     * @see #add(BigDecimal, BigDecimal, PrecisionMode)
     */
    public static BigDecimal add(BigDecimal n1, BigDecimal n2) {
        return add(n1, n2, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Subtracts two numbers.
     *
     * @param n1        The first number.
     * @param n2        The second number.
     * @param precision The number of significant figures to return.
     * @return The difference of the two numbers.
     */
    public static BigDecimal subtract(BigDecimal n1, BigDecimal n2, int precision) {
        BigDecimal result = n1.subtract(n2);
        if (precision != PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            result = result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
        return result;
    }
    
    /**
     * Subtracts two numbers.
     *
     * @param n1            The first number.
     * @param n2            The second number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The difference of the two numbers.
     * @see #subtract(BigDecimal, BigDecimal, int)
     */
    public static BigDecimal subtract(BigDecimal n1, BigDecimal n2, PrecisionMode precisionMode) {
        return subtract(n1, n2, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Subtracts two numbers.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return The difference of the two numbers.
     * @see #subtract(BigDecimal, BigDecimal, PrecisionMode)
     */
    public static BigDecimal subtract(BigDecimal n1, BigDecimal n2) {
        return subtract(n1, n2, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Multiplies two numbers.
     *
     * @param n1        The first number.
     * @param n2        The second number.
     * @param precision The number of significant figures to return.
     * @return The product of the two numbers.
     */
    public static BigDecimal multiply(BigDecimal n1, BigDecimal n2, int precision) {
        BigDecimal br = n1.multiply(n2);
        if (precision != PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            br = br.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
        return br;
    }
    
    /**
     * Multiplies two numbers.
     *
     * @param n1            The first number.
     * @param n2            The second number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The product of the two numbers.
     * @see #multiply(BigDecimal, BigDecimal, int)
     */
    public static BigDecimal multiply(BigDecimal n1, BigDecimal n2, PrecisionMode precisionMode) {
        return multiply(n1, n2, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Multiplies two numbers.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return The product of the two numbers.
     * @see #multiply(BigDecimal, BigDecimal, PrecisionMode)
     */
    public static BigDecimal multiply(BigDecimal n1, BigDecimal n2) {
        return multiply(n1, n2, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Divides two numbers.
     *
     * @param n1        The dividend.
     * @param n2        The divisor.
     * @param precision The number of significant figures to return.
     * @return The quotient of the two numbers.
     * @throws ArithmeticException When the divisor is zero.
     */
    public static BigDecimal divide(BigDecimal n1, BigDecimal n2, int precision) throws ArithmeticException {
        if (n2.compareTo(ZERO) == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return n1.divide(n2, DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return n1.divide(n2, precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Divides two numbers.
     *
     * @param n1            The dividend.
     * @param n2            The divisor.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The quotient of the two numbers.
     * @throws ArithmeticException When the divisor is zero.
     * @see #divide(BigDecimal, BigDecimal, int)
     */
    public static BigDecimal divide(BigDecimal n1, BigDecimal n2, PrecisionMode precisionMode) throws ArithmeticException {
        return divide(n1, n2, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Divides two numbers.
     *
     * @param n1 The dividend.
     * @param n2 The divisor.
     * @return The quotient of the two numbers.
     * @throws ArithmeticException When the divisor is zero.
     * @see #divide(BigDecimal, BigDecimal, PrecisionMode)
     */
    public static BigDecimal divide(BigDecimal n1, BigDecimal n2) throws ArithmeticException {
        return divide(n1, n2, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Performs the modulus operation on two numbers.
     *
     * @param n1        The dividend.
     * @param n2        The divisor.
     * @param precision The number of significant figures to return.
     * @return The modulus of the two numbers.
     * @throws ArithmeticException When the divisor is zero.
     */
    public static BigDecimal mod(BigDecimal n1, BigDecimal n2, int precision) throws ArithmeticException {
        if (n2.compareTo(ZERO) == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        
        BigDecimal result = n1.remainder(n2);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Performs the modulus operation on two numbers.
     *
     * @param n1            The dividend.
     * @param n2            The divisor.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The modulus of the two numbers.
     * @throws ArithmeticException When the divisor is zero.
     * @see #mod(BigDecimal, BigDecimal, int)
     */
    public static BigDecimal mod(BigDecimal n1, BigDecimal n2, PrecisionMode precisionMode) throws ArithmeticException {
        return mod(n1, n2, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Performs the modulus operation on two numbers.
     *
     * @param n1 The dividend.
     * @param n2 The divisor.
     * @return The modulus of the two numbers.
     * @throws ArithmeticException When the divisor is zero.
     * @see #divide(BigDecimal, BigDecimal, PrecisionMode)
     */
    public static BigDecimal mod(BigDecimal n1, BigDecimal n2) throws ArithmeticException {
        return mod(n1, n2, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Performs the power operation on two numbers.
     *
     * @param n1        The number.
     * @param n2        The power.
     * @param precision The number of significant figures to return.
     * @return The first number raised to the power of the second number.
     * @see BigDecimalMath#pow(BigDecimal, BigDecimal, MathContext)
     */
    public static BigDecimal power(BigDecimal n1, BigDecimal n2, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.pow(n1, n2, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Performs the power operation on two numbers.
     *
     * @param n1            The number.
     * @param n2            The power.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The first number raised to the power of the second number.
     * @see #power(BigDecimal, BigDecimal, int)
     */
    public static BigDecimal power(BigDecimal n1, BigDecimal n2, PrecisionMode precisionMode) {
        return power(n1, n2, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Performs the power operation on two numbers.
     *
     * @param n1 The number.
     * @param n2 The power.
     * @return The first number raised to the power of the second number.
     * @see #power(BigDecimal, BigDecimal, PrecisionMode)
     */
    public static BigDecimal power(BigDecimal n1, BigDecimal n2) {
        return power(n1, n2, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Performs the root operation on two numbers.
     *
     * @param n1        The number.
     * @param n2        The root.
     * @param precision The number of significant figures to return.
     * @return The second number root of the first number.
     * @throws ArithmeticException When the first number is less than zero.
     * @see BigDecimalMath#root(BigDecimal, BigDecimal, MathContext)
     */
    public static BigDecimal root(BigDecimal n1, BigDecimal n2, int precision) throws ArithmeticException {
        MathContext context = new MathContext(PrecisionMode.MATH_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.root(n1, n2, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Performs the root operation on two numbers.
     *
     * @param n1            The number.
     * @param n2            The root.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The second number root of the first number.
     * @throws ArithmeticException When the first number is less than zero.
     * @see #root(BigDecimal, BigDecimal, int)
     */
    public static BigDecimal root(BigDecimal n1, BigDecimal n2, PrecisionMode precisionMode) throws ArithmeticException {
        return root(n1, n2, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Performs the root operation on two numbers.
     *
     * @param n1 The number.
     * @param n2 The root.
     * @return The second number root of the first number.
     * @throws ArithmeticException When the first number is less than zero.
     * @see #root(BigDecimal, BigDecimal, PrecisionMode)
     */
    public static BigDecimal root(BigDecimal n1, BigDecimal n2) throws ArithmeticException {
        return root(n1, n2, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Performs the square root operation on a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The square root of the number.
     * @throws ArithmeticException When the number is less than zero.
     * @see BigDecimalMath#sqrt(BigDecimal, MathContext)
     */
    public static BigDecimal sqrt(BigDecimal n, int precision) throws ArithmeticException {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.sqrt(n, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Performs the square root operation on a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The square root of the number.
     * @throws ArithmeticException When the number is less than zero.
     * @see #sqrt(BigDecimal, int)
     */
    public static BigDecimal sqrt(BigDecimal n, PrecisionMode precisionMode) throws ArithmeticException {
        return sqrt(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Performs the square root operation on a number.
     *
     * @param n The number.
     * @return The square root of the number.
     * @throws ArithmeticException When the number is less than zero.
     * @see #sqrt(BigDecimal, PrecisionMode)
     */
    public static BigDecimal sqrt(BigDecimal n) throws ArithmeticException {
        return sqrt(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the log of a number to a specified base.
     *
     * @param n         The number.
     * @param base      The log base.
     * @param precision The number of significant figures to return.
     * @return The log of the number to the specified base.
     * @throws ArithmeticException When the number is less than or equal to zero or the base of the log is invalid.
     * @see BigDecimalMath#log(BigDecimal, MathContext)
     */
    public static BigDecimal log(BigDecimal n, int base, int precision) throws ArithmeticException {
        if (base < 2) {
            throw new ArithmeticException("Cannot take a log with base: " + base);
        }
        if (base == 2) {
            return log2(n, precision);
        }
        if (base == 10) {
            return log10(n, precision);
        }
        
        if (n.signum() <= 0) {
            throw new ArithmeticException("Cannot handle imaginary numbers");
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal numerator = BigDecimalMath.log(n, context);
        BigDecimal denominator = BigDecimalMath.log(new BigDecimal(base), context);
        return divide(numerator, denominator, precision);
    }
    
    /**
     * Computes the log of a number to a specified base.
     *
     * @param n             The number.
     * @param base          The log base.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The log of the number to the specified base.
     * @throws ArithmeticException When the number is less than or equal to zero or the base of the log is invalid.
     * @see #log(BigDecimal, int, int)
     */
    public static BigDecimal log(BigDecimal n, int base, PrecisionMode precisionMode) throws ArithmeticException {
        return log(n, base, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the log of a number to a specified base.
     *
     * @param n    The number.
     * @param base The log base.
     * @return The log of the number to the specified base.
     * @throws ArithmeticException When the number is less than or equal to zero or the base of the log is invalid.
     * @see #log(BigDecimal, int, PrecisionMode)
     */
    public static BigDecimal log(BigDecimal n, int base) throws ArithmeticException {
        return log(n, base, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the log base 10 of a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The log base 10 of the number.
     * @throws ArithmeticException When the number is less than or equal to zero.
     * @see BigDecimalMath#log10(BigDecimal, MathContext)
     */
    public static BigDecimal log10(BigDecimal n, int precision) throws ArithmeticException {
        if (n.signum() <= 0) {
            throw new ArithmeticException("Cannot handle imaginary numbers");
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.log10(n, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the log base 10 of a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The log base 10 of the number.
     * @throws ArithmeticException When the number is less than or equal to zero.
     * @see #log10(BigDecimal, int)
     */
    public static BigDecimal log10(BigDecimal n, PrecisionMode precisionMode) throws ArithmeticException {
        return log10(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the log base 10 of a number.
     *
     * @param n The number.
     * @return The log base 10 of the number.
     * @throws ArithmeticException When the number is less than or equal to zero.
     * @see #log10(BigDecimal, PrecisionMode)
     */
    public static BigDecimal log10(BigDecimal n) throws ArithmeticException {
        return log10(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the log base 2 of a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The log base 2 of the number.
     * @throws ArithmeticException When the number is less than or equal to zero.
     * @see BigDecimalMath#log2(BigDecimal, MathContext)
     */
    public static BigDecimal log2(BigDecimal n, int precision) throws ArithmeticException {
        if (n.signum() <= 0) {
            throw new ArithmeticException("Cannot handle imaginary numbers");
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.log2(n, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the log base 2 of a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The log base 2 of the number.
     * @throws ArithmeticException When the number is less than or equal to zero.
     * @see #log2(BigDecimal, int)
     */
    public static BigDecimal log2(BigDecimal n, PrecisionMode precisionMode) throws ArithmeticException {
        return log2(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the log base 2 of a number.
     *
     * @param n The number.
     * @return The log base 2 of the number.
     * @throws ArithmeticException When the number is less than or equal to zero.
     * @see #log2(BigDecimal, PrecisionMode)
     */
    public static BigDecimal log2(BigDecimal n) throws ArithmeticException {
        return log2(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the natural log of a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The natural log of the number.
     * @throws ArithmeticException When the number is less than or equal to zero.
     * @see BigDecimalMath#log(BigDecimal, MathContext)
     * @see BigDecimalMath#e(MathContext)
     */
    public static BigDecimal ln(BigDecimal n, int precision) throws ArithmeticException {
        if (n.signum() <= 0) {
            throw new ArithmeticException("Cannot handle imaginary numbers");
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal numerator = BigDecimalMath.log(n, context);
        BigDecimal denominator = BigDecimalMath.log(BigDecimalMath.e(context), context);
        return divide(numerator, denominator, precision);
    }
    
    /**
     * Computes the natural log of a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The natural log of the number.
     * @throws ArithmeticException When the number is less than or equal to zero.
     * @see #ln(BigDecimal, int)
     */
    public static BigDecimal ln(BigDecimal n, PrecisionMode precisionMode) throws ArithmeticException {
        return ln(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the natural log of a number.
     *
     * @param n The number.
     * @return The natural log of the number.
     * @throws ArithmeticException When the number is less than or equal to zero.
     * @see #ln(BigDecimal, PrecisionMode)
     */
    public static BigDecimal ln(BigDecimal n) throws ArithmeticException {
        return ln(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the exponential function at a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The value of the exponential function at the number.
     * @see BigDecimalMath#exp(BigDecimal, MathContext)
     */
    public static BigDecimal exp(BigDecimal n, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.exp(n, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the exponential function at a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The value of the exponential function at the number.
     * @see #exp(BigDecimal, int)
     */
    public static BigDecimal exp(BigDecimal n, PrecisionMode precisionMode) {
        return exp(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the exponential function at a number.
     *
     * @param n The number.
     * @return The value of the exponential function at the number.
     * @see #exp(BigDecimal, PrecisionMode)
     */
    public static BigDecimal exp(BigDecimal n) {
        return exp(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the factorial of a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The factorial of the number.
     * @throws ArithmeticException When the number is less than 0 or is not an integer.
     * @see BigDecimalMath#factorial(BigDecimal, MathContext)
     */
    public static BigDecimal factorial(BigDecimal n, int precision) throws ArithmeticException {
        if (n.signum() < 0) {
            throw new ArithmeticException("Cannot take the factorial of a number less than 0");
        }
        if (BigDecimalMath.fractionalPart(n).signum() != 0) {
            throw new ArithmeticException("Cannot take the factorial of a number that is not an integer");
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.factorial(n, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the factorial of a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The factorial of the number.
     * @throws ArithmeticException When the number is less than 0.
     * @see #factorial(BigDecimal, int)
     */
    public static BigDecimal factorial(BigDecimal n, PrecisionMode precisionMode) throws ArithmeticException {
        return factorial(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the factorial of a number.
     *
     * @param n The number.
     * @return The factorial of the number.
     * @throws ArithmeticException When the number is less than 0.
     * @see #factorial(BigDecimal, PrecisionMode)
     */
    public static BigDecimal factorial(BigDecimal n) throws ArithmeticException {
        return factorial(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the gamma function on a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The gamma function on the number.
     * @see BigDecimalMath#gamma(BigDecimal, MathContext)
     */
    public static BigDecimal gamma(BigDecimal n, int precision) {
        if (n.signum() <= 0) {
            throw new ArithmeticException("Cannot take the gamma of a number less than or equal to 0");
        }
        if (BigDecimalMath.fractionalPart(n).signum() != 0) {
            throw new ArithmeticException("Cannot take the gamma of a number that is not an integer");
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.gamma(n, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the gamma function on a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The gamma function on the number.
     * @see #gamma(BigDecimal, int)
     */
    public static BigDecimal gamma(BigDecimal n, PrecisionMode precisionMode) {
        return gamma(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the gamma function on a number.
     *
     * @param n The number.
     * @return The exp function on the number.
     * @see #gamma(BigDecimal, PrecisionMode)
     */
    public static BigDecimal gamma(BigDecimal n) {
        return gamma(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the reciprocal of a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The reciprocal of the number.
     * @throws ArithmeticException When the number is zero.
     * @see BigDecimalMath#reciprocal(BigDecimal, MathContext)
     */
    public static BigDecimal reciprocal(BigDecimal n, int precision) {
        if (n.compareTo(ZERO) == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.reciprocal(n, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the reciprocal of a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The reciprocal of the number.
     * @throws ArithmeticException When the number is zero.
     * @see #reciprocal(BigDecimal, int)
     */
    public static BigDecimal reciprocal(BigDecimal n, PrecisionMode precisionMode) {
        return reciprocal(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the reciprocal of a number.
     *
     * @param n The number.
     * @return The reciprocal of the number.
     * @throws ArithmeticException When the number is zero.
     * @see #reciprocal(BigDecimal, PrecisionMode)
     */
    public static BigDecimal reciprocal(BigDecimal n) {
        return reciprocal(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the sine of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The sine of the angle.
     * @see BigDecimalMath#sin(BigDecimal, MathContext)
     */
    public static BigDecimal sin(BigDecimal x, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.sin(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the sine of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The sine of the angle.
     * @see #sin(BigDecimal, int)
     */
    public static BigDecimal sin(BigDecimal x, PrecisionMode precisionMode) {
        return sin(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the sine of an angle.
     *
     * @param x The angle in radians.
     * @return The sine of the angle.
     * @see #sin(BigDecimal, PrecisionMode)
     */
    public static BigDecimal sin(BigDecimal x) {
        return sin(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the arc sine of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The arc sine of the number, in radians.
     * @throws ArithmeticException When the number is not in the domain [-1,1].
     * @see BigDecimalMath#asin(BigDecimal, MathContext)
     */
    public static BigDecimal asin(BigDecimal x, int precision) throws ArithmeticException {
        if ((x.compareTo(NEGATIVE_ONE) < 0) || (x.compareTo(ONE) > 0)) {
            throw new ArithmeticException("Illegal input for asin where input < -1 or input > 1; input = " + x.toPlainString());
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.asin(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the arc sine of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The arc sine of the number, in radians.
     * @throws ArithmeticException When the number is not in the domain [-1,1].
     * @see #asin(BigDecimal, int)
     */
    public static BigDecimal asin(BigDecimal x, PrecisionMode precisionMode) throws ArithmeticException {
        return asin(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the arc sine of a number.
     *
     * @param x The number.
     * @return The arc sine of the number, in radians.
     * @throws ArithmeticException When the number is not in the domain [-1,1].
     * @see #asin(BigDecimal, PrecisionMode)
     */
    public static BigDecimal asin(BigDecimal x) throws ArithmeticException {
        return asin(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the hyperbolic sine of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic sine of the angle.
     * @see BigDecimalMath#sinh(BigDecimal, MathContext)
     */
    public static BigDecimal sinh(BigDecimal x, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.sinh(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the hyperbolic sine of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic sine of the angle.
     * @see #sinh(BigDecimal, int)
     */
    public static BigDecimal sinh(BigDecimal x, PrecisionMode precisionMode) {
        return sinh(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the hyperbolic sine of an angle.
     *
     * @param x The angle in radians.
     * @return The hyperbolic sine of the angle.
     * @see #sinh(BigDecimal, PrecisionMode)
     */
    public static BigDecimal sinh(BigDecimal x) {
        return sinh(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the hyperbolic arc sine of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic arc sine of the number, in radians.
     * @see BigDecimalMath#asinh(BigDecimal, MathContext)
     */
    public static BigDecimal asinh(BigDecimal x, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.asinh(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the hyperbolic arc sine of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic arc sine of the number, in radians.
     * @see #asinh(BigDecimal, int)
     */
    public static BigDecimal asinh(BigDecimal x, PrecisionMode precisionMode) {
        return asinh(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the hyperbolic arc sine of a number.
     *
     * @param x The number.
     * @return The hyperbolic arc sine of the number, in radians.
     * @see #asinh(BigDecimal, PrecisionMode)
     */
    public static BigDecimal asinh(BigDecimal x) {
        return asinh(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the cosine of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The cosine of the angle.
     * @see BigDecimalMath#cos(BigDecimal, MathContext)
     */
    public static BigDecimal cos(BigDecimal x, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.cos(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the cosine of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The cosine of the angle.
     * @see #cos(BigDecimal, int)
     */
    public static BigDecimal cos(BigDecimal x, PrecisionMode precisionMode) {
        return cos(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the cosine of an angle.
     *
     * @param x The angle in radians.
     * @return The cosine of the angle.
     * @see #cos(BigDecimal, PrecisionMode)
     */
    public static BigDecimal cos(BigDecimal x) {
        return cos(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the arc cosine of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The arc cosine of the number, in radians.
     * @throws ArithmeticException When the number is not in the domain [-1,1].
     * @see BigDecimalMath#acos(BigDecimal, MathContext)
     */
    public static BigDecimal acos(BigDecimal x, int precision) throws ArithmeticException {
        if ((x.compareTo(NEGATIVE_ONE) < 0) || (x.compareTo(ONE) > 0)) {
            throw new ArithmeticException("Illegal input for acos where input < -1 or input > 1; input = " + x.toPlainString());
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.acos(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the arc cosine of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The arc cosine of the number, in radians.
     * @throws ArithmeticException When the number is not in the domain [-1,1].
     * @see #acos(BigDecimal, int)
     */
    public static BigDecimal acos(BigDecimal x, PrecisionMode precisionMode) throws ArithmeticException {
        return acos(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the arc cosine of a number.
     *
     * @param x The number.
     * @return The arc cosine of the number, in radians.
     * @throws ArithmeticException When the number is not in the domain [-1,1].
     * @see #acos(BigDecimal, PrecisionMode)
     */
    public static BigDecimal acos(BigDecimal x) throws ArithmeticException {
        return acos(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the hyperbolic cosine of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic cosine of the angle.
     * @see BigDecimalMath#cosh(BigDecimal, MathContext)
     */
    public static BigDecimal cosh(BigDecimal x, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.cosh(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the hyperbolic cosine of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic cosine of the angle.
     * @see #cosh(BigDecimal, int)
     */
    public static BigDecimal cosh(BigDecimal x, PrecisionMode precisionMode) {
        return cosh(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the hyperbolic cosine of an angle.
     *
     * @param x The angle in radians.
     * @return The hyperbolic cosine of the angle.
     * @see #cosh(BigDecimal, PrecisionMode)
     */
    public static BigDecimal cosh(BigDecimal x) {
        return cosh(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the hyperbolic arc cosine of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic arc cosine of the number, in radians.
     * @throws ArithmeticException When the number is less than 1.
     * @see BigDecimalMath#acosh(BigDecimal, MathContext)
     */
    public static BigDecimal acosh(BigDecimal x, int precision) throws ArithmeticException {
        if (x.compareTo(ONE) < 0) {
            throw new ArithmeticException("Illegal input for acosh where input < 1; input = " + x.toPlainString());
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.acosh(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the hyperbolic arc cosine of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic arc cosine of the number, in radians.
     * @throws ArithmeticException When the number is less than 1.
     * @see #acosh(BigDecimal, int)
     */
    public static BigDecimal acosh(BigDecimal x, PrecisionMode precisionMode) throws ArithmeticException {
        return acosh(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the hyperbolic arc cosine of a number.
     *
     * @param x The number.
     * @return The hyperbolic arc cosine of the number, in radians.
     * @throws ArithmeticException When the number is less than 1.
     * @see #acosh(BigDecimal, PrecisionMode)
     */
    public static BigDecimal acosh(BigDecimal x) throws ArithmeticException {
        return acosh(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the tangent of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The tangent of the angle.
     * @see BigDecimalMath#tan(BigDecimal, MathContext)
     */
    public static BigDecimal tan(BigDecimal x, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.tan(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the tangent of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The tangent of the angle.
     * @see #tan(BigDecimal, int)
     */
    public static BigDecimal tan(BigDecimal x, PrecisionMode precisionMode) {
        return tan(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the tangent of an angle.
     *
     * @param x The angle in radians.
     * @return The tangent of the angle.
     * @see #tan(BigDecimal, PrecisionMode)
     */
    public static BigDecimal tan(BigDecimal x) {
        return tan(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the arc tangent of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The arc tangent of the number, in radians.
     * @see BigDecimalMath#atan(BigDecimal, MathContext)
     */
    public static BigDecimal atan(BigDecimal x, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.atan(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the arc tangent of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The arc tangent of the number, in radians.
     * @see #atan(BigDecimal, int)
     */
    public static BigDecimal atan(BigDecimal x, PrecisionMode precisionMode) {
        return atan(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the arc tangent of a number.
     *
     * @param x The number.
     * @return The arc tangent of the number, in radians.
     * @see #atan(BigDecimal, PrecisionMode)
     */
    public static BigDecimal atan(BigDecimal x) {
        return atan(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the arc tangent of a coordinate while preserving quadrant information.
     *
     * @param y         The y coordinate
     * @param x         The x coordinate.
     * @param precision The number of significant figures to return.
     * @return The arc tangent of the coordinate, in radians.
     * @throws ArithmeticException When x is 0 and y is 0.
     * @see BigDecimalMath#atan2(BigDecimal, BigDecimal, MathContext)
     */
    public static BigDecimal atan2(BigDecimal y, BigDecimal x, int precision) throws ArithmeticException {
        if ((x.compareTo(ZERO) == 0) && y.compareTo(ZERO) == 0) {
            throw new ArithmeticException("Illegal input for atan2 where x == 0 and y == 0");
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.atan2(y, x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the arc tangent of a coordinate while preserving quadrant information.
     *
     * @param y             The y coordinate
     * @param x             The x coordinate.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The arc tangent of the coordinate, in radians.
     * @throws ArithmeticException When x is 0 and y is 0.
     * @see #atan2(BigDecimal, BigDecimal, int)
     */
    public static BigDecimal atan2(BigDecimal y, BigDecimal x, PrecisionMode precisionMode) throws ArithmeticException {
        return atan2(y, x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the arc tangent of a coordinate while preserving quadrant information.
     *
     * @param y The y coordinate
     * @param x The x coordinate.
     * @return The arc tangent of the coordinate, in radians.
     * @throws ArithmeticException When x is 0 and y is 0.
     * @see #atan2(BigDecimal, BigDecimal, PrecisionMode)
     */
    public static BigDecimal atan2(BigDecimal y, BigDecimal x) throws ArithmeticException {
        return atan2(y, x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the hyperbolic tangent of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic tangent of the angle.
     * @see BigDecimalMath#tanh(BigDecimal, MathContext)
     */
    public static BigDecimal tanh(BigDecimal x, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.tanh(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the hyperbolic tangent of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic tangent of the angle.
     * @see #tanh(BigDecimal, int)
     */
    public static BigDecimal tanh(BigDecimal x, PrecisionMode precisionMode) {
        return tanh(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the hyperbolic tangent of an angle.
     *
     * @param x The angle in radians.
     * @return The hyperbolic tangent of the angle.
     * @see #tanh(BigDecimal, PrecisionMode)
     */
    public static BigDecimal tanh(BigDecimal x) {
        return tanh(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the hyperbolic arc tangent of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic arc tangent of the number, in radians.
     * @throws ArithmeticException When the number is not in the domain (-1,1).
     * @see BigDecimalMath#atanh(BigDecimal, MathContext)
     */
    public static BigDecimal atanh(BigDecimal x, int precision) throws ArithmeticException {
        if ((x.compareTo(NEGATIVE_ONE) <= 0) || (x.compareTo(ONE) >= 0)) {
            throw new ArithmeticException("Illegal input for atanh where input <= -1 or input >= 1; input = " + x.toPlainString());
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.atanh(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the hyperbolic arc tangent of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic arc tangent of the number, in radians.
     * @throws ArithmeticException When the number is not in the domain (-1,1).
     * @see #atanh(BigDecimal, int)
     */
    public static BigDecimal atanh(BigDecimal x, PrecisionMode precisionMode) throws ArithmeticException {
        return atanh(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the hyperbolic arc tangent of a number.
     *
     * @param x The number.
     * @return The hyperbolic arc tangent of the number, in radians.
     * @throws ArithmeticException When the number is not in the domain (-1,1).
     * @see #atanh(BigDecimal, PrecisionMode)
     */
    public static BigDecimal atanh(BigDecimal x) throws ArithmeticException {
        return atanh(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the cotangent of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The cotangent of the angle.
     * @throws ArithmeticException When the angle is 0.
     * @see BigDecimalMath#cot(BigDecimal, MathContext)
     */
    public static BigDecimal cot(BigDecimal x, int precision) throws ArithmeticException {
        if (x.compareTo(ZERO) == 0) {
            throw new ArithmeticException("Illegal input for cot where input == 0");
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.cot(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the cotangent of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The cotangent of the angle.
     * @throws ArithmeticException When the angle is 0.
     * @see #cot(BigDecimal, int)
     */
    public static BigDecimal cot(BigDecimal x, PrecisionMode precisionMode) throws ArithmeticException {
        return cot(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the cotangent of an angle.
     *
     * @param x The angle in radians.
     * @return The cotangent of the angle.
     * @throws ArithmeticException When the angle is 0.
     * @see #cot(BigDecimal, PrecisionMode)
     */
    public static BigDecimal cot(BigDecimal x) throws ArithmeticException {
        return cot(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the arc cotangent of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The arc cotangent of the number, in radians.
     * @see BigDecimalMath#acot(BigDecimal, MathContext)
     */
    public static BigDecimal acot(BigDecimal x, int precision) {
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.acot(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the arc cotangent of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The arc cotangent of the number, in radians.
     * @see #acot(BigDecimal, int)
     */
    public static BigDecimal acot(BigDecimal x, PrecisionMode precisionMode) {
        return acot(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the arc cotangent of a number.
     *
     * @param x The number.
     * @return The arc cotangent of the number, in radians.
     * @see #acot(BigDecimal, PrecisionMode)
     */
    public static BigDecimal acot(BigDecimal x) {
        return acot(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the hyperbolic cotangent of an angle.
     *
     * @param x         The angle in radians.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic cotangent of the angle.
     * @throws ArithmeticException When the angle is 0.
     * @see BigDecimalMath#coth(BigDecimal, MathContext)
     */
    public static BigDecimal coth(BigDecimal x, int precision) throws ArithmeticException {
        if (x.compareTo(ZERO) == 0) {
            throw new ArithmeticException("Illegal input for coth where input == 0");
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.coth(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the hyperbolic cotangent of an angle.
     *
     * @param x             The angle in radians.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic cotangent of the angle.
     * @throws ArithmeticException When the angle is 0.
     * @see #coth(BigDecimal, int)
     */
    public static BigDecimal coth(BigDecimal x, PrecisionMode precisionMode) throws ArithmeticException {
        return coth(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the hyperbolic cotangent of an angle.
     *
     * @param x The angle in radians.
     * @return The hyperbolic cotangent of the angle.
     * @throws ArithmeticException When the angle is 0.
     * @see #coth(BigDecimal, PrecisionMode)
     */
    public static BigDecimal coth(BigDecimal x) throws ArithmeticException {
        return coth(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the hyperbolic arc cotangent of a number.
     *
     * @param x         The number.
     * @param precision The number of significant figures to return.
     * @return The hyperbolic arc cotangent of the number, in radians.
     * @throws ArithmeticException When the angle is in the range [-1,1].
     * @see BigDecimalMath#acoth(BigDecimal, MathContext)
     */
    public static BigDecimal acoth(BigDecimal x, int precision) throws ArithmeticException {
        if ((x.compareTo(NEGATIVE_ONE) >= 0) && (x.compareTo(ONE) <= 0)) {
            throw new ArithmeticException("Illegal input for acoth where input >= -1 and input <= 1; input = " + x.toPlainString());
        }
        
        MathContext context = new MathContext(PrecisionMode.MAX_PRECISION.getPrecision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.acoth(x, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(DEFAULT_MATH_PRECISION, DEFAULT_ROUNDING_MODE).stripTrailingZeros();
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Computes the hyperbolic arc cotangent of a number.
     *
     * @param x             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The hyperbolic arc cotangent of the number, in radians.
     * @throws ArithmeticException When the angle is in the range [-1,1].
     * @see #acoth(BigDecimal, int)
     */
    public static BigDecimal acoth(BigDecimal x, PrecisionMode precisionMode) throws ArithmeticException {
        return acoth(x, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the hyperbolic arc cotangent of a number.
     *
     * @param x The number.
     * @return The hyperbolic arc cotangent of the number, in radians.
     * @throws ArithmeticException When the angle is in the range [-1,1].
     * @see #acoth(BigDecimal, PrecisionMode)
     */
    public static BigDecimal acoth(BigDecimal x) throws ArithmeticException {
        return acoth(x, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes pi.
     *
     * @param precision The number of significant figures to return.
     * @return Pi.
     * @see BigDecimalMath#pi(MathContext)
     */
    public static BigDecimal pi(int precision) {
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            precision = DEFAULT_MATH_PRECISION;
        } else if (precision == PrecisionMode.NO_PRECISION.getPrecision()) {
            precision = 1;
        }
        
        MathContext context = new MathContext(precision, DEFAULT_ROUNDING_MODE);
        return BigDecimalMath.pi(context);
    }
    
    /**
     * Computes pi.
     *
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return Pi.
     * @see #pi(int)
     */
    public static BigDecimal pi(PrecisionMode precisionMode) {
        return pi((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision());
    }
    
    /**
     * Computes pi.
     *
     * @return Pi.
     * @see #pi(PrecisionMode)
     */
    public static BigDecimal pi() {
        return pi(PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes e.
     *
     * @param precision The number of significant figures to return.
     * @return E.
     * @see BigDecimalMath#e(MathContext)
     */
    public static BigDecimal e(int precision) {
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            precision = DEFAULT_MATH_PRECISION;
        } else if (precision == PrecisionMode.NO_PRECISION.getPrecision()) {
            precision = 1;
        }
        
        MathContext context = new MathContext(precision, DEFAULT_ROUNDING_MODE);
        return BigDecimalMath.e(context);
    }
    
    /**
     * Computes e.
     *
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return E.
     * @see #e(int)
     */
    public static BigDecimal e(PrecisionMode precisionMode) {
        return e((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision());
    }
    
    /**
     * Computes e.
     *
     * @return E.
     * @see #e(PrecisionMode)
     */
    public static BigDecimal e() {
        return e(PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Computes the nth bernoulli number.
     *
     * @param n         The index of the bernoulli number.
     * @param precision The number of significant figures to return.
     * @return The nth bernoulli number.
     * @see BigDecimalMath#bernoulli(int, MathContext)
     */
    public static BigDecimal bernoulli(int n, int precision) {
        if (n < 0) {
            throw new ArithmeticException("Unable to compute bernoulli number at indices less that 0");
        }
        
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            precision = DEFAULT_MATH_PRECISION;
        } else if (precision == PrecisionMode.NO_PRECISION.getPrecision()) {
            precision = 1;
        }
        
        MathContext context = new MathContext(precision, DEFAULT_ROUNDING_MODE);
        return BigDecimalMath.bernoulli(n, context);
    }
    
    /**
     * Computes the nth bernoulli number.
     *
     * @param n             The index of the bernoulli number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The nth bernoulli number.
     * @see #bernoulli(int, int)
     */
    public static BigDecimal bernoulli(int n, PrecisionMode precisionMode) {
        return bernoulli(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Computes the nth bernoulli number.
     *
     * @param n The index of the bernoulli number.
     * @return The nth bernoulli number.
     * @see #bernoulli(int, PrecisionMode)
     */
    public static BigDecimal bernoulli(int n) {
        return bernoulli(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Returns the integral part of a number.
     *
     * @param n The number.
     * @return The integral part of the number.
     * @see BigDecimalMath#integralPart(BigDecimal)
     */
    public static BigDecimal integralPart(BigDecimal n) {
        return BigDecimalMath.integralPart(n);
    }
    
    /**
     * Returns the fractional part of a number.
     *
     * @param n The number.
     * @return The fractional part of the number.
     * @see BigDecimalMath#fractionalPart(BigDecimal)
     */
    public static BigDecimal fractionalPart(BigDecimal n) {
        return BigDecimalMath.fractionalPart(n);
    }
    
    /**
     * Returns the significant digits of a number.
     *
     * @param n The number.
     * @return The significant digits of the number.
     * @see BigDecimalMath#significantDigits(BigDecimal)
     */
    public static int significantDigits(BigDecimal n) {
        return BigDecimalMath.significantDigits(n);
    }
    
    /**
     * Returns the mantissa of a number.
     *
     * @param n The number.
     * @return The mantissa of the number.
     * @see BigDecimalMath#mantissa(BigDecimal)
     */
    public static BigDecimal mantissa(BigDecimal n) {
        return BigDecimalMath.mantissa(n);
    }
    
    /**
     * Returns the exponent of a number.
     *
     * @param n The number.
     * @return The exponent of the number.
     * @see BigDecimalMath#exponent(BigDecimal)
     */
    public static int exponent(BigDecimal n) {
        return BigDecimalMath.exponent(n);
    }
    
    /**
     * Rounds a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The rounded number.
     * @see BigDecimalMath#round(BigDecimal, MathContext)
     */
    public static BigDecimal round(BigDecimal n, int precision) {
        MathContext context = new MathContext(n.precision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = BigDecimalMath.round(n, context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result.setScale(0, DEFAULT_ROUNDING_MODE);
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Rounds a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The rounded number.
     * @see #round(BigDecimal, int)
     */
    public static BigDecimal round(BigDecimal n, PrecisionMode precisionMode) {
        return round(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Rounds a number.
     *
     * @param n The number.
     * @return The rounded number.
     * @see #round(BigDecimal, PrecisionMode)
     */
    public static BigDecimal round(BigDecimal n) {
        return round(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Returns the absolute value of a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The absolute value of the number.
     */
    public static BigDecimal abs(BigDecimal n, int precision) {
        MathContext context = new MathContext(n.precision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = n.abs(context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result;
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Returns the absolute value of a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The absolute value of the number.
     * @see #abs(BigDecimal, int)
     */
    public static BigDecimal abs(BigDecimal n, PrecisionMode precisionMode) {
        return abs(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Returns the absolute value of a number.
     *
     * @param n The number.
     * @return The absolute value of the number.
     * @see #abs(BigDecimal, PrecisionMode)
     */
    public static BigDecimal abs(BigDecimal n) {
        return abs(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Returns the negated value of a number.
     *
     * @param n         The number.
     * @param precision The number of significant figures to return.
     * @return The negated value of the number.
     */
    public static BigDecimal negate(BigDecimal n, int precision) {
        MathContext context = new MathContext(n.precision(), DEFAULT_ROUNDING_MODE);
        BigDecimal result = n.negate(context);
        if (precision == PrecisionMode.DEFAULT_PRECISION.getPrecision()) {
            return result;
        } else {
            return result.setScale(precision, DEFAULT_ROUNDING_MODE);
        }
    }
    
    /**
     * Returns the negated value of a number.
     *
     * @param n             The number.
     * @param precisionMode The precision mode specifying the number of significant figures to return.
     * @return The negated value of the number.
     * @see #negate(BigDecimal, int)
     */
    public static BigDecimal negate(BigDecimal n, PrecisionMode precisionMode) {
        return negate(n, ((precisionMode != null) ? precisionMode.getPrecision() : PrecisionMode.DEFAULT_PRECISION.getPrecision()));
    }
    
    /**
     * Returns the negated value of a number.
     *
     * @param n The number.
     * @return The negated value of the number.
     * @see #negate(BigDecimal, PrecisionMode)
     */
    public static BigDecimal negate(BigDecimal n) {
        return negate(n, PrecisionMode.DEFAULT_PRECISION);
    }
    
    /**
     * Determines if a number is greater than another number.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return Whether the first number is greater than the second number or not.
     */
    public static boolean greaterThan(BigDecimal n1, BigDecimal n2) {
        return (n1.compareTo(n2) > 0);
    }
    
    /**
     * Determines if a number is less than another number.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return Whether the first number is less than the second number or not.
     */
    public static boolean lessThan(BigDecimal n1, BigDecimal n2) {
        return (n1.compareTo(n2) < 0);
    }
    
    /**
     * Determines if a number is equal to another number.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return Whether the first number is equal to the second number or not.
     */
    public static boolean equalTo(BigDecimal n1, BigDecimal n2) {
        return (n1.compareTo(n2) == 0);
    }
    
    /**
     * Determines if a number is not equal to another number.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return Whether the first number is not equal to the second number or not.
     */
    public static boolean notEqualTo(BigDecimal n1, BigDecimal n2) {
        return !equalTo(n1, n2);
    }
    
    /**
     * Determines if a number is greater than or equal to another number.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return Whether the first number is greater than or equal to the second number or not.
     * @see #greaterThan(BigDecimal, BigDecimal)
     * @see #equalTo(BigDecimal, BigDecimal)
     */
    public static boolean greaterThanOrEqualTo(BigDecimal n1, BigDecimal n2) {
        return !lessThan(n1, n2);
    }
    
    /**
     * Determines if a number is less than or equal to another number.
     *
     * @param n1 The first number.
     * @param n2 The second number.
     * @return Whether the first number is less than or equal to the second number or not.
     * @see #lessThan(BigDecimal, BigDecimal)
     * @see #equalTo(BigDecimal, BigDecimal)
     */
    public static boolean lessThanOrEqualTo(BigDecimal n1, BigDecimal n2) {
        return !greaterThan(n1, n2);
    }
    
}
