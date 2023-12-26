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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.experiment.util.stream;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Various useful stream collectors.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/19/15.
 */
@Deprecated
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
                    right.forEach((key, value) -> left.put(key, left.containsKey(key)
                            ? left.get(key)
                            + value : value));
                    return left;
                };
            }

            @Override
            public Function<Map<K, Long>, NavigableMap<Long, M>> finisher() {
                return m1 -> m1.entrySet().stream()
                        .collect(Collectors.groupingBy(Map.Entry::getValue, TreeMap::new, mapping(Map.Entry::getKey, downstream)))
                        .descendingMap();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.noneOf(Characteristics.class);
            }
        };
    }

    public static <T> Collector<T, List<List<T>>, List<List<T>>> slidingCollector(int size, int step) {
        final int window = Math.max(size, step);

        return new Collector<>() {

            private final Queue<T> buffer = new ArrayDeque<>();
            private int totalIn = 0;

            @Override
            public Supplier<List<List<T>>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<List<T>>, T> accumulator() {
                return (lists, t) -> {
                    buffer.offer(t);
                    ++totalIn;
                    if (buffer.size() == window) {
                        dumpCurrent(lists);
                        shiftBy(step);
                    }
                };
            }

            @Override
            public Function<List<List<T>>, List<List<T>>> finisher() {
                return lists -> {
                    if (!buffer.isEmpty()) {
                        final int totalOut = estimateTotalOut();
                        if (totalOut > lists.size()) {
                            dumpCurrent(lists);
                        }
                    }
                    return lists;
                };
            }

            private int estimateTotalOut() {
                return Math.max(0, (totalIn + step - size - 1) / step) + 1;
            }

            private void dumpCurrent(List<List<T>> lists) {
                final List<T> batch = buffer.stream().limit(size).collect(Collectors.toList());
                lists.add(batch);
            }

            private void shiftBy(int by) {
                for (int i = 0; i < by; i++) {
                    buffer.remove();
                }
            }

            @Override
            public BinaryOperator<List<List<T>>> combiner() {
                return (l1, l2) -> {
                    throw new UnsupportedOperationException("Combining not possible");
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.noneOf(Characteristics.class);
            }
        };
    }
}
