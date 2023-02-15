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

package rapaio.math.tensor;

import rapaio.math.tensor.iterators.ChunkIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.storage.Storage;
import rapaio.printer.Printable;

public interface Tensor<N extends Number, S extends Storage<N>, T extends Tensor<N, S, T>> extends Printable {

    TensorManager manager();

    /**
     * @return shape of the tensor
     */
    Shape shape();

    S storage();

    N getValue(int... idxs);

    void setValue(N value, int... idxs);

    default PointerIterator pointerIterator() {
        return pointerIterator(Order.S);
    }

    PointerIterator pointerIterator(Order askOrder);

    default ChunkIterator chunkIterator() {
        return chunkIterator(Order.S);
    }

    ChunkIterator chunkIterator(Order askOrder);

    /**
     * Creates a new tensor with a different shape. If possible, the data will not be copied.
     * If data is copied, the result will be a dense tensor of default order.
     * <p>
     * In order to reshape a tensor, the source shape and destination shape must have the same size.
     *
     * @param shape destination shape
     * @return new tensor instance, wrapping, if possible, the data from the old tensor.
     */
    default Tensor<N, S, T> reshape(Shape shape) {
        return reshape(shape, Order.defaultOrder());
    }

    /**
     * Creates a new tensor with a different shape. If possible, the data will not be copied.
     * <p>
     * In order to reshape a tensor, the source shape and destination shape must have the same size.
     *
     * @param shape destination shape
     * @param askOrder destination order, if the data will be copied, otherwise the parameter is ignored.
     * @return new tensor instance, wrapping, if possible, the data from the old tensor.
     */
    Tensor<N, S, T> reshape(Shape shape, Order askOrder);

    /**
     * Transpose of a tensor. A transposed tensor is a tensor which reverts axis, the first axis becomes the last,
     * the second axis becomes the second to last and so on. Data storage remain the same, no new storage copy is created.
     * As such, any modification on a transposed tensor will affect the original tensor.
     *
     * @return a transposed view of the tensor
     */
    Tensor<N, S, T> t();

    /**
     * Creates a copy of the original tensor with the given order. Only {@link Order#C} or {@link Order#F} are allowed.
     *
     * @param askOrder desired order of the copy tensor.
     * @return new copy of the tensor
     */
    Tensor<N, S, T> copy(Order askOrder);
}
