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

import rapaio.util.collection.IntArrays;

public class StrideAlgebra {

    public static int[] computeStorageOrder(int[] dims, int[] strides) {
        int[] storageOrder = IntArrays.newSeq(dims.length);
        IntArrays.quickSort(storageOrder, (i, j) -> {
            int cmp = Integer.compare(strides[i], strides[j]);
            return (cmp == 0) ? Integer.compare(dims[i], dims[j]) : cmp;
        });
        return storageOrder;
    }
}
