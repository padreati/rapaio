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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.experiment.ml.feature.relief;

import rapaio.experiment.math.linear.DMatrix;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/20/18.
 */
public interface ReliefDifferenceFunction {

    double difference(DMatrix x, boolean[] numeric, int index, int row1, int row2);

    static ReliefDifferenceFunction standard() {
        return new StandardReliefDifferenceFunction();
    }
}

class StandardReliefDifferenceFunction implements ReliefDifferenceFunction {

    @Override
    public double difference(DMatrix x, boolean[] numeric, int index, int row1, int row2) {
        if (Double.isNaN(x.get(row1, index)) || Double.isNaN(x.get(row2, index))) {
            return Double.NaN;
        }
        if (numeric[index]) {
            return Math.abs(x.get(row1, index) - x.get(row2, index));
        }
        return x.get(row1, index) == x.get(row2, index) ? 0 : 1;
    }
}
