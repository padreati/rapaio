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

package rapaio.ml.regressor.linear;

import rapaio.data.Frame;
import rapaio.ml.regressor.RResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/1/14.
 */
public class RResultOLS extends RResult {

    public static RResultOLS newEmpty(Frame df, boolean withResiduals, String... targetVarNames) {
        return new RResultOLS(df, withResiduals, targetVarNames);
    }

    private RResultOLS(Frame df, boolean withResiduals, String... targetNames) {
        super(df, withResiduals, targetNames);
    }
}
