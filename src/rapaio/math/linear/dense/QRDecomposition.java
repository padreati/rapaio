/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import rapaio.math.linear.RM;
import rapaio.math.linear.RV;

import java.io.Serializable;

/**
 * QR Decomposition.
 * <p>
 * For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
 * orthogonal matrix Q and an n-by-n upper triangular matrix R so that A = Q*R.
 * <p>
 * The QR decomposition always exists, even if the matrix does not have
 * full rank.  The primary use of the QR decomposition is in the least squares solution
 * of non square systems of simultaneous linear equations. This will fail if A is not of full rank.
 */
public class QRDecomposition implements Serializable {

    public static QRDecomposition from(RM A) {
        return new QRDecomposition(A);
    }

    private static final long serialVersionUID = -8322866575684242727L;

    private RM QR;
    private RV Rdiag;

    private QRDecomposition(RM A) {
        // Initialize.
        QR = A.solidCopy();
        Rdiag = SolidRV.empty(QR.getColCount());

        // Main loop.
        for (int k = 0; k < QR.getColCount(); k++) {
            // Compute 2-norm of k-th column without under/overflow.
            double nrm = 0;
            for (int i = k; i < QR.getRowCount(); i++) {
                nrm = StrictMath.hypot(nrm, QR.get(i, k));
            }

            if (nrm != 0.0) {
                // Form k-th Householder var.
                if (QR.get(k, k) < 0) {
                    nrm = -nrm;
                }
                for (int i = k; i < QR.getRowCount(); i++) {
                    QR.set(i, k, QR.get(i, k) / nrm);
                }
                QR.set(k, k, QR.get(k, k) + 1.0);

                // Apply transformation to remaining columns.
                for (int j = k + 1; j < QR.getColCount(); j++) {
                    double s = 0.0;
                    for (int i = k; i < QR.getRowCount(); i++) {
                        s += QR.get(i, k) * QR.get(i, j);
                    }
                    s = -s / QR.get(k, k);
                    for (int i = k; i < QR.getRowCount(); i++) {
                        QR.set(i, j, QR.get(i, j) + s * QR.get(i, k));
                    }
                }
            }
            Rdiag.increment(k, -nrm);
        }
    }

    /**
     * Is the matrix full rank?
     *
     * @return true if R, and hence A, has full rank.
     */
    public boolean isFullRank() {
        for (int j = 0; j < QR.getColCount(); j++) {
            if (Rdiag.get(j) == 0)
                return false;
        }
        return true;
    }

    /**
     * Return the Householder vectors
     *
     * @return Lower trapezoidal matrix whose columns define the reflections
     */
    public RM getH() {
        RM H = SolidRM.empty(QR.getRowCount(), QR.getColCount());
        for (int i = 0; i < QR.getRowCount(); i++) {
            for (int j = 0; j < QR.getColCount(); j++) {
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
    public RM getR() {
        RM R = SolidRM.empty(QR.getColCount(), QR.getColCount());
        for (int i = 0; i < QR.getColCount(); i++) {
            for (int j = 0; j < QR.getColCount(); j++) {
                if (i < j) {
                    R.set(i, j, QR.get(i, j));
                } else if (i == j) {
                    R.set(i, j, Rdiag.get(i));
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

    public RM getQ() {
        RM Q = SolidRM.empty(QR.getRowCount(), QR.getColCount());
        for (int k = QR.getColCount() - 1; k >= 0; k--) {
            for (int i = 0; i < QR.getRowCount(); i++) {
                Q.set(i, k, 0.0);
            }
            Q.set(k, k, 1.0);
            for (int j = k; j < QR.getColCount(); j++) {
                if (QR.get(k, k) != 0) {
                    double s = 0.0;
                    for (int i = k; i < QR.getRowCount(); i++) {
                        s += QR.get(i, k) * Q.get(i, j);
                    }
                    s = -s / QR.get(k, k);
                    for (int i = k; i < QR.getRowCount(); i++) {
                        Q.set(i, j, Q.get(i, j) + s * QR.get(i, k));
                    }
                }
            }
        }
        return Q;
    }

    /**
     * Least squares solution of A*X = B
     *
     * @param B A Matrix with as many rows as A and any number of columns.
     * @return X that minimizes the two norm of Q*R*X-B.
     * @throws IllegalArgumentException Matrix row dimensions must agree.
     * @throws RuntimeException         Matrix is rank deficient.
     */

    public RM solve(RM B) {
        if (B.getRowCount() != QR.getRowCount()) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!isFullRank()) {
            throw new RuntimeException("Matrix is rank deficient.");
        }

        // Copy right hand side
        RM X = B.solidCopy();

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < QR.getColCount(); k++) {
            for (int j = 0; j < B.getColCount(); j++) {
                double s = 0.0;
                for (int i = k; i < QR.getRowCount(); i++) {
                    s += QR.get(i, k) * X.get(i, j);
                }
                s = -s / QR.get(k, k);
                for (int i = k; i < QR.getRowCount(); i++) {
                    X.set(i, j, X.get(i, j) + s * QR.get(i, k));
                }
            }
        }

        // Solve R*X = Y;
        for (int k = QR.getColCount() - 1; k >= 0; k--) {
            for (int j = 0; j < B.getColCount(); j++) {
                X.set(k, j, X.get(k, j) / Rdiag.get(k));
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < B.getColCount(); j++) {
                    X.set(i, j, X.get(i, j) - X.get(k, j) * QR.get(i, k));
                }
            }
        }
        return X.rangeRows(0, QR.getColCount()).rangeCols(0, B.getColCount());
    }
}
