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

import java.util.NoSuchElementException;

import rapaio.math.tensor.Shape;

public final class TensorRowMajorIterator implements TensorPointerIterator {

    private final Shape shape;
    private final int[] strides;
    private int position = 0;
    private int newPointer;
    private int pointer;
    private final int[] index;

    public TensorRowMajorIterator(Shape shape, int offset, int[] strides) {
        this.shape = shape;
        this.strides = strides;
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
        position++;
        pointer = newPointer;
        int i = shape.rank() - 1;
        index[i]++;
        newPointer += strides[i];
        while (i >= 0) {
            if (index[i] == shape.dim(i)) {
                index[i] = 0;
                newPointer -= shape.dim(i) * strides[i];
                if (i > 0) {
                    index[i - 1]++;
                    newPointer += strides[i - 1];
                }
                i--;
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
