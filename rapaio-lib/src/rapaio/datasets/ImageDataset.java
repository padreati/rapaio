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

import java.util.Iterator;

import rapaio.darray.DArray;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class ImageDataset implements TensorDataset<ImageDataset> {

    private final TensorManager tm;

    public ImageDataset(TensorManager tm) {
        this.tm = tm;
    }

    @Override
    public TensorManager tm() {
        return tm;
    }

    @Override
    public int len() {
        return 0;
    }

    @Override
    public DArray<?>[] darrays() {
        return new DArray[0];
    }

    @Override
    public DArray<?> darray(int index) {
        return null;
    }

    @Override
    public Tensor[] tensors() {
        return new Tensor[0];
    }

    @Override
    public Tensor tensor(int index) {
        return null;
    }

    @Override
    public ImageDataset[] trainTestSplit(double testPercentage) {
        return new ImageDataset[0];
    }

    @Override
    public Iterator<Batch> batchIterator(int batchSize, boolean shuffle, boolean skipLast) {
        return null;
    }
}
