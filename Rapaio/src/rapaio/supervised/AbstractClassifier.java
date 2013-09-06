package rapaio.supervised;

import rapaio.data.Frame;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractClassifier implements Classifier {

    @Override
    public void learn(Frame df, String className) {
        learn(df, df.getColIndex(className));
    }
}
