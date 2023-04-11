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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.io.Serial;

import rapaio.data.Var;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptions;
import rapaio.graphics.opt.PchPalette;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.graphics.plot.Plot;

/**
 * Plot component which allows one to add points to a plot.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Points extends Artist {

    @Serial
    private static final long serialVersionUID = -4766079423843859315L;

    private final Var x;
    private final Var y;

    public Points(Var x, Var y, GOption<?>... opts) {
        this.x = x;
        this.y = y;
        this.options = new GOptions().apply(opts);
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
    public void bind(Plot parent) {
        super.bind(parent);
        parent.xLab(x.name());
        parent.yLab(y.name());
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        if (x.size() == 0) {
            return;
        }
        for (int i = 0; i < Math.min(x.size(), y.size()); i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            union(x.getDouble(i), y.getDouble(i));
        }
    }

    @Override
    public void paint(Graphics2D g2d) {

        int len = Math.min(x.size(), y.size());
        for (int i = 0; i < len; i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }

            double xx = x.getDouble(i);
            double yy = y.getDouble(i);

            if (!contains(xx, yy)) {
                continue;
            }

            g2d.setColor(options.getFill(i));
            g2d.setStroke(new BasicStroke(options.getLwd()));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

            PchPalette.STANDARD.draw(g2d, xScale(xx), yScale(yy), options.getSz(i), options.getPch(i), options.getLwd(),
                    options.getColor(i), options.getFill(i));
        }
    }
}
