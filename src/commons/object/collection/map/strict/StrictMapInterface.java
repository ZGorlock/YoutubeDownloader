/*
 * File:    StrictMapInterface.java
 * Package: commons.object.collection.map.strict
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object.collection.map.strict;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines the contract for a Strict Map.
 *
 * @param <K> The type of the keys of the map.
 * @param <V> The type of the values of the map.
 */
public interface StrictMapInterface<K, V> {
    
    /**
     * Gets a mutable entry set of the map.
     *
     * @return A mutable entry set.
     */
    Set<Map.Entry<K, V>> exposedEntrySet();
    
    /**
     * Gets an immutable entry set of the map.
     *
     * @return An immutable entry set.
     * @see #exposedEntrySet()
     */
    default Set<Map.Entry<K, V>> immutableEntrySet() {
        return Collections.unmodifiableSet(exposedEntrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue()))
                .collect(Collectors.toSet()));
    }
    
    /**
     * Gets a mutable key set of the map.
     *
     * @return A mutable key set.
     */
    Set<K> exposedKeySet();
    
    /**
     * Gets an immutable key set of the map.
     *
     * @return An immutable key set.
     * @see #exposedKeySet()
     */
    default Set<K> immutableKeySet() {
        return Collections.unmodifiableSet(exposedKeySet());
    }
    
    /**
     * Gets a mutable collection of values of the map.
     *
     * @return A mutable collection of values.
     */
    Collection<V> exposedValues();
    
    /**
     * Gets an immutable collection of values of the map.
     *
     * @return An immutable collection of values.
     * @see #exposedValues()
     */
    default Collection<V> immutableValues() {
        return Collections.unmodifiableCollection(exposedValues());
    }
    
}
