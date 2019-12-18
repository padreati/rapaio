package rapaio.util.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/18/19.
 */
public class DoubleArraysTest {

    private static final double TOL = 1e-20;

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
}
