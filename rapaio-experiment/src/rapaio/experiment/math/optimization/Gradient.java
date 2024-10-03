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

package rapaio.experiment.math.optimization;

import rapaio.experiment.math.linear.DVector;
import rapaio.math.MathTools;
import rapaio.util.Pair;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/24/15.
 */
@Deprecated
public interface Gradient {

    /**
     * Compute the gradient and loss given the features of a single data point.
     *
     * @param data    features for one data point
     * @param label   label for this data point
     * @param weights weights/coefficients corresponding to features
     * @return Pair(Vector gradient, Double loss)
     */
    default Pair<DVector, Double> compute(DVector data, double label, DVector weights) {
        DVector gradient = DVector.zeros(weights.size());
        Double loss = compute(data, label, weights, gradient);
        return Pair.from(gradient, loss);
    }

    /**
     * Compute the gradient and loss given the features of a single data point,
     * add the gradient to a provided vector to avoid creating new objects, and return loss.
     *
     * @param data        features for one data point
     * @param label       label for this data point
     * @param weights     weights/coefficients corresponding to features
     * @param cumGradient the computed gradient will be added to this vector
     * @return loss
     */
    Double compute(DVector data, double label, DVector weights, DVector cumGradient);
}

/**
 * Compute gradient and loss for a Least-squared loss function, as used in linear regression.
 * This is correct for the averaged least squares loss function (mean squared error)
 * L = 1/2n ||A weights-y||^2
 * See also the documentation for the precise formulation.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/24/15.
 */
@Deprecated
class LeastSquareGradient implements Gradient {

    @Override
    public Pair<DVector, Double> compute(DVector data, double label, DVector weights) {
        double diff = data.dot(weights) - label;
        double loss = diff * diff / 2.0;
        DVector gradient = data.copy();
        gradient.mul(diff);
        return Pair.from(gradient, loss);
    }

    @Override
    public Double compute(DVector data, double label, DVector weights, DVector cumGradient) {
        double diff = data.dot(weights) - label;
        cumGradient.add(data.copy().mul(diff));
        return diff * diff / 2.0;
    }
}

/**
 * /**
 * Compute gradient and loss for a multinomial logistic loss function, as used
 * in multi-class classification (it is also used in binary logistic regression).
 * <p>
 * In `The Elements of Statistical Learning: Data Mining, Inference, and Prediction, 2nd Edition`
 * by Trevor Hastie, Robert Tibshirani, and Jerome Friedman, which can be downloaded from
 * http://statweb.stanford.edu/~tibs/ElemStatLearn/ , Eq. (4.17) on page 119 gives the formula of
 * multinomial logistic regression model. A simple calculation shows that
 * <p>
 * {{{
 * P(y=0|x, w) = 1 / (1 + \sum_i^{K-1} \exp(x w_i))
 * P(y=1|x, w) = exp(x w_1) / (1 + \sum_i^{K-1} \exp(x w_i))
 * ...
 * P(y=K-1|x, w) = exp(x w_{K-1}) / (1 + \sum_i^{K-1} \exp(x w_i))
 * }}}
 * <p>
 * for K classes multiclass classification problem.
 * <p>
 * The model weights w = (w_1, w_2, ..., w_{K-1})^T becomes a matrix which has dimension of
 * (K-1) * (N+1) if the intercepts are added. If the intercepts are not added, the dimension
 * will be (K-1) * N.
 * <p>
 * As a result, the loss of objective function for a single instance of data can be written as
 * {{{
 * l(w, x) = -log P(y|x, w) = -\alpha(y) log P(y=0|x, w) - (1-\alpha(y)) log P(y|x, w)
 * = log(1 + \sum_i^{K-1}\exp(x w_i)) - (1-\alpha(y)) x w_{y-1}
 * = log(1 + \sum_i^{K-1}\exp(margins_i)) - (1-\alpha(y)) margins_{y-1}
 * }}}
 * <p>
 * where \alpha(i) = 1 if i != 0, and
 * \alpha(i) = 0 if i == 0,
 * margins_i = x w_i.
 * <p>
 * For optimization, we have to calculate the first derivative of the loss function, and
 * a simple calculation shows that
 * <p>
 * {{{
 * \frac{\partial l(w, x)}{\partial w_{ij}}
 * = (\exp(x w_i) / (1 + \sum_k^{K-1} \exp(x w_k)) - (1-\alpha(y)\delta_{y, i+1})) * x_j
 * = multiplier_i * x_j
 * }}}
 * <p>
 * where \delta_{i, j} = 1 if i == j,
 * \delta_{i, j} = 0 if i != j, and
 * multiplier =
 * \exp(margins_i) / (1 + \sum_k^{K-1} \exp(margins_i)) - (1-\alpha(y)\delta_{y, i+1})
 * <p>
 * If any of margins is larger than 709.78, the numerical computation of multiplier and loss
 * function will be suffered from arithmetic overflow. This issue occurs when there are outliers
 * in data which are far away from hyperplane, and this will cause the failing of training once
 * infinity / infinity is introduced. Note that this is only a concern when max(margins) > 0.
 * <p>
 * Fortunately, when max(margins) = maxMargin > 0, the loss function and the multiplier can be
 * easily rewritten into the following equivalent numerically stable formula.
 * <p>
 * {{{
 * l(w, x) = log(1 + \sum_i^{K-1}\exp(margins_i)) - (1-\alpha(y)) margins_{y-1}
 * = log(\exp(-maxMargin) + \sum_i^{K-1}\exp(margins_i - maxMargin)) + maxMargin
 * - (1-\alpha(y)) margins_{y-1}
 * = log(1 + sum) + maxMargin - (1-\alpha(y)) margins_{y-1}
 * }}}
 * <p>
 * where sum = \exp(-maxMargin) + \sum_i^{K-1}\exp(margins_i - maxMargin) - 1.
 * <p>
 * Note that each term, (margins_i - maxMargin) in \exp is smaller than zero; as a result,
 * overflow will not happen with this formula.
 * <p>
 * For multiplier, similar trick can be applied as the following,
 * <p>
 * {{{
 * multiplier = \exp(margins_i) / (1 + \sum_k^{K-1} \exp(margins_i)) - (1-\alpha(y)\delta_{y, i+1})
 * = \exp(margins_i - maxMargin) / (1 + sum) - (1-\alpha(y)\delta_{y, i+1})
 * }}}
 * <p>
 * where each term in \exp is also smaller than zero, so overflow is not a concern.
 * <p>
 * For the detailed mathematical derivation, see the reference at
 * http://www.slideshare.net/dbtsai/2014-0620-mlor-36132297
 * <p>
 * numClasses the number of possible outcomes for k classes classification problem in
 * Multinomial Logistic Regression. By default, it is binary logistic regression
 * so numClasses will be set to 2.
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/24/15.
 */
@Deprecated
class LogisticGradient implements Gradient {

    private final int numClasses;

    public LogisticGradient() {
        this(2);
    }

    public LogisticGradient(int numClasses) {
        this.numClasses = numClasses;
    }

    @Override
    public Pair<DVector, Double> compute(DVector data, double label, DVector weights) {
        DVector gradient = DVector.zeros(weights.size());
        double loss = compute(data, label, weights, gradient);
        return Pair.from(gradient, loss);
    }

    @Override
    public Double compute(DVector data, double label, DVector weights, DVector cumGradient) {
        int dataSize = data.size();

        // (weights.size / dataSize + 1) is number of classes
        if (weights.size() % dataSize == 0 && numClasses == 1.0 * weights.size() / dataSize + 1)
            throw new IllegalArgumentException("");
        if (numClasses == 2) {
            /*
             * For Binary Logistic Regression.
             *
             * Although the loss and gradient calculation for multinomial one is more generalized,
             * and multinomial one can also be used in binary case, we still implement a specialized
             * binary version for performance reason.
             */
            double margin2 = -1.0 * data.dot(weights);
            double multiplier2 = (1.0 / (1.0 + Math.exp(margin2))) - label;
            cumGradient.add(data.copy().mul(multiplier2));
            if (label > 0) {
                // The following is equivalent to log(1 + exp(margin)) but more numerically stable.
                return MathTools.log1pExp(margin2);
            } else {
                return MathTools.log1pExp(margin2) - margin2;
            }
        }
        /*
         * For Multinomial Logistic Regression.
         */

        // marginY is margins(label - 1) in the formula.
        double marginY = 0.0;
        double maxMargin = Double.NEGATIVE_INFINITY;
        double maxMarginIndex = 0;

        DVector margins = DVector.zeros(numClasses - 1);
        for (int i = 0; i < margins.size(); i++) {
            double margin = 0.0;
            for (int j = 0; j < data.size(); j++) {
                double value = data.get(j);
                if (value != 0.0)
                    margin += value * weights.get((i * dataSize) + j);
            }
            if (i == (int) label - 1)
                marginY = margin;
            if (margin > maxMargin) {
                maxMargin = margin;
                maxMarginIndex = i;
            }
            margins.set(i, margin);
        }

        /*
         * When maxMargin > 0, the original formula will cause overflow as we discuss
         * in the previous comment.
         * We address this by subtracting maxMargin from all the margins, so it's guaranteed
         * that all of the new margins will be smaller than zero to prevent arithmetic overflow.
         */
        double sum = 0.0;
        if (maxMargin > 0) {
            for (int i = 0; i < numClasses - 1; i++) {
                margins.set(i, margins.get(i) - maxMargin);
                if (i == maxMarginIndex) {
                    sum += Math.exp(-maxMargin);
                } else {
                    sum += Math.exp(margins.get(i));
                }
            }
        } else {
            for (int i = 0; i < numClasses - 1; i++) {
                sum += Math.exp(margins.get(i));
            }
        }

        for (int i = 0; i < numClasses - 1; i++) {
            double multiplier = Math.exp(margins.get(i)) / (sum + 1.0) -
                    ((label != 0.0 && label == i + 1) ? 1.0 : 0.0);
            for (int j = 0; j < data.size(); j++) {
                double value = data.get(j);
                if (value != 0.0) {
                    int pos = i * dataSize + j;
                    cumGradient.set(pos, cumGradient.get(pos) + multiplier * value);
                }
            }
        }

        double loss = (label > 0.0) ? Math.log1p(sum) - marginY : Math.log1p(sum);

        if (maxMargin > 0) {
            return loss + maxMargin;
        } else {
            return loss;
        }

    }
}

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/24/15.
 */
@Deprecated
class HingeGradient implements Gradient {

    @Override
    public Pair<DVector, Double> compute(DVector data, double label, DVector weights) {
        double dotProduct = data.dot(weights);

        // Our loss function with {0, 1} labels is max(0, 1 - (2y - 1) (f_w(x)))
        // Therefore the gradient is -(2y - 1)*x
        double labelScaled = 2 * label - 1.0;
        if (1.0 > labelScaled * dotProduct) {
            DVector gradient = data.copy();
            gradient.mul(-labelScaled);
            return Pair.from(gradient, 1.0 - labelScaled * dotProduct);
        } else {
            return Pair.from(DVector.zeros(weights.size()), 0.0);
        }
    }

    @Override
    public Double compute(DVector data, double label, DVector weights, DVector cumGradient) {
        double dotProduct = data.dot(weights);
        // Our loss function with {0, 1} labels is max(0, 1 - (2y - 1) (f_w(x)))
        // Therefore the gradient is -(2y - 1)*x

        double labelScaled = 2 * label - 1.0;
        if (1.0 > labelScaled * dotProduct) {
            cumGradient.add(data.copy().mul(-labelScaled));
            return 1.0 - labelScaled * dotProduct;
        }
        return 0.0;
    }
}
