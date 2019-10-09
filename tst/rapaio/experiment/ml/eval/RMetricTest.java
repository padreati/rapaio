package rapaio.experiment.ml.eval;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/15/19.
 */
public class RMetricTest {

    private static final double TOL = 1e-20;

    @Test
    public void testRMS() {

        RMetric rms = RMetric.RMS;

        assertEquals("RMS", rms.name());

        // missing values

        assertTrue(Double.isNaN(rms.compute(VarDouble.empty(), VarDouble.empty())));
        assertTrue(Double.isNaN(rms.compute(VarDouble.wrap(Double.NaN, 1), VarDouble.wrap(1, Double.NaN))));

        // single value

        assertEquals(0.0, rms.compute(VarDouble.wrap(Double.NaN, 1, 2), VarDouble.wrap(2, 1, Double.NaN)), TOL);

        // equal values

        VarDouble x = VarDouble.from(100, RandomSource::nextDouble);
        assertEquals(0.0, rms.compute(x, x), TOL);

        VarDouble y = VarDouble.from(x, val -> val + 1);
        assertEquals(1.0, rms.compute(x, y), TOL);

        VarDouble z = VarDouble.from(100, RandomSource::nextDouble);
        assertNotEquals(0, rms.compute(x, z), TOL);
    }
}
