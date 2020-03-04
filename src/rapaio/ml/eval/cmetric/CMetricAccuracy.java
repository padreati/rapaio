package rapaio.ml.eval.cmetric;

import rapaio.data.Var;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public class CMetricAccuracy implements CMetric {

    private static final long serialVersionUID = -3526955062164344415L;

    private final boolean normalize;

    public CMetricAccuracy(boolean normalize) {
        this.normalize = normalize;
    }

    @Override
    public String name() {
        return "Accuracy";
    }

    @Override
    public <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> double compute(ClassifierResult<M> result, Var testVar) {
        Var predictionVar = result.firstClasses();
        double match = 0;
        for (int i = 0; i < testVar.rowCount(); i++) {
            if (predictionVar.getLabel(i).equals(testVar.getLabel(i))) {
                match++;
            }
        }
        return normalize ? match / testVar.rowCount() : match;
    }
}

