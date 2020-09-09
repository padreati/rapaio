package rapaio.ml.regression.linear;

import rapaio.core.stat.Variance;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/26/20.
 */
public enum Scaling {
    NONE {
        @Override
        public double compute(Var x) {
            return 1;
        }
    },
    SD {
        @Override
        public double compute(Var x) {
            return Variance.of(x).biasedSdValue();
        }
    },
    NORM {
        @Override
        public double compute(Var x) {
            return Math.sqrt(x.copy().op().apply(a -> a * a).op().nansum());
        }
    };

    public abstract double compute(Var x);
}
