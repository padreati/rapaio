package rapaio.ml.regression.linear;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rapaio.core.stat.Variance;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/26/20.
 */
@RequiredArgsConstructor
@Getter
public enum Scaling {
    NONE("None") {
        @Override
        public double compute(Var x) {
            return 1;
        }
    },
    SD("StandardDeviation") {
        @Override
        public double compute(Var x) {
            return Variance.of(x).biasedSdValue();
        }
    },
    NORM("Normalization") {
        @Override
        public double compute(Var x) {
            return Math.sqrt(x.copy().op().apply(a -> a * a).op().nansum());
        }
    };
    private final String name;

    public abstract double compute(Var x);
}
