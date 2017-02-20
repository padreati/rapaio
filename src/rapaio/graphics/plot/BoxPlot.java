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

import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.data.stream.VSpot;
import rapaio.graphics.base.HostFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.opt.GOpts;
import rapaio.graphics.opt.PchPalette;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BoxPlot extends HostFigure {

    private static final long serialVersionUID = 8868603141563818477L;

    private final Var[] vars;
    private final String[] names;
    private final GOpts options = new GOpts();

    public BoxPlot(Var x, Var factor, GOpt... opts) {

        Map<String, List<Double>> map = x.stream().collect(groupingBy(s -> factor.label(s.row()), mapping(VSpot::value, toList())));
        names = factor.streamLevels().filter(map::containsKey).toArray(String[]::new);
        vars = Arrays.stream(names).map(map::get).map(Numeric::copy).toArray(Var[]::new);

        this.options.apply(opts);
        initialize();
    }

    public BoxPlot(Var x, GOpt... opts) {
        this(new Var[]{x}, opts);
    }

    public BoxPlot(Var[] vars, GOpt... opts) {
        this.vars = vars;
        this.names = Arrays.stream(vars).map(Var::name).toArray(String[]::new);
        this.options.apply(opts);
        initialize();
    }

    public BoxPlot(Frame df, GOpt... opts) {
        this.vars = df.varStream().filter(var -> var.stream().complete().count() > 0).toArray(Var[]::new);
        this.names = Arrays.stream(vars).map(Var::name).toArray(String[]::new);
        this.options.apply(opts);
        initialize();
    }

    private void initialize() {
        leftMarkers(true);
        leftThick(true);
        bottomMarkers(true);
        bottomThick(true);

        options.setPchDefault(gOpts -> Index.wrap(0, 3));
        options.setColorDefault(gOpts -> new Color[]{new Color(240, 240, 240)});
    }

    @Override
    public Range buildRange() {
        Range range = new Range();
        range.union(0, Double.NaN);
        range.union(vars.length, Double.NaN);
        for (Var v : vars) {
            for (int i = 0; i < v.rowCount(); i++) {
                if (v.missing(i)) continue;
                range.union(Double.NaN, v.value(i));
            }
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

        double xSpotWidth = getViewport().width / vars.length;

        for (int i = 0; i < vars.length; i++) {
            bottomMarkersPos.add(i * xSpotWidth + xSpotWidth / 2);
            bottomMarkersMsg.add(names[i]);
        }
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        super.paint(g2d, rect);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

        for (int i = 0; i < vars.length; i++) {
            Var v = vars[i];
            if (v.rowCount() == 0) {
                continue;
            }
            double[] p = new double[]{0.25, 0.5, 0.75};
            double[] q = Quantiles.from(v, p).values();
            double iqr = q[2] - q[0];
            double innerFence = 1.5 * iqr;
            double outerFence = 3 * iqr;

            double x1 = i + 0.5 - 0.3;
            double x2 = i + 0.5;
            double x3 = i + 0.5 + 0.3;

            // first we fill the space

            g2d.setColor(options.getColor(i));
            g2d.fill(new Rectangle2D.Double(xScale(x1), yScale(q[2]),
                    xScale(x3) - xScale(x1), yScale(q[0]) - yScale(q[2])));

            g2d.setColor(ColorPalette.STANDARD.getColor(0));

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
            for (int j = 0; j < v.rowCount(); j++) {
                double point = v.value(j);
                if ((point > q[2] + outerFence) || (point < q[0] - outerFence)) {
                    // big outlier
                    g2d.setStroke(new BasicStroke(options.getLwd()));
                    PchPalette.STANDARD.draw(g2d,
                            xScale(x2),
                            yScale(point),
                            options.getSz(i), options.getPch(1));
                    continue;
                }
                if ((point > q[2] + innerFence) || (point < q[0] - innerFence)) {
                    // outlier
                    g2d.setStroke(new BasicStroke(options.getLwd()));
                    PchPalette.STANDARD.draw(g2d,
                            xScale(x2),
                            yScale(point),
                            options.getSz(i), options.getPch(0));
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
