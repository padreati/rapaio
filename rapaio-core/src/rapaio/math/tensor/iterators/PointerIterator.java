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

import java.util.PrimitiveIterator;

/**
 * Iterator over pointers. A pointer is an unidimensional index into the storage
 * of the tensor. Each tensor stores its data into a contiguous block of memory.
 * The API which allows accessing data through pointers are {@link rapaio.math.tensor.DTensor#getAtDouble(int)}
 * and {@link rapaio.math.tensor.DTensor#setAtDouble(int, double)}, with similar methods for other types
 * of tensors.
 * <p>
 * Pointers are used to access the tensor data in a more direct way.
 * <p>
 * The iterator is backed by a {@link PrimitiveIterator.OfInt}
 */
public interface PointerIterator extends PrimitiveIterator.OfInt {

    /**
     * Returns next pointer, if available.
     *
     * @return next pointer
     * @throws java.util.NoSuchElementException if there is no next element
     */
    @Override
    int nextInt();

    /**
     * Returns position for the corresponding pointer.
     *
     * @return current position
     */
    int position();
}

