/*
 * File:    UncheckedFunction.java
 * Package: commons.lambda.function.unchecked
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.function.unchecked;

import java.util.function.Function;

/**
 * A lambda function that accepts an argument and tries to produce a result and throws a runtime exception in the event of an error.
 *
 * @param <T> The type of the argument.
 * @param <R> The type of the result.
 * @see Function
 */
@FunctionalInterface
public interface UncheckedFunction<T, R> extends Function<T, R> {
    
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
     * Tries to produce a result from an argument and throws a runtime exception in the event of an error.
     *
     * @param arg The argument.
     * @return The result.
     * @throws RuntimeException When there is an error.
     * @see Function#apply(Object)
     * @see #tryApply(Object)
     */
    @Override
    default R apply(T arg) {
        try {
            return tryApply(arg);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    
    //Static Methods
    
    /**
     * Invokes an UncheckedFunction.
     *
     * @param uncheckedFunction The UncheckedFunction.
     * @param arg               The argument.
     * @param <T>               The type of the argument.
     * @param <R>               The type of the result.
     * @return The result.
     * @throws RuntimeException When there is an error.
     * @see #apply(Object)
     */
    static <T, R> R invoke(UncheckedFunction<T, R> uncheckedFunction, T arg) {
        return uncheckedFunction.apply(arg);
    }
    
}
