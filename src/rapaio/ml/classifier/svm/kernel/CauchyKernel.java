/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.ml.classifier.svm.kernel;

import rapaio.data.Frame;

/**
 * The Cauchy kernel comes from the Cauchy distribution (Basak, 2008).
 * It is a long-tailed kernel and can be used to give long-range influence
 * and sensitivity over the high dimension space.
 * <p>
 * k(x, y) = \frac{1}{1 + \frac{\lVert x-y \rVert^2}{\sigma^2} }
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
@Deprecated
public class CauchyKernel extends AbstractKernel {

    private final double sigma;

    /**
     * The Cauchy kernel comes from the Cauchy distribution (Basak, 2008).
     * It is a long-tailed kernel and can be used to give long-range influence
     * and sensitivity over the high dimension space.
     * <p>
     * k(x, y) = \frac{1}{1 + \frac{\lVert x-y \rVert^2}{\sigma^2} }
     *
     * @param sigma sigma value
     */
    public CauchyKernel(double sigma) {
        this.sigma = sigma;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double dot = deltaDotProd(df1, row1, df2, row2);
        return 1.0 / (1.0 + Math.pow(dot / sigma, 2));
    }

    @Override
    public Kernel newInstance() {
        return new CauchyKernel(sigma);
    }
}
