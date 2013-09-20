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

package rapaio.distributions.empirical;

import static rapaio.core.BaseMath.pow;
import static rapaio.core.BaseMath.sqrt;
import rapaio.data.RowComparators;
import rapaio.data.Vector;
import rapaio.distributions.Distribution;
import static rapaio.filters.RowFilters.sort;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Discrete empirical distribution obtained from data.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Empirical extends Distribution {

    private final double[] values;
    private final int[] freq;
    private final double min;
    private final double max;
    private final double mean;
    private final double mode;
    private final double var;
    private final double skewness;
    private final double kurtosis;

    public Empirical(Vector vector) {
        if (vector.getRowCount() == 0) {
            throw new IllegalArgumentException("Input vector can't be empty.");
        }
        Vector sort = sort(vector, true);
        Comparator<Integer> comp = RowComparators.numericComparator(sort, true);
        int len = 1;
        for (int i = 1; i < sort.getRowCount(); i++) {
            if (comp.compare(i, i - 1) != 0) {
                len++;
            }
        }
        values = new double[len];
        freq = new int[len];
        int pos = 0;
        values[0] = sort.getValue(0);
        freq[0]++;
        for (int i = 1; i < sort.getRowCount(); i++) {
            if (comp.compare(i, i - 1) == 0) {
                freq[pos]++;
            } else {
                pos++;
                values[pos] = sort.getValue(i);
                freq[pos] = freq[pos - 1] + 1;
            }
        }

        // compute other stuff

        // mean 
        double total = values[0] * freq[0];
        for (int i = 1; i < values.length; i++) {
            total += values[i] * (freq[i] - freq[i - 1]);
        }
        mean = total / (1. * freq[freq.length - 1]);

        // mode
        int index = 0;
        for (int i = 1; i < freq.length; i++) {
            if (freq[index] < freq[i]) {
                index = i;
            }
        }
        mode = values[index];

        // min
        min = values[0];

        // max
        max = values[values.length - 1];

        // variance 
        total = freq[0] * pow(values[0] - mean, 2);
        for (int i = 1; i < values.length; i++) {
            total += (freq[i] - freq[i - 1]) * pow(values[i] - mean, 2);
        }
        var = total / (1. * (freq[freq.length - 1]));

        // skewness
        total = freq[0] * pow(values[0] - mean, 3);
        for (int i = 1; i < values.length; i++) {
            total += (freq[i] - freq[i - 1]) * pow(values[i] - mean, 3);
        }
        skewness = total / (pow(sqrt(var), 3));

        // kurtosis
        total = freq[0] * pow(values[0] - mean, 4);
        for (int i = 1; i < values.length; i++) {
            total += (freq[i] - freq[i - 1]) * pow(values[i] - mean, 4);
        }
        kurtosis = total / pow(var, 2) - 3;
    }

    @Override
    public String getName() {
        return "Empirical Distribution";
    }

    @Override
    public double pdf(double x) {
        final int index = Arrays.binarySearch(values, x);
        if (index < 0) {
            return 0;
        }
        if (index == 0) {
            return freq[0] / (1. * freq[freq.length - 1]);
        }
        return (freq[index] - freq[index - 1]) / (1. * freq[freq.length - 1]);
    }

    @Override
    public double cdf(double x) {
        int index = Arrays.binarySearch(values, x);
        if (index < 0) {
            index = -index - 1;
        }
        if (index == 0) {
            return 0;
        }
        return freq[index] / (1. * freq[freq.length - 1]);
    }

    @Override
    public double quantile(double p) {
        int index = (int) Math.round(p * (freq.length - 1));
        return values[index];
    }

    @Override
    public double min() {
        return min;
    }

    @Override
    public double max() {
        return max;
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double mode() {
        return mode;
    }

    @Override
    public double variance() {
        return var;
    }

    @Override
    public double skewness() {
        return skewness;
    }

    @Override
    public double kurtosis() {
        return kurtosis;
    }
}
