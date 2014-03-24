/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data;

import rapaio.data.mapping.Mapping;

import java.util.Arrays;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class Index extends AbstractVector {

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final int DEFAULT_CAPACITY = 10;
    private static final int[] EMPTY_DATA = {};

    private static final int MISSING_VALUE = Integer.MIN_VALUE;
    private int[] data;
    private int rows;

    public Index() {
        this(0, 0, 0);
    }

    public Index(int rows, int capacity, int fill) {
        super();
        if (capacity < 0) {
            throw new IllegalArgumentException("Illegal capacity: " + capacity);
        }
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        if (rows > capacity) {
            throw new IllegalArgumentException(
                    "Illegal row count" + rows + " less than capacity:" + capacity);
        }
        this.data = new int[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    public Index(int[] values) {
        data = Arrays.copyOf(values, values.length);
        this.rows = values.length;
    }

    private void ensureCapacityInternal(int minCapacity) {
        if (data == EMPTY_DATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        if (minCapacity - data.length > 0)
            grow(minCapacity);
    }

    @Override
    public VectorType type() {
        return VectorType.INDEX;
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = data.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        data = Arrays.copyOf(data, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    private void rangeCheck(int index) {
        if (index > rows || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + rows;
    }

    @Override
    public boolean isMappedVector() {
        return false;
    }

    @Override
    public Vector source() {
        return this;
    }

    @Override
    public Mapping mapping() {
        return null;
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int rowId(int row) {
        return row;
    }

    @Override
    public int getIndex(int row) {
        return data[row];
    }

    @Override
    public void setIndex(int row, int value) {
        data[row] = value;
    }

    @Override
    public void addIndex(int value) {
        ensureCapacityInternal(rows + 1);
        data[rows++] = value;
    }

    @Override
    public void addIndex(int index, int value) {
        rangeCheck(index);

        ensureCapacityInternal(rows + 1);
        System.arraycopy(data, index, data, index + 1, rows - index);
        data[index] = value;
        rows++;
    }

    @Override
    public double getValue(int row) {
        return getIndex(row);
    }

    @Override
    public void setValue(int row, double value) {
        setIndex(row, (int) Math.rint(value));
    }

    @Override
    public void addValue(double value) {
        addIndex((int) Math.rint(value));
    }

    @Override
    public void addValue(int row, double value) {
        addIndex(row, (int) Math.rint(value));
    }

    @Override
    public String getLabel(int row) {
        return "";
    }

    @Override
    public void setLabel(int row, String value) {
        throw new RuntimeException("Operation not available for getIndex vectors.");
    }

    @Override
    public void addLabel(String value) {
        throw new RuntimeException("Operation not available for getIndex vectors.");
    }

    @Override
    public void addLabel(int row, String value) {
        throw new RuntimeException("Operation not available for getIndex vectors.");
    }

    @Override
    public String[] getDictionary() {
        throw new RuntimeException("Operation not available for getIndex vectors.");
    }

    @Override
    public void setDictionary(String[] dict) {
        throw new RuntimeException("Operation not available for getIndex vectors.");
    }

    @Override
    public boolean isMissing(int row) {
        return getIndex(row) == MISSING_VALUE;
    }

    @Override
    public void setMissing(int row) {
        setIndex(row, MISSING_VALUE);
    }

    @Override
    public void addMissing() {
        addIndex(MISSING_VALUE);
    }

    @Override
    public void remove(int index) {
        rangeCheck(index);
        int numMoved = rows - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
    }

    @Override
    public void clear() {
        rows = 0;
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        int minExpand = (data != EMPTY_DATA) ? 0 : DEFAULT_CAPACITY;
        if (minCapacity > minExpand && minCapacity - data.length > 0)
            grow(minCapacity);
    }

    @Override
    public Index solidCopy() {
        Index copy = new Index(rowCount(), rowCount(), 0);
        for (int i = 0; i < rowCount(); i++) {
            copy.setIndex(i, getIndex(i));
        }
        return copy;
    }

    @Override
    public String toString() {
        return "Index[" + rowCount() + "]";
    }
}
