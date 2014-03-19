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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class VSpots implements Stream<VSpot> {

    private final Stream<VSpot> stream;

    public VSpots(Stream<VSpot> stream) {
        this.stream = stream;
    }

    @Override
    public VSpots filter(Predicate<? super VSpot> predicate) {
        return new VSpots(stream.filter(predicate));
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

    public DoubleStream mapToDouble() {
        return mapToDouble((VSpot inst) -> inst.getValue());
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
        return new VSpots(stream.distinct());
    }

    @Override
    public VSpots sorted() {
        return new VSpots(stream.sorted());
    }

    @Override
    public VSpots sorted(Comparator<? super VSpot> comparator) {
        return new VSpots(stream.sorted(comparator));
    }

    @Override
    public VSpots peek(Consumer<? super VSpot> action) {
        return new VSpots(stream.peek(action));
    }

    @Override
    public VSpots limit(long maxSize) {
        return new VSpots(stream.limit(maxSize));
    }

    @Override
    public VSpots skip(long n) {
        return new VSpots(stream.skip(n));
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
        return new VSpots(stream.sequential());
    }

    @Override
    public VSpots parallel() {
        return new VSpots(stream.parallel());
    }

    @Override
    public VSpots unordered() {
        return new VSpots(stream.unordered());
    }

    @Override
    public VSpots onClose(Runnable closeHandler) {
        return new VSpots(stream.onClose(closeHandler));
    }

    @Override
    public void close() {
        stream.close();
    }

    public VSpots complete() {
        return new VSpots(stream.filter((VSpot inst) -> !inst.isMissing()));
    }
}
