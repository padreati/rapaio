/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import java.io.Serial;

import rapaio.narray.NArray;
import rapaio.printer.Format;

/**
 * The Linear kernel is the simplest kernel function. It is given by the inner product <x,y> plus an optional constant c.
 * Kernel algorithms using a linear kernel are often equivalent to their non-kernel counterparts,
 * i.e. KPCA with linear kernel is the same as standard PCA.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/14/21.
 */
public class LinearKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -4046108952113023779L;

    private final double c;

    public LinearKernel() {
        this(0);
    }

    public LinearKernel(double c) {
        this.c = c;
    }

    @Override
    public Kernel newInstance() {
        return new LinearKernel(c);
    }

    @Override
    public String name() {
        return "LinearKernel(c="+Format.floatFlex(c)+")";
    }

    @Override
    public double compute(NArray<Double> v, NArray<Double> u) {
        return v.inner(u) + c;
    }
}
