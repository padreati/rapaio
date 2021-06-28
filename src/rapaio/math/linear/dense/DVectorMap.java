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

package rapaio.math.linear.dense;

import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.VType;
import rapaio.math.linear.base.AbstractDVector;
import rapaio.util.function.Double2DoubleFunction;

import java.io.Serial;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/28/21.
 */
public class DVectorMap extends AbstractDVector {

    @Serial
    private static final long serialVersionUID = -1952913189054878826L;
    private final DVector source;
    private final int[] indexes;

    public DVectorMap(DVector source, int... indexes) {
        this.source = source;
        this.indexes = indexes;
    }

    @Override
    public VType type() {
        return VType.MAP;
    }

    @Override
    public int size() {
        return indexes.length;
    }

    @Override
    public double get(int i) {
        return source.get(indexes[i]);
    }

    @Override
    public void set(int i, double value) {
        source.set(indexes[i], value);
    }

    @Override
    public void inc(int i, double value) {
        source.inc(indexes[i], value);
    }

    @Override
    public DVectorDense mapCopy(int... idxs) {
        double[] copy = new double[idxs.length];
        for (int i = 0; i < idxs.length; i++) {
            copy[i] = source.get(indexes[idxs[i]]);
        }
        return new DVectorDense(copy.length, copy);
    }

    @Override
    public DVectorDense copy() {
        double[] copy = new double[indexes.length];
        int pos = 0;
        for (int i : indexes) {
            copy[pos++] = get(indexes[i]);
        }
        return new DVectorDense(copy.length, copy);
    }

    @Override
    public DVector add(double x) {
        for (int i = 0; i < size(); i++) {
            set(i, get(i) + x);
        }
        return this;
    }

    @Override
    public DVector add(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            set(i, get(i) + b.get(i));
        }
        return this;
    }

    @Override
    public DVector sub(double x) {
        for (int i = 0; i < size(); i++) {
            set(i, get(i) - x);
        }
        return this;
    }

    @Override
    public DVector sub(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            set(i, get(i) - b.get(i));
        }
        return this;
    }

    @Override
    public DVector mult(double scalar) {
        for (int i = 0; i < size(); i++) {
            set(i, get(i) * scalar);
        }
        return this;
    }

    @Override
    public DVector mult(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            set(i, get(i) * b.get(i));
        }
        return this;
    }

    @Override
    public DVector div(double scalar) {
        for (int i = 0; i < size(); i++) {
            set(i, get(i) / scalar);
        }
        return this;
    }

    @Override
    public DVector div(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            set(i, get(i) / b.get(i));
        }
        return this;
    }

    @Override
    public DVector axpyCopy(double a, DVector y) {
        checkConformance(y);
        double[] copy = new double[size()];
        for (int i = 0; i < size(); i++) {
            copy[i] = a * get(i) + y.get(i);
        }
        return new DVectorDense(copy.length, copy);
    }

    @Override
    public double dotBilinear(DMatrix m, DVector y) {
        if (m.rowCount() != size() || m.colCount() != y.size()) {
            throw new IllegalArgumentException("Bilinear matrix and vector are not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                sum += get(i) * m.get(i, j) * y.get(j);
            }
        }
        return sum;
    }

    @Override
    public double dotBilinear(DMatrix m) {
        if (m.rowCount() != size() || m.colCount() != size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < size(); j++) {
                sum += get(i) * m.get(i, j) * get(j);
            }
        }
        return sum;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        double s = 0;
        for (int i = 0; i < size(); i++) {
            s = Math.fma(get(i), b.get(i), s);
        }
        return s;
    }

    public double norm(double p) {
        if (p <= 0) {
            return size();
        }
        if (p == Double.POSITIVE_INFINITY) {
            double max = Double.NaN;
            for (int i = 0; i < size(); i++) {
                double value = get(i);
                if (Double.isNaN(max)) {
                    max = value;
                } else {
                    max = Math.max(max, value);
                }
            }
            return max;
        }

        double s = 0.0;
        for (int i = 0; i < size(); i++) {
            s += Math.pow(Math.abs(get(i)), p);
        }
        return Math.pow(s, 1.0 / p);
    }

    @Override
    public double sum() {
        double sum = 0;
        for (int i : indexes) {
            sum += source.get(i);
        }
        return sum;
    }

    @Override
    public double nansum() {
        double nansum = 0;
        for (int i : indexes) {
            double value = source.get(i);
            if (Double.isNaN(value)) {
                continue;
            }
            nansum += value;
        }
        return nansum;
    }

    @Override
    public DVector cumsum() {
        for (int i = 1; i < size(); i++) {
            inc(i, get(i - 1));
        }
        return this;
    }

    @Override
    public double prod() {
        double prod = 1;
        for (int i = 0; i < size(); i++) {
            prod *= get(i);
        }
        return prod;
    }

    @Override
    public double nanprod() {
        double nanprod = 1;
        for (int i = 0; i < size(); i++) {
            double value = get(i);
            if (Double.isNaN(value)) {
                continue;
            }
            nanprod *= value;
        }
        return nanprod;
    }

    @Override
    public DVector cumprod() {
        for (int i = 1; i < size(); i++) {
            set(i, get(i - 1) * get(i));
        }
        return this;
    }

    @Override
    public int nancount() {
        int nancount = 0;
        for (int i = 0; i < size(); i++) {
            double value = get(i);
            if (Double.isNaN(value)) {
                continue;
            }
            nancount++;
        }
        return nancount;
    }

    @Override
    public double mean() {
        return sum() / size();
    }

    @Override
    public double nanmean() {
        return nansum() / nancount();
    }

    @Override
    public double variance() {
        if (size() == 0) {
            return Double.NaN;
        }
        double mean = mean();
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < size(); i++) {
            sum2 += Math.pow(get(i) - mean, 2);
            sum3 += get(i) - mean;
        }
        return (sum2 - Math.pow(sum3, 2) / size()) / (size() - 1.0);
    }

    @Override
    public double nanvariance() {
        double mean = nanmean();
        int missingCount = 0;
        int completeCount = 0;
        for (int i = 0; i < size(); i++) {
            if (Double.isNaN(get(i))) {
                missingCount++;
            } else {
                completeCount++;
            }
        }
        if (completeCount == 0) {
            return Double.NaN;
        }
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < size(); i++) {
            if (Double.isNaN(get(i))) {
                continue;
            }
            sum2 += Math.pow(get(i) - mean, 2);
            sum3 += get(i) - mean;
        }
        return (sum2 - Math.pow(sum3, 2) / completeCount) / (completeCount - 1.0);
    }

    @Override
    public DVector apply(Double2DoubleFunction f) {
        for (int i = 0; i < size(); i++) {
            set(i, f.applyAsDouble(get(i)));
        }
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.of(indexes).mapToDouble(i -> source.get(indexes[i]));
    }

    @Override
    public VarDouble asVarDouble() {
        double[] copy = new double[indexes.length];
        int pos = 0;
        for (int i : indexes) {
            copy[pos++] = source.get(indexes[i]);
        }
        return VarDouble.wrapArray(copy.length, copy);
    }
}
