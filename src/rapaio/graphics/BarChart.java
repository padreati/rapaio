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
package rapaio.graphics;

import rapaio.data.*;
import rapaio.graphics.base.BaseFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.colors.ColorPalette;

import java.awt.*;
import java.util.HashSet;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BarChart extends BaseFigure {

    private final Vector category;
    private final Vector condition;
    private final Vector numeric;
    private boolean density = false;
    private Range range;
    private double[][] hits;
    private double[] totals;

    public BarChart(Vector category) {
        this(category, new NominalVector("", category.getRowCount(), new HashSet<String>()));
    }

    public BarChart(Vector category, Vector condition) {
        this(category, condition, new IndexVector("count", category.getRowCount(), 1));
    }

    public BarChart(Vector category, Vector condition, Vector numeric) {
        if (!category.isNominal()) {
            throw new IllegalArgumentException("categories are nominal only");
        }
        if (!condition.isNominal()) {
            throw new IllegalArgumentException("conditions are nominal only");
        }
        if (!numeric.isNumeric()) {
            throw new IllegalArgumentException("Numeric vector must be .. isNumeric");
        }

        this.category = category;
        this.condition = condition;
        this.numeric = numeric;

        leftThicker = true;
        leftMarkers = true;
        bottomThicker = true;
        bottomMarkers = true;

        int shift = 9;
        opt().setColorIndex(new IndexVector("colors", shift, condition.getDictionary().length + shift - 1, 1));

        this.setLeftLabel(numeric.getName());
        this.setBottomLabel(category.getName());
    }

    public void useDensity(boolean density) {
        this.density = density;
    }

    @Override
    public Range buildRange() {
        if (range == null) {

            // build preliminaries
            int width = category.getDictionary().length;
            int height = condition.getDictionary().length;

            totals = new double[width];
            hits = new double[width][height];

            int len = Integer.MAX_VALUE;
            len = Math.min(len, category.getRowCount());
            len = Math.min(len, condition.getRowCount());
            len = Math.min(len, numeric.getRowCount());

            for (int i = 0; i < len; i++) {
                hits[category.getIndex(i)][condition.getIndex(i)] += numeric.getValue(i);
                totals[category.getIndex(i)] += numeric.getValue(i);
            }

            if (density) {
                double t = 0;
                for (int i = 0; i < totals.length; i++) {
                    t += totals[i];
                }
                for (int i = 0; i < hits.length; i++) {
                    for (int j = 0; j < hits[i].length; j++) {
                        hits[i][j] /= t;
                    }
                    totals[i] /= t;
                }
            }

            // now build range
            range = new Range();
            for (int i = 0; i < totals.length; i++) {
                range.union(Double.NaN, totals[i]);
                range.union(Double.NaN, 0);
            }
            int cnt = 0;
            for (int i = 0; i < totals.length; i++) {
                if (totals[i] > 0) cnt++;
            }
            range.union(-0.5, 0);
            range.union(cnt - 0.5, 0);
        }
        return range;
    }

    @Override
    public void buildLeftMarkers() {
        buildNumericLeftMarkers();
    }

    @Override
    public void buildBottomMarkers() {
        bottomMarkersPos.clear();
        bottomMarkersMsg.clear();

        int cnt = 0;
        for (int i = 0; i < category.getDictionary().length; i++) {
            if (totals[i] > 0) cnt++;
        }
        int xspots = cnt;
        double xspotwidth = viewport.width / (1. * xspots);

        cnt = 0;
        for (int i = 0; i < category.getDictionary().length; i++) {
            if (totals[i] == 0) continue;
            bottomMarkersPos.add(xspotwidth * (0.5 + cnt));
            bottomMarkersMsg.add(category.getDictionary()[i]);
            cnt++;
        }
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        super.paint(g2d, rect);

        int colindex = 0;
        int col = 0;
        for (int i = 0; i < category.getDictionary().length; i++) {
            if (totals[i] == 0) continue;

            double ystart = 0;
            for (int j = 0; j < condition.getDictionary().length; j++) {
                double yend = ystart + hits[i][j];

                int[] x = {xscale(col - 0.4), xscale(col - 0.4), xscale(col + 0.4), xscale(col + 0.4), xscale(col - 0.4)};
                int[] y = {yscale(ystart), yscale(yend), yscale(yend), yscale(ystart), yscale(ystart)};

                g2d.setColor(ColorPalette.STANDARD.getColor(0));
                g2d.drawPolygon(x, y, 4);

                x = new int[]{xscale(col - 0.4) + 1, xscale(col - 0.4) + 1, xscale(col + 0.4), xscale(col + 0.4), xscale(col - 0.4) + 1};
                y = new int[]{yscale(ystart), yscale(yend) + 1, yscale(yend) + 1, yscale(ystart), yscale(ystart)};

                g2d.setColor(opt().getColor(colindex++));
                g2d.fillPolygon(x, y, 4);

                ystart = yend;
            }
            col++;
        }
    }
}
