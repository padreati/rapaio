/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package rapaio.util.collection;

import java.io.Serial;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import rapaio.util.IntComparator;
import rapaio.util.IntIterator;

public class IntArrayList implements RandomAccess, Cloneable, java.io.Serializable {

    @Serial
    private static final long serialVersionUID = -7046029254386353130L;

    /**
     * The initial default capacity of an array list.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 10;

    /**
     * The backing array.
     */
    protected transient int[] data;

    /**
     * The current actual size of the list (never greater than the backing-array length).
     */
    protected int size;

    /**
     * Creates a new array list with given capacity.
     *
     * @param capacity the initial capacity of the array list (may be 0).
     */

    public IntArrayList(final int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
        }
        if (capacity == 0) {
            data = IntArrays.EMPTY_ARRAY;
        } else {
            data = new int[capacity];
        }
    }

    /**
     * Creates a new array list with {@link #DEFAULT_INITIAL_CAPACITY} capacity.
     */

    public IntArrayList() {
        data = IntArrays.DEFAULT_EMPTY_ARRAY; // We delay allocation
    }

    /**
     * Creates a new array list and fills it with a given collection.
     *
     * @param c a collection that will be used to fill the array list.
     */
    public IntArrayList(final Collection<? extends Integer> c) {
        this(c.size());
        int pos = 0;
        for (int value : c) {
            data[pos++] = value;
        }
        size = c.size();
    }

    /**
     * Creates a new array list and fills it with the elements of a given array.
     *
     * @param a an array whose elements will be used to fill the array list.
     */
    public IntArrayList(final int[] a) {
        this(a, 0, a.length);
    }

    /**
     * Creates a new array list and fills it with the elements of a given array.
     *
     * @param a      an array whose elements will be used to fill the array list.
     * @param offset the first element to use.
     * @param length the number of elements to use.
     */
    public IntArrayList(final int[] a, final int offset, final int length) {
        this(length);
        System.arraycopy(a, offset, this.data, 0, length);
        size = length;
    }

    /**
     * Creates a new array list and fills it with the elements returned by an iterator..
     *
     * @param i an iterator whose returned elements will fill the array list.
     */
    public IntArrayList(final Iterator<? extends Integer> i) {
        this();
        while (i.hasNext()) this.add((i.next()).intValue());
    }

    /**
     * Creates a new array list and fills it with the elements returned by a type-specific iterator..
     *
     * @param i a type-specific iterator whose returned elements will fill the array list.
     */
    public IntArrayList(final IntIterator i) {
        this();
        while (i.hasNext()) this.add(i.nextInt());
    }

    /**
     * Returns the backing array of this list.
     *
     * @return the backing array.
     */
    public int[] elements() {
        return data;
    }

    /**
     * Wraps a given array into an array list of given size.
     *
     * <p>Note it is guaranteed
     * that the type of the array returned by {@link #elements()} will be the same
     * (see the comments in the class documentation).
     *
     * @param a      an array to wrap.
     * @param length the length of the resulting array list.
     * @return a new array list of the given size, wrapping the given array.
     */
    public static IntArrayList wrap(final int[] a, final int length) {
        if (length > a.length)
            throw new IllegalArgumentException("The specified length (" + length + ") is greater than the array size (" + a.length + ")");
        final IntArrayList l = new IntArrayList(a);
        l.size = length;
        return l;
    }

    /**
     * Wraps a given array into an array list.
     *
     * <p>Note it is guaranteed
     * that the type of the array returned by {@link #elements()} will be the same
     * (see the comments in the class documentation).
     *
     * @param a an array to wrap.
     * @return a new array list wrapping the given array.
     */
    public static IntArrayList wrap(final int[] a) {
        return wrap(a, a.length);
    }

    /**
     * Ensures that this array list can contain the given number of entries without resizing.
     *
     * @param capacity the new minimum capacity for this array list.
     */

    public void ensureCapacity(final int capacity) {
        if (capacity <= data.length || (size == 0 && capacity <= DEFAULT_INITIAL_CAPACITY)) return;
        data = IntArrays.ensureCapacity(data, capacity, size);
        assert size <= data.length;
    }

    /**
     * Grows this array list, ensuring that it can contain the given number of entries without resizing,
     * and in case increasing the current capacity at least by a factor of 50%.
     *
     * @param capacity the new minimum capacity for this array list.
     */

    private void grow(int capacity) {
        if (capacity <= data.length) return;
        if (size == 0) {
            capacity = (int) Math.max(Math.min((long) data.length + (data.length >> 1), TArrays.MAX_ARRAY_SIZE), capacity);
        } else if (capacity < DEFAULT_INITIAL_CAPACITY) {
            capacity = DEFAULT_INITIAL_CAPACITY;
        }
        data = IntArrays.forceCapacity(data, capacity, size);
        assert size <= data.length;
    }

    protected void ensureIndex(final int index) {
        if (index < 0) throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
        if (index > size()) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + (size()) + ")");
    }

    public boolean containsInt(int value) {
        return indexOf(value) >= 0;
    }

    public void add(final int index, final int k) {
        ensureIndex(index);
        grow(size + 1);
        if (index != size) System.arraycopy(data, index, data, index + 1, size - index);
        data[index] = k;
        size++;
        assert size <= data.length;
    }

    public void add(final int k) {
        grow(size + 1);
        data[size++] = k;
    }

    public int getInt(final int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        return data[index];
    }

    public int indexOf(final int k) {
        for (int i = 0; i < size; i++) if (((k) == (data[i]))) return i;
        return -1;
    }

    public int lastIndexOf(final int k) {
        for (int i = size; i-- != 0; ) if (((k) == (data[i]))) return i;
        return -1;
    }

    public int removeInt(final int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        final int old = data[index];
        size--;
        if (index != size) System.arraycopy(data, index + 1, data, index, size - index);
        assert size <= data.length;
        return old;
    }

    public boolean rem(final int k) {
        int index = indexOf(k);
        if (index == -1) return false;
        removeInt(index);
        assert size <= data.length;
        return true;
    }

    public int set(final int index, final int k) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        int old = data[index];
        data[index] = k;
        return old;
    }

    public void clear() {
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Trims this array list so that the capacity is equal to the size.
     *
     * @see java.util.ArrayList#trimToSize()
     */
    public void trim() {
        trim(0);
    }

    /**
     * Trims the backing array if it is too large.
     * <p>
     * If the current array length is smaller than or equal to
     * {@code n}, this method does nothing. Otherwise, it trims the
     * array length to the maximum between {@code n} and {@link #size()}.
     *
     * <p>This method is useful when reusing lists.  {@linkplain #clear() Clearing a
     * list} leaves the array length untouched. If you are reusing a list
     * many times, you can call this method with a typical
     * size to avoid keeping around a very large array just
     * because of a few large transient lists.
     *
     * @param n the threshold for the trimming.
     */

    public void trim(final int n) {
        // TODO: use Arrays.trim() and preserve type only if necessary
        if (n >= data.length || size == data.length) return;
        final int[] t = new int[Math.max(n, size)];
        System.arraycopy(data, 0, t, 0, size);
        data = t;
        assert size <= data.length;
    }

    /**
     * Copies element of this type-specific list into the given array using optimized system calls.
     *
     * @param from   the start index (inclusive).
     * @param a      the destination array.
     * @param offset the offset into the destination array where to store the first element copied.
     * @param length the number of elements to be copied.
     */
    public void getElements(final int from, final int[] a, final int offset, final int length) {
        IntArrays.ensureOffsetLength(a, offset, length);
        System.arraycopy(this.data, from, a, offset, length);
    }

    /**
     * Removes elements of this type-specific list using optimized system calls.
     *
     * @param from the start index (inclusive).
     * @param to   the end index (exclusive).
     */
    public void removeElements(final int from, final int to) {
        TArrays.ensureFromTo(size, from, to);
        System.arraycopy(data, to, data, from, size - to);
        size -= (to - from);
    }

    /**
     * Adds elements to this type-specific list using optimized system calls.
     *
     * @param index  the index at which to add elements.
     * @param a      the array containing the elements.
     * @param offset the offset of the first element to add.
     * @param length the number of elements to add.
     */
    public void addElements(final int index, final int[] a, final int offset, final int length) {
        ensureIndex(index);
        IntArrays.ensureOffsetLength(a, offset, length);
        grow(size + length);
        System.arraycopy(this.data, index, this.data, index + length, size - index);
        System.arraycopy(a, offset, this.data, index, length);
        size += length;
    }

    /**
     * Sets elements to this type-specific list using optimized system calls.
     *
     * @param index  the index at which to start setting elements.
     * @param a      the array containing the elements.
     * @param offset the offset of the first element to add.
     * @param length the number of elements to add.
     */
    public void setElements(final int index, final int[] a, final int offset, final int length) {
        ensureIndex(index);
        IntArrays.ensureOffsetLength(a, offset, length);
        if (index + length > size)
            throw new IndexOutOfBoundsException("End index (" + (index + length) + ") is greater than list size (" + size + ")");
        System.arraycopy(a, offset, this.data, index, length);
    }

    public int[] toArray(int[] a) {
        if (a == null || a.length < size) a = new int[size];
        System.arraycopy(this.data, 0, a, 0, size);
        return a;
    }

    public boolean removeAll(final Collection<?> c) {
        final int[] a = this.data;
        int j = 0;
        for (int i = 0; i < size; i++)
            if (!c.contains(a[i])) a[j++] = a[i];
        final boolean modified = size != j;
        size = j;
        return modified;
    }

    public IntIterator iterator() {
        return iterator(0);
    }

    public IntIterator iterator(final int index) {
        ensureIndex(index);
        return new IntIterator() {
            int pos = index, last = -1;

            @Override
            public boolean hasNext() {
                return pos < size;
            }

            @Override
            public int nextInt() {
                if (!hasNext()) throw new NoSuchElementException();
                return data[last = pos++];
            }

            @Override
            public void remove() {
                if (last == -1) throw new IllegalStateException();
                IntArrayList.this.removeInt(last);
                /* If the last operation was a next(), we are removing an element *before* us, and we must decrease pos correspondingly. */
                if (last < pos) pos--;
                last = -1;
            }
        };
    }

    public void sort(final IntComparator comp) {
        if (comp == null) {
            IntArrays.quickSort(data, 0, size);
        } else {
            IntArrays.quickSort(data, 0, size, comp);
        }
    }

    @Override
    public IntArrayList clone() throws CloneNotSupportedException {
        super.clone();
        IntArrayList c = new IntArrayList(size);
        System.arraycopy(data, 0, c.data, 0, size);
        c.size = size;
        return c;
    }

    /**
     * Compares this type-specific array list to another one.
     *
     * <p>This method exists only for sake of efficiency. The implementation
     * inherited from the abstract implementation would already work.
     *
     * @param l a type-specific array list.
     * @return true if the argument contains the same elements of this type-specific array list.
     */
    public boolean equalArray(final IntArrayList l) {
        if (l == this) return true;
        int s = size();
        if (s != l.size()) return false;
        final int[] a1 = data;
        final int[] a2 = l.data;
        while (s-- != 0) if (a1[s] != a2[s]) return false;
        return true;
    }

    /**
     * Compares this array list to another array list.
     *
     * <p>This method exists only for sake of efficiency. The implementation
     * inherited from the abstract implementation would already work.
     *
     * @param l an array list.
     * @return a negative integer,
     * zero, or a positive integer as this list is lexicographically less than, equal
     * to, or greater than the argument.
     */

    public int compareTo(final IntArrayList l) {
        final int s1 = size(), s2 = l.size();
        final int[] a1 = data, a2 = l.data;
        int e1, e2;
        int r, i;
        for (i = 0; i < s1 && i < s2; i++) {
            e1 = a1[i];
            e2 = a2[i];
            if ((r = (Integer.compare((e1), (e2)))) != 0) return r;
        }
        return i < s2 ? -1 : (i < s1 ? 1 : 0);
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        for (int i = 0; i < size; i++) s.writeInt(data[i]);
    }

    @Serial
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        data = new int[size];
        for (int i = 0; i < size; i++) data[i] = s.readInt();
    }
}
