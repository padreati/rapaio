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


import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import rapaio.data.accessor.*;
import rapaio.printer.format.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

/**
 * Builds a numeric double variable. Double variables stores data as double values
 * and allows modelling of any type of continuous or discrete numeric variable.
 * <p>
 * The placeholder for missing value is Double.NaN. Any form of usage of Double.NaN
 * on set/add operation will result in a missing value.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class VarDouble extends AbstractVar {

    /**
     * @return new empty double variable of size 0
     */
    public static VarDouble empty() {
        return new VarDouble(0, 0, Double.NaN);
    }

    /**
     * Builds an empty double var wil all values set missing
     *
     * @param rows size of the variable
     * @return new instance of numeric var
     */
    public static VarDouble empty(int rows) {
        return new VarDouble(rows, rows, Double.NaN);
    }

    /**
     * Builds a double variable with values copied from given collection
     *
     * @param values given values
     * @return new instance of numeric variable
     */
    public static VarDouble copy(Collection<? extends Number> values) {
        final VarDouble numeric = new VarDouble(0, 0, Double.NaN);
        numeric.data = new double[values.size()];
        numeric.rows = values.size();
        Iterator<? extends Number> it = values.iterator();
        int pos = 0;
        while (it.hasNext()) {
            numeric.data[pos++] = it.next().doubleValue();
        }
        return numeric;
    }

    /**
     * Builds a double variable with values copied from given array of integer values
     *
     * @param values given numeric values
     * @return new instance of numeric variable
     */
    public static VarDouble copy(int... values) {
        VarDouble numeric = new VarDouble(values.length, values.length, 0);
        for (int i = 0; i < values.length; i++) {
            numeric.data[i] = values[i];
        }
        return numeric;
    }

    /**
     * Builds new instance of double var with values copied from given array of doubles
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
     * Builds new double variable with values copied from another numeric variable
     *
     * @param source source numeric var
     * @return new instance of numeric variable
     */
    public static VarDouble copy(Var source) {
        VarDouble numeric = new VarDouble(source.rowCount(), source.rowCount(), 0).withName(source.name());
        if (!(source instanceof VarDouble)) {
            for (int i = 0; i < source.rowCount(); i++) {
                numeric.data[i] = source.getDouble(i);
            }
        } else {
            double[] srcArray = ((VarDouble) source).data;
            System.arraycopy(srcArray, 0, numeric.data, 0, source.rowCount());
        }
        return numeric;
    }

    /**
     * Builds new double variable as a wrapper around an array of doubles
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

    /**
     * Builds a double variable as a wrapper around the array of double values
     * from a DoubleArrayList variable.
     *
     * @param values double array list of doubles
     * @return new instance of numeric values
     */
    public static VarDouble wrap(DoubleArrayList values) {
        VarDouble numeric = new VarDouble(0, 0, 0);
        numeric.data = values.elements();
        numeric.rows = values.size();
        return numeric;
    }

    /**
     * Builds new double variable filled with 0
     *
     * @param rows size of the variable
     * @return new instance of numeric variable of given size and filled with 0
     */
    public static VarDouble fill(int rows) {
        return new VarDouble(rows, rows, 0);
    }

    /**
     * Builds new double variable filled with given fill value
     *
     * @param rows size of the variable
     * @param fill fill value used to set all the values
     * @return new instance of double variable of given size and filled with given value
     */
    public static VarDouble fill(int rows, double fill) {
        return new VarDouble(rows, rows, fill);
    }

    /**
     * Builds a double variable of size 1 filled with given value
     *
     * @param value fill value
     * @return new instance of numeric variable of size 1 and filled with given fill value
     */
    public static VarDouble scalar(double value) {
        return new VarDouble(1, 1, value);
    }

    /**
     * Builds a double variable with values starting from 0, step 1
     * and ending inclusive with end
     *
     * @param end exclusive end of the sequence
     * @return new instance double variable filled with sequence
     */
    public static VarDouble seq(double end) {
        return seq(0, end);
    }

    /**
     * Builds a new double variable with values starting from start, step 1,
     * and ending inclusive with end
     *
     * @param start start of the sequence
     * @param end   inclusive end of the sequence
     * @return new instance double variable filled with sequence
     */
    public static VarDouble seq(double start, double end) {
        return seq(start, end, 1.0);
    }

    /**
     * Builds a new double variable with values starting from start,
     * to inclusive end and given step.
     *
     * @param start inclusive start of the sequence
     * @param end   inclusive end of the sequence
     * @param step  step of the sequence
     * @return new instance double variable filled with sequence
     */
    public static VarDouble seq(double start, double end, double step) {
        VarDouble num = VarDouble.empty();
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
     * Builds a new double variable from a double supplier functional.
     *
     * @param rows     number of rows
     * @param supplier supplier of double values
     * @return new instance of double values
     */
    public static VarDouble from(int rows, Supplier<Double> supplier) {
        VarDouble numeric = new VarDouble(0, 0, 0);
        numeric.data = new double[rows];
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

    private static final long serialVersionUID = -3167416341273129670L;
    public static final double MISSING_VALUE = Double.NaN;
    private double[] data;
    private int rows;

    /**
     * Builds a double variable.
     *
     * @param rows     number of rows in the variable
     * @param capacity capacity of the array
     * @param fill     fill value for the array
     */
    private VarDouble(int rows, int capacity, double fill) {
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new double[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

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
                return var -> var;
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
    public VType type() {
        return VType.DOUBLE;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > data.length) {
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
        ensureCapacity(rows + rowCount);
        for (int i = 0; i < rowCount; i++) {
            data[rows + i] = MISSING_VALUE;
        }
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
        return !Double.isFinite(getDouble(row));
    }

    @Override
    public void setMissing(int row) {
        setDouble(row, MISSING_VALUE);
    }

    @Override
    public void addMissing() {
        addDouble(MISSING_VALUE);
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
        return isMissing(row) ? "?" : String.valueOf(data[row]);
    }

    @Override
    public void setLabel(int row, String value) {
        if ("?".equals(value)) {
            data[row] = Double.NaN;
            return;
        }
        if ("Inf".equals(value)) {
            data[row] = Double.POSITIVE_INFINITY;
            return;
        }
        if ("-Inf".equals(value)) {
            data[row] = Double.NEGATIVE_INFINITY;
            return;
        }
        data[row] = Double.parseDouble(value);
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
        data[row] = Double.valueOf(String.valueOf(value));
    }

    @Override
    public void addLong(long value) {
        addDouble(Double.valueOf(String.valueOf(value)));
    }

    @Override
    public Var newInstance(int rows) {
        return VarDouble.empty(rows);
    }

    @Override
    public VarDouble solidCopy() {
        VarDouble copy = new VarDouble(0, 0, 0).withName(name());
        copy.data = Arrays.copyOf(data, rows);
        copy.rows = rows;
        return copy;
    }

    public VarDoubleDataAccessor getDataAccessor() {
        return new VarDoubleDataAccessor() {
            @Override
            public double getMissingValue() {
                return MISSING_VALUE;
            }

            @Override
            public double[] getData() {
                return data;
            }

            @Override
            public void setData(double[] values) {
                data = values;
            }

            @Override
            public int getRowCount() {
                return rows;
            }

            @Override
            public void setRowCount(int rowCount) {
                rows = rowCount;
            }
        };
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

    @Override
    protected String stringClassName() {
        return "VarDouble";
    }

    @Override
    protected int stringPrefix() {
        return 10;
    }

    @Override
    void stringPutValue(TextTable tt, int i, int j, int row) {
        if (isMissing(row)) {
            tt.textCenter(i, j, "?");
        } else {
            tt.floatFlex(i, j, getDouble(row));
        }
    }

    @Override
    public VarDouble updateDouble(Double2DoubleFunction fun) {
        for (int i = 0; i < data.length; i++) {
            data[i] = fun.applyAsDouble(data[i]);
        }
        return this;
    }

    @Override
    public VarDouble cupdateDouble(Double2DoubleFunction fun) {
        double[] copy = new double[rowCount()];
        for (int i = 0; i < data.length; i++) {
            copy[i] = fun.applyAsDouble(data[i]);
        }
        return VarDouble.wrap(copy).withName(name());
    }
}
