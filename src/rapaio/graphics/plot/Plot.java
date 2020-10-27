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

import rapaio.core.distributions.empirical.KFunc;
import rapaio.data.Var;
import rapaio.experiment.grid.MeshGrid;
import rapaio.graphics.base.Figure;
import rapaio.graphics.base.XWilkinson;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptions;
import rapaio.graphics.plot.artist.ABLine;
import rapaio.graphics.plot.artist.DensityLine;
import rapaio.graphics.plot.artist.FunctionLine;
import rapaio.graphics.plot.artist.Histogram;
import rapaio.graphics.plot.artist.Histogram2D;
import rapaio.graphics.plot.artist.Legend;
import rapaio.graphics.plot.artist.Lines;
import rapaio.graphics.plot.artist.MeshContour;
import rapaio.graphics.plot.artist.Points;
import rapaio.graphics.plot.artist.ROCCurve;
import rapaio.graphics.plot.artist.Segment2D;
import rapaio.ml.eval.metric.ROC;
import rapaio.util.function.Double2DoubleFunction;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Plot implements Figure {

    private static final long serialVersionUID = 1898871481989584539L;

    protected static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 18);
    protected static final Font MARKERS_FONT = new Font("Verdana", Font.PLAIN, 13);
    protected static final Font LABELS_FONT = new Font("Verdana", Font.BOLD, 16);
    protected static final double DEFAULT_THICKER_MIN_SPACE = 130.;
    protected static final int THICKER_PAD = 7;
    protected static final int MARKER_PAD = 15;
    protected static final int LABEL_PAD = 30;
    protected static final int TITLE_PAD = 40;
    protected static final int MINIMUM_PAD = 20;

    final GOptions options = new GOptions();
    Axes axes = new Axes(this);

    Rectangle viewport;

    //
    protected String title;
    protected String yLabel;
    protected String xLabel;
    protected double thickerMinSpace = DEFAULT_THICKER_MIN_SPACE;

    //
    protected boolean leftThicker;
    protected boolean bottomThicker;
    protected boolean leftMarkers;
    protected boolean bottomMarkers;
    protected final ArrayList<String> bottomMarkersMsg = new ArrayList<>();
    protected final ArrayList<Double> bottomMarkersPos = new ArrayList<>();
    protected final ArrayList<String> leftMarkersMsg = new ArrayList<>();
    protected final ArrayList<Double> leftMarkersPos = new ArrayList<>();

    protected int sizeLeftThicker;
    protected int sizeBottomThicker;
    protected int sizeLeftMarkers;
    protected int sizeBottomMarkers;
    protected int sizeTitle;
    protected int sizeYLabel;
    protected int sizeXLabel;

    //
    public Plot(GOption<?>... opts) {
        bottomThick(true);
        bottomMarkers(true);
        leftThick(true);
        leftMarkers(true);
        this.options.bind(opts);
    }

    protected void buildViewport(Rectangle rectangle) {
        viewport = new Rectangle(rectangle);

        viewport.x += MINIMUM_PAD;
        viewport.width -= 2 * MINIMUM_PAD;

        viewport.y += MINIMUM_PAD;
        viewport.height -= 2 * MINIMUM_PAD;

        if (leftThicker) {
            sizeLeftThicker = 2 * THICKER_PAD;
        }
        if (leftMarkers) {
            sizeLeftMarkers = MARKER_PAD;
        }
        if (yLabel != null) {
            sizeYLabel = LABEL_PAD;
        }
        if (title != null) {
            sizeTitle = TITLE_PAD;
        }
        if (bottomThicker) {
            sizeBottomThicker = 2 * THICKER_PAD;
        }
        if (bottomMarkers) {
            sizeBottomMarkers = MARKER_PAD;
        }
        if (xLabel != null) {
            sizeXLabel = LABEL_PAD;
        }

        viewport.x += sizeLeftThicker + sizeLeftMarkers + sizeYLabel;
        viewport.width -= sizeLeftThicker + sizeLeftMarkers + sizeYLabel;

        viewport.y += sizeTitle;
        viewport.height -= sizeTitle;
        viewport.height -= sizeBottomThicker + sizeBottomMarkers + sizeXLabel;
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        buildViewport(rect);
        axes.buildDataRange();

        g2d.setColor(ColorPalette.STANDARD.getColor(255));
        g2d.fill(rect);

        g2d.setBackground(ColorPalette.STANDARD.getColor(255));
        g2d.setColor(ColorPalette.STANDARD.getColor(0));

        if (title != null) {
            g2d.setFont(TITLE_FONT);
            double titleWidth = g2d.getFontMetrics().getStringBounds(title, g2d).getWidth();
            g2d.drawString(title, (int) (rect.x + (rect.width - titleWidth) / 2), rect.y + TITLE_PAD);
        }

        // left part
        buildLeftMarkers();

        g2d.setFont(MARKERS_FONT);
        g2d.drawLine(viewport.x - THICKER_PAD,
                viewport.y,
                viewport.x - THICKER_PAD,
                viewport.y + viewport.height);

        for (int i = 0; i < leftMarkersPos.size(); i++) {
            if (leftThicker) {
                g2d.drawLine(
                        viewport.x - 2 * THICKER_PAD,
                        (int) (viewport.y + viewport.height - leftMarkersPos.get(i)),
                        viewport.x - THICKER_PAD,
                        (int) (viewport.y + viewport.height - leftMarkersPos.get(i)));
            }
            if (leftMarkers) {
                int xx = viewport.x - 3 * THICKER_PAD;
                int yy = (int) (viewport.y + viewport.height - leftMarkersPos.get(i)
                        + g2d.getFontMetrics().getStringBounds(leftMarkersMsg.get(i), g2d).getWidth() / 2);
                g2d.translate(xx, yy);
                g2d.rotate(-Math.PI / 2);
                g2d.drawString(leftMarkersMsg.get(i), 0, 0);
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

        // bottom part
        buildBottomMarkers();

        g2d.setFont(MARKERS_FONT);
        g2d.drawLine(viewport.x,
                viewport.y + viewport.height + THICKER_PAD,
                viewport.x + viewport.width,
                viewport.y + viewport.height + THICKER_PAD);

        for (int i = 0; i < bottomMarkersPos.size(); i++) {
            if (bottomThicker) {
                g2d.drawLine(
                        (int) (viewport.x + bottomMarkersPos.get(i)),
                        viewport.y + viewport.height + THICKER_PAD,
                        (int) (viewport.x + bottomMarkersPos.get(i)),
                        viewport.y + viewport.height + 2 * THICKER_PAD);
            }
            if (bottomMarkers) {
                g2d.drawString(
                        bottomMarkersMsg.get(i),
                        (int) (viewport.x + bottomMarkersPos.get(i)
                                - g2d.getFontMetrics().getStringBounds(bottomMarkersMsg.get(i), g2d).getWidth() / 2),
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

        for (Artist pc : axes.getArtistList()) {
            pc.paint(g2d);
        }
    }

    protected void buildNumericBottomMarkers() {
        bottomMarkersPos.clear();
        bottomMarkersMsg.clear();

        int xspots = (int) Math.floor(viewport.width / thickerMinSpace);
        if (xspots < 2) {
            return;
        }
        DataRange range = axes.getDataRange();
        XWilkinson.Labels xlabels = XWilkinson.base10(XWilkinson.DEEFAULT_EPS).searchBounded(
                range.xMin(), range.xMax(), xspots);

        for (double label : xlabels.getList()) {
            bottomMarkersPos.add((label - range.xMin()) * viewport.width / range.width());
            bottomMarkersMsg.add(xlabels.getFormattedValue(label));
        }
    }

    protected void buildNumericLeftMarkers() {
        leftMarkersPos.clear();
        leftMarkersMsg.clear();

        int yspots = (int) Math.floor(viewport.height / thickerMinSpace);
        if (yspots < 2) {
            return;
        }
        DataRange range = axes.getDataRange();
        XWilkinson.Labels ylabels = XWilkinson.base10(XWilkinson.DEEFAULT_EPS).searchBounded(
                range.yMin(), range.yMax(), yspots);

        for (double label : ylabels.getList()) {
            leftMarkersPos.add((label - range.yMin()) * viewport.height / range.height());
            leftMarkersMsg.add(String.valueOf(ylabels.getFormattedValue(label)));
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

    protected void buildLeftMarkers() {
        buildNumericLeftMarkers();
    }

    protected void buildBottomMarkers() {
        buildNumericBottomMarkers();
    }

    public Plot add(Artist pc) {
        pc.bind(axes);
        axes.addArtist(pc);
        return this;
    }

    // OPTIONS


    public Plot xLim(double start, double end) {
        axes.xLimStart = start;
        axes.xLimEnd = end;
        return this;
    }

    public Plot yLim(double start, double end) {
        axes.yLimStart = start;
        axes.yLimEnd = end;
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

    public Plot segment2d(double x1, double y1, double x2, double y2, GOption<?>... opts) {
        add(new Segment2D(x1, y1, x2, y2, opts));
        return this;
    }
}
