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

import static rapaio.sys.WS.formatFlex;

/**
 * Compensated version of arithmetic mean of values from a {@code Var}.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:21 PM
 */
public final class Mean implements Printable {

    private final String varName;
    private final double value;
    private int missingCount = 0;
    private int completeCount = 0;

    public Mean(Var var) {
        this.varName = var.name();
        double sum = 0.0;
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i)) {
                missingCount++;
            } else {
                completeCount++;
                sum += var.value(i);
            }
        }
        final double mean = sum / (1.0 * completeCount);
        this.value = mean + var.spotStream().complete().mapToDouble(s -> s.value() - mean).sum() / (1.0 * completeCount);
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
                "total rows: " + (completeCount + missingCount) + " (complete: " + completeCount + ", missing: " + missingCount + ")\n" +
                "mean: " + formatFlex(value) + "\n";
    }
}
