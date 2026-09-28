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

import java.io.Serial;

import rapaio.darray.DArray;
import rapaio.printer.Format;

/**
 * The Wave kernel is a symmetric positive semi-definite kernel given by:
 * <p>
 * k(x,y) = \frac{\theta}{\lVert x-y \rVert} \sin\left(\frac{\lVert x-y \rVert}{\theta}\right)
 * <p>
 * Since sin(z)/z tends to one as z tends to zero, k(x, x) = 1.
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */
public class WaveKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = 3332090004050972059L;

    private final double theta;

    public WaveKernel() {
        this(1.0);
    }

    public WaveKernel(double theta) {
        this.theta = theta;
    }

    @Override
    public double compute(DArray<Double> v, DArray<Double> u) {
        double norm = deltaNorm(v, u);
        if (norm == 0) {
            return 1;
        }
        return theta * Math.sin(norm / theta) / norm;
    }

    @Override
    public Kernel newInstance() {
        return new WaveKernel(theta);
    }

    @Override
    public String name() {
        return "Wave(theta=" + Format.floatFlex(theta) + ")";
    }
}
