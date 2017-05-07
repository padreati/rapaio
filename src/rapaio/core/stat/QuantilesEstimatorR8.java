package rapaio.core.stat;

import rapaio.data.Var;
import rapaio.data.filter.var.VFSort;

public class QuantilesEstimatorR8 extends QuantilesEstimator {

    @Override
    protected double[] estimateNontrivial(Var complete, double[] percentiles) {
        Var x = new VFSort().fitApply(complete);
        int N = x.rowCount();
        double[] values = new double[percentiles.length];
        for (int i = 0; i < percentiles.length; i++) {
            double p = percentiles[i];
            double h = (N + 1.0 / 3.0) * p + 1.0 / 3.0;
            int hfloor = (int) StrictMath.floor(h);

            if (p < (2.0 / 3.0) / (N + 1.0 / 3.0)) {
                values[i] = x.value(0);
            } else if (p >= (N - 1.0 / 3.0) / (N + 1.0 / 3.0)) {
                values[i] = x.value(N - 1);
            } else {
                values[i] = x.value(hfloor - 1) + 
                        (h - hfloor) * (x.value(hfloor) - x.value(hfloor - 1));
            }
        }
        return values;
    }

}
