/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.core.stat;

import rapaio.core.Printable;
import rapaio.data.Vector;

import static rapaio.core.MathBase.floor;
import static rapaio.data.filters.BaseFilters.sort;


/**
 * Estimates quantiles from a numerical {@link rapaio.data.Vector} of values.
 * <p>
 * The estimated quantiles implements R-8, SciPy-(1/3,1/3) version of estimating quantiles.
 * <p>
 * For further reference see:
 * http://en.wikipedia.org/wiki/Quantile
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Quantiles implements Printable {

    private final Vector vector;
    private final double[] percentiles;
    private final double[] quantiles;

    public Quantiles(Vector vector, double[] percentiles) {
        this.vector = vector;
        this.percentiles = percentiles;
        this.quantiles = compute();
    }

    private double[] compute() {
        if (vector.rowCount() == 1) {
            double[] values = new double[percentiles.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = vector.getValue(0);
            }
            return values;
        }
        Vector sorted = sort(vector);
        int start = 0;
        while (sorted.isMissing(start)) {
            start++;
            if (start == sorted.rowCount()) {
                break;
            }
        }
        double[] values = new double[percentiles.length];
        if (start == sorted.rowCount()) {
            return values;
        }
        for (int i = 0; i < percentiles.length; i++) {
            int N = sorted.rowCount() - start;
            double h = (N + 1. / 3.) * percentiles[i] + 1. / 3.;
            int hfloor = (int) floor(h);

            if (percentiles[i] < (2. / 3.) / (N + 1. / 3.)) {
                values[i] = sorted.getValue(start);
                continue;
            }
            if (percentiles[i] >= (N - 1. / 3.) / (N + 1. / 3.)) {
                values[i] = sorted.getValue(sorted.rowCount() - 1);
                continue;
            }
            values[i] = sorted.getValue(start + hfloor - 1)
                    + (h - hfloor) * (sorted.getValue(start + hfloor) - sorted.getValue(start + hfloor - 1));
        }
        return values;
    }

    public double[] getValues() {
        return quantiles;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("> quantiles - estimated quantiles\n");
        for (int i = 0; i < quantiles.length; i++) {
            sb.append(String.format("quantile[%f = %f\n", percentiles[i], quantiles[i]));
        }
    }
}
