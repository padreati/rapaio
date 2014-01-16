/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

import static rapaio.core.BaseMath.pow;

/**
 * @author tutuianu
 */
public class Uniform extends Distribution {

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
    public String getName() {
        return "Continuous Uniform Distribution";
    }

    @Override
    public double pdf(double x) {
        if (x < a || x > b) {
            return 0;
        }
        if (a == b) {
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
        if (p < 0 || p > 1) {
            throw new ArithmeticException("probability value should lie in [0,1] interval");
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
        return a + (b - a) / 2.;
    }

    @Override
    public double mode() {
        return mean();
    }

    @Override
    public double variance() {
        return pow(b - a, 2) / 12.;
    }

    @Override
    public double skewness() {
        return 0;
    }

    @Override
    public double kurtosis() {
        return -6. / 5.;
    }
}
