package rapaio.data.filter.frame;

import org.junit.Test;
import rapaio.data.*;

import static org.junit.Assert.assertFalse;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class FShuffleTest {

    @Test
    public void testDouble() {

        Frame orig = FFilterTestUtil.allDoubles(100, 1);
        Frame[] shuffles = new Frame[10];
        for (int i = 0; i < shuffles.length; i++) {
            shuffles[i] = orig.fapply(FShuffle.filter().newInstance());
        }
        for (int i = 1; i < shuffles.length; i++) {
            assertFalse(shuffles[i-1].deepEquals(shuffles[i]));
        }
    }
}
