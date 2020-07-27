package rapaio.ml.regression.linear;

import org.junit.jupiter.api.Test;
import rapaio.core.stat.Mean;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/27/20.
 */
public class CenteringTest {

    @Test
    void centeringTest() {
        assertEquals(Centering.NONE.compute(null), 0.0);
        assertEquals(Centering.NONE.compute(VarDouble.seq(10)), 0.0);

        assertEquals(Centering.MEAN.compute(VarDouble.seq(10)), Mean.of(VarDouble.seq(10)).value());
        assertEquals(Centering.MEAN.compute(VarDouble.seq(100)), Mean.of(VarDouble.seq(100)).value());
    }
}
