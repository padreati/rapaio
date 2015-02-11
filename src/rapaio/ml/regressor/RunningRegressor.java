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
 */

package rapaio.ml.regressor;

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/1/14.
 */
public interface RunningRegressor {

    RunningRegressor withRuns(int runs);

    /**
     * Builds a new regressor using artifacts from a previous regressor.
     * The weights used comes from:
     * <ul>
     * <li>if prev classifier has weights, then these weights are used</li>
     * <li>if not, but non-null weights are specified in this method, than
     * weights given as parameters are used</li>
     * <li>if not than weights equal with one are build and used as parameter</li>
     * </ul>
     *
     * @param df         data set instances
     * @param targetVars target column name
     * @param runs       additional runs to build
     */
    default void learnFurther(Frame df, int runs, String... targetVars) {
        learnFurther(df, Numeric.newFill(df.rowCount(), 1.0), runs, targetVars);
    }

    void learnFurther(Frame df, Var weights, int runs, String... targetVars);
}
