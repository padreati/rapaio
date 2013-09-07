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

import static rapaio.core.BaseFilters.sort;
import static rapaio.core.BaseMath.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class BaseStat {

    private BaseStat() {
    }

    /**
     * Compensated version of arithmetic mean of values from a {@code Vector}.
     *
     * @param v vector of values
     * @return the mean function of values from vector v
     */
    public static BaseStatOneValueResult mean(Vector v) {
        double sum = 0.;
        double count = 0;
        for (int i = 0; i < v.getRowCount(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            sum += v.getValue(i);
            count++;
        }
        if (count == 0) {
            return new BaseStatOneValueResult(v, Double.NaN, "mean[\"%s\"]\n%.10f") {
            };
        }
        sum /= count;
        double t = 0;
        for (int i = 0; i < v.getRowCount(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            t += v.getValue(i) - sum;
        }
        sum += t / count;
        return new BaseStatOneValueResult(v, sum, "mean[\"%s\"]\n%.10f");
    }

    /**
     * Compensated version of the algorithm for calculation of
     * sample variance of values from a {@link Vector}.
     *
     * @param v vector of values
     * @return the variance function of values from vector v
     */
    public static BaseStatOneValueResult variance(Vector v) {
        double mean = mean(v).value();
        double count = 0;
        for (int i = 0; i < v.getRowCount(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            count++;
        }
        if (count == 0) {
            return new BaseStatOneValueResult(v, Double.NaN, "variance[\"%s\"]\n%.10f");
        }
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < v.getRowCount(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            sum2 += pow(v.getValue(i) - mean, 2);
            sum3 += v.getValue(i) - mean;
        }
        return new BaseStatOneValueResult(v, (sum2 - pow(sum3, 2) / count) / (count - 1), "variance[\"%s\"]\n%.10f");
    }

    /**
     * Returns the minimum of valid number values from a {@link Vector}.
     *
     * @param v vector of values
     * @return the minimum of values from vector v
     */
    public static BaseStatOneValueResult minimum(Vector v) {
        double min = Double.MAX_VALUE;
        boolean valid = false;
        for (int i = 0; i < v.getRowCount(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            valid = true;
            min = BaseMath.min(min, v.getValue(i));
        }
        return new BaseStatOneValueResult(v, valid ? min : Double.NaN, "minimum[%s]\n%.10f");
    }

    /**
     * Returns the maximum of valid number values from a {@link Vector}.
     *
     * @param v vector of values
     * @return the maximum of valid values from vector v
     */
    public static BaseStatOneValueResult maximum(Vector v) {
        double max = Double.MIN_VALUE;
        boolean valid = false;
        for (int i = 0; i < v.getRowCount(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            max = BaseMath.max(max, v.getValue(i));
            valid = true;
        }
        return new BaseStatOneValueResult(v, valid ? max : Double.NaN, "minimum[%s]\n%.10f");
    }

    /**
     * Returns the sum of all valid numbers from a {@link Vector},
     *
     * @param v vector of values
     * @return the sum function of valid values from vector v
     */
    public static BaseStatOneValueResult sum(Vector v) {
        double sum = 0;
        for (int i = 0; i < v.getRowCount(); i++) {
            if (validNumber(v.getValue(i))) {
                sum += v.getValue(i);
            }
        }
        return new BaseStatOneValueResult(v, sum, "sum[\"%s\"]\n%.10f");
    }

    /**
     * Estimates quantiles from a numerical {@link Vector} of values.
     * <p/>
     * The estimated quantiles implements R-8, SciPy-(1/3,1/3) version of estimating quantiles.
     * <p/>
     * For further reference see:
     * http://en.wikipedia.org/wiki/Quantile
     *
     * @param vector      numerical sample as a {@link Vector} of values
     * @param percentiles percentiles for which quantiles will be produced.
     * @return an array of quantile estimates
     */
    public static QuantilesResult quantiles(Vector vector, double[] percentiles) {
        Vector sorted = sort(vector);
        int start = 0;
        while (sorted.isMissing(start)) {
            start++;
            if (start == sorted.getRowCount()) {
                break;
            }
        }
        double[] values = new double[percentiles.length];
        if (start == sorted.getRowCount()) {
            return new QuantilesResult(vector, percentiles, values);
        }
        for (int i = 0; i < percentiles.length; i++) {
            int N = sorted.getRowCount() - start;
            double h = (N + 1. / 3.) * percentiles[i] + 1. / 3.;
            int hfloor = (int) floor(h);

            if (percentiles[i] < (2. / 3.) / (N + 1. / 3.)) {
                values[i] = sorted.getValue(start);
                continue;
            }
            if (percentiles[i] >= (N - 1. / 3.) / (N + 1. / 3.)) {
                values[i] = sorted.getValue(sorted.getRowCount() - 1);
                continue;
            }
            values[i] = sorted.getValue(start + hfloor - 1)
                    + (h - hfloor) * (sorted.getValue(start + hfloor) - sorted.getValue(start + hfloor - 1));
        }
        return new QuantilesResult(vector, percentiles, values);
    }

    public static class BaseStatOneValueResult implements Summarizable {

        private final double value;
        private final Vector vector;
        private final String format;

        public BaseStatOneValueResult(Vector vector, double value, String format) {
            this.vector = vector;
            this.value = value;
            this.format = format;
        }

        public double value() {
            return value;
        }

        @Override
        public String summary() {
            return String.format(format, vector.getName(), value);
        }
    }

    public static class QuantilesResult implements Summarizable {
        private final Vector vector;
        private final double[] perc;
        private final double[] quantiles;

        public QuantilesResult(Vector vector, double[] perc, double[] quantiles) {
            this.vector = vector;
            this.perc = perc;
            this.quantiles = quantiles;
        }

        public double[] value() {
            return quantiles;
        }

        @Override
        public String summary() {
            StringBuilder sb = new StringBuilder();
            sb.append("quantiles[\"").append(vector.getName()).append("\", ...] - estimated quantiles").append("\n");
            for (int i = 0; i < quantiles.length; i++) {
                sb.append(String.format("quantile[\"%s\",%f = %f\n", vector.getName(), perc[i], quantiles[i]));
            }
            return sb.toString();
        }
    }
}
