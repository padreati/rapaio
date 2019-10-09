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
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import rapaio.data.Var;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Unique value feature for label values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueLabel extends AbstractUnique {

    public static UniqueLabel of(Var var, boolean sorted) {
        return new UniqueLabel(var, sorted);
    }

    private ArrayList<String> values;

    private UniqueLabel(Var var, boolean sorted) {
        super(sorted);
        HashSet<String> keySet = new HashSet<>();
        for (int i = 0; i < var.rowCount(); i++) {
            keySet.add(var.getLabel(i));
        }
        values = new ArrayList<>(keySet);
        if(sorted) {
            values.sort(new UniqueLabelComparator());
        }
        Object2IntOpenHashMap<String> uniqueKeys = new Object2IntOpenHashMap<>();
        for (int i = 0; i < values.size(); i++) {
            uniqueKeys.put(values.get(i), i);
        }
        rowLists = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < var.rowCount(); i++) {
            int id = uniqueKeys.getInt(var.getLabel(i));
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
                UniqueLabelComparator cmp = new UniqueLabelComparator();
                IntArrays.quickSort(ids, (i, j) -> cmp.compare(values.get(i), values.get(j)));
            }
            valueSortedIds = new IntArrayList(ids);
        }
        return valueSortedIds;
    }

    @Override
    public IntList rowList(int id) {
        return rowLists.get(id);
    }

    public String uniqueValue(int id) {
        return values.get(id);
    }

    @Override
    protected String stringClass() {
        return "UniqueLabel";
    }

    @Override
    protected String stringUniqueValue(int i) {
        return values.get(i);
    }
}

class UniqueLabelComparator implements Comparator<String>, Serializable {

    private static final long serialVersionUID = 1347615489598406390L;

    @Override
    public int compare(String v1, String v2) {
        boolean nan1 = "?".equals(v1);
        boolean nan2 = "?".equals(v2);
        if (!(nan1 || nan2)) {
            return (v1.compareTo(v2));
        }
        return nan1 ? -1 : 1;
    }
}