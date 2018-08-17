/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

import static rapaio.sys.WS.formatFlex;
import static rapaio.core.CoreTools.mean;

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

    public static Variance from(Var var) {
        return new Variance(var);
    }

    private final String varName;
    private double value;
    private double biasedValue;
    private int completeCount;
    private int missingCount;

    private Variance(Var var) {
        this.varName = var.name();
        compute(var);
    }

    private void compute(final Var var) {
        double mean = mean(var).value();
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.isMissing(i)) {
                missingCount++;
            } else {
                completeCount++;
            }
        }
        if (completeCount == 0) {
            value = Double.NaN;
            biasedValue = Double.NaN;
        }
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.isMissing(i)) {
                continue;
            }
            sum2 += Math.pow(var.getDouble(i) - mean, 2);
            sum3 += var.getDouble(i) - mean;
        }
        value = (sum2 - Math.pow(sum3, 2) / (1.0 * completeCount)) / (completeCount - 1.0);
        biasedValue = (sum2 - Math.pow(sum3, 2) / (1.0 * completeCount)) / (1.0 * completeCount);
    }

    public double value() {
        return value;
    }

    public double biasedValue() {
        return biasedValue;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n> variance[%s]\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n",
                completeCount() + missingCount(), completeCount(), missingCount()));
        sb.append(String.format("variance: %s\n", formatFlex(value)));
        sb.append(String.format("sd: %s\n", formatFlex(sdValue())));
        return sb.toString();
    }

    public double sdValue() {
        return Math.sqrt(value);
    }

    public double biasedSdValue() {
        return Math.sqrt(biasedValue);
    }

    public int completeCount() {
        return completeCount;
    }

    public int missingCount() {
        return missingCount;
    }
}
