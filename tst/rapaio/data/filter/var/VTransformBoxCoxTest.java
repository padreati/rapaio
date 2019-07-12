package rapaio.data.filter.var;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/18.
 */
public class VTransformBoxCoxTest {

    private static final double TOL = 1e-20;

    @Test
    public void testDouble() {

        RandomSource.setSeed(1233);
        Normal normal = Normal.of(1, 10);
        double[] values = new double[100];
        for (int i = 0; i < values.length; i++) {
            values[i] = normal.sampleNext();
        }
        Var x = VarDouble.wrap(values);

        Var bc1 = x.copy().fapply(VTransformBoxCox.with(0));
        Var bc2 = x.copy().fapply(VTransformBoxCox.with(1.1, 12));

        assertEquals(1.1, VTransformBoxCox.with(1.1, 2.0).lambda(), TOL);
        assertEquals(2.0, VTransformBoxCox.with(1.1, 2.0).shift(), TOL);

        for (int i = 0; i < x.rowCount(); i++) {
            assertEquals(Math.log(x.getDouble(i)), bc1.getDouble(i), TOL);
            assertEquals((Math.pow(x.getDouble(i) + 12, 1.1) - 1) / 1.1, bc2.getDouble(i), TOL);
        }
    }
}
