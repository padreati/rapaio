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

package rapaio.ml.common.kernel.cache;

import java.util.HashMap;
import java.util.Map;

import rapaio.math.narray.NArray;
import rapaio.ml.common.kernel.Kernel;

/**
 * Defines operations available on a kernel cache.
 * A kernel cache is a in memory data structure which is able to cache results of
 * operations to fasten th training and prediction of a svm.
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/25/16.
 */
public class KernelCache {

    private final Cache cache;
    private final Kernel kernel;

    public KernelCache(NArray<Double> df, Kernel kernel) {
        if (df.dim(0) <= 10_000) {
            cache = new SolidCache(df.dim(0));
        } else {
            cache = new MapCache();
        }
        this.kernel = kernel;
    }

    public double cachedCompute(int row1, int row2, NArray<Double> r1, NArray<Double> r2) {
        Double value = cache.retrieve(row1, row2);
        if (value == null) {
            value = kernel.compute(r1, r2);
            cache.store(row1, row2, value);
        }
        return value;
    }

    public void clean() {
        cache.clear();
    }

    private interface Cache {

        void store(int row1, int row2, double value);

        Double retrieve(int row1, int row2);

        void clear();
    }

    private static final class MapCache implements Cache {

        final transient private Map<Long, Double> cache = new HashMap<>();

        @Override
        public void store(int row1, int row2, double value) {
            long id = ((long) row1 << 32) | ((long) row2 & 0xffffffffL);
            cache.put(id, value);
        }

        @Override
        public Double retrieve(int row1, int row2) {
            return cache.get(((long) row1 << 32) | (row2 & 0xffffffffL));
        }

        @Override
        public void clear() {
            cache.clear();
        }
    }

    private static final class SolidCache implements Cache {
        private final int n;
        private boolean[] flag;
        private double[] cache;

        public SolidCache(int n) {
            this.n = n;
            flag = new boolean[n * n];
            cache = new double[n * n];
        }

        @Override
        public Double retrieve(int row1, int row2) {
            if (cache == null) {
                return null;
            }
            if (row1 > row2) {
                return retrieve(row2, row1);
            }
            int p = row1 * n + row2;
            if (flag[p]) {
                return cache[p];
            }
            return null;
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
            int p = row1 * n + row2;
            flag[p] = true;
            cache[p] = value;
        }

        @Override
        public void clear() {
            flag = null;
            cache = null;
        }
    }
}
