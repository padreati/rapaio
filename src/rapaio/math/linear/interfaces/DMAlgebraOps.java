package rapaio.math.linear.interfaces;

import rapaio.math.linear.DM;
import rapaio.math.linear.DV;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/20/20.
 */
public interface DMAlgebraOps {
    /**
     * Computes matrix vector multiplication.
     *
     * @param b vector to be multiplied with
     * @return result vector
     */
    DV dot(DV b);

    /**
     * Compute matrix multiplication between the current
     * matrix and the diagonal matrix obtained from the given vector.
     * <p>
     * A * I * v
     *
     * @param v diagonal vector
     * @return result matrix
     */
    DM dotDiag(DV v);

    /**
     * Compute matrix multiplication between the current
     * matrix and the diagonal matrix obtained from the given vector.
     * <p>
     * v^T * I * A
     *
     * @param v diagonal vector
     * @return result matrix
     */
    DM dotDiagT(DV v);

    /**
     * Computes matrix - matrix multiplication.
     *
     * @param b matrix to be multiplied with
     * @return matrix result
     */
    DM dot(DM b);
}
