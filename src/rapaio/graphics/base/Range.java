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

package rapaio.graphics.base;

import java.io.Serializable;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Range implements Serializable {

    private static final long serialVersionUID = -7868093307393360861L;
    private static final double extendedFactor = 1.025;

    private final Range1D xRange = new Range1D();
    private final Range1D yRange = new Range1D();

    public Range() {
    }

    public Range(double x1, double y1, double x2, double y2) {
        xRange.setRange(x1, x2);
        yRange.setRange(y1, y2);
    }

    public void union(Range range) {
        xRange.union(range.xRange());
        yRange.union(range.yRange());
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

    public double x1() {
        return xRange.getMin();
    }

    public double y1() {
        return yRange.getMin();
    }

    public double x2() {
        return xRange.getMax();
    }

    public double y2() {
        return yRange.getMax();
    }

    public void x1(double x1) {
        xRange.setMin(x1);
    }

    public void x2(double x2) {
        xRange.setMax(x2);
    }

    public void y1(double y1) {
        yRange.setMin(y1);
    }

    public void y2(double y2) {
        yRange.setMax(y2);
    }

    public Range1D xRange() {
        return xRange;
    }

    public Range1D yRange() {
        return yRange;
    }

    public int getProperDecimalsX() {
        return xRange.getProperDecimals();
    }

    public int getProperDecimalsY() {
        return yRange.getProperDecimals();
    }

    public Range getExtendedRange() {

        Range extended = new Range();
        double xExtRange = (x2() - x1()) * extendedFactor;
        double xMid = (x1() + x2()) / 2;
        if (x1() == x2()) {
            extended.x1(x1() - 1);
            extended.x2(x2() + 1);
        } else {
            extended.x1(xMid - xExtRange / 2);
            extended.x2(xMid + xExtRange / 2);
        }
        double yExtRange = (y2() - y1()) * extendedFactor;
        double yMid = (y1() + y2()) / 2;
        if (y1() == y2()) {
            extended.y1(y1() - 1);
            extended.y2(y2() + 1);
        } else {
            extended.y1(yMid - yExtRange / 2);
            extended.y2(yMid + yExtRange / 2);
        }
        return extended;
    }

    @Override
    public String toString() {
        return String.format("Range{x1=%f,y1=%f,x2=%f,y2=%f", x1(), y1(), x2(), y2());
    }
}
