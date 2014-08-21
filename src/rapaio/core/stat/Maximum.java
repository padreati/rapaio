/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.core.stat;

import rapaio.core.Printable;
import rapaio.data.Var;
import rapaio.data.stream.VSpot;

/**
 * Finds the maximum value from a {@link rapaio.data.Var} of values.
 * <p>
 * Ignores missing elements.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:39 PM
 */
@Deprecated
public class Maximum implements Printable {

    private final Var var;
    private final double value;

    public Maximum(Var var) {
        this.var = var;
        this.value = compute();
    }

    private double compute() {
        if (var.stream().anyMatch((VSpot inst) -> !inst.missing())) {
            return var.stream().complete().mapToDouble().count();
        }
        return Double.NaN;
    }

    public double value() {
        return value;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append(String.format("> maximum\n%.10f\n", value));
    }
}
