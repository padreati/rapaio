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

package rapaio.math.linear.dense;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.DoubleStream;

/**
 * Dense 2 dimensional matrix with values in double floating point precision
 */
public class SolidRM implements RM {

    private static final long serialVersionUID = -2186520026933442642L;

    private final int rowCount;
    private final int colCount;
    private final double[] values;

    /**
     * Builds a zero filled matrix with n rows and m columns
     * @param rowCount number of rows
     * @param colCount number of columns
     * @return a new instance of the matrix object
     */
    public static SolidRM empty(int rowCount, int colCount) {
        return new SolidRM(rowCount, colCount);
    }

    /**
     * Builds an identity matrix with n rows and n columns.
     * An identity matrix is a matrix with 1 on the main diagonal
     * and 0 otherwise.
     *
     * @param n number of rows and also number of columns
     * @return a new instance of identity matrix of order n
     */
    public static SolidRM identity(int n) {
        SolidRM m = new SolidRM(n, n);
        for (int i = 0; i < n; i++) {
            m.set(i, i, 1.0);
        }
        return m;
    }

    /**
     * Builds a new matrix filled with a given value.
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fill value which fills all cells of the matrix
     * @return new matrix filled with value
     */
    public static SolidRM fill(int rowCount, int colCount, double fill) {
        SolidRM ret = new SolidRM(rowCount, colCount);
        if (fill != 0.0)
            Arrays.fill(ret.values, fill);
        return ret;
    }

    /**
     * Builds a new matrix filled with a given value
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fun lambda function which computes a value given row and column positions
     * @return new matrix filled with value
     */
    public static SolidRM fill(int rowCount, int colCount, BiFunction<Integer, Integer, Double> fun) {
        SolidRM ret = new SolidRM(rowCount, colCount);
        for (int i = 0; i < ret.rowCount(); i++) {
            for (int j = 0; j < ret.colCount(); j++) {
                ret.set(i, j, fun.apply(i, j));
            }
        }
        return ret;
    }

    /**
     * Builds a new matrix from a linearized array of values.
     * The array contains the values by row, aka first cols elements
     * is the first row, second cols elements is the second row,
     * and so on.
     * @param rows number of rows
     * @param cols number of columns
     * @param source value array
     * @return new matrix which contains a copy of the source
     */
    public static SolidRM copy(int rows, int cols, double... source) {
        SolidRM m = empty(rows, cols);
        System.arraycopy(source, 0, m.values, 0, rows * cols);
        return m;
    }


    public static SolidRM copy(double[][] source) {
        int colCount = source[0].length;
        int rowCount = source.length;
        SolidRM m = empty(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(source[i], 0, m.values, i * colCount, colCount);
        }
        return m;
    }

    public static RM copy(double[][] source, int rowStart, int rowEnd, int colStart, int colEnd) {
        RM mm = new SolidRM(rowEnd - rowStart, colEnd - colStart);
        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = colStart; j < colEnd; j++) {
                mm.set(i-rowStart, j-colStart, source[i][j]);
            }
        }
        return mm;
    }

    public static SolidRM copy(Frame df) {
        SolidRM m = empty(df.rowCount(), df.varCount());
        for (int j = 0; j < df.varCount(); j++) {
            for (int i = 0; i < df.rowCount(); i++) {
                m.set(i, j, df.value(i, j));
            }
        }
        return m;
    }

    public static SolidRM copy(Var... vars) {
        Frame df = BoundFrame.byVars(vars);
        SolidRM m = empty(df.rowCount(), df.varCount());
        for (int j = 0; j < df.varCount(); j++) {
            for (int i = 0; i < df.rowCount(); i++) {
                m.set(i, j, df.value(i, j));
            }
        }
        return m;
    }

    private SolidRM(int rowCount, int colCount) {
        if (((long) rowCount) * ((long) colCount) >= (long) Integer.MAX_VALUE)
            throw new IllegalArgumentException("Array is too large to allocate with integer indexes");

        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = new double[rowCount*colCount];
    }

    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
    public int colCount() {
        return colCount;
    }

    @Override
    public double get(int i, int j) {
        return values[i * colCount + j];
    }

    @Override
    public void set(int i, int j, double value) {
        values[i * colCount + j] = value;
    }

    @Override
    public void increment(int i, int j, double value) {
        values[i * colCount + j] += value;
    }

    @Override
    public RM t() {
        SolidRM t = new SolidRM(colCount, rowCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                t.set(j, i, get(i, j));
            }
        }
        return t;
    }

    @Override
    public SolidRV mapCol(int i) {
        SolidRV v = SolidRV.empty(rowCount);
        for (int j = 0; j < rowCount; j++) {
            v.set(j, get(j, i));
        }
        return v;
    }

    @Override
    public RV mapRow(int i) {
        SolidRV v = SolidRV.empty(colCount);
        for (int j = 0; j < colCount; j++) {
            v.set(j, get(i, j));
        }
        return v;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values);
    }

    @Override
    public SolidRM solidCopy() {
        SolidRM copy = new SolidRM(rowCount, colCount);
        System.arraycopy(values, 0, copy.values, 0, values.length);
        return copy;
    }
}
