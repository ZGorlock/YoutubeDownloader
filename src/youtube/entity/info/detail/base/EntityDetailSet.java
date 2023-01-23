/*
 * File:    EntityDetailSet.java
 * Package: youtube.entity.info.detail.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.entity.info.detail.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import commons.object.collection.ListUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.entity.info.base.EntityData;

/**
 * Defines an Entity Detail Set of an Entity.
 *
 * @param <T> The type of Entity Detail.
 */
public abstract class EntityDetailSet<T extends EntityDetail> extends EntityData {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EntityDetailSet.class);
    
    
    //Fields
    
    /**
     * The list of Detail entries in the Entity Detail Set.
     */
    private final List<T> entries;
    
    
    //Constructors
    
    /**
     * Creates a Detail Set for an Entity.
     *
     * @param entitySetData The json data of the Entity Detail Set.
     */
    protected EntityDetailSet(Map<String, Object> entitySetData) {
        super(entitySetData);
        
        this.entries = new ArrayList<>();
    }
    
    /**
     * Creates an empty Entity Detail Set.
     */
    protected EntityDetailSet() {
        super();
        
        this.entries = new ArrayList<>();
    }
    
    
    //Methods
    
    /**
     * Returns the list of Detail entries in the Entity Detail Set.
     *
     * @return The list of Detail entries in the Entity Detail Set.
     */
    public List<T> getAll() {
        return getEntries();
    }
    
    /**
     * Returns the Detail entry a specified index in the Entity Detail Set.
     *
     * @param i The index of the Detail entry.
     * @return The Detail entry at the specified index, or null if does not exist.
     */
    public T get(int i) {
        return ListUtility.getOrNull(getEntries(), i);
    }
    
    /**
     * Returns the Detail entry with a specified key in the Entity Detail Set.
     *
     * @param key The key of the Detail entry.
     * @return The Detail entry with the specified key, or null if does not exist.
     */
    public T get(String key) {
        return getEntries().stream()
                .filter(e -> Objects.equals(e.getKey(), key))
                .findFirst().orElse(null);
    }
    
    /**
     * Adds a Detail entry to the Entity Detail Set.
     *
     * @param entry The Detail entry.
     */
    public void add(T entry) {
        getEntries().add(entry);
    }
    
    /**
     * Adds a collection of Detail entries to the Entity Detail Set.
     *
     * @param entries The collection of Detail entries.
     */
    public void addAll(Collection<T> entries) {
        getEntries().addAll(entries);
    }
    
    /**
     * Clears the Detail Set.
     */
    public void clear() {
        getEntries().clear();
    }
    
    /**
     * Returns the number of Detail entries in the Entity Detail Set.
     *
     * @return The number of Detail entries in the Entity Detail Set.
     */
    public int size() {
        return getEntries().size();
    }
    
    /**
     * Returns whether the Entity Detail Set is empty.
     *
     * @return Whether the Entity Detail Set is empty.
     */
    public boolean isEmpty() {
        return getEntries().isEmpty();
    }
    
    
    //Getters
    
    /**
     * Returns the list of Detail entries in the Entity Detail Set.
     *
     * @return The list of Detail entries in the Entity Detail Set.
     */
    private List<T> getEntries() {
        return entries;
    }
    
}
