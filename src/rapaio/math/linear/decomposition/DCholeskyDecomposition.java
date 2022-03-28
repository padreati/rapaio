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

import java.io.Serializable;

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.printer.Printable;

/**
 * Cholesky Decomposition.
 * <p>
 * For a symmetric, positive definite matrix A, the Cholesky decomposition is a lower triangular matrix L so that A = L*L'.
 * <p>
 * If the matrix is not symmetric or positive definite, the constructor returns a partial decomposition and sets an internal
 * flag that may be queried by the {@code #isSPD()} method.
 */
public abstract class DCholeskyDecomposition implements Serializable {

    protected final DMatrix ref;
    protected final boolean rightFlag;
    protected boolean spd = false;

    public DCholeskyDecomposition(DMatrix ref, boolean rightFlag) {
        if (ref.rows() != ref.cols()) {
            throw new IllegalArgumentException("Only square matrices can have Cholesky decomposition.");
        }
        this.ref = ref;
        this.rightFlag = rightFlag;
    }

    /**
     * @return true if A is symmetric and positive definite.
     */
    public boolean isSPD() {
        return spd;
    }

    public boolean hasRightFlag() {
        return rightFlag;
    }

    /**
     * @return L triangular factor
     */
    public abstract DMatrix l();

    /**
     * @return R triangular decomposition
     */
    public abstract DMatrix r();

    /**
     * Solve A*X = B
     *
     * @param b A Matrix with as many rows as A and any number of columns.
     * @return X so that L*L'*X = B
     * @throws IllegalArgumentException Matrix row dimensions must agree.
     * @throws IllegalArgumentException Matrix is not symmetric positive definite.
     */
    public abstract DMatrix solve(DMatrix b);

    /**
     * Solve A*x=b linear system when A is symmetric positive definite.
     *
     * @param b result vector
     * @return coefficient vector
     */
    public DVector solve(DVector b) {
        return solve(b.asMatrix()).mapCol(0);
    }

    /**
     * Computes the inverse of a symmetric positive definite matrix.
     *
     * @return inverse of the original matrix
     */
    public abstract DMatrix inv();
}
