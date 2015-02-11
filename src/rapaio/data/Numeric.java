/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
 */

package rapaio.data;


import java.util.Arrays;
import java.util.Collection;

/**
 * Builds a numeric variable. Numeric variables stores data as double values
 * and allows modelling of any type of continuous or discrete numeric variable.
 * <p>
 * The placeholder for missing value is Double.NaN. Any form of usage of this variable
 * on set/add value will result in a missing value.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class Numeric extends AbstractVar {

    private static final double missingValue = Double.NaN;
    private double[] data;
    private int rows;

    // public static builders

    /**
     * @return new empty numeric variable of size 0
     */
    public static Numeric newEmpty() {
        return new Numeric(0, 0, Double.NaN);
    }

    /**
     * Builds an empty numeric var wil all values set missing
     *
     * @param rows size of the variable
     * @return new instance of numeric var
     */
    public static Numeric newEmpty(int rows) {
        return new Numeric(rows, rows, Double.NaN);
    }

    /**
     * Builds a numeric variable with values copied from given collection
     *
     * @param values given values
     * @return new instance of numeric variable
     */
    public static Numeric newCopyOf(Collection<? extends Number> values) {
        final Numeric numeric = new Numeric(0, 0, Double.NaN);
        values.forEach(n -> numeric.addValue(n.doubleValue()));
        return numeric;
    }

    /**
     * Builds a numeric variable with values copied from given array of integer values
     *
     * @param values given numeric values
     * @return new instance of numeric variable
     */
    public static Numeric newCopyOf(int... values) {
        Numeric numeric = new Numeric(0, 0, 0);
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
    public static Numeric newCopyOf(double... values) {
        Numeric numeric = new Numeric(values.length, values.length, 0);
        numeric.data = Arrays.copyOf(values, values.length);
        return numeric;
    }

    /**
     * Builds new numeric variable with values copied from another numeric variable
     *
     * @param source source numeric var
     * @return new instance of numeric variable
     */
    public static Numeric newCopyOf(Var source) {
        Numeric numeric = new Numeric(source.rowCount(), source.rowCount(), 0).withName(source.name());
        if (source instanceof MappedVar || source.type() != VarType.NUMERIC) {
            for (int i = 0; i < source.rowCount(); i++) {
                numeric.setValue(i, source.value(i));
            }
        } else {
            numeric.data = Arrays.copyOf(((Numeric) source).data, source.rowCount());
        }
        return numeric;
    }

    /**
     * Builds new numeric variable as a wrapper around an array of doubles
     *
     * @param values wrapped array of doubles
     * @return new instance of numeric variable
     */
    public static Numeric newWrapOf(double... values) {
        Numeric numeric = new Numeric(0, 0, 0);
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
    public static Numeric newFill(int rows) {
        return new Numeric(rows, rows, 0);
    }

    /**
     * Builds new numeric variable filled with given fill value
     *
     * @param rows size of the variable
     * @param fill fill value used to set all the values
     * @return new instance of numeric variable of given size and filled with given value
     */
    public static Numeric newFill(int rows, double fill) {
        return new Numeric(rows, rows, fill);
    }

    /**
     * Builds a numeric variable of size 1 filled with given value
     *
     * @param value fill value
     * @return new instance of numeric variable of size 1 and filled with given fill value
     */
    public static Numeric newScalar(double value) {
        return new Numeric(1, 1, value);
    }

    public static Numeric newSeq(double end) {
        return newSeq(0, end);
    }

    public static Numeric newSeq(double start, double end) {
        return newSeq(start, end, 1.0);
    }

    public static Numeric newSeq(double start, double end, double step) {
        Numeric num = Numeric.newEmpty();
        while (start <= end) {
            num.addValue(start);
            start += step;
        }
        return num;
    }
    // private constructor

    @Override
    public Numeric withName(String name) {
        return (Numeric) super.withName(name);
    }

    private Numeric(int rows, int capacity, double fill) {
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        this.data = new double[capacity];
        this.rows = rows;
        if (fill != 0)
            Arrays.fill(data, 0, rows, fill);
    }

    @Override
    public VarType type() {
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
    public int rowCount() {
        return rows;
    }

    @Override
    public double value(int row) {
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
    public int index(int row) {
        return (int) Math.rint(value(row));
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
    public String label(int row) {
        if (missing(row))
            return "?";
        return String.valueOf(value(row));
    }

    @Override
    public void setLabel(int row, String value) {
        if ("?".equals(value)) {
            setMissing(row);
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
        addValue(Double.parseDouble(value));
    }

    @Override
    public String[] dictionary() {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    @Override
    public void setDictionary(String[] dict) {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    @Override
    public boolean binary(int row) {
        return value(row) == 1.0;
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
    public long stamp(int row) {
        return (long) Math.rint(value(row));
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
    public boolean missing(int row) {
        return value(row) != value(row);
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
    public Numeric solidCopy() {
        return Numeric.newCopyOf(this);
    }

    @Override
    public String toString() {
        return "Numeric[" + rowCount() + "]";
    }
}
