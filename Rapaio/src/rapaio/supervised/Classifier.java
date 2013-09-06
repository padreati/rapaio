package rapaio.supervised;

import rapaio.data.Frame;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Classifier {

    void learn(Frame df, int classIndex);

    void learn(Frame df, String className);

    void printModelSummary();

    ClassifierResult predict(Frame df);
}
