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
import rapaio.math.linear.DM;
import rapaio.math.linear.DV;
import rapaio.math.linear.base.DMBase;
import rapaio.util.collection.DArrays;
import rapaio.util.function.IntInt2DoubleBiFunction;

import java.util.Arrays;

/**
 * Dense matrix with values in double floating point precision.
 * Values are stored in arrays of arrays with first array holding row references
 * and secondary level arrays being the row arrays.
 */
public class DMStripe extends DMBase {

    private static final long serialVersionUID = -2186520026933442642L;

    /**
     * Builds a zero filled matrix with n rows and m columns
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @return a new instance of the matrix object
     */
    public static DMStripe empty(int rowCount, int colCount) {
        return new DMStripe(rowCount, colCount);
    }

    /**
     * Builds an identity matrix with n rows and n columns.
     * An identity matrix is a matrix with 1 on the main diagonal
     * and 0 otherwise.
     *
     * @param n number of rows and also number of columns
     * @return a new instance of identity matrix of order n
     */
    public static DMStripe identity(int n) {
        DMStripe m = new DMStripe(n, n);
        for (int i = 0; i < n; i++) {
            m.set(i, i, 1.0);
        }
        return m;
    }

    public static DMStripe random(int rowCount, int colCount) {
        Normal normal = Normal.std();
        return rapaio.math.linear.dense.DMStripe.fill(rowCount, colCount, (r, c) -> normal.sampleNext());
    }

    /**
     * Builds a new matrix filled with a given value.
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fill     value which fills all cells of the matrix
     * @return new matrix filled with value
     */
    public static DMStripe fill(int rowCount, int colCount, double fill) {
        DMStripe ret = new DMStripe(rowCount, colCount);
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
    public static DMStripe fill(int rowCount, int colCount, IntInt2DoubleBiFunction fun) {
        DMStripe ret = new DMStripe(rowCount, colCount);
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
    public static DMStripe copy(int rowCount, int colCount, double... source) {
        DMStripe m = empty(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(source, i * colCount, m.values[i], 0, colCount);
        }
        return m;
    }

    public static DMStripe wrap(double[][] source) {
        int colCount = source[0].length;
        int rowCount = source.length;
        return new DMStripe(rowCount, colCount, source);
    }

    public static DMStripe copy(double[][] source) {
        int colCount = source[0].length;
        int rowCount = source.length;
        DMStripe m = empty(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(source[i], 0, m.values[i], 0, colCount);
        }
        return m;
    }

    public static DM copy(double[][] source, int rowStart, int rowEnd, int colStart, int colEnd) {
        DM mm = new DMStripe(rowEnd - rowStart, colEnd - colStart);
        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = colStart; j < colEnd; j++) {
                mm.set(i - rowStart, j - colStart, source[i][j]);
            }
        }
        return mm;
    }

    public static DMStripe copy(Frame df) {
        DMStripe m = empty(df.rowCount(), df.varCount());
        for (int j = 0; j < df.varCount(); j++) {
            for (int i = 0; i < df.rowCount(); i++) {
                m.set(i, j, df.getDouble(i, j));
            }
        }
        return m;
    }

    public static DMStripe copy(Var... vars) {
        Frame df = BoundFrame.byVars(vars);
        DMStripe m = empty(df.rowCount(), df.varCount());
        for (int j = 0; j < df.varCount(); j++) {
            for (int i = 0; i < df.rowCount(); i++) {
                m.set(i, j, df.getDouble(i, j));
            }
        }
        return m;
    }

    private DMStripe(int rowCount, int colCount) {
        super(rowCount, colCount);
    }

    private DMStripe(int rowCount, int colCount, double[][] values) {
        super(rowCount, colCount, values);
    }

    @Override
    public Type type() {
        return Type.STRIPE;
    }

    @Override
    public DM dotDiag(DV v) {
        if (v instanceof DVDense) {
            var array = v.asDense().elements();
            var len = v.size();
            for (int i = 0; i < rowCount; i++) {
                DArrays.mult(values[i], 0, array, 0, len);
            }
            return this;
        }
        return super.dotDiag(v);
    }

    @Override
    public DM dotDiagT(DV v) {
        if (v.isDense()) {
            var array = v.asDense().elements();
            var len = v.size();
            for (int i = 0; i < rowCount; i++) {
                DArrays.mult(values[i], 0, array[i], colCount);
            }
            return this;
        }
        return super.dotDiagT(v);
    }

    @Override
    public DM t() {
        DMStripe t = new DMStripe(colCount, rowCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                t.values[j][i] = get(i, j);
            }
        }
        return t;
    }

    @Override
    public DMStripe copy() {
        DMStripe copy = new DMStripe(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(values[i], 0, copy.values[i], 0, values[i].length);
        }
        return copy;
    }
}
