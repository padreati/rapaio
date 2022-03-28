/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear.decomposition;

import java.io.Serial;
import java.util.Arrays;
import java.util.function.BiConsumer;

import rapaio.math.linear.DMatrix;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.collection.IntArrays;

/**
 * LU Decomposition.
 * <p>
 * For an m-by-n matrix A with m >= n, the LU decomposition is an m-by-n unit
 * lower triangular matrix L, an n-by-n upper triangular matrix U, and a
 * permutation var piv of length m so that A.mapRows(piv) = L*U. If m < n, then L
 * is m-by-m and U is m-by-n.
 * <p>
 * The LU decompostion with pivoting always exists, even if the matrix is
 * singular, so the constructor will never fail. The primary use of the LU
 * decomposition is in the solution of square systems of simultaneous linear
 * equations. This will fail if isNonSingular() returns false.
 */
public class DBaseLUDecomposition extends DLUDecomposition {

    @Serial
    private static final long serialVersionUID = -4226024886673558685L;

    // internal storage of decomposition
    private DMatrix LU;
    private int rowCount;
    private int colCount;
    // pivot sign
    private int pivSign;
    // internal storage for row pivot indexes
    private int[] piv;

    public DBaseLUDecomposition(DMatrix A, Method method) {
        super(A, method);
        switch (method) {
            case CROUT -> buildCrout();
            case GAUSSIAN_ELIMINATION -> buildGaussianElimination();
        }
    }

    @Override
    public boolean isNonSingular() {
        for (int j = 0; j < colCount; j++) {
            if (LU.get(j, j) == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public DMatrix l() {
        DMatrix X = DMatrix.empty(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j <= i; j++) {
                if (i > j) {
                    X.set(i, j, LU.get(i, j));
                } else {
                    X.set(i, j, 1.0);
                }
            }
        }
        return X;
    }

    @Override
    public DMatrix u() {
        DMatrix U = DMatrix.empty(colCount, colCount);
        for (int i = 0; i < colCount; i++) {
            for (int j = i; j < colCount; j++) {
                U.set(i, j, LU.get(i, j));
            }
        }
        return U;
    }

    @Override
    public int[] getPivot() {
        return Arrays.copyOf(piv, rowCount);
    }

    @Override
    public double det() {
        if (rowCount != colCount) {
            throw new IllegalArgumentException("The determinant can be computed only for squared matrices.");
        }
        double d = pivSign;
        for (int j = 0; j < colCount; j++) {
            d *= LU.get(j, j);
        }
        return d;
    }

    @Override
    public DMatrix solve(DMatrix B) {
        if (B.rows() != rowCount) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!isNonSingular()) {
            throw new IllegalArgumentException("Matrix is singular.");
        }

        // Copy right hand side with pivoting
        int nx = B.cols();
        DMatrix X = B.mapRows(piv).copy();

        // Solve L*Y = B(piv,:)

        for (int k = 0; k < colCount; k++) {
            for (int i = k + 1; i < colCount; i++) {
                for (int j = 0; j < nx; j++) {
                    X.set(i, j, X.get(i, j) - X.get(k, j) * LU.get(i, k));
                }
            }
        }

        // Solve U*X = Y;

        for (int k = colCount - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                X.set(k, j, X.get(k, j) / LU.get(k, k));
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    X.set(i, j, X.get(i, j) - X.get(k, j) * LU.get(i, k));
                }
            }
        }
        return X;
    }

    /**
     * LU Decomposition, computed by Gaussian elimination. It computes L and U
     * with the "daxpy"-based elimination algorithm used in LINPACK and MATLAB.
     **/
    public void buildCrout() {

        LU = ref.copy();
        rowCount = ref.rows();
        colCount = ref.cols();
        piv = IntArrays.newSeq(rowCount);
        pivSign = 1;
        double[] LUcolj = new double[rowCount];

        // Outer loop.
        for (int j = 0; j < colCount; j++) {

            // Make a copy of the j-th column to localize references.
            for (int i = 0; i < rowCount; i++) {
                LUcolj[i] = LU.get(i, j);
            }

            // Apply previous transformations.
            for (int i = 0; i < rowCount; i++) {

                // Most of the time is spent in the following dot product.

                int kmax = Math.min(i, j);
                double s = 0.0;
                for (int k = 0; k < kmax; k++) {
                    s += LU.get(i, k) * LUcolj[k];
                }
                LUcolj[i] -= s;
                LU.set(i, j, LUcolj[i]);
            }

            // Find pivot and exchange if necessary.

            int p = j;
            for (int i = j + 1; i < LU.rows(); i++) {
                if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
                    p = i;
                }
            }
            if (p != j) {
                for (int k = 0; k < LU.cols(); k++) {
                    double t = LU.get(p, k);
                    LU.set(p, k, LU.get(j, k));
                    LU.set(j, k, t);
                }
                int k = piv[p];
                piv[p] = piv[j];
                piv[j] = k;
                pivSign = -pivSign;
            }

            // Compute multipliers.
            if (j < LU.rows() && LU.get(j, j) != 0.0) {
                for (int i = j + 1; i < LU.rows(); i++) {
                    LU.set(i, j, LU.get(i, j) / LU.get(j, j));
                }
            }
        }
    }

    /**
     * LU Decomposition, computed by Gaussian elimination. It computes L and U
     * with the "daxpy"-based elimination algorithm used in LINPACK and MATLAB.
     */
    public void buildGaussianElimination() {
        // Initialize.
        LU = ref.copy();
        rowCount = ref.rows();
        colCount = ref.cols();
        piv = IntArrays.newSeq(rowCount);
        pivSign = 1;
        // Main loop.
        for (int k = 0; k < colCount; k++) {
            // Find pivot.
            int p = k;
            for (int i = k + 1; i < rowCount; i++) {
                if (Math.abs(LU.get(i, k)) > Math.abs(LU.get(p, k))) {
                    p = i;
                }
            }
            // Exchange if necessary.
            if (p != k) {
                for (int j = 0; j < colCount; j++) {
                    double t = LU.get(p, j);
                    LU.set(p, j, LU.get(k, j));
                    LU.set(k, j, t);
                }
                int t = piv[p];
                piv[p] = piv[k];
                piv[k] = t;
                pivSign = -pivSign;
            }
            // Compute multipliers and eliminate k-th column.
            if (LU.get(k, k) != 0.0) {
                for (int i = k + 1; i < rowCount; i++) {
                    LU.set(i, k, LU.get(i, k) / LU.get(k, k));
                    for (int j = k + 1; j < colCount; j++) {
                        LU.set(i, j, LU.get(i, j) - LU.get(i, k) * LU.get(k, j));
                    }
                }
            }
        }
    }
}
