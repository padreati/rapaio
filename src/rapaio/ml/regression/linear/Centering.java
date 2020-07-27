package rapaio.ml.regression.linear;

import lombok.RequiredArgsConstructor;
import rapaio.core.stat.Mean;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/26/20.
 */
@RequiredArgsConstructor
public enum Centering {
    NONE("None") {
        @Override
        public double compute(Var x) {
            return 0;
        }
    },
    MEAN("Mean") {
        @Override
        public double compute(Var x) {
            return Mean.of(x).value();
        }
    };
    private final String name;

    public abstract double compute(Var x);
}
