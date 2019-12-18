package rapaio.data.filter;

import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/1/18.
 */
public class VQuantileDiscreteTest {

    @Test
    void testDouble() {
        Var x = VarDouble.seq(10, 11, 0.01);

        VFilter f1 = VQuantileDiscrete.split(2);
        Var q1 = x.fapply(f1);

        assertEquals(101, q1.rowCount());
        assertEquals(3, q1.levels().size());
        assertEquals("?", q1.levels().get(0));
        assertEquals("-Inf~10.5", q1.levels().get(1));
        assertEquals("10.5~Inf", q1.levels().get(2));

        for (int i = 0; i < x.rowCount() / 2 + 1; i++) {
            assertEquals("-Inf~10.5", q1.getLabel(i));
        }
        for (int i = x.rowCount() / 2 + 1; i < x.rowCount(); i++) {
            assertEquals("10.5~Inf", q1.getLabel(i));
        }

        VFilter f2 = VQuantileDiscrete.with(0.25, 0.50, 0.75);
        Var q2 = x.fapply(f2);

        assertEquals(101, q2.rowCount());
        assertEquals(5, q2.levels().size());
        for (int i = 1; i < x.rowCount(); i++) {
            assertTrue(x.getInt(i - 1) <= x.getInt(i));
        }

        Var y = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : RandomSource.nextDouble());
        Var qy = y.fapply(VQuantileDiscrete.split(10));
        for (int i = 0; i < y.rowCount(); i++) {
            if (y.isMissing(i)) {
                assertTrue(qy.isMissing(i));
            } else {
                assertFalse(qy.isMissing(i));
            }
        }
    }

    @Test
    void testInvalidNumperOfPercentiles() {
        var ex = assertThrows(IllegalArgumentException.class, VQuantileDiscrete::with);
        assertEquals("Number of quantiles must be positive.", ex.getMessage());
    }

    @Test
    void testInvalidK() {
        var ex = assertThrows(IllegalArgumentException.class, () -> VQuantileDiscrete.split(1));
        assertEquals("Number of parts k: 1 of the split must be greater than 1.", ex.getMessage());
    }
}
