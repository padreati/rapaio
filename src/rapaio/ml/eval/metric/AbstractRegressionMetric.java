package rapaio.ml.eval.metric;

import lombok.Getter;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/28/20.
 */
@Getter
public abstract class AbstractRegressionMetric implements RegressionMetric {

    private static final long serialVersionUID = -1936941004623984728L;

    private final String name;

    protected AbstractRegressionMetric(String name) {
        this.name = name;
    }
}
