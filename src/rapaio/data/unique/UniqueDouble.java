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

import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.data.*;

import java.io.Serializable;

/**
 * Unique value feature for double values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueDouble extends AbstractUnique {

    public static UniqueDouble of(Var var, boolean sorted) {
        return new UniqueDouble(var, sorted);
    }

    private DoubleArrayList values;

    private UniqueDouble(Var var, boolean sorted) {
        super(sorted);
        DoubleOpenHashSet keySet = new DoubleOpenHashSet();
        for (int i = 0; i < var.rowCount(); i++) {
            keySet.add(var.getDouble(i));
        }
        double[] elements = keySet.toDoubleArray();
        if (sorted) {
            DoubleArrays.quickSort(elements, new UniqueDoubleComparator());
        }
        Double2IntOpenHashMap uniqueKeys = new Double2IntOpenHashMap();
        values = new DoubleArrayList(elements);
        for (int i = 0; i < elements.length; i++) {
            uniqueKeys.put(elements[i], i);
        }
        rowLists = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < var.rowCount(); i++) {
            double key = var.getDouble(i);
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
                UniqueDoubleComparator cmp = new UniqueDoubleComparator();
                IntArrays.quickSort(ids, (i, j) -> cmp.compare(values.getDouble(i), values.getDouble(j)));
            }
            valueSortedIds = new IntArrayList(ids);
        }
        return valueSortedIds;
    }

    @Override
    public IntList rowList(int id) {
        return rowLists.get(id);
    }

    public double uniqueValue(int id) {
        return values.getDouble(id);
    }
}

@SuppressWarnings("ComparatorMethodParameterNotUsed")
class UniqueDoubleComparator implements it.unimi.dsi.fastutil.doubles.DoubleComparator, Serializable {

    private static final long serialVersionUID = 1347615489598406390L;

    @Override
    public int compare(double v1, double v2) {
        boolean nan1 = Double.isNaN(v1);
        boolean nan2 = Double.isNaN(v2);
        if (!(nan1 || nan2)) {
            return (v1 < v2) ? -1 : 1;
        }
        return nan1 ? -1 : 1;
    }
}