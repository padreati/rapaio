/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
 */

package rapaio.stream;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Various useful stream collectors.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/19/15.
 */
public class SCollectors {

    public static <K> Collector<K, ?, NavigableMap<Long, List<K>>> countingTop() {
        return countingTop(k -> k);
    }

    public static <K, M> Collector<K, ?, NavigableMap<Long, M>> countingTop(Collector<K, ?, M> downstream) {
        return countingTop(k -> k, downstream);
    }

    public static <K, T> Collector<T, ?, NavigableMap<Long, List<K>>> countingTop(Function<T, K> classifier) {
        return countingTop(classifier, toList());
    }

    /**
     * Builds a group by counting in reverse order.
     *
     * @param classifier maps input type to the key used for grouping
     * @param <T>        class of the original type
     * @param <K>        class of the key type
     * @return a navigable map in reverse order
     */
    public static <T, K, M> Collector<T, ?, NavigableMap<Long, M>> countingTop(Function<T, K> classifier, Collector<K, ?, M> downstream) {

        return new Collector<T, Map<K, Long>, NavigableMap<Long, M>>() {

            @Override
            public Supplier<Map<K, Long>> supplier() {
                return HashMap::new;
            }

            @Override
            public BiConsumer<Map<K, Long>, T> accumulator() {
                return (acc, obj) -> {
                    K k = classifier.apply(obj);
                    Long counter = acc.get(k);
                    if (counter != null) {
                        acc.put(k, acc.get(k) + 1);
                    } else {
                        acc.put(k, 1L);
                    }
                };
            }

            @Override
            public BinaryOperator<Map<K, Long>> combiner() {
                return (left, right) -> {
                    right.entrySet()
                            .forEach(e -> left.put(e.getKey(), left.containsKey(e.getKey())
                                    ? left.get(e.getKey())
                                    + e.getValue() : e.getValue()));
                    return left;
                };
            }

            @Override
            public Function<Map<K, Long>, NavigableMap<Long, M>> finisher() {
                return m1 -> m1.entrySet().stream()
                        .collect(Collectors.groupingBy(Map.Entry::getValue, TreeMap::new, Collectors.mapping(Map.Entry::getKey, downstream)))
                        .descendingMap();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.noneOf(Characteristics.class);
            }
        };
    }
}
