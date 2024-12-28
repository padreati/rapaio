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

package rapaio.ml.model.simple;

import java.io.Serial;

import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Simple regression which predicts with the median value of the target columns.
 * <p>
 * This simple regression is used alone for simple prediction or as a
 * starting point for other more complex regression algorithms.
 * <p>
 * This regression implements the regression by a constant paradigm using
 * sum of absolute deviations loss function: L1(y - y_hat) = \sum(|y - y_hat|).
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class L1Regression extends RegressionModel<L1Regression, RegressionResult, RunInfo<L1Regression>> {

    @Serial
    private static final long serialVersionUID = 6125284399953219419L;

    public static L1Regression newL1() {
        return new L1Regression();
    }

    private double[] medians;

    private L1Regression() {
    }

    @Override
    public L1Regression newInstance() {
        return new L1Regression();
    }

    @Override
    public String name() {
        return "L1Regression";
    }

    public double[] getMedians() {
        return medians;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(0, 1_000_000, true, VarType.DOUBLE, VarType.BINARY, VarType.INT, VarType.NOMINAL, VarType.LONG, VarType.STRING)
                .targets(1, 1_000_000, true, VarType.DOUBLE);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        medians = new double[targetNames().length];
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            medians[i] = Quantiles.of(df.rvar(target), 0.5).values()[0];
        }
        return true;
    }

    @Override
    public RegressionResult corePredict(final Frame df, final boolean withResiduals, final double[] quantiles) {
        RegressionResult pred = RegressionResult.build(this, df, withResiduals, quantiles);
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            double median = medians[i];
            pred.prediction(target).stream().forEach(s -> s.setDouble(median));
        }
        pred.buildComplete();
        return pred;
    }

    @Override
    public String toString() {
        if (isFitted()) {
            StringBuilder sb = new StringBuilder();
            sb.append(fullName());
            sb.append("; fitted values={");
            for (int i = 0; i < Math.min(5, targetNames.length); i++) {
                sb.append(targetName(i)).append(":").append(Format.floatFlex(medians[i]));
                if (i < targetNames.length - 1) {
                    sb.append(",");
                }
            }
            if (targetNames.length > 5) {
                sb.append("...");
            }
            sb.append("}");
            return sb.toString();
        }
        return fullName() + "; not fitted";
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (isFitted()) {
            sb.append("Fitted values:\n");
            sb.append("\n");

            Var target = VarNominal.empty().name("Target");
            Var median = VarDouble.empty().name("Fitted value");
            for (int i = 0; i < medians.length; i++) {
                target.addLabel(targetName(i));
                median.addDouble(medians[i]);
            }
            sb.append(SolidFrame.byVars(target, median).toContent(printer, options));
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (isFitted()) {
            sb.append("Fitted values:\n");
            sb.append("\n");

            Var target = VarNominal.empty().name("Target");
            Var median = VarDouble.empty().name("Fitted value");
            for (int i = 0; i < medians.length; i++) {
                target.addLabel(targetName(i));
                median.addDouble(medians[i]);
            }
            sb.append(SolidFrame.byVars(target, median).toFullContent(printer, options));
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        return toContent(printer, options);
    }
}
