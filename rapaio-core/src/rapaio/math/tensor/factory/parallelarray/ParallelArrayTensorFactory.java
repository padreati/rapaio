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

package rapaio.math.tensor.factory.parallelarray;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.FTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.factory.AbstractTensorFactory;

public class ParallelArrayTensorFactory extends AbstractTensorFactory {

    private static final class ImplOfDouble implements OfDouble {

        private final ParallelArrayTensorFactory self;

        ImplOfDouble(ParallelArrayTensorFactory self) {
            this.self = self;
        }

        @Override
        public DTensor zeros(Shape shape, Order order) {
            return new DTensorStride(self, shape, 0, Order.autoFC(order), new double[shape.size()]);
        }

        @Override
        public DTensor seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).iteratorApply(Order.C, (i, p) -> (double) i);
        }

        @Override
        public DTensor random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).iteratorApply(order, (i, p) -> random.nextDouble());
        }

        @Override
        public DTensor wrap(Shape shape, double[] array, Order order) {
            return new DTensorStride(self, shape, 0, Order.autoFC(order), array);
        }

        @Override
        public DTensor stride(Shape shape, int offset, int[] strides, double[] array) {
            return new DTensorStride(self, shape, offset, strides, array);
        }

        @Override
        public DTensor concatenate(int axis, DTensor... tensors) {

            for (int i = 1; i < tensors.length; i++) {
                int[] dimsPrev = tensors[i - 1].layout().shape().dims();
                int[] dimsNext = tensors[i].layout().shape().dims();
                for (int j = 0; j < dimsPrev.length; j++) {
                    if (j != axis && dimsNext[j] != dimsPrev[j]) {
                        throw new RuntimeException("Tensors are not valid for concatenation");
                    }
                }
            }

            int newDim = Arrays.stream(tensors).mapToInt(tensor -> tensor.layout().shape().dim(axis)).sum();
            int[] newDims = Arrays.copyOf(tensors[0].shape().dims(), tensors[0].shape().rank());
            newDims[axis] = newDim;
            var result = zeros(Shape.of(newDims), Order.defaultOrder());

            int start = 0;
            for (var tensor : tensors) {
                int end = start + tensor.layout().shape().dim(axis);
                var dst = result.truncate(axis, start, end);

                var it1 = tensor.pointerIterator(Order.defaultOrder());
                var it2 = dst.pointerIterator(Order.defaultOrder());

                while (it1.hasNext() && it2.hasNext()) {
                    dst.ptrSet(it2.nextInt(), tensor.ptrGet(it1.nextInt()));
                }
                start = end;
            }
            return result;
        }

        @Override
        public DTensor stack(int axis, DTensor... tensors) {
            for (int i = 1; i < tensors.length; i++) {
                if (!tensors[i - 1].shape().equals(tensors[i].shape())) {
                    throw new RuntimeException("Tensors are not valid for stack, they have to have the same dimensions.");
                }
            }
            int[] newDims = new int[tensors[0].shape().rank() + 1];
            for (int i = 0; i < tensors[0].shape().rank(); i++) {
                if (i < axis) {
                    newDims[i] = tensors[0].shape().dim(i);
                }
            }
            newDims[axis] = tensors.length;
            DTensor result = zeros(Shape.of(newDims), Order.defaultOrder());
            List<DTensor> slices = result.slice(axis, 1);
            for (int i = 0; i < tensors.length; i++) {
                var it1 = slices.get(i).squeeze().pointerIterator(Order.defaultOrder());
                var it2 = tensors[i].pointerIterator(Order.defaultOrder());
                while (it1.hasNext() && it2.hasNext()) {
                    slices.get(i).ptrSet(it1.nextInt(), tensors[i].ptrGet(it2.nextInt()));
                }
            }
            return result;
        }
    }

    private static final class ImplOfFloat implements OfFloat {

        private final ParallelArrayTensorFactory self;

        ImplOfFloat(ParallelArrayTensorFactory self) {
            this.self = self;
        }

        @Override
        public FTensor zeros(Shape shape, Order order) {
            return new FTensorStride(self, shape, 0, Order.autoFC(order), new float[shape.size()]);
        }

        @Override
        public FTensor seq(Shape shape, Order order) {
            return zeros(shape, order).iteratorApply(Order.C, (i, _) -> (float) i);
        }

        @Override
        public FTensor random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).iteratorApply(Order.C, (_, __) -> random.nextFloat());
        }

        @Override
        public FTensor wrap(Shape shape, float[] array, Order order) {
            return new FTensorStride(self, shape, 0, Order.autoFC(order), array);
        }

        @Override
        public FTensor stride(Shape shape, int offset, int[] strides, float[] array) {
            return new FTensorStride(self, shape, offset, strides, array);
        }

        @Override
        public FTensor concatenate(int axis, FTensor... tensors) {

            for (int i = 1; i < tensors.length; i++) {
                int[] dimsPrev = tensors[i - 1].layout().shape().dims();
                int[] dimsNext = tensors[i].layout().shape().dims();
                for (int j = 0; j < dimsPrev.length; j++) {
                    if (j != axis && dimsNext[j] != dimsPrev[j]) {
                        throw new RuntimeException("Tensors are not valid for concatenation");
                    }
                }
            }

            int newDim = Arrays.stream(tensors).mapToInt(tensor -> tensor.layout().shape().dim(axis)).sum();
            int[] newDims = Arrays.copyOf(tensors[0].shape().dims(), tensors[0].shape().rank());
            newDims[axis] = newDim;
            FTensor result = zeros(Shape.of(newDims), Order.defaultOrder());

            int start = 0;
            for (FTensor tensor : tensors) {
                int end = start + tensor.layout().shape().dim(axis);
                var dst = result.truncate(axis, start, end);

                var it1 = tensor.pointerIterator(Order.defaultOrder());
                var it2 = dst.pointerIterator(Order.defaultOrder());

                while (it1.hasNext() && it2.hasNext()) {
                    dst.ptrSet(it2.nextInt(), tensor.ptrGet(it1.nextInt()));
                }
                start = end;
            }
            return result;
        }

        @Override
        public FTensor stack(int axis, FTensor... tensors) {
            for (int i = 1; i < tensors.length; i++) {
                if (!tensors[i - 1].shape().equals(tensors[i].shape())) {
                    throw new RuntimeException("Tensors are not valid for stack, they have to have the same dimensions.");
                }
            }
            int[] newDims = new int[tensors[0].shape().rank() + 1];
            for (int i = 0; i < tensors[0].shape().rank(); i++) {
                if (i < axis) {
                    newDims[i] = tensors[0].shape().dim(i);
                }
            }
            newDims[axis] = tensors.length;
            var result = zeros(Shape.of(newDims), Order.defaultOrder());
            var slices = result.slice(axis, 1);
            for (int i = 0; i < tensors.length; i++) {
                var it1 = slices.get(i).squeeze().pointerIterator(Order.defaultOrder());
                var it2 = tensors[i].pointerIterator(Order.defaultOrder());
                while (it1.hasNext() && it2.hasNext()) {
                    slices.get(i).ptrSet(it1.nextInt(), tensors[i].ptrGet(it2.nextInt()));
                }
            }
            return result;
        }
    }

    private final OfDouble _ofDouble = new ParallelArrayTensorFactory.ImplOfDouble(this);
    private final OfFloat _ofFloat = new ParallelArrayTensorFactory.ImplOfFloat(this);

    @Override
    public OfDouble ofDouble() {
        return _ofDouble;
    }

    @Override
    public OfFloat ofFloat() {
        return _ofFloat;
    }
}
