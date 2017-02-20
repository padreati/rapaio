/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import rapaio.data.Var;
import rapaio.data.filter.var.VFSort;
import rapaio.printer.Printable;

import java.util.stream.IntStream;

import static rapaio.sys.WS.formatFlex;


/**
 * Estimates quantiles from a numerical {@link rapaio.data.Var} of values.
 * <p>
 * The estimated quantiles implements two version of the algorithms:
 * R-7, Excel, SciPy-(1,1), Maple-6
 * R-8, SciPy-(1/3,1/3) version of estimating quantiles.
 * <p>
 * Default type is R-7, but is can be changed.
 * <p>
 * <p>
 * For further reference see:
 * http://en.wikipedia.org/wiki/Quantile
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Quantiles implements Printable {

    public static Quantiles from(Var var, double...percentiles) {
        return new Quantiles(var, Type.R7, percentiles);
    }

    public static Quantiles from(Var var, Quantiles.Type type, double...percentiles) {
        return new Quantiles(var, type, percentiles);
    }

    private final String varName;
    private final double[] percentiles;
    private final double[] quantiles;
    private int completeCount;
    private int missingCount;
    private final Type type;

    private Quantiles(Var var, Type type, double... percentiles) {
        this.varName = var.name();
        this.percentiles = percentiles;
        this.type = type;
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
        Var x = new VFSort().fitApply(complete);
        double[] values = new double[percentiles.length];
        for (int i = 0; i < percentiles.length; i++) {
            double p = percentiles[i];
            if (type.equals(Type.R8)) {
                int N = x.rowCount();
                double h = (N + 1. / 3.) * p + 1. / 3.;
                int hfloor = (int) StrictMath.floor(h);

                if (p < (2. / 3.) / (N + 1. / 3.)) {
                    values[i] = x.value(0);
                    continue;
                }
                if (p >= (N - 1. / 3.) / (N + 1. / 3.)) {
                    values[i] = x.value(x.rowCount() - 1);
                    continue;
                }
                values[i] = x.value(hfloor - 1) + (h - hfloor) * (x.value(hfloor) - x.value(hfloor - 1));
            }
            if (type.equals(Type.R7)) {
                int N = x.rowCount();
                double h = (N - 1.0) * p + 1;
                int hfloor = (int) Math.min(StrictMath.floor(h), x.rowCount() - 1);
                values[i] = x.value(hfloor - 1) + (h - hfloor) * (x.value(hfloor) - x.value(hfloor - 1));
            }
        }
        return values;
    }

    public double[] values() {
        return quantiles;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n > quantiles[%s] - estimated quantiles\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        for (int i = 0; i < quantiles.length; i++) {
            sb.append(String.format("quantile[%s] = %s\n", formatFlex(percentiles[i]), formatFlex(quantiles[i])));
        }
        return sb.toString();
    }

    public enum Type {
        R7,
        R8
    }
}
