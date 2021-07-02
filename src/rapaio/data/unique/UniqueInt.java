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

package rapaio.data.unique;

import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.util.collection.IntArrays;
import rapaio.util.collection.IntOpenHashSet;

import java.util.HashMap;

/**
 * Unique value feature for integer values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueInt extends AbstractUnique {

    public static UniqueInt of(Var var, boolean sorted) {
        return new UniqueInt(var, sorted);
    }

    private final VarInt values;

    private UniqueInt(Var var, boolean sorted) {
        super(sorted);
        IntOpenHashSet keySet = new IntOpenHashSet();
        for (int i = 0; i < var.size(); i++) {
            keySet.add(var.getInt(i));
        }
        int[] elements = keySet.toArray();
        if (sorted) {
            IntArrays.quickSort(elements, 0, elements.length, (k1, k2) -> {
                if (k1 == VarInt.MISSING_VALUE) {
                    return -1;
                }
                if (k2 == VarInt.MISSING_VALUE) {
                    return 1;
                }
                return Integer.compare(k1, k2);
            });
        }
        HashMap<Integer, Integer> uniqueKeys = new HashMap<>();
        values = VarInt.wrap(elements);
        for (int i = 0; i < elements.length; i++) {
            uniqueKeys.put(elements[i], i);
        }
        rowLists = new HashMap<>();
        for (int i = 0; i < var.size(); i++) {
            int key = var.getInt(i);
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
            if (sorted) {
                valueSortedIds = VarInt.wrap(ids);
            } else {
                IntArrays.quickSort(ids, 0, uniqueCount(), (i, j) -> Integer.compare(values.getInt(i), values.getInt(j)));
            }
            valueSortedIds = VarInt.wrap(ids);
        }
        return valueSortedIds;
    }

    @Override
    public Mapping rowList(int id) {
        return rowLists.get(id);
    }

    public int uniqueValue(int id) {
        return values.getInt(id);
    }

    @Override
    protected String stringClass() {
        return "UniqueInt";
    }

    @Override
    protected String stringUniqueValue(int i) {
        return values.getInt(i) == VarInt.MISSING_VALUE ? "?" : Integer.toString(values.getInt(i));
    }
}