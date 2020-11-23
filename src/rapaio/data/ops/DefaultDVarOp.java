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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.data.ops;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.filter.VRefSort;
import rapaio.util.IntComparator;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.Double2DoubleFunction;

import java.util.Arrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/5/19.
 */
public final class DefaultDVarOp<T extends Var> implements DVarOp<T> {

    private final T source;

    public DefaultDVarOp(T source) {
        this.source = source;
    }

    @Override
    public T apply(Double2DoubleFunction fun) {
        for (int i = 0; i < source.rowCount(); i++) {
            if (!source.isMissing(i)) {
                source.setDouble(i, fun.applyAsDouble(source.getDouble(i)));
            }
        }
        return source;
    }

    @Override
    public VarDouble capply(Double2DoubleFunction fun) {
        double[] data = new double[source.rowCount()];
        for (int i = 0; i < source.rowCount(); i++) {
            if (source.isMissing(i)) {
                data[i] = Double.NaN;
            } else {
                data[i] = fun.applyAsDouble(source.getDouble(i));
            }
        }
        return VarDouble.wrap(data).name(source.name());
    }

    @Override
    public double nansum() {
        double sum = 0.0;
        for (int i = 0; i < source.rowCount(); i++) {
            if (source.isMissing(i)) {
                continue;
            }
            sum += source.getDouble(i);
        }
        return sum;
    }

    @Override
    public double nanmean() {
        double count = 0.0;
        double sum = 0.0;
        for (int i = 0; i < source.rowCount(); i++) {
            if (source.isMissing(i)) {
                continue;
            }
            sum += source.getDouble(i);
            count += 1;
        }
        return count > 0 ? sum / count : 0.0;
    }

    @Override
    public T fill(double a) {
        for (int i = 0; i < source.rowCount(); i++) {
            source.setDouble(i, a);
        }
        return source;
    }

    @Override
    public T plus(double a) {
        for (int i = 0; i < source.rowCount(); i++) {
            source.setDouble(i, source.getDouble(i) + a);
        }
        return source;
    }

    @Override
    public T plus(Var x) {
        for (int i = 0; i < source.rowCount(); i++) {
            source.setDouble(i, source.getDouble(i) + x.getDouble(i));
        }
        return source;
    }

    @Override
    public T minus(double a) {
        for (int i = 0; i < source.rowCount(); i++) {
            source.setDouble(i, source.getDouble(i) - a);
        }
        return source;
    }

    @Override
    public T minus(Var x) {
        for (int i = 0; i < source.rowCount(); i++) {
            source.setDouble(i, source.getDouble(i) - x.getDouble(i));
        }
        return source;
    }

    @Override
    public T mult(double a) {
        for (int i = 0; i < source.rowCount(); i++) {
            source.setDouble(i, source.getDouble(i) * a);
        }
        return source;
    }

    @Override
    public T mult(Var x) {
        for (int i = 0; i < source.rowCount(); i++) {
            source.setDouble(i, source.getDouble(i) * x.getDouble(i));
        }
        return source;

    }

    @Override
    public T divide(double a) {
        for (int i = 0; i < source.rowCount(); i++) {
            source.setDouble(i, source.getDouble(i) / a);
        }
        return source;
    }

    @Override
    public T divide(Var x) {
        for (int i = 0; i < source.rowCount(); i++) {
            source.setDouble(i, source.getDouble(i) / x.getDouble(i));
        }
        return source;

    }

    @Override
    public T sort(IntComparator comparator) {
        return (T) source.fapply(VRefSort.from(comparator)).copy();
    }

    @Override
    public T sort(boolean asc) {
        return (T) source.fapply(VRefSort.from(source.refComparator(asc))).copy();
    }

    @Override
    public int[] sortedCompleteRows(boolean asc) {
        int[] rows = new int[source.rowCount()];
        int len = 0;
        for (int i = 0; i < source.rowCount(); i++) {
            if (source.isMissing(i)) {
                continue;
            }
            rows[len++] = i;
        }
        IntArrays.quickSort(rows, 0, len, source.refComparator(asc));
        return Arrays.copyOf(rows, len);
    }

    @Override
    public int[] sortedRows(boolean asc) {
        int[] rows = new int[source.rowCount()];
        int len = 0;
        for (int i = 0; i < source.rowCount(); i++) {
            rows[len++] = i;
        }
        IntArrays.quickSort(rows, 0, len, source.refComparator(asc));
        return rows;
    }

}
