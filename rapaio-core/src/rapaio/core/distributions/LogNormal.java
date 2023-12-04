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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import static rapaio.math.MathTools.DOUBLE_PI;
import static rapaio.math.MathTools.PI;
import static rapaio.math.MathTools.SQRT_2;
import static rapaio.math.MathTools.erf;
import static rapaio.math.MathTools.inverf;
import static rapaio.math.MathTools.log2;
import static rapaio.printer.Format.floatFlex;

public class LogNormal implements Distribution {

    public static LogNormal of(double mu, double sigma) {
        return new LogNormal(mu, sigma);
    }

    private final double mu;
    private final double sigma;

    private final double sigma_square;

    private LogNormal(double mu, double sigma) {
        this.mu = mu;
        this.sigma = sigma;
        this.sigma_square = sigma * sigma;
    }

    @Override
    public String name() {
        return "LogNormal(mu=" + floatFlex(mu) + ", sigma=" + floatFlex(sigma) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double x) {
        return exp(-pow(log(x) - mu, 2) / (2 * sigma_square)) / (x * sigma * sqrt(DOUBLE_PI));
    }

    @Override
    public double cdf(double x) {
        return (1 + erf((log(x) - mu) / (sigma * SQRT_2))) / 2;
    }

    @Override
    public double quantile(double p) {
        return exp(mu + SQRT_2 * sigma * inverf(2 * p - 1));
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double sampleNext() {
        return exp(Normal.of(mu, sigma).sampleNext());
    }

    @Override
    public double mean() {
        return exp(mu + sigma_square / 2);
    }

    @Override
    public double mode() {
        return exp(mu - sigma_square);
    }

    @Override
    public double median() {
        return exp(mu);
    }

    @Override
    public double var() {
        return exp(2 * mu + sigma_square) * (exp(sigma_square) - 1);
    }

    @Override
    public double skewness() {
        return (exp(sigma_square) + 2) * sqrt(sigma_square - 1);
    }

    @Override
    public double kurtosis() {
        return exp(4 * sigma_square) + 2 * exp(3 * sigma_square) + 3 * exp(2 * sigma_square) - 6;
    }

    @Override
    public double entropy() {
        return log2(sigma * exp(mu + 0.5) * sqrt(2 * PI));
    }
}
