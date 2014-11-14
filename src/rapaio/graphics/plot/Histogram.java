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

import rapaio.data.Var;
import rapaio.graphics.base.Range;
import rapaio.graphics.colors.ColorPalette;

import java.awt.*;
import java.util.Arrays;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class Histogram extends PlotComponent {

    private final Var v;
    int bins = 30;
    boolean prob = true;
    double[] freqTable;
    double minValue = Double.NaN;
    double maxValue = Double.NaN;

    public Histogram(Var v) {
        this.v = v;
    }

    @Override
    public void initialize() {
        getParent().yLab(prob ? "density" : "frequency");
        getParent().leftThick(true);
        getParent().leftMarkers(true);
        getParent().bottomThick(true);
        getParent().bottomMarkers(true);
        color(7);
    }

    public int getBins() {
        return bins;
    }

    public Histogram bins(int bins) {
        this.bins = bins;
        return this;
    }

    public boolean isProb() {
        return prob;
    }

    public Histogram prob(boolean prob) {
        this.prob = prob;
        return this;
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

        double step = (maxValue - minValue) / (1. * bins);
        freqTable = new double[bins];
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

        if (prob && (total != 0)) {
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
            double mind = Math.min(d, getParent().getRange().y2());
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
        double value = minValue;
        double fraction = 1. * (maxValue - minValue) / (1. * bins);
        return value + fraction * (i);
    }
}
