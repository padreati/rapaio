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

package rapaio.graphics.plot;

import java.io.Serializable;

/**
 * A pair of axis which defines the 2 dimensional data range where artists
 * can draw.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DataRange implements Serializable {

    private static final long serialVersionUID = -7868093307393360861L;
    private static final double extendedFactor = 1.025;

    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;

    public DataRange() {
        this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public DataRange(double x1, double y1, double x2, double y2) {
        xMin = x1;
        xMax = x2;
        yMin = y1;
        yMax = y2;
    }

    public void union(double x, double y) {
        xMin = Double.isFinite(xMin) ? Math.min(xMin, x) : x;
        xMax = Double.isFinite(xMax) ? Math.max(xMax, x) : x;
        yMin = Double.isFinite(yMin) ? Math.min(yMin, y) : y;
        yMax = Double.isFinite(yMax) ? Math.max(yMax, y) : y;
    }

    public boolean contains(double x, double y) {
        return (xMin <= x) && (x <= xMax) && (yMin <= y) && (y <= yMax);
    }

    public double width() {
        return xMax - xMin;
    }

    public double height() {
        return yMax - yMin;
    }

    public double xMin() {
        return xMin;
    }

    public double yMin() {
        return yMin;
    }

    public double xMax() {
        return xMax;
    }

    public double yMax() {
        return yMax;
    }

    public void xMin(double x1) {
        xMin = x1;
    }

    public void xMax(double x2) {
        xMax = x2;
    }

    public void yMin(double y1) {
        yMin = y1;
    }

    public void yMax(double y2) {
        yMax = y2;
    }

    public DataRange getExtendedRange() {
        DataRange extended = new DataRange();
        double xExtRange = (xMax() - xMin()) * extendedFactor;
        double xMid = (xMin() + xMax()) / 2;
        if (xMin() == xMax()) {
            extended.xMin(xMin() - 1);
            extended.xMax(xMax() + 1);
        } else {
            extended.xMin(xMid - xExtRange / 2);
            extended.xMax(xMid + xExtRange / 2);
        }
        double yExtRange = (yMax() - yMin()) * extendedFactor;
        double yMid = (yMin() + yMax()) / 2;
        if (yMin() == yMax()) {
            extended.yMin(yMin() - 1);
            extended.yMax(yMax() + 1);
        } else {
            extended.yMin(yMid - yExtRange / 2);
            extended.yMax(yMid + yExtRange / 2);
        }
        return extended;
    }
}
