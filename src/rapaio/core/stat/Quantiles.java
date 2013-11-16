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

import static rapaio.core.BaseMath.floor;

import rapaio.core.Summarizable;
import rapaio.data.Vector;

import static rapaio.filters.RowFilters.sort;
import static rapaio.explore.Workspace.*;


/**
 * Estimates quantiles from a numerical {@link rapaio.data.Vector} of values.
 * <p/>
 * The estimated quantiles implements R-8, SciPy-(1/3,1/3) version of estimating quantiles.
 * <p/>
 * For further reference see:
 * http://en.wikipedia.org/wiki/Quantile
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Quantiles implements Summarizable {

    private final Vector vector;
    private final double[] percentiles;
    private final double[] quantiles;

    public Quantiles(Vector vector, double[] percentiles) {
        this.vector = vector;
        this.percentiles = percentiles;
        this.quantiles = compute();
    }

    private double[] compute() {
        Vector sorted = sort(vector);
        int start = 0;
        while (sorted.isMissing(start)) {
            start++;
            if (start == sorted.getRowCount()) {
                break;
            }
        }
        double[] values = new double[percentiles.length];
        if (start == sorted.getRowCount()) {
            return values;
        }
        for (int i = 0; i < percentiles.length; i++) {
            int N = sorted.getRowCount() - start;
            double h = (N + 1. / 3.) * percentiles[i] + 1. / 3.;
            int hfloor = (int) floor(h);

            if (percentiles[i] < (2. / 3.) / (N + 1. / 3.)) {
                values[i] = sorted.getValue(start);
                continue;
            }
            if (percentiles[i] >= (N - 1. / 3.) / (N + 1. / 3.)) {
                values[i] = sorted.getValue(sorted.getRowCount() - 1);
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
    public void summary() {
        printfln("quantiles[\"%s\", ...] - estimated quantiles", vector.getName());
        for (int i = 0; i < quantiles.length; i++) {
            printfln("quantile[\"%s\",%f = %f\n", vector.getName(), percentiles[i], quantiles[i]);
        }
    }
}
