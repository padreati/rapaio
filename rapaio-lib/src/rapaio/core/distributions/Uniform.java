/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

import static rapaio.printer.Format.floatFlex;

import java.io.Serial;

/**
 * Continuous uniform distribution on the closed interval {@code [a, b]}, with {@code a < b}.
 * A degenerate interval ({@code a == b}) is rejected: it would be a point mass, for which
 * {@code pdf} is not a density and {@code cdf(a)} is {@code 0/0}.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public record Uniform(double a, double b) implements Distribution {

    public Uniform {
        if (!(Double.isFinite(a) && Double.isFinite(b))) {
            throw new IllegalArgumentException("Interval bounds must be finite, not [" + a + "," + b + "]");
        }
        if (!(a < b)) {
            throw new IllegalArgumentException("Lower bound a=" + a + " must be less than upper bound b=" + b);
        }
    }

    public static Uniform of(double a, double b) {
        return new Uniform(a, b);
    }

    @Serial
    private static final long serialVersionUID = -6077483164719205038L;

    @Override
    public String name() {
        return "Uniform(a=" + floatFlex(a) + ",b=" + floatFlex(b) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double x) {
        if (x < a || x > b) {
            return 0;
        }
        return 1 / (b - a);
    }

    @Override
    public double cdf(double x) {
        if (x < a) {
            return 0;
        }
        if (x > b) {
            return 1;
        }
        return (x - a) / (b - a);
    }

    @Override
    public double quantile(double p) {
        QuantileSearch.checkProbability(p);
        return a + p * (b - a);
    }

    @Override
    public double minValue() {
        return a;
    }

    @Override
    public double maxValue() {
        return b;
    }

    @Override
    public double mean() {
        return a + (b - a) / 2.0;
    }

    @Override
    public double mode() {
        return mean();
    }

    @Override
    public double var() {
        return pow(b - a, 2) / 12.0;
    }

    @Override
    public double skewness() {
        return 0;
    }

    @Override
    public double kurtosis() {
        return -6.0 / 5.0;
    }

    @Override
    public double entropy() {
        return log(b - a);
    }
}
