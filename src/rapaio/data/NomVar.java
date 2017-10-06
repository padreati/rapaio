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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Nominal var contains values for categorical observations where order of labels is not important.
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
 * Even if index values is an integer number the order of the indexes for
 * nominal variables is irrelevant.
 *
 * @author Aurelian Tutuianu
 */
public final class NomVar extends FactorBase {

    /**
     * Builds a new empty nominal variable
     *
     * @return new variable instance of nominal type
     */
    public static NomVar empty() {
        return new NomVar();
    }

    /**
     * Builds a new nominal variable of given size, with given term levels, filled with missing values.
     *
     * @param rows variable size
     * @param dict term levels
     * @return new variable instance of nominal type
     */
    public static NomVar empty(int rows, String... dict) {
        return NomVar.empty(rows, Arrays.asList(dict));
    }

    /**
     * Builds a new nominal variable of given size, with given term levels, filled with missing values.
     *
     * @param rows variable size
     * @param dict term levels
     * @return new variable instance of nominal type
     */
    public static NomVar empty(int rows, List<String> dict) {
        NomVar nominal = new NomVar();
        HashSet<String> used = new HashSet<>();
        used.add("?");
        for (String next : dict) {
            if (used.contains(next)) continue;
            used.add(next);
            nominal.dict.add(next);
            nominal.reverse.put(next, nominal.reverse.size());
        }
        nominal.data = new int[rows];
        nominal.rows = rows;
        return nominal;
    }

    public static NomVar copy(String... values) {
        NomVar nominal = NomVar.empty();
        for (String value : values)
            nominal.addLabel(value);
        return nominal;
    }

    public static NomVar copy(List<String> values) {
        NomVar nominal = NomVar.empty();
        for (String value : values)
            nominal.addLabel(value);
        return nominal;
    }

    public static NomVar from(int rows, Function<Integer, String> func, String... dict) {
        NomVar nominal = NomVar.empty(rows, dict);
        for (int i = 0; i < rows; i++) {
            nominal.setLabel(i, func.apply(i));
        }
        return nominal;
    }

    private static final long serialVersionUID = 1645571732133272467L;

    private NomVar() {
        // set the missing value
        this.reverse = new HashMap<>();
        this.reverse.put("?", 0);
        this.dict = new ArrayList<>();
        this.dict.add("?");
        data = new int[0];
        rows = 0;
    }

    public static Collector<String, NomVar, NomVar> collector() {

        return new Collector<String, NomVar, NomVar>() {
            @Override
            public Supplier<NomVar> supplier() {
                return NomVar::empty;
            }

            @Override
            public BiConsumer<NomVar, String> accumulator() {
                return FactorBase::addLabel;
            }

            @Override
            public BinaryOperator<NomVar> combiner() {
                return (left, right) -> (NomVar) left.bindRows(right);
            }

            @Override
            public Function<NomVar, NomVar> finisher() {
                return NomVar::solidCopy;
            }

            @Override
            public Set<Collector.Characteristics> characteristics() {
                return EnumSet.of(Collector.Characteristics.CONCURRENT, Collector.Characteristics.IDENTITY_FINISH);
            }
        };
    }

    @Override
    public NomVar withName(String name) {
        return (NomVar) super.withName(name);
    }

    @Override
    public VarType type() {
        return VarType.NOMINAL;
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
        return NomVar.empty(rows, levels());
    }

    @Override
    public NomVar solidCopy() {
        return (NomVar) super.solidCopy();
    }

    @Override
    public String toString() {
        return "Nominal[name:" + name() + ", rowCount:" + rowCount() + "]";
    }
}
