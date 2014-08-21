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

import static rapaio.core.MathBase.validNumber;

/**
 * Computes the sum of elements for a {@link rapaio.data.Var} of values.
 * <p>
 * Ignore invalid numeric values. See {@link rapaio.core.MathBase#validNumber(double)}.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class Sum implements Printable {

    private final Var var;
    private final double value;

    public Sum(Var var) {
        this.var = var;
        this.value = compute();
    }

    private double compute() {
        double sum = 0;
        for (int i = 0; i < var.rowCount(); i++) {
            if (validNumber(var.value(i))) {
                sum += var.value(i);
            }
        }
        return sum;
    }

    public double value() {
        return value;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append(String.format("> sum\n%.10f\n", value));
    }
}