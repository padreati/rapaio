package rapaio.ml.classifier.bayes.nb;

import rapaio.data.Frame;
import rapaio.data.Var;

import java.io.Serializable;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/4/20.
 */
public interface Estimator extends Serializable {

    /**
     * @return new unfitted instance of the estimator
     */
    Estimator newInstance();

    /**
     * @return estimator name
     */
    String name();

    /**
     * @return estimator name after fit
     */
    String fittedName();

    /**
     * Get test variable names for this estimator
     * @return list of test variable names
     */
    List<String> getTestVarNames();

    /**
     * Fit the estimator on data
     *
     * @param df frame with observations
     * @param weights vector of weights
     * @param targetName target variable name
     * @return true if estimator fit on data, false for failure
     */
    boolean fit(Frame df, Var weights, String targetName);

    /**
     * Predicts p(x|target=targetLevel)
     *
     * @param df frame with observations
     * @param row row index of the observation
     * @param targetLevel target level for conditional distribution
     * @return conditioned probability prediction
     */
    double predict(Frame df, int row, String targetLevel);
}
