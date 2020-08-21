package rapaio.ml.regression.linear;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.filter.FIntercept;
import rapaio.math.linear.DM;
import rapaio.math.linear.decomposition.QRDecomposition;
import rapaio.math.linear.dense.DMStripe;
import rapaio.math.linear.dense.DVDense;
import rapaio.ml.regression.linear.impl.BaseLinearRegressionModel;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/21/20.
 */
public class WeightedLinearRegression extends BaseLinearRegressionModel<WeightedLinearRegression> {

    /**
     * Builds a linear regression model with intercept.
     *
     * @return new instance of linear regression model
     */
    public static WeightedLinearRegression newModel() {
        return new WeightedLinearRegression();
    }

    private static final long serialVersionUID = 8595413796946622895L;

    @Override
    public WeightedLinearRegression newInstance() {
        return new WeightedLinearRegression().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "WeightedLinearRegression";
    }

    @Override
    protected FitSetup prepareFit(Frame df, Var weights, String... targetVarNames) {
        // add intercept variable
        Frame transformed = intercept.get() ? FIntercept.filter().apply(df) : df;

        // collect standard information
        return super.prepareFit(transformed, weights, targetVarNames);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        var w = DVDense.from(weights);
        w.apply(Math::sqrt);
        DM X = DMStripe.copy(df.mapVars(inputNames())).dotDiagT(w);
        DM Y = DMStripe.copy(df.mapVars(targetNames())).dotDiagT(w);
        beta = QRDecomposition.from(X).solve(Y);
        return true;
    }
}
