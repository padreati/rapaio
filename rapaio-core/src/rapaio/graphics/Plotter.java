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

package rapaio.graphics;

import static rapaio.graphics.opt.GOpts.bins;

import java.awt.image.BufferedImage;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.empirical.KFunc;
import rapaio.core.tools.DistanceMatrix;
import rapaio.core.tools.Grid2D;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.plot.GridLayer;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.ABLine;
import rapaio.graphics.plot.artist.BarPlot;
import rapaio.graphics.plot.artist.BoxPlot;
import rapaio.graphics.plot.artist.CorrGram;
import rapaio.graphics.plot.artist.DensityLine;
import rapaio.graphics.plot.artist.FunLine;
import rapaio.graphics.plot.artist.Histogram;
import rapaio.graphics.plot.artist.Histogram2D;
import rapaio.graphics.plot.artist.ImageArtist;
import rapaio.graphics.plot.artist.IsoCurves;
import rapaio.graphics.plot.artist.Lines;
import rapaio.graphics.plot.artist.Matrix;
import rapaio.graphics.plot.artist.Points;
import rapaio.graphics.plot.artist.PolyFill;
import rapaio.graphics.plot.artist.PolyLine;
import rapaio.graphics.plot.artist.PolyPath;
import rapaio.graphics.plot.artist.ROCCurve;
import rapaio.graphics.plot.artist.Text;
import rapaio.math.tensor.Tensor;
import rapaio.ml.eval.ClusterSilhouette;
import rapaio.ml.eval.metric.ROC;
import rapaio.util.function.Double2DoubleFunction;

public final class Plotter {

    public static GridLayer gridLayer(int rows, int cols, GOpt<?>... opts) {
        return GridLayer.of(rows, cols, opts);
    }

    public static Plot plot(GOpt<?>... opts) {
        return new Plot(opts);
    }

    public static Plot qqplot(Var points, Distribution dist, GOpt<?>... opts) {
        return plot().qqplot(points, dist, opts);
    }

    public static Plot boxplot(Var x, Var factor, GOpt<?>... opts) {
        return plot().add(new BoxPlot(x, factor, opts));
    }

    public static Plot boxplot(Var x, GOpt<?>... opts) {
        return plot().add(new BoxPlot(x, opts));
    }

    public static Plot boxplot(Var[] vars, GOpt<?>... opts) {
        return plot().add(new BoxPlot(vars, opts));
    }

    public static Plot boxplot(Frame df, GOpt<?>... opts) {
        return plot().add(new BoxPlot(df, opts));
    }

    public static Plot hist(Var v, GOpt<?>... opts) {
        return plot().add(new Histogram(v, opts));
    }

    public static Plot hist(Var v, double minValue, double maxValue, GOpt<?>... opts) {
        return plot().add(new Histogram(v, minValue, maxValue, opts));
    }

    public static Plot hist2d(Var x, Var y, GOpt<?>... opts) {
        return plot().add(new Histogram2D(x, y, opts));
    }

    public static Plot polyline(boolean closed, Var x, Var y, GOpt<?>... opts) {
        return plot().add(new PolyLine(closed, x, y, opts));
    }

    public static Plot polyfill(Var x, Var y, GOpt<?>... opts) {
        return plot().add(new PolyFill(x, y, opts));
    }

    public static Plot polyfill(PolyPath polyPath, GOpt<?>... opts) {
        return plot().add(new PolyFill(polyPath, opts));
    }

    public static Plot densityLine(Var var, GOpt<?>... opts) {
        return plot().add(new DensityLine(var, opts));
    }

    public static Plot densityLine(Var var, double bandwidth, GOpt<?>... opts) {
        return plot().add(new DensityLine(var, bandwidth, opts));
    }

    public static Plot densityLine(Var var, KFunc kfunc, GOpt<?>... opts) {
        return plot().add(new DensityLine(var, kfunc, opts));
    }

    public static Plot densityLine(Var var, KFunc kfunc, double bandwidth, GOpt<?>... opts) {
        return plot().add(new DensityLine(var, kfunc, bandwidth, opts));
    }

    public static Plot funLine(Double2DoubleFunction f, GOpt<?>... opts) {
        return plot().add(new FunLine(f, opts));
    }

    public static Plot lines(Var x, Var y, GOpt<?>... opts) {
        return plot().add(new Lines(x, y, opts));
    }

    public static Plot lines(Var y, GOpt<?>... opts) {
        return plot().add(new Lines(y, opts));
    }

    public static Plot points(Var x, Var y, GOpt<?>... opts) {
        return plot().add(new Points(x, y, opts));
    }

    public static Plot points(Var y, GOpt<?>... opts) {
        return plot().add(new Points(VarInt.seq(y.size()).name("pos"), y, opts));
    }

    public static Plot rocCurve(ROC roc, GOpt<?>... opts) {
        return plot().add(new ROCCurve(roc, opts));
    }

    public static Plot barplot(Var category, GOpt<?>... opts) {
        return plot().add(new BarPlot(category, null, null, opts));
    }

    public static Plot barplot(Var category, Var cond, GOpt<?>... opts) {
        return plot().add(new BarPlot(category, cond, null, opts));
    }

    public static Plot barplot(Var category, Var cond, Var numeric, GOpt<?>... opts) {
        return plot().add(new BarPlot(category, cond, numeric, opts));
    }

    public static Plot hLine(double a, GOpt<?>... opts) {
        return plot().add(new ABLine(true, a, opts));
    }

    public static Plot vLine(double a, GOpt<?>... opts) {
        return plot().add(new ABLine(false, a, opts));
    }

    public static Plot abLine(double a, double b, GOpt<?>... opts) {
        return plot().add(new ABLine(a, b, opts));
    }

    public static Plot corrGram(DistanceMatrix d, GOpt<?>... opts) {
        return corrGram(d, true, true, opts);
    }

    public static Plot corrGram(DistanceMatrix d, boolean labels, boolean grid, GOpt<?>... opts) {
        return plot().add(new CorrGram(d, labels, grid, opts));
    }

    public static Plot image(BufferedImage image, GOpt<?>... opts) {
        return plot().add(new ImageArtist(image, opts));
    }

    public static Plot text(double x, double y, String text, GOpt<?>... opts) {
        return plot().add(new Text(x, y, text, opts));
    }

    public static Plot matrix(Tensor<?> m, GOpt<?>... opts) {
        return plot().add(new Matrix(m, opts));
    }

    public static Plot isoCurves(Grid2D grid, double[] levels, GOpt<?>... opts) {
        return plot().add(new IsoCurves(grid, true, true, levels, opts));
    }

    public static Plot isoLines(Grid2D grid, double[] levels, GOpt<?>... opts) {
        return plot().add(new IsoCurves(grid, true, false, levels, opts));
    }

    public static Plot isoBands(Grid2D grid, double[] levels, GOpt<?>... opts) {
        return plot().add(new IsoCurves(grid, false, true, levels, opts));
    }

    public static Plot silhouette(ClusterSilhouette silhouette, GOpt<?>... opts) {
        return plot().silhouette(silhouette, opts);
    }

    public static GridLayer scatters(Frame df, GOpt<?>... opts) {
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

}
