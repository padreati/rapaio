/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core.distributions;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.floor;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.rint;

import java.io.Serial;

/**
 * Discrete uniform distribution
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DUniform implements Distribution {

    public static DUniform of(int a, int b) {
        return new DUniform(a, b);
    }

    @Serial
    private static final long serialVersionUID = -6164593855805329051L;
    private final double a;
    private final double b;
    private final double n;

    private DUniform(int a, int b) {
        this.a = a;
        this.b = b;
        this.n = b - a + 1;
    }

    public boolean discrete() {
        return true;
    }

    public int a() {
        return (int) a;
    }

    public int b() {
        return (int) b;
    }

    @Override
    public String name() {
        return String.format("DUniform(a=%d,b=%d)", (int) a, (int) b);
    }

    @Override
    public double pdf(double x) {
        double rint = rint(x);
        if (Double.isFinite(x) && x == rint) {
            if (x < a || x > b) {
                return 0;
            }
            return 1 / n;
        }
        return 0;
    }

    @Override
    public double cdf(double x) {
        if (x < a) {
            return 0;
        }
        if (x > b) {
            return 1;
        }
        return (floor(x) - a + 1) / n;
    }

    @Override
    public double quantile(double p) {
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("Probability must be interface the range [0,1], not " + p);
        }
        if (a == b) {
            return a;
        }
        double v = a + p * n;
        int vi = (int) v;
        if (abs(v - floor(v)) < Double.MIN_VALUE)
            return vi - 1;
        return vi;
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
        return (a + b) / 2;
    }

    @Override
    public double mode() {
        return Double.NaN;
    }


    @Override
    public double var() {
        return (n * n - 1) / 12.;
    }

    @Override
    public double skewness() {
        return 0;
    }

    @Override
    public double kurtosis() {
        return -6. * (n * n + 1) / (5. * (n * n - 1));
    }

    @Override
    public double entropy() {
        return log(n);
    }
}
