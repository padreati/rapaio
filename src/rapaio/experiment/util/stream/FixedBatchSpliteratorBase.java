/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.util.stream;

import static java.util.Spliterators.spliterator;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

@Deprecated
public abstract class FixedBatchSpliteratorBase<T> implements Spliterator<T> {
    private final int batchSize;
    private final int characteristics;
    private long est;

    public FixedBatchSpliteratorBase(int characteristics, int batchSize, long est) {
        characteristics |= ORDERED;
        if ((characteristics & SIZED) != 0) characteristics |= SUBSIZED;
        this.characteristics = characteristics;
        this.batchSize = batchSize;
        this.est = est;
    }

    public FixedBatchSpliteratorBase(int characteristics, int batchSize) {
        this(characteristics, batchSize, Long.MAX_VALUE);
    }

    public FixedBatchSpliteratorBase(int characteristics) {
        this(characteristics, 64, Long.MAX_VALUE);
    }

    @Override
    public Spliterator<T> trySplit() {
        final HoldingConsumer<T> holder = new HoldingConsumer<>();
        if (!tryAdvance(holder)) return null;
        final Object[] a = new Object[batchSize];
        int j = 0;
        do a[j] = holder.value; while (++j < batchSize && tryAdvance(holder));
        if (est != Long.MAX_VALUE) est -= j;
        return spliterator(a, 0, j, characteristics());
    }

    @Override
    public Comparator<? super T> getComparator() {
        if (hasCharacteristics(SORTED)) return null;
        throw new IllegalStateException();
    }

    @Override
    public long estimateSize() {
        return est;
    }

    @Override
    public int characteristics() {
        return characteristics;
    }

    static final class HoldingConsumer<T> implements Consumer<T> {
        Object value;

        @Override
        public void accept(T value) {
            this.value = value;
        }
    }
}