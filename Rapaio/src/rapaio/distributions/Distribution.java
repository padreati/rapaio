/*
 * Copyright 2013 Aurelian Tutuianu
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

package rapaio.distributions;

import rapaio.core.RandomSource;
import rapaio.data.NumericVector;
import rapaio.functions.UnivariateFunction;

import static rapaio.core.BaseMath.*;

/**
 * @author tutuianu
 */
public abstract class Distribution {

    /**
     * @return canonical getName of the distribution
     */
    public abstract String getName();

    /**
     * @return full description of the distributions; contains parameters
     */
    public String getDescription() {
        return "Not yet implemented.";
    }

    /**
     * @param x getValue for which it calculates log of probability
     * @return log of probability of x
     */
    public double logpdf(double x) {
        double pdf = pdf(x);
        if (pdf <= 0) {
            return -Double.MAX_VALUE;
        }
        return Math.log(pdf);
    }

    /**
     * Calculates probability mass function (pmf) of a discrete distribution or
     * probability density function (pdf) of a continuous distribution for given
     * getValue x
     *
     * @param x getValue for which it calculates
     * @return pmf / pdf of x
     */
    abstract public double pdf(double x);

    abstract public double cdf(double x);

    abstract public double quantile(double p);

    public UnivariateFunction getPdfFunction() {
        return new UnivariateFunction() {

            @Override
            public double eval(double value) {
                return pdf(value);
            }
        };
    }

    public UnivariateFunction getCdfFunction() {
        return new UnivariateFunction() {

            @Override
            public double eval(double value) {
                return cdf(value);
            }
        };
    }

    abstract public double min();

    abstract public double max();

    public NumericVector sample(int n) {
        return sample(n, getRandomSource());
    }

    public NumericVector sample(int n, RandomSource rand) {
        NumericVector samples = new NumericVector("sample", n);
        for (int i = 0; i < samples.getRowCount(); i++) {
            samples.setValue(i, quantile(rand.nextDouble()));
        }
        return samples;
    }

    abstract public double mean();

    abstract public double mode();

    abstract public double variance();

    abstract public double skewness();

    abstract public double kurtosis();

    public double sd() {
        return Math.sqrt(variance());
    }
}
