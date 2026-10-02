/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.util.Reference;

/**
 * The kernel row cache, driven the way {@link SvcKernelMatrix#getQ} drives it: a first request while the active set
 * is still narrow, then a second one for the full row after the solver unshrinks.
 * <p>
 * The growth step used to go through {@code pad(0, more, 1)}, which pads an axis on both sides. The row came back
 * {@code oldLen + 2 * more} elements long instead of {@code len}, with the cached values displaced to the middle and
 * zeros left in the prefix the caller reads without recomputing it.
 */
public class CacheTest {

    /** Fills the row the way the kernel matrix does, so position j holds a value identifying j. */
    private static void fill(DArray<Double> row, int start, int len) {
        for (int j = start; j < len; j++) {
            row.set((double) (j + 1), j);
        }
    }

    @Test
    void growingACachedRowKeepsItsPrefixInPlace() {
        // every split of a 10 long row into a cached prefix and a requested remainder
        for (int oldLen = 1; oldLen < 10; oldLen++) {
            int len = 10;
            Cache cache = new Cache(len, 1L << 20);
            Reference<DArray<Double>> ref = new Reference<>();

            int start = cache.getData(0, ref, oldLen);
            assertEquals(0, start, "a cold entry must be computed from the first position");
            assertEquals(oldLen, ref.get().size());
            fill(ref.get(), start, oldLen);

            start = cache.getData(0, ref, len);
            DArray<Double> row = ref.get();
            assertEquals(oldLen, start, "the cached prefix must not be recomputed");
            assertEquals(len, row.size(), "the row must be exactly as long as requested, oldLen=" + oldLen);
            fill(row, start, len);

            for (int j = 0; j < len; j++) {
                assertEquals(j + 1, row.getDouble(j), 0.0,
                        "position " + j + " of the row grown from " + oldLen);
            }
        }
    }

    @Test
    void aRequestNoLongerThanTheCachedRowLeavesItAlone() {
        Cache cache = new Cache(4, 1L << 20);
        Reference<DArray<Double>> ref = new Reference<>();

        int start = cache.getData(0, ref, 5);
        fill(ref.get(), start, 5);

        // asking for the same length, and for less, returns the row untouched
        for (int len : new int[] {5, 3}) {
            int s = cache.getData(0, ref, len);
            assertEquals(5, ref.get().size());
            assertEquals(5, s, "nothing needs recomputing when the row is already long enough");
            for (int j = 0; j < 5; j++) {
                assertEquals(j + 1, ref.get().getDouble(j), 0.0);
            }
        }
    }
}
