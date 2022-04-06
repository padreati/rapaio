/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core.distributions;

import static java.lang.StrictMath.sqrt;

import java.io.Serializable;

import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

/**
 * Interface which models all types of uni-variate statistical distributions.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 11/3/14.
 */
public interface Distribution extends Serializable {

    /**
     * @return canonical name of the densities with parameter values
     */
    String name();

    /**
     * @return true if the distribution is discrete, false if it is continuous
     */
    boolean discrete();

    /**
     * Calculates probability density/mass function for given value x
     *
     * @param x value for which it calculates
     * @return pdf(x)
     */
    double pdf(double x);

    /**
     * Computes cumulative density function value for given x
     *
     * @param x given value
     * @return cdf(x)
     */
    double cdf(double x);

    /**
     * Computes quantile for the given probability value.
     * Quantile function is the inverse of the cdf, aka it
     * returns the value for which the cdf evaluates to
     * a given probability.
     *
     * @param p given probability
     * @return quantile value
     */
    double quantile(double p);

    /**
     * Minimum value for which this pdf is defined
     *
     * @return minimum value, might be -Inf
     */
    double minValue();

    /**
     * Maximum value for which pdf is defined
     *
     * @return maximum value, might be Inf
     */
    double maxValue();

    /**
     * Generates a random value from this distribution
     *
     * @return new random value
     */
    default double sampleNext() {
        return quantile(RandomSource.nextDouble());
    }

    /**
     * Generate a sample for this distribution with the given size
     *
     * @param n number of elements in sample
     * @return sample values
     */
    default VarDouble sample(final int n) {
        return VarDouble.from(n, i -> sampleNext());
    }

    /**
     * Computes expected value
     *
     * @return expected value / mean
     */
    double mean();

    /**
     * Computes the mode of the densities, if multiple modes are defined, than one of them is returned
     *
     * @return mode value for which the pdf has the maximum value
     */
    double mode();

    /**
     * Computes the median values of the density
     *
     * @return median value for density
     */
    default double median() {
        return quantile(0.5);
    }

    /**
     * @return variance of the distribution.
     */
    double var();

    /**
     * @return standard deviation of the distribution
     */
    default double sd() {
        return sqrt(var());
    }

    /**
     * @return skewness of the distribution
     */
    double skewness();

    /**
     * @return kurtosis of the distribution
     */
    double kurtosis();

    /**
     * @return entropy of the distribution
     */
    double entropy();
}
