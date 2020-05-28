package rapaio.ml.eval.metric;

import rapaio.data.Var;
import rapaio.ml.classifier.ClassifierResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public class LogLoss extends AbstractClassifierMetric {

    public static LogLoss newMetric(double eps) {
        return new LogLoss(eps);
    }

    private static final long serialVersionUID = 8850076650664844719L;
    private static final String NAME = "LogLoss";

    private final double eps;

    private LogLoss(double eps) {
        super(NAME);
        this.eps = eps;
    }

    @Override
    public LogLoss compute(Var actual, ClassifierResult result) {
        double logloss = 0;
        for (int i = 0; i < actual.rowCount(); i++) {
            logloss -= Math.log(Math.max(eps, Math.min(1 - eps, result.firstDensity().getDouble(i, actual.getLabel(i)))));
        }
        score = ClassifierScore.builder().value(logloss).build();
        return this;
    }
}
