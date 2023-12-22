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

import java.util.Collection;
import java.util.Random;

import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.mill.barray.BaseArrayTensorMill;
import rapaio.math.tensor.mill.varray.VectorizedArrayTensorMill;

public interface TensorMill {

    static TensorMill defaultMill() {
        return barray();
    }

    static TensorMill barray() {
        return new BaseArrayTensorMill();
    }

    static TensorMill barray(int cpuThreads) {
        return new BaseArrayTensorMill(cpuThreads);
    }

    static TensorMill varray() {
        return new VectorizedArrayTensorMill();
    }

    static TensorMill varray(int cpuThreads) {
        return new VectorizedArrayTensorMill(cpuThreads);
    }

    interface OfType<N extends Number, T extends Tensor<N, T>> {

        DType<N, T> dtype();

        T scalar(N value);

        default T zeros(Shape shape) {
            return zeros(shape, Order.defaultOrder());
        }

        T zeros(Shape shape, Order order);

        default T eye(int n) {
            return eye(n, Order.defaultOrder());
        }

        T eye(int n, Order order);

        default T full(Shape shape, N value) {
            return full(shape, value, Order.defaultOrder());
        }

        T full(Shape shape, N value, Order order);

        default T seq(Shape shape) {
            return seq(shape, Order.defaultOrder());
        }

        T seq(Shape shape, Order order);

        default T random(Shape shape, Random random) {
            return random(shape, random, Order.defaultOrder());
        }

        T random(Shape shape, Random random, Order order);

        default T stride(Shape shape, Order order, byte[] array) {
            return stride(StrideLayout.ofDense(shape, 0, order), array);
        }

        default T stride(StrideLayout layout, byte[] array) {
            return stride(layout.shape(), layout.offset(), layout.strides(), array);
        }

        T stride(Shape shape, int offset, int[] strides, byte[] array);

        default T stride(Shape shape, Order order, int[] array) {
            return stride(StrideLayout.ofDense(shape, 0, order), array);
        }

        default T stride(StrideLayout layout, int[] array) {
            return stride(layout.shape(), layout.offset(), layout.strides(), array);
        }

        T stride(Shape shape, int offset, int[] strides, int[] array);

        default T stride(Shape shape, Order order, float[] array) {
            return stride(StrideLayout.ofDense(shape, 0, order), array);
        }

        default T stride(StrideLayout layout, float[] array) {
            return stride(layout.shape(), layout.offset(), layout.strides(), array);
        }

        T stride(Shape shape, int offset, int[] strides, float[] array);

        default T stride(Shape shape, Order order, double[] array) {
            return stride(StrideLayout.ofDense(shape, 0, order), array);
        }

        default T stride(StrideLayout layout, double[] array) {
            return stride(layout.shape(), layout.offset(), layout.strides(), array);
        }

        T stride(Shape shape, int offset, int[] strides, double[] array);
    }

    default <N extends Number, T extends Tensor<N, T>> T scalar(DType<N, T> dType, N value) {
        return ofType(dType).scalar(value);
    }

    default <N extends Number, T extends Tensor<N, T>> T zeros(DType<N, T> dType, Shape shape) {
        return ofType(dType).zeros(shape, Order.defaultOrder());
    }

    default <N extends Number, T extends Tensor<N, T>> T zeros(DType<N, T> dType, Shape shape, Order order) {
        return ofType(dType).zeros(shape, order);
    }

    default <N extends Number, T extends Tensor<N, T>> T eye(DType<N, T> dType, int n) {
        return ofType(dType).eye(n, Order.defaultOrder());
    }

    default <N extends Number, T extends Tensor<N, T>> T eye(DType<N, T> dType, int n, Order order) {
        return ofType(dType).eye(n, order);
    }

    default <N extends Number, T extends Tensor<N, T>> T full(DType<N, T> dType, Shape shape, N value) {
        return ofType(dType).full(shape, value, Order.defaultOrder());
    }

    default <N extends Number, T extends Tensor<N, T>> T full(DType<N, T> dType, Shape shape, N value, Order order) {
        return ofType(dType).full(shape, value, order);
    }

    default <N extends Number, T extends Tensor<N, T>> T seq(DType<N, T> dType, Shape shape) {
        return ofType(dType).seq(shape, Order.defaultOrder());
    }

    default <N extends Number, T extends Tensor<N, T>> T seq(DType<N, T> dType, Shape shape, Order order) {
        return ofType(dType).seq(shape, order);
    }

    default <N extends Number, T extends Tensor<N, T>> T random(DType<N, T> dType, Shape shape, Random random) {
        return ofType(dType).random(shape, random, Order.defaultOrder());
    }

    default <N extends Number, T extends Tensor<N, T>> T random(DType<N, T> dType, Shape shape, Random random, Order order) {
        return ofType(dType).random(shape, random, order);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, Shape shape, Order order, byte[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, StrideLayout layout, byte[] array) {
        return ofType(dType).stride(layout.shape(), layout.offset(), layout.strides(), array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, Shape shape, int offset, int[] strides, byte[] array) {
        return ofType(dType).stride(shape, offset, strides, array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, Shape shape, Order order, int[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, StrideLayout layout, int[] array) {
        return ofType(dType).stride(layout.shape(), layout.offset(), layout.strides(), array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, Shape shape, int offset, int[] strides, int[] array) {
        return ofType(dType).stride(shape, offset, strides, array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, Shape shape, Order order, float[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, StrideLayout layout, float[] array) {
        return ofType(dType).stride(layout.shape(), layout.offset(), layout.strides(), array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, Shape shape, int offset, int[] strides, float[] array) {
        return ofType(dType).stride(shape, offset, strides, array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, Shape shape, Order order, double[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, StrideLayout layout, double[] array) {
        return ofType(dType).stride(layout.shape(), layout.offset(), layout.strides(), array);
    }

    default <N extends Number, T extends Tensor<N, T>> T stride(DType<N, T> dType, Shape shape, int offset, int[] strides, double[] array) {
        return ofType(dType).stride(shape, offset, strides, array);
    }

    OfType<Double, DoubleTensor> ofDouble();

    OfType<Float, FloatTensor> ofFloat();

    OfType<Integer, IntTensor> ofInt();

    OfType<Byte, ByteTensor> ofByte();

    @SuppressWarnings("unchecked")
    default <N extends Number, T extends Tensor<N, T>> OfType<N, T> ofType(DType<N, T> dType) {
        if (dType.equals(DTypes.FLOAT)) {
            return (OfType<N, T>) ofFloat();
        }
        if (dType.equals(DTypes.DOUBLE)) {
            return (OfType<N, T>) ofDouble();
        }
        if (dType.equals(DTypes.INTEGER)) {
            return (OfType<N, T>) ofInt();
        }
        if(dType.equals(DTypes.BYTE)) {
            return (OfType<N, T>) ofByte();
        }
        return null;
    }

    int cpuThreads();

    /**
     * Concatenates multiple tensors along a given axis.
     * Tensors must have compatible size, all other dimensions must be equal.
     *
     * @param axis    axis to concatenate along
     * @param tensors tensors to concatenate
     * @return new tensor with concatenated data
     */
    <N extends Number, T extends Tensor<N, T>> T concat(int axis, Collection<? extends T> tensors);

    /**
     * Concatenates multiple tensors along a new axis.
     * All tensors must have the same shape. The position of the new axis is between 0 (inclusive)
     * and the number of dimensions (inclusive).
     *
     * @param axis    index of the new dimension
     * @param tensors tensors to concatenate
     * @return new tensor with concatenated data
     */
    <N extends Number, T extends Tensor<N, T>> T stack(int axis, Collection<? extends T> tensors);
}
