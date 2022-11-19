/*
 * File:    CheckedBiFunction.java
 * Package: commons.lambda.function.checked
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.function.checked;

import java.util.function.BiFunction;

/**
 * A lambda function that accepts two arguments and tries to produce a result and ignores errors.
 *
 * @param <T> The type of the first argument.
 * @param <U> The type of the second argument.
 * @param <R> The type of the result.
 * @see BiFunction
 */
@FunctionalInterface
public interface CheckedBiFunction<T, U, R> extends BiFunction<T, U, R> {
    
    //Methods
    
    /**
     * Tries to produce a result from two arguments.
     *
     * @param arg1 The first argument.
     * @param arg2 The second argument.
     * @return The result.
     * @throws Throwable When there is an error.
     */
    R tryApply(T arg1, U arg2) throws Throwable;
    
    /**
     * Tries to produce a result from two arguments and ignores errors.
     *
     * @param arg1 The first argument.
     * @param arg2 The second argument.
     * @return The result, or null if there was an error.
     * @see BiFunction#apply(Object, Object)
     * @see #tryApply(Object, Object)
     */
    @Override
    default R apply(T arg1, U arg2) {
        try {
            return tryApply(arg1, arg2);
        } catch (Throwable ignored) {
            return null;
        }
    }
    
    
    //Static Methods
    
    /**
     * Invokes a CheckedBiFunction.
     *
     * @param checkedBiFunction The CheckedBiFunction.
     * @param arg1              The first argument.
     * @param arg2              The second argument.
     * @param <T>               The type of the first argument.
     * @param <U>               The type of the second argument.
     * @param <R>               The type of the result.
     * @return The result, or null if there was an error.
     * @see #apply(Object, Object)
     */
    static <T, U, R> R invoke(CheckedBiFunction<T, U, R> checkedBiFunction, T arg1, U arg2) {
        return checkedBiFunction.apply(arg1, arg2);
    }
    
}
