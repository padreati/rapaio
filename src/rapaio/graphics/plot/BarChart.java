/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import rapaio.data.*;
import rapaio.graphics.Plotter;
import rapaio.graphics.base.HostFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.opt.GOpts;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class BarChart extends HostFigure {

    private static final long serialVersionUID = -3953248625109450364L;

    private final Var category;
    private final Var condition;
    private final Var numeric;
    private final GOpts options = new GOpts().apply(Plotter.color(0));
    private boolean density = false;
    private Range range;
    private int[] sel;
    private double[][] hits;
    private double[] totals;
    private SortType sort = SortType.NONE;
    private int top = Integer.MAX_VALUE;

    public BarChart(Var category, GOpt... opts) {
        this(category, null, opts);
    }

    public BarChart(Var category, Var condition, GOpt... opts) {
        this(category, condition, null, opts);
    }

    public BarChart(Var category, Var condition, Var numeric, GOpt... opts) {
        List<VarType> varTypes = Arrays.asList(VarType.BINARY, VarType.NOMINAL, VarType.ORDINAL);
        if (!varTypes.contains(category.getType())) {
            throw new IllegalArgumentException("categories are nominal only");
        }
        if (condition == null) {
            condition = NominalVar.empty(category.getRowCount(), new ArrayList<>());
        }
        if (!condition.getType().isNominal()) {
            throw new IllegalArgumentException("conditions are nominal only");
        }
        if (numeric == null) {
            numeric = NumericVar.fill(category.getRowCount(), 1);
        }
        if (!numeric.getType().isNumeric()) {
            throw new IllegalArgumentException("Numeric var must be .. isNumeric");
        }

        this.category = category;
        this.condition = condition;
        this.numeric = numeric;

        leftThick(true);
        leftMarkers(true);
        bottomThick(true);
        bottomMarkers(true);

        int shift = 9;
        options.apply(Plotter.color(IndexVar.seq(shift, condition.getLevels().length)));
        options.apply(opts);
    }

    public BarChart useSortType(SortType sort) {
        this.sort = sort;
        return this;
    }

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
            int width = category.getLevels().length;
            int height = condition.getLevels().length;

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

            // this needs reworking since it does not work for negative values
            if (density) {
                double t = 0;
                for (double total : totals) {
                    t += total;
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

            Collections.sort(list, (o1, o2) -> {
                if (totals[o1] == totals[o2])
                    return 0;
                int sign = (SortType.ASC.equals(sort)) ? 1 : -1;
                return totals[o1] < totals[o2] ? -sign : sign;
            });
            if (top < list.size()) {
                list = list.subList(0, top);
            }

            sel = new int[list.size()];
            if (SortType.NONE.equals(sort)) {
                Set<Integer> set = new HashSet<>(list);
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
            for (int aSel1 : sel) {
                range.union(Double.NaN, totals[aSel1]);
                range.union(Double.NaN, 0);
            }
            range.union(-0.5, 0);
            range.union(sel.length + 0.5, 0);
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

        int xspots = sel.length;
        double xspotwidth = getViewport().width / (1. * xspots);

        int cnt = 0;
        for (int aSel : sel) {
            if (totals[aSel] == 0)
                continue;
            bottomMarkersPos.add(xspotwidth * (0.5 + cnt));
            bottomMarkersMsg.add(category.getLevels()[aSel]);
            cnt++;
        }
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        super.paint(g2d, rect);

        int col = 0;
        for (int aSel : sel) {
            if (totals[aSel] == 0) {
                continue;
            }

            double ystart = 0;
            for (int j = 0; j < condition.getLevels().length; j++) {
                double yend = ystart + hits[aSel][j];
                int sign = ((yend > 0) ? 1 : -1);

                int[] x = {
                        (int) xScale(col - 0.4),
                        (int) xScale(col - 0.4),
                        (int) xScale(col + 0.4),
                        (int) xScale(col + 0.4),
                        (int) xScale(col - 0.4)};
                int[] y = {
                        (int) yScale(ystart),
                        (int) yScale(yend),
                        (int) yScale(yend),
                        (int) yScale(ystart),
                        (int) yScale(ystart)};

                g2d.setColor(ColorPalette.STANDARD.getColor(0));
                g2d.drawPolygon(x, y, 4);

                x = new int[]{
                        (int) xScale(col - 0.4) + 1,
                        (int) xScale(col - 0.4) + 1,
                        (int) xScale(col + 0.4),
                        (int) xScale(col + 0.4),
                        (int) xScale(col - 0.4) + 1};
                y = new int[]{
                        (int) yScale(ystart),
                        (int) yScale(yend) + sign,
                        (int) yScale(yend) + sign,
                        (int) yScale(ystart),
                        (int) yScale(ystart)};

                g2d.setColor(options.getColor(j));
                g2d.fillPolygon(x, y, 4);

                ystart = yend;
            }
            col++;
        }
    }

    public enum SortType {
        NONE, ASC, DESC
    }
}
