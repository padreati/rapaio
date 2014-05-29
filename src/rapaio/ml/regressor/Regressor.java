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

package rapaio.ml.regressor;

import rapaio.data.Frame;
import rapaio.data.Var;

import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public interface Regressor {

    /**
     * Builds a new regressor model. It keeps teh same parameters, but
     * it does not copy all the regression artifacts
     * @return a new instance
     */
    Regressor newInstance();

    @Deprecated
    default void learn(Frame df, List<Double> weights, String targetColName) {}

    void learn(Frame df, String targetCols);

    void predict(Frame df);

    Var getFitValues();

    Frame getAllFitValues();
}
