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

package rapaio.ml.common;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;

import java.util.HashSet;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/1/14.
 */
public class Capabilities {

    private TargetCount targetCount;
    private HashSet<VarType> allowedTargetTypes = new HashSet<>();

    public Capabilities withTargetCount(TargetCount targetCount) {
        this.targetCount = targetCount;
        return this;
    }

    private void validate() {
        if (targetCount == null) {
            throw new IllegalArgumentException("Capabilities not initialized: target count not specified.");
        }
    }

    public void check(Frame df, Var weights, String... targetVarNames) {

        // check if capabilities are well-specified

        validate();

        // check target type

        checkTargetType(df, weights, targetVarNames);
    }

    private void checkTargetType(Frame df, Var weights, String... targetVarNames) {
        List<String> varList = new VarRange(targetVarNames).parseVarNames(df);
        switch (targetCount) {
            case NO_TARGET:
                if (!varList.isEmpty()) {
                    throw new IllegalArgumentException("Algorithm does not allow specification of target variables.");
                }
                break;
            case SINGLE_TARGET:
                if (varList.size() != 1) {
                    throw new IllegalArgumentException("Algorithm requires specification of a single target variable.");
                }
                break;
            case MULTIPLE_TARGET:
                if (varList.size() < 1) {
                    throw new IllegalArgumentException("Algorithm requires specification of at least one target variable.");
                }
                break;
            default:
                throw new RuntimeException("This should not ever happen");
        }
    }

    public static enum TargetCount {
        NO_TARGET,
        SINGLE_TARGET,
        MULTIPLE_TARGET
    }
}
