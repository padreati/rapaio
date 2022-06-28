/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.preprocessing;

import java.util.List;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.util.Pair;

/**
 * Creates a nominal variable based on a source variable and a list of regular expressions with corresponding output values.
 * <p>
 * The match list contains pairs of lists of regular expressions and corresponding output value.
 * For each label value of the source variable all the pairs are iterated in order.
 * If the label value match any  regular expression from the pair list, the output value from the pair becomes
 * the value in the target variable. If none of the pairs matches the source value than
 * the target variable will set a missing value.
 * <p>
 * If more than one regular expressions match the source label value, the first match provides the output value.
 * <p>
 * The filter will fail if there is already a variable in the fitted frame with the same name as target variable.
 * It will also fail if the fitted frame does not contain a variable with source variable name.
 */
public class AddNominalStringMatch extends AbstractTransform {

    @SafeVarargs
    public static AddNominalStringMatch filter(String sourceVarName, String targetVarName, boolean useBreaks,
            Pair<String, List<String>>... matchList) {
        return new AddNominalStringMatch(matchList, useBreaks, targetVarName, VarRange.of(sourceVarName));
    }

    private final String newVarName;
    private final Pair<String, List<String>>[] matchList;
    private final boolean useBreaks;

    private AddNominalStringMatch(Pair<String, List<String>>[] matchList, boolean useBreaks, String newVarName, VarRange varRange) {
        super(varRange);
        this.newVarName = newVarName;
        this.matchList = matchList;
        this.useBreaks = useBreaks;
    }

    @Override
    protected void coreFit(Frame df) {
        // nothing to fit
    }

    private String match(String value) {
        for (var entry : matchList) {
            for (String regex : entry.v2) {
                String expression = useBreaks ? "\\b" + regex + "\\b" : regex;
                if (value.matches(expression)) {
                    return entry.v1;
                }

            }
        }
        return VarNominal.MISSING_VALUE;
    }

    @Override
    public Frame apply(Frame df) {

        // validation
        if (varNames != null && varNames.length != 1) {
            throw new IllegalArgumentException(
                    "Cannot fit data since there are more than one matched variable.");
        }
        if (df.rvar(newVarName) != null) {
            throw new IllegalArgumentException("Frame contains already a variable with name: %s".formatted(newVarName));
        }

        Var v = df.rvar(varNames[0]);
        if (v == null) {
            throw new IllegalArgumentException("Frame does not contain variable with name: %s".formatted(varNames[0]));
        }
        Var newVar = VarNominal.from(df.rowCount(), row -> match(df.getLabel(row, varNames[0]))).name(newVarName);
        return df.bindVars(newVar);
    }

    @Override
    public AddNominalStringMatch newInstance() {
        return new AddNominalStringMatch(matchList, useBreaks, newVarName, varRange);
    }
}
