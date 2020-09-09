package rapaio.ml.regression.linear;

import rapaio.core.stat.Mean;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/26/20.
 */
public enum Centering {
    NONE {
        @Override
        public double compute(Var x) {
            return 0;
        }
    },
    MEAN {
        @Override
        public double compute(Var x) {
            return Mean.of(x).value();
        }
    };
    public abstract double compute(Var x);
}
