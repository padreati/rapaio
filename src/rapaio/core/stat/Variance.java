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

    private final Var var;
    private final double value;

    public Variance(Var var) {
        this.var = var;
        this.value = compute();
    }

    private double compute() {
        double mean = new Mean(var).getValue();
        double n = 0;
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i)) {
                continue;
            }
            n++;
        }
        if (n == 0) {
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
        return (sum2 - Math.pow(sum3, 2) / n) / (n - 1);

    }

    public double getValue() {
        return value;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append(String.format("> variance\n%.10f", value));
    }

}
