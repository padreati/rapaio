package rapaio.data.filter.frame;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.data.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class FQuantileDiscreteTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testDouble() {
        Frame src = FFilterTestUtil.allDoubleNominal(100, 2, 2);

        Frame q1 = src.fapply(FQuantileDiscrete.on(VRange.all(), 0.5));
        Frame q2 = src.fapply(FQuantileDiscrete.split(VRange.onlyTypes(VType.DOUBLE), 2).newInstance());

        assertTrue(q1.deepEquals(q2));
    }

    @Test
    public void testInvalidSplit() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Frame quantile discrete filter allows only splits greater than 1.");
        FFilterTestUtil.allDoubleNominal(100, 2, 2).fapply(FQuantileDiscrete.split(VRange.all(), 1));
    }

    @Test
    public void testInvalidProbabilities() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Frame quantile discrete filter requires at least one probability.");
        FFilterTestUtil.allDoubleNominal(100, 2, 2).fapply(FQuantileDiscrete.on(VRange.all()));
    }
}
