package rapaio.ml.classifier.bayes.nb;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/2/20.
 */
public abstract class AbstractEstimator implements Estimator {

    private final List<String> testVarNames;
    private boolean hasLearned = false;

    public AbstractEstimator(List<String> testVarNames) {
        this.testVarNames = new ArrayList<>(testVarNames);
    }

    private static final long serialVersionUID = 2641684738382610007L;

    public List<String> getTestNames() {
        return testVarNames;
    }
}
