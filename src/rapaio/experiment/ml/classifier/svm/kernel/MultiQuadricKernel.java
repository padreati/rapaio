/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.experiment.ml.classifier.svm.kernel;

import rapaio.data.Frame;
import rapaio.printer.format.Format;

/**
 * The Multiquadric kernel can be used in the same situations as the Rational Quadratic kernel.
 * As is the case with the Sigmoid kernel, it is also an example of an
 * non-positive definite kernel.
 * <p>
 * k(x, y) = \sqrt{\lVert x-y \rVert^2 + c^2}
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class MultiQuadricKernel extends AbstractKernel {

    private static final long serialVersionUID = -4215277675823113044L;

    private final double c;

    public MultiQuadricKernel(double c) {
        this.c = c;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double dot = deltaDotProd(df1, row1, df2, row2);
        return Math.sqrt(dot * dot + c * c);
    }

    @Override
    public Kernel newInstance() {
        return new MultiQuadricKernel(c);
    }

    @Override
    public String name() {
        return "MultiQuadratic(c=" + Format.floatFlex(c) + ")";
    }
}
