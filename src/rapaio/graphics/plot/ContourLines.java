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

import rapaio.core.MathBase;
import rapaio.graphics.base.Range;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.function.BiFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/20/15.
 */
public class ContourLines extends PlotComponent {

    private final BiFunction<Double, Double, Double> f;
    private final double threshold;
    private int xBins = 256;
    private int yBins = 256;

    // computation
    private boolean computed = false;
    private boolean[][] grid;
    private double xStart;
    private double xEnd;
    private double yStart;
    private double yEnd;

    private double xStep;
    private double yStep;

    public ContourLines(BiFunction<Double, Double, Double> f, double threshold) {
        this.f = f;
        this.threshold = threshold;
    }

    @Override
    protected Range buildRange() {
        return null;
    }

    private void computeGrid() {
        xStart = getRange().x1();
        xEnd = getRange().x2();
        yStart = getRange().y1();
        yEnd = getRange().y2();

        grid = new boolean[xBins + 1][yBins + 1];
        xStep = (xEnd - xStart) / ((double) xBins);
        yStep = (yEnd - yStart) / ((double) yBins);

        for (int i = 0; i < xBins + 1; i++) {
            for (int j = 0; j < yBins + 1; j++) {
                double value = f.apply(xStart + i * xStep, yStart + j * yStep);
                grid[i][j] = MathBase.sm(value, threshold);
            }
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        if (!computed) {
            computeGrid();
        }

        g2d.setColor(getCol(0));
        for (int i = 0; i < xBins; i++) {
            for (int j = 0; j < yBins; j++) {

                boolean tl = grid[i][j];
                boolean tr = grid[i + 1][j];
                boolean bl = grid[i][j + 1];
                boolean br = grid[i + 1][j + 1];

                if (tl && tr && !bl && !br) {
                    g2d.draw(new Line2D.Double(
                            parent.xScale(xStart + i * xStep),
                            parent.yScale(yStart + j * yStep),
                            parent.xScale(xStart + (i + 1) * xStep),
                            parent.yScale(yStart + j * yStep)
                    ));
                }
            }
        }
    }
}
