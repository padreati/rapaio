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
 * @author tutuianu
 */
public class Range implements Serializable {

    private static final long serialVersionUID = -7868093307393360861L;
    private Range1D xRange = new Range1D();
    private Range1D yRange = new Range1D();
    private static final double extendedFactor = 1.05;

    public Range() {
    }

    public Range(double x1, double y1, double x2, double y2) {
        xRange.setRange(x1, x2);
        yRange.setRange(y1, y2);
    }

    public void union(Range range) {
        xRange.union(range.getXRange());
        yRange.union(range.getYRange());
    }

    public void union(double x, double y) {
        xRange.union(x);
        yRange.union(y);
    }

    public boolean contains(double x, double y) {
        return xRange.contains(x) && yRange.contains(y);
    }

    public double width() {
        return xRange.length();
    }

    public double height() {
        return yRange.length();
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

    public void setX1(double x1) {
        xRange.setMin(x1);
    }

    public void setX2(double x2) {
        xRange.setMax(x2);
    }

    public void setY1(double y1) {
        yRange.setMin(y1);
    }

    public void setY2(double y2) {
        yRange.setMax(y2);
    }

    public Range1D getXRange() {
        return xRange;
    }

    public Range1D getYRange() {
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
            extended.setX1(x1() - 1);
            extended.setX2(x2() + 1);
        } else {
            extended.setX1(xMid - xExtRange / 2);
            extended.setX2(xMid + xExtRange / 2);
        }
        double yExtRange = (y2() - y1()) * extendedFactor;
        double yMid = (y1() + y2()) / 2;
        if(y1()==y2()) {
            extended.setY1(y1()-1);
            extended.setY2(y2()+1);
        } else {
            extended.setY1(yMid - yExtRange / 2);
            extended.setY2(yMid + yExtRange / 2);
        }
        return extended;
    }

    @Override
    public String toString() {
        return "Range{" +
                "xRange=" + xRange +
                ", yRange=" + yRange +
                '}';
    }
}
