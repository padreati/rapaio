/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/10/20.
 */
public class Int2IntOpenHashMap implements Serializable {

    @Serial
    private static final long serialVersionUID = 5146955493932065218L;

    public enum Probing {
        LINEAR {
            @Override
            public int step(int round) {
                return round;
            }
        },
        QUADRATIC {
            @Override
            public int step(int round) {
                return round * round;
            }
        };

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

    private int[] array;
    private int size;

    public Int2IntOpenHashMap() {
        this(DEFAULT_SEED, DEFAULT_LOAD_FACTOR, DEFAULT_ALLOCATION, DEFAULT_PROBING);
    }

    public Int2IntOpenHashMap(int seed) {
        this(seed, DEFAULT_LOAD_FACTOR, DEFAULT_ALLOCATION, DEFAULT_PROBING);
    }

    public Int2IntOpenHashMap(int seed, double loadFactor, int allocation, Probing probing) {
        this.seed = seed;
        this.loadFactor = loadFactor;
        this.probing = probing;

        this.array = Ints.fill(2 * allocation, MISSING);
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(int key) {
        if (key == MISSING) {
            return false;
        }
        int len = array.length / 2;
        int hash = Murmur3.murmur3A(key, seed) % len;
        if (hash < 0) {
            hash += len;
        }
        int step = 0;
        while (true) {
            hash += probing.step(step);
            step++;
            hash %= len;
            if (array[hash * 2] == MISSING) {
                return false;
            }
            if (array[hash * 2] == key) {
                return true;
            }
        }
    }

    public int get(int key) {
        if (key == MISSING) {
            throw new IllegalArgumentException();
        }
        int len = array.length / 2;
        int hash = Murmur3.murmur3A(key, seed) % len;
        if (hash < 0) {
            hash += len;
        }
        int step = 0;
        while (true) {
            hash += probing.step(step);
            step++;
            hash %= len;
            if (array[hash * 2] == MISSING) {
                return MISSING;
            }
            if (array[hash * 2] == key) {
                return array[hash * 2 + 1];
            }
        }
    }

    public void put(int key, int value) {
        if (key == MISSING) {
            throw new IllegalArgumentException();
        }
        ensureCapacity(1);
        if (putInArray(array, key, value)) {
            size++;
        }
    }

    private boolean putInArray(int[] data, int key, int value) {
        int len = data.length / 2;
        int hash = Murmur3.murmur3A(key, seed) % len;
        if (hash < 0) {
            hash += len;
        }
        int step = 0;
        while (true) {
            hash += probing.step(step);
            step++;
            hash %= len;
            if (data[hash * 2] == MISSING) {
                data[hash * 2] = key;
                data[hash * 2 + 1] = value;
                return true;
            }
            if (data[hash * 2] == key) {
                data[hash * 2] = key;
                data[hash * 2 + 1] = value;
                return false;
            }
        }
    }

    private void ensureCapacity(int increment) {
        if (size + increment < (int) (1.0 + loadFactor * array.length / 2)) {
            return;
        }
        int[] copy = Ints.fill(array.length * 2, MISSING);
        for (int i = 0; i < array.length; i += 2) {
            if (array[i] != MISSING) {
                putInArray(copy, array[i], array[i + 1]);
            }
        }
        array = copy;
    }

    public IntOpenHashSet keySet() {
        IntOpenHashSet set = new IntOpenHashSet(IntOpenHashSet.DEFAULT_SEEED, IntOpenHashSet.DEFAULT_LOAD_FACTOR,
                Math.max(IntOpenHashSet.DEFAULT_ALLOCATION, (int) Math.ceil(size * IntOpenHashSet.DEFAULT_LOAD_FACTOR)),
                IntOpenHashSet.DEFAULT_PROBING);
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
            if (array[i + 1] != MISSING) {
                list.add(array[i + 1]);
            }
        }
        return list;
    }
}
