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

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.ints.IntArrays;
import rapaio.data.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/5/19.
 */
public class DoubleVarOp implements VarOp<VarDouble> {

    private final VarDouble source;
    private final int rowCount;
    private final double[] data;

    public DoubleVarOp(VarDouble source) {
        this.source = source;
        this.rowCount = source.rowCount();
        this.data = source.array();
    }

    @Override
    public VarDouble apply(Double2DoubleFunction fun) {
        for (int i = 0; i < rowCount; i++) {
            data[i] = fun.applyAsDouble(data[i]);
        }
        return source;
    }

    @Override
    public VarDouble capply(Double2DoubleFunction fun) {
        double[] copy = new double[rowCount];
        for (int i = 0; i < rowCount; i++) {
            copy[i] = fun.applyAsDouble(data[i]);
        }
        return VarDouble.wrap(copy).withName(source.name());
    }

    @Override
    public double sum() {
        double sum = 0.0;
        for (int i = 0; i < rowCount; i++) {
            if (Double.isNaN(data[i])) {
                continue;
            }
            sum += data[i];
        }
        return sum;
    }

    @Override
    public double avg() {
        double count = 0.0;
        double sum = 0.0;
        for (int i = 0; i < rowCount; i++) {
            if (Double.isNaN(data[i])) {
                continue;
            }
            sum += data[i];
            count += 1;
        }
        return count > 0 ? sum / count : 0.0;
    }

    @Override
    public VarDouble plus(double a) {
        for (int i = 0; i < rowCount; i++) {
            data[i] += a;
        }
        return source;
    }

    @Override
    public VarDouble plus(Var x) {
        if (x instanceof VarDouble) {
            VarDouble xd = (VarDouble) x;
            double[] xdarray = xd.array();
            for (int i = 0; i < rowCount; i++) {
                data[i] += xdarray[i];
            }
        }
        return source;
    }

    @Override
    public VarDouble mult(double a) {
        for (int i = 0; i < rowCount; i++) {
            data[i] /= a;
        }
        return source;
    }

    @Override
    public int[] sortedCompleteRows(boolean asc) {
        int[] rows = new int[rowCount];
        int len = 0;
        for (int i = 0; i < rowCount; i++) {
            if(source.isMissing(i)) {
                continue;
            }
            rows[len++] = i;
        }
        IntArrays.quickSort(rows, 0, len, source.refComparator(asc));
        return IntArrays.copy(rows, 0, len);
    }
}
