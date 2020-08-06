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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.ml.regression.simple;

import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.ml.common.Capabilities;
import rapaio.ml.param.ValueParam;
import rapaio.ml.regression.AbstractRegressionModel;
import rapaio.ml.regression.RegressionResult;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

import java.util.Arrays;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class ConstantRegressionModel extends AbstractRegressionModel<ConstantRegressionModel, RegressionResult> {

    private static final long serialVersionUID = -2537862585258148528L;

    public static ConstantRegressionModel with(double c) {
        return new ConstantRegressionModel().constant.set(c);
    }

    public final ValueParam<Double, ConstantRegressionModel> constant = new ValueParam<>(this, 0.0,
            "constant",
            "Constant value used for prediction.",
            x -> true);

    @Override
    public ConstantRegressionModel newInstance() {
        ConstantRegressionModel model = new ConstantRegressionModel();
        model.copyParameterValues(this);
        return model;
    }

    @Override
    public String name() {
        return "ConstantRegression";
    }

    @Override
    public Capabilities capabilities() {
        return Capabilities.builder()
                .minInputCount(0)
                .maxInputCount(1_000_000)
                .minTargetCount(1)
                .maxTargetCount(1)
                .inputTypes(Arrays.asList(VType.DOUBLE, VType.BINARY, VType.INT, VType.NOMINAL, VType.LONG, VType.STRING))
                .targetType(VType.DOUBLE)
                .allowMissingInputValues(true)
                .allowMissingTargetValues(true)
                .build();
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        return true;
    }

    @Override
    protected RegressionResult corePredict(final Frame df, final boolean withResiduals) {
        RegressionResult fit = RegressionResult.build(this, df, withResiduals);
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
