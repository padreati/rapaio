package rapaio.data.filter.var;

import org.junit.Test;
import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Gamma;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/18.
 */
public class VStandardizeTest {

    @Test
    public void testDouble() {

        Distribution d = Gamma.of(0.5, 2);
        VarDouble x = VarDouble.from(1000, d::sampleNext);

        double mean = Mean.of(x).value();
        double sd = Variance.of(x).sdValue();

        Var m1 = x.solidCopy().fapply(VStandardize.filter());
        Var m2 = x.solidCopy().fapply(VStandardize.filter(mean));
        Var m3 = x.solidCopy().fapply(VStandardize.filter(mean, sd));

        assertTrue(m1.deepEquals(m2));
        assertTrue(m2.deepEquals(m3));
    }

    @Test
    public void testConstant() {
        VarDouble x = VarDouble.fill(100, 10);
        Var sd = x.solidCopy().fapply(VStandardize.filter());
        assertTrue(x.deepEquals(sd));
    }

    @Test
    public void testNonNumeric() {
        VarNominal x = VarNominal.copy("a", "b");
        Var sd = x.fapply(VStandardize.filter());
        assertTrue(x.deepEquals(sd));
    }
}
