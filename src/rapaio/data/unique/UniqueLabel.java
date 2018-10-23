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

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.data.Var;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Unique value feature for label values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueLabel extends AbstractUnique {

    public static UniqueLabel of(Var var) {
        return new UniqueLabel(var);
    }

    private ArrayList<String> uniqueValues;

    private UniqueLabel(Var var) {
        uniqueIds = new IntArrayList();
        uniqueValues = new ArrayList<>();
        uniqueRowLists = new Int2ObjectOpenHashMap<>();
        int rowCount = var.rowCount();

        HashMap<String, Integer> uniqueKeys = new HashMap<>();
        for (int i = 0; i < rowCount; i++) {
            String key = var.getLabel(i);
            if (!uniqueKeys.containsKey(key)) {
                uniqueIds.add(uniqueKeys.size());
                uniqueValues.add(key);
                uniqueKeys.put(key, uniqueKeys.size());
            }
            int id = uniqueKeys.get(key);
            if (!uniqueRowLists.containsKey(id)) {
                uniqueRowLists.put(id, new IntArrayList());
            }
            uniqueRowLists.get(id).add(i);
        }
        updateIdsByRow(var.rowCount());
    }

    @Override
    public int uniqueCount() {
        return uniqueIds.size();
    }

    @Override
    public IntList valueSortedIds() {
        if (valueSortedIds == null) {
            int[] ids = uniqueIds.toIntArray();
            IntArrays.quickSort(ids, (i, j) -> {
                int cmp = uniqueValues.get(i).compareTo(uniqueValues.get(j));
                if (cmp == 0) return 0;
                if ("?".equals(uniqueValues.get(i))) return -1;
                if ("?".equals(uniqueValues.get(j))) return 1;
                return cmp;
            });
            valueSortedIds = new IntArrayList(ids);
        }
        return valueSortedIds;
    }

    @Override
    public IntList rowList(int id) {
        return uniqueRowLists.get(id);
    }

    public String uniqueValue(int id) {
        return uniqueValues.get(id);
    }
}
