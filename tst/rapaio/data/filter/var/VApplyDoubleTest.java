package rapaio.data.filter.var;

import org.junit.Test;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.filter.VFilter;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VApplyDoubleTest {

    private static final double TOL = 1e-20;

    @Test
    public void testApplyDouble() {
        VFilter vf = VApplyDouble.with(x -> {
            if (Double.isNaN(x))
                return 0.0;
            return (x > 0) ? (x * x) : (-x * x);
        });

        Var x = VarDouble.wrap(0, Double.NaN, 1, Double.NaN, -12, 3.1);

        double[] a1 = new double[] {0, 0, 1, 0, -144, 3.1*3.1};
        double[] a2 = x.copy().fapply(vf).stream().mapToDouble().toArray();
        double[] a3 = x.stream().mapToDouble().toArray();
        double[] a4 = x.fapply(vf).stream().mapToDouble().toArray();

        assertArrayEquals(a1, a2, TOL);
        assertEquals(Double.NaN, a3[1], TOL);
        assertArrayEquals(a1, a4, TOL);
    }
}
