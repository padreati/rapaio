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
import rapaio.util.Pin;

/**
 * Compensated version of arithmetic mean of values from a {@code Vector}.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:21 PM
 */
public final class Mean implements Printable {

    private final String varName;
    private final double value;

    public Mean(Var var) {
        this.varName = var.name();
        this.value = compute(var);
    }

    private double compute(final Var var) {
        final double count = var.stream().complete().mapToDouble().count();
        if (count == 0) {
            return Double.NaN;
        }
        final double sum = var.stream().complete().mapToDouble().sum() / count;
        final Pin<Double> t = new Pin<>(0.0);
        var.stream().complete().forEach(s -> t.set(t.get() + s.value() - sum));
        return sum + t.get() / count;
    }

    /**
     * Returns the computed mean of the vector
     * @return computed mean
     */
    public double value() {
        return value;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append(String.format("> mean['%s']\n%.10f\n", varName, value));
    }
}
