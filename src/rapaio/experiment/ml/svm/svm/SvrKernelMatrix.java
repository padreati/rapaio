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

import rapaio.math.linear.DVector;
import rapaio.ml.common.kernel.Kernel;
import rapaio.util.Reference;
import rapaio.util.collection.TArrays;

class SvrKernelMatrix extends AbstractKernelMatrix {

    private final int l;
    private final Cache cache;
    private final byte[] sign;
    private final int[] index;
    private int nextBuffer;
    private final double[][] buffer;
    private final double[] qd;

    SvrKernelMatrix(int l, DVector[] xs, Kernel kernel, long cacheSize) {
        super(xs, kernel);
        this.l = l;
        cache = new Cache(l, cacheSize * (1 << 20));
        qd = new double[2 * l];
        sign = new byte[2 * l];
        index = new int[2 * l];
        for (int k = 0; k < l; k++) {
            sign[k] = 1;
            sign[k + l] = -1;
            index[k] = k;
            index[k + l] = k;
            qd[k] = kernel.compute(xs[k], xs[k]);
            qd[k + l] = qd[k];
        }
        buffer = new double[2][2 * l];
        nextBuffer = 0;
    }

    void swapIndex(int i, int j) {
        TArrays.swap(sign, i, j);
        TArrays.swap(index, i, j);
        TArrays.swap(qd, i, j);
    }

    double[] getQ(int i, int len) {
        Reference<double[]> data = new Reference<>();
        if (cache.getData(index[i], data, l) < l) {
            for (int j = 0; j < l; j++) {
                data.get()[j] = kernel.compute(xs[index[i]], xs[j]);
            }
        }

        // reorder and copy
        double[] buf = buffer[nextBuffer];
        nextBuffer = 1 - nextBuffer;
        byte si = sign[i];
        for (int j = 0; j < len; j++) {
            buf[j] = si * sign[j] * data.get()[index[j]];
        }
        return buf;
    }

    double[] getQD() {
        return qd;
    }
}
