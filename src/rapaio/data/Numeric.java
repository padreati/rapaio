/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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


import rapaio.data.mapping.Mapping;

import java.util.Arrays;
import java.util.Collection;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class Numeric extends AbstractVar {

    private static final double missingValue = Double.NaN;
    private double[] data;
    private int rows;

    // public static builders

    public static Numeric newEmpty() {
        return new Numeric(0, 0, Double.NaN);
    }

    public static Numeric newEmpty(int rows) {
        return new Numeric(rows, rows, Double.NaN);
    }

    public static Numeric newCopyOf(Collection<Number> values) {
        final Numeric numeric = new Numeric(0, 0, Double.NaN);
        values.forEach(n -> numeric.addValue(n.doubleValue()));
        return numeric;
    }

    public static Numeric newCopyOf(int... values) {
        Numeric numeric = new Numeric(0, 0, 0);
        numeric.data = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            numeric.data[i] = values[i];
        }
        numeric.rows = values.length;
        return numeric;
    }

    public static Numeric newCopyOf(double... values) {
        Numeric numeric = new Numeric(values.length, values.length, 0);
        numeric.data = Arrays.copyOf(values, values.length);
        return numeric;
    }

    public static Numeric newCopyOf(Var source) {
        Numeric numeric = new Numeric(source.rowCount(), source.rowCount(), 0);
        if (source.isMappedVector() || source.type() != VarType.NUMERIC) {
            for (int i = 0; i < source.rowCount(); i++) {
                numeric.setValue(i, source.value(i));
            }
        } else {
            numeric.data = Arrays.copyOf(((Numeric) source).data, source.rowCount());
        }
        return numeric;
    }

    public static Numeric newWrapOf(double... values) {
        Numeric numeric = new Numeric(0, 0, 0);
        numeric.data = values;
        numeric.rows = values.length;
        return numeric;
    }

//    public static Numeric newWrapOf(Numeric source) {
//        Numeric numeric = new Numeric(source.rowCount(), source.rowCount(), 0);
//        if (source.isMappedVector()) {
//            for (int i = 0; i < source.rowCount(); i++) {
//                numeric.setValue(i, source.value(i));
//            }
//        } else {
//            numeric.data = Arrays.copyOf(source.data, source.rowCount());
//        }
//        return numeric;
//    }

    public static Numeric newFill(int rows) {
        return new Numeric(rows, rows, 0);
    }

    public static Numeric newFill(int rows, double fill) {
        return new Numeric(rows, rows, fill);
    }

    public static Numeric newScalar(double value) {
        return new Numeric(1, 1, value);
    }

    // private constructor

    protected Numeric(int rows, int capacity, double fill) {
        super();
        if (capacity < 0) {
            throw new IllegalArgumentException("Illegal capacity: " + capacity);
        }
        if (rows < 0) {
            throw new IllegalArgumentException("Illegal row count: " + rows);
        }
        if (rows > capacity) {
            throw new IllegalArgumentException(
                    "Illegal row count" + rows + " less than capacity:" + capacity);
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

    private void ensureCapacityInternal(int minCapacity) {
        minCapacity = Math.max(10, minCapacity);
        // overflow-conscious code
        if (minCapacity - data.length > 0)
            grow(minCapacity);
    }

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = data.length;
        int newCapacity = oldCapacity > 0xFFFF ? oldCapacity << 1 : oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity < 0 || minCapacity < 0)
            throw new OutOfMemoryError();
        data = Arrays.copyOf(data, newCapacity);
    }

    @Override
    public boolean isMappedVector() {
        return false;
    }

    @Override
    public Var source() {
        return this;
    }

    @Override
    public Mapping mapping() {
        return null;
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int rowId(int row) {
        return row;
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
        ensureCapacityInternal(rows + 1);
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
        ensureCapacityInternal(rows + 1);
        data[rows++] = value;
    }

    @Override
    public String label(int row) {
        return "";
    }

    @Override
    public void setLabel(int row, String value) {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    @Override
    public void addLabel(String value) {
        throw new RuntimeException("Operation not available for numeric vectors.");
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
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
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
