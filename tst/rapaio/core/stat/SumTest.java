package rapaio.core.stat;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/18.
 */
public class SumTest {

    private static final double TOL = 1e-12;

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testDouble() {
        VarDouble x = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : RandomSource.nextDouble());
        double s = 0.0;
        for (int i = 0; i <x.rowCount(); i++) {
            if(x.isMissing(i)) {
                continue;
            }
            s += x.getDouble(i);
        }
        Sum sum = Sum.of(x);
        assertEquals(s, sum.value(), TOL);

        assertEquals("> sum[?]\n" +
                "total rows: 100 (complete: 85, missing: 15)\n" +
                "sum: 42.3307915\n", sum.toSummary());
    }
}
