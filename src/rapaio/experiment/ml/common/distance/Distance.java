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

import java.io.Serializable;

/**
 * Distance interface
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/23/15.
 */
public interface Distance extends Serializable {

    String name();

    /**
     * Computes the distance and the error for that distance.
     * The distance is the value which measures the similarity
     * between two items, the error is an individual additive value which is used to
     * assess the performance of an algorithm.
     *
     * @param s        first data frame
     * @param sRow     row index of the instance from that data frame
     * @param t        second data frame
     * @param tRow     row index of the instance from the second data frame
     * @param varNames variable names of the features used in computation
     * @return a pair of values, first in pair is the distance, second in pair is the error
     */
    Pair<Double, Double> compute(Frame s, int sRow, Frame t, int tRow, String... varNames);

    Distance EUCLIDEAN = new EuclideanDistance();
}
