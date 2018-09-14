package rapaio.ml.eval;

import org.junit.Test;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/6/17.
 */
public class MAETest {

    private static final double TOL = 1e-20;

    @Test
    public void smokeTest() {

        final int N = 100;
        Var x = VarDouble.fill(N, 0).withName("x");
        Var y = VarDouble.seq(1, N).withName("y");
        Var z = VarDouble.from(y, value -> -value).withName("z");


        MAE mae1 = MAE.from(x, y);
        MAE mae2 = MAE.from(x, z);

        MAE mae3 = MAE.from(SolidFrame.byVars(x, x), SolidFrame.byVars(y, z));

        assertEquals((N+1)/2.0, mae1.mae(0), TOL);
        assertEquals(mae1.mae(0), mae1.totalMae(), TOL);

        assertEquals(mae1.mae(0), mae2.mae(0), TOL);

        double[] mae_values_3 = mae3.mae();
        assertEquals(mae1.mae(0), mae_values_3[0], TOL);
        assertEquals(mae2.mae(0), mae_values_3[1], TOL);

        assertEquals("> MAE (Mean Absolute Error):\n" +
                "\n" +
                " names  mae\n" +
                " x | y 50.5\n" +
                "\n" +
                "Total mae: 50.5\n" +
                "\n", mae1.summary());
    }
}
