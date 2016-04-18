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

package rapaio.core;

import rapaio.core.correlation.CorrPearson;
import rapaio.core.correlation.CorrSpearman;
import rapaio.core.distributions.Bernoulli;
import rapaio.core.distributions.DUniform;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.core.stat.*;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;

/**
 * Utility class for calling basic statistical tools on variables.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/4/15.
 */
public final class CoreTools {

    //
    // statistical tools
    //

    public static OnlineStat newOnlineStat() {
        return new OnlineStat();
    }

    public static Mean mean(Var var) {
        return new Mean(var);
    }

    public static GeometricMean geometricMean(Var var) {
        return new GeometricMean(var);
    }

    public static Variance var(Var var) {
        return new Variance(var);
    }

    public static Maximum max(Var var) {
        return new Maximum(var);
    }

    public static Minimum min(Var var) {
        return new Minimum(var);
    }

    public static Sum sum(Var var) {
        return new Sum(var);
    }

    public static Quantiles quantiles(Var var, double... p) {
        return new Quantiles(var, p);
    }

    public static Quantiles quantiles(Var var, Numeric p) {
        return new Quantiles(var, p.stream().mapToDouble().toArray());
    }

    public static Quantiles quantiles(Var var, Quantiles.Type type, Numeric p) {
        return new Quantiles(var, type, p.stream().mapToDouble().toArray());
    }

    public static Modes modes(Var var) {
        return new Modes(var, false);
    }

    public static Modes modes(Var var, boolean missing) {
        return new Modes(var, missing);
    }

    public static Covariance cov(Var x, Var y) {
        return new Covariance(x, y);
    }

    //
    // correlations
    //

    public static CorrPearson corrPearson(Frame df) {
        return new CorrPearson(df);
    }

    public static CorrPearson corrPearson(Var... vars) {
        return new CorrPearson(vars);
    }

    public static CorrSpearman corrSpearman(Frame df) {
        return new CorrSpearman(df);
    }

    public static CorrSpearman corrSpearman(Var... vars) {
        return new CorrSpearman(vars);
    }

    ///
    // distribution tools
    //
    public static Normal distNormal() {
        return new Normal(0, 1);
    }

    public static Normal distNormal(double mu, double sd) {
        return new Normal(mu, sd);
    }

    public static DUniform distDUnif(int a, int b) {
        return new DUniform(a, b);
    }

    public static Uniform distUnif() {
        return new Uniform(0, 1);
    }

    public static Uniform distUnif(double a, double b) {
        return new Uniform(a, b);
    }

    public static Bernoulli distBer(double p) {
        return new Bernoulli(p);
    }
}
