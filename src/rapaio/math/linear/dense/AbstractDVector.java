/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.util.DoubleComparator;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.Double2DoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/8/20.
 */
public abstract class AbstractDVector implements DVector {

    @Serial
    private static final long serialVersionUID = 4164614372206348682L;

    protected void checkConformance(DVector vector) {
        if (size() != vector.size()) {
            throw new IllegalArgumentException(
                    String.format("Vectors are not conform for operation: [%d] vs [%d]", size(), vector.size()));
        }
    }

    @Override
    public DVector fill(double value) {
        for (int i = 0; i < size(); i++) {
            set(i, value);
        }
        return this;
    }

    @Override
    public DVectorDense denseCopy(int len) {
        double[] copy = new double[len];
        for (int i = 0; i < Math.min(size(), len); i++) {
            copy[i] = get(i);
        }
        return new DVectorDense(0, len, copy);
    }

    @Override
    public void swap(final int a, final int b) {
        final double t = get(a);
        set(a, get(b));
        set(b, t);
    }

    @Override
    public void swap(int a, int b, final int n) {
        for (int i = 0; i < n; i++, a++, b++) {
            swap(a, b);
        }
    }

    @Override
    public DVector log() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.log(get(i)));
        }
        return this;
    }

    @Override
    public DVector logTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.log(get(i)));
        }
        return to;
    }

    @Override
    public DVector log1p() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.log1p(get(i)));
        }
        return this;
    }

    @Override
    public DVector log1pTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.log1p(get(i)));
        }
        return to;
    }

    @Override
    public DVector log10() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.log10(get(i)));
        }
        return this;
    }

    @Override
    public DVector log10To(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.log10(get(i)));
        }
        return to;
    }

    @Override
    public DVector abs() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.abs(get(i)));
        }
        return this;
    }

    @Override
    public DVector absTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.abs(get(i)));
        }
        return to;
    }

    @Override
    public DVector neg() {
        for (int i = 0; i < size(); i++) {
            set(i, -get(i));
        }
        return this;
    }

    @Override
    public DVector negTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, -get(i));
        }
        return to;
    }

    @Override
    public DVector cos() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.cos(get(i)));
        }
        return this;
    }

    @Override
    public DVector cosTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.cos(get(i)));
        }
        return to;
    }

    @Override
    public DVector cosh() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.cosh(get(i)));
        }
        return this;
    }

    @Override
    public DVector coshTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.cosh(get(i)));
        }
        return to;
    }

    @Override
    public DVector acos() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.acos(get(i)));
        }
        return this;
    }

    @Override
    public DVector acosTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.acos(get(i)));
        }
        return to;
    }

    @Override
    public DVector sin() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.sin(get(i)));
        }
        return this;
    }

    @Override
    public DVector sinTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.sin(get(i)));
        }
        return to;
    }

    @Override
    public DVector sinh() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.sinh(get(i)));
        }
        return this;
    }

    @Override
    public DVector sinhTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.sinh(get(i)));
        }
        return to;
    }

    @Override
    public DVector asin() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.asin(get(i)));
        }
        return this;
    }

    @Override
    public DVector asinTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.asin(get(i)));
        }
        return to;
    }

    @Override
    public DVector tan() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.tan(get(i)));
        }
        return this;
    }

    @Override
    public DVector tanTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.tan(get(i)));
        }
        return to;
    }

    @Override
    public DVector tanh() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.tanh(get(i)));
        }
        return this;
    }

    @Override
    public DVector tanhTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.tanh(get(i)));
        }
        return to;
    }

    @Override
    public DVector atan() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.atan(get(i)));
        }
        return this;
    }

    @Override
    public DVector atanTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.atan(get(i)));
        }
        return to;
    }

    @Override
    public DVector exp() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.exp(get(i)));
        }
        return this;
    }

    @Override
    public DVector expTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.exp(get(i)));
        }
        return to;
    }

    @Override
    public DVector expm1() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.expm1(get(i)));
        }
        return this;
    }

    @Override
    public DVector expm1To(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.expm1(get(i)));
        }
        return to;
    }

    @Override
    public DVector sqrt() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.sqrt(get(i)));
        }
        return this;
    }

    @Override
    public DVector sqrtTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.sqrt(get(i)));
        }
        return to;
    }

    @Override
    public DVector cbrt() {
        for (int i = 0; i < size(); i++) {
            set(i, StrictMath.cbrt(get(i)));
        }
        return this;
    }

    @Override
    public DVector cbrtTo(DVector to) {
        for (int i = 0; i < size(); i++) {
            to.set(i, StrictMath.cbrt(get(i)));
        }
        return to;
    }

    @Override
    public DVector add(double x) {
        for (int i = 0; i < size(); i++) {
            inc(i, x);
        }
        return this;
    }

    @Override
    public DVector addTo(DVector to, double x) {
        for (int i = 0; i < size(); i++) {
            to.set(i, get(i) + x);
        }
        return to;
    }

    @Override
    public DVector add(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            inc(i, b.get(i));
        }
        return this;
    }

    @Override
    public DVector addTo(DVector to, DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            to.set(i, get(i) + b.get(i));
        }
        return to;
    }

    @Override
    public DVector sub(double x) {
        for (int i = 0; i < size(); i++) {
            inc(i, -x);
        }
        return this;
    }

    @Override
    public DVector subTo(DVector to, double x) {
        for (int i = 0; i < size(); i++) {
            to.set(i, get(i) - x);
        }
        return to;
    }

    @Override
    public DVector sub(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            inc(i, -b.get(i));
        }
        return this;
    }

    @Override
    public DVector subTo(DVector to, DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            to.set(i, get(i) - b.get(i));
        }
        return to;
    }

    @Override
    public DVector mul(double x) {
        for (int i = 0; i < size(); i++) {
            set(i, get(i) * x);
        }
        return this;
    }

    @Override
    public DVector mulTo(DVector to, double x) {
        for (int i = 0; i < size(); i++) {
            to.set(i, get(i) * x);
        }
        return to;
    }

    @Override
    public DVector mul(DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            set(i, get(i) * b.get(i));
        }
        return this;
    }

    @Override
    public DVector mulTo(DVector to, DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            to.set(i, get(i) * b.get(i));
        }
        return to;
    }

    @Override
    public DVector div(double x) {
        for (int i = 0; i < size(); i++) {
            set(i, get(i) / x);
        }
        return this;
    }

    @Override
    public DVector divTo(DVector to, double x) {
        for (int i = 0; i < size(); i++) {
            to.set(i, get(i) / x);
        }
        return to;
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
    public DVector divTo(DVector to, DVector b) {
        checkConformance(b);
        for (int i = 0; i < size(); i++) {
            to.set(i, get(i) / b.get(i));
        }
        return to;
    }

    @Override
    public DVector fma(double a, DVector y) {
        checkConformance(y);
        for (int i = 0; i < size(); i++) {
            set(i, get(i) + a * y.get(i));
        }
        return this;
    }

    @Override
    public DVector fmaNew(double a, DVector y) {
        checkConformance(y);
        double[] copy = new double[size()];
        for (int i = 0; i < size(); i++) {
            copy[i] = a * get(i) + y.get(i);
        }
        return new DVectorDense(0, copy.length, copy);
    }

    @Override
    public DVector cut(double low, double high) {
        for (int i = 0; i < size(); i++) {
            double v = get(i);
            if (!Double.isNaN(low)) {
                v = Math.max(low, v);
            }
            if (!Double.isNaN(high)) {
                v = Math.min(high, v);
            }
            set(i, v);
        }
        return this;
    }

    @Override
    public DVector cutTo(DVector to, double low, double high) {
        for (int i = 0; i < size(); i++) {
            double v = get(i);
            if (!Double.isNaN(low)) {
                v = Math.min(low, v);
            }
            if (!Double.isNaN(high)) {
                v = Math.min(high, v);
            }
            to.set(i, v);
        }
        return this;
    }

    @Override
    public double dotBilinear(DMatrix m, DVector y) {
        if (m.rows() != size() || m.cols() != y.size()) {
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
        if (m.rows() != size() || m.cols() != size()) {
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
    public double dotBilinearDiag(DMatrix m, DVector y) {
        if (m.rows() != size() || m.cols() != y.size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += get(i) * m.get(i, i) * y.get(i);
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
            sum += get(i) * m.get(i) * y.get(i);
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DMatrix m) {
        if (m.rows() != size() || m.cols() != size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            double xi = get(i);
            sum += xi * m.get(i, i) * xi;
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
            double xi = get(i);
            sum += xi * m.get(i) * xi;
        }
        return sum;
    }

    @Override
    public DMatrix outer(DVector b) {
        DMatrix m = DMatrix.empty(size(), b.size());
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < b.size(); j++) {
                m.set(i, j, get(i) * b.get(j));
            }
        }
        return m;
    }

    @Override
    public DVector apply(BiFunction<Integer, Double, Double> f) {
        for (int i = 0; i < size(); i++) {
            set(i, f.apply(i, get(i)));
        }
        return this;
    }

    @Override
    public DVector applyTo(DVector to, BiFunction<Integer, Double, Double> f) {
        for (int i = 0; i < size(); i++) {
            to.set(i, f.apply(i, get(i)));
        }
        return to;
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

    @Override
    public double norm(double p) {
        if (p <= 0) {
            return size();
        }
        if (p == Double.POSITIVE_INFINITY) {
            return max();
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
        for (int i = 0; i < size(); i++) {
            sum += get(i);
        }
        return sum;
    }

    @Override
    public double nansum() {
        double nansum = 0;
        for (int i = 0; i < size(); i++) {
            double value = get(i);
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
    public int argmin() {
        int amin = 0;
        for (int i = 1; i < size(); i++) {
            if (get(amin) > get(i)) {
                amin = i;
            }
        }
        return amin;
    }

    @Override
    public double min() {
        double min = get(0);
        for (int i = 0; i < size(); i++) {
            if (min > get(i)) {
                min = get(i);
            }
        }
        return min;
    }

    @Override
    public int argmax() {
        int amax = 0;
        for (int i = 0; i < size(); i++) {
            if (get(amax) < get(i)) {
                amax = i;
            }
        }
        return amax;
    }

    @Override
    public double max() {
        double max = get(0);
        for (int i = 0; i < size(); i++) {
            if (max < get(i)) {
                max = get(i);
            }
        }
        return max;
    }

    @Override
    public DVector apply(Double2DoubleFunction f) {
        for (int i = 0; i < size(); i++) {
            set(i, f.applyAsDouble(get(i)));
        }
        return this;
    }

    @Override
    public DVector applyTo(DVector to, Double2DoubleFunction f) {
        for (int i = 0; i < size(); i++) {
            to.set(i, f.applyAsDouble(get(i)));
        }
        return to;
    }

    @Override
    public DVector sortValues(DoubleComparator comp) {
        quickSort(0, size(), comp);
        return this;
    }

    @Override
    public DVector sortValuesNew(DoubleComparator comp) {
        double[] copy = new double[size()];
        for (int i = 0; i < size(); i++) {
            copy[i] = get(i);
        }
        DoubleArrays.quickSort(copy, 0, size(), comp);
        return new DVectorDense(0, size(), copy);
    }

    private static final int QUICKSORT_NO_REC = 16;
    private static final int QUICKSORT_MEDIAN_OF_9 = 128;

    private int med3(final int a, final int b, final int c, DoubleComparator comp) {
        final int ab = comp.compare(get(a), get(b));
        final int ac = comp.compare(get(a), get(c));
        final int bc = comp.compare(get(b), get(c));
        return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
    }

    private void selectionSort(final int from, final int to, final DoubleComparator comp) {
        for (int i = from; i < to - 1; i++) {
            int m = i;
            for (int j = i + 1; j < to; j++) {
                if (comp.compare(get(j), get(m)) < 0) {
                    m = j;
                }
            }
            if (m != i) {
                swap(m, i);
            }
        }
    }

    private void quickSort(final int from, final int to, final DoubleComparator comp) {
        final int len = to - from;
        // Selection sort on smallest arrays
        if (len < QUICKSORT_NO_REC) {
            selectionSort(from, to, comp);
            return;
        }
        // Choose a partition element, v
        int m = from + len / 2;
        int l = from;
        int n = to - 1;
        if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
            int s = len / 8;
            l = med3(l, l + s, l + 2 * s, comp);
            m = med3(m - s, m, m + s, comp);
            n = med3(n - 2 * s, n - s, n, comp);
        }
        m = med3(l, m, n, comp); // Mid-size, med of 3
        final double v = get(m);
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from;
        int b = a;
        int c = to - 1;
        int d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = comp.compare(get(b), v)) <= 0) {
                if (comparison == 0) {
                    swap(a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = comp.compare(get(c), v)) >= 0) {
                if (comparison == 0) {
                    swap(c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            swap(b++, c--);
        }
        // Swap partition elements back to middle
        int s = Math.min(a - from, b - a);
        swap(from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        swap(b, to - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            quickSort(from, from + s, comp);
        }
        if ((s = d - c) > 1) {
            quickSort(to - s, to, comp);
        }
    }

    @Override
    public void sortIndexes(DoubleComparator comp, int[] indexes) {
        quickSortIndirect(indexes, 0, indexes.length, comp);
    }

    private void insertionSortIndirect(final int[] perm, final int from, final int to, final DoubleComparator comp) {
        for (int i = from; ++i < to; ) {
            int t = perm[i];
            int j = i;
            for (int u = perm[j - 1]; comp.compare(get(t), get(u)) < 0; u = perm[--j - 1]) {
                perm[j] = u;
                if (from == j - 1) {
                    --j;
                    break;
                }
            }
            perm[j] = t;
        }
    }

    private int med3Indirect(final int[] perm, final int a, final int b, final int c, final DoubleComparator comp) {
        final double aa = get(perm[a]);
        final double bb = get(perm[b]);
        final double cc = get(perm[c]);
        final int ab = comp.compare((aa), (bb));
        final int ac = comp.compare((aa), (cc));
        final int bc = comp.compare((bb), (cc));
        return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
    }

    private void quickSortIndirect(final int[] perm, final int from, final int to, final DoubleComparator comp) {
        final int len = to - from;
        // Selection sort on smallest arrays
        if (len < QUICKSORT_NO_REC) {
            insertionSortIndirect(perm, from, to, comp);
            return;
        }
        // Choose a partition element, v
        int m = from + len / 2;
        int l = from;
        int n = to - 1;
        if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
            int s = len / 8;
            l = med3Indirect(perm, l, l + s, l + 2 * s, comp);
            m = med3Indirect(perm, m - s, m, m + s, comp);
            n = med3Indirect(perm, n - 2 * s, n - s, n, comp);
        }
        m = med3Indirect(perm, l, m, n, comp); // Mid-size, med of 3
        final double v = get(perm[m]);
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = comp.compare(get(perm[b]), v)) <= 0) {
                if (comparison == 0) {
                    IntArrays.swap(perm, a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = comp.compare(get(perm[c]), v)) >= 0) {
                if (comparison == 0) {
                    IntArrays.swap(perm, c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            IntArrays.swap(perm, b++, c--);
        }
        // Swap partition elements back to middle
        int s;
        s = Math.min(a - from, b - a);
        IntArrays.swap(perm, from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        IntArrays.swap(perm, b, to - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            quickSortIndirect(perm, from, from + s, comp);
        }
        if ((s = d - c) > 1) {
            quickSortIndirect(perm, to - s, to, comp);
        }
    }

    private static final class DoubleIterator implements Iterator<Double> {
        private final DVector parent;
        private int pos = 0;

        public DoubleIterator(DVector parent) {
            this.parent = parent;
        }

        @Override
        public boolean hasNext() {
            return pos < parent.size();
        }

        @Override
        public Double next() {
            if (pos < parent.size()) {
                pos++;
                return parent.get(pos - 1);
            }
            throw new NoSuchElementException();
        }
    }

    @Override
    public Iterator<Double> iterator() {
        return new DoubleIterator(this);
    }

    @Override
    public boolean deepEquals(DVector v, double eps) {
        if (size() != v.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (Math.abs(get(i) - v.get(i)) > eps) {
                return false;
            }
        }
        return true;
    }

    @Override
    public DMatrixDenseC asMatrix() {
        DMatrixDenseC res = DMatrix.empty(size(), 1);
        for (int i = 0; i < size(); i++) {
            res.set(i, 0, get(i));
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("{");
        sb.append("size:").append(size()).append(", values:");
        sb.append("[");
        for (int i = 0; i < Math.min(20, size()); i++) {
            sb.append(Format.floatFlex(get(i)));
            if (i != size() - 1) {
                sb.append(",");
            }
        }
        if (size() > 20) {
            sb.append("...");
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        return toContent(printer, options);
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        int head = 20;
        int tail = 2;

        boolean full = head + tail >= size();

        if (full) {
            return toFullContent(printer, options);
        }

        int[] rows = new int[Math.min(head + tail + 1, size())];
        for (int i = 0; i < head; i++) {
            rows[i] = i;
        }
        rows[head] = -1;
        for (int i = 0; i < tail; i++) {
            rows[i + head + 1] = i + size() - tail;
        }
        TextTable tt = TextTable.empty(rows.length, 2, 0, 1);
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == -1) {
                tt.textCenter(i, 0, "...");
                tt.textCenter(i, 1, "...");
            } else {
                tt.intRow(i, 0, rows[i]);
                tt.floatFlexLong(i, 1, get(rows[i]));
            }
        }
        return tt.getDynamicText(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {

        TextTable tt = TextTable.empty(size(), 2, 0, 1);
        for (int i = 0; i < size(); i++) {
            tt.intRow(i, 0, i);
            tt.floatFlexLong(i, 1, get(i));
        }
        return tt.getDynamicText(printer, options);
    }
}
