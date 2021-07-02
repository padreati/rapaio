/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.clustering;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/31/20.
 */
public interface ClusteringModel extends Printable {

    /**
     * Creates a new clustering instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    ClusteringModel newInstance();

    /**
     * Returns the clustering algorithm name.
     *
     * @return clustering name
     */
    String name();

    /**
     * Builds a string which contains the clustering instance name and parameters.
     *
     * @return clustering algorithm name and parameters
     */
    String fullName();

    /**
     * Describes the clustering algorithm
     *
     * @return capabilities of the clustering algorithm
     */
    default Capabilities capabilities() {
        return null;
    }

    /**
     * Returns input variable names built at learning time
     *
     * @return input variable names
     */
    String[] inputNames();

    /**
     * Shortcut method which returns input variable name at the given position
     *
     * @param pos given position
     * @return variable name
     */
    default String inputName(int pos) {
        return inputNames()[pos];
    }

    /**
     * Returns the types of input variables built at learning time
     *
     * @return array of input variable types
     */
    VarType[] inputTypes();

    /**
     * Shortcut method which returns the type of the input variable at the given position
     *
     * @param pos given position
     * @return variable type
     */
    default VarType inputType(int pos) {
        return inputTypes()[pos];
    }

    /**
     * @return true if the algorithm was fitted successfully
     */
    boolean hasLearned();

    /**
     * Fit a clustering model on instances specified by frame, with row weights
     * equal to 1 and target specified by targetNames
     *
     * @param df data set instances
     */
    ClusteringModel fit(Frame df);

    /**
     * Fit a clustering on instances specified by frame, with row weights and targetNames
     *
     * @param df      predict frame
     * @param weights instance weights
     */
    ClusteringModel fit(Frame df, Var weights);

    /**
     * Predict clusters for new data set instances, with
     * default option to compute probability scores if they are available.
     *
     * @param df data set instances
     */
    default <R extends ClusteringResult> R predict(Frame df) {
        return predict(df, true);
    }

    /**
     * Predict clusters for given instances, generating also scores if it is specified.
     *
     * @param df         frame instances
     * @param withScores generate classes
     */
    <R extends ClusteringResult> R predict(Frame df, boolean withScores);
}
