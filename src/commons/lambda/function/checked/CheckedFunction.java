/*
 * File:    CheckedFunction.java
 * Package: commons.lambda.function.checked
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.function.checked;

import java.util.function.Function;

/**
 * A lambda function that accepts an argument and tries to produce a result and ignores errors.
 *
 * @param <T> The type of the argument.
 * @param <R> The type of the result.
 * @see Function
 */
@FunctionalInterface
public interface CheckedFunction<T, R> extends Function<T, R> {
    
    //Methods
    
    /**
     * Tries to produce a result from an argument.
     *
     * @param arg The argument.
     * @return The result.
     * @throws Throwable When there is an error.
     */
    R tryApply(T arg) throws Throwable;
    
    /**
     * Tries to produce a result from an argument and ignores errors.
     *
     * @param arg The argument.
     * @return The result, or null if there was an error.
     * @see Function#apply(Object)
     * @see #tryApply(Object)
     */
    @Override
    default R apply(T arg) {
        try {
            return tryApply(arg);
        } catch (Throwable ignored) {
            return null;
        }
    }
    
    
    //Static Methods
    
    /**
     * Invokes a CheckedFunction.
     *
     * @param checkedFunction The CheckedFunction.
     * @param arg             The argument.
     * @param <T>             The type of the argument.
     * @param <R>             The type of the result.
     * @return The result, or null if there was an error.
     * @see #apply(Object)
     */
    static <T, R> R invoke(CheckedFunction<T, R> checkedFunction, T arg) {
        return checkedFunction.apply(arg);
    }
    
}
