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
 * Created by tutuianu on 7/10/14.
 */
@Deprecated
public class Stamp extends AbstractVar {

    private static final long MISSING_VALUE = Long.MIN_VALUE;
    private long[] data;
    private int rows;

    // static builders

    public static Stamp newEmpty() {
        return new Stamp(0, 0, 0);
    }

    public static Stamp newEmpty(int rows) {
        return new Stamp(rows, rows, 0);
    }

    public static Stamp newScalar(long value) {
        return new Stamp(1, 1, value);
    }

    public static Stamp newFill(int rows, long value) {
        return new Stamp(rows, rows, value);
    }

    public static Stamp newCopyOf(long[] values) {
        Stamp stamp = new Stamp(0, 0, 0);
        stamp.data = Arrays.copyOf(values, values.length);
        stamp.rows = values.length;
        return stamp;
    }

    public static Stamp newWrapOf(long[] values) {
        Stamp stamp = new Stamp(0, 0, 0);
        stamp.data = values;
        stamp.rows = values.length;
        return stamp;
    }

    public static Stamp newSeq(int len) {
        return newSeq(0, len, 1);
    }

    public static Stamp newSeq(long start, int len) {
        return newSeq(start, len, 1);
    }

    public static Stamp newSeq(final long start, final int len, final long step) {
        Stamp stamp = new Stamp(len, len, 0);
        long s = start;
        for (int i = 0; i < len; i++) {
            stamp.data[i] = s;
            s = s + step;
        }
        return stamp;
    }

    // private constructor, only public static builders available

    private Stamp(int rows, int capacity, long fill) {
        super();
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new long[capacity];
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
        return VarType.STAMP;
    }

    private void rangeCheck(int index) {
        if (index > rows || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Stamp: " + index + ", Size: " + rows;
    }

    @Override
    public boolean isMapped() {
        return false;
    }

    @Override
    public Var source() {
        return this;
    }

    @Override
    public Mapping mapping() {
        return Mapping.newSolidMap(rowCount());
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int index(int row) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public void setIndex(int row, int value) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public void addIndex(int value) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public double value(int row) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public void setValue(int row, double value) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public void addValue(double value) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public String label(int row) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public void setLabel(int row, String value) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public void addLabel(String value) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public String[] dictionary() {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public void setDictionary(String[] dict) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public boolean binary(int row) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public void setBinary(int row, boolean value) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public void addBinary(boolean value) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public long stamp(int row) {
        return data[row];
    }

    @Override
    public void setStamp(int row, long value) {
        data[row] = value;
    }

    @Override
    public void addStamp(long value) {
        ensureCapacityInternal(rows + 1);
        data[rows] = value;
        rows++;
    }

    @Override
    public boolean missing(int row) {
        return stamp(row) == MISSING_VALUE;
    }

    @Override
    public void setMissing(int row) {
        setStamp(row, MISSING_VALUE);
    }

    @Override
    public void addMissing() {
        addStamp(MISSING_VALUE);
    }

    @Override
    public void remove(int row) {
        rangeCheck(row);
        int numMoved = rows - row - 1;
        if (numMoved > 0)
            System.arraycopy(data, row + 1, data, row, numMoved);
    }

    @Override
    public void clear() {
        rows = 0;
    }

    @Override
    public Stamp solidCopy() {
        Stamp copy = new Stamp(rowCount(), rowCount(), 0);
        for (int i = 0; i < rowCount(); i++) {
            copy.setStamp(i, index(i));
        }
        return copy;
    }

    @Override
    public String toString() {
        return "Stamp[" + rowCount() + "]";
    }
}
