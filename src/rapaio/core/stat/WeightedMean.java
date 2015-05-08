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
import rapaio.printer.Printer;

import static rapaio.WS.formatFlex;

/**
 * Compensated weighted mean.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/25/14.
 */
public final class WeightedMean implements Printable {

    private final String varName;
    private final double mean;
    private int missingCount;
    private int completeCount;

    public WeightedMean(final Var var, final Var weights) {
        this.varName = var.name();
        this.mean = compute(var, weights);
    }

    private double compute(final Var var, final Var weights) {
        if (var.rowCount() != weights.rowCount()) {
            throw new IllegalArgumentException("weights must have the same count as values");
        }
        double total = 0;
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i) || weights.missing(i)) {
                missingCount++;
                continue;
            }
            completeCount++;
            total += weights.value(i);
        }
        if (completeCount == 0 || total == 0) {
            return Double.NaN;
        }
        double sum = 0;
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i) || weights.missing(i))
                continue;
            sum += weights.value(i) * var.value(i);
        }
        double avg = sum / total;
        double residual = 0;
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i) || weights.missing(i))
                continue;
            residual += weights.value(i) * (var.value(i) - avg);
        }
        return avg + residual / total;
    }

    public double value() {
        return mean;
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
        sb.append(String.format("\n > weightedMean[%s]\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        sb.append(String.format("weightedMean: %s\n", formatFlex(mean)));
    }
}
