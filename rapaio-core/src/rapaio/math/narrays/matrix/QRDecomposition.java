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

package rapaio.math.narrays.matrix;

import java.io.Serial;
import java.io.Serializable;

import rapaio.math.narrays.NArray;
import rapaio.math.narrays.Shape;
import rapaio.math.narrays.NArrayManager;

public class QRDecomposition<N extends Number> implements Serializable {

    @Serial
    private static final long serialVersionUID = -8322866575684242727L;

    protected final NArray<N> ref;
    protected final NArray<N> QR;
    protected final NArray<N> diag;

    protected final NArrayManager.OfType<N> tmt;

    public QRDecomposition(NArray<N> ref) {
        // Initialize.
        this.ref = ref;
        this.tmt = ref.manager().ofType(ref.dtype());

        QR = ref.copy();
        diag = tmt.zeros(Shape.of(QR.dim(1)));

        // Main loop.
        for (int k = 0; k < QR.dim(1); k++) {
            // Compute 2-norm of k-th column without under/overflow.
            double nrm = 0;
            for (int i = k; i < QR.dim(0); i++) {
                nrm = StrictMath.hypot(nrm, QR.getDouble(i, k));
            }

            if (nrm != 0.0) {
                // Form k-th Householder var.
                if (QR.getDouble(k, k) < 0) {
                    nrm = -nrm;
                }
                for (int i = k; i < QR.dim(0); i++) {
                    QR.setDouble(QR.getDouble(i, k) / nrm, i, k);
                }
                QR.setDouble(QR.getDouble(k, k) + 1, k, k);

                // Apply transformation to remaining columns.
                for (int j = k + 1; j < QR.dim(1); j++) {
                    double s = 0.0;
                    for (int i = k; i < QR.dim(0); i++) {
                        s += QR.getDouble(i, k) * QR.getDouble(i, j);
                    }
                    s = -s / QR.getDouble(k, k);
                    for (int i = k; i < QR.dim(0); i++) {
                        QR.incDouble(s * QR.getDouble(i, k), i, j);
                    }
                }
            }
            diag.setDouble(diag.getDouble(k) - nrm, k);
        }
    }

    /**
     * Is the matrix full rank?
     *
     * @return true if R, and hence A, has full rank.
     */
    public boolean isFullRank() {
        for (int j = 0; j < QR.dim(1); j++) {
            if (diag.getDouble(j) == 0) {
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
    public NArray<N> h() {
        var h = tmt.zeros(Shape.of(QR.dim(0), QR.dim(1)));
        for (int i = 0; i < QR.dim(0); i++) {
            for (int j = 0; j < QR.dim(1); j++) {
                if (i >= j) {
                    h.setDouble(QR.getDouble(i, j), i, j);
                } else {
                    h.setDouble(0.0, i, j);
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
    public NArray<N> r() {
        var r = tmt.zeros(Shape.of(QR.dim(1), QR.dim(1)));
        for (int i = 0; i < QR.dim(1); i++) {
            for (int j = 0; j < QR.dim(1); j++) {
                if (i < j) {
                    r.setDouble(QR.getDouble(i, j), i, j);
                } else if (i == j) {
                    r.setDouble(diag.getDouble(i), i, j);
                } else {
                    r.setDouble(0.0, i, j);
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

    public NArray<N> q() {
        var q = tmt.zeros(Shape.of(QR.dim(0), QR.dim(1)));
        for (int k = QR.dim(1) - 1; k >= 0; k--) {
            for (int i = 0; i < QR.dim(0); i++) {
                q.setDouble(0.0, i, k);
            }
            q.setDouble(1.0, k, k);
            for (int j = k; j < QR.dim(1); j++) {
                if (QR.getDouble(k, k) != 0) {
                    double s = 0.0;
                    for (int i = k; i < QR.dim(0); i++) {
                        s += QR.getDouble(i, k) * q.getDouble(i, j);
                    }
                    s = -s / QR.getDouble(k, k);
                    for (int i = k; i < QR.dim(0); i++) {
                        q.incDouble(s * QR.getDouble(i, k), i, j);
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
    public NArray<N> solve(NArray<N> B) {
        boolean isVector = B.isVector();

        if (isVector) {
            B = B.stretch(1);
        }

        if (B.dim(0) != QR.dim(0)) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!isFullRank()) {
            throw new RuntimeException("Matrix is rank deficient.");
        }

        // Copy right hand side
        NArray<N> X = B.copy();

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < QR.dim(1); k++) {
            for (int j = 0; j < B.dim(1); j++) {
                double s = 0.0;
                for (int i = k; i < QR.dim(0); i++) {
                    s += QR.getDouble(i, k) * X.getDouble(i, j);
                }
                s = -s / QR.getDouble(k, k);
                for (int i = k; i < QR.dim(0); i++) {
                    X.incDouble(s * QR.getDouble(i, k), i, j);
                }
            }
        }

        // Solve R*X = Y;
        for (int k = QR.dim(1) - 1; k >= 0; k--) {
            for (int j = 0; j < B.dim(1); j++) {
                X.setDouble(X.getDouble(k, j) / diag.getDouble(k), k, j);
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < B.dim(1); j++) {
                    X.incDouble(-X.getDouble(k, j) * QR.getDouble(i, k), i, j);
                }
            }
        }
        var sol = X.narrow(0, true, 0, QR.dim(1)).narrow(1, true, 0, B.dim(1)).copy();
        return isVector ? sol.squeeze(1) : sol;
    }

    public NArray<N> inv() {
        return solve(tmt.eye(ref.dim(0)));
    }
}
