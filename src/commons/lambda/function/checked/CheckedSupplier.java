/*
 * File:    CheckedSupplier.java
 * Package: commons.lambda.function.checked
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.function.checked;

import java.util.function.Supplier;

/**
 * A lambda function that tries to supply a result and ignores errors.
 *
 * @param <T> The type of the result.
 * @see Supplier
 */
@FunctionalInterface
public interface CheckedSupplier<T> extends Supplier<T> {
    
    //Methods
    
    /**
     * Tries to supply a result.
     *
     * @return The result.
     * @throws Throwable When there is an error.
     */
    T tryGet() throws Throwable;
    
    /**
     * Tries to supply a result and ignores errors.
     *
     * @return The result, or null if there was an error.
     * @see Supplier#get()
     * @see #tryGet()
     */
    @Override
    default T get() {
        try {
            return tryGet();
        } catch (Throwable ignored) {
            return null;
        }
    }
    
    
    //Static Methods
    
    /**
     * Invokes a CheckedSupplier.
     *
     * @param checkedSupplier The CheckedSupplier.
     * @param <T>             The type of the result.
     * @return The result, or null if there was an error.
     * @see #get()
     */
    static <T> T invoke(CheckedSupplier<T> checkedSupplier) {
        return checkedSupplier.get();
    }
    
}
