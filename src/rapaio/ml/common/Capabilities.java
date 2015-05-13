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
 *
 */

package rapaio.ml.common;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;

import java.util.HashSet;
import java.util.List;

/**
 * Capabilities describes what a machine learning algorithm can learn and fit.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/1/14.
 */
public class Capabilities {

    private TargetCount targetCount;
    private TargetType targetType;
    private HashSet<VarType> allowedTargetTypes = new HashSet<>();

    public Capabilities withTargetCount(TargetCount targetCount) {
        this.targetCount = targetCount;
        return this;
    }

    public Capabilities withTargetType(TargetType targetType) {
        this.targetType = targetType;
        return this;
    }

    /**
     * This method evaluates the capabilities of the algorithm at the learning phase.
     *
     * @param df         data frame to be learned
     * @param weights    weights of the data frame
     * @param targetVars target variable names
     */
    public void checkAtLearnPhase(Frame df, Var weights, String... targetVars) {

        // check if capabilities are well-specified
        if (targetCount == null) {
            throw new IllegalArgumentException("Capabilities not initialized: target count not specified.");
        }

        // check target type
        checkTargetType(df, weights, targetVars);
        checkTargetTypes(df, weights, targetVars);
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
            case MULTIPLE_TARGETS:
                if (varList.size() < 1) {
                    throw new IllegalArgumentException("Algorithm requires specification of at least one target variable.");
                }
                break;
        }
    }

    private void checkTargetTypes(Frame df, Var weights, String... targetVars) {
        List<String> varList = new VarRange(targetVars).parseVarNames(df);
        for (String varName : varList) {
            Var var = df.var(varName);
            VarType type = var.type();
            switch (targetType) {

                // classifier allow a single term in dictionary (other than missing labels)
                case UNARY_CLASSIFIER:
                    if (!(type.equals(VarType.NOMINAL) || type.equals(VarType.ORDINAL)))
                        throw new IllegalArgumentException("The learning algorithm allows only nominal or ordinal variables");
                    if (var.dictionary().length != 2) {
                        throw new IllegalArgumentException("The learning algorithm allows only unary nominal/ordinal targets");
                    }
                    break;

                // classifier which allows only 2 terms in dictionary (other than missing label)
                case BINARY_CLASSIFIER:
                    if (!(type.equals(VarType.NOMINAL) || type.equals(VarType.ORDINAL)))
                        throw new IllegalArgumentException("The learning algorithm allows only nominal or ordinal variables");
                    if (var.dictionary().length != 3) {
                        throw new IllegalArgumentException("The learning algorithm allows only binary nominal/ordinal targets");
                    }
                    break;

                // classifier which allows more than 2 terms in dictionary (other than missing label)
                case MULTICLASS_CLASSIFIER:
                    if (!(type.equals(VarType.NOMINAL) || type.equals(VarType.ORDINAL)))
                        throw new IllegalArgumentException("The learning algorithm allows only nominal or ordinal variables");
                    if (var.dictionary().length < 3) {
                        throw new IllegalArgumentException("The learning algorithm allows binary or multi-class targets");
                    }
                    break;
            }
        }
    }

    public enum TargetCount {
        NO_TARGET,
        SINGLE_TARGET,
        MULTIPLE_TARGETS
    }

    public enum TargetType {
        UNARY_CLASSIFIER,
        BINARY_CLASSIFIER,
        MULTICLASS_CLASSIFIER
    }
}
