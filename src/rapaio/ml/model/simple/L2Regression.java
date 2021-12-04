/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.simple;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;

import rapaio.core.stat.Mean;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ParametricEquals;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class L2Regression extends RegressionModel<L2Regression, RegressionResult, RunInfo<L2Regression>>
        implements ParametricEquals<L2Regression> {

    @Serial
    private static final long serialVersionUID = -8666168876139028337L;

    public static L2Regression newModel() {
        return new L2Regression();
    }

    private double[] means;

    private L2Regression() {
    }

    @Override
    public L2Regression newInstance() {
        return new L2Regression();
    }

    @Override
    public String name() {
        return "L2Regression";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(
                0, 1_000_000,
                Arrays.asList(VarType.DOUBLE, VarType.BINARY, VarType.INT, VarType.NOMINAL, VarType.LONG, VarType.STRING), true,
                1, 1_000_000, List.of(VarType.DOUBLE), true);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        means = new double[targetNames().length];
        for (int i = 0; i < targetNames().length; i++) {
            double mean = Mean.of(df.rvar(targetName(i))).value();
            means[i] = mean;
        }
        return true;
    }

    @Override
    protected RegressionResult corePredict(final Frame df, final boolean withResiduals, final double[] quantiles) {
        RegressionResult fit = RegressionResult.build(this, df, withResiduals, quantiles);
        for (int i = 0; i < targetNames().length; i++) {
            double mean = means[i];
            Var v = fit.prediction(targetName(i));
            v.stream().forEach(s -> s.setDouble(mean));
        }
        fit.buildComplete();
        return fit;
    }

    public double[] getMeans() {
        return means;
    }

    @Override
    public String toString() {
        if (isFitted()) {
            StringBuilder sb = new StringBuilder();
            sb.append(fullName());
            sb.append("; fitted values={");
            for (int i = 0; i < Math.min(5, targetNames.length); i++) {
                sb.append(targetName(i)).append(":").append(Format.floatFlex(means[i]));
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
    public String toContent(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (isFitted()) {
            sb.append("Fitted values:\n");
            sb.append("\n");

            Var target = VarNominal.empty().name("Target");
            Var median = VarDouble.empty().name("Fitted value");
            for (int i = 0; i < means.length; i++) {
                target.addLabel(targetName(i));
                median.addDouble(means[i]);
            }
            sb.append(SolidFrame.byVars(target, median).toContent(printer, options));
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (isFitted()) {
            sb.append("Fitted values:\n");
            sb.append("\n");

            Var target = VarNominal.empty().name("Target");
            Var median = VarDouble.empty().name("Fitted value");
            for (int i = 0; i < means.length; i++) {
                target.addLabel(targetName(i));
                median.addDouble(means[i]);
            }
            sb.append(SolidFrame.byVars(target, median).toFullContent(printer, options));
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        return toContent(printer, options);
    }

    @Override
    public boolean equalOnParams(L2Regression object) {
        return true;
    }
}
