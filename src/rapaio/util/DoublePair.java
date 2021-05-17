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

package rapaio.util;

import static rapaio.printer.Format.floatFlex;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/29/18.
 */
public final class DoublePair {

    public static DoublePair of(double u, double v) {
        return new DoublePair(u, v);
    }

    public static DoublePair zeros() {
        return new DoublePair(0.0, 0.0);
    }

    public double v1;
    public double v2;

    private DoublePair(double v1, double v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public String toString() {
        return "Pair{" + floatFlex(v1) + ", " + floatFlex(v2) + "}";
    }

    public void update(double v1, double v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public void update(DoublePair p) {
        this.v1 = p.v1;
        this.v2 = p.v2;
    }

    public double sum() {
        return v1 + v2;
    }

    public void normalize() {
        double sum = sum();
        if (sum == 0) {
            v1 = 0.0;
            v2 = 0.0;
        } else {
            v1 /= sum;
            v2 /= sum;
        }
    }

    public void fill(double value) {
        this.v1 = value;
        this.v2 = value;
    }

    public void increment(DoublePair p) {
        this.v1 += p.v1;
        this.v2 += p.v2;
    }
}
