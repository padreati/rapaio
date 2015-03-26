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

/**
 * QR Decomposition.
 * <p>
 * For an m-by-n rapaio.data.matrix A with m >= n, the QR decomposition is an m-by-n
 * orthogonal rapaio.data.matrix Q and an n-by-n upper triangular rapaio.data.matrix R so that
 * A = Q*R.
 * <p>
 * The QR decompostion always exists, even if the rapaio.data.matrix does not have
 * full rank, so the constructor will never fail.  The primary use of the
 * QR decomposition is in the least squares solution of nonsquare systems
 * of simultaneous linear equations.  This will fail if isFullRank()
 * returns false.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class QRDecomposition implements java.io.Serializable {

    private M QR;
    private int m, n;
    private double[] Rdiag;

    public QRDecomposition(M A) {
        // Initialize.
        QR = A.solidCopy();
        m = A.rowCount();
        n = A.colCount();
        Rdiag = new double[n];

        // Main loop.
        for (int k = 0; k < n; k++) {
            // Compute 2-norm of k-th column without under/overflow.
            double nrm = 0;
            for (int i = k; i < m; i++) {
                nrm = StrictMath.hypot(nrm, QR.get(i, k));
            }

            if (nrm != 0.0) {
                // Form k-th Householder var.
                if (QR.get(k, k) < 0) {
                    nrm = -nrm;
                }
                for (int i = k; i < m; i++) {
                    QR.set(i, k, QR.get(i, k) / nrm);
                }
                QR.set(k, k, QR.get(k, k) + 1.0);

                // Apply transformation to remaining columns.
                for (int j = k + 1; j < n; j++) {
                    double s = 0.0;
                    for (int i = k; i < m; i++) {
                        s += QR.get(i, k) * QR.get(i, j);
                    }
                    s = -s / QR.get(k, k);
                    for (int i = k; i < m; i++) {
                        QR.set(i, j, QR.get(i, j) + s * QR.get(i, k));
                    }
                }
            }
            Rdiag[k] = -nrm;
        }
    }

    /**
     * Is the matrix full rank?
     *
     * @return true if R, and hence A, has full rank.
     */
    public boolean isFullRank() {
        for (int j = 0; j < n; j++) {
            if (Rdiag[j] == 0)
                return false;
        }
        return true;
    }

    /**
     * Return the Householder vectors
     *
     * @return Lower trapezoidal matrix whose columns define the reflections
     */
    public M getH() {
        M H = LA.newMEmpty(m, n);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (i >= j) {
                    H.set(i, j, QR.get(i, j));
                } else {
                    H.set(i, j, 0.0);
                }
            }
        }
        return H;
    }

    /**
     * Return the upper triangular factor
     *
     * @return R
     */

    public M getR() {
        M R = LA.newMEmpty(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i < j) {
                    R.set(i, j, QR.get(i, j));
                } else if (i == j) {
                    R.set(i, j, Rdiag[i]);
                } else {
                    R.set(i, j, 0.0);
                }
            }
        }
        return R;
    }

    /**
     * Generate and return the (economy-sized) orthogonal factor
     *
     * @return Q
     */

    public M getQ() {
        M Q = LA.newMEmpty(m, n);
        for (int k = n - 1; k >= 0; k--) {
            for (int i = 0; i < m; i++) {
                Q.set(i, k, 0.0);
            }
            Q.set(k, k, 1.0);
            for (int j = k; j < n; j++) {
                if (QR.get(k, k) != 0) {
                    double s = 0.0;
                    for (int i = k; i < m; i++) {
                        s += QR.get(i, k) * Q.get(i, j);
                    }
                    s = -s / QR.get(k, k);
                    for (int i = k; i < m; i++) {
                        Q.set(i, j, Q.get(i, j) + s * QR.get(i, k));
                    }
                }
            }
        }
        return Q;
    }

    public M getQR() {
        return QR.solidCopy();
    }

    /**
     * Least squares solution of A*X = B
     *
     * @param B A Matrix with as many getRowCount as A and any number of columns.
     * @return X that minimizes the two norm of Q*R*X-B.
     * @throws IllegalArgumentException Matrix row dimensions must agree.
     * @throws RuntimeException         Matrix is rank deficient.
     */

    public M solve(M B) {
        if (B.rowCount() != m) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!this.isFullRank()) {
            throw new RuntimeException("Matrix is rank deficient.");
        }

        // Copy right hand side
        int nx = B.colCount();
        M X = B.solidCopy();

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < n; k++) {
            for (int j = 0; j < nx; j++) {
                double s = 0.0;
                for (int i = k; i < m; i++) {
                    s += QR.get(i, k) * X.get(i, j);
                }
                s = -s / QR.get(k, k);
                for (int i = k; i < m; i++) {
                    X.set(i, j, X.get(i, j) + s * QR.get(i, k));
                }
            }
        }
        // Solve R*X = Y;
        for (int k = n - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                X.set(k, j, X.get(k, j) / Rdiag[k]);
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    X.set(i, j, X.get(i, j) - X.get(k, j) * QR.get(i, k));
                }
            }
        }
        return X.rangeRows(0, n).rangeCols(0, nx);
    }
}
