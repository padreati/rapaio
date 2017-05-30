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

package rapaio.data.stream;

import rapaio.data.MappedVar;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.experiment.util.stream.StreamUtil;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * Stream of variable spots which enrich the standard java streams with some specific
 * operations.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class VSpots implements Stream<VSpot>, Serializable {

    private final Stream<VSpot> stream;
    private final Var source;

    /**
     * Builds a stream of variable spots based on a standard java stream of spots
     *
     * @param stream nested stream
     */
    public VSpots(Stream<VSpot> stream, Var source) {
        this.stream = stream;
        this.source = source;
    }

    @Override
    public VSpots filter(Predicate<? super VSpot> predicate) {
        return new VSpots(stream.filter(predicate), source);
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
     * Map the observations to the numerical value given by {@link VSpot#getValue()}
     *
     * @return stream of numerical values
     */
    public DoubleStream mapToDouble() {
        return mapToDouble(VSpot::getValue);
    }

    /**
     * Map the observations to the index value given by {@link VSpot#getIndex()}
     *
     * @return stream of integer values
     */
    public IntStream mapToInt() {
        return mapToInt(VSpot::getIndex);
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
        return new VSpots(stream.distinct(), source);
    }

    @Override
    public VSpots sorted() {
        return new VSpots(stream.sorted(), source);
    }

    @Override
    public VSpots sorted(Comparator<? super VSpot> comparator) {
        return new VSpots(stream.sorted(comparator), source);
    }

    @Override
    public VSpots peek(Consumer<? super VSpot> action) {
        return new VSpots(stream.peek(action), source);
    }

    @Override
    public VSpots limit(long maxSize) {
        return new VSpots(stream.limit(maxSize), source);
    }

    @Override
    public VSpots skip(long n) {
        return new VSpots(stream.skip(n), source);
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
        return new VSpots(stream.sequential(), source);
    }

    @Override
    public VSpots parallel() {
        return new VSpots(stream.parallel(), source);
    }

    @Override
    public VSpots unordered() {
        return new VSpots(stream.unordered(), source);
    }

    @Override
    public VSpots onClose(Runnable closeHandler) {
        return new VSpots(stream.onClose(closeHandler), source);
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
        return new VSpots(stream.filter(s -> !s.isMissing()), source);
    }

    /**
     * Filters the spot stream by removing all spots which does not contain missing values
     *
     * @return stream with spots with missing values
     */
    public VSpots incomplete() {
        return new VSpots(stream.filter(VSpot::isMissing), source);
    }

    /**
     * Builds a stream of spot streams, each stream having the size given by {@param groupSize}
     *
     * @param groupSize the size of the groups which forms each stream
     * @return a stream of streams
     */
    public Stream<VSpots> group(int groupSize) {
        return StreamUtil.partition(stream, groupSize).map(list -> new VSpots(list.stream(), source));
    }

    /**
     * Makes a string which contains a concatenation of mapped values
     *
     * @param mapper mapper used to transform a spot into a specific value
     * @return a string made from the concatenation of mapped values
     */
    public <R> String mkString(Function<VSpot, R> mapper) {
        StringBuilder sb = new StringBuilder();
        Iterator<R> it = stream.map(mapper).iterator();
        while (it.hasNext()) {
            if (sb.length() != 0) sb.append(",");
            sb.append(it.next().toString());
        }
        return "[" + sb.toString() + "]";
    }

    /**
     * Applies a given transformation to all the numerical values of the underlying variable
     *
     * @param trans given transformation
     */
    public VSpots transValue(Function<Double, Double> trans) {
        return new VSpots(stream.map(spot -> {
            spot.setValue(trans.apply(spot.getValue()));
            return spot;
        }), source);
    }

    /**
     * Applies a given transformation to all index values of the underlying variable
     *
     * @param trans given transformation
     */
    public VSpots transIndex(Function<Integer, Integer> trans) {
        return new VSpots(stream.map(spot -> {
            spot.setIndex(trans.apply(spot.getIndex()));
            return spot;
        }), source);
    }

    /**
     * Applies a given transformation to all label values of the underlying variable
     *
     * @param trans given transformation
     */
    public VSpots transLabel(Function<String, String> trans) {
        return new VSpots(stream.map(spot -> {
            spot.setLabel(trans.apply(spot.getLabel()));
            return spot;
        }), source);
    }

    /**
     * Builds a mapped variable which contains all the observations from the stream.
     * This is a terminal operation.
     *
     * @return new mapped variable
     */
    public MappedVar toMappedVar() {
        return MappedVar.byRows(source, Mapping.wrap(stream.map(VSpot::getRow).collect(Collectors.toList())));
    }
}
