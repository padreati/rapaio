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

import rapaio.data.NomVector;
import rapaio.data.Vector;
import rapaio.data.Vectors;
import rapaio.graphics.base.AbstractFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.colors.ColorPalette;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BarChart extends AbstractFigure {

    private final Vector category;
    private final Vector condition;
    private final Vector numeric;
    private boolean density = false;
    private Range range;
    private int[] sel;
    private double[][] hits;
    private double[] totals;

    public BarChart(Vector category) {
        this(category, null);
    }

    public BarChart(Vector category, Vector condition) {
        this(category, condition, null);
    }

    public BarChart(Vector category, Vector condition, Vector numeric) {
        if (!category.isNominal()) {
            throw new IllegalArgumentException("categories are nominal only");
        }
        if (condition == null) {
            condition = new NomVector(category.getRowCount(), new HashSet<String>());
        }
        if (!condition.isNominal()) {
            throw new IllegalArgumentException("conditions are nominal only");
        }
        if (numeric == null) {
            numeric = Vectors.newIdx(category.getRowCount(), 1);
        }
        if (!numeric.isNumeric()) {
            throw new IllegalArgumentException("Numeric vector must be .. isNumeric");
        }

        this.category = category;
        this.condition = condition;
        this.numeric = numeric;

        setLeftThicker(true);
        setLeftMarkers(true);
        setBottomThicker(true);
        setBottomMarkers(true);

        int shift = 9;
        setColorIndex(Vectors.newSeq(shift, condition.getDictionary().length + shift - 1, 1));
    }

    private SortType sort = SortType.NONE;

    public static enum SortType {

        NONE, ASC, DESC
    }

    public BarChart useSortType(SortType sort) {
        this.sort = sort;
        return this;
    }

    private int top = Integer.MAX_VALUE;

    public BarChart useTop(int top) {
        this.top = top;
        return this;
    }

    public void useDensity(boolean density) {
        this.density = density;
    }

    @Override
    public Range buildRange() {
        if (range == null) {

            // learn preliminaries
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

            // now restrict values
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < totals.length; i++) {
                list.add(i);
            }

            Collections.sort(list, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    if (totals[o1] == totals[o2])
                        return 0;
                    int sign = (SortType.ASC.equals(sort)) ? 1 : -1;
                    return totals[o1] < totals[o2] ? -sign : sign;
                }
            });
            if (top < list.size()) {
                list = list.subList(0, top);
            }

            sel = new int[list.size()];
            if (SortType.NONE.equals(sort)) {
                Set<Integer> set = new HashSet(list);
                int pos = 0;
                for (int i = 0; i < totals.length; i++) {
                    if (set.contains(i)) {
                        sel[pos++] = i;
                    }
                }
            } else {
                for (int i = 0; i < sel.length; i++) {
                    sel[i] = list.get(i);
                }
            }

            // now learn range
            range = new Range();
            for (int i = 0; i < sel.length; i++) {
                range.union(Double.NaN, totals[sel[i]]);
                range.union(Double.NaN, 0);
            }
            int cnt = 0;
            for (int i = 0; i < sel.length; i++) {
                if (totals[sel[i]] > 0)
                    cnt++;
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
        getBottomMarkersPos().clear();
        getBottomMarkersMsg().clear();

        int cnt = 0;
        for (int i = 0; i < sel.length; i++) {
            if (totals[sel[i]] > 0)
                cnt++;
        }
        int xspots = cnt;
        double xspotwidth = getViewport().width / (1. * xspots);

        cnt = 0;
        for (int i = 0; i < sel.length; i++) {
            if (totals[sel[i]] == 0)
                continue;
            getBottomMarkersPos().add(xspotwidth * (0.5 + cnt));
            getBottomMarkersMsg().add(category.getDictionary()[sel[i]]);
            cnt++;
        }
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        super.paint(g2d, rect);

        int col = 0;
        for (int i = 0; i < sel.length; i++) {
            if (totals[sel[i]] == 0)
                continue;

            double ystart = 0;
            for (int j = 0; j < condition.getDictionary().length; j++) {
                double yend = ystart + hits[sel[i]][j];

                int[] x = {
                        (int) xscale(col - 0.4),
                        (int) xscale(col - 0.4),
                        (int) xscale(col + 0.4),
                        (int) xscale(col + 0.4),
                        (int) xscale(col - 0.4)};
                int[] y = {
                        (int) yscale(ystart),
                        (int) yscale(yend),
                        (int) yscale(yend),
                        (int) yscale(ystart),
                        (int) yscale(ystart)};

                g2d.setColor(ColorPalette.STANDARD.getColor(0));
                g2d.drawPolygon(x, y, 4);

                x = new int[]{
                        (int) xscale(col - 0.4) + 1,
                        (int) xscale(col - 0.4) + 1,
                        (int) xscale(col + 0.4),
                        (int) xscale(col + 0.4),
                        (int) xscale(col - 0.4) + 1};
                y = new int[]{
                        (int) yscale(ystart),
                        (int) yscale(yend) + 1,
                        (int) yscale(yend) + 1,
                        (int) yscale(ystart),
                        (int) yscale(ystart)};

                g2d.setColor(getColor(j));
                g2d.fillPolygon(x, y, 4);

                ystart = yend;
            }
            col++;
        }
    }
}
