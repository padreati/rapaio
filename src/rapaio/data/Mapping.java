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

package rapaio.data;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import rapaio.data.mapping.IntervalMapping;
import rapaio.data.mapping.ListMapping;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;

/**
 * A mapping is a collection of row numbers used to build a mapped frame as a
 * wrapped selection of rows.
 * <p>
 * The mapping holds the rows which needs to be kept in the mapped frame.
 * <p>
 * If a mapped frame is built over another mapped frame, than the provided
 * mapping at creation time will be transformed into a mapped of the
 * solid frame which is referenced by the wrapped frame.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Mapping extends Serializable {

    // static builders

    /**
     * @return an empty mapping
     */
    static Mapping empty() {
        return new ListMapping();
    }

    /**
     * Builds a mapping having the mapped values specified as parameter,
     * the list of values being used as reference inside mapping.
     *
     * @param mapping list of mapped values
     * @return new mapping which wraps the given list of indexed values
     */
    static Mapping wrap(IntList mapping) {
        return new ListMapping(mapping, false);
    }

    /**
     * Builds a mapping having the mapped values given as an array of indexed values,
     * a copy of the values is used.
     *
     * @param mapping array of mapped values
     * @return new mapping which is build on a copy of the array of values
     */
    static Mapping wrap(int... mapping) {
        return new ListMapping(mapping);
    }

    /**
     * Builds a mapping having the mapped values given as a list of indexed values,
     * a copy of the list of values is used.
     *
     * @param mapping list of mapped values
     * @return new mapping which is build on a copy of the list of values
     */
    static Mapping copy(IntList mapping) {
        return new ListMapping(mapping, false);
    }

    /**
     * Builds a mapping having the mapped values given as a list of indexed values,
     * a copy of the list of values is used.
     *
     * @param mapping list of mapped values
     * @return new mapping which is build on a copy of the list of values
     */
    static Mapping copy(IntList mapping, Int2IntFunction fun) {
        return new ListMapping(mapping, fun);
    }

    static Mapping range(int end) {
        return range(0, end);
    }

    static Mapping range(int start, int end) {
        return new IntervalMapping(start, end);
    }

    /**
     * @return the size of mapping
     */
    int size();

    /**
     * Gets mapped index for given position
     *
     * @param pos given position
     * @return mapped value
     */
    int get(int pos);

    /**
     * Adds at the end of mapping a mapped index
     *
     * @param row mapped index
     */
    void add(int row);

    /**
     * Adds at the end of mapping the given indexes contained in collection
     *
     * @param rows collection of row numbers to be added to the mapping
     */
    void addAll(IntCollection rows);

    /**
     * Removes the element from the given position.
     *
     * @param pos position of the element which will be removed
     */
    void remove(int pos);

    /**
     * Removes all elements from the mapping
     *
     * @param positions collection with positions which will be removed
     */
    void removeAll(IntCollection positions);

    /**
     * Removes all elements from mapping
     */
    void clear();

    IntListIterator iterator();

    IntList toList();

    static Collector<Integer, IntList, Mapping> collector() {
        return new Collector<Integer, IntList, Mapping>() {
            @Override
            public Supplier<IntList> supplier() {
                return IntArrayList::new;
            }

            @Override
            public BiConsumer<IntList, Integer> accumulator() {
                return List::add;
            }

            @Override
            public BinaryOperator<IntList> combiner() {
                return (list1, list2) -> {
                    list1.addAll(list2);
                    return list1;
                };
            }

            @Override
            public Function<IntList, Mapping> finisher() {
                return Mapping::wrap;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    IntStream stream();
}

