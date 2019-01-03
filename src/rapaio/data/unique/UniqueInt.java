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

package rapaio.data.unique;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import rapaio.data.*;

import java.io.Serializable;

/**
 * Unique value feature for integer values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueInt extends AbstractUnique {

    public static UniqueInt of(Var var, boolean sorted) {
        return new UniqueInt(var, sorted);
    }

    private IntArrayList values;

    private UniqueInt(Var var, boolean sorted) {
        super(sorted);
        IntOpenHashSet keySet = new IntOpenHashSet();
        for (int i = 0; i < var.rowCount(); i++) {
            int key = var.getInt(i);
            if (!keySet.contains(key)) {
                keySet.add(key);
            }
        }
        int[] elements = keySet.toIntArray();
        if (sorted) {
            IntArrays.quickSort(elements, new UniqueIntComparator());
        }
        Int2IntOpenHashMap uniqueKeys = new Int2IntOpenHashMap();
        values = new IntArrayList(elements);
        for (int i = 0; i < elements.length; i++) {
            uniqueKeys.put(elements[i], i);
        }
        rowLists = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < var.rowCount(); i++) {
            int key = var.getInt(i);
            int id = uniqueKeys.get(key);
            if (!rowLists.containsKey(id)) {
                rowLists.put(id, new IntArrayList());
            }
            rowLists.get(id).add(i);
        }
        updateIdsByRow(var.rowCount());
    }

    @Override
    public int uniqueCount() {
        return values.size();
    }

    @Override
    public IntList valueSortedIds() {
        if (valueSortedIds == null) {
            int[] ids = new int[uniqueCount()];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = i;
            }
            if (sorted) {
                valueSortedIds = new IntArrayList(ids);
            } else {
                UniqueIntComparator cmp = new UniqueIntComparator();
                IntArrays.quickSort(ids, (i, j) -> cmp.compare(values.getInt(i), values.getInt(j)));
            }
            valueSortedIds = new IntArrayList(ids);
        }
        return valueSortedIds;
    }

    @Override
    public IntList rowList(int id) {
        return rowLists.get(id);
    }

    public int uniqueValue(int id) {
        return values.getInt(id);
    }
}

@SuppressWarnings("ComparatorMethodParameterNotUsed")
class UniqueIntComparator implements it.unimi.dsi.fastutil.ints.IntComparator, Serializable {

    private static final long serialVersionUID = 1347615489598406390L;

    @Override
    public int compare(int v1, int v2) {
        return (v1 < v2) ? -1 : 1;
    }
}