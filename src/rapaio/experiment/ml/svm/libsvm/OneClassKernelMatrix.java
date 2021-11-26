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

package rapaio.experiment.ml.svm.libsvm;

import java.lang.ref.Reference;
import java.util.concurrent.atomic.AtomicReference;

import rapaio.util.collection.TArrays;

class OneClassKernelMatrix extends AbstractKernelMatrix {
    private final Cache cache;
    private final double[] QD;

    OneClassKernelMatrix(svm_problem prob, svm_parameter param) {
        super(prob.l, prob.x, param);
        cache = new Cache(prob.l, (long) (param.cache_size * (1 << 20)));
        QD = new double[prob.l];
        for (int i = 0; i < prob.l; i++) {
            QD[i] = kernel_function(i, i);
        }
    }

    float[] get_Q(int i, int len) {
        AtomicReference<float[]> data = new AtomicReference<>();
        int start, j;
        if ((start = cache.getData(i, data, len)) < len) {
            for (j = start; j < len; j++) {
                data.get()[j] = (float) kernel_function(i, j);
            }
        }
        return data.get();
    }

    double[] get_QD() {
        return QD;
    }

    void swap_index(int i, int j) {
        cache.swap_index(i, j);
        super.swap_index(i, j);
        TArrays.swap(QD, i, j);
    }
}
