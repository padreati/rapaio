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

package rapaio.math.linear.base;

import java.io.Serial;
import java.util.function.BiFunction;

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.MType;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.math.linear.dense.DVectorMap;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
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
    public DVector map(int[] indexes, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[indexes.length];
            for (int i = 0; i < indexes.length; i++) {
                copy[i] = get(indexes[i]);
            }
            return DVector.wrap(copy);
        }
        return new DVectorMap(this, indexes);
    }

    @Override
    public DVector add(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = get(i) + x;
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, get(i) + x);
        }
        return this;
    }

    @Override
    public DVector add(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = get(i) + b.get(i);
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, get(i) + b.get(i));
        }
        return this;
    }

    @Override
    public DVector sub(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = get(i) - x;
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, get(i) - x);
        }
        return this;
    }

    @Override
    public DVector sub(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = get(i) - b.get(i);
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, get(i) - b.get(i));
        }
        return this;
    }

    @Override
    public DVector mul(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = get(i) * x;
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, get(i) * x);
        }
        return this;
    }

    @Override
    public DVector mul(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = get(i) * b.get(i);
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, get(i) * b.get(i));
        }
        return this;
    }

    @Override
    public DVector div(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = get(i) / x;
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, get(i) / x);
        }
        return this;
    }

    @Override
    public DVector div(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = get(i) / b.get(i);
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, get(i) / b.get(i));
        }
        return this;
    }

    @Override
    public DVector addMul(double a, DVector y, AlgebraOption<?>... opts) {
        checkConformance(y);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < size(); i++) {
                copy[i] = a * get(i) + y.get(i);
            }
            return new DVectorDense(0, copy.length, copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, get(i) + a * y.get(i));
        }
        return this;
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
    public double dotBilinearDiag(DMatrix m, DVector y) {
        if (m.rowCount() != size() || m.colCount() != y.size()) {
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
        if (m.rowCount() != size() || m.colCount() != size()) {
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
    public DVector apply(BiFunction<Integer, Double, Double> f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < size(); i++) {
                copy[i] = f.apply(i, get(i));
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, f.apply(i, get(i)));
        }
        return this;
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

    public double pnorm(double p) {
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
    public DVector apply(Double2DoubleFunction f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            for (int i = 0; i < size(); i++) {
                copy[i] = f.apply(get(i));
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size(); i++) {
            set(i, f.applyAsDouble(get(i)));
        }
        return this;
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
    public DMatrix asMatrix(MType type) {
        DMatrix res = DMatrix.empty(type, size(), 1);
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
