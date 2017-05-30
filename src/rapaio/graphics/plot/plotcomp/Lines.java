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

import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.plot.PlotComponent;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Lines extends PlotComponent {

    private static final long serialVersionUID = 9183829873670164532L;
    private final Var x;
    private final Var y;

    public Lines(Var y, GOpt... opts) {
        this(NumericVar.seq(0, y.getRowCount() - 1), y, opts);
    }

    public Lines(Var x, Var y, GOpt... opts) {
        this.x = x;
        this.y = y;
        this.options.apply(opts);
    }

    @Override
    public Range buildRange() {
        if (x.getRowCount() == 0) {
            return null;
        }
        Range range = new Range();
        for (int i = 0; i < x.getRowCount(); i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            range.union(x.getValue(i), y.getValue(i));
        }
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(options.getLwd()));
        g2d.setBackground(ColorPalette.STANDARD.getColor(255));

        for (int i = 1; i < x.getRowCount(); i++) {
            g2d.setColor(options.getColor(i));
            double x1 = x.getValue(i - 1);
            double y1 = y.getValue(i - 1);
            double x2 = x.getValue(i);
            double y2 = y.getValue(i);

            Range r = new Clip(parent.getRange()).lineClip(x1, y1, x2, y2);
            if (r != null) {
                x1 = xScale(r.x1());
                x2 = xScale(r.x2());
                y1 = yScale(r.y1());
                y2 = yScale(r.y2());
                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }
    }

}

/**
 * Code to compute a line segment clipped by a rectangle
 * <p>
 * Code copied from wikipedia page for Cohen-Sutherland algorithm:
 * <p>
 * https://en.wikipedia.org/wiki/Cohen%E2%80%93Sutherland_algorithm
 */
class Clip {

    private final int INSIDE = 0; // 0000
    private final int LEFT = 1;   // 0001
    private final int RIGHT = 2;  // 0010
    private final int BOTTOM = 4; // 0100
    private final int TOP = 8;    // 1000

    private final double xmin;
    private final double ymin;
    private final double xmax;
    private final double ymax;

    public Clip(Range r) {
        this.xmin = Math.min(r.x1(), r.x2());
        this.ymin = Math.min(r.y1(), r.y2());
        this.xmax = Math.max(r.x1(), r.x2());
        this.ymax = Math.max(r.y1(), r.y2());
    }
    // Compute the bit code for a point (x, y) using the clip rectangle
    // bounded diagonally by (xmin, ymin), and (xmax, ymax)

    // ASSUME THAT xmax, xmin, ymax and ymin are global constants.

    private int computeOutCode(double x, double y) {
        int code;

        code = INSIDE;          // initialised as being inside of clip window

        if (x < xmin)           // to the left of clip window
            code |= LEFT;
        else if (x > xmax)      // to the right of clip window
            code |= RIGHT;
        if (y < ymin)           // below the clip window
            code |= BOTTOM;
        else if (y > ymax)      // above the clip window
            code |= TOP;

        return code;
    }

    // Cohen–Sutherland clipping algorithm clips a line from
    // P0 = (x0, y0) to P1 = (x1, y1) against a rectangle with
    // diagonal from (xmin, ymin) to (xmax, ymax).
    public Range lineClip(double x0, double y0, double x1, double y1) {
        // compute outcodes for P0, P1, and whatever point lies outside the clip rectangle
        int outcode0 = computeOutCode(x0, y0);
        int outcode1 = computeOutCode(x1, y1);
        boolean accept = false;

        while (true) {
            if ((outcode0 | outcode1) == 0) { // Bitwise OR is 0. Trivially accept and get out of loop
                accept = true;
                break;
            } else if ((outcode0 & outcode1) > 0) { // Bitwise AND is not 0. Trivially reject and get out of loop
                break;
            } else {
                // failed both tests, so calculate the line segment to clip
                // from an outside point to an intersection with clip edge
                double x = 0;
                double y = 0;

                // At least one endpoint is outside the clip rectangle; pick it.
                int outcodeOut = outcode0 > 0 ? outcode0 : outcode1;

                // Now find the intersection point;
                // use formulas y = y0 + slope * (x - x0), x = x0 + (1 / slope) * (y - y0)
                if ((outcodeOut & TOP) > 0) {           // point is above the clip rectangle
                    x = x0 + (x1 - x0) * (ymax - y0) / (y1 - y0);
                    y = ymax;
                } else if ((outcodeOut & BOTTOM) > 0) { // point is below the clip rectangle
                    x = x0 + (x1 - x0) * (ymin - y0) / (y1 - y0);
                    y = ymin;
                } else if ((outcodeOut & RIGHT) > 0) {  // point is to the right of clip rectangle
                    y = y0 + (y1 - y0) * (xmax - x0) / (x1 - x0);
                    x = xmax;
                } else if ((outcodeOut & LEFT) > 0) {   // point is to the left of clip rectangle
                    y = y0 + (y1 - y0) * (xmin - x0) / (x1 - x0);
                    x = xmin;
                }

                // Now we move outside point to intersection point to clip
                // and get ready for next pass.
                if (outcodeOut == outcode0) {
                    x0 = x;
                    y0 = y;
                    outcode0 = computeOutCode(x0, y0);
                } else {
                    x1 = x;
                    y1 = y;
                    outcode1 = computeOutCode(x1, y1);
                }
            }
        }
        return accept ? new Range(x0, y0, x1, y1) : null;
    }
}
