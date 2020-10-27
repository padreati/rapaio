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

    private final Axis xRange = new Axis();
    private final Axis yRange = new Axis();

    public DataRange() {
    }

    public DataRange(double x1, double y1, double x2, double y2) {
        xRange.setRange(x1, x2);
        yRange.setRange(y1, y2);
    }

    public void union(double x, double y) {
        xRange.union(x);
        yRange.union(y);
    }

    public boolean contains(double x, double y) {
        return xRange.contains(x) && yRange.contains(y);
    }

    public double width() {
        return xRange.getLength();
    }

    public double height() {
        return yRange.getLength();
    }

    public double xMin() {
        return xRange.getMin();
    }

    public double yMin() {
        return yRange.getMin();
    }

    public double xMax() {
        return xRange.getMax();
    }

    public double yMax() {
        return yRange.getMax();
    }

    public void xMin(double x1) {
        xRange.setMin(x1);
    }

    public void xMax(double x2) {
        xRange.setMax(x2);
    }

    public void yMin(double y1) {
        yRange.setMin(y1);
    }

    public void yMax(double y2) {
        yRange.setMax(y2);
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
