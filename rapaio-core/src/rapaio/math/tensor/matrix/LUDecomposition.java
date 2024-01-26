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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.matrix;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.min;

import java.io.Serializable;
import java.util.Arrays;

import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;
import rapaio.printer.Format;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;
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
public class LUDecomposition<N extends Number> implements Serializable, Printable {

    public enum Method {
        CROUT,
        GAUSSIAN_ELIMINATION
    }

    private final TensorManager.OfType<N> tmt;
    protected final Tensor<N> ref;
    protected final Method method;

    // internal storage of decomposition
    private Tensor<N> LU;
    // pivot sign
    private int pivSign;
    // internal storage for row pivot indexes
    private int[] piv;

    public LUDecomposition(Tensor<N> ref, Method method) {
        if (ref.dim(0) < ref.dim(1)) {
            throw new IllegalArgumentException("For LU decomposition, number of rows must be greater or equal with number of columns.");
        }
        this.tmt = ref.manager().ofType(ref.dtype());
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
        piv = IntArrays.newSeq(ref.dim(0));
        pivSign = 1;
        double[] LUcolj = new double[ref.dim(0)];

        // Outer loop.
        for (int j = 0; j < ref.dim(1); j++) {

            // Make a copy of the j-th column to localize references.
            for (int i = 0; i < ref.dim(0); i++) {
                LUcolj[i] = LU.getDouble(i, j);
            }

            // Apply previous transformations.
            for (int i = 0; i < ref.dim(0); i++) {

                // Most of the time is spent in the following dot product.

                int kmax = min(i, j);
                double s = 0.0;
                for (int k = 0; k < kmax; k++) {
                    s += LU.getDouble(i, k) * LUcolj[k];
                }
                LUcolj[i] -= s;
                LU.setDouble(LUcolj[i], i, j);
            }

            // Find pivot and exchange if necessary.

            int p = j;
            for (int i = j + 1; i < LU.dim(0); i++) {
                if (abs(LUcolj[i]) > abs(LUcolj[p])) {
                    p = i;
                }
            }
            if (p != j) {
                for (int k = 0; k < LU.dim(1); k++) {
                    double t = LU.getDouble(p, k);
                    LU.setDouble(LU.getDouble(j, k), p, k);
                    LU.setDouble(t, j, k);
                }
                int k = piv[p];
                piv[p] = piv[j];
                piv[j] = k;
                pivSign = -pivSign;
            }

            // Compute multipliers.
            if (j < LU.dim(0) && LU.getDouble(j, j) != 0.0) {
                for (int i = j + 1; i < LU.dim(0); i++) {
                    LU.setDouble(LU.getDouble(i, j) / LU.getDouble(j, j), i, j);
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
        piv = IntArrays.newSeq(ref.dim(0));
        pivSign = 1;
        // Main loop.
        for (int k = 0; k < ref.dim(1); k++) {
            // Find pivot.
            int p = k;
            for (int i = k + 1; i < ref.dim(0); i++) {
                if (abs(LU.getDouble(i, k)) > abs(LU.getDouble(p, k))) {
                    p = i;
                }
            }
            // Exchange if necessary.
            if (p != k) {
                for (int j = 0; j < ref.dim(1); j++) {
                    double t = LU.getDouble(p, j);
                    LU.setDouble(LU.getDouble(k, j), p, j);
                    LU.setDouble(t, k, j);
                }
                int t = piv[p];
                piv[p] = piv[k];
                piv[k] = t;
                pivSign = -pivSign;
            }
            // Compute multipliers and eliminate k-th column.
            if (LU.getDouble(k, k) != 0.0) {
                for (int i = k + 1; i < ref.dim(0); i++) {
                    LU.setDouble(LU.getDouble(i, k) / LU.getDouble(k, k), i, k);
                    for (int j = k + 1; j < ref.dim(1); j++) {
                        LU.incDouble(-LU.getDouble(i, k) * LU.getDouble(k, j), i, j);
                    }
                }
            }
        }
    }

    public boolean isNonSingular() {
        for (int j = 0; j < ref.dim(1); j++) {
            if (LU.getDouble(j, j) == 0) {
                return false;
            }
        }
        return true;
    }

    public Tensor<N> l() {
        Tensor<N> x = tmt.zeros(Shape.of(ref.dim(0), ref.dim(1)));
        int i = 0;
        for (; i < ref.dim(1); i++) {
            for (int j = 0; j < i; j++) {
                x.setDouble(LU.getDouble(i, j), i, j);
            }
            x.setDouble(1.0, i, i);
        }
        for (; i < ref.dim(0); i++) {
            for (int j = 0; j < ref.dim(1); j++) {
                x.setDouble(LU.getDouble(i, j), i, j);
            }
        }
        return x;
    }

    public Tensor<N> u() {
        Tensor<N> U = tmt.zeros(Shape.of(ref.dim(1), ref.dim(1)));
        for (int i = 0; i < ref.dim(1); i++) {
            for (int j = i; j < ref.dim(1); j++) {
                U.setDouble(LU.getDouble(i, j), i, j);
            }
        }
        return U;
    }

    public int[] pivots() {
        return Arrays.copyOf(piv, ref.dim(0));
    }

    public double det() {
        if (ref.dim(0) != ref.dim(1)) {
            throw new IllegalArgumentException("The determinant can be computed only for squared matrices.");
        }
        double d = pivSign;
        for (int j = 0; j < ref.dim(1); j++) {
            d *= LU.getDouble(j, j);
        }
        return d;
    }

    public Tensor<N> solve(Tensor<N> B) {
        if (B.dim(0) != ref.dim(0)) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!isNonSingular()) {
            throw new IllegalArgumentException("Matrix is singular.");
        }

        // Copy right hand side with pivoting
        int nx = B.dim(1);
        Tensor<N> X = B.take(0, piv).copy();

        // Solve L*Y = B(piv,:)

        for (int k = 0; k < ref.dim(1); k++) {
            for (int i = k + 1; i < ref.dim(1); i++) {
                for (int j = 0; j < nx; j++) {
                    X.incDouble(-X.getDouble(k, j) * LU.getDouble(i, k), i, j);
                }
            }
        }

        // Solve U*X = Y;

        for (int k = ref.dim(1) - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                X.setDouble(X.getDouble(k, j) / LU.getDouble(k, k), k, j);
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    X.incDouble(-X.getDouble(k, j) * LU.getDouble(i, k), i, j);
                }
            }
        }
        return X;
    }


    public Tensor<N> inv() {
        return solve(tmt.eye(ref.dim(0)));
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("LU decomposition summary\n")
                .append("========================\n")
                .append("\nL matrix\n")
                .append(l().toContent(printer, options))
                .append("\nU matrix:\n")
                .append(u().toContent(printer, options))
                .append("\npivots: [");
        int[] pivots = pivots();
        for (int i = 0; i < Math.min(12, pivots.length); i++) {
            sb.append(pivots[i]).append(",");
        }
        if (pivots.length > 12) {
            sb.append("...");
        }
        sb.append("]");

        if (ref.dim(0) == ref.dim(1)) {
            sb.append("\ndet: ").append(Format.floatFlex(det()));
        }
        return sb.toString();
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        return toString();
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return toString();
    }
}
