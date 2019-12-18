package rapaio.data.filter;

import org.junit.jupiter.api.Test;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VApplyIntTest {

    @Test
    void testApplyInt() {
        VFilter vf = VApplyInt.with(x -> {
            if (x == Integer.MIN_VALUE) return 0;
            return (x > 0) ? (x * x) : (-x * x);
        });

        Var x = VarDouble.wrap(0, Double.NaN, 1, Double.NaN, -12, 3);

        Var y = x.copy().fapply(vf);
        assertEquals(0, y.getDouble(0), 1e-20);
        assertEquals(0, y.getDouble(1), 1e-20);
        assertEquals(1, y.getDouble(2), 1e-20);
        assertEquals(0, y.getDouble(3), 1e-20);
        assertEquals(-144, y.getDouble(4), 1e-20);
        assertEquals(9, y.getDouble(5), 1e-20);
    }
}
