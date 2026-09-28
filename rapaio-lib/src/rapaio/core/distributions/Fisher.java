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
import static java.lang.StrictMath.sqrt;

import static rapaio.math.MathTools.beta;
import static rapaio.math.MathTools.betaIncReg;
import static rapaio.math.MathTools.digamma;
import static rapaio.math.MathTools.invBetaIncReg;
import static rapaio.math.MathTools.lnBeta;

import java.io.Serial;

import rapaio.printer.Format;

/**
 * F distribution, also known as Fisher-Snedecor distribution.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/10/17.
 *
 * @see <a href="https://en.wikipedia.org/wiki/F-distribution">https://en.wikipedia.org/wiki/F-distribution</a>
 */
public record Fisher(double df1, double df2) implements Distribution {

    public static Fisher of(double df1, double df2) {
        return new Fisher(df1, df2);
    }

    @Serial
    private static final long serialVersionUID = 2272786897584427248L;

    @Override
    public String name() {
        return "Fisher(" + Format.floatFlex(df1) + "," + Format.floatFlex(df2) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double x) {
        if (x < 0) {
            return 0;
        }
        if (x == 0) {
            return df1 < 2 ? Double.POSITIVE_INFINITY : (df1 == 2 ? 1 : 0);
        }
        return pow(df1 / df2, df1 / 2) * pow(x, df1 / 2 - 1) * pow(1 + df1 * x / df2, -(df1 + df2) / 2) / beta(df1 / 2, df2 / 2);
    }

    @Override
    public double cdf(double x) {
        if (x <= 0.0) return 0.0;
        return betaIncReg(df1 * x / (df1 * x + df2), df1 / 2, df2 / 2);
    }

    @Override
    public double quantile(double p) {
        double a = invBetaIncReg(p, df1 / 2, df2 / 2);
        return df2 * a / (df1 * (1 - a));
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
        if (df2 <= 2)
            return Double.NaN;
        return df2 / (df2 - 2);
    }

    @Override
    public double mode() {
        if (df1 <= 2)
            return Double.NaN;
        return (df1 - 2) * df2 / (df1 * (df2 - 2));
    }

    @Override
    public double var() {
        if (df2 <= 2) {
            return Double.NaN;
        }
        if (df2 <= 4) {
            return Double.POSITIVE_INFINITY;
        }
        return (2 * df2 * df2 * (df1 + df2 - 2)) / (df1 * (df2 - 2) * (df2 - 2) * (df2 - 4));
    }

    @Override
    public double skewness() {
        if (df2 <= 6) {
            return Double.NaN;
        }
        return (2 * df1 + df2 - 2) * sqrt(8 * (df2 - 4)) / ((df2 - 6) * sqrt(df1 * (df1 + df2 - 2)));
    }

    /**
     * @return excess kurtosis for {@code df2 > 8}; {@code +Infinity} for {@code 4 < df2 <= 8}
     * (finite variance, divergent fourth moment) and {@code NaN} otherwise
     */
    @Override
    public double kurtosis() {
        if (df2 <= 4) {
            return Double.NaN;
        }
        if (df2 <= 8) {
            return Double.POSITIVE_INFINITY;
        }
        return 12 * (df1 * (5 * df2 - 22) * (df1 + df2 - 2) + (df2 - 4) * (df2 - 2) * (df2 - 2))
                / (df1 * (df2 - 6) * (df2 - 8) * (df1 + df2 - 2));
    }

    /**
     * @return differential entropy in nats; with {@code a = df1/2, b = df2/2}:
     * {@code ln B(a,b) + (a+b) psi(a+b) - (a-1) psi(a) - (b+1) psi(b) + ln(df2/df1)}
     */
    @Override
    public double entropy() {
        double a = df1 / 2;
        double b = df2 / 2;
        return lnBeta(a, b) + (a + b) * digamma(a + b) - (a - 1) * digamma(a) - (b + 1) * digamma(b) + log(df2 / df1);
    }
}

