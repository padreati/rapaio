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

package rapaio.ml.model.svm.libsvm;

import rapaio.math.linear.dense.DVectorDense;
import rapaio.util.Reference;
import rapaio.util.collection.TArrays;

class OneClassKernelMatrix extends AbstractKernelMatrix {

    OneClassKernelMatrix(SvmProblem prob, SvmParameter param) {
        super(prob.xs, param.kernel, new Cache(prob.len, param.cacheSize * (1 << 20)), new double[prob.len]);
        for (int i = 0; i < prob.len; i++) {
            qd[i] = kernel.compute(xs[i], xs[i]);
        }
    }

    DVectorDense getQ(int i, int len) {
        Reference<DVectorDense> data = new Reference<>();
        int start = cache.getData(i, data, len);
        if (start < len) {
            for (int j = start; j < len; j++) {
                data.get().set(j, kernel.compute(xs[i], xs[j]));
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
        TArrays.swap(qd, i, j);
    }
}
