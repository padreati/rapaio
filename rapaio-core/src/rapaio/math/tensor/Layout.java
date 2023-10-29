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

package rapaio.math.tensor;

/**
 * A layout describes the dimensionality of a tensor and its related properties.
 * The interface {@link Layout} does not propose any constraints regarding how the
 * layout is described: dense or sparse, stride or other indexing method.
 */
public interface Layout {

    /**
     * Tensor shape, external dimensions of the tensor.
     *
     * @return tensor shape
     */
    Shape shape();

    /**
     * Shortcut method for tensor rank obtained from shape. It provides the
     * number of dimensions of the tensor.
     *
     * @return tensor rank.
     */
    int rank();

    /**
     * Number of elements contained in tensor. Shortcut method ontained
     * from shape. It is equal with the product of all dimension sizes.
     *
     * @return tensor shape
     */
    int size();

    /**
     * Tells if the tensor values are stored in C order, row major.
     * This flag does not guarantee that elements are continuous in storage.
     *
     * @return true if elements are row major ordered in storage
     */
    boolean isCOrdered();

    /**
     * Tells if the tensor values are stored in Fortran order, col major.
     * This flag does not guarantee that elements are contguous in storage.
     *
     * @return true if elements are col major ordered in storage
     */
    boolean isFOrdered();

    /**
     * Finds the best order which is closest to how values are stored in data storage.
     * It returns {@link Order#C} or {@link Order#F} for standard row major or col
     * major orderings. If none of the standard orderings are appropriate, then it returns
     * {@link Order#S}.
     *
     * @return ordering closest to how values are stored
     */
    Order storageFastOrder();

    /**
     * Computes a pointer, given an index. A pointer is the location in the storage where the
     * value from the corresponding index is stored.
     *
     * @param index an integer array which logically describes the location of a value in a tensor
     * @return the location in the storage of the indexed element
     */
    int pointer(int... index);

    /**
     * Computes the index of an element, given the pointer of the element in storage
     *
     * @param pointer location of the element in storage
     * @return element index
     */
    int[] index(int pointer);

    /**
     * Removes all dimensions which are equal with 1.
     *
     * @return same layout if no squeezed dimensions exist, otherwise a new layout with all unitary dimensions removed
     */
    Layout squeeze();

    Layout unsqueeze(int axis);

    /**
     * Revert all axes of the layout
     *
     * @return a layout with axes reverted
     */
    Layout revert();

    Layout moveAxis(int src, int dst);

    Layout swapAxis(int src, int dst);
}
