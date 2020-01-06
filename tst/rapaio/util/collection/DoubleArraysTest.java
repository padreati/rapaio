package rapaio.util.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Variance;
import rapaio.data.VarDouble;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static rapaio.util.collection.DoubleArrays.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/18/19.
 */
public class DoubleArraysTest {

    private static final double TOL = 1e-12;
    private Normal normal = Normal.std();

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void buildersTest() {
        assertArrayEquals(new double[]{10., 10., 10.}, DoubleArrays.newFill(3, 10.));
        assertArrayEquals(new double[]{10., 11., 12.}, DoubleArrays.newSeq(10, 13));
        assertArrayEquals(new double[]{4., 9., 16.}, DoubleArrays.newFrom(new double[]{1, 2, 3, 4, 5}, 1, 4, x -> x * x));
        assertArrayEquals(new double[]{3., 5.}, DoubleArrays.newCopy(new double[]{1, 3, 5, 7}, 1, 3));
    }

    @Test
    void capacityTest() {
        double[] array1 = new double[]{1., 2., 3., 4., 5.};
        assertTrue(DoubleArrays.checkCapacity(array1, 2));
        assertTrue(DoubleArrays.checkCapacity(array1, array1.length));
        assertFalse(DoubleArrays.checkCapacity(array1, array1.length + 1));

        double[] array2 = DoubleArrays.ensureCapacity(array1, 10);
        for (int i = 0; i < 10; i++) {
            if (i < array1.length) {
                assertEquals(array1[i], array2[i], TOL);
            } else {
                assertEquals(0, array2[i], TOL);
            }
        }

        double[] array3 = DoubleArrays.ensureCapacity(array1, 2);
        assertArrayEquals(array1, array3);
    }

    @Test
    void sortingTest() {
        int N = 1000;
        double[] array1 = DoubleArrays.newFrom(DoubleArrays.newSeq(0, N), 0, N, x -> 1000. - x);

        double[] sort1 = DoubleArrays.newCopy(array1, 0, N);
        Arrays.sort(sort1);

        double[] sort2 = DoubleArrays.newCopy(array1, 0, N);
        DoubleArrays.quickSort(sort2, 0, N, DoubleComparator.ASC_COMPARATOR);

        double[] sort3 = DoubleArrays.newCopy(array1, 0, N);
        DoubleArrays.quickSort(sort3, 0, N, DoubleComparator.DESC_COMPARATOR);
        DoubleArrays.reverse(sort3);
        DoubleArrays.reverse(sort3);
        DoubleArrays.reverse(sort3, 0, N);

        assertArrayEquals(sort1, sort2);
        assertArrayEquals(sort1, sort3);

        double[] sort4 = DoubleArrays.newCopy(array1, 0, 10);
        DoubleArrays.quickSort(sort4, 0, 10, DoubleComparator.ASC_COMPARATOR);
        for (int i = 1; i < 10; i++) {
            assertTrue(sort4[i - 1] <= sort4[i]);
        }

        int[] indexes1 = IntArrays.newSeq(0, N);
        DoubleArrays.quickSortIndirect(indexes1, array1, 0, N);
        for (int i = 1; i < N; i++) {
            assertTrue(array1[indexes1[i - 1]] <= array1[indexes1[i]]);
        }

        int[] indexes2 = IntArrays.newSeq(0, 10);
        DoubleArrays.quickSortIndirect(indexes2, array1, 0, 10);
        for (int i = 1; i < 10; i++) {
            assertTrue(array1[indexes1[i - 1]] <= array1[indexes1[i]]);
        }
    }

    @Test
    void shuffleTest() {
        int N = 1000;
        double[] array1 = DoubleArrays.newSeq(0, N);
        double[] shuffle1 = DoubleArrays.newCopy(array1, 0, N);
        DoubleArrays.shuffle(shuffle1, RandomSource.getRandom());

        double[] shuffle2 = DoubleArrays.newCopy(array1, 0, N);
        DoubleArrays.shuffle(shuffle2, 10, 100, RandomSource.getRandom());

        int sum1 = 0;
        DoubleIterator it = DoubleArrays.iterator(shuffle1, 0, N);
        while (it.hasNext()) {
            sum1 += it.nextDouble();
        }
        double sum2 = DoubleArrays.stream(shuffle2, 0, N).sum();
        assertEquals(sum1, sum2, TOL);

        boolean equal = true;
        for (int i = 0; i < N; i++) {
            if (shuffle1[i] != shuffle2[i]) {
                equal = false;
                break;
            }
        }
        assertFalse(equal);
    }

    @Test
    void testDelete() {
        testEqualArrays(DoubleArrays.delete(new double[]{1, 2, 3}, 3, 0), 2, 3, 3);
        testEqualArrays(DoubleArrays.delete(new double[]{1, 2, 3}, 3, 1), 1, 3, 3);
        testEqualArrays(DoubleArrays.delete(new double[]{1, 2, 3}, 3, 2), 1, 2, 3);
    }

    private void testEqualArrays(double[] actual, double... expected) {
        assertArrayEquals(expected, actual, TOL);
    }

    @Test
    void testIterator() {
        double[] array = DoubleArrays.newFrom(0, 100, row -> normal.sampleNext());
        DoubleIterator it1 = DoubleArrays.iterator(array, 0, 10);
        for (int i = 0; i < 10; i++) {
            assertEquals(array[i], it1.nextDouble(), TOL);
        }
        assertThrows(NoSuchElementException.class, it1::nextDouble);

        DoubleIterator it2 = DoubleArrays.iterator(array, 0, 100);
        for (int i = 0; i < 100; i++) {
            assertEquals(array[i], it2.nextDouble(), TOL);
        }
        assertThrows(NoSuchElementException.class, it2::nextDouble);
    }

    @Test
    void testCounts() {
        double[] array1 = DoubleArrays.newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : normal.sampleNext());
        assertEquals(4, nancount(array1, 0, 5));
        assertEquals(6, nancount(array1, 10, 17));
        assertEquals(85, nancount(array1, 0, 100));
    }

    @Test
    void testSums() {
        double[] array1 = DoubleArrays.newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : normal.sampleNext());
        double[] array2 = DoubleArrays.newFrom(0, 100, row -> normal.sampleNext());

        for (int i = 0; i < 1000; i++) {
            int start = RandomSource.nextInt(30);
            int mid = RandomSource.nextInt(30) + start;
            int end = RandomSource.nextInt(40) + mid;

            assertEquals(sum(array1, start, mid) + sum(array1, mid, end), sum(array1, start, end), TOL);
            assertEquals(nansum(array1, start, mid) + nansum(array1, mid, end), nansum(array1, start, end), TOL);

            assertEquals(sum(array2, start, mid) + sum(array2, mid, end), sum(array2, start, end), TOL);
            assertEquals(nansum(array2, start, mid) + nansum(array2, mid, end), nansum(array2, start, end), TOL);
        }
    }

    @Test
    void testMeans() {
        double[] array1 = DoubleArrays.newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : normal.sampleNext());
        double[] array2 = DoubleArrays.newFrom(0, 100, row -> normal.sampleNext());

        for (int i = 0; i < 1000; i++) {
            int start = RandomSource.nextInt(20) + 10;
            int mid = RandomSource.nextInt(19) + start + 10;
            int end = RandomSource.nextInt(29) + mid + 10;

            assertEquals(mean(array1, start, mid) * (mid - start) + mean(array1, mid, end) * (end - mid),
                    sum(array1, start, end) * (end - start), TOL);
            assertEquals(nanmean(array1, start, mid) * nancount(array1, start, mid) + nanmean(array1, mid, end) * nancount(array1, mid, end),
                    nanmean(array1, start, end) * nancount(array1, start, end), TOL);

            assertEquals(mean(array2, start, mid) * (mid - start) + mean(array2, mid, end) * (end - mid),
                    mean(array2, start, end) * (end - start), TOL);
            assertEquals(nanmean(array2, start, mid) * nancount(array2, start, mid) + nanmean(array2, mid, end) * nancount(array2, mid, end),
                    nanmean(array2, start, end) * nancount(array2, start, end), TOL);


            assertEquals(Double.NaN, mean(array1, 0, 0));
            assertEquals(Double.NaN, nanmean(array1, 10, 10));
        }
    }

    @Test
    void testVariances() {
        double[] array1 = DoubleArrays.newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : row);
        double[] array2 = DoubleArrays.newFrom(0, 100, row -> row);

        for (int i = 0; i < 1000; i++) {
            int start = RandomSource.nextInt(50);
            int end = RandomSource.nextInt(49) + 51;

            assertEquals(nanvariance(array1, start, end), Variance.of(VarDouble.wrap(Arrays.copyOfRange(array1, start, end))).value(), TOL);
            assertEquals(variance(array2, start, end), Variance.of(VarDouble.wrap(Arrays.copyOfRange(array2, start, end))).value(), TOL);
        }

        assertEquals(Double.NaN, variance(array1, 0, 0));
        assertEquals(Double.NaN, nanvariance(array1, 10, 10));
    }
}
