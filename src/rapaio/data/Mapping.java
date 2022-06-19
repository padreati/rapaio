/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;

import rapaio.data.mapping.ArrayMapping;
import rapaio.data.mapping.IntervalMapping;
import rapaio.util.IntIterator;
import rapaio.util.function.Int2IntFunction;

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
public interface Mapping extends Iterable<Integer>, Serializable {

    // static builders

    /**
     * @return an empty mapping
     */
    static Mapping empty() {
        return new ArrayMapping();
    }

    /**
     * Builds a mapping having the mapped values given as an array of indexed values,
     * a copy of the values is used.
     *
     * @param array array of values
     * @return new mapping which is build on a copy of the array of values
     */
    static Mapping wrap(int... array) {
        return new ArrayMapping(array, 0, array.length);
    }

    static Mapping wrap(VarInt var) {
        return new ArrayMapping(var.elements(), 0, var.size());
    }

    /**
     * Builds a copy mapping.
     *
     * @param mapping list of mapped values
     * @return new mapping which wraps the given list of indexed values
     */
    static Mapping copy(Mapping mapping, int start, int end) {
        return new ArrayMapping(mapping.elements(), start, end);
    }

    static Mapping copy(VarInt var) {
        return copy(var, 0, var.size());
    }

    static Mapping copy(VarInt var, int start, int end) {
        return new ArrayMapping(var.elements(), start, end);
    }

    static Mapping from(VarInt var, Int2IntFunction fun) {
        return new ArrayMapping(var.elements(), 0, var.size(), fun);
    }

    /**
     * Builds a mapping having the mapped values given as a list of indexed values,
     * a copy of the list of values is used.
     *
     * @param mapping list of mapped values
     * @return new mapping which is build on a copy of the list of values
     */
    static Mapping from(Mapping mapping, Int2IntFunction fun) {
        return new ArrayMapping(mapping.elements(), 0, mapping.size(), fun);
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

    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Gets mapped index for given position
     *
     * @param pos given position
     * @return mapped value
     */
    int get(int pos);

    /**
     * Adds at the end of mapping a row index
     *
     * @param row mapped index
     */
    void add(int row);

    /**
     * Adds at the end of mapping the given indexes contained in collection
     *
     * @param rows collection of row numbers to be added to the mapping
     */
    void addAll(IntIterator rows);

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
    void removeAll(IntIterator positions);

    /**
     * Removes all elements from mapping
     */
    void clear();

    IntIterator iterator();

    /**
     * Raw array of elements. The length of the array might be longer than
     * the size of mapping. If the underlying implementation does not
     * use an array, a new array is created.
     *
     * @return int array of values
     */
    int[] elements();

    void shuffle();

    static Collector<Integer, VarInt, Mapping> collector() {
        return new Collector<>() {
            @Override
            public Supplier<VarInt> supplier() {
                return VarInt::empty;
            }

            @Override
            public BiConsumer<VarInt, Integer> accumulator() {
                return VarInt::addInt;
            }

            @Override
            public BinaryOperator<VarInt> combiner() {
                return (list1, list2) -> {
                    IntIterator it = list2.iterator();
                    while (it.hasNext()) {
                        list1.addInt(it.nextInt());
                    }
                    return list1;
                };
            }

            @Override
            public Function<VarInt, Mapping> finisher() {
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

