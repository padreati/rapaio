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

import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.printer.Format;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.DoubleComparator;
import rapaio.util.collection.IntArrays;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Unique value feature for double values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueDouble extends AbstractUnique {

    public static UniqueDouble of(Var var, boolean sorted) {
        return new UniqueDouble(var, sorted);
    }

    private VarDouble values;

    private UniqueDouble(Var var, boolean sorted) {
        super(sorted);
        HashSet<Double> keySet = new HashSet<>();
        for (int i = 0; i < var.rowCount(); i++) {
            keySet.add(var.getDouble(i));
        }
        double[] elements = new double[keySet.size()];
        int pos = 0;
        for(double value : keySet) {
            elements[pos++]=value;
        }
        if (sorted) {
            DoubleArrays.quickSort(elements, 0, elements.length, new UniqueDoubleComparator());
        }
        HashMap<Double, Integer> uniqueKeys = new HashMap<>();
        values = VarDouble.wrap(elements);
        for (int i = 0; i < elements.length; i++) {
            uniqueKeys.put(elements[i], i);
        }
        rowLists = new HashMap<>();
        for (int i = 0; i < var.rowCount(); i++) {
            double key = var.getDouble(i);
            int id = uniqueKeys.get(key);
            if (!rowLists.containsKey(id)) {
                rowLists.put(id, Mapping.empty());
            }
            rowLists.get(id).add(i);
        }
        updateIdsByRow(var.rowCount());
    }

    @Override
    public int uniqueCount() {
        return values.rowCount();
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
                UniqueDoubleComparator cmp = new UniqueDoubleComparator();
                IntArrays.quickSort(ids,0, uniqueCount(), (i, j) -> cmp.compare(values.getDouble(i), values.getDouble(j)));
            }
            valueSortedIds = VarInt.wrap(ids);
        }
        return valueSortedIds;
    }

    @Override
    public Mapping rowList(int id) {
        return rowLists.get(id);
    }

    public double uniqueValue(int id) {
        return values.getDouble(id);
    }

    public VarDouble getValues() {
        return values;
    }

    @Override
    protected String stringClass() {
        return "UniqueDouble";
    }

    @Override
    protected String stringUniqueValue(int i) {
        return Double.isNaN(values.getDouble(i)) ? "?" : Format.floatFlex(values.getDouble(i));
    }
}

class UniqueDoubleComparator implements DoubleComparator, Serializable {

    private static final long serialVersionUID = 1347615489598406390L;

    @Override
    public int compareDouble(double v1, double v2) {
        boolean nan1 = Double.isNaN(v1);
        boolean nan2 = Double.isNaN(v2);
        if (!(nan1 || nan2)) {
            return (v1 < v2) ? -1 : 1;
        }
        return nan1 ? -1 : 1;
    }
}