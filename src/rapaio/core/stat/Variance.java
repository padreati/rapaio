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

import static rapaio.core.CoreStat.mean;

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

    private final String varName;
    private final double value;
    int rowCount;
    int completeCount;
    int missingCount;

    public Variance(Var var) {
        this.varName = var.name();
        this.value = compute(var);
    }

    private double compute(final Var var) {
        double mean = mean(var).value();
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i)) {
                missingCount++;
            } else {
                completeCount++;
            }
            rowCount++;
        }
        if (completeCount == 0) {
            return Double.NaN;
        }
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i)) {
                continue;
            }
            sum2 += Math.pow(var.value(i) - mean, 2);
            sum3 += var.value(i) - mean;
        }
        return (sum2 - Math.pow(sum3, 2) / (1.0 * completeCount)) / (completeCount - 1.0);
    }

    public double value() {
        return value;
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
        sb.append(String.format("> variance['%s']\n", varName));
        sb.append(String.format("total rows: %d\n", rowCount));
        sb.append(String.format("complete: %d, missing: %d\n", completeCount, missingCount));
        sb.append(String.format("variance: %s\n", Printer.formatDecFlex.format(value)));
        sb.append(String.format("sd: %s\n", Printer.formatDecFlex.format(sdValue())));
    }

    public double sdValue() {
        return Math.sqrt(value);
    }
}
