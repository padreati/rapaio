/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.util.collection;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rapaio.util.hash.Murmur3;

/**
 * Open addressing hash map from primitive int keys to primitive int values.
 * <p>
 * Keys and values are stored interleaved in one array ({@code key, value, key, value, ...}). The number of
 * slots is always a power of two and probing is triangular ({@code h, h+1, h+3, h+6, ...}), which visits every
 * slot of a power-of-two table exactly once, so lookups and insertions always terminate; the load factor keeps
 * at least one slot empty. {@link Probing#LINEAR} keeps the classic sequential probe.
 * <p>
 * {@link #MISSING} ({@code Integer.MIN_VALUE}) marks an empty slot and therefore cannot be used as a key;
 * {@link #get(int)} returns it for an absent key. Any int, including {@code MISSING}, is a valid value.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/10/20.
 */
public class Int2IntOpenHashMap implements Serializable {

    @Serial
    private static final long serialVersionUID = 5146955493932065218L;

    public enum Probing {
        /**
         * Sequential probing: slots {@code h, h+1, h+2, ...}.
         */
        LINEAR {
            @Override
            public int step(int round) {
                return 1;
            }
        },
        /**
         * Triangular probing: slots {@code h, h+1, h+3, h+6, ...}, a full permutation of a power-of-two table.
         */
        QUADRATIC {
            @Override
            public int step(int round) {
                return round;
            }
        };

        /**
         * Distance from the previous probed slot to the next one, for the given probe round (starting at 1).
         */
        public abstract int step(int round);
    }

    public static final int MISSING = Integer.MIN_VALUE;
    public static final int DEFAULT_ALLOCATION = 12;
    public static final double DEFAULT_LOAD_FACTOR = 0.75;
    public static final Probing DEFAULT_PROBING = Probing.QUADRATIC;
    public static final int DEFAULT_SEED = 42;

    private final int seed;
    private final double loadFactor;
    private final Probing probing;

    /**
     * Interleaved keys and values; length is twice the number of slots.
     */
    private int[] array;
    private int size;
    /**
     * Largest size before the table is grown.
     */
    private int threshold;

    public Int2IntOpenHashMap() {
        this(DEFAULT_SEED, DEFAULT_LOAD_FACTOR, DEFAULT_ALLOCATION, DEFAULT_PROBING);
    }

    public Int2IntOpenHashMap(int seed) {
        this(seed, DEFAULT_LOAD_FACTOR, DEFAULT_ALLOCATION, DEFAULT_PROBING);
    }

    /**
     * @param seed       hash seed
     * @param loadFactor maximum fill ratio before growing, in {@code (0, 1)}
     * @param allocation expected number of entries; the initial number of slots is derived from it
     * @param probing    probing strategy
     */
    public Int2IntOpenHashMap(int seed, double loadFactor, int allocation, Probing probing) {
        if (!(loadFactor > 0 && loadFactor < 1)) {
            throw new IllegalArgumentException("Load factor must be in (0, 1), given: " + loadFactor);
        }
        if (allocation < 0) {
            throw new IllegalArgumentException("Allocation must be non-negative, given: " + allocation);
        }
        this.seed = seed;
        this.loadFactor = loadFactor;
        this.probing = probing;
        allocate(slotsFor(allocation, loadFactor));
    }

    private static int slotsFor(int expected, double loadFactor) {
        long needed = (long) Math.ceil(Math.max(expected, 1) / loadFactor) + 1;
        int slots = 4;
        while (slots < needed) {
            slots <<= 1;
        }
        return slots;
    }

    private void allocate(int slots) {
        array = Ints.fill(2 * slots, MISSING);
        threshold = Math.min(slots - 1, (int) (slots * loadFactor));
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Slot index (not array index) holding the key, or of the empty slot where it would be inserted.
     */
    private int find(int[] data, int key) {
        int mask = data.length / 2 - 1;
        int pos = Murmur3.murmur3A(key, seed) & mask;
        for (int round = 1; ; round++) {
            int current = data[pos << 1];
            if (current == MISSING || current == key) {
                return pos;
            }
            pos = (pos + probing.step(round)) & mask;
        }
    }

    public boolean containsKey(int key) {
        if (key == MISSING) {
            return false;
        }
        return array[find(array, key) << 1] == key;
    }

    /**
     * @param key key to look up, must not be {@link #MISSING}
     * @return the value mapped to the key, or {@link #MISSING} if the key is absent
     */
    public int get(int key) {
        if (key == MISSING) {
            throw new IllegalArgumentException("Key " + MISSING + " is reserved and cannot be used.");
        }
        int pos = find(array, key) << 1;
        return array[pos] == key ? array[pos + 1] : MISSING;
    }

    /**
     * @param key   key to insert or update, must not be {@link #MISSING}
     * @param value any int value
     */
    public void put(int key, int value) {
        if (key == MISSING) {
            throw new IllegalArgumentException("Key " + MISSING + " is reserved and cannot be used.");
        }
        if (size >= threshold) {
            grow();
        }
        if (putInArray(array, key, value)) {
            size++;
        }
    }

    private boolean putInArray(int[] data, int key, int value) {
        int pos = find(data, key) << 1;
        boolean inserted = data[pos] == MISSING;
        data[pos] = key;
        data[pos + 1] = value;
        return inserted;
    }

    private void grow() {
        int[] old = array;
        allocate(old.length);   // old.length == 2 * old slots, so this doubles the slot count
        for (int i = 0; i < old.length; i += 2) {
            if (old[i] != MISSING) {
                putInArray(array, old[i], old[i + 1]);
            }
        }
    }

    public IntOpenHashSet keySet() {
        IntOpenHashSet set = new IntOpenHashSet(IntOpenHashSet.DEFAULT_SEEED, IntOpenHashSet.DEFAULT_LOAD_FACTOR,
                Math.max(IntOpenHashSet.DEFAULT_ALLOCATION, size), IntOpenHashSet.DEFAULT_PROBING);
        for (int i = 0; i < array.length; i += 2) {
            if (array[i] != MISSING) {
                set.add(array[i]);
            }
        }
        return set;
    }

    public Collection<Integer> values() {
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < array.length; i += 2) {
            // presence is decided by the key slot; a value equal to MISSING is a legitimate value
            if (array[i] != MISSING) {
                list.add(array[i + 1]);
            }
        }
        return list;
    }
}
