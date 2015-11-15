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
import rapaio.printer.Printable;

import java.util.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Capabilities describes what a machine learning algorithm can train and fit.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/1/14.
 */
public class Capabilities implements Printable {

    private LearnType learnType;
    private Integer minInputCount;
    private Integer maxInputCount;
    private List<VarType> inputTypes;
    private Boolean allowMissingInputValues;
    private Integer minTargetCount;
    private Integer maxTargetCount;
    private List<VarType> targetTypes;
    private Boolean allowMissingTargetValues;

    /**
     * Specifies the type of learning algorithm
     *
     * @param learnType implemented learning type
     * @return builder instance
     */
    public Capabilities withLearnType(LearnType learnType) {
        this.learnType = learnType;
        return this;
    }

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
        if (learnType == null) {
            throw new IllegalArgumentException("Capabilities not initialized completely: missing learnType");
        }
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
        checkLearnType(df, weights, targetVars);
        checkInputCount(df, weights, targetVars);
        checkInputTypes(df, weights, targetVars);
        checkMissingInputValues(df, weights, targetVars);
        checkTargetCount(df, weights, targetVars);
        checkTargetTypes(df, weights, targetVars);
        checkMissingTargetValues(df, weights, targetVars);
    }

    private void checkLearnType(Frame df, Var weights, String... targetVars) {
        List<String> varList = new VarRange(targetVars).parseVarNames(df);
        for (String varName : varList) {
            Var var = df.var(varName);
            VarType type = var.type();
            switch (learnType) {

                // classifier allow a single term in levels (other than missing labels)
                case UNARY_CLASSIFIER:
                    if (var.levels().length != 2) {
                        throw new IllegalArgumentException("The learning algorithm allows only unary nominal/ordinal targets");
                    }
                    break;

                // classifier which allows only 2 levels in levels (other than missing label)
                case BINARY_CLASSIFIER:
                    if (var.levels().length != 3) {
                        throw new IllegalArgumentException("The learning algorithm allows only binary nominal/ordinal targets");
                    }
                    break;

                // classifier which allows more than 2 levels in levels (other than missing label)
                case MULTICLASS_CLASSIFIER:
                    if (var.levels().length < 3) {
                        throw new IllegalArgumentException("The learning algorithm allows binary or multi-class targets");
                    }
                    break;
            }
        }
    }

    private void checkTargetCount(Frame df, Var weights, String... targetVarNames) {
        List<String> varList = new VarRange(targetVarNames).parseVarNames(df);
        int size = varList.size();
        if (size < minTargetCount) {
            throw new IllegalArgumentException("Algorithm requires more than " + minInputCount + " target variables.");
        }
        if (size > maxTargetCount) {
            throw new IllegalArgumentException("Algorithm does not allow more than " + maxInputCount + " target variables");
        }
    }

    private void checkTargetTypes(Frame df, Var weights, String... targetVarNames) {
        List<String> varList = new VarRange(targetVarNames).parseVarNames(df);
        for (String varName : varList) {
            if (!targetTypes.contains(df.var(varName).type())) {
                throw new IllegalArgumentException("Algorithm does not allow " + df.var(varName).type().name() + " as target type vor var: " + varName);
            }
        }
    }

    private void checkMissingTargetValues(Frame df, Var weights, String... targetVarNames) {
        if (allowMissingTargetValues)
            return;
        List<String> varList = new VarRange(targetVarNames).parseVarNames(df);
        StringBuilder sb = new StringBuilder();
        for (String targetName : varList) {
            if (df.var(targetName).stream().complete().count() != df.var(targetName).rowCount()) {
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
        List<String> inputNames = new VarRange(targetVars).parseInverseVarNames(df);
        int size = inputNames.size();
        if (size < minInputCount) {
            throw new IllegalArgumentException("Algorithm requires more than " + minInputCount + " input variables.");
        }
        if (size > maxInputCount) {
            throw new IllegalArgumentException("Algorithm does not allow more than " + maxInputCount + " input variables");
        }
    }

    void checkInputTypes(Frame df, Var weights, String... targetVars) {
        List<String> inputNames = new VarRange(targetVars).parseInverseVarNames(df);
        StringBuilder sb = new StringBuilder();
        for (String inputName : inputNames) {
            Var inputVar = df.var(inputName);
            if (!inputTypes.contains(inputVar.type())) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(inputName).append("[").append(inputVar.type().name()).append("]");
            }
        }
        if (sb.length() > 0) {
            throw new IllegalArgumentException("Algorithm does not allow input variables of give types: " + sb.toString());
        }
    }

    private void checkMissingInputValues(Frame df, Var weights, String... targetVarNames) {
        if (allowMissingInputValues)
            return;
        List<String> varList = new VarRange(targetVarNames).parseInverseVarNames(df);
        StringBuilder sb = new StringBuilder();
        for (String inputName : varList) {
            if (df.var(inputName).stream().complete().count() != df.var(inputName).rowCount()) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(inputName);
            }
        }
        if (sb.length() > 0)
            throw new IllegalArgumentException("Algorithm does not allow input variables with missing values; see : " + sb.toString());
    }

    public enum LearnType {
        UNARY_CLASSIFIER,
        BINARY_CLASSIFIER,
        MULTICLASS_CLASSIFIER,
        REGRESSION
    }

    public LearnType getLearnType() {
        return learnType;
    }

    public Integer getMinInputCount() {
        return minInputCount;
    }

    public Integer getMaxInputCount() {
        return maxInputCount;
    }

    public List<VarType> getInputTypes() {
        return inputTypes;
    }

    public Boolean getAllowMissingInputValues() {
        return allowMissingInputValues;
    }

    public Integer getMinTargetCount() {
        return minTargetCount;
    }

    public Integer getMaxTargetCount() {
        return maxTargetCount;
    }

    public List<VarType> getTargetTypes() {
        return targetTypes;
    }

    public Boolean getAllowMissingTargetValues() {
        return allowMissingTargetValues;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("learning: ").append(learnType.name()).append("\n");
        sb.append("types inputs/targets: ").append(inputTypes.stream().map(Enum::name).collect(joining(","))).append("/").append(targetTypes.stream().map(Enum::name).collect(joining(","))).append("\n");
        sb.append("counts inputs/targets: [").append(minInputCount).append(",").append(maxInputCount).append("] / [")
                .append(minTargetCount).append(",").append(maxTargetCount).append("]\n");
        sb.append("missing inputs/targets: ").append(allowMissingInputValues).append("/").append(allowMissingTargetValues).append("\n");
        return sb.toString();
    }

}
