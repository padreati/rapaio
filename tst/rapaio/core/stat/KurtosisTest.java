package rapaio.core.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/10/18.
 */
public class KurtosisTest {

    private static final double TOL = 1e-12;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDouble() {
        Kurtosis kt = Kurtosis.of(VarDouble.wrap(1, 2, 45, 109, 200));

        // these values were computed in R from
        // library(fBasics)
        // kurtosis(c(1, 2, 45, 109, 200))

        assertEquals(-1.7174503726358747, kt.value(), TOL);
        assertEquals(-0.9960162072435548, kt.g2(), TOL);
        assertEquals(-1.7174503726358747, kt.b2(), TOL);
        assertEquals(0.007967585512890452, kt.bigG2(), TOL);

        assertEquals("> kurtosis[?]\n" +
                "total rows: 5 (complete: 5, missing: 0)\n" +
                "kurtosis (g2): -0.9960162\n" +
                "kurtosis (b2): -1.7174504\n" +
                "kurtosis (G2): 0.0079676\n", kt.toSummary());
    }
}
