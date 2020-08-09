package rapaio.ml.regression.linear;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.filter.FIntercept;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.decomposition.QRDecomposition;
import rapaio.math.linear.dense.SolidDMatrix;
import rapaio.ml.common.ValueParam;
import rapaio.ml.regression.linear.impl.BaseLinearRegressionModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/26/20.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RidgeRegression extends BaseLinearRegressionModel<RidgeRegression> {

    public static RidgeRegression newModel(double lambda) {
        return newModel(lambda, Centering.MEAN, Scaling.SD);
    }

    public static RidgeRegression newModel(double lambda, Centering centering, Scaling scaling) {
        return new RidgeRegression()
                .lambda.set(lambda)
                .centering.set(centering)
                .scaling.set(scaling);
    }

    private static final long serialVersionUID = 6868244273014714128L;

    @Getter
    public final ValueParam<Double, RidgeRegression> lambda = new ValueParam<>(this, 1.0,
            "lambda",
            "Coefficient of the ridge penalry term.",
            Double::isFinite);

    public final ValueParam<Centering, RidgeRegression> centering = new ValueParam<>(this, Centering.MEAN,
            "centering",
            "Type of variable centering",
            Objects::nonNull);

    public final ValueParam<Scaling, RidgeRegression> scaling = new ValueParam<>(this, Scaling.SD,
            "scaling",
            "Type if the variable scaling.",
            Objects::nonNull);

    private Map<String, Double> inputMean;
    private Map<String, Double> inputScale;
    private Map<String, Double> targetMean;
    private Map<String, Double> targetScale;

    @Override
    public RidgeRegression newInstance() {
        return new RidgeRegression().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "RidgeRegression";
    }

    @Override
    protected FitSetup prepareFit(Frame df, Var weights, String... targetVarNames) {
        // add intercept variable
        Frame transformed = intercept.get() ? FIntercept.filter().apply(df) : df;

        // collect standard information
        FitSetup fitSetup = super.prepareFit(transformed, weights, targetVarNames);

        inputMean = new HashMap<>();
        inputScale = new HashMap<>();
        targetMean = new HashMap<>();
        targetScale = new HashMap<>();

        for (String inputName : inputNames) {
            if (FIntercept.INTERCEPT.equals(inputName)) {
                inputMean.put(inputName, 0.0);
                inputScale.put(inputName, 1.0);
            } else {
                inputMean.put(inputName, centering.get().compute(fitSetup.df.rvar(inputName)));
                inputScale.put(inputName, scaling.get().compute(fitSetup.df.rvar(inputName)));
            }
        }
        for (String targetName : targetNames) {
            targetMean.put(targetName, centering.get().compute(fitSetup.df.rvar(targetName)));
            targetScale.put(targetName, scaling.get().compute(fitSetup.df.rvar(targetName)));
        }

        return FitSetup.valueOf(fitSetup.df, fitSetup.w, fitSetup.targetVars);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        int interceptIndex = -1;
        String[] selNames = new String[inputNames.length - (intercept.get() ? 1 : 0)];
        int pos = 0;
        for (int i = 0; i < inputNames.length; i++) {
            if (FIntercept.INTERCEPT.equals(inputNames[i])) {
                interceptIndex = i;
                continue;
            }
            selNames[pos++] = inputNames[i];
        }

        DMatrix X = SolidDMatrix.empty(df.rowCount(), selNames.length);
        DMatrix Y = SolidDMatrix.empty(df.rowCount(), targetNames.length);

        if (intercept.get()) {
            // scale in values if we have intercept
            for (int j = 0; j < selNames.length; j++) {
                int varIndex = df.varIndex(selNames[j]);
                double mean = inputMean.get(selNames[j]);
                double sd = inputScale.get(selNames[j]);
                for (int i = 0; i < df.rowCount(); i++) {
                    X.set(i, j, (df.getDouble(i, varIndex) - mean) / sd);
                }
            }
            for (int j = 0; j < targetNames.length; j++) {
                int varIndex = df.varIndex(targetNames[j]);
                double mean = targetMean.get(targetNames[j]);
                double sd = targetScale.get(targetNames[j]);
                for (int i = 0; i < df.rowCount(); i++) {
                    Y.set(i, j, (df.getDouble(i, varIndex) - mean) / sd);
                }
            }
        } else {
            // if we do not have intercept we ignore centering and scaling
            for (int j = 0; j < selNames.length; j++) {
                int varIndex = df.varIndex(selNames[j]);
                for (int i = 0; i < df.rowCount(); i++) {
                    X.set(i, j, df.getDouble(i, varIndex));
                }
            }
            for (int j = 0; j < targetNames.length; j++) {
                int varIndex = df.varIndex(targetNames[j]);
                for (int i = 0; i < df.rowCount(); i++) {
                    Y.set(i, j, df.getDouble(i, varIndex));
                }
            }
        }

        // solve the scaled system
        DMatrix l = SolidDMatrix.identity(X.colCount()).times(lambda.get());
        DMatrix A = X.t().dot(X).plus(l);
        DMatrix B = X.t().dot(Y);
        DMatrix scaledBeta = QRDecomposition.from(A).solve(B);

        if (intercept.get()) {
            beta = SolidDMatrix.fill(scaledBeta.rowCount() + 1, scaledBeta.colCount(), 0);

            for (int i = 0; i < targetNames.length; i++) {
                String targetName = targetName(i);
                double targetScale = this.targetScale.get(targetName);
                for (int j = 0; j < inputNames.length; j++) {
                    if (FIntercept.INTERCEPT.equals(inputNames[j])) {
                        double interceptValue = targetMean.get(targetName);
                        for (int k = 0; k < inputNames.length; k++) {
                            if (k == j) {
                                continue;
                            }
                            int offset = k >= interceptIndex ? 1 : 0;
                            interceptValue -= scaledBeta.get(k - offset, i) * targetScale * inputMean.get(inputNames[k]) / inputScale.get(inputNames[k]);
                        }
                        beta.set(j, i, interceptValue);
                    } else {
                        int offset = j >= interceptIndex ? 1 : 0;
                        beta.set(j, i, scaledBeta.get(j - offset, i) * targetScale / inputScale.get(inputNames[j]));
                    }
                }
            }
        } else {
            beta = scaledBeta;
        }
        return true;
    }

}
