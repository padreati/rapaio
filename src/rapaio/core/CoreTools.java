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

package rapaio.core;

import rapaio.core.correlation.CorrPearson;
import rapaio.core.correlation.CorrSpearman;
import rapaio.core.distributions.Bernoulli;
import rapaio.core.distributions.DUniform;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.core.stat.*;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;

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
        return OnlineStat.empty();
    }

    public static Mean mean(Var var) {
        return Mean.from(var);
    }

    public static GeometricMean geometricMean(Var var) {
        return GeometricMean.from(var);
    }

    public static Variance variance(Var var) {
        return Variance.from(var);
    }

    public static Maximum max(Var var) {
        return Maximum.from(var);
    }

    public static Minimum min(Var var) {
        return Minimum.from(var);
    }

    public static Sum sum(Var var) {
        return Sum.from(var);
    }

    public static Quantiles quantiles(Var var, double... p) {
        return Quantiles.from(var, p);
    }

    public static Quantiles quantiles(Var var, VarDouble p) {
        return Quantiles.from(var, p.stream().mapToDouble().toArray());
    }

    public static Quantiles quantiles(Var var, Quantiles.Type type, VarDouble p) {
        return Quantiles.from(var, type, p.stream().mapToDouble().toArray());
    }

    public static Modes modes(Var var) {
        return Modes.from(var, false);
    }

    public static Modes modes(Var var, boolean missing) {
        return Modes.from(var, missing);
    }

    public static Covariance cov(Var x, Var y) {
        return Covariance.from(x, y);
    }

    //
    // correlations
    //

    public static CorrPearson corrPearson(Frame df) {
        return CorrPearson.from(df);
    }

    public static CorrPearson corrPearson(Var... vars) {
        return CorrPearson.from(vars);
    }

    public static CorrSpearman corrSpearman(Frame df) {
        return CorrSpearman.from(df);
    }

    public static CorrSpearman corrSpearman(Var... vars) {
        return CorrSpearman.from(vars);
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
