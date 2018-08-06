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


import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Builds a numeric variable. Numeric variables stores data as double values
 * and allows modelling of any type of continuous or discrete numeric variable.
 * <p>
 * The placeholder for missing value is Double.NaN. Any form of usage of Double.NaN
 * on set/add operation will result in a missing value.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class VarDouble extends AbstractVar {

    /**
     * @return new empty numeric variable of size 0
     */
    public static VarDouble empty() {
        return new VarDouble(0, 0, Double.NaN);
    }

    /**
     * Builds an empty numeric var wil all values set missing
     *
     * @param rows size of the variable
     * @return new instance of numeric var
     */
    public static VarDouble empty(int rows) {
        return new VarDouble(rows, rows, Double.NaN);
    }

    /**
     * Builds a numeric variable with values copied from given collection
     *
     * @param values given values
     * @return new instance of numeric variable
     */
    public static VarDouble copy(Collection<? extends Number> values) {
        final VarDouble numeric = new VarDouble(0, 0, Double.NaN);
        values.forEach(n -> numeric.addDouble(n.doubleValue()));
        return numeric;
    }

    /**
     * Builds a numeric variable with values copied from given array of integer values
     *
     * @param values given numeric values
     * @return new instance of numeric variable
     */
    public static VarDouble copy(int... values) {
        VarDouble numeric = new VarDouble(0, 0, 0);
        numeric.data = new double[values.length];
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
    public static VarDouble copy(double... values) {
        VarDouble numeric = new VarDouble(values.length, values.length, 0);
        numeric.data = Arrays.copyOf(values, values.length);
        return numeric;
    }

    /**
     * Builds new numeric variable with values copied from another numeric variable
     *
     * @param source source numeric var
     * @return new instance of numeric variable
     */
    public static VarDouble copy(Var source) {
        VarDouble numeric = new VarDouble(source.rowCount(), source.rowCount(), 0).withName(source.name());
        if (!(source instanceof VarDouble)) {
            for (int i = 0; i < source.rowCount(); i++) {
                numeric.setDouble(i, source.getDouble(i));
            }
        } else {
            numeric.data = Arrays.copyOf(((VarDouble) source).data, source.rowCount());
        }
        return numeric;
    }

    /**
     * Builds new numeric variable as a wrapper around an array of doubles
     *
     * @param values wrapped array of doubles
     * @return new instance of numeric variable
     */
    public static VarDouble wrap(double... values) {
        VarDouble numeric = new VarDouble(0, 0, 0);
        numeric.data = values;
        numeric.rows = values.length;
        return numeric;
    }

    public static VarDouble wrap(DoubleArrayList values) {
        VarDouble numeric = new VarDouble(0, 0, 0);
        numeric.data = values.elements();
        numeric.rows = values.size();
        return numeric;
    }

    /**
     * Builds new numeric variable filled with 0
     *
     * @param rows size of the variable
     * @return new instance of numeric variable of given size and filled with 0
     */
    public static VarDouble fill(int rows) {
        return new VarDouble(rows, rows, 0);
    }

    /**
     * Builds new numeric variable filled with given fill value
     *
     * @param rows size of the variable
     * @param fill fill value used to set all the values
     * @return new instance of numeric variable of given size and filled with given value
     */
    public static VarDouble fill(int rows, double fill) {
        return new VarDouble(rows, rows, fill);
    }

    /**
     * Builds a numeric variable of size 1 filled with given value
     *
     * @param value fill value
     * @return new instance of numeric variable of size 1 and filled with given fill value
     */
    public static VarDouble scalar(double value) {
        return new VarDouble(1, 1, value);
    }

    public static VarDouble seq(double end) {
        return seq(0, end);
    }

    public static VarDouble seq(double start, double end) {
        return seq(start, end, 1.0);
    }

    public static VarDouble seq(double start, double end, double step) {
        VarDouble num = VarDouble.empty();
        int i = 0;
        while (start + i * step <= end) {
            num.addDouble(start + i * step);
            i++;
        }
        return num;
    }

    public static VarDouble from(int rows, Supplier<Double> supplier) {
        VarDouble numeric = new VarDouble(0, 0, 0);
        numeric.data = new double[rows];
        numeric.rows = rows;
        for (int i = 0; i < rows; i++) {
            numeric.data[i] = supplier.get();
        }
        return numeric;
    }

    private static final long serialVersionUID = -3167416341273129670L;

    private static final double missingValue = Double.NaN;
    private double[] data;
    private int rows;


    // private constructor

    private VarDouble(int rows, int capacity, double fill) {
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new double[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    // public static builders

    /**
     * Builds a new numeric variable of a given size and values produced by a function
     * which transforms a row number into a value by a given transformation function.
     *
     * @param rows           number of rows
     * @param transformation transformation function
     * @return new numeric variable which contains the computed values
     */
    public static VarDouble from(int rows, Function<Integer, Double> transformation) {
        VarDouble numeric = new VarDouble(0, 0, 0);
        numeric.data = new double[rows];
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
    public static VarDouble from(Var reference, Function<Double, Double> transform) {
        return VarDouble.from(reference.rowCount(), i -> transform.apply(reference.getDouble(i)));
    }


    // stream collectors
    public static Collector<Double, VarDouble, VarDouble> collector() {

        return new Collector<Double, VarDouble, VarDouble>() {
            @Override
            public Supplier<VarDouble> supplier() {
                return VarDouble::empty;
            }

            @Override
            public BiConsumer<VarDouble, Double> accumulator() {
                return VarDouble::addDouble;
            }

            @Override
            public BinaryOperator<VarDouble> combiner() {
                return (x, y) -> {
                    y.stream().forEach(s -> x.addDouble(s.getDouble()));
                    return x;
                };
            }

            @Override
            public Function<VarDouble, VarDouble> finisher() {
                return VarDouble::solidCopy;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    @Override
    public VarDouble withName(String name) {
        return (VarDouble) super.withName(name);
    }

    @Override
    public VarType type() {
        return VarType.DOUBLE;
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
            data[rows + i] = VarDouble.missingValue;
        }
        rows += rowCount;
    }

    @Override
    public double getDouble(int row) {
        return data[row];
    }

    @Override
    public void setDouble(int row, double value) {
        data[row] = value;
    }

    @Override
    public void addDouble(double value) {
        ensureCapacity(rows + 1);
        data[rows++] = value;
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
            setDouble(row, Double.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            setDouble(row, Double.NEGATIVE_INFINITY);
            return;
        }
        setDouble(row, Double.parseDouble(value));
    }

    @Override
    public void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        if ("Inf".equals(value)) {
            addDouble(Double.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            addDouble(Double.NEGATIVE_INFINITY);
            return;
        }
        addDouble(Double.parseDouble(value));
    }

    @Override
    public List<String> levels() {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    @Override
    public void setLevels(String[] dict) {
        throw new RuntimeException("Operation not available for numeric vectors.");
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
    public Var newInstance(int rows) {
        return VarDouble.empty(rows);
    }

    @Override
    public String toString() {
        return "Numeric[name:" + name() + ", rowCount:" + rowCount() + "]";
    }

    @Override
    public VarDouble solidCopy() {
        return (VarDouble) super.solidCopy();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(rowCount());
        for (int i = 0; i < rowCount(); i++) {
            out.writeDouble(data[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        rows = in.readInt();
        data = new double[rows];
        for (int i = 0; i < rows; i++) {
            data[i] = in.readDouble();
        }
    }

}
