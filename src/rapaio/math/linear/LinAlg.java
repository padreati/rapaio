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

package rapaio.math.linear;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.math.linear.impl.SolidRMatrix;
import rapaio.math.linear.impl.SolidRVector;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
public final class LinAlg {

    /**
     * Builds a new 0 filled matrix with given rows and cols
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @return new matrix object
     */
    public static RMatrix newMatrixEmpty(int rowCount, int colCount) {
        return new SolidRMatrix(rowCount, colCount);
    }

    public static RMatrix newMatrixWrapOf(int rowCount, int colCount, double... values) {
        return new SolidRMatrix(rowCount, colCount, values);
    }

    public static RMatrix newMatrixCopyOf(double[][] source) {
        return newMatrixCopyOf(source, 0, source.length, 0, source[0].length);
    }

    public static RMatrix newMatrixCopyOf(double[][] source, int mFirst, int mLast, int nFirst, int nLast) {
        RMatrix mm = new SolidRMatrix(mLast - mFirst, nLast - nFirst);
        for (int i = mFirst; i < mLast; i++) {
            for (int j = nFirst; j < nLast; j++) {
                mm.set(i, j, source[i][j]);
            }
        }
        return mm;
    }

    public static RMatrix newMatrixCopyOf(Frame df) {
        RMatrix RMatrix = new SolidRMatrix(df.rowCount(), df.varCount());
        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < df.varCount(); j++) {
                RMatrix.set(i, j, df.value(i, j));
            }
        }
        return RMatrix;
    }

    public static RMatrix newMatrixCopyOf(Var... vars) {
        int rowCount = Arrays.stream(vars).mapToInt(Var::rowCount).min().getAsInt();
        RMatrix RMatrix = new SolidRMatrix(rowCount, vars.length);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < vars.length; j++) {
                RMatrix.set(i, j, vars[j].value(i));
            }
        }
        return RMatrix;
    }

    /**
     * Builds a new matrix with given rows and cols, fillen with given value
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fill     initial value for all matrix cells
     * @return new matrix object
     */
    public static RMatrix newMatrixFill(int rowCount, int colCount, double fill) {
        if (fill == 0) {
            return newMatrixEmpty(rowCount, colCount);
        }
        RMatrix RMatrix = new SolidRMatrix(rowCount, colCount);
        for (int i = 0; i < RMatrix.rowCount(); i++) {
            for (int j = 0; j < RMatrix.colCount(); j++) {
                RMatrix.set(i, j, fill);
            }
        }
        return RMatrix;
    }

    public static RMatrix newMatrixFill(int rowCount, int colCount, BiFunction<Integer, Integer, Double> f) {
        RMatrix RMatrix = new SolidRMatrix(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                RMatrix.set(i, j, f.apply(i, j));
            }
        }
        return RMatrix;
    }

    public static RVector newVectorCopyOf(Var var) {
        return new SolidRVector(var);
    }

    public static RVector newVectorEmpty(int rows) {
        return new SolidRVector(rows);
    }

    public static RMatrix newMatrixId(int n) {
        RMatrix id = LinAlg.newMatrixEmpty(n, n);
        for (int i = 0; i < n; i++) {
            id.set(i, i, 1.0);
        }
        return id;
    }

    // tools

    public static RMatrix chol2inv(RMatrix R) {
        return chol2inv(R, LinAlg.newMatrixId(R.rowCount()));
    }

    public static RMatrix chol2inv(RMatrix R, RMatrix B) {
        RMatrix ref = R.t();
        if (B.rowCount() != R.rowCount()) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }

        // Copy right hand side.
        RMatrix X = B.solidCopy();

        // Solve L*Y = B;
        for (int k = 0; k < ref.rowCount(); k++) {
            for (int j = 0; j < X.colCount(); j++) {
                for (int i = 0; i < k; i++) {
                    X.increment(k, j, -X.get(i, j) * ref.get(k, i));
                }
                X.set(k, j, X.get(k, j) / ref.get(k, k));
            }
        }

        // Solve L'*X = Y;
        for (int k = ref.rowCount() - 1; k >= 0; k--) {
            for (int j = 0; j < X.colCount(); j++) {
                for (int i = k + 1; i < ref.rowCount(); i++) {
                    X.increment(k, j, -X.get(i, j) * ref.get(i, k));
                }
                X.set(k, j, X.get(k, j) / ref.get(k, k));
            }
        }
        return X;
    }

}
