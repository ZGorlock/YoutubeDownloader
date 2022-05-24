/*
 * File:    Mappers.java
 * Package: commons.lambda.stream.mapper
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.stream.mapper;

import java.util.function.Consumer;
import java.util.function.Function;

import commons.lambda.function.checked.CheckedConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides access to custom stream mappers.
 */
public final class Mappers {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Mappers.class);
    
    
    //Static Methods
    
    /**
     * Performs an inline action on an element.
     *
     * @param element The element.
     * @param action  The consumer that performs an action on the element.
     * @param <T>     The type of the element.
     * @return The element.
     */
    public static <T> T perform(T element, Consumer<T> action) {
        action.accept(element);
        return element;
    }
    
    /**
     * Tries to perform an inline action on an element.
     *
     * @param element The element.
     * @param action  The consumer that attempts to performs an action on the element, ignoring any exceptions.
     * @param <T>     The type of the element.
     * @return The element.
     * @see #perform(Object, Consumer)
     */
    public static <T> T tryPerform(T element, CheckedConsumer<T> action) {
        return perform(element, action);
    }
    
    /**
     * Creates a custom mapper that performs an action on each element in a stream.
     *
     * @param action The consumer that performs an action on an element.
     * @param <T>    The type of the elements in the stream.
     * @return The element.
     * @see #perform(Object, Consumer)
     */
    public static <T> Function<T, T> forEach(Consumer<T> action) {
        return e -> perform(e, action);
    }
    
    /**
     * Creates a custom mapper that tries to perform an action on each element in a stream.
     *
     * @param action The consumer that attempts to perform an action on an element, ignoring any exceptions.
     * @param <T>    The type of the elements in the stream.
     * @return The element.
     * @see #forEach(Consumer)
     */
    public static <T> Function<T, T> tryForEach(CheckedConsumer<T> action) {
        return forEach(action);
    }
    
}
