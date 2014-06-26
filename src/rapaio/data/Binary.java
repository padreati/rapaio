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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.BitSet;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public final class Binary implements Var {

    private int rows;
    private BitSet missing;
    private BitSet values;

    @Override
    public VarType type() {
        return VarType.BINARY;
    }

    /**
     * Static builders
     */

    public static Binary newEmpty() {
        return new Binary(0, false, false);
    }

    public static Binary newEmpty(int rows) {
        return new Binary(rows, true, false);
    }

    public static Binary newFill(int rows, boolean fillValue) {
        return new Binary(rows, false, fillValue);
    }

    public static Binary copyOf(int... values) {
        final Binary b = new Binary(values.length, false, false);
        for (int i = 0; i < values.length; i++) {
            if (values[i] == 0) continue;
            if (values[i] == 1) {
                b.setBinary(i, true);
                continue;
            }
            b.setMissing(i);
        }
        return b;
    }

    public static Binary copyOf(boolean... values) {
        final Binary b = new Binary(values.length, false, false);
        for (int i = 0; i < values.length; i++) {
            if (values[i]) {
                b.setBinary(i, true);
            }
        }
        return b;
    }

    /**
     * Private constructor to avoid instantiation from outside, other than statical builders.
     */
    private Binary(final int rows, final boolean fillMissing, final boolean fillValue) {
        this.rows = rows;
        this.missing = new BitSet(rows);
        this.values = new BitSet(rows);
        if (fillMissing)
            this.missing.flip(0, rows);
        else if (fillValue)
            this.values.flip(0, rows);
    }

    void increaseCapacity(int minCapacity) {
        if (minCapacity > rows) {
            final int r = rows + rows >> 1;
            if (minCapacity < r) {
                minCapacity = r;
            }
            BitSet missingCopy = new BitSet(minCapacity);
            BitSet valuesCopy = new BitSet(minCapacity);
            missingCopy.or(missing);
            valuesCopy.or(values);
            missing = missingCopy;
            values = valuesCopy;
        }
    }

    @Override
    public boolean isMapped() {
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
        return values.get(row) ? 1.0 : 0.0;
    }

    @Override
    public void setValue(int row, double value) {
        if (value == 1.0)
            setBinary(row, true);
        if (value == 0.0)
            setBinary(row, false);
        throw new IllegalArgumentException(String.format("Value %f is not a valid binary value", value));
    }

    @Override
    public void addValue(double value) {
        if (value == 1.0)
            addBinary(true);
        if (value == 0.0)
            addBinary(false);
        throw new IllegalArgumentException(String.format("Value %f is not a valid binary value", value));
    }

    @Override
    public int index(int row) {
        return binary(row) ? 1 : 0;
    }

    @Override
    public void setIndex(int row, int value) {
        if (value == 1)
            setBinary(row, true);
        if (value == 0)
            setBinary(row, false);
        throw new IllegalArgumentException(String.format("Value %d is not a valid binary value", value));
    }

    @Override
    public void addIndex(int value) {
        if (value == 1)
            addBinary(true);
        if (value == 0)
            addBinary(false);
        throw new IllegalArgumentException(String.format("Value %d is not a valid binary value", value));
    }

    @Override
    public String label(int row) {
        return missing(row) ? "?" : (binary(row) ? "true" : "false");
    }

    @Override
    public void setLabel(int row, String value) {
        throw new IllegalArgumentException("Operation not implemented on binary variables");
    }

    @Override
    public void addLabel(String value) {
        throw new IllegalArgumentException("Operation not implemented on binary variables");
    }

    @Override
    public String[] dictionary() {
        throw new IllegalArgumentException("Operation not implemented on binary variables");
    }

    @Override
    public void setDictionary(String[] dict) {
        throw new IllegalArgumentException("Operation not implemented on binary variables");
    }

    @Override
    public boolean binary(int row) {
        return values.get(row);
    }

    @Override
    public void setBinary(int row, boolean value) {
        if (missing(row))
            missing.set(row, false);
        values.set(row, value);
    }

    @Override
    public void addBinary(boolean value) {
        increaseCapacity(rows + 1);
        setBinary(rows, value);
        rows++;
    }

    @Override
    public long stamp(int row) {
        return binary(row) ? 1L : 0L;
    }

    @Override
    public void setStamp(int row, long value) {
        if (value == 1)
            setBinary(row, true);
        if (value == 0)
            setBinary(row, false);
        throw new IllegalArgumentException(String.format("This value %d is not a valid binary value", value));
    }

    @Override
    public void addStamp(long value) {
        if (value == 1)
            addBinary(true);
        if (value == 0)
            addBinary(false);
        throw new IllegalArgumentException(String.format("This value %d is not a valid binary value", value));
    }

    @Override
    public boolean missing(int row) {
        return missing.get(row);
    }

    @Override
    public void setMissing(int row) {
        missing.set(row);
    }

    @Override
    public void addMissing() {
        increaseCapacity(rows + 1);
        missing.set(rows);
        rows++;
    }

    @Override
    public void remove(int row) {
        throw new NotImplementedException();
    }

    @Override
    public void clear() {
        this.rows = 0;
    }

    @Override
    public Var solidCopy() {
        throw new NotImplementedException();
    }
}
