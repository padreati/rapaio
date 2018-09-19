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

package rapaio.data;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Ordinal variables contains values for categorical observations where order of labels is important.
 * <p>
 * The domain of the definition is called levels and is given at construction time or can be changed latter.
 * <p>
 * This type of variable accepts two value representation: as labels and as indexes.
 * <p>
 * Label representation is the natural representation since in experiments
 * the nominal vectors are given as string values.
 * <p>
 * The index representation is learn based on the term levels and is used often for performance
 * reasons instead of label representation, where the actual label value does not matter.
 * <p>
 * Index values can be used to compare to values, however other numeric statistics such as mean
 * does not apply since there are meaningless. The reason why the index mean is meaningless is
 * that there are no guarantees that the difference between index i and i-1 is the same between
 * index i and i+1. Indexes specify only order, but not numerical quantities.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public final class VarOrdinal extends FactorBase {

    /**
     * Builds a new empty ordinal variable.
     *
     * @return new variable instance of ordinal type
     */
    public static VarOrdinal empty() {
        return new VarOrdinal();
    }

    /**
     * Builds a new ordinal variable with given levels and of give size filled with missing values.
     *
     * @param rows variable size
     * @param dict term levels
     * @return new variable instance of ordinal type
     */
    public static VarOrdinal empty(int rows, String... dict) {
        return VarOrdinal.empty(rows, Arrays.asList(dict));
    }

    /**
     * Builds a new  ordinal variable with given levels and of given size filled with missing values.
     *
     * @param rows variable size
     * @param dict term levels
     * @return new variable instance of ordinal type
     */
    public static VarOrdinal empty(int rows, Collection<String> dict) {
        VarOrdinal nominal = new VarOrdinal();
        for (String next : dict) {
            if (nominal.dict.contains(next)) continue;
            nominal.dict.add(next);
            nominal.reverse.put(next, nominal.reverse.size());
        }
        nominal.data = new int[rows];
        nominal.rows = rows;
        return nominal;
    }

    private static final long serialVersionUID = 5438713835700406847L;

    private VarOrdinal() {
        super();
        // set the missing value
        this.reverse = new Object2IntOpenHashMap<>();
        this.reverse.put("?", 0);
        this.dict = new ArrayList<>();
        this.dict.add("?");
        data = new int[0];
        rows = 0;
    }

    @Override
    public VarOrdinal withName(String name) {
        return (VarOrdinal) super.withName(name);
    }

    @Override
    public VType type() {
        return VType.ORDINAL;
    }

    @Override
    public void addRows(int rowCount) {
        grow(rows + rowCount);
        for (int i = 0; i < rowCount; i++) {
            data[rows + i] = 0;
        }
        rows += rowCount;
    }

    @Override
    public Var newInstance(int rows) {
        return VarOrdinal.empty(rows, levels());
    }

    @Override
    public VarOrdinal solidCopy() {
        return (VarOrdinal) super.solidCopy();
    }

    @Override
    public String toString() {
        return "Ordinal[name:" + name() + ", rowCount:" + rowCount() + "]";
    }
}