/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

import rapaio.sys.WS;

import static java.lang.Math.pow;
import static rapaio.math.MTools.*;

/**
 * F distribution, also known as Fisher-Snedecor distribution.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/10/17.
 *
 * @see <a href="https://en.wikipedia.org/wiki/F-distribution">https://en.wikipedia.org/wiki/F-distribution</a>
 */
public class Fisher implements Distribution {

    private static final long serialVersionUID = 2272786897584427248L;

    private final double df1;
    private final double df2;

    public Fisher(double df1, double df2) {
        this.df1 = df1;
        this.df2 = df2;
    }

    @Override
    public String name() {
        return "Fisher(" + WS.formatFlex(df1) + "," + WS.formatFlex(df2) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double x) {
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
    public double min() {
        return 0;
    }

    @Override
    public double max() {
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
        if (df2 <= 4)
            return Double.NaN;
        return (2 * df2 * df2 * (df1 + df2 - 2)) / (df1 * (df2 - 2) * (df2 - 2) * (df2 - 4));
    }

    @Override
    public double skewness() {
        return Double.NaN;
    }

    @Override
    public double kurtosis() {
        return Double.NaN;
    }

    @Override
    public double entropy() {
        return Double.NaN;
    }
}

