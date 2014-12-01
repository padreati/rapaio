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

import rapaio.ml.regressor.RPrediction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/1/14.
 */
public class RPredictionOLS extends RPrediction {

    public static RPredictionOLS newEmpty(int rows, boolean withResiduals, String... targetNames) {
        return new RPredictionOLS(rows, withResiduals, targetNames);
    }

    private RPredictionOLS(int rows, boolean withResiduals, String... targetNames) {
        super(rows, withResiduals);
        for (String targetName : targetNames) {
            addTarget(targetName);
        }
    }
}
