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

import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

/**
 * Regression evaluation tool which enables one to compute
 * Root Mean Squared Error, which is the sum of the squared
 * values of the residuals for all pairs of actual and
 * prediction variables.
 * <p>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RMSE implements Printable {

    public static RMSE from(Var actual, Var fit) {
        return new RMSE(SolidFrame.byVars(actual.solidCopy()),
                SolidFrame.byVars(fit.solidCopy()));
    }

    public static RMSE from(Frame actual, Frame fit) {
        return new RMSE(actual, fit);
    }

    private final Frame actual;
    private final Frame fit;

    // artifacts

    private VarDouble rmse = VarDouble.empty().withName("rmse");
    private VarDouble mse = VarDouble.empty().withName("mse");

    private double totalRmse;
    private double totalMse;

    private RMSE(Frame actual, Frame fit) {
        this.actual = actual;
        this.fit = fit;
        if (actual.varCount()!=fit.varCount()) {
            throw new IllegalArgumentException("actual and predict frames have not the same size.");
        }
        compute();
    }

    private void compute() {
        double totalSum = 0;
        double totalCount = 0;

        for (int i = 0; i < actual.varCount(); i++) {
            double sum = 0.0;
            double count = 0.0;
            for (int j = 0; j < actual.rowCount(); j++) {
                count++;
                sum += Math.pow(actual.getDouble(j, i) - fit.getDouble(j, i), 2);
            }
            rmse.addDouble(Math.sqrt(sum / count));
            mse.addDouble(sum / count);

            totalSum += sum;
            totalCount += count;
        }
        totalRmse = Math.sqrt(totalSum / totalCount);
        totalMse = totalSum / totalCount;
    }

    public VarDouble rmse() {
        return rmse;
    }

    public VarDouble mse() {
        return mse;
    }

    public double totalRmse() {
        return totalRmse;
    }

    public double totalMse() {
        return totalMse;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("> Root Mean Squared Error (RMSE):\n");
        sb.append("\n");

        TextTable tt = TextTable.newEmpty(actual.varCount() + 1, 3)
                .withHeaderRows(1)
                .withHeaderCols(1);

        tt.set(0, 0, "target", 0);
        tt.set(0, 1, "rmse", 0);
        tt.set(0, 2, "mse", 0);

        for (int i = 0; i < actual.varCount(); i++) {
            tt.set(i + 1, 0, actual.varNames()[i] + " | " + fit.varNames()[i], 1);
            tt.set(i + 1, 1, WS.formatFlex(rmse.getDouble(i)), 1);
            tt.set(i + 1, 2, WS.formatFlex(mse.getDouble(i)), 1);
        }
        sb.append(tt.summary());
        sb.append("\n");

        sb.append("Total RMSE: ").append(WS.formatFlex(totalRmse)).append("\n");
        sb.append("Total MSE: ").append(WS.formatFlex(totalMse)).append("\n");
        sb.append("\n");

        return sb.toString();
    }
}
