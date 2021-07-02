/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core.stat;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import static rapaio.printer.Format.floatFlex;

/**
 * Compensated weighted mean.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/25/14.
 */
public final class WeightedMean implements Printable {

    public static WeightedMean of(Var var, Var weights) {
        return new WeightedMean(var, weights);
    }

    public static WeightedMean of(Frame df, Var weights, String varName) {
        return new WeightedMean(df, weights, varName);
    }

    private final String varName;
    private final double mean;
    private int missingCount;
    private int completeCount;

    private WeightedMean(final Var var, final Var weights) {
        this.varName = var.name();
        this.mean = compute(var, weights);
    }

    private WeightedMean(final Frame df, final Var weights, final String varName) {
        this.varName = varName;
        this.mean = compute(df, weights, varName);
    }

    private double compute(final Var var, final Var weights) {
        if (var.size() != weights.size()) {
            throw new IllegalArgumentException("weights must have the same count as values");
        }
        double total = 0;
        double[] v = new double[var.size()];
        double[] w = new double[var.size()];
        for (int i = 0; i < var.size(); i++) {
            if (var.isMissing(i) || weights.isMissing(i)) {
                missingCount++;
                continue;
            }
            total += weights.getDouble(i);
            v[completeCount] = var.getDouble(i);
            w[completeCount] = weights.getDouble(i);
            completeCount++;
        }
        if (completeCount == 0 || total == 0) {
            return Double.NaN;
        }
        double sum = 0;
        for (int i = 0; i < completeCount; i++) {
            sum += w[i] * v[i];
        }
        double avg = sum / total;
        double residual = 0;
        for (int i = 0; i < completeCount; i++) {
            residual += w[i] * (v[i] - avg);
        }
        return avg + residual / total;
    }

    private double compute(final Frame df, final Var weights, String varName) {
        int varNameIndex = df.varIndex(varName);
        if (df.rowCount() != weights.size()) {
            throw new IllegalArgumentException("weights must have the same count as values");
        }
        double total = 0;
        double[] v = new double[df.rowCount()];
        double[] w = new double[weights.size()];
        for (int i = 0; i < df.rowCount(); i++) {
            if (df.isMissing(i, varNameIndex) || weights.isMissing(i)) {
                missingCount++;
                continue;
            }
            total += weights.getDouble(i);
            v[completeCount] = df.getDouble(i, varNameIndex);
            w[completeCount] = weights.getDouble(i);
            completeCount++;
        }
        if (completeCount == 0 || total == 0) {
            return Double.NaN;
        }
        double sum = 0;
        for (int i = 0; i < completeCount; i++) {
            sum += w[i] * v[i];
        }
        double avg = sum / total;
        double residual = 0;
        for (int i = 0; i < completeCount; i++) {
            residual += w[i] * (v[i] - avg);
        }
        return avg + residual / total;
    }

    public double value() {
        return mean;
    }

    @Override
    public String toString() {
        return String.format("weightedMean[%s] = %s", varName, floatFlex(mean));
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("> weightedMean[%s]\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n",
                completeCount + missingCount, completeCount, missingCount));
        sb.append(String.format("weightedMean: %s\n", floatFlex(mean)));
        return sb.toString();
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
