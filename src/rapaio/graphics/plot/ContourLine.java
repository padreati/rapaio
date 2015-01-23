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

import rapaio.data.Var;
import rapaio.data.grid.MeshGrid;
import rapaio.graphics.base.Range;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/20/15.
 */
public class ContourLine extends PlotComponent {

    private final MeshGrid mg;
    private final double threshold;
    private boolean fill = false;

    public ContourLine(MeshGrid mg, double threshold) {
        this.mg = mg;
        this.threshold = threshold;
    }

    public ContourLine withFill(boolean fill) {
        this.fill = fill;
        return this;
    }

    @Override
    protected Range buildRange() {
        return new Range(
                mg.getX().value(0),
                mg.getY().value(0),
                mg.getX().value(mg.getX().rowCount() - 1),
                mg.getY().value(mg.getY().rowCount() - 1)
        );
    }

    @Override
    public void paint(Graphics2D g2d) {
        Point2D.Double[] points = new Point2D.Double[4];
        g2d.setColor(getCol(0));
        int pointsCount = 0;

        Var x = mg.getX();
        Var y = mg.getY();

        for (int i = 0; i < mg.getX().rowCount() - 1; i++) {
            for (int j = 0; j < mg.getY().rowCount() - 1; j++) {

                for (int k = 0; k < 4; k++) {
                    points[k] = null;
                }
                pointsCount = 0;

                if (mg.value(i, j) >= threshold ^ mg.value(i, j + 1) >= threshold) {
                    double r = Math.abs(threshold - mg.value(i, j)) / Math.abs(mg.value(i, j) - mg.value(i, j + 1));
                    points[0] = new Point2D.Double(
                            parent.xScale(x.value(i)),
                            parent.yScale(y.value(j) + Math.abs(y.value(j) - y.value(j + 1)) * r)
                    );
                    pointsCount++;
                }
                if (mg.value(i, j + 1) >= threshold ^ mg.value(i + 1, j + 1) >= threshold) {
                    double r = Math.abs(threshold - mg.value(i, j + 1)) / Math.abs(mg.value(i, j + 1) - mg.value(i + 1, j + 1));
                    points[1] = new Point2D.Double(
                            parent.xScale(x.value(i) + Math.abs(x.value(i) - x.value(i + 1)) * r),
                            parent.yScale(y.value(j + 1))
                    );
                    pointsCount++;
                }
                if (mg.value(i + 1, j) >= threshold ^ mg.value(i + 1, j + 1) >= threshold) {
                    double r = Math.abs(threshold - mg.value(i + 1, j)) / Math.abs(mg.value(i + 1, j) - mg.value(i + 1, j + 1));
                    points[2] = new Point2D.Double(
                            parent.xScale(x.value(i + 1)),
                            parent.yScale(y.value(j) + Math.abs(y.value(j) - y.value(j + 1)) * r)
                    );
                    pointsCount++;
                }
                if (mg.value(i, j) >= threshold ^ mg.value(i + 1, j) >= threshold) {
                    double r = Math.abs(threshold - mg.value(i, j)) / Math.abs(mg.value(i, j) - mg.value(i + 1, j));
                    points[3] = new Point2D.Double(
                            parent.xScale(x.value(i) + Math.abs(x.value(i) - x.value(i + 1)) * r),
                            parent.yScale(y.value(j))
                    );
                    pointsCount++;
                }
                if (pointsCount == 0) {
                    if (fill) {
                        if (mg.value(i, j) >= threshold) {
                            Path2D.Double path = new Path2D.Double();
                            path.moveTo(parent.xScale(x.value(i)), parent.yScale(y.value(j)));
                            path.lineTo(parent.xScale(x.value(i)), parent.yScale(y.value(j + 1)));
                            path.lineTo(parent.xScale(x.value(i + 1)), parent.yScale(y.value(j + 1)));
                            path.lineTo(parent.xScale(x.value(i + 1)), parent.yScale(y.value(j)));
                            path.lineTo(parent.xScale(x.value(i)), parent.yScale(y.value(j)));
                            g2d.fill(path);
                        }
                    }
                    continue;
                }
                if (pointsCount == 2) {
                    Point2D.Double p1 = null;
                    Point2D.Double p2 = null;
                    for (int k = 0; k < 4; k++) {
                        if (points[k] != null) {
                            if (p1 == null) {
                                p1 = points[k];
                            } else {
                                p2 = points[k];
                            }
                        }
                    }
                    g2d.draw(new Line2D.Double(p1, p2));
                    if (fill) {

                        Path2D.Double path = new Path2D.Double();
                        boolean start = false;
                        double startX = Double.NaN;
                        double startY = Double.NaN;

                        if (mg.value(i, j) >= threshold) {
                            double xx = parent.xScale(x.value(i));
                            double yy = parent.yScale(y.value(j));

                            start = true;
                            startX = xx;
                            startY = yy;
                            path.moveTo(xx, yy);
                        }
                        if (points[0] != null) {
                            if (!start) {
                                start = true;
                                startX = points[0].x;
                                startY = points[0].y;
                                path.moveTo(points[0].x, points[0].y);
                            } else {
                                path.lineTo(points[0].x, points[0].y);
                            }
                        }
                        if (mg.value(i, j + 1) >= threshold) {
                            double xx = parent.xScale(x.value(i));
                            double yy = parent.yScale(y.value(j + 1));

                            if (!start) {
                                start = true;
                                startX = xx;
                                startY = yy;
                                path.moveTo(xx, yy);
                            } else {
                                path.lineTo(xx, yy);
                            }
                        }
                        if (points[1] != null) {
                            if (!start) {
                                start = true;
                                startX = points[1].x;
                                startY = points[1].y;
                                path.moveTo(points[1].x, points[1].y);
                            } else {
                                path.lineTo(points[1].x, points[1].y);
                            }
                        }
                        if (mg.value(i + 1, j + 1) >= threshold) {
                            double xx = parent.xScale(x.value(i + 1));
                            double yy = parent.yScale(y.value(j + 1));

                            if (!start) {
                                start = true;
                                startX = xx;
                                startY = yy;
                                path.moveTo(xx, yy);
                            } else {
                                path.lineTo(xx, yy);
                            }
                        }
                        if (points[2] != null) {
                            if (!start) {
                                start = true;
                                startX = points[2].x;
                                startY = points[2].y;
                                path.moveTo(points[2].x, points[2].y);
                            } else {
                                path.lineTo(points[2].x, points[2].y);
                            }
                        }
                        if (mg.value(i + 1, j) >= threshold) {
                            double xx = parent.xScale(x.value(i + 1));
                            double yy = parent.yScale(y.value(j));

                            if (!start) {
                                start = true;
                                startX = xx;
                                startY = yy;
                                path.moveTo(xx, yy);
                            } else {
                                path.lineTo(xx, yy);
                            }
                        }
                        if (points[3] != null) {
                            if (!start) {
                                startX = points[3].x;
                                startY = points[3].y;
                                path.moveTo(points[3].x, points[3].y);
                            } else {
                                path.lineTo(points[3].x, points[3].y);
                            }
                        }

                        path.lineTo(startX, startY);
                        g2d.fill(path);
                    }
                    continue;
                }
                if (pointsCount == 4) {
                    g2d.draw(new Line2D.Double(points[0], points[2]));
                    g2d.draw(new Line2D.Double(points[1], points[3]));
                    continue;
                }
                throw new RuntimeException("This should not happen");
            }
        }
    }
}