/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.sys.WS;

/**
 * Inverse Multiquadric Kernel
 * <p>
 * The Inverse Multi Quadric kernel. As with the GaussianPdf kernel,
 * it results in a kernel matrix with full rank (Micchelli, 1986)
 * and thus forms a infinite dimension feature space.
 * <p>
 * k(x, y) = \frac{1}{\sqrt{\lVert x-y \rVert^2 + \theta^2}}
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class InverseMultiQuadraticKernel extends AbstractKernel {

    private static final long serialVersionUID = -2377890141986212381L;

    private final double c;

    public InverseMultiQuadraticKernel(double c) {
        this.c = c;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double dot = deltaDotProd(df1, row1, df2, row2);
        return 1.0 / Math.sqrt(dot * dot + c * c);
    }

    @Override
    public Kernel newInstance() {
        return new InverseMultiQuadraticKernel(c);
    }

    @Override
    public String name() {
        return "InverseMultiQuadratic(c=" + WS.formatFlex(c) + ")";
    }
}
