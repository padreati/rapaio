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

import java.util.Random;

import rapaio.math.tensor.layout.StrideLayout;

public interface TensorFactory {

    interface OfDouble {
        DTensor zeros(Shape shape, Order order);

        default DTensor zeros(Shape shape) {
            return zeros(shape, Order.defaultOrder());
        }

        DTensor seq(Shape shape, Order order);

        default DTensor seq(Shape shape) {
            return seq(shape, Order.defaultOrder());
        }

        DTensor random(Shape shape, Random random, Order order);

        default DTensor random(Shape shape, Random random) {
            return random(shape, random, Order.defaultOrder());
        }

        DTensor wrap(Shape shape, double[] array, Order order);

        default DTensor stride(StrideLayout layout, double[] array) {
            return stride(layout.shape(), layout.offset(), layout.strides(), array);
        }

        DTensor stride(Shape shape, int offset, int[] strides, double[] array);

        /**
         * Concatenates multiple tensors along a given axis.
         * Tensors must have compatible size, all other dimensions must be equal.
         *
         * @param axis    axis to concatenate along
         * @param tensors tensors to concatenate
         * @return new tensor with concatenated data
         */
        DTensor concatenate(int axis, DTensor... tensors);

        /**
         * Concatenates multiple tensors along a new axis.
         * All tensors must have the same shape. The position of the new axis is between 0 (inclusive)
         * and the number of dimensions (inclusive).
         *
         * @param axis index of the new dimension
         * @param tensors tensors to concatenate
         * @return new tensor with concatenated data
         */
        DTensor stack(int axis, DTensor... tensors);
    }

    interface OfFloat {

        FTensor zeros(Shape shape, Order order);

        default FTensor zeros(Shape shape) {
            return zeros(shape, Order.defaultOrder());
        }

        FTensor seq(Shape shape, Order order);

        default FTensor seq(Shape shape) {
            return seq(shape, Order.defaultOrder());
        }

        FTensor random(Shape shape, Random random, Order order);

        default FTensor random(Shape shape, Random random) {
            return random(shape, random, Order.defaultOrder());
        }

        FTensor wrap(Shape shape, float[] array, Order order);

        default FTensor stride(StrideLayout layout, float[] array) {
            return stride(layout.shape(), layout.offset(), layout.strides(), array);
        }

        FTensor stride(Shape shape, int offset, int[] strides, float[] array);

        /**
         * Concatenates multiple tensors along a given axis.
         * Tensors must have compatible size, all other dimensions must be equal.
         *
         * @param axis    axis to concatenate along
         * @param tensors tensors to concatenate
         * @return new tensor with concatenated data
         */
        FTensor concatenate(int axis, FTensor... tensors);

        /**
         * Concatenates multiple tensors along a new axis.
         * All tensors must have the same shape. The position of the new axis is between 0 (inclusive)
         * and the number of dimensions (inclusive).
         *
         * @param axis index of the new dimension
         * @param tensors tensors to concatenate
         * @return new tensor with concatenated data
         */
        FTensor stack(int axis, FTensor... tensors);

    }

    OfDouble ofDouble();

    OfFloat ofFloat();
}
