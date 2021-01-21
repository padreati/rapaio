/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.experiment.grid.GridData;
import rapaio.experiment.ml.clustering.DistanceMatrix;
import rapaio.graphics.opt.ColorGradient;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptionAlpha;
import rapaio.graphics.opt.GOptionBins;
import rapaio.graphics.opt.GOptionColor;
import rapaio.graphics.opt.GOptionFill;
import rapaio.graphics.opt.GOptionFont;
import rapaio.graphics.opt.GOptionHAlign;
import rapaio.graphics.opt.GOptionHeights;
import rapaio.graphics.opt.GOptionHorizontal;
import rapaio.graphics.opt.GOptionLabels;
import rapaio.graphics.opt.GOptionLwd;
import rapaio.graphics.opt.GOptionPalette;
import rapaio.graphics.opt.GOptionPch;
import rapaio.graphics.opt.GOptionPoints;
import rapaio.graphics.opt.GOptionProb;
import rapaio.graphics.opt.GOptionSort;
import rapaio.graphics.opt.GOptionStacked;
import rapaio.graphics.opt.GOptionSz;
import rapaio.graphics.opt.GOptionTop;
import rapaio.graphics.opt.GOptionVAlign;
import rapaio.graphics.opt.GOptionWidths;
import rapaio.graphics.plot.GridLayer;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.ABLine;
import rapaio.graphics.plot.artist.BarPlot;
import rapaio.graphics.plot.artist.BoxPlot;
import rapaio.graphics.plot.artist.CorrGram;
import rapaio.graphics.plot.artist.DensityLine;
import rapaio.graphics.plot.artist.FunctionLine;
import rapaio.graphics.plot.artist.Histogram;
import rapaio.graphics.plot.artist.Histogram2D;
import rapaio.graphics.plot.artist.IsoCurves;
import rapaio.graphics.plot.artist.Lines;
import rapaio.graphics.plot.artist.Matrix;
import rapaio.graphics.plot.artist.Points;
import rapaio.graphics.plot.artist.PolyFill;
import rapaio.graphics.plot.artist.PolyLine;
import rapaio.graphics.plot.artist.ROCCurve;
import rapaio.graphics.plot.artist.Text;
import rapaio.math.linear.DMatrix;
import rapaio.ml.eval.metric.ROC;
import rapaio.util.function.Double2DoubleFunction;

import java.awt.*;

public final class Plotter {

    public static GridLayer gridLayer(int rows, int cols, GOption<?>... opts) {
        return GridLayer.of(rows, cols, opts);
    }

    public static Plot plot(GOption<?>... opts) {
        return new Plot(opts);
    }

    public static Plot qqplot(Var points, Distribution dist, GOption<?>... opts) {
        return plot().qqplot(points, dist, opts);
    }

    public static Plot boxplot(Var x, Var factor, GOption<?>... opts) {
        return plot().add(new BoxPlot(x, factor, opts));
    }

    public static Plot boxplot(Var x, GOption<?>... opts) {
        return plot().add(new BoxPlot(x, opts));
    }

    public static Plot boxplot(Var[] vars, GOption<?>... opts) {
        return plot().add(new BoxPlot(vars, opts));
    }

    public static Plot boxplot(Frame df, GOption<?>... opts) {
        return plot().add(new BoxPlot(df, opts));
    }

    public static Plot hist(Var v, GOption<?>... opts) {
        return plot().add(new Histogram(v, opts));
    }

    public static Plot hist(Var v, double minValue, double maxValue, GOption<?>... opts) {
        return plot().add(new Histogram(v, minValue, maxValue, opts));
    }

    public static Plot hist2d(Var x, Var y, GOption<?>... opts) {
        return plot().add(new Histogram2D(x, y, opts));
    }

    public static Plot polyline(boolean closed, Var x, Var y, GOption<?>... opts) {
        return plot().add(new PolyLine(closed, x, y, opts));
    }

    public static Plot polyfill(Var x, Var y, GOption<?>... opts) {
        return plot().add(new PolyFill(x, y, opts));
    }

    public static Plot densityLine(Var var, GOption<?>... opts) {
        return plot().add(new DensityLine(var, opts));
    }

    public static Plot densityLine(Var var, double bandwidth, GOption<?>... opts) {
        return plot().add(new DensityLine(var, bandwidth, opts));
    }

    public static Plot densityLine(Var var, KFunc kfunc, GOption<?>... opts) {
        return plot().add(new DensityLine(var, kfunc, opts));
    }

    public static Plot densityLine(Var var, KFunc kfunc, double bandwidth, GOption<?>... opts) {
        return plot().add(new DensityLine(var, kfunc, bandwidth, opts));
    }

    public static Plot funLine(Double2DoubleFunction f, GOption<?>... opts) {
        return plot().add(new FunctionLine(f, opts));
    }

    public static Plot lines(Var x, Var y, GOption<?>... opts) {
        return plot().add(new Lines(x, y, opts));
    }

    public static Plot lines(Var y, GOption<?>... opts) {
        return plot().add(new Lines(y, opts));
    }

    public static Plot points(Var x, Var y, GOption<?>... opts) {
        return plot().add(new Points(x, y, opts));
    }

    public static Plot points(Var y, GOption<?>... opts) {
        return plot().add(new Points(VarInt.seq(y.size()).name("pos"), y, opts));
    }

    public static Plot rocCurve(ROC roc, GOption<?>... opts) {
        return plot().add(new ROCCurve(roc, opts));
    }

    public static Plot barplot(Var category, GOption<?>... opts) {
        return plot().add(new BarPlot(category, null, null, opts));
    }

    public static Plot barplot(Var category, Var cond, GOption<?>... opts) {
        return plot().add(new BarPlot(category, cond, null, opts));
    }

    public static Plot barplot(Var category, Var cond, Var numeric, GOption<?>... opts) {
        return plot().add(new BarPlot(category, cond, numeric, opts));
    }

    public static Plot hLine(double a, GOption<?>... opts) {
        return plot().add(new ABLine(true, a, opts));
    }

    public static Plot vLine(double a, GOption<?>... opts) {
        return plot().add(new ABLine(false, a, opts));
    }

    public static Plot abLine(double a, double b, GOption<?>... opts) {
        return plot().add(new ABLine(a, b, opts));
    }

    public static Plot corrGram(DistanceMatrix d, GOption<?>... opts) {
        return corrGram(d, true, true, opts);
    }

    public static Plot corrGram(DistanceMatrix d, boolean labels, boolean grid, GOption<?>... opts) {
        return plot().add(new CorrGram(d, labels, grid, opts));
    }

    public static Plot text(double x, double y, String text, GOption<?>... opts) {
        return plot().add(new Text(x, y, text, opts));
    }

    public static Plot matrix(DMatrix m, GOption<?>... opts) {
        return plot().add(new Matrix(m, opts));
    }

    public static Plot isoCurves(GridData grid, ColorGradient gradient, double[] levels, GOption<?>... opts) {
        return plot().add(new IsoCurves(grid, true, true, gradient, levels, opts));
    }

    public static Plot isoLines(GridData grid, ColorGradient gradient, double[] levels, GOption<?>... opts) {
        return plot().add(new IsoCurves(grid, true, false, gradient, levels, opts));
    }

    public static Plot isoBands(GridData grid, ColorGradient gradient, double[] levels, GOption<?>... opts) {
        return plot().add(new IsoCurves(grid, false, true, gradient, levels, opts));
    }

    public static GridLayer scatters(Frame df, GOption<?>... opts) {
        var grid = gridLayer(df.varCount(), df.varCount());
        for (int i = 0; i < df.varCount(); i++) {
            for (int j = 0; j < df.varCount(); j++) {
                if (i == j) {
                    grid.add(hist(df.rvar(i), bins(40)).title(df.rvar(i).name())
                            .leftMarkers(true).leftThick(true)
                            .bottomMarkers(true).bottomThick(true)
                            .yLab(null).xLab(null)
                    );
                } else {
                    grid.add(points(df.rvar(j), df.rvar(i), opts).xLab(null).yLab(null));
                }
            }
        }
        return grid;
    }

    // GRAPHICAL OPTIONS

    public static GOptionPalette palette(ColorPalette colorPalette) {
        return new GOptionPalette(colorPalette);
    }

    public static GOptionColor color(int... index) {
        return new GOptionColor(index);
    }

    public static GOptionColor color(Color color) {
        return new GOptionColor(color);
    }

    public static GOptionColor color(Color[] colors) {
        return new GOptionColor(colors);
    }

    public static GOptionColor color(Var color) {
        return new GOptionColor(color);
    }

    public static GOptionFill fill(int... index) {
        return new GOptionFill(index);
    }

    public static GOptionFill fill(Color color) {
        return new GOptionFill(color);
    }

    public static GOptionFill fill(Color[] colors) {
        return new GOptionFill(colors);
    }

    public static GOptionFill fill(Var color) {
        return new GOptionFill(color);
    }

    public static GOptionLwd lwd(float lwd) {
        return new GOptionLwd(lwd);
    }

    public static GOptionSz sz(Var sizeIndex) {
        return sz(sizeIndex, 1);
    }

    public static GOptionSz sz(Var sizeIndex, double factor) {
        return sz(sizeIndex, factor, 0);
    }

    public static GOptionSz sz(Var sizeIndex, double factor, double offset) {
        VarDouble size = sizeIndex
                .stream()
                .mapToDouble()
                .map(x -> x * factor + offset)
                .boxed()
                .collect(VarDouble.collector());
        return new GOptionSz(size);
    }

    public static GOptionSz sz(double size) {
        return new GOptionSz(VarDouble.scalar(size));
    }

    public static GOptionPch pch(Var pchIndex, int... mapping) {
        VarInt pch = VarInt.from(pchIndex.size(), row -> {
            int i = pchIndex.getInt(row);
            if (i >= 0 && i < mapping.length) {
                return mapping[i];
            }
            return mapping[0];
        });
        return new GOptionPch(pch);
    }

    public static GOptionPch pch(int pch) {
        return new GOptionPch(VarInt.scalar(pch));
    }

    public static GOptionAlpha alpha(float alpha) {
        return new GOptionAlpha(alpha);
    }

    public static GOptionBins bins(int bins) {
        return new GOptionBins(bins);
    }

    public static GOptionProb prob(boolean prob) {
        return new GOptionProb(prob);
    }

    public static GOptionStacked stacked(boolean stacked) {
        return new GOptionStacked(stacked);
    }

    public static GOptionPoints points(int points) {
        return new GOptionPoints(points);
    }

    public static GOptionTop top(int top) {
        return new GOptionTop(top);
    }

    public static final int SORT_DESC = -1;
    public static final int SORT_NONE = 0;
    public static final int SORT_ASC = 1;

    public static GOptionSort sort(int sort) {
        return new GOptionSort(sort);
    }

    public static GOptionHorizontal horizontal(boolean horizontal) {
        return new GOptionHorizontal(horizontal);
    }

    public static GOptionLabels labels(String... labels) {
        return new GOptionLabels(labels);
    }

    public static GOptionWidths widths(double... relativeSizes) {
        return new GOptionWidths(relativeSizes);
    }

    public static GOptionWidths widths(int... absoluteSizes) {
        return new GOptionWidths(absoluteSizes);
    }

    public static GOptionHeights heights(double... relativeSizes) {
        return new GOptionHeights(relativeSizes);
    }

    public static GOptionHeights heights(int... absoluteSizes) {
        return new GOptionHeights(absoluteSizes);
    }

    public static final int HALIGN_LEFT = -1;
    public static final int HALIGN_CENTER = 0;
    public static final int HALIGN_RIGHT = 1;

    public static GOptionHAlign hAlign(int hAlign) {
        return new GOptionHAlign(hAlign);
    }

    public static final int VALIGN_TOP = -1;
    public static final int VALIGN_CENTER = 0;
    public static final int VALIGN_BOTTOM = 1;

    public static GOptionVAlign vAlign(int vAlign) {
        return new GOptionVAlign(vAlign);
    }

    public static GOptionFont font(Font font) {
        return new GOptionFont(font);
    }

    public static GOptionFont font(String fontName) {
        return new GOptionFont(fontName);
    }

    public static GOptionFont font(String fontName, int style, int size) {
        return new GOptionFont(new Font(fontName, style, size));
    }
}
