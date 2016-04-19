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
 * Core tool which computes geometric mean for a given numerical variable.
 * Geometric mean is not defined for negative values, as such, when negative values
 * are encountered in vector the {@link Double#NaN} value is returned and
 * {@link #isDefined()} will return true. The geometric mean is also undefined
 * when all the available values are negative or missing.
 *
 * The geometric mean is defined as the n-th square root of the product of
 * all the values from the variable. The algorithm implied below uses
 * the the logarithmic approach. As such the geometric mean is computed as
 * \exp(\frac{1}{n}\sum{log{x_i}}).
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/7/15.
 */
public class GeometricMean implements Printable {

    public static GeometricMean from(Var var) {
        return new GeometricMean(var);
    }

    private final String varName;
    private final double value;
    private int missingCount = 0;
    private int completeCount = 0;
    private int negativeCount = 0;

    private GeometricMean(Var var) {
        this.varName = var.name();
        double sum = 0.0;
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.missing(i)) {
                missingCount++;
            } else {
                completeCount++;
                sum += Math.log(var.value(i));
            }
            if (var.value(i) < 0)
                negativeCount++;
        }
        if (negativeCount > 0 || completeCount == 0) {
            this.value = Double.NaN;
            return;
        }
        this.value = Math.exp(sum / completeCount);
    }

    /**
     * Returns the computed mean of the vector
     *
     * @return computed mean
     */
    public double value() {
        return value;
    }

    /**
     * @return true if the geometric mean is defined, false if there are negative elements
     * or all elements are missing
     */
    public boolean isDefined() {
        return negativeCount == 0;
    }

    @Override
    public String summary() {
        return "\n" +
                "> geometricMean[" + varName + "]\n" +
                "total rows: " + (completeCount + missingCount) + " (complete: " + completeCount + ", missing: " + missingCount + ", negative values: " + negativeCount + ")\n" +
                "mean: " + formatFlex(value) + "\n";
    }
}
