package rapaio.core.stat;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/18.
 */
public class MeanTest {

    private static final double TOL = 1e-20;

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testDouble() {
        VarDouble x = VarDouble.from(100, row -> row % 2 == 0 ? Double.NaN : row);
        Mean mean = Mean.of(x);
        assertEquals(50, mean.value(), TOL);

        assertEquals("> mean[?]\n" +
                "total rows: 100 (complete: 50, missing: 50)\n" +
                "mean: 50\n", mean.toSummary());

        assertEquals(Double.NaN, Mean.of(VarDouble.wrap(Double.NaN)).value(), TOL);

        double[] values = x.stream().mapToDouble().toArray();
        assertEquals(50, Mean.of(values, 0, values.length).value(), TOL);

        assertEquals(Double.NaN, Mean.of(VarDouble.empty(10).stream().mapToDouble().toArray(), 0, 10).value(), TOL);
    }
}
