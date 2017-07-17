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

import rapaio.math.MTools;
import rapaio.data.Var;
import rapaio.printer.Printable;

import java.util.stream.DoubleStream;

import static rapaio.sys.WS.formatFlex;

/**
 * Computes the sum of elements for a {@link rapaio.data.Var} of values.
 * <p>
 * Ignore invalid numeric values. See {@link MTools#validNumber(double)}.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Sum implements Printable {

    public static Sum from(Var var) {
        return new Sum(var);
    }

    private final String varName;
    private final double value;
    private int completeCount;
    private int missingCount;

    private Sum(Var var) {
        this.varName = var.getName();
        this.value = compute(var);
    }

    private double compute(Var var) {
        double sum = 0;
        int pos = 0;
        for (int i = 0; i < var.getRowCount(); i++) {
            if (var.isMissing(i)) {
                missingCount++;
                continue;
            }
            sum = var.getValue(i);
            pos = i + 1;
            completeCount++;
            break;
        }

        // A running compensation for lost low-order bits.
        double c = 0.0;
        for (int i = pos; i < var.getRowCount(); i++) {
            if(var.isMissing(i)) {
                missingCount++;
                continue;
            }

            completeCount++;

            double t = sum + var.getValue(i);

            if (Math.abs(sum) >= Math.abs(var.getValue(i))) {
                // If sum is bigger, low-order digits of input[i] are lost.
                c += (sum - t) + var.getValue(i);
            } else {
                // Else low-order digits of sum are lost
                c += (var.getValue(i) - t) + sum;
            }
            sum = t;
        }
        // Correction only applied once in the very end
        return sum + c;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n > sum['%s']\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        sb.append(String.format("sum: %s\n", formatFlex(value)));
        return sb.toString();
    }
}
