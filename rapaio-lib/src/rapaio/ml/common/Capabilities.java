/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;

/**
 * Capabilities describes what a machine learning algorithm can predict and predict.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/1/14.
 */
public final class Capabilities {

    private int minInputCount = 0;
    private int maxInputCount = 0;
    private final List<VarType> inputTypes = new ArrayList<>();
    private boolean allowMissingInputValues = false;
    private int minTargetCount = 0;
    private int maxTargetCount = 0;
    private final List<VarType> targetTypes = new ArrayList<>();
    private boolean allowMissingTargetValues = false;

    public Capabilities() {
    }

    public Capabilities inputs(int min, int max, boolean allowMissing, VarType...types) {
        return minInputCount(min)
                .maxInputCount(max)
                .allowMissingInputValues(allowMissing)
                .inputTypes(types);
    }

    public Capabilities minInputCount(int minInputCount) {
        this.minInputCount = minInputCount;
        return this;
    }

    public int minInputCount() {
        return minInputCount;
    }

    public Capabilities maxInputCount(int maxInputCount) {
        this.maxInputCount = maxInputCount;
        return this;
    }

    public int maxInputCount() {
        return maxInputCount;
    }

    public Capabilities inputTypes(VarType...varTypes) {
        inputTypes.clear();
        inputTypes.addAll(Arrays.asList(varTypes));
        return this;
    }

    public VarType[] inputTypes() {
        return inputTypes.toArray(VarType[]::new);
    }

    public Capabilities allowMissingInputValues(boolean allow) {
        this.allowMissingInputValues = allow;
        return this;
    }

    public boolean allowMissingInputValues() {
        return allowMissingInputValues;
    }

    public Capabilities targets(int min, int max, boolean allowMissing, VarType...types) {
        return minTargetCount(min).maxTargetCount(max).allowMissingTargetValues(allowMissing).targetTypes(types);
    }

    public Capabilities minTargetCount(int minTargetCount) {
        this.minTargetCount = minTargetCount;
        return this;
    }

    public int minTargetCount() {
        return minTargetCount;
    }

    public Capabilities maxTargetCount(int maxTargetCount) {
        this.maxTargetCount = maxTargetCount;
        return this;
    }

    public int maxTargetCount() {
        return maxTargetCount;
    }

    public Capabilities targetTypes(VarType...varTypes) {
        targetTypes.clear();
        targetTypes.addAll(Arrays.asList(varTypes));
        return this;
    }

    public VarType[] targetTypes() {
        return targetTypes.toArray(VarType[]::new);
    }

    public Capabilities allowMissingTargetValues(boolean allow) {
        this.allowMissingTargetValues = allow;
        return this;
    }

    public boolean allowMissingTargetValues() {
        return allowMissingTargetValues;
    }

    /**
     * This method evaluates the capabilities of the algorithm at the learning phase.
     *
     * @param df         data frame to be learned
     * @param weights    weights of the data frame
     * @param targetVars target variable names
     */
    public void checkAtLearnPhase(Frame df, Var weights, String... targetVars) {

        // check target type
        checkInputCount(df, weights, targetVars);
        checkInputTypes(df, weights, targetVars);
        checkMissingInputValues(df, weights, targetVars);
        checkTargetCount(df, weights, targetVars);
        checkTargetTypes(df, weights, targetVars);
        checkMissingTargetValues(df, weights, targetVars);
    }

    private void checkInputCount(Frame df, Var weights, String... targetVars) {
        List<String> inputNames = VarRange.of(targetVars).parseComplementVarNames(df);
        int size = inputNames.size();
        if (size < minInputCount) {
            throw new IllegalArgumentException("Algorithm requires more than " + minInputCount + " input variables.");
        }
        if (size > maxInputCount) {
            throw new IllegalArgumentException("Algorithm does not allow more than " + maxInputCount + " input variables");
        }
    }

    private void checkTargetCount(Frame df, Var weights, String... targetVarNames) {
        List<String> varList = VarRange.of(targetVarNames).parseVarNames(df);
        int size = varList.size();
        if (size < minTargetCount) {
            throw new IllegalArgumentException("Algorithm requires more than " + minInputCount + " target variables.");
        }
        if (size > maxTargetCount) {
            throw new IllegalArgumentException("Algorithm does not allow more than " + maxInputCount + " target variables");
        }
    }

    private void checkTargetTypes(Frame df, Var weights, String... targetVarNames) {
        List<String> varList = VarRange.of(targetVarNames).parseVarNames(df);
        for (String varName : varList) {
            if (!targetTypes.contains(df.rvar(varName).type())) {
                throw new IllegalArgumentException("Algorithm does not allow " + df.rvar(varName).type().name() + " as target type for var: " + varName);
            }
        }
    }

    private void checkMissingTargetValues(Frame df, Var weights, String... targetVarNames) {
        if (allowMissingTargetValues)
            return;
        List<String> varList = VarRange.of(targetVarNames).parseVarNames(df);
        StringBuilder sb = new StringBuilder();
        for (String targetName : varList) {
            if (df.rvar(targetName).stream().complete().count() != df.rowCount()) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(targetName);
            }
        }
        if (sb.length() > 0)
            throw new IllegalArgumentException("Algorithm does not allow target variables with missing values; see : " + sb);
    }

    void checkInputTypes(Frame df, Var weights, String... targetVars) {
        List<String> inputNames = VarRange.of(targetVars).parseComplementVarNames(df);
        StringBuilder sb = new StringBuilder();
        for (String inputName : inputNames) {
            if (!inputTypes.contains(df.type(inputName))) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(inputName).append("[").append(df.type(inputName).name()).append("]");
            }
        }
        if (sb.length() > 0) {
            throw new IllegalArgumentException("Algorithm does not allow input variables of give types: " + sb);
        }
    }

    private void checkMissingInputValues(Frame df, Var weights, String... targetVarNames) {
        if (allowMissingInputValues)
            return;
        List<String> varList = VarRange.of(targetVarNames).parseComplementVarNames(df);
        StringBuilder sb = new StringBuilder();
        for (String inputName : varList) {
            if (df.rvar(inputName).stream().complete().count() != df.rvar(inputName).size()) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(inputName);
            }
        }
        if (sb.length() > 0)
            throw new IllegalArgumentException("Algorithm does not allow input variables with missing values; see : " + sb);
    }

    @Override
    public String toString() {
        return "types inputs/targets: " + inputTypes.stream().map(Enum::name).collect(joining(","))
                + "/" + targetTypes.stream().map(Enum::name).collect(joining(",")) + "\n"
                + "counts inputs/targets: [" + minInputCount + "," + maxInputCount + "] "
                + "/ [" + minTargetCount + "," + maxTargetCount + "]\n"
                + "missing inputs/targets: " + allowMissingInputValues
                + "/" + allowMissingTargetValues + "\n";
    }
}
