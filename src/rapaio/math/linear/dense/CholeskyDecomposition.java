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

    private CholeskyDecompositionData data = new CholeskyDecompositionData();

	/**
     * Cholesky algorithm for symmetric and positive definite matrix.
     *
     * @param A Square, symmetric matrix.
     */

    private CholeskyDecomposition(RM A) {

        // Initialize.
        data.setDimension(A.rowCount());
        data.setDecompositionArray(new double[data.getDimension()][data.getDimension()]);
        data.setSymAndPositive((A.colCount() == data.getDimension()));

        // Main loop.
        for (int j = 0; j < data.getDimension(); j++) {
            double[] Lrowj = data.getDecompositionArray()[j];
            double d = 0.0;
            for (int k = 0; k < j; k++) {
                double[] Lrowk = data.getDecompositionArray()[k];
                double s = 0.0;
                for (int i = 0; i < k; i++) {
                    s += Lrowk[i] * Lrowj[i];
                }
                Lrowj[k] = s = (A.get(j, k) - s) / data.getDecompositionArraySelect(k, k);
                d = d + s * s;
                if (A.get(k, j) != A.get(j, k)) {
                    data.setSymAndPositive(false);
                }
            }
            d = A.get(j, j) - d;
            if (d <= 0.0)
                data.setSymAndPositive(false);
            data.setDecompositionArraySelect(j, j, Math.sqrt(Math.max(d, 0.0)));
            for (int k = j + 1; k < data.getDimension(); k++) {
                data.setDecompositionArraySelect(j, k, 0.0);
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
//        n = A.colCount();
//        R = new double[n][n];
//        isspd = (A.colCount() == n); // Main loop.
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
    public boolean isSymAndPositive() {
        return data.isSymAndPositive();
    }

    /**
     * @return decompositionArray triangular factor
     */
    public RM getDecompositionArray() {
        return SolidRM.wrap(data.getDecompositionArray());
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
        if (B.rowCount() != data.getDimension()) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!data.isSymAndPositive()) {
            throw new IllegalArgumentException("Matrix is not symmetric positive definite.");
        }

        // Copy right hand side.
        RM X = B.solidCopy();
        int nx = B.colCount();

        // Solve L*Y = B;
        for (int k = 0; k < data.getDimension(); k++) {
            for (int j = 0; j < nx; j++) {
                for (int i = 0; i < k; i++) {
                    X.set(k, j, X.get(k, j) - X.get(i, j) * data.getDecompositionArraySelect(k,i));
                }
                X.set(k, j, X.get(k, j) / data.getDecompositionArraySelect(k,k));
            }
        }

        // Solve L'*X = Y;
        for (int k = data.getDimension() - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                for (int i = k + 1; i < data.getDimension(); i++) {
                    X.set(k, j, X.get(k, j) - X.get(i, j) * data.getDecompositionArraySelect(i, k));
                }
                X.set(k, j, X.get(k, j) / data.getDecompositionArraySelect(k, k));
            }
        }

        return X;
    }
}
