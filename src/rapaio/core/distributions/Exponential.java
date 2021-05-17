/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.core.distributions;

import rapaio.printer.Format;

import java.io.Serial;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/19/20.
 */
public class Exponential implements Distribution {

    public static Exponential of(double lambda) {
        return new Exponential(lambda);
    }

    @Serial
    private static final long serialVersionUID = 5064238118800143270L;

    private final double lambda;

    private Exponential(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public String name() {
        return "Exponential(lambda=" + Format.floatFlex(lambda) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double x) {
        if (x < 0) {
            return 0;
        }
        return lambda * Math.exp(-lambda * x);
    }

    @Override
    public double cdf(double x) {
        if (x < 0) {
            return 0;
        }
        return 1 - Math.exp(-lambda * x);
    }

    @Override
    public double quantile(double p) {
        if (p < 0) {
            return 0;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return -Math.log(1 - p) / lambda;
    }

    @Override
    public double min() {
        return 0;
    }

    @Override
    public double max() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double mean() {
        return 1 / lambda;
    }

    @Override
    public double mode() {
        return 0;
    }

    @Override
    public double median() {
        return Math.log(2) / lambda;
    }

    @Override
    public double var() {
        return 1 / (lambda * lambda);
    }

    @Override
    public double skewness() {
        return 2;
    }

    @Override
    public double kurtosis() {
        return 6;
    }

    @Override
    public double entropy() {
        return 1 - Math.log(lambda);
    }
}
