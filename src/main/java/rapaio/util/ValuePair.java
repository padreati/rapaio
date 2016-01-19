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

    public double _1 = 0.0;
    public double _2 = 0.0;

    public static ValuePair of(double _1, double _2) {
        return new ValuePair(_1, _2);
    }

    public static ValuePair empty() {
        return new ValuePair(0.0, 0.0);
    }

    private ValuePair(double _1, double _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public void fill(double value) {
        this._1 = value;
        this._2 = value;
    }

    public double sum() {
        return _1 + _2;
    }

    public void normalize() {
        double sum = _1 + _2;
        if (sum == 0) {
            _1 = 0.0;
            _2 = 0.0;
        } else {
            _1 /= sum;
            _2 /= sum;
        }
    }

    public void update(double _1, double _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public void update(ValuePair p) {
        this._1 = p._1;
        this._2 = p._2;
    }

    public void increment(ValuePair p) {
        this._1 += p._1;
        this._2 += p._2;
    }
}
