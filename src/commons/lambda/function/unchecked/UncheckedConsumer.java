/*
 * File:    UncheckedConsumer.java
 * Package: commons.lambda.function.unchecked
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.function.unchecked;

import java.util.function.Consumer;

/**
 * A lambda function that tries to consume an input and returns no result and throws a runtime exception in the event of an error.
 *
 * @param <T> The type of the input.
 * @see Consumer
 */
@FunctionalInterface
public interface UncheckedConsumer<T> extends Consumer<T> {
    
    //Methods
    
    /**
     * Tries to consume an input.
     *
     * @param in The input.
     * @throws Throwable When there is an error.
     */
    void tryAccept(T in) throws Throwable;
    
    /**
     * Tries to consume an input and throws a runtime exception in the event of an error.
     *
     * @param in the input.
     * @throws RuntimeException When there is an error.
     * @see Consumer#accept(Object)
     * @see #tryAccept(Object)
     */
    @Override
    default void accept(T in) {
        try {
            tryAccept(in);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    
    //Static Methods
    
    /**
     * Invokes an UncheckedConsumer.
     *
     * @param uncheckedConsumer The UncheckedConsumer.
     * @param in                the input.
     * @param <T>               The type of the input.
     * @throws RuntimeException When there is an error.
     * @see #accept(Object)
     */
    static <T> void invoke(UncheckedConsumer<T> uncheckedConsumer, T in) {
        uncheckedConsumer.accept(in);
    }
    
}
