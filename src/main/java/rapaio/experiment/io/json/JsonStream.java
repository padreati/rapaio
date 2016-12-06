/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.experiment.io.json;

import rapaio.experiment.io.json.tree.JsonValue;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/13/15.
 */
public class JsonStream implements Stream<JsonValue> {

    private final Stream<JsonValue> stream;

    public JsonStream(Stream<JsonValue> stream) {
        this.stream = stream;
    }

    @Override
    public JsonStream filter(Predicate<? super JsonValue> predicate) {
        return new JsonStream(stream.filter(predicate));
    }

    @Override
    public <R> Stream<R> map(Function<? super JsonValue, ? extends R> mapper) {
        return stream.map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super JsonValue> mapper) {
        return stream.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super JsonValue> mapper) {
        return stream.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super JsonValue> mapper) {
        return stream.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super JsonValue, ? extends Stream<? extends R>> mapper) {
        return stream.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super JsonValue, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super JsonValue, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super JsonValue, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    @Override
    public JsonStream distinct() {
        return new JsonStream(stream.distinct());
    }

    @Override
    public JsonStream sorted() {
        return new JsonStream(stream.sorted());
    }

    @Override
    public JsonStream sorted(Comparator<? super JsonValue> comparator) {
        return new JsonStream(stream.sorted(comparator));
    }

    @Override
    public JsonStream peek(Consumer<? super JsonValue> action) {
        return new JsonStream(stream.peek(action));
    }

    @Override
    public JsonStream limit(long maxSize) {
        return new JsonStream(stream.limit(maxSize));
    }

    @Override
    public JsonStream skip(long n) {
        return new JsonStream(stream.skip(n));
    }

    @Override
    public void forEach(Consumer<? super JsonValue> action) {
        stream.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super JsonValue> action) {
        stream.forEachOrdered(action);
    }

    @Override
    public JsonValue[] toArray() {
        List<JsonValue> list = collect(toList());
        return list.toArray(new JsonValue[list.size()]);
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    @Override
    public JsonValue reduce(JsonValue identity, BinaryOperator<JsonValue> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    @Override
    public Optional<JsonValue> reduce(BinaryOperator<JsonValue> accumulator) {
        return stream.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super JsonValue, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super JsonValue> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super JsonValue, A, R> collector) {
        return stream.collect(collector);
    }

    @Override
    public Optional<JsonValue> min(Comparator<? super JsonValue> comparator) {
        return stream.min(comparator);
    }

    @Override
    public Optional<JsonValue> max(Comparator<? super JsonValue> comparator) {
        return stream.max(comparator);
    }

    @Override
    public long count() {
        return stream.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super JsonValue> predicate) {
        return stream.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super JsonValue> predicate) {
        return stream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super JsonValue> predicate) {
        return stream.noneMatch(predicate);
    }

    @Override
    public Optional<JsonValue> findFirst() {
        return stream.findFirst();
    }

    @Override
    public Optional<JsonValue> findAny() {
        return stream.findAny();
    }

    @Override
    public Iterator<JsonValue> iterator() {
        return stream.iterator();
    }

    @Override
    public Spliterator<JsonValue> spliterator() {
        return stream.spliterator();
    }

    @Override
    public boolean isParallel() {
        return stream.isParallel();
    }

    @Override
    public JsonStream sequential() {
        return new JsonStream(stream.sequential());
    }

    @Override
    public JsonStream parallel() {
        return new JsonStream(stream.parallel());
    }

    @Override
    public JsonStream unordered() {
        return new JsonStream(stream.unordered());
    }

    @Override
    public JsonStream onClose(Runnable closeHandler) {
        return new JsonStream(stream.onClose(closeHandler));
    }

    @Override
    public void close() {
        stream.close();
    }

    // custom tools

    public <T> Map<Integer, List<T>> countingTop(Function<JsonValue, ? extends T> mapping) {
        final HashMap<T, Integer> countMap = new HashMap<>();
        stream.forEach(js -> {
            final T t = mapping.apply(js);
            Integer cnt = countMap.get(t);
            if (cnt == null)
                cnt = 0;
            cnt++;
            countMap.put(t, cnt);
        });
        return countMap.entrySet().stream().collect(groupingBy(Map.Entry::getValue, TreeMap::new, mapping(Map.Entry::getKey, toList()))).descendingMap();
    }
}
