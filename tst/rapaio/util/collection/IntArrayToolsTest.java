package rapaio.util.collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/11/19.
 */
public class IntArrayToolsTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void buildersTest() {
        assertArrayEquals(new int[]{10, 10, 10}, IntArrayTools.newFill(3, 10));
        assertArrayEquals(new int[]{10, 11, 12}, IntArrayTools.newSeq(10, 13));
        assertArrayEquals(new int[]{4, 9, 16}, IntArrayTools.newFrom(new int[]{1, 2, 3, 4, 5}, 1, 4, x -> x * x));
        assertArrayEquals(new int[]{3, 5}, IntArrayTools.newCopy(new int[]{1, 3, 5, 7}, 1, 3));
    }

    private void testEqualArrays(int[] actual, int... expected) {
        Assertions.assertArrayEquals(expected, actual);
    }
}
