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
public class Index extends AbstractVar {

    private static final int MISSING_VALUE = Integer.MIN_VALUE;
    private int[] data;
    private int rows;

    // static builders

    public static Index newEmpty() {
        return new Index(0, 0, 0);
    }

    public static Index newEmpty(int rows) {
        return new Index(rows, rows, 0);
    }

    public static Index newScalar(int value) {
        return new Index(1, 1, value);
    }

    public static Index newFill(int rows, int value) {
        return new Index(rows, rows, value);
    }

    public static Index newCopyOf(int[] values) {
        Index index = new Index(0, 0, 0);
        index.data = Arrays.copyOf(values, values.length);
        index.rows = values.length;
        return index;
    }

    public static Index newWrapOf(int[] values) {
        Index index = new Index(0, 0, 0);
        index.data = values;
        index.rows = values.length;
        return index;
    }

    public static Index newSeq(int len) {
        return newSeq(0, len, 1);
    }

    public static Index newSeq(int start, int len) {
        return newSeq(start, len, 1);
    }

    public static Index newSeq(final int start, final int len, final int step) {
        Index index = new Index(len, len, 0);
        int s = start;
        for (int i = 0; i < len; i++) {
            index.data[i] = s;
            s = s + step;
        }
        return index;
    }

    // private constructor, only public static builders available

    private Index(int rows, int capacity, int fill) {
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

    private void ensureCapacityInternal(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = data.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        // minCapacity is usually close to size, so this is a win:
        data = Arrays.copyOf(data, newCapacity);
    }

    @Override
    public VarType type() {
        return VarType.INDEX;
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
    public Var source() {
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
    public int index(int row) {
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
    public double value(int row) {
        return index(row);
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
    public String label(int row) {
        return "";
    }

    @Override
    public void setLabel(int row, String value) {
        throw new RuntimeException("Operation not available for index vectors.");
    }

    @Override
    public void addLabel(String value) {
        throw new RuntimeException("Operation not available for index vectors.");
    }

    @Override
    public String[] dictionary() {
        throw new RuntimeException("Operation not available for index vectors.");
    }

    @Override
    public void setDictionary(String[] dict) {
        throw new RuntimeException("Operation not available for index vectors.");
    }

    @Override
    public boolean missing(int row) {
        return index(row) == MISSING_VALUE;
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
    public Index solidCopy() {
        Index copy = new Index(rowCount(), rowCount(), 0);
        for (int i = 0; i < rowCount(); i++) {
            copy.setIndex(i, index(i));
        }
        return copy;
    }

    @Override
    public String toString() {
        return "Index[" + rowCount() + "]";
    }
}
