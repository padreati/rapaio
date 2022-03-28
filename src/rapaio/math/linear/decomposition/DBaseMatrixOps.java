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

import rapaio.math.linear.DMatrix;

public class DBaseMatrixOps implements MatrixOps {

    private final DMatrix ref;

    public DBaseMatrixOps(DMatrix ref) {
        this.ref = ref;
    }

    @Override
    public DCholeskyDecomposition cholesky() {
        return cholesky(false);
    }

    @Override
    public DCholeskyDecomposition cholesky(boolean rightFlag) {
        return new DBaseCholeskyDecomposition(ref, rightFlag);
    }

    @Override
    public DLUDecomposition lu() {
        return lu(DLUDecomposition.Method.GAUSSIAN_ELIMINATION);
    }

    @Override
    public DLUDecomposition lu(DLUDecomposition.Method method) {
        return new DBaseLUDecomposition(ref, method);
    }
}
