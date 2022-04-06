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

import static java.lang.Math.abs;
import static java.lang.Math.hypot;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

import static rapaio.math.MathTools.*;

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.math.linear.dense.DVectorDense;

/**
 * Calculates the compact Singular Value Decomposition of a matrix.
 * <p>
 * The Singular Value Decomposition of matrix A is a set of three matrices: U,
 * &Sigma; and V such that A = U &times; &Sigma; &times; V<sup>T</sup>.
 * <p>
 * Let A be a m &times; n matrix, then U is a m &times; p orthogonal matrix,
 * &Sigma; is a p &times; p diagonal matrix with positive or null elements,
 * V is a p &times; n orthogonal matrix (hence V<sup>T</sup> is also orthogonal) where
 * {@code p=min(m,n)}.
 * </p>
 * The singular values, sigma[k] = S[k][k], are ordered so that sigma[0] >=
 * sigma[1] >= ... >= sigma[n-1].
 * <p>
 * The singular value decomposition always exists, so the constructor will never
 * fail. The matrix condition number and the effective numerical rank can be
 * computed from this decomposition.
 *
 * @see <a href="http://mathworld.wolfram.com/SingularValueDecomposition.html">MathWorld</a>
 * @see <a href="http://en.wikipedia.org/wiki/Singular_value_decomposition">Wikipedia</a>
 */
public class DoubleSVDecomposition implements java.io.Serializable {

    /**
     * Absolute threshold for small singular values.
     */
    private static final double tiny = 0x1.0p-966;

    /**
     * Number of rows of the input matrix
     */
    private final int m;
    /**
     * Number of columns of the input matrix
     */
    private final int n;
    /**
     * Tolerance computed during constructor execution used to select
     * numerically relevant non-zero singular values.
     */
    private final double tol;
    /**
     * U matrix from the resulted decomposition
     */
    private final DMatrixDenseC u;
    /**
     * Vector of values of the diagonal S matrix from the resulted decomposition
     */
    private final DVectorDense s;
    /**
     * V matrix from the resulted decomposition
     */
    private final DMatrixDenseC v;

    public DoubleSVDecomposition(DMatrix Arg, boolean wantu, boolean wantv) {

        // Derived from LINPACK code.
        // Initialize.
        DMatrix A = Arg.copy();
        m = Arg.rows();
        n = Arg.cols();

        if (m < n) {
            throw new IllegalArgumentException("Jama SVD only works for m >= n");
        }

        s = new DVectorDense(n);
        u = new DMatrixDenseC(m, n);
        v = new DMatrixDenseC(n, n);
        double[] e = new double[n];
        double[] work = new double[m];

        // Reduce A to bidiagonal form, storing the diagonal elements
        // in s and the super-diagonal elements in e.

        int nct = Math.min(m - 1, n);
        int nrt = max(0, n - 2);
        for (int k = 0; k < max(nct, nrt); k++) {
            if (k < nct) {

                // Compute the transformation for the k-th column and
                // place the k-th diagonal in s[k].
                // Compute 2-norm of k-th column without under/overflow.
                s.set(k, 0);
                for (int i = k; i < m; i++) {
                    s.set(k, hypot(s.get(k), A.get(i, k)));
                }
                if (s.get(k) != 0) {
                    if (A.get(k, k) < 0) {
                        s.set(k, -s.get(k));
                    }
                    for (int i = k; i < m; i++) {
                        A.set(i, k, A.get(i, k) / s.get(k));
                    }
                    A.inc(k, k, 1);
                }
                s.set(k, -s.get(k));
            }
            for (int j = k + 1; j < n; j++) {
                if ((k < nct) && (s.get(k) != 0.0)) {

                    // Apply the transformation.

                    double t = 0;
                    for (int i = k; i < m; i++) {
                        t += A.get(i, k) * A.get(i, j);
                    }
                    t = -t / A.get(k, k);
                    for (int i = k; i < m; i++) {
                        A.inc(i, j, t * A.get(i, k));
                    }
                }

                // Place the k-th row of A into e for the
                // subsequent calculation of the row transformation.

                e[j] = A.get(k, j);
            }
            if (wantu && (k < nct)) {
                // Place the transformation in U for subsequent back
                // multiplication.
                for (int i = k; i < m; i++) {
                    u.set(i, k, A.get(i, k));
                }
            }
            if (k < nrt) {

                // Compute the k-th row transformation and place the
                // k-th super-diagonal in e[k].
                // Compute 2-norm without under/overflow.
                e[k] = 0;
                for (int i = k + 1; i < n; i++) {
                    e[k] = hypot(e[k], e[i]);
                }
                if (e[k] != 0.0) {
                    if (e[k + 1] < 0.0) {
                        e[k] = -e[k];
                    }
                    for (int i = k + 1; i < n; i++) {
                        e[i] /= e[k];
                    }
                    e[k + 1] += 1.0;
                }
                e[k] = -e[k];
                if ((k + 1 < m) && (e[k] != 0.0)) {
                    // Apply the transformation.
                    for (int i = k + 1; i < m; i++) {
                        work[i] = 0.0;
                    }
                    for (int j = k + 1; j < n; j++) {
                        for (int i = k + 1; i < m; i++) {
                            work[i] += e[j] * A.get(i, j);
                        }
                    }
                    for (int j = k + 1; j < n; j++) {
                        double t = -e[j] / e[k + 1];
                        for (int i = k + 1; i < m; i++) {
                            A.inc(i, j, t * work[i]);
                        }
                    }
                }
                if (wantv) {
                    // Place the transformation in V for subsequent
                    // back multiplication.
                    for (int i = k + 1; i < n; i++) {
                        v.set(i, k, e[i]);
                    }
                }
            }
        }
        // Set up the final bidiagonal matrix or order p.
        int p = n;
        if (nct < n) {
            s.set(nct, A.get(nct, nct));
        }
        if (m < p) {
            s.set(p - 1, 0.0);
        }
        if (nrt + 1 < p) {
            e[nrt] = A.get(nrt, p - 1);
        }
        e[p - 1] = 0.0;

        // If required, generate U.

        if (wantu) {
            for (int j = nct; j < n; j++) {
                for (int i = 0; i < m; i++) {
                    u.set(i, j, 0.0);
                }
                u.set(j, j, 1.0);
            }
            for (int k = nct - 1; k >= 0; k--) {
                if (s.get(k) != 0.0) {
                    for (int j = k + 1; j < n; j++) {
                        double t = 0;
                        for (int i = k; i < m; i++) {
                            t += u.get(i, k) * u.get(i, j);
                        }
                        t = -t / u.get(k, k);
                        for (int i = k; i < m; i++) {
                            u.inc(i, j, t * u.get(i, k));
                        }
                    }
                    for (int i = k; i < m; i++) {
                        u.set(i, k, -u.get(i, k));
                    }
                    u.inc(k, k, 1.0);
                    for (int i = 0; i < k - 1; i++) {
                        u.set(i, k, 0.0);
                    }
                } else {
                    for (int i = 0; i < m; i++) {
                        u.set(i, k, 0.0);
                    }
                    u.set(k, k, 1.0);
                }
            }
        }

        // If required, generate V.

        if (wantv) {
            for (int k = n - 1; k >= 0; k--) {
                if ((k < nrt) && (e[k] != 0.0)) {
                    for (int j = k + 1; j < n; j++) {
                        double t = 0;
                        for (int i = k + 1; i < n; i++) {
                            t += v.get(i, k) * v.get(i, j);
                        }
                        t = -t / v.get(k + 1, k);
                        for (int i = k + 1; i < n; i++) {
                            v.inc(i, j, t * v.get(i, k));
                        }
                    }
                }
                for (int i = 0; i < n; i++) {
                    v.set(i, k, 0.0);
                }
                v.set(k, k, 1.0);
            }
        }

        // Main iteration loop for the singular values.

        int pp = p - 1;
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

            for (k = p - 2; k >= 0; k--) {

                final double threshold = tiny + DBL_EPSILON * (abs(s.get(k)) + abs(s.get(k + 1)));
                // the following condition is written this way in order
                // to break out of the loop when NaN occurs, writing it
                // as "if (JdkMath.abs(e[k]) <= threshold)" would loop
                // indefinitely in case of NaNs because comparison on NaNs
                // always return false, regardless of what is checked
                // see issue MATH-947
                if (!(abs(e[k]) > threshold)) {
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
                    double t = (ks != p ? abs(e[ks]) : 0) + (ks != k + 1 ? abs(e[ks - 1]) : 0);
                    if (abs(s.get(ks)) <= tiny + DBL_EPSILON * t) {
                        s.set(ks, 0.0);
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
                case 1 -> {
                    double f = e[p - 2];
                    e[p - 2] = 0.0;
                    for (int j = p - 2; j >= k; j--) {
                        double t = hypot(s.get(j), f);
                        double cs = s.get(j) / t;
                        double sn = f / t;
                        s.set(j, t);
                        if (j != k) {
                            f = -sn * e[j - 1];
                            e[j - 1] = cs * e[j - 1];
                        }
                        if (wantv) {
                            for (int i = 0; i < n; i++) {
                                t = cs * v.get(i, j) + sn * v.get(i, p - 1);
                                v.set(i, p - 1, -sn * v.get(i, j) + cs * v.get(i, p - 1));
                                v.set(i, j, t);
                            }
                        }
                    }
                }
                // Split at negligible s(k).
                case 2 -> {
                    double f = e[k - 1];
                    e[k - 1] = 0;
                    for (int j = k; j < p; j++) {
                        double t = hypot(s.get(j), f);
                        double cs = s.get(j) / t;
                        double sn = f / t;
                        s.set(j, t);
                        f = -sn * e[j];
                        e[j] = cs * e[j];
                        if (wantu) {
                            for (int i = 0; i < m; i++) {
                                t = cs * u.get(i, j) + sn * u.get(i, k - 1);
                                u.set(i, k - 1, -sn * u.get(i, j) + cs * u.get(i, k - 1));
                                u.set(i, j, t);
                            }
                        }
                    }
                }
                // Perform one qr step.
                case 3 -> {
                    // Calculate the shift.
                    double maxPm1Pm2 = max(abs(s.get(p - 1)), abs(s.get(p - 2)));
                    double scale = max(max(max(maxPm1Pm2, abs(e[p - 2])), abs(s.get(k))), abs(e[k]));
                    double sp = s.get(p - 1) / scale;
                    double spm1 = s.get(p - 2) / scale;
                    double epm1 = e[p - 2] / scale;
                    double sk = s.get(k) / scale;
                    double ek = e[k] / scale;
                    double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2;
                    double c = (sp * epm1) * (sp * epm1);
                    double shift = 0;
                    if ((b != 0) | (c != 0)) {
                        shift = Math.sqrt(b * b + c);
                        if (b < 0) {
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
                        f = cs * s.get(j) + sn * e[j];
                        e[j] = cs * e[j] - sn * s.get(j);
                        g = sn * s.get(j + 1);
                        s.set(j + 1, cs * s.get(j + 1));
                        if (wantv) {
                            for (int i = 0; i < n; i++) {
                                t = cs * v.get(i, j) + sn * v.get(i, j + 1);
                                v.set(i, j + 1, -sn * v.get(i, j) + cs * v.get(i, j + 1));
                                v.set(i, j, t);
                            }
                        }
                        t = hypot(f, g);
                        cs = f / t;
                        sn = g / t;
                        s.set(j, t);
                        f = cs * e[j] + sn * s.get(j + 1);
                        s.set(j + 1, -sn * e[j] + cs * s.get(j + 1));
                        g = sn * e[j + 1];
                        e[j + 1] = cs * e[j + 1];
                        if (wantu && (j < m - 1)) {
                            for (int i = 0; i < m; i++) {
                                t = cs * u.get(i, j) + sn * u.get(i, j + 1);
                                u.set(i, j + 1, -sn * u.get(i, j) + cs * u.get(i, j + 1));
                                u.set(i, j, t);
                            }
                        }
                    }
                    e[p - 2] = f;
                }
                // Convergence.
                case 4 -> {
                    // Make the singular values positive.
                    if (s.get(k) <= 0.0) {
                        s.set(k, (s.get(k) < 0.0 ? -s.get(k) : 0.0));
                        if (wantv) {
                            for (int i = 0; i <= pp; i++) {
                                v.set(i, k, -v.get(i, k));
                            }
                        }
                    }
                    // Order the singular values.
                    while (k < pp) {
                        if (s.get(k) >= s.get(k + 1)) {
                            break;
                        }
                        double t = s.get(k);
                        s.set(k, s.get(k + 1));
                        s.set(k + 1, t);
                        if (wantv && (k < n - 1)) {
                            for (int i = 0; i < n; i++) {
                                t = v.get(i, k + 1);
                                v.set(i, k + 1, v.get(i, k));
                                v.set(i, k, t);
                            }
                        }
                        if (wantu && (k < m - 1)) {
                            for (int i = 0; i < m; i++) {
                                t = u.get(i, k + 1);
                                u.set(i, k + 1, u.get(i, k));
                                u.set(i, k, t);
                            }
                        }
                        k++;
                    }
                    p--;
                }
            }
        }

        tol = max(m * s.get(0) * DBL_EPSILON, sqrt(Double.MIN_NORMAL));
    }

    /**
     * Return the left singular vectors
     *
     * @return U
     */
    public DMatrix u() {
        return u;
    }

    /**
     * Return the right singular vectors
     *
     * @return V
     */
    public DMatrix v() {
        return v;
    }

    /**
     * Return the one-dimensional array of singular values
     *
     * @return diagonal of S.
     */
    public DVectorDense singularValues() {
        return s;
    }

    /**
     * Return the diagonal matrix of singular values
     *
     * @return S
     */
    public DMatrix s() {
        return DMatrix.diagonal(s);
    }

    /**
     * Returns the L<sub>2</sub> norm of the matrix.
     * <p>The L<sub>2</sub> norm is max(|A &times; u|<sub>2</sub> /
     * |u|<sub>2</sub>), where |.|<sub>2</sub> denotes the vectorial 2-norm
     * (i.e. the traditional euclidean norm).</p>
     *
     * @return norm
     */
    public double norm2() {
        return s.get(0);
    }

    /**
     * Two norm condition number
     *
     * @return max(S)/min(S)
     */
    public double conditionNumber() {
        return s.get(0) / s.get(n - 1);
    }

    /**
     * Computes the inverse of the condition number.
     * In cases of rank deficiency, the {@link #conditionNumber() condition
     * number} will become undefined.
     *
     * @return the inverse of the condition number.
     */
    public double inverseConditionNumber() {
        return s.get(n - 1) / s.get(0);
    }


    /**
     * Return the effective numerical matrix rank.
     * <p>The effective numerical rank is the number of non-negligible
     * singular values. The threshold used to identify non-negligible
     * terms is max(m,n) &times; ulp(s<sub>1</sub>) where ulp(s<sub>1</sub>)
     * is the least significant bit of the largest singular value.</p>
     *
     * @return effective numerical matrix rank
     */

    public int rank() {
        int r = 0;
        for (int i = 0; i < s.size(); i++) {
            if (s.get(i) > tol) {
                r++;
            }
        }
        return r;
    }
}