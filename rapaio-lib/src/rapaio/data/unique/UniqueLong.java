/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.data.unique;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.util.collection.Ints;

/**
 * Unique value feature for long values, used for {@code LONG} and {@code INSTANT} variables.
 * <p>
 * Values are kept as {@code long}, so no precision is lost; routing these types through
 * {@link UniqueDouble} would merge values which differ beyond 2^53, and through {@link UniqueLabel}
 * would order them lexicographically instead of numerically.
 * <p>
 * The missing value ({@link VarLong#MISSING_VALUE}) is a value like any other here, and sorts last, as
 * {@code NaN} does in {@link UniqueDouble}.
 */
public class UniqueLong extends AbstractUnique {

    public static UniqueLong of(Var var, boolean sorted) {
        return new UniqueLong(var, sorted);
    }

    private final VarLong values;

    private UniqueLong(Var var, boolean sorted) {
        super(sorted);
        HashSet<Long> keySet = new HashSet<>();
        for (int i = 0; i < var.size(); i++) {
            keySet.add(var.getLong(i));
        }
        long[] elements = new long[keySet.size()];
        int pos = 0;
        for (long value : keySet) {
            elements[pos++] = value;
        }
        if (sorted) {
            // natural order puts VarLong.MISSING_VALUE (Long.MAX_VALUE) last
            Arrays.sort(elements);
        }
        HashMap<Long, Integer> uniqueKeys = new HashMap<>();
        values = VarLong.wrap(elements);
        for (int i = 0; i < elements.length; i++) {
            uniqueKeys.put(elements[i], i);
        }
        rowLists = new HashMap<>();
        for (int i = 0; i < var.size(); i++) {
            long key = var.getLong(i);
            int id = uniqueKeys.get(key);
            if (!rowLists.containsKey(id)) {
                rowLists.put(id, Mapping.empty());
            }
            rowLists.get(id).add(i);
        }
        updateIdsByRow(var.size());
    }

    @Override
    public int uniqueCount() {
        return values.size();
    }

    @Override
    public VarInt valueSortedIds() {
        if (valueSortedIds == null) {
            int[] ids = new int[uniqueCount()];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = i;
            }
            if (!sorted) {
                Ints.quickSort(ids, 0, uniqueCount(), (i, j) -> Long.compare(values.getLong(i), values.getLong(j)));
            }
            valueSortedIds = VarInt.wrap(ids);
        }
        return valueSortedIds;
    }

    @Override
    public Mapping rowList(int id) {
        return rowLists.get(id);
    }

    public long uniqueValue(int id) {
        return values.getLong(id);
    }

    @Override
    protected String stringClass() {
        return "UniqueLong";
    }

    @Override
    protected String stringUniqueValue(int i) {
        return values.getLong(i) == VarLong.MISSING_VALUE ? "?" : Long.toString(values.getLong(i));
    }
}
