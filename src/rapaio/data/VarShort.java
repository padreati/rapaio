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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/7/18.
 */
@Deprecated
public class VarShort extends AbstractVar {

    public static VarShort empty(int rows) {
        return new VarShort(rows, rows, MISSING_VALUE);
    }

    // private

    private static final short MISSING_VALUE = Short.MIN_VALUE;
    private short[] data;
    private int rows;

    private VarShort(int rows, int capacity, short fill) {
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new short[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    public static Collector<? super Integer, VarInt, VarInt> collector() {
        return new Collector<Integer, VarInt, VarInt>() {
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
                return VarInt::solidCopy;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    @Override
    public VarShort withName(String name) {
        return (VarShort) super.withName(name);
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
    public VarType type() {
        return VarType.SHORT;
    }

    @Override
    public int rowCount() {
        return rows;
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
        return data[row];
    }

    @Override
    public void setInt(int row, int value) {
        data[row] = (short)value;
    }

    @Override
    public void addInt(int value) {
        ensureCapacityInternal(rows + 1);
        data[rows] = (short)value;
        rows++;
    }

    @Override
    public double getDouble(int row) {
        if (isMissing(row))
            return Double.NaN;
        return getInt(row);
    }

    @Override
    public void setDouble(int row, double value) {
        setInt(row, (int) Math.rint(value));
    }

    @Override
    public void addDouble(double value) {
        addInt((int) Math.rint(value));
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
        setInt(row, Short.parseShort(value));
    }

    @Override
    public void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        addInt(Short.parseShort(value));
    }

    @Override
    public List<String> levels() {
        TreeSet<Integer> distinctValues = new TreeSet<>();
        for (int i = 0; i < rowCount(); i++) {
            if (isMissing(i))
                continue;
            distinctValues.add(getInt(i));
        }
        List<String> levels = new ArrayList<>();
        levels.add("?");
        for (Integer value : distinctValues) {
            levels.add(String.valueOf(value));
        }
        return levels;
    }

    @Override
    public void setLevels(String[] dict) {
        throw new IllegalArgumentException("Operation not available for index vectors.");
    }

    @Override
    public boolean getBoolean(int row) {
        return getInt(row) == 1;
    }

    @Override
    public void setBoolean(int row, boolean value) {
        setInt(row, value ? 1 : 0);
    }

    @Override
    public void addBoolean(boolean value) {
        addInt(value ? 1 : 0);
    }

    @Override
    public long getLong(int row) {
        return getInt(row);
    }

    @Override
    public void setLong(int row, long value) {
        setInt(row, Integer.valueOf(String.valueOf(value)));
    }

    @Override
    public void addLong(long value) {
        addInt(Integer.valueOf(String.valueOf(value)));
    }

    @Override
    public boolean isMissing(int row) {
        return getInt(row) == MISSING_VALUE;
    }

    @Override
    public void setMissing(int row) {
        setInt(row, MISSING_VALUE);
    }

    @Override
    public void addMissing() {
        addInt(MISSING_VALUE);
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
        return VarShort.empty(rows);
    }

    @Override
    public String toString() {
        return "VarShort[name:" + name() + ", rowCount:" + rowCount() + "]";
    }

    @Override
    public VarShort solidCopy() {
        return (VarShort) super.solidCopy();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(rowCount());
        for (int i = 0; i < rowCount(); i++) {
            out.writeShort(data[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        rows = in.readInt();
        data = new short[rows];
        for (int i = 0; i < rows; i++) {
            data[i] = in.readShort();
        }
    }
}
