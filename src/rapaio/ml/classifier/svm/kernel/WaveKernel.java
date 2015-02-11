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
 */

package rapaio.ml.classifier.svm.kernel;

import rapaio.data.Frame;

/**
 * The Wavelet kernel (Zhang et al, 2004) comes from Wavelet theory and is given as:
 * <p>
 * k(x,y) = \prod_{i=1}^N h(\frac{x_i-c_i}{a}) \: h(\frac{y_i-c_i}{a})
 * <p>
 * Where a and c are the wavelet dilation and translation coefficients, respectively
 * (the form presented above is a simplification, please see the original paper for
 * details). A translation-invariant version of this kernel can be given as:
 * <p>
 * k(x,y) = \prod_{i=1}^N h(\frac{x_i-y_i}{a})
 * <p>
 * Where in both h(x) denotes a mother wavelet function. In the paper by Li Zhang,
 * Weida Zhou, and Licheng Jiao, the authors suggests a possible h(x) as:
 * <p>
 * h(x) = cos(1.75x)exp(-\frac{x^2}{2})
 * <p>
 * Which they also prove as an admissible kernel function.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */
public class WaveKernel extends AbstractKernel {

    private final double theta;

    public WaveKernel() {
        this(1.0);
    }

    public WaveKernel(double theta) {
        this.theta = theta;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double dot = dotProd(df1, row1, df2, row2);
        return theta * Math.sin(dot / theta) / dot;
    }

    @Override
    public Kernel newInstance() {
        return new WaveKernel(theta);
    }
}
