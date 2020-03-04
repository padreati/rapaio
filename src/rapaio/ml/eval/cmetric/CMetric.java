package rapaio.ml.eval.cmetric;

import rapaio.data.Var;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public interface CMetric extends Serializable {

    String name();

    <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> double compute(ClassifierResult<M> result, Var testVar);

    static CMetric accuracy() {
        return new CMetricAccuracy(true);
    }

    static CMetric accuracy(boolean normalize) {
        return new CMetricAccuracy(normalize);
    }

    static CMetric logloss() {
        return new CMetricLogLoss(1e-15);
    }

    static CMetric logloss(double eps) {
        return new CMetricLogLoss(eps);
    }
}