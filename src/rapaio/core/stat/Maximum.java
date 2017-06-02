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

import rapaio.data.Var;
import rapaio.printer.Printable;

import static rapaio.sys.WS.formatFlex;

/**
 * Finds the maximum value from a {@link rapaio.data.Var} of values.
 * <p>
 * Ignores missing elements.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Maximum implements Printable {

    public static Maximum from(Var var) {
        return new Maximum(var);
    }

    private final String varName;
    private final double value;
    private int completeCount;
    private int missingCount;

    private Maximum(Var var) {
        this.varName = var.getName();
        for (int i = 0; i < var.getRowCount(); i++) {
            if (var.isMissing(i)) {
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

    public double getValue() {
        return value;
    }

    @Override
    public String getSummary() {
        return "\n" +
                "> maximum[" + varName + "]\n" +
                "total rows: " + (completeCount + missingCount) + " (complete: " + completeCount + " missing: " + missingCount + ")\n" +
                "maximum: " + formatFlex(value) + "\n";
    }
}
