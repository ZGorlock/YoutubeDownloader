/*
 * File:    MapUtility.java
 * Package: commons.object.collection
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import commons.lambda.stream.collector.MapCollectors;
import commons.lambda.stream.mapper.Mappers;
import commons.object.string.EntityStringUtility;
import commons.object.string.StringUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional map functionality.
 */
public final class MapUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(MapUtility.class);
    
    
    //Constants
    
    /**
     * The default map class to use when one is not specified.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Class<? extends Map<?, ?>> DEFAULT_MAP_CLASS = (Class) HashMap.class;
    
    
    //Static Methods
    
    /**
     * Creates a new map instance of a certain class.
     *
     * @param clazz The class of the map.
     * @param <K>   The type of the keys of the map.
     * @param <V>   The type of the values of the map.
     * @param <M>   The class of the map.
     * @return The map instance.
     */
    @SuppressWarnings("SortedCollectionWithNonComparableKeys")
    public static <K, V, M extends Map<K, V>> Map<K, V> emptyMap(Class<M> clazz) {
        switch (EntityStringUtility.simpleClassString(Objects.requireNonNull(clazz))) {
            case "HashMap":
                return new HashMap<>();
            case "LinkedHashMap":
                return new LinkedHashMap<>();
            case "TreeMap":
                return new TreeMap<>();
            default:
                return emptyMap();
        }
    }
    
    /**
     * Creates a new map instance.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The map instance.
     * @see #emptyMap(Class)
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> emptyMap() {
        return emptyMap((Class<Map<K, V>>) DEFAULT_MAP_CLASS);
    }
    
    /**
     * Creates a new unmodifiable map from an existing map.
     *
     * @param map The map.
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @param <M> The class of the map.
     * @return The unmodifiable map.
     * @see Collections#unmodifiableMap(Map)
     */
    public static <K, V, M extends Map<K, V>> Map<K, V> unmodifiableMap(Map<K, V> map) {
        return Collections.unmodifiableMap(map);
    }
    
    /**
     * Creates a new unmodifiable map instance of a certain class.
     *
     * @param clazz The class of the map.
     * @param <K>   The type of the keys of the map.
     * @param <V>   The type of the values of the map.
     * @param <M>   The class of the map.
     * @return The unmodifiable map instance.
     * @see #emptyMap(Class)
     * @see #unmodifiableMap(Map)
     */
    public static <K, V, M extends Map<K, V>> Map<K, V> unmodifiableMap(Class<M> clazz) {
        return unmodifiableMap(emptyMap(clazz));
    }
    
    /**
     * Creates a new unmodifiable map instance.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The unmodifiable map instance.
     * @see #unmodifiableMap(Class)
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> unmodifiableMap() {
        return unmodifiableMap((Class<Map<K, V>>) DEFAULT_MAP_CLASS);
    }
    
    /**
     * Creates a new synchronized map from an existing map.
     *
     * @param map The map.
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @param <M> The class of the map.
     * @return The synchronized map.
     * @see Collections#unmodifiableMap(Map)
     */
    public static <K, V, M extends Map<K, V>> Map<K, V> synchronizedMap(Map<K, V> map) {
        return Collections.synchronizedMap(map);
    }
    
    /**
     * Creates a new synchronized map instance of a certain class.
     *
     * @param clazz The class of the map.
     * @param <K>   The type of the keys of the map.
     * @param <V>   The type of the values of the map.
     * @param <M>   The class of the map.
     * @return The synchronized map instance.
     * @see #emptyMap(Class)
     * @see #synchronizedMap(Map)
     */
    public static <K, V, M extends Map<K, V>> Map<K, V> synchronizedMap(Class<M> clazz) {
        return synchronizedMap(emptyMap(clazz));
    }
    
    /**
     * Creates a new synchronized map instance.
     *
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The synchronized map instance.
     * @see #synchronizedMap(Class)
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> synchronizedMap() {
        return synchronizedMap((Class<Map<K, V>>) DEFAULT_MAP_CLASS);
    }
    
    /**
     * Creates a new map of a certain class, key type, and value type.
     *
     * @param clazz     The class of the map.
     * @param keyType   The type of the keys of the map.
     * @param valueType The type of the values of the map.
     * @param <K>       The type of the keys of the map.
     * @param <V>       The type of the values of the map.
     * @param <M>       The class of the map.
     * @return The created map.
     * @throws ClassCastException When attempting to create a TreeMap with a key type that is not Comparable.
     * @see #emptyMap(Class)
     */
    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<?, ?>> Map<K, V> create(Class<M> clazz, Class<K> keyType, Class<V> valueType) throws ClassCastException {
        if (clazz.equals(TreeMap.class) && !ArrayUtility.contains(keyType.getInterfaces(), Comparable.class)) {
            throw new ClassCastException("class " + EntityStringUtility.classString(keyType) + " cannot be cast to class " + EntityStringUtility.classString(Comparable.class));
        }
        
        return emptyMap((Class<Map<K, V>>) clazz);
    }
    
    /**
     * Creates a new map of a certain key type and value type.
     *
     * @param keyType   The type of the keys of the map.
     * @param valueType The type of the values of the map.
     * @param <K>       The type of the keys of the map.
     * @param <V>       The type of the values of the map.
     * @return The created map.
     * @throws ClassCastException When attempting to create a TreeMap with a key type that is not Comparable.
     * @see #create(Class, Class, Class)
     */
    public static <K, V> Map<K, V> create(Class<K> keyType, Class<V> valueType) throws ClassCastException {
        return create(DEFAULT_MAP_CLASS, keyType, valueType);
    }
    
    /**
     * Creates and populates a new map of a certain class.
     *
     * @param clazz   The class of the map.
     * @param entries The entries to populate the map with.
     * @param <K>     The type of the keys of the map.
     * @param <V>     The type of the values of the map.
     * @param <M>     The class of the map.
     * @return The created and populated map.
     * @throws ClassCastException When attempting to create a TreeMap with a key type that is not Comparable.
     * @see MapCollectors#toMap(Class)
     */
    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<?, ?>> Map<K, V> mapOf(Class<M> clazz, Pair<K, V>... entries) throws ClassCastException {
        return Arrays.stream(entries).collect(MapCollectors.toMap(clazz));
    }
    
    /**
     * Creates and populates a new map of a certain class.
     *
     * @param clazz   The class of the map.
     * @param entries The entries to populate the map with.
     * @param <K>     The type of the keys of the map.
     * @param <V>     The type of the values of the map.
     * @param <M>     The class of the map.
     * @return The created and populated map.
     * @throws ClassCastException When attempting to create a TreeMap with a key type that is not Comparable.
     * @see #mapOf(Class, Pair[])
     */
    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<?, ?>> Map<K, V> mapOf(Class<M> clazz, Collection<Pair<K, V>> entries) throws ClassCastException {
        return mapOf(clazz, entries.toArray(Pair[]::new));
    }
    
    /**
     * Creates and populates a new map of a certain class.
     *
     * @param clazz  The class of the map.
     * @param keys   The keys to populate the map with.
     * @param values The values to populate the map with.
     * @param <K>    The type of the keys of the map.
     * @param <V>    The type of the values of the map.
     * @param <M>    The class of the map.
     * @return The created and populated map.
     * @throws IndexOutOfBoundsException When the array of keys and the array of values are not the same length.
     * @throws ClassCastException        When attempting to create a TreeMap with a key type that is not Comparable.
     * @see #mapOf(Class, Pair[])
     */
    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<?, ?>> Map<K, V> mapOf(Class<M> clazz, K[] keys, V[] values) throws IndexOutOfBoundsException, ClassCastException {
        if (keys.length != values.length) {
            throw new IndexOutOfBoundsException();
        }
        
        return mapOf(clazz, IntStream.range(0, keys.length).boxed().map(i -> new ImmutablePair<>(keys[i], values[i])).toArray(Pair[]::new));
    }
    
    /**
     * Creates and populates a new map of a certain class.
     *
     * @param clazz  The class of the map.
     * @param keys   The keys to populate the map with.
     * @param values The values to populate the map with.
     * @param <K>    The type of the keys of the map.
     * @param <V>    The type of the values of the map.
     * @param <M>    The class of the map.
     * @return The created and populated map.
     * @throws IndexOutOfBoundsException When the list of keys and the list of values are not the same size.
     * @throws ClassCastException        When attempting to create a TreeMap with a key type that is not Comparable.
     * @see #mapOf(Class, Collection)
     */
    public static <K, V, M extends Map<?, ?>> Map<K, V> mapOf(Class<M> clazz, List<K> keys, List<V> values) throws IndexOutOfBoundsException, ClassCastException {
        if (keys.size() != values.size()) {
            throw new IndexOutOfBoundsException();
        }
        
        return mapOf(clazz, IntStream.range(0, keys.size()).boxed().map(i -> new ImmutablePair<>(keys.get(i), values.get(i))).collect(Collectors.toList()));
    }
    
    /**
     * Creates and populates a new map.
     *
     * @param entries The entries to populate the map with.
     * @param <K>     The type of the keys of the map.
     * @param <V>     The type of the values of the map.
     * @return The created and populated map.
     * @see #mapOf(Class, Pair[])
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(Pair<K, V>... entries) {
        return mapOf(DEFAULT_MAP_CLASS, entries);
    }
    
    /**
     * Creates and populates a new map.
     *
     * @param entries The entries to populate the map with.
     * @param <K>     The type of the keys of the map.
     * @param <V>     The type of the values of the map.
     * @return The created and populated map.
     * @see #mapOf(Class, Collection)
     */
    public static <K, V> Map<K, V> mapOf(Collection<Pair<K, V>> entries) {
        return mapOf(DEFAULT_MAP_CLASS, entries);
    }
    
    /**
     * Creates and populates a new map.
     *
     * @param keys   The keys to populate the map with.
     * @param values The values to populate the map with.
     * @param <K>    The type of the keys of the map.
     * @param <V>    The type of the values of the map.
     * @return The created and populated map.
     * @throws IndexOutOfBoundsException When the array of keys and the array of values are not the same length.
     * @see #mapOf(Class, Object[], Object[])
     */
    public static <K, V> Map<K, V> mapOf(K[] keys, V[] values) throws IndexOutOfBoundsException {
        return mapOf(DEFAULT_MAP_CLASS, keys, values);
    }
    
    /**
     * Creates and populates a new map.
     *
     * @param keys   The keys to populate the map with.
     * @param values The values to populate the map with.
     * @param <K>    The type of the keys of the map.
     * @param <V>    The type of the values of the map.
     * @return The created and populated map.
     * @throws IndexOutOfBoundsException When the list of keys and the list of values are not the same size.
     * @see #mapOf(Class, List, List)
     */
    public static <K, V> Map<K, V> mapOf(List<K> keys, List<V> values) throws IndexOutOfBoundsException {
        return mapOf(DEFAULT_MAP_CLASS, keys, values);
    }
    
    /**
     * Clones a map.
     *
     * @param map The map.
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @param <M> The class of the map.
     * @return The clone of the map.
     */
    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<K, V>> M clone(M map) {
        return (M) putAllAndGet(emptyMap((Class<M>) map.getClass()), map);
    }
    
    /**
     * Casts a map to a map of a specific class.
     *
     * @param map   The map.
     * @param clazz The map class to cast to.
     * @param <K>   The type of the keys of the map.
     * @param <V>   The type of the values of the map.
     * @param <M>   The map class to cast to.
     * @return The casted map, or the same map if the class is the same as specified.
     * @throws ClassCastException When attempting to cast to a TreeMap with a key type that is not Comparable.
     */
    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<?, ?>> Map<K, V> cast(Map<K, V> map, Class<M> clazz) throws ClassCastException {
        return map.getClass().equals(clazz) ? map :
               putAllAndGet(emptyMap((Class<Map<K, V>>) clazz), map);
    }
    
    /**
     * Merges two maps.
     *
     * @param map1 The first map.
     * @param map2 The second map.
     * @param <K>  The type of the keys of the maps.
     * @param <V>  The type of the values of the maps.
     * @param <M>  The class of the map.
     * @return The merged map.
     */
    public static <K, V, M extends Map<K, V>> M merge(M map1, Map<? extends K, ? extends V> map2) {
        return putAllAndGet(clone(map1), map2);
    }
    
    /**
     * Determines if a map is null or empty.
     *
     * @param map The map.
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return Whether the map is null or empty.
     */
    public static <K, V> boolean isNullOrEmpty(Map<K, V> map) {
        return (map == null) || map.isEmpty();
    }
    
    /**
     * Determines if a map equals another map.
     *
     * @param map1 The first map.
     * @param map2 The second map.
     * @return Whether the maps are equal or not.
     */
    public static boolean equals(Map<?, ?> map1, Map<?, ?> map2) {
        return ((map1 == null) || (map2 == null)) ? ((map1 == null) && (map2 == null)) : ((map1.size() == map2.size()) &&
                map1.keySet().stream().allMatch(e -> map2.containsKey(e) && Objects.equals(map1.get(e), map2.get(e))));
    }
    
    /**
     * Performs an operation on a map and returns the map.
     *
     * @param map       The map.
     * @param operation The operation to perform.
     * @param <K>       The type of the keys of the map.
     * @param <V>       The type of the values of the map.
     * @param <M>       The class of the map.
     * @return The same map.
     * @see Mappers#perform(Object, Consumer)
     */
    public static <K, V, M extends Map<K, V>> M doAndGet(M map, Consumer<M> operation) {
        return Mappers.perform(map, operation);
    }
    
    /**
     * Puts an entry in a map and returns the map.
     *
     * @param map   The map.
     * @param key   The key.
     * @param value The value.
     * @param <K>   The type of the keys of the map.
     * @param <V>   The type of the values of the map.
     * @param <M>   The class of the map.
     * @return The same map.
     * @see #doAndGet(Map, Consumer)
     */
    public static <K, V, M extends Map<K, V>> M putAndGet(M map, K key, V value) {
        return doAndGet(map, (m -> m.put(key, value)));
    }
    
    /**
     * Puts a map of entries in a map and returns the map.
     *
     * @param map     The map.
     * @param entries The map of entries.
     * @param <K>     The type of the keys of the map.
     * @param <V>     The type of the values of the map.
     * @param <M>     The class of the map.
     * @return The same map.
     * @see #doAndGet(Map, Consumer)
     */
    public static <K, V, M extends Map<K, V>> M putAllAndGet(M map, Map<? extends K, ? extends V> entries) {
        return doAndGet(map, (m -> m.putAll(entries)));
    }
    
    /**
     * Puts an entry in a map if it is absent and returns the map.
     *
     * @param map   The map.
     * @param key   The key.
     * @param value The value.
     * @param <K>   The type of the keys of the map.
     * @param <V>   The type of the values of the map.
     * @param <M>   The class of the map.
     * @return The same map.
     * @see #doAndGet(Map, Consumer)
     */
    public static <K, V, M extends Map<K, V>> M putIfAbsentAndGet(M map, K key, V value) {
        return doAndGet(map, (m -> m.putIfAbsent(key, value)));
    }
    
    /**
     * Replaces the value of an entry in a map and returns the map.
     *
     * @param map   The map.
     * @param key   The key.
     * @param value The value.
     * @param <K>   The type of the keys of the map.
     * @param <V>   The type of the values of the map.
     * @param <M>   The class of the map.
     * @return The same map.
     * @see #doAndGet(Map, Consumer)
     */
    public static <K, V, M extends Map<K, V>> M replaceAndGet(M map, K key, V value) {
        return doAndGet(map, (m -> m.replace(key, value)));
    }
    
    /**
     * Removes a key from a map and returns the map.
     *
     * @param map The map.
     * @param key The key.
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @param <M> The class of the map.
     * @return The same map.
     * @see #doAndGet(Map, Consumer)
     */
    public static <K, V, M extends Map<K, V>> M removeAndGet(M map, K key) {
        return doAndGet(map, (m -> m.remove(key)));
    }
    
    /**
     * Clears a map and returns the map.
     *
     * @param map The map.
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @param <M> The class of the map.
     * @return The same map.
     * @see #doAndGet(Map, Consumer)
     */
    public static <K, V, M extends Map<K, V>> M clearAndGet(M map) {
        return doAndGet(map, Map::clear);
    }
    
    /**
     * Determines if a key exists in a map.
     *
     * @param map The map.
     * @param key The key.
     * @param <K> The type of the keys of the map.
     * @return Whether the map contains the specified key or not.
     */
    public static <K> boolean contains(Map<? extends K, ?> map, K key) {
        return !isNullOrEmpty(map) && map.containsKey(key);
    }
    
    /**
     * Determines if a string key exists in a map, regardless of case.
     *
     * @param map The map.
     * @param key The key.
     * @return Whether the map contains the specified string key or not, regardless of case.
     */
    public static boolean containsIgnoreCase(Map<String, ?> map, String key) {
        return !isNullOrEmpty(map) && map.keySet().stream().anyMatch(e -> StringUtility.equalsIgnoreCase(e, key));
    }
    
    /**
     * Returns a value from a map with a specified key, or a default value if the key is not present.
     *
     * @param map          The map.
     * @param key          The key.
     * @param defaultValue The default value.
     * @param <K>          The type of the keys of the map.
     * @param <V>          The type of the values of the map.
     * @return The value in the map with the specified key, or the default value if the key is not present.
     */
    public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        return isNullOrEmpty(map) ? defaultValue :
               map.getOrDefault(key, defaultValue);
    }
    
    /**
     * Returns a value from a map with a specified string key, regardless of case, or a default value if the key is not present.
     *
     * @param map          The map.
     * @param key          The key.
     * @param defaultValue The default value.
     * @param <V>          The type of the values of the map.
     * @return The value in the map with the specified string key, regardless of case, or the default value if the key is not present.
     */
    public static <V> V getOrDefaultIgnoreCase(Map<String, V> map, String key, V defaultValue) {
        return isNullOrEmpty(map) ? defaultValue :
               map.keySet().stream().filter(e -> StringUtility.equalsIgnoreCase(e, key)).findFirst().map(map::get).orElse(defaultValue);
    }
    
    /**
     * Returns a value from a map with a specified key, or null if the key is not present.
     *
     * @param map The map.
     * @param key The key.
     * @param <K> The type of the keys of the map.
     * @param <V> The type of the values of the map.
     * @return The value in the map with the specified key, or null if the key is not present.
     * @see #getOrDefault(Map, Object, Object)
     */
    public static <K, V> V getOrNull(Map<K, V> map, K key) {
        return getOrDefault(map, key, null);
    }
    
}
