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

import java.util.concurrent.atomic.AtomicReference;

class SvrKernelMatrix extends AbstractKernelMatrix {
    private final int l;
    private final Cache cache;
    private final byte[] sign;
    private final int[] index;
    private int next_buffer;
    private float[][] buffer;
    private final double[] QD;

    SvrKernelMatrix(svm_problem prob, svm_parameter param) {
        super(prob.l, prob.x, param);
        l = prob.l;
        cache = new Cache(l, (long) (param.cache_size * (1 << 20)));
        QD = new double[2 * l];
        sign = new byte[2 * l];
        index = new int[2 * l];
        for (int k = 0; k < l; k++) {
            sign[k] = 1;
            sign[k + l] = -1;
            index[k] = k;
            index[k + l] = k;
            QD[k] = kernel_function(k, k);
            QD[k + l] = QD[k];
        }
        buffer = new float[2][2 * l];
        next_buffer = 0;
    }

    void swap_index(int i, int j) {
        do {
            byte tmp = sign[i];
            sign[i] = sign[j];
            sign[j] = tmp;
        } while (false);
        do {
            int tmp = index[i];
            index[i] = index[j];
            index[j] = tmp;
        } while (false);
        do {
            double tmp = QD[i];
            QD[i] = QD[j];
            QD[j] = tmp;
        } while (false);
    }

    float[] get_Q(int i, int len) {
        AtomicReference<float[]> data = new AtomicReference<>();
        int j, real_i = index[i];
        if (cache.getData(real_i, data, l) < l) {
            for (j = 0; j < l; j++) {
                data.get()[j] = (float) kernel_function(real_i, j);
            }
        }

        // reorder and copy
        float buf[] = buffer[next_buffer];
        next_buffer = 1 - next_buffer;
        byte si = sign[i];
        for (j = 0; j < len; j++) {
            buf[j] = (float) si * sign[j] * data.get()[index[j]];
        }
        return buf;
    }

    double[] get_QD() {
        return QD;
    }
}
