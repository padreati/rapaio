/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

import static rapaio.graphics.opt.GOptions.color;
import static rapaio.graphics.opt.GOptions.fill;
import static rapaio.graphics.opt.GOptions.horizontal;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.IntStream;

import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.data.VarDouble;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptions;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.graphics.plot.Plot;
import rapaio.ml.eval.ClusterSilhouette;

public class Silhouette extends Artist {

    private final ClusterSilhouette silhouette;

    public Silhouette(ClusterSilhouette silhouette, GOption<?>... opts) {
        this.silhouette = silhouette;

        // default fill and color
        options = new GOptions()
                .apply(horizontal(true), fill(IntStream.range(1, silhouette.getScores().length + 1).toArray()),
                        color(IntStream.range(1, silhouette.getScores().length + 1).toArray()))
                .apply(opts);
    }

    @Override
    public Axis.Type xAxisType() {
        return options.getHorizontal() ? Axis.Type.newNumeric() : Axis.Type.newCategory();
    }

    @Override
    public Axis.Type yAxisType() {
        return options.getHorizontal() ? Axis.Type.newCategory() : Axis.Type.newNumeric();
    }

    @Override
    public void bind(Plot parent) {
        super.bind(parent);

        if (options.getHorizontal()) {
            parent.yLab("clusters");
            parent.xLab("scores");
        } else {
            parent.xLab("clusters");
            parent.yLab("scores");
        }
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        VarDouble scores = VarDouble.wrap(silhouette.getScores());
        int clusterCount = silhouette.getClusterCount();
        List<Integer> clusterOrder = silhouette.getClusterOrder();
        List<List<Integer>> instanceOrder = silhouette.getInstanceOrder();

        if (options.getHorizontal()) {
            plot.yAxis().domain().unionNumeric(0);
            plot.yAxis().domain().unionNumeric(scores.size());
            plot.xAxis().domain().unionNumeric(0);
            plot.xAxis().domain().unionNumeric(1);
            plot.xAxis().domain().unionNumeric(Minimum.of(scores).value());
            plot.xAxis().domain().unionNumeric(Maximum.of(scores).value());


            double pos = 0;
            for (int i = 0; i < clusterCount; i++) {
                plot.yAxis().domain().unionCategory(
                        scores.size() - pos - instanceOrder.get(i).size(),
                        scores.size() - pos - instanceOrder.get(i).size() / 2.0,
                        scores.size() - pos,
                        silhouette.getClusterLabels()[clusterOrder.get(i)]);
                pos += instanceOrder.get(i).size();
            }

        } else {
            plot.xAxis().domain().unionNumeric(0);
            plot.xAxis().domain().unionNumeric(scores.size());
            plot.yAxis().domain().unionNumeric(0);
            plot.yAxis().domain().unionNumeric(1);
            plot.yAxis().domain().unionNumeric(Minimum.of(scores).value());
            plot.yAxis().domain().unionNumeric(Maximum.of(scores).value());

            double pos = 0;
            for (int i = 0; i < clusterCount; i++) {
                plot.xAxis().domain().unionCategory(
                        pos,
                        pos + instanceOrder.get(i).size() / 2.0,
                        pos + instanceOrder.get(i).size(),
                        silhouette.getClusterLabels()[clusterOrder.get(i)]);
                pos += instanceOrder.get(i).size();
            }
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        VarDouble scores = VarDouble.wrap(silhouette.getScores());

        int clusterCount = silhouette.getClusterCount();
        List<Integer> clusterOrder = silhouette.getClusterOrder();
        List<List<Integer>> instanceOrder = silhouette.getInstanceOrder();
        int pos = 0;
        for (int i = 0; i < clusterCount; i++) {
            int cluster = clusterOrder.get(i);
            for (int j = 0; j < instanceOrder.get(i).size(); j++) {
                int instance = instanceOrder.get(i).get(j);

                Composite old = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

                double x = xScale(0);
                double y = yScale(scores.size() - pos);
                double w = xScale(scores.getDouble(instance));
                double h = yScale(scores.size() - pos - 1) - yScale(scores.size() - pos);

                if (!options.getHorizontal()) {
                    x = xScale(pos);
                    y = yScale(scores.getDouble(instance));
                    w = xScale(pos + 1) - xScale(pos);
                    h = yScale(0) - yScale(scores.getDouble(instance));
                }

                g2d.setColor(options.getFill(cluster));
                g2d.fill(new Rectangle2D.Double(x, y, w, h));

                g2d.setColor(options.getColor(cluster));
                g2d.draw(new Rectangle2D.Double(x, y, w, h));
                g2d.setComposite(old);

                pos++;

            }
        }
    }
}
