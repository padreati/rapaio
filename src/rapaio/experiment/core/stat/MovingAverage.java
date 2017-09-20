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

package rapaio.experiment.core.stat;

import rapaio.data.NumericVar;
import rapaio.data.Var;

public class MovingAverage {

    public static MovingAverage from(Var x, int window) {
        return new MovingAverage(x, window);
    }

    private final NumericVar ma;

    private MovingAverage(Var source, int window) {

        int left = Math.floorDiv(window - 1, 2);
        int right = window - 1 - left;

        ma = NumericVar.empty(source.rowCount()).withName("ma-" + source.name());

        for (int i = left; i < source.rowCount() - right; i++) {
            double sum = 0;
            double count = 0;

            for (int j = i - left; j < i + right + 1; j++) {
                if (source.isMissing(j)) {
                    continue;
                }
                count++;
                sum += source.value(j);
            }
            ma.setValue(i, count > 0 ? sum / count : Double.NaN);
        }
    }

    public NumericVar getValue() {
        return ma;
    }
}
