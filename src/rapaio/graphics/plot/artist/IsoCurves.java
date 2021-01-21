/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import rapaio.data.Var;
import rapaio.experiment.grid.GridData;
import rapaio.graphics.opt.ColorGradient;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/20/15.
 */
public class IsoCurves extends Artist {

    private static final long serialVersionUID = -642370269224702175L;
    private final GridData grid;
    private final ColorGradient gradient;
    private final double[] levels;
    private final boolean contour;
    private final boolean fill;

    public IsoCurves(GridData grid, boolean contour, boolean fill, ColorGradient gradient, double[] levels, GOption<?>... opts) {
        this.grid = grid;
        this.gradient = gradient;
        this.levels = levels;
        this.contour = contour;
        this.fill = fill;
        this.options.bind(opts);
    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        Var x = grid.getX();
        Var y = grid.getY();
        union(x.getDouble(0), y.getDouble(0));
        union(x.getDouble(x.size() - 1), y.getDouble(y.size() - 1));
    }

    @Override
    public void paint(Graphics2D g2d) {
        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

        Var x = grid.getX();
        Var y = grid.getY();

        BasicStroke fillStroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke contourStroke = new BasicStroke(options.getLwd(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        if (fill) {
            visitEach(g2d, x, y, fillStroke, contourStroke, (fillPoints, fillIndexes, levelIndex) -> {
                if (fillPoints.size() > 2) {
                    g2d.setColor(gradient.getColor(levelIndex));
                    g2d.setStroke(fillStroke);
                    Path2D.Double path = new Path2D.Double();
                    path.moveTo(fillPoints.get(fillPoints.size() - 1).x, fillPoints.get(fillPoints.size() - 1).y);
                    for (Point2D.Double p : fillPoints) {
                        path.lineTo(p.x, p.y);
                    }
                    g2d.draw(path);
                    g2d.fill(path);
                }
            });
        }
        if (contour) {
            visitEach(g2d, x, y, fillStroke, contourStroke, (fillPoints, fillIndexes, levelIndex) -> {

                g2d.setColor(options.getColor(0));
                g2d.setStroke(contourStroke);

                if (fillPoints.size() > 2) {
                    fillPoints.add(fillPoints.get(0));
                    fillIndexes.add(fillIndexes.get(0) + 12);
                }
                int lines = 0;
                for (int l = 0; l < fillPoints.size() - 1; l++) {
                    int i1 = fillIndexes.get(l);
                    int i2 = fillIndexes.get(l + 1);
                    if (((lines > 1) && (i1 < 12 && i2 > 12)) || (i1 < 3 && i2 > 3) || (i1 < 6 && i2 > 6) || (i1 < 9 && i2 > 9)) {
                        g2d.draw(new Line2D.Double(fillPoints.get(l), fillPoints.get(l + 1)));
                        lines++;
                    }
                }
            });
        }
    }

    @FunctionalInterface
    private interface Painter {
        void painter(List<Point2D.Double> fillStroke, List<Integer> contourStroke, Integer index);
    }

    private void visitEach(Graphics2D g2d, Var x, Var y, BasicStroke fillStroke, BasicStroke contourStroke, Painter consumer) {

        for (int l = 0; l < levels.length - 1; l++) {
            MeshStripe mg = new MeshStripe(grid, levels[l], levels[l + 1]);

            for (int i = 0; i < grid.getX().size() - 1; i++) {
                for (int j = 0; j < grid.getY().size() - 1; j++) {

                    if (!contains(x.getDouble(i), y.getDouble(j)))
                        continue;
                    if (!contains(x.getDouble(i + 1), y.getDouble(j + 1)))
                        continue;

                    int k = mg.computeIndex(i, j);

                    if (k == 0 || k == 2222) {
                        // no contour, no fill
                        continue;
                    }

                    Point.Double[] points = new Point2D.Double[12];
                    int[] sides = new int[12];

                    // first fill corners

                    points[0] = new Point2D.Double(xScale(x.getDouble(i)), yScale(y.getDouble(j)));
                    sides[0] = mg.side(i, j);

                    points[3] = new Point2D.Double(xScale(x.getDouble(i + 1)), yScale(y.getDouble(j)));
                    sides[3] = mg.side(i + 1, j);

                    points[6] = new Point2D.Double(xScale(x.getDouble(i + 1)), yScale(y.getDouble(j + 1)));
                    sides[6] = mg.side(i + 1, j + 1);

                    points[9] = new Point2D.Double(xScale(x.getDouble(i)), yScale(y.getDouble(j + 1)));
                    sides[9] = mg.side(i, j + 1);


                    // now fill middle points

                    // (i,j) -> (i+1,j)

                    if (sides[0] == 0 && sides[3] >= 1) {
                        points[1] = new Point2D.Double(xScale(mg.xLow(i, j)), yScale(y.getDouble(j)));
                        sides[1] = 1;
                    }
                    if (sides[0] >= 1 && sides[3] == 0) {
                        points[2] = new Point2D.Double(xScale(mg.xLow(i, j)), yScale(y.getDouble(j)));
                        sides[2] = 1;
                    }

                    if (sides[0] <= 1 && sides[3] == 2) {
                        points[2] = new Point2D.Double(xScale(mg.xHigh(i, j)), yScale(y.getDouble(j)));
                        sides[2] = 2;
                    }
                    if (sides[0] == 2 && sides[3] <= 1) {
                        points[1] = new Point2D.Double(xScale(mg.xHigh(i, j)), yScale(y.getDouble(j)));
                        sides[1] = 2;
                    }

                    // (i+1,j) -> (i+1,j+1)


                    if (sides[3] == 0 && sides[6] >= 1) {
                        points[4] = new Point2D.Double(xScale(x.getDouble(i + 1)), yScale(mg.yLow(i + 1, j)));
                        sides[4] = 1;
                    }
                    if (sides[3] >= 1 && sides[6] == 0) {
                        points[5] = new Point2D.Double(xScale(x.getDouble(i + 1)), yScale(mg.yLow(i + 1, j)));
                        sides[5] = 1;
                    }

                    if (sides[3] <= 1 && sides[6] == 2) {
                        points[5] = new Point2D.Double(xScale(x.getDouble(i + 1)), yScale(mg.yHigh(i + 1, j)));
                        sides[5] = 2;
                    }
                    if (sides[3] == 2 && sides[6] <= 1) {
                        points[4] = new Point2D.Double(xScale(x.getDouble(i + 1)), yScale(mg.yHigh(i + 1, j)));
                        sides[4] = 2;
                    }

                    // (i+1,j+1) -> (i,j+1)

                    if (sides[6] == 0 && sides[9] >= 1) {
                        points[7] = new Point2D.Double(xScale(mg.xLow(i, j + 1)), yScale(y.getDouble(j + 1)));
                        sides[7] = 1;
                    }
                    if (sides[6] >= 1 && sides[9] == 0) {
                        points[8] = new Point2D.Double(xScale(mg.xLow(i, j + 1)), yScale(y.getDouble(j + 1)));
                        sides[8] = 1;
                    }

                    if (sides[6] <= 1 && sides[9] == 2) {
                        points[8] = new Point2D.Double(xScale(mg.xHigh(i, j + 1)), yScale(y.getDouble(j + 1)));
                        sides[8] = 2;
                    }
                    if (sides[6] == 2 && sides[9] <= 1) {
                        points[7] = new Point2D.Double(xScale(mg.xHigh(i, j + 1)), yScale(y.getDouble(j + 1)));
                        sides[7] = 2;
                    }

                    // (i,j+1) -> (i,j)

                    if (sides[9] == 0 && sides[0] >= 1) {
                        points[10] = new Point2D.Double(xScale(x.getDouble(i)), yScale(mg.yLow(i, j)));
                        sides[10] = 1;
                    }
                    if (sides[9] >= 1 && sides[0] == 0) {
                        points[11] = new Point2D.Double(xScale(x.getDouble(i)), yScale(mg.yLow(i, j)));
                        sides[11] = 1;
                    }

                    if (sides[9] <= 1 && sides[0] == 2) {
                        points[11] = new Point2D.Double(xScale(x.getDouble(i)), yScale(mg.yHigh(i, j)));
                        sides[11] = 2;
                    }
                    if (sides[9] == 2 && sides[0] <= 1) {
                        points[10] = new Point2D.Double(xScale(x.getDouble(i)), yScale(mg.yHigh(i, j)));
                        sides[10] = 2;
                    }

                    List<Point2D.Double> fillPoints = new ArrayList<>();
                    List<Integer> fillIndexes = new ArrayList<>();

                    for (int q = 0; q < 12; q++) {
                        Point2D.Double p = points[q];
                        if ((p == null || ((q % 3 == 0) && (sides[q] == 0 || sides[q] == 2)))) {
                            continue;
                        }
                        fillPoints.add(p);
                        fillIndexes.add(q);
                    }

                    consumer.painter(fillPoints, fillIndexes, l);
                }
            }
        }

    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/27/15.
     */
    private static final class MeshStripe implements Serializable {

        private static final long serialVersionUID = -2138677255967203689L;

        private final GridData g;
        private final double low;
        private final double high;
        private final int[] sides;

        public MeshStripe(GridData g, double low, double high) {
            this.g = g;
            this.low = low;
            this.high = high;

            this.sides = new int[g.getX().size() * g.getY().size()];
            for (int i = 0; i < g.getX().size(); i++) {
                for (int j = 0; j < g.getY().size(); j++) {
                    if (g.getValue(i, j) < low) {
                        sides[i * g.getY().size() + j] = 0;
                        continue;
                    }
                    if (g.getValue(i, j) > high) {
                        sides[i * g.getY().size() + j] = 2;
                        continue;
                    }
                    sides[i * g.getY().size() + j] = 1;
                }
            }
        }

        public Var x() {
            return g.getX();
        }

        public Var y() {
            return g.getY();
        }

        /**
         * Computes the side of the mesh grid point.
         *
         * @param i index of the x coordinate
         * @param j index of the y coordinate
         * @return 0 if below isoBand, 1 if inside isoBand, 2 if above isoBand
         */
        public int side(int i, int j) {
            return sides[i * g.getY().size() + j];
        }

        /**
         * Computes x coordinate of the low threshold between
         * grid points with indexes (i,j) and (i+1,j)
         *
         * @param i index of the x coordinate of the starting point
         * @param j index of the y coordinate of the starting point
         * @return x coordinate value of the low threshold
         */
        public double xLow(int i, int j) {
            if ((side(i, j) == 0 && side(i + 1, j) >= 1) || (side(i, j) >= 1 && side(i + 1, j) == 0)) {
                double value = x().getDouble(i) + abs(x().getDouble(i + 1) - x().getDouble(i)) * abs(low - g.getValue(i, j))
                        / abs(g.getValue(i + 1, j) - g.getValue(i, j));
                return max(x().getDouble(i), min(x().getDouble(i + 1), value));
            }
            return Double.NaN;
        }

        /**
         * Computes x coordinate of the high threshold between
         * grid points with indexes (i, j) and (i+1,j)
         *
         * @param i index of the x coordinate of the starting point
         * @param j index of the y coordinate of the starting point
         * @return x coordinate value of the high threshold
         */
        public double xHigh(int i, int j) {
            if ((side(i, j) <= 1 && side(i + 1, j) == 2) || (side(i, j) == 2 && side(i + 1, j) <= 1)) {
                double value = x().getDouble(i) + abs(x().getDouble(i + 1) - x().getDouble(i)) * abs(high - g.getValue(i, j))
                        / abs(g.getValue(i + 1, j) - g.getValue(i, j));
                return max(x().getDouble(i), min(x().getDouble(i + 1), value));
            }
            return Double.NaN;
        }

        /**
         * Computes y coordinate of the low threshold between
         * grid points with indexes (i,j) and (i,j+1)
         *
         * @param i index of the x coordinate of the starting point
         * @param j index of the y coordinate of the starting point
         * @return y coordinate value of the low threshold
         */
        public double yLow(int i, int j) {
            if ((side(i, j) == 0 && side(i, j + 1) >= 1) || (side(i, j) >= 1 && side(i, j + 1) == 0)) {
                double value = y().getDouble(j) + abs(y().getDouble(j + 1) - y().getDouble(j)) * abs(g.getValue(i, j) - low)
                        / abs(g.getValue(i, j + 1) - g.getValue(i, j));
                return max(y().getDouble(j), min(y().getDouble(j + 1), value));
            }
            return Double.NaN;
        }

        /**
         * Computes y coordinate of the high threshold between
         * grid points with indexes (i,j) and (i,j+1)
         *
         * @param i index of the x coordinate of the starting point
         * @param j index of the y coordinate of the starting point
         * @return y coordinate value of the high threshold
         */
        public double yHigh(int i, int j) {
            if ((side(i, j) <= 1 && side(i, j + 1) == 2) || (side(i, j) == 2 && side(i, j + 1) <= 1)) {
                double value = y().getDouble(j) + abs(y().getDouble(j + 1) - y().getDouble(j)) * abs(high - g.getValue(i, j))
                        / abs(g.getValue(i, j + 1) - g.getValue(i, j));
                return max(y().getDouble(j), min(y().getDouble(j + 1), value));
            }
            return Double.NaN;
        }

        public int computeIndex(int i, int j) {
            int k = side(i, j);
            k = k * 10 + side(i + 1, j);
            k = k * 10 + side(i + 1, j + 1);
            k = k * 10 + side(i, j + 1);
            return k;
        }
    }
}