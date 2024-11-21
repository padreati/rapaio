/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.narray.matrix;

import java.io.Serializable;

import rapaio.narray.DType;
import rapaio.narray.NArray;
import rapaio.narray.Order;
import rapaio.narray.Shape;
import rapaio.narray.NArrayManager;

/**
 * Cholesky Decomposition.
 * <p>
 * For a symmetric, positive definite matrix A, the Cholesky decomposition is a lower triangular matrix L so that A = L*L'.
 * <p>
 * If the matrix is not symmetric or positive definite, the constructor returns a partial decomposition and sets an internal
 * flag that may be queried by the {@code #isSPD()} method.
 */
public class CholeskyDecomposition<N extends Number> implements Serializable {

    protected final DType<N> dt;
    protected final NArrayManager tm;
    protected final NArray<N> ref;
    protected final boolean rightFlag;
    protected boolean spd = false;
    /**
     * Left lower triangular decomposition
     */
    private NArray<N> l;
    /**
     * Right upper triangular decomposition
     */
    private NArray<N> r;

    public CholeskyDecomposition(NArray<N> ref, boolean rightFlag) {
        if (ref.rank() != 2) {
            throw new IllegalArgumentException("Only matrix tensors can have Cholesky decomposition.");
        }
        if (ref.dim(0) != ref.dim(1)) {
            throw new IllegalArgumentException("Only square matrices can have Cholesky decomposition.");
        }
        if(ref.dtype().isInteger()) {
            throw new IllegalArgumentException("Cannot compute decomposition for integer types (dtype: "+ref.dtype().id()+")");
        }
        this.dt = ref.dtype();
        this.tm = ref.manager();
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

    public NArray<N> l() {
        return l;
    }

    public NArray<N> r() {
        return r;
    }

    /**
     * Left Triangular Cholesky Decomposition.
     * <p>
     * For a symmetric, positive definite matrix A, the Left Triangular Cholesky decomposition is a lower
     * triangular matrix L so that A = L*L'.
     */
    protected void leftCholesky() {
        int n = ref.dim(0);
        l = ref.manager().zeros(ref.dtype(), Shape.of(n, n), Order.C);
        spd = (ref.dim(1) == n);
        for (int i = 0; i < n; i++) {
            double d = 0.0;
            for (int j = 0; j < i; j++) {
                double s = 0.0;
                for (int k = 0; k < j; k++) {
                    s += l.getDouble(i, k) * l.getDouble(j, k);
                }
                l.setDouble((ref.getDouble(i, j) - s) / l.getDouble(j, j), i, j);
                d += l.getDouble(i, j) * l.getDouble(i, j);
                spd = spd && (ref.getDouble(j, i) == ref.getDouble(i, j));
            }
            d = ref.getDouble(i, i) - d;
            if (d <= 0.0) {
                spd = false;
            }
            l.setDouble(Math.sqrt(Math.max(d, 0.0)), i, i);
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
        int n = ref.dim(1);
        r = tm.zeros(dt, Shape.of(n, n));
        spd = (ref.dim(0) == n);
        for (int j = 0; j < n; j++) {
            double d = 0.0;
            for (int k = 0; k < j; k++) {
                double s = ref.getDouble(k, j);
                for (int i = 0; i < k; i++) {
                    s -= r.getDouble(i, k) * r.getDouble(i, j);
                }
                r.setDouble(s / r.getDouble(k, k), k, j);
                d += r.getDouble(k, j) * r.getDouble(k, j);
                spd = spd & (ref.getDouble(k, j) == ref.getDouble(j, k));
            }
            d = ref.getDouble(j, j) - d;
            spd = spd & (d > 0.0);
            r.setDouble(Math.sqrt(Math.max(d, 0.0)), j, j);
        }
    }

    /**
     * Solve A*x=b linear system when A is symmetric positive definite.
     *
     * @param b result vector
     * @return coefficient vector
     */
    public NArray<N> solve(NArray<N> b) {
        boolean vectorShape = b.rank() == 1;
        if (vectorShape) {
            b = b.stretch(1);
        }
        if (b.dim(0) != ref.dim(0)) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!spd) {
            throw new IllegalArgumentException("Matrix is not symmetric positive definite.");
        }

        // Copy right-hand side.
        NArray<N> x = b.copy();
        NArray<N> triangle = rightFlag ? r.t() : l;

        forwardSubstitution(x, triangle);
        backwardSubstitution(x, triangle);

        return vectorShape ? x.squeeze(1) : x;
    }

    public NArray<N> inv() {
        if (!spd) {
            throw new IllegalArgumentException("Matrix is not symmetric positive definite.");
        }

        // Copy right-hand side.
        NArray<N> x = tm.eye(dt, ref.dim(0), Order.C);
        NArray<N> triangle = rightFlag ? r.t() : l;

        forwardSubstitution(x, triangle);
        backwardSubstitution(x, triangle);

        return x;
    }

    protected void backwardSubstitution(NArray<N> x, NArray<N> l) {
        // Solve L'*X = Y;
        for (int k = ref.dim(0) - 1; k >= 0; k--) {
            for (int j = 0; j < x.dim(1); j++) {
                for (int i = k + 1; i < ref.dim(0); i++) {
                    x.incDouble(-x.getDouble(i, j) * l.getDouble(i, k), k, j);
                }
                x.setDouble(x.getDouble(k, j) / l.getDouble(k, k), k, j);
            }
        }
    }

    protected void forwardSubstitution(NArray<N> x, NArray<N> l) {
        // Solve L*Y = B;
        for (int k = 0; k < ref.dim(0); k++) {
            for (int j = 0; j < x.dim(1); j++) {
                for (int i = 0; i < k; i++) {
                    x.incDouble(-x.getDouble(i, j) * l.getDouble(k, i), k, j);
                }
                x.setDouble(x.getDouble(k, j) / l.getDouble(k, k), k, j);
            }
        }
    }
}
