package rapaio.data.filter.var;

import org.junit.Test;
import rapaio.core.*;
import rapaio.data.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VShuffleTest {

    @Test
    public void testShuffle() {
        RandomSource.setSeed(1);
        double N = 1000.0;
        Var x = VarDouble.seq(0, N, 1);
        Var first = VarDouble.empty();
        for (int i = 0; i < 100; i++) {
            Var y = x.fapply(VShuffle.filter());
            double t = y.stream().mapToDouble().sum();
            assertEquals(N * (N + 1) / 2, t, 1e-30);
            first.addDouble(y.getDouble(0));
        }
    }
}
