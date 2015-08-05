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

package rapaio.ml.eval;

import rapaio.printer.Printable;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;

import java.util.ArrayList;
import java.util.List;

import static rapaio.sys.WS.formatFlex;

/**
 * Regression evaluation tool which enables one to compute
 * sum of absolute differences between actual and prediction '
 * variables (in other words the total sum of the absolute
 * value of residuals).
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class MAE implements Printable {
    private final List<String> targetNames;
    private final List<Var> actualVars;
    private final List<Var> predictVars;
    private double value;

    public MAE(Frame actual, Frame predict, String... targetVarNames) {
        List<String> actualNames = new VarRange(targetVarNames).parseVarNames(actual);
        List<String> predictNames = new VarRange(targetVarNames).parseVarNames(predict);

        for (String varName : actualNames) {
            if (!predictNames.contains(varName)) {
                throw new IllegalArgumentException("actual and predict variables are not the same");
            }
        }
        for (String varName : predictNames) {
            if (!actualNames.contains(varName)) {
                throw new IllegalArgumentException("actual and predict variables are not the same");
            }
        }

        targetNames = actualNames;
        actualVars = new ArrayList<>();
        predictVars = new ArrayList<>();

        for (String targetName : targetNames) {
            actualVars.add(actual.getVar(targetName));
            predictVars.add(predict.getVar(targetName));
        }

        compute();
    }

    public MAE(Var actual, Var predict) {
        targetNames = new ArrayList<>();
        targetNames.add(actual.name());

        actualVars = new ArrayList<>();
        predictVars = new ArrayList<>();

        actualVars.add(actual);
        predictVars.add(predict);

        compute();
    }

    private void compute() {
        double total = 0;
        double count = 0;

        for (int i = 0; i < targetNames.size(); i++) {
            for (int j = 0; j < actualVars.get(i).rowCount(); j++) {
                count++;
                total += Math.abs(actualVars.get(i).value(j) - predictVars.get(i).value(j));
            }
        }
        value = total / count;
    }

    public double value() {
        return value;
    }

    @Override
    public String summary() {
        return "\n" +
                "> mean absolute error\n" +
                "MAE: " + formatFlex(value) + "\n";
    }
}
