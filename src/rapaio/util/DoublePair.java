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

import rapaio.sys.WS;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/29/18.
 */
public class DoublePair {
    public double _1;
    public double _2;

    protected DoublePair(double _1, double _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public static DoublePair from(double u, double v) {
        return new DoublePair(u, v);
    }

    @Override
    public String toString() {
        return "Pair{" + WS.formatFlex(_1) + ", " + WS.formatFlex(_2) + "}";
    }


    public void update(double _1, double _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public void update(DoublePair p) {
        this._1 = p._1;
        this._2 = p._2;
    }
}
