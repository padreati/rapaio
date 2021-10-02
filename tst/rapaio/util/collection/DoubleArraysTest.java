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

package rapaio.util.collection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.util.collection.DoubleArrays.add;
import static rapaio.util.collection.DoubleArrays.addTo;
import static rapaio.util.collection.DoubleArrays.copy;
import static rapaio.util.collection.DoubleArrays.div;
import static rapaio.util.collection.DoubleArrays.divTo;
import static rapaio.util.collection.DoubleArrays.ensureCapacity;
import static rapaio.util.collection.DoubleArrays.forceCapacity;
import static rapaio.util.collection.DoubleArrays.grow;
import static rapaio.util.collection.DoubleArrays.mean;
import static rapaio.util.collection.DoubleArrays.mult;
import static rapaio.util.collection.DoubleArrays.multTo;
import static rapaio.util.collection.DoubleArrays.nanCount;
import static rapaio.util.collection.DoubleArrays.nanMean;
import static rapaio.util.collection.DoubleArrays.nanSum;
import static rapaio.util.collection.DoubleArrays.nanVariance;
import static rapaio.util.collection.DoubleArrays.newFill;
import static rapaio.util.collection.DoubleArrays.newFrom;
import static rapaio.util.collection.DoubleArrays.newSeq;
import static rapaio.util.collection.DoubleArrays.sub;
import static rapaio.util.collection.DoubleArrays.subTo;
import static rapaio.util.collection.DoubleArrays.sum;
import static rapaio.util.collection.DoubleArrays.trim;
import static rapaio.util.collection.DoubleArrays.variance;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Variance;
import rapaio.data.VarDouble;
import rapaio.util.DoubleComparator;
import rapaio.util.DoubleComparators;
import rapaio.util.DoubleIterator;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/18/19.
 */
public class DoubleArraysTest {

    private static final double TOL = 1e-12;
    private final Normal normal = Normal.std();

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void buildersTest() {
        assertArrayEquals(new double[]{10., 10., 10.}, newFill(3, 10.));
        assertArrayEquals(new double[]{10., 11., 12.}, newSeq(10, 13));
        assertArrayEquals(new double[]{4., 9., 16.}, newFrom(new double[]{1, 2, 3, 4, 5}, 1, 4, x -> x * x));
        assertArrayEquals(new double[]{3., 5.}, copy(new double[]{1, 3, 5, 7}, 1, 2));
    }

    private void testEqualArrays(double[] actual, double... expected) {
        assertArrayEquals(expected, actual, TOL);
    }

    @Test
    void testIterator() {
        double[] array = newFrom(0, 100, row -> normal.sampleNext());
        DoubleIterator it1 = DoubleArrays.iterator(array, 0, 10);
        for (int i = 0; i < 10; i++) {
            assertTrue(it1.hasNext());
            assertEquals(array[i], it1.nextDouble(), TOL);
        }
        assertThrows(NoSuchElementException.class, it1::nextDouble);

        DoubleIterator it2 = DoubleArrays.iterator(array, 0, 100);
        for (int i = 0; i < 100; i++) {
            assertTrue(it2.hasNext());
            assertEquals(array[i], it2.nextDouble(), TOL);
        }
        assertThrows(NoSuchElementException.class, it2::nextDouble);
    }

    @Test
    void testCounts() {
        double[] array1 = newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : normal.sampleNext());
        assertEquals(4, nanCount(array1, 0, 5));
        assertEquals(6, nanCount(array1, 10, 7));
        assertEquals(85, nanCount(array1, 0, 100));
    }

    @Test
    void testSums() {
        double[] array1 = newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : normal.sampleNext());
        double[] array2 = newFrom(0, 100, row -> normal.sampleNext());

        for (int i = 0; i < 100; i++) {
            int start = RandomSource.nextInt(30);
            int mid = RandomSource.nextInt(30) + start;
            int end = RandomSource.nextInt(40) + mid;

            assertEquals(sum(array1, start, mid - start) + sum(array1, mid, end - mid), sum(array1, start, end - start), TOL);
            assertEquals(nanSum(array1, start, mid - start) + nanSum(array1, mid, end - mid), nanSum(array1, start, end - start), TOL);

            assertEquals(sum(array2, start, mid - start) + sum(array2, mid, end - mid), sum(array2, start, end - start), TOL);
            assertEquals(nanSum(array2, start, mid - start) + nanSum(array2, mid, end - mid), nanSum(array2, start, end - start), TOL);
        }
    }

    @Test
    void testMeans() {
        double[] array1 = newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : normal.sampleNext());
        double[] array2 = newFrom(0, 100, row -> normal.sampleNext());

        for (int i = 0; i < 1000; i++) {
            int start = RandomSource.nextInt(20) + 10;
            int mid = RandomSource.nextInt(19) + start + 10;
            int end = RandomSource.nextInt(29) + mid + 10;

            assertEquals(mean(array1, start, mid - start) * (mid - start) + mean(array1, mid, end - mid) * (end - mid),
                    sum(array1, start, end - start) * (end - start), TOL);
            assertEquals(nanMean(array1, start, mid - start) * nanCount(array1, start, mid - start) +
                            nanMean(array1, mid, end - mid) * nanCount(array1, mid, end - mid),
                    nanMean(array1, start, end - start) * nanCount(array1, start, end - start), TOL);

            assertEquals(mean(array2, start, mid - start) * (mid - start) + mean(array2, mid, end - mid) * (end - mid),
                    mean(array2, start, end - start) * (end - start), TOL);
            assertEquals(nanMean(array2, start, mid - start) * nanCount(array2, start, mid - start) + nanMean(array2, mid, end - mid) * nanCount(array2, mid, end - mid),
                    nanMean(array2, start, end - start) * nanCount(array2, start, end - start), TOL);


            assertEquals(Double.NaN, mean(array1, 0, 0));
            assertEquals(Double.NaN, nanMean(array1, 10, 0));
        }
    }

    @Test
    void testVariances() {
        double[] array1 = newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : row);
        double[] array2 = newFrom(0, 100, row -> row);

        for (int i = 0; i < 1000; i++) {
            int start = RandomSource.nextInt(50);
            int end = RandomSource.nextInt(49) + 51;
            int len = end - start;

            assertEquals(nanVariance(array1, start, len), Variance.of(VarDouble.wrap(Arrays.copyOfRange(array1, start, end))).value(), TOL);
            assertEquals(variance(array2, start, len), Variance.of(VarDouble.wrap(Arrays.copyOfRange(array2, start, end))).value(), TOL);
        }

        assertEquals(Double.NaN, variance(array1, 0, 0));
        assertEquals(Double.NaN, nanVariance(array1, 10, 0));
    }

    @Test
    void testAdd() {
        double[] array1 = newFrom(0, 100, row -> row);

        for (int i = 0; i < 100; i++) {
            int start = RandomSource.nextInt(50);
            int len = 50 + RandomSource.nextInt(50) - start;

            double[] array2 = copy(array1, start, len);
            DoubleArrays.add(array2, 0, 10, len);
            double[] array3 = new double[len];
            addTo(array1, start, 10, array3, 0, len);

            assertArrayEquals(array2, array3, TOL);

            double[] array4 = newFill(len, 10);
            double[] array5 = new double[len];
            addTo(copy(array1, start, len), 0, array4, 0, array5, 0, len);
            double[] array6 = new double[len];
            addTo(copy(array1, start, len), 0, array4, 0, array6, 0, len);

            double[] array7 = copy(array1, start, len);
            add(array7, 0, array4, 0, len);

            assertArrayEquals(array5, array6);
            assertArrayEquals(array5, array7);
        }
    }

    @Test
    void testMinus() {
        double[] array1 = newFrom(0, 100, row -> row);

        for (int i = 0; i < 100; i++) {
            int start = RandomSource.nextInt(50);
            int len = RandomSource.nextInt(50);

            double[] array2 = copy(array1, start, len);
            sub(array2, 0, 10, len);
            double[] array3 = new double[len];
            subTo(array1, start, 10, array3, 0, len);

            assertArrayEquals(array2, array3, TOL);

            double[] array4 = newFill(len, 10);
            double[] array5 = copy(array1, start, len);
            sub(array5, 0, array4, 0, len);
            double[] array6 = new double[len];
            subTo(array1, start, array4, 0, array6, 0, len);

            double[] array7 = copy(array1, start, len);
            sub(array7, 0, array4, 0, len);

            assertArrayEquals(array5, array6);
            assertArrayEquals(array5, array7);
        }
    }

    @Test
    void testDot() {
        double[] array1 = newFrom(0, 100, row -> row);

        for (int i = 0; i < 100; i++) {
            int start = RandomSource.nextInt(50);
            int end = 50 + RandomSource.nextInt(50);
            int len = end - start;

            double[] array2 = copy(array1, start, len);
            mult(array2, 0, 10, len);
            double[] array3 = new double[len];
            multTo(array1, start, 10, array3, 0, len);

            assertArrayEquals(array2, array3, TOL);

            double[] array4 = newFill(len, 10);
            double[] array5 = copy(array1, start, len);
            mult(array5, 0, array4, 0, len);
            double[] array6 = new double[len];
            multTo(array1, start, array4, 0, array6, 0, len);

            double[] array7 = copy(array1, start, len);
            mult(array7, 0, array4, 0, len);

            assertArrayEquals(array5, array6);
            assertArrayEquals(array5, array7);
        }
    }

    @Test
    void testDiv() {
        double[] array1 = newFrom(0, 100, row -> row);

        for (int i = 0; i < 100; i++) {
            int start = RandomSource.nextInt(50);
            int len = 50 + RandomSource.nextInt(50) - start;

            double[] array2 = copy(array1, start, len);
            div(array2, 0, 10, len);
            double[] array3 = new double[len];
            divTo(array1, start, 10, array3, 0, len);

            assertArrayEquals(array2, array3, TOL);

            double[] array4 = newFill(len, 10);
            double[] array5 = copy(array1, start, len);
            div(array5, 0, array4, 0, len);
            double[] array6 = new double[len];
            divTo(array1, start, array4, 0, array6, 0, len);

            double[] array7 = copy(array1, start, len);
            div(array7, 0, array4, 0, len);

            assertArrayEquals(array5, array6);
            assertArrayEquals(array5, array7);
        }
    }

    @Test
    void testCapacity() {
        double[] array1 = newSeq(0, 100);

        // new copy preserving 10
        double[] array2 = forceCapacity(array1, 10, 10);
        assertTrue(DoubleArrays.equals(array1, 0, array2, 0, 10));
        assertEquals(10, array2.length);

        // new copy preserving 80
        double[] array3 = forceCapacity(array1, 120, 80);
        assertTrue(DoubleArrays.equals(array1, 0, array3, 0, 80));
        assertEquals(120, array3.length);

        // leave array untouched
        double[] array4 = ensureCapacity(array1, 10);
        assertTrue(DoubleArrays.equals(array1, 0, array4, 0, 100));
        assertEquals(100, array4.length);

        // new copy preserving all available
        double[] array5 = ensureCapacity(array1, 120);
        assertTrue(DoubleArrays.equals(array1, 0, array5, 0, 100));
        assertEquals(120, array5.length);

        // new copy preserving 10
        double[] array6 = ensureCapacity(array1, 120, 10);
        assertTrue(DoubleArrays.equals(array1, 0, array6, 0, 10));
        assertTrue(DoubleArrays.equals(newFill(90, 0), 0, array6, 10, 90));

        // leave untouched
        double[] array7 = grow(array1, 10);
        assertTrue(DoubleArrays.equals(array1, 0, array7, 0, 100));
        assertEquals(100, array7.length);

        // new copy preserving all
        double[] array8 = grow(array1, 120);
        assertTrue(DoubleArrays.equals(array1, 0, array8, 0, 100));
        assertEquals(150, array8.length);

        // new copy preserving 10
        double[] array9 = grow(array1, 200, 10);
        assertTrue(DoubleArrays.equals(array1, 0, array9, 0, 10));
        assertTrue(DoubleArrays.equals(newFill(190, 0), 0, array9, 10, 190));
        assertEquals(200, array9.length);

        // trim array to 10
        double[] array10 = trim(array1, 10);
        assertEquals(10, array10.length);
        assertTrue(DoubleArrays.equals(array1, 0, array10, 0, 10));
    }

    @Test
    void testSorting() {

        int len = 100_000;
        double[] a = Normal.std().sample(len).elements();
        double[] b = Normal.std().sample(len).elements();

        assertAsc(a, DoubleArrays::quickSort);
        assertAsc(a, DoubleArrays::parallelQuickSort);

        assertDesc(a, DoubleArrays::quickSort);
        assertDesc(a, DoubleArrays::parallelQuickSort);

        assertAscIndirect(a, DoubleArrays::quickSortIndirect);
        assertAscIndirect(a, DoubleArrays::parallelQuickSortIndirect);

        assertAsc(a, DoubleArrays::mergeSort);
        assertDesc(a, DoubleArrays::mergeSort);

        assertAsc(a, DoubleArrays::stableSort);
        assertDesc(a, DoubleArrays::stableSort);

        assertAsc(a, DoubleArrays::radixSort);
        assertAsc(a, DoubleArrays::parallelQuickSort);

        assertAscIndirect(a, (p, v) -> DoubleArrays.radixSortIndirect(p, v, true));
        assertAscIndirect(a, (p, v) -> DoubleArrays.parallelRadixSortIndirect(p, v, true));

        assertAsc2(a, b, DoubleArrays::quickSort);
        assertAsc2(a, b, DoubleArrays::parallelQuickSort);
        assertAsc2(a, b, DoubleArrays::radixSort);
        assertAsc2(a, b, DoubleArrays::parallelRadixSort);
    }

    private void assertAsc(double[] src, Consumer<double[]> fun) {
        var s = DoubleArrays.copy(src, 0, src.length);
        fun.accept(s);
        for (int i = 1; i < s.length; i++) {
            assertTrue(s[i - 1] <= s[i]);
        }
    }

    private void assertDesc(double[] src, BiConsumer<double[], DoubleComparator> fun) {
        var s = DoubleArrays.copy(src, 0, src.length);
        fun.accept(s, DoubleComparators.OPPOSITE_COMPARATOR);
        for (int i = 1; i < s.length; i++) {
            assertTrue(s[i - 1] >= s[i]);
        }
    }

    private void assertAsc2(double[] a, double[] b, BiConsumer<double[], double[]> alg) {
        double[] sa = DoubleArrays.copy(a, 0, a.length);
        double[] sb = DoubleArrays.copy(b, 0, b.length);
        alg.accept(sa, sb);
        for (int i = 1; i < sa.length; i++) {
            if (sa[i - 1] == sa[i]) {
                assertTrue(sb[i - 1] <= sb[i]);
            } else {
                assertTrue(sa[i - 1] < sa[i]);
            }
        }
    }

    private void assertAscIndirect(double[] array, BiConsumer<int[], double[]> alg) {
        int[] perm = IntArrays.newSeq(0, array.length);
        alg.accept(perm, array);
        for (int i = 1; i < array.length; i++) {
            assertTrue(array[perm[i - 1]] <= array[perm[i]]);
        }
    }

}
