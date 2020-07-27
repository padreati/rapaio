package rapaio.ml.regression.linear.impl;

import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.filter.FIntercept;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegressionModel;
import rapaio.ml.regression.linear.LinearRegressionResult;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

import java.util.Arrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/26/20.
 */
public abstract class BaseLinearRegressionModel<M extends BaseLinearRegressionModel<M>>
        extends AbstractRegressionModel<M, LinearRegressionResult> {

    private static final long serialVersionUID = -3722395862627404126L;

    protected boolean intercept = true;
    protected DMatrix beta;


    @Override
    public M newInstanceDecoration(M regression) {
        return super.newInstanceDecoration(regression)
                .withIntercept(intercept);
    }

    /**
     * @return true if the linear model add an intercept if doesn't exists
     */
    public boolean hasIntercept() {
        return intercept;
    }

    /**
     * Configure the model to introduce an intercept or not.
     *
     * @param intercept if true an intercept variable will be generated, false otherwise
     * @return linear model instance
     */
    public M withIntercept(boolean intercept) {
        this.intercept = intercept;
        return (M)this;
    }

    public DVector firstCoefficients() {
        return beta.mapCol(0);
    }

    public DVector getCoefficients(int targetIndex) {
        return beta.mapCol(targetIndex);
    }

    public DMatrix getAllCoefficients() {
        return beta;
    }

    @Override
    public Capabilities capabilities() {
        return Capabilities.builder()
                .inputTypes(Arrays.asList(VType.DOUBLE, VType.INT, VType.BINARY))
                .targetType(VType.DOUBLE)
                .minInputCount(1).maxInputCount(1_000_000)
                .minTargetCount(1).maxTargetCount(1_000_000)
                .allowMissingInputValues(false)
                .allowMissingTargetValues(false)
                .build();
    }

    @Override
    protected PredSetup preparePredict(Frame df, boolean withResiduals) {
        Frame transformed = intercept ? FIntercept.filter().apply(df) : df;
        return super.preparePredict(transformed, withResiduals);
    }

    @Override
    protected LinearRegressionResult corePredict(Frame df, boolean withResiduals) {
        LinearRegressionResult result = new LinearRegressionResult(this, df, withResiduals);
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            for (int j = 0; j < result.prediction(target).rowCount(); j++) {
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
                    .append(inputNames.length).append(" IVs [").append(joinMax(5, inputNames)).append("], ")
                    .append(targetNames.length).append(" DVs [").append(joinMax(5, targetNames)).append("].");
        }
        return sb.toString();
    }

    private String joinMax(int max, String[] tokens) {
        int len = Math.min(tokens.length, max);
        String[] firstTokens = new String[len];
        System.arraycopy(tokens, 0, firstTokens, 0, len);
        return String.join(",", firstTokens);
    }

    @Override
    public String toSummary(Printer printer, POption... options) {
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
            DVector coeff = beta.mapCol(i);

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
