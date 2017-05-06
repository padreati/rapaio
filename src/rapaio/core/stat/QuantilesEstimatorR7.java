package rapaio.core.stat;

import rapaio.data.Var;
import rapaio.data.filter.var.VFSort;

public class QuantilesEstimatorR7 extends QuantilesEstimator {

    @Override
    protected double[] estimateNontrivial(Var complete, double[] percentiles) {
        Var x = new VFSort().fitApply(complete);
        int N = x.rowCount();
        double[] values = new double[percentiles.length];
        for (int i = 0; i < percentiles.length; i++) {
            double p = percentiles[i];
            double h = (N - 1) * p + 1;
            int hfloor = (int) Math.min(StrictMath.floor(h), N - 1);
            values[i] = x.value(hfloor - 1) + (h - hfloor) * (x.value(hfloor) - x.value(hfloor - 1));
        }
        return values;
    }
}
