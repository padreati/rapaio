/* Apache License
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

import java.io.Serializable;

/**
 * Eigenvalues and eigen vectors of a real matrix.
 * <p>
 * If A is symmetric, then A = RV*D*RV' where the eigenvalue matrix D is diagonal
 * and the eigenvector matrix RV is orthogonal. I.e. A = RV.prod(D.prod(RV.t())) and RV.prod(RV.prod()) equals the
 * identity matrix.
 * <p>
 * If A is not symmetric, then the eigenvalue matrix D is block diagonal with
 * the real eigenvalues in 1-by-1 blocks and any complex eigenvalues, lambda +
 * i*mu, in 2-by-2 blocks, [lambda, mu; -mu, lambda]. The columns of RV represent
 * the eigenvectors in the sense that A*RV = RV*D, i.e. A.times(RV) equals
 * RV.times(D). The matrix RV may be badly conditioned, or even singular, so the
 * validity of the equation A = RV*D*inverse(RV) depends upon RV.cond().
 */

public class EigenvalueDecomposition implements Serializable {

    private static final long serialVersionUID = 5064091847331016868L;

    /**
     * Row and column dimension (square matrix).
     *
     * @serial matrix dimension.
     */
    private int dimension;

    /**
     * Arrays for internal storage of eigenvalues.
     *
     * @serial internal storage of eigenvalues.
     */
    private double[] realEigenValues, imagEigenValues;

    /**
     * Array for internal storage of eigenvectors.
     *
     * @serial internal storage of eigenvectors.
     */
    private RM eigenVectors;

    /**
     * Array for internal storage of nonsymmetric Hessenberg form.
     *
     * @serial internal storage of nonsymmetric Hessenberg form.
     */
    private double[][] nonSymHessenbergForm;

    /**
     * Working storage for nonsymmetric algorithm.
     *
     * @serial working storage for nonsymmetric algorithm.
     */
    private double[] ort;

    /**
     * Check for symmetry, then construct the eigenvalue decomposition
     * Structure to access D and V.
     *
     * @param rMatrixArg Square matrix
     */

    public EigenvalueDecomposition(RM rMatrixArg) {
        RM rMatrix = rMatrixArg.solidCopy();
        dimension = rMatrixArg.colCount();
        eigenVectors = SolidRM.empty(dimension, dimension);
        realEigenValues = new double[dimension];
        imagEigenValues = new double[dimension];

        if (isSymmetric(rMatrix)) {
            for (int row = 0; row < dimension; row++) {
                for (int col = 0; col < dimension; col++) {
                    eigenVectors.set(row, col, rMatrix.get(row, col));
                }
            }

            tridiagonalize();

            diagonalize();

        } else {
            nonSymHessenbergForm = new double[dimension][dimension];
            ort = new double[dimension];

            for (int col = 0; col < dimension; col++) {
                for (int row = 0; row < dimension; row++) {
                    nonSymHessenbergForm[row][col] = rMatrix.get(row, col);
                }
            }

            reduceToHessenbergForm();

            hessenbergToRealSchurForm();
        }
    }

	private boolean isSymmetric(RM rMatrix) {
		
		boolean returnValue = true;
		for (int row = 0; (row < dimension) & returnValue; row++) {
            for (int col = 0; (col < dimension); col++) {
                if (!(rMatrix.get(col, row) == rMatrix.get(row, col))) {
                    returnValue = false;
                    break;
                }
            }
        }
		return returnValue;
	}

/* ------------------------
   Private Methods
 * ------------------------ */

    // Symmetric Householder reduction to tridiagonal form.

    private void tridiagonalize() {

        //  This is derived from the Algol procedures tred2 by
        //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
        //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        for (int col = 0; col < dimension; col++) {
            realEigenValues[col] = eigenVectors.get(dimension - 1, col);
        }

        // Householder reduction to tridiagonal form.

        for (int i = dimension - 1; i > 0; i--) {

            // Scale to avoid under/overflow.

            double scale = 0.0;
            double h = 0.0;
            for (int k = 0; k < i; k++) {
                scale = scale + Math.abs(realEigenValues[k]);
            }
            if (scale == 0.0) {
                imagEigenValues[i] = realEigenValues[i - 1];
                for (int j = 0; j < i; j++) {
                    realEigenValues[j] = eigenVectors.get(i - 1, j);
                    eigenVectors.set(i, j, 0.0);
                    eigenVectors.set(j, i, 0.0);
                }
            } else {

                // Generate Householder vector.

                for (int k = 0; k < i; k++) {
                    realEigenValues[k] /= scale;
                    h += realEigenValues[k] * realEigenValues[k];
                }
                double f = realEigenValues[i - 1];
                double g = Math.sqrt(h);
                if (f > 0) {
                    g = -g;
                }
                imagEigenValues[i] = scale * g;
                h = h - f * g;
                realEigenValues[i - 1] = f - g;
                for (int j = 0; j < i; j++) {
                    imagEigenValues[j] = 0.0;
                }

                // Apply similarity transformation to remaining columns.

                for (int j = 0; j < i; j++) {
                    f = realEigenValues[j];
                    eigenVectors.set(j, i, f);
                    g = imagEigenValues[j] + eigenVectors.get(j, j) * f;
                    for (int k = j + 1; k <= i - 1; k++) {
                        g += eigenVectors.get(k, j) * realEigenValues[k];
                        imagEigenValues[k] += eigenVectors.get(k, j) * f;
                    }
                    imagEigenValues[j] = g;
                }
                f = 0.0;
                for (int j = 0; j < i; j++) {
                    imagEigenValues[j] /= h;
                    f += imagEigenValues[j] * realEigenValues[j];
                }
                double hh = f / (h + h);
                for (int j = 0; j < i; j++) {
                    imagEigenValues[j] -= hh * realEigenValues[j];
                }
                for (int j = 0; j < i; j++) {
                    f = realEigenValues[j];
                    g = imagEigenValues[j];
                    for (int k = j; k <= i - 1; k++) {
                        eigenVectors.increment(k, j, -(f * imagEigenValues[k] + g * realEigenValues[k]));
                    }
                    realEigenValues[j] = eigenVectors.get(i - 1, j);
                    eigenVectors.set(i, j, 0.0);
                }
            }
            realEigenValues[i] = h;
        }

        // Accumulate transformations.

        for (int i = 0; i < dimension - 1; i++) {
            eigenVectors.set(dimension - 1, i, eigenVectors.get(i, i));
            eigenVectors.set(i, i, 1.0);
            double h = realEigenValues[i + 1];
            if (h != 0.0) {
                for (int k = 0; k <= i; k++) {
                    realEigenValues[k] = eigenVectors.get(k, i + 1) / h;
                }
                for (int j = 0; j <= i; j++) {
                    double g = 0.0;
                    for (int k = 0; k <= i; k++) {
                        g += eigenVectors.get(k, i + 1) * eigenVectors.get(k, j);
                    }
                    for (int k = 0; k <= i; k++) {
                        eigenVectors.increment(k, j, -g * realEigenValues[k]);
                    }
                }
            }
            for (int k = 0; k <= i; k++) {
                eigenVectors.set(k, i + 1, 0.0);
            }
        }
        for (int j = 0; j < dimension; j++) {
            realEigenValues[j] = eigenVectors.get(dimension - 1, j);
            eigenVectors.set(dimension - 1, j, 0.0);
        }
        eigenVectors.set(dimension - 1, dimension - 1, 1.0);
        imagEigenValues[0] = 0.0;
    }

    // Symmetric tridiagonal QL algorithm.

    private void diagonalize() {

        //  This is derived from the Algol procedures tql2, by
        //  Bowdler, Martin, Reinsch, and Wilkinson, Handbook for
        //  Auto. Comp., Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        for (int i = 1; i < dimension; i++) {
            imagEigenValues[i - 1] = imagEigenValues[i];
        }
        imagEigenValues[dimension - 1] = 0.0;

        double f = 0.0;
        double tst1 = 0.0;
        double eps = Math.pow(2.0, -52.0);
        for (int l = 0; l < dimension; l++) {

            // Find small subdiagonal element

            tst1 = Math.max(tst1, Math.abs(realEigenValues[l]) + Math.abs(imagEigenValues[l]));
            int m = l;
            while (m < dimension) {
                if (Math.abs(imagEigenValues[m]) <= eps * tst1) {
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

                    double g = realEigenValues[l];
                    double p = (realEigenValues[l + 1] - g) / (2.0 * imagEigenValues[l]);
                    double r = Math.hypot(p, 1.0);
                    if (p < 0) {
                        r = -r;
                    }
                    realEigenValues[l] = imagEigenValues[l] / (p + r);
                    realEigenValues[l + 1] = imagEigenValues[l] * (p + r);
                    double dl1 = realEigenValues[l + 1];
                    double h = g - realEigenValues[l];
                    for (int i = l + 2; i < dimension; i++) {
                        realEigenValues[i] -= h;
                    }
                    f = f + h;

                    // Implicit QL transformation.

                    p = realEigenValues[m];
                    double c = 1.0;
                    double c2 = c;
                    double c3 = c;
                    double el1 = imagEigenValues[l + 1];
                    double s = 0.0;
                    double s2 = 0.0;
                    for (int i = m - 1; i >= l; i--) {
                        c3 = c2;
                        c2 = c;
                        s2 = s;
                        g = c * imagEigenValues[i];
                        h = c * p;
                        r = Math.hypot(p, imagEigenValues[i]);
                        imagEigenValues[i + 1] = s * r;
                        s = imagEigenValues[i] / r;
                        c = p / r;
                        p = c * realEigenValues[i] - s * g;
                        realEigenValues[i + 1] = h + s * (c * g + s * realEigenValues[i]);

                        // Accumulate transformation.

                        for (int k = 0; k < dimension; k++) {
                            h = eigenVectors.get(k, i + 1);
                            eigenVectors.set(k, i + 1, s * eigenVectors.get(k, i) + c * h);
                            eigenVectors.set(k, i, c * eigenVectors.get(k, i) - s * h);
                        }
                    }
                    p = -s * s2 * c3 * el1 * imagEigenValues[l] / dl1;
                    imagEigenValues[l] = s * p;
                    realEigenValues[l] = c * p;

                    // Check for convergence.

                } while (Math.abs(imagEigenValues[l]) > eps * tst1);
            }
            realEigenValues[l] = realEigenValues[l] + f;
            imagEigenValues[l] = 0.0;
        }

        // Sort eigenvalues and corresponding vectors.

        for (int i = 0; i < dimension - 1; i++) {
            int k = i;
            double p = realEigenValues[i];
            for (int j = i + 1; j < dimension; j++) {
                if (realEigenValues[j] < p) {
                    k = j;
                    p = realEigenValues[j];
                }
            }
            if (k != i) {
                realEigenValues[k] = realEigenValues[i];
                realEigenValues[i] = p;
                for (int j = 0; j < dimension; j++) {
                    p = eigenVectors.get(j, i);
                    eigenVectors.set(j, i, eigenVectors.get(j, k));
                    eigenVectors.set(j, k, p);
                }
            }
        }
    }

    // Nonsymmetric reduction to Hessenberg form.

    private void reduceToHessenbergForm() {

        //  This is derived from the Algol procedures orthes and ortran,
        //  by Martin and Wilkinson, Handbook for Auto. Comp.,
        //  Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutines in EISPACK.

        int low = 0;
        int high = dimension - 1;

        for (int m = low + 1; m <= high - 1; m++) {

            // Scale column.

            double scale = 0.0;
            for (int i = m; i <= high; i++) {
                scale = scale + Math.abs(nonSymHessenbergForm[i][m - 1]);
            }
            if (scale != 0.0) {

                // Compute Householder transformation.

                double h = 0.0;
                for (int i = high; i >= m; i--) {
                    ort[i] = nonSymHessenbergForm[i][m - 1] / scale;
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

                for (int j = m; j < dimension; j++) {
                    double f = 0.0;
                    for (int i = high; i >= m; i--) {
                        f += ort[i] * nonSymHessenbergForm[i][j];
                    }
                    f = f / h;
                    for (int i = m; i <= high; i++) {
                        nonSymHessenbergForm[i][j] -= f * ort[i];
                    }
                }

                for (int i = 0; i <= high; i++) {
                    double f = 0.0;
                    for (int j = high; j >= m; j--) {
                        f += ort[j] * nonSymHessenbergForm[i][j];
                    }
                    f = f / h;
                    for (int j = m; j <= high; j++) {
                        nonSymHessenbergForm[i][j] -= f * ort[j];
                    }
                }
                ort[m] = scale * ort[m];
                nonSymHessenbergForm[m][m - 1] = scale * g;
            }
        }

        // Accumulate transformations (Algol's ortran).

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                eigenVectors.set(i, j, (i == j ? 1.0 : 0.0));
            }
        }

        for (int m = high - 1; m >= low + 1; m--) {
            if (nonSymHessenbergForm[m][m - 1] != 0.0) {
                for (int i = m + 1; i <= high; i++) {
                    ort[i] = nonSymHessenbergForm[i][m - 1];
                }
                for (int j = m; j <= high; j++) {
                    double g = 0.0;
                    for (int i = m; i <= high; i++) {
                        g += ort[i] * eigenVectors.get(i, j);
                    }
                    // Double division avoids possible underflow
                    g = (g / ort[m]) / nonSymHessenbergForm[m][m - 1];
                    for (int i = m; i <= high; i++) {
                        eigenVectors.increment(i, j, g * ort[i]);
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

    private void hessenbergToRealSchurForm() {

        //  This is derived from the Algol procedure hqr2,
        //  by Martin and Wilkinson, Handbook for Auto. Comp.,
        //  Vol.ii-Linear Algebra, and the corresponding
        //  Fortran subroutine in EISPACK.

        // Initialize

        int n = dimension - 1;
        int low = 0;
        int high = dimension - 1;
        double eps = Math.pow(2.0, -52.0);
        double exshift = 0.0;
        double p = 0, q = 0, r = 0, s = 0, z = 0, t, w, x, y;

        // Store roots isolated by balanc and compute matrix norm

        double norm = storeRoot(low, high);

        // Outer loop over eigenvalue index

        int iter = 0;
        while (n >= low) {

            // Look for single small sub-diagonal element

            int l = n;
            while (l > low) {
                s = Math.abs(nonSymHessenbergForm[l - 1][l - 1]) + Math.abs(nonSymHessenbergForm[l][l]);
                if (s == 0.0) {
                    s = norm;
                }
                if (Math.abs(nonSymHessenbergForm[l][l - 1]) < eps * s) {
                    break;
                }
                l--;
            }

            // Check for convergence
            // One root found

            if (l == n) {
                nonSymHessenbergForm[n][n] = nonSymHessenbergForm[n][n] + exshift;
                realEigenValues[n] = nonSymHessenbergForm[n][n];
                imagEigenValues[n] = 0.0;
                n--;
                iter = 0;

                // Two roots found

            } else if (l == n - 1) {
                w = nonSymHessenbergForm[n][n - 1] * nonSymHessenbergForm[n - 1][n];
                p = (nonSymHessenbergForm[n - 1][n - 1] - nonSymHessenbergForm[n][n]) / 2.0;
                q = p * p + w;
                z = Math.sqrt(Math.abs(q));
                nonSymHessenbergForm[n][n] = nonSymHessenbergForm[n][n] + exshift;
                nonSymHessenbergForm[n - 1][n - 1] = nonSymHessenbergForm[n - 1][n - 1] + exshift;
                x = nonSymHessenbergForm[n][n];

                // Real pair

                if (q >= 0) {
                    if (p >= 0) {
                        z = p + z;
                    } else {
                        z = p - z;
                    }
                    realEigenValues[n - 1] = x + z;
                    realEigenValues[n] = realEigenValues[n - 1];
                    if (z != 0.0) {
                        realEigenValues[n] = x - w / z;
                    }
                    imagEigenValues[n - 1] = 0.0;
                    imagEigenValues[n] = 0.0;
                    x = nonSymHessenbergForm[n][n - 1];
                    s = Math.abs(x) + Math.abs(z);
                    p = x / s;
                    q = z / s;
                    r = Math.sqrt(p * p + q * q);
                    p = p / r;
                    q = q / r;

                    // Row modification

                    for (int j = n - 1; j < dimension; j++) {
                        z = nonSymHessenbergForm[n - 1][j];
                        nonSymHessenbergForm[n - 1][j] = q * z + p * nonSymHessenbergForm[n][j];
                        nonSymHessenbergForm[n][j] = q * nonSymHessenbergForm[n][j] - p * z;
                    }

                    // Column modification

                    for (int i = 0; i <= n; i++) {
                        z = nonSymHessenbergForm[i][n - 1];
                        nonSymHessenbergForm[i][n - 1] = q * z + p * nonSymHessenbergForm[i][n];
                        nonSymHessenbergForm[i][n] = q * nonSymHessenbergForm[i][n] - p * z;
                    }

                    // Accumulate transformations

                    for (int i = low; i <= high; i++) {
                        z = eigenVectors.get(i, n - 1);
                        eigenVectors.set(i, n - 1, q * z + p * eigenVectors.get(i, n));
                        eigenVectors.set(i, n, q * eigenVectors.get(i, n) - p * z);
                    }

                    // Complex pair

                } else {
                    realEigenValues[n - 1] = x + p;
                    realEigenValues[n] = x + p;
                    imagEigenValues[n - 1] = z;
                    imagEigenValues[n] = -z;
                }
                n = n - 2;
                iter = 0;

                // No convergence yet

            } else {

                // Form shift

                x = nonSymHessenbergForm[n][n];
                y = 0.0;
                w = 0.0;
                if (l < n) {
                    y = nonSymHessenbergForm[n - 1][n - 1];
                    w = nonSymHessenbergForm[n][n - 1] * nonSymHessenbergForm[n - 1][n];
                }

                // Wilkinson's original ad hoc shift

                
                switch(iter) {
                case 10:
                	exshift += x;
                    for (int i = low; i <= n; i++) {
                        nonSymHessenbergForm[i][i] -= x;
                    }
                    s = Math.abs(nonSymHessenbergForm[n][n - 1]) + Math.abs(nonSymHessenbergForm[n - 1][n - 2]);
                    x = y = 0.75 * s;
                    w = -0.4375 * s * s;
                    break;
                case 30:
                	s = (y - x) / 2.0;
                    s = s * s + w;
                    if (s > 0) {
                        s = Math.sqrt(s);
                        if (y < x) {
                            s = -s;
                        }
                        s = x - w / ((y - x) / 2.0 + s);
                        for (int i = low; i <= n; i++) {
                            nonSymHessenbergForm[i][i] -= s;
                        }
                        exshift += s;
                        x = y = w = 0.964;
                    }
                	
                }

                // MATLAB's new ad hoc shift

                iter++;   // (Could check iteration count here.)

                // Look for two consecutive small sub-diagonal elements

                int m = n - 2;
                while (m >= l) {
                    z = nonSymHessenbergForm[m][m];
                    r = x - z;
                    s = y - z;
                    p = (r * s - w) / nonSymHessenbergForm[m + 1][m] + nonSymHessenbergForm[m][m + 1];
                    q = nonSymHessenbergForm[m + 1][m + 1] - z - r - s;
                    r = nonSymHessenbergForm[m + 2][m + 1];
                    s = Math.abs(p) + Math.abs(q) + Math.abs(r);
                    p = p / s;
                    q = q / s;
                    r = r / s;
                    if (m == l) {
                        break;
                    }
                    if (Math.abs(nonSymHessenbergForm[m][m - 1]) * (Math.abs(q) + Math.abs(r)) <
                            eps * (Math.abs(p) * (Math.abs(nonSymHessenbergForm[m - 1][m - 1]) + Math.abs(z) +
                                    Math.abs(nonSymHessenbergForm[m + 1][m + 1])))) {
                        break;
                    }
                    m--;
                }

                for (int i = m + 2; i <= n; i++) {
                    nonSymHessenbergForm[i][i - 2] = 0.0;
                    if (i > m + 2) {
                        nonSymHessenbergForm[i][i - 3] = 0.0;
                    }
                }

                // Double QR step involving rows l:n and columns m:n


                for (int k = m; k <= n - 1; k++) {
                    boolean notlast = (k != n - 1);
                    if (k != m) {
                        p = nonSymHessenbergForm[k][k - 1];
                        q = nonSymHessenbergForm[k + 1][k - 1];
                        r = (notlast ? nonSymHessenbergForm[k + 2][k - 1] : 0.0);
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
                            nonSymHessenbergForm[k][k - 1] = -s * x;
                        } else if (l != m) {
                            nonSymHessenbergForm[k][k - 1] = -nonSymHessenbergForm[k][k - 1];
                        }
                        p = p + s;
                        x = p / s;
                        y = q / s;
                        z = r / s;
                        q = q / p;
                        r = r / p;

                        // Row modification

                        for (int j = k; j < dimension; j++) {
                            p = nonSymHessenbergForm[k][j] + q * nonSymHessenbergForm[k + 1][j];
                            if (notlast) {
                                p = p + r * nonSymHessenbergForm[k + 2][j];
                                nonSymHessenbergForm[k + 2][j] = nonSymHessenbergForm[k + 2][j] - p * z;
                            }
                            nonSymHessenbergForm[k][j] = nonSymHessenbergForm[k][j] - p * x;
                            nonSymHessenbergForm[k + 1][j] = nonSymHessenbergForm[k + 1][j] - p * y;
                        }

                        // Column modification

                        for (int i = 0; i <= Math.min(n, k + 3); i++) {
                            p = x * nonSymHessenbergForm[i][k] + y * nonSymHessenbergForm[i][k + 1];
                            if (notlast) {
                                p = p + z * nonSymHessenbergForm[i][k + 2];
                                nonSymHessenbergForm[i][k + 2] = nonSymHessenbergForm[i][k + 2] - p * r;
                            }
                            nonSymHessenbergForm[i][k] = nonSymHessenbergForm[i][k] - p;
                            nonSymHessenbergForm[i][k + 1] = nonSymHessenbergForm[i][k + 1] - p * q;
                        }

                        // Accumulate transformations

                        for (int i = low; i <= high; i++) {
                            p = x * eigenVectors.get(i, k) + y * eigenVectors.get(i, k + 1);
                            if (notlast) {
                                p = p + z * eigenVectors.get(i, k + 2);
                                eigenVectors.set(i, k + 2, eigenVectors.get(i, k + 2) - p * r);
                            }
                            eigenVectors.set(i, k, eigenVectors.get(i, k) - p);
                            eigenVectors.set(i, k + 1, eigenVectors.get(i, k + 1) - p * q);
                        }
                    }  // (s != 0)
                }  // k loop
            }  // check convergence
        }  // while (n >= low)

        // Backsubstitute to find vectors of upper triangular form

        if (norm == 0.0) {
            return;
        }

        for (n = dimension - 1; n >= 0; n--) {
            p = realEigenValues[n];
            q = imagEigenValues[n];

            // Real vector

            if (q == 0) {
                int l = n;
                nonSymHessenbergForm[n][n] = 1.0;
                for (int i = n - 1; i >= 0; i--) {
                    w = nonSymHessenbergForm[i][i] - p;
                    r = 0.0;
                    for (int j = l; j <= n; j++) {
                        r = r + nonSymHessenbergForm[i][j] * nonSymHessenbergForm[j][n];
                    }
                    if (imagEigenValues[i] < 0.0) {
                        z = w;
                        s = r;
                    } else {
                        l = i;
                        if (imagEigenValues[i] == 0.0) {
                            if (w != 0.0) {
                                nonSymHessenbergForm[i][n] = -r / w;
                            } else {
                                nonSymHessenbergForm[i][n] = -r / (eps * norm);
                            }

                            // Solve real equations

                        } else {
                            x = nonSymHessenbergForm[i][i + 1];
                            y = nonSymHessenbergForm[i + 1][i];
                            q = (realEigenValues[i] - p) * (realEigenValues[i] - p) + imagEigenValues[i] * imagEigenValues[i];
                            t = (x * s - z * r) / q;
                            nonSymHessenbergForm[i][n] = t;
                            if (Math.abs(x) > Math.abs(z)) {
                                nonSymHessenbergForm[i + 1][n] = (-r - w * t) / x;
                            } else {
                                nonSymHessenbergForm[i + 1][n] = (-s - y * t) / z;
                            }
                        }

                        // Overflow control

                        t = Math.abs(nonSymHessenbergForm[i][n]);
                        if ((eps * t) * t > 1) {
                            for (int j = i; j <= n; j++) {
                                nonSymHessenbergForm[j][n] = nonSymHessenbergForm[j][n] / t;
                            }
                        }
                    }
                }

                // Complex vector

            } else if (q < 0) {
                int l = n - 1;

                // Last vector component imaginary so matrix is triangular

                if (Math.abs(nonSymHessenbergForm[n][n - 1]) > Math.abs(nonSymHessenbergForm[n - 1][n])) {
                    nonSymHessenbergForm[n - 1][n - 1] = q / nonSymHessenbergForm[n][n - 1];
                    nonSymHessenbergForm[n - 1][n] = -(nonSymHessenbergForm[n][n] - p) / nonSymHessenbergForm[n][n - 1];
                } else {
                    cdiv(0.0, -nonSymHessenbergForm[n - 1][n], nonSymHessenbergForm[n - 1][n - 1] - p, q);
                    nonSymHessenbergForm[n - 1][n - 1] = cdivr;
                    nonSymHessenbergForm[n - 1][n] = cdivi;
                }
                nonSymHessenbergForm[n][n - 1] = 0.0;
                nonSymHessenbergForm[n][n] = 1.0;
                for (int i = n - 2; i >= 0; i--) {
                    double ra, sa, vr, vi;
                    ra = 0.0;
                    sa = 0.0;
                    for (int j = l; j <= n; j++) {
                        ra = ra + nonSymHessenbergForm[i][j] * nonSymHessenbergForm[j][n - 1];
                        sa = sa + nonSymHessenbergForm[i][j] * nonSymHessenbergForm[j][n];
                    }
                    w = nonSymHessenbergForm[i][i] - p;

                    if (imagEigenValues[i] < 0.0) {
                        z = w;
                        r = ra;
                        s = sa;
                    } else {
                        l = i;
                        if (imagEigenValues[i] == 0) {
                            cdiv(-ra, -sa, w, q);
                            nonSymHessenbergForm[i][n - 1] = cdivr;
                            nonSymHessenbergForm[i][n] = cdivi;
                        } else {

                            // Solve complex equations

                            x = nonSymHessenbergForm[i][i + 1];
                            y = nonSymHessenbergForm[i + 1][i];
                            vr = (realEigenValues[i] - p) * (realEigenValues[i] - p) + imagEigenValues[i] * imagEigenValues[i] - q * q;
                            vi = (realEigenValues[i] - p) * 2.0 * q;
                            if (vr == 0.0 & vi == 0.0) {
                                vr = eps * norm * (Math.abs(w) + Math.abs(q) +
                                        Math.abs(x) + Math.abs(y) + Math.abs(z));
                            }
                            cdiv(x * r - z * ra + q * sa, x * s - z * sa - q * ra, vr, vi);
                            nonSymHessenbergForm[i][n - 1] = cdivr;
                            nonSymHessenbergForm[i][n] = cdivi;
                            if (Math.abs(x) > (Math.abs(z) + Math.abs(q))) {
                                nonSymHessenbergForm[i + 1][n - 1] = (-ra - w * nonSymHessenbergForm[i][n - 1] + q * nonSymHessenbergForm[i][n]) / x;
                                nonSymHessenbergForm[i + 1][n] = (-sa - w * nonSymHessenbergForm[i][n] - q * nonSymHessenbergForm[i][n - 1]) / x;
                            } else {
                                cdiv(-r - y * nonSymHessenbergForm[i][n - 1], -s - y * nonSymHessenbergForm[i][n], z, q);
                                nonSymHessenbergForm[i + 1][n - 1] = cdivr;
                                nonSymHessenbergForm[i + 1][n] = cdivi;
                            }
                        }

                        // Overflow control

                        t = Math.max(Math.abs(nonSymHessenbergForm[i][n - 1]), Math.abs(nonSymHessenbergForm[i][n]));
                        if ((eps * t) * t > 1) {
                            for (int j = i; j <= n; j++) {
                                nonSymHessenbergForm[j][n - 1] = nonSymHessenbergForm[j][n - 1] / t;
                                nonSymHessenbergForm[j][n] = nonSymHessenbergForm[j][n] / t;
                            }
                        }
                    }
                }
            }
        }

        // Vectors of isolated roots

        for (int i = 0; i < dimension; i++) {
            if (i < low | i > high) {
                for (int j = i; j < dimension; j++) {
                    eigenVectors.set(i, j, nonSymHessenbergForm[i][j]);
                }
            }
        }

        // Back transformation to get eigenvectors of original matrix

        for (int j = dimension - 1; j >= low; j--) {
            for (int i = low; i <= high; i++) {
                z = 0.0;
                for (int k = low; k <= Math.min(j, high); k++) {
                    z = z + eigenVectors.get(i, k) * nonSymHessenbergForm[k][j];
                }
                eigenVectors.set(i, j, z);
            }
        }
    }

	private double storeRoot(int low, int high) {
		double norm = 0.0;
        for (int i = 0; i < dimension; i++) {
            if (i < low | i > high) {
                realEigenValues[i] = nonSymHessenbergForm[i][i];
                imagEigenValues[i] = 0.0;
            }
            for (int j = Math.max(i - 1, 0); j < dimension; j++) {
                norm = norm + Math.abs(nonSymHessenbergForm[i][j]);
            }
        }
		return norm;
	}


    /**
     * Return the eigen vector matrix
     *
     * @return V
     */

    public RM getV() {
        return eigenVectors;
    }

    /**
     * Return the real parts of the eigenvalues
     *
     * @return real(diag(D))
     */

    public double[] getRealEigenvalues() {
        return realEigenValues;
    }

    /**
     * Return the imaginary parts of the eigenvalues
     *
     * @return imag(diag(D))
     */

    public double[] getImagEigenvalues() {
        return imagEigenValues;
    }

    /**
     * Return the block diagonal eigenvalue matrix
     *
     * @return D
     */

    public RM getD() {
        RM rMatrix = SolidRM.empty(dimension, dimension);
        for (int i = 0; i < dimension; i++) {
            rMatrix.set(i, i, realEigenValues[i]);
            if (imagEigenValues[i] > 0) {
                rMatrix.set(i, i + 1, imagEigenValues[i]);
            } else if (imagEigenValues[i] < 0) {
                rMatrix.set(i, i - 1, imagEigenValues[i]);
            }
        }
        return rMatrix;
    }
}
