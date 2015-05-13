/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.ml.classifier;

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;

import java.io.Serializable;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public interface RunningClassifier extends Classifier, Serializable {

    /**
     * Specifies the number of runs for a classifier.
     *
     * @param runs number of runs
     * @return classifier instance
     */
    RunningClassifier withRuns(int runs);

    /**
     * Builds classifier using artifacts from a previous classifier.
     * All weights are equal with 1.
     * </ul>
     *
     * @param df         data set instances
     * @param targetVars target column name
     * @param runs       additional runs to build
     */
    default void learnFurther(Frame df, String targetVars, int runs) {
        learnFurther(df, Numeric.newFill(df.rowCount(), 1.0), targetVars, runs);
    }

    /**
     * Builds classifier using artifacts from a previous classifier.
     *
     * @param df         data set instances
     * @param weights    weights of the instances
     * @param targetVars target column name
     * @param runs       additional runs to build
     */
    void learnFurther(Frame df, Var weights, String targetVars, int runs);

    /**
     * Builds a new fit, using as starting point the previous fit object.
     *
     * @param fit previous fit object, if null a new fit is completed
     * @param df  the frame to be fitted
     * @return new fit object
     */
    CFit fitFurther(CFit fit, Frame df);
}
