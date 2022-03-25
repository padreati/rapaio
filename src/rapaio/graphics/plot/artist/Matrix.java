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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.Serial;

import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.math.linear.DMatrix;
import rapaio.sys.With;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/17/19.
 */
public class Matrix extends Artist {

    @Serial
    private static final long serialVersionUID = -642370269224702175L;
    private final DMatrix m;

    public Matrix(DMatrix m, GOption<?>... opts) {
        this.m = m;
        this.options.setColor(With.color(-1));
        this.options.bind(opts);
    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.newNumeric();
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.newNumeric();
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        union(0, 0);
        union(m.cols(), m.rows());
    }

    @Override
    public void paint(Graphics2D g2d) {
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

        int size = options.getPalette().getSize();

        double max = Double.NaN;
        double min = Double.NaN;

        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
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

        final double eps = 1 / (xScale(1) - xScale(0));

        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                Path2D.Double path = new Path2D.Double();
                path.moveTo(xScale(j), yScale(i));
                path.lineTo(xScale(j + 1 + eps), yScale(i));
                path.lineTo(xScale(j + 1 + eps), yScale(i + 1 + eps));
                path.lineTo(xScale(j), yScale(i + 1 + eps));
                path.lineTo(xScale(j), yScale(i));

                int color = (int) Math.floor((m.get(i, j) - min) * size / (max - min));
                g2d.setColor(options.getPalette().getColor(color));
                g2d.setStroke(new BasicStroke());
                g2d.fill(path);
            }
        }

        if (options.getColor(0) != null) {
            for (int i = 0; i <= m.cols(); i++) {

                Point2D.Double from = new Point2D.Double(xScale(i), yScale(0));
                Point2D.Double to = new Point2D.Double(xScale(i), yScale(m.rows()));

                g2d.setColor(options.getColor(0));
                g2d.setStroke(new BasicStroke(0f));
                g2d.draw(new Line2D.Double(from, to));
            }
            for (int i = 0; i <= m.rows(); i++) {

                Point2D.Double from = new Point2D.Double(xScale(0), yScale(i));
                Point2D.Double to = new Point2D.Double(xScale(m.cols()), yScale(i));

                g2d.setColor(options.getColor(0));
                g2d.setStroke(new BasicStroke(options.getLwd()));
                g2d.draw(new Line2D.Double(from, to));
            }
        }

        g2d.setComposite(oldComposite);
    }
}

