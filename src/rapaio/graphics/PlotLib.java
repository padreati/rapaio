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

package rapaio.graphics;

import rapaio.data.Vector;
import rapaio.graphics.base.Figure;
import rapaio.graphics.plot.Histogram;
import rapaio.graphics.plot.Lines;
import rapaio.graphics.plot.Points;
import rapaio.workspace.Workspace;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public final class PlotLib {

    private static Figure lastFigure;

    public static Plot plot() {
        Plot p = new Plot();
        lastFigure = p;
        return p;
    }

    public static Points points(final Vector x, final Vector y) {
        if (!(lastFigure instanceof Plot)) {
            lastFigure = new Plot();
        }

        Plot p = (Plot) lastFigure;
        Points points = new Points(x, y);
        p.add(points);
        return points;
    }

    public static Lines lines(final Vector x, final Vector y) {
        if (!(lastFigure instanceof Plot)) {
            lastFigure = new Plot();
        }

        Plot p = (Plot) lastFigure;
        Lines lines = new Lines(x, y);
        p.add(lines);
        return lines;
    }

    public static Histogram histogram(Vector v) {
        return histogram(v, 30, true, Double.NaN, Double.NaN);
    }

    public static Histogram histogram(Vector v, int bins, boolean prob) {
        return histogram(v, bins, prob, Double.NaN, Double.NaN);
    }

    public static Histogram histogram(Vector v, int bins, boolean prob, double minValue, double maxValue) {
        if (!(lastFigure instanceof Plot)) {
            lastFigure = new Plot();
        }

        Plot p = (Plot) lastFigure;
        Histogram h = new Histogram(v, bins, prob, minValue, maxValue);
        p.add(h);
        return h;
    }

    public static void draw() {
        Workspace.draw(lastFigure);
    }
}
