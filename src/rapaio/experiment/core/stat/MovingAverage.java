/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.core.stat;

import rapaio.data.Var;
import rapaio.data.VarDouble;

public class MovingAverage {

    public static MovingAverage from(Var x, int window) {
        return new MovingAverage(x, window);
    }

    private final VarDouble ma;

    private MovingAverage(Var source, int window) {

        int left = Math.floorDiv(window - 1, 2);
        int right = window - 1 - left;

        ma = VarDouble.empty(source.size()).name("ma-" + source.name());

        for (int i = left; i < source.size() - right; i++) {
            double sum = 0;
            double count = 0;

            for (int j = i - left; j < i + right + 1; j++) {
                if (source.isMissing(j)) {
                    continue;
                }
                count++;
                sum += source.getDouble(j);
            }
            ma.setDouble(i, count > 0 ? sum / count : Double.NaN);
        }
    }

    public VarDouble getValue() {
        return ma;
    }
}
