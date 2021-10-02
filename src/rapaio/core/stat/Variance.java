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

import static rapaio.printer.Format.floatFlex;

import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Compensated version of the algorithm for calculation of
 * sample variance of values from a {@link rapaio.data.Var}.
 * <p>
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:26 PM
 */
public class Variance implements Printable {

    public static Variance of(Var var) {
        return new Variance(var);
    }

    private final String varName;
    private double value;
    private double biasedValue;
    private int completeCount;
    private int missingCount;

    private Variance(Var var) {
        this.varName = var.name();
        compute(var);
    }

    private void compute(final Var var) {
        double mean = Mean.of(var).value();
        for (int i = 0; i < var.size(); i++) {
            if (var.isMissing(i)) {
                missingCount++;
            } else {
                completeCount++;
            }
        }
        if (completeCount == 0) {
            value = Double.NaN;
            biasedValue = Double.NaN;
        }
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < var.size(); i++) {
            if (var.isMissing(i)) {
                continue;
            }
            sum2 += Math.pow(var.getDouble(i) - mean, 2);
            sum3 += var.getDouble(i) - mean;
        }
        value = (sum2 - Math.pow(sum3, 2) / (1.0 * completeCount)) / (completeCount - 1.0);
        biasedValue = (sum2 - Math.pow(sum3, 2) / (1.0 * completeCount)) / (1.0 * completeCount);
    }

    public double value() {
        return value;
    }

    public double biasedValue() {
        return biasedValue;
    }

    public double sdValue() {
        return Math.sqrt(value);
    }

    public double biasedSdValue() {
        return Math.sqrt(biasedValue);
    }

    public int completeCount() {
        return completeCount;
    }

    public int missingCount() {
        return missingCount;
    }

    @Override
    public String toString() {
        return String.format("variance[%s] = %s, std: %s", varName, floatFlex(value), floatFlex(sdValue()));
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("> variance[%s]\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n",
                completeCount() + missingCount(), completeCount(), missingCount()));
        sb.append(String.format("variance: %s\n", floatFlex(value)));
        sb.append(String.format("sd: %s\n", floatFlex(sdValue())));
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
