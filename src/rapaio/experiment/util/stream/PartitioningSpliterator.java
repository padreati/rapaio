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

package rapaio.experiment.util.stream;

import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

@Deprecated
public class PartitioningSpliterator<E> extends AbstractSpliterator<List<E>> {
    private final Spliterator<E> spliterator;
    private final int partitionSize;

    public PartitioningSpliterator(Spliterator<E> toWrap, int partitionSize) {
        super(toWrap.estimateSize(), toWrap.characteristics() | Spliterator.NONNULL);
        if (partitionSize <= 0) throw new IllegalArgumentException(
                "Partition size must be positive, but was " + partitionSize);
        this.spliterator = toWrap;
        this.partitionSize = partitionSize;
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<E>> action) {
        final HoldingConsumer<E> holder = new HoldingConsumer<>();
        if (!spliterator.tryAdvance(holder)) {
            return false;
        }
        final List<E> partition = new LinkedList<>();
        int j = 0;
        do {
            partition.add(holder.value);
        } while (++j < partitionSize && spliterator.tryAdvance(holder));
        action.accept(partition);
        return true;
    }

    @Override
    public long estimateSize() {
        final long est = spliterator.estimateSize();
        return est == Long.MAX_VALUE ? est
                : est / partitionSize + (est % partitionSize > 0 ? 1 : 0);
    }

    static final class HoldingConsumer<T> implements Consumer<T> {
        T value;

        @Override
        public void accept(T value) {
            this.value = value;
        }
    }
}
