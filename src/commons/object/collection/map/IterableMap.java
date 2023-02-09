/*
 * File:    IterableMap.java
 * Package: commons.object.collection.map
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object.collection.map;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import commons.lambda.stream.collector.ListCollectors;
import commons.math.number.BoundUtility;
import commons.object.collection.ListUtility;
import commons.object.collection.MapUtility;
import commons.object.collection.iterator.CustomIterator;
import commons.object.collection.map.strict.StrictHashMap;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines an Iterable Map.
 *
 * @param <K> The type of the keys of the map.
 * @param <V> The type of the values of the map.
 */
public class IterableMap<K, V> extends StrictHashMap<K, V> implements Iterable<Map.Entry<K, V>> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(IterableMap.class);
    
    
    //Fields
    
    /**
     * The ordered list of keys of the Iterable Map.
     */
    private final List<K> keyList = new ArrayList<>();
    
    
    //Constructors
    
    /**
     * The constructor for an Iterable Map from a map.
     *
     * @param map The map.
     * @see #putAll(Map)
     */
    public IterableMap(Map<? extends K, ? extends V> map) {
        this.putAll(map);
    }
    
    /**
     * The constructor for an Iterable Map from another Iterable Map.
     *
     * @param map The Iterable Map.
     * @see #iterator()
     * @see #put(Object, Object)
     */
    public IterableMap(IterableMap<? extends K, ? extends V> map) {
        map.iterator().forEachRemaining(entry ->
                this.put(entry.getKey(), entry.getValue()));
    }
    
    /**
     * The default no-argument constructor for an Iterable Map.
     */
    public IterableMap() {
    }
    
    
    //Methods
    
    /**
     * Gets a key from the map.
     *
     * @param index The index.
     * @return The key at the specified index.
     * @throws IndexOutOfBoundsException When the map does not contain a key at the specified index.
     */
    public synchronized K getKey(int index) {
        if (!BoundUtility.inListBounds(index, keyList)) {
            throw new IndexOutOfBoundsException(StringUtility.format("Index {} out of bounds for length {}", index, size()));
        }
        return keyList.get(index);
    }
    
    /**
     * Gets a value from the map.
     *
     * @param index The index.
     * @return The value at the specified index.
     * @throws IndexOutOfBoundsException When the map does not contain a value at the specified index.
     * @see HashMap#get(Object)
     */
    public synchronized V get(int index) {
        return super.get(getKey(index));
    }
    
    /**
     * Gets an entry from the map.<br>
     * The entry returned is not backed by the map.
     *
     * @param index The index.
     * @return The entry at the specified index.
     * @throws IndexOutOfBoundsException When the map does not contain an entry at the specified index.
     * @see #getEntry(Object)
     */
    public synchronized Map.Entry<K, V> getEntry(int index) {
        return getEntry(getKey(index));
    }
    
    /**
     * Gets an entry from the map.<br>
     * The entry returned is not backed by the map.
     *
     * @param key The key.
     * @return The entry.
     */
    public synchronized Map.Entry<K, V> getEntry(K key) {
        return Map.entry(key, get(key));
    }
    
    /**
     * Gets the index of a key from the map.
     *
     * @param key The key.
     * @return The index of the specified key, or -1 if the map does not contain the specified key.
     */
    public synchronized int indexOf(K key) {
        return keyList.indexOf(key);
    }
    
    /**
     * Gets an ordered entry set of the map.<br>
     * The entry set returned is not backed by the map.
     *
     * @return An ordered entry set.
     */
    public synchronized List<Map.Entry<K, V>> orderedEntrySet() {
        return entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> indexOf(entry.getKey())))
                .collect(ListCollectors.toUnmodifiableArrayList());
    }
    
    /**
     * Gets an ordered key set of the map.<br>
     * The key set returned is not backed by the map.
     *
     * @return An ordered key set.
     */
    public synchronized List<K> orderedKeySet() {
        return ListUtility.unmodifiableList(keyList);
    }
    
    /**
     * Gets an ordered collection of values of the map.<br>
     * The collection of values returned is not backed by the map.
     *
     * @return An ordered collection of values.
     */
    public synchronized List<V> orderedValues() {
        return keyList.stream().map(this::get)
                .collect(ListCollectors.toUnmodifiableArrayList());
    }
    
    /**
     * Puts an entry in the map.
     *
     * @param index The index to insert a new entry at; if the map already contains the key then an update will occur and the previous index will not change.
     * @param key   The key.
     * @param value The value.
     * @return The previous value of the specified key, or null if the map did not previously contain the specified key.
     * @throws IndexOutOfBoundsException When attempting to insert a new entry and the entry can not be inserted at the specified index.
     * @see HashMap#put(Object, Object)
     */
    public synchronized V put(int index, K key, V value) {
        if (!containsKey(key)) {
            keyList.add(index, key);
        }
        return super.put(key, value);
    }
    
    /**
     * Puts an entry in the map.
     *
     * @param key   The key.
     * @param value The value.
     * @return The previous value of the specified key, or null if the map did not previously contain the specified key.
     * @see #put(int, Object, Object)
     */
    @Override
    public synchronized V put(K key, V value) {
        return put(size(), key, value);
    }
    
    /**
     * Puts an entry in the map if the map does not already contain the key.
     *
     * @param index The index to insert the new entry at.
     * @param key   The key.
     * @param value The value.
     * @return The existing value of the specified key, or null if the map did not previously contain the specified key and it was added.
     * @throws IndexOutOfBoundsException When the entry can not be inserted at the specified index.
     * @see #put(int, Object, Object)
     */
    public synchronized V putIfAbsent(int index, K key, V value) {
        return containsKey(key) ? get(key) :
               put(index, key, value);
    }
    
    /**
     * Puts an entry in the map if the map does not already contain the key.
     *
     * @param key   The key.
     * @param value The value.
     * @return The existing value of the specified key, or null if the map did not previously contain the specified key and it was added.
     * @see #putIfAbsent(int, Object, Object)
     */
    @Override
    public synchronized V putIfAbsent(K key, V value) {
        return putIfAbsent(size(), key, value);
    }
    
    /**
     * Puts a map of entries in the map.<br>
     * Insertion order cannot be guaranteed if the map of entries is not ordered.
     *
     * @param index   The index to begin inserting new entries at; if the map already contains a key of an entry then an update will occur and the previous index will not change, determined per entry in the order of insertion.
     * @param entries The map of entries.
     * @throws IndexOutOfBoundsException When the entries can not be inserted at the specified index.
     * @see #put(int, Object, Object)
     */
    public synchronized void putAll(int index, Map<? extends K, ? extends V> entries) {
        final AtomicInteger indexCounter = new AtomicInteger(index);
        entries.forEach((key, value) ->
                put((containsKey(key) ? indexCounter.get() : indexCounter.getAndIncrement()), key, value));
    }
    
    /**
     * Puts a map of entries in the map.
     *
     * @param entries The map of entries.
     * @see #putAll(int, Map)
     */
    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> entries) {
        putAll(size(), entries);
    }
    
    /**
     * Replaces a value in the map.
     *
     * @param index The index to replace the value for.
     * @param value The new value.
     * @return The previous value at the specified index.
     * @throws IndexOutOfBoundsException When the map does not contain an entry at the specified index.
     * @see HashMap#replace(Object, Object)
     */
    public synchronized V replace(int index, V value) {
        return super.replace(getKey(index), value);
    }
    
    /**
     * Replaces a value in the map.
     *
     * @param index    The index to replace the value for.
     * @param oldValue The expected previous value.
     * @param newValue The new value.
     * @return Whether or not the previous value matched the expected value and was replaced.
     * @throws IndexOutOfBoundsException When the map does not contain an entry at the specified index.
     * @see #replace(int, Object)
     */
    public synchronized boolean replace(int index, V oldValue, V newValue) {
        return Objects.equals(get(index), oldValue) &&
                Objects.equals(oldValue, replace(index, newValue));
    }
    
    /**
     * Removes an entry from the map.
     *
     * @param index The index to remove.
     * @return The previous value at the specified index.
     * @throws IndexOutOfBoundsException When the map does not contain an entry at the specified index.
     * @see #remove(Object)
     */
    public synchronized V remove(int index) {
        return remove(getKey(index));
    }
    
    /**
     * Removes an entry from the map.
     *
     * @param index The index to remove.
     * @param value The expected value.
     * @return Whether or not the previous value matched the expected value and was removed.
     * @throws IndexOutOfBoundsException When the map does not contain an entry at the specified index.
     * @see #remove(Object, Object)
     */
    public synchronized boolean remove(int index, V value) {
        return remove(getKey(index), value);
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
        keyList.remove(key);
        return super.remove(key);
    }
    
    /**
     * Removes an entry from the map.
     *
     * @param key   The key to remove.
     * @param value The expected value.
     * @return Whether or not the map contained the specified key and the previous value matched the expected value and was removed.
     * @see #remove(Object)
     */
    @Override
    public synchronized boolean remove(Object key, Object value) {
        return containsKey(key) && Objects.equals(get(key), value) &&
                Objects.equals(value, remove(key));
    }
    
    /**
     * Clears the map.
     *
     * @see HashMap#clear()
     */
    @Override
    public synchronized void clear() {
        keyList.clear();
        super.clear();
    }
    
    /**
     * Clones the map.
     *
     * @return A clone of the map.
     * @see #IterableMap(IterableMap)
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public IterableMap<K, V> clone() {
        return new IterableMap<>(this);
    }
    
    /**
     * Determines if another IterableMap is equal to this IterableMap.
     *
     * @param o The other IterableMap.
     * @return Whether the two IterableMaps are equal or not.
     * @see ListUtility#equals(List, List)
     * @see MapUtility#equals(Map, Map)
     */
    @Override
    public synchronized boolean equals(Object o) {
        if (!(o instanceof IterableMap)) {
            return false;
        }
        IterableMap<?, ?> other = (IterableMap<?, ?>) o;
        
        return ListUtility.equals(keyList, other.keyList, true) &&
                MapUtility.equals(this, other);
    }
    
    /**
     * Computes a value of the map.
     *
     * @param index             The index to insert a new entry at; if the map already contains the key then an update will occur and the previous index will not change.
     * @param key               The key to compute the value for.
     * @param remappingFunction The function that produces the computed value given the key and the previous value; will remove the entry from the map if the function produces null.
     * @return The previous value of the specified key, or null if the map does not previously contain the specified key.
     * @throws IndexOutOfBoundsException When attempting to insert a new entry and the entry can not be inserted at the specified index.
     * @see #put(int, Object, Object)
     * @see #replace(int, Object)
     * @see #remove(Object)
     */
    public synchronized V compute(int index, K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        final V value = remappingFunction.apply(key, get(key));
        return (value == null) ? remove(key) :
               containsKey(key) ? replace(key, value) :
               put(index, key, value);
    }
    
    /**
     * Computes a value of the map.
     *
     * @param index             The index to compute the value for.
     * @param remappingFunction The function that produces the computed value given the key and the previous value; will remove the entry from the map if the function produces null.
     * @return The previous value at the specified index.
     * @throws IndexOutOfBoundsException When the map does not contain an entry at the specified index.
     * @see #compute(int, Object, BiFunction)
     */
    public synchronized V compute(int index, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return compute(index, getKey(index), remappingFunction);
    }
    
    /**
     * Computes a value of the map.
     *
     * @param key               The key to compute the value for.
     * @param remappingFunction The function that produces the computed value given the key and the previous value; will remove the entry from the map if the function produces null.
     * @return The previous value of the specified key.
     * @see #compute(int, Object, BiFunction)
     */
    @Override
    public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return compute(size(), key, remappingFunction);
    }
    
    /**
     * Computes a value of the map if the map does not already contain the key.
     *
     * @param index           The index to insert the new entry at.
     * @param key             The key to compute the value for.
     * @param mappingFunction The function that produces the computed value given the key.
     * @return The existing value of the specified key, or null if the map did not previously contain the specified key and it was added.
     * @throws IndexOutOfBoundsException When attempting to insert a new entry and the entry can not be inserted at the specified index.
     * @see #compute(int, Object, BiFunction)
     */
    public synchronized V computeIfAbsent(int index, K key, Function<? super K, ? extends V> mappingFunction) {
        return containsKey(key) ? get(key) :
               compute(index, key, (k, v) -> mappingFunction.apply(k));
    }
    
    /**
     * Computes a value of the map if the map does not already contain the key.
     *
     * @param key             The key to compute the value for.
     * @param mappingFunction The function that produces the computed value given the key.
     * @return The existing value of the specified key, or null if the map did not previously contain the specified key and it was added.
     * @see #computeIfAbsent(int, Object, Function)
     */
    @Override
    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return computeIfAbsent(size(), key, mappingFunction);
    }
    
    /**
     * Computes a value of the map if the map already contains the key.
     *
     * @param index             The index to compute the value for.
     * @param remappingFunction The function that produces the computed value given the key and the previous value; will remove the entry from the map if the function produces null.
     * @return The previous value at the specified index.
     * @throws IndexOutOfBoundsException When the map does not contain an entry at the specified index.
     * @see #computeIfPresent(Object, BiFunction)
     */
    public synchronized V computeIfPresent(int index, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return computeIfPresent(getKey(index), remappingFunction);
    }
    
    /**
     * Computes a value of the map if the map already contains the key.
     *
     * @param key               The key to compute the value for.
     * @param remappingFunction The function that produces the computed value given the key and the previous value; will remove the entry from the map if the function produces null.
     * @return The previous value at the specified index.
     * @see #compute(Object, BiFunction)
     */
    @Override
    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return !containsKey(key) ? null :
               compute(key, remappingFunction);
    }
    
    /**
     * Merges a value of the map.
     *
     * @param index             The index to insert a new entry at; if the map already contains the key then an update will occur and the previous index will not change.
     * @param key               The key to merge the value for.
     * @param value             The value to merge with the existing value.
     * @param remappingFunction The function that produces the merged value given the previous value and the value to be merged; will remove the entry from the map if the function produces null.
     * @return The previous value of the specified key, or null if the map did not previously contain the specified key.
     * @throws IndexOutOfBoundsException When attempting to insert a new entry and the entry can not be inserted at the specified index.
     * @see #compute(int, Object, BiFunction)
     */
    public synchronized V merge(int index, K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return compute(index, key, (k, v) -> (containsKey(k) ? remappingFunction.apply(v, value) : value));
    }
    
    /**
     * Merges a value of the map.
     *
     * @param index             The index to merge the value for.
     * @param value             The value to merge with the existing value.
     * @param remappingFunction The function that produces the merged value given the previous value and the value to be merged; will remove the entry from the map if the function produces null.
     * @return The previous value of the specified key
     * @throws IndexOutOfBoundsException When the map does not contain an entry at the specified index.
     * @see #merge(int, Object, Object, BiFunction)
     */
    public synchronized V merge(int index, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return merge(index, getKey(index), value, remappingFunction);
    }
    
    /**
     * Merges a value of the map.
     *
     * @param key               The key to merge the value for.
     * @param value             The value to merge with the existing value.
     * @param remappingFunction The function that produces the merged value given the previous value and the value to be merged; will remove the entry from the map if the function produces null.
     * @return The previous value of the specified key, or null if the map did not previously contain the specified key.
     * @see #merge(int, Object, Object, BiFunction)
     */
    @Override
    public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return merge(size(), key, value, remappingFunction);
    }
    
    /**
     * Performs an action on each key value pair of the map.
     *
     * @param action The action to perform.
     * @see #orderedEntrySet()
     */
    @Override
    public synchronized void forEach(BiConsumer<? super K, ? super V> action) {
        orderedEntrySet().forEach(entry ->
                action.accept(entry.getKey(), entry.getValue()));
    }
    
    /**
     * Performs an action on each entry of the map.
     *
     * @param action The action to perform.
     * @see #orderedEntrySet()
     */
    @Override
    public synchronized void forEach(Consumer<? super Map.Entry<K, V>> action) {
        orderedEntrySet().forEach(action);
    }
    
    /**
     * Performs an action on each indexed entry of the map.
     *
     * @param action The action to perform.
     * @see #orderedEntrySet()
     */
    public synchronized void indexedForEach(BiConsumer<? super Map.Entry<K, V>, Integer> action) {
        final AtomicInteger index = new AtomicInteger(0);
        orderedEntrySet().forEach(entry ->
                action.accept(entry, index.getAndIncrement()));
    }
    
    /**
     * Creates an iterator of the entries of the map.
     *
     * @return The iterator.
     * @see CustomIterator
     * @see #exposedEntrySet()
     */
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new CustomIterator<>(
                exposedEntrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> indexOf(entry.getKey())))
                        .collect(ListCollectors.toArrayList()),
                (index, entry) -> remove(entry.getKey()));
    }
    
}
