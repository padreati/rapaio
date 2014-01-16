package rapaio.data.matrix;

/**
 * Matrix operations
 * <p/>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class MatrixMath {

    /**
     * Computes matrix sum of given input matrices
     *
     * @param A list of matrices
     * @return sum of As
     */
    public static Matrix add(Matrix... A) {
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
                for (int k = 0; k < A.length; k++) {
                    sum += A[k].get(i, j);
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
     * @return value*A
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
     * @param m Number of rowCount.
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

}
