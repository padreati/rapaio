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

import static rapaio.math.MathTools.*;

import java.io.Serial;

import rapaio.printer.Format;

/**
 * Discrete probability distribution which expresses the probability of a
 * given number of events occuring in a fixed interval of time/space
 * if these events occur with a known average rate and independent
 * of the last occurrence of last events.
 */
public record Poisson(double lambda) implements Distribution {

    public static Poisson of(double lambda) {
        return new Poisson(lambda);
    }

    @Serial
    private static final long serialVersionUID = 2013039227493064895L;

    public Poisson {
        if (lambda <= 0) {
            throw new IllegalArgumentException("lambda parameter value must be a real positive value");
        }
    }

    @Override
    public String name() {
        return "Poisson(lambda=" + Format.floatFlex(lambda) + ")";
    }

    public double getLambda() {
        return lambda;
    }

    @Override
    public boolean discrete() {
        return true;
    }

    @Override
    public double pdf(double x) {
        if (x < 0)
            return 0.0;
        double xx = rint(x);
        if (xx != x)
            return 0;
        return pdfPois(x, lambda);
    }

    @Override
    public double cdf(double x) {
        if (x < 0)
            return 0.0;
        return incGammaC(floor(x + 1), lambda);
    }

    @Override
    public double quantile(double p) {
        if (p == 1)
            return Double.POSITIVE_INFINITY;

        if (p <= cdf(0))
            return 0;

        // unbounded binary search
        int low = 0;
        int up = 1;

        // double up until we found a bound
        double cdf_up = cdf(up);
        while (cdf_up <= p) {
            up *= 2;
            cdf_up = cdf(up);
        }
        while (true) {
            int mid = floorDiv(low + up, 2);
            if (mid == low)
                return up;
            double cdf_mid = cdf(mid);
            if (cdf_mid < p) {
                low = mid;
            } else {
                up = mid;
            }
        }
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
    public double mean() {
        return lambda;
    }

    @Override
    public double mode() {
        return floor(lambda);
    }

    @Override
    public double var() {
        return lambda;
    }

    @Override
    public double skewness() {
        return 1.0 / sqrt(lambda);
    }

    @Override
    public double kurtosis() {
        return 1 / lambda;
    }

    @Override
    public double entropy() {
        return 0;
    }
}
