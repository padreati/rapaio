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

package rapaio.graphics;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.empirical.KFunc;
import rapaio.data.Frame;
import rapaio.data.IndexVar;
import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.plot.*;
import rapaio.graphics.plot.plotcomp.*;
import rapaio.ml.eval.ROC;
import rapaio.util.func.SFunction;

import java.awt.*;
import java.util.Arrays;

public final class Plotter {

    public static Plot plot(GOpt... opts) {
        return new Plot(opts);
    }

    public static GridLayer gridLayer(int rows, int cols) {
        return new GridLayer(rows, cols);
    }

    public static QQPlot qqplot(Var points, Distribution dist, GOpt... opts) {
        return new QQPlot(points, dist, opts);
    }

    public static BoxPlot boxPlot(Var x, Var factor, GOpt... opts) {
        return new BoxPlot(x, factor, opts);
    }

    public static BoxPlot boxPlot(Var x, GOpt... opts) {
        return new BoxPlot(x, opts);
    }

    public static BoxPlot boxPlot(Var[] vars, GOpt... opts) {
        return new BoxPlot(vars, opts);
    }

    public static BoxPlot boxPlot(Frame df, GOpt... opts) {
        return new BoxPlot(df, opts);
    }

    public static Plot hist(Var v, GOpt... opts) {
        return plot().add(new Histogram(v, opts));
    }

    public static Plot hist(Var v, double minValue, double maxValue, GOpt... opts) {
        return plot().add(new Histogram(v, minValue, maxValue, opts));
    }

    public static Plot hist2d(Var x, Var y, GOpt... opts) {
        return plot().add(new Histogram2D(x, y, opts));
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

    public static Plot funLine(SFunction<Double, Double> f, GOpt... opts) {
        return plot().add(new FunctionLine(f, opts));
    }

    public static Plot lines(Var x, Var y, GOpt... opts) {
        return plot().add(new Lines(x, y, opts));
    }

    public static Plot lines(Var y, GOpt... opts) {
        return plot().add(new Lines(y, opts));
    }

    public static Plot points(Var x, Var y, GOpt... opts) {
        return plot().add(new Points(x, y, opts));
    }

    public static Plot points(Var y, GOpt... opts) {
        return plot().add(new Points(IndexVar.seq(y.getRowCount()).withName("pos"), y, opts));
    }

    public static Plot rocCurve(ROC roc, GOpt... opts) {
        return plot().add(new ROCCurve(roc, opts));
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

    public static GOpt palette(ColorPalette colorPalette) {
        return opt -> opt.setPalette(gOpts -> colorPalette);
    }

    public static GOpt color(int... index) {
        return opt -> opt.setColor(gOpts -> Arrays.stream(index).boxed().map(i -> gOpts.getPalette().getColor(i)).toArray(Color[]::new));
    }

    public static GOpt color(Color color) {
        return opt -> opt.setColor(gOpts -> new Color[]{color});
    }

    // GRAPHICAL OPTIONS

    public static GOpt color(Color[] colors) {
        return opt -> opt.setColor(gOpts -> colors);
    }

    public static GOpt color(Var color) {
        return opt -> opt.setColor(gOpts -> {
            Color[] colors = new Color[color.getRowCount()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = opt.getPalette().getColor(color.getIndex(i));
            }
            return colors;
        });
    }

    public static GOpt lwd(float lwd) {
        return opt -> opt.setLwd(gOpts -> lwd);
    }

    public static GOpt sz(Var sizeIndex) {
        return sz(sizeIndex, 1);
    }

    public static GOpt sz(Var sizeIndex, double factor) {
        return sz(sizeIndex, factor, 0);
    }

    public static GOpt sz(Var sizeIndex, double factor, double offset) {
        NumericVar size = sizeIndex
                .stream()
                .mapToDouble()
                .map(x -> x * factor + offset)
                .boxed()
                .collect(NumericVar.collector());
        return opt -> opt.setSz(gOpts -> size);
    }

    public static GOpt sz(double size) {
        return opt -> opt.setSz(gOpts -> NumericVar.scalar(size));
    }

    public static GOpt pch(Var pchIndex, int... mapping) {
        IndexVar pch = IndexVar.from(pchIndex.getRowCount(), row -> {
            int i = pchIndex.getIndex(row);
            if (i >= 0 || i < mapping.length) {
                return mapping[i];
            }
            return mapping[0];
        });
        return opt -> opt.setPch(gOpts -> pch);
    }

    public static GOpt pch(int pch) {
        return opt -> opt.setPch(gOpts -> IndexVar.scalar(pch));
    }

    public static GOpt alpha(float alpha) {
        return opt -> opt.setAlpha(gOpts -> alpha);
    }

    public static GOpt bins(int bins) {
        return opt -> opt.setBins(gOpts -> bins);
    }

    public static GOpt prob(boolean prob) {
        return opt -> opt.setProb(gOpts -> prob);
    }

    public static GOpt points(int points) {
        return opt -> opt.setPoints(gOpts -> points);
    }

    public static GOpt labels(String... labels) {
        return opt -> opt.setLabels(gOpts -> labels);
    }

    public Plot hLine(double a, GOpt... opts) {
        return plot().add(new ABLine(true, a, opts));
    }

    public Plot vLine(double a, GOpt... opts) {
        return plot().add(new ABLine(false, a, opts));
    }

    public Plot abLine(double a, double b, GOpt... opts) {
        return plot().add(new ABLine(a, b, opts));
    }
}
