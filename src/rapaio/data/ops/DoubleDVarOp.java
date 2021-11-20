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

package rapaio.data.ops;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.filter.VRefSort;
import rapaio.util.DoubleComparator;
import rapaio.util.DoubleComparators;
import rapaio.util.IntComparator;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.Double2DoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/5/19.
 */
public final class DoubleDVarOp implements DVarOp<VarDouble> {

    private final VarDouble source;
    private final int rowCount;
    private final double[] data;

    public DoubleDVarOp(VarDouble source) {
        this.source = source;
        this.rowCount = source.size();
        this.data = source.elements();
    }

    @Override
    public VarDouble sort(IntComparator comparator) {
        source.fapply(VRefSort.from(comparator));
        return source;
    }

    @Override
    public VarDouble sort(boolean asc) {
        DoubleComparator comparator = getComparator(asc);
        DoubleArrays.quickSort(source.elements(), 0, source.size(), comparator);
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
        return IntArrays.newCopy(rows, 0, len);
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
        return asc ? DoubleComparators.NATURAL_COMPARATOR : DoubleComparators.OPPOSITE_COMPARATOR;
    }
}
