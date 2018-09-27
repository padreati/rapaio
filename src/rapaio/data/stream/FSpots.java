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

package rapaio.data.stream;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Stream of frame spots.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class FSpots implements Stream<FSpot>, Serializable {

    private static final long serialVersionUID = -1062266227832968382L;

    private final Frame source;
    private Stream<FSpot> stream;

    public FSpots(Frame source) {
        this.stream = StreamSupport.stream(new FSpotSpliterator(source, 0, source.rowCount(), 0), false);
        this.source = source;
    }

    @Override
    public FSpots filter(Predicate<? super FSpot> predicate) {
        stream = stream.filter(predicate);
        return this;
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
        stream = stream.distinct();
        return this;
    }

    @Override
    public FSpots sorted() {
        stream = stream.sorted();
        return this;
    }

    @Override
    public FSpots sorted(Comparator<? super FSpot> comparator) {
        stream = stream.sorted(comparator);
        return this;
    }

    @Override
    public FSpots peek(Consumer<? super FSpot> action) {
        stream = stream.peek(action);
        return this;
    }

    @Override
    public FSpots limit(long maxSize) {
        stream = stream.limit(maxSize);
        return this;
    }

    @Override
    public FSpots skip(long n) {
        stream = stream.skip(n);
        return this;
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
    public Iterator<FSpot> iterator() {
        return stream.iterator();
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
        stream = stream.sequential();
        return this;
    }

    @Override
    public FSpots parallel() {
        stream = stream.parallel();
        return this;
    }

    @Override
    public FSpots unordered() {
        stream = stream.unordered();
        return this;
    }

    @Override
    public FSpots onClose(Runnable closeHandler) {
        stream = stream.onClose(closeHandler);
        return this;
    }

    @Override
    public void close() {
        stream.close();
    }

    /**
     * Filters the stream leaving in the stream only the spots which contains no missing values on any of the variables
     * @return list of complete (non-missing) frame spots
     */
    public FSpots complete() {
        return filter(s -> !s.isMissing());
    }

    /**
     * Filters the stream leaving in the stream only the spots which contains missing values on any of the variables
     * @return list of incomplete (missing) frame spots
     */
    public FSpots incomplete() {
        return filter(FSpot::isMissing);
    }

    /**
     * Collects the elements of the stream into a list of spots
     * @return list of collected spots
     */
    public List<FSpot> collectFSpotList() {
        final List<FSpot> list = new ArrayList<>();
        forEach(list::add);
        return list;
    }

    /**
     * Map spots into row numbers and collect them into a list
     * @return lit of collected row numbers
     */
    public List<Integer> collectRowList() {
        final List<Integer> list = new ArrayList<>();
        forEach(spot -> list.add(spot.row()));
        return list;
    }

    /**
     * Map spots into row numbers and collect the into a mapping
     * @return mapping of collected row numbers
     */
    public Mapping collectMapping() {
        final IntList m = new IntArrayList();
        forEach(s -> m.add(s.row()));
        return Mapping.wrap(m);
    }

    /**
     * Builds a mapped frame from stream spots
     * @return mapped frame with spots from the stream
     */
    public Frame toMappedFrame() {
        return MappedFrame.byRow(source, collectMapping());
    }
}
