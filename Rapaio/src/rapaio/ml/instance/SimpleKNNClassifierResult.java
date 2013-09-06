package rapaio.ml.instance;

import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.supervised.ClassifierResult;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SimpleKNNClassifierResult implements ClassifierResult {

    private final Frame test;

    public SimpleKNNClassifierResult(Frame test) {
        this.test = test;
    }

    @Override
    public Frame getTestFrame() {
        return test;
    }

    @Override
    public Vector getClassification() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Frame getProbabilities() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
