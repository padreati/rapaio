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

package rapaio.util;

/**
 * Utility pair class for working with numbers
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/23/15.
 */
public class ValuePair extends Pair<Double, Double> {

    private static final long serialVersionUID = -6294811698229236502L;

    public static ValuePair of(double _1, double _2) {
        return new ValuePair(_1, _2);
    }

    public static ValuePair empty() {
        return new ValuePair(0.0, 0.0);
    }


    private ValuePair(double _1, double _2) {
    	super(_1, _2);
    }


    public double sum() {
        return _1 + _2;
    }

    public void normalize() {
        double sum = sum();
        if (sum == 0) {
            _1 = 0.0;
            _2 = 0.0;
        } else {
            _1 /= sum;
            _2 /= sum;
        }
    }


    public void fill(double value) {
        this._1 = value;
        this._2 = value;
    }

    public void increment(ValuePair p) {
        this._1 += p._1;
        this._2 += p._2;
    }
}
