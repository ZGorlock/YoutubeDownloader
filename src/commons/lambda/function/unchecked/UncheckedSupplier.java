/*
 * File:    UncheckedSupplier.java
 * Package: commons.lambda.function.unchecked
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.function.unchecked;

import java.util.function.Supplier;

/**
 * A lambda function that tries to supply a result and throws a runtime exception in the event of an error.
 *
 * @param <T> The type of the result.
 * @see Supplier
 */
@FunctionalInterface
public interface UncheckedSupplier<T> extends Supplier<T> {
    
    //Methods
    
    /**
     * Tries to supply a result.
     *
     * @return The result.
     * @throws Throwable When there is an error.
     */
    T tryGet() throws Throwable;
    
    /**
     * Tries to supply a result and throws a runtime exception in the event of an error.
     *
     * @return The result.
     * @throws RuntimeException When there is an error.
     * @see Supplier#get()
     * @see #tryGet()
     */
    @Override
    default T get() {
        try {
            return tryGet();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    
    //Static Methods
    
    /**
     * Invokes an UncheckedSupplier.
     *
     * @param uncheckedSupplier The UncheckedSupplier.
     * @param <T>               The type of the result.
     * @return The result.
     * @throws RuntimeException When there is an error.
     * @see #get()
     */
    static <T> T invoke(UncheckedSupplier<T> uncheckedSupplier) {
        return uncheckedSupplier.get();
    }
    
}
