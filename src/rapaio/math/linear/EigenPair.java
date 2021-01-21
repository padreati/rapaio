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

package rapaio.math.linear;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import rapaio.math.linear.dense.DMatrixStripe;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/7/15.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EigenPair {

    private final DVector DVector;
    private final DMatrix DMatrix;

    public static EigenPair from(DVector values, DMatrix vectors) {
        return new EigenPair(values, vectors);
    }

    public DMatrix expandedValues() {
        DMatrix expandedRV = DMatrixStripe.empty(DVector.size(), DVector.size());
        for (int i = 0; i < DVector.size(); i++) {
            expandedRV.set(i, i, DVector.get(i));
        }
        return expandedRV;
    }

    public DVector vector(int colNum) {
        return DMatrix.mapCol(colNum);
    }
}
