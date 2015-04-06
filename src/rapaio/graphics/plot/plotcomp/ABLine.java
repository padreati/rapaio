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
 */

package rapaio.graphics.plot.plotcomp;

import rapaio.graphics.base.Range;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.plot.PlotComponent;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * @author Aurelian Tutuianu
 */
public class ABLine extends PlotComponent {

    private static final long serialVersionUID = 8980967297314815554L;
    private final double a;
    private final double b;
    private final boolean h;
    private final boolean v;

    public ABLine(double a, boolean horiz, GOpt... opts) {
        this.a = a;
        this.b = a;
        this.h = horiz;
        this.v = !horiz;
        this.options.apply(opts);
    }

    public ABLine(double a, double b, GOpt... opts) {
        this.a = a;
        this.b = b;
        this.h = false;
        this.v = false;
        this.options.apply(opts);
    }

    @Override
    public Range buildRange() {
        if (h) {
            return new Range(Double.NaN, a, Double.NaN, a);
        }
        if (v) {
            return new Range(a, Double.NaN, a, Double.NaN);
        }
        return null;
    }

    @Override
    public void paint(Graphics2D g2d) {
        Range range = parent.getRange();
        g2d.setColor(options.getColor(0));

        double x1, x2, y1, y2;
        if (!h && !v) {
            double xx = range.x1();
            double yy = a * xx + b;
            if (range.contains(xx, yy)) {
                x1 = (int) parent.xScale(xx);
                y1 = (int) parent.yScale(yy);
            } else {
                y1 = (int) parent.yScale(range.y1());
                x1 = (int) parent.xScale((range.y1() - b) / a);
            }

            xx = range.x2();
            yy = a * xx + b;
            if (range.contains(xx, yy)) {
                x2 = (int) parent.xScale(xx);
                y2 = (int) parent.yScale(yy);
            } else {
                y2 = (int) parent.yScale(range.y2());
                x2 = (int) parent.xScale((range.y2() - b) / a);
            }
        } else {
            if (h) {
                x1 = (int) parent.xScale(range.x1());
                y1 = (int) parent.yScale(a);
                x2 = (int) parent.xScale(range.x2());
                y2 = (int) parent.yScale(a);
            } else {
                x1 = (int) parent.xScale(a);
                y1 = (int) parent.yScale(range.y1());
                x2 = (int) parent.xScale(a);
                y2 = (int) parent.yScale(range.y2());
            }
        }
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(options.getLwd()));
        g2d.draw(new Line2D.Double(x1, y1, x2, y2));
        g2d.setStroke(oldStroke);
    }
}
