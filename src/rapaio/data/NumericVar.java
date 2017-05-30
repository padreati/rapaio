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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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
public final class NumericVar extends AbstractVar {

    /**
     * @return new empty numeric variable of size 0
     */
    public static NumericVar empty() {
        return new NumericVar(0, 0, Double.NaN);
    }

    /**
     * Builds an empty numeric var wil all values set missing
     *
     * @param rows size of the variable
     * @return new instance of numeric var
     */
    public static NumericVar empty(int rows) {
        return new NumericVar(rows, rows, Double.NaN);
    }

    /**
     * Builds a numeric variable with values copied from given collection
     *
     * @param values given values
     * @return new instance of numeric variable
     */
    public static NumericVar copy(Collection<? extends Number> values) {
        final NumericVar numeric = new NumericVar(0, 0, Double.NaN);
        values.forEach(n -> numeric.addValue(n.doubleValue()));
        return numeric;
    }

    /**
     * Builds a numeric variable with values copied from given array of integer values
     *
     * @param values given numeric values
     * @return new instance of numeric variable
     */
    public static NumericVar copy(int... values) {
        NumericVar numeric = new NumericVar(0, 0, 0);
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
    public static NumericVar copy(double... values) {
        NumericVar numeric = new NumericVar(values.length, values.length, 0);
        numeric.data = Arrays.copyOf(values, values.length);
        return numeric;
    }

    /**
     * Builds new numeric variable with values copied from another numeric variable
     *
     * @param source source numeric var
     * @return new instance of numeric variable
     */
    public static NumericVar copy(Var source) {
        NumericVar numeric = new NumericVar(source.getRowCount(), source.getRowCount(), 0).withName(source.getName());
        if (!(source instanceof NumericVar)) {
            for (int i = 0; i < source.getRowCount(); i++) {
                numeric.setValue(i, source.getValue(i));
            }
        } else {
            numeric.data = Arrays.copyOf(((NumericVar) source).data, source.getRowCount());
        }
        return numeric;
    }

    /**
     * Builds new numeric variable as a wrapper around an array of doubles
     *
     * @param values wrapped array of doubles
     * @return new instance of numeric variable
     */
    public static NumericVar wrap(double... values) {
        NumericVar numeric = new NumericVar(0, 0, 0);
        numeric.data = values;
        numeric.rows = values.length;
        return numeric;
    }

    /**
     * Builds new numeric variable filled with 0
     *
     * @param rows size of the variable
     * @return new instance of numeric variable of given size and filled with 0
     */
    public static NumericVar fill(int rows) {
        return new NumericVar(rows, rows, 0);
    }

    /**
     * Builds new numeric variable filled with given fill value
     *
     * @param rows size of the variable
     * @param fill fill value used to set all the values
     * @return new instance of numeric variable of given size and filled with given value
     */
    public static NumericVar fill(int rows, double fill) {
        return new NumericVar(rows, rows, fill);
    }

    /**
     * Builds a numeric variable of size 1 filled with given value
     *
     * @param value fill value
     * @return new instance of numeric variable of size 1 and filled with given fill value
     */
    public static NumericVar scalar(double value) {
        return new NumericVar(1, 1, value);
    }

    public static NumericVar seq(double end) {
        return seq(0, end);
    }

    public static NumericVar seq(double start, double end) {
        return seq(start, end, 1.0);
    }

    public static NumericVar seq(double start, double end, double step) {
        NumericVar num = NumericVar.empty();
        int i = 0;
        while (start + i * step <= end) {
            num.addValue(start + i * step);
            i++;
        }
        return num;
    }

    public static NumericVar from(int rows, Supplier<Double> supplier) {
        NumericVar numeric = new NumericVar(0, 0, 0);
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

    private NumericVar(int rows, int capacity, double fill) {
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
    public static NumericVar from(int rows, Function<Integer, Double> transformation) {
        NumericVar numeric = new NumericVar(0, 0, 0);
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
    public static NumericVar from(Var reference, Function<Double, Double> transform) {
        return NumericVar.from(reference.getRowCount(), i -> transform.apply(reference.getValue(i)));
    }


    // stream collectors
    public static Collector<Double, NumericVar, NumericVar> collector() {

        return new Collector<Double, NumericVar, NumericVar>() {
            @Override
            public Supplier<NumericVar> supplier() {
                return NumericVar::empty;
            }

            @Override
            public BiConsumer<NumericVar, Double> accumulator() {
                return NumericVar::addValue;
            }

            @Override
            public BinaryOperator<NumericVar> combiner() {
                return (x, y) -> {
                    y.stream().forEach(s -> x.addValue(s.getValue()));
                    return x;
                };
            }

            @Override
            public Function<NumericVar, NumericVar> finisher() {
                return NumericVar::solidCopy;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    @Override
    public NumericVar withName(String name) {
        return (NumericVar) super.withName(name);
    }

    @Override
    public VarType getType() {
        return VarType.NUMERIC;
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
    public int getRowCount() {
        return rows;
    }

    @Override
    public void addRows(int rowCount) {
        ensureCapacity(this.rows + rowCount + 1);
        for (int i = 0; i < rowCount; i++) {
            data[rows + i] = NumericVar.missingValue;
        }
        rows += rowCount;
    }

    @Override
    public double getValue(int row) {
        return data[row];
    }

    @Override
    public void setValue(int row, double value) {
        data[row] = value;
    }

    @Override
    public void addValue(double value) {
        ensureCapacity(rows + 1);
        data[rows++] = value;
    }

    @Override
    public int getIndex(int row) {
        return (int) Math.rint(getValue(row));
    }

    @Override
    public void setIndex(int row, int value) {
        setValue(row, value);
    }

    @Override
    public void addIndex(int value) {
        ensureCapacity(rows + 1);
        data[rows++] = value;
    }

    @Override
    public String getLabel(int row) {
        if (isMissing(row))
            return "?";
        return String.valueOf(getValue(row));
    }

    @Override
    public void setLabel(int row, String value) {
        if ("?".equals(value)) {
            setMissing(row);
            return;
        }
        if ("Inf".equals(value)) {
            setValue(row, Double.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            setValue(row, Double.NEGATIVE_INFINITY);
            return;
        }
        setValue(row, Double.parseDouble(value));
    }

    @Override
    public void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        if ("Inf".equals(value)) {
            addValue(Double.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            addValue(Double.NEGATIVE_INFINITY);
            return;
        }
        addValue(Double.parseDouble(value));
    }

    @Override
    public String[] getLevels() {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    @Override
    public void setLevels(String[] dict) {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    @Override
    public boolean getBinary(int row) {
        return getValue(row) == 1.0;
    }

    @Override
    public void setBinary(int row, boolean value) {
        setValue(row, value ? 1 : 0);
    }

    @Override
    public void addBinary(boolean value) {
        addValue(value ? 1 : 0);
    }

    @Override
    public long getStamp(int row) {
        return (long) Math.rint(getValue(row));
    }

    @Override
    public void setStamp(int row, long value) {
        setValue(row, Double.valueOf(String.valueOf(value)));
    }

    @Override
    public void addStamp(long value) {
        addValue(Double.valueOf(String.valueOf(value)));
    }

    @Override
    public boolean isMissing(int row) {
        return getValue(row) != getValue(row);
    }

    @Override
    public void setMissing(int row) {
        setValue(row, missingValue);
    }

    @Override
    public void addMissing() {
        addValue(missingValue);
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
        return NumericVar.empty(rows);
    }

    @Override
    public String toString() {
        return "Numeric[name:" + getName() + ", rowCount:" + getRowCount() + "]";
    }

    @Override
    public NumericVar solidCopy() {
        return (NumericVar) super.solidCopy();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(getRowCount());
        for (int i = 0; i < getRowCount(); i++) {
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
