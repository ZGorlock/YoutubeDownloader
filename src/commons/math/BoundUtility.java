/*
 * File:    BoundUtility.java
 * Package: commons.math
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional bound functionality.
 */
public final class BoundUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(BoundUtility.class);
    
    
    //Functions
    
    /**
     * Determines if the number if in the defined bounds.
     *
     * @param num        The number to test.
     * @param lower      The lower bound.
     * @param upper      The upper bound.
     * @param touchLower Whether the number can be equal to the lower bound or not.
     * @param touchUpper Whether the number can be equal to the upper bound or not.
     * @return Whether the number is in the defined bounds or not.
     */
    public static boolean inBounds(Number num, Number lower, Number upper, boolean touchLower, boolean touchUpper) {
        double n = num.doubleValue();
        double min = lower.doubleValue();
        double max = upper.doubleValue();
        
        boolean inLower = touchLower ? (n >= min) : (n > min);
        boolean inUpper = touchUpper ? (n <= max) : (n < max);
        return inLower && inUpper;
    }
    
    /**
     * Determines if the number if in the defined bounds.<br>
     * The number can be in between or equal to these bounds. (lower &lt;= num &lt;= upper)<br>
     * To test bounds for other cases see inBounds(Number, Number, Number, boolean, boolean).
     *
     * @param num   The number to test.
     * @param lower The lower bound.
     * @param upper The upper bound.
     * @return Whether the number is in the defined bounds or not.
     * @see #inBounds(Number, Number, Number, boolean, boolean)
     */
    public static boolean inBounds(Number num, Number lower, Number upper) {
        return inBounds(num, lower, upper, true, true);
    }
    
    /**
     * Forces a number within defined bounds.
     *
     * @param num The number value.
     * @param min The minimum value allowed.
     * @param max The maximum value allowed.
     * @return The truncated number.
     */
    public static Number truncateNum(Number num, Number min, Number max) {
        Number n = num;
        if (num.doubleValue() < min.doubleValue()) {
            n = min;
        }
        if (num.doubleValue() > max.doubleValue()) {
            n = max;
        }
        return n;
    }
    
}
