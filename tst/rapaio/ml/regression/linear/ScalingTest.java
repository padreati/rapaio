package rapaio.ml.regression.linear;

import org.junit.jupiter.api.Test;
import rapaio.core.stat.Variance;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/27/20.
 */
public class ScalingTest {

    @Test
    void scaleTest() {
        assertEquals("None", Scaling.NONE.getName());
        assertEquals(Scaling.NONE.compute(VarDouble.seq(10)), 1.0);
        assertEquals(Scaling.NONE.compute(VarDouble.seq(100)), 1.0);

        assertEquals("StandardDeviation", Scaling.SD.getName());
        assertEquals(Scaling.SD.compute(VarDouble.seq(10)), Variance.of(VarDouble.seq(10)).biasedSdValue());
        assertEquals(Scaling.SD.compute(VarDouble.seq(100)), Variance.of(VarDouble.seq(100)).biasedSdValue());

        assertEquals("Normalization", Scaling.NORM.getName());
        assertEquals(Scaling.NORM.compute(VarDouble.seq(10)), Math.sqrt(VarDouble.seq(10).op().apply(x -> x * x).op().nansum()));
        assertEquals(Scaling.NORM.compute(VarDouble.seq(100)), Math.sqrt(VarDouble.seq(100).op().apply(x -> x * x).op().nansum()));
    }
}
