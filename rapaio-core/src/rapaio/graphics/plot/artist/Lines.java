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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serial;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.opt.GOpts;
import rapaio.graphics.opt.Palette;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Lines extends Artist {

    @Serial
    private static final long serialVersionUID = 9183829873670164532L;
    private final Var x;
    private final Var y;

    public Lines(Var y, GOpt<?>... opts) {
        this(VarDouble.seq(0, y.size() - 1).name("index"), y, opts);
    }

    public Lines(Var x, Var y, GOpt<?>... opts) {
        Frame df = BoundFrame.byVars(x, y).stream().complete().toMappedFrame();
        this.x = df.rvar(0).copy();
        this.y = df.rvar(1).copy();
        this.options = new GOpts().apply(opts);
    }

    @Override
    public Axis.Type xAxisType() {
        return x.type() == VarType.INSTANT ? Axis.Type.newTime() : Axis.Type.newNumeric();
    }

    @Override
    public Axis.Type yAxisType() {
        return y.type() == VarType.INSTANT ? Axis.Type.newTime() : Axis.Type.newNumeric();
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        if (x.size() == 0) {
            return;
        }
        for (int i = 0; i < x.size(); i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            union(x.getDouble(i), y.getDouble(i));
        }
    }

    @Override
    public void paint(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(options.getLwd()));
        g2d.setBackground(Palette.standard().getColor(255));

        for (int i = 1; i < x.size(); i++) {
            g2d.setColor(options.getColor(i));
            g2d.setStroke(new BasicStroke(options.getLwd()));
            double x1 = x.getDouble(i - 1);
            double y1 = y.getDouble(i - 1);
            double x2 = x.getDouble(i);
            double y2 = y.getDouble(i);

            Rectangle2D r = new Clip(plot.xAxis().min(), plot.yAxis().min(), plot.xAxis().max(), plot.yAxis().max())
                    .lineClip(x1, y1, x2, y2);
            if (r != null) {
                x1 = xScale(r.getMinX());
                x2 = xScale(r.getMaxX());
                y1 = yScale(r.getMinY());
                y2 = yScale(r.getMaxY());
                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
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
    private static final class Clip {

        private static final int INSIDE = 0; // 0000
        private static final int LEFT = 1;   // 0001
        private static final int RIGHT = 2;  // 0010
        private static final int BOTTOM = 4; // 0100
        private static final int TOP = 8;    // 1000

        private final double xmin;
        private final double ymin;
        private final double xmax;
        private final double ymax;

        public Clip(double x1, double y1, double x2, double y2) {
            this.xmin = Math.min(x1, x2);
            this.ymin = Math.min(y1, y2);
            this.xmax = Math.max(x1, x2);
            this.ymax = Math.max(y1, y2);
        }
        // Compute the bit code for a point (x, y) using the clip rectangle
        // bounded diagonally by (xmin, ymin), and (xmax, ymax)

        // ASSUME THAT xmax, xmin, ymax and ymin are global constants.

        private int computeOutCode(double x, double y) {
            int code;

            code = INSIDE;          // initialised as being inside of clip window

            if (x < xmin) {
                // to the left of clip window
                code |= LEFT;
            } else if (x > xmax) {
                // to the right of clip window
                code |= RIGHT;
            }
            if (y < ymin) {
                // below the clip window
                code |= BOTTOM;
            } else if (y > ymax) {
                // above the clip window
                code |= TOP;
            }
            return code;
        }

        // Cohen-Sutherland clipping algorithm clips a line from
        // P0 = (x0, y0) to P1 = (x1, y1) against a rectangle with
        // diagonal from (xmin, ymin) to (xmax, ymax).
        public Rectangle2D lineClip(double x0, double y0, double x1, double y1) {
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
            return accept ? new Rectangle2D.Double(x0, y0, x1 - x0, y1 - y0) : null;
        }
    }
}
