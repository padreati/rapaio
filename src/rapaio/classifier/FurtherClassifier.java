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

package rapaio.classifier;

import rapaio.data.Frame;
import rapaio.data.Numeric;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public interface FurtherClassifier<T extends Classifier> extends Classifier {

    /**
     * Builds a new classifier using artifacts from a previous classifier, and
     * given weights for current data set.
     *
     * @param df         data set instances
     * @param targetName target column name
     */
    default void learnFurther(Frame df, String targetName) {
        Numeric weights = new Numeric(df.rowCount());
        weights.stream().transformValue(x -> 1.0);
        learnFurther(df, weights, targetName);
    }

    /**
     * Builds a new classifier using artifacts from a previous classifier.
     *
     * @param df         data set instances
     * @param weights    weights for each observation
     * @param targetName target column name
     */
    void learnFurther(Frame df, Numeric weights, String targetName);

    /**
     * Predict further classes for new data set instances, using
     * as much as possible fitted artifacts from previous classifier.
     * <p>
     * The frame df is supposed to be the same, otherwise
     * the result is unpredictable
     *
     * @param df data set instances
     */
    void predictFurther(Frame df, T classifier);

}
