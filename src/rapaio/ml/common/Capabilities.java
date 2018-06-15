/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.printer.Printable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.*;

/**
 * Capabilities describes what a machine learning algorithm can predict and predict.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/1/14.
 */
public class Capabilities implements Printable {

    private Integer minInputCount;
    private Integer maxInputCount;
    private List<VarType> inputTypes;
    private Boolean allowMissingInputValues;
    private Integer minTargetCount;
    private Integer maxTargetCount;
    private List<VarType> targetTypes;
    private Boolean allowMissingTargetValues;

    public Capabilities withInputCount(int minInputCount, int maxInputCount) {
        this.minInputCount = minInputCount;
        this.maxInputCount = maxInputCount;
        return this;
    }

    public Capabilities withInputTypes(VarType... types) {
        this.inputTypes = Arrays.stream(types).collect(toList());
        Collections.sort(this.inputTypes);
        return this;
    }

    public Capabilities withTargetCount(int minTargetCount, int maxTargetCount) {
        this.minTargetCount = minTargetCount;
        this.maxTargetCount = maxTargetCount;
        return this;
    }

    public Capabilities withTargetTypes(VarType... types) {
        this.targetTypes = Arrays.stream(types).collect(toList());
        Collections.sort(this.inputTypes);
        return this;
    }

    public Capabilities withAllowMissingInputValues(boolean allow) {
        this.allowMissingInputValues = allow;
        return this;
    }

    public Capabilities withAllowMissingTargetValues(boolean allow) {
        this.allowMissingTargetValues = allow;
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
        if (inputTypes == null) {
            throw new IllegalArgumentException("Capabilities not initialized completely: missing inputTypes");
        }
        if (minInputCount == null) {
            throw new IllegalArgumentException("Capabilities not initialized completely: missing minInputCount");
        }
        if (maxInputCount == null) {
            throw new IllegalArgumentException("Capabilities not initialized completely: missing maxInputCount");
        }
        if (allowMissingInputValues == null) {
            throw new IllegalArgumentException("Capabilities not initialized completely: missing allowMissingInputValues");
        }
        if (targetTypes == null) {
            throw new IllegalArgumentException("Capabilities not initialized completely: miaaing targetTypes");
        }
        if (minTargetCount == null) {
            throw new IllegalArgumentException("Capabilities not initialized completely: missing minTargetCount");
        }
        if (maxTargetCount == null) {
            throw new IllegalArgumentException("Capabilities not initialized completely: missing maxTargetCount");
        }
        if (allowMissingTargetValues == null) {
            throw new IllegalArgumentException("Capabilities not initialized completely: missing allowMissingTargetValues");
        }

        // check target type
        checkInputCount(df, weights, targetVars);
        checkInputTypes(df, weights, targetVars);
        checkMissingInputValues(df, weights, targetVars);
        checkTargetCount(df, weights, targetVars);
        checkTargetTypes(df, weights, targetVars);
        checkMissingTargetValues(df, weights, targetVars);
    }

    private void checkTargetCount(Frame df, Var weights, String... targetVarNames) {
        List<String> varList = VRange.of(targetVarNames).parseVarNames(df);
        int size = varList.size();
        if (size < minTargetCount) {
            throw new IllegalArgumentException("Algorithm requires more than " + minInputCount + " target variables.");
        }
        if (size > maxTargetCount) {
            throw new IllegalArgumentException("Algorithm does not allow more than " + maxInputCount + " target variables");
        }
    }

    private void checkTargetTypes(Frame df, Var weights, String... targetVarNames) {
        List<String> varList = VRange.of(targetVarNames).parseVarNames(df);
        for (String varName : varList) {
            if (!targetTypes.contains(df.rvar(varName).type())) {
                throw new IllegalArgumentException("Algorithm does not allow " + df.rvar(varName).type().name() + " as target type vor var: " + varName);
            }
        }
    }

    private void checkMissingTargetValues(Frame df, Var weights, String... targetVarNames) {
        if (allowMissingTargetValues)
            return;
        List<String> varList = VRange.of(targetVarNames).parseVarNames(df);
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
            throw new IllegalArgumentException("Algorithm does not allow target variables with missing values; see : " + sb.toString());
    }

    private void checkInputCount(Frame df, Var weights, String... targetVars) {
        List<String> inputNames = VRange.of(targetVars).parseInverseVarNames(df);
        int size = inputNames.size();
        if (size < minInputCount) {
            throw new IllegalArgumentException("Algorithm requires more than " + minInputCount + " input variables.");
        }
        if (size > maxInputCount) {
            throw new IllegalArgumentException("Algorithm does not allow more than " + maxInputCount + " input variables");
        }
    }

    void checkInputTypes(Frame df, Var weights, String... targetVars) {
        List<String> inputNames = VRange.of(targetVars).parseInverseVarNames(df);
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
            throw new IllegalArgumentException("Algorithm does not allow input variables of give types: " + sb.toString());
        }
    }

    private void checkMissingInputValues(Frame df, Var weights, String... targetVarNames) {
        if (allowMissingInputValues)
            return;
        List<String> varList = VRange.of(targetVarNames).parseInverseVarNames(df);
        StringBuilder sb = new StringBuilder();
        for (String inputName : varList) {
            if (df.rvar(inputName).stream().complete().count() != df.rvar(inputName).rowCount()) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(inputName);
            }
        }
        if (sb.length() > 0)
            throw new IllegalArgumentException("Algorithm does not allow input variables with missing values; see : " + sb.toString());
    }

    public Integer minInputCount() {
        return minInputCount;
    }

    public Integer maxInputCount() {
        return maxInputCount;
    }

    public List<VarType> inputTypes() {
        return inputTypes;
    }

    public Boolean allowMissingInputValues() {
        return allowMissingInputValues;
    }

    public Integer minTargetCount() {
        return minTargetCount;
    }

    public Integer maxTargetCount() {
        return maxTargetCount;
    }

    public List<VarType> targetTypes() {
        return targetTypes;
    }

    public Boolean allowMissingTargetValues() {
        return allowMissingTargetValues;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("types inputs/targets: ").append(inputTypes.stream().map(Enum::name).collect(joining(","))).append("/").append(targetTypes.stream().map(Enum::name).collect(joining(","))).append("\n");
        sb.append("counts inputs/targets: [").append(minInputCount).append(",").append(maxInputCount).append("] / [")
                .append(minTargetCount).append(",").append(maxTargetCount).append("]\n");
        sb.append("missing inputs/targets: ").append(allowMissingInputValues).append("/").append(allowMissingTargetValues).append("\n");
        return sb.toString();
    }

}
