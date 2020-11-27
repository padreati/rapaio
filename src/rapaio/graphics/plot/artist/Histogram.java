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

package rapaio.graphics.plot.artist;

import rapaio.core.stat.Quantiles;
import rapaio.data.Var;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptionFill;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.graphics.plot.Plot;
import rapaio.math.MTools;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static rapaio.graphics.Plotter.bins;

/**
 * Plot component which allows one to add a histogram to a plot.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Histogram extends Artist {

    private static final long serialVersionUID = -7990247895216501553L;

    private final Var v;
    private double[] freqTable;
    private double minValue;
    private double maxValue;

    public Histogram(Var v, GOption<?>... opts) {
        this(v, Double.NaN, Double.NaN, opts);
    }

    public Histogram(Var v, double minValue, double maxValue, GOption<?>... opts) {
        this.v = v;
        this.minValue = minValue;
        this.maxValue = maxValue;

        // default values for histogram
        options.setFill(new GOptionFill(new Color[]{options.getPalette().getColor(7)}));
        options.bind(opts);
    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.NUMERIC;
    }

    private int computeFreedmanDiaconisEstimation(Var v) {
        double[] q = Quantiles.of(v, 0, 0.25, 0.75, 1).values();
        double iqr = q[2] - q[1];
        return (int) Math.min(1024, Math.ceil((q[3] - q[0]) / (2 * iqr * Math.pow(v.stream().complete().count(), -1.0 / 3.0))));
    }

    @Override
    public void bind(Plot parent) {
        super.bind(parent);

        parent.yLab(options.getProb() ? "density" : "frequency");
        parent.xLab(v.name());
        parent.leftThick(true);
        parent.leftMarkers(true);
        parent.bottomThick(true);
        parent.bottomMarkers(true);
        if (options.getBins() == -1) {
            options.bind(bins(computeFreedmanDiaconisEstimation(v)));
        }
    }

    private void rebuild() {
        minValue = Double.NaN;
        maxValue = Double.NaN;

        for (int i = 0; i < v.size(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            if (!Double.isFinite(minValue)) {
                minValue = v.getDouble(i);
            } else {
                minValue = Math.min(minValue, v.getDouble(i));
            }
            if (!Double.isFinite(maxValue)) {
                maxValue = v.getDouble(i);
            } else {
                maxValue = Math.max(maxValue, v.getDouble(i));
            }
        }

        double step = (maxValue - minValue) / (1. * options.getBins());
        freqTable = new double[options.getBins()];
        if (freqTable.length == 0) {
            return;
        }
        double total = 0;
        for (int i = 0; i < v.size(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            total++;
            if (v.getDouble(i) < minValue || v.getDouble(i) > maxValue) {
                continue;
            }
            int index = (int) ((v.getDouble(i) - minValue) / step);
            index = MTools.cut(index, 0, freqTable.length - 1);
            freqTable[index]++;
        }

        if (options.getProb() && (total != 0)) {
            for (int i = 0; i < freqTable.length; i++) {
                freqTable[i] /= (total * step);
            }
        }
    }

    @Override
    public void updateDataRange() {
        rebuild();

        if (options.getHorizontal()) {
            union(Double.NaN, minValue);
            union(Double.NaN, maxValue);
            for (double freq : freqTable) {
                union(freq, Double.NaN);
            }
            union(0, Double.NaN);
        } else {
            union(minValue, Double.NaN);
            union(maxValue, Double.NaN);
            for (double freq : freqTable) {
                union(Double.NaN, freq);
            }
            union(Double.NaN, 0);
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(options.getLwd()));
        for (int i = 0; i < freqTable.length; i++) {
            double d = freqTable[i];
            double mind = Math.min(d, plot.yAxis().max());
            Composite old = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));
            double x = xScale(binStart(i));
            double y = yScale(mind);
            double w = xScale(binStart(i + 1)) - xScale(binStart(i));
            double h = yScale(0) - yScale(mind);

            if (getOptions().getHorizontal()) {
                mind = Math.min(d, plot.xAxis().max());
                x = xScale(0);
                y = yScale(binStart(i + 1));
                w = -(xScale(0) - xScale(mind));
                h = -(yScale(binStart(i + 1)) - yScale(binStart(i)));
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
        double fraction = (maxValue - minValue) / (1. * options.getBins());
        return minValue + fraction * i;
    }
}
