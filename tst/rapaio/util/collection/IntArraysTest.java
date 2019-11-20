package rapaio.util.collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/11/19.
 */
public class IntArraysTest {

    @Before
    public void setUp() {
        RandomSource.setSeed(1234);
    }

    @Test
    public void buildersTest() {
        assertArrayEquals(new int[]{10, 10, 10}, IntArrays.newFill(3, 10));
        assertArrayEquals(new int[]{10, 11, 12}, IntArrays.newSeq(10, 13));
        assertArrayEquals(new int[]{4, 9, 16}, IntArrays.newFrom(new int[]{1, 2, 3, 4, 5}, 1, 4, x -> x * x));
        assertArrayEquals(new int[]{3, 5}, IntArrays.newCopy(new int[]{1, 3, 5, 7}, 1, 3));
    }

    @Test
    public void capacityTest() {
        int[] array1 = new int[]{1, 2, 3, 4, 5};
        assertTrue(IntArrays.checkCapacity(array1, 2));
        assertTrue(IntArrays.checkCapacity(array1, array1.length));
        assertFalse(IntArrays.checkCapacity(array1, array1.length + 1));

        int[] array2 = IntArrays.ensureCapacity(array1, 10);
        for (int i = 0; i < 10; i++) {
            if (i < array1.length) {
                assertEquals(array1[i], array2[i]);
            } else {
                assertEquals(0, array2[i]);
            }
        }

        int[] array3 = IntArrays.ensureCapacity(array1, 2);
        assertArrayEquals(array1, array3);
    }

    @Test
    public void sortingTest() {
        int N = 1000;
        int[] array1 = IntArrays.newFrom(IntArrays.newSeq(0, N), 0, N, x -> 1000 - x);

        int[] sort1 = IntArrays.newCopy(array1, 0, N);
        Arrays.sort(sort1);

        int[] sort2 = IntArrays.newCopy(array1, 0, N);
        IntArrays.quickSort(sort2, 0, N, IntComparator.ASC_COMPARATOR);

        int[] sort3 = IntArrays.newCopy(array1, 0, N);
        IntArrays.quickSort(sort3, 0, N, IntComparator.DESC_COMPARATOR);
        IntArrays.reverse(sort3);
        IntArrays.reverse(sort3);
        IntArrays.reverse(sort3, 0, N);

        assertArrayEquals(sort1, sort2);
        assertArrayEquals(sort1, sort3);
    }

    @Test
    public void shuffleTest() {
        int N = 1000;
        int[] array1 = IntArrays.newSeq(0, N);
        int[] shuffle1 = IntArrays.newCopy(array1, 0, N);
        IntArrays.shuffle(shuffle1, RandomSource.getRandom());

        int[] shuffle2 = IntArrays.newCopy(array1, 0, N);
        IntArrays.shuffle(shuffle2, 10, 100, RandomSource.getRandom());

        int sum1 = 0;
        IntIterator it = IntArrays.iterator(shuffle1, 0, N);
        while (it.hasNext()) {
            sum1 += it.nextInt();
        }
        int sum2 = IntArrays.stream(shuffle2, 0, N).sum();
        assertEquals(sum1, sum2);

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
    public void testDelete() {

        testEqualArrays(IntArrays.delete(new int[]{1, 2, 3}, 3, 0), 2, 3, 3);
        testEqualArrays(IntArrays.delete(new int[]{1, 2, 3}, 3, 1), 1, 3, 3);
        testEqualArrays(IntArrays.delete(new int[]{1, 2, 3}, 3, 2), 1, 2, 3);
    }

    private void testEqualArrays(int[] actual, int... expected) {
        Assert.assertArrayEquals(expected, actual);
    }
}
