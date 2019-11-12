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
import rapaio.data.VarInt;
import rapaio.data.filter.var.VRefSort;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.DoubleComparator;
import rapaio.util.collection.IntArrays;
import rapaio.util.collection.IntComparator;
import rapaio.util.function.DoubleDoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/5/19.
 */
public final class DoubleVarOp implements VarOp<VarDouble> {

    private final VarDouble source;
    private final int rowCount;
    private final double[] data;

    public DoubleVarOp(VarDouble source) {
        this.source = source;
        this.rowCount = source.rowCount();
        this.data = source.array();
    }

    @Override
    public VarDouble apply(DoubleDoubleFunction fun) {
        for (int i = 0; i < rowCount; i++) {
            data[i] = fun.applyAsDouble(data[i]);
        }
        return source;
    }

    @Override
    public VarDouble capply(DoubleDoubleFunction fun) {
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
        } else if (x instanceof VarInt) {
            VarInt xi = (VarInt) x;
            int[] xiarray = xi.elements();
            for (int i = 0; i < rowCount; i++) {
                if (xiarray[i] == VarInt.MISSING_VALUE) {
                    data[i] = Double.NaN;
                } else {
                    data[i] += xiarray[i];
                }
            }
        } else {
            for (int i = 0; i < rowCount; i++) {
                data[i] += x.getDouble(i);
            }
        }
        return source;
    }

    @Override
    public VarDouble minus(double a) {
        for (int i = 0; i < rowCount; i++) {
            data[i] -= a;
        }
        return source;
    }

    @Override
    public VarDouble minus(Var x) {
        if (x instanceof VarDouble) {
            VarDouble xd = (VarDouble) x;
            double[] xdarray = xd.array();
            for (int i = 0; i < rowCount; i++) {
                data[i] -= xdarray[i];
            }
        } else if (x instanceof VarInt) {
            VarInt xi = (VarInt) x;
            int[] xiarray = xi.elements();
            for (int i = 0; i < rowCount; i++) {
                if (xiarray[i] == VarInt.MISSING_VALUE) {
                    data[i] = Double.NaN;
                } else {
                    data[i] -= xiarray[i];
                }
            }
        } else {
            for (int i = 0; i < rowCount; i++) {
                data[i] -= x.getDouble(i);
            }
        }
        return source;
    }

    @Override
    public VarDouble mult(double a) {
        for (int i = 0; i < rowCount; i++) {
            data[i] *= a;
        }
        return source;
    }

    @Override
    public VarDouble mult(Var x) {
        if (x instanceof VarDouble) {
            VarDouble xd = (VarDouble) x;
            double[] xdarray = xd.array();
            for (int i = 0; i < rowCount; i++) {
                data[i] *= xdarray[i];
            }
        } else if (x instanceof VarInt) {
            VarInt xi = (VarInt) x;
            int[] xiarray = xi.elements();
            for (int i = 0; i < rowCount; i++) {
                if (xiarray[i] == VarInt.MISSING_VALUE) {
                    data[i] = Double.NaN;
                } else {
                    data[i] *= xiarray[i];
                }
            }
        } else {
            for (int i = 0; i < rowCount; i++) {
                data[i] *= x.getDouble(i);
            }
        }
        return source;
    }

    @Override
    public VarDouble divide(double a) {
        for (int i = 0; i < rowCount; i++) {
            data[i] /= a;
        }
        return source;
    }

    @Override
    public VarDouble divide(Var x) {
        if (x instanceof VarDouble) {
            VarDouble xd = (VarDouble) x;
            double[] xdarray = xd.array();
            for (int i = 0; i < rowCount; i++) {
                data[i] /= xdarray[i];
            }
        } else if (x instanceof VarInt) {
            VarInt xi = (VarInt) x;
            int[] xiarray = xi.elements();
            for (int i = 0; i < rowCount; i++) {
                if (xiarray[i] == VarInt.MISSING_VALUE) {
                    data[i] = Double.NaN;
                } else {
                    data[i] /= xiarray[i];
                }
            }
        } else {
            for (int i = 0; i < rowCount; i++) {
                data[i] /= x.getDouble(i);
            }
        }
        return source;
    }

    @Override
    public VarDouble sort(IntComparator comparator) {
        source.fapply(VRefSort.from(comparator));
        return source;
    }

    @Override
    public VarDouble sort(boolean asc) {
        DoubleComparator comparator = getComparator(asc);
        DoubleArrays.quickSort(source.array(), 0, source.rowCount(), comparator);
        return source;
    }

    @Override
    public int[] sortedCompleteRows(boolean asc) {
        int[] rows = new int[rowCount];
        int len = 0;
        for (int i = 0; i < rowCount; i++) {
            if (source.isMissing(i)) {
                continue;
            }
            rows[len++] = i;
        }
        DoubleArrays.quickSortIndirect(rows, data, 0, len);
        if (!asc) {
            IntArrays.reverse(rows, 0, len);
        }
        return IntArrays.copy(rows, 0, len);
    }

    @Override
    public int[] sortedRows(boolean asc) {
        int[] rows = new int[rowCount];
        for (int i = 0; i < rowCount; i++) {
            rows[i] = i;
        }
        DoubleArrays.quickSortIndirect(rows, data, 0, rowCount);
        if (!asc) {
            IntArrays.reverse(rows);
        }
        return rows;
    }

    private DoubleComparator getComparator(boolean asc) {
        return asc ? DoubleComparator.NATURAL_COMPARATOR : DoubleComparator.REVERSE_COMPARATOR;
    }
}
