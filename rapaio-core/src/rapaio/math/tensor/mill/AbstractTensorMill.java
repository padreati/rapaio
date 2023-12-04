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

package rapaio.math.tensor.mill;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import rapaio.math.tensor.TensorMill;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;

public abstract class AbstractTensorMill implements TensorMill {

    @Override
    public <N extends Number, T extends Tensor<N, T>> T concat(int axis, Iterable<T> tensors) {
        var tensorList = StreamSupport.stream(tensors.spliterator(), false).toList();
        validateForConcatenation(axis, tensorList.stream().map(t -> t.shape().dims()).collect(Collectors.toList()));

        int newDim = tensorList.stream().mapToInt(tensor -> tensor.layout().shape().dim(axis)).sum();
        int[] newDims = Arrays.copyOf(tensorList.get(0).shape().dims(), tensorList.get(0).rank());
        newDims[axis] = newDim;
        var result = ofType(tensorList.get(0).dtype()).zeros(Shape.of(newDims), Order.defaultOrder());

        int start = 0;
        for (T tensor : tensors) {
            int end = start + tensor.shape().dim(axis);
            var dst = result.narrow(axis, true, start, end);

            var it1 = tensor.ptrIterator(Order.defaultOrder());
            var it2 = dst.ptrIterator(Order.defaultOrder());

            while (it1.hasNext() && it2.hasNext()) {
                dst.ptrSet(it2.nextInt(), tensor.ptrGet(it1.nextInt()));
            }
            start = end;
        }
        return result;
    }

    @Override
    public <N extends Number, T extends Tensor<N, T>> T stack(int axis, Iterable<T> tensors) {
        var tensorList = StreamSupport.stream(tensors.spliterator(), false).toList();
        for (int i = 1; i < tensorList.size(); i++) {
            if (!tensorList.get(i - 1).shape().equals(tensorList.get(i).shape())) {
                throw new IllegalArgumentException("Tensors are not valid for stack, they have to have the same dimensions.");
            }
        }
        int[] newDims = new int[tensorList.get(0).shape().rank() + 1];
        for (int i = 0; i < tensorList.get(0).shape().rank(); i++) {
            if (i < axis) {
                newDims[i] = tensorList.get(0).shape().dim(i);
            } else {
                newDims[i + 1] = tensorList.get(0).shape().dim(i);
            }
        }
        newDims[axis] = tensorList.size();
        var result = ofType(tensorList.get(0).dtype()).zeros(Shape.of(newDims), Order.defaultOrder());
        var slices = result.chunk(axis, true, 1);
        for (int i = 0; i < tensorList.size(); i++) {
            var it1 = slices.get(i).squeeze().ptrIterator(Order.defaultOrder());
            var it2 = tensorList.get(i).ptrIterator(Order.defaultOrder());
            while (it1.hasNext() && it2.hasNext()) {
                slices.get(i).ptrSet(it1.nextInt(), tensorList.get(i).ptrGet(it2.nextInt()));
            }
        }
        return result;
    }

    protected static void validateForConcatenation(int axis, List<int[]> dims) {
        for (int i = 1; i < dims.size(); i++) {
            int[] dimsPrev = dims.get(i - 1);
            int[] dimsNext = dims.get(i);
            for (int j = 0; j < dimsPrev.length; j++) {
                if (j != axis && dimsNext[j] != dimsPrev[j]) {
                    throw new IllegalArgumentException("Tensors are not valid for concatenation");
                }
            }
        }
    }
}
