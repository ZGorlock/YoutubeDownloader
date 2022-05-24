/*
 * File:    MapCollectors.java
 * Package: commons.lambda.stream.collector
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.lambda.stream.collector;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import commons.object.collection.MapUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides access to custom stream collectors for collecting to maps.
 */
public final class MapCollectors {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(MapCollectors.class);
    
    
    //Enums
    
    /**
     * An enumeration of Map Flavors.
     */
    public enum MapFlavor {
        
        //Values
        
        STANDARD(Function.identity()),
        UNMODIFIABLE(MapUtility::unmodifiableMap),
        SYNCHRONIZED(MapUtility::synchronizedMap);
        
        
        //Fields
        
        /**
         * The function for applying the Map Flavor.
         */
        private final Function<Map<?, ?>, Map<?, ?>> styler;
        
        
        //Constructors
        
        /**
         * Constructs a Map Flavor.
         *
         * @param styler The function for applying the Map Flavor.
         */
        MapFlavor(Function<Map<?, ?>, Map<?, ?>> styler) {
            this.styler = styler;
        }
        
        
        //Methods
        
        /**
         * Applies the Map Flavor.
         *
         * @param map The map.
         * @param <K> The type of the keys of the map.
         * @param <V> The type of the values of the map.
         * @param <R> The type of the map.
         * @return The map with the Map Flavor.
         */
        @SuppressWarnings("unchecked")
        public <K, V, R extends Map<K, V>> R apply(R map) {
            return (R) styler.apply(map);
        }
        
    }
    
    
    //Static Methods
    
    /**
     * Creates a new custom collector that collects a stream to a map.
     *
     * @param mapSupplier The supplier that provides a map of a certain type.
     * @param mapFlavor   The flavor of the map.
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @param <R>         The type of the map.
     * @return The custom collector.
     * @see CustomCollectors#collect(Supplier, BiConsumer, BinaryOperator)
     * @see MapFlavor#apply(Map)
     */
    public static <T, K, V, R extends Map<K, V>> Collector<T, ?, R> toMap(Supplier<R> mapSupplier, MapFlavor mapFlavor, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return Collectors.collectingAndThen(
                CustomCollectors.collect(
                        mapSupplier,
                        (m, e) -> m.put(keyMapper.apply(e), valueMapper.apply(e)),
                        MapUtility::putAllAndGet),
                mapFlavor::apply);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a map.
     *
     * @param mapSupplier The supplier that provides a map of a certain type.
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @param <R>         The type of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor, Function, Function)
     */
    public static <T, K, V, R extends Map<K, V>> Collector<T, ?, R> toMap(Supplier<R> mapSupplier, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toMap(mapSupplier, MapFlavor.STANDARD, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a map.
     *
     * @param mapClass    The class of the map.
     * @param mapFlavor   The flavor of the map.
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @param <M>         The class of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor, Function, Function)
     */
    public static <T, K, V, M extends Map<?, ?>> Collector<T, ?, Map<K, V>> toMap(Class<M> mapClass, MapFlavor mapFlavor, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toMap(generator(mapClass), mapFlavor, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a map.
     *
     * @param mapClass    The class of the map.
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @param <M>         The class of the map.
     * @return The custom collector.
     * @see #toMap(Class, MapFlavor, Function, Function)
     */
    public static <T, K, V, M extends Map<?, ?>> Collector<T, ?, Map<K, V>> toMap(Class<M> mapClass, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toMap(mapClass, MapFlavor.STANDARD, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a map.
     *
     * @param mapSupplier The supplier that provides a map of a certain type.
     * @param mapFlavor   The flavor of the map.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @param <R>         The type of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor, Function, Function)
     */
    public static <K, V, R extends Map<K, V>> Collector<Map.Entry<K, V>, ?, R> toMap(Supplier<R> mapSupplier, MapFlavor mapFlavor) {
        return toMap(mapSupplier, mapFlavor, Map.Entry::getKey, Map.Entry::getValue);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a map.
     *
     * @param mapSupplier The supplier that provides a map of a certain type.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @param <R>         The type of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor)
     */
    public static <K, V, R extends Map<K, V>> Collector<Map.Entry<K, V>, ?, R> toMap(Supplier<R> mapSupplier) {
        return toMap(mapSupplier, MapFlavor.STANDARD);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a map.
     *
     * @param mapClass  The class of the map.
     * @param mapFlavor The flavor of the map.
     * @param <K>       The type of the keys of the map.
     * @param <V>       The type of the values of the map.
     * @param <M>       The class of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor)
     */
    public static <K, V, M extends Map<?, ?>> Collector<Map.Entry<K, V>, ?, Map<K, V>> toMap(Class<M> mapClass, MapFlavor mapFlavor) {
        return toMap(generator(mapClass), mapFlavor);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a map.
     *
     * @param mapClass The class of the map.
     * @param <K>      The type of the keys of the map.
     * @param <V>      The type of the values of the map.
     * @param <M>      The class of the map.
     * @return The custom collector.
     * @see #toMap(Class, MapFlavor)
     */
    public static <K, V, M extends Map<?, ?>> Collector<Map.Entry<K, V>, ?, Map<K, V>> toMap(Class<M> mapClass) {
        return toMap(mapClass, MapFlavor.STANDARD);
    }
    
    /**
     * Creates a supplier that supplies a map of a certain type.
     *
     * @param mapClass The class of the map.
     * @param <K>      The type of the keys of the map.
     * @param <V>      The type of the values of the map.
     * @param <M>      The class of the map.
     * @return The map supplier.
     * @see MapUtility#emptyMap(Class)
     */
    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<?, ?>> Supplier<Map<K, V>> generator(Class<M> mapClass) {
        return () -> MapUtility.emptyMap((Class<Map<K, V>>) mapClass);
    }
    
    /**
     * Creates a supplier that supplies a map of a certain type.
     *
     * @param mapClass  The class of the map.
     * @param keyType   The type of the keys of the map.
     * @param valueType The type of the values of the map.
     * @param <K>       The type of the keys of the map.
     * @param <V>       The type of the values of the map.
     * @param <M>       The class of the map.
     * @return The map supplier.
     * @see #generator(Class)
     */
    public static <K, V, M extends Map<?, ?>> Supplier<Map<K, V>> generator(Class<M> mapClass, Class<K> keyType, Class<V> valueType) {
        return generator(mapClass);
    }
    
    /**
     * Creates a supplier that supplies a map.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The map supplier.
     * @see #generator(Class)
     */
    public static <K, V> Supplier<Map<K, V>> generator() {
        return generator(HashMap.class);
    }
    
    /**
     * Creates a function that generates a map entry.
     *
     * @param keyMapper   The function that produces the key of the map entry.
     * @param valueMapper The function that produces the value of the map entry.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the key of the map entry.
     * @param <V>         The type of the value of the map entry.
     * @return The map entry generator.
     */
    public static <T, K, V> Function<T, Map.Entry<K, V>> toEntry(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return e -> Map.entry(keyMapper.apply(e), valueMapper.apply(e));
    }
    
    /**
     * Creates a new custom collector that collects a stream to a hash map.
     *
     * @param mapFlavor   The flavor of the map.
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor, Function, Function)
     */
    public static <T, K, V> Collector<T, ?, HashMap<K, V>> toHashMap(MapFlavor mapFlavor, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toMap(HashMap::new, mapFlavor, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a hash map.
     *
     * @param mapFlavor The flavor of the map.
     * @param <K>       The type of the keys of the map.
     * @param <V>       The type of the values of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor)
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, HashMap<K, V>> toHashMap(MapFlavor mapFlavor) {
        return toMap(HashMap::new, mapFlavor);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a hash map.
     *
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toHashMap(MapFlavor, Function, Function)
     */
    public static <T, K, V> Collector<T, ?, HashMap<K, V>> toHashMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toHashMap(MapFlavor.STANDARD, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a hash map.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The custom collector.
     * @see #toHashMap(MapFlavor)
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, HashMap<K, V>> toHashMap() {
        return toHashMap(MapFlavor.STANDARD);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable hash map.
     *
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toHashMap(MapFlavor, Function, Function)
     */
    public static <T, K, V> Collector<T, ?, HashMap<K, V>> toUnmodifiableHashMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toHashMap(MapFlavor.UNMODIFIABLE, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to an unmodifiable hash map.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The custom collector.
     * @see #toHashMap(MapFlavor)
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, HashMap<K, V>> toUnmodifiableHashMap() {
        return toHashMap(MapFlavor.UNMODIFIABLE);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized hash map.
     *
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toHashMap(MapFlavor, Function, Function)
     */
    public static <T, K, V> Collector<T, ?, HashMap<K, V>> toSynchronizedHashMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toHashMap(MapFlavor.SYNCHRONIZED, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a synchronized hash map.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The custom collector.
     * @see #toHashMap(MapFlavor)
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, HashMap<K, V>> toSynchronizedHashMap() {
        return toHashMap(MapFlavor.SYNCHRONIZED);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a linked hash map.
     *
     * @param mapFlavor   The flavor of the map.
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor, Function, Function)
     */
    public static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> toLinkedHashMap(MapFlavor mapFlavor, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toMap(LinkedHashMap::new, mapFlavor, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a linked hash map.
     *
     * @param mapFlavor The flavor of the map.
     * @param <K>       The type of the keys of the map.
     * @param <V>       The type of the values of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor)
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, LinkedHashMap<K, V>> toLinkedHashMap(MapFlavor mapFlavor) {
        return toMap(LinkedHashMap::new, mapFlavor);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a linked hash map.
     *
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toLinkedHashMap(MapFlavor, Function, Function)
     */
    public static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toLinkedHashMap(MapFlavor.STANDARD, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a linked hash map.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The custom collector.
     * @see #toLinkedHashMap(MapFlavor)
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, LinkedHashMap<K, V>> toLinkedHashMap() {
        return toLinkedHashMap(MapFlavor.STANDARD);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable linked hash map.
     *
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toLinkedHashMap(MapFlavor, Function, Function)
     */
    public static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> toUnmodifiableLinkedHashMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toLinkedHashMap(MapFlavor.UNMODIFIABLE, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to an unmodifiable linked hash map.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The custom collector.
     * @see #toLinkedHashMap(MapFlavor)
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, LinkedHashMap<K, V>> toUnmodifiableLinkedHashMap() {
        return toLinkedHashMap(MapFlavor.UNMODIFIABLE);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized linked hash map.
     *
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toLinkedHashMap(MapFlavor, Function, Function)
     */
    public static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> toSynchronizedLinkedHashMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toLinkedHashMap(MapFlavor.SYNCHRONIZED, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a synchronized linked hash map.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The custom collector.
     * @see #toLinkedHashMap(MapFlavor)
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, LinkedHashMap<K, V>> toSynchronizedLinkedHashMap() {
        return toLinkedHashMap(MapFlavor.SYNCHRONIZED);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a tree map.
     *
     * @param mapFlavor   The flavor of the map.
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor, Function, Function)
     */
    public static <T, K extends Comparable<?>, V> Collector<T, ?, TreeMap<K, V>> toTreeMap(MapFlavor mapFlavor, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toMap(TreeMap::new, mapFlavor, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a tree map.
     *
     * @param mapFlavor The flavor of the map.
     * @param <K>       The type of the keys of the map.
     * @param <V>       The type of the values of the map.
     * @return The custom collector.
     * @see #toMap(Supplier, MapFlavor)
     */
    public static <K extends Comparable<?>, V> Collector<Map.Entry<K, V>, ?, TreeMap<K, V>> toTreeMap(MapFlavor mapFlavor) {
        return toMap(TreeMap::new, mapFlavor);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a tree map.
     *
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toTreeMap(MapFlavor, Function, Function)
     */
    public static <T, K extends Comparable<?>, V> Collector<T, ?, TreeMap<K, V>> toTreeMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toTreeMap(MapFlavor.STANDARD, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a tree map.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The custom collector.
     * @see #toTreeMap(MapFlavor)
     */
    public static <K extends Comparable<?>, V> Collector<Map.Entry<K, V>, ?, TreeMap<K, V>> toTreeMap() {
        return toTreeMap(MapFlavor.STANDARD);
    }
    
    /**
     * Creates a new custom collector that collects a stream to an unmodifiable tree map.
     *
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toTreeMap(MapFlavor, Function, Function)
     */
    public static <T, K extends Comparable<?>, V> Collector<T, ?, TreeMap<K, V>> toUnmodifiableTreeMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toTreeMap(MapFlavor.UNMODIFIABLE, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to an unmodifiable tree map.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The custom collector.
     * @see #toTreeMap(MapFlavor)
     */
    public static <K extends Comparable<?>, V> Collector<Map.Entry<K, V>, ?, TreeMap<K, V>> toUnmodifiableTreeMap() {
        return toTreeMap(MapFlavor.UNMODIFIABLE);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a synchronized tree map.
     *
     * @param keyMapper   The function that produces the keys of the map.
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <K>         The type of the keys of the map.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toTreeMap(MapFlavor, Function, Function)
     */
    public static <T, K extends Comparable<?>, V> Collector<T, ?, TreeMap<K, V>> toSynchronizedTreeMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return toTreeMap(MapFlavor.SYNCHRONIZED, keyMapper, valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a synchronized tree map.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The custom collector.
     * @see #toTreeMap(MapFlavor)
     */
    public static <K extends Comparable<?>, V> Collector<Map.Entry<K, V>, ?, TreeMap<K, V>> toSynchronizedTreeMap() {
        return toTreeMap(MapFlavor.SYNCHRONIZED);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a hash map between the stream elements and a produced value.
     *
     * @param valueMapper The function that produces the values of the map.
     * @param <T>         The type of the elements of the stream.
     * @param <V>         The type of the values of the map.
     * @return The custom collector.
     * @see #toHashMap(Function, Function)
     */
    public static <T, V> Collector<T, ?, HashMap<T, V>> mapEachTo(Function<? super T, ? extends V> valueMapper) {
        return toHashMap(Function.identity(), valueMapper);
    }
    
    /**
     * Creates a new custom collector that collects a stream to a hash map between the stream elements and a supplied value.
     *
     * @param valueSupplier The supplier that supplies the values of the map.
     * @param <T>           The type of the elements of the stream.
     * @param <V>           The type of the values of the map.
     * @return The custom collector.
     * @see #mapEachTo(Function)
     */
    public static <T, V> Collector<T, ?, HashMap<T, V>> mapEachTo(Supplier<? extends V> valueSupplier) {
        return mapEachTo(e -> valueSupplier.get());
    }
    
    /**
     * Creates a new custom collector that collects a stream to a hash map between the stream elements and a value.
     *
     * @param value The value to set the values of the map.
     * @param <T>   The type of the elements of the stream.
     * @param <V>   The type of the values of the map.
     * @return The custom collector.
     * @see #mapEachTo(Supplier)
     */
    public static <T, V> Collector<T, ?, HashMap<T, V>> mapEachTo(V value) {
        return mapEachTo(() -> value);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a hash map of counters.
     *
     * @param <T> The type of the elements of the stream.
     * @return The custom collector.
     * @see #mapEachTo(Object)
     */
    public static <T> Collector<T, ?, HashMap<T, Integer>> toCounterMap() {
        return mapEachTo(0);
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a hash map of atomic counters.
     *
     * @param <T> The type of the elements of the stream.
     * @return The custom collector.
     * @see #mapEachTo(Supplier)
     */
    public static <T> Collector<T, ?, HashMap<T, AtomicInteger>> toAtomicCounterMap() {
        return mapEachTo(() -> new AtomicInteger(0));
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries to a hash map of strings.
     *
     * @return The custom collector.
     * @see #toHashMap(Function, Function)
     */
    public static Collector<Map.Entry<?, ?>, ?, HashMap<String, String>> toStringMap() {
        return toHashMap((e -> String.valueOf(e.getKey())), (e -> String.valueOf(e.getValue())));
    }
    
    /**
     * Creates a new custom collector that collects a stream of map entries into an existing map.
     *
     * @param map The existing map to add to.
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The custom collector.
     * @see #toLinkedHashMap()
     * @see MapUtility#putAllAndGet(Map, Map)
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> addTo(Map<K, V> map) {
        return Collectors.collectingAndThen(
                toLinkedHashMap(),
                collected -> MapUtility.putAllAndGet(map, collected));
    }
    
}
