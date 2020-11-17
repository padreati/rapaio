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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.graphics.plot;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.empirical.KFunc;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.filter.VSort;
import rapaio.experiment.grid.MeshGrid;
import rapaio.graphics.base.Figure;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptions;
import rapaio.graphics.plot.artist.ABLine;
import rapaio.graphics.plot.artist.BarPlot;
import rapaio.graphics.plot.artist.BoxPlot;
import rapaio.graphics.plot.artist.DensityLine;
import rapaio.graphics.plot.artist.FunctionLine;
import rapaio.graphics.plot.artist.Histogram;
import rapaio.graphics.plot.artist.Histogram2D;
import rapaio.graphics.plot.artist.Legend;
import rapaio.graphics.plot.artist.Lines;
import rapaio.graphics.plot.artist.MeshContour;
import rapaio.graphics.plot.artist.Points;
import rapaio.graphics.plot.artist.ROCCurve;
import rapaio.graphics.plot.artist.Segment;
import rapaio.ml.eval.metric.ROC;
import rapaio.util.function.Double2DoubleFunction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Plot implements Figure {

    private static final long serialVersionUID = 1898871481989584539L;

    protected static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 18);
    protected static final Font MARKERS_FONT = new Font("Verdana", Font.PLAIN, 13);
    protected static final Font LABELS_FONT = new Font("Verdana", Font.BOLD, 16);
    protected static final double DEFAULT_THICKER_MIN_SPACE = 110.;
    protected static final int THICKER_PAD = 7;
    protected static final int MARKER_PAD = 15;
    protected static final int LABEL_PAD = 30;
    protected static final int TITLE_PAD = 40;
    protected static final int MINIMUM_PAD = 20;

    final GOptions options = new GOptions();

    Rectangle viewport;

    protected String title;
    protected String yLabel;
    protected String xLabel;
    protected double thickerMinSpace = DEFAULT_THICKER_MIN_SPACE;

    protected boolean leftThicker;
    protected boolean bottomThicker;
    protected boolean leftMarkers;
    protected boolean bottomMarkers;

    private final List<Artist> artistList = new ArrayList<>();

    private Axis xAxis;
    private Axis yAxis;

    protected double xLimStart = Double.NaN;
    protected double xLimEnd = Double.NaN;
    protected double yLimStart = Double.NaN;
    protected double yLimEnd = Double.NaN;

    public Plot(GOption<?>... opts) {
        bottomThick(true);
        bottomMarkers(true);
        leftThick(true);
        leftMarkers(true);
        this.options.bind(opts);
    }

    public void addArtist(Artist artist) {
        if (artistList.isEmpty()) {
            this.xAxis = artist.newXAxis();
            this.yAxis = artist.newYAxis();
        }
        this.artistList.add(artist);
    }

    public Axis xAxis() {
        return xAxis;
    }

    public Axis yAxis() {
        return yAxis;
    }

    protected void buildDataRange() {
        xAxis.clear();
        yAxis.clear();

        for (Artist artist : artistList) {
            artist.updateDataRange();
        }

        xAxis.computeArtifacts(viewport.width, xLimStart, xLimEnd);
        yAxis.computeArtifacts(viewport.height, yLimStart, yLimEnd);
    }

    protected void buildViewport(Rectangle rectangle) {
        viewport = new Rectangle(rectangle);

        viewport.x += MINIMUM_PAD;
        viewport.width -= 2 * MINIMUM_PAD;

        viewport.y += MINIMUM_PAD;
        viewport.height -= 2 * MINIMUM_PAD;

        int sizeTitle = (title != null) ? TITLE_PAD : 0;

        int sizeLeftThicker = (leftThicker) ? 2 * THICKER_PAD : 0;
        int sizeLeftMarkers = (leftMarkers) ? MARKER_PAD : 0;
        int sizeYLabel = (yLabel != null) ? LABEL_PAD : 0;

        int sizeBottomThicker = (bottomThicker) ? 2 * THICKER_PAD : 0;
        int sizeBottomMarkers = (bottomMarkers) ? MARKER_PAD : 0;
        int sizeXLabel = (xLabel != null) ? LABEL_PAD : 0;

        viewport.x += sizeLeftThicker + sizeLeftMarkers + sizeYLabel;
        viewport.width -= sizeLeftThicker + sizeLeftMarkers + sizeYLabel;

        viewport.y += sizeTitle;
        viewport.height -= sizeTitle;
        viewport.height -= sizeBottomThicker + sizeBottomMarkers + sizeXLabel;
    }

    public double xScale(double x) {
        return viewport.x + viewport.width * (x - xAxis.min()) / xAxis.length();
    }

    public double yScale(double y) {
        return viewport.y + viewport.height * (1. - (y - yAxis.min()) / yAxis.length());
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        buildViewport(rect);
        buildDataRange();

        g2d.setColor(ColorPalette.STANDARD.getColor(255));
        g2d.fill(rect);

        g2d.setBackground(ColorPalette.STANDARD.getColor(255));
        g2d.setColor(ColorPalette.STANDARD.getColor(0));

        if (title != null) {
            g2d.setFont(TITLE_FONT);
            double titleWidth = g2d.getFontMetrics().getStringBounds(title, g2d).getWidth();
            g2d.drawString(title, (int) (rect.x + (rect.width - titleWidth) / 2), rect.y + TITLE_PAD);
        }

        g2d.setFont(MARKERS_FONT);
        g2d.drawLine(viewport.x - THICKER_PAD,
                viewport.y,
                viewport.x - THICKER_PAD,
                viewport.y + viewport.height);

        for (int i = 0; i < yAxis.tickers().size(); i++) {
            if (leftThicker) {
                g2d.drawLine(
                        viewport.x - 2 * THICKER_PAD,
                        (int) (viewport.y + viewport.height - yAxis.tickers().get(i)),
                        viewport.x - THICKER_PAD,
                        (int) (viewport.y + viewport.height - yAxis.tickers().get(i)));
            }
            if (leftMarkers) {
                int xx = viewport.x - 3 * THICKER_PAD;
                int yy = (int) (viewport.y + viewport.height - yAxis.tickers().get(i)
                        + g2d.getFontMetrics().getStringBounds(yAxis.labels().get(i), g2d).getWidth() / 2);
                g2d.translate(xx, yy);
                g2d.rotate(-Math.PI / 2);
                g2d.drawString(yAxis.labels().get(i), 0, 0);
                g2d.rotate(Math.PI / 2);
                g2d.translate(-xx, -yy);
            }
        }
        if (yLabel != null) {
            g2d.setFont(LABELS_FONT);
            double ywidth = g2d.getFontMetrics().getStringBounds(yLabel, g2d).getWidth();
            int xx = viewport.x - 5 * THICKER_PAD - MARKER_PAD;
            int yy = (int) (viewport.y + ywidth + (viewport.height - ywidth) / 2);
            g2d.translate(xx, yy);
            g2d.rotate(-Math.PI / 2);
            g2d.drawString(yLabel, 0, 0);
            g2d.rotate(Math.PI / 2);
            g2d.translate(-xx, -yy);
        }


        g2d.setFont(MARKERS_FONT);
        g2d.drawLine(viewport.x,
                viewport.y + viewport.height + THICKER_PAD,
                viewport.x + viewport.width,
                viewport.y + viewport.height + THICKER_PAD);

        for (int i = 0; i < xAxis.tickers().size(); i++) {
            if (bottomThicker) {
                g2d.drawLine(
                        (int) (viewport.x + xAxis.tickers().get(i)),
                        viewport.y + viewport.height + THICKER_PAD,
                        (int) (viewport.x + xAxis.tickers().get(i)),
                        viewport.y + viewport.height + 2 * THICKER_PAD);
            }
            if (bottomMarkers) {
                g2d.drawString(
                        xAxis.labels().get(i),
                        (int) (viewport.x + xAxis.tickers().get(i)
                                - g2d.getFontMetrics().getStringBounds(xAxis.labels().get(i), g2d).getWidth() / 2),
                        viewport.y + viewport.height + 2 * THICKER_PAD + MARKER_PAD
                );
            }
        }

        if (xLabel != null) {
            g2d.setFont(LABELS_FONT);
            double xwidth = g2d.getFontMetrics().getStringBounds(xLabel, g2d).getWidth();
            g2d.drawString(xLabel,
                    (int) (viewport.x + (viewport.width - xwidth) / 2),
                    viewport.y + viewport.height + 2 * THICKER_PAD + MARKER_PAD + LABEL_PAD);
        }

        for (Artist pc : artistList) {
            pc.paint(g2d);
        }
    }

    public Plot leftThick(boolean leftThicker) {
        this.leftThicker = leftThicker;
        return this;
    }

    public Plot bottomThick(boolean bottomThicker) {
        this.bottomThicker = bottomThicker;
        return this;
    }

    public Plot leftMarkers(boolean leftMarkers) {
        this.leftMarkers = leftMarkers;
        return this;
    }

    public Plot bottomMarkers(boolean bottomMarkers) {
        this.bottomMarkers = bottomMarkers;
        return this;
    }

    public Plot thickMinSpace(double minSpace) {
        thickerMinSpace = minSpace;
        return this;
    }

    public Plot title(String title) {
        this.title = title;
        return this;
    }

    public Plot xLab(String xLabel) {
        this.xLabel = xLabel;
        return this;
    }

    public Plot yLab(String yLabel) {
        this.yLabel = yLabel;
        return this;
    }

    public Plot add(Artist pc) {
        pc.bind(this);
        addArtist(pc);
        return this;
    }

    public Plot xLim(double start, double end) {
        xLimStart = start;
        xLimEnd = end;
        return this;
    }

    public Plot yLim(double start, double end) {
        yLimStart = start;
        yLimEnd = end;
        return this;
    }

    // COMPONENTS

    public Plot hist(Var v, GOption<?>... opts) {
        add(new Histogram(v, opts));
        return this;
    }

    public Plot hist(Var v, double minValue, double maxValue, GOption<?>... opts) {
        add(new Histogram(v, minValue, maxValue, opts));
        return this;
    }

    public Plot hist2d(Var x, Var y, GOption<?>... opts) {
        add(new Histogram2D(x, y, opts));
        return this;
    }

    public Plot points(Var x, Var y, GOption<?>... opts) {
        add(new Points(x, y, opts));
        return this;
    }

    public Plot lines(Var y, GOption<?>... opts) {
        add(new Lines(y, opts));
        return this;
    }

    public Plot lines(Var x, Var y, GOption<?>... opts) {
        add(new Lines(x, y, opts));
        return this;
    }

    public Plot hLine(double a, GOption<?>... opts) {
        add(new ABLine(true, a, opts));
        return this;
    }

    public Plot vLine(double a, GOption<?>... opts) {
        add(new ABLine(false, a, opts));
        return this;
    }

    public Plot abLine(double a, double b, GOption<?>... opts) {
        add(new ABLine(a, b, opts));
        return this;
    }

    public Plot funLine(Double2DoubleFunction f, GOption<?>... opts) {
        add(new FunctionLine(f, opts));
        return this;
    }

    public Plot densityLine(Var var, GOption<?>... opts) {
        add(new DensityLine(var, opts));
        return this;
    }

    public Plot densityLine(Var var, double bandwidth, GOption<?>... opts) {
        add(new DensityLine(var, bandwidth, opts));
        return this;
    }

    public Plot densityLine(Var var, KFunc kfunc, GOption<?>... opts) {
        add(new DensityLine(var, kfunc, opts));
        return this;
    }

    public Plot densityLine(Var var, KFunc kfunc, double bandwidth, GOption<?>... opts) {
        add(new DensityLine(var, kfunc, bandwidth, opts));
        return this;
    }

    public Plot rocCurve(ROC roc, GOption<?>... opts) {
        add(new ROCCurve(roc, opts));
        return this;
    }

    public Plot meshContour(MeshGrid mg, boolean contour, boolean fill, GOption<?>... opts) {
        add(new MeshContour(mg, contour, fill, opts));
        return this;
    }

    public Plot legend(double x, double y, GOption<?>... opts) {
        add(new Legend(x, y, opts));
        return this;
    }

    public Plot legend(int place, GOption<?>... opts) {
        add(new Legend(place, opts));
        return this;
    }

    public Plot segmentLine(double x1, double y1, double x2, double y2, GOption<?>... opts) {
        return segment(Segment.Type.LINE, x1, y1, x2, y2, opts);
    }

    public Plot segmentArrow(double x1, double y1, double x2, double y2, GOption<?>... opts) {
        return segment(Segment.Type.ARROW, x1, y1, x2, y2, opts);
    }

    public Plot segment(Segment.Type type, double x1, double y1, double x2, double y2, GOption<?>... opts) {
        add(new Segment(type, x1, y1, x2, y2, opts));
        return this;
    }

    public Plot boxplot(Var x, Var factor, GOption<?>... opts) {
        add(new BoxPlot(x, factor, opts));
        return this;
    }

    public Plot boxplot(Var x, GOption<?>... opts) {
        add(new BoxPlot(x, opts));
        return this;
    }

    public Plot boxplot(Var[] vars, GOption<?>... opts) {
        add(new BoxPlot(vars, opts));
        return this;
    }

    public Plot boxplot(Frame df, GOption<?>... opts) {
        add(new BoxPlot(df, opts));
        return this;
    }

    public Plot barplot(Var category, GOption<?>... opts) {
        add(new BarPlot(category, null, null, opts));
        return this;
    }

    public Plot barplot(Var category, Var cond, GOption<?>... opts) {
        add(new BarPlot(category, cond, null, opts));
        return this;
    }

    public Plot barplot(Var category, Var cond, Var numeric, GOption<?>... opts) {
        add(new BarPlot(category, cond, numeric, opts));
        return this;
    }

    public Plot qqplot(Var points, Distribution distribution, GOption<?>... opts) {
        Var x = VSort.asc().fapply(points);
        Var y = VarDouble.empty(x.rowCount());
        for (int i = 0; i < y.rowCount(); i++) {
            double p = (i + 1) / (y.rowCount() + 1.);
            y.setDouble(i, distribution.quantile(p));
        }
        add(new Points(y, x, opts));
        yLab("Sampling Quantiles");
        xLab("Theoretical Quantiles");
        title("QQPlot - sample vs. " + distribution.name());
        return this;
    }
}
