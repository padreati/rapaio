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

package rapaio.graphics.base;

import java.io.Serializable;

/**
 * @author tutuianu
 */
@Deprecated
public class Range implements Serializable {

    private double x1 = Double.NaN;
    private double y1 = Double.NaN;
    private double x2 = Double.NaN;
    private double y2 = Double.NaN;

    public Range() {
    }

    public Range(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public void union(Range range) {
        union(range.x1(), range.y1());
        union(range.x2(), range.y2());
    }

    public void union(double x, double y) {
        if (x == x) {
            x1 = Math.min(x1 != x1 ? x : x1, x);
            x2 = Math.max(x2 != x2 ? x : x2, x);
        }
        if (y == y) {
            y1 = Math.min(y1 != y1 ? y : y1, y);
            y2 = Math.max(y2 != y2 ? y : y2, y);
        }
    }

    public boolean contains(double x, double y) {
        return x1 <= x && x <= x2 && y1 <= y && y <= y2;
    }

    public double width() {
        return x2 - x1;
    }

    public double height() {
        return y2 - y1;
    }

    public double x1() {
        return x1;
    }

    public double y1() {
        return y1;
    }

    public double x2() {
        return x2;
    }

    public double y2() {
        return y2;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    public int getProperDecimalsX() {
        int decimals = 0;
        double max = Math.abs(x2 - x1);
        while (max <= 10.) {
            max *= 10;
            decimals++;
            if (decimals > 7) {
                return decimals;
            }
        }
        return decimals;
    }

    public int getProperDecimalsY() {
        int decimals = 0;
        double max = Math.abs(y2 - y1);
        while (max <= 10.) {
            max *= 10;
            decimals++;
            if (decimals > 7) {
                return decimals;
            }
        }
        return decimals;
    }
}
