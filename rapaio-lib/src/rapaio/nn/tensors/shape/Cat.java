/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.nn.tensors.shape;

import java.util.Arrays;
import java.util.List;

import rapaio.darray.DArray;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class Cat extends Tensor {

    public Cat(TensorManager tm, int axis, Tensor... tensors) {
        super(tm, Cat.class.getSimpleName());

        List<? extends DArray<?>> arrays = Arrays.stream(tensors).map(Tensor::value).toList();

        int[] indices = new int[arrays.size() + 1];
        for (int i = 0; i < arrays.size(); i++) {
            indices[i + 1] = arrays.get(i).dim(axis);
        }
        for (int i = 1; i < indices.length; i++) {
            indices[i] += indices[i - 1];
        }

        this.setValue(tm.arrayManager().cat(tm.dt(), axis, arrays));
        for (int i = 1; i < indices.length; i++) {
            int ii = i;
            backEdge(tensors[i - 1], () -> this.grad().narrow(axis, true, indices[ii - 1], indices[ii]));
        }
    }
}
