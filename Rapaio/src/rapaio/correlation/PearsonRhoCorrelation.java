/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.correlation;

import rapaio.core.Summarizable;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Vector;

import static rapaio.core.BaseMath.max;
import static rapaio.core.BaseMath.sqrt;

/**
 * /**
 * Sample Pearson product-moment correlation coefficient.
 * <p/>
 * See
 * http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class PearsonRhoCorrelation implements Summarizable {
    private final Vector x;
    private final Vector y;
    private final double rho;

    public PearsonRhoCorrelation(Vector x, Vector y) {
        this.x = x;
        this.y = y;
        this.rho = compute();
    }

    private double compute() {
        double xMean = new Mean(x).getValue();
        double yMean = new Mean(y).getValue();
        double sum = 0;
        int len = max(x.getRowCount(), y.getRowCount());
        double sdp = sqrt(new Variance(x).getValue()) * sqrt(new Variance(y).getValue());
        double count = 0;
        for (int i = 0; i < len; i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            sum += ((x.getValue(i) - xMean) * (y.getValue(i) - yMean));
            count++;
        }
        return sum / (sdp * (count - 1));
    }

    public double value() {
        return rho;
    }

    @Override
    public String summary() {
        return String.format(
                "pearson[\"%s\",\"%s\"] - Pearson product-moment correlation coefficient\n%.10f",
                x.getName(), y.getName(), rho);
    }

}
