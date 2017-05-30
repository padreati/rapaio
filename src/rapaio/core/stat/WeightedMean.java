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

import rapaio.printer.Printable;
import rapaio.data.Var;

import static rapaio.sys.WS.formatFlex;

/**
 * Compensated weighted mean.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/25/14.
 */
public final class WeightedMean implements Printable {

    public static WeightedMean from(Var var, Var weights) {
        return new WeightedMean(var, weights);
    }

    private final String varName;
    private final double mean;
    private int missingCount;
    private int completeCount;

    private WeightedMean(final Var var, final Var weights) {
        this.varName = var.getName();
        this.mean = compute(var, weights);
    }

    private double compute(final Var var, final Var weights) {
        if (var.getRowCount() != weights.getRowCount()) {
            throw new IllegalArgumentException("weights must have the same count as values");
        }
        double total = 0;
        for (int i = 0; i < var.getRowCount(); i++) {
            if (var.isMissing(i) || weights.isMissing(i)) {
                missingCount++;
                continue;
            }
            completeCount++;
            total += weights.getValue(i);
        }
        if (completeCount == 0 || total == 0) {
            return Double.NaN;
        }
        double sum = 0;
        for (int i = 0; i < var.getRowCount(); i++) {
            if (var.isMissing(i) || weights.isMissing(i))
                continue;
            sum += weights.getValue(i) * var.getValue(i);
        }
        double avg = sum / total;
        double residual = 0;
        for (int i = 0; i < var.getRowCount(); i++) {
            if (var.isMissing(i) || weights.isMissing(i))
                continue;
            residual += weights.getValue(i) * (var.getValue(i) - avg);
        }
        return avg + residual / total;
    }

    public double getValue() {
        return mean;
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n > weightedMean[%s]\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        sb.append(String.format("weightedMean: %s\n", formatFlex(mean)));
        return sb.toString();
    }
}
