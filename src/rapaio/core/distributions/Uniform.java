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

import static rapaio.printer.Format.floatFlex;

/**
 * Continuous uniform distribution
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Uniform implements Distribution {

    public static Uniform of(double a, double b) {
        return new Uniform(a, b);
    }

    private static final long serialVersionUID = -6077483164719205038L;
    private final double a;
    private final double b;

    private Uniform(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public double a() {
        return a;
    }

    public double b() {
        return b;
    }

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
        if (a == b) {
            return 1;
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
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("probability value should lie in [0,1] interval");
        }
        return a + p * (b - a);
    }

    @Override
    public double min() {
        return a;
    }

    @Override
    public double max() {
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
        return Math.pow(b - a, 2) / 12.0;
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
        return Math.log(b - a);
    }
}
