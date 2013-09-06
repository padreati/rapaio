package rapaio.supervised.rule;

import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.supervised.ClassifierResult;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRuleClassifierResult implements ClassifierResult {

    private final Frame test;
    private final Vector pred;

    public OneRuleClassifierResult(Frame test, Vector pred) {
        this.test = test;
        this.pred = pred;
    }

    @Override
    public Frame getTestFrame() {
        return test;
    }

    @Override
    public Vector getClassification() {
        return pred;
    }

    @Override
    public Frame getProbabilities() {
        return null;
    }

}
