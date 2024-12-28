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
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;

import static rapaio.math.MathTools.E;
import static rapaio.math.MathTools.PI;
import static rapaio.printer.Format.floatFlex;

import java.io.Serial;

import rapaio.math.MathTools;

/**
 * @author <a href="mailto:padreatiyahoo.com">Aurelian Tutuianu</a>
 */
public class Normal implements Distribution {

    public static Normal std() {
        return new Normal(0, 1);
    }

    public static Normal of(double mean, double sd) {
        return new Normal(mean, sd);
    }

    @Serial
    private static final long serialVersionUID = 3618971055326379083L;
    private final double mu;
    private final double sd;
    private final double var;

    private Normal(double mu, double sd) {
        this.mu = mu;
        this.sd = sd;
        this.var = sd * sd;
    }

    @Override
    public String name() {
        return "Normal(mu=" + floatFlex(mu) + ", sd=" + floatFlex(sd) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double x) {
        return 1 / sqrt(2 * PI * var) * exp(-pow(x - mu, 2) / (2 * var));
    }

    @Override
    public double cdf(double x) {
        return cdf(x, mu, sd);
    }

    private double cdf(double x, double mu, double sd) {
        return MathTools.normalCdf(x, mu, sd);
    }

    @Override
    public double quantile(double p) {
        return MathTools.normalQuantile(p, mu, sd);
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double mean() {
        return mu;
    }

    @Override
    public double mode() {
        return mean();
    }

    @Override
    public double var() {
        return var;
    }

    @Override
    public double skewness() {
        return 0;
    }

    @Override
    public double kurtosis() {
        return 0;
    }

    @Override
    public double entropy() {
        return log(2 * PI * E * var);
    }
}
