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
import rapaio.sys.WS;

/**
 * The Hyperbolic Tangent Kernel is also known as the Sigmoid Kernel and as
 * the Multilayer Perceptron (MLP) kernel. The Sigmoid Kernel comes from the
 * Neural Networks field, where the bipolar sigmoid function is often used as
 * an activation function for artificial neurons.
 * <p>
 * k(x, y) = \tanh (\alpha x^T y + c)
 * <p>
 * It is interesting to note that a SVM model using a sigmoid kernel function
 * is equivalent to a two-layer, perceptron neural network. This kernel was
 * quite popular for support vector machines due to its origin from neural
 * network theory. Also, despite being only conditionally positive definite,
 * it has been found to perform well in practice.
 * <p>
 * There are two adjustable parameters in the sigmoid kernel, the slope alpha
 * and the intercept constant c. A common value for alpha is 1/N,
 * where N is the data dimension.
 * <p>
 * A more detailed study on sigmoid kernels can be found in the
 * works by Hsuan-Tien and Chih-Jen.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class SigmoidKernel extends AbstractKernel {

    private static final long serialVersionUID = 7321024091559311770L;

    private final double alpha;
    private final double c;

    public SigmoidKernel(double alpha, double c) {
        this.alpha = alpha;
        this.c = c;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        return Math.atan(alpha * dotProd(df1, row1, df2, row2) + c);
    }

    @Override
    public Kernel newInstance() {
        return new SigmoidKernel(alpha, c);
    }

    @Override
    public String name() {
        return "Sigmoid(alpha=" + WS.formatFlex(alpha) + ",c=" + WS.formatFlex(c) + ")";
    }
}
