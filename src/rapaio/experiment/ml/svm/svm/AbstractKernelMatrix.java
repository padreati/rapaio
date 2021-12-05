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
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.util.collection.TArrays;

public abstract class AbstractKernelMatrix {
    private DVector[] x;
    private final double[] x_square;

    // svm_parameter
    private final Kernel kernel;

    abstract double[] getQD();

    void swapIndex(int i, int j) {
        TArrays.swap(x, i, j);
        if (x_square != null) {
            TArrays.swap(x_square, i, j);
        }
    }

    abstract float[] getQ(int column, int len);

    private static double powi(double base, int times) {
        double tmp = base, ret = 1.0;

        for (int t = times; t > 0; t /= 2) {
            if (t % 2 == 1) {
                ret *= tmp;
            }
            tmp = tmp * tmp;
        }
        return ret;
    }

    double kernel_function(int i, int j) {
        return kernel.compute(x[i], x[j]);
    }

    AbstractKernelMatrix(int l, DVector[] x_, Kernel kernel) {
        this.kernel = kernel;

        x = x_.clone();

        if (kernel instanceof RBFKernel) {
            x_square = new double[l];
            for (int i = 0; i < l; i++) {
                x_square[i] = dot(x[i], x[i]);
            }
        } else {
            x_square = null;
        }
    }

    static double dot(DVector x, DVector y) {
        return x.dot(y);
    }
}
