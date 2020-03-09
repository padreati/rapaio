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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Variable which stores long 64-bit integer values.
 * Basically the algorithms uses double for computations, so any
 * usage of long values would fail to work. However, for some certain
 * necessary and specific usage scenarios this type of variable
 * is useful.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarLong extends AbstractVar {

    /**
     * Builds an empty long value variable of size 0
     *
     * @return new instance of long variable
     */
    public static VarLong empty() {
        return new VarLong(0, 0, MISSING_VALUE);
    }

    /**
     * Builds a long value variable of given size with missing values
     *
     * @param rows variable size
     * @return new instance of long value variable
     */
    public static VarLong empty(int rows) {
        return new VarLong(rows, rows, MISSING_VALUE);
    }

    /**
     * Builds a long value variable of size 1 with given fill value
     *
     * @param value fill value
     * @return new instance of long value variable
     */
    public static VarLong scalar(long value) {
        return new VarLong(1, 1, value);
    }

    /**
     * Builds a long value variable of given size with filled with 0
     *
     * @param rows variable size
     * @return new instance of long value variable
     */
    public static VarLong fill(int rows) {
        return new VarLong(rows, rows, 0);
    }

    /**
     * Builds a long value variable of given size with given fill value
     *
     * @param rows  variable size
     * @param value fill value
     * @return new instance of long value variable
     */
    public static VarLong fill(int rows, long value) {
        return new VarLong(rows, rows, value);
    }

    /**
     * Builds a long value variable with values copied from given array
     *
     * @param values array of value
     * @return new instance of long value variable
     */
    public static VarLong copy(int... values) {
        VarLong stamp = new VarLong(values.length, values.length, 0);
        for (int i = 0; i < values.length; i++) {
            stamp.data[i] = values[i];
        }
        return stamp;
    }

    /**
     * Builds a long value variable with values copied from given list
     *
     * @param values array of value
     * @return new instance of long value variable
     */
    public static VarLong copy(List<Integer> values) {
        VarLong stamp = new VarLong(values.size(), values.size(), 0);
        for (int i = 0; i < values.size(); i++) {
            stamp.data[i] = values.get(i);
        }
        return stamp;
    }

    /**
     * Builds a long value variable with values copied from given variable
     *
     * @param var source variable
     * @return new instance of long value variable
     */
    public static VarLong copy(Var var) {
        if (var instanceof VarLong) {
            VarLong stamp = VarLong.fill(var.rowCount());
            System.arraycopy(((VarLong) var).data, 0, stamp.data, 0, var.rowCount());
            return stamp;
        }
        VarLong stamp = new VarLong(var.rowCount(), var.rowCount(), 0);
        for (int i = 0; i < var.rowCount(); i++) {
            stamp.data[i] = var.getLong(i);
        }
        return stamp;
    }

    /**
     * Builds a long value variable with values copied from given array
     *
     * @param values array of value
     * @return new instance of long value variable
     */
    public static VarLong copy(long... values) {
        VarLong stamp = new VarLong(0, 0, 0);
        stamp.data = Arrays.copyOf(values, values.length);
        stamp.rows = values.length;
        return stamp;
    }

    /**
     * Builds a long value variable as a wrapper over the array of long values
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
     * Builds a long value variable with values as a sequence in increasing order starting with 0 and of given length
     *
     * @param len size of the var
     * @return new instance of long value variable
     */
    public static VarLong seq(int len) {
        return seq(0, len, 1);
    }

    /**
     * Builds a long value variable with values as an increasing sequence starting with a given start
     * point and of given length
     *
     * @param start start value
     * @param len   size of the variable
     * @return new instance of long value variable
     */
    public static VarLong seq(long start, int len) {
        return seq(start, len, 1);
    }

    /**
     * Builds a long value variable with values provided by a supplier
     *
     * @param rows     number of rows
     * @param supplier long value supplier
     * @return long value variable
     */
    public static VarLong from(int rows, Supplier<Long> supplier) {
        VarLong var = VarLong.empty();
        for (int i = 0; i < rows; i++) {
            var.addLong(supplier.get());
        }
        return var;
    }

    /**
     * Builds a long value variable with given values provided by a function
     * of row number
     *
     * @param rows     number of rows
     * @param supplier supplier function which produces a long value from row number
     * @return long value variable
     */
    public static VarLong from(int rows, Function<Integer, Long> supplier) {
        VarLong var = VarLong.empty();
        for (int i = 0; i < rows; i++) {
            var.addLong(supplier.apply(i));
        }
        return var;
    }

    /**
     * Builds a long value variable with given values provided by a function of transformation applied
     * to the values from the source variable
     *
     * @param source    source variable
     * @param transform transform function applied to values of the source variable
     * @return long value variable
     */
    public static VarLong from(Var source, Function<Long, Long> transform) {
        VarLong var = VarLong.empty();
        for (int i = 0; i < source.rowCount(); i++) {
            var.addLong(transform.apply(source.getLong(i)));
        }
        return var;
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

    public static final long MISSING_VALUE = Long.MAX_VALUE;
    private static final long serialVersionUID = -6387573611986137666L;
    private long[] data;
    private int rows;

    private VarLong(int rows, int capacity, long fill) {
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new long[capacity];
        this.rows = rows;
        if (fill != 0) {
            Arrays.fill(data, 0, rows, fill);
        }
    }

    public static Collector<Long, VarLong, VarLong> collector() {

        return new Collector<>() {
            @Override
            public Supplier<VarLong> supplier() {
                return VarLong::empty;
            }

            @Override
            public BiConsumer<VarLong, Long> accumulator() {
                return VarLong::addLong;
            }

            @Override
            public BinaryOperator<VarLong> combiner() {
                return (x, y) -> {
                    y.stream().forEach(s -> x.addLong(s.getLong()));
                    return x;
                };
            }

            @Override
            public Function<VarLong, VarLong> finisher() {
                return var -> var;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
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
    public VType type() {
        return VType.LONG;
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
    public MappedVar mapRows(Mapping mapping) {
        return MappedVar.byRows(this, mapping);
    }

    @Override
    public void addRows(int rowCount) {
        ensureCapacityInternal(this.rows + rowCount + 1);
        for (int i = 0; i < rowCount; i++) {
            data[rows + i] = MISSING_VALUE;
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
        if (isMissing(row)) {
            return "?";
        }
        return String.valueOf(getLong(row));
    }

    @Override
    public void setLabel(int row, String value) {
        if ("?".equals(value)) {
            setMissing(row);
            return;
        }
        setLong(row, Long.parseLong(value));
    }

    @Override
    public void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        addLong(Long.parseLong(value));
    }

    @Override
    public List<String> levels() {
        throw new IllegalArgumentException("Operation not available for long variable");
    }

    @Override
    public void setLevels(String[] dict) {
        throw new IllegalArgumentException("Operation not available for long variable");
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
    public void removeRow(int row) {
        int numMoved = rows - row - 1;
        if (numMoved > 0) {
            System.arraycopy(data, row + 1, data, row, numMoved);
            rows--;
        }
    }

    @Override
    public void clearRows() {
        rows = 0;
    }

    @Override
    public Var newInstance(int rows) {
        return VarLong.empty(rows);
    }

    @Override
    public VarLong copy() {
        return (VarLong) super.copy();
    }

    @Override
    protected String classNameInToString() {
        return "VarLong";
    }

    @Override
    protected int elementsInToString() {
        return 12;
    }

    public long[] getArray() {
        return data;
    }

    public void setArray(long[] values, int rowCount) {
        data = values;
        rows = rowCount;
    }

    @Override
    protected void textTablePutValue(TextTable tt, int i, int j, int row, Printer printer, POption<?>[] options) {
        tt.textRight(i, j, String.valueOf(getLong(row)));
    }
}
