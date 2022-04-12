/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
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

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.min;

import java.io.Serializable;
import java.util.Arrays;

import rapaio.math.linear.DMatrix;
import rapaio.printer.Format;
import rapaio.printer.Printable;
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
public class DoubleLUDecomposition implements Serializable, Printable {

    public enum Method {
        CROUT,
        GAUSSIAN_ELIMINATION
    }

    protected final DMatrix ref;
    protected final Method method;

    // internal storage of decomposition
    private DMatrix LU;
    // pivot sign
    private int pivSign;
    // internal storage for row pivot indexes
    private int[] piv;

    public DoubleLUDecomposition(DMatrix ref, Method method) {
        if (ref.rows() < ref.cols()) {
            throw new IllegalArgumentException("For LU decomposition, number of rows must be greater or equal with number of columns.");
        }
        this.ref = ref;
        this.method = method;
        switch (method) {
            case CROUT -> buildCrout();
            case GAUSSIAN_ELIMINATION -> buildGaussianElimination();
        }
    }

    /**
     * LU Decomposition, computed by Gaussian elimination. It computes L and U
     * with the "daxpy"-based elimination algorithm used in LINPACK and MATLAB.
     **/
    public void buildCrout() {

        LU = ref.copy();
        piv = IntArrays.newSeq(ref.rows());
        pivSign = 1;
        double[] LUcolj = new double[ref.rows()];

        // Outer loop.
        for (int j = 0; j < ref.cols(); j++) {

            // Make a copy of the j-th column to localize references.
            for (int i = 0; i < ref.rows(); i++) {
                LUcolj[i] = LU.get(i, j);
            }

            // Apply previous transformations.
            for (int i = 0; i < ref.rows(); i++) {

                // Most of the time is spent in the following dot product.

                int kmax = min(i, j);
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
                if (abs(LUcolj[i]) > abs(LUcolj[p])) {
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
        piv = IntArrays.newSeq(ref.rows());
        pivSign = 1;
        // Main loop.
        for (int k = 0; k < ref.cols(); k++) {
            // Find pivot.
            int p = k;
            for (int i = k + 1; i < ref.rows(); i++) {
                if (abs(LU.get(i, k)) > abs(LU.get(p, k))) {
                    p = i;
                }
            }
            // Exchange if necessary.
            if (p != k) {
                for (int j = 0; j < ref.cols(); j++) {
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
                for (int i = k + 1; i < ref.rows(); i++) {
                    LU.set(i, k, LU.get(i, k) / LU.get(k, k));
                    for (int j = k + 1; j < ref.cols(); j++) {
                        LU.set(i, j, LU.get(i, j) - LU.get(i, k) * LU.get(k, j));
                    }
                }
            }
        }
    }

    public boolean isNonSingular() {
        for (int j = 0; j < ref.cols(); j++) {
            if (LU.get(j, j) == 0) {
                return false;
            }
        }
        return true;
    }

    public DMatrix l() {
        DMatrix x = DMatrix.empty(ref.rows(), ref.cols());
        int i=0;
        for (; i < ref.cols(); i++) {
            for (int j = 0; j < i; j++) {
                x.set(i, j, LU.get(i, j));
            }
            x.set(i, i, 1.0);
        }
        for(; i<ref.rows();i++) {
            for (int j = 0; j < ref.cols(); j++) {
                x.set(i, j, LU.get(i, j));
            }
        }
        return x;
    }

    public DMatrix u() {
        DMatrix U = DMatrix.empty(ref.cols(), ref.cols());
        for (int i = 0; i < ref.cols(); i++) {
            for (int j = i; j < ref.cols(); j++) {
                U.set(i, j, LU.get(i, j));
            }
        }
        return U;
    }

    public int[] pivots() {
        return Arrays.copyOf(piv, ref.rows());
    }

    public double det() {
        if (ref.rows() != ref.cols()) {
            throw new IllegalArgumentException("The determinant can be computed only for squared matrices.");
        }
        double d = pivSign;
        for (int j = 0; j < ref.cols(); j++) {
            d *= LU.get(j, j);
        }
        return d;
    }

    public DMatrix solve(DMatrix B) {
        if (B.rows() != ref.rows()) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!isNonSingular()) {
            throw new IllegalArgumentException("Matrix is singular.");
        }

        // Copy right hand side with pivoting
        int nx = B.cols();
        DMatrix X = B.mapRows(piv).copy();

        // Solve L*Y = B(piv,:)

        for (int k = 0; k < ref.cols(); k++) {
            for (int i = k + 1; i < ref.cols(); i++) {
                for (int j = 0; j < nx; j++) {
                    X.set(i, j, X.get(i, j) - X.get(k, j) * LU.get(i, k));
                }
            }
        }

        // Solve U*X = Y;

        for (int k = ref.cols() - 1; k >= 0; k--) {
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

    public DMatrix inv() {
        return solve(DMatrix.identity(ref.rows()));
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("LU decomposition summary\n")
                .append("========================\n")
                .append("\nL matrix\n")
                .append(l().toSummary(printer, options))
                .append("\nU matrix:\n")
                .append(u().toSummary(printer, options))
                .append("\npivots: [");
        int[] pivots = pivots();
        for (int i = 0; i < Math.min(12, pivots.length); i++) {
            sb.append(pivots[i]).append(",");
        }
        if (pivots.length > 12) {
            sb.append("...");
        }
        sb.append("]");

        if (ref.rows() == ref.cols()) {
            sb.append("\ndet: ").append(Format.floatFlex(det()));
        }
        return sb.toString();
    }
}
