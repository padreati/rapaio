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

import java.io.Serial;
import java.io.Serializable;

import rapaio.math.linear.DVector;
import rapaio.math.linear.DMatrix;

public class DoubleQRDecomposition implements Serializable {

    @Serial
    private static final long serialVersionUID = -8322866575684242727L;
    protected final DMatrix ref;
    protected final DMatrix QR;
    protected final DVector diag;

    public DoubleQRDecomposition(DMatrix ref) {
        // Initialize.
        this.ref = ref;
        QR = ref.copy();
        diag = DVector.zeros(QR.cols());

        // Main loop.
        for (int k = 0; k < QR.cols(); k++) {
            // Compute 2-norm of k-th column without under/overflow.
            double nrm = 0;
            for (int i = k; i < QR.rows(); i++) {
                nrm = StrictMath.hypot(nrm, QR.get(i, k));
            }

            if (nrm != 0.0) {
                // Form k-th Householder var.
                if (QR.get(k, k) < 0) {
                    nrm = -nrm;
                }
                for (int i = k; i < QR.rows(); i++) {
                    QR.set(i, k, QR.get(i, k) / nrm);
                }
                QR.set(k, k, QR.get(k, k) + 1.0);

                // Apply transformation to remaining columns.
                for (int j = k + 1; j < QR.cols(); j++) {
                    double s = 0.0;
                    for (int i = k; i < QR.rows(); i++) {
                        s += QR.get(i, k) * QR.get(i, j);
                    }
                    s = -s / QR.get(k, k);
                    for (int i = k; i < QR.rows(); i++) {
                        QR.set(i, j, QR.get(i, j) + s * QR.get(i, k));
                    }
                }
            }
            diag.set(k, diag.get(k) - nrm);
        }
    }

    /**
     * Is the matrix full rank?
     *
     * @return true if R, and hence A, has full rank.
     */
    public boolean isFullRank() {
        for (int j = 0; j < QR.cols(); j++) {
            if (diag.get(j) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the Householder vectors
     *
     * @return Lower trapezoidal matrix whose columns define the reflections
     */
    public DMatrix h() {
        DMatrix h = DMatrix.empty(QR.rows(), QR.cols());
        for (int i = 0; i < QR.rows(); i++) {
            for (int j = 0; j < QR.cols(); j++) {
                if (i >= j) {
                    h.set(i, j, QR.get(i, j));
                } else {
                    h.set(i, j, 0.0);
                }
            }
        }
        return h;
    }

    /**
     * Return the upper triangular factor
     *
     * @return R
     */
    public DMatrix r() {
        DMatrix r = DMatrix.empty(QR.cols(), QR.cols());
        for (int i = 0; i < QR.cols(); i++) {
            for (int j = 0; j < QR.cols(); j++) {
                if (i < j) {
                    r.set(i, j, QR.get(i, j));
                } else if (i == j) {
                    r.set(i, j, diag.get(i));
                } else {
                    r.set(i, j, 0.0);
                }
            }
        }
        return r;
    }

    /**
     * Generate and return the (economy-sized) orthogonal factor
     *
     * @return Q
     */

    public DMatrix q() {
        DMatrix q = DMatrix.empty(QR.rows(), QR.cols());
        for (int k = QR.cols() - 1; k >= 0; k--) {
            for (int i = 0; i < QR.rows(); i++) {
                q.set(i, k, 0.0);
            }
            q.set(k, k, 1.0);
            for (int j = k; j < QR.cols(); j++) {
                if (QR.get(k, k) != 0) {
                    double s = 0.0;
                    for (int i = k; i < QR.rows(); i++) {
                        s += QR.get(i, k) * q.get(i, j);
                    }
                    s = -s / QR.get(k, k);
                    for (int i = k; i < QR.rows(); i++) {
                        q.set(i, j, q.get(i, j) + s * QR.get(i, k));
                    }
                }
            }
        }
        return q;
    }

    /**
     * Least squares solution of A*X = B
     *
     * @param B A Matrix with as many rows as A and any number of columns.
     * @return X that minimizes the two norm of Q*R*X-B.
     * @throws IllegalArgumentException Matrix row dimensions must agree.
     * @throws RuntimeException         Matrix is rank deficient.
     */

    public DMatrix solve(DMatrix B) {
        if (B.rows() != QR.rows()) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!isFullRank()) {
            throw new RuntimeException("Matrix is rank deficient.");
        }

        // Copy right hand side
        DMatrix X = B.copy();

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < QR.cols(); k++) {
            for (int j = 0; j < B.cols(); j++) {
                double s = 0.0;
                for (int i = k; i < QR.rows(); i++) {
                    s += QR.get(i, k) * X.get(i, j);
                }
                s = -s / QR.get(k, k);
                for (int i = k; i < QR.rows(); i++) {
                    X.set(i, j, X.get(i, j) + s * QR.get(i, k));
                }
            }
        }

        // Solve R*X = Y;
        for (int k = QR.cols() - 1; k >= 0; k--) {
            for (int j = 0; j < B.cols(); j++) {
                X.set(k, j, X.get(k, j) / diag.get(k));
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < B.cols(); j++) {
                    X.set(i, j, X.get(i, j) - X.get(k, j) * QR.get(i, k));
                }
            }
        }
        return X.rangeRows(0, QR.cols()).rangeCols(0, B.cols()).copy();
    }

    /**
     * Least squares solution of A*x = b
     *
     * @param b A Matrix with as many rows as A and any number of columns.
     * @return X that minimizes the two norm of Q*R*X-B.
     * @throws IllegalArgumentException Matrix row dimensions must agree.
     * @throws RuntimeException         Matrix is rank deficient.
     */
    public DVector solve(DVector b) {
        return solve(b.asMatrix()).mapCol(0);
    }

    public DMatrix inv() {
        return solve(DMatrix.eye(ref.rows()));
    }
}
