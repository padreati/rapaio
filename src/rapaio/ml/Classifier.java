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

package rapaio.ml;

import rapaio.core.Summarizable;
import rapaio.data.Frame;
import rapaio.data.Nominal;

import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Classifier<T> extends Summarizable {

    /**
     * Creates a new classifier instance with the same parameters as the original.
     * <p>
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    Classifier newInstance();

    /**
     * Fit a classifier on instances specified by frame, with row weights
     * and target as classColName.
     *
     * @param df            data set instances
     * @param weights       row weights
     * @param targetColName target column name
     */
    void learn(Frame df, List<Double> weights, String targetColName);

    /**
     * Fit a classifier on instances specified by frame, with row weights
     * equal to 1 and target as classColName.
     *
     * @param df            data set instances
     * @param targetColName target column name
     */
    void learn(Frame df, String targetColName);

    /**
     * Builds a new classifier using artifacts from a previous classifier.
     *
     * @param df            data set instances
     * @param targetColName target column name
     */
    void learnFurther(Frame df, List<Double> weights, String targetColName);

    /**
     * Predict classes for new data set instances
     *
     * @param df data set instances
     */
    void predict(Frame df);

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

    /**
     * Returns predicted classes
     *
     * @return nominal vector with predicted classes
     */
    Nominal getPrediction();

    /**
     * Returns predicted class distribution if is computed,
     * otherwise returns null.
     *
     * @return predicted class distribution (frame with one
     * column for each target class, including missing getValue)
     */
    Frame getDistribution();

}
