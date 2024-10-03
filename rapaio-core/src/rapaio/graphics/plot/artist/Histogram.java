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

package rapaio.graphics.plot.artist;

import static rapaio.graphics.opt.GOpts.bins;
import static rapaio.graphics.opt.GOpts.fill;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serial;

import rapaio.core.tools.HistogramTable;
import rapaio.data.Var;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.opt.GOpts;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.graphics.plot.Plot;
import rapaio.math.tensor.Tensor;

/**
 * Plot component which allows one to add a histogram to a plot.
 * <p>
 * Histogram frequency bins are computed in the interval specified
 * by parameters {@code rangeMinValue} and {@code rangeMaxValue} specified at
 * construction time. If those values are equal with {@code Double.NaN},
 * than minimum, respectively maximum values are used instead.
 * <p>
 * The number of bins could be given as parameter through graphical
 * {@link GOpts#bins(int)} option. If this is missing, the number
 * of bins is computed through Friedman-Diaconis estimator.
 * <p>
 * The range of displayed values is controlled through {@link Plot#xLim(double, double)}
 * and {@link Plot#yLim(double, double)} constructs. As such, it is possible
 * to compute the number of bins in an interval which is not the same as the
 * displayed interval.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Histogram extends Artist {

    @Serial
    private static final long serialVersionUID = -7990247895216501553L;

    private final String varName;
    private final HistogramTable hist;

    public Histogram(Var v, GOpt<?>... opts) {
        this(v, Double.NaN, Double.NaN, opts);
    }

    public Histogram(Var v, double rangeMinValue, double rangeMaxValue, GOpt<?>... opts) {
        // default values for histogram
        options = new GOpts().apply(fill(7)).apply(opts);

        this.varName = v.name();
        this.hist = new HistogramTable(v, rangeMinValue, rangeMaxValue, options.getBins());
    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.newNumeric();
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.newNumeric();
    }

    @Override
    public void bind(Plot parent) {
        super.bind(parent);

        parent.yLab(options.getProb() ? "density" : "frequency");
        parent.xLab(varName);
        parent.leftThick(true);
        parent.leftMarkers(true);
        parent.bottomThick(true);
        parent.bottomMarkers(true);
        if (options.getBins() == -1) {
            options = options.bind(bins(hist.bins()));
        }
    }

    private Tensor<Double> freqTable;

    private void buildData() {
        freqTable = hist.freq().copy();
        if (getOptions().getProb()) {
            double step = (hist.max() - hist.min()) / hist.bins();
            freqTable.div(freqTable.sum() * step);
        }
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        buildData();
        if (options.getHorizontal()) {
            plot.yAxis().domain().unionNumeric(hist.min());
            plot.yAxis().domain().unionNumeric(hist.max());
            for (double freq : freqTable) {
                plot.xAxis().domain().unionNumeric(freq);
            }
            plot.xAxis().domain().unionNumeric(0);
        } else {
            plot.xAxis().domain().unionNumeric(hist.min());
            plot.xAxis().domain().unionNumeric(hist.max());
            for (double freq : freqTable) {
                plot.yAxis().domain().unionNumeric(freq);
            }
            plot.yAxis().domain().unionNumeric(0);
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(options.getLwd()));
        for (int i = 0; i < freqTable.size(); i++) {
            double d = freqTable.getDouble(i);
            Composite old = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));


            double mind;
            double x;
            double y;
            double w;
            double h;

            double minBin = Math.min(binStart(i), binStart(i + 1));
            double maxBin = Math.max(binStart(i), binStart(i + 1));

            if (getOptions().getHorizontal()) {
                if (maxBin > plot.yAxis().max() || minBin < plot.yAxis().min()) {
                    continue;
                }
                mind = Math.min(d, plot.xAxis().max());
                x = xScale(0);
                y = yScale(binStart(i + 1));
                w = -(xScale(0) - xScale(mind));
                h = -(yScale(binStart(i + 1)) - yScale(binStart(i)));
            } else {
                if (maxBin > plot.xAxis().max() || minBin < plot.xAxis().min()) {
                    continue;
                }
                mind = Math.min(d, plot.yAxis().max());
                x = xScale(binStart(i));
                y = yScale(mind);
                w = xScale(binStart(i + 1)) - xScale(binStart(i));
                h = yScale(0) - yScale(mind);
            }

            if (d != 0) {
                g2d.setColor(options.getFill(i));
                g2d.fill(new Rectangle2D.Double(x, y, w, h));
            }
            g2d.setColor(options.getColor(i));
            g2d.draw(new Rectangle2D.Double(x, y, w, h));
            g2d.setComposite(old);
        }
    }

    private double binStart(int i) {
        double fraction = (hist.max() - hist.min()) / hist.bins();
        return hist.min() + fraction * i;
    }
}
