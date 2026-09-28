/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.ml.common.kernel;

import java.io.Serializable;

import rapaio.darray.DArray;

/**
 * Kernel function interface
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */
public interface Kernel extends Serializable {

    Kernel newInstance();

    String name();

    /**
     * True when the kernel is the inner product up to an additive constant, {@code k(x, y) = x'y + c}.
     * Consumers use it to keep an explicit weight vector {@code w = sum(alpha_i y_i x_i)} and predict with
     * {@code w'x}, which is exact only under that form (the constant cancels because {@code sum(alpha_i y_i) = 0}).
     * A scaled inner product or a polynomial of degree one with slope different from one must return false.
     *
     * @return true if the kernel is the plain inner product plus a constant
     */
    boolean isLinear();

    double compute(DArray<Double> v, DArray<Double> u);
}
