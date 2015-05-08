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
 *
 */

package rapaio.util.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@Deprecated
public class FixedBatchSpliterator<T> extends FixedBatchSpliteratorBase<T> {
    private final Spliterator<T> spliterator;

    public FixedBatchSpliterator(Spliterator<T> toWrap, int batchSize, long est) {
        super(toWrap.characteristics(), batchSize, est);
        this.spliterator = toWrap;
    }

    public FixedBatchSpliterator(Spliterator<T> toWrap, int batchSize) {
        this(toWrap, batchSize, toWrap.estimateSize());
    }

    public FixedBatchSpliterator(Spliterator<T> toWrap) {
        this(toWrap, 64, toWrap.estimateSize());
    }

    public static <T> Stream<T> withBatchSize(Stream<T> in, int batchSize) {
        return stream(new FixedBatchSpliterator<>(in.spliterator(), batchSize), true);
    }

    public static <T> FixedBatchSpliterator<T> batchedSpliterator(Spliterator<T> toWrap, int batchSize) {
        return new FixedBatchSpliterator<>(toWrap, batchSize);
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        return spliterator.tryAdvance(action);
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        spliterator.forEachRemaining(action);
    }
}