package rapaio.ml.instance;

import rapaio.data.Frame;
import rapaio.supervised.AbstractClassifier;
import rapaio.supervised.ClassifierResult;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class SimpleKNN extends AbstractClassifier {

    private Frame matrix;
    private int k;
    private Frame train;
    private int classIndex;

    public Frame getMatrix() {
        return matrix;
    }

    public void setMatrix(Frame matrix) {
        this.matrix = matrix;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    @Override
    public void learn(Frame train, int classIndex) {
        this.train = train;
        this.classIndex = classIndex;
    }

    @Override
    public ClassifierResult predict(Frame test) {
        for (int i = 0; i < test.getRowCount(); i++) {
            for (int j = 0; j < train.getRowCount(); j++) {
                double dist = matrix.getCol(test.rowId(j)).getValue(train.rowId(i));

            }
        }
        return null;
    }

    @Override
    public void printModelSummary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
