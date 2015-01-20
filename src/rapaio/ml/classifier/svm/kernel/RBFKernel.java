/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.classifier.svm.kernel;

import rapaio.data.Frame;

/**
 * The Gaussian kernel is an example of radial basis function kernel.
 *
 *      K(x,y) = exp( -(<x-y, x-y>^2) / (2*sigma) )
 *
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */
public class RBFKernel extends AbstractKernel {

    private final double sigma;
    private final double factor;

    public RBFKernel() {
        this(7);
    }

    public RBFKernel(double sigma) {
        this.sigma = sigma;
        this.factor = 1.0 / (2.0 * sigma * sigma);
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double value = deltaDotProd(df1, row1, df2, row2);
        return 1.0 / Math.pow(Math.E, factor * value * value);
    }
}
