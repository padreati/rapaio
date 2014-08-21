/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.classifier;

import rapaio.data.Frame;
import rapaio.data.Numeric;

import java.io.Serializable;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public interface RunningClassifier extends Classifier, Serializable {


    RunningClassifier withRuns(int runs);

    /**
     * Builds a new classifier using artifacts from a previous classifier.
     * The weights used comes from:
     * <ul>
     * <li>if prev classifier has weights, then these weights are used</li>
     * <li>if not, but non-null weights are specified in this method, than
     * weights given as parameters are used</li>
     * <li>if not than weights equal with one are build and used as parameter</li>
     * </ul>
     *
     * @param df         data set instances
     * @param targetName target column name
     * @param runs       additional runs to build
     */
    default void learnFurther(Frame df, String targetName, int runs) {
        learnFurther(df, Numeric.newFill(df.rowCount(), 1.0), targetName, runs);
    }

    void learnFurther(Frame df, Numeric weights, String targetName, int runs);
}
