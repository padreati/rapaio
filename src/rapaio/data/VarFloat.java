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

import it.unimi.dsi.fastutil.floats.Float2IntOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatAVLTreeSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.data.unique.UniqueRows;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/7/18.
 */
public class VarFloat extends AbstractVar {
    /**
     * @return new empty numeric variable of size 0
     */
    public static VarFloat empty() {
        return new VarFloat(0, 0, Float.NaN);
    }

    /**
     * Builds an empty numeric var wil all values set missing
     *
     * @param rows size of the variable
     * @return new instance of numeric var
     */
    public static VarFloat empty(int rows) {
        return new VarFloat(rows, rows, Float.NaN);
    }

    /**
     * Builds a numeric variable with values copied from given collection
     *
     * @param values given values
     * @return new instance of numeric variable
     */
    public static VarFloat copy(Collection<? extends Number> values) {
        final VarFloat numeric = new VarFloat(0, 0, Float.NaN);
        values.forEach(n -> numeric.addDouble(n.doubleValue()));
        return numeric;
    }

    /**
     * Builds a numeric variable with values copied from given array of integer values
     *
     * @param values given numeric values
     * @return new instance of numeric variable
     */
    public static VarFloat copy(int... values) {
        VarFloat numeric = new VarFloat(0, 0, 0);
        numeric.data = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            numeric.data[i] = values[i];
        }
        numeric.rows = values.length;
        return numeric;
    }

    /**
     * Builds new instance of numeric var with values copied from given array of doubles
     *
     * @param values given numeric values
     * @return new instance of numeric variable
     */
    public static VarFloat copy(float... values) {
        VarFloat numeric = new VarFloat(values.length, values.length, 0);
        for (int i = 0; i < values.length; i++) {
            numeric.data[i] = values[i];
        }
        return numeric;
    }

    /**
     * Builds new numeric variable with values copied from another numeric variable
     *
     * @param source source numeric var
     * @return new instance of numeric variable
     */
    public static VarFloat copy(Var source) {
        VarFloat numeric = new VarFloat(source.rowCount(), source.rowCount(), 0).withName(source.name());
        if (!(source instanceof VarFloat)) {
            for (int i = 0; i < source.rowCount(); i++) {
                numeric.setDouble(i, source.getDouble(i));
            }
        } else {
            numeric.data = Arrays.copyOf(((VarFloat) source).data, source.rowCount());
        }
        return numeric;
    }

    /**
     * Builds new numeric variable as a wrapper around an array of doubles
     *
     * @param values wrapped array of doubles
     * @return new instance of numeric variable
     */
    public static VarFloat wrap(float... values) {
        VarFloat numeric = new VarFloat(values.length, values.length, 0);
        for (int i = 0; i < values.length; i++) {
            numeric.data[i] = values[i];
        }
        numeric.rows = values.length;
        return numeric;
    }

    /**
     * Builds new numeric variable filled with 0
     *
     * @param rows size of the variable
     * @return new instance of numeric variable of given size and filled with 0
     */
    public static VarFloat fill(int rows) {
        return new VarFloat(rows, rows, 0);
    }

    /**
     * Builds new numeric variable filled with given fill value
     *
     * @param rows size of the variable
     * @param fill fill value used to set all the values
     * @return new instance of numeric variable of given size and filled with given value
     */
    public static VarFloat fill(int rows, float fill) {
        return new VarFloat(rows, rows, fill);
    }

    /**
     * Builds a numeric variable of size 1 filled with given value
     *
     * @param value fill value
     * @return new instance of numeric variable of size 1 and filled with given fill value
     */
    public static VarFloat scalar(float value) {
        return new VarFloat(1, 1, value);
    }

    public static VarFloat seq(float end) {
        return seq(0, end);
    }

    public static VarFloat seq(float start, float end) {
        return seq(start, end, 1.0);
    }

    public static VarFloat seq(double start, double end, double step) {
        VarFloat num = VarFloat.empty();
        int i = 0;
        while (start + i * step <= end) {
            num.addDouble(start + i * step);
            i++;
        }
        return num;
    }

    private static final float missingValue = Float.NaN;
    private float[] data;
    private int rows;


    // private constructor

    private VarFloat(int rows, int capacity, float fill) {
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new float[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    // stream collectors
    public static Collector<Float, VarFloat, VarFloat> collector() {

        return new Collector<>() {
            @Override
            public Supplier<VarFloat> supplier() {
                return VarFloat::empty;
            }

            @Override
            public BiConsumer<VarFloat, Float> accumulator() {
                return VarFloat::addDouble;
            }

            @Override
            public BinaryOperator<VarFloat> combiner() {
                return (x, y) -> {
                    y.stream().forEach(s -> x.addDouble(s.getDouble()));
                    return x;
                };
            }

            @Override
            public Function<VarFloat, VarFloat> finisher() {
                return VarFloat::solidCopy;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    @Override
    public VarFloat withName(String name) {
        return (VarFloat) super.withName(name);
    }

    @Override
    public VType type() {
        return VType.FLOAT;
    }

    private void ensureCapacity(int minCapacity) {
        minCapacity = Math.max(10, minCapacity);
        // overflow-conscious code
        if (minCapacity - data.length > 0) {
            // overflow-conscious code
            int oldCapacity = data.length;
            int newCapacity = oldCapacity > 0xFFFF ? oldCapacity << 1 : oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public void addRows(int rowCount) {
        ensureCapacity(this.rows + rowCount + 1);
        for (int i = 0; i < rowCount; i++) {
            data[rows + i] = missingValue;
        }
        rows += rowCount;
    }

    @Override
    public double getDouble(int row) {
        return data[row];
    }

    @Override
    public void setDouble(int row, double value) {
        data[row] = (float) value;
    }

    @Override
    public void addDouble(double value) {
        ensureCapacity(rows + 1);
        data[rows++] = (float) value;
    }

    @Override
    public int getInt(int row) {
        return (int) Math.rint(getDouble(row));
    }

    @Override
    public void setInt(int row, int value) {
        setDouble(row, value);
    }

    @Override
    public void addInt(int value) {
        ensureCapacity(rows + 1);
        data[rows++] = value;
    }

    @Override
    public String getLabel(int row) {
        if (isMissing(row))
            return "?";
        return String.valueOf(getDouble(row));
    }

    @Override
    public void setLabel(int row, String value) {
        if ("?".equals(value)) {
            setMissing(row);
            return;
        }
        if ("Inf".equals(value)) {
            setDouble(row, Float.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            setDouble(row, Float.NEGATIVE_INFINITY);
            return;
        }
        setDouble(row, Float.parseFloat(value));
    }

    @Override
    public void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        if ("Inf".equals(value)) {
            addDouble(Float.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            addDouble(Float.NEGATIVE_INFINITY);
            return;
        }
        addDouble(Float.parseFloat(value));
    }

    @Override
    public List<String> levels() {
        throw new RuntimeException("Operation not available for float variables.");
    }

    @Override
    public void setLevels(String[] dict) {
        throw new RuntimeException("Operation not available for float variables.");
    }

    @Override
    public boolean getBoolean(int row) {
        return getDouble(row) == 1.0;
    }

    @Override
    public void setBoolean(int row, boolean value) {
        setDouble(row, value ? 1 : 0);
    }

    @Override
    public void addBoolean(boolean value) {
        addDouble(value ? 1 : 0);
    }

    @Override
    public long getLong(int row) {
        return (long) Math.rint(getDouble(row));
    }

    @Override
    public void setLong(int row, long value) {
        setDouble(row, Double.valueOf(String.valueOf(value)));
    }

    @Override
    public void addLong(long value) {
        addDouble(Double.valueOf(String.valueOf(value)));
    }

    @Override
    public boolean isMissing(int row) {
        return !Double.isFinite(getDouble(row));
    }

    @Override
    public void setMissing(int row) {
        setDouble(row, missingValue);
    }

    @Override
    public void addMissing() {
        addDouble(missingValue);
    }

    @Override
    public void remove(int index) {
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
    public UniqueRows uniqueRows() {
        FloatAVLTreeSet set = new FloatAVLTreeSet();
        for (int i = 0; i < rows; i++) {
            set.add(data[i]);
        }
        int uniqueId = 0;
        Float2IntOpenHashMap uniqueKeys = new Float2IntOpenHashMap();
        for (float key : set) {
            uniqueKeys.put(key, uniqueId);
            uniqueId++;
        }
        Int2ObjectOpenHashMap<IntList> uniqueRowLists = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < rows; i++) {
            int id = uniqueKeys.get(data[i]);
            if (!uniqueRowLists.containsKey(id)) {
                uniqueRowLists.put(id, new IntArrayList());
            }
            uniqueRowLists.get(id).add(i);
        }
        return new UniqueRows(uniqueRowLists);
    }

    @Override
    public Var newInstance(int rows) {
        return VarFloat.empty(rows);
    }

    @Override
    public String toString() {
        return "VarFloat[name:" + name() + ", rowCount:" + rowCount() + "]";
    }

    @Override
    public VarFloat solidCopy() {
        return (VarFloat) super.solidCopy();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(rowCount());
        for (int i = 0; i < rowCount(); i++) {
            out.writeFloat(data[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        rows = in.readInt();
        data = new float[rows];
        for (int i = 0; i < rows; i++) {
            data[i] = in.readFloat();
        }
    }
}