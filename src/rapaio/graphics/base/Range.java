/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

    public int getProperDecimasX() {
        return xRange.getProperDecimals();
    }

    public int getProperDecimalsY() {
        return yRange.getProperDecimals();
    }
=======
/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

    public int getProperDecimasX() {
        return xRange.getProperDecimals();
    }

    public int getProperDecimalsY() {
        return yRange.getProperDecimals();
    }
}
