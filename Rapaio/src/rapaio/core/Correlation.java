/*
 * Copyright 2013 Aurelian Tutuianu
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

package rapaio.core;

import rapaio.data.Vector;

import static rapaio.core.BaseMath.max;
import static rapaio.core.BaseMath.sqrt;
import static rapaio.core.BaseStat.mean;
import static rapaio.core.BaseStat.variance;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class Correlation {

    /**
     * Sample Pearson product-moment correlation coefficient.
     * <p/>
     * See
     * http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient
     *
     * @param x first set of values
     * @param y second set of values
     * @return Pearson Rho coefficient
     */
    public static PearsonRhoResult pearsonRho(Vector x, Vector y) {
        double xmean = mean(x).value();
        double ymean = mean(y).value();
        double sum = 0;
        int len = max(x.getRowCount(), y.getRowCount());
        double sdp = sqrt(variance(x).value()) * sqrt(variance(y).value());
        double count = 0;
        for (int i = 0; i < len; i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            sum += ((x.getValue(i) - xmean) * (y.getValue(i) - ymean));
            count++;
        }
        return new PearsonRhoResult(x, y, sum / (sdp * (count - 1)));
    }

    public static final class PearsonRhoResult implements Summarizable {
        private final Vector x;
        private final Vector y;
        private final double rho;

        public PearsonRhoResult(Vector x, Vector y, double rho) {
            this.x = x;
            this.y = y;
            this.rho = rho;
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
}
