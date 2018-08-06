/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

package rapaio.data;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

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
public class VarLong extends AbstractVar {

    /**
     * Builds an empty stamp var of size 0
     *
     * @return new instance of stamp var
     */
    public static VarLong empty() {
        return new VarLong(0, 0, MISSING_VALUE);
    }

    /**
     * Builds a stamp var of given size with missing values
     *
     * @param rows var size
     * @return new instance of stamp var
     */
    public static VarLong empty(int rows) {
        return new VarLong(rows, rows, MISSING_VALUE);
    }

    /**
     * Builds a stamp var of size 1 with given fill value
     *
     * @param value fill value
     * @return new instance of stamp var
     */
    public static VarLong scalar(long value) {
        return new VarLong(1, 1, value);
    }

    /**
     * Builds a stamp var of given size with given fill value
     *
     * @param rows  var size
     * @param value fill value
     * @return new instance of stamp var
     */
    public static VarLong fill(int rows, long value) {
        return new VarLong(rows, rows, value);
    }

    /**
     * Builds a stamp var with values copied from given array
     *
     * @param values array of value
     * @return new instance of stamp var
     */
    public static VarLong copy(long... values) {
        VarLong stamp = new VarLong(0, 0, 0);
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
    public static VarLong wrap(long... values) {
        VarLong stamp = new VarLong(0, 0, 0);
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
    public static VarLong seq(int len) {
        return seq(0, len, 1);
    }

    /**
     * Builds a stamp var with values as an increasing sequence starting with a given start point and of given length
     *
     * @param start start value
     * @param len   size of the var
     * @return new instance of stamp var
     */
    public static VarLong seq(long start, int len) {
        return seq(start, len, 1);
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
    public static VarLong seq(final long start, final int len, final long step) {
        VarLong stamp = new VarLong(len, len, 0);
        long s = start;
        for (int i = 0; i < len; i++) {
            stamp.data[i] = s;
            s = s + step;
        }
        return stamp;
    }

    public static final long MISSING_VALUE = Long.MIN_VALUE;
    private static final long serialVersionUID = -6387573611986137666L;
    private long[] data;
    private int rows;

    // static builders

    private VarLong(int rows, int capacity, long fill) {
        super();
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new long[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    // private constructor, only public static builders available

    public static VarLong from(int rows, Supplier<Long> supplier) {
        VarLong var = VarLong.empty();
        for (int i = 0; i < rows; i++) {
            var.addLong(supplier.get());
        }
        return var;
    }

    @Override
    public VarLong withName(String name) {
        return (VarLong) super.withName(name);
    }

    private void ensureCapacityInternal(int minCapacity) {
        if (minCapacity <= data.length)
            return;
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
        return VarType.LONG;
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
        return BoundVar.from(this, var);
    }

    @Override
    public Var mapRows(Mapping mapping) {
        return MappedVar.byRows(this, mapping);
    }

    @Override
    public void addRows(int rowCount) {
        ensureCapacityInternal(this.rows + rowCount + 1);
        for (int i = 0; i < rowCount; i++) {
            data[rows + i] = VarLong.MISSING_VALUE;
        }
        rows += rowCount;
    }

    @Override
    public int getInt(int row) {
        return (int) getLong(row);
    }

    @Override
    public void setInt(int row, int value) {
        setLong(row, value);
    }

    @Override
    public void addInt(int value) {
        addLong(value);
    }

    @Override
    public double getDouble(int row) {
        return getLong(row);
    }

    @Override
    public void setDouble(int row, double value) {
        setLong(row, (long) Math.rint(value));
    }

    @Override
    public void addDouble(double value) {
        addLong((long) Math.rint(value));
    }

    @Override
    public String getLabel(int row) {
        return String.valueOf(getLong(row));
    }

    @Override
    public void setLabel(int row, String value) {
        setLong(row, Long.parseLong(value));
    }

    @Override
    public void addLabel(String value) {
        addLong(Long.parseLong(value));
    }

    @Override
    public List<String> levels() {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public void setLevels(String[] dict) {
        throw new IllegalArgumentException("Operation not available for stamp variable");
    }

    @Override
    public boolean getBoolean(int row) {
        if (getLong(row) == 1) return true;
        if (getLong(row) == 0) return false;
        throw new IllegalArgumentException("Stamp value could not be represented as binary value");
    }

    @Override
    public void setBoolean(int row, boolean value) {
        setLong(row, value ? 1 : 0);
    }

    @Override
    public void addBoolean(boolean value) {
        addLong(value ? 1 : 0);
    }

    @Override
    public long getLong(int row) {
        return data[row];
    }

    @Override
    public void setLong(int row, long value) {
        data[row] = value;
    }

    @Override
    public void addLong(long value) {
        ensureCapacityInternal(rows + 1);
        data[rows] = value;
        rows++;
    }

    @Override
    public boolean isMissing(int row) {
        return getLong(row) == MISSING_VALUE;
    }

    @Override
    public void setMissing(int row) {
        setLong(row, MISSING_VALUE);
    }

    @Override
    public void addMissing() {
        addLong(MISSING_VALUE);
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
    public Var newInstance(int rows) {
        return VarLong.empty(rows);
    }

    @Override
    public VarLong solidCopy() {
        return (VarLong) super.solidCopy();
    }

    @Override
    public String toString() {
        return "Stamp[" + rowCount() + "]";
    }
}
