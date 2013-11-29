/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
package rapaio.graphics.plot;

import rapaio.graphics.Plot;
import rapaio.graphics.base.Range;

import java.awt.*;

/**
 * @author Aurelian Tutuianu
 */
public class ABLine extends PlotComponent {

    private final double a;
    private final double b;
    private final boolean h;
    private final boolean v;

    public ABLine(Plot parent, double a, boolean horiz) {
        super(parent);
        this.a = a;
        this.b = a;
        this.h = horiz;
        this.v = !horiz;
    }

    public ABLine(Plot parent, double a, double b) {
        super(parent);
        this.a = a;
        this.b = b;
        this.h = false;
        this.v = false;
    }

    @Override
    public Range getComponentDataRange() {
        return null;
    }

    @Override
    public void paint(Graphics2D g2d) {
        Range range = plot.getRange();
        g2d.setColor(opt().getColor(0));

        int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
        if (!h && !v) {
            double xx = range.getX1();
            double yy = a * xx + b;
            if (range.contains(xx, yy)) {
                x1 = (int) plot.xscale(xx);
                y1 = (int) plot.yscale(yy);
            } else {
                y1 = (int) plot.yscale(range.getY1());
                x1 = (int) plot.xscale((range.getY1() - b) / a);
            }

            xx = range.getX2();
            yy = a * xx + b;
            if (range.contains(xx, yy)) {
                x2 = (int) plot.xscale(xx);
                y2 = (int) plot.yscale(yy);
            } else {
                y2 = (int) plot.yscale(range.getY2());
                x2 = (int) plot.xscale((range.getY2() - b) / a);
            }
        } else {
            if (h) {
                x1 = (int) plot.xscale(range.getX1());
                y1 = (int) plot.yscale(a);
                x2 = (int) plot.xscale(range.getX2());
                y2 = (int) plot.yscale(a);
            } else {
                x1 = (int) plot.xscale(a);
                y1 = (int) plot.yscale(range.getY1());
                x2 = (int) plot.xscale(a);
                y2 = (int) plot.yscale(range.getY2());
            }
        }
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(opt().getLwd()));
        g2d.drawLine(x1, y1, x2, y2);
        g2d.setStroke(oldStroke);
    }
}
