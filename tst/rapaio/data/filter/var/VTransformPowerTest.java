package rapaio.data.filter.var;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.core.RandomSource;
import rapaio.core.correlation.CorrPearson;
import rapaio.core.correlation.CorrSpearman;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VTransformPowerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testTransformPositiveLambda() {
        RandomSource.setSeed(1);

        Var x = Normal.std().sample(1000).stream().mapToDouble(s -> Math.pow(s.getDouble(), 2)).boxed().collect(VarDouble.collector());
        Var y = x.solidCopy().fapply(VTransformPower.with(0.2));

        assertEquals(1.459663, Variance.of(x).sdValue(), 1e-6);
        assertEquals(0.5788231, Variance.of(y).sdValue(), 1e-6);

        assertEquals(0.8001133350403581, CorrPearson.of(x, y).matrix().get(0,1), 1e-6);
        assertEquals(1, CorrSpearman.of(x, y).matrix().get(0,1), 1e-6);
    }

    @Test
    public void testTransformZeroLambda() {
        RandomSource.setSeed(1);

        Var x = Normal.std().sample(1000).stream().mapToDouble(s -> Math.pow(s.getDouble(), 2)).boxed().collect(VarDouble.collector());
        Var y = x.solidCopy().fapply(VTransformPower.with(0));

        assertEquals(1.459663, Variance.of(x).sdValue(), 1e-6);
        assertEquals(0.6713084463366682, Variance.of(y).sdValue(), 1e-6);

        assertEquals(0.6406002413733152, CorrPearson.of(x, y).matrix().get(0,1), 1e-6);
        assertEquals(1, CorrSpearman.of(x, y).matrix().get(0,1), 1e-6);
    }

    @Test
    public void testNegativeValues() {
        RandomSource.setSeed(120);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The source variable ? contains negative values, geometric mean cannot be computed");
        VarDouble.from(100, row -> Normal.std().sampleNext()).fapply(VTransformPower.with(10)).printContent();
    }

}
