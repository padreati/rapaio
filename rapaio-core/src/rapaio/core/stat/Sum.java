/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Computes the sum of elements for a {@link rapaio.data.Var} of values.
 * Ignore missing data values.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Sum implements Printable {

    public static Sum of(Var var) {
        return new Sum(var);
    }

    private final String varName;
    private final double value;
    private int completeCount;
    private int missingCount;

    private Sum(Var var) {
        this.varName = var.name();
        this.value = compute(var);
    }

    private double compute(Var var) {
        double sum = 0;
        int pos = 0;
        for (int i = 0; i < var.size(); i++) {
            if (var.isMissing(i)) {
                missingCount++;
                continue;
            }
            sum = var.getDouble(i);
            pos = i + 1;
            completeCount++;
            break;
        }

        // A running compensation for lost low-order bits.
        double c = 0.0;
        for (int i = pos; i < var.size(); i++) {
            if (var.isMissing(i)) {
                missingCount++;
                continue;
            }
            completeCount++;

            double t = sum + var.getDouble(i);
            if (Math.abs(sum) >= Math.abs(var.getDouble(i))) {
                // If sum is bigger, low-order digits of input[i] are lost.
                c += (sum - t) + var.getDouble(i);
            } else {
                // Else low-order digits of sum are lost
                c += (var.getDouble(i) - t) + sum;
            }
            sum = t;
        }
        // Correction only applied once in the very end
        return sum + c;
    }

    public double value() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("sum[%s] = %s", varName, floatFlex(value));
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return String.format("""
                        > sum[%s]
                        total rows: %d (complete: %d, missing: %d)
                        sum: %s
                        """,
                varName, completeCount + missingCount, completeCount, missingCount,
                floatFlex(value));
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        return toContent(printer, options);
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        return toContent(printer, options);
    }
}
