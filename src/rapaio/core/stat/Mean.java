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

import static rapaio.sys.WS.formatFlex;

/**
 * Compensated version of arithmetic mean of values from a {@code Var}.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:21 PM
 */
public final class Mean implements Printable {

    public static Mean from(Var var) {
        return new Mean(var);
    }

    public static Mean from(double[] values) {
        return new Mean(values, 0, values.length);
    }

    public static Mean from(double[] values, int start, int end) {
        return new Mean(values, start, end);
    }

    private final String varName;
    private final double value;
    private int missingCount = 0;
    private int completeCount = 0;

    private Mean(Var var) {
        this.varName = var.name();
        double sum = 0.0;
        double[] v = new double[var.rowCount()];
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.isMissing(i)) {
                missingCount++;
                continue;
            }
            sum += var.value(i);
            v[completeCount] = var.value(i);
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
            if(Double.isNaN(values[i])) {
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
    public String summary() {
        return "\n" +
                "> mean[" + varName + "]\n" +
                "total rows: " + formatFlex(completeCount + missingCount) +
                " (complete: " + formatFlex(completeCount) +
                ", missing: " + formatFlex(missingCount) + ")\n" +
                "mean: " + formatFlex(value) + "\n";
    }
}
