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

package rapaio.core.distributions.cu;

import rapaio.core.RandomSource;
import rapaio.data.Numeric;
import rapaio.data.Var;

import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Continuous Univariate Distribution
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public interface CUDistribution {

    /**
     * @return canonical name of the distribution with parameter values
     */
    abstract String getName();

    /**
     * Logarithm of the density function
     *
     * @param x value for which it calculates log of probability
     * @return log of probability of x
     */
    default double logpdf(double x) {
        double pdf = pdf(x);
        if (pdf <= 0) {
            return -Double.MAX_VALUE;
        }
        return Math.log(pdf);
    }

    /**
     * Calculates probability density function (pdf) for given value x
     *
     * @param x value for which it calculates
     * @return pdf(x)
     */
    double pdf(double x);

    /**
     * Computes cumulative distribution function value for given x
     * @param x given value
     * @return cdf(x)
     */
    double cdf(double x);

    /**
     * Computes quantile for the given probability value
     * @param p given probability
     * @return quantile value
     */
    double quantile(double p);

    /**
     * Minimum value for which this pdf is defined
     *
     * @return minimum value, might be -Inf
     */
    abstract public double min();

    /**
     * Maximum value for which pdf is defined
     *
     * @return maximum value, might be Inf
     */
    abstract public double max();

    /**
     * Generate a sample for this distribution
     * @param n number of elements in sample
     * @return sample values
     */
    default Numeric sample(int n) {
        return IntStream.range(0, n)
                .mapToObj(i -> quantile(RandomSource.nextDouble()))
                .collect(Var.numericCollector());
    }

    /**
     * Computes expected value
     * @return expected value / mean
     */
    abstract public double mean();

    /**
     * Computes the mode of the distribution, if multiple modes are defined, than one of them is returned
     * @return mode value for which the pdf has the maximum value
     */
    abstract public double mode();

    /**
     * Computes variance of the distribution
     *
     * @return variance of the distribution, or NaN if it is not defined
     */
    abstract public double variance();

    /**
     * Computes the skewness
     *
     * @return skewness value
     */
    abstract public double skewness();

    /**
     * Computes the kurtosis
     *
     * @return the kurtosis value
     */
    abstract public double kurtosis();

    /**
     * Computes the standard deviation as a sqrt of variance
     *
     * @return standard deviation
     */
    default double sd() {
        return Math.sqrt(variance());
    }
}
