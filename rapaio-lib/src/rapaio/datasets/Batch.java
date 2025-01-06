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

package rapaio.datasets;

import java.util.Arrays;

import rapaio.darray.DArray;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public final class Batch {

    private final TensorManager tm;
    private final TensorDataset<?> dataset;
    private final int[] indices;
    private Tensor[] outputs;

    public Batch(TensorDataset<?> dataset, int[] indices) {
        this.tm = dataset.tm();
        this.dataset = dataset;
        this.indices = indices;
    }

    public TensorManager tm() {
        return tm;
    }

    public int[] indices() {
        return indices;
    }

    public Tensor tensor(int index) {
        return tm.var(dataset.darray(index).sel(0, indices));
    }

    public Tensor[] tensors() {
        return Arrays.stream(dataset.darrays()).map(array -> tm.var(array.sel(0, indices))).toArray(Tensor[]::new);
    }

    public DArray<?>[] arrays() {
        return Arrays.stream(dataset.darrays()).map(array -> array.sel(0, indices)).toArray(DArray[]::new);
    }

    public void withOutputs(Tensor... outputs) {
        this.outputs = outputs;
    }

    public Tensor[] outputs() {
        return outputs;
    }
}
