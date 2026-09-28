/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import static rapaio.printer.Format.floatFlex;

import java.util.Arrays;

import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;
import rapaio.util.collection.Doubles;


/**
 * Estimates quantiles from a numerical {@link rapaio.data.Var} of values.
 * <p>
 * The estimated quantiles implements two version of the algorithms:
 * R-7, Excel, SciPy-(1,1), Maple-6
 * R-8, SciPy-(1/3,1/3) version of estimating quantiles.
 * <p>
 * Default type is R-7, but is can be changed.
 * <p>
 * The values passed to the factory methods are <em>probabilities</em> in
 * {@code [0, 1]} (R calls them {@code probs}, numpy {@code q}), not percentages;
 * anything outside that interval is rejected. Missing values are ignored:
 * {@link #values()} always has one entry per requested probability, {@code NaN}
 * for each of them when the variable has no complete value, and the single
 * complete value when it has exactly one.
 * <p>
 * For further reference see:
 * <a href="http://en.wikipedia.org/wiki/Quantile">http://en.wikipedia.org/wiki/Quantile</a>
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
        for (double p : percentiles) {
            if (!(p >= 0 && p <= 1)) {
                throw new IllegalArgumentException("Probability value should lie in [0,1] interval, not " + p);
            }
        }
        this.varName = var.name();
        this.percentiles = Arrays.copyOf(percentiles, percentiles.length);
        this.type = type;
        this.quantiles = compute(var);
    }

    private double[] compute(final Var var) {

        double[] x = new double[var.size()];
        completeCount = 0;
        for (int i = 0; i < x.length; i++) {
            if (var.isMissing(i))
                continue;
            x[completeCount++] = var.getDouble(i);
        }
        missingCount = var.size() - completeCount;

        if (completeCount == 0) {
            return Doubles.newFill(percentiles.length, Double.NaN);
        }
        if (completeCount == 1) {
            return Doubles.newFill(percentiles.length, x[0]);
        }

        Arrays.sort(x, 0, completeCount);

        double[] values = new double[percentiles.length];
        for (int i = 0; i < percentiles.length; i++) {
            double p = percentiles[i];
            int N = completeCount;
            switch (type) {
                case R8 -> {
                    if (p < (2. / 3.) / (N + 1. / 3.)) {
                        values[i] = x[0];
                    } else if (p >= (N - 1. / 3.) / (N + 1. / 3.)) {
                        values[i] = x[N - 1];
                    } else {
                        double h = (N + 1. / 3.) * p + 1. / 3.;
                        int hfloor = (int) StrictMath.floor(h);
                        values[i] = x[hfloor - 1] + (h - hfloor) * (x[hfloor] - x[hfloor - 1]);
                    }
                }
                case R7 -> {
                    double h = (N - 1.0) * p + 1;
                    int hfloor = (int) Math.min(StrictMath.floor(h), N - 1);
                    values[i] = x[hfloor - 1] + (h - hfloor) * (x[hfloor] - x[hfloor - 1]);
                }
            }
        }
        return values;
    }

    /**
     * @return one estimated quantile per requested probability, in the order they were given
     */
    public double[] values() {
        return quantiles;
    }

    /**
     * @return the probabilities these quantiles were estimated for
     */
    public double[] probabilities() {
        return Arrays.copyOf(percentiles, percentiles.length);
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
    public String toContent(Printer printer, POpt<?>... options) {
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
    public String toFullContent(Printer printer, POpt<?>... options) {
        return toContent(printer, options);
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        return toContent(printer, options);
    }

    public enum Type {
        R7,
        R8
    }
}
