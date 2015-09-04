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

package rapaio.graphics.plot;

import rapaio.core.stat.Quantiles;
import rapaio.data.Var;
import rapaio.graphics.Plotter;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOpt;

import java.awt.*;
import java.util.Arrays;

import static rapaio.graphics.Plotter.bins;
import static rapaio.graphics.Plotter.color;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class Histogram extends PlotComponent {

    private static final long serialVersionUID = -7990247895216501553L;

    private final Var v;
    double[] freqTable;
    double minValue = Double.NaN;
    double maxValue = Double.NaN;

    public Histogram(Var v, GOpt... opts) {
        this(v, Double.NaN, Double.NaN, opts);
    }

    public Histogram(Var v, double minValue, double maxValue, GOpt... opts) {
        this.v = v;
        this.minValue = minValue;
        this.maxValue = maxValue;
        // default values for histogram
        options.apply(Plotter.color(7));
        options.apply(opts);
    }

    private int computeFreedmanDiaconisEstimation(Var v) {
        double[] q = new Quantiles(v, 0, 0.25, 0.75, 1).values();
        double iqr = q[2] - q[1];
        return (int) Math.ceil((q[3] - q[0]) / (2 * iqr * Math.pow(v.spotStream().complete().count(), -1.0 / 3.0)));
    }

    @Override
    public void initialize(Plot parent) {
        super.initialize(parent);

        parent.yLab(options.getProb() ? "density" : "frequency");
        parent.xLab(v.name());
        parent.leftThick(true);
        parent.leftMarkers(true);
        parent.bottomThick(true);
        parent.bottomMarkers(true);
        if (options.getBins() == -1) {
            options.apply(bins(computeFreedmanDiaconisEstimation(v)));
        }
    }

    public Histogram minValue(double minValue) {
        this.minValue = minValue;
        return this;
    }

    public Histogram maxValue(double maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    private void rebuild() {
        if (minValue != minValue) {
            for (int i = 0; i < v.rowCount(); i++) {
                if (v.missing(i)) {
                    continue;
                }
                if (minValue != minValue) {
                    minValue = v.value(i);
                } else {
                    minValue = Math.min(minValue, v.value(i));
                }
                if (maxValue != maxValue) {
                    maxValue = v.value(i);
                } else {
                    maxValue = Math.max(maxValue, v.value(i));
                }
            }
        }

        double step = (maxValue - minValue) / (1. * options.getBins());
        freqTable = new double[options.getBins()];
        double total = 0;
        for (int i = 0; i < v.rowCount(); i++) {
            if (v.missing(i)) {
                continue;
            }
            total++;
            if (v.value(i) < minValue || v.value(i) > maxValue) {
                continue;
            }
            int index = (int) ((v.value(i) - minValue) / step);
            if (index == freqTable.length)
                index--;
            freqTable[index]++;
        }

        if (options.getProb() && (total != 0)) {
            for (int i = 0; i < freqTable.length; i++) {
                freqTable[i] /= (total * step);
            }
        }
    }

    @Override
    public Range buildRange() {
        rebuild();
        Range range = new Range();
        range.union(minValue, Double.NaN);
        range.union(maxValue, Double.NaN);
        Arrays.stream(freqTable).sequential().forEach(t -> range.union(Double.NaN, t));
        range.union(Double.NaN, 0);
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {
        rebuild();

        g2d.setColor(ColorPalette.STANDARD.getColor(0));
        for (int i = 0; i < freqTable.length; i++) {
            double d = freqTable[i];
            double mind = Math.min(d, parent.getRange().y2());
            if (!parent.getRange().contains(binStart(i), 0)) {
                continue;
            }
            Composite old = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));
            int[] x;
            int[] y;
            g2d.setColor(ColorPalette.STANDARD.getColor(0));
            x = new int[]{
                    (int) parent.xScale(binStart(i)),
                    (int) parent.xScale(binStart(i)),
                    (int) parent.xScale(binStart(i + 1)),
                    (int) parent.xScale(binStart(i + 1)),
                    (int) parent.xScale(binStart(i)),};
            y = new int[]{
                    (int) parent.yScale(0),
                    (int) parent.yScale(mind),
                    (int) parent.yScale(mind),
                    (int) parent.yScale(0),
                    (int) parent.yScale(0)};
            g2d.drawPolyline(x, y, 5);

            if (d != 0) {
                x = new int[]{
                        (int) parent.xScale(binStart(i)) + 1,
                        (int) parent.xScale(binStart(i)) + 1,
                        (int) parent.xScale(binStart(i + 1)),
                        (int) parent.xScale(binStart(i + 1)),
                        (int) parent.xScale(binStart(i)) + 1
                };
                y = new int[]{
                        (int) parent.yScale(0),
                        (int) parent.yScale(mind) + ((d == mind) ? 1 : -2),
                        (int) parent.yScale(mind) + ((d == mind) ? 1 : -2),
                        (int) parent.yScale(0),
                        (int) parent.yScale(0)};
                g2d.setColor(options.getColor(i));
                g2d.fillPolygon(x, y, 5);
            }

            g2d.setComposite(old);
        }
    }

    private double binStart(int i) {
        double value = minValue;
        double fraction = 1. * (maxValue - minValue) / (1. * options.getBins());
        return value + fraction * (i);
    }
}
