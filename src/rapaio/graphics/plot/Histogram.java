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
        getParent().setYLab(prob ? "density" : "frequency");
        getParent().setLeftThicker(true);
        getParent().setLeftMarkers(true);
        getParent().setBottomThicker(true);
        getParent().setBottomMarkers(true);
        setCol(7);
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
            for (int i = 0; i < v.rowCount(); i++) {
                if (v.missing(i)) {
                    continue;
                }
                if (minvalue != minvalue) {
                    minvalue = v.value(i);
                } else {
                    minvalue = Math.min(minvalue, v.value(i));
                }
                if (maxvalue != maxvalue) {
                    maxvalue = v.value(i);
                } else {
                    maxvalue = Math.max(maxvalue, v.value(i));
                }
            }
        }

        double step = (maxvalue - minvalue) / (1. * bins);
        freqtable = new double[bins];
        double total = 0;
        for (int i = 0; i < v.rowCount(); i++) {
            if (v.missing(i)) {
                continue;
            }
            total++;
            if (v.value(i) < minvalue || v.value(i) > maxvalue) {
                continue;
            }
            int index = (int) ((v.value(i) - minvalue) / step);
            if (index == freqtable.length)
                index--;
            freqtable[index]++;
        }

        if (prob && (total != 0)) {
            for (int i = 0; i < freqtable.length; i++) {
                freqtable[i] /= (total * step);
            }
        }
    }

    @Override
    protected Color[] getDefaultCol() {
        return new Color[]{getCol(7)};
    }

    @Override
    public Range buildRange() {
        rebuild();

        Range range = new Range();
        range.union(minvalue, Double.NaN);
        range.union(maxvalue, Double.NaN);
        for (double aFreqtable : freqtable) {
            range.union(Double.NaN, aFreqtable);
        }
        range.union(Double.NaN, 0);
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {
        rebuild();

        g2d.setColor(ColorPalette.STANDARD.getColor(0));
        for (int i = 0; i < freqtable.length; i++) {
            double d = freqtable[i];
            double mind = Math.min(d, getParent().getRange().getY2());
            if (!getParent().getRange().contains(binStart(i), 0)) {
                continue;
            }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
            int[] x;
            int[] y;
            g2d.setColor(ColorPalette.STANDARD.getColor(0));
            x = new int[]{
                    (int) getParent().xScale(binStart(i)),
                    (int) getParent().xScale(binStart(i)),
                    (int) getParent().xScale(binStart(i + 1)),
                    (int) getParent().xScale(binStart(i + 1)),
                    (int) getParent().xScale(binStart(i)),};
            y = new int[]{
                    (int) getParent().yScale(0),
                    (int) getParent().yScale(mind),
                    (int) getParent().yScale(mind),
                    (int) getParent().yScale(0),
                    (int) getParent().yScale(0)};
            g2d.drawPolyline(x, y, 5);

            if (d != 0) {
                x = new int[]{
                        (int) getParent().xScale(binStart(i)) + 1,
                        (int) getParent().xScale(binStart(i)) + 1,
                        (int) getParent().xScale(binStart(i + 1)),
                        (int) getParent().xScale(binStart(i + 1)),
                        (int) getParent().xScale(binStart(i)) + 1
                };
                y = new int[]{
                        (int) getParent().yScale(0),
                        (int) getParent().yScale(mind) + ((d == mind) ? 1 : -2),
                        (int) getParent().yScale(mind) + ((d == mind) ? 1 : -2),
                        (int) getParent().yScale(0),
                        (int) getParent().yScale(0)};
                g2d.setColor(getCol(i));
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
