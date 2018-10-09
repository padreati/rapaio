package rapaio.core.stat;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/18.
 */
public class MinimumTest {

    private static final double TOL = 1e-20;

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testDouble() {
        VarDouble x = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : RandomSource.nextDouble());
        double min = 1.0;
        for (int i = 0; i <x.rowCount(); i++) {
            if(x.isMissing(i)) {
                continue;
            }
            min = Math.min(min, x.getDouble(i));
        }
        Minimum minimum = Minimum.of(x);
        assertEquals(min, minimum.value(), TOL);

        assertEquals("> minimum[?]\n" +
                "total rows: 100 (complete: 85, missing: 15)\n" +
                "minimum: 0.0174893\n", minimum.summary());
    }
}
