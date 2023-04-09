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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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
import java.io.Serial;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import rapaio.data.stream.VSpot;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.math.linear.dense.DVectorVar;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POpt;

/**
 * Builds a numeric float variable. Float variables stores data as float values
 * and allows modelling of any type of continuous or discrete numeric variable.
 * <p>
 * The placeholder for missing value is Float.NaN. Any form of usage of Float.NaN
 * on set/add operation will result in a missing value.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class VarFloat extends AbstractVar implements Iterable<Float> {

    /**
     * @return new empty double variable of size 0
     */
    public static VarFloat empty() {
        return new VarFloat(0, 0, MISSING_VALUE);
    }

    /**
     * Builds an empty double var wil all values set missing
     *
     * @param rows size of the variable
     * @return new instance of numeric var
     */
    public static VarFloat empty(int rows) {
        return new VarFloat(rows, rows, MISSING_VALUE);
    }

    /**
     * Builds a double variable with values copied from given collection
     *
     * @param values given values
     * @return new instance of numeric variable
     */
    public static VarFloat copy(Collection<? extends Number> values) {
        float[] data = new float[values.size()];
        Iterator<? extends Number> it = values.iterator();
        int pos = 0;
        while (it.hasNext()) {
            data[pos++] = it.next().floatValue();
        }
        return VarFloat.wrapArray(data.length, data);
    }

    /**
     * Builds a double variable with values copied from given array of integer values
     *
     * @param values given numeric values
     * @return new instance of numeric variable
     */
    public static VarFloat copy(int... values) {
        float[] data = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            data[i] = values[i];
        }
        return VarFloat.wrapArray(values.length, data);
    }

    /**
     * Builds new instance of double var with values copied from given array of doubles
     *
     * @param values given numeric values
     * @return new instance of numeric variable
     */
    public static VarFloat copy(float... values) {
        VarFloat numeric = new VarFloat(values.length, values.length, 0);
        numeric.data = Arrays.copyOf(values, values.length);
        return numeric;
    }

    /**
     * Builds new double variable with values copied from another numeric variable
     *
     * @param source source numeric var
     * @return new instance of numeric variable
     */
    public static VarFloat copy(Var source) {
        VarFloat numeric = new VarFloat(source.size(), source.size(), 0).name(source.name());
        if (source instanceof VarFloat fsource) {
            float[] srcArray = fsource.data;
            System.arraycopy(srcArray, 0, numeric.data, 0, source.size());
        } else {
            for (int i = 0; i < source.size(); i++) {
                numeric.data[i] = source.getFloat(i);
            }
        }
        return numeric;
    }

    /**
     * Builds new double variable as a wrapper around an array of doubles
     *
     * @param values wrapped array of doubles
     * @return new instance of numeric variable
     */
    public static VarFloat wrap(float... values) {
        VarFloat numeric = new VarFloat(0, 0, 0);
        numeric.data = values;
        numeric.rows = values.length;
        return numeric;
    }

    /**
     * Builds new double variable as a wrapper around an array of doubles
     *
     * @param values wrapped array of doubles
     * @return new instance of numeric variable
     */
    public static VarFloat wrapArray(int size, float... values) {
        VarFloat numeric = new VarFloat(0, 0, 0);
        numeric.data = values;
        numeric.rows = size;
        return numeric;
    }

    /**
     * Builds new double variable filled with 0
     *
     * @param rows size of the variable
     * @return new instance of numeric variable of given size and filled with 0
     */
    public static VarFloat fill(int rows) {
        return new VarFloat(rows, rows, 0);
    }

    /**
     * Builds new double variable filled with given fill value
     *
     * @param rows size of the variable
     * @param fill fill value used to set all the values
     * @return new instance of double variable of given size and filled with given value
     */
    public static VarFloat fill(int rows, float fill) {
        return new VarFloat(rows, rows, fill);
    }

    /**
     * Builds a float variable of size 1 filled with given value
     *
     * @param value fill value
     * @return new instance of numeric variable of size 1 and filled with given fill value
     */
    public static VarFloat scalar(float value) {
        return new VarFloat(1, 1, value);
    }

    /**
     * Builds a double variable with values starting from 0, step 1
     * and ending inclusive with end
     *
     * @param end exclusive end of the sequence
     * @return new instance double variable filled with sequence
     */
    public static VarFloat seq(float end) {
        return seq(0, end);
    }

    /**
     * Builds a new float variable with values starting from start, step 1,
     * and ending inclusive with end
     *
     * @param start start of the sequence
     * @param end   inclusive end of the sequence
     * @return new instance double variable filled with sequence
     */
    public static VarFloat seq(float start, float end) {
        return seq(start, end, 1.0f);
    }

    /**
     * Builds a new float variable with values starting from start,
     * to inclusive end and given step.
     *
     * @param start inclusive start of the sequence
     * @param end   inclusive end of the sequence
     * @param step  step of the sequence
     * @return new instance double variable filled with sequence
     */
    public static VarFloat seq(float start, float end, float step) {
        VarFloat num = VarFloat.empty();
        int pos = 0;
        double current = start;
        while (current <= end) {
            num.addDouble(current);
            pos++;
            current = start + pos * step;
        }
        return num;
    }

    /**
     * Builds a new float variable from a float supplier functional.
     *
     * @param rows     number of rows
     * @param supplier supplier of double values
     * @return new instance of double values
     */
    public static VarFloat from(int rows, Supplier<Float> supplier) {
        VarFloat numeric = new VarFloat(0, 0, 0);
        numeric.data = new float[rows];
        numeric.rows = rows;
        for (int i = 0; i < rows; i++) {
            numeric.data[i] = supplier.get();
        }
        return numeric;
    }

    /**
     * Builds a new numeric variable of a given size and values produced by a function
     * which transforms a row number into a value by a given transformation function.
     *
     * @param rows           number of rows
     * @param transformation transformation function
     * @return new double variable which contains the computed values
     */
    public static VarFloat from(int rows, Function<Integer, Float> transformation) {
        VarFloat numeric = new VarFloat(0, 0, 0);
        numeric.data = new float[rows];
        numeric.rows = rows;
        for (int i = 0; i < rows; i++) {
            numeric.data[i] = transformation.apply(i);
        }
        return numeric;
    }

    /**
     * Builds a numeric variable as a transformation of another variable.
     * Each value from the source variable is transformed into a value of a destination variable.
     *
     * @param reference source variable which provides data
     * @param transform transformation applied to source variable
     * @return new numeric variable which contains transformed variables
     */
    public static VarFloat from(Var reference, Function<Float, Float> transform) {
        return VarFloat.from(reference.size(), i -> transform.apply(reference.getFloat(i)));
    }

    @Serial
    private static final long serialVersionUID = -3167416341273129670L;
    public static final float MISSING_VALUE = Float.NaN;
    private float[] data;
    private int rows;

    /**
     * Builds a double variable.
     *
     * @param rows     number of rows in the variable
     * @param capacity capacity of the array
     * @param fill     fill value for the array
     */
    private VarFloat(int rows, int capacity, float fill) {
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new float[capacity];
        this.rows = rows;
        if (fill != 0) {
            Arrays.fill(data, 0, rows, fill);
        }
    }

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
                    y.stream().forEach(s -> x.addFloat(s.getFloat()));
                    return x;
                };
            }

            @Override
            public Function<VarFloat, VarFloat> finisher() {
                return var -> var;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    @Override
    public VarFloat name(String name) {
        return (VarFloat) super.name(name);
    }

    @Override
    public VarType type() {
        return VarType.FLOAT;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > data.length) {
            int oldCapacity = data.length;
            int newCapacity = oldCapacity > 0xFFFF ? oldCapacity << 1 : oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    @Override
    public int size() {
        return rows;
    }

    public boolean isMissingValue(float value) {
        return Float.isNaN(value);
    }

    public float[] elements() {
        return data;
    }

    public void setElements(float[] values, int rowCount) {
        data = values;
        rows = rowCount;
    }

    @Override
    public void addRows(int rowCount) {
        ensureCapacity(rows + rowCount);
        Arrays.fill(data, rows, rows + rowCount, MISSING_VALUE);
        rows += rowCount;
    }

    @Override
    public void removeRow(int index) {
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
    public boolean isMissing(int row) {
        return Float.isNaN(data[row]);
    }

    @Override
    public void setMissing(int row) {
        setFloat(row, MISSING_VALUE);
    }

    @Override
    public void addMissing() {
        addFloat(MISSING_VALUE);
    }

    @Override
    public float getFloat(int row) {
        return data[row];
    }

    @Override
    public void setFloat(int row, float value) {
        data[row] = value;
    }

    @Override
    public void addFloat(float value) {
        ensureCapacity(rows + 1);
        data[rows++] = value;
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
        data[rows++] = (float)value;
    }

    @Override
    public int getInt(int row) {
        return (int) Math.rint(data[row]);
    }

    @Override
    public void setInt(int row, int value) {
        data[row] = value;
    }

    @Override
    public void addInt(int value) {
        ensureCapacity(rows + 1);
        data[rows++] = value;
    }

    @Override
    public String getLabel(int row) {
        return isMissing(row) ? VarNominal.MISSING_VALUE : String.valueOf(data[row]);
    }

    @Override
    public void setLabel(int row, String value) {
        if (VarNominal.MISSING_VALUE.equals(value)) {
            data[row] = Float.NaN;
            return;
        }
        if ("Inf".equals(value)) {
            data[row] = Float.POSITIVE_INFINITY;
            return;
        }
        if ("-Inf".equals(value)) {
            data[row] = Float.NEGATIVE_INFINITY;
            return;
        }
        data[row] = Float.parseFloat(value);
    }

    @Override
    public void addLabel(String value) {
        if (VarNominal.MISSING_VALUE.equals(value)) {
            addMissing();
            return;
        }
        if ("Inf".equals(value)) {
            addFloat(Float.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            addFloat(Float.NEGATIVE_INFINITY);
            return;
        }
        addFloat(Float.parseFloat(value));
    }

    @Override
    public List<String> levels() {
        throw new RuntimeException("Operation not available for double vectors.");
    }

    @Override
    public void setLevels(String[] dict) {
        throw new RuntimeException("Operation not available for double vectors.");
    }

    @Override
    public long getLong(int row) {
        return (long) Math.rint(data[row]);
    }

    @Override
    public void setLong(int row, long value) {
        data[row] = Float.parseFloat(String.valueOf(value));
    }

    @Override
    public void addLong(long value) {
        addDouble(Double.parseDouble(String.valueOf(value)));
    }

    @Override
    public void addInstant(Instant value) {
        if (value == VarInstant.MISSING_VALUE) {
            addMissing();
        } else {
            addFloat(value.toEpochMilli());
        }
    }

    @Override
    public void setInstant(int row, Instant value) {
        if (value == VarInstant.MISSING_VALUE) {
            setMissing(row);
        } else {
            setFloat(row, value.toEpochMilli());
        }
    }

    @Override
    public Instant getInstant(int row) {
        if (isMissing(row)) {
            return VarInstant.MISSING_VALUE;
        }
        return Instant.ofEpochMilli((long) data[row]);
    }

    @Override
    public Var newInstance(int rows) {
        return VarFloat.empty(rows);
    }

    @Override
    public VarFloat copy() {
        VarFloat copy = new VarFloat(0, 0, 0).name(name());
        copy.data = Arrays.copyOf(data, rows);
        copy.rows = rows;
        return copy;
    }

    @Override
    public DVector dv() {
        return new DVectorVar<>(this);
    }

    @Override
    public DVector dvNew() {
        double[] values = new double[size()];
        for (int i = 0; i < size(); i++) {
            values[i] = data[i];
        }
        return new DVectorDense(0, size(), values);
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(size());
        for (int i = 0; i < size(); i++) {
            out.writeFloat(data[i]);
        }
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException {
        rows = in.readInt();
        data = new float[rows];
        for (int i = 0; i < rows; i++) {
            data[i] = in.readFloat();
        }
    }

    @Override
    protected String toStringClassName() {
        return "VarFloat";
    }

    @Override
    protected int toStringDisplayValueCount() {
        return 12;
    }

    @Override
    protected void textTablePutValue(TextTable tt, int i, int j, int row, Printer printer, POpt<?>[] options) {
        if (isMissing(row)) {
            tt.textCenter(i, j, "?");
        } else {
            DecimalFormat format = printer.getOptions().bind(options).getFloatFormat();
            tt.floatString(i, j, format.format(getFloat(row)));
        }
    }

    @Override
    public Iterator<Float> iterator() {
        return stream().map(VSpot::getFloat).iterator();
    }
}
