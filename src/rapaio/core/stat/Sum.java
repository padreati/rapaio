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

import rapaio.data.Var;
import rapaio.printer.Printable;

import static rapaio.WS.formatFlex;

/**
 * Computes the sum of elements for a {@link rapaio.data.Var} of values.
 * <p>
 * Ignore invalid numeric values. See {@link rapaio.core.MathBase#validNumber(double)}.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Sum implements Printable {

    private final String varName;
    private final double value;
    private int completeCount;
    private int missingCount;

    public Sum(Var var) {
        this.varName = var.name();
        this.value = compute(var);
    }

    private double compute(Var var) {
        double sum = 0;
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i)) {
                missingCount++;
            } else {
                completeCount++;
                sum += var.value(i);
            }
        }
        return sum;
    }

    public double value() {
        return value;
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
        sb.append(String.format("> sum['%s']\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        sb.append(String.format("sum: %s\n", formatFlex(value)));
    }
}