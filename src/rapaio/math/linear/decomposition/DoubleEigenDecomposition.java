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
 * http://www.apache.org/licenses/LICENSE-2.0
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
import static java.lang.StrictMath.max;

import java.io.Serial;
import java.io.Serializable;

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.math.linear.dense.DVectorDense;

/**
 * Eigenvalues and eigenvectors of a squared real matrix.
 * <p>
 * If A is symmetric, then A = V*D*V' where the eigenvalue matrix D is diagonal
 * and the eigenvector matrix V is orthogonal. I.e. A = V.dot(D.dot(V.t())) and I = V.dot(V.t()).
 * <p>
 * If A is not symmetric, then the eigenvalue matrix D is block diagonal with
 * the real eigenvalues in 1-by-1 blocks and any complex eigenvalues, lambda +
 * i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda]. The columns of V represent
 * the eigenvectors in the sense that A*V = V*D, i.e. A.dot(V) equals
 * V.dot(D). The matrix V may be badly conditioned, or even singular, so the
 * validity of the equation A = V*D*inverse(V) depends upon V.cond().
 */
public class DoubleEigenDecomposition implements Serializable {

    @Serial
    private static final long serialVersionUID = 5064091847331016868L;

    // Row and column dimension (square matrix).
    private final int n;

    // Arrays for internal storage of eigenvalues.
    private final DVectorDense real;
    private final DVectorDense imag;

    // Array for internal storage of eigenvectors.
    private final DMatrix vectors;

    /**
     * Check for symmetry, then construct the eigenvalue decomposition
     * Structure to access D and V.
     *
     * @param a Square matrix
     */
    public DoubleEigenDecomposition(DMatrix a) {
        if (a.rows() != a.cols()) {
            throw new IllegalArgumentException("Only square matrices can have eigen decomposition.");
        }
        n = a.cols();
        real = new DVectorDense(n);
        imag = new DVectorDense(n);

        if (a.isSymmetric()) {
            vectors = a.copy();
            tridiagonalize();
            diagonalize();
        } else {
            vectors = new DMatrixDenseC(n, n);
            //Array for internal storage of non-symmetric Hessenberg form.
            double[][] nonSymHess = new double[n][n];

            for (int col = 0; col < n; col++) {
                for (int row = 0; row < n; row++) {
                    nonSymHess[row][col] = a.get(row, col);
                }
            }
            reduceToHessenbergForm(nonSymHess);
            hessenbergToRealSchurForm(nonSymHess);
        }

        // revert order of eigen vectors and eigen values
        reverseOrder();
    }

    // Symmetric Householder reduction to tridiagonal form.
    private void tridiagonalize() {

        //  This is derived from the Algol procedures tred2 by
        //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
        //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        for (int j = 0; j < n; j++) {
            real.set(j, vectors.get(n - 1, j));
        }

        // Householder reduction to tridiagonal form.

        for (int i = n - 1; i > 0; i--) {

            // Scale to avoid under/overflow.

            double scale = 0.0;
            double h = 0.0;
            for (int k = 0; k < i; k++) {
                scale = scale + abs(real.get(k));
            }
            if (scale == 0.0) {
                imag.set(i, real.get(i - 1));
                for (int j = 0; j < i; j++) {
                    real.set(j, vectors.get(i - 1, j));
                    vectors.set(i, j, 0.0);
                    vectors.set(j, i, 0.0);
                }
            } else {

                // Generate Householder vector.

                for (int k = 0; k < i; k++) {
                    real.set(k, real.get(k) / scale);
                    h += real.get(k) * real.get(k);
                }
                double f = real.get(i - 1);
                double g = Math.sqrt(h);
                if (f > 0) {
                    g = -g;
                }
                imag.set(i, scale * g);
                h = h - f * g;
                real.set(i - 1, f - g);
                for (int j = 0; j < i; j++) {
                    imag.set(j, 0.0);
                }

                // Apply similarity transformation to remaining columns.

                for (int j = 0; j < i; j++) {
                    f = real.get(j);
                    vectors.set(j, i, f);
                    g = imag.get(j) + vectors.get(j, j) * f;
                    for (int k = j + 1; k <= i - 1; k++) {
                        g += vectors.get(k, j) * real.get(k);
                        imag.inc(k, vectors.get(k, j) * f);
                    }
                    imag.set(j, g);
                }
                f = 0.0;
                for (int j = 0; j < i; j++) {
                    imag.set(j, imag.get(j) / h);
                    f += imag.get(j) * real.get(j);
                }
                double hh = f / (h + h);
                for (int j = 0; j < i; j++) {
                    imag.set(j, imag.get(j) - hh * real.get(j));
                }
                for (int j = 0; j < i; j++) {
                    f = real.get(j);
                    g = imag.get(j);
                    for (int k = j; k <= i - 1; k++) {
                        vectors.set(k, j, vectors.get(k, j) - (f * imag.get(k) + g * real.get(k)));
                    }
                    real.set(j, vectors.get(i - 1, j));
                    vectors.set(i, j, 0.0);
                }
            }
            real.set(i, h);
        }

        // Accumulate transformations.

        for (int i = 0; i < n - 1; i++) {
            vectors.set(n - 1, i, vectors.get(i, i));
            vectors.set(i, i, 1.0);
            double h = real.get(i + 1);
            if (h != 0.0) {
                for (int k = 0; k <= i; k++) {
                    real.set(k, vectors.get(k, i + 1) / h);
                }
                for (int j = 0; j <= i; j++) {
                    double g = 0.0;
                    for (int k = 0; k <= i; k++) {
                        g += vectors.get(k, i + 1) * vectors.get(k, j);
                    }
                    for (int k = 0; k <= i; k++) {
                        vectors.set(k, j, vectors.get(k, j) - g * real.get(k));
                    }
                }
            }
            for (int k = 0; k <= i; k++) {
                vectors.set(k, i + 1, 0.0);
            }
        }
        for (int j = 0; j < n; j++) {
            real.set(j, vectors.get(n - 1, j));
            vectors.set(n - 1, j, 0.0);
        }
        vectors.set(n - 1, n - 1, 1.0);
        imag.set(0, 0);
    }

    // Symmetric tridiagonal QL algorithm.
    private void diagonalize() {

        //  This is derived from the Algol procedures tql2, by
        //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
        //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        if (n - 1 >= 0) {
            System.arraycopy(imag.array(), 1, imag.array(), 0, n - 1);
        }
        imag.set(n - 1, 0);

        double f = 0.0;
        double tst1 = 0.0;
        double eps = Math.pow(2.0, -52.0);
        for (int l = 0; l < n; l++) {

            // Find small subdiagonal element

            tst1 = max(tst1, abs(real.get(l)) + Math.abs(imag.get(l)));
            int m = l;
            while (m < n) {
                if (Math.abs(imag.get(m)) <= eps * tst1) {
                    break;
                }
                m++;
            }

            // If m == l, d[l] is an eigenvalue,
            // otherwise, iterate.

            if (m > l) {
                int iter = 0;
                do {
                    iter = iter + 1;  // (Could check iteration count here.)

                    // Compute implicit shift

                    double g = real.get(l);
                    double p = (real.get(l + 1) - g) / (2.0 * imag.get(l));
                    double r = Math.hypot(p, 1.0);
                    if (p < 0) {
                        r = -r;
                    }
                    real.set(l, imag.get(l) / (p + r));
                    real.set(l + 1, imag.get(l) * (p + r));
                    double dl1 = real.get(l + 1);
                    double h = g - real.get(l);
                    for (int i = l + 2; i < n; i++) {
                        real.inc(i, -h);
                    }
                    f = f + h;

                    // Implicit QL transformation.

                    p = real.get(m);
                    double c = 1.0;
                    double c2 = c;
                    double c3 = c;
                    double el1 = imag.get(l + 1);
                    double s = 0.0;
                    double s2 = 0.0;
                    for (int i = m - 1; i >= l; i--) {
                        c3 = c2;
                        c2 = c;
                        s2 = s;
                        g = c * imag.get(i);
                        h = c * p;
                        r = Math.hypot(p, imag.get(i));
                        imag.set(i + 1, s * r);
                        s = imag.get(i) / r;
                        c = p / r;
                        p = c * real.get(i) - s * g;
                        real.set(i + 1, h + s * (c * g + s * real.get(i)));

                        // Accumulate transformation.

                        for (int k = 0; k < n; k++) {
                            h = vectors.get(k, i + 1);
                            vectors.set(k, i + 1, s * vectors.get(k, i) + c * h);
                            vectors.set(k, i, c * vectors.get(k, i) - s * h);
                        }
                    }
                    p = -s * s2 * c3 * el1 * imag.get(l) / dl1;
                    imag.set(l, s * p);
                    real.set(l, c * p);

                    // Check for convergence.

                } while (Math.abs(imag.get(l)) > eps * tst1);
            }
            real.set(l, real.get(l) + f);
            imag.set(l, 0);
        }

        // Sort eigenvalues and corresponding vectors.

        for (int i = 0; i < n - 1; i++) {
            int k = i;
            double p = real.get(i);
            for (int j = i + 1; j < n; j++) {
                if (real.get(j) < p) {
                    k = j;
                    p = real.get(j);
                }
            }
            if (k != i) {
                real.set(k, real.get(i));
                real.set(i, p);
                for (int j = 0; j < n; j++) {
                    p = vectors.get(j, i);
                    vectors.set(j, i, vectors.get(j, k));
                    vectors.set(j, k, p);
                }
            }
        }
    }

    // Nonsymmetric reduction to Hessenberg form.
    private void reduceToHessenbergForm(double[][] nonSymHess) {

        //  This is derived from the Algol procedures orthes and ortran,
        //  by Martin and Wilkinson, Handbook for Auto. Comp.,
        //  Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutines in EISPACK.

        // Working storage for nonsymmetric algorithm.
        double[] ort = new double[n];

        int low = 0;
        int high = n - 1;

        for (int m = low + 1; m <= high - 1; m++) {

            // Scale column.

            double scale = 0.0;
            for (int i = m; i <= high; i++) {
                scale = scale + Math.abs(nonSymHess[i][m - 1]);
            }
            if (scale != 0.0) {

                // Compute Householder transformation.

                double h = 0.0;
                for (int i = high; i >= m; i--) {
                    ort[i] = nonSymHess[i][m - 1] / scale;
                    h += ort[i] * ort[i];
                }
                double g = Math.sqrt(h);
                if (ort[m] > 0) {
                    g = -g;
                }
                h = h - ort[m] * g;
                ort[m] = ort[m] - g;

                // Apply Householder similarity transformation
                // H = (I-u*u'/h)*H*(I-u*u')/h)

                for (int j = m; j < n; j++) {
                    double f = 0.0;
                    for (int i = high; i >= m; i--) {
                        f += ort[i] * nonSymHess[i][j];
                    }
                    f = f / h;
                    for (int i = m; i <= high; i++) {
                        nonSymHess[i][j] -= f * ort[i];
                    }
                }

                for (int i = 0; i <= high; i++) {
                    double f = 0.0;
                    for (int j = high; j >= m; j--) {
                        f += ort[j] * nonSymHess[i][j];
                    }
                    f = f / h;
                    for (int j = m; j <= high; j++) {
                        nonSymHess[i][j] -= f * ort[j];
                    }
                }
                ort[m] = scale * ort[m];
                nonSymHess[m][m - 1] = scale * g;
            }
        }

        // Accumulate transformations (Algol's ortran).

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                vectors.set(i, j, (i == j ? 1.0 : 0.0));
            }
        }

        for (int m = high - 1; m >= low + 1; m--) {
            if (nonSymHess[m][m - 1] != 0.0) {
                for (int i = m + 1; i <= high; i++) {
                    ort[i] = nonSymHess[i][m - 1];
                }
                for (int j = m; j <= high; j++) {
                    double g = 0.0;
                    for (int i = m; i <= high; i++) {
                        g += ort[i] * vectors.get(i, j);
                    }
                    // Double division avoids possible underflow
                    g = (g / ort[m]) / nonSymHess[m][m - 1];
                    for (int i = m; i <= high; i++) {
                        vectors.set(i, j, vectors.get(i, j) + g * ort[i]);
                    }
                }
            }
        }
    }

    // Complex scalar division.
    private transient double cdivr, cdivi;

    private void cdiv(double xr, double xi, double yr, double yi) {
        double r, d;
        if (Math.abs(yr) > Math.abs(yi)) {
            r = yi / yr;
            d = yr + r * yi;
            cdivr = (xr + r * xi) / d;
            cdivi = (xi - r * xr) / d;
        } else {
            r = yr / yi;
            d = yi + r * yr;
            cdivr = (r * xr + xi) / d;
            cdivi = (r * xi - xr) / d;
        }
    }

    // Nonsymmetric reduction from Hessenberg to real Schur form.
    private void hessenbergToRealSchurForm(double[][] nonSymHess) {

        //  This is derived from the Algol procedure hqr2,
        //  by Martin and Wilkinson, Handbook for Auto. Comp.,
        //  Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        // Initialize

        int nn = this.n;
        int n = nn - 1;
        int low = 0;
        int high = nn - 1;
        double eps = Math.pow(2.0, -52.0);
        double exshift = 0.0;
        double p = 0, q = 0, r = 0, s = 0, z = 0, t, w, x, y;

        // Store roots isolated by balanc and compute matrix norm

        double norm = 0.0;
        for (int i = 0; i < nn; i++) {
            if (i < low | i > high) {
                real.set(i, nonSymHess[i][i]);
                imag.set(i, 0);
            }
            for (int j = Math.max(i - 1, 0); j < nn; j++) {
                norm = norm + Math.abs(nonSymHess[i][j]);
            }
        }

        // Outer loop over eigenvalue index

        int iter = 0;
        while (n >= low) {

            // Look for single small sub-diagonal element

            int l = n;
            while (l > low) {
                s = Math.abs(nonSymHess[l - 1][l - 1]) + Math.abs(nonSymHess[l][l]);
                if (s == 0.0) {
                    s = norm;
                }
                if (Math.abs(nonSymHess[l][l - 1]) < eps * s) {
                    break;
                }
                l--;
            }

            // Check for convergence
            // One root found

            if (l == n) {
                nonSymHess[n][n] = nonSymHess[n][n] + exshift;
                real.set(n, nonSymHess[n][n]);
                imag.set(n, 0);
                n--;
                iter = 0;

                // Two roots found

            } else if (l == n - 1) {
                w = nonSymHess[n][n - 1] * nonSymHess[n - 1][n];
                p = (nonSymHess[n - 1][n - 1] - nonSymHess[n][n]) / 2.0;
                q = p * p + w;
                z = Math.sqrt(Math.abs(q));
                nonSymHess[n][n] = nonSymHess[n][n] + exshift;
                nonSymHess[n - 1][n - 1] = nonSymHess[n - 1][n - 1] + exshift;
                x = nonSymHess[n][n];

                // Real pair

                if (q >= 0) {
                    if (p >= 0) {
                        z = p + z;
                    } else {
                        z = p - z;
                    }
                    real.set(n - 1, x + z);
                    real.set(n, real.get(n - 1));
                    if (z != 0.0) {
                        real.set(n, x - w / z);
                    }
                    imag.set(n - 1, 0);
                    imag.set(n, 0);
                    x = nonSymHess[n][n - 1];
                    s = Math.abs(x) + Math.abs(z);
                    p = x / s;
                    q = z / s;
                    r = Math.sqrt(p * p + q * q);
                    p = p / r;
                    q = q / r;

                    // Row modification

                    for (int j = n - 1; j < nn; j++) {
                        z = nonSymHess[n - 1][j];
                        nonSymHess[n - 1][j] = q * z + p * nonSymHess[n][j];
                        nonSymHess[n][j] = q * nonSymHess[n][j] - p * z;
                    }

                    // Column modification

                    for (int i = 0; i <= n; i++) {
                        z = nonSymHess[i][n - 1];
                        nonSymHess[i][n - 1] = q * z + p * nonSymHess[i][n];
                        nonSymHess[i][n] = q * nonSymHess[i][n] - p * z;
                    }

                    // Accumulate transformations

                    for (int i = low; i <= high; i++) {
                        z = vectors.get(i, n - 1);
                        vectors.set(i, n - 1, q * z + p * vectors.get(i, n));
                        vectors.set(i, n, q * vectors.get(i, n) - p * z);
                    }

                    // Complex pair

                } else {
                    real.set(n - 1, x + p);
                    real.set(n, x + p);
                    imag.set(n - 1, z);
                    imag.set(n, -z);
                }
                n = n - 2;
                iter = 0;

                // No convergence yet

            } else {

                // Form shift

                x = nonSymHess[n][n];
                y = 0.0;
                w = 0.0;
                if (l < n) {
                    y = nonSymHess[n - 1][n - 1];
                    w = nonSymHess[n][n - 1] * nonSymHess[n - 1][n];
                }

                // Wilkinson's original ad hoc shift


                switch (iter) {
                    case 10 -> {
                        exshift += x;
                        for (int i = low; i <= n; i++) {
                            nonSymHess[i][i] -= x;
                        }
                        s = Math.abs(nonSymHess[n][n - 1]) + Math.abs(nonSymHess[n - 1][n - 2]);
                        x = y = 0.75 * s;
                        w = -0.4375 * s * s;
                    }
                    case 30 -> {
                        s = (y - x) / 2.0;
                        s = s * s + w;
                        if (s > 0) {
                            s = Math.sqrt(s);
                            if (y < x) {
                                s = -s;
                            }
                            s = x - w / ((y - x) / 2.0 + s);
                            for (int i = low; i <= n; i++) {
                                nonSymHess[i][i] -= s;
                            }
                            exshift += s;
                            x = y = w = 0.964;
                        }
                    }
                }

                // MATLAB's new ad hoc shift

                iter++;   // (Could check iteration count here.)

                // Look for two consecutive small sub-diagonal elements

                int m = n - 2;
                while (m >= l) {
                    z = nonSymHess[m][m];
                    r = x - z;
                    s = y - z;
                    p = (r * s - w) / nonSymHess[m + 1][m] + nonSymHess[m][m + 1];
                    q = nonSymHess[m + 1][m + 1] - z - r - s;
                    r = nonSymHess[m + 2][m + 1];
                    s = Math.abs(p) + Math.abs(q) + Math.abs(r);
                    p = p / s;
                    q = q / s;
                    r = r / s;
                    if (m == l) {
                        break;
                    }
                    if (Math.abs(nonSymHess[m][m - 1]) * (Math.abs(q) + Math.abs(r)) <
                            eps * (Math.abs(p) * (Math.abs(nonSymHess[m - 1][m - 1]) + Math.abs(z) +
                                    Math.abs(nonSymHess[m + 1][m + 1])))) {
                        break;
                    }
                    m--;
                }

                for (int i = m + 2; i <= n; i++) {
                    nonSymHess[i][i - 2] = 0.0;
                    if (i > m + 2) {
                        nonSymHess[i][i - 3] = 0.0;
                    }
                }

                // Double QR step involving rows l:n and columns m:n


                for (int k = m; k <= n - 1; k++) {
                    boolean notlast = (k != n - 1);
                    if (k != m) {
                        p = nonSymHess[k][k - 1];
                        q = nonSymHess[k + 1][k - 1];
                        r = (notlast ? nonSymHess[k + 2][k - 1] : 0.0);
                        x = Math.abs(p) + Math.abs(q) + Math.abs(r);
                        if (x == 0.0) {
                            continue;
                        }
                        p = p / x;
                        q = q / x;
                        r = r / x;
                    }

                    s = Math.sqrt(p * p + q * q + r * r);
                    if (p < 0) {
                        s = -s;
                    }
                    if (s != 0) {
                        if (k != m) {
                            nonSymHess[k][k - 1] = -s * x;
                        } else if (l != m) {
                            nonSymHess[k][k - 1] = -nonSymHess[k][k - 1];
                        }
                        p = p + s;
                        x = p / s;
                        y = q / s;
                        z = r / s;
                        q = q / p;
                        r = r / p;

                        // Row modification

                        for (int j = k; j < nn; j++) {
                            p = nonSymHess[k][j] + q * nonSymHess[k + 1][j];
                            if (notlast) {
                                p = p + r * nonSymHess[k + 2][j];
                                nonSymHess[k + 2][j] = nonSymHess[k + 2][j] - p * z;
                            }
                            nonSymHess[k][j] = nonSymHess[k][j] - p * x;
                            nonSymHess[k + 1][j] = nonSymHess[k + 1][j] - p * y;
                        }

                        // Column modification

                        for (int i = 0; i <= Math.min(n, k + 3); i++) {
                            p = x * nonSymHess[i][k] + y * nonSymHess[i][k + 1];
                            if (notlast) {
                                p = p + z * nonSymHess[i][k + 2];
                                nonSymHess[i][k + 2] = nonSymHess[i][k + 2] - p * r;
                            }
                            nonSymHess[i][k] = nonSymHess[i][k] - p;
                            nonSymHess[i][k + 1] = nonSymHess[i][k + 1] - p * q;
                        }

                        // Accumulate transformations

                        for (int i = low; i <= high; i++) {
                            p = x * vectors.get(i, k) + y * vectors.get(i, k + 1);
                            if (notlast) {
                                p = p + z * vectors.get(i, k + 2);
                                vectors.set(i, k + 2, vectors.get(i, k + 2) - p * r);
                            }
                            vectors.set(i, k, vectors.get(i, k) - p);
                            vectors.set(i, k + 1, vectors.get(i, k + 1) - p * q);
                        }
                    }  // (s != 0)
                }  // k loop
            }  // check convergence
        }  // while (n >= low)

        // Backsubstitute to find vectors of upper triangular form

        if (norm == 0.0) {
            return;
        }

        for (n = nn - 1; n >= 0; n--) {
            p = real.get(n);
            q = imag.get(n);

            // Real vector

            if (q == 0) {
                int l = n;
                nonSymHess[n][n] = 1.0;
                for (int i = n - 1; i >= 0; i--) {
                    w = nonSymHess[i][i] - p;
                    r = 0.0;
                    for (int j = l; j <= n; j++) {
                        r = r + nonSymHess[i][j] * nonSymHess[j][n];
                    }
                    if (imag.get(i) < 0) {
                        z = w;
                        s = r;
                    } else {
                        l = i;
                        if (imag.get(i) == 0) {
                            if (w != 0.0) {
                                nonSymHess[i][n] = -r / w;
                            } else {
                                nonSymHess[i][n] = -r / (eps * norm);
                            }

                            // Solve real equations

                        } else {
                            x = nonSymHess[i][i + 1];
                            y = nonSymHess[i + 1][i];
                            q = (real.get(i) - p) * (real.get(i) - p) + imag.get(i) * imag.get(i);
                            t = (x * s - z * r) / q;
                            nonSymHess[i][n] = t;
                            if (Math.abs(x) > Math.abs(z)) {
                                nonSymHess[i + 1][n] = (-r - w * t) / x;
                            } else {
                                nonSymHess[i + 1][n] = (-s - y * t) / z;
                            }
                        }

                        // Overflow control

                        t = Math.abs(nonSymHess[i][n]);
                        if ((eps * t) * t > 1) {
                            for (int j = i; j <= n; j++) {
                                nonSymHess[j][n] = nonSymHess[j][n] / t;
                            }
                        }
                    }
                }

                // Complex vector

            } else if (q < 0) {
                int l = n - 1;

                // Last vector component imaginary so matrix is triangular

                if (Math.abs(nonSymHess[n][n - 1]) > Math.abs(nonSymHess[n - 1][n])) {
                    nonSymHess[n - 1][n - 1] = q / nonSymHess[n][n - 1];
                    nonSymHess[n - 1][n] = -(nonSymHess[n][n] - p) / nonSymHess[n][n - 1];
                } else {
                    cdiv(0.0, -nonSymHess[n - 1][n], nonSymHess[n - 1][n - 1] - p, q);
                    nonSymHess[n - 1][n - 1] = cdivr;
                    nonSymHess[n - 1][n] = cdivi;
                }
                nonSymHess[n][n - 1] = 0.0;
                nonSymHess[n][n] = 1.0;
                for (int i = n - 2; i >= 0; i--) {
                    double ra, sa, vr, vi;
                    ra = 0.0;
                    sa = 0.0;
                    for (int j = l; j <= n; j++) {
                        ra = ra + nonSymHess[i][j] * nonSymHess[j][n - 1];
                        sa = sa + nonSymHess[i][j] * nonSymHess[j][n];
                    }
                    w = nonSymHess[i][i] - p;

                    if (imag.get(i) < 0.0) {
                        z = w;
                        r = ra;
                        s = sa;
                    } else {
                        l = i;
                        if (imag.get(i) == 0) {
                            cdiv(-ra, -sa, w, q);
                            nonSymHess[i][n - 1] = cdivr;
                            nonSymHess[i][n] = cdivi;
                        } else {

                            // Solve complex equations

                            x = nonSymHess[i][i + 1];
                            y = nonSymHess[i + 1][i];
                            vr = (real.get(i) - p) * (real.get(i) - p) + imag.get(i) * imag.get(i) - q * q;
                            vi = (real.get(i) - p) * 2.0 * q;
                            if (vr == 0.0 & vi == 0.0) {
                                vr = eps * norm * (Math.abs(w) + Math.abs(q) +
                                        Math.abs(x) + Math.abs(y) + Math.abs(z));
                            }
                            cdiv(x * r - z * ra + q * sa, x * s - z * sa - q * ra, vr, vi);
                            nonSymHess[i][n - 1] = cdivr;
                            nonSymHess[i][n] = cdivi;
                            if (Math.abs(x) > (Math.abs(z) + Math.abs(q))) {
                                nonSymHess[i + 1][n - 1] = (-ra - w * nonSymHess[i][n - 1] + q * nonSymHess[i][n]) / x;
                                nonSymHess[i + 1][n] = (-sa - w * nonSymHess[i][n] - q * nonSymHess[i][n - 1]) / x;
                            } else {
                                cdiv(-r - y * nonSymHess[i][n - 1], -s - y * nonSymHess[i][n], z, q);
                                nonSymHess[i + 1][n - 1] = cdivr;
                                nonSymHess[i + 1][n] = cdivi;
                            }
                        }

                        // Overflow control

                        t = Math.max(Math.abs(nonSymHess[i][n - 1]), Math.abs(nonSymHess[i][n]));
                        if ((eps * t) * t > 1) {
                            for (int j = i; j <= n; j++) {
                                nonSymHess[j][n - 1] = nonSymHess[j][n - 1] / t;
                                nonSymHess[j][n] = nonSymHess[j][n] / t;
                            }
                        }
                    }
                }
            }
        }

        // Vectors of isolated roots

        for (int i = 0; i < nn; i++) {
            if (i < low | i > high) {
                for (int j = i; j < nn; j++) {
                    vectors.set(i, j, nonSymHess[i][j]);
                }
            }
        }

        // Back transformation to get eigenvectors of original matrix

        for (int j = nn - 1; j >= low; j--) {
            for (int i = low; i <= high; i++) {
                z = 0.0;
                for (int k = low; k <= Math.min(j, high); k++) {
                    z = z + vectors.get(i, k) * nonSymHess[k][j];
                }
                vectors.set(i, j, z);
            }
        }
    }

    // reverse order of eigen values and eigen vectors
    private void reverseOrder() {
        int[] indexes = new int[n];
        for (int i = 0; i < n; i++) {
            indexes[i] = n - 1 - i;
        }

        for (int i = 0; i < n / 2; i++) {

            // swap real
            double tmp = real.get(i);
            real.set(i, real.get(indexes[i]));
            real.set(indexes[i], tmp);

            // swap imag
            tmp = imag.get(i);
            imag.set(i, imag.get(indexes[i]));
            imag.set(indexes[i], tmp);

            // swap vectors
            for (int j = 0; j < n; j++) {
                tmp = vectors.get(j, i);
                vectors.set(j, i, vectors.get(j, indexes[i]));
                vectors.set(j, indexes[i], tmp);
            }
        }
    }

    /**
     * Return the eigenvector matrix
     *
     * @return V
     */
    public DMatrix v() {
        return vectors;
    }

    /**
     * Return the real parts of the eigenvalues
     *
     * @return real(diag ( D))
     */
    public DVectorDense real() {
        return real;
    }

    /**
     * Return the imaginary parts of the eigenvalues
     *
     * @return {@code imag(diag(D))}
     */
    public DVectorDense imag() {
        return imag;
    }

    public DMatrix power(double power) {
        DMatrix lambda = d();
        for (int i = 0; i < lambda.rows(); i++) {
            lambda.set(i, i, Math.pow(lambda.get(i, i), power));
        }
        return vectors.dot(lambda).dot(vectors.t());
    }

    /**
     * Return the block diagonal eigenvalue matrix
     *
     * @return D the block diagonal eigenvalue matrix
     */
    public DMatrix d() {
        DMatrix d = DMatrixDenseC.diagonal(real);
        for (int i = 0; i < n; i++) {
            if (imag.get(i) > 0) {
                d.set(i, i - 1, imag.get(i));
            } else if (imag.get(i) < 0) {
                d.set(i, i + 1, imag.get(i));
            }
        }
        return d;
    }
}
