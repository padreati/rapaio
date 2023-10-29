/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.tensor.iterators;

import rapaio.util.IntIterator;

/**
 * A chunk iterator iterates over chunks of contiguous blocks of memory.
 *
 * All chunks are of the same size. The iterator is backed by a {@link IntIterator}
 */
public interface ChunkIterator extends IntIterator {

    /**
     * @return number of chunks
     */
    int chunkCount();

    /**
     * @return chunk size
     */
    int loopSize();

    int loopStep();

    default int loopBound() {
        return loopSize() * loopStep();
    }
}

