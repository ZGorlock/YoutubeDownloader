/*
 * File:    ListCollectors.java
 * Package: commons.lambda.stream.collector
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.stream.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import commons.object.collection.ListUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides access to custom stream collectors for collecting to lists.
 */
public final class ListCollectors {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ListCollectors.class);
    
    
    //Enums
    
    /**
     * An enumeration of List Flavors.
     */
    public enum ListFlavor {
        
        //Values
        
        STANDARD(Function.identity()),
        UNMODIFIABLE(ListUtility::unmodifiableList),
        SYNCHRONIZED(ListUtility::synchronizedList);
        
        
        //Fields
        
        /**
         * The function for applying the List Flavor.
         */
        private final Function<List<?>, List<?>> styler;
        
        
        //Constructors
        
        /**
         * Constructs a List Flavor.
         *
         * @param styler The function for applying the List Flavor.
         */
        ListFlavor(Function<List<?>, List<?>> styler) {
            this.styler = styler;
        }
        
        
        //Methods
        
        /**
         * Applies the List Flavor.
         *
         * @param list The list.
         * @param <T>  The type of the elements of the list.
         * @param <R>  The type of the list.
         * @return The list with the List Flavor.
         */
        @SuppressWarnings("unchecked")
        public <T, R extends List<T>> R apply(R list) {
            return (R) styler.apply(list);
        }
        
    }
    
    
    //Static Methods
    
    /**
     * Creates a new custom collector that collects a stream to a list.
     *
     * @param listSupplier The supplier that provides a list of a certain type.
     * @param listFlavor   The flavor of the list.
     * @param mapper       The function that produces the elements of the list.
     * @param <T>          The type of the elements of the stream.
     * @param <U>          The type of the elements of the list.
     * @param <R>          The type of the list.
     * @return The custom collector.
     * @see CustomCollectors#collect(Supplier, BiConsumer, BinaryOperator)
     * @see ListFlavor#apply(List)
     */
    public static <T, U, R extends List<U>> Collector<T, ?, R> toList(Supplier<R> listSupplier, ListFlavor listFlavor, Function<? super T, ? extends U> mapper) {
        return Collectors.collectingAndThen(
                CustomCollectors.collect(
                        listSupplier,
                        (l, e) -> l.add(mapper.apply(e)),
                        ListUtility::addAllAndGet),
                listFlavor::apply);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a list.
     *
     * @param listSupplier The supplier that provides a list of a certain type.
     * @param mapper       The function that produces the elements of the list.
     * @param <T>          The type of the elements of the stream.
     * @param <U>          The type of the elements of the list.
     * @param <R>          The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor, Function)
     */
    public static <T, U, R extends List<U>> Collector<T, ?, R> toList(Supplier<R> listSupplier, Function<? super T, ? extends U> mapper) {
        return toList(listSupplier, ListFlavor.STANDARD, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a list.
     *
     * @param listClass  The class of the list.
     * @param listFlavor The flavor of the list.
     * @param mapper     The function that produces the elements of the list.
     * @param <T>        The type of the elements of the stream.
     * @param <U>        The type of the list.
     * @param <L>        The class of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor, Function)
     */
    public static <T, U, L extends List<?>> Collector<T, ?, List<U>> toList(Class<L> listClass, ListFlavor listFlavor, Function<? super T, ? extends U> mapper) {
        return toList(generator(listClass), listFlavor, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a list.
     *
     * @param listClass The class of the list.
     * @param mapper    The function that produces the elements of the list.
     * @param <T>       The type of the elements of the stream.
     * @param <U>       The type of the list.
     * @param <L>       The class of the list.
     * @return The custom collector.
     * @see #toList(Class, ListFlavor, Function)
     */
    public static <T, U, L extends List<?>> Collector<T, ?, List<U>> toList(Class<L> listClass, Function<? super T, ? extends U> mapper) {
        return toList(listClass, ListFlavor.STANDARD, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a list.
     *
     * @param listSupplier The supplier that provides a list of a certain type.
     * @param listFlavor   The flavor of the list.
     * @param <T>          The type of the elements of the stream.
     * @param <R>          The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor, Function)
     */
    public static <T, R extends List<T>> Collector<T, ?, R> toList(Supplier<R> listSupplier, ListFlavor listFlavor) {
        return toList(listSupplier, listFlavor, Function.identity());
    }
    
    /**
     * Creates a new custom collector that collects a stream to a list.
     *
     * @param listSupplier The supplier that provides a list of a certain type.
     * @param <T>          The type of the elements of the stream.
     * @param <R>          The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor)
     */
    public static <T, R extends List<T>> Collector<T, ?, R> toList(Supplier<R> listSupplier) {
        return toList(listSupplier, ListFlavor.STANDARD);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a list.
     *
     * @param listClass  The class of the list.
     * @param listFlavor The flavor of the list.
     * @param <T>        The type of the elements of the stream.
     * @param <L>        The class of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor)
     */
    public static <T, L extends List<?>> Collector<T, ?, List<T>> toList(Class<L> listClass, ListFlavor listFlavor) {
        return toList(generator(listClass), listFlavor);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a list.
     *
     * @param listClass The class of the list.
     * @param <T>       The type of the elements of the stream.
     * @param <L>       The class of the list.
     * @return The custom collector.
     * @see #toList(Class, ListFlavor)
     */
    public static <T, L extends List<?>> Collector<T, ?, List<T>> toList(Class<L> listClass) {
        return toList(listClass, ListFlavor.STANDARD);
    }
    
    /**
     * Creates a supplier that supplies a list of a certain type.
     *
     * @param listClass The class of the list.
     * @param <T>       The type of the list.
     * @param <L>       The class of the list.
     * @return The list supplier.
     * @see ListUtility#emptyList(Class)
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<?>> Supplier<List<T>> generator(Class<L> listClass) {
        return () -> ListUtility.emptyList((Class<List<T>>) listClass);
    }
    
    /**
     * Creates a supplier that supplies a list of a certain type.
     *
     * @param listClass   The class of the list.
     * @param elementType The type of list.
     * @param <T>         The type of the list.
     * @param <L>         The class of the list.
     * @return The list supplier.
     * @see #generator(Class)
     */
    public static <T, L extends List<?>> Supplier<List<T>> generator(Class<L> listClass, Class<T> elementType) {
        return generator(listClass);
    }
    
    /**
     * Creates a supplier that supplies a list.
     *
     * @param <T> The type of the list.
     * @return The list supplier.
     * @see #generator(Class)
     */
    public static <T> Supplier<List<T>> generator() {
        return generator(ArrayList.class);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an array list.
     *
     * @param listFlavor The flavor of the list.
     * @param mapper     The function that produces the elements of the list.
     * @param <T>        The type of the elements of the stream.
     * @param <U>        The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, ArrayList<U>> toArrayList(ListFlavor listFlavor, Function<? super T, ? extends U> mapper) {
        return toList(ArrayList::new, listFlavor, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an array list.
     *
     * @param listFlavor The flavor of the list.
     * @param <T>        The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor)
     */
    public static <T> Collector<T, ?, ArrayList<T>> toArrayList(ListFlavor listFlavor) {
        return toList(ArrayList::new, listFlavor);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an array list.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toArrayList(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, ArrayList<U>> toArrayList(Function<? super T, ? extends U> mapper) {
        return toArrayList(ListFlavor.STANDARD, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an array list.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toArrayList(ListFlavor)
     */
    public static <T> Collector<T, ?, ArrayList<T>> toArrayList() {
        return toArrayList(ListFlavor.STANDARD);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable array list.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toArrayList(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, ArrayList<U>> toUnmodifiableArrayList(Function<? super T, ? extends U> mapper) {
        return toArrayList(ListFlavor.UNMODIFIABLE, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable array list.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toArrayList(ListFlavor)
     */
    public static <T> Collector<T, ?, ArrayList<T>> toUnmodifiableArrayList() {
        return toArrayList(ListFlavor.UNMODIFIABLE);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized array list.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toArrayList(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, ArrayList<U>> toSynchronizedArrayList(Function<? super T, ? extends U> mapper) {
        return toArrayList(ListFlavor.SYNCHRONIZED, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized array list.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toArrayList(ListFlavor)
     */
    public static <T> Collector<T, ?, ArrayList<T>> toSynchronizedArrayList() {
        return toArrayList(ListFlavor.SYNCHRONIZED);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a linked list.
     *
     * @param listFlavor The flavor of the list.
     * @param mapper     The function that produces the elements of the list.
     * @param <T>        The type of the elements of the stream.
     * @param <U>        The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, LinkedList<U>> toLinkedList(ListFlavor listFlavor, Function<? super T, ? extends U> mapper) {
        return toList(LinkedList::new, listFlavor, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a linked list.
     *
     * @param listFlavor The flavor of the list.
     * @param <T>        The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor)
     */
    public static <T> Collector<T, ?, LinkedList<T>> toLinkedList(ListFlavor listFlavor) {
        return toList(LinkedList::new, listFlavor);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a linked list.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toLinkedList(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, LinkedList<U>> toLinkedList(Function<? super T, ? extends U> mapper) {
        return toLinkedList(ListFlavor.STANDARD, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a linked list.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toLinkedList(ListFlavor)
     */
    public static <T> Collector<T, ?, LinkedList<T>> toLinkedList() {
        return toLinkedList(ListFlavor.STANDARD);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable linked list.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toLinkedList(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, LinkedList<U>> toUnmodifiableLinkedList(Function<? super T, ? extends U> mapper) {
        return toLinkedList(ListFlavor.UNMODIFIABLE, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable linked list.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toLinkedList(ListFlavor)
     */
    public static <T> Collector<T, ?, LinkedList<T>> toUnmodifiableLinkedList() {
        return toLinkedList(ListFlavor.UNMODIFIABLE);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized linked list.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toLinkedList(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, LinkedList<U>> toSynchronizedLinkedList(Function<? super T, ? extends U> mapper) {
        return toLinkedList(ListFlavor.SYNCHRONIZED, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized linked list.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toLinkedList(ListFlavor)
     */
    public static <T> Collector<T, ?, LinkedList<T>> toSynchronizedLinkedList() {
        return toLinkedList(ListFlavor.SYNCHRONIZED);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a stack.
     *
     * @param listFlavor The flavor of the list.
     * @param mapper     The function that produces the elements of the list.
     * @param <T>        The type of the elements of the stream.
     * @param <U>        The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, Stack<U>> toStack(ListFlavor listFlavor, Function<? super T, ? extends U> mapper) {
        return toList(Stack::new, listFlavor, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a stack.
     *
     * @param listFlavor The flavor of the list.
     * @param <T>        The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor)
     */
    public static <T> Collector<T, ?, Stack<T>> toStack(ListFlavor listFlavor) {
        return toList(Stack::new, listFlavor);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a stack.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toStack(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, Stack<U>> toStack(Function<? super T, ? extends U> mapper) {
        return toStack(ListFlavor.STANDARD, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a stack.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toStack(ListFlavor)
     */
    public static <T> Collector<T, ?, Stack<T>> toStack() {
        return toStack(ListFlavor.STANDARD);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable stack.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toStack(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, Stack<U>> toUnmodifiableStack(Function<? super T, ? extends U> mapper) {
        return toStack(ListFlavor.UNMODIFIABLE, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable stack.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toStack(ListFlavor)
     */
    public static <T> Collector<T, ?, Stack<T>> toUnmodifiableStack() {
        return toStack(ListFlavor.UNMODIFIABLE);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized stack.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toStack(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, Stack<U>> toSynchronizedStack(Function<? super T, ? extends U> mapper) {
        return toStack(ListFlavor.SYNCHRONIZED, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized stack.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toStack(ListFlavor)
     */
    public static <T> Collector<T, ?, Stack<T>> toSynchronizedStack() {
        return toStack(ListFlavor.SYNCHRONIZED);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a vector.
     *
     * @param listFlavor The flavor of the list.
     * @param mapper     The function that produces the elements of the list.
     * @param <T>        The type of the elements of the stream.
     * @param <U>        The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, Vector<U>> toVector(ListFlavor listFlavor, Function<? super T, ? extends U> mapper) {
        return toList(Vector::new, listFlavor, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a vector.
     *
     * @param listFlavor The flavor of the list.
     * @param <T>        The type of the list.
     * @return The custom collector.
     * @see #toList(Supplier, ListFlavor)
     */
    public static <T> Collector<T, ?, Vector<T>> toVector(ListFlavor listFlavor) {
        return toList(Vector::new, listFlavor);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a vector.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toVector(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, Vector<U>> toVector(Function<? super T, ? extends U> mapper) {
        return toVector(ListFlavor.STANDARD, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a vector.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toVector(ListFlavor)
     */
    public static <T> Collector<T, ?, Vector<T>> toVector() {
        return toVector(ListFlavor.STANDARD);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable vector.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toVector(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, Vector<U>> toUnmodifiableVector(Function<? super T, ? extends U> mapper) {
        return toVector(ListFlavor.UNMODIFIABLE, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable vector.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toVector(ListFlavor)
     */
    public static <T> Collector<T, ?, Vector<T>> toUnmodifiableVector() {
        return toVector(ListFlavor.UNMODIFIABLE);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized vector.
     *
     * @param mapper The function that produces the elements of the list.
     * @param <T>    The type of the elements of the stream.
     * @param <U>    The type of the list.
     * @return The custom collector.
     * @see #toVector(ListFlavor, Function)
     */
    public static <T, U> Collector<T, ?, Vector<U>> toSynchronizedVector(Function<? super T, ? extends U> mapper) {
        return toVector(ListFlavor.SYNCHRONIZED, mapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized vector.
     *
     * @param <T> The type of the list.
     * @return The custom collector.
     * @see #toVector(ListFlavor)
     */
    public static <T> Collector<T, ?, Vector<T>> toSynchronizedVector() {
        return toVector(ListFlavor.SYNCHRONIZED);
    }
    
    /**
     * Creates a new custom collector that collects a stream into an existing list.
     *
     * @param list The existing list to add to.
     * @param <T>  The type of the elements of the stream.
     * @param <U>  The type of the list.
     * @return The custom collector.
     * @see #toArrayList()
     * @see ListUtility#addAllAndGet(List, Collection)
     */
    public static <T extends U, U> Collector<T, ?, List<U>> addTo(List<U> list) {
        return Collectors.collectingAndThen(
                toArrayList(),
                collected -> ListUtility.addAllAndGet(list, collected));
    }
    
}
