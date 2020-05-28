package rapaio.ml.eval.metric;

import rapaio.data.Var;
import rapaio.ml.classifier.ClassifierResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public class Accuracy extends AbstractClassifierMetric implements ClassifierMetric {

    public static Accuracy newMetric(boolean normalize) {
        return new Accuracy(normalize);
    }

    public static Accuracy newMetric() {
        return newMetric(true);
    }

    private static final long serialVersionUID = -3526955062164344415L;

    private final boolean normalize;

    private Accuracy(boolean normalize) {
        super("Accuracy");
        this.normalize = normalize;
    }

    @Override
    public Accuracy compute(Var actual, ClassifierResult prediction) {
        double match = 0;
        for (int i = 0; i < prediction.firstClasses().rowCount(); i++) {
            if (prediction.firstClasses().getLabel(i).equals(actual.getLabel(i))) {
                match++;
            }
        }
        score = ClassifierScore.builder()
                .value(normalize ? match / prediction.firstClasses().rowCount() : match)
                .build();
        return this;
    }
}

