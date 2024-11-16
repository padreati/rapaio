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

package rapaio.ml.model.svm.libsvm;

import java.util.Arrays;

import rapaio.math.narray.NArray;
import rapaio.ml.common.kernel.Kernel;
import rapaio.util.Reference;
import rapaio.util.collection.TArrays;

/**
 * Q matrix for C formulation.
 */
class SvcKernelMatrix extends AbstractKernelMatrix {
    private final byte[] y;

    public SvcKernelMatrix(int l, NArray<Double>[] xs, Kernel kernel, long cacheSize, byte[] y) {
        super(xs, kernel, new Cache(l, cacheSize * (1 << 20)), new double[l]);
        this.y = Arrays.copyOf(y, y.length);
        for (int i = 0; i < l; i++) {
            this.qd[i] = kernel.compute(xs[i], xs[i]);
        }
    }

    NArray<Double> getQ(int i, int len) {
        Reference<NArray<Double>> data = new Reference<>();
        int start = cache.getData(i, data, len);
        if (start < len) {
            for (int j = start; j < len; j++) {
                data.get().set(y[i] * y[j] * kernel.compute(xs[i], xs[j]), j);
            }
        }
        return data.get();
    }

    double[] getQD() {
        return qd;
    }

    void swapIndex(int i, int j) {
        cache.swapIndex(i, j);
        super.swapIndex(i, j);
        TArrays.swap(y, i, j);
        TArrays.swap(qd, i, j);
    }
}
