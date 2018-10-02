package rapaio.data.filter.var;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static org.junit.Assert.assertEquals;
import static rapaio.core.CoreTools.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VTransformPowerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testTransformPositiveLambda() {
        RandomSource.setSeed(1);

        Var x = distNormal().sample(1000).stream().mapToDouble(s -> Math.pow(s.getDouble(), 2)).boxed().collect(VarDouble.collector());
        Var y = x.solidCopy().fapply(VTransformPower.with(0.2));

        variance(x).printSummary();
        assertEquals(1.459663, variance(x).sdValue(), 1e-6);
        variance(y).printSummary();
        assertEquals(0.5788231, variance(y).sdValue(), 1e-6);

        corrPearson(x, y).printSummary();
        assertEquals(0.8001133350403581, corrPearson(x, y).matrix().get(0,1), 1e-6);
        corrSpearman(x, y).printSummary();
        assertEquals(1, corrSpearman(x, y).matrix().get(0,1), 1e-6);
    }

    @Test
    public void testTransformZeroLambda() {
        RandomSource.setSeed(1);

        Var x = distNormal().sample(1000).stream().mapToDouble(s -> Math.pow(s.getDouble(), 2)).boxed().collect(VarDouble.collector());
        Var y = x.solidCopy().fapply(VTransformPower.with(0));

        variance(x).printSummary();
        assertEquals(1.459663, variance(x).sdValue(), 1e-6);
        variance(y).printSummary();
        assertEquals(0.6713084463366682, variance(y).sdValue(), 1e-6);

        corrPearson(x, y).printSummary();
        assertEquals(0.6406002413733152, corrPearson(x, y).matrix().get(0,1), 1e-6);
        corrSpearman(x, y).printSummary();
        assertEquals(1, corrSpearman(x, y).matrix().get(0,1), 1e-6);
    }

    @Test
    public void testNegativeValues() {
        RandomSource.setSeed(120);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The source variable ? contains negative values, geometric mean cannot be computed");
        VarDouble.from(100, row -> new Normal().sampleNext()).fapply(VTransformPower.with(10)).printLines();
    }

}
