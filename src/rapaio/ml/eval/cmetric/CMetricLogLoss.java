package rapaio.ml.eval.cmetric;

import rapaio.data.Var;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public class CMetricLogLoss implements CMetric {

    private static final long serialVersionUID = 8850076650664844719L;

    private final double eps;

    public CMetricLogLoss(double eps) {
        this.eps = eps;
    }

    @Override
    public String name() {
        return "LogLoss";
    }

    @Override
    public <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> double compute(ClassifierResult<M> result, Var testVar) {
        var densities = result.firstDensity();
        double logloss = 0;
        for (int i = 0; i < testVar.rowCount(); i++) {
            logloss -= Math.log(Math.max(eps, Math.min(1 - eps, densities.getDouble(i, testVar.getLabel(i)))));
        }
        return logloss;
    }
}
