/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import static rapaio.math.MTools.*;

/**
 * Student's T distribution, or T distribution.
 * This distribution arises when a gaussian distribution is approximated
 * for small sample size or when the standard deviation of the population is not known.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class StudentT implements Distribution {

    private static final long serialVersionUID = 2573925611489986427L;

    private final double df;
    private final double mu;
    private final double sigma;

    public StudentT(double df) {
        this(df, 0, 1);
    }

    public StudentT(double df, double mu, double sigma) {
        this.df = df;
        this.mu = mu;
        this.sigma = sigma;
    }

    @Override
    public String name() {
        return "StudentT(df=" + WS.formatFlex(df) +
                ", mu=" + WS.formatFlex(mu) +
                ", sigma=" + WS.formatFlex(sigma) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double t) {
        return Math.exp(lnGamma((df + 1) / 2) - lnGamma(df / 2) - Math.log(df * Math.PI) / 2 - Math.log(sigma)
                - (df + 1) / 2 * Math.log(1 + Math.pow((t - mu) / sigma, 2) / df));
    }

    @Override
    public double cdf(double t) {
        double x = df / (df + Math.pow((t - mu) / sigma, 2));
        double p = betaIncReg(x, df / 2, 0.5) / 2;
        if (t > mu) {
            return 1 - p;
        } else {
            return p;
        }
    }

    @Override
    public double quantile(double p) {
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("Probability must be in the range [0,1]");
        }
        if (p + 1e-20 >= 0.5 && p - 1e-20 <= 0.5) {
            return mu;
        }
        double x = invBetaIncReg(2 * Math.min(p, 1 - p), df / 2, 0.5);
        x = sigma * Math.sqrt(df * (1 - x) / x);
        if (p >= 0.5) {
            return mu + x;
        } else {
            return mu - x;
        }
    }

    @Override
    public double min() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double max() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double mean() {
        return mu;
    }

    @Override
    public double mode() {
        return mu;
    }

    @Override
    public double skewness() {
        if (df <= 3) {
            return Double.NaN;
        }
        return 0;
    }

    @Override
    public double var() {
        if (df <= 1) {
            return Double.NaN;
        }
        if (df == 2) {
            return Double.POSITIVE_INFINITY;
        }
        return df / (df - 2) * sigma * sigma;
    }

    @Override
    public double kurtosis() {
        if (df <= 2) {
            return Double.NaN;
        }
        if (df <= 4) {
            return Double.POSITIVE_INFINITY;
        }
        return 6 / (df - 4);
    }

    @Override
    public double entropy() {
        // take a look at the wiki page - it's scary
        throw new IllegalArgumentException("not implemented");
    }
}
