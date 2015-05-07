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
import rapaio.data.stream.VSpot;
import rapaio.printer.Printer;

/**
 * Finds the maximum value from a {@link rapaio.data.Var} of values.
 * <p>
 * Ignores missing elements.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Maximum implements Printable {

    private final String varName;
    private final double value;
    private int completeCount;
    private int missingCount;

    public Maximum(Var var) {
        this.varName = var.name();
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i)) {
                missingCount++;
            } else {
                completeCount++;
            }
        }
        if (completeCount == 0) {
            value = Double.NaN;
        } else {
            value = var.stream().complete().mapToDouble().max().getAsDouble();
        }
    }

    public double value() {
        return value;
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
        sb.append(String.format("> maximum[%s]\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        sb.append(String.format("maximum: %s\n", Printer.formatDecFlex.format(value)));
    }
}
