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
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

public abstract class DLUDecomposition implements Serializable, Printable {

    public enum Method {
        CROUT, GAUSSIAN_ELIMINATION
    }
    protected final DMatrix ref;
    protected final Method method;

    public DLUDecomposition(DMatrix ref, Method method) {
        if (ref.rows() < ref.cols()) {
            throw new IllegalArgumentException("For LU decomposition, number of rows must be greater or equal with number of columns.");
        }
        this.ref = ref;
        this.method = method;
    }

    public DMatrix ref() {
        return ref;
    }

    public Method method() {
        return method;
    }

    /**
     * Is the matrix nonsingular?
     *
     * @return true if U, and hence A, is nonsingular.
     */
    public abstract boolean isNonSingular();

    /**
     * @return L lower triangular factor matrix
     */
    public abstract DMatrix l();

    /**
     * @return U upper triangular factor matrix
     */
    public abstract DMatrix u();

    /**
     * Return pivot permutation var
     *
     * @return piv
     */
    public abstract int[] getPivot();

    /**
     * Computes determinant
     *
     * @return det(A)
     * @throws IllegalArgumentException Matrix must be square
     */
    public abstract double det();

    /**
     * Solve A*X = B
     *
     * @param B A Matrix with as many rows as A and any number of columns.
     * @return X so that L*U*X = B(piv,:)
     * @throws IllegalArgumentException Matrix row dimensions must agree.
     * @throws RuntimeException         Matrix is singular.
     */
    public abstract DMatrix solve(DMatrix B);

    public DMatrix inv() {
        return solve(DMatrix.identity(ref.rows()));
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {

        return "LU decomposition summary\n"
                + "========================\n"
                + "\nL matrix\n" + l().toSummary(printer, options)
                + "\nU matrix:\n" + u().toSummary(printer, options);
    }
}
