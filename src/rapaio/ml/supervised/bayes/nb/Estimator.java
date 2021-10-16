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

package rapaio.ml.supervised.bayes.nb;

import java.io.Serializable;
import java.util.List;

import rapaio.data.Frame;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/4/20.
 */
public interface Estimator extends Serializable {

    /**
     * @return new unfitted instance of the estimator
     */
    Estimator newInstance();

    /**
     * @return estimator name
     */
    String name();

    /**
     * @return estimator name after fit
     */
    String fittedName();

    /**
     * Get test variable names for this estimator
     *
     * @return list of test variable names
     */
    List<String> getTestNames();

    /**
     * Fit the estimator on data
     *
     * @param df         frame with observations
     * @param weights    vector of weights
     * @param targetName target variable name
     * @return true if estimator fit on data, false for failure
     */
    boolean fit(Frame df, Var weights, String targetName);

    /**
     * Predicts p(x|target=targetLevel)
     *
     * @param df          frame with observations
     * @param row         row index of the observation
     * @param targetLevel target level for conditional distribution
     * @return conditioned probability prediction
     */
    double predict(Frame df, int row, String targetLevel);
}
