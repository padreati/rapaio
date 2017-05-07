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
import rapaio.printer.Printable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * LU Decomposition.
 * <p>
 * For an m-by-n matrix A with m >= n, the LU decomposition is an m-by-n unit
 * lower triangular matrix L, an n-by-n upper triangular matrix U, and a
 * permutation var piv of length m so that A.mapRows(piv) = L*U. If m < n, then L
 * is m-by-m and U is m-by-n.
 * <p>
 * The LU decompostion with pivoting always exists, even if the matrix is
 * singular, so the constructor will never fail. The primary use of the LU
 * decomposition is in the solution of square systems of simultaneous linear
 * equations. This will fail if isNonSingular() returns false.
 */
public class LUDecomposition implements Serializable, Printable {

    private static final long serialVersionUID = -4226024886673558685L;

    private LUDecompositionData data = new LUDecompositionData();

	/**
     * LU Decomposition Structure to access L, U and piv.
     *
     * @param A input matrix
     */
    public static LUDecomposition from(RM A) {
        if(A.rowCount()<A.colCount())
            throw new IllegalArgumentException("for LU decomposition, rows must be greater or equal with cols.");
        return new LUDecomposition(A, Method.GAUSSIAN_ELIMINATION);
    }

    public static LUDecomposition from(RM A, Method method) {
        if(A.rowCount()<A.colCount())
            throw new IllegalArgumentException("for LU decomposition, rows must be greater or equal with cols.");
        return new LUDecomposition(A, method);
    }

    private LUDecomposition(RM A, Method method) {
        method.method().accept(this, A);
    }

    /**
     * Is the matrix nonsingular?
     *
     * @return true if U, and hence A, is nonsingular.
     */
    public boolean isNonSingular() {
        for (int j = 0; j < data.getColCount(); j++) {
            if (data.getLU().get(j, j) == 0) return false;
        }
        return true;
    }

    /**
     * Lower triangular factor matrix
     *
     * @return L lower triangular factor
     */
    public RM getL() {
        RM X = SolidRM.empty(data.getRowCount(), data.getColCount());
        for (int i = 0; i < data.getRowCount(); i++) {
            for (int j = 0; j <= i; j++) {
                if (i > j) {
                    X.set(i, j, data.getLU().get(i, j));
                } else if (i == j) {
                    X.set(i, j, 1.0);
                }
            }
        }
        return X;
    }

    /**
     * Upper triangular factor matrix
     *
     * @return U upper triangular factor
     */
    public RM getU() {
        RM U = SolidRM.empty(data.getColCount(), data.getColCount());
        for (int i = 0; i < data.getColCount(); i++) {
            for (int j = i; j < data.getColCount(); j++) {
                U.set(i, j, data.getLU().get(i, j));
            }
        }
        return U;
    }

    /**
     * Return pivot permutation var
     *
     * @return piv
     */
    public int[] getPivot() {
        return Arrays.copyOf(data.getPiv(), data.getRowCount());
    }

    /**
     * Computes determinant
     *
     * @return det(A)
     * @throws IllegalArgumentException Matrix must be square
     */
    public double det() {
        if (data.getRowCount() != data.getColCount()) {
            throw new IllegalArgumentException("Matrix must be square.");
        }
        double d = (double) data.getPivSign();
        for (int j = 0; j < data.getColCount(); j++) {
            d *= data.getLU().get(j, j);
        }
        return d;
    }

    /**
     * Solve A*X = B
     *
     * @param B A Matrix with as many rows as A and any number of columns.
     * @return X so that L*U*X = B(piv,:)
     * @throws IllegalArgumentException Matrix row dimensions must agree.
     * @throws RuntimeException         Matrix is singular.
     */
    public RM solve(RM B) {
        if (B.rowCount() != data.getRowCount()) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!this.isNonSingular()) {
            throw new RuntimeException("Matrix is singular.");
        }

        // Copy right hand side with pivoting
        int nx = B.colCount();
        RM X = B.mapRows(data.getPiv()).solidCopy();

        // Solve L*Y = B(piv,:)

        for (int k = 0; k < data.getColCount(); k++) {
            for (int i = k + 1; i < data.getColCount(); i++) {
                for (int j = 0; j < nx; j++) {
                    X.set(i, j, X.get(i, j) - X.get(k, j) * data.getLU().get(i, k));
                }
            }
        }

        // Solve U*X = Y;

        for (int k = data.getColCount() - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                X.set(k, j, X.get(k, j) / data.getLU().get(k, k));
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    X.set(i, j, X.get(i, j) - X.get(k, j) * data.getLU().get(i, k));
                }
            }
        }
        return X;
    }

    @Override
    public String summary() {

        StringBuilder sb = new StringBuilder();
        sb.append("LU decomposition summary\n");
        sb.append("========================\n");

        sb.append("\nL matrix\n").append(getL().summary());
        sb.append("\nU matrix:\n").append(getU().summary());
        return sb.toString();
    }

    public enum Method {

        /**
         * LU Decomposition, computed by Gaussian elimination. It computes L and U
         * with the "daxpy"-based elimination algorithm used in LINPACK and MATLAB.
         **/
        CROUT {
            @Override
            BiConsumer<LUDecomposition, RM> method() {
                return (lu, A) -> {
                    lu.data.setLU(A.solidCopy());
                    lu.data.setRowCount(A.rowCount());
                    lu.data.setColCount(A.colCount());
                    lu.data.setPiv(new int[lu.data.getRowCount()]);
                    for (int i = 0; i < lu.data.getRowCount(); i++) {
                        lu.data.getPiv()[i] = i;
                    }
                    lu.data.setPivSign(1);
                    double[] LUcolj = new double[lu.data.getRowCount()];

                    // Outer loop.
                    for (int j = 0; j < lu.data.getColCount(); j++) {

                        // Make a copy of the j-th column to localize references.
                        for (int i = 0; i < lu.data.getRowCount(); i++) {
                            LUcolj[i] = lu.data.getLU().get(i, j);
                        }

                        // Apply previous transformations.
                        for (int i = 0; i < lu.data.getRowCount(); i++) {


                            // Most of the time is spent in the following dot product.

                            int kmax = Math.min(i, j);
                            double s = 0.0;
                            for (int k = 0; k < kmax; k++) {
                                s += lu.data.getLU().get(i, k) * LUcolj[k];
                            }
                            LUcolj[i] -= s;
                            lu.data.getLU().set(i, j, LUcolj[i]);
                        }

                        // Find pivot and exchange if necessary.

                        int p = j;
                        for (int i = j + 1; i < lu.data.getLU().rowCount(); i++) {
                            if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
                                p = i;
                            }
                        }
                        if (p != j) {
                            for (int k = 0; k < lu.data.getLU().colCount(); k++) {
                                double t = lu.data.getLU().get(p, k);
                                lu.data.getLU().set(p, k, lu.data.getLU().get(j, k));
                                lu.data.getLU().set(j, k, t);
                            }
                            int k = lu.data.getPiv()[p];
                            lu.data.getPiv()[p] = lu.data.getPiv()[j];
                            lu.data.getPiv()[j] = k;
                            lu.data.setPivSign(-lu.data.getPivSign());
                        }

                        // Compute multipliers.
                        if (j < lu.data.getLU().rowCount() & lu.data.getLU().get(j, j) != 0.0) {
                            for (int i = j + 1; i < lu.data.getLU().rowCount(); i++) {
                                lu.data.getLU().set(i, j, lu.data.getLU().get(i, j) / lu.data.getLU().get(j, j));
                            }
                        }
                    }
                };
            }
        },

        /**
         * LU Decomposition, computed by Gaussian elimination. It computes L and U
         * with the "daxpy"-based elimination algorithm used in LINPACK and MATLAB.
         */
        GAUSSIAN_ELIMINATION {
            @Override
            BiConsumer<LUDecomposition, RM> method() {
                return (lu, A) -> {

                    // Initialize.
                    lu.data.setLU(A.solidCopy());
                    lu.data.setRowCount(A.rowCount());
                    lu.data.setColCount(A.colCount());
                    lu.data.setPiv(new int[lu.data.getRowCount()]);
                    for (int i = 0; i < lu.data.getRowCount(); i++) {
                        lu.data.getPiv()[i] = i;
                    }
                    lu.data.setPivSign(1);
                    // Main loop.
                    for (int k = 0; k < lu.data.getColCount(); k++) {
                        // Find pivot.
                        int p = k;
                        for (int i = k + 1; i < lu.data.getRowCount(); i++) {
                            if (Math.abs(lu.data.getLU().get(i, k)) > Math.abs(lu.data.getLU().get(p, k))) {
                                p = i;
                            }
                        }
                        // Exchange if necessary.
                        if (p != k) {
                            for (int j = 0; j < lu.data.getColCount(); j++) {
                                double t = lu.data.getLU().get(p, j);
                                lu.data.getLU().set(p, j, lu.data.getLU().get(k, j));
                                lu.data.getLU().set(k, j, t);
                            }
                            int t = lu.data.getPiv()[p];
                            lu.data.getPiv()[p] = lu.data.getPiv()[k];
                            lu.data.getPiv()[k] = t;
                            lu.data.setPivSign(-lu.data.getPivSign());
                        }
                        // Compute multipliers and eliminate k-th column.
                        if (lu.data.getLU().get(k, k) != 0.0) {
                            for (int i = k + 1; i < lu.data.getRowCount(); i++) {
                                lu.data.getLU().set(i, k, lu.data.getLU().get(i, k) / lu.data.getLU().get(k, k));
                                for (int j = k + 1; j < lu.data.getColCount(); j++) {
                                    lu.data.getLU().set(i, j, lu.data.getLU().get(i, j) - lu.data.getLU().get(i, k) * lu.data.getLU().get(k, j));
                                }
                            }
                        }
                    }
                };
            }
        };

        abstract BiConsumer<LUDecomposition, RM> method();
    }
}
