/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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
import rapaio.math.linear.DVector;
import rapaio.math.linear.base.DMatrixBase;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.IntInt2DoubleBiFunction;

import java.util.Arrays;

/**
 * Dense matrix with values in double floating point precision.
 * Values are stored in arrays of arrays with first array holding row references
 * and secondary level arrays being the row arrays.
 */
public class DMatrixStripe extends DMatrixBase {

    private static final long serialVersionUID = -2186520026933442642L;

    /**
     * Builds a zero filled matrix with n rows and m columns
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @return a new instance of the matrix object
     */
    public static DMatrixStripe empty(int rowCount, int colCount) {
        return new DMatrixStripe(rowCount, colCount);
    }

    /**
     * Builds an identity matrix with n rows and n columns.
     * An identity matrix is a matrix with 1 on the main diagonal
     * and 0 otherwise.
     *
     * @param n number of rows and also number of columns
     * @return a new instance of identity matrix of order n
     */
    public static DMatrixStripe identity(int n) {
        DMatrixStripe m = new DMatrixStripe(n, n);
        for (int i = 0; i < n; i++) {
            m.set(i, i, 1.0);
        }
        return m;
    }

    public static DMatrixStripe random(int rowCount, int colCount) {
        Normal normal = Normal.std();
        return DMatrixStripe.fill(rowCount, colCount, (r, c) -> normal.sampleNext());
    }

    /**
     * Builds a new matrix filled with a given value.
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fill     value which fills all cells of the matrix
     * @return new matrix filled with value
     */
    public static DMatrixStripe fill(int rowCount, int colCount, double fill) {
        DMatrixStripe ret = new DMatrixStripe(rowCount, colCount);
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
    public static DMatrixStripe fill(int rowCount, int colCount, IntInt2DoubleBiFunction fun) {
        DMatrixStripe ret = new DMatrixStripe(rowCount, colCount);
        for (int i = 0; i < ret.rowCount(); i++) {
            for (int j = 0; j < ret.colCount(); j++) {
                ret.set(i, j, fun.applyIntIntAsDouble(i, j));
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
    public static DMatrixStripe copy(int rowCount, int colCount, double... source) {
        DMatrixStripe m = empty(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(source, i * colCount, m.values[i], 0, colCount);
        }
        return m;
    }

    public static DMatrixStripe wrap(double[][] source) {
        int colCount = source[0].length;
        int rowCount = source.length;
        return new DMatrixStripe(rowCount, colCount, source);
    }

    public static DMatrixStripe copy(double[][] source) {
        int colCount = source[0].length;
        int rowCount = source.length;
        DMatrixStripe m = empty(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(source[i], 0, m.values[i], 0, colCount);
        }
        return m;
    }

    public static DMatrix copy(double[][] source, int rowStart, int rowEnd, int colStart, int colEnd) {
        DMatrix mm = new DMatrixStripe(rowEnd - rowStart, colEnd - colStart);
        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = colStart; j < colEnd; j++) {
                mm.set(i - rowStart, j - colStart, source[i][j]);
            }
        }
        return mm;
    }

    public static DMatrixStripe copy(Frame df) {
        DMatrixStripe m = empty(df.rowCount(), df.varCount());
        for (int j = 0; j < df.varCount(); j++) {
            for (int i = 0; i < df.rowCount(); i++) {
                m.set(i, j, df.getDouble(i, j));
            }
        }
        return m;
    }

    public static DMatrixStripe copy(Var... vars) {
        Frame df = BoundFrame.byVars(vars);
        DMatrixStripe m = empty(df.rowCount(), df.varCount());
        for (int j = 0; j < df.varCount(); j++) {
            for (int i = 0; i < df.rowCount(); i++) {
                m.set(i, j, df.getDouble(i, j));
            }
        }
        return m;
    }

    private DMatrixStripe(int rowCount, int colCount) {
        super(rowCount, colCount);
    }

    private DMatrixStripe(int rowCount, int colCount, double[][] values) {
        super(rowCount, colCount, values);
    }

    @Override
    public Type type() {
        return Type.STRIPE;
    }

    @Override
    public DMatrix dotDiag(DVector v) {
        if (v instanceof DVectorDense) {
            var array = v.asDense().elements();
            var len = v.size();
            for (int i = 0; i < rowCount; i++) {
                DoubleArrays.mult(values[i], 0, array, 0, len);
            }
            return this;
        }
        return super.dotDiag(v);
    }

    @Override
    public DMatrix dotDiagT(DVector v) {
        if (v.isDense()) {
            var array = v.asDense().elements();
            var len = v.size();
            for (int i = 0; i < rowCount; i++) {
                DoubleArrays.mult(values[i], 0, array[i], colCount);
            }
            return this;
        }
        return super.dotDiagT(v);
    }

    @Override
    public DMatrix t() {
        DMatrixStripe t = new DMatrixStripe(colCount, rowCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                t.values[j][i] = get(i, j);
            }
        }
        return t;
    }

    @Override
    public DMatrixStripe copy() {
        DMatrixStripe copy = new DMatrixStripe(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(values[i], 0, copy.values[i], 0, values[i].length);
        }
        return copy;
    }
}
