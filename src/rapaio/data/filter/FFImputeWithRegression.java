package rapaio.data.filter;

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.filter.ffilter.AbstractFFilter;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.RegressionResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/12/19.
 */
public class FFImputeWithRegression extends AbstractFFilter {

    public static FFImputeWithRegression of(RegressionModel model, VRange inputVars, String targetName) {
        return new FFImputeWithRegression(model, inputVars, targetName);
    }

    private static final long serialVersionUID = 7428989420235407246L;

    private final RegressionModel model;
    private final String targetName;

    private FFImputeWithRegression(RegressionModel model, VRange inputVars, String targetName) {
        super(inputVars);
        this.model = model;
        this.targetName = targetName;
    }

    @Override
    protected void coreFit(Frame df) {
        var selection = df.mapVars(varNames).stream().filter(s -> !s.isMissing(targetName)).toMappedFrame().copy();
        model.fit(selection, targetName);
    }

    @Override
    public Frame apply(Frame df) {
        var toFill = df.stream().filter(s -> s.isMissing(targetName)).toMappedFrame();
        RegressionResult result = model.predict(toFill, false);
        Var prediction = result.firstPrediction();
        for (int i = 0; i < prediction.rowCount(); i++) {
            toFill.setDouble(i, targetName, prediction.getDouble(i));
        }
        return df;
    }

    @Override
    public FFImputeWithRegression newInstance() {
        return new FFImputeWithRegression(model.newInstance(), vRange, targetName);
    }
}
