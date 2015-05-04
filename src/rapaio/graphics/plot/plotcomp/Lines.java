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

package rapaio.graphics.plot.plotcomp;

import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.plot.PlotComponent;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * @author Aurelian Tutuianu
 */
@Deprecated
public class Lines extends PlotComponent {

    private static final long serialVersionUID = 9183829873670164532L;
    private final Var x;
    private final Var y;

    public Lines(Var y, GOpt... opts) {
        this(Numeric.newSeq(0, y.rowCount() - 1), y, opts);
    }

    public Lines(Var x, Var y, GOpt... opts) {
        this.x = x;
        this.y = y;
        this.options.apply(opts);
    }

    @Override
    public Range buildRange() {
        if (x.rowCount() == 0) {
            return null;
        }
        Range range = new Range();
        for (int i = 0; i < x.rowCount(); i++) {
            if (x.missing(i) || y.missing(i)) {
                continue;
            }
            range.union(x.value(i), y.value(i));
        }
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(options.getLwd()));
        g2d.setBackground(ColorPalette.STANDARD.getColor(255));

        for (int i = 1; i < x.rowCount(); i++) {
            g2d.setColor(options.getColor(i));
            double x1 = parent.xScale(x.value(i - 1));
            double y1 = parent.yScale(y.value(i - 1));
            double x2 = parent.xScale(x.value(i));
            double y2 = parent.yScale(y.value(i));

            //TODO improve this crap to clip only parts of lines outside of the data range
            if (parent.getRange().contains(x.value(i - 1), y.value(i - 1))
                    && parent.getRange().contains(x.value(i), y.value(i))) {
                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }
    }
}
