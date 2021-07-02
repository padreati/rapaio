/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.graphics.plot.artist;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptions;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.util.collection.IntArrays;

import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static rapaio.graphics.Plotter.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BarPlotArtist extends Artist {

    @Serial
    private static final long serialVersionUID = -3953248625109450364L;

    private static final Set<VarType> categoryTypes = Set.of(VarType.BINARY, VarType.NOMINAL);
    private final GOptions options = new GOptions();
    private final List<String> categories;
    private final List<String> conditions;
    private final int[] selection;
    private final double[][] hits;
    private final double[] totals;

    public BarPlotArtist(Var category, Var condition, Var weights, GOption<?>... opts) {
        if (!categoryTypes.contains(category.type())) {
            throw new IllegalArgumentException("Categories are nominal only");
        }
        if (condition == null) {
            condition = VarNominal.empty(category.size(), new ArrayList<>());
        }
        if (!categoryTypes.contains(condition.type())) {
            throw new IllegalArgumentException("Conditions are nominal only.");
        }
        if (weights == null) {
            weights = VarDouble.fill(category.size(), 1);
        }
        if (!weights.type().isNumeric()) {
            throw new IllegalArgumentException("Numeric var must be numeric.");
        }

        int shift = 9;
        options.bind(fill(VarInt.seq(shift, condition.levels().size())));
        options.bind(opts);

        Map<String, Map<String, Double>> map = new HashMap<>();
        int rowCount = category.size();
        rowCount = Math.min(rowCount, condition.size());
        rowCount = Math.min(rowCount, weights.size());
        LinkedHashSet<String> categoryLevels = new LinkedHashSet<>();
        LinkedHashSet<String> conditionLevels = new LinkedHashSet<>();
        for (int i = 0; i < rowCount; i++) {
            map.computeIfAbsent(category.getLabel(i), label -> new HashMap<>())
                    .merge(condition.getLabel(i), weights.getDouble(i), Double::sum);
            categoryLevels.add(category.getLabel(i));
            conditionLevels.add(condition.getLabel(i));
        }

        categories = new ArrayList<>(categoryLevels);
        conditions = new ArrayList<>(conditionLevels);

        // learn preliminaries
        int width = categoryLevels.size();
        int height = conditionLevels.size();

        totals = new double[width];
        hits = new double[width][height];

        for (int i = 0; i < categories.size(); i++) {
            for (int j = 0; j < conditions.size(); j++) {
                String categ = categories.get(i);
                String cond = conditions.get(j);
                if (!map.containsKey(categ)) {
                    continue;
                }
                if (!map.get(categ).containsKey(cond)) {
                    continue;
                }
                hits[i][j] += map.get(categ).get(cond);
                totals[i] += map.get(categ).get(cond);
            }
        }

        // selected indexes without top

        int[] indexes = new int[totals.length];
        int len = 0;
        for (int i = 0; i < totals.length; i++) {
            if (totals[i] != 0.0) {
                indexes[len++] = i;
            }
        }

        // sort if required
        switch (options.getSort()) {
            case SORT_ASC:
                IntArrays.quickSort(indexes, 0, len, (o1, o2) -> {
                    if (totals[o1] == totals[o2])
                        return 0;
                    return totals[o1] < totals[o2] ? -1 : 1;
                });
                break;
            case SORT_DESC:
                IntArrays.quickSort(indexes, 0, len, (o1, o2) -> {
                    if (totals[o1] == totals[o2])
                        return 0;
                    return totals[o1] < totals[o2] ? 1 : -1;
                });
                break;
            case SORT_NONE:
            default:
                // do not sort
                break;
        }

        // apply top if it is the case
        selection = IntArrays.copy(indexes, 0, Math.min(options.getTop(), len));

    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.CATEGORY;
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {

        // now learn range
        plot.yAxis().unionNumeric(0);
        for (int i = 0; i < selection.length; i++) {
            if (options.getStacked()) {
                plot.yAxis().unionNumeric(totals[selection[i]]);
            } else {
                for (int j = 0; j < conditions.size(); j++) {
                    plot.yAxis().unionNumeric(hits[selection[i]][j]);
                }
            }
            plot.xAxis().unionCategory(i + 0.5, categories.get(selection[i]));
            plot.xAxis().unionNumeric(i);
            plot.xAxis().unionNumeric(i + 1);
        }
    }

    @Override
    public void paint(Graphics2D g2d) {

        if (options.getStacked()) {
            for (int i = 0; i < selection.length; i++) {
                int sel = selection[i];
                double pos = i + 0.5;

                double ystart = 0;
                for (int j = 0; j < conditions.size(); j++) {
                    double yend = ystart + hits[sel][j];

                    int[] x = new int[]{
                            (int) xScale(pos - 0.4),
                            (int) xScale(pos - 0.4),
                            (int) xScale(pos + 0.4),
                            (int) xScale(pos + 0.4),
                            (int) xScale(pos - 0.4)};
                    int[] y = new int[]{
                            (int) yScale(ystart),
                            (int) yScale(yend),
                            (int) yScale(yend),
                            (int) yScale(ystart),
                            (int) yScale(ystart)};

                    g2d.setColor(options.getFill(j));
                    g2d.fillPolygon(x, y, 4);

                    g2d.setColor(options.getColor(j));
                    g2d.drawPolygon(x, y, 4);

                    ystart = yend;
                }
            }
        } else {
            for (int i = 0; i < selection.length; i++) {
                int sel = selection[i];

                double wstep = 0.8 / conditions.size();
                for (int j = 0; j < conditions.size(); j++) {
                    int[] x = new int[]{
                            (int) xScale(i + 0.1 + j * wstep),
                            (int) xScale(i + 0.1 + j * wstep),
                            (int) xScale(i + 0.1 + (j + 1) * wstep),
                            (int) xScale(i + 0.1 + (j + 1) * wstep),
                            (int) xScale(i + 0.1 + j * wstep),
                    };
                    int[] y = new int[]{
                            (int) yScale(0),
                            (int) yScale(hits[sel][j]),
                            (int) yScale(hits[sel][j]),
                            (int) yScale(0),
                            (int) yScale(0),
                    };

                    g2d.setColor(options.getFill(j));
                    g2d.fillPolygon(x, y, 4);

                    g2d.setColor(options.getColor(j));
                    g2d.drawPolygon(x, y, 4);
                }
            }
        }
    }
}
