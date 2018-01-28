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
 * The Power kernel is also known as the (unrectified) triangular kernel.
 * It is an example of scale-invariant kernel (Sahbi and Fleuret, 2004)
 * and is also only conditionally positive definite.
 * <p>
 * k(x,y) = - \lVert x-y \rVert ^d
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class PowerKernel extends AbstractKernel {

    private static final long serialVersionUID = -974630838457936489L;

    private final double degree;

    public PowerKernel(double degree) {
        this.degree = degree;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        return -Math.pow(deltaDotProd(df1, row1, df2, row2), degree);
    }

    @Override
    public Kernel newInstance() {
        return new PowerKernel(degree);
    }

    @Override
    public String name() {
        return "Power(degree=" + WS.formatFlex(degree) + ")";
    }
}
