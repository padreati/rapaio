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

package rapaio.core.distributions.du;

import rapaio.core.RandomSource;
import rapaio.data.Numeric;
import rapaio.data.Var;

import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public interface DUDistribution {

    /**
     * @return canonical getName of the distribution
     */
    String getName();

    /**
     * @param x value for which it calculates log of probability
     * @return log of probability of x
     */
    default double logPmf(double x) {
        double pmf = pmf(x);
        if (pmf <= 0) {
            return -Double.MAX_VALUE;
        }
        return Math.log(pmf);
    }

    /**
     * Calculates probability mass function (pmf) for given value x
     *
     * @param x value for which it calculates
     * @return pmf of x
     */
    double pmf(double x);

    /**
     * Computes cumulative density function for given value
     *
     * @param x given value
     * @return cdf(x)
     */
    double cdf(double x);

    /**
     * Computes the quantile for given probability p
     *
     * @param p given probability
     * @return quantile value
     */
    double quantile(double p);

    /**
     * Minimum value for which pmf is defined
     *
     * @return minimum value
     */
    double min();

    /**
     * Maximum value for which pmf is defined
     *
     * @return maximum value
     */
    double max();

    /**
     * The step between consecutive values defined by pmf
     * <p>
     * For DUnif(0,5), the minimum value is 0, maximum value is 5
     * and step value is 1, since the only points where pmf is defined
     * are {0,1,2,3,4,5}
     *
     * @return step value, NaN if not defined
     */
    default double step() {
        return 1.0;
    }

    /**
     * Generates a sample of values from distribution
     *
     * @param n sample size
     * @return sample values
     */
    default Numeric sample(int n) {
        return IntStream.range(0, n).mapToObj(i -> quantile(RandomSource.nextDouble())).collect(Var.numericCollector());
    }

    /**
     * Compute distribution's expected value
     *
     * @return expected value / mean
     */
    double mean();

    /**
     * Computes distribution's mode, if multiple modes, one of them is returned
     *
     * @return distribution's mode
     */
    double mode();

    /**
     * Computes variance of the distribution
     *
     * @return variance or NaN if not defined
     */
    double variance();

    /**
     * Computes skewness of the distribution
     *
     * @return skewness, NaN if not defined
     */
    double skewness();

    /**
     * Computes kurtosis of the distribution
     *
     * @return kurtosis, NaN if not defined
     */
    double kurtosis();

    /**
     * Computes standard deviation as sqrt of variance
     *
     * @return standard deviation
     */
    default double sd() {
        return Math.sqrt(variance());
    }
}
