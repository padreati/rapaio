/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import static rapaio.printer.Format.floatFlex;

/**
 * Compensated version of arithmetic mean of values from a {@code Var}.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:21 PM
 */
public final class Mean implements Printable {

    public static Mean of(Var var) {
        return new Mean(var);
    }

    public static Mean of(double[] values, int start, int end) {
        return new Mean(values, start, end);
    }

    private final String varName;
    private final double value;
    private int missingCount = 0;
    private int completeCount = 0;

    private Mean(Var var) {
        this.varName = var.name();
        double sum = 0.0;
        double[] v = new double[var.size()];
        for (int i = 0; i < var.size(); i++) {
            if (var.isMissing(i)) {
                missingCount++;
                continue;
            }
            sum += var.getDouble(i);
            v[completeCount] = var.getDouble(i);
            completeCount++;
        }
        if (completeCount == 0) {
            this.value = Double.NaN;
            return;
        }
        final double mean = sum / completeCount;
        double mean2 = 0.0;
        for (int i = 0; i < completeCount; i++) {
            mean2 += v[i] - mean;
        }
        this.value = mean + mean2 / completeCount;
    }

    private Mean(double[] values, int start, int end) {
        this.varName = "?";
        double sum = 0.0;
        for (int i = start; i < end; i++) {
            if (Double.isNaN(values[i])) {
                missingCount++;
                continue;
            }
            sum += values[i];
            completeCount++;
        }
        if (completeCount == 0) {
            this.value = Double.NaN;
            return;
        }

        final double mean = sum / completeCount;
        double mean2 = 0.0;
        for (int i = start; i < end; i++) {
            if (Double.isNaN(values[i])) {
                continue;
            }
            mean2 += values[i] - mean;
        }
        this.value = mean + mean2 / completeCount;
    }

    /**
     * Returns the computed mean of the vector
     *
     * @return computed mean
     */
    public double value() {
        return value;
    }

    @Override
    public String toString() {
        return "mean[" + varName + "] = " + floatFlex(value);
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return "> mean[" + varName + "]\n" +
                "total rows: " + floatFlex(completeCount + missingCount) +
                " (complete: " + floatFlex(completeCount) +
                ", missing: " + floatFlex(missingCount) + ")\n" +
                "mean: " + floatFlex(value) + "\n";
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toContent(printer, options);
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        return toContent(printer, options);
    }
}
