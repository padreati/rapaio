package rapaio.ml.eval.metric;

import lombok.Getter;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/28/20.
 */
@Getter
public abstract class AbstractClassifierMetric implements ClassifierMetric {

    private static final long serialVersionUID = 518689520489561196L;

    private final String name;
    protected ClassifierScore score;

    protected AbstractClassifierMetric(String name) {
        this.name = name;
    }
}
