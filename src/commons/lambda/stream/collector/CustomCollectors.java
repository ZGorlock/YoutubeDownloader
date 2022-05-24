/*
 * File:    CustomCollectors.java
 * Package: commons.lambda.stream.collector
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.stream.collector;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides access to custom stream collectors.
 */
public final class CustomCollectors {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(CustomCollectors.class);
    
    
    //Static Methods
    
    /**
     * Creates a new custom collector.
     *
     * @param supplier        The supplier of the collector.
     * @param accumulator     The accumulator of the collector.
     * @param combiner        The combiner of the collector.
     * @param finisher        The finisher of the collector.
     * @param characteristics The characteristics of the collector.
     * @param <T>             The type of the elements of the stream.
     * @param <A>             The type of the accumulator of the collector.
     * @param <R>             The type of the result of the collector.
     * @return The custom collector.
     * @see CustomCollector#CustomCollector(Supplier, BiConsumer, BinaryOperator, Function, Set)
     */
    public static <T, A, R> Collector<T, A, R> collect(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher, Set<Collector.Characteristics> characteristics) {
        return new CustomCollector<>(supplier, accumulator, combiner, finisher, characteristics);
    }
    
    /**
     * Creates a new custom collector.
     *
     * @param supplier    The supplier of the collector.
     * @param accumulator The accumulator of the collector.
     * @param combiner    The combiner of the collector.
     * @param finisher    The finisher of the collector.
     * @param <T>         The type of the elements of the stream.
     * @param <A>         The type of the accumulator of the collector.
     * @param <R>         The type of the result of the collector.
     * @return The custom collector.
     * @see #collect(Supplier, BiConsumer, BinaryOperator, Function, Set)
     */
    public static <T, A, R> Collector<T, A, R> collect(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher) {
        return collect(supplier, accumulator, combiner, finisher, Set.of());
    }
    
    /**
     * Creates a new custom collector.
     *
     * @param supplier    The supplier of the collector.
     * @param accumulator The accumulator of the collector.
     * @param combiner    The combiner of the collector.
     * @param <T>         The type of the elements of the stream.
     * @param <A>         The type of the accumulator and the result of the collector.
     * @return The custom collector.
     * @see #collect(Supplier, BiConsumer, BinaryOperator, Function, Set)
     */
    public static <T, A> Collector<T, A, A> collect(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner) {
        return collect(supplier, accumulator, combiner, Function.identity(), Set.of(Collector.Characteristics.IDENTITY_FINISH));
    }
    
    
    //Inner Classes
    
    /**
     * Defines a custom collector.
     *
     * @param <T> The type of the elements of the stream.
     * @param <A> The type of the accumulator of the collector.
     * @param <R> The type of the result of the collector.
     * @see Collector
     */
    private static class CustomCollector<T, A, R> implements Collector<T, A, R> {
        
        //Fields
        
        /**
         * The supplier of the collector.
         */
        private final Supplier<A> supplier;
        
        /**
         * The accumulator of the collector.
         */
        private final BiConsumer<A, T> accumulator;
        
        /**
         * The combiner of the collector.
         */
        private final BinaryOperator<A> combiner;
        
        /**
         * The finisher of the collector.
         */
        private final Function<A, R> finisher;
        
        /**
         * The characteristics of the collector.
         */
        private final Set<Characteristics> characteristics;
        
        
        //Constructors
        
        /**
         * Private constructor for a Custom Collector.
         *
         * @param supplier        The supplier of the collector.
         * @param accumulator     The accumulator of the collector.
         * @param combiner        The combiner of the collector.
         * @param finisher        The finisher of the collector.
         * @param characteristics The characteristics of the collector.
         */
        private CustomCollector(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher, Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }
        
        
        //Getters
        
        /**
         * Returns the supplier of the collector.
         *
         * @return The supplier of the collector.
         */
        @Override
        public Supplier<A> supplier() {
            return supplier;
        }
        
        /**
         * Returns the accumulator of the collector.
         *
         * @return The accumulator of the collector.
         */
        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }
        
        /**
         * Returns the combiner of the collector.
         *
         * @return The combiner of the collector.
         */
        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }
        
        /**
         * Returns the finisher of the collector.
         *
         * @return The finisher of the collector.
         */
        @Override
        public Function<A, R> finisher() {
            return finisher;
        }
        
        /**
         * Returns the characteristics of the collector.
         *
         * @return The characteristics of the collector.
         */
        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
        
    }
    
}
