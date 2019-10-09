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

package rapaio.experiment.ml.feature.relief;

import rapaio.math.linear.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/19/18.
 */
public interface ReliefDistanceFunction {

    double distance(RM m, boolean[] numeric, int i, int j);

    static ReliefDistanceFunction l2() {
        return new L2ReliefScoreFunction();
    }
}

class L2ReliefScoreFunction implements ReliefDistanceFunction {

    @Override
    public double distance(RM m, boolean[] numeric, int i, int j) {
        double distance = 0.0;
        for (int k = 0; k < numeric.length; k++) {
            if (Double.isNaN(m.get(i, k)) || Double.isNaN(m.get(j, k))) {
                continue;
            }
            boolean num = numeric[k];
            if (num) {
                distance += Math.pow(m.get(i, k) - m.get(j, k), 2);
            } else {
                distance += m.get(i, k) == m.get(j, k) ? 0 : 1;
            }
        }
        return distance / numeric.length;
    }
}