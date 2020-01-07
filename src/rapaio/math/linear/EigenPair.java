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

package rapaio.math.linear;

import rapaio.math.linear.dense.SolidDMatrix;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/7/15.
 */
public class EigenPair {

    private DVector DVector;
    private DMatrix DMatrix;

    public static EigenPair from(DVector values, DMatrix vectors) {
        return new EigenPair(values, vectors);
    }

    private EigenPair(DVector values, DMatrix vectors) {
        this.DVector = values;
        this.DMatrix = vectors;
    }

    public DVector getRV() {
        return DVector;
    }

    public DMatrix getRM() {
        return DMatrix;
    }

    public DMatrix expandedValues() {
        DMatrix expandedRV = SolidDMatrix.empty(DVector.size(), DVector.size());
        for (int i = 0; i < DVector.size(); i++) {
            expandedRV.set(i, i, DVector.get(i));
        }
        return expandedRV;
    }

    public DVector vector(int colNum) {
        return DMatrix.mapCol(colNum);
    }
}
