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

package rapaio.math.tensor.operator;

import java.util.Arrays;
import java.util.List;

import rapaio.math.tensor.Tensor;
import rapaio.util.collection.IntArrays;

public final class Broadcast {

    public static ElementWise elementWise(List<Tensor<?>> tensors) {
        if (tensors.isEmpty()) {
            return new ElementWise(true, true);
        }
        int len = 0;
        int[] ranks = new int[tensors.size()];
        for (int i = 0; i < tensors.size(); i++) {
            int rank = tensors.get(i).rank();
            ranks[i] = rank;
            len = Math.max(rank, len);
        }
        int[] dims = IntArrays.newFill(len, 1);
        boolean unchanged = true;
        for (int i = 1; i <= len; i++) {
            int max = ranks[0] - i < 0 ? 0 : tensors.getFirst().dim(ranks[0] - i);
            for (int j = 1; j < tensors.size(); j++) {
                max = Math.max(max, ranks[j] - i < 0 ? 0 : tensors.get(j).dim(ranks[j] - i));
            }
            dims[len - i] = max;
            for (int j = 0; j < tensors.size(); j++) {
                int index = ranks[j] - i;
                if (index < 0) {
                    unchanged = false;
                    continue;
                }
                int size = tensors.get(j).dim(index);
                if (size == max) {
                    continue;
                }
                if (size == 1) {
                    unchanged = false;
                    continue;
                }
                return new ElementWise(false, true);
            }
        }
        return new ElementWise(true, unchanged, dims);
    }

    public record ElementWise(boolean valid, boolean unchanged, int[] dims) {

        public ElementWise(boolean valid, boolean unchanged) {
            this(valid, unchanged, new int[0]);
        }

        public <N extends Number> boolean hasShape(Tensor<N> t) {
            if (t.rank() != dims.length) {
                return false;
            }
            for (int i = 0; i < dims.length; i++) {
                if (dims[i] != t.dim(i)) {
                    return false;
                }
            }
            return true;
        }

        public <N extends Number> Tensor<N> transform(Tensor<N> t) {
            for (int i = 1; i <= dims.length; i++) {
                int index = t.rank() - i;
                if (index < 0) {
                    t = t.stretch(0).expand(0, dims[dims.length - i]);
                    continue;
                }
                int dim = t.dim(index);
                if (dim == dims[dims.length - i]) {
                    continue;
                }
                if (dim == 1) {
                    t = t.expand(index, dims[dims.length - i]);
                    continue;
                }
                throw new IllegalArgumentException("Tensor not compatible for broadcasting.");
            }
            return t;
        }

        @Override
        public String toString() {
            return String.format("ElementWise[valid=%b, unchanged=%b, dims=%s]", valid, unchanged, Arrays.toString(dims));
        }
    }
}
