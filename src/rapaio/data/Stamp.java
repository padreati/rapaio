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

import java.util.Arrays;

/**
 * Variable which stores long 64-bit integer values.
 * Basically the algorithms uses double for computations, so any
 * usage of stamps would fail to work. However, for some certain
 * necessary and specific usage scenarios this type of variable
 * is useful. One plausible scenario is the representation on
 * time stamps.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Stamp extends AbstractVar {

    public static final long MISSING_VALUE = Long.MIN_VALUE;
    private long[] data;
    private int rows;

    // static builders

    /**
     * Builds an empty stamp var of size 0
     *
     * @return new instance of stamp var
     */
    public static Stamp newEmpty() {
        return new Stamp(0, 0, MISSING_VALUE);
    }

    /**
     * Builds a stamp var of given size with missing values
     *
     * @param rows var size
     * @return new instance of stamp var
     */
    public static Stamp newEmpty(int rows) {
        return new Stamp(rows, rows, MISSING_VALUE);
    }

    /**
     * Builds a stamp var of size 1 with given fill value
     *
     * @param value fill value
     * @return new instance of stamp var
     */
    public static Stamp newScalar(long value) {
        return new Stamp(1, 1, value);
    }

    /**
     * Builds a stamp var of given size with given fill value
     *
     * @param rows  var size
     * @param value fill value
     * @return new instance of stamp var
     */
    public static Stamp newFill(int rows, long value) {
        return new Stamp(rows, rows, value);
    }

    /**
     * Builds a stamp var with values copied from given array
     *
     * @param values array of value
     * @return new instance of stamp var
     */
    public static Stamp newCopyOf(long... values) {
        Stamp stamp = new Stamp(0, 0, 0);
        stamp.data = Arrays.copyOf(values, values.length);
        stamp.rows = values.length;
        return stamp;
    }

    /**
     * Builds a stamp var as a wrapper over the arrat of long values
     *
     * @param values wrapped array of values
     * @return new instance of stamp var
     */
    public static Stamp newWrapOf(long... values) {
        Stamp stamp = new Stamp(0, 0, 0);
        stamp.data = values;
        stamp.rows = values.length;
        return stamp;
    }

    /**
     * Builds a stamp var with values as a sequence in increasing order starting with 0 and of given length
     *
     * @param len size of the var
     * @return new instance of stamp var
     */
    public static Stamp newSeq(int len) {
        return newSeq(0, len, 1);
    }

    /**
     * Builds a stamp var with values as an increasing sequence starting with a given start point and of given length
     *
     * @param start start value
     * @param len   size of the var
     * @return new instance of stamp var
     */
    public static Stamp newSeq(long start, int len) {
        return newSeq(start, len, 1);
    }

    /**
     * Builds a stamp var with values as an increasing sequence of values with a given start,
     * of a given length and with a given step increment value
     *
     * @param start start of the sequence
     * @param len   size of the sequence
     * @param step  step/increment value
     * @return new instance of stamp var
     */
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

    @Override
    public Stamp withName(String name) {
        return (Stamp) super.withName(name);
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
    public int rowCount() {
        return rows;
    }

    @Override
    public Var bindRows(Var var) {
        return BoundVar.newFrom(this, var);
    }

    @Override
    public Var mapRows(Mapping mapping) {
        return MappedVar.newByRows(this, mapping);
    }

    @Override
    public int index(int row) {
        return (int) stamp(row);
    }

    @Override
    public void setIndex(int row, int value) {
        setStamp(row, value);
    }

    @Override
    public void addIndex(int value) {
        addStamp(value);
    }

    @Override
    public double value(int row) {
        return stamp(row);
    }

    @Override
    public void setValue(int row, double value) {
        setStamp(row, (long) Math.rint(value));
    }

    @Override
    public void addValue(double value) {
        addStamp((long) Math.rint(value));
    }

    @Override
    public String label(int row) {
        return String.valueOf(stamp(row));
    }

    @Override
    public void setLabel(int row, String value) {
        setStamp(row, Long.parseLong(value));
    }

    @Override
    public void addLabel(String value) {
        addStamp(Long.parseLong(value));
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
        if (stamp(row) == 1) return true;
        if (stamp(row) == 0) return false;
        throw new IllegalArgumentException("Stamp value could not be represented as binary value");
    }

    @Override
    public void setBinary(int row, boolean value) {
        setStamp(row, value ? 1 : 0);
    }

    @Override
    public void addBinary(boolean value) {
        addStamp(value ? 1 : 0);
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
        if (numMoved > 0) {
            System.arraycopy(data, row + 1, data, row, numMoved);
            rows--;
        }
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
