/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear.dense;

import java.io.Serial;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.DoubleStream;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;
import rapaio.core.distributions.Distribution;
import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.storage.DVectorStore;
import rapaio.math.linear.dense.storage.DVectorStoreDense;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.DoubleComparator;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;

public class DVectorDense extends AbstractStoreDVector {

    public static DVectorDense empty(int n) {
        return new DVectorDense(0, n, new double[n]);
    }

    public static DVectorDense fill(int n, double fill) {
        return new DVectorDense(0, n, DoubleArrays.newFill(n, fill));
    }

    public static DVectorDense wrap(int offset, int size, double[] values) {
        return new DVectorDense(offset, size, values);
    }

    public static DVectorDense random(int size, Distribution distribution) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = distribution.sampleNext();
        }
        return new DVectorDense(0, array.length, array);
    }

    @Serial
    private static final long serialVersionUID = 5763094452899116225L;
    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    private final DVectorStoreDense store;

    public DVectorDense(int offset, int size, double[] array) {
        this.store = new DVectorStoreDense(offset, size, array);
    }

    @Override
    public DVectorStore store() {
        return store;
    }

    @Override
    public int size() {
        return store.size;
    }

    @Override
    public DVector map(int[] indexes, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.copyByIndex(store.array, store.offset, indexes);
            return new DVectorDense(0, copy.length, copy);
        } else {
            return new DVectorMap(store.offset, indexes, store.array);
        }
    }

    public DVectorDense copy() {
        return new DVectorDense(0, store.size, Arrays.copyOfRange(store.array, store.offset, store.offset + store.size));
    }

    @Override
    public DVector fill(double value) {
        Arrays.fill(store.array, store.offset, store.offset + store.size, value);
        return this;
    }

    @Override
    public DVector add(double x, AlgebraOption<?>... opts) {
        if(AlgebraOptions.from(opts).isCopy()) {
            DVectorDense copy = new DVectorDense(0, store.size(), store.solidArrayCopy());
            copy.store.add(x);
            return copy;
        }
        store.add(x);
        return this;
    }

    @Override
    public DVectorDense add(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[store.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.addTo(store.array, store.offset, bd.store.array, bd.store.offset, copy, 0, store.size);
            } else {
                for (int i = 0; i < store.size; i++) {
                    copy[i] = store.array[store.offset + i] + b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.add(store.array, store.offset, bd.store.array, bd.store.offset, store.size);
            return this;
        }
        for (int i = 0; i < store.size; i++) {
            store.array[store.offset + i] += b.get(i);
        }
        return this;
    }


    @Override
    public DVector sub(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[store.size];
            DoubleArrays.subTo(store.array, store.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.sub(store.array, store.offset, x, store.size);
        return this;
    }

    @Override
    public DVectorDense sub(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[store.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.subTo(store.array, store.offset, bd.store.array, bd.store.offset, copy, 0, store.size);
            } else {
                for (int i = 0; i < store.size; i++) {
                    copy[i] = store.array[store.offset + i] - b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.sub(store.array, store.offset, bd.store.array, bd.store.offset, store.size);
            return this;
        }
        for (int i = 0; i < store.size; i++) {
            store.array[store.offset + i] -= b.get(i);
        }
        return this;
    }

    @Override
    public DVector mul(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[store.size];
            DoubleArrays.multTo(store.array, store.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.mul(store.array, store.offset, x, store.size);
        return this;
    }

    @Override
    public DVector mul(DVector b, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[store.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.multTo(store.array, store.offset, bd.store.array, bd.store.offset, copy, 0, store.size);
            } else {
                for (int i = 0; i < store.size; i++) {
                    copy[i] = store.array[store.offset + i] * b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            DoubleArrays.mul(store.array, store.offset, bd.store.array, bd.store.offset, store.size);
            return this;
        }
        for (int i = 0; i < store.size; i++) {
            store.array[store.offset + i] *= b.get(i);
        }
        return this;
    }

    @Override
    public DVector div(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[store.size];
            DoubleArrays.divTo(store.array, store.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.div(store.array, store.offset, x, store.size);
        return this;
    }

    @Override
    public DVector div(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[store.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.divTo(store.array, store.offset, bd.store.array, bd.store.offset, copy, 0, store.size);
            } else {
                for (int i = 0; i < store.size; i++) {
                    copy[i] = store.array[store.offset + i] / b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.div(store.array, store.offset, bd.store.array, bd.store.offset, store.size);
            return this;
        }
        for (int i = 0; i < store.size; i++) {
            store.array[store.offset + i] /= b.get(i);
        }
        return this;
    }

    @Override
    public DVector addMul(double a, DVector y, AlgebraOption<?>... opts) {
        checkConformance(y);
        if (AlgebraOptions.from(opts).isCopy()) {
            if (y instanceof DVectorDense yd) {
                double[] copy = new double[store.size];
                DoubleArrays.addMulTo(store.array, store.offset, a, yd.store.array, yd.store.offset, copy, 0, store.size);
                return new DVectorDense(0, copy.length, copy);
            }
            double[] copy = new double[store.size];
            for (int i = 0; i < size(); i++) {
                copy[i] = store.array[store.offset + i] + a * y.get(i);
            }
            return new DVectorDense(0, copy.length, copy);
        }
        if (y instanceof DVectorDense yd) {
            int bound = SPECIES.loopBound(store.size);
            int i = 0;
            var va = DoubleVector.broadcast(SPECIES, a);
            for (; i < bound; i += SPECIES.length()) {
                var vs = store.loadVector(i);
                var vy = yd.store.loadVector(i).fma(va, vs);
                store.storeVector(vy, i);
            }
            for (; i < store.size; i++) {
                inc(i, yd.get(i) * a);
            }
            return this;
        }
        for (int i = 0; i < store.size; i++) {
            store.array[store.offset + i] += a * y.get(i);
        }
        return this;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            double ss = 0;
            for (int i = 0; i < store.size; i++) {
                ss = Math.fma(store.array[store.offset + i], bd.store.array[bd.store.offset + i], ss);
            }
            return ss;
        }
        double ss = 0;
        for (int i = 0; i < store.size; i++) {
            ss = Math.fma(store.array[store.offset + i], b.get(i), ss);
        }
        return ss;
    }

    @Override
    public double dotBilinear(DMatrix m, DVector y) {
        if (m.rowCount() != store.size || m.colCount() != y.size()) {
            throw new IllegalArgumentException("Bilinear matrix and vector are not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < store.size; i++) {
            for (int j = 0; j < y.size(); j++) {
                sum += store.array[store.offset + i] * m.get(i, j) * y.get(j);
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
                sum += store.array[store.offset + i] * m.get(i, j) * store.array[store.offset + j];
            }
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DVector m, DVector y) {
        if (m.size() != size() || m.size() != y.size()) {
            throw new IllegalArgumentException("Bilinear diagonal vector is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += store.array[store.offset + i] * m.get(i) * y.get(i);
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DMatrix m) {
        if (m.rowCount() != size() || m.colCount() != size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += store.array[store.offset + i] * m.get(i, i) * store.array[store.offset + i];
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DMatrix m, DVector y) {
        if (m.rowCount() != size() || m.colCount() != y.size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += store.array[store.offset + i] * m.get(i, i) * y.get(i);
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DVector m) {
        if (m.size() != size() || m.size() != size()) {
            throw new IllegalArgumentException("Bilinear diagonal vector is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += store.array[store.offset + i] * m.get(i) * store.array[store.offset + i];
        }
        return sum;
    }

    @Override
    public double pnorm(double p) {
        if (p <= 0) {
            return store.size;
        }
        if (p == Double.POSITIVE_INFINITY) {
            return max();
        }
        double sum = 0.0;
        for (int i = store.offset; i < store.offset + store.size; i++) {
            sum += Math.pow(Math.abs(store.array[i]), p);
        }
        return Math.pow(sum, 1.0 / p);
    }

    @Override
    public double sum() {
        return store.sum();
    }

    @Override
    public DVector cumsum() {
        for (int i = store.offset + 1; i < store.offset + store.size; i++) {
            store.array[i] += store.array[i - 1];
        }
        return this;
    }

    @Override
    public double nanprod() {
        return DoubleArrays.nanProd(store.array, store.offset, store.size);
    }

    @Override
    public DVector cumprod() {
        for (int i = store.offset + 1; i < store.offset + store.size; i++) {
            store.array[i] = store.array[i - 1] * store.array[i];
        }
        return this;
    }

    @Override
    public int nancount() {
        return DoubleArrays.nanCount(store.array, store.offset, store.size);
    }

    @Override
    public double mean() {
        return DoubleArrays.mean(store.array, store.offset, store.size);
    }

    @Override
    public double nanmean() {
        return DoubleArrays.nanMean(store.array, store.offset, store.size);
    }

    @Override
    public double variance() {
        return DoubleArrays.variance(store.array, store.offset, store.size);
    }

    @Override
    public double nanvariance() {
        return DoubleArrays.nanVariance(store.array, store.offset, store.size);
    }

    @Override
    public int argmin() {
        return DoubleArrays.argmin(store.array, store.offset, store.size) - store.offset;
    }

    @Override
    public double min() {
        return DoubleArrays.min(store.array, store.offset, store.size);
    }

    @Override
    public int argmax() {
        return DoubleArrays.argmax(store.array, store.offset, store.size) - store.offset;
    }

    @Override
    public double max() {
        return DoubleArrays.max(store.array, store.offset, store.size);
    }

    @Override
    public DVector apply(Double2DoubleFunction f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.newFrom(store.array, store.offset, store.size + store.offset, f);
            return DVector.wrap(copy);
        }
        for (int i = store.offset; i < store.offset + store.size; i++) {
            store.array[i] = f.applyAsDouble(store.array[i]);
        }
        return this;
    }

    @Override
    public DVector apply(BiFunction<Integer, Double, Double> f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[store.size];
            for (int i = 0; i < store.size; i++) {
                copy[i] = f.apply(i, store.array[store.offset + i]);
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < store.size; i++) {
            store.array[store.offset + i] = f.apply(i, store.array[store.offset + i]);
        }
        return this;
    }

    @Override
    public DVector sortValues(DoubleComparator comp, AlgebraOption<?>... opts) {
        DoubleArrays.quickSort(store.array, store.offset, store.offset + store.size, comp);
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(store.array).skip(store.offset).limit(store.size);
    }

    @Override
    public VarDouble dVar(AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy() || store.offset != 0) {
            double[] copy = Arrays.copyOfRange(store.array, store.offset, store.size + store.offset);
            return VarDouble.wrapArray(store.size, copy);
        }
        return VarDouble.wrapArray(store.size, store.array);
    }
}
