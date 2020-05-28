package rapaio.ml.eval.metric;

import rapaio.data.Var;
import rapaio.ml.classifier.ClassifierResult;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public interface ClassifierMetric extends Serializable {

    String getName();

    ClassifierMetric compute(Var actual, ClassifierResult prediction);

    ClassifierScore getScore();
}