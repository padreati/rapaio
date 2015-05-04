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

import rapaio.core.Printable;
import rapaio.data.Var;
import rapaio.printer.Printer;

/**
 * Compute covariance of two variables
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/24/15.
 */
@Deprecated
public class Covariance implements Printable {

    private final String varName1;
    private final String varName2;
    private final double value;

    public Covariance(Var var1, Var var2) {
        this.varName1 = var1.name();
        this.varName2 = var2.name();
        this.value = compute(var1, var2);
    }

    private double compute(final Var var1, final Var var2) {

        double m1 = var1.value(0);
        double m2 = var2.value(0);
        double c = 0;

        for (int i = 1; i < Math.min(var1.rowCount(), var2.rowCount()); i++) {
            double n = i + 1;
            double m1_new = m1 + (var1.value(i) - m1) / n;
            double m2_new = m2 + (var2.value(i) - m2) / n;
            c += (var1.value(i) - m1_new) * (var2.value(i) - m2);
            m1 = m1_new;
            m2 = m2_new;
        }
        return c;
    }

    public double value() {
        return value;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append(String.format("> cov[%s,%s]\n%s",
                varName1, varName2,
                Printer.formatDecLong.format(value)));
    }

    public double sdValue() {
        return Math.sqrt(value);
    }
}
