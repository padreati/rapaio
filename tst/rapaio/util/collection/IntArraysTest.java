package rapaio.util.collection;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/11/19.
 */
public class IntArraysTest {

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
