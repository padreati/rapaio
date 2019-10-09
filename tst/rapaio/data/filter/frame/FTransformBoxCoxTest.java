package rapaio.data.filter.frame;

import org.junit.Test;
import rapaio.data.*;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class FTransformBoxCoxTest {

    @Test
    public void testDouble() {
        Frame src = FFilterTestUtil.allDoubleNominal(100, 2, 2);

        Frame f1 = src.copy().fapply(FTransformBoxCox.on(1.1, 1.1, VRange.of(1)));
        Frame f2 = src.copy().fapply(FTransformBoxCox.on(1.1, 1.1, "v2").newInstance());

        assertTrue(f1.deepEquals(f2));
        assertFalse(src.deepEquals(f1));
    }
}
