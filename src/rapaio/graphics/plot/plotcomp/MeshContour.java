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

import rapaio.data.Var;
import rapaio.data.grid.MeshGrid;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.plot.PlotComponent;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/20/15.
 */
@Deprecated
public class MeshContour extends PlotComponent {

    private static final long serialVersionUID = -642370269224702175L;
    private final MeshGrid mg;
    private boolean contour = false;
    private boolean fill = false;

    public MeshContour(MeshGrid mg, boolean contour, boolean fill, GOpt... opts) {
        this.mg = mg;
        this.contour = contour;
        this.fill = fill;
        this.options.apply(opts);
    }

    @Override
    protected Range buildRange() {
        return new Range(
                mg.x().value(0),
                mg.y().value(0),
                mg.x().value(mg.x().rowCount() - 1),
                mg.y().value(mg.y().rowCount() - 1)
        );
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

        Var x = mg.x();
        Var y = mg.y();

        LinkedList<Point2D.Double> lines = new LinkedList<>();

        for (int i = 0; i < mg.x().rowCount() - 1; i++) {
            for (int j = 0; j < mg.y().rowCount() - 1; j++) {

                if (!parent.getRange().contains(x.value(i), y.value(j)))
                    continue;
                if (!parent.getRange().contains(x.value(i + 1), y.value(j + 1)))
                    continue;

                int k = mg.side(i, j);
                k = k * 10 + mg.side(i + 1, j);
                k = k * 10 + mg.side(i + 1, j + 1);
                k = k * 10 + mg.side(i, j + 1);

                switch (k) {
                    case 0:
                    case 2222:
                        // no contour
                        // no fill
                        break;

                    case 1111:

                        // no contour
                        // full fill
                        if (fill) {
                            Path2D.Double path = new Path2D.Double();
                            path.moveTo(parent.xScale(x.value(i)), parent.yScale(y.value(j)));
                            path.lineTo(parent.xScale(x.value(i + 1)), parent.yScale(y.value(j)));
                            path.lineTo(parent.xScale(x.value(i + 1)), parent.yScale(y.value(j + 1)));
                            path.lineTo(parent.xScale(x.value(i)), parent.yScale(y.value(j + 1)));
                            path.lineTo(parent.xScale(x.value(i)), parent.yScale(y.value(j)));

                            g2d.setColor(options.getColor(0));
                            g2d.setStroke(new BasicStroke());
                            g2d.draw(path);
                            g2d.fill(path);
                        }
                        break;

                    default:

                        // no saddle point impl for now

                        Point.Double[] points = new Point2D.Double[12];
                        int[] sides = new int[12];

                        // first fill corners

                        points[0] = new Point2D.Double(parent.xScale(x.value(i)), parent.yScale(y.value(j)));
                        sides[0] = mg.side(i, j);

                        points[3] = new Point2D.Double(parent.xScale(x.value(i + 1)), parent.yScale(y.value(j)));
                        sides[3] = mg.side(i + 1, j);

                        points[6] = new Point2D.Double(parent.xScale(x.value(i + 1)), parent.yScale(y.value(j + 1)));
                        sides[6] = mg.side(i + 1, j + 1);

                        points[9] = new Point2D.Double(parent.xScale(x.value(i)), parent.yScale(y.value(j + 1)));
                        sides[9] = mg.side(i, j + 1);


                        // now fill middle points

                        // (i,j) -> (i+1,j)

                        if (sides[0] == 0 && sides[3] >= 1) {
                            points[1] = new Point2D.Double(parent.xScale(mg.xLow(i, j)), parent.yScale(y.value(j)));
                            sides[1] = 1;
                        }
                        if (sides[0] >= 1 && sides[3] == 0) {
                            points[2] = new Point2D.Double(parent.xScale(mg.xLow(i, j)), parent.yScale(y.value(j)));
                            sides[2] = 1;
                        }

                        if (sides[0] <= 1 && sides[3] == 2) {
                            points[2] = new Point2D.Double(parent.xScale(mg.xHigh(i, j)), parent.yScale(y.value(j)));
                            sides[2] = 2;
                        }
                        if (sides[0] == 2 && sides[3] <= 1) {
                            points[1] = new Point2D.Double(parent.xScale(mg.xHigh(i, j)), parent.yScale(y.value(j)));
                            sides[1] = 2;
                        }

                        // (i+1,j) -> (i+1,j+1)


                        if (sides[3] == 0 && sides[6] >= 1) {
                            points[4] = new Point2D.Double(parent.xScale(x.value(i + 1)), parent.yScale(mg.yLow(i + 1, j)));
                            sides[4] = 1;
                        }
                        if (sides[3] >= 1 && sides[6] == 0) {
                            points[5] = new Point2D.Double(parent.xScale(x.value(i + 1)), parent.yScale(mg.yLow(i + 1, j)));
                            sides[5] = 1;
                        }

                        if (sides[3] <= 1 && sides[6] == 2) {
                            points[5] = new Point2D.Double(parent.xScale(x.value(i + 1)), parent.yScale(mg.yHigh(i + 1, j)));
                            sides[5] = 2;
                        }
                        if (sides[3] == 2 && sides[6] <= 1) {
                            points[4] = new Point2D.Double(parent.xScale(x.value(i + 1)), parent.yScale(mg.yHigh(i + 1, j)));
                            sides[4] = 2;
                        }

                        // (i+1,j+1) -> (i,j+1)

                        if (sides[6] == 0 && sides[9] >= 1) {
                            points[7] = new Point2D.Double(parent.xScale(mg.xLow(i, j + 1)), parent.yScale(y.value(j + 1)));
                            sides[7] = 1;
                        }
                        if (sides[6] >= 1 && sides[9] == 0) {
                            points[8] = new Point2D.Double(parent.xScale(mg.xLow(i, j + 1)), parent.yScale(y.value(j + 1)));
                            sides[8] = 1;
                        }

                        if (sides[6] <= 1 && sides[9] == 2) {
                            points[8] = new Point2D.Double(parent.xScale(mg.xHigh(i, j + 1)), parent.yScale(y.value(j + 1)));
                            sides[8] = 2;
                        }
                        if (sides[6] == 2 && sides[9] <= 1) {
                            points[7] = new Point2D.Double(parent.xScale(mg.xHigh(i, j + 1)), parent.yScale(y.value(j + 1)));
                            sides[7] = 2;
                        }

                        // (i,j+1) -> (i,j)

                        if (sides[9] == 0 && sides[0] >= 1) {
                            points[10] = new Point2D.Double(parent.xScale(x.value(i)), parent.yScale(mg.yLow(i, j)));
                            sides[10] = 1;
                        }
                        if (sides[9] >= 1 && sides[0] == 0) {
                            points[11] = new Point2D.Double(parent.xScale(x.value(i)), parent.yScale(mg.yLow(i, j)));
                            sides[11] = 1;
                        }

                        if (sides[9] <= 1 && sides[0] == 2) {
                            points[11] = new Point2D.Double(parent.xScale(x.value(i)), parent.yScale(mg.yHigh(i, j)));
                            sides[11] = 2;
                        }
                        if (sides[9] == 2 && sides[0] <= 1) {
                            points[10] = new Point2D.Double(parent.xScale(x.value(i)), parent.yScale(mg.yHigh(i, j)));
                            sides[11] = 2;
                        }


                        if (fill) {
                            Path2D.Double path = new Path2D.Double();
                            Point2D.Double start = null;

                            for (int q = 0; q < points.length; q++) {
                                Point2D.Double p = points[q];
                                if (p == null || ((q % 3 == 0) && (sides[q] == 0 || sides[q] == 2)))
                                    continue;
                                if (start == null) {
                                    start = p;
                                    path.moveTo(p.x, p.y);
                                } else {
                                    path.lineTo(p.x, p.y);
                                }
                            }
                            if (start == null) {
                                continue;
                            }
                            path.lineTo(start.x, start.y);

                            g2d.setColor(options.getColor(0));
                            g2d.setStroke(new BasicStroke());
                            g2d.draw(path);
                            g2d.fill(path);
                        }

                        if (contour) {
                            LinkedList<Point2D.Double> list = new LinkedList<>();
                            LinkedList<Integer> sideList = new LinkedList<>();
                            for (int q = 0; q < points.length; q++) {
                                Point2D.Double p = points[q];
                                if (p == null || q % 3 == 0)
                                    continue;
                                list.add(p);
                                sideList.add(sides[q]);
                            }
                            if (sideList.size() > 1 && (Math.abs(sideList.get(0) - sideList.get(1)) < 1e-20)) {
                                Point2D.Double p = list.pollLast();
                                list.addFirst(p);
                            }
                            lines.addAll(list);
                        }
                }
            }
        }

        while (!lines.isEmpty()) {
            Point2D.Double from = lines.pollLast();
            Point2D.Double to = lines.pollLast();

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(options.getLwd()));
            g2d.draw(new Line2D.Double(from, to));
        }

    }
}