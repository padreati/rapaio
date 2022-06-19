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

package rapaio.ml.model.simple;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class ConstantRegression extends RegressionModel<ConstantRegression, RegressionResult, RunInfo<ConstantRegression>> {

    @Serial
    private static final long serialVersionUID = -2537862585258148528L;

    public static ConstantRegression with(double c) {
        return new ConstantRegression().constant.set(c);
    }

    /**
     * Constant value used for prediction.
     */
    public final ValueParam<Double, ConstantRegression> constant = new ValueParam<>(this, 0.0, "constant", x -> true);

    @Override
    public ConstantRegression newInstance() {
        return new ConstantRegression().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "ConstantRegression";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(0, 1_000_000, true, VarType.DOUBLE, VarType.BINARY, VarType.INT, VarType.NOMINAL, VarType.LONG, VarType.STRING)
                .targets(1, 1, true, VarType.DOUBLE);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        return true;
    }

    @Override
    protected RegressionResult corePredict(final Frame df, final boolean withResiduals, final double[] quantiles) {
        RegressionResult fit = RegressionResult.build(this, df, withResiduals, quantiles);
        for (String targetName : targetNames) {
            fit.prediction(targetName).stream().forEach(s -> s.setDouble(constant.get()));
        }
        fit.buildComplete();
        return fit;
    }

    @Override
    public String toString() {
        return fullName();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return fullName();
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return fullName();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (isFitted()) {
            sb.append("Fitted values:\n");
            sb.append("\n");

            TextTable tt = TextTable.empty(1 + targetNames.length, 2);
            tt.textRight(0, 0, "Target");
            tt.textRight(0, 1, "Estimate");

            for (int i = 0; i < targetNames().length; i++) {
                tt.textRight(1 + i, 0, targetName(i));
                tt.floatFlex(1 + i, 1, constant.get());
            }
            sb.append(tt.getDynamicText(printer, options));
        }
        sb.append("\n");
        return sb.toString();
    }
}
