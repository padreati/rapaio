/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.DoubleStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/3/16.
 */
public class SolidRM implements RM {

    private static final long serialVersionUID = -2186520026933442642L;

    private final int rowCount;
    private final int colCount;
    private final double[] values;

    public static SolidRM empty(int n, int m) {
        return new SolidRM(n, m);
    }

    public static SolidRM identity(int n) {
        SolidRM m = new SolidRM(n, n);
        for (int i = 0; i < n; i++) {
            m.set(i, i, 1.0);
        }
        return m;
    }

    public static SolidRM fill(int n, int m, double fill) {
        SolidRM ret = new SolidRM(n, m);
        if (fill != 0.0)
            Arrays.fill(ret.values, fill);
        return ret;
    }

    public static SolidRM fill(int n, int m, BiFunction<Integer, Integer, Double> bf) {
        SolidRM ret = new SolidRM(n, m);
        for (int i = 0; i < ret.rowCount(); i++) {
            for (int j = 0; j < ret.colCount(); j++) {
                ret.set(i, j, bf.apply(i, j));
            }
        }
        return ret;
    }

    public static SolidRM copyOf(int rows, int cols, double... source) {
        SolidRM m = empty(rows, cols);
        System.arraycopy(source, 0, m.values, 0, rows * cols);
        return m;
    }

    public static SolidRM copyOf(double[][] source) {
        int colCount = source[0].length;
        int rowCount = source.length;
        SolidRM m = empty(rowCount, colCount);
        for (int i = 0; i < colCount; i++) {
            System.arraycopy(source[i], 0, m.values, i * rowCount, colCount);
        }
        return m;
    }

    public static RM copyOf(double[][] source, int mFirst, int mLast, int nFirst, int nLast) {
        RM mm = new SolidRM(mLast - mFirst, nLast - nFirst);
        for (int i = mFirst; i < mLast; i++) {
            for (int j = nFirst; j < nLast; j++) {
                mm.set(i, j, source[i][j]);
            }
        }
        return mm;
    }

    public static SolidRM copyOf(Frame df) {
        SolidRM m = empty(df.rowCount(), df.varCount());
        for (int j = 0; j < df.varCount(); j++) {
            for (int i = 0; i < df.rowCount(); i++) {
                m.set(i, j, df.value(i, j));
            }
        }
        return m;
    }

    public static SolidRM copyOf(Var... vars) {
        Frame df = BoundFrame.newByVars(vars);
        SolidRM m = empty(df.rowCount(), df.varCount());
        for (int j = 0; j < df.varCount(); j++) {
            for (int i = 0; i < df.rowCount(); i++) {
                m.set(i, j, df.value(i, j));
            }
        }
        return m;
    }

    private SolidRM(int rowCount, int colCount) {
        if (((long) rowCount) * ((long) colCount) >= (long) Integer.MAX_VALUE)
            throw new IllegalArgumentException("Array is too large to allocate with integer indexes");

        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = new double[rowCount * colCount];
    }

    private SolidRM(int rowCount, int colCount, double[] values) {
        if (((long) rowCount) * ((long) colCount) >= (long) Integer.MAX_VALUE)
            throw new IllegalArgumentException("Array is too large to allocate with integer indexes");
        if (rowCount * colCount != values.length) {
            throw new IllegalArgumentException("rows*cols does not match the number of given values");
        }
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = values;
    }

    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
    public int colCount() {
        return colCount;
    }

    @Override
    public double get(int i, int j) {
        return values[i * colCount + j];
    }

    @Override
    public void set(int i, int j, double value) {
        values[i * colCount + j] = value;
    }

    @Override
    public void increment(int i, int j, double value) {
        values[i * colCount + j] += value;
    }

    @Override
    public RM t() {
        SolidRM t = new SolidRM(colCount, rowCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                t.set(j, i, get(i, j));
            }
        }
        return t;
    }

    @Override
    public SolidRV mapCol(int i) {
        SolidRV v = SolidRV.empty(rowCount);
        for (int j = 0; j < rowCount; j++) {
            v.set(j, get(j, i));
        }
        return v;
    }

    @Override
    public RV mapRow(int i) {
        SolidRV v = SolidRV.empty(colCount);
        for (int j = 0; j < colCount; j++) {
            v.set(j, get(i, j));
        }
        return v;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values);
    }

    @Override
    public SolidRM solidCopy() {
        SolidRM copy = new SolidRM(rowCount, colCount);
        System.arraycopy(values, 0, copy.values, 0, values.length);
        return copy;
    }
}
