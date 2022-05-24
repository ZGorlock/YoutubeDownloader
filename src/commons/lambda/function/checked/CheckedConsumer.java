/*
 * File:    CheckedConsumer.java
 * Package: commons.lambda.function.checked
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.function.checked;

import java.util.function.Consumer;

/**
 * A lambda function that tries to consume an input and returns no result and ignores errors.
 *
 * @param <T> The type of the input.
 * @see Consumer
 */
@FunctionalInterface
public interface CheckedConsumer<T> extends Consumer<T> {
    
    //Methods
    
    /**
     * Tries to consume an input.
     *
     * @param in The input.
     * @throws Throwable When there is an error.
     */
    void tryAccept(T in) throws Throwable;
    
    /**
     * Tries to consume an input and ignores errors.
     *
     * @param in the input.
     * @see Consumer#accept(Object)
     * @see #tryAccept(Object)
     */
    @Override
    default void accept(T in) {
        try {
            tryAccept(in);
        } catch (Throwable ignored) {
        }
    }
    
    
    //Static Methods
    
    /**
     * Invokes a CheckedConsumer.
     *
     * @param checkedConsumer The CheckedConsumer.
     * @param in              the input.
     * @param <T>             The type of the input.
     * @see #accept(Object)
     */
    static <T> void invoke(CheckedConsumer<T> checkedConsumer, T in) {
        checkedConsumer.accept(in);
    }
    
}
