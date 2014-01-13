/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

import rapaio.data.Vector;
import rapaio.data.Vectors;
import rapaio.graphics.base.Range;
import rapaio.graphics.colors.ColorPalette;

import java.awt.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Histogram extends PlotComponent {

    private final Vector v;
    int bins = 20;
    boolean prob = false;
    double[] freqtable;
    double minvalue = Double.NaN;
    double maxvalue = Double.NaN;

    public Histogram(Vector v) {
        this(v, 30, true);
    }

    public Histogram(Vector v, int bins, boolean prob) {
        this(v, bins, prob, Double.NaN, Double.NaN);
    }

    public Histogram(Vector v, int bins, boolean prob, double minvalue, double maxvalue) {
        this.v = v;
        this.bins = bins;
        this.prob = prob;
        this.minvalue = minvalue;
        this.maxvalue = maxvalue;
    }

    @Override
    public void initialize() {
        getParent().setLeftLabel(prob ? "density" : "frequency");
        getParent().setLeftThicker(true);
        getParent().setLeftMarkers(true);
        getParent().setBottomThicker(true);
        getParent().setBottomMarkers(true);
    }

    public int getBins() {
        return bins;
    }

    public Histogram setBins(int bins) {
        this.bins = bins;
        return this;
    }

    public boolean isProb() {
        return prob;
    }

    public Histogram setProb(boolean prob) {
        this.prob = prob;
        return this;
    }

    private void rebuild() {
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
        if (getColorIndex().getRowCount() == 1 && getColorIndex().getIndex(0) == 0) {
            setColorIndex(Vectors.newIdxOne(7));
        }
    }

    @Override
    public Range buildRange() {
        rebuild();

        Range range = new Range();
        if (getXRangeStart() != getXRangeStart()) {
            range.union(minvalue, Double.NaN);
            range.union(maxvalue, Double.NaN);
        } else {
            range.union(getXRangeStart(), Double.NaN);
            range.union(getXRangeEnd(), Double.NaN);
        }
        if (getYRangeStart() != getYRangeStart()) {
            for (int i = 0; i < freqtable.length; i++) {
                range.union(Double.NaN, freqtable[i]);
            }
            range.union(Double.NaN, 0);
        } else {
            range.union(Double.NaN, 0);
            range.union(Double.NaN, getYRangeEnd());
        }
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {
        rebuild();

        g2d.setColor(ColorPalette.STANDARD.getColor(0));
        for (int i = 0; i < freqtable.length; i++) {
            double d = freqtable[i];
            if (!getParent().getRange().contains(binStart(i), 0)) {
                continue;
            }
            if (!getParent().getRange().contains(binStart(i + 1), d)) {
                continue;
            }
            g2d.setColor(ColorPalette.STANDARD.getColor(0));
            int[] x = new int[]{
                    (int) getParent().xscale(binStart(i)),
                    (int) getParent().xscale(binStart(i)),
                    (int) getParent().xscale(binStart(i + 1)),
                    (int) getParent().xscale(binStart(i + 1)),
                    (int) getParent().xscale(binStart(i)),};
            int[] y = new int[]{
                    (int) getParent().yscale(0),
                    (int) getParent().yscale(d),
                    (int) getParent().yscale(d),
                    (int) getParent().yscale(0),
                    (int) getParent().yscale(0)};
            g2d.drawPolyline(x, y, 5);
            if (d != 0) {
                x = new int[]{
                        (int) getParent().xscale(binStart(i)) + 1,
                        (int) getParent().xscale(binStart(i)) + 1,
                        (int) getParent().xscale(binStart(i + 1)),
                        (int) getParent().xscale(binStart(i + 1)),
                        (int) getParent().xscale(binStart(i)) + 1
                };
                y = new int[]{
                        (int) getParent().yscale(0),
                        (int) getParent().yscale(d) + 1,
                        (int) getParent().yscale(d) + 1,
                        (int) getParent().yscale(0),
                        (int) getParent().yscale(0)};
                g2d.setColor(getColor(i));
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
