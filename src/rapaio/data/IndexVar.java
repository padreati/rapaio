/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Builds a numeric variable which stores values as 32-bit integers.
 * There are two general usage scenarios: use variable as an
 * positive integer index or save storage for numeric
 * variables from Z loosing decimal precision.
 * <p>
 * Missing value is {@link Integer#MIN_VALUE}. Any use of this value in
 * add/set operations will lead to missing values.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class IndexVar extends AbstractVar {

    /**
     * Builds an empty index var of size 0
     *
     * @return new instance of index var
     */
    public static IndexVar empty() {
        return new IndexVar(0, 0, 0);
    }

    /**
     * Builds an index of given size filled with missing values
     *
     * @param rows index size
     * @return new instance of index var
     */
    public static IndexVar empty(int rows) {
        return new IndexVar(rows, rows, 0);
    }

    /**
     * Builds an index of size 1 filled with the given value
     *
     * @param value fill value
     * @return new instance of index var
     */
    public static IndexVar scalar(int value) {
        return new IndexVar(1, 1, value);
    }

    /**
     * Builds an index var of given size with given fill value
     *
     * @param rows  index size
     * @param value fill value
     * @return new instance of index var
     */
    public static IndexVar fill(int rows, int value) {
        return new IndexVar(rows, rows, value);
    }

    /**
     * Builds an index with values copied from a given array
     *
     * @param values given array of values
     * @return new instance of index var
     */
    public static IndexVar copy(int... values) {
        IndexVar index = new IndexVar(0, 0, 0);
        index.data = Arrays.copyOf(values, values.length);
        index.rows = values.length;
        return index;
    }

    /**
     * Builds an index as a wrapper over a given array of index values
     *
     * @param values given array of values
     * @return new instance of index var
     */
    public static IndexVar wrap(int... values) {
        IndexVar index = new IndexVar(0, 0, 0);
        index.data = values;
        index.rows = values.length;
        return index;
    }

    /**
     * Builds an index of given size as a ascending sequence starting with 0
     *
     * @param len size of the index
     * @return new instance of index var
     */
    public static IndexVar seq(int len) {
        return seq(0, len, 1);
    }

    /**
     * Builds an index of given size as ascending sequence with a given start value
     *
     * @param start start value
     * @param len   size of the index
     * @return new instance of index var
     */
    public static IndexVar seq(int start, int len) {
        return seq(start, len, 1);
    }

    /**
     * Builds an index of given size as ascending sequence with a given start value and a given step
     *
     * @param start start value
     * @param len   size of the index
     * @param step  increment value
     * @return new instance of index var
     */
    public static IndexVar seq(final int start, final int len, final int step) {
        IndexVar index = new IndexVar(len, len, 0);
        int s = start;
        for (int i = 0; i < len; i++) {
            index.data[i] = s;
            s = s + step;
        }
        return index;
    }

    // private constructor, only public static builders available

    public static IndexVar from(int len, Function<Integer, Integer> supplier) {
        IndexVar index = new IndexVar(len, len, 0);
        for (int i = 0; i < index.data.length; i++) {
            index.data[i] = supplier.apply(i);
        }
        return index;
    }

    private static final long serialVersionUID = -2809318697565282310L;

    private static final int MISSING_VALUE = Integer.MIN_VALUE;
    private int[] data;
    private int rows;

    // static builders

    private IndexVar(int rows, int capacity, int fill) {
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new int[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    public static Collector<? super Integer, IndexVar, IndexVar> collector() {
        return new Collector<Integer, IndexVar, IndexVar>() {
            @Override
            public Supplier<IndexVar> supplier() {
                return IndexVar::empty;
            }

            @Override
            public BiConsumer<IndexVar, Integer> accumulator() {
                return IndexVar::addIndex;
            }

            @Override
            public BinaryOperator<IndexVar> combiner() {
                return (x, y) -> {
                    y.stream().forEach(s -> x.addValue(s.getValue()));
                    return x;
                };
            }

            @Override
            public Function<IndexVar, IndexVar> finisher() {
                return IndexVar::solidCopy;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    @Override
    public IndexVar withName(String name) {
        return (IndexVar) super.withName(name);
    }

    private void ensureCapacityInternal(int minCapacity) {
        // overflow-conscious code
        if (minCapacity < data.length)
            return;
        int oldCapacity = data.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        // minCapacity is usually close to size, so this is a win:
        data = Arrays.copyOf(data, newCapacity);
    }

    @Override
    public VarType getType() {
        return VarType.INDEX;
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    @Override
    public void addRows(int rowCount) {
        ensureCapacityInternal(this.rows + rowCount + 1);
        for (int i = 0; i < rowCount; i++) {
            data[rows + i] = IndexVar.MISSING_VALUE;
        }
        rows += rowCount;
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
        data[rows] = value;
        rows++;
    }

    @Override
    public double getValue(int row) {
        if(isMissing(row))
            return Double.NaN;
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
    public String getLabel(int row) {
        if (isMissing(row))
            return "?";
        return String.valueOf(getIndex(row));
    }

    @Override
    public void setLabel(int row, String value) {
        if ("?".equals(value)) {
            setMissing(row);
            return;
        }
        setIndex(row, Integer.parseInt(value));
    }

    @Override
    public void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        addIndex(Integer.parseInt(value));
    }

    @Override
    public String[] getLevels() {
        throw new IllegalArgumentException("Operation not available for index vectors.");
    }

    @Override
    public void setLevels(String[] dict) {
        throw new IllegalArgumentException("Operation not available for index vectors.");
    }

    @Override
    public boolean getBinary(int row) {
        return getIndex(row) == 1;
    }

    @Override
    public void setBinary(int row, boolean value) {
        setIndex(row, value ? 1 : 0);
    }

    @Override
    public void addBinary(boolean value) {
        addIndex(value ? 1 : 0);
    }

    @Override
    public long getStamp(int row) {
        return getIndex(row);
    }

    @Override
    public void setStamp(int row, long value) {
        setIndex(row, Integer.valueOf(String.valueOf(value)));
    }

    @Override
    public void addStamp(long value) {
        addIndex(Integer.valueOf(String.valueOf(value)));
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
        if (index > rows || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + rows);
        int numMoved = rows - index - 1;
        if (numMoved > 0) {
            System.arraycopy(data, index + 1, data, index, numMoved);
            rows--;
        }
    }

    @Override
    public void clear() {
        rows = 0;
    }

    @Override
    public Var newInstance(int rows) {
        return IndexVar.empty(rows);
    }

    @Override
    public String toString() {
        return "Index[name:" + getName() + ", rowCount:" + getRowCount() + "]";
    }

    @Override
    public IndexVar solidCopy() {
        return (IndexVar) super.solidCopy();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(getRowCount());
        for (int i = 0; i < getRowCount(); i++) {
            out.writeInt(data[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        rows = in.readInt();
        data = new int[rows];
        for (int i = 0; i < rows; i++) {
            data[i] = in.readInt();
        }
    }

}
