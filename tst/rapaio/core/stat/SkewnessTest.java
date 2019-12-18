package rapaio.core.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/10/18.
 */
public class SkewnessTest {

    private static final double TOL = 1e-12;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDouble() {
        Skewness sk = Skewness.of(VarDouble.wrap(1, 2, 45, 109, 200));

        // these values were computed in R from
        // library(fBasics)
        // skewness(c(1, 2, 45, 109, 200))
        assertEquals(0.493673230307975, sk.value(), TOL);
        assertEquals(0.493673230307975, sk.b1(), TOL);
        assertEquals(0.6899293135253384, sk.g1(), TOL);
        assertEquals(1.0284858964749477, sk.bigG1(), TOL);

        assertEquals("> skewness[?]\n" +
                "total rows: 5 (complete: 5, missing: 0)\n" +
                "skewness (g1): 0.6899293\n" +
                "skewness (b1): 0.4936732\n" +
                "skewness (G1): 1.0284859\n", sk.toSummary());
    }
}
