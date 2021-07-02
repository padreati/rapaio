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

package rapaio.data;

import rapaio.core.RandomSource;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.util.IntIterator;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.Int2IntFunction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;

/**
 * Builds a numeric variable which stores values as 32-bit integers. There are two general usage scenarios:
 * use variable as an positive integer index or save storage for numeric variables from Z loosing decimal
 * precision.
 * <p>
 * Missing value is {@link Integer#MIN_VALUE}. Any use of this value in add/set operations will lead to missing values.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class VarInt extends AbstractVar implements Iterable<Integer> {

    /**
     * Builds an empty integer variable of size 0
     *
     * @return new instance of integer variable
     */
    public static VarInt empty() {
        return new VarInt(0, 0, 0);
    }

    /**
     * Builds an index of given size filled with missing values
     *
     * @param rows index size
     * @return new instance of integer variable
     */
    public static VarInt empty(int rows) {
        return new VarInt(rows, rows, MISSING_VALUE);
    }

    /**
     * Builds an index of size 1 filled with the given value
     *
     * @param value fill value
     * @return new instance of integer variable
     */
    public static VarInt scalar(int value) {
        return new VarInt(1, 1, value);
    }

    /**
     * Builds an integer variable of given size with given fill value
     *
     * @param rows  integer variable size
     * @param value fill value
     * @return new instance of integer variable
     */
    public static VarInt fill(int rows, int value) {
        return new VarInt(rows, rows, value);
    }

    /**
     * Builds an integer variable with values copied from a given array
     *
     * @param values given array of values
     * @return new instance of integer variable
     */
    public static VarInt copy(int... values) {
        VarInt index = new VarInt(0, 0, 0);
        index.data = Arrays.copyOf(values, values.length);
        index.rows = values.length;
        return index;
    }

    /**
     * Builds an integer variable as a wrapper over a given array of integer variable values
     *
     * @param values given array of values
     * @return new instance of integer variable
     */
    public static VarInt wrap(int... values) {
        VarInt index = new VarInt(0, 0, 0);
        index.data = values;
        index.rows = values.length;
        return index;
    }

    /**
     * Builds an integer variable of given size as a ascending sequence starting with 0
     *
     * @param len size of the integer variable
     * @return new instance of integer variable
     */
    public static VarInt seq(int len) {
        return seq(0, len, 1);
    }

    /**
     * Builds an integer variable of given size as ascending sequence with a given start value
     *
     * @param start start value
     * @param len   size of the integer variable
     * @return new instance of integer variable
     */
    public static VarInt seq(int start, int len) {
        return seq(start, len, 1);
    }

    /**
     * Builds an integer variable of given size as ascending sequence with a given start value and a given step
     *
     * @param start start value
     * @param len   size of the index
     * @param step  increment value
     * @return new instance of integer variable
     */
    public static VarInt seq(final int start, final int len, final int step) {
        VarInt index = new VarInt(len, len, 0);
        int s = start;
        for (int i = 0; i < len; i++) {
            index.data[i] = s;
            s = s + step;
        }
        return index;
    }

    /**
     * Builds an integer variable of given size with values provided by a supplier with row as parameter
     *
     * @param rows     number of rows
     * @param supplier integer value supplier
     * @return new integer variable
     */
    public static VarInt from(int rows, Int2IntFunction supplier) {
        VarInt index = new VarInt(rows, rows, 0);
        for (int i = 0; i < index.data.length; i++) {
            index.data[i] = supplier.applyAsInt(i);
        }
        return index;
    }

    @Serial
    private static final long serialVersionUID = -2809318697565282310L;
    public static final int MISSING_VALUE = Integer.MAX_VALUE;
    private int[] data;
    private int rows;

    private VarInt(int rows, int capacity, int fill) {
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new int[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    public static Collector<? super Integer, VarInt, VarInt> collector() {
        return new Collector<>() {
            @Override
            public Supplier<VarInt> supplier() {
                return VarInt::empty;
            }

            @Override
            public BiConsumer<VarInt, Integer> accumulator() {
                return VarInt::addInt;
            }

            @Override
            public BinaryOperator<VarInt> combiner() {
                return (x, y) -> {
                    y.stream().forEach(s -> x.addDouble(s.getDouble()));
                    return x;
                };
            }

            @Override
            public Function<VarInt, VarInt> finisher() {
                return VarInt::copy;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    @Override
    public VarInt name(String name) {
        return (VarInt) super.name(name);
    }

    private void ensureCapacityInternal(int minCapacity) {
        if (minCapacity < data.length)
            return;
        int oldCapacity = data.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        data = Arrays.copyOf(data, newCapacity);
    }

    @Override
    public VarType type() {
        return VarType.INT;
    }

    @Override
    public int size() {
        return rows;
    }

    public void rowCount(int rowCount) {
        if (rowCount <= rows) {
            rows = rowCount;
        } else {
            addRows(rowCount - rows);
        }
    }

    @Override
    public void addRows(int rowCount) {
        ensureCapacityInternal(this.rows + rowCount + 1);
        for (int i = 0; i < rowCount; i++) {
            data[rows + i] = VarInt.MISSING_VALUE;
        }
        rows += rowCount;
    }

    @Override
    public int getInt(int row) {
        return data[row];
    }

    @Override
    public void setInt(int row, int value) {
        data[row] = value;
    }

    @Override
    public void addInt(int value) {
        ensureCapacityInternal(rows + 1);
        data[rows] = value;
        rows++;
    }

    public void addAllInt(IntIterator it) {
        while (it.hasNext()) {
            addInt(it.nextInt());
        }
    }

    public IntIterator iterator() {
        return new VarIntIterator(this);
    }

    public IntIterator iterator(int start, int end) {
        return new VarIntIterator(this, start, end);
    }

    public IntStream intStream() {
        return Arrays.stream(data, 0, rows);
    }

    @Override
    public double getDouble(int row) {
        if (isMissing(row))
            return VarDouble.MISSING_VALUE;
        return getInt(row);
    }

    @Override
    public void setDouble(int row, double value) {
        data[row] = Double.isNaN(value) ? MISSING_VALUE : (int) Math.rint(value);
    }

    @Override
    public void addDouble(double value) {
        if (Double.isNaN(value)) {
            addMissing();
        } else {
            addInt((int) Math.rint(value));
        }
    }

    @Override
    public String getLabel(int row) {
        if (isMissing(row))
            return "?";
        return String.valueOf(getInt(row));
    }

    @Override
    public void setLabel(int row, String value) {
        if ("?".equals(value)) {
            setMissing(row);
            return;
        }
        setInt(row, Integer.parseInt(value));
    }

    @Override
    public void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        addInt(Integer.parseInt(value));
    }

    @Override
    public List<String> levels() {
        throw new IllegalStateException("Operation not available for integer variables.");
    }

    @Override
    public void setLevels(String[] dict) {
        throw new IllegalStateException("Operation not available for integer variables.");
    }

    @Override
    public long getLong(int row) {
        return getInt(row);
    }

    @Override
    public void setLong(int row, long value) {
        setInt(row, Integer.parseInt(String.valueOf(value)));
    }

    @Override
    public void addLong(long value) {
        addInt(Integer.parseInt(String.valueOf(value)));
    }

    @Override
    public void addInstant(Instant value) {
        if (value == VarInstant.MISSING_VALUE) {
            addMissing();
        } else {
            addInt((int) value.toEpochMilli());
        }
    }

    @Override
    public void setInstant(int row, Instant value) {
        if (value == VarInstant.MISSING_VALUE) {
            setMissing(row);
        } else {
            setInt(row, (int) value.toEpochMilli());
        }
    }

    @Override
    public Instant getInstant(int row) {
        if (isMissing(row)) {
            return VarInstant.MISSING_VALUE;
        }
        return Instant.ofEpochMilli(getInt(row));
    }

    @Override
    public boolean isMissing(int row) {
        return data[row] == MISSING_VALUE;
    }

    @Override
    public void setMissing(int row) {
        data[row] = MISSING_VALUE;
    }

    @Override
    public void addMissing() {
        addInt(MISSING_VALUE);
    }

    @Override
    public void removeRow(int index) {
        if (index > rows || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + rows);
        int numMoved = rows - index - 1;
        if (numMoved > 0) {
            System.arraycopy(data, index + 1, data, index, numMoved);
            rows--;
        }
    }

    @Override
    public void clearRows() {
        rows = 0;
    }

    @Override
    public VarInt newInstance(int rows) {
        return VarInt.empty(rows);
    }

    @Override
    public VarInt copy() {
        return (VarInt) super.copy();
    }

    public VarInt shuffle() {
        return shuffle(0, rows);
    }

    public VarInt shuffle(int start, int end) {
        IntArrays.shuffle(data, start, end, RandomSource.getRandom());
        return this;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(size());
        for (int i = 0; i < size(); i++) {
            out.writeInt(data[i]);
        }
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException {
        rows = in.readInt();
        data = new int[rows];
        for (int i = 0; i < rows; i++) {
            data[i] = in.readInt();
        }
    }

    @Override
    protected String toStringClassName() {
        return "VarInt";
    }

    @Override
    protected int toStringDisplayValueCount() {
        return 14;
    }

    @Override
    protected void textTablePutValue(TextTable tt, int i, int j, int row, Printer printer, POption<?>... options) {
        tt.textRight(i, j, getLabel(row));
    }

    public int[] elements() {
        return data;
    }

    public void setElements(int[] values, int rowCount) {
        data = values;
        rows = rowCount;
    }

    private static final class VarIntIterator implements IntIterator {

        private int pos;
        private final int start;
        private final int end;
        private final VarInt parent;

        public VarIntIterator(VarInt parent) {
            this(parent, 0, parent.rows);
        }

        public VarIntIterator(VarInt parent, int start, int end) {
            this.parent = parent;
            this.start = start;
            this.end = end;
            pos = start;
        }

        @Override
        public boolean hasNext() {
            return pos < end;
        }

        @Override
        public int nextInt() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return parent.data[pos++];
        }
    }
}
