/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.core.stat;

import rapaio.printer.Printable;
import rapaio.data.Var;
import rapaio.data.filter.var.VFSort;
import rapaio.printer.Printer;

import java.util.stream.IntStream;


/**
 * Estimates quantiles from a numerical {@link rapaio.data.Var} of values.
 * <p>
 * The estimated quantiles implements R-8, SciPy-(1/3,1/3) version of estimating quantiles.
 * <p>
 * For further reference see:
 * http://en.wikipedia.org/wiki/Quantile
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Quantiles implements Printable {

    private final String varName;
    private final double[] percentiles;
    private final double[] quantiles;
    private int completeCount;
    private int missingCount;

    public Quantiles(Var var, double... percentiles) {
        this.varName = var.name();
        this.percentiles = percentiles;
        this.quantiles = compute(var);
    }

    private double[] compute(final Var var) {
        Var complete = var.stream().complete().toMappedVar();
        missingCount = var.rowCount() - complete.rowCount();
        completeCount = complete.rowCount();
        if (complete.rowCount() == 0) {
            return IntStream.range(0, percentiles.length).mapToDouble(i -> Double.NaN).toArray();
        }
        if (complete.rowCount() == 1) {
            double[] values = new double[percentiles.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = complete.value(0);
            }
            return values;
        }
        Var sorted = new VFSort().fitApply(complete);
        double[] values = new double[percentiles.length];
        for (int i = 0; i < percentiles.length; i++) {
            int N = sorted.rowCount();
            double h = (N + 1. / 3.) * percentiles[i] + 1. / 3.;
            int hfloor = (int) Math.floor(h);

            if (percentiles[i] < (2. / 3.) / (N + 1. / 3.)) {
                values[i] = sorted.value(0);
                continue;
            }
            if (percentiles[i] >= (N - 1. / 3.) / (N + 1. / 3.)) {
                values[i] = sorted.value(sorted.rowCount() - 1);
                continue;
            }
            values[i] = sorted.value(hfloor - 1) + (h - hfloor) * (sorted.value(hfloor) - sorted.value(hfloor - 1));
        }
        return values;
    }

    public double[] values() {
        return quantiles;
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
        sb.append(String.format("> quantiles[%s] - estimated quantiles\n", varName));
        sb.append(String.format("total rows: %d\n", completeCount + missingCount));
        sb.append(String.format("complete: %d, missing: %d\n", completeCount, missingCount));
        for (int i = 0; i < quantiles.length; i++) {
            sb.append(String.format("quantile[%f = %f\n", percentiles[i], quantiles[i]));
        }
    }
}
