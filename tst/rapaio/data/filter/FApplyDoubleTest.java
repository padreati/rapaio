package rapaio.data.filter;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.VRange;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class FApplyDoubleTest {

    private static final double TOL = 1e-20;

    @Test
    public void testDouble() {

        Frame df = FFilterTestUtil.allDoubles(100, 2);
        Frame sign = df.copy().fapply(FApplyDouble.on(Math::signum, VRange.all()));

        for (int i = 0; i < df.varCount(); i++) {
            for (int j = 0; j < df.rowCount(); j++) {
                assertEquals(Math.signum(df.getDouble(j, i)), sign.getDouble(j, i), TOL);
            }
        }

        Frame sign2 = df.copy().fapply(FApplyDouble.on(Math::signum, VRange.all()).newInstance());
        assertTrue(sign.deepEquals(sign2));
    }
}
