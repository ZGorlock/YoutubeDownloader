/*
 * File:    BiMap.java
 * Package: commons.object.collection.map
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object.collection.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import commons.object.collection.map.strict.StrictHashMap;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a BiMap.
 *
 * @param <K> The type of the keys of the map.
 * @param <V> The type of the values of the map.
 */
public class BiMap<K, V> extends StrictHashMap<K, V> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(BiMap.class);
    
    
    //Fields
    
    /**
     * The inverse map.
     */
    private final Map<V, K> inverseMap = new StrictHashMap<>();
    
    
    //Constructors
    
    /**
     * The constructor for a BiMap from a map.
     *
     * @param map The map.
     * @see #putAll(Map)
     */
    public BiMap(Map<? extends K, ? extends V> map) {
        this.putAll(map);
    }
    
    /**
     * The default no-argument constructor for a BiMap.
     */
    public BiMap() {
    }
    
    
    //Methods
    
    /**
     * Gets a key from the map.
     *
     * @param value The value.
     * @return The key corresponding to the specified value, or null if the map does not contain the specified value.
     */
    public synchronized K inverseGet(Object value) {
        return inverseMap.get(value);
    }
    
    /**
     * Gets a key from the map.
     *
     * @param value      The value.
     * @param defaultKey The default key.
     * @return The key corresponding to the specified value, or the default key if the map does not contain the specified value.
     * @see #inverseGet(Object)
     */
    public synchronized K inverseGetOrDefault(Object value, K defaultKey) {
        return !containsValue(value) ? defaultKey :
               inverseGet(value);
    }
    
    /**
     * Gets an entry set of the inverse map.<br>
     * This entry set returned is not backed by the map.
     *
     * @return An inverse entry set.
     */
    public synchronized Set<Entry<V, K>> inverseEntrySet() {
        return inverseMap.entrySet();
    }
    
    /**
     * Puts an entry in the map.
     *
     * @param key   The key.
     * @param value The value.
     * @param force Whether or not to replace the key if the map already contains the specified value.
     * @return The previous value of the specified key, or null if the map did not previously contain the specified key.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the specified value associated with a different key.
     * @see HashMap#put(Object, Object)
     */
    public synchronized V put(K key, V value, boolean force) {
        if (containsKey(key) && Objects.equals(get(key), value)) {
            return value;
        }
        if (containsValue(value)) {
            if (force) {
                inverseRemove(value);
            } else {
                throw new IllegalArgumentException(StringUtility.format("The map already contains the value: {}", value));
            }
        }
        
        final V oldValue = super.put(key, value);
        inverseMap.remove(oldValue);
        inverseMap.put(value, key);
        return oldValue;
    }
    
    /**
     * Puts an entry in the map.
     *
     * @param key   The key.
     * @param value The value.
     * @return The previous value of the specified key, or null if the map did not previously contain the specified key.
     * @throws IllegalArgumentException When the map already contains the specified value associated with a different key.
     * @see #put(Object, Object, boolean)
     */
    @Override
    public synchronized V put(K key, V value) {
        return put(key, value, false);
    }
    
    /**
     * Puts an entry in the map.
     *
     * @param value The value.
     * @param key   The key.
     * @param force Whether or not to replace the value if the map already contains the specified key.
     * @return The previous key corresponding to the specified value, or null if the map did not previously contain the specified value.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the specified key corresponding to a different value.
     * @see HashMap#put(Object, Object)
     */
    public synchronized K inversePut(V value, K key, boolean force) {
        if (containsValue(value) && Objects.equals(inverseGet(value), key)) {
            return key;
        }
        if (containsKey(key)) {
            if (force) {
                remove(key);
            } else {
                throw new IllegalArgumentException(StringUtility.format("The map already contains the key: {}", key));
            }
        }
        
        final K oldKey = inverseMap.put(value, key);
        super.remove(oldKey);
        super.put(key, value);
        return oldKey;
    }
    
    /**
     * Puts an entry in the map.
     *
     * @param value The value.
     * @param key   The key.
     * @return The previous key corresponding to the specified value, or null if the map did not previously contain the specified value.
     * @throws IllegalArgumentException When the map already contains the specified key corresponding to a different value.
     * @see #inversePut(Object, Object, boolean)
     */
    public synchronized K inversePut(V value, K key) {
        return inversePut(value, key, false);
    }
    
    /**
     * Puts an entry in the map if the map does not already contain the key.
     *
     * @param key   The key.
     * @param value The value.
     * @param force Whether or not to replace the key if the map already contains the specified value.
     * @return The existing value of the specified key, or null if the map did not previously contain the specified key and it was added.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the specified value associated with a different key.
     * @see #put(Object, Object, boolean)
     */
    public synchronized V putIfAbsent(K key, V value, boolean force) {
        return containsKey(key) ? get(key) :
               put(key, value, force);
    }
    
    /**
     * Puts an entry in the map if the map does not already contain the key.
     *
     * @param key   The key.
     * @param value The value.
     * @return The existing value of the specified key, or null if the map did not previously contain the specified key and it was added.
     * @throws IllegalArgumentException When the map already contains the specified value associated with a different key.
     * @see #putIfAbsent(Object, Object, boolean)
     */
    @Override
    public synchronized V putIfAbsent(K key, V value) {
        return putIfAbsent(key, value, false);
    }
    
    /**
     * Puts an entry in the map if the map does not already contain the value.
     *
     * @param value The value.
     * @param key   The key.
     * @param force Whether or not to replace the value if the map already contains the specified key.
     * @return The existing key corresponding to the specified value, or null if the map did not previously contain the specified value and it was added.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the specified key corresponding to a different value.
     * @see #inversePut(Object, Object, boolean)
     */
    public synchronized K inversePutIfAbsent(V value, K key, boolean force) {
        return containsValue(value) ? inverseGet(value) :
               inversePut(value, key, force);
    }
    
    /**
     * Puts an entry in the map if the map does not already contain the value.
     *
     * @param value The value.
     * @param key   The key.
     * @return The existing key corresponding to the specified value, or null if the map did not previously contain the specified value and it was added.
     * @throws IllegalArgumentException When the map already contains the specified key corresponding to a different value.
     * @see #inversePutIfAbsent(Object, Object, boolean)
     */
    public synchronized K inversePutIfAbsent(V value, K key) {
        return inversePutIfAbsent(value, key, false);
    }
    
    /**
     * Puts a map of entries in the map.<br>
     * Insertion order cannot be guaranteed.
     *
     * @param entries The map of entries.
     * @param force   Whether or not to replace a key if the map already contains its associated value.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains a value associated with a different key.
     * @see #put(Object, Object, boolean)
     */
    public synchronized void putAll(Map<? extends K, ? extends V> entries, boolean force) {
        entries.forEach((key, value) ->
                put(key, value, force));
    }
    
    /**
     * Puts a map of entries in the map.<br>
     * Insertion order cannot be guaranteed.
     *
     * @param entries The map of entries.
     * @throws IllegalArgumentException When the map already contains a value associated with a different key.
     * @see #putAll(Map, boolean)
     */
    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> entries) {
        putAll(entries, false);
    }
    
    /**
     * Puts a map of inverse entries in the map.<br>
     * Insertion order cannot be guaranteed.
     *
     * @param inverseEntries The map of inverse entries.
     * @param force          Whether or not to replace a value if the map already contains its corresponding key.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains a key corresponding to a different value.
     * @see #inversePut(Object, Object, boolean)
     */
    public synchronized void inversePutAll(Map<? extends V, ? extends K> inverseEntries, boolean force) {
        inverseEntries.forEach((value, key) ->
                inversePut(value, key, force));
    }
    
    /**
     * Puts a map of inverse entries in the map.<br>
     * Insertion order cannot be guaranteed.
     *
     * @param inverseEntries The map of inverse entries.
     * @throws IllegalArgumentException When the map already contains a key corresponding to a different value.
     * @see #inversePutAll(Map, boolean)
     */
    public synchronized void inversePutAll(Map<? extends V, ? extends K> inverseEntries) {
        inversePutAll(inverseEntries, false);
    }
    
    /**
     * Replaces a value in the map.
     *
     * @param key   The key.
     * @param value The value.
     * @param force Whether or not to replace the key if the map already contains the specified value.
     * @return The previous value of the specified key, or null if the map did not previously contain the specified key.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the specified value associated with a different key.
     * @see #put(Object, Object, boolean)
     */
    public synchronized V replace(K key, V value, boolean force) {
        return put(key, value, force);
    }
    
    /**
     * Replaces a value in the map.
     *
     * @param key   The key.
     * @param value The value.
     * @return The previous value of the specified key, or null if the map did not previously contain the specified key.
     * @throws IllegalArgumentException When the specified value associated with a different key.
     * @see #replace(Object, Object, boolean)
     */
    @Override
    public synchronized V replace(K key, V value) {
        return replace(key, value, false);
    }
    
    /**
     * Replaces a value in the map.
     *
     * @param key      The key.
     * @param oldValue The expected previous value.
     * @param newValue The new value.
     * @param force    Whether or not to replace the key if the map already contains the specified new value.
     * @return Whether or not the previous value matched the expected value and was replaced.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the specified new value associated with a different key.
     * @see #replace(Object, Object, boolean)
     */
    public synchronized boolean replace(K key, V oldValue, V newValue, boolean force) {
        return Objects.equals(get(key), oldValue) &&
                Objects.equals(oldValue, replace(key, newValue, force));
    }
    
    /**
     * Replaces a value in the map.
     *
     * @param key      The key.
     * @param oldValue The expected previous value.
     * @param newValue The new value.
     * @return Whether or not the previous value matched the expected value and was replaced.
     * @throws IllegalArgumentException When the map already contains the specified new value associated with a different key.
     * @see #replace(Object, Object, Object, boolean)
     */
    @Override
    public synchronized boolean replace(K key, V oldValue, V newValue) {
        return replace(key, oldValue, newValue, false);
    }
    
    /**
     * Replaces a key in the map.
     *
     * @param value The value.
     * @param key   The key.
     * @param force Whether or not to replace the value if the map already contains the associated key.
     * @return The previous key corresponding to the specified value, or null if the map did not previously contain the specified value.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the specified key corresponding to a different value.
     * @see #inversePut(Object, Object, boolean)
     */
    public synchronized K inverseReplace(V value, K key, boolean force) {
        return inversePut(value, key, force);
    }
    
    /**
     * Replaces a key in the map.
     *
     * @param value The value.
     * @param key   The key.
     * @return The previous key corresponding to the specified value, or null if the map did not previously contain the specified value.
     * @throws IllegalArgumentException When the map already contains the specified key corresponding to a different value.
     * @see #inverseReplace(Object, Object, boolean)
     */
    public synchronized K inverseReplace(V value, K key) {
        return inverseReplace(value, key, false);
    }
    
    /**
     * Replaces a key in the map.
     *
     * @param value  The value.
     * @param oldKey The expected previous key.
     * @param newKey The new key.
     * @param force  Whether or not to replace the value if the map already contains the specified new key.
     * @return Whether or not the previous key matched the expected key and was replaced.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the specified new key corresponding to a different value.
     * @see #inverseReplace(Object, Object, boolean)
     */
    public synchronized boolean inverseReplace(V value, K oldKey, K newKey, boolean force) {
        return Objects.equals(inverseGet(value), oldKey) &&
                Objects.equals(oldKey, inverseReplace(value, newKey, force));
    }
    
    /**
     * Replaces a key in the map.
     *
     * @param value  The value.
     * @param oldKey The expected previous key.
     * @param newKey The new key.
     * @return Whether or not the previous key matched the expected key and was replaced.
     * @throws IllegalArgumentException When the map already contains the specified new key corresponding to a different value.
     * @see #inverseReplace(Object, Object, Object, boolean)
     */
    public synchronized boolean inverseReplace(V value, K oldKey, K newKey) {
        return inverseReplace(value, oldKey, newKey, false);
    }
    
    /**
     * Replaces all values in the map.<br>
     * Replacement order cannot be guaranteed.
     *
     * @param function The function that produces a new value given the key and the old value.
     * @param force    Whether or not to replace the associated key if the map already contains a computed new value.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains a computed new value associated with a different key.
     * @see #replace(Object, Object, boolean)
     */
    public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function, boolean force) {
        immutableEntrySet().forEach(entry ->
                replace(entry.getKey(), function.apply(entry.getKey(), entry.getValue()), force));
    }
    
    /**
     * Replaces all values in the map.<br>
     * Replacement order cannot be guaranteed.
     *
     * @param function The function that produces a new value given the key and the old value.
     * @throws IllegalArgumentException When the map already contains a computed new value associated with a different key.
     * @see #replaceAll(BiFunction, boolean)
     */
    @Override
    public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        replaceAll(function, false);
    }
    
    /**
     * Replaces all keys in the map.<br>
     * Replacement order cannot be guaranteed.
     *
     * @param function The function that produces a new key given the value and the old key.
     * @param force    Whether or not to replace the corresponding value if the map already contains a computed new key.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains a computed new key corresponding to a different value.
     * @see #inverseReplace(Object, Object, boolean)
     */
    public synchronized void inverseReplaceAll(BiFunction<? super V, ? super K, ? extends K> function, boolean force) {
        immutableEntrySet().forEach(entry ->
                inverseReplace(entry.getValue(), function.apply(entry.getValue(), entry.getKey()), force));
    }
    
    /**
     * Replaces all keys in the map.<br>
     * Replacement order cannot be guaranteed.
     *
     * @param function The function that produces a new key given the value and the old key.
     * @throws IllegalArgumentException When the map already contains a computed new key corresponding to a different value.
     * @see #inverseReplaceAll(BiFunction, boolean)
     */
    public synchronized void inverseReplaceAll(BiFunction<? super V, ? super K, ? extends K> function) {
        inverseReplaceAll(function, false);
    }
    
    /**
     * Removes an entry from the map.
     *
     * @param key The key to remove.
     * @return The previous value of the specified key, or null if the map did not contain the specified key.
     * @see HashMap#remove(Object)
     */
    @Override
    public synchronized V remove(Object key) {
        inverseMap.remove(get(key));
        return super.remove(key);
    }
    
    /**
     * Removes an entry from the map.
     *
     * @param key   The key to remove.
     * @param value The expected value.
     * @return Whether or not the map contained the specified key and value and the previous value matched the expected value and was removed.
     * @see #remove(Object)
     */
    @Override
    public synchronized boolean remove(Object key, Object value) {
        return containsKey(key) && containsValue(value) && Objects.equals(get(key), value) &&
                Objects.equals(value, remove(key));
    }
    
    /**
     * Removes an entry from the map.
     *
     * @param value The value to remove.
     * @return The previous key corresponding to the specified key, or null if the map did not contain the specified value.
     * @see HashMap#remove(Object)
     */
    public synchronized K inverseRemove(Object value) {
        super.remove(inverseGet(value));
        return inverseMap.remove(value);
    }
    
    /**
     * Removes an entry from the map.
     *
     * @param value The value to remove.
     * @param key   The expected key.
     * @return Whether or not the map contained the specified value and key and the previous key matched the expected key and was removed.
     * @see #inverseRemove(Object)
     */
    public synchronized boolean inverseRemove(Object value, Object key) {
        return containsValue(value) && containsKey(key) && Objects.equals(inverseGet(value), key) &&
                Objects.equals(key, inverseRemove(value));
    }
    
    /**
     * Clears the map.
     *
     * @see HashMap#clear()
     */
    @Override
    public synchronized void clear() {
        inverseMap.clear();
        super.clear();
    }
    
    /**
     * Clones the map.
     *
     * @return A clone of the map.
     * @see #BiMap(Map)
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public BiMap<K, V> clone() {
        return new BiMap<>(this);
    }
    
    /**
     * Computes a value of the map.
     *
     * @param key               The key to compute the value for.
     * @param remappingFunction The function that produces the computed value given the key and the previous value; will remove the entry from the map if the function produces null.
     * @param force             Whether or not to replace the associated key if the map already contains the computed new value.
     * @return The previous value of the specified key.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the computed value associated with a different key.
     * @see #put(Object, Object, boolean)
     * @see #replace(Object, Object, boolean)
     * @see #remove(Object)
     */
    public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, boolean force) {
        final V value = remappingFunction.apply(key, get(key));
        return (value == null) ? remove(key) :
               containsKey(key) ? replace(key, value, force) :
               put(key, value, force);
    }
    
    /**
     * Computes a value of the map.
     *
     * @param key               The key to compute the value for.
     * @param remappingFunction The function that produces the computed value given the key and the previous value; will remove the entry from the map if the function produces null.
     * @return The previous value of the specified key.
     * @throws IllegalArgumentException When the map already contains the computed value associated with a different key.
     * @see #compute(Object, BiFunction, boolean)
     */
    @Override
    public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return compute(key, remappingFunction, false);
    }
    
    /**
     * Computes a key of the map.
     *
     * @param value             The value to compute the key for.
     * @param remappingFunction The function that produces the computed key given the value and the previous key; will remove the entry from the map if the function produces null.
     * @param force             Whether or not to replace the corresponding value if the map already contains the computed key.
     * @return The previous key corresponding to the specified value.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the computed key corresponding to a different value.
     * @see #inversePut(Object, Object, boolean)
     * @see #inverseReplace(Object, Object, boolean)
     * @see #inverseRemove(Object)
     */
    public synchronized K inverseCompute(V value, BiFunction<? super V, ? super K, ? extends K> remappingFunction, boolean force) {
        final K key = remappingFunction.apply(value, inverseGet(value));
        return (key == null) ? inverseRemove(value) :
               containsValue(value) ? inverseReplace(value, key, force) :
               inversePut(value, key, force);
    }
    
    /**
     * Computes a key of the map.
     *
     * @param value             The value to compute the key for.
     * @param remappingFunction The function that produces the computed key given the value and the previous key; will remove the entry from the map if the function produces null.
     * @return The previous key corresponding to the specified value.
     * @throws IllegalArgumentException When the map already contains the computed key corresponding to a different value.
     * @see #inverseCompute(Object, BiFunction, boolean)
     */
    public synchronized K inverseCompute(V value, BiFunction<? super V, ? super K, ? extends K> remappingFunction) {
        return inverseCompute(value, remappingFunction, false);
    }
    
    /**
     * Computes a value of the map if the map does not already contain the key.
     *
     * @param key             The key to compute the value for.
     * @param mappingFunction The function that produces the computed value given the key.
     * @param force           Whether or not to replace the associated key if the map already contains the computed value.
     * @return The existing value of the specified key, or null if the map did not previously contain the specified key and it was added.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the computed value associated with a different key.
     * @see #compute(Object, BiFunction, boolean)
     */
    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, boolean force) {
        return containsKey(key) ? get(key) :
               compute(key, (k, v) -> mappingFunction.apply(k), force);
    }
    
    /**
     * Computes a value of the map if the map does not already contain the key.
     *
     * @param key             The key to compute the value for.
     * @param mappingFunction The function that produces the computed value given the key.
     * @return The existing value of the specified key, or null if the map did not previously contain the specified key and it was added.
     * @throws IllegalArgumentException When the map already contains the computed value associated with a different key.
     * @see #computeIfAbsent(Object, Function, boolean)
     */
    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return computeIfAbsent(key, mappingFunction, false);
    }
    
    /**
     * Computes a key of the map if the map does not already contain the value.
     *
     * @param value           The value to compute the key for.
     * @param mappingFunction The function that produces the computed key given the value.
     * @param force           Whether or not to replace the corresponding value if the map already contains the computed key.
     * @return The existing key corresponding to the specified value, or null if the map did not previously contain the specified value and it was added.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the computed key corresponding to a different value.
     * @see #inverseCompute(Object, BiFunction, boolean)
     */
    public synchronized K inverseComputeIfAbsent(V value, Function<? super V, ? extends K> mappingFunction, boolean force) {
        return containsValue(value) ? inverseGet(value) :
               inverseCompute(value, (v, k) -> mappingFunction.apply(v), force);
    }
    
    /**
     * Computes a key of the map if the map does not already contain the value.
     *
     * @param value           The value to compute the key for.
     * @param mappingFunction The function that produces the computed key given the value.
     * @return The existing key corresponding to the specified value, or null if the map did not previously contain the specified value and it was added.
     * @throws IllegalArgumentException When the map already contains the computed key corresponding to a different value.
     * @see #inverseComputeIfAbsent(Object, Function, boolean)
     */
    public synchronized K inverseComputeIfAbsent(V value, Function<? super V, ? extends K> mappingFunction) {
        return inverseComputeIfAbsent(value, mappingFunction, false);
    }
    
    /**
     * Computes a value of the map if the map already contains the key.
     *
     * @param key               The key to compute the value for.
     * @param remappingFunction The function that produces the computed value given the key and the previous value; will remove the entry from the map if the function produces null.
     * @param force             Whether or not to replace the associated key if the map already contains the computed value.
     * @return The previous value of the specified key.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the computed value associated with a different key.
     * @see #compute(Object, BiFunction)
     */
    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, boolean force) {
        return !containsKey(key) ? null :
               compute(key, remappingFunction, force);
    }
    
    /**
     * Computes a value of the map if the map already contains the key.
     *
     * @param key               The key to compute the value for.
     * @param remappingFunction The function that produces the computed value given the key and the previous value; will remove the entry from the map if the function produces null.
     * @return The previous value of the specified key.
     * @throws IllegalArgumentException When the map already contains the computed value associated with a different key.
     * @see #computeIfPresent(Object, BiFunction, boolean)
     */
    @Override
    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return computeIfPresent(key, remappingFunction, false);
    }
    
    /**
     * Computes a key of the map if the map already contains the value.
     *
     * @param value             The value to compute the key for.
     * @param remappingFunction The function that produces the computed key given the value and the previous key; will remove the entry from the map if the function produces null.
     * @param force             Whether or not to replace the corresponding value if the map already contains the computed key.
     * @return The previous key corresponding to the specified value.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the computed key corresponding to a different value.
     * @see #inverseCompute(Object, BiFunction, boolean)
     */
    public synchronized K inverseComputeIfPresent(V value, BiFunction<? super V, ? super K, ? extends K> remappingFunction, boolean force) {
        return !containsValue(value) ? null :
               inverseCompute(value, remappingFunction, force);
    }
    
    /**
     * Computes a key of the map if the map already contains the value.
     *
     * @param value             The value to compute the key for.
     * @param remappingFunction The function that produces the computed key given the value and the previous key; will remove the entry from the map if the function produces null.
     * @return The previous key corresponding to the specified value.
     * @throws IllegalArgumentException When the map already contains the computed key corresponding to a different value.
     * @see #inverseComputeIfPresent(Object, BiFunction, boolean)
     */
    public synchronized K inverseComputeIfPresent(V value, BiFunction<? super V, ? super K, ? extends K> remappingFunction) {
        return inverseComputeIfPresent(value, remappingFunction, false);
    }
    
    /**
     * Merges a value of the map.
     *
     * @param key               The key to merge the value for.
     * @param value             The value to merge with the existing value.
     * @param remappingFunction The function that produces the merged value given the previous value and the value to be merged; will remove the entry from the map if the function produces null.
     * @param force             Whether or not to replace the associated key if the map already contains the merged value.
     * @return The previous value of the specified key, or null if the map did not previously contain the specified key.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the merged value associated with a different key.
     * @see #compute(Object, BiFunction, boolean)
     */
    public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction, boolean force) {
        return compute(key, (k, v) -> (containsKey(k) ? remappingFunction.apply(v, value) : value), force);
    }
    
    /**
     * Merges a value of the map.
     *
     * @param key               The key to merge the value for.
     * @param value             The value to merge with the existing value.
     * @param remappingFunction The function that produces the merged value given the previous value and the value to be merged; will remove the entry from the map if the function produces null.
     * @return The previous value of the specified key, or null if the map did not previously contain the specified key.
     * @throws IllegalArgumentException When the map already contains the merged value associated with a different key.
     * @see #merge(Object, Object, BiFunction, boolean)
     */
    @Override
    public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return merge(key, value, remappingFunction, false);
    }
    
    /**
     * Merges a key of the map.
     *
     * @param value             The value to merge the key for.
     * @param key               The key to merge with the existing key.
     * @param remappingFunction The function that produces the merged key given the previous key and the key to be merged; will remove the entry from the map if the function produces null.
     * @param force             Whether or not to replace the corresponding value if the map already contains the merged key.
     * @return The previous key corresponding to the specified value, or null if the map did not previously contain the specified value.
     * @throws IllegalArgumentException When performing a non-force operation and the map already contains the merged key corresponding with a different value.
     * @see #inverseCompute(Object, BiFunction, boolean)
     */
    public synchronized K inverseMerge(V value, K key, BiFunction<? super K, ? super K, ? extends K> remappingFunction, boolean force) {
        return inverseCompute(value, (v, k) -> (containsValue(v) ? remappingFunction.apply(k, key) : key), force);
    }
    
    /**
     * Merges a key of the map.
     *
     * @param value             The value to merge the key for.
     * @param key               The key to merge with the existing key.
     * @param remappingFunction The function that produces the merged key given the previous key and the key to be merged; will remove the entry from the map if the function produces null.
     * @return The previous key corresponding to the specified value, or null if the map did not previously contain the specified value.
     * @throws IllegalArgumentException When the map already contains the merged key corresponding with a different value.
     * @see #inverseMerge(Object, Object, BiFunction, boolean)
     */
    public synchronized K inverseMerge(V value, K key, BiFunction<? super K, ? super K, ? extends K> remappingFunction) {
        return inverseMerge(value, key, remappingFunction, false);
    }
    
    /**
     * Performs an action on each value key pair of the inverse map.
     *
     * @param action The action to perform.
     */
    public synchronized void inverseForEach(BiConsumer<? super V, ? super K> action) {
        inverseMap.forEach(action);
    }
    
}
