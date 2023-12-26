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
import static rapaio.graphics.opt.GOptions.palette;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.io.Serial;

import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptions;
import rapaio.graphics.opt.Palette;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.math.linear.DMatrix;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/17/19.
 */
public class Matrix extends Artist {

    @Serial
    private static final long serialVersionUID = -642370269224702175L;
    private final DMatrix m;

    public Matrix(DMatrix m, GOption<?>... opts) {
        this.m = m;
        this.options = new GOptions()
                .apply(color(-1), palette(Palette.hue(0, 240, m.min(0).min(), m.max(0).max())))
                .apply(opts);
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

        final double eps = 1 / (xScale(1) - xScale(0));

        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                Path2D.Double path = new Path2D.Double();

                path.moveTo(xScale(j), yScale(m.rows() - i));
                path.lineTo(xScale(j + 1 + eps), yScale(m.rows() - i));
                path.lineTo(xScale(j + 1 + eps), yScale(m.rows() -  i - 1 - eps));
                path.lineTo(xScale(j), yScale(m.rows() - i - 1 - eps));
                path.lineTo(xScale(j), yScale(m.rows() - i));

                g2d.setColor(options.getPalette().getColor(m.get(i, j)));
                g2d.setStroke(new BasicStroke());
                g2d.fill(path);
            }
        }

        if (options.getColor(0) != null) {
            for (int i = 0; i < m.rows(); i++) {
                for (int j = 0; j < m.cols(); j++) {
                    Path2D.Double path = new Path2D.Double();

                    path.moveTo(xScale(j), yScale(m.rows() - i));
                    path.lineTo(xScale(j + 1 + eps), yScale(m.rows() - i));
                    path.lineTo(xScale(j + 1 + eps), yScale(m.rows() -  i - 1 - eps));
                    path.lineTo(xScale(j), yScale(m.rows() - i - 1 - eps));
                    path.lineTo(xScale(j), yScale(m.rows() - i));

                    g2d.setColor(options.getColor(0));
                    g2d.setStroke(new BasicStroke(options.getLwd()));
                    g2d.draw(path);
                }
            }
        }

        g2d.setComposite(oldComposite);
    }
}

