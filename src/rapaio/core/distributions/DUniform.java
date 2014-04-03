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

package rapaio.core.distributions;

/**
 * @author tutuianu
 */
public class DUniform extends Distribution {

    private final int a;
    private final int b;

    public DUniform(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    @Override
    public String getName() {
        return "Discrete Uniform Distribution ";
    }

    @Override
    public double pdf(double x) {
        double rint = Math.rint(x);
        if (!Double.isNaN(x) && !Double.isInfinite(x) && x == rint) {
            if (x < a || x > b) {
                return 0;
            }
            return 1 / (b - a + 1.);
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
        return (Math.floor(x) - a + 1) / (b - a + 1);
    }

    @Override
    public double quantile(double p) {
        if (p < 0 || p > 1) {
            throw new ArithmeticException("Probability must be interface the range [0,1], not " + p);
        }

        if (a == b && p == 1) {
            return a;
        }

        return (int) (a + p * (b - a + 1));
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
        return (b - a) / 2.;
    }

    @Override
    public double mode() {
        return mean();
    }

    @Override
    public double variance() {
        double n = b - a + 1;
        return (n * 2 - 1) / 12.;
    }

    @Override
    public double skewness() {
        return 0;
    }

    @Override
    public double kurtosis() {
        double len = (b - a);
        return -6. * (Math.pow(len, 2) + 1) / (5. * (Math.pow(len, 2) - 1));
    }
}
