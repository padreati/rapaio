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

package rapaio.graphics.plot.plotcomp;

import rapaio.core.CoreTools;
import rapaio.core.MathTools;
import rapaio.core.correlation.CorrPearson;
import rapaio.data.Frame;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.ColorGradient;
import rapaio.graphics.plot.PlotComponent;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;

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
    private final RM dist;
    private final int[][] colors;

    public CorrGram(Frame df) {
        dist = SolidRM.empty(df.varCount(), df.varCount());

        CorrPearson corr = CoreTools.corrPearson(df);
        for (int i = 0; i < df.varCount(); i++) {
            for (int j = 0; j < df.varCount(); j++) {
                dist.set(i,j, corr.values()[i][j]);
            }
        }

        colors = new int[dist.rowCount()][dist.colCount()];

        for (int i = 0; i < dist.rowCount(); i++) {
            for (int j = 0; j < dist.colCount(); j++) {
                double x = dist.get(i, j);
                if (x > 1)
                    x = 1;
                if (x < -1)
                    x = -1;

                int c = (int) MathTools.floor((1 + x) * 50);
                colors[i][j] = c;
            }
        }
    }

    @Override
    protected Range buildRange() {
        return new Range(0, 0, dist.colCount(), dist.rowCount());
    }

    @Override
    public void paint(Graphics2D g2d) {

        if (grid) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(options.getLwd()));

            g2d.draw(new Line2D.Double(xScale(0), yScale(0), xScale(dist.colCount()), yScale(0)));
            g2d.draw(new Line2D.Double(xScale(0), yScale(dist.rowCount()), xScale(0), yScale(0)));
            for (int i = 0; i < dist.rowCount(); i++) {
                for (int j = 0; j < dist.colCount(); j++) {
                    g2d.draw(new Line2D.Double(xScale(i + 1), yScale(j + 1), xScale(dist.colCount()), yScale(j + 1)));
                    g2d.draw(new Line2D.Double(xScale(i + 1), yScale(dist.rowCount()), xScale(i + 1), yScale(j + 1)));
                }
            }
        }

        ColorGradient gradient = ColorGradient.newHueGradient(DoubleStream.iterate(0, x -> x + 0.01).limit(101).toArray());
//        ColorGradient gradient = ColorGradient.newBiColorGradient(Color.RED, Color.BLUE, DoubleStream.iterate(0, x -> x + 0.01).limit(101).toArray());
        for (int i = dist.rowCount()-1; i >=0 ; i--) {
            for (int j = 0; j < dist.colCount(); j++) {
                g2d.setColor(gradient.getColor(colors[i][j]));
                g2d.fill(new Rectangle2D.Double(xScale(i), yScale(j), Math.abs(xScale(i + 1) - xScale(i)), Math.abs(yScale(j + 1)-yScale(j))));
            }
        }
    }
}
