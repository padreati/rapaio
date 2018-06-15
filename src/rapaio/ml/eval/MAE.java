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

package rapaio.ml.eval;

import rapaio.data.BoundFrame;
import rapaio.printer.Printable;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VRange;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

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
public class MAE implements Printable {

    public static MAE from(Var actual, Var fit) {
        return new MAE(BoundFrame.byVars(actual), BoundFrame.byVars(fit));
    }

    public static MAE from(Frame actual, Frame fit) {
        return new MAE(actual, fit);
    }

    private final Frame actual;
    private final Frame fit;

    private final double[] mae;
    private final double totalMae;

    private MAE(Frame actual, Frame fit, String... targetVarNames) {
        if (actual.varCount() != fit.varCount()) {
            throw new IllegalArgumentException("Number of actual and predict variables is not the same");
        }

        this.actual = actual;
        this.fit = fit;

        mae = new double[actual.varCount()];
        double totalSum = 0;
        int totalCount = 0;
        for (int i = 0; i < actual.varCount(); i++) {
            double sum = 0;
            double count = 0;
            for (int j = 0; j < actual.rowCount(); j++) {
                count++;
                sum += Math.abs(actual.value(j, i) - fit.value(j, i));
            }
            mae[i] = sum / count;

            totalSum += sum;
            totalCount += count;
        }
        totalMae = totalSum / totalCount;
    }

    public double[] mae() {
        return mae;
    }

    public double mae(int i) {
        return mae[i];
    }

    public double totalMae() {
        return totalMae;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("> MAE (Mean Absolute Error):\n");
        sb.append("\n");

        TextTable tt = TextTable.newEmpty(actual.varCount() + 1, 2).withHeaderRows(1).withHeaderCols(1);

        tt.set(0, 0, "names", 1);
        tt.set(0, 1, "mae", 1);

        for (int i = 0; i < actual.varCount(); i++) {
            tt.set(i + 1, 0, actual.varName(i) + " | " + fit.varName(i), 1);
            tt.set(i + 1, 1, WS.formatFlex(mae[i]), 1);
        }
        sb.append(tt.summary());
        sb.append("\n");
        sb.append("Total mae: " + WS.formatFlex(totalMae) + "\n");
        sb.append("\n");

        return sb.toString();
    }
}
