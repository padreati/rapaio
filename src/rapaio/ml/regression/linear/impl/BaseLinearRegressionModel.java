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

package rapaio.ml.regression.linear.impl;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import rapaio.data.Frame;
import rapaio.data.VarType;
import rapaio.data.filter.FIntercept;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.regression.DefaultHookInfo;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.linear.LinearRegressionResult;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/26/20.
 */
@SuppressWarnings("unchecked")
public abstract class BaseLinearRegressionModel<M extends BaseLinearRegressionModel<M>>
        extends RegressionModel<M, LinearRegressionResult, DefaultHookInfo> {

    @Serial
    private static final long serialVersionUID = -3722395862627404126L;

    public final ValueParam<Boolean, M> intercept = new ValueParam<>((M) this, true,
            "intercept",
            "Configures the model to add an intercept term or not",
            Objects::nonNull);

    protected DMatrix beta;

    public DVector firstCoefficients() {
        return beta.map(0, 1);
    }

    public DVector getCoefficients(int targetIndex) {
        return beta.map(targetIndex, 1);
    }

    public DMatrix getAllCoefficients() {
        return beta;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(
                1, 1_000_000,
                Arrays.asList(VarType.DOUBLE, VarType.INT, VarType.BINARY), false,
                1, 1_000_000, List.of(VarType.DOUBLE), false);
    }

    @Override
    protected PredSetup preparePredict(Frame df, boolean withResiduals, final double[] quantiles) {
        Frame transformed = intercept.get() ? FIntercept.filter().apply(df) : df;
        return super.preparePredict(transformed, withResiduals, quantiles);
    }

    @Override
    protected LinearRegressionResult corePredict(Frame df, boolean withResiduals, final double[] quantiles) {
        LinearRegressionResult result = new LinearRegressionResult(this, df, withResiduals, quantiles);
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            for (int j = 0; j < result.prediction(target).size(); j++) {
                double fit = 0.0;
                for (int k = 0; k < inputNames().length; k++) {
                    String inputName = inputNames[k];
                    if (FIntercept.INTERCEPT.equals(inputName)) {
                        fit += beta.get(k, i);
                    } else {
                        fit += beta.get(k, i) * df.getDouble(j, inputName);
                    }
                }
                result.prediction(target).setDouble(j, fit);
            }
        }
        result.buildComplete();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName());
        if (!isFitted()) {
            sb.append(", not fitted.");
        } else {
            sb.append(", fitted on: ")
                    .append(inputNames.length).append(" IVs [").append(joinMax(inputNames)).append("], ")
                    .append(targetNames.length).append(" DVs [").append(joinMax(targetNames)).append("].");
        }
        return sb.toString();
    }

    private String joinMax(String[] tokens) {
        int len = Math.min(tokens.length, 5);
        String[] firstTokens = new String[len];
        System.arraycopy(tokens, 0, firstTokens, 0, len);
        return String.join(",", firstTokens);
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (!hasLearned) {
            return sb.toString();
        }

        for (int i = 0; i < targetNames.length; i++) {
            String targetName = targetNames[i];
            sb.append("Target <<< ").append(targetName).append(" >>>\n\n");
            sb.append("> Coefficients: \n");
            DVector coeff = beta.map(i, 1);

            TextTable tt = TextTable.empty(coeff.size() + 1, 2, 1, 0);
            tt.textCenter(0, 0, "Name");
            tt.textCenter(0, 1, "Estimate");
            for (int j = 0; j < coeff.size(); j++) {
                tt.textLeft(j + 1, 0, inputNames[j]);
                tt.floatMedium(j + 1, 1, coeff.get(j));
            }
            sb.append(tt.getDynamicText(printer, options));
            sb.append("\n");
        }
        return sb.toString();
    }
}
