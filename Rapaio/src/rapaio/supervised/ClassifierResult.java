package rapaio.supervised;

import rapaio.data.Frame;
import rapaio.data.Vector;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface ClassifierResult {

    Frame getTestFrame();

    Vector getClassification();

    Frame getProbabilities();
}
