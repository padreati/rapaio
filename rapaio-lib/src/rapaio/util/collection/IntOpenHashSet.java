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
import java.util.Arrays;
import java.util.Collection;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import rapaio.util.IntIterable;
import rapaio.util.hash.Murmur3;

/**
 * Open addressing hash set of primitive int values.
 * <p>
 * The table capacity is always a power of two and probing is triangular ({@code h, h+1, h+3, h+6, ...}),
 * which visits every slot of a power-of-two table exactly once, so a lookup or insertion always terminates
 * as long as the table is not full; the load factor guarantees it never is. {@link Probing#LINEAR} keeps the
 * classic sequential probe. Every int value, including {@link Integer#MIN_VALUE}, can be stored: the value used
 * as the empty-slot marker is tracked with a separate flag.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/10/20.
 */
public class IntOpenHashSet implements Serializable, IntIterable {

    @Serial
    private static final long serialVersionUID = 8709214224656233765L;

    public static final int DEFAULT_SEEED = 42;
    public static final double DEFAULT_LOAD_FACTOR = 0.75;
    public static final int DEFAULT_ALLOCATION = 16;
    public static final Probing DEFAULT_PROBING = Probing.QUADRATIC;

    /**
     * Marker of an empty slot in the backing array. The value itself is still a valid set member,
     * stored through a dedicated flag.
     */
    public static final int MISSING = Integer.MIN_VALUE;

    private final int seed;
    private final double loadFactor;
    private final Probing probing;

    /**
     * Number of values stored in the array (does not count the MISSING member).
     */
    private int arraySize;
    /**
     * Whether the value equal to the empty-slot marker is a member.
     */
    private boolean hasMissingValue;
    private int[] array;
    /**
     * Largest arraySize before the table is grown.
     */
    private int threshold;

    public IntOpenHashSet() {
        this(DEFAULT_SEEED, DEFAULT_LOAD_FACTOR, DEFAULT_ALLOCATION, DEFAULT_PROBING);
    }

    public IntOpenHashSet(int seed) {
        this(seed, DEFAULT_LOAD_FACTOR, DEFAULT_ALLOCATION, DEFAULT_PROBING);
    }

    /**
     * @param seed       hash seed
     * @param loadFactor maximum fill ratio before growing, in {@code (0, 1)}
     * @param allocation expected number of elements; the initial capacity is derived from it
     * @param probing    probing strategy
     */
    public IntOpenHashSet(int seed, double loadFactor, int allocation, Probing probing) {
        if (!(loadFactor > 0 && loadFactor < 1)) {
            throw new IllegalArgumentException("Load factor must be in (0, 1), given: " + loadFactor);
        }
        if (allocation < 0) {
            throw new IllegalArgumentException("Allocation must be non-negative, given: " + allocation);
        }
        this.seed = seed;
        this.loadFactor = loadFactor;
        this.probing = probing;
        allocate(capacityFor(allocation, loadFactor));
    }

    private static int capacityFor(int expected, double loadFactor) {
        long needed = (long) Math.ceil(Math.max(expected, 1) / loadFactor) + 1;
        int capacity = 4;
        while (capacity < needed) {
            capacity <<= 1;
        }
        return capacity;
    }

    private void allocate(int capacity) {
        array = Ints.fill(capacity, MISSING);
        // keep at least one empty slot so that an unsuccessful lookup always terminates
        threshold = Math.min(capacity - 1, (int) (capacity * loadFactor));
    }

    public int size() {
        return arraySize + (hasMissingValue ? 1 : 0);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private int home(int value, int mask) {
        return Murmur3.murmur3A(value, seed) & mask;
    }

    /**
     * Index of the slot holding the value, or of the empty slot where it would be inserted.
     */
    private int find(int[] table, int value) {
        int mask = table.length - 1;
        int pos = home(value, mask);
        for (int round = 1; ; round++) {
            int current = table[pos];
            if (current == MISSING || current == value) {
                return pos;
            }
            pos = (pos + probing.step(round)) & mask;
        }
    }

    public boolean contains(int value) {
        if (value == MISSING) {
            return hasMissingValue;
        }
        return array[find(array, value)] == value;
    }

    public PrimitiveIterator.OfInt iterator() {
        int[] copy = toArray();
        return Ints.iterator(copy, 0, copy.length);
    }

    public int[] toArray() {
        IntStream values = IntStream.of(array).filter(value -> value != MISSING);
        if (hasMissingValue) {
            values = IntStream.concat(IntStream.of(MISSING), values);
        }
        return values.toArray();
    }

    public boolean add(int value) {
        if (value == MISSING) {
            boolean added = !hasMissingValue;
            hasMissingValue = true;
            return added;
        }
        if (arraySize >= threshold) {
            grow();
        }
        int pos = find(array, value);
        if (array[pos] == value) {
            return false;
        }
        array[pos] = value;
        arraySize++;
        return true;
    }

    public boolean addAll(Collection<? extends Integer> c) {
        for (Object o : c) {
            if (!(o instanceof Integer)) {
                throw new ClassCastException();
            }
        }
        boolean changed = false;
        for (Integer i : c) {
            changed |= add(i);
        }
        return changed;
    }

    public void clear() {
        Arrays.fill(array, MISSING);
        arraySize = 0;
        hasMissingValue = false;
    }

    private void grow() {
        int[] old = array;
        allocate(old.length << 1);
        for (int x : old) {
            if (x != MISSING) {
                array[find(array, x)] = x;
            }
        }
    }

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
}
