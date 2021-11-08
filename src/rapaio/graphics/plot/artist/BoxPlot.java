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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import static rapaio.sys.With.*;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.stream.VSpot;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptionFill;
import rapaio.graphics.opt.GOptionPch;
import rapaio.graphics.opt.PchPalette;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BoxPlot extends Artist {

    @Serial
    private static final long serialVersionUID = 8868603141563818477L;

    private final Var[] vars;
    private final String[] names;

    public BoxPlot(Var x, Var factor, GOption<?>... opts) {
        Map<String, List<Double>> map = x.stream().collect(groupingBy(s -> factor.getLabel(s.row()), mapping(VSpot::getDouble, toList())));
        names = factor.levels().stream().filter(map::containsKey).toArray(String[]::new);
        vars = Arrays.stream(names).map(map::get).map(VarDouble::copy).toArray(Var[]::new);
        this.options.bind(opts);
    }

    public BoxPlot(Var x, GOption<?>... opts) {
        this(new Var[]{x}, opts);
    }

    public BoxPlot(Var[] vars, GOption<?>... opts) {
        this.vars = Arrays.copyOf(vars, vars.length);
        this.names = Arrays.stream(vars).map(Var::name).toArray(String[]::new);

        options.setPch(new GOptionPch(VarInt.wrap(0, 3)));
        options.setColor(color(0));
        options.setFill(new GOptionFill(new Color[]{new Color(240, 240, 240)}));
        this.options.bind(opts);
    }

    public BoxPlot(Frame df, GOption<?>... opts) {
        this.vars = df.varStream().filter(var -> var.stream().complete().count() > 0).toArray(Var[]::new);
        this.names = Arrays.stream(vars).map(Var::name).toArray(String[]::new);

        options.setPch(new GOptionPch(VarInt.wrap(0, 3)));
        options.setColor(color(0));
        options.setFill(new GOptionFill(new Color[]{new Color(240, 240, 240)}));
        this.options.bind(opts);
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
        union(0, Double.NaN);
        union(vars.length, Double.NaN);
        for (int i = 0; i < vars.length; i++) {
            Var v = vars[i];
            plot.xAxis().unionCategory(i + 0.5, names[i]);
            for (int j = 0; j < v.size(); j++) {
                if (v.isMissing(j)) continue;
                union(Double.NaN, v.getDouble(j));
            }
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

        for (int i = 0; i < vars.length; i++) {
            Var v = vars[i];
            if (v.size() == 0) {
                continue;
            }
            double[] p = new double[]{0.25, 0.5, 0.75};
            double[] q = Quantiles.of(v, p).values();
            double iqr = q[2] - q[0];
            double innerFence = 1.5 * iqr;
            double outerFence = 3 * iqr;

            double x1 = i + 0.5 - 0.3;
            double x2 = i + 0.5;
            double x3 = i + 0.5 + 0.3;

            // first we fill the space

            g2d.setColor(options.getFill(i));
            g2d.fill(new Rectangle2D.Double(xScale(x1), yScale(q[2]), xScale(x3) - xScale(x1), yScale(q[0]) - yScale(q[2])));

            g2d.setColor(options.getColor(i));

            // median
            g2d.setStroke(new BasicStroke(options.getLwd() * 2));
            g2d.draw(new Line2D.Double(
                    xScale(x1), yScale(q[1]), xScale(x3), yScale(q[1])));

            // box
            g2d.setStroke(new BasicStroke(options.getLwd()));

            g2d.draw(new Line2D.Double(xScale(x1), yScale(q[0]), xScale(x3), yScale(q[0])));
            g2d.draw(new Line2D.Double(xScale(x1), yScale(q[2]), xScale(x3), yScale(q[2])));
            g2d.draw(new Line2D.Double(xScale(x1), yScale(q[0]), xScale(x1), yScale(q[2])));
            g2d.draw(new Line2D.Double(xScale(x3), yScale(q[0]), xScale(x3), yScale(q[2])));

            // outliers
            double upperwhisker = q[2];
            double lowerqhisker = q[0];
            for (int j = 0; j < v.size(); j++) {
                double point = v.getDouble(j);
                if ((point > q[2] + outerFence) || (point < q[0] - outerFence)) {
                    // big outlier
                    g2d.setStroke(new BasicStroke(options.getLwd()));
                    PchPalette.STANDARD.draw(g2d,
                            xScale(x2),
                            yScale(point),
                            options.getSz(i), options.getPch(1), options.getLwd(), options.getColor(i), options.getFill(i));
                    continue;
                }
                if ((point > q[2] + innerFence) || (point < q[0] - innerFence)) {
                    // outlier
                    g2d.setStroke(new BasicStroke(options.getLwd()));
                    PchPalette.STANDARD.draw(g2d,
                            xScale(x2),
                            yScale(point),
                            options.getSz(i), options.getPch(0), options.getLwd(), options.getColor(i), options.getFill(i));
                    continue;
                }
                if ((point > upperwhisker) && (point < q[2] + innerFence)) {
                    upperwhisker = Math.max(upperwhisker, point);
                }
                if ((point < lowerqhisker) && (point >= q[0] - innerFence)) {
                    lowerqhisker = Math.min(lowerqhisker, point);
                }
            }

            // whiskers
            g2d.draw(new Line2D.Double(xScale(x1), yScale(upperwhisker), xScale(x3), yScale(upperwhisker)));
            g2d.draw(new Line2D.Double(xScale(x1), yScale(lowerqhisker), xScale(x3), yScale(lowerqhisker)));

            g2d.setStroke(new BasicStroke(options.getLwd(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{8}, 0));
            g2d.draw(new Line2D.Double(xScale(x2), yScale(q[2]), xScale(x2), yScale(upperwhisker)));
            g2d.draw(new Line2D.Double(xScale(x2), yScale(q[0]), xScale(x2), yScale(lowerqhisker)));
        }
    }
}
