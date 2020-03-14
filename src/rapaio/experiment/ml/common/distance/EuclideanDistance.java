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

package rapaio.experiment.ml.common.distance;

import rapaio.data.Frame;
import rapaio.util.Pair;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/18/17.
 */
public class EuclideanDistance implements Distance {

    private static final long serialVersionUID = -8612343329421925879L;

    @Override
    public String name() {
        return "Euclidean";
    }

    /**
     * The distance in the euclidean space is the square root value of the sum of squared differences for each feature.
     * The Euclidean error is the squared distance
     *
     * @param s first data frame
     * @param sRow row index of the instance from that data frame
     * @param t second data frame
     * @param tRow row index of the instance from the second data frame
     * @param varNames variable names of the features used in computation
     * @return
     */
    @Override
    public Pair<Double, Double> compute(Frame s, int sRow, Frame t, int tRow, String... varNames) {
        double total = 0;
        for (String varName : varNames) {
            if (s.isMissing(sRow, varName) || t.isMissing(tRow, varName))
                continue;
            total += Math.pow(s.getDouble(sRow, varName) - t.getDouble(tRow, varName), 2);
        }
        return Pair.from(Math.sqrt(total), total);
    }


}
