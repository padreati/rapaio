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

package rapaio.experiment.math.tensor.iterators;

import java.util.NoSuchElementException;

import rapaio.experiment.math.tensor.Shape;
import rapaio.util.collection.IntArrays;

public final class TensorStoreMajorIterator implements TensorPointerIterator {

    private final Shape shape;
    private final int[] strides;
    private final int[] index;
    private int position = 0;
    private int pointer;
    private int newPointer;

    public TensorStoreMajorIterator(Shape shape, int offset, int[] strides) {
        int[] priority = new int[shape.dims().length];
        for (int i = 0; i < priority.length; i++) {
            priority[i] = i;
        }
        IntArrays.quickSort(priority, (i, j) -> {
            int cmp = Integer.compare(strides[i], strides[j]);
            if (cmp == 0) {
                return Integer.compare(shape.dim(i), shape.dim(j));
            }
            return cmp < 0 ? -1 : 1;
        });

        this.shape = Shape.of(IntArrays.newPermutation(shape.dims(), priority));
        this.strides = IntArrays.newPermutation(strides, priority);
        this.pointer = offset;
        this.newPointer = offset;
        this.index = new int[shape.rank()];
    }

    @Override
    public boolean hasNext() {
        return position < shape.size();
    }

    @Override
    public int nextInt() {
        if (position >= shape.size()) {
            throw new NoSuchElementException();
        }
        pointer = newPointer;
        position++;
        int i = 0;
        index[i]++;
        newPointer += strides[i];
        while (i < strides.length) {
            if (index[i] == shape.dim(i)) {
                index[i] = 0;
                newPointer -= shape.dim(i) * strides[i];
                if (i < strides.length - 1) {
                    index[i + 1]++;
                    newPointer += strides[i + 1];
                }
                i++;
                continue;
            }
            break;
        }
        return pointer;
    }

    @Override
    public int getPosition() {
        return position - 1;
    }
}
