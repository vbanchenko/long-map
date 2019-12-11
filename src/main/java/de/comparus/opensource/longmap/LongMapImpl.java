package de.comparus.opensource.longmap;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * LongMapImpl is similar to HashMap by algorithm of storing elements.
 * It stores data in array. Collisions problem is resolved by using LinkedList.
 * Array creates with size = INITIAL_CAPACITY, if size becomes more then LOAD_FACTOR of capacity,
 * map creates new array with double capacity and moves all values to it.
 * If size becomes less then 1 - LOAD_FACTOR of capacity, map moves all values to new array with half capacity.
 * This map can contain null values.
 * @param <V>
 */
public class LongMapImpl<V> implements LongMap<V> {

    private static final float LOAD_FACTOR = 0.75F;
    private static final int INITIAL_CAPACITY = 16;

    private final Class<V> type;

    private LinkedList<MapEntry>[] entries;
    private int size;

    /**
     * @param type - needed to instantiate array of values by 'values()' method.
     *             Do this in such way to keep original signature of 'values()' method.
     */
    public LongMapImpl(Class<V> type) {
        this.type = type;
        clear();
    }

    /**
     * @return - previous value if exists.
     */
    public V put(long key, V value) {
        resize();
        return putValue(key, value);
    }

    /**
     * @return - previous value if exists.
     */
    public V get(long key) {
        final MapEntry existingEntry = findExistingEntry(indexFor(key), key);
        if (existingEntry == null) {
            return null;
        }
        return existingEntry.getValue();
    }

    /**
     * @return - previous value if exists.
     */
    public V remove(long key) {
        resize();
        final int index = indexFor(key);
        final MapEntry existingEntry = findExistingEntry(index, key);
        if (existingEntry != null) {
            final LinkedList<MapEntry> entryChain = entries[index];
            entryChain.remove(existingEntry);
            if (entryChain.isEmpty()) {
                entries[index] = null;
            }
            size--;
            return existingEntry.getValue();
        }
        return null;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(long key) {
        int index = indexFor(key);
        final LinkedList<MapEntry> entryChain = entries[index];
        if (entryChain != null) {
            for (final MapEntry entry : entryChain) {
                if (entry.getKey() == key) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsValue(V value) {
        int index = 0;
        for (int i = 0; i < size;) {
            final LinkedList<MapEntry> entryChain = entries[index++];
            if (entryChain != null) {
                for (final MapEntry entry : entryChain) {
                    if (Objects.equals(entry.getValue(), value)) {
                        return true;
                    }
                    i++;
                }
            }
        }
        return false;
    }

    public long[] keys() {
        return collectKeys();
    }

    public V[] values() {
        return collectValues();
    }

    public long size() {
        return size;
    }

    public void clear() {
        //noinspection unchecked
        entries = ((LinkedList<MapEntry>[]) new LinkedList[INITIAL_CAPACITY]);
        size = 0;
    }

    private V putValue(long key, V value) {
        final int index = indexFor(key);
        final MapEntry existingEntry = findExistingEntry(index, key);
        if (existingEntry == null) {
            LinkedList<MapEntry> entryChain = entries[index];
            if (entryChain == null) {
                // add new chain
                entryChain = new LinkedList<>();
                entries[index] = entryChain;
            }
            // add new entry
            entryChain.add(new MapEntry(key, value));
            size++;
            return null;
        } else {
            // replace value of existing entry
            final V oldValue = existingEntry.getValue();
            existingEntry.setValue(value);
            return oldValue;
        }
    }

    private int indexFor(long key) {
        // do this in same way as HashMap
        return (int) ((entries.length - 1) & key);
    }

    private MapEntry findExistingEntry(int index, long key) {
        final LinkedList<MapEntry> entryChain = entries[index];
        if (entryChain != null) {
            for (final MapEntry entry : entryChain) {
                if (entry.getKey() == key) {
                    return entry;
                }
            }
        }
        return null;
    }

    private long[] collectKeys() {
        int index = 0;
        final long[] keys = new long[size];
        for (int i = 0; i < size; ) {
            final LinkedList<MapEntry> entryChain = entries[index++];
            if (entryChain != null) {
                for (final MapEntry entry : entryChain) {
                    keys[i++] = entry.getKey();
                }
            }
        }
        return keys;
    }

    private V[] collectValues() {
        int index = 0;
        //noinspection unchecked
        final V[] values = (V[]) Array.newInstance(type, size);
        for (int i = 0; i < size;) {
            final LinkedList<MapEntry> entryChain = entries[index++];
            if (entryChain != null) {
                for (final MapEntry entry : entryChain) {
                    values[i++] = entry.getValue();
                }
            }
        }
        return values;
    }

    /**
     * Replaces array of values to bigger one when size becomes to large or
     * replace to smaller one when size becomes too small.
     * Recalculates indexes for entries according to new capacity.
     * Do nothing if resize is not needed.
     */
    private void resize() {
        final int newCapacity = getNewCapacity();
        if (newCapacity != entries.length) {
            final LinkedList<MapEntry>[] oldEntries = entries;
            final int oldSize = size;
            //noinspection unchecked
            entries = ((LinkedList<MapEntry>[]) new LinkedList[newCapacity]);
            size = 0;
            int index = 0;
            for (int i = 0; i < oldSize;) {
                final LinkedList<MapEntry> entryChain = oldEntries[index];
                if (entryChain != null) {
                    for (final MapEntry entry : entryChain) {
                        putValue(entry.getKey(), entry.getValue());
                        i++;
                    }
                    oldEntries[index] = null;
                }
                index++;
            }
        }
    }

    /**
     * Calculates actually needed capacity according to current size.
     * @return - needed capacity
     */
    private int getNewCapacity() {
        final int newCapacity;
        if (size > entries.length * LOAD_FACTOR) {
            newCapacity = Math.min(entries.length * 2, Integer.MAX_VALUE);
        } else if (size < entries.length * (1 - LOAD_FACTOR)) {
            newCapacity = Math.max(entries.length / 2, INITIAL_CAPACITY);
        } else {
            return entries.length;
        }
        return newCapacity;
    }

    @Data
    @AllArgsConstructor
    private class MapEntry {
        private final long key;
        private V value;
    }
}
