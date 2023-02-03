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

package rapaio.experiment.math.tensor.storage;

import java.util.Arrays;
import java.util.Random;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;
import rapaio.util.collection.FloatArrays;
import rapaio.util.collection.IntArrays;

public class FStorage implements Storage<Float> {

    public static FStorage wrap(float[] array) {
        return new FStorage(array);
    }

    public static FStorage zeros(int size) {
        return new FStorage(new float[size]);
    }

    public static FStorage fill(int size, float value) {
        return new FStorage(FloatArrays.newFill(size, value));
    }

    public static FStorage random(int size, Random random) {
        FStorage storage = zeros(size);
        for (int i = 0; i < size; i++) {
            storage.setFloat(i, random.nextFloat());
        }
        return storage;
    }

    public static FStorage seq(int start, int end) {
        FStorage storage = FStorage.zeros(end - start);
        for (int i = 0; i < end - start; i++) {
            storage.set(i, (float) (start + i));
        }
        return storage;
    }

    public static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
    public static final int SPECIES_LEN = SPECIES.length();
    private final float[] array;

    private FStorage(float[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public Float get(int index) {
        return getFloat(index);
    }

    public float getFloat(int pointer) {
        return array[pointer];
    }

    @Override
    public void set(int pointer, Float v) {
        setFloat(pointer, v);
    }

    public void setFloat(int pointer, float v) {
        array[pointer] = v;
    }

    @Override
    public void swap(int left, int right) {
        float tmp = array[left];
        array[left] = array[right];
        array[right] = tmp;
    }

    @Override
    public void swap(int a, int b, final int n) {
        int loopBound = SPECIES.loopBound(n);
        int i = 0;
        for (; i < loopBound; i += SPECIES_LEN, a += SPECIES_LEN, b += SPECIES_LEN) {
            FloatVector va = FloatVector.fromArray(SPECIES, array, a);
            FloatVector vb = FloatVector.fromArray(SPECIES, array, b);

            va.intoArray(array, b);
            vb.intoArray(array, a);
        }
        for (; i < n; i++, a++, b++) {
            swap(a, b);
        }
    }

    @Override
    public void fill(int start, int len, Float v) {
        fillFloat(start, len, v);
    }

    public void fillFloat(int start, int len, float v) {
        Arrays.fill(array, start, start + len, v);
    }

    @Override
    public void reverse(int start, int len) {
        int head = start;
        int tail = start + len;
        int step = 2 * SPECIES_LEN;
        int[] indexes = IntArrays.newSeq(0, SPECIES_LEN);
        IntArrays.reverse(indexes);
        VectorShuffle<Float> shuffle = SPECIES.shuffleFromValues(indexes);

        while (tail - head >= step) {
            FloatVector hv = FloatVector.fromArray(SPECIES, array, head);
            FloatVector tv = FloatVector.fromArray(SPECIES, array, tail - SPECIES_LEN);
            hv.rearrange(shuffle).intoArray(array, tail - SPECIES_LEN);
            tv.rearrange(shuffle).intoArray(array, head);

            head += SPECIES_LEN;
            tail -= SPECIES_LEN;
        }
        while (tail - head > 1) {
            swap(head, tail - 1);
            tail--;
            head++;
        }
    }

    /**
     * Sorts the specified range of elements according to the order induced by the specified
     * comparator using quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>Note that this implementation does not allocate any object, contrarily to the implementation
     * used to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
     *
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     */
    @Override
    public void quicksort(int from, int to, boolean asc) {
        quickSort(from, to, asc);
    }

    private static final int QUICKSORT_NO_REC = 16;
    private static final int QUICKSORT_MEDIAN_OF_9 = 128;

    public void quickSort(final int from, final int to, boolean asc) {
        final int len = to - from;
        // Selection sort on smallest arrays
        if (len < QUICKSORT_NO_REC) {
            selectionSort(from, to, asc);
            return;
        }
        // Choose a partition element, v
        int m = from + len / 2;
        int l = from;
        int n = to - 1;
        if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
            int s = len / 8;
            l = med3(l, l + s, l + 2 * s, asc);
            m = med3(m - s, m, m + s, asc);
            n = med3(n - 2 * s, n - s, n, asc);
        }
        m = med3(l, m, n, asc); // Mid-size, med of 3
        final float v = array[m];
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = cmp(array[b], v, asc)) <= 0) {
                if (comparison == 0) {
                    swap(a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = cmp(array[c], v, asc)) >= 0) {
                if (comparison == 0) {
                    swap(c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            swap(b++, c--);
        }
        // Swap partition elements back to middle
        int s;
        s = Math.min(a - from, b - a);
        swap(from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        swap(b, to - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            quickSort(from, from + s, asc);
        }
        if ((s = d - c) > 1) {
            quickSort(to - s, to, asc);
        }
    }

    private void selectionSort(final int from, final int to, final boolean asc) {
        for (int i = from; i < to - 1; i++) {
            int m = i;
            for (int j = i + 1; j < to; j++) {
                if ((asc && array[j] < array[m]) || (!asc && array[j] > m)) {
                    m = j;
                }
            }
            if (m != i) {
                swap(i, m);
            }
        }
    }

    private int cmp(float a, float b, boolean asc) {
        int sign = asc ? 1 : -1;
        return a < b ? sign : (a == b ? 0 : -sign);
    }

    private int med3(final int a, final int b, final int c, boolean asc) {
        final int ab = cmp(array[a], array[b], asc);
        final int ac = cmp(array[a], array[c], asc);
        final int bc = cmp(array[b], array[c], asc);
        return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
    }

    @Override
    public void add(int start, int len, Float v) {
        addFloat(start, len, v);
    }

    public void addFloat(int start, int len, float v) {
        FloatVector vv = FloatVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            FloatVector xv = FloatVector.fromArray(SPECIES, array, i);
            xv = xv.add(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] += v;
        }
    }

    @Override
    public void sub(int start, int len, Float v) {
        subFloat(start, len, v);
    }

    public void subFloat(int start, int len, float v) {
        FloatVector vv = FloatVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            FloatVector xv = FloatVector.fromArray(SPECIES, array, i);
            xv = xv.sub(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] -= v;
        }
    }

    @Override
    public void mul(int start, int len, Float v) {
        mulFloat(start, len, v);
    }

    public void mulFloat(int start, int len, float v) {
        FloatVector vv = FloatVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            FloatVector xv = FloatVector.fromArray(SPECIES, array, i);
            xv = xv.mul(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] *= v;
        }
    }

    @Override
    public void div(int start, int len, Float v) {
        divFloat(start, len, v);
    }

    public void divFloat(int start, int len, float v) {
        FloatVector vv = FloatVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            FloatVector xv = FloatVector.fromArray(SPECIES, array, i);
            xv = xv.div(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] /= v;
        }
    }

    @Override
    public Float min(int start, int len) {
        return minFloat(start, len);
    }

    public float minFloat(int start, int len) {
        if (len <= 0) {
            return Float.NaN;
        }
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        boolean vectorized = false;
        FloatVector minVector = FloatVector.broadcast(SPECIES, Float.POSITIVE_INFINITY);
        for (; i < loopBound; i += SPECIES_LEN) {
            FloatVector xv = FloatVector.fromArray(SPECIES, array, i);
            minVector = minVector.min(xv);
            vectorized = true;
        }
        float minValue = vectorized ? minVector.reduceLanes(VectorOperators.MIN) : Float.NaN;
        for (; i < start + len; i++) {
            minValue = Float.isNaN(minValue) ? array[i] : Math.min(minValue, array[i]);
        }
        return minValue;
    }

    @Override
    public int argMin(int start, int len) {
        return argMinFloat(start, len);
    }

    public int argMinFloat(int start, int len) {
        if (len <= 0) {
            return -1;
        }
        float min = array[start];
        int index = start;
        for (int i = start + 1; i < start + len; i++) {
            float value = array[i];
            if (value < min) {
                min = value;
                index = i;
            }
        }
        return index;
    }
}
