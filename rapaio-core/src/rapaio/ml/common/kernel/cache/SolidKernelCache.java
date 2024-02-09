/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.ml.common.kernel.cache;

import java.io.Serial;

import rapaio.math.tensor.Tensor;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/25/16.
 */
public class SolidKernelCache implements KernelCache {

    @Serial
    private static final long serialVersionUID = -1003713236239594088L;

    private Double[][] cache;

    public SolidKernelCache(Tensor<Double> df) {
        cache = new Double[df.dim(0)][df.dim(0)];
    }

    @Override
    public Double retrieve(int row1, int row2) {
        if (cache == null) {
            return null;
        }
        if (row1 > row2) {
            return retrieve(row2, row1);
        }
        return cache[row1][row2];
    }

    @Override
    public void store(int row1, int row2, double value) {
        if (cache == null) {
            return;
        }
        if (row1 > row2) {
            store(row2, row1, value);
            return;
        }
        cache[row1][row2] = value;
    }

    @Override
    public void clear() {
        cache = null;
    }
}
