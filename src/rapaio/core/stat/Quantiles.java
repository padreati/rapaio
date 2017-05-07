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

import rapaio.core.stat.QuantilesEstimator.Type;


/**
 * Estimates quantiles from a numerical {@link rapaio.data.Var} of values.
 * <p>
 * The estimated quantiles implements two version of the algorithms:
 * R-7, Excel, SciPy-(1,1), Maple-6
 * R-8, SciPy-(1/3,1/3) version of estimating quantiles.
 * <p>
 * Default type is R-7, but is can be changed.
 * <p>
 * <p>
 * For further reference see:
 * http://en.wikipedia.org/wiki/Quantile
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Quantiles implements Printable {

    public static Quantiles from(Var var, double...percentiles) {
        return new Quantiles(var, Type.R7, percentiles);
    }

    public static Quantiles from(Var var, Type type, double...percentiles) {
        return new Quantiles(var, type, percentiles);
    }

    private final String varName;
    private final double[] percentiles;
    private final double[] quantiles;
    private int completeCount;
    private int missingCount;
    private QuantilesEstimator quantilesEstimator;
    
    private Quantiles(Var var, Type type, double... percentiles) {
        this.varName = var.name();
        this.percentiles = percentiles;
        this.quantilesEstimator = QuantilesEstimator.newInstance(type);
        this.quantiles = compute(var);
    }

    private double[] compute(final Var var) {
        Var complete = var.stream().complete().toMappedVar();
        missingCount = var.rowCount() - complete.rowCount();
        completeCount = complete.rowCount();
        return quantilesEstimator.estimate(complete,  percentiles);
    }

    public double[] values() {
        return quantiles;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n > quantiles[%s] - estimated quantiles\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        for (int i = 0; i < quantiles.length; i++) {
            sb.append(String.format("quantile[%s] = %s\n", formatFlex(percentiles[i]), formatFlex(quantiles[i])));
        }
        return sb.toString();
    }


}
