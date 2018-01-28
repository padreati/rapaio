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

package rapaio.math.linear;

import rapaio.math.linear.dense.SolidRM;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/7/15.
 */
public class EigenPair {

    private RV rv;
    private RM rm;

    public static EigenPair from(RV values, RM vectors) {
        return new EigenPair(values, vectors);
    }

    private EigenPair(RV values, RM vectors) {
        this.rv = values;
        this.rm = vectors;
    }

    public RV getRV() {
        return rv;
    }

    public RM getRM() {
        return rm;
    }

    public RM expandedValues() {
        RM expandedRV = SolidRM.empty(rv.count(), rv.count());
        for (int i = 0; i < rv.count(); i++) {
            expandedRV.set(i, i, rv.get(i));
        }
        return expandedRV;
    }

    public RV vector(int colNum) {
        return rm.mapCol(colNum);
    }
}
