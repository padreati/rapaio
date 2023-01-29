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

import rapaio.math.tensor.iterators.TensorChunkIterator;
import rapaio.math.tensor.iterators.TensorPointerIterator;
import rapaio.math.tensor.storage.Storage;
import rapaio.printer.Printable;

public interface Tensor<N extends Number, S extends Storage<N>, T extends Tensor<N, S, T>> extends Printable {

    enum Type {
        DenseRow,
        DenseCol,
        Stride,
        Map;

        public static Type defaultType() {
            return DenseRow;
        }
    }

    enum Order {
        RowMajor,
        ColMajor,
        Storage;
    }


    /**
     * @return shape of the tensor
     */
    Shape shape();

    Type type();

    S storage();

    N get(int... idxs);

    void set(N value, int... idxs);

    TensorPointerIterator pointerIterator(Order order);

    TensorChunkIterator chunkIterator();

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
        return reshape(shape, Tensor.Type.defaultType());
    }

    /**
     * Creates a new tensor with a different shape. If possible, the data will not be copied.
     * <p>
     * In order to reshape a tensor, the source shape and destination shape must have the same size.
     *
     * @param shape destination shape
     * @param type  destination type, if the data will be copied, otherwise the parameter is ignored.
     * @return new tensor instance, wrapping, if possible, the data from the old tensor.
     */
    Tensor<N, S, T> reshape(Shape shape, Tensor.Type type);

    /**
     * Transpose of a tensor.
     *
     * @return
     */
    Tensor<N, S, T> t();
}
