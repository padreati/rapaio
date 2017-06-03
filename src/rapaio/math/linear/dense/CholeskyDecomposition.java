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


import rapaio.math.linear.BackwardSubstitution;
import rapaio.math.linear.ForwardSubstitution;
import rapaio.math.linear.RM;
import rapaio.math.linear.SubstitutionStrategy;

import java.io.Serializable;

/**
 * Cholesky Decomposition.
 * <p>
 * For a symmetric, positive definite matrix A, the Cholesky decomposition is an
 * lower triangular matrix L so that A = L*L'.
 * <p>
 * If the matrix is not symmetric or positive definite, the constructor returns
 * a partial decomposition and sets an internal flag that may be queried by the
 * isSPD() method.
 */
public class CholeskyDecomposition implements Serializable {

    public static CholeskyDecomposition from(RM a) {
        return new CholeskyDecomposition(a);
    }

    private static final long serialVersionUID = -3047433451986241586L;

    /**
     * Array for internal storage of decomposition.
     */
    private final double[][] L;

    /**
     * Row and column dimension (square matrix).
     */
    private int n;

    /**
     * Symmetric and positive definite flag.
     */
    private boolean isspd;

    /**
     * Cholesky algorithm for symmetric and positive definite matrix.
     *
     * @param A Square, symmetric matrix.
     */

    private CholeskyDecomposition(RM A) {

        // Initialize.
        n = A.getRowCount();
        L = new double[n][n];
        isspd = (A.getColCount() == n);

        // Main loop.
        for (int j = 0; j < n; j++) {
            double[] Lrowj = L[j];
            double d = 0.0;
            for (int k = 0; k < j; k++) {
                double[] Lrowk = L[k];
                double s = 0.0;
                for (int i = 0; i < k; i++) {
                    s += Lrowk[i] * Lrowj[i];
                }
                Lrowj[k] = s = (A.get(j, k) - s) / L[k][k];
                d = d + s * s;
                if (A.get(k, j) != A.get(j, k)) {
                    isspd = false;
                }
            }
            d = A.get(j, j) - d;
            if (d <= 0.0)
                isspd = false;
            L[j][j] = Math.sqrt(Math.max(d, 0.0));
            for (int k = j + 1; k < n; k++) {
                L[j][k] = 0.0;
            }
        }
    }


//    /**
//     * Array for internal storage of right triangular decomposition.
//     **/
//    private transient double[][] R;
//
//    /** Right Triangular Cholesky Decomposition.
//     *
//     * For a symmetric, positive definite matrix A, the Right Cholesky decomposition is an upper
//     * triangular matrix R so that A = R'*R. This constructor computes R with
//     * the Fortran inspired column oriented algorithm used in LINPACK and
//     * MATLAB. In Java, we suspect a row oriented, lower triangular
//     * decomposition is faster. We have temporarily included this constructor
//     * here until timing experiments confirm this suspicion.
//     *
//     * @param A square, symmetric matrix
//     * @param rightflag
//     */
//    public CholeskyDecomposition(RM A, int rightflag) {
//
//        n = A.getColCount();
//        R = new double[n][n];
//        isspd = (A.getColCount() == n); // Main loop.
//        for (int j = 0; j < n; j++) {
//            double d = 0.0;
//            for (int k = 0; k < j; k++) {
//                double s = A.get(k, j);
//                for (int i = 0; i < k; i++) {
//                    s = s - R[i][k] * R[i][j];
//                }
//                R[k][j] = s = s / R[k][k];
//                d = d + s * s;
//                isspd = isspd & (A.get(k, j) == A.get(j, k));
//            }
//            d = A.get(j, j) - d;
//            isspd = isspd & (d > 0.0);
//            R[j][j] = Math.sqrt(Math.max(d, 0.0));
//            for (int k = j + 1; k < n; k++) {
//                R[k][j] = 0.0;
//            }
//        }
//    }
//    /**
//     * Return upper triangular factor. @return R
//     */
//    public RM getR() {
//        return SolidRM.wrap(R);
//    }

    /**
     * Is the matrix symmetric and positive definite?
     *
     * @return true if A is symmetric and positive definite.
     */
    public boolean isSPD() {
        return isspd;
    }

    /**
     * @return L triangular factor
     */
    public RM getL() {
        return SolidRM.wrap(L);
    }

    /**
     * Solve A*X = B
     *
     * @param B A Matrix with as many rows as A and any number of columns.
     * @return X so that L*L'*X = B
     * @throws IllegalArgumentException Matrix row dimensions must agree.
     * @throws RuntimeException         Matrix is not symmetric positive definite.
     */

    public RM solve(RM B) {
        if (B.getRowCount() != n) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!isspd) {
            throw new IllegalArgumentException("Matrix is not symmetric positive definite.");
        }

        // Copy right hand side.
        RM X = B.solidCopy();
        int nx = B.getColCount();

        SubstitutionStrategy sStrategy = new ForwardSubstitution();
        
        X = sStrategy.getSubstitution(n, nx, X, L);

        sStrategy = new BackwardSubstitution();
        
        X = sStrategy.getSubstitution(n, nx, X, L);
        
        return X;
    }
}
