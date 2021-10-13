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

import rapaio.core.RandomSource;
import rapaio.printer.Format;

/**
 * ChiSquare distribution.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/29/15.
 */
public final class ChiSquare implements Distribution {

    public static ChiSquare of(double df) {
        return new ChiSquare(df);
    }

    @Serial
    private static final long serialVersionUID = 2967287812574824823L;
    private final double df;
    private final double b;
    private final double vm;
    private final double vd;

    private ChiSquare(double df) {
        if (df < 1) {
            throw new IllegalArgumentException("degrees of freedom parameter must have value greater than zero");
        }
        this.df = df;
        this.b = sqrt(df - 1.0);
        double vm1 = -0.6065306597 * (1.0 - 0.25 / (b * b + 1.0));
        this.vm = max(-b, vm1);
        this.vd = 0.6065306597 * (0.7071067812 + b) / (0.5 + b) - vm;
    }

    @Override
    public String name() {
        return "ChiSq(df=" + Format.floatFlex(df) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double x) {
        if (x < 0.0)
            return 0;
        return exp((df / 2.0 - 1.0) * log(x / 2.0) - x / 2.0 - lnGamma(df / 2.0)) / 2.0;
    }

    @Override
    public double cdf(double x) {
        if (x < 0.0)
            return 0.0;
        return incGamma(df / 2.0, x / 2.0);
    }

    @Override
    public double quantile(double p) {

        // implement binary search
        double low = 0;
        double high = 1;

        while (cdf(high) < p) {
            high *= 2;
        }

        while (true) {
            double mid = (low + high) / 2.0;
            double v = cdf(mid);
            if (v < p) {
                low = mid;
            } else {
                high = mid;
            }
            if (abs(p - v) < 1e-14) {
                break;
            }
        }
        return low;
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
        return df;
    }

    @Override
    public double mode() {
        return max(0, df - 2);
    }

    @Override
    public double var() {
        return 2 * df;
    }

    @Override
    public double skewness() {
        return sqrt(8 / df);
    }

    @Override
    public double kurtosis() {
        return 12 / df;
    }

    @Override
    public double entropy() {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public double sampleNext() {
        /* *********************************************************************
         * * Chi Distribution - Ratio of Uniforms with shift * *
         * ***************************************************************** *
         * FUNCTION : - chru samples a random number from the Chi * distribution
         * with parameter a > 1. * REFERENCE : - J.F. Monahan (1987): An
         * algorithm for * generating chi random variables, ACM Trans. * Math.
         * Software 13, 168-172. * SUBPROGRAM : - anEngine ... pointer to a
         * (0,1)-Uniform * engine * * Implemented by R. Kremer, 1990 *
         **********************************************************************/

        double u, v, z, zz, r;

        // if( a < 1 ) return (-1.0); // Check for invalid input value

        if (df == 1.0) {
            for (; ; ) {
                u = RandomSource.nextDouble();
                v = RandomSource.nextDouble() * 0.857763884960707;
                z = v / u;
                zz = z * z;
                r = 2.5 - zz;
                if (u < r * 0.3894003915)
                    return (z * z);
                if (zz > (1.036961043 / u + 1.4))
                    continue;
                if (2.0 * log(u) < (-zz * 0.5))
                    return (z * z);
            }
        } else {
            for (; ; ) {
                u = RandomSource.nextDouble();
                v = RandomSource.nextDouble() * vd + vm;
                z = v / u;
                if (z < -b)
                    continue;
                zz = z * z;
                r = 2.5 - zz;
                if (z < 0.0)
                    r = r + zz * z / (3.0 * (z + b));
                if (u < r * 0.3894003915)
                    return ((z + b) * (z + b));
                if (zz > (1.036961043 / u + 1.4))
                    continue;
                if (2.0 * log(u) < (log(1.0 + z / b) * b * b - zz * 0.5 - z * b))
                    return ((z + b) * (z + b));
            }
        }
    }
}
