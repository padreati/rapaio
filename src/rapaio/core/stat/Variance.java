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
import rapaio.data.Vector;

import static rapaio.core.MathBase.pow;

/**
 * Compensated version of the algorithm for calculation of
 * sample variance of values from a {@link rapaio.data.Vector}.
 * <p>
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:26 PM
 */
public class Variance implements Printable {

    private final Vector vector;
    private final double value;

    public Variance(Vector vector) {
        this.vector = vector;
        this.value = compute();
    }

    private double compute() {
        double mean = new Mean(vector).getValue();
        double n = 0;
        for (int i = 0; i < vector.rowCount(); i++) {
            if (vector.isMissing(i)) {
                continue;
            }
            n++;
        }
        if (n == 0) {
            return Double.NaN;
        }
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < vector.rowCount(); i++) {
            if (vector.isMissing(i)) {
                continue;
            }
            sum2 += pow(vector.getValue(i) - mean, 2);
            sum3 += vector.getValue(i) - mean;
        }
        return (sum2 - pow(sum3, 2) / n) / (n - 1);

    }

    public double getValue() {
        return value;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append(String.format("> variance\n%.10f", value));
    }

}
