package rapaio.data.filter;

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.VType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class FQuantileDiscreteTest {

    @Test
    void testDouble() {
        Frame src = FFilterTestUtil.allDoubleNominal(100, 2, 2);

        Frame q1 = src.fapply(FQuantileDiscrete.on(VRange.all(), 0.5));
        Frame q2 = src.fapply(FQuantileDiscrete.split(VRange.onlyTypes(VType.DOUBLE), 2).newInstance());

        assertTrue(q1.deepEquals(q2));
    }

    @Test
    void testInvalidSplit() {
        var ex = assertThrows(IllegalArgumentException.class, () -> FFilterTestUtil.allDoubleNominal(100, 2, 2).fapply(FQuantileDiscrete.split(VRange.all(), 1)));
        assertEquals("Frame quantile discrete filter allows only splits greater than 1.", ex.getMessage());
    }

    @Test
    void testInvalidProbabilities() {
        var ex = assertThrows(IllegalArgumentException.class, () -> FFilterTestUtil.allDoubleNominal(100, 2, 2).fapply(FQuantileDiscrete.on(VRange.all())));
        assertEquals("Frame quantile discrete filter requires at least one probability.", ex.getMessage());
    }
}
