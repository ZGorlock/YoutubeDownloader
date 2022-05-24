/*
 * File:    StrictHashMap.java
 * Package: commons.object.collection.map.strict
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object.collection.map.strict;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a Strict Hash Map.<br>
 * The views backed by the map are returned as immutable views.
 *
 * @param <K> The type of the keys of the map.
 * @param <V> The type of the values of the map.
 */
public class StrictHashMap<K, V> extends HashMap<K, V> implements StrictMapInterface<K, V> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(StrictHashMap.class);
    
    
    //Constructors
    
    /**
     * The constructor for an Strict Hash Map from a map.
     *
     * @param map The map.
     * @see #putAll(Map)
     */
    public StrictHashMap(Map<? extends K, ? extends V> map) {
        this.putAll(map);
    }
    
    /**
     * The default no-argument constructor for a Strict Hash Map.
     */
    public StrictHashMap() {
    }
    
    
    //Methods
    
    /**
     * Gets a mutable entry set of the map.
     *
     * @return A mutable entry set.
     * @see HashMap#entrySet()
     */
    @Override
    public Set<Entry<K, V>> exposedEntrySet() {
        return super.entrySet();
    }
    
    /**
     * Gets an immutable entry set of the map.
     *
     * @return An immutable entry set.
     * @see #immutableEntrySet()
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return immutableEntrySet();
    }
    
    /**
     * Gets a mutable key set of the map.
     *
     * @return A mutable key set.
     * @see HashMap#keySet()
     */
    @Override
    public Set<K> exposedKeySet() {
        return super.keySet();
    }
    
    /**
     * Gets an immutable key set of the map.
     *
     * @return An immutable key set.
     * @see #immutableKeySet()
     */
    @Override
    public Set<K> keySet() {
        return immutableKeySet();
    }
    
    /**
     * Gets a mutable collection of values of the map.
     *
     * @return A mutable collection of values.
     * @see HashMap#values()
     */
    @Override
    public Collection<V> exposedValues() {
        return super.values();
    }
    
    /**
     * Gets an immutable collection of values of the map.
     *
     * @return An immutable collection of values.
     * @see #immutableValues()
     */
    @Override
    public Collection<V> values() {
        return immutableValues();
    }
    
}
