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

package rapaio.math.linear.dense;

import rapaio.core.distributions.Normal;
import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.math.linear.DMatrix;
import rapaio.util.function.IntIntDoubleBiFunction;

import java.util.Arrays;

/**
 * Dense matrix with values in double floating point precision.
 * Values are stored in arrays of arrays with first array holding row references
 * and secondary level arrays being the row arrays.
 */
public class SolidDMatrix extends BaseDMatrix {

    private static final long serialVersionUID = -2186520026933442642L;

    /**
     * Builds a zero filled matrix with n rows and m columns
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @return a new instance of the matrix object
     */
    public static SolidDMatrix empty(int rowCount, int colCount) {
        return new SolidDMatrix(rowCount, colCount);
    }

    /**
     * Builds an identity matrix with n rows and n columns.
     * An identity matrix is a matrix with 1 on the main diagonal
     * and 0 otherwise.
     *
     * @param n number of rows and also number of columns
     * @return a new instance of identity matrix of order n
     */
    public static SolidDMatrix identity(int n) {
        SolidDMatrix m = new SolidDMatrix(n, n);
        for (int i = 0; i < n; i++) {
            m.set(i, i, 1.0);
        }
        return m;
    }

    public static SolidDMatrix random(int rowCount, int colCount) {
        Normal normal = Normal.std();
        return SolidDMatrix.fill(rowCount, colCount, (r, c) -> normal.sampleNext());
    }

    /**
     * Builds a new matrix filled with a given value.
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fill     value which fills all cells of the matrix
     * @return new matrix filled with value
     */
    public static SolidDMatrix fill(int rowCount, int colCount, double fill) {
        SolidDMatrix ret = new SolidDMatrix(rowCount, colCount);
        if (fill != 0.0) {
            for (int i = 0; i < rowCount; i++) {
                Arrays.fill(ret.values[i], fill);
            }
        }

        return ret;
    }

    /**
     * Builds a new matrix filled with a given value
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fun      lambda function which computes a value given row and column positions
     * @return new matrix filled with value
     */
    public static SolidDMatrix fill(int rowCount, int colCount, IntIntDoubleBiFunction fun) {
        SolidDMatrix ret = new SolidDMatrix(rowCount, colCount);
        for (int i = 0; i < ret.rowCount(); i++) {
            for (int j = 0; j < ret.colCount(); j++) {
                ret.set(i, j, fun.applyAsDouble(i, j));
            }
        }
        return ret;
    }

    /**
     * Builds a new matrix from a linearized array of values.
     * The array contains the values by row, aka first elements in {@param source}
     * is the first row, followed by the second row elements, and so on.
     * and so on.
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param source   value array
     * @return new matrix which contains a copy of the source
     */
    public static SolidDMatrix copy(int rowCount, int colCount, double... source) {
        SolidDMatrix m = empty(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(source, i * colCount, m.values[i], 0, colCount);
        }
        return m;
    }

    public static SolidDMatrix wrap(double[][] source) {
        int colCount = source[0].length;
        int rowCount = source.length;
        return new SolidDMatrix(rowCount, colCount, source);
    }

    public static SolidDMatrix copy(double[][] source) {
        int colCount = source[0].length;
        int rowCount = source.length;
        SolidDMatrix m = empty(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(source[i], 0, m.values[i], 0, colCount);
        }
        return m;
    }

    public static DMatrix copy(double[][] source, int rowStart, int rowEnd, int colStart, int colEnd) {
        DMatrix mm = new SolidDMatrix(rowEnd - rowStart, colEnd - colStart);
        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = colStart; j < colEnd; j++) {
                mm.set(i - rowStart, j - colStart, source[i][j]);
            }
        }
        return mm;
    }

    public static SolidDMatrix copy(Frame df) {
        SolidDMatrix m = empty(df.rowCount(), df.varCount());
        for (int j = 0; j < df.varCount(); j++) {
            for (int i = 0; i < df.rowCount(); i++) {
                m.set(i, j, df.getDouble(i, j));
            }
        }
        return m;
    }

    public static SolidDMatrix copy(Var... vars) {
        Frame df = BoundFrame.byVars(vars);
        SolidDMatrix m = empty(df.rowCount(), df.varCount());
        for (int j = 0; j < df.varCount(); j++) {
            for (int i = 0; i < df.rowCount(); i++) {
                m.set(i, j, df.getDouble(i, j));
            }
        }
        return m;
    }

    private SolidDMatrix(int rowCount, int colCount) {
        super(rowCount, colCount);
    }

    private SolidDMatrix(int rowCount, int colCount, double[][] values) {
        super(rowCount, colCount, values);
    }

    @Override
    public DMatrix t() {
        SolidDMatrix t = new SolidDMatrix(colCount, rowCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                t.values[j][i] = get(i, j);
            }
        }
        return t;
    }

    @Override
    public SolidDMatrix copy() {
        SolidDMatrix copy = new SolidDMatrix(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(values[i], 0, copy.values[i], 0, values[i].length);
        }
        return copy;
    }
}
