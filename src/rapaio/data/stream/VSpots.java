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

package rapaio.data.stream;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import rapaio.data.*;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
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
 * Stream of variable spots which enrich the standard java streams with some specific
 * operations.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VSpots implements Stream<VSpot>, Serializable {

    private static final long serialVersionUID = 6173562979588338610L;

    private Stream<VSpot> stream;
    private final Var source;

    /**
     * Builds a stream of variable spots based on a standard java stream of spots
     */
    public VSpots(Var source) {
        this.stream = StreamSupport.stream(new VSpotSpliterator(source, 0, source.rowCount(), 0), false);
        this.source = source;
    }

    @Override
    public VSpots filter(Predicate<? super VSpot> predicate) {
        stream = stream.filter(predicate);
        return this;
    }

    public VSpots filterValue(Predicate<Double> predicate) {
        stream = stream.filter(s -> predicate.test(s.getDouble()));
        return this;
    }

    @Override
    public <R> Stream<R> map(Function<? super VSpot, ? extends R> mapper) {
        return stream.map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super VSpot> mapper) {
        return stream.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super VSpot> mapper) {
        return stream.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super VSpot> mapper) {
        return stream.mapToDouble(mapper);
    }

    /**
     * Map the observations to the numerical value given by {@link VSpot#getDouble()}
     *
     * @return stream of numerical values
     */
    public DoubleStream mapToDouble() {
        return mapToDouble(VSpot::getDouble);
    }

    /**
     * Map the observations to the index value given by {@link VSpot#getInt()}
     *
     * @return stream of integer values
     */
    public IntStream mapToInt() {
        return mapToInt(VSpot::getInt);
    }

    /**
     * Map the observations to the label value given by {@link VSpot#getLabel()}
     *
     * @return stream of label values
     */
    public Stream<String> mapToString() {
        return map(VSpot::getLabel);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super VSpot, ? extends Stream<? extends R>> mapper) {
        return stream.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super VSpot, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super VSpot, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super VSpot, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    @Override
    public VSpots distinct() {
        stream = stream.distinct();
        return this;
    }

    @Override
    public VSpots sorted() {
        stream = stream.sorted();
        return this;
    }

    @Override
    public VSpots sorted(Comparator<? super VSpot> comparator) {
        stream = stream.sorted(comparator);
        return this;
    }

    @Override
    public VSpots peek(Consumer<? super VSpot> action) {
        stream = stream.peek(action);
        return this;
    }

    @Override
    public VSpots limit(long maxSize) {
        stream = stream.limit(maxSize);
        return this;
    }

    @Override
    public VSpots skip(long n) {
        stream = stream.skip(n);
        return this;
    }

    @Override
    public void forEach(Consumer<? super VSpot> action) {
        stream.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super VSpot> action) {
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
    public VSpot reduce(VSpot identity, BinaryOperator<VSpot> accumulator) {
        return stream.reduce(identity, accumulator, accumulator);
    }

    @Override
    public Optional<VSpot> reduce(BinaryOperator<VSpot> accumulator) {
        return stream.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super VSpot, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super VSpot> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super VSpot, A, R> collector) {
        return stream.collect(collector);
    }

    @Override
    public Optional<VSpot> min(Comparator<? super VSpot> comparator) {
        return stream.min(comparator);
    }

    @Override
    public Optional<VSpot> max(Comparator<? super VSpot> comparator) {
        return stream.max(comparator);
    }

    @Override
    public long count() {
        return stream.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super VSpot> predicate) {
        return stream.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super VSpot> predicate) {
        return stream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super VSpot> predicate) {
        return stream.noneMatch(predicate);
    }

    @Override
    public Optional<VSpot> findFirst() {
        return stream.findFirst();
    }

    @Override
    public Optional<VSpot> findAny() {
        return stream.findAny();
    }

    @Override
    public Iterator<VSpot> iterator() {
        return stream.iterator();
    }

    @Override
    public Spliterator<VSpot> spliterator() {
        return stream.spliterator();
    }

    @Override
    public boolean isParallel() {
        return stream.isParallel();
    }

    @Override
    public VSpots sequential() {
        stream = stream.sequential();
        return this;
    }

    @Override
    public VSpots parallel() {
        stream = stream.parallel();
        return this;
    }

    @Override
    public VSpots unordered() {
        stream = stream.unordered();
        return this;
    }

    @Override
    public VSpots onClose(Runnable closeHandler) {
        stream = stream.onClose(closeHandler);
        return this;
    }

    @Override
    public void close() {
        stream.close();
    }

    /**
     * Filters the spot stream by removing all spots which contains missing values
     *
     * @return stream with complete spots
     */
    public VSpots complete() {
        stream = stream.filter(s -> !s.isMissing());
        return this;
    }

    /**
     * Filters the spot stream by removing all spots which does not contain missing values
     *
     * @return stream with spots with missing values
     */
    public VSpots incomplete() {
        stream = stream.filter(VSpot::isMissing);
        return this;
    }

    /**
     * Builds a mapped variable which contains all the observations from the stream.
     * This is a terminal operation.
     *
     * @return new mapped variable
     */
    public MappedVar toMappedVar() {
        int[] rows = stream.mapToInt(VSpot::row).toArray();
        return MappedVar.byRows(source, Mapping.wrap(IntArrayList.wrap(rows)));
    }
}

