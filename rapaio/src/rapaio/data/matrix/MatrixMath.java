package rapaio.data.matrix;

/**
 * Matrix operations
 * <p/>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class MatrixMath {

    /**
     * Linear algebraic matrix multiplication, A * B
     *
     * @param A first matrix
     * @param B second matrix
     * @return matrix product, A * B
     * @throws IllegalArgumentException Matrix inner dimensions must agree.
     */

    public Matrix times(Matrix A, Matrix B) {
        if (B.m != A.n) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        Matrix C = new Matrix(A.m, B.n);
        double[] Bcolj = new double[A.n];
        for (int j = 0; j < B.n; j++) {
            for (int k = 0; k < A.n; k++) {
                Bcolj[k] = B.get(k, j);
            }
            for (int i = 0; i < A.m; i++) {
                double s = 0;
                for (int k = 0; k < A.n; k++) {
                    s += A.get(i, k) * Bcolj[k];
                }
                C.set(i, j, s);
            }
        }
        return C;
    }
}
