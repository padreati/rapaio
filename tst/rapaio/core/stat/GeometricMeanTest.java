package rapaio.core.stat;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/18.
 */
public class GeometricMeanTest {

    private static final double TOL = 1e-12;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDouble() {
        VarDouble x = VarDouble.wrap(2, 2, 2, 2);
        GeometricMean mean = GeometricMean.of(x);
        assertEquals(2, mean.value(), TOL);

        assertEquals("> geometricMean[?]\n" +
                "total rows: 4 (complete: 4, missing: 0, negative values: 0)\n" +
                "mean: 2\n", mean.toSummary());
        assertEquals(4, GeometricMean.of(VarDouble.wrap(2, 4, 8)).value(), TOL);
        assertEquals(Double.NaN, GeometricMean.of(VarDouble.wrap(Double.NaN)).value(), TOL);
        assertFalse(GeometricMean.of(VarDouble.wrap(-1)).isDefined());
    }
}
