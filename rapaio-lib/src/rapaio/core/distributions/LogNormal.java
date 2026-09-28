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

package rapaio.core.distributions;

import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.sqrt;

import static rapaio.math.MathTools.DOUBLE_PI;
import static rapaio.math.MathTools.SQRT_2;
import static rapaio.math.MathTools.erf;
import static rapaio.math.MathTools.inverf;
import static rapaio.printer.Format.floatFlex;

import java.io.Serial;
import java.util.Random;

/**
 * Log-normal distribution: {@code X = exp(Y)} with {@code Y ~ Normal(mu, sigma)}.
 * {@code mu} and {@code sigma} are the mean and standard deviation of {@code log(X)},
 * as in R {@code dlnorm(x, meanlog, sdlog)} or scipy {@code lognorm(s=sigma, scale=exp(mu))}.
 */
public class LogNormal implements Distribution {

    public static LogNormal of(double mu, double sigma) {
        return new LogNormal(mu, sigma);
    }

    @Serial
    private static final long serialVersionUID = 4396052325470349091L;

    private final double mu;
    private final double sigma;

    private final double sigma_square;

    private LogNormal(double mu, double sigma) {
        if (!(sigma > 0)) {
            throw new IllegalArgumentException("Parameter sigma must be strictly positive, got: " + floatFlex(sigma) + ".");
        }
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
        if (x <= 0) {
            return 0;
        }
        double z = (log(x) - mu) / sigma;
        return exp(-z * z / 2) / (x * sigma * sqrt(DOUBLE_PI));
    }

    @Override
    public double cdf(double x) {
        if (x <= 0) {
            return 0;
        }
        return (1 + erf((log(x) - mu) / (sigma * SQRT_2))) / 2;
    }

    @Override
    public double quantile(double p) {
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("Probability value should lie in [0,1] interval");
        }
        if (p == 0) {
            return 0;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
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
        return sampleNext(new Random());
    }

    @Override
    public double sampleNext(Random random) {
        return exp(mu + sigma * random.nextGaussian());
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
        return (exp(sigma_square) + 2) * sqrt(exp(sigma_square) - 1);
    }

    @Override
    public double kurtosis() {
        return exp(4 * sigma_square) + 2 * exp(3 * sigma_square) + 3 * exp(2 * sigma_square) - 6;
    }

    /**
     * @return differential entropy in nats: {@code mu + 1/2 + log(sigma sqrt(2 pi))}
     */
    @Override
    public double entropy() {
        return mu + 0.5 + log(sigma * sqrt(DOUBLE_PI));
    }
}
