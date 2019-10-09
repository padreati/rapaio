/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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
import rapaio.printer.Printable;

import java.util.Arrays;

import static rapaio.printer.format.Format.floatFlex;


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

    public static Quantiles of(Var var, double... percentiles) {
        return new Quantiles(var, Type.R7, percentiles);
    }

    public static Quantiles of(Var var, Quantiles.Type type, double... percentiles) {
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

        double[] x = new double[var.rowCount()];
        completeCount = 0;
        for (int i = 0; i < x.length; i++) {
            if (var.isMissing(i))
                continue;
            x[completeCount++] = var.getDouble(i);
        }
        missingCount = var.rowCount() - completeCount;

        if (completeCount == 0) {
            Arrays.fill(x, Double.NaN);
            return x;
        }
        if (completeCount == 1) {
            double[] values = new double[percentiles.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = x[0];
            }
            return values;
        }

        Arrays.sort(x, 0, completeCount);

        double[] values = new double[percentiles.length];
        for (int i = 0; i < percentiles.length; i++) {
            double p = percentiles[i];
            if (type.equals(Type.R8)) {
                int N = completeCount;
                double h = (N + 1. / 3.) * p + 1. / 3.;
                int hfloor = (int) StrictMath.floor(h);

                if (p < (2. / 3.) / (N + 1. / 3.)) {
                    values[i] = x[0];
                    continue;
                }
                if (p >= (N - 1. / 3.) / (N + 1. / 3.)) {
                    values[i] = x[completeCount - 1];
                    continue;
                }
                values[i] = x[hfloor - 1] + (h - hfloor) * (x[hfloor] - x[hfloor - 1]);
            }
            if (type.equals(Type.R7)) {
                int N = completeCount;
                double h = (N - 1.0) * p + 1;
                int hfloor = (int) Math.min(StrictMath.floor(h), completeCount - 1);
                values[i] = x[hfloor - 1] + (h - hfloor) * (x[hfloor] - x[hfloor - 1]);
            }
        }
        return values;
    }

    public double[] values() {
        return quantiles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("quantiles[").append(varName).append("] = {");
        for (int i = 0; i < quantiles.length; i++) {
            sb.append(floatFlex(percentiles[i])).append(":").append(floatFlex(quantiles[i]));
            if (i < quantiles.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String content() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("> quantiles[%s] - estimated quantiles\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        for (int i = 0; i < quantiles.length; i++) {
            sb.append(String.format("quantile[%s] = %s\n", floatFlex(percentiles[i]), floatFlex(quantiles[i])));
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String fullContent() {
        return content();
    }

    @Override
    public String summary() {
        return content();
    }

    public enum Type {
        R7,
        R8
    }
}
