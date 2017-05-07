/*
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

import static java.lang.StrictMath.hypot;

/**
 * Singular Value Decomposition.
 * <p>
 * For an m-by-n rapaio.data.matrix A with m >= n, the singular value decomposition is an
 * m-by-n orthogonal rapaio.data.matrix U, an n-by-n diagonal rapaio.data.matrix S, and an n-by-n
 * orthogonal rapaio.data.matrix RV so that A = U*S*RV'.
 * <p>
 * The singular values, sigma[k] = S[k][k], are ordered so that sigma[0] >=
 * sigma[1] >= ... >= sigma[n-1].
 * <p>
 * The singular value decompostion always exists, so the constructor will never
 * fail. The matrix condition number and the effective numerical rank can be
 * computed from this decomposition.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class SVDecomposition implements java.io.Serializable {

    private static final long serialVersionUID = -502574786523851631L;
    private double[][] U, V;
    private double[] s;
    private int rowCount, colCount;

    public SVDecomposition(RM Arg) {

        // Derived from LINPACK code.
        // Initialize.
        RM A = Arg.solidCopy();
        rowCount = Arg.rowCount();
        colCount = Arg.colCount();

        /*
         * Apparently the failing cases are only a proper subset of (m<n), so
         * let's not throw error. Correct fix to come later? if (m<n) { throw
         * new IllegalArgumentException("Jama SVD only works for m >= n"); }
         */
        int minCount = Math.min(rowCount, colCount);
        s = new double[Math.min(rowCount + 1, colCount)];
        U = new double[rowCount][minCount];
        V = new double[colCount][colCount];
        double[] e = new double[colCount];
        double[] work = new double[rowCount];
        boolean wantu = true;
        boolean wantv = true;

        // Reduce A to bidiagonal form, storing the diagonal elements
        // in s and the super-diagonal elements in e.
        int nct = Math.min(rowCount - 1, colCount);
        int nrt = Math.max(0, Math.min(colCount - 2, rowCount));
        for (int k = 0; k < Math.max(nct, nrt); k++) {
            if (k < nct) {

                // Compute the transformation for the k-th column and
                // place the k-th diagonal in s[k].
                // Compute 2-norm of k-th column without under/overflow.
                s[k] = 0;
                for (int i = k; i < rowCount; i++) {
                    s[k] = hypot(s[k], A.get(i, k));
                }
                if (s[k] != 0.0) {
                    if (A.get(k, k) < 0.0) {
                        s[k] = -s[k];
                    }
                    for (int i = k; i < rowCount; i++) {
                        A.set(i, k, A.get(i, k) / s[k]);
                    }
                    A.increment(k, k, 1.0);
                }
                s[k] = -s[k];
            }
            for (int j = k + 1; j < colCount; j++) {
                if ((k < nct) & (s[k] != 0.0)) {

                    // Apply the transformation.
                    double t = 0;
                    for (int i = k; i < rowCount; i++) {
                        t += A.get(i, k) * A.get(i, j);
                    }
                    t = -t / A.get(k, k);
                    for (int i = k; i < rowCount; i++) {
                        A.increment(i, j, t * A.get(i, k));
                    }
                }

                // Place the k-th row of A into e for the
                // subsequent calculation of the row transformation.
                e[j] = A.get(k, j);
            }
            if (wantu & (k < nct)) {

                // Place the transformation in U for subsequent back
                // multiplication.
                for (int i = k; i < rowCount; i++) {
                    U[i][k] = A.get(i, k);
                }
            }
            if (k < nrt) {

                // Compute the k-th row transformation and place the
                // k-th super-diagonal in e[k].
                // Compute 2-norm without under/overflow.
                e[k] = 0;
                for (int i = k + 1; i < colCount; i++) {
                    e[k] = hypot(e[k], e[i]);
                }
                if (e[k] != 0.0) {
                    if (e[k + 1] < 0.0) {
                        e[k] = -e[k];
                    }
                    for (int i = k + 1; i < colCount; i++) {
                        e[i] /= e[k];
                    }
                    e[k + 1] += 1.0;
                }
                e[k] = -e[k];
                if ((k + 1 < rowCount) & (e[k] != 0.0)) {

                    // Apply the transformation.
                    for (int i = k + 1; i < rowCount; i++) {
                        work[i] = 0.0;
                    }
                    for (int j = k + 1; j < colCount; j++) {
                        for (int i = k + 1; i < rowCount; i++) {
                            work[i] += e[j] * A.get(i, j);
                        }
                    }
                    for (int j = k + 1; j < colCount; j++) {
                        double t = -e[j] / e[k + 1];
                        for (int i = k + 1; i < rowCount; i++) {
                            A.increment(i, j, t * work[i]);
                        }
                    }
                }
                if (wantv) {

                    // Place the transformation in RV for subsequent
                    // back multiplication.
                    for (int i = k + 1; i < colCount; i++) {
                        V[i][k] = e[i];
                    }
                }
            }
        }

        // Set up the final bidiagonal rapaio.data.matrix or order p.
        int p = Math.min(colCount, rowCount + 1);
        if (nct < colCount) {
            s[nct] = A.get(nct, nct);
        }
        if (rowCount < p) {
            s[p - 1] = 0.0;
        }
        if (nrt + 1 < p) {
            e[nrt] = A.get(nrt, p - 1);
        }
        e[p - 1] = 0.0;

        // If required, generate U.
        if (wantu) {
            for (int j = nct; j < minCount; j++) {
                for (int i = 0; i < rowCount; i++) {
                    U[i][j] = 0.0;
                }
                U[j][j] = 1.0;
            }
            for (int k = nct - 1; k >= 0; k--) {
                if (s[k] != 0.0) {
                    for (int j = k + 1; j < minCount; j++) {
                        double t = 0;
                        for (int i = k; i < rowCount; i++) {
                            t += U[i][k] * U[i][j];
                        }
                        t = -t / U[k][k];
                        for (int i = k; i < rowCount; i++) {
                            U[i][j] += t * U[i][k];
                        }
                    }
                    for (int i = k; i < rowCount; i++) {
                        U[i][k] = -U[i][k];
                    }
                    U[k][k] = 1.0 + U[k][k];
                    for (int i = 0; i < k - 1; i++) {
                        U[i][k] = 0.0;
                    }
                } else {
                    for (int i = 0; i < rowCount; i++) {
                        U[i][k] = 0.0;
                    }
                    U[k][k] = 1.0;
                }
            }
        }

        // If required, generate RV.
        if (wantv) {
            for (int k = colCount - 1; k >= 0; k--) {
                if ((k < nrt) & (e[k] != 0.0)) {
                    for (int j = k + 1; j < minCount; j++) {
                        double t = 0;
                        for (int i = k + 1; i < colCount; i++) {
                            t += V[i][k] * V[i][j];
                        }
                        t = -t / V[k + 1][k];
                        for (int i = k + 1; i < colCount; i++) {
                            V[i][j] += t * V[i][k];
                        }
                    }
                }
                for (int i = 0; i < colCount; i++) {
                    V[i][k] = 0.0;
                }
                V[k][k] = 1.0;
            }
        }

        // Main iteration loop for the singular values.
        int pp = p - 1;
        int iter = 0;
        double eps = Math.pow(2.0, -52.0);
        double tiny = Math.pow(2.0, -966.0);
        while (p > 0) {
            int k, kase;

            // Here is where a test for too many iterations would go.
            // This section of the program inspects for
            // negligible elements in the s and e arrays.  On
            // completion the variables kase and k are set as follows.
            // kase = 1     if s(p) and e[k-1] are negligible and k<p
            // kase = 2     if s(k) is negligible and k<p
            // kase = 3     if e[k-1] is negligible, k<p, and
            //              s(k), ..., s(p) are not negligible (qr step).
            // kase = 4     if e(p-1) is negligible (convergence).
            for (k = p - 2; k >= -1; k--) {
                if (k == -1) {
                    break;
                }
                if (Math.abs(e[k])
                        <= tiny + eps * (Math.abs(s[k]) + Math.abs(s[k + 1]))) {
                    e[k] = 0.0;
                    break;
                }
            }
            if (k == p - 2) {
                kase = 4;
            } else {
                int ks;
                for (ks = p - 1; ks >= k; ks--) {
                    if (ks == k) {
                        break;
                    }
                    double t = (ks != p ? Math.abs(e[ks]) : 0.)
                            + (ks != k + 1 ? Math.abs(e[ks - 1]) : 0.);
                    if (Math.abs(s[ks]) <= tiny + eps * t) {
                        s[ks] = 0.0;
                        break;
                    }
                }
                if (ks == k) {
                    kase = 3;
                } else if (ks == p - 1) {
                    kase = 1;
                } else {
                    kase = 2;
                    k = ks;
                }
            }
            k++;

            // Perform the task indicated by kase.
            switch (kase) {

                // Deflate negligible s(p).
                case 1: {
                    double f = e[p - 2];
                    e[p - 2] = 0.0;
                    for (int j = p - 2; j >= k; j--) {
                        double t = hypot(s[j], f);
                        double cs = s[j] / t;
                        double sn = f / t;
                        s[j] = t;
                        if (j != k) {
                            f = -sn * e[j - 1];
                            e[j - 1] = cs * e[j - 1];
                        }
                        if (wantv) {
                            for (int i = 0; i < colCount; i++) {
                                t = cs * V[i][j] + sn * V[i][p - 1];
                                V[i][p - 1] = -sn * V[i][j] + cs * V[i][p - 1];
                                V[i][j] = t;
                            }
                        }
                    }
                }
                break;

                // Split at negligible s(k).
                case 2: {
                    double f = e[k - 1];
                    e[k - 1] = 0.0;
                    for (int j = k; j < p; j++) {
                        double t = hypot(s[j], f);
                        double cs = s[j] / t;
                        double sn = f / t;
                        s[j] = t;
                        f = -sn * e[j];
                        e[j] = cs * e[j];
                        if (wantu) {
                            for (int i = 0; i < rowCount; i++) {
                                t = cs * U[i][j] + sn * U[i][k - 1];
                                U[i][k - 1] = -sn * U[i][j] + cs * U[i][k - 1];
                                U[i][j] = t;
                            }
                        }
                    }
                }
                break;

                // Perform one qr step.
                case 3: {

                    // Calculate the shift.
                    double scale = Math.max(Math.max(Math.max(Math.max(
                                    Math.abs(s[p - 1]), Math.abs(s[p - 2])), Math.abs(e[p - 2])),
                            Math.abs(s[k])
                    ), Math.abs(e[k]));
                    double sp = s[p - 1] / scale;
                    double spm1 = s[p - 2] / scale;
                    double epm1 = e[p - 2] / scale;
                    double sk = s[k] / scale;
                    double ek = e[k] / scale;
                    double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
                    double c = (sp * epm1) * (sp * epm1);
                    double shift = 0.0;
                    if ((b != 0.0) | (c != 0.0)) {
                        shift = Math.sqrt(b * b + c);
                        if (b < 0.0) {
                            shift = -shift;
                        }
                        shift = c / (b + shift);
                    }
                    double f = (sk + sp) * (sk - sp) + shift;
                    double g = sk * ek;

                    // Chase zeros.
                    for (int j = k; j < p - 1; j++) {
                        double t = hypot(f, g);
                        double cs = f / t;
                        double sn = g / t;
                        if (j != k) {
                            e[j - 1] = t;
                        }
                        f = cs * s[j] + sn * e[j];
                        e[j] = cs * e[j] - sn * s[j];
                        g = sn * s[j + 1];
                        s[j + 1] = cs * s[j + 1];
                        if (wantv) {
                            for (int i = 0; i < colCount; i++) {
                                t = cs * V[i][j] + sn * V[i][j + 1];
                                V[i][j + 1] = -sn * V[i][j] + cs * V[i][j + 1];
                                V[i][j] = t;
                            }
                        }
                        t = hypot(f, g);
                        cs = f / t;
                        sn = g / t;
                        s[j] = t;
                        f = cs * e[j] + sn * s[j + 1];
                        s[j + 1] = -sn * e[j] + cs * s[j + 1];
                        g = sn * e[j + 1];
                        e[j + 1] = cs * e[j + 1];
                        if (wantu && (j < rowCount - 1)) {
                            for (int i = 0; i < rowCount; i++) {
                                t = cs * U[i][j] + sn * U[i][j + 1];
                                U[i][j + 1] = -sn * U[i][j] + cs * U[i][j + 1];
                                U[i][j] = t;
                            }
                        }
                    }
                    e[p - 2] = f;
                    iter = iter + 1;
                }
                break;

                // Convergence.
                case 4: {

                    // Make the singular values positive.
                    if (s[k] <= 0.0) {
                        s[k] = (s[k] < 0.0 ? -s[k] : 0.0);
                        if (wantv) {
                            for (int i = 0; i <= pp; i++) {
                                V[i][k] = -V[i][k];
                            }
                        }
                    }

                    // Order the singular values.
                    while (k < pp) {
                        if (s[k] >= s[k + 1]) {
                            break;
                        }
                        double t = s[k];
                        s[k] = s[k + 1];
                        s[k + 1] = t;
                        if (wantv && (k < colCount - 1)) {
                            for (int i = 0; i < colCount; i++) {
                                t = V[i][k + 1];
                                V[i][k + 1] = V[i][k];
                                V[i][k] = t;
                            }
                        }
                        if (wantu && (k < rowCount - 1)) {
                            for (int i = 0; i < rowCount; i++) {
                                t = U[i][k + 1];
                                U[i][k + 1] = U[i][k];
                                U[i][k] = t;
                            }
                        }
                        k++;
                    }
                    iter = 0;
                    p--;
                }
                break;
            }
        }
    }

    public RM getU() {
        return SolidRM.copy(U, 0, rowCount, 0, Math.min(rowCount + 1, colCount));
    }

    /**
     * Return the right singular vectors
     *
     * @return RV
     */
    public RM getV() {
        return SolidRM.copy(V);
    }

    /**
     * Return the one-dimensional array of singular values
     *
     * @return diagonal of S.
     */
    public double[] getSingularValues() {
        return s;
    }

    /**
     * Return the diagonal matrix of singular values
     *
     * @return S
     */
    public RM getS() {
        RM S = SolidRM.empty(colCount, colCount);
        for (int i = 0; i < colCount; i++) {
            S.set(i, i, this.s[i]);
        }
        return S;
    }

    /**
     * Two norm
     *
     * @return max(S)
     */
    public double norm2() {
        return s[0];
    }

    /**
     * Two norm condition number
     *
     * @return max(S)/min(S)
     */
    public double cond() {
        return s[0] / s[Math.min(rowCount, colCount) - 1];
    }

    /**
     * Effective numerical matrix rank
     *
     * @return Number of non-negligible singular values.
     */
    public int rank() {
        double eps = Math.pow(2.0, -52.0);
        double tol = Math.max(rowCount, colCount) * s[0] * eps;
        int r = 0;
        for (double value : s) {
            if (value > tol) {
                r++;
            }
        }
        return r;
    }
}