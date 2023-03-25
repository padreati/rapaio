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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.iterators;

import java.util.NoSuchElementException;

import rapaio.math.tensor.Shape;

public final class DensePointerIterator implements PointerIterator {

    private final int offset;
    private final int size;
    private final int step;

    private int pointer;

    public DensePointerIterator(Shape shape, int offset, int step) {
        this.offset = offset;
        this.size = shape.size() * step + offset;
        this.step = step;
        this.pointer = offset;
    }

    @Override
    public int nextInt() {
        if (pointer >= size) {
            throw new NoSuchElementException();
        }
        pointer += step;
        return pointer - step;
    }

    @Override
    public boolean hasNext() {
        return pointer < size;
    }

    @Override
    public int position() {
        return (pointer - offset) / step - 1;
    }
}
