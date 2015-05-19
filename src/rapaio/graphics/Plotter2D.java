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

package rapaio.graphics;

import rapaio.core.distributions.empirical.KFunc;
import rapaio.data.Var;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.opt.GOpts;
import rapaio.graphics.plot.*;
import rapaio.graphics.plot.plotcomp.DensityLine;
import rapaio.graphics.plot.plotcomp.FunctionLine;
import rapaio.ml.eval.ROC;

import java.util.function.Function;

@Deprecated
public final class Plotter2D {

    private static GOpt[] mergedOpts(GOpt... opts) {
        GOpt[] defaults = GOpts.DEFAULTS.toArray();
        GOpt[] op = new GOpt[defaults.length + opts.length];
        System.arraycopy(defaults, 0, op, 0, defaults.length);
        System.arraycopy(opts, 0, op, defaults.length, opts.length);
        return op;
    }

    public static Plot plot(GOpt... opts) {
        return new Plot(mergedOpts(opts));
    }

    public static QQPlot qqplot(GOpt... opts) {
        return new QQPlot(mergedOpts(opts));
    }

    public static Plot hist(Var v, GOpt... opts) {
        return plot().add(new Histogram(v, opts));
    }

    public static Plot hist(Var v, double minValue, double maxValue, GOpt... opts) {
        return plot().add(new Histogram(v, minValue, maxValue, opts));
    }

    public static Plot densityLine(Var var, GOpt... opts) {
        return plot().add(new DensityLine(var, opts));
    }

    public static Plot densityLine(Var var, double bandwidth, GOpt... opts) {
        return plot().add(new DensityLine(var, bandwidth, opts));
    }

    public static Plot densityLine(Var var, KFunc kfunc, GOpt... opts) {
        return plot().add(new DensityLine(var, kfunc, opts));
    }

    public static Plot densityLine(Var var, KFunc kfunc, double bandwidth, GOpt... opts) {
        return plot().add(new DensityLine(var, kfunc, bandwidth, opts));
    }

    public static Plot funLine(Function<Double, Double> f, GOpt... opts) {
        return plot().add(new FunctionLine(f, opts));
    }

    public static Plot lines(Var x, Var y, GOpt... opts) {
        return plot().lines(x, y, opts);
    }

    public static Plot lines(Var y, GOpt... opts) {
        return plot().lines(y, opts);
    }

    public static Plot points(Var x, Var y, GOpt... opts) {
        return plot().add(new Points(x, y, opts));
    }

    public static Plot rocCurve(ROC roc, GOpt... opts) {
        return plot().rocCurve(roc, opts);
    }

    public static BarChart barChart(Var categ, GOpt... opts) {
        return new BarChart(categ, opts);
    }

    public static BarChart barChart(Var categ, Var cond, GOpt... opts) {
        return new BarChart(categ, cond, opts);
    }

    public static BarChart barChart(Var categ, Var cond, Var numeric, GOpt... opts) {
        return new BarChart(categ, cond, numeric, opts);
    }
}
