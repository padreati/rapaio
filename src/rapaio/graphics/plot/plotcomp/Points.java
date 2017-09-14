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

import rapaio.data.Var;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.PchPalette;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.PlotComponent;

import java.awt.*;

/**
 * Plot component which allows one to add points to a plot.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Points extends PlotComponent {

    private static final long serialVersionUID = -4766079423843859315L;

    private final Var x;
    private final Var y;

    public Points(Var x, Var y, GOption... opts) {
        this.x = x;
        this.y = y;
        this.options.bind(opts);
    }

    @Override
    public void initialize(Plot parent) {
        super.initialize(parent);
        parent.xLab(x.getName());
        parent.yLab(y.getName());
    }

    @Override
    public Range buildRange() {
        if (x.getRowCount() == 0) {
            return null;
        }
        Range range = new Range();
        for (int i = 0; i < Math.min(x.getRowCount(), y.getRowCount()); i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            range.union(x.getValue(i), y.getValue(i));
        }
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {

        int len = Math.min(x.getRowCount(), y.getRowCount());
        for (int i = 0; i < len; i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }

            double xx = x.getValue(i);
            double yy = y.getValue(i);

            if (!parent.getRange().contains(xx, yy)) continue;

            g2d.setColor(options.getColor(i));
            g2d.setStroke(new BasicStroke(options.getLwd()));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

            PchPalette.STANDARD.draw(g2d,
                    parent.xScale(xx),
                    parent.yScale(yy),
                    options.getSz(i), options.getPch(i));
        }
    }
}
