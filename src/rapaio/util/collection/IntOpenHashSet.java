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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.IntStream;

import static rapaio.util.hash.Murmur3.murmur3A;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/10/20.
 */
public class IntOpenHashSet implements IntSet {

    private static final long serialVersionUID = 8709214224656233765L;

    public static final double DEFAULT_LOAD_FACTOR = 0.75;
    public static final int DEFAULT_SIZE = 16;
    public static final Probing DEFAULT_PROBING = Probing.Quadratic;

    private static final int MISSING = Integer.MIN_VALUE;

    private final int seed = RandomSource.nextInt();
    private final double loadFactor;
    private final Probing probing;
    private int size;
    private int[] array;

    public IntOpenHashSet() {
        this(DEFAULT_LOAD_FACTOR, DEFAULT_SIZE, DEFAULT_PROBING);
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
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Deprecated in favour of {@link #containsInt(int)}
     */
    @Override
    @Deprecated
    public boolean contains(Object o) {
        if (!(o instanceof Integer)) {
            throw new ClassCastException();
        }
        return containsInt((int) o);
    }

    /**
     * Returns {@code true} if and only if the int {@code value} is a
     * member of the set, false otherwise.
     *
     * @param value value to be checked
     * @return true if value is in the set, false otherwise
     */
    @Override
    public boolean containsInt(int value) {
        int hash = murmur3A(value, seed) % array.length;
        if (hash < 0) {
            hash += array.length;
        }
        int step = 0;
        while (true) {
            hash += probing.equals(Probing.Linear) ? step : step * step;
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
    @Override
    public Iterator<Integer> iterator() {
        return IntStream.of(array).filter(value -> value != MISSING).iterator();
    }

    @Override
    public Object[] toArray() {
        return IntStream.of(array).filter(value -> value != MISSING).boxed().toArray();
    }

    public int[] toIntArray() {
        return IntStream.of(array).filter(value -> value != MISSING).toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean add(Integer integer) {
        if (integer == null) {
            throw new ClassCastException();
        }
        return addInt(integer);
    }

    public boolean addInt(int value) {
        if (needsCapacity(1)) {
            ensureCapacity(1);
        }
        int hash = murmur3A(value, seed) % array.length;
        if (hash < 0) {
            hash += array.length;
        }
        int step = 0;
        while (true) {
            hash += probing.equals(Probing.Linear) ? step : step * step;
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

    @Override
    @Deprecated
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns {@code true} if this set contains all of the elements of the
     * specified collection.  If the specified collection is also a set, this
     * method returns {@code true} if it is a <i>subset</i> of this set.
     *
     * @param c collection to be checked for containment in this set
     * @return {@code true} if this set contains all of the elements of the
     * specified collection
     * @throws ClassCastException   if the types of one or more elements
     *                              in the specified collection are incompatible with this
     *                              set
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one
     *                              or more null elements and this set does not permit null
     *                              elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>),
     *                              or if the specified collection is null
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object x : c) {
            if (!(x instanceof Integer) || !containsInt((int) x)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation).  If the specified
     * collection is also a set, the {@code addAll} operation effectively
     * modifies this set so that its value is the <i>union</i> of the two
     * sets.  The behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress.
     *
     * @param c collection containing elements to be added to this set
     * @return {@code true} if this set changed as a result of the call
     * @throws UnsupportedOperationException if the {@code addAll} operation
     *                                       is not supported by this set
     * @throws ClassCastException            if the class of an element of the
     *                                       specified collection prevents it from being added to this set
     * @throws NullPointerException          if the specified collection contains one
     *                                       or more null elements and this set does not permit null
     *                                       elements, or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this set
     * @see #add(Object)
     */
    @Override
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
            changed |= addInt((int) o);
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all of the elements from this set (optional operation).
     * The set will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} method
     *                                       is not supported by this set
     */
    @Override
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
                hash += probing.equals(Probing.Linear) ? step : step * step;
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
        Linear, Quadratic
    }
}

