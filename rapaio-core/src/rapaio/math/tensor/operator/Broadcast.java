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

import rapaio.math.tensor.Tensor;

public final class Broadcast {

    @SafeVarargs
    public static <N extends Number> ElementWise<N> elementWise(Tensor<N>... tensors) {
        if (tensors.length == 0) {
            return new ElementWise<>(true, true, tensors);
        }
        int len = 0;
        int[] ranks = new int[tensors.length];
        for (int i = 0; i < tensors.length; i++) {
            int rank = tensors[i].rank();
            ranks[i] = rank;
            len = Math.max(rank, len);
        }
        Tensor<N>[] transformed = Arrays.copyOf(tensors, tensors.length);
        boolean unchanged = true;
        for (int i = 1; i <= len; i++) {
            int max = ranks[0] - i < 0 ? 0 : tensors[0].dim(ranks[0] - i);
            for (int j = 1; j < tensors.length; j++) {
                max = Math.max(max, ranks[j] - i < 0 ? 0 : tensors[j].dim(ranks[j] - i));
            }
            for (int j = 0; j < tensors.length; j++) {
                int index = ranks[j] - i;
                if (index < 0) {
                    transformed[j] = transformed[j].stretch(0).expand(0, max);
                    continue;
                }
                int value = tensors[j].dim(index);
                if (value == max) {
                    continue;
                }
                if (value == 1) {
                    transformed[j] = transformed[j].expand(index, max);
                    unchanged = false;
                    continue;
                }
                return new ElementWise<>(false, true, tensors);
            }
        }
        return new ElementWise<>(true, unchanged, transformed);
    }

    public record ElementWise<N extends Number>(boolean valid, boolean unchanged, Tensor<N>... transformed) {

        @SafeVarargs
        public ElementWise {
        }
    }
}
