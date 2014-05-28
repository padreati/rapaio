/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

package rapaio.data.stream;

import rapaio.data.Frame;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.util.Pin;

import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class FSpots implements Stream<FSpot>, Serializable {

    private final Stream<FSpot> stream;

    public FSpots(Stream<FSpot> stream) {
        this.stream = stream;
    }

    public FSpots(List<FSpot> list) {
        this.stream = list.stream();
    }

    @Override
    public FSpots filter(Predicate<? super FSpot> predicate) {
        return new FSpots(stream.filter(predicate));
    }

    public FSpots complete() {
        return filter((FSpot fi) -> !fi.missing());
    }

    @Override
    public <R> Stream<R> map(Function<? super FSpot, ? extends R> mapper) {
        return stream.map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super FSpot> mapper) {
        return stream.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super FSpot> mapper) {
        return stream.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super FSpot> mapper) {
        return stream.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super FSpot, ? extends Stream<? extends R>> mapper) {
        return stream.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super FSpot, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super FSpot, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super FSpot, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    @Override
    public FSpots distinct() {
        return new FSpots(stream.distinct());
    }

    @Override
    public FSpots sorted() {
        return new FSpots(stream.sorted());
    }

    @Override
    public FSpots sorted(Comparator<? super FSpot> comparator) {
        return new FSpots(stream.sorted(comparator));
    }

    @Override
    public FSpots peek(Consumer<? super FSpot> action) {
        return new FSpots(stream.peek(action));
    }

    @Override
    public FSpots limit(long maxSize) {
        return new FSpots(stream.limit(maxSize));
    }

    @Override
    public FSpots skip(long n) {
        return new FSpots(stream.skip(n));
    }

    @Override
    public void forEach(Consumer<? super FSpot> action) {
        stream.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super FSpot> action) {
        stream.forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return stream.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    @Override
    public FSpot reduce(FSpot identity, BinaryOperator<FSpot> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    @Override
    public Optional<FSpot> reduce(BinaryOperator<FSpot> accumulator) {
        return stream.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super FSpot, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super FSpot> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super FSpot, A, R> collector) {
        return stream.collect(collector);
    }

    @Override
    public Optional<FSpot> min(Comparator<? super FSpot> comparator) {
        return stream.min(comparator);
    }

    @Override
    public Optional<FSpot> max(Comparator<? super FSpot> comparator) {
        return stream.max(comparator);
    }

    @Override
    public long count() {
        return stream.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super FSpot> predicate) {
        return stream.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super FSpot> predicate) {
        return stream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super FSpot> predicate) {
        return stream.noneMatch(predicate);
    }

    @Override
    public Optional<FSpot> findFirst() {
        return stream.findFirst();
    }

    @Override
    public Optional<FSpot> findAny() {
        return stream.findAny();
    }

    @Override
    public FIterator iterator() {
        return new FIterator(stream.iterator());
    }

    @Override
    public Spliterator<FSpot> spliterator() {
        return stream.spliterator();
    }

    @Override
    public boolean isParallel() {
        return stream.isParallel();
    }

    @Override
    public FSpots sequential() {
        return new FSpots(stream.sequential());
    }

    @Override
    public FSpots parallel() {
        return new FSpots(stream.parallel());
    }

    @Override
    public FSpots unordered() {
        return new FSpots(stream.unordered());
    }

    @Override
    public FSpots onClose(Runnable closeHandler) {
        return new FSpots(stream.onClose(closeHandler));
    }

    @Override
    public void close() {
        stream.close();
    }

    public List<FSpot> collectFSpotList() {
        final List<FSpot> list = new ArrayList<>();
        forEach(list::add);
        return list;
    }

    public List<Integer> collectRowList() {
        final List<Integer> list = new ArrayList<>();
        forEach(spot -> list.add(spot.row()));
        return list;
    }

    public List<Integer> collectRowIdList() {
        final List<Integer> list = new ArrayList<>();
        forEach(spot -> list.add(spot.rowId()));
        return list;
    }

    public Frame toMappedFrame() {
        final Mapping mapping = new Mapping();
        final Pin<Frame> dfPin = new Pin<>();
        forEach(spot -> {
            mapping.add(spot.rowId());
            if (dfPin.isEmpty()) {
                dfPin.set(spot.getFrame());
            }
        });
        return new MappedFrame(dfPin.get(), mapping);
    }
}
