/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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
 */

package rapaio.graphics.plot;

import rapaio.data.OneIndexVector;
import rapaio.data.Vector;
import rapaio.graphics.Plot;
import rapaio.graphics.base.Range;
import rapaio.graphics.colors.ColorPalette;

import java.awt.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class HistogramBars extends PlotComponent {

    private final Vector v;
    int bins = 20;
    boolean prob = false;
    boolean rebuild = true;
    double[] freqtable;
    double minvalue = Double.NaN;
    double maxvalue = Double.NaN;

    public HistogramBars(Plot parent, Vector v) {
        this(parent, v, 30, true);
    }

    public HistogramBars(Plot parent, Vector v, int bins, boolean prob) {
        this(parent, v, bins, prob, Double.NaN, Double.NaN);
    }

    public HistogramBars(Plot parent, Vector v, int bins, boolean prob, double minvalue, double maxvalue) {
        super(parent);
        this.v = v;
        this.bins = bins;
        this.prob = prob;
        this.rebuild = true;
        this.minvalue = minvalue;
        this.maxvalue = maxvalue;

        plot.setLeftLabel(prob ? "density" : "frequency");
        plot.setLeftThicker(true);
        plot.setLeftMarkers(true);
        plot.setBottomThicker(true);
        plot.setBottomMarkers(true);
    }

    public int getBins() {
        return bins;
    }

    public void setBins(int bins) {
        this.bins = bins;
        rebuild = true;
    }

    public boolean isProb() {
        return prob;
    }

    public void setProb(boolean prob) {
        this.prob = prob;
        rebuild = true;
    }

    public void setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
    }

    private void rebuild() {
        if (rebuild) {
            rebuild = false;

            if (minvalue != minvalue) {
                for (int i = 0; i < v.getRowCount(); i++) {
                    if (v.isMissing(i)) {
                        continue;
                    }
                    if (minvalue != minvalue) {
                        minvalue = v.getValue(i);
                    } else {
                        minvalue = Math.min(minvalue, v.getValue(i));
                    }
                    if (maxvalue != maxvalue) {
                        maxvalue = v.getValue(i);
                    } else {
                        maxvalue = Math.max(maxvalue, v.getValue(i));
                    }
                }
            }

            double step = (maxvalue - minvalue) / (1. * bins);
            freqtable = new double[bins];
            double total = 0;
            for (int i = 0; i < v.getRowCount(); i++) {
                if (v.isMissing(i)) {
                    continue;
                }
                total++;
                if (v.getValue(i) < minvalue || v.getValue(i) > maxvalue) {
                    continue;
                }
                int index = (int) ((v.getValue(i) - minvalue) / step);
                if (index == freqtable.length)
                    index--;
                freqtable[index]++;
            }

            if (prob && (total != 0)) {
                for (int i = 0; i < freqtable.length; i++) {
                    freqtable[i] /= (total * step);
                }
            }

            // defaults
            if (opt().getColorIndex().getRowCount() == 1 && opt().getColorIndex().getIndex(0) == 0) {
                opt().setColorIndex(new OneIndexVector(7));
            }

        }
    }

    @Override
    public Range getComponentDataRange() {
        rebuild();

        Range range = new Range();
        if (opt().getXRangeStart() != opt().getXRangeStart()) {
            range.union(minvalue, Double.NaN);
            range.union(maxvalue, Double.NaN);
        } else {
            range.union(opt().getXRangeStart(), Double.NaN);
            range.union(opt().getXRangeEnd(), Double.NaN);
        }
        if (opt().getYRangeStart() != opt().getYRangeStart()) {
            for (int i = 0; i < freqtable.length; i++) {
                range.union(Double.NaN, freqtable[i]);
            }
            range.union(Double.NaN, 0);
        } else {
            range.union(Double.NaN, 0);
            range.union(Double.NaN, opt().getYRangeEnd());
        }

        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {
        rebuild();

        g2d.setColor(ColorPalette.STANDARD.getColor(0));
        for (int i = 0; i < freqtable.length; i++) {
            double d = freqtable[i];
            if (!plot.getRange().contains(binStart(i), 0)) {
                continue;
            }
            if (!plot.getRange().contains(binStart(i + 1), d)) {
                continue;
            }
            g2d.setColor(ColorPalette.STANDARD.getColor(0));
            int[] x = new int[]{
                    plot.xscale(binStart(i)),
                    plot.xscale(binStart(i)),
                    plot.xscale(binStart(i + 1)),
                    plot.xscale(binStart(i + 1)),
                    plot.xscale(binStart(i)),};
            int[] y = new int[]{
                    plot.yscale(0),
                    plot.yscale(d),
                    plot.yscale(d),
                    plot.yscale(0),
                    plot.yscale(0)};
            g2d.drawPolyline(x, y, 5);
            if (d != 0) {
                x = new int[]{
                        plot.xscale(binStart(i)) + 1,
                        plot.xscale(binStart(i)) + 1,
                        plot.xscale(binStart(i + 1)),
                        plot.xscale(binStart(i + 1)),
                        plot.xscale(binStart(i)) + 1
                };
                y = new int[]{
                        plot.yscale(0),
                        plot.yscale(d) + 1,
                        plot.yscale(d) + 1,
                        plot.yscale(0),
                        plot.yscale(0)};
                g2d.setColor(opt().getColor(i));
                g2d.fillPolygon(x, y, 5);
            }
        }
    }

    private double binStart(int i) {
        double value = minvalue;
        double fraction = 1. * (maxvalue - minvalue) / (1. * bins);
        return value + fraction * (i);
    }
}
