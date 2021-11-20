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
import rapaio.data.filter.VRefSort;
import rapaio.util.IntComparator;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.Double2DoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/5/19.
 */
public final class DefaultDVarOp<T extends Var> implements DVarOp<T> {

    private final T source;

    public DefaultDVarOp(T source) {
        this.source = source;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T sort(IntComparator comparator) {
        return (T) source.fapply(VRefSort.from(comparator)).copy();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T sort(boolean asc) {
        return (T) source.fapply(VRefSort.from(source.refComparator(asc))).copy();
    }

    @Override
    public int[] sortedCompleteRows(boolean asc) {
        int len = 0;
        for (int i = 0; i < source.size(); i++) {
            if (source.isMissing(i)) {
                continue;
            }
            len++;
        }
        int[] rows = new int[len];
        len = 0;
        for (int i = 0; i < source.size(); i++) {
            if (source.isMissing(i)) {
                continue;
            }
            rows[len++] = i;
        }
        IntArrays.quickSort(rows, 0, len, source.refComparator(asc));
        return rows;
    }

    @Override
    public int[] sortedRows(boolean asc) {
        int[] rows = new int[source.size()];
        int len = 0;
        for (int i = 0; i < source.size(); i++) {
            rows[len++] = i;
        }
        IntArrays.quickSort(rows, 0, len, source.refComparator(asc));
        return rows;
    }

}
