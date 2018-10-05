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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.Function;

/**
 * Numerical variable which store only 1,0 and missing values. This is a storage-optimized version of a
 * binary variable which does not allows update operations. The implementation uses bit sets for storing
 * values, one bit set for 0 or 1, and another for missing values.
 * <p>
 * The possible numerical values are: 0, 1. Any other values is treating as missing value. However each representation
 * returns it's specific missing values: "?", Double.NaN, Integer.MIN_VALUE, Long.MIN_VALUE.
 * 0 and 1 are the only possible two non missing values.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public final class VarBinary extends AbstractVar {

    /**
     * Builds an empty binary var
     *
     * @return new instance of binary var
     */
    public static VarBinary empty() {
        // fill with missing values
        return new VarBinary(0, 1, 0);
    }

    /**
     * Builds a binary variable of given size with filled missing values
     *
     * @param rows size of variable
     * @return new instance of binary var
     */
    public static VarBinary empty(int rows) {
        // fill with missing values
        return new VarBinary(rows, 1, 0);
    }

    /**
     * Builds a new binary variable of given size filled with given value
     *
     * @param rows      size of variable
     * @param fillValue fill value
     * @return new instance of binary var
     */
    public static VarBinary fill(int rows, int fillValue) {
        if (fillValue == 1 || fillValue == 0) {
            return new VarBinary(rows, 0, fillValue);
        }
        return new VarBinary(rows, 1, 0);
    }

    /**
     * Builds a new binary variable with values copied from given array of values
     *
     * @param values given array of values
     * @return new instance of binary var
     */
    public static VarBinary copy(int... values) {
        final VarBinary b = new VarBinary(values.length, 0, 0);
        for (int i = 0; i < values.length; i++) {
            if (values[i] == 0) continue;
            if (values[i] == 1) {
                b.values.flip(i);
                continue;
            }
            b.setMissing(i);
        }
        return b;
    }

    public static VarBinary fromIndex(int rows, Function<Integer, Integer> supplier) {
        VarBinary result = new VarBinary(rows, 0, 0);
        for (int i = 0; i < rows; i++) {
            int value = supplier.apply(i);
            if (value == 0) continue;
            if (value == 1) {
                result.values.flip(i);
            } else {
                result.missing.flip(i);
            }
        }
        return result;
    }

    public static VarBinary from(int rows, Function<Integer, Boolean> supplier) {
        VarBinary result = new VarBinary(rows, 0, 0);
        for (int i = 0; i < rows; i++) {
            Boolean value = supplier.apply(i);
            if (value == null) {
                result.missing.set(i, true);
                continue;
            }
            if (value) {
                result.values.set(i, true);
            }
        }
        return result;
    }


    private static final long serialVersionUID = -4977697633437126744L;
    private int rows;

    // bit set of flags for missing values
    private BitSet missing;

    // bit set of flags for 1=true or 0=false values
    private BitSet values;

    /**
     * Private constructor to avoid instantiation from outside, other than statical builders.
     */
    private VarBinary(final int rows, final int fillMissing, final int fillValue) {
        this.rows = rows;
        this.missing = new BitSet(rows);
        this.values = new BitSet(rows);
        if (fillMissing == 1)
            this.missing.flip(0, rows);
        else if (fillValue == 1)
            this.values.flip(0, rows);
    }

    @Override
    public VType type() {
        return VType.BINARY;
    }

    @Override
    public VarBinary withName(String name) {
        return (VarBinary) super.withName(name);
    }

    void increaseCapacity(int minCapacity) {
        if (minCapacity <= values.size()) {
            return;
        }
        if (minCapacity > rows) {
            minCapacity = Math.max(minCapacity, rows + (rows >> 1));
            BitSet missingCopy = new BitSet(minCapacity);
            BitSet valuesCopy = new BitSet(minCapacity);
            missingCopy.or(missing);
            valuesCopy.or(values);
            missing = missingCopy;
            values = valuesCopy;
        }
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public void addRows(int rowCount) {
        increaseCapacity(rows + rowCount);
        for (int i = 0; i < rowCount; i++) {
            missing.set(i + rows);
        }
        rows += rowCount;
    }

    @Override
    public void removeRow(int row) {
        for (int i = row + 1; i < rows; i++) {
            values.set(i - 1, values.get(i));
            missing.set(i - 1, missing.get(i));
        }
        rows--;
    }

    @Override
    public void clearRows() {
        this.rows = 0;
    }

    @Override
    public double getDouble(int row) {
        if (isMissing(row)) return Double.NaN;
        return values.get(row) ? 1.0 : 0.0;
    }

    @Override
    public void setDouble(int row, double value) {
        if (value == 1.0) {
            setInt(row, 1);
            return;
        }
        if (value == 0.0) {
            setInt(row, 0);
            return;
        }
        setMissing(row);
    }

    @Override
    public void addDouble(double value) {
        if (value == 1.0) {
            addInt(1);
            return;
        }
        if (value == 0) {
            addInt(0);
            return;
        }
        addMissing();
    }

    @Override
    public int getInt(int row) {
        if (missing.get(row))
            return Integer.MIN_VALUE;
        return values.get(row) ? 1 : 0;
    }

    @Override
    public void setInt(int row, int value) {
        if (value == 1) {
            values.set(row, true);
            missing.set(row, false);
            return;
        }
        if (value == 0) {
            values.set(row, false);
            missing.set(row, false);
            return;
        }
        missing.set(row, true);
    }

    @Override
    public void addInt(int value) {
        increaseCapacity(rows + 1);
        rows++;
        if (value == 1) {
            values.set(rows - 1, true);
            missing.set(rows - 1, false);
            return;
        }
        if (value == 0) {
            values.set(rows - 1, false);
            missing.set(rows - 1, false);
            return;
        }
        missing.set(rows - 1, true);
    }

    @Override
    public String getLabel(int row) {
        return isMissing(row) ? "?" : (getInt(row) == 0 ? "0" : "1");
    }

    @Override
    public void setLabel(int row, String value) {
        if ("?".equals(value)) {
            setMissing(row);
            return;
        }
        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            setInt(row, 1);
            return;
        }
        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            setInt(row, 0);
            return;
        }
        throw new IllegalArgumentException(
                String.format("The value %s could not be converted to a binary value", value));
    }

    @Override
    public void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            addInt(1);
            return;
        }
        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            addInt(0);
            return;
        }
        throw new IllegalArgumentException(
                String.format("The value %s could not be converted to a binary value", value));
    }

    @Override
    public List<String> levels() {
        return Arrays.asList("?", "true", "false");
    }

    @Override
    public void setLevels(String... dict) {
        throw new IllegalArgumentException("Operation not implemented on binary variables");
    }

    @Override
    public long getLong(int row) {
        if (isMissing(row)) {
            return Long.MIN_VALUE;
        }
        return getInt(row);
    }

    @Override
    public void setLong(int row, long value) {
        if (value == 1) {
            setInt(row, 1);
            return;
        }
        if (value == 0) {
            setInt(row, 0);
            return;
        }
        setMissing(row);
    }

    @Override
    public void addLong(long value) {
        if (value == 1) {
            addInt(1);
            return;
        }
        if (value == 0) {
            addInt(0);
            return;
        }
        addMissing();
    }

    @Override
    public boolean isMissing(int row) {
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
    public Var newInstance(int rows) {
        return VarBinary.empty(rows).withName(name());
    }

    @Override
    public VarBinary solidCopy() {
        return (VarBinary) super.solidCopy();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(rowCount());
        byte[] buff = values.toByteArray();
        out.writeInt(buff.length);
        out.write(buff);
        buff = missing.toByteArray();
        out.writeInt(buff.length);
        out.write(buff);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        rows = in.readInt();
        byte[] buff = new byte[in.readInt()];
        in.readFully(buff);
        values = BitSet.valueOf(buff);
        buff = new byte[in.readInt()];
        in.readFully(buff);
        missing = BitSet.valueOf(buff);
    }
}
