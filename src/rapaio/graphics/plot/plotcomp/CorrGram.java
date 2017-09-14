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

package rapaio.graphics.plot.plotcomp;

import rapaio.core.CoreTools;
import rapaio.core.correlation.Correlation;
import rapaio.core.tools.DistanceMatrix;
import rapaio.math.MTools;
import rapaio.core.correlation.CorrPearson;
import rapaio.data.Frame;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.ColorGradient;
import rapaio.graphics.plot.PlotComponent;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;
import rapaio.sys.WS;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.stream.DoubleStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/19/16.
 */
public class CorrGram extends PlotComponent {

    private static final long serialVersionUID = 7529398214880633755L;

    private boolean grid = false;
    private boolean labels = true;
    private final DistanceMatrix d;
    private final int[][] colors;

    public CorrGram(DistanceMatrix d, boolean labels, boolean grid) {
        this.labels = labels;
        this.grid = grid;
        this.d = d;

        colors = new int[d.getLength()][d.getLength()];

        for (int i = 0; i < d.getLength(); i++) {
            for (int j = 0; j < d.getLength(); j++) {
                double x = d.get(i, j);
                if (x > 1)
                    x = 1;
                if (x < -1)
                    x = -1;

                int c = (int) MTools.floor((1 + x) * 50);
                colors[i][j] = c;
            }
        }
    }

    @Override
    protected Range buildRange() {
        return new Range(0, 0, d.getLength(), d.getLength());
    }

    @Override
    public void paint(Graphics2D g2d) {

        ColorGradient gradient = ColorGradient.newHueGradient(DoubleStream.iterate(0, x -> x + 0.01).limit(101).toArray());
//        ColorGradient gradient = ColorGradient.newBiColorGradient(Color.MAGENTA, Color.yellow, DoubleStream.iterate(0, x -> x + 0.01).limit(101).toArray());

        double xstep = Math.abs(xScale(1) - xScale(0));
        double ystep = Math.abs(yScale(1) - yScale(0));
        for (int i = 0; i < d.getLength(); i++) {
            for (int j = 0; j < d.getLength(); j++) {
                if (i != j) {
                    g2d.setColor(gradient.getColor(colors[i][j]));
                    g2d.fill(new Rectangle2D.Double(
                            xScale(j),
                            yScale(d.getLength() - i),
                            xstep,
                            ystep));
                }
                if (labels) {
                    String label = WS.formatFlexShort(d.get(i, j));
                    if (i == j) {
                        label = d.getName(i);
                    }
                    Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(label, g2d);
                    double width = bounds.getWidth();
                    double height = bounds.getHeight();

                    g2d.setColor(Color.BLACK);
                    g2d.drawString(label,
                            (int) (xScale(j) + xstep / 2 - width / 2),
                            (int) (yScale(d.getLength() - i) + ystep / 2 + height / 2));
                }
            }
        }

        if (grid) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(options.getLwd()));

            g2d.draw(new Line2D.Double(xScale(0), yScale(0), xScale(d.getLength()), yScale(0)));
            g2d.draw(new Line2D.Double(xScale(0), yScale(d.getLength()), xScale(0), yScale(0)));
            for (int i = 0; i <= d.getLength(); i++) {
                for (int j = 0; j <= d.getLength(); j++) {
                    g2d.draw(new Line2D.Double(xScale(j), yScale(i), xScale(d.getLength()), yScale(i)));
                    g2d.draw(new Line2D.Double(xScale(j), yScale(d.getLength()), xScale(j), yScale(i)));
                }
            }
        }
    }
}
