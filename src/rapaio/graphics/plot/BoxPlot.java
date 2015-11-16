/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.graphics.base.HostFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.opt.GOpts;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class BoxPlot extends HostFigure {

    private static final long serialVersionUID = 8868603141563818477L;

    private final Var[] vars;
    private final String[] labels;
    private final GOpts options = new GOpts();

    public BoxPlot(Var v, String label, GOpt... opts) {
        this(new Var[]{v}, new String[]{label}, opts);
    }

    public BoxPlot(Var numeric, Var nominal, GOpt... opts) {
        this.labels = nominal.levels();
        this.vars = new Var[labels.length];
        int[] count = new int[labels.length];
        for (int i = 0; i < numeric.rowCount(); i++) {
            count[nominal.index(i)]++;
        }
        for (int i = 0; i < count.length; i++) {
            vars[i] = Numeric.newFill(count[i], 0);
        }
        int[] pos = new int[vars.length];
        for (int i = 0; i < nominal.rowCount(); i++) {
            vars[nominal.index(i)].setValue(pos[nominal.index(i)], numeric.value(i));
            pos[nominal.index(i)]++;
        }
        this.options.apply(opts);
        initialize();
    }

    public BoxPlot(Var[] vars, String[] labels, GOpt... opts) {
        this.vars = vars;
        this.labels = labels;
        this.options.apply(opts);
        initialize();
    }

    public BoxPlot(Frame df, GOpt... opts) {
        this(df, new VarRange("all"), opts);
    }

    public BoxPlot(Frame df, VarRange varRange, GOpt... opts) {
        if (varRange == null) {
            int len = 0;
            for (int i = 0; i < df.varCount(); i++) {
                if (df.var(i).type().isNumeric()) {
                    len++;
                }
            }
            int[] indexes = new int[len];
            len = 0;
            for (int i = 0; i < df.varCount(); i++) {
                if (df.var(i).type().isNumeric()) {
                    indexes[len++] = i;
                }
            }
            varRange = new VarRange(indexes);
        }
        List<Integer> indexes = varRange.parseVarIndexes(df);
        this.vars = new Var[indexes.size()];
        this.labels = new String[indexes.size()];

        int pos = 0;
        for (int index : indexes) {
            vars[pos] = df.var(index);
            labels[pos] = df.varNames()[index];
            pos++;
        }
        this.options.apply(opts);
        initialize();
    }

    private void initialize() {
        leftMarkers(true);
        leftThick(true);
        bottomMarkers(true);
        bottomThick(true);
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
            bottomMarkersMsg.add(labels[i]);
        }
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        super.paint(g2d, rect);

        for (int i = 0; i < vars.length; i++) {
            Var v = vars[i];
            if (v.rowCount() == 0) {
                continue;
            }
            double[] p = new double[]{0.25, 0.5, 0.75};
            double[] q = new Quantiles(v, p).values();
            double iqr = q[2] - q[0];
            double innerFence = 1.5 * iqr;
            double outerFence = 3 * iqr;

            double x1 = i + 0.5 - 0.3;
            double x2 = i + 0.5;
            double x3 = i + 0.5 + 0.3;

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
            for (int j = 0; j < v.rowCount(); j++) {
                double point = v.value(j);
                if ((point > q[2] + outerFence) || (point < q[0] - outerFence)) {
                    // big outlier
                    int width = (int) (3 * options.getSz(i));
                    g2d.fillOval(
                            (int) xScale(x2) - width / 2 - 1,
                            (int) yScale(point) - width / 2 - 1,
                            width, width);
                    continue;
                }
                if ((point > q[2] + innerFence) || (point < q[0] - innerFence)) {
                    // outlier
                    int width = (int) (3.5 * options.getSz(i));
                    g2d.drawOval(
                            (int) xScale(x2) - width / 2 - 1,
                            (int) yScale(point) - width / 2 - 1,
                            width, width);
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
