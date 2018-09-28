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

/**
 * @author algoshipda
 */
public class Range1D {
    private double min = Double.NaN;
    private double max = Double.NaN;

    public void union(double x) {
        if (Double.isFinite(x)) {
            min = (Double.isFinite(min)) ? Math.min(min, x) : x;
            max = (Double.isFinite(max)) ? Math.max(max, x) : x;
        }
    }

    public void union(Range1D range) {
        union(range.getMin());
        union(range.getMax());
    }

    public boolean contains(double x) {
        return min <= x && x <= max;
    }

    public double length() {
        return max - min;
    }

    public void setRange(double pMin, double pMax) {
        min = pMin;
        max = pMax;
    }

    public void setMin(double x) {
        min = x;
    }

    public void setMax(double x) {
        max = x;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getProperDecimals() {
        int decimals = 0;
        double len = length();
        while (len <= 10.0 && decimals <= 7) {
            len *= 10;
            decimals++;
        }
        return decimals;
    }

    @Override
    public String toString() {
        return "Range1D{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
