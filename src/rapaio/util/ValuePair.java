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

package rapaio.util;

import java.io.Serializable;

/**
 * Utility pair class for working with numbers
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/23/15.
 */
public class ValuePair implements Serializable {

    private static final long serialVersionUID = -6294811698229236502L;

    public double a = 0.0;
    public double b = 0.0;

    public static ValuePair of(double a, double b) {
        return new ValuePair(a, b);
    }

    public static ValuePair empty() {
        return new ValuePair(0.0, 0.0);
    }

    private ValuePair(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public void fill(double value) {
        this.a = value;
        this.b = value;
    }

    public double sum() {
        return a + b;
    }

    public void normalize() {
        double sum = a + b;
        if (sum == 0) {
            a = 0.0;
            b = 0.0;
        } else {
            a /= sum;
            b /= sum;
        }
    }

    public void update(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public void update(ValuePair p) {
        this.a = p.a;
        this.b = p.b;
    }

    public void increment(ValuePair p) {
        this.a += p.a;
        this.b += p.b;
    }
}
