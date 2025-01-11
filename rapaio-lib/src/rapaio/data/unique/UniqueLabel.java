/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.util.collection.Ints;

/**
 * Unique value feature for label values.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueLabel extends AbstractUnique {

    public static UniqueLabel of(Var var, boolean sorted) {
        return new UniqueLabel(var, sorted);
    }

    private final ArrayList<String> values;

    private UniqueLabel(Var var, boolean sorted) {
        super(sorted);
        HashSet<String> keySet = new HashSet<>();
        for (int i = 0; i < var.size(); i++) {
            keySet.add(var.getLabel(i));
        }
        values = new ArrayList<>(keySet);
        if (sorted) {
            values.sort(new UniqueLabelComparator());
        }
        HashMap<String, Integer> uniqueKeys = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            uniqueKeys.put(values.get(i), i);
        }
        rowLists = new HashMap<>();
        for (int i = 0; i < var.size(); i++) {
            int id = uniqueKeys.get(var.getLabel(i));
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
                UniqueLabelComparator cmp = new UniqueLabelComparator();
                Ints.quickSort(ids, 0, ids.length, (i, j) -> cmp.compare(values.get(i), values.get(j)));
            }
            valueSortedIds = VarInt.wrap(ids);
        }
        return valueSortedIds;
    }

    @Override
    public Mapping rowList(int id) {
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

    @Serial
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