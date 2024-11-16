/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import static rapaio.graphics.opt.GOpts.color;
import static rapaio.graphics.opt.GOpts.palette;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.io.Serial;

import rapaio.graphics.opt.GOpt;
import rapaio.graphics.opt.GOpts;
import rapaio.graphics.opt.Palette;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.math.narray.NArray;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/17/19.
 */
public class Matrix extends Artist {

    @Serial
    private static final long serialVersionUID = -642370269224702175L;
    private final NArray<?> m;

    public Matrix(NArray<?> m, GOpt<?>... opts) {
        this.m = m;
        this.options = new GOpts()
                .apply(color(-1), palette(Palette.hue(0, 240, m.amin().doubleValue(), m.amax().doubleValue())))
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
        union(m.dim(1), m.dim(0));
    }

    @Override
    public void paint(Graphics2D g2d) {
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

        final double eps = 1 / (xScale(1) - xScale(0));

        for (int i = 0; i < m.dim(0); i++) {
            for (int j = 0; j < m.dim(1); j++) {
                Path2D.Double path = new Path2D.Double();

                path.moveTo(xScale(j), yScale(m.dim(0) - i));
                path.lineTo(xScale(j + 1 + eps), yScale(m.dim(0) - i));
                path.lineTo(xScale(j + 1 + eps), yScale(m.dim(0) -  i - 1 - eps));
                path.lineTo(xScale(j), yScale(m.dim(0) - i - 1 - eps));
                path.lineTo(xScale(j), yScale(m.dim(0) - i));

                g2d.setColor(options.getPalette().getColor(m.getDouble(i, j)));
                g2d.setStroke(new BasicStroke());
                g2d.fill(path);
            }
        }

        if (options.getColor(0) != null) {
            for (int i = 0; i < m.dim(0); i++) {
                for (int j = 0; j < m.dim(1); j++) {
                    Path2D.Double path = new Path2D.Double();

                    path.moveTo(xScale(j), yScale(m.dim(0) - i));
                    path.lineTo(xScale(j + 1 + eps), yScale(m.dim(0) - i));
                    path.lineTo(xScale(j + 1 + eps), yScale(m.dim(0) -  i - 1 - eps));
                    path.lineTo(xScale(j), yScale(m.dim(0) - i - 1 - eps));
                    path.lineTo(xScale(j), yScale(m.dim(0) - i));

                    g2d.setColor(options.getColor(0));
                    g2d.setStroke(new BasicStroke(options.getLwd()));
                    g2d.draw(path);
                }
            }
        }

        g2d.setComposite(oldComposite);
    }
}

