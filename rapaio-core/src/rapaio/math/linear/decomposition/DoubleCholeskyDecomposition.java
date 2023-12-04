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

package rapaio.math.linear.decomposition;

import java.io.Serializable;

import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.math.linear.DMatrix;

/**
 * Cholesky Decomposition.
 * <p>
 * For a symmetric, positive definite matrix A, the Cholesky decomposition is a lower triangular matrix L so that A = L*L'.
 * <p>
 * If the matrix is not symmetric or positive definite, the constructor returns a partial decomposition and sets an internal
 * flag that may be queried by the {@code #isSPD()} method.
 */
public class DoubleCholeskyDecomposition implements Serializable {

    protected final DMatrix ref;
    protected final boolean rightFlag;
    protected boolean spd = false;
    /**
     * Left lower triangular decomposition
     */
    private DMatrixDenseC l;
    /**
     * Right upper triangular decomposition
     */
    private DMatrixDenseC r;

    public DoubleCholeskyDecomposition(DMatrix ref, boolean rightFlag) {
        if (ref.rows() != ref.cols()) {
            throw new IllegalArgumentException("Only square matrices can have Cholesky decomposition.");
        }
        this.ref = ref;
        this.rightFlag = rightFlag;

        if (!rightFlag) {
            leftCholesky();
        } else {
            rightCholesky();
        }
    }

    /**
     * @return true if A is symmetric and positive definite.
     */
    public boolean isSPD() {
        return spd;
    }

    public boolean hasRightFlag() {
        return rightFlag;
    }

    public DMatrix l() {
        return l;
    }

    /**
     * Left Triangular Cholesky Decomposition.
     * <p>
     * For a symmetric, positive definite matrix A, the Left Triangular Cholesky decomposition is a lower
     * triangular matrix L so that A = L*L'.
     */
    protected void leftCholesky() {
        int n = ref.rows();
        l = new DMatrixDenseC(n, n);
        spd = (ref.cols() == n);
        for (int i = 0; i < n; i++) {
            double d = 0.0;
            for (int j = 0; j < i; j++) {
                double s = 0.0;
                for (int k = 0; k < j; k++) {
                    s += l.get(i, k) * l.get(j, k);
                }
                l.set(i, j, (ref.get(i, j) - s) / l.get(j, j));
                d += l.get(i, j) * l.get(i, j);
                spd = spd && (ref.get(j, i) == ref.get(i, j));
            }
            d = ref.get(i, i) - d;
            if (d <= 0.0) {
                spd = false;
            }
            l.set(i, i, Math.sqrt(Math.max(d, 0.0)));
        }
    }

    /**
     * Right Triangular Cholesky Decomposition.
     * <p>
     * For a symmetric, positive definite matrix A, the Right Cholesky decomposition is an upper
     * triangular matrix R so that A = R'*R. This constructor computes R with
     * the Fortran inspired column oriented algorithm used in LINPACK and MATLAB.
     */
    protected void rightCholesky() {
        int n = ref.cols();
        r = new DMatrixDenseC(n, n);
        spd = (ref.cols() == n);
        for (int j = 0; j < n; j++) {
            double d = 0.0;
            for (int k = 0; k < j; k++) {
                double s = ref.get(k, j);
                for (int i = 0; i < k; i++) {
                    s -= r.get(i, k) * r.get(i, j);
                }
                r.set(k, j, s / r.get(k, k));
                d += r.get(k, j) * r.get(k, j);
                spd = spd & (ref.get(k, j) == ref.get(j, k));
            }
            d = ref.get(j, j) - d;
            spd = spd & (d > 0.0);
            r.set(j, j, Math.sqrt(Math.max(d, 0.0)));
        }
    }

    public DMatrix r() {
        return r;
    }

    public DMatrix solve(DMatrix b) {
        if (b.rows() != ref.rows()) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!spd) {
            throw new IllegalArgumentException("Matrix is not symmetric positive definite.");
        }

        // Copy right-hand side.
        DMatrix x = b.copy();
        DMatrix triangle = rightFlag ? r.t() : l;

        forwardSubstitution(x, triangle);
        backwardSubstitution(x, triangle);

        return x;
    }

    /**
     * Solve A*x=b linear system when A is symmetric positive definite.
     *
     * @param b result vector
     * @return coefficient vector
     */
    public DVector solve(DVector b) {
        return solve(b.asMatrix()).mapCol(0);
    }

    public DMatrix inv() {
        if (!spd) {
            throw new IllegalArgumentException("Matrix is not symmetric positive definite.");
        }

        // Copy right-hand side.
        DMatrix x = DMatrixDenseC.eye(ref.rows());
        DMatrix triangle = rightFlag ? r.t() : l;

        forwardSubstitution(x, triangle);
        backwardSubstitution(x, triangle);

        return x;
    }

    protected void backwardSubstitution(DMatrix x, DMatrix l) {
        // Solve L'*X = Y;
        for (int k = ref.rows() - 1; k >= 0; k--) {
            for (int j = 0; j < x.cols(); j++) {
                for (int i = k + 1; i < ref.rows(); i++) {
                    x.inc(k, j, -x.get(i, j) * l.get(i, k));
                }
                x.set(k, j, x.get(k, j) / l.get(k, k));
            }
        }
    }

    protected void forwardSubstitution(DMatrix x, DMatrix l) {
        // Solve L*Y = B;
        for (int k = 0; k < ref.rows(); k++) {
            for (int j = 0; j < x.cols(); j++) {
                for (int i = 0; i < k; i++) {
                    x.inc(k, j, -x.get(i, j) * l.get(k, i));
                }
                x.set(k, j, x.get(k, j) / l.get(k, k));
            }
        }
    }
}
