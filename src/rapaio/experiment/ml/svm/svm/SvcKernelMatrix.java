/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.ml.svm.svm;

import java.util.Arrays;

import rapaio.math.linear.DVector;
import rapaio.ml.common.kernel.Kernel;
import rapaio.util.Reference;
import rapaio.util.collection.TArrays;

/**
 * Q matrix for C formulation.
 */
class SvcKernelMatrix extends AbstractKernelMatrix {
    private final byte[] y;
    private final Cache cache;
    private final double[] qd;

    public SvcKernelMatrix(int l, DVector[] xs, Kernel kernel, long cacheSize, byte[] y) {
        super(l, xs, kernel);
        this.y = Arrays.copyOf(y, y.length);
        this.cache = new Cache(l, cacheSize * (1 << 20));
        this.qd = new double[l];
        for (int i = 0; i < l; i++) {
            this.qd[i] = kernel_function(i, i);
        }
    }

    float[] getQ(int i, int len) {
        Reference<float[]> data = new Reference<>();
        int start = cache.getData(i, data, len);
        if (start < len) {
            for (int j = start; j < len; j++) {
                data.get()[j] = (float) (y[i] * y[j] * kernel_function(i, j));
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
