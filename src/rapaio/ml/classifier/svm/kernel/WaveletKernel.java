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

package rapaio.ml.classifier.svm.kernel;

import rapaio.data.Frame;
import rapaio.sys.WS;

import java.util.function.Function;

/**
 * The Wavelet kernel (Zhang et al, 2004) comes from Wavelet theory and is given as:
 * <p>
 * k(x,y) = \prod_{i=1}^N h(\frac{x_i-c_i}{a}) \: h(\frac{y_i-c_i}{a})
 * <p>
 * Where a and c are the wavelet dilation and translation coefficients,
 * respectively (the form presented above is a simplification, please see
 * the original paper for details). A translation-invariant version of this
 * kernel can be given as:
 * <p>
 * k(x,y) = \prod_{i=1}^N h(\frac{x_i-y_i}{a})
 * <p>
 * Where in both h(x) denotes a mother wavelet function. In the paper
 * by Li Zhang, Weida Zhou, and Licheng Jiao, the authors suggests a
 * possible h(x) as:
 * <p>
 * h(x) = cos(1.75x)exp(-\frac{x^2}{2})
 * <p>
 * Which they also prove as an admissible kernel function.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
public class WaveletKernel extends AbstractKernel {

    private static final long serialVersionUID = -3640571660076086354L;

    private final boolean invariant;
    private final double dilation;
    private final double translation;
    private final Function<Double, Double> wavelet;

    public WaveletKernel(double dilation) {
        this(true, dilation, 0, x -> Math.cos(1.75 * x) * Math.exp(-x * x / 2));
    }

    public WaveletKernel(boolean invariant, double dilation, double translation) {
        this(invariant, dilation, translation, x -> Math.cos(1.75 * x) * Math.exp(-x * x / 2));
    }

    public WaveletKernel(boolean invariant, double dilation, double translation, Function<Double, Double> wavelet) {
        this.invariant = invariant;
        this.dilation = dilation;
        this.translation = translation;
        this.wavelet = wavelet;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double result = 1;
        for (String varName : varNames) {
            if (invariant) {
                double diff = df1.getDouble(row1, varName) - df2.getDouble(row2, varName);
                result *= wavelet.apply(diff / dilation);
            } else {
                result *= wavelet.apply((df1.getDouble(row1, varName) - translation) / dilation);
                result *= wavelet.apply((df2.getDouble(row2, varName) - translation) / dilation);
            }
        }
        return result;
    }

    @Override
    public Kernel newInstance() {
        return new WaveletKernel(invariant, dilation, translation, wavelet);
    }

    @Override
    public String name() {
        return "Wavelet(invariant=" + invariant +
                ",dilation=" + WS.formatFlex(dilation) +
                ",translation=" + WS.formatFlex(translation) +
                ")";
    }
}
