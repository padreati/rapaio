/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import static rapaio.printer.Format.floatFlex;

/**
 * Core tool which computes geometric mean for a given numerical variable.
 * Geometric mean is not defined for negative values, as such, when negative values
 * are encountered in vector the {@link Double#NaN} value is returned and
 * {@link #isDefined()} will return true. The geometric mean is also undefined
 * when all the available values are negative or missing.
 * <p>
 * The geometric mean is defined as the n-th square root of the product of
 * all the values from the variable. The algorithm implied below uses
 * the the logarithmic approach. As such the geometric mean is computed as
 * \exp(\frac{1}{n}\sum{log{x_i}}).
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/7/15.
 */
public class GeometricMean implements Printable {

    public static GeometricMean of(Var var) {
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
        for (int i = 0; i < var.size(); i++) {
            if (var.isMissing(i)) {
                missingCount++;
            } else {
                completeCount++;
                sum += Math.log(var.getDouble(i));
            }
            if (var.getDouble(i) < 0)
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
    public String toString() {
        return "geometricMean[" + varName + "] = " + floatFlex(value);
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return "> geometricMean[" + varName + "]\n" +
                "total rows: " + (completeCount + missingCount) + " (complete: " + completeCount + ", missing: " + missingCount + ", negative values: " + negativeCount + ")\n" +
                "mean: " + floatFlex(value) + "\n";
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toContent(printer, options);
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        return toContent(printer, options);
    }
}
