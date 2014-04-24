/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

package rapaio.data.matrix;

import rapaio.core.RandomSource;

/**
 * Matrix operations
 * <p>
 *
 * @author The MathWorks, Inc. and the National Institute of Standards and Technology.
 * @version 5 August 1998
 *          User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class MathMatrix {

    /**
     * Computes matrix sum of given input matrices
     *
     * @param A list of matrices
     * @return sum of As
     */
    public static Matrix plus(Matrix... A) {
        if (A.length == 0) {
            throw new IllegalArgumentException("can't add zero matrices");
        }
        for (int i = 1; i < A.length; i++) {
            if (A[i - 1].m != A[i].m || A[i - 1].n != A[i].n) {
                throw new IllegalArgumentException("Added matrices must have same dimensions");
            }
        }
        Matrix C = new Matrix(A[0].m, A[0].n);
        for (int i = 0; i < C.m; i++) {
            for (int j = 0; j < C.n; j++) {
                double sum = 0.;
                for (Matrix aA : A) {
                    sum += aA.get(i, j);
                }
                C.set(i, j, sum);
            }
        }
        return C;
    }

    public static Matrix minus(Matrix A, Matrix B) {
        if (A.m != B.m || A.n != A.n) {
            throw new IllegalArgumentException("Matrices must have the same dimensions");
        }
        Matrix C = new Matrix(A.m, A.n);
        for (int i = 0; i < C.m; i++) {
            for (int j = 0; j < C.n; j++) {
                C.set(i, j, A.get(i, j) - B.get(i, j));
            }
        }
        return C;
    }

    /**
     * Unary minus
     *
     * @return -A
     */
    public Matrix uminus(Matrix A) {
        Matrix C = new Matrix(A.m, A.n);
        for (int i = 0; i < C.m; i++) {
            for (int j = 0; j < C.n; j++) {
                C.set(i, j, -A.get(i, j));
            }
        }
        return C;
    }

    /**
     * Computes the transpose of a given matrix
     *
     * @param A given matrix
     * @return A^T transpose of A
     */
    public static Matrix t(Matrix A) {
        Matrix X = new Matrix(A.n, A.m);
        for (int i = 0; i < X.m; i++) {
            for (int j = 0; j < X.n; j++) {
                X.set(i, j, A.get(j, i));
            }
        }
        return X;
    }

    /**
     * Matrix scalar multiplication
     *
     * @param A     given matrix
     * @param value given scalar
     * @return getValue*A
     */
    public static Matrix times(Matrix A, double value) {
        Matrix C = new Matrix(A.m, A.n);
        for (int i = 0; i < C.m; i++) {
            for (int j = 0; j < C.n; j++) {
                C.set(i, j, A.get(i, j) * value);
            }
        }
        return C;
    }

    /**
     * Matrix multiplication, A * B
     *
     * @param A first matrix
     * @param B second matrix
     * @return matrix product, A * B
     * @throws IllegalArgumentException Matrix inner dimensions must agree.
     */
    public static Matrix times(Matrix A, Matrix B) {
        if (B.m != A.n) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        Matrix C = new Matrix(A.m, B.n);
        double[] BCol = new double[A.n];
        for (int j = 0; j < B.n; j++) {
            for (int k = 0; k < A.n; k++) {
                BCol[k] = B.get(k, j);
            }
            for (int i = 0; i < A.m; i++) {
                double s = 0;
                for (int k = 0; k < A.n; k++) {
                    s += A.get(i, k) * BCol[k];
                }
                C.set(i, j, s);
            }
        }
        return C;
    }

    /**
     * Generate identity matrix
     *
     * @param m Number of getRowCount.
     * @param n Number of colums.
     * @return An m-by-n matrix with ones on the diagonal and zeros elsewhere.
     */
    public static Matrix identity(int m, int n) {
        Matrix A = new Matrix(m, n);
        for (int i = 0; i < m; i++) {
            A.set(i, i, 1.0);
        }
        return A;
    }

    /**
     * Generate matrix with random elements
     *
     * @param m Number of getRowCount.
     * @param n Number of colums.
     * @return An m-by-n matrix with uniformly distributed random elements.
     */

    public static Matrix random(int m, int n) {
        Matrix A = new Matrix(m, n);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A.set(i, j, RandomSource.nextDouble());
            }
        }
        return A;
    }


    /**
     * One norm
     *
     * @return maximum column sum.
     */

    public double norm1(Matrix A) {
        double f = 0;
        for (int j = 0; j < A.n; j++) {
            double s = 0;
            for (int i = 0; i < A.m; i++) {
                s += Math.abs(A.get(i, j));
            }
            f = Math.max(f, s);
        }
        return f;
    }


    /**
     * Two norm
     *
     * @return maximum singular getValue.
     */
    public double norm2(Matrix A) {
        return (new SingularValueDecomposition(A).norm2());
    }

    /**
     * Infinity norm
     *
     * @return maximum row sum.
     */

    public double normInf(Matrix A) {
        double f = 0;
        for (int i = 0; i < A.m; i++) {
            double s = 0;
            for (int j = 0; j < A.n; j++) {
                s += Math.abs(A.get(i, j));
            }
            f = Math.max(f, s);
        }
        return f;
    }

    /**
     * Frobenius norm
     *
     * @return sqrt of sum of squares of all elements.
     */

    public static double normF(Matrix A) {
        double f = 0;
        for (int i = 0; i < A.m; i++) {
            for (int j = 0; j < A.n; j++) {
                f = StrictMath.hypot(f, A.get(i, j));
            }
        }
        return f;
    }

    /**
     * Matrix inverse or pseudoinverse
     *
     * @return inverse(A) if A is square, pseudoinverse otherwise.
     */

    public Matrix inverse(Matrix A) {
        return solve(A, identity(A.m, A.m));
    }

    /**
     * Matrix determinant
     *
     * @return determinant
     */
    public double det(Matrix A) {
        return new LUDecomposition(A).det();
    }

    /**
     * Matrix rank
     *
     * @return effective numerical rank, obtained from SVD.
     */
    public int rank(Matrix A) {
        return new SingularValueDecomposition(A).rank();
    }

    /**
     * Matrix condition (2 norm)
     *
     * @return ratio of largest to smallest singular getValue.
     */
    public double cond(Matrix A) {
        return new SingularValueDecomposition(A).cond();
    }

    /**
     * Matrix trace.
     *
     * @return sum of the diagonal elements.
     */
    public double trace(Matrix A) {
        double t = 0;
        for (int i = 0; i < Math.min(A.m, A.n); i++) {
            t += A.get(i, i);
        }
        return t;
    }

    /**
     * Solve A*X = B
     *
     * @param B right hand side
     * @return solution if A is square, least squares solution otherwise
     */
    public Matrix solve(Matrix A, Matrix B) {
        return (A.m == A.n ? (new LUDecomposition(A)).solve(B) :
                (new QRDecomposition(A)).solve(B));
    }

    /**
     * Solve X*A = B, which is also A'*X' = B'
     *
     * @param B right hand side
     * @return solution if A is square, least squares solution otherwise.
     */
    public Matrix solveTranspose(Matrix A, Matrix B) {
        return solve(t(A), t(B));
    }

    private void checkMatrixDimensions(Matrix A, Matrix B) {
        if (B.m != A.m || B.n != A.n) {
            throw new IllegalArgumentException("Matrix dimensions must agree.");
        }
    }

//    /**
//     * Element-by-element multiplication, C = A.*B
//     *
//     * @param B another matrix
//     * @return A.*B
//     */
//
//    public Matrix arrayTimes(Matrix B) {
//        checkMatrixDimensions(B);
//        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
//        for (int i = 0; i < m; i++) {
//            for (int j = 0; j < n; j++) {
//                C[i][j] = A[i][j] * B.A[i][j];
//            }
//        }
//        return X;
//    }
//
//    /**
//     * Element-by-element right division, C = A./B
//     *
//     * @param B another matrix
//     * @return A./B
//     */
//
//    public Matrix arrayRightDivide(Matrix B) {
//        checkMatrixDimensions(B);
//        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
//        for (int i = 0; i < m; i++) {
//            for (int j = 0; j < n; j++) {
//                C[i][j] = A[i][j] / B.A[i][j];
//            }
//        }
//        return X;
//    }
//
//
//    /**
//     * Element-by-element left division, C = A.\B
//     *
//     * @param B another matrix
//     * @return A.\B
//     */
//
//    public Matrix arrayLeftDivide(Matrix A, Matrix B) {
//        checkMatrixDimensions(A, B);
//        Matrix C = new Matrix(A.m, A.n);
//        for (int i = 0; i < A.m; i++) {
//            for (int j = 0; j < A.n; j++) {
//                C.set(i,j, B.A[i][j] / A[i][j];
//            }
//        }
//        return X;
//    }

}
