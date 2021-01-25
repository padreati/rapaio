/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import rapaio.core.RandomSource;
import rapaio.util.IntIterable;
import rapaio.util.IntIterator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.IntStream;

import static rapaio.util.hash.Murmur3.murmur3A;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/10/20.
 */
public class IntOpenHashSet implements Serializable, IntIterable {

    private static final long serialVersionUID = 8709214224656233765L;

    public static final double DEFAULT_LOAD_FACTOR = 0.75;
    public static final int DEFAULT_ALLOCATION = 16;
    public static final Probing DEFAULT_PROBING = Probing.QUADRATIC;

    public static final int MISSING = Integer.MIN_VALUE;

    private final int seed = RandomSource.nextInt();
    private final double loadFactor;
    private final Probing probing;
    private int size;
    private int[] array;

    public IntOpenHashSet() {
        this(DEFAULT_LOAD_FACTOR, DEFAULT_ALLOCATION, DEFAULT_PROBING);
    }

    public IntOpenHashSet(double loadFactor, int allocation, Probing probing) {
        this.loadFactor = loadFactor;
        this.probing = probing;
        this.array = IntArrays.newFill(allocation, MISSING);
    }

    /**
     * Returns the number of elements in this set (its cardinality).  If this
     * set contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this set (its cardinality)
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns {@code true} if and only if the int {@code value} is a
     * member of the set, false otherwise.
     *
     * @param value value to be checked
     * @return true if value is in the set, false otherwise
     */
    public boolean contains(int value) {
        int hash = murmur3A(value, seed) % array.length;
        if (hash < 0) {
            hash += array.length;
        }
        int step = 0;
        while (true) {
            hash += probing.step(step);
            step++;
            hash %= array.length;
            if (array[hash] == MISSING) {
                return false;
            }
            if (array[hash] == value) {
                return true;
            }
        }
    }

    /**
     * Returns an iterator over the elements in this set.  The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).
     *
     * @return an iterator over the elements in this set
     */
    public IntIterator iterator() {
        int[] copy = toArray();
        return IntArrays.iterator(copy, 0, copy.length);
    }

    public int[] toArray() {
        return IntStream.of(array).filter(value -> value != MISSING).toArray();
    }

    public boolean add(int value) {
        if (needsCapacity(1)) {
            ensureCapacity(1);
        }
        int hash = murmur3A(value, seed) % array.length;
        if (hash < 0) {
            hash += array.length;
        }
        int step = 0;
        while (true) {
            hash += probing.step(step);
            step++;
            hash %= array.length;
            if (array[hash] == value) {
                return false;
            }
            if (array[hash] == MISSING) {
                array[hash] = value;
                size++;
                return true;
            }
        }
    }

    public boolean addAll(Collection<? extends Integer> c) {
        for (Object o : c) {
            if (!(o instanceof Integer)) {
                throw new ClassCastException();
            }
        }
        if (needsCapacity(c.size())) {
            ensureCapacity(c.size());
        }
        boolean changed = false;
        for (Object o : c) {
            int i = (int) o;
            changed |= add((int) o);
        }
        return changed;
    }

    /**
     * Removes all of the elements from this set (optional operation).
     * The set will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} method
     *                                       is not supported by this set
     */
    public void clear() {
        Arrays.fill(array, MISSING);
        this.size = 0;
    }

    private boolean needsCapacity(int increment) {
        return size >= Math.floor(loadFactor * array.length);
    }

    private void ensureCapacity(int increment) {
        int len = (int) Math.ceil(2 * size / loadFactor);
        int[] copy = IntArrays.newFill(len, MISSING);
        for (int x : array) {
            if (x == MISSING) {
                continue;
            }
            int hash = murmur3A(x, seed) % copy.length;
            if (hash < 0) {
                hash += copy.length;
            }
            int step = 0;
            while (true) {
                hash += probing.step(step);
                step++;
                hash %= copy.length;
                if (copy[hash] == MISSING) {
                    copy[hash] = x;
                    break;
                }
            }
        }
        array = copy;
    }

    public enum Probing {
        LINEAR {
            @Override
            public int step(int round) {
                return round;
            }
        }, QUADRATIC {
            @Override
            public int step(int round) {
                return round * round;
            }
        };

        public abstract int step(int round);
    }
}

