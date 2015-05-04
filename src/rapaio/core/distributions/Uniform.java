/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import rapaio.printer.Printer;

/**
 * Continuous uniform distribution
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class Uniform implements Distribution {

    private final double a;
    private final double b;

    public Uniform(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    @Override
    public String name() {
        return String.format("Uniform(a=%s,b=%s)",
                Printer.formatDecShort.format(getA()),
                Printer.formatDecShort.format(getB()));
    }

    @Override
    public boolean isDiscrete() {
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
