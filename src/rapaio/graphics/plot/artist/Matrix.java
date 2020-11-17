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

import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.math.linear.DM;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/17/19.
 */
@Deprecated
public class Matrix extends Artist {

    private static final long serialVersionUID = -642370269224702175L;
    private final DM m;
    private final boolean contour;

    public Matrix(DM m, boolean contour, GOption<?>... opts) {
        this.m = m;
        this.contour = contour;
        this.options.bind(opts);
    }

    @Override
    public Axis newXAxis() {
        return Axis.numeric(plot);
    }

    @Override
    public Axis newYAxis() {
        return Axis.numeric(plot);
    }

    @Override
    public void updateDataRange() {
        union(0, 0);
        union(m.colCount(), m.rowCount());
    }

    @Override
    public void paint(Graphics2D g2d) {
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

        int size = options.getPalette().getSize();

        double max = Double.NaN;
        double min = Double.NaN;

        for (int i = 0; i < m.rowCount(); i++) {
            for (int j = 0; j < m.colCount(); j++) {
                double v = m.get(i, j);
                if (Double.isNaN(v)) {
                    continue;
                }
                if (Double.isNaN(min)) {
                    min = v;
                }
                if (Double.isNaN(max)) {
                    max = v;
                }
                min = Double.min(min, v);
                max = Double.max(max, v);
            }
        }

        for (int i = 0; i < m.colCount(); i++) {
            for (int j = 0; j < m.rowCount(); j++) {
                Path2D.Double path = new Path2D.Double();
                path.moveTo(xScale(i), yScale(j));
                path.lineTo(xScale(i + 1), yScale(j));
                path.lineTo(xScale(i + 1), yScale(j + 1));
                path.lineTo(xScale(i), yScale(j + 1));
                path.lineTo(xScale(i), yScale(j));

                int color = (int) Math.floor((m.get(j, i) - min) * size / (max - min));
                g2d.setColor(options.getPalette().getColor(color));
                g2d.setStroke(new BasicStroke());
                g2d.draw(path);
                g2d.fill(path);
            }
        }

        if (contour) {
            for (int i = 0; i <= m.colCount(); i++) {

                Point2D.Double from = new Point2D.Double(xScale(i), yScale(0));
                Point2D.Double to = new Point2D.Double(xScale(i), yScale(m.rowCount()));

                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(options.getLwd()));
                g2d.draw(new Line2D.Double(from, to));
            }
            for (int i = 0; i <= m.rowCount(); i++) {

                Point2D.Double from = new Point2D.Double(xScale(0), yScale(i));
                Point2D.Double to = new Point2D.Double(xScale(m.colCount()), yScale(i));

                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(options.getLwd()));
                g2d.draw(new Line2D.Double(from, to));
            }
        }

        g2d.setComposite(oldComposite);
    }
}

